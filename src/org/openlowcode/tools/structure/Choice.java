/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

/**
 * An interface for a choice value object. This has a stored value
 * that is transported in the package. This interface is used to decouple
 * the structure package with the different classes of Choice Value used in
 * the Open Lowcode client and server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
/**
 * @author demau
 *
 */
public interface Choice {
	
	/**
	 * @return the storage code of the choice value
	 */
	public String getStorageCode();
}
