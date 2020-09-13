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
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A multiple choice field allows to store multiple choices amongst a defined
 * list (choice category)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MultipleChoiceField
		extends
		Field {

	private StoredElement plainfield;
	private ChoiceCategory choice;
	private int storagelength;

	/**
	 * @return the choice category
	 */
	public ChoiceCategory getChoice() {
		return this.choice;
	}

	/**
	 * create a multiple choice field with default priority
	 * 
	 * 
	 * @param name          unique name of the field, should be a valid java field
	 *                      name and a valid database column name
	 * @param displayname   description of the field in the default language of the
	 *                      application
	 * @param tooltip       roll-over tooltip
	 * @param choice        choice category
	 * @param storagelength length of storage. Should be enough to accomodate the
	 *                      storage code of the expected number of elements
	 */
	public MultipleChoiceField(
			String name,
			String displayname,
			String tooltip,
			ChoiceCategory choice,
			int storagelength) {
		this(name, displayname, tooltip, choice, storagelength, 0);
	}

	/**
	 * create a multiple choice field
	 * 
	 * @param name            unique name of the field, should be a valid java field
	 *                        name and a valid database column name
	 * @param displayname     description of the field in the default language of
	 *                        the application
	 * @param tooltip         roll-over tooltip
	 * @param choice          choice category
	 * @param storagelength   length of storage. Should be enough to accomodate the
	 *                        storage code of the expected number of elements
	 * @param displaypriority priority of the field, with a priority between -1000
	 *                        and 1000
	 */
	public MultipleChoiceField(
			String name,
			String displayname,
			String tooltip,
			ChoiceCategory choice,
			int storagelength,
			int displaypriority) {
		super(name, displayname, tooltip, displaypriority);
		this.choice = choice;
		this.storagelength = storagelength;
		plainfield = new StringStoredElement("", storagelength);

	}

	@Override
	public String getDataObjectFieldName() {
		return "MultipleChoiceDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		int columnwidth = choice.getDisplayLabelLength(this.getDisplayname().length());
		if (this.getDisplayPriority() != 0)
			return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
					+ this.isNoUserEdition() + "," + StringFormatter.formatForJavaClass(choice.getName())
					+ "ChoiceDefinition.get()," + this.storagelength + "," + this.getDisplayPriority() + ","
					+ columnwidth;

		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.isNoUserEdition() + "," + StringFormatter.formatForJavaClass(choice.getName())
				+ "ChoiceDefinition.get()," + this.storagelength;

	}

	@Override
	public String getJavaType() {

		return "ChoiceValue<" + StringFormatter.formatForJavaClass(choice.getName()) + "ChoiceDefinition>[]";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import " + choice.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(choice.getName()) + "ChoiceDefinition;");

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		return plainfield;
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new MultipleChoiceField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(), choice,
				storagelength, this.getDisplayPriority());
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
