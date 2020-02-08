/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.util.ArrayList;

/**
 * An interface to set constraints on widgets displaying multiple objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 *
 */
public interface CMultiFieldObjectAccess {
	/**
	 * @param fieldname        the name of the field
	 * @param restrainedvalues the set of possible selections for this field. The
	 *                         following will be performed:
	 *                         <ul>
	 *                         <li>limit selection to provided values</li>
	 *                         <li>if value currently selected is not valid, empty
	 *                         value (unless a selected value exists)</li>
	 *                         <li>if only one selection item is provided, it will
	 *                         be automatically selected, even if no prefered value
	 *                         is given</li>
	 *                         </ul>
	 * @param selected         the prefered value out of the restrained value. Will
	 *                         force the field to the prefered value if entered if
	 *                         provided with null, wil not select anyting, if
	 *                         provided wih empty string "", will force to blank
	 * @return true if the constraint resulted in a blank field
	 */
	public boolean setConstraint(String fieldname, ArrayList<String> restrainedvalues, String selected);

	/**
	 * gets the field value according to constraint for the given field
	 * 
	 * @param fieldname unique java name of the field
	 * @return the value the field should take according to contraints
	 */
	public String getFieldValueForConstraint(String fieldname);

	/**
	 * lift constraint on the given field
	 * 
	 * @param fieldname unique java name of the field
	 */
	public void liftConstraint(String fieldname);
}
