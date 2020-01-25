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
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * A choice field is a field offering a choice to the user between items of a
 * list. As an example would be a choice field asking for size of apparel
 * between S, M, L, XL.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 *
 */
public class ChoiceField
		extends
		Field {
	public static final int INDEXTYPE_NONE = 0;
	public static final int INDEXTYPE_SEARCHWITHNOINDEX = 1;
	public static final int INDEXTYPE_RAWINDEX = 2;

	private StoredElement plainfield;
	private ChoiceCategory choice;
	private int indextype;

	/**
	 * @return returns the choice category used for this choice field
	 */
	public ChoiceCategory getChoice() {
		return this.choice;
	}

	/**
	 * creates a choice field to be added to a data object, with default priority
	 * and no index
	 * 
	 * @param name        unique name of the field, should be a valid java field
	 *                    name and a valid database column name
	 * @param displayname description of the field in the default language of the
	 *                    application
	 * @param tooltip     roll-over tooltip
	 * @param choice      choice category
	 */
	public ChoiceField(String name, String displayname, String tooltip, ChoiceCategory choice) {
		this(name, displayname, tooltip, choice, 0);
	}

	/**
	 * creates a choice field to be added to a data object, with given priority and
	 * no index
	 * 
	 * @param name            unique name of the field, should be a valid java field
	 *                        name and a valid database column name
	 * @param displayname     description of the field in the default language of
	 *                        the application
	 * @param tooltip         roll-over tooltip
	 * @param choice          choice category
	 * @param displaypriority length of storage. Should be enough to accomodate the
	 *                        storage code of the expected number of elements
	 */
	public ChoiceField(String name, String displayname, String tooltip, ChoiceCategory choice, int displaypriority) {
		this(name, displayname, tooltip, choice, displaypriority, INDEXTYPE_NONE);

	}

	/**
	 * creates a choice field to be added to a data object, with given priority and
	 * index
	 * 
	 * @param name            unique name of the field, should be a valid java field
	 *                        name and a valid database column name
	 * @param displayname     description of the field in the default language of
	 *                        the application
	 * @param tooltip         roll-over tooltip
	 * @param choice          choice category
	 * @param displaypriority length of storage. Should be enough to accomodate the
	 *                        storage code of the expected number of elements
	 * @param indextype       type of index as defined in a constant integer on this
	 *                        class
	 */
	public ChoiceField(
			String name,
			String displayname,
			String tooltip,
			ChoiceCategory choice,
			int displaypriority,
			int indextype) {
		super(name, displayname, tooltip, displaypriority);
		this.choice = choice;
		this.indextype = indextype;
		plainfield = new StringStoredElement("", choice.getKeyStorageLength());
		if (this.indextype == INDEXTYPE_RAWINDEX) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname, choice, false));
			this.addIndex(new Index("RAWSEARCH", plainfield, false));
		}

		if (this.indextype == INDEXTYPE_SEARCHWITHNOINDEX) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname, choice, false));
		}
		if (this.indextype == INDEXTYPE_NONE) {
			this.addElement(plainfield);
		}
	}

	@Override
	public String getDataObjectFieldName() {
		return "ChoiceDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		int columnwidth = choice.getDisplayLabelLength(this.getDisplayname().length());
		if (this.getDisplayPriority() != 0)
			return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
					+ this.isNoUserEdition() + "," + StringFormatter.formatForJavaClass(choice.getName())
					+ "ChoiceDefinition.get()," + this.getDisplayPriority() + "," + columnwidth;

		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.isNoUserEdition() + "," + StringFormatter.formatForJavaClass(choice.getName())
				+ "ChoiceDefinition.get()";

	}

	@Override
	public String getJavaType() {

		return "ChoiceValue<" + StringFormatter.formatForJavaClass(choice.getName()) + "ChoiceDefinition>";
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
		return new ChoiceField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(), choice,
				this.getDisplayPriority(), indextype);
	}

}
