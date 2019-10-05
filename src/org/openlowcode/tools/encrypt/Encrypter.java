/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.encrypt;

/**
 * A an abstract placeholder for the encryption / decryption mechanism. This should
 * allow non-synchronized access
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 */
public interface Encrypter {
	/**
	 * @param payload the clear payload
	 * @return encrypted payload
	 */
	public abstract String encryptStringTwoWays(String payload);
	
	/**
	 * @param payload encrypted payload
	 * @return clear payload
	 */
	public abstract String decryptStringTwoWays(String payload);
		
	
}
