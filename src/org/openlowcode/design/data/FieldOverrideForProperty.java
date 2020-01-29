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

/**
 * a Field overrides allows to define a new label and a different priority for a
 * display element of the property. E.g. in property Numbered, the label of the
 * field is "Number", but it could be personalized to something else (e.g.
 * 'Social Security Number')
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FieldOverrideForProperty {
	public static final int NO_PRIORITY = -1000;
	private String newlabel;
	private int newpriority = NO_PRIORITY; // -1000 = not filled
	private String newcomment;
	private String fieldcode;

	/**
	 * creates a field override defining a new label
	 * 
	 * @param fieldcode code of the field of the property
	 * @param newlabel  new label for the field
	 */
	public FieldOverrideForProperty(String fieldcode, String newlabel) {
		this.newlabel = newlabel;
		this.fieldcode = fieldcode;
	}

	/**
	 * @param fieldcode code of the field of the property
	 * @param newlabel  new label for the field
	 * @param priority  new priority
	 */
	public FieldOverrideForProperty(String fieldcode, String newlabel, int priority) {
		this.newlabel = newlabel;
		this.fieldcode = fieldcode;
		this.newpriority = priority;
	}

	/**
	 * @param fieldcode  code of the field of the property
	 * @param newlabel   new label for the field
	 * @param priority   new priority
	 * @param newcomment new mouse roll-over comment
	 */
	public FieldOverrideForProperty(String fieldcode, String newlabel, int priority, String newcomment) {
		this.newlabel = newlabel;
		this.fieldcode = fieldcode;
		this.newpriority = priority;
		this.newcomment = newcomment;
	}

	/**
	 * @param fieldcode  code of the field of the property
	 * @param newlabel   new label for the field
	 * @param newcomment new mouse roll-over comment
	 */
	public FieldOverrideForProperty(String fieldcode, String newlabel, String newcomment) {
		this.newlabel = newlabel;
		this.fieldcode = fieldcode;
		this.newcomment = newcomment;
	}

	/**
	 * @return get the new label
	 */
	public String getNewlabel() {
		return newlabel;
	}

	/**
	 * @return get the new priority (if it exists)
	 */
	public int getNewpriority() {
		return newpriority;
	}

	/**
	 * @return get the new comment (if it exists)
	 */
	public String getNewcomment() {
		return newcomment;
	}

	/**
	 * @return get the field code for this field overrides
	 */
	public String getFieldcode() {
		return fieldcode;
	}
}
