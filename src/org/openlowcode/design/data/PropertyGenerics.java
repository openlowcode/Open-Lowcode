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
 * A propertyGenerics allows to link a property on a main object to a
 * third-party object. It is possible to specify the significant property this
 * third-party object should have<br>
 * As an example, if you wish to link to a third-party object,you will specify
 * that a significant property of the third-party object should be
 * "UniqueIdentified".
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PropertyGenerics
		extends
		Named {
	private DataObjectDefinition otherobject;
	private Property<?> otherobjectsignificantproperty;

	/**
	 * @param name                      name of the property generics
	 * @param object                    the object the property is configured to
	 * @param objectsignificantproperty the property the object absolutely needs
	 */
	public PropertyGenerics(String name, DataObjectDefinition otherobject, Property<?> otherobjectsignificantproperty) {
		super(name);
		this.otherobject = otherobject;
		this.otherobjectsignificantproperty = otherobjectsignificantproperty;
	}

	/**
	 * @return the other object being refered to in the generics
	 */
	public DataObjectDefinition getOtherObject() {
		return otherobject;
	}

	/**
	 * @return the significant property in the other object used in the generics
	 */
	public Property<?> getOtherObjectsignificantproperty() {
		return otherobjectsignificantproperty;
	}

}
