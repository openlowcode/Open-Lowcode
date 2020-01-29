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

import org.openlowcode.design.generation.StringFormatter;

/**
 * an external element showing data on a related property on another data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ExternalElement
		extends
		Element {
	@SuppressWarnings("unused")
	private Property<?> localproperty;
	@SuppressWarnings("unused")
	private DataObjectDefinition referencedobject;
	@SuppressWarnings("unused")
	private Property<?> referencedproperty;
	private StoredElement referencedpropertyelement;

	public StoredElement getReferencedPropertyElement() {
		return this.referencedpropertyelement;
	}

	@SuppressWarnings("unused")
	private boolean conditional;

	/**
	 * creates an external element
	 * 
	 * @param localproperty      property of the current object
	 * @param referencedobject   related object the external element is coming from
	 * @param referencedproperty property on the related object the external element
	 *                           is coming from
	 * @param conditional        if true, the field will show only in some
	 *                           conditions (not used, see issue #19 in Github)
	 * @param propertyelement    element of the property to display
	 */
	public ExternalElement(
			Property<?> localproperty,
			DataObjectDefinition referencedobject,
			Property<?> referencedproperty,
			boolean conditional,
			StoredElement referencedpropertyelement) {
		this(localproperty, referencedobject, referencedproperty, conditional, referencedpropertyelement, null);
	}

	/**
	 * @param localproperty      property of the current object
	 * @param referencedobject   related object the external element is coming from
	 * @param referencedproperty property on the related object the external element
	 *                           is coming from
	 * @param conditional        if true, the field will show only in some
	 *                           conditions (not used, see issue #19 in Github)
	 * @param propertyelement    element of the property to display
	 * @param namesuffix         name suffix of the external element to ensure
	 *                           external element name is unique
	 */
	public ExternalElement(
			Property<?> localproperty,
			DataObjectDefinition referencedobject,
			Property<?> referencedproperty,
			boolean conditional,
			StoredElement referencedpropertyelement,
			String namesuffix) {
		super("TEMP");
		this.localproperty = localproperty;
		this.referencedobject = referencedobject;
		this.referencedproperty = referencedproperty;
		this.referencedpropertyelement = referencedpropertyelement;
		this.conditional = conditional;
		String name = localproperty.getName();
		if (namesuffix != null)
			name = name + namesuffix;
		name = name + StringFormatter.formatForJavaClass(this.referencedpropertyelement.getName());

		this.changeName(name);
		String genericsname = localproperty.getPropertyclassname();
		if (namesuffix != null)
			genericsname = genericsname + namesuffix;
		genericsname = genericsname + StringFormatter.formatForJavaClass(this.referencedpropertyelement.getName());
		this.setGenericsName(genericsname);

	}

	@Override
	public String getJavaFieldName() {
		return referencedpropertyelement.getJavaFieldName();
	}

}
