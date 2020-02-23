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

/***
 * This class just holds the encoding key for 2-ways encoding. Recommendation is
 * for companies to override this class in their client and server build
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcEncrypterString {
	/**
	 * gets the encryption string
	 * 
	 * @return the encryption string
	 */
	protected static String getEncryptionString() {
		return "HeureuxQuiCommeUlysse";
	}
}
