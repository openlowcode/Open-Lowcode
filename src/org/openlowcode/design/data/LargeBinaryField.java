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
 * A field storing a large binary file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LargeBinaryField
		extends
		Field {

	private int size;

	/**
	 * Create a large binary field with no specified maximum size
	 * 
	 * @param name        name of the field (should be a unique java field or SQL
	 *                    field name)
	 * @param displayname name in the default language
	 * @param helper      tooltip for mouse roll-over
	 */
	public LargeBinaryField(String name, String displayname, String helper) {
		super(name, displayname, helper);
		this.size = 0;
	}

	/**
	 * @param name        name of the field (should be a unique java field or SQL
	 *                    field name)
	 * @param displayname name in the default language
	 * @param helper      tooltip for mouse roll-over
	 * @param size        maximum size of the field in bytes
	 */
	public LargeBinaryField(String name, String displayname, String helper, int size) {
		super(name, displayname, helper);
		this.size = size;

	}

	@Override
	public String getDataObjectFieldName() {

		return "LargeBinaryDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		if (this.size > 0)
			return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
					+ this.size;
		if (this.size == 0)
			return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\"";
		return "#ERROR";
	}

	@Override
	public String getJavaType() {
		return "SFile";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.tools.messages.SFile;");

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		throw new RuntimeException("Composite index not supported for large binary field");
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new LargeBinaryField((newname != null ? newname : this.getName()),
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
	
	@Override
	public String writeStringPrinterAndConsolidator() {
		return "(a)->(not yet implemented)";
	}
}
