/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode;

/**
 * tools used to manage version of the framework for auto-update purposes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcVersionTools {

	/**
	 * compare two versions that in the shape of a succession of numbers (e.g. 1.2.3
	 * vs 2.1.23.5
	 * 
	 * @param firstversion  first version
	 * @param secondversion second version
	 * @return a positive number if the first version is higher
	 */
	public static int CompareTwoVersions(String firstversion, String secondversion) {
		int[] firstversionarray = OLcVersionTools.parseVersionNumber(firstversion, '.');
		int[] secondversionarray = OLcVersionTools.parseVersionNumber(secondversion, '.');
		return compareTwoIntArrays(firstversionarray, secondversionarray);
	}

	/**
	 * comparison of two integer arrays that are used as numbering scheme. Numbers
	 * are compared in the order (first the two first numbers in each array, and
	 * only if equal, the next number is shown)
	 * 
	 * @param firstarray  an array of integer
	 * @param secondarray a second array of integers
	 * @return a positive number if the first array is bigger, zero if two arrays
	 *         are equal
	 */
	public static int compareTwoIntArrays(int[] firstarray, int[] secondarray) {
		// manage null cases
		if (firstarray == null) {
			if (secondarray == null)
				return 0;
			if (secondarray != null)
				return -1;
		}
		if (secondarray == null)
			return 1;

		int minsize = firstarray.length;
		if (secondarray.length < minsize)
			minsize = secondarray.length;

		boolean firsthasmore = false;
		if (minsize < firstarray.length)
			firsthasmore = true;
		boolean secondhasmore = false;
		if (minsize < secondarray.length)
			secondhasmore = true;

		for (int i = 0; i < minsize; i++) {
			int firstvalue = firstarray[i];
			int secondvalue = secondarray[i];
			if (firstvalue > secondvalue)
				return 1;
			if (firstvalue < secondvalue)
				return -1;
		}
		if (firsthasmore)
			return 1;
		if (secondhasmore)
			return -1;
		return 0;
	}

	/**
	 * parses a String made of a version number of type '0.23.140' . In the example
	 * given, it will provide a 3 elements array with numbers '0', '23', and '140'.
	 * 
	 * @param separator typically '.', the separator between the integer numbers
	 * @return an array of integer with the parsed numbers
	 */
	public static int[] parseVersionNumber(String versionnumber, char separator) {
		if (versionnumber == null)
			return new int[0];
		if (versionnumber.trim().length() == 0)
			return new int[0];
		String[] versions = versionnumber.trim().split("\\.");

		int[] parsednumbers = new int[versions.length];
		for (int i = 0; i < versions.length; i++)
			parsednumbers[i] = Integer.parseInt(versions[i]);
		return parsednumbers;
	}

}
