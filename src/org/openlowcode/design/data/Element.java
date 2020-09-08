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
 * A data element contains a unitary element of information, typically one field
 * stored in a database
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Element
		extends
		Named {
	private String genericsname;

	/**
	 * create an element with the given name
	 * 
	 * @param name name of the element
	 */
	public Element(String name) {
		super(name);
		genericsname = this.getName();
	}

	/**
	 * provides the generic name of the variable for properties that can be
	 * instanciated multiple times. THis is especially the name of the getter
	 * in the property class
	 * 
	 * @return
	 */
	public String getGenericNameForProperty() {
		return this.genericsname;
	}
	
	/**
	 * provides the element name inside the property. Especially, for properties
	 * that can be instanciated multiple time, this may be typically property instance name + suffix
	 * 
	 * @return
	 */
	public String getNameElementForProperty() {
		return this.genericsname;
	}

	/**
	 * sets the generics name of the variable, typically, this will be the same even
	 * if there are different instances of the field or property. As an exemple,
	 * there can be several properties 'Generic Link' on the same object. The
	 * element name for the id of the object linked will be 'generic link name+id',
	 * generics name will just be 'id'
	 * 
	 * @param genericsname generics name
	 */
	public void setGenericsName(String genericsname) {
		this.genericsname = genericsname;
	}

	/**
	 * @return the java field name (type of object) for use in code generation
	 */
	public abstract String getJavaFieldName();

	@Override
	public void changeName(String name) {
		super.changeName(name);
		genericsname = this.getName();
	}

}
