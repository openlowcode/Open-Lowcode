/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A filter element that will only show texts that corresponds to the pattern
 * given
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.6
 */
public class TextFilterElement
		extends
		FilterElement<String> {

	private StringField stringfield;
	@SuppressWarnings("unused")
	private boolean wildcardbefore;
	@SuppressWarnings("unused")
	private boolean wildcardafter;

	/**
	 * A filter element allowing to keep only items with the value corresponding to
	 * the string gien
	 * 
	 * @param stringfield    the field to filter
	 * @param wildcardbefore consider all values given with wildcard before (e.g. if
	 *                       given AB, CAB will match, ABC will not match)
	 * @param wildcardafter  consider all values given with wildcare after (e.g. if
	 *                       given AB, CAB will not match, ABC will match)
	 * @param needhelper     if true, filter element is provided by helper (code),
	 *                       if false, filter element is provided by widget
	 */
	public TextFilterElement(
			StringField stringfield,
			boolean wildcardbefore,
			boolean wildcardafter,
			boolean needhelper) {
		super(needhelper);
		this.stringfield = stringfield;
		this.wildcardbefore = wildcardbefore;
		this.wildcardafter = wildcardafter;

	}

	@Override
	public DataObjectDefinition getParent() {
		return stringfield.getParentObject();
	}

	@Override
	public ArgumentContent getArgumentContent(String suffix) {
		String suffixname = stringfield.getParentObject().getName() + "_" + stringfield.getName()
				+ (suffix != null ? (suffix.length() > 0 ? "_" + suffix : "") : "");
		suffixname = suffixname.toUpperCase();
		return new StringArgument(suffixname, stringfield.getLength());

	}

	@Override
	public boolean hasSuggestionValues() {
		if (!this.isHardCoded()) {
			return stringfield.hasListOfValuesHelper();
		}
		return false;
	}

	@Override
	public ArgumentContent getSuggestionArgumentContent(String suffix) {
		if (!this.isHardCoded())
			if (stringfield.hasListOfValuesHelper()) {
				String suffixname = stringfield.getParentObject().getName() + "_" + stringfield.getName()
						+ "_suggestions" + (suffix != null ? (suffix.length() > 0 ? "_" + suffix : "") : "");
				return new ArrayArgument(new StringArgument(suffixname, stringfield.getLength()));
			}
		return null;
	}

	@Override
	public void writeFilterCriteria(SourceGenerator sg, String reportname) throws IOException {

		String stringfieldattribute = stringfield.getParentObject().getName().toLowerCase() + "_"
				+ stringfield.getName().toLowerCase();
		String stringfieldclass = StringFormatter.formatForJavaClass(
				stringfield.getParentObject().getName().toLowerCase() + "_" + stringfield.getName().toLowerCase());

		sg.wl("		STextField " + stringfieldattribute + " ");
		sg.wl("		= new STextField(\"" + stringfield.getDisplayname() + "\",\"" + stringfieldclass.toUpperCase()
				+ "\",\"\"," + stringfield.getLength() + ", \"\", this,null);");
		sg.wl("		mainband.addElement(" + stringfieldattribute + ");");
		sg.wl("		" + stringfieldattribute + ".setTextBusinessData(this.get" + stringfieldclass + "_thru());");
		sg.wl("		" + stringfieldattribute + ".setSuggestions(this.get" + stringfieldclass + "_suggestions_thru());");
		sg.wl("		launchreport.set" + stringfieldclass + "(" + stringfieldattribute + ".getTextInput());  ");
		sg.wl("		launchreport.set" + stringfieldclass + "_suggestions(" + stringfieldattribute
				+ ".getSuggestions(true));");
		sg.wl("		launchexcelreport.set" + stringfieldclass + "(" + stringfieldattribute + ".getTextInput());  ");
		sg.wl("		launchexcelreport.set" + stringfieldclass + "_suggestions(" + stringfieldattribute
				+ ".getSuggestions(true));");

	}

	@Override
	public String[] getImportClasses() {
		return new String[0];
	}

	@Override
	public String[] getImportClassesForAction(String reportname) {
		ArrayList<String> imports = new ArrayList<String>();
		if (this.isHardCoded()) {
			String reportnameclass = StringFormatter.formatForJavaClass(reportname);
			String fieldclass = StringFormatter.formatForJavaClass(stringfield.getName());
			String objectclass = StringFormatter.formatForJavaClass(stringfield.getParentObject().getName());
			imports.add("import " + stringfield.getParentObject().getOwnermodule().getPath() + ".utility." + objectclass
					+ fieldclass + "SelectionHelperFor" + reportnameclass + ";");
			imports.add("import org.openlowcode.server.data.storage.OrQueryCondition;");
			imports.add("import java.util.List;");
			imports.add("import java.util.function.Function;");

		} else {
			imports.add("import " + stringfield.getParentObject().getOwnermodule().getPath() + ".data."
					+ StringFormatter.formatForJavaClass(stringfield.getParentObject().getName()) + ";");
		}

		return imports.toArray(new String[0]);
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
			String stepsuffix,
			DataObjectDefinition reportroot,
			String reportname) throws IOException {
		String objectattribute = StringFormatter.formatForAttribute(stringfield.getParentObject().getName());
		String objectfielduc = StringFormatter.formatForJavaClass(stringfield.getName());
		String reportnameclass = StringFormatter.formatForJavaClass(reportname);
		String queryconditionname = objectattribute + "_step" + stepsuffix + "_query";
		String fieldname = StringFormatter
				.formatForAttribute(stringfield.getParentObject().getName() + "_" + stringfield.getName());

		String fieldclass = StringFormatter.formatForJavaClass(stringfield.getName());
		String objectclass = StringFormatter.formatForJavaClass(stringfield.getParentObject().getName());
		if (!this.isHardCoded()) {
			sg.wl("		if (" + fieldname + "!=null) if ("+fieldname+".trim().length()>0) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<String>");
			sg.wl("						(" + objectclass
					+ ".getDefinition().getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),");
			sg.wl("								" + objectclass + ".getDefinition().get" + fieldclass
					+ "FieldSchema(),new QueryOperatorEqual<String>(), " + fieldname + ");");
			sg.wl("			" + queryconditionname + ".addCondition(filtercondition);");
			sg.wl("		}");
		} else {

			if (reportroot == null)
				throw new RuntimeException("Helper String Filter Element not implemented without root object");
			String rootobjectclass = StringFormatter.formatForJavaClass(reportroot.getName());

			sg.wl("		OrQueryCondition " + queryconditionname + "_" + objectattribute
					+ "helper = new OrQueryCondition();");
			sg.wl("		Function<DataObjectId<" + rootobjectclass + ">,List<String>> " + objectattribute + "_helper=");
			sg.wl("				new " + objectclass + fieldclass + "SelectionHelperFor" + reportnameclass + "();");
			sg.wl("		List<String> values = " + objectattribute + "_helper.apply(parentid);");
			sg.wl("		if (values!=null) for (int i=0;i<values.size();i++) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<String>");
			sg.wl("						(" + objectclass
					+ ".getDefinition().getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),");
			sg.wl("								" + objectclass + ".getDefinition().get" + objectfielduc
					+ "FieldSchema(),new QueryOperatorEqual(),values.get(i));");
			sg.wl("			" + queryconditionname + "_helper.addCondition(filtercondition);");
			sg.wl("");
			sg.wl("		}");
			sg.wl("		" + queryconditionname + ".addCondition(" + queryconditionname + "_helper);");
		}

	}

	@Override
	protected String getBlankValue() {
		return "null";
	}

	@Override
	public Field getFieldForSuggestion() {
		return this.stringfield;
	}

	@Override
	public boolean needArrayOfObjectId() {
		return false;
	}

}
