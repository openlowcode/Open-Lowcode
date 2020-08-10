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

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * a field holding an integer as payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IntegerField
		extends
		Field {

	/**
	 * creates an integer field
	 * 
	 * @param name        unique name of the field, should be a valid java field
	 *                    name and a valid database column name
	 * @param displayname description of the field in the default language of the
	 *                    application
	 * @param tooltip     roll-over tooltip
	 */
	public IntegerField(String name, String displayname, String tooltip) {
		super(name, displayname, tooltip);

	}

	/**
	 * creates an integer field
	 * 
	 * @param name            unique name of the field, should be a valid java field
	 *                        name and a valid database column name
	 * @param displayname     description of the field in the default language of
	 *                        the application
	 * @param tooltip         roll-over tooltip
	 * @param displaypriority priority of the field, with a priority between -1000
	 *                        and 1000
	 */
	public IntegerField(String name, String displayname, String tooltip, int displaypriority) {
		super(name, displayname, tooltip);
		this.setDisplayPriority(displaypriority);

	}

	@Override
	public String getDataObjectFieldName() {
		return "IntegerDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.getDisplayPriority();

	}

	@Override
	public String getJavaType() {
		return "Integer";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// nothing to do
	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		throw new RuntimeException("Composite index not supported for integer field");
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new IntegerField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(),
				this.getDisplayPriority());
	}
	@Override
	public String writeCellExtractor() {
		throw new RuntimeException("Not yet implemented !");
	}

	@Override
	public String writeCellFiller() {
		throw new RuntimeException("Not yet implemented !");
	}

	@Override
	public String writePayloadFiller() {
		return "Not yet implemented";
	}
	
}
