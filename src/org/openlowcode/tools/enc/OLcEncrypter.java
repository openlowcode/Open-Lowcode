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
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import org.openlowcode.tools.encrypt.Encrypter;

/**
 * The encrypting mechanism for communication between client and server. 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcEncrypter
		implements
		Encrypter {
	private static Logger logger = Logger.getLogger(OLcEncrypter.class.getName());
	private String DES_ENCRYPTION_SCHEME = "DES";
	private byte[] bytekey = OLcEncrypterString.getEncryptionString().getBytes(StandardCharsets.UTF_8);
	private DESKeySpec myKeySpec;
	private SecretKeyFactory mySecretKeyFactory;
	private Cipher cipher;
	private static OLcEncrypter singleton;
	private SecretKey key;

	

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

	@Override
	public String encryptStringTwoWays(String origintext) {
		if (origintext == null)
			origintext = "";
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] plainText = origintext.getBytes(StandardCharsets.UTF_8);
			byte[] encryptedText = cipher.doFinal(plainText);

			return Base64.getEncoder().encodeToString(encryptedText);
		} catch (Exception e) {
			throw new RuntimeException("error in two-ways encryption, originalexception = " + e.getMessage());
		}

	}

	@Override
	public String decryptStringTwoWays(String encryptedtext) {
		try {

			cipher.init(Cipher.DECRYPT_MODE, key);
			byte[] encryptedbytes = Base64.getDecoder().decode(encryptedtext);

			byte[] plainText = cipher.doFinal(encryptedbytes);
			return new String(plainText);

		} catch (Exception e) {
			throw new RuntimeException("error in two-ways decryption, originalexception = " + e.getMessage());
		}

	}

	private OLcEncrypter() {
		try {
			myKeySpec = new DESKeySpec(bytekey);
			mySecretKeyFactory = SecretKeyFactory.getInstance(DES_ENCRYPTION_SCHEME);
			cipher = Cipher.getInstance(DES_ENCRYPTION_SCHEME);
			key = mySecretKeyFactory.generateSecret(myKeySpec);
		} catch (Exception e) {
			logger.severe("Exception in starting encrypter " + e.getClass().getName() + " - " + e.getMessage());
			throw new RuntimeException("error in initializing encryption, origin exception = " + e.getMessage());
		}
	}

	public static void main(String args[]) {
		try {
			OLcEncrypter encrypter = OLcEncrypter.getEncrypter();
			System.out.println("One way encryption of string papagayolololololomlsdkjfmsdflj lmdfj smlfd slmfdj = "
					+ encrypter.encryptStringOneWay("papagayolololololomlsdkjfmsdflj lmdfj smlfd slmfdj "));
			String encodedtwoways = encrypter.encryptStringTwoWays("salsifi");
			System.out.println("Two way encryption of string salsifi = " + encodedtwoways);
			System.out.println("decoding of salsifi = " + encrypter.decryptStringTwoWays(encodedtwoways));
		} catch (Exception e) {
			System.err.println("Exception in encoding test");
			e.printStackTrace();
		}
	}
}
