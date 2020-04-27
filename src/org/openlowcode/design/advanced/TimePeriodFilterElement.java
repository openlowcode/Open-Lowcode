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
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.TimePeriodField;
import org.openlowcode.design.data.argument.TimePeriodArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.tools.data.TimePeriod;

/**
 * A filter element that will only show time periods corresponding to the
 * selected values
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriodFilterElement
		extends
		FilterElement<TimePeriod> {

	private TimePeriodField timeperiodfield;
	private boolean needhelper = false;

	/**
	 * create a time period filter where filter criteria will be input ( a time
	 * period selection widget will appear to allow selection of one time period)
	 * 
	 * @param timeperiodfield field to use for filter on the current node object
	 */
	public TimePeriodFilterElement(TimePeriodField timeperiodfield) {
		super(false);
		if (timeperiodfield == null)
			throw new RuntimeException("timeperiodfield cannot be null");
		this.timeperiodfield = timeperiodfield;
	}

	/**
	 * create a time period filter where filter criteria will be either input or
	 * automatically selected through an helper that the user should develop
	 * 
	 * @param timeperiodfield field to use for filter on the current node object
	 * @param needhelper      if true, the user will develop an helper to select the
	 *                        good time period, if false, a field will appear for
	 *                        user selection on the smart report page
	 */
	public TimePeriodFilterElement(TimePeriodField timeperiodfield, boolean needhelper) {
		super(true);
		this.needhelper = needhelper;
		if (timeperiodfield == null)
			throw new RuntimeException("timeperiodfield cannot be null");
		this.timeperiodfield = timeperiodfield;
	}

	@Override
	public DataObjectDefinition getParent() {
		return timeperiodfield.getParentObject();
	}

	@Override
	public ArgumentContent getArgumentContent(String suffix) {
		String suffixname = timeperiodfield.getParentObject().getName() + "_" + timeperiodfield.getName()
				+ (suffix != null ? (suffix.length() > 0 ? "_" + suffix : "") : "");
		suffixname = suffixname.toUpperCase();
		return new TimePeriodArgument(suffixname);
	}

	@Override
	public void writeFilterCriteria(SourceGenerator sg, String reportname) throws IOException {

		String tpfieldname = timeperiodfield.getParentObject().getName().toLowerCase() + "_"
				+ timeperiodfield.getName().toLowerCase();

		sg.wl("		STimePeriodField " + tpfieldname + " ");
		sg.wl("		= new STimePeriodField(\"" + StringFormatter.escapeforjavastring(timeperiodfield.getDisplayname())
				+ "\",\"" + timeperiodfield.getJavaType().toUpperCase() + "\",\"\",");
		sg.wl("			TimePeriod.PeriodType." + timeperiodfield.getPeriodType()
				+ ", null, this,true, false, false, false, launchreport);");
		sg.wl("		mainband.addElement(" + tpfieldname + ");");
		sg.wl("		" + tpfieldname + ".setLinkedData(this.get" + StringFormatter.formatForJavaClass(tpfieldname)
				+ "_thru());");

		sg.wl("		launchreport.set" + StringFormatter.formatForJavaClass(tpfieldname) + "(" + tpfieldname
				+ ".getTimePeriodInput());  ");
		sg.wl("		launchexcelreport.set" + StringFormatter.formatForJavaClass(tpfieldname) + "(" + tpfieldname
				+ ".getTimePeriodInput());  ");
	}

	@Override
	public String[] getImportClasses() {
		return new String[] { "import org.openlowcode.tools.data.TimePeriod;",
				"import org.openlowcode.server.graphic.widget.STimePeriodField;" };
	}

	@Override
	public String[] getImportClassesForAction(String reportname) {
		ArrayList<String> imports = new ArrayList<String>();
		if (needhelper) {
			String reportnameclass = StringFormatter.formatForJavaClass(reportname);
			String fieldclass = StringFormatter.formatForJavaClass(timeperiodfield.getName());
			String objectclass = StringFormatter.formatForJavaClass(timeperiodfield.getParentObject().getName());
			imports.add("import " + timeperiodfield.getParentObject().getOwnermodule().getPath() + ".utility."
					+ objectclass + fieldclass + "SelectionHelperFor" + reportnameclass + ";");
			imports.add("import org.openlowcode.server.data.storage.OrQueryCondition;");
			imports.add("import java.util.List;");
			imports.add("import java.util.function.Function;");
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
			String prefixforlinkandchild,
			DataObjectDefinition reportroot,
			String reportname) throws IOException {
		String objectattribute = StringFormatter.formatForAttribute(timeperiodfield.getParentObject().getName());
		String objectfielduc = StringFormatter.formatForJavaClass(timeperiodfield.getName());
		String reportnameclass = StringFormatter.formatForJavaClass(reportname);
		String queryconditionname = objectattribute + "_step" + prefixforlinkandchild + "_query";
		String fieldname = StringFormatter
				.formatForAttribute(timeperiodfield.getParentObject().getName() + "_" + timeperiodfield.getName());

		String fieldclass = StringFormatter.formatForJavaClass(timeperiodfield.getName());
		String objectclass = StringFormatter.formatForJavaClass(timeperiodfield.getParentObject().getName());
		if (!this.needhelper) {
			sg.wl("		if (" + fieldname + "!=null) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<TimePeriod>");
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

			sg.wl("		OrQueryCondition " + queryconditionname + "_timeperiodhelper = new OrQueryCondition();");
			sg.wl("		Function<DataObjectId<" + rootobjectclass + ">,List<TimePeriod>> "
					+ objectattribute + "_timeperiodhelper=");
			sg.wl("				new " + objectclass + fieldclass + "SelectionHelperFor" + reportnameclass + "();");
			sg.wl("		List<TimePeriod> values = " + objectattribute + "_timeperiodhelper.apply(parentid);");
			sg.wl("		if (values!=null) for (int i=0;i<values.size();i++) {");
			sg.wl("			QueryCondition filtercondition =");
			sg.wl("					new SimpleQueryCondition<TimePeriod>");
			sg.wl("						(" + objectclass
					+ ".getDefinition().getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),");
			sg.wl("								" + objectclass + ".getDefinition().get" + objectfielduc
					+ "FieldSchema(),new QueryOperatorEqual(),values.get(i));");
			sg.wl("			" + queryconditionname + "_timeperiodhelper.addCondition(filtercondition);");
			sg.wl("");
			sg.wl("		}");
			sg.wl("		" + queryconditionname + ".addCondition(" + queryconditionname + "_timeperiodhelper);");

		}

	}

	@Override
	protected String getBlankValue() {
		return "null";
	}

	@Override
	public boolean hasSuggestionValues() {
		return false;
	}

	@Override
	public ArgumentContent getSuggestionArgumentContent(String suffix) {
		return null;
	}

}
