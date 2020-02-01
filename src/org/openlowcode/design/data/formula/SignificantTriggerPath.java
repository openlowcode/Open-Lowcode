/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.formula;

/**
 * an interface representing a significant element in a trigger path
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface SignificantTriggerPath {

	/**
	 * generates the path of the trigger
	 * 
	 * @param fullpath full path so far
	 * @return the path with this path node included
	 */
	public String generatePath(String fullpath);

}
