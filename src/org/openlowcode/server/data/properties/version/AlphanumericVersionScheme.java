/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.version;

/**
 * A simple alphanumeric version scheme (A, B, C...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AlphanumericVersionScheme
		extends
		VersionScheme {
	/**
	 * creates an alphanumering version scheme
	 */
	public AlphanumericVersionScheme() {

	}

	@Override
	public String getFirstVersion() {
		return "A";
	}

	@Override
	public String getDefaultRevisionNextVersion(String currentversion) {
		if (currentversion.length() == 1)
			if ((currentversion.compareTo("A") >= 0) && (currentversion.compareTo("Z") < 0)) {
				return new String(new char[] { ((char) (currentversion.charAt(0) + 1)) });
			}
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String getDefaultRevisionPreviousVersion(String currentversion) {
		if (currentversion.length() == 1)
			if ((currentversion.compareTo("A") > 0) && (currentversion.compareTo("Z") <= 0)) {
				return new String(new char[] { ((char) (currentversion.charAt(0) - 1)) });
			}
		if (currentversion.equals("A"))
			return null;
		throw new RuntimeException("Not yet implemented");
	}

}
