/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.util.ArrayList;
import java.util.List;

/**
 * An utility class that encodes in a single string several non-null string values in a simple and compact way. Encoding principles:<ul>
 * <li>basic separator is '|' (pipe) </li>
 * <li>if a value includes '|', an error is thrown</li>
 * </ul>
 * As stated above, this simple algorithm will not work for null values inside the array (else, you should use CSV instead)
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class MultiStringEncoding {

	/**
	 * @param encodedmultistring a string encoded with this class
	 * @return the parsed array of strings
	 */
	public static String[] parse(String encodedmultistring) {
		if (encodedmultistring==null) return new String[0];
		if (encodedmultistring.length()==0) return new String[0];
		return encodedmultistring.split("\\|");
	}
	/**
	 * @param multistringtoencode an array of strings to encode
	 * @return the encoded String
	 */
	public static String encode(String[] multistringtoencode) {
		if (multistringtoencode==null) return "";
		if (multistringtoencode.length==0) return "";
		StringBuffer result = new StringBuffer();
		for (int i=0;i<multistringtoencode.length;i++) {
			String value = multistringtoencode[i];
			if (value.indexOf('|')!=-1) throw new RuntimeException("Cannot encode a String with a '|' character inside");
			if (i>0) result.append('|');
			result.append(value);
		}
		return result.toString();
	}
	
	public static String encode(List<String> multistringtoencode) {
		if (multistringtoencode==null) return "";
		if (multistringtoencode.size()==0) return "";
		StringBuffer result = new StringBuffer();
		for (int i=0;i<multistringtoencode.size();i++) {
			String value = multistringtoencode.get(i);
			if (value.indexOf('|')!=-1) throw new RuntimeException("Cannot encode a String with a '|' character inside");
			if (i>0) result.append('|');
			result.append(value);
		}
		return result.toString();
	}
	
	public static void main(String[] args) {
		System.err.println(encode(new String[] {"aaa","bbb","cc","dazeaze","eee"}));
		String[] parsing = parse("aaaa|bb|cc||dd|ee||||ff|ggg||");
		for (int i=0;i<parsing.length;i++) {
			System.err.println(" - "+parsing[i]);
		}
		System.err.println("Expecting exception now");
		System.err.println(encode(new String[] {"abc","dd|ee"}));
	}
}
