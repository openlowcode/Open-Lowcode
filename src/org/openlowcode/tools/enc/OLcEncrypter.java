/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.enc;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Encrypting mechanism for storing one-way information  in the database. Typically used for passwords. 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcEncrypter {

	private static OLcEncrypter singleton;
	

	/**
	 * gets the singleton encrypter. Encoding and decoding is thread-safe
	 * 
	 * @return the singleton encrypter
	 */
	public static OLcEncrypter getEncrypter() {
		if (singleton == null) 
			singleton = new OLcEncrypter();
		return singleton;
	}

	/**
	 * Encrypts string one way (cannot be decrypted but the encoded text will be the
	 * same each time, allowing to test if the provided input is equal to the stored
	 * value in DB). This is typically used to encrypt passwords on the database
	 * 
	 * @param origintext original text
	 * @return a one-way encoded text
	 */
	public String encryptStringOneWay(String origintext) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA algorithm not supported");
		}

		md.update(origintext.getBytes(StandardCharsets.UTF_8));
		byte raw[] = md.digest();
		String returnvalue = Base64.getEncoder().encodeToString(raw);
		return returnvalue;
	}


	private OLcEncrypter() {
	}

	public static void main(String args[]) {
		try {
			OLcEncrypter encrypter = OLcEncrypter.getEncrypter();
			System.out.println("One way encryption of string papagayolololololomlsdkjfmsdflj lmdfj smlfd slmfdj = "
					+ encrypter.encryptStringOneWay("papagayolololololomlsdkjfmsdflj lmdfj smlfd slmfdj "));
		} catch (Exception e) {
			System.err.println("Exception in encoding test");
			e.printStackTrace();
		}
	}
}
