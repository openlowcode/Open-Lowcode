/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * A simplified class to authenticate an OTP on a Radius server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleRadiusConnection {

	private static final int MAX_PACKET_SIZE = 4096;
	private static final int REQUEST_ACCESS_TYPE = 1;
	private static final int ACCESS_ACCEPT = 2;
	@SuppressWarnings("unused")
	private static final int ACCESS_REJECT = 3;
	private static final int USER_NAME = 1;
	private static final int USER_PASSWORD = 2;
	private static final int HEADER_LENGTH = 20;
	private static final int TIMEOUT = 5000;
	private static final int RETRY = 5;
	private String server;
	private int port;
	private String secret;

	/**
	 * Creates a simple connection with given server coordinates
	 * 
	 * @param server server URL or IP
	 * @param port port for radius server
	 * @param secret shared secret between client and server
	 */
	public SimpleRadiusConnection(String server, int port, String secret) {
		this.server = server;
		this.port = port;
		this.secret = secret;
	}

	/**
	 * @param userid user id for security check
	 * @param otp one time password
	 * @return true if the authentication was confirmed, false else
	 * @throws UnknownHostException if the server is not known
	 * @throws UnsupportedEncodingException if UTF-8 is not installed on the server
	 * @throws IOException if anything bad happens and connection could not be retried
	 */
	public boolean checkOTP(String userid, String otp)
			throws UnknownHostException, UnsupportedEncodingException, IOException {
		RequestSummary requestsummary = generateRequestPacket(userid, otp);
		DatagramPacket reply = new DatagramPacket(new byte[MAX_PACKET_SIZE], MAX_PACKET_SIZE);

		DatagramSocket socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT);
		for (int i = 0; i <= RETRY; i++) {
			try {
				socket.send(requestsummary.getPayload());
				socket.receive(reply);
			} catch (IOException e) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e1) {

				}
			}
		}
		socket.close();
		ByteArrayInputStream in = new ByteArrayInputStream(reply.getData());
		int responsetype = in.read() & 0x0ff;
		int serverid = in.read() & 0x0ff;
		int messagelength = (in.read() & 0x0ff) << 8 | (in.read() & 0x0ff);
		byte[] authenticator = new byte[16];
		byte[] attributepayload = new byte[messagelength - HEADER_LENGTH];
		in.read(authenticator);
		in.read(attributepayload);
		in.close();
		checkReplyAuthenticator(secret, responsetype, serverid, messagelength, attributepayload,
				requestsummary.getAuthenticator(), authenticator);
		if (responsetype == ACCESS_ACCEPT)
			return true;
		return false;

	}

	private RequestSummary generateRequestPacket(String userid, String otp) throws IOException {
		ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
		SecureRandom securerandom = new SecureRandom();
		byte[] authenticator = createRequestAuthenticator(secret, securerandom);
		DataOutputStream dataoutputstream = new DataOutputStream(outputstream);
		dataoutputstream.writeByte(REQUEST_ACCESS_TYPE);
		dataoutputstream.writeByte(0);
		byte[] encodedpassword = encodepassword(otp, authenticator);
		byte[] user = userid.getBytes("UTF-8");

		short packetlength = (short) (HEADER_LENGTH + (2 + user.length) + (2 + encodedpassword.length));

		dataoutputstream.writeShort(packetlength);
		dataoutputstream.write(authenticator);
		dataoutputstream.writeByte(USER_NAME);
		dataoutputstream.writeByte(2 + user.length);
		dataoutputstream.write(user);

		dataoutputstream.writeByte(USER_PASSWORD);
		dataoutputstream.writeByte(2 + encodedpassword.length);
		dataoutputstream.write(encodedpassword);

		dataoutputstream.flush();

		byte[] data = outputstream.toByteArray();
		InetAddress radiusserveraddress = InetAddress.getByName(server);
		DatagramPacket datagram = new DatagramPacket(data, data.length, radiusserveraddress, port);
		return new RequestSummary(datagram, authenticator);
	}

	private byte[] encodepassword(String otp, byte[] authenticator) throws UnsupportedEncodingException {

		byte[] otpbytes = otp.getBytes("UTF-8");
		byte[] secretbytes = secret.getBytes("UTF-8");
		byte[] otpbytesby16 = new byte[(((otpbytes.length / 16) + 1) * 16)];
		System.arraycopy(otpbytes, 0, otpbytesby16, 0, otpbytes.length);
		MessageDigest messagedigest5 = getMessageDigest();
		for (int i = 0; i < otpbytesby16.length; i += 16) {
			messagedigest5.reset();
			messagedigest5.update(secretbytes);
			if (i == 0)
				messagedigest5.update(authenticator);
			if (i > 0)
				messagedigest5.update(otpbytesby16, i - 16, 16);

			byte bn[] = messagedigest5.digest();
			for (int j = 0; j < 16; j++)
				otpbytesby16[i + j] = (byte) (bn[j] ^ otpbytesby16[i + j]);
		}
		return otpbytesby16;

	}

	private byte[] createRequestAuthenticator(String secret, SecureRandom securerandom)
			throws UnsupportedEncodingException {
		byte[] secretBytes = secret.getBytes("UTF-8");
		byte[] randomBytes = new byte[16];
		securerandom.nextBytes(randomBytes);

		MessageDigest messagedigest = getMessageDigest();
		messagedigest.reset();
		messagedigest.update(secretBytes);
		messagedigest.update(randomBytes);
		return messagedigest.digest();
	}

	private void checkReplyAuthenticator(
			String secret,
			int packettype,
			int packetid,
			int replylength,
			byte[] replyattributes,
			byte[] requestauthenticator,
			byte[] replyauthenticator) throws UnsupportedEncodingException {
		MessageDigest messagedigest = getMessageDigest();
		messagedigest.reset();
		messagedigest.update((byte) packettype);
		messagedigest.update((byte) packetid);
		messagedigest.update((byte) (replylength >> 8));
		messagedigest.update((byte) (replylength & 0x0ff));
		messagedigest.update(requestauthenticator, 0, requestauthenticator.length);
		messagedigest.update(replyattributes, 0, replyattributes.length);
		messagedigest.update(secret.getBytes("UTF-8"));
		byte[] digest = messagedigest.digest();
		for (int i = 0; i < 16; i++)
			if (digest[i] != replyauthenticator[i])
				throw new RuntimeException("Reply authenticator incorrect");
	}

	private MessageDigest getMessageDigest() {
		try {
			return MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException nsae) {
			throw new RuntimeException("md5 digest not available", nsae);
		}
	}

	private class RequestSummary {
		private DatagramPacket payload;
		private byte[] authenticator;

		public RequestSummary(DatagramPacket payload, byte[] authenticator) {
			super();
			this.payload = payload;
			this.authenticator = authenticator;
		}

		public DatagramPacket getPayload() {
			return payload;
		}

		public byte[] getAuthenticator() {
			return authenticator;
		}

	}
}