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

/**
 * An interface to all classes that can transform a stored value in a display
 * value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@FunctionalInterface
public interface StringDecoder {
	/**
	 * performs the decoding
	 * 
	 * @param storedvalue stored value (typically in the database)
	 * @return decoded value (typically human readable
	 */
	public String decode(String storedvalue);
}
