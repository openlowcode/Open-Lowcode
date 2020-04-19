/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import org.openlowcode.tools.misc.Named;

/**
 * Definition of a field inside the persistence layer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> class of the payload object
 */
public abstract class FieldSchema<E extends Object>
		extends
		Named {

	public final static String TEMP_SUFFIX = "_TMP";
	public final static String EASY_SEARCH_FIELD_SUFFIX = "_ESR";

	public FieldSchema(String name) {
		super(name);
		if (name != null)
			if (name.trim().toUpperCase().endsWith(TEMP_SUFFIX))
				throw new RuntimeException("Field Schema name '" + name.trim().toUpperCase() + "' cannot end with '"
						+ TEMP_SUFFIX + "' as this is a reserved suffix for internal proceedings.");
	}

	/**
	 * @return the default value
	 */
	public abstract E defaultValue();

	/**
	 * @param o an uncasted object
	 * @return the object casted to the actual class, if class not good, throws a
	 *         RuntimeException
	 */
	public abstract E castToType(Object o);

	/**
	 * this method returns a subclass of Field appropriate for the field schema
	 * 
	 * @return the generated field with the default value
	 */
	public abstract Field<E> initBlankField();

	@Override
	public String toString() {
		return "[" + this.getClass().getName() + "," + this.getName() + "]";
	}

}
