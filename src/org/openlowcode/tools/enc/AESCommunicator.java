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

import java.io.ByteArrayOutputStream;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.10
 *
 */
public class AESCommunicator {
	private static Logger logger = Logger.getLogger(AESCommunicator.class.getName());
	@SuppressWarnings("unused")
	private SecretKey secretkey;
	private Cipher encryptaescipher;
	private Cipher decryptaescipher;
	private Deflater deflater;
	private Inflater inflater;

	/**
	 * @param secretkey
	 * @throws Exception
	 */
	public AESCommunicator(SecretKey secretkey) throws Exception {
		this.secretkey = secretkey;
		encryptaescipher = Cipher.getInstance("AES");
		encryptaescipher.init(Cipher.ENCRYPT_MODE, secretkey);
		decryptaescipher = Cipher.getInstance("AES");
		decryptaescipher.init(Cipher.DECRYPT_MODE, secretkey);
		deflater = new Deflater();
		inflater = new Inflater();

	}

	/**
	 * @param message
	 * @return
	 * @throws Exception
	 */
	public byte[] zipandencrypt(String message) throws Exception {
		try {
			byte[] messagebinary = message.getBytes("UTF-8");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			deflater.setInput(messagebinary);
			deflater.finish();
			byte[] buffer = new byte[4000];
			while (!deflater.finished()) {
				int chars = deflater.deflate(buffer);
				baos.write(buffer, 0, chars);
			}
			deflater.reset();
			return encryptaescipher.doFinal(baos.toByteArray());
		} catch (Exception e) {
			logger.severe(" Exception in  ZipAndEncrypt " + e.getClass().getName() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe("     * " + e.getStackTrace()[i]);
			deflater.reset();
			throw new RuntimeException("Error in ZipAndEncrypt " + e.getMessage());
		}
	}

	/**
	 * @param encryptedmessage
	 * @return
	 * @throws Exception
	 */
	public String decryptandunzip(byte[] encryptedmessage) throws Exception {
		try {
			byte[] decryptedzipcontent = decryptaescipher.doFinal(encryptedmessage);
			inflater.setInput(decryptedzipcontent);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4000];
			while (!inflater.finished()) {
				int chars = inflater.inflate(buffer);
				baos.write(buffer, 0, chars);
			}
			inflater.reset();
			return baos.toString("UTF-8");
		} catch (Exception e) {
			logger.severe(" Exception in  DecryptAndUnzip " + e.getClass().getName() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe("     * " + e.getStackTrace()[i]);
			deflater.reset();
			throw new RuntimeException("Error in DecryptAndUnzip " + e.getMessage());
		}
	}

	public static void main(String args[]) {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(256);
			SecretKey secretKey = keyGen.generateKey();
			AESCommunicator communicator = new AESCommunicator(secretKey);
			for (int a = 0; a < 3; a++) {
				System.err.println("       * * * run " + a);
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < 20000; i++)
					buffer.append("-" + i);
				System.err.println("Encrypting string of size " + buffer.length());
				byte[] encodedstuff = communicator.zipandencrypt(buffer.toString());
				System.err.println("Encrypted data size " + encodedstuff.length);
				String decodedstring = communicator.decryptandunzip(encodedstuff);
				System.err.println("Decrypted string size " + decodedstring.length() + " starts with "
						+ decodedstring.substring(0, 100));
			}
		} catch (Exception e) {
			System.err.println("----------------------- Exception ------------------");
			System.err.println("   " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				System.err.println("          * " + e.getStackTrace()[i]);
			}
		}
	}

}
