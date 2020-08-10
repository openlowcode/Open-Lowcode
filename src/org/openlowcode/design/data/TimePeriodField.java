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
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.tools.data.TimePeriod;

/**
 * creates a field holding a time period
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriodField
		extends
		Field {
	private boolean haspriority=false;
	
	/**
	 * creates a field holding a time period with a specified type with default
	 * priority
	 * 
	 * @param name        unique name for the data object (should be a valid java
	 *                    and sql field name
	 * @param displayname name in the default language for display in the GUI
	 * @param tooltip     mouse roll-over tooltip. Can be longer
	 * @param type        type of Time Period (quarter, month, year...)
	 */
	public TimePeriodField(String name, String displayname, String tooltip, TimePeriod.PeriodType type) {
		super(name, displayname, tooltip);
		this.indextype = INDEXTYPE_NONE;
		init(type, name, displayname);

	}

	private void init(TimePeriod.PeriodType type, String name, String displayname) {
		plainfield = new StringStoredElement("", 12);
		if (this.indextype == INDEXTYPE_RAWINDEX) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname, type));
			this.addIndex(new Index("RAWSEARCH", plainfield, false));
		}

		if (this.indextype == INDEXTYPE_SEARCHWITHNOINDEX) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname, type));
		}

		if (this.indextype == INDEXTYPE_NONE) {
			this.addElement(plainfield);
		}
		this.type = type;
	}

	/**
	 * @param name        unique name on the data object (should be a valid java and
	 *                    sql field name
	 * @param displayname name in the default language for display in the GUI
	 * @param tooltip     mouse roll-over tooltip. Can be longer
	 * @param priority    priority of the field between -1000 (low) and 1000 (high)
	 * @param type        type of Time Period (quarter, month, year...)
	 */
	public TimePeriodField(String name, String displayname, String tooltip, int priority, TimePeriod.PeriodType type) {
		super(name, displayname, tooltip, priority);
		this.indextype = INDEXTYPE_NONE;
		init(type, name, displayname);
		haspriority=true;
	}

	/**
	 * no index on the field (this is default)
	 */
	public static final int INDEXTYPE_NONE = 0;
	/**
	 * search element with no index
	 */
	public static final int INDEXTYPE_SEARCHWITHNOINDEX = 1;
	/**
	 * search element with a basic index
	 */
	public static final int INDEXTYPE_RAWINDEX = 2;
	private StoredElement plainfield;
	private int indextype;
	private TimePeriod.PeriodType type;

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.tools.data.TimePeriod;");

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		return plainfield;
	}

	@Override
	public String getDataObjectFieldName() {
		return "TimePeriodDataObjectField";
	}

	@Override

	public String getDataObjectConstructorAttributes() {
		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.isNoUserEdition() + "," + (this.type != null ? "TimePeriod.PeriodType." + type : "null")
				+ (haspriority ? "," + this.getDisplayPriority() : "");

	}

	@Override
	public String getJavaType() {
		return "TimePeriod";
	}

	public TimePeriod.PeriodType getPeriodType() {
		return this.type;
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new TimePeriodField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(),
				this.getDisplayPriority(), type);
	}
	
	@Override
	public String writeCellExtractor() {
		return "(a,b)->(TimePeriod.generateFromObject(a))";
	}

	@Override
	public String writeCellFiller() {
		return "(a,b)->TimePeriodDataObjectFieldFlatFileLoader.putContentInCell(a,b)";
	}
	
	@Override
	public String writePayloadFiller() {
		return "Not yet implemented";
	}

}
