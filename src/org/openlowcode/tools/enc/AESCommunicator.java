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
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.SFile;

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
			if (message==null) return null;
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
			if (encryptedmessage==null) return null;
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

	/**
	 * perform an handshake with the server to get and send back to the server an
	 * AES Key
	 * 
	 * @param reader message reader connected with the server
	 * @param writer message writer connected with the server
	 * @return the AES communicator allowing encryption for communication with the
	 *         server
	 * @throws Exception if any communication error is encountered
	 */
	public static AESCommunicator performServerHandshake(MessageSimpleReader reader, MessageBufferedWriter writer)
			throws Exception {
		reader.returnNextMessageStart();
		reader.returnNextStartStructure("RSAKEY");
		byte[] rsapublickey = reader.returnNextLargeBinary("PUBLICKEY").getContent();
		reader.returnNextEndStructure("RSAKEY");
		reader.returnNextEndMessage();

		// ----------------------Generate AES Key --------------------------------------
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256);
		SecretKey secretKey = keyGen.generateKey();
		byte[] aeskey = secretKey.getEncoded();

		// ----------- keep AES keys ----
		AESCommunicator aescommunicator = new AESCommunicator(secretKey);
		// --- Encrypt AES key with RSA key ----

		KeyFactory kf = KeyFactory.getInstance("RSA");
		PublicKey rsapublickeyasobject = kf.generatePublic(new X509EncodedKeySpec(rsapublickey));
		Cipher encryptrsacipher = Cipher.getInstance("RSA");
		encryptrsacipher.init(Cipher.ENCRYPT_MODE, rsapublickeyasobject);
		byte[] aeskeyencoded = encryptrsacipher.doFinal(aeskey);
		writer.startNewMessage();
		writer.startStructure("SESAESKEY");
		writer.addLongBinaryField("AESKEY", new SFile("Aeskey", aeskeyencoded));
		writer.endStructure("SESAESKEY");
		writer.endMessage();
		return aescommunicator;
	}
}
