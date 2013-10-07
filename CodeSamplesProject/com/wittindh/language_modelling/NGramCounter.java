package com.wittindh.language_modelling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Class NGramCounter.
 * 
 * @author David Witting
 */
public final class NGramCounter {

	/**
	 * private default constructor.
	 */
	private NGramCounter() {
	}

	/**
	 * constant newline character.
	 */
	public static final String NL = System.getProperty("line.separator");

	/**
	 * The main method.
	 * 
	 * @param args
	 *            arguments representing the filepath for the data to be read,
	 *            the output file, and the maximum number of tokens in an n-gram
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(final String[] args) throws IOException {
		File trainingData = new File(args[0]);
		File nGramCountFile = new File(args[1]);
		int nValue = Integer.parseInt(args[2]);

		List<TreeMap<String, AtomicInteger>> nGramMapList = new LinkedList<TreeMap<String, AtomicInteger>>();
		for (int i = 0; i < nValue; i++) {
			nGramMapList.add(new TreeMap<String, AtomicInteger>());
		}

		Scanner trainingScanner = new Scanner(trainingData);
		FileWriter output = new FileWriter(nGramCountFile);

		while (trainingScanner.hasNextLine()) {
			countSentenceNGrams(trainingScanner.nextLine(), nGramMapList);
		}
		trainingScanner.close();
		for (TreeMap<String, AtomicInteger> nGramMap : nGramMapList) {
			LinkedList<Entry<String, AtomicInteger>> nGramEntries = createSortedEntryList(nGramMap);
			for (Entry<String, AtomicInteger> entry : nGramEntries) {
				output.write(entry.getValue() + "\t" + entry.getKey() + NL);
			}
		}
		output.close();
	}

	/**
	 * Creates a list of the entries of a map and sorts them by their value.
	 * 
	 * @param nGramMap
	 *            the map whose entries are to be sorted
	 * 
	 * @return the entry list sorted by the value of each entry
	 */
	public static LinkedList<Entry<String, AtomicInteger>> createSortedEntryList(
			final Map<String, AtomicInteger> nGramMap) {
		LinkedList<Entry<String, AtomicInteger>> nGramEntries = new LinkedList<Entry<String, AtomicInteger>>(
				nGramMap.entrySet());
		Collections.sort(nGramEntries, new Comparator<Entry<String, AtomicInteger>>() {
			@Override
			public int compare(final Entry<String, AtomicInteger> o1, final Entry<String, AtomicInteger> o2) {
				return (o2.getValue().get() - o1.getValue().get());
			}
		});
		return nGramEntries;
	}

	/**
	 * Counts all the nGrams for a given sentence, incrementing the values in
	 * the nGramMapList.
	 * 
	 * @param line
	 *            A line of text containing one sentence with tokens separated
	 *            by white space
	 * 
	 * @param nGramMapList
	 *            the list of maps containing the associations of nGrams to
	 *            their counts, ordered from unigrams to nGrams
	 */
	private static void countSentenceNGrams(final String line, final List<TreeMap<String, AtomicInteger>> nGramMapList) {
		Scanner lineScanner = new Scanner(line);
		// processes each token, splitting on white space
		LinkedList<String> tokenList = new LinkedList<String>();
		enqueueToken(tokenList, "<s>", nGramMapList.size());
		incrementNGramCounts(nGramMapList, tokenList);
		while (lineScanner.hasNext()) {
			enqueueToken(tokenList, lineScanner.next(), nGramMapList.size());
			incrementNGramCounts(nGramMapList, tokenList);
		}
		enqueueToken(tokenList, "</s>", nGramMapList.size());
		incrementNGramCounts(nGramMapList, tokenList);
		lineScanner.close();
	}

	/**
	 * Advances counts for all nGrams (from uni- to n-grams) for tokens in the
	 * current token list.
	 * 
	 * @param nGramMapList
	 *            the list of maps containing the associations of nGrams to
	 *            their counts, ordered from unigrams to nGrams
	 * 
	 * @param tokenList
	 *            the list of tokens including the n most recent tokens from
	 *            most recent to least recent
	 */
	private static void incrementNGramCounts(final List<TreeMap<String, AtomicInteger>> nGramMapList,
			final LinkedList<String> tokenList) {
		String ngram = tokenList.getFirst();
		increment(nGramMapList.get(0), ngram);
		for (int n = 1; n < tokenList.size(); n++) {
			ngram = tokenList.get(n) + " " + ngram;
			increment(nGramMapList.get(n), ngram);
		}
	}

	/**
	 * Adds the next token to the beginning of a list and removes the element at
	 * the end if the list exceeds a certain capacity.
	 * 
	 * @param tokenList
	 *            the list of tokens ordered from most recent to least recent
	 * @param token
	 *            the token to be enqueued
	 * @param capacity
	 *            the maximum capacity
	 */
	private static void enqueueToken(final LinkedList<String> tokenList, final String token, final int capacity) {
		tokenList.addFirst(token);
		if (tokenList.size() > capacity) {
			tokenList.removeLast();
		}
	}

	/**
	 * Advances count of occurrences of a particular nGram by one.
	 * 
	 * @param map
	 *            the map containing the associations of nGrams to their counts
	 * @param nGram
	 *            the word whose count is to be increased.
	 */
	public static void increment(final Map<String, AtomicInteger> map, final String nGram) {
		if (map.containsKey(nGram)) {
			map.get(nGram).incrementAndGet();
		} else {
			map.put(nGram, new AtomicInteger(1));
		}
	}
}