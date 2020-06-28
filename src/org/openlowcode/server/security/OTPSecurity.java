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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * A utility to perform checks on an OTP Server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.10
 */
public class OTPSecurity {
	private static final Logger logger = Logger.getLogger(OTPSecurity.class.getName());
	public static final String MOCK_OTP = "MOCKOTP";
	public static final String RADIUS_OTP = "RADIUSOTP";
	private static final SimpleDateFormat printlocalhour = new SimpleDateFormat("HH");
	private String type;
	private String url;
	private int port;
	private String secret;
	private SimpleRadiusConnection radiusconnection;

	/**
	 * create a mock OTP security that will return true if the OTP password is user
	 * id + hour in server local time
	 */
	public OTPSecurity() {
		type = MOCK_OTP;
	}

	/**
	 * create a real OTP security connection to a Radius server
	 * 
	 * @param url    url of the server
	 * @param port   port of the server
	 * @param secret secret of the server
	 */
	public OTPSecurity(String url, int port, String secret) {
		type = RADIUS_OTP;
		this.url = url;
		this.port = port;
		this.secret = secret;
		this.radiusconnection = new SimpleRadiusConnection(this.url, this.port, this.secret);
	}

	/**
	 * checks if the OTP (one-time password) combination is valid
	 * 
	 * @param userid id of the user (could be the enterprise id)
	 * @param otp    the actual OTP to be used for your radius server, or a mock OTP
	 *               made of your userid and the hour in local time
	 * @return true if the otp
	 */
	public boolean checkOTP(String userid, String otp) {
		if (type.equals(MOCK_OTP)) {
			logger.severe("   ---- Using mock otp to check "+userid+" and otp "+otp);
			String requestedotp = userid + printlocalhour.format(new Date());
			logger.severe("           referenceotp = "+requestedotp+", valid request = "+requestedotp.equals(otp));
			if (requestedotp.equals(otp))
				return true;
			return false;
		}
		if (type.equals(RADIUS_OTP)) {
			try {
				return radiusconnection.checkOTP(userid, otp);
			} catch (Exception e) {
				logger.warning("Exception in  checking radius connection " + e.getClass() + " - " + e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++)
					logger.warning("   - " + e.getStackTrace()[i]);
				return false;
			}
		}
		return false;
	}
}
