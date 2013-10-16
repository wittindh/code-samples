package coding_tasks;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class WordOccurrenceCounter.
 * 
 * @author David Witting
 */
public final class WordOccurrenceCounter {

	/**
	 * Prevents the instantiation of the WordOccurrenceCounter.
	 */
	private WordOccurrenceCounter() {
	}

	/**
	 * Count word occurrences.
	 * 
	 * @param word
	 *            the word
	 * @param paragraph
	 *            the paragraph in which to search for occurrences of the word
	 * @return count the number of occurrences of the word
	 */
	public static int countWordOccurrences(final String word, final String paragraph) {
		Scanner scanner = new Scanner(paragraph);
		int count = 0;
		Pattern p = Pattern.compile("\\W*" + word.toLowerCase() + "\\W*");
		while (scanner.hasNext()) {
			String token = scanner.next().toLowerCase();
			Matcher m = p.matcher(token);
			if (m.matches()) {
				count++;
			}
		}
		return count;
	}
}
