/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalFormatter;
import org.openlowcode.design.data.DecimalStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.formula.CalculatedFieldTriggerPath;
import org.openlowcode.design.data.formula.FormulaDefinitionElement;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A computed decimal is a stored decimal field that is computed based on
 * information on the object or on adjacent objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ComputedDecimal
		extends
		Property<ComputedDecimal>
		implements
		FormulaDefinitionElement {
	private FormulaDefinitionElement formula;
	private ArrayList<CalculatedFieldTriggerPath> alltriggers;
	private DecimalFormatter decimalformatter;
	private int precision;
	private int scale;
	private String fieldlabel;
	private int priority;

	
	/**
	 * creates a computed decimal field with a formatter with default priority for display
	 * 
	 * @param name             name of the field (should be valid java attribute
	 *                         name)
	 * @param fieldlabel       label in default language
	 * 
	 * @param precision        (digits to the right of decimal point (e.g. 533.33
	 *                         has precision on 2)
	 * @param scale            total number of digits of the number (e.g. 533.33 is
	 *                         5 digits)
	 * @param formula          formula to calculate the field value
	 * @param decimalformatter formatter for the field (if graphical display is
	 *                         expected)
	 */
	public ComputedDecimal(
			String name,
			String fieldlabel,
			int precision,
			int scale,
			FormulaDefinitionElement formula,
			DecimalFormatter decimalformatter) {
		this(name,fieldlabel,precision,scale,formula,decimalformatter,100);
	}
	
	/**
	 * creates a computed decimal field with a formatter
	 * 
	 * @param name             name of the field (should be valid java attribute
	 *                         name)
	 * @param fieldlabel       label in default language
	 * 
	 * @param precision        (digits to the right of decimal point (e.g. 533.33
	 *                         has precision on 2)
	 * @param scale            total number of digits of the number (e.g. 533.33 is
	 *                         5 digits)
	 * @param formula          formula to calculate the field value
	 * @param decimalformatter formatter for the field (if graphical display is
	 *                         expected)
	 * @param priority         priority for the field display
	 */
	public ComputedDecimal(
			String name,
			String fieldlabel,
			int precision,
			int scale,
			FormulaDefinitionElement formula,
			DecimalFormatter decimalformatter,
			int priority) {
		super(name, "COMPUTEDDECIMAL");
		this.formula = formula;
		this.decimalformatter = decimalformatter;
		this.precision = precision;
		this.scale = scale;
		this.fieldlabel = fieldlabel;
		this.priority = priority;
	}

	@Override
	public void controlAfterParentDefinition() {
		if (this.formula.getOwnerObject() != null)
			if (!this.formula.getOwnerObject().equals(this.parent))
				throw new RuntimeException(
						"Inconsistent owner for formula and calculated field for field " + this.getName());
		String valuename = this.getName();
		StoredElement value = new DecimalStoredElement(valuename, precision, scale);
		value.setGenericsName("COMPUTEDDECIMAL");
		this.addElement(value);

		this.setExtraAttributes(",\"" + fieldlabel.replace("\"", "\\\"") + "\"," + precision + "," + scale
				+ ",new Formula(" + StringFormatter.formatForJavaClass(parent.getName()) + ".getComputeddecimalfor"
				+ this.getInstancename().toLowerCase() + "Extractor()," + formula.generateFormulaElement() + "),"
				+ (decimalformatter != null ? decimalformatter.generateDefinition() : "null")+","+priority);
		alltriggers = new ArrayList<CalculatedFieldTriggerPath>();
		formula.setTriggersOnSourceFields(new CalculatedFieldTriggerPath(this));

	}

	/**
	 * creates a computed decimal field without a formatter
	 * 
	 * @param name       name of the field (should be valid java attribute name)
	 * @param fieldlabel label in default language
	 * 
	 * @param precision  (digits to the right of decimal point (e.g. 533.33 has
	 *                   precision on 2)
	 * @param scale      total number of digits of the number (e.g. 533.33 is 5
	 *                   digits)
	 * @param formula    formula to calculate the field value
	 */
	public ComputedDecimal(String name, String fieldlabel, int precision, int scale, FormulaDefinitionElement formula) {
		this(name, fieldlabel, precision, scale, formula, null);

	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public void setFinalSettings() {

	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.formula.ConstantFigure;");
		sg.wl("import org.openlowcode.server.data.formula.Formula;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectProduct;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectSum;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectSubstract;");
		sg.wl("import org.openlowcode.server.data.formula.SameObjectDivide;");

		sg.wl("import org.openlowcode.server.data.formula.CalculatedFieldExtractor;");
		sg.wl("import org.openlowcode.server.data.formula.CalculatedField;");
		sg.wl("import org.openlowcode.server.data.formula.CalculatedFieldTrigger;");
		sg.wl("import org.openlowcode.server.data.formula.SumOnChildren;");
		sg.wl("import org.openlowcode.server.data.formula.LocalPath;");
		sg.wl("import org.openlowcode.server.data.formula.PathToCalculatedField;");
		sg.wl("import org.openlowcode.server.data.formula.SumOnLinkRightObject;");
		sg.wl("import org.openlowcode.server.data.formula.SumProductOnLinkRightObject;");
		sg.wl("import org.openlowcode.server.data.formula.LinkToLeftReverseNavigator;");

	}

	@Override
	public DataObjectDefinition getOwnerObject() {
		return this.parent;
	}

	@Override
	public String generateFormulaElement() {
		String objectclass = StringFormatter.formatForJavaClass(this.parent.getName());
		String propertynameclass = StringFormatter.formatForJavaClass(this.getName());
		return objectclass + ".getDefinition().get" + propertynameclass + "FormulaElement()";
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
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
