/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.version;

/**
 * This class provides utilities for managing version
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class VersionScheme {
	/**
	 * @return a string with the first version in the version scheme (e.g. for
	 *         alphanumeric version scheme, A)
	 */
	public abstract String getFirstVersion();

	/**
	 * 
	 * @param currentversion the current version of the object
	 * @return the next version for the default revision. (E.g. for alphanumeric
	 *         version scheme, after A: B, after Z: AA)
	 */
	public abstract String getDefaultRevisionNextVersion(String currentversion);

	/**
	 * @param currentversion the current version
	 * @return the normal previous version
	 */
	public abstract String getDefaultRevisionPreviousVersion(String currentversion);
}
