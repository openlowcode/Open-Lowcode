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
import java.util.ArrayList;

import org.openlowcode.design.data.formula.CalculatedFieldTriggerPath;
import org.openlowcode.design.data.formula.FormulaDefinitionElement;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A field storing a decimal value payload. It can be used in formulas and as
 * main value for smart reports
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DecimalField
		extends
		Field
		implements
		FormulaDefinitionElement {
	private int length;
	private int scale;
	private int indextype;
	private ArrayList<CalculatedFieldTriggerPath> alltriggers;
	private DecimalFormatter decimalformatter;
	private StoredElement plainfield;

	public int getPrecision() {
		return this.scale;
	}

	public static int INDEXTYPE_NONE = 0;
	public static int INDEXTYPE_RAWINDEX = 2;

	/**
	 * creates a decimal field with default priority and no index, and default
	 * display (number)
	 * 
	 * @param name        unique name for the object that should be a legal java
	 *                    argument and SQL column name
	 * @param displayname plain language description in the default language
	 * @param tooltip     roll-over tip
	 * @param length      total number of digits of the number (e.g. 533.33 is 5
	 *                    digits)
	 * @param scale       (digits to the right of decimal point (e.g. 533.33 has
	 *                    scale on 2)
	 */
	public DecimalField(String name, String displayname, String tooltip, int length, int scale, int indextype) {

		super(name, displayname, tooltip);
		this.length = length;
		this.scale = scale;
		this.indextype = indextype;
		plainfield = new DecimalStoredElement("", length, scale);
		if (this.indextype == StringField.INDEXTYPE_RAWINDEX) {
			this.addIndex(new Index("RAWSEARCH", plainfield, false));
		}
		alltriggers = new ArrayList<CalculatedFieldTriggerPath>();
	}

	/**
	 * creates a decimal field with default display
	 * 
	 * @param name            unique name for the object that should be a legal java
	 *                        argument and SQL column name
	 * @param displayname     plain language description in the default language
	 * @param tooltip         roll-over tip
	 * @param length          total number of digits of the number (e.g. 533.33 is 5
	 *                        digits)
	 * @param scale           (digits to the right of decimal point (e.g. 533.33 has
	 *                        precision on 2)
	 * @param indextype       type of index (one of the static int of this class
	 * @param displaypriority a number strictly between -1000 and 1000
	 */
	public DecimalField(
			String name,
			String displayname,
			String tooltip,
			int length,
			int scale,
			int indextype,
			int displaypriority) {
		this(name, displayname, tooltip, length, scale, indextype);
		this.setDisplayPriority(displaypriority);
	}

	/**
	 * creates a decimal field with specified display (Decimal Formatter). This
	 * allows to show the figure as a progress bar
	 * 
	 * @param name             unique name for the object that should be a legal
	 *                         java argument and SQL column name
	 * @param displayname      plain language description in the default language
	 * @param tooltip          roll-over tip
	 * @param length           total number of digits of the number (e.g. 533.33 is
	 *                         5 digits)
	 * @param scale            (digits to the right of decimal point (e.g. 533.33
	 *                         has scale on 2)
	 * @param indextype        type of index (one of the static int of this class
	 * @param displaypriority  a number strictly between -1000 and 1000
	 * @param decimalformatter
	 */
	public DecimalField(
			String name,
			String displayname,
			String tooltip,
			int length,
			int scale,
			int indextype,
			int displaypriority,
			DecimalFormatter decimalformatter) {
		this(name, displayname, tooltip, length, scale, indextype);
		this.setDisplayPriority(displaypriority);
		this.decimalformatter = decimalformatter;
	}

	@Override
	public String getDataObjectFieldName() {

		return "DecimalDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.length + "," + this.scale + "," + this.isNoUserEdition() + ",false,false,"
				+ (decimalformatter != null ? decimalformatter.generateDefinition()
						: "null" + "," + this.getDisplayPriority());
	}

	@Override
	public String getJavaType() {
		return "BigDecimal";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import java.math.BigDecimal;");
		sg.wl("import org.openlowcode.server.data.formula.ConstantFigure;");
		sg.wl("import org.openlowcode.server.data.formula.Formula;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectProduct;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectSum;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectSubstract;");
		sg.wl("import org.openlowcode.server.data.formula.CalculatedFieldExtractor;");
		sg.wl("import org.openlowcode.server.data.formula.CalculatedField;");
		sg.wl("import org.openlowcode.server.data.formula.CalculatedFieldTrigger;");
		sg.wl("import org.openlowcode.server.data.formula.SumOnChildren;");
		sg.wl("import org.openlowcode.server.data.formula.LocalPath;");
		sg.wl("import org.openlowcode.server.data.formula.PathToCalculatedField;");
		sg.wl("import org.openlowcode.server.data.formula.SumOnLinkRightObject;");
		sg.wl("import org.openlowcode.server.data.formula.SumProductOnLinkRightObject;");
		sg.wl("import org.openlowcode.server.data.formula.LinkToLeftReverseNavigator;");
		sg.wl("import org.openlowcode.server.data.helpers.ReportTree;");
	}

	@Override
	public DataObjectDefinition getOwnerObject() {
		return this.getParentObject();
	}

	@Override
	public String generateFormulaElement() {
		String objectclass = StringFormatter.formatForJavaClass(this.getParentObject().getName());
		String fieldclass = StringFormatter.formatForJavaClass(this.getName());
		return objectclass + "Definition.get" + objectclass + "Definition().get" + fieldclass + "FormulaElement()";
	}

	@Override
	public void setTriggersOnSourceFields(CalculatedFieldTriggerPath triggerpath) {
		alltriggers.add(triggerpath);

	}

	@Override
	public CalculatedFieldTriggerPath[] getAllTriggerPaths() {
		return alltriggers.toArray(new CalculatedFieldTriggerPath[0]);
	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		return this.plainfield;
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new DecimalField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(), length, scale,
				indextype, this.getDisplayPriority(), this.decimalformatter);
	}

	@Override
	public String writeCellExtractor() {
		return "(a,b,c)->(DecimalDataObjectFieldFlatFileLoaderColumn.getContentFromCell( a,2,12 ,b, (c!=null?(c.length>0?DecimalDataObjectFieldFlatFileLoaderColumn.parseMultiplierForImport(c[0]):0):0),null))";
	}

	@Override
	public String writeCellFiller() {
		return "(a,b,c)->DecimalDataObjectFieldFlatFileLoaderColumn.putContentInCell(a,c,(b!=null?(b.length>0?b[0]:null):null))";
	}

	@Override
	public String writePayloadFiller() {
		return "Not yet implemented";
	}

	@Override
	public String writeStringPrinterAndConsolidator() {
		return "(a)->(DecimalDataObjectField.printDecimal(a)),(a,b)->(ReportTree.sumIfNotNull(a, b))";
	}
}
