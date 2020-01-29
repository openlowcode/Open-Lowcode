/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import org.openlowcode.tools.misc.Named;

/**
 * A display profile allows to define modes where some fields are filtered
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DisplayProfile
		extends
		Named {

	/**
	 * creates a display profile
	 * 
	 * @param name unique name of the display profile for the data object
	 */
	public DisplayProfile(String name) {
		super(name);

	}

}
