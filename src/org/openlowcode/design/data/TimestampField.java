package org.openlowcode.design.data;
/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * A field holding a timestamp in a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimestampField
		extends
		Field {
	private int indextype;
	/**
	 * no index, and the field is not searchable
	 */
	public static int INDEXTYPE_NONE = 0;
	/**
	 * 
	 */
	public static int INDEXTYPE_RAWINDEX = 2;
	/**
	 * 
	 * @since 1.6
	 */
	public static int INDEXTYPE_RAWINDEXWITHSEARCH = 3;
	private boolean timeedit;
	private TimestampStoredElement field;

	/**
	 * Creates a timestamp field with no time edit and no search index and default
	 * priority
	 * 
	 * @param name        unique name for the data object (should be a valid java
	 *                    and sql field name
	 * @param displayname name in the default language for display in the GUI
	 * @param tooltip     mouse roll-over tooltip. Can be longer
	 * @param indextype   a type of index as declared in a static integer on this
	 *                    class
	 */
	public TimestampField(String name, String displayname, String tooltip, int indextype) {
		super(name, displayname, tooltip);
		this.indextype = indextype;
		field = new TimestampStoredElement(getName());
		if (this.indextype == INDEXTYPE_RAWINDEXWITHSEARCH) {
			this.AddElementWithSearch(field, new SearchWidgetDefinition(true, this.getName(), this.getDisplayname(),
					SearchWidgetDefinition.TYPE_DATE, SearchWidgetDefinition.POSTTREATMENT_NONE));
		} else {
			this.addElement(field);
		}
		if ((this.indextype == INDEXTYPE_RAWINDEX) || (this.indextype == INDEXTYPE_RAWINDEXWITHSEARCH)) {
			this.addIndex(new Index("RAWSEARCH", field, false));
		}
		this.timeedit = false;
	}

	/**
	 * creates a timestamp field with no time edit
	 * 
	 * @param name            unique name for the data object (should be a valid
	 *                        java and sql field name
	 * @param displayname     name in the default language for display in the GUI
	 * @param tooltip         mouse roll-over tooltip. Can be longer
	 * @param indextype       a type of index as declared in a static integer on
	 *                        this class
	 * @param displaypriority priority of the field between -1000 (low) and 1000
	 *                        (excluded)
	 */
	public TimestampField(String name, String displayname, String tooltip, int indextype, int displaypriority) {
		super(name, displayname, tooltip, displaypriority);
		this.indextype = indextype;
		TimestampStoredElement field = new TimestampStoredElement(getName());
		this.addElement(field);
		if (this.indextype == INDEXTYPE_RAWINDEX) {
			this.addIndex(new Index("RAWSEARCH", field, false));
		}
		this.timeedit = false;
	}

	/**
	 * creates a timestamp field
	 * 
	 * @param name            unique name for the data object (should be a valid
	 *                        java and sql field name
	 * @param displayname     name in the default language for display in the GUI
	 * @param tooltip         mouse roll-over tooltip. Can be longer
	 * @param indextype       a type of index as declared in a static integer on
	 *                        this class
	 * @param displaypriority priority of the field between -1000 (low) and 1000
	 *                        (excluded)
	 * @param timeedit        if true, edition of time value is authorized on the
	 *                        pages
	 */
	public TimestampField(
			String name,
			String displayname,
			String tooltip,
			int indextype,
			int displaypriority,
			boolean timeedit) {
		super(name, displayname, tooltip, displaypriority);
		this.indextype = indextype;
		TimestampStoredElement field = new TimestampStoredElement(getName());
		this.addElement(field);
		if (this.indextype == INDEXTYPE_RAWINDEX) {
			this.addIndex(new Index("RAWSEARCH", field, false));
		}
		this.timeedit = timeedit;
	}

	@Override
	public String getDataObjectFieldName() {
		return "DateDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.timeedit + "," + this.isNoUserEdition() + "," + this.getDisplayPriority();
	}

	@Override
	public String getJavaType() {
		return "Date";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import java.util.Date;");

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		return field;
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new TimestampField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(), indextype,
				this.getDisplayPriority(), timeedit);
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
