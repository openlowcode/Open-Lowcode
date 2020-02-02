/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A filter element allowing to filter data based on the values of the choice
 * field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of object
 */
public class ChoiceFilterElement<E extends Object>
		extends
		FilterElement<E> {

	private boolean needhelper = false;
	private ChoiceField field;

	/**
	 * create a choice filter element for the given choice field
	 * 
	 * @param field choice field to use as filter element
	 */
	public ChoiceFilterElement(ChoiceField field) {
		super(false);
		if (field == null)
			throw new RuntimeException("ChoiceField cannot be null");
		this.field = field;
	}

	/**
	 * create a choice filter element for the given choice field
	 * 
	 * @param field      choice field to use as filter element
	 * @param needhelper true if the filter needs an helper, false if data entered
	 *                   by the user
	 */
	public ChoiceFilterElement(ChoiceField field, boolean needhelper) {
		super(true);
		this.needhelper = needhelper;
		if (field == null)
			throw new RuntimeException("ChoiceField cannot be null");
		this.field = field;
	}

	/**
	 * create a choice filter element for the given choice field
	 * 
	 * @param field    choice field to use as filter element
	 * @param valuesin list of values that will be filtered
	 */
	public ChoiceFilterElement(ChoiceField field, ChoiceValue[] valuesin) {
		super(true);
		if (field == null)
			throw new RuntimeException("ChoiceField cannot be null");
		this.field = field;
	}

	@Override
	public void writeFilterCriteria(SourceGenerator sg, String reportname) throws IOException {

		String choiceclass = StringFormatter.formatForJavaClass(field.getChoice().getName()) + "ChoiceDefinition";
		String choicefield = field.getParentObject().getName().toLowerCase() + "_" + field.getName().toLowerCase();

		sg.wl("		SChoiceTextField<" + choiceclass + "> " + choicefield + " ");
		sg.wl("		= new SChoiceTextField<" + choiceclass + ">(\""
				+ StringFormatter.escapeforjavastring(field.getDisplayname()) + "\",\"" + choicefield.toUpperCase()
				+ "\",\"\",");
		sg.wl("				" + choiceclass + ".get(), null, this,true, false, false, false, launchreport);");
		sg.wl("		mainband.addElement(" + choicefield + ");");
		sg.wl("		" + choicefield + ".setLinkedData(this.get" + StringFormatter.formatForJavaClass(choicefield)
				+ "_thru());");

		sg.wl("		launchreport.set" + StringFormatter.formatForJavaClass(choicefield) + "(" + choicefield
				+ ".getChoiceInput());  ");
		sg.wl("		launchexcelreport.set" + StringFormatter.formatForJavaClass(choicefield) + "(" + choicefield
				+ ".getChoiceInput());  ");
	}

	@Override
	public DataObjectDefinition getParent() {
		return field.getParentObject();
	}

	@Override
	public ArgumentContent getArgumentContent(String suffix) {
		String suffixname = field.getParentObject().getName() + "_" + field.getName()
				+ (suffix != null ? (suffix.length() > 0 ? "_" + suffix : "") : "");
		suffixname = suffixname.toUpperCase();
		return new ChoiceArgument(suffixname, field.getChoice());
	}

	@Override
	public String[] getImportClasses() {
		return new String[0];
	}

	@Override
	protected boolean hasfilterbefore() {
		return true;
	}

	@Override
	protected boolean hasfilterafter() {
		return false;
	}

	@Override
	public void writeFilterInDataGathering(
			SourceGenerator sg,
			String prefixforlinkandchild,
			DataObjectDefinition reportroot,
			String reportname) throws IOException {
		String objectattribute = StringFormatter.formatForAttribute(field.getParentObject().getName());
		String objectfielduc = StringFormatter.formatForJavaClass(field.getName());
		String reportnameclass = StringFormatter.formatForJavaClass(reportname);
		String queryconditionname = objectattribute + "_step" + prefixforlinkandchild + "_filter";
		String fieldname = StringFormatter
				.formatForAttribute(field.getParentObject().getName() + "_" + field.getName());
		String choiceclass = StringFormatter.formatForJavaClass(field.getChoice().getName());
		String choiceattribute = StringFormatter.formatForAttribute(field.getChoice().getName());
		String fieldclass = StringFormatter.formatForJavaClass(field.getName());
		String objectclass = StringFormatter.formatForJavaClass(field.getParentObject().getName());
		if (!this.needhelper) {
			sg.wl("		if (" + fieldname + "!=null) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<ChoiceValue<" + choiceclass + "ChoiceDefinition>>");
			sg.wl("						(" + objectclass
					+ ".getDefinition().getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),");
			sg.wl("								" + objectclass + ".getDefinition().get" + fieldclass
					+ "FieldSchema(),new QueryOperatorEqual(), " + fieldname + ");");
			sg.wl("			" + queryconditionname + ".addCondition(filtercondition);");
			sg.wl("		}");
		} else {

			if (reportroot == null)
				throw new RuntimeException("Helper Choice Filter Element not implemented without root object");
			String rootobjectclass = StringFormatter.formatForJavaClass(reportroot.getName());

			sg.wl("		OrQueryCondition " + queryconditionname + "_" + choiceattribute
					+ "helper = new OrQueryCondition();");
			sg.wl("		Function<DataObjectId<" + rootobjectclass + ">,List<ChoiceValue<"
					+ choiceclass + "ChoiceDefinition>>> " + objectattribute + "_" + choiceattribute + "helper=");
			sg.wl("				new " + objectclass + fieldclass + "SelectionHelperFor" + reportnameclass + "();");
			sg.wl("		List<ChoiceValue<" + choiceclass + "ChoiceDefinition>> values = " + objectattribute + "_"
					+ choiceattribute + "helper.apply(parentid);");
			sg.wl("		if (values!=null) for (int i=0;i<values.size();i++) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<ChoiceValue<" + choiceclass + "ChoiceDefinition>>");
			sg.wl("						(" + objectclass
					+ ".getDefinition().getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),");
			sg.wl("								" + objectclass + ".getDefinition().get" + objectfielduc
					+ "FieldSchema(),new QueryOperatorEqual(),values.get(i));");
			sg.wl("			" + queryconditionname + "_" + choiceattribute + "helper.addCondition(filtercondition);");
			sg.wl("");
			sg.wl("		}");
			sg.wl("		" + queryconditionname + ".addCondition(" + queryconditionname + "_" + choiceattribute
					+ "helper);");

		}
	}

	@Override
	public String[] getImportClassesForAction(String reportname) {
		ArrayList<String> imports = new ArrayList<String>();
		if (this.needhelper) {
			String reportnameclass = StringFormatter.formatForJavaClass(reportname);
			String fieldclass = StringFormatter.formatForJavaClass(field.getName());
			String objectclass = StringFormatter.formatForJavaClass(field.getParentObject().getName());
			imports.add("import " + field.getParentObject().getOwnermodule().getPath() + ".utility." + objectclass
					+ fieldclass + "SelectionHelperFor" + reportnameclass + ";");
			imports.add("import org.openlowcode.server.data.storage.OrQueryCondition;");
			imports.add("import java.util.List;");
			imports.add("import java.util.Function;");
		}
		return imports.toArray(new String[0]);
	}

	@Override
	protected String getBlankValue() {
		return "null";
	}
}
