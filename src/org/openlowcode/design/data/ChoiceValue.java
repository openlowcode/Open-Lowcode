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
 * an authorized value in a choice category
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChoiceValue
		extends
		Named {
	private String displayname;
	private String tooltip;
	private Integer pseudonumber;

	/**
	 * create a choice value without classification as pseudo-number
	 * 
	 * @param name        storage code
	 * @param displayname code displayed in the application
	 * @param tooltip     more information
	 */
	public ChoiceValue(String name, String displayname, String tooltip) {
		super(name);
		this.displayname = displayname;
		this.tooltip = tooltip;
	}

	/**
	 * create a choice value with classification as pseudo-number
	 * 
	 * @param name         storage code
	 * @param displayname  code displayed in the application
	 * @param tooltip      more information
	 * @param pseudonumber a forced pseudonumber for the value. It allows to have a
	 *                     display name that does not translate in a number for a
	 *                     choice category that supports pseudo-number
	 */
	public ChoiceValue(String name, String displayname, String tooltip, int pseudonumber) {
		this(name, displayname, tooltip);
		this.pseudonumber = new Integer(pseudonumber);
	}

	/**
	 * @return the pseudo-number value of this choice value if it exists
	 */
	public Integer hasPseudoNumber() {
		return this.pseudonumber;
	}

	/**
	 * @return the display name of the value
	 */
	public String getDisplayName() {
		return this.displayname;
	}

	/**
	 * @return the tooltip of the value
	 */
	public String getTooltip() {
		return this.tooltip;
	}
}
