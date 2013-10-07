package com.wittindh.language_modelling;

/**
 * @author David Witting
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

// TODO: Auto-generated Javadoc
/**
 * The Class LanguageModelBuilder.
 */
public final class LanguageModelBuilder {

	/**
	 * Instantiates a new language model builder.
	 */
	private LanguageModelBuilder() {
	}

	/** The Constant NL. */
	public static final String NL = System.getProperty("line.separator");

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(final String[] args) throws IOException {
		File nGramCountFile = new File(args[0]);
		File languageModelFile = new File(args[1]);
		File vocabFile = new File(args[2]);
		double delta = Double.parseDouble(args[3]);
		FileWriter output = new FileWriter(languageModelFile);

		DecimalFormat df = new DecimalFormat("#.#####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		createAddDeltaSmoothedLanguageModel(nGramCountFile, df, output, vocabFile, delta);
	}

	/**
	 * Creates the unsmoothed language model.
	 * 
	 * @param nGramCountFile
	 *            the n gram count file
	 * @param df
	 *            the df
	 * @param output
	 *            the output
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected static void createUnsmoothedLanguageModel(final File nGramCountFile, final DecimalFormat df,
			final FileWriter output) throws IOException {
		createAddDeltaSmoothedLanguageModel(nGramCountFile, df, output, null, 0);
	}

	/**
	 * Creates the add delta smoothed language model.
	 * 
	 * @param nGramCountFile
	 *            the n gram count file
	 * @param df
	 *            the decimalformat
	 * @param output
	 *            the output f
	 * @param vocabFile
	 *            the vocabulary file
	 * @param delta
	 *            the value to add to each nGram
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void createAddDeltaSmoothedLanguageModel(final File nGramCountFile, final DecimalFormat df,
			final FileWriter output, final File vocabFile, final double delta) throws IOException {
		List<Map<String, AtomicInteger>> nGramMapList = new LinkedList<Map<String, AtomicInteger>>();
		List<AtomicInteger> totalNGramCounts = new LinkedList<AtomicInteger>();
		nGramMapList.add(new TreeMap<String, AtomicInteger>());
		totalNGramCounts.add(new AtomicInteger(0));
		readCountsFromFile(nGramCountFile, totalNGramCounts, nGramMapList);
		if (vocabFile != null) {
			readVocabularyFromFile(vocabFile, nGramMapList);
		}
		calculateProbabilities(nGramMapList, totalNGramCounts, df, output, delta);
	}

	/**
	 * Calculate probabilities.
	 * 
	 * @param nGramMapList
	 *            the n gram map list
	 * @param totalNGramCounts
	 *            the total n gram counts
	 * @param df
	 *            the df
	 * @param output
	 *            the output
	 * @param delta
	 *            the delta
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private static void calculateProbabilities(final List<? extends Map<String, AtomicInteger>> nGramMapList,
			final List<AtomicInteger> totalNGramCounts, final DecimalFormat df, final FileWriter output,
			final double delta) throws IOException {
		// output the data
		output.write("\\data\\" + NL);
		for (int i = 0; i < nGramMapList.size(); i++) {
			output.write((i + 1) + "-grams: unique=" + nGramMapList.get(i).size() + "; total="
					+ totalNGramCounts.get(i) + NL);
		}
		output.write(NL);
		for (int i = 0; i < nGramMapList.size(); i++) {
			output.write("\\" + (i + 1) + "-grams:" + NL);
			Map<String, AtomicInteger> nGramMap = nGramMapList.get(i);
			LinkedList<Entry<String, AtomicInteger>> nGramEntryList = NGramCounter.createSortedEntryList(nGramMap);
			double denominatorModifier = nGramMapList.get(i).size() * delta;
			for (Entry<String, AtomicInteger> entry : nGramEntryList) {
				double nGramCountPlusDelta = entry.getValue().doubleValue() + delta;
				double probability;
				if (i == 0) {
					double totalTokens = totalNGramCounts.get(0).doubleValue();
					probability = nGramCountPlusDelta / (totalTokens + denominatorModifier);
				} else {
					String[] tokenizedKey = entry.getKey().split("\\s+");
					String nMinusOneGram = tokenizedKey[0];
					for (int j = 1; j < tokenizedKey.length - 1; j++) {
						nMinusOneGram = nMinusOneGram + " " + tokenizedKey[j];
					}
					double nMinusOneGramCount = nGramMapList.get(i - 1).get(nMinusOneGram).doubleValue();
					probability = nGramCountPlusDelta / (nMinusOneGramCount + denominatorModifier);
				}
				String prob = df.format(probability);
				double logprobability = Math.log10(probability) / Math.log10(2);
				String logprob = df.format(logprobability);
				output.write(entry.getValue() + "\t" + prob + "\t" + logprob + "\t" + entry.getKey() + NL);
			}
			output.write(NL);
		}
		output.write("\\end\\");
		output.close();
	}

	/**
	 * Read vocabulary from file.
	 * 
	 * @param vocabFile
	 *            the vocab file
	 * @param nGramMapList
	 *            the n gram map list
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private static void readVocabularyFromFile(final File vocabFile,
			final List<? extends Map<String, AtomicInteger>> nGramMapList) throws FileNotFoundException {
		Scanner vocabFileScanner = new Scanner(vocabFile);
		Map<String, AtomicInteger> unigramMap = nGramMapList.get(0);
		while (vocabFileScanner.hasNextLine()) {
			Scanner lineScanner = new Scanner(vocabFileScanner.nextLine());
			if (lineScanner.hasNext()) {
				String key = lineScanner.next();
				if (!unigramMap.containsKey(key)) {
					unigramMap.put(key, new AtomicInteger(0));
				}
			}
			lineScanner.close();
		}
		vocabFileScanner.close();
		for (int i = 1; i < nGramMapList.size(); i++) {
			for (String unigram : unigramMap.keySet()) {
				for (String ngram : nGramMapList.get(i - 1).keySet()) {
					String key = ngram + " " + unigram;
					if (!nGramMapList.get(i).containsKey(key)) {
						nGramMapList.get(i).put(key, new AtomicInteger(0));
					}
				}
			}
		}
	}

	/**
	 * Read counts from file.
	 * 
	 * @param nGramCountFile
	 *            the n gram count file
	 * @param totalNGramCounts
	 *            the total n gram counts
	 * @param nGramMapList
	 *            the n gram map list
	 * @throws FileNotFoundException
	 *             the file not found exception
	 */
	private static void readCountsFromFile(final File nGramCountFile, final List<AtomicInteger> totalNGramCounts,
			final List<Map<String, AtomicInteger>> nGramMapList) throws FileNotFoundException {
		Scanner ngramcountscanner = new Scanner(nGramCountFile);
		while (ngramcountscanner.hasNextLine()) {
			Scanner linescanner = new Scanner(ngramcountscanner.nextLine());
			if (linescanner.hasNext()) {
				int count = Integer.parseInt(linescanner.next());
				int nvalue = 0;
				String nGram = "";
				while (linescanner.hasNext()) {
					nvalue++;
					if (nvalue == 1) {
						nGram = linescanner.next();
					} else {
						nGram = nGram + " " + linescanner.next();
					}
				}
				while (nGramMapList.size() < nvalue) {
					nGramMapList.add(new TreeMap<String, AtomicInteger>());
				}
				while (totalNGramCounts.size() < nvalue) {
					totalNGramCounts.add(new AtomicInteger(0));
				}
				nGramMapList.get(nvalue - 1).put(nGram, new AtomicInteger(count));
				totalNGramCounts.get(nvalue - 1).addAndGet(count);
			}
		}
		ngramcountscanner.close();
	}
}