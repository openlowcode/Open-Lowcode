/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.fields;

/**
 * A light-weight interface to validate string input for a specific format. This
 * is especially used to validate dates and numbers
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public interface FormatValidator<E extends Object> {
	/**
	 * will parse the string value and decide if the format is correct or not
	 * 
	 * @param valueasstring
	 * @return the reformatted string if needed, null string if format is invalid
	 *         (note: Exception should not be thrown if format is invalid). Note: A
	 *         null value will be rendered as an empty string;
	 */
	public String valid(String valueasstring);
	
	/**
	 * @param stringvalue
	 * @return
	 */
	public E parse(String stringvalue);
	
	/**
	 * @param value
	 * @return
	 */
	public String print(E value);
}
