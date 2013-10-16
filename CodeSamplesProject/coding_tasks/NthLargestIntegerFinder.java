package coding_tasks;

import java.util.LinkedList;
import java.util.List;

/**
 * The Class NthLargestIntegerFinder.
 * 
 * @author David Witting
 */
public final class NthLargestIntegerFinder {

	/**
	 * Prevents the instantiation of a new nth largest integer finder.
	 */
	private NthLargestIntegerFinder() {
	}

	/**
	 * Find 5th largest integer.
	 * 
	 * @param integers
	 *            the array of integers
	 * @return the fifth largest
	 */
	public static int find5thLargestInteger(final int[] integers) {
		return findNthLargestInteger(5, integers);
	}

	/**
	 * Find nth largest integer.
	 * 
	 * @param n
	 *            the value of n
	 * @param integers
	 *            the array of integers in which to find the nth largest
	 * @return the int the smallest value in the buffer containing the n largest
	 *         values
	 */
	public static int findNthLargestInteger(final int n, final int[] integers) {
		if (n < 1) {
			throw new IndexOutOfBoundsException();
		}
		if (integers.length < n) {
			throw new IndexOutOfBoundsException();
		}
		List<Integer> buffer = new LinkedList<Integer>();
		for (int i = 0; i < integers.length; i++) {
			addToBuffer(integers[i], buffer, n);
		}
		return buffer.get(0);
	}

	/**
	 * Adds the to buffer.
	 * 
	 * @param integer
	 *            the integer to be added to the buffer
	 * @param buffer
	 *            the list of the largest values encountered in the array
	 * @param maxSize
	 *            the maximum number of values to store in the buffer
	 */
	private static void addToBuffer(final int integer, final List<Integer> buffer, final int maxSize) {
		int size = buffer.size();
		if (size == 0) {
			buffer.add(integer);
		} else {
			for (int i = 0; i < size; i++) {
				if (integer < buffer.get(i)) {
					buffer.add(i, integer);
					break;
				} else if (i == size - 1) {
					buffer.add(i + 1, integer);
					break;
				}
			}
		}
		if (buffer.size() > maxSize) {
			buffer.remove(0);
		}
	}
}