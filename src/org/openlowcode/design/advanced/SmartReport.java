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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import java.util.logging.Logger;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.action.StaticActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.NodeTreeArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.properties.basic.FlexibleDecimalFields;
import org.openlowcode.design.data.properties.basic.TransientParent;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.pages.DynamicPageDefinition;

/**
 * A smart report performs multi-object data navigation to display data:
 * <ul>
 * <li>supporting parent and link object</li>
 * <li>displaying data as a hierarchy with amounts consolidated (typically, sum
 * or average)</li>
 * <li>link back from the report to main objects</li>
 * <li>with possibility to filter data on specific criteria</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SmartReport
		extends
		AdvancedDesignFeature {
	private static Logger logger = Logger.getLogger(SmartReport.class.getName());
	private SmartReportNode rootnode;
	private ActionDefinition blankreport;
	private DynamicActionDefinition launchreport;
	private String label;
	private HashMap<String, MainReportValue> totalcolumns;
	private ArrayList<String> totalkeys;
	private DynamicActionDefinition launchexcelreport;

	/**
	 * Create a smart report with the provided root node
	 * 
	 * @param name     unique name of the report for the module (should be a valid
	 *                 java language attribute name)
	 * @param label    a plain language label in the default language
	 * @param rootnode the root node of the report
	 */
	public SmartReport(String name, String label, SmartReportNode rootnode) {
		super(name);
		this.rootnode = rootnode;
		this.label = label;
	}

	@Override
	public ActionDefinition[] getActionsInGroup() {
		return new ActionDefinition[] { blankreport, launchreport, launchexcelreport };
	}

	@Override
	public void generateActionsAndPages() {
		logger.severe(" +-------------------------------------- generating actions and pages for " + label
				+ " ---------------+");
		blankreport = null;
		launchreport = new DynamicActionDefinition("LAUNCHREPORTFOR" + this.getName(), true);
		launchexcelreport = new DynamicActionDefinition("LAUNCHEXCELREPORTFOR" + this.getName(), true);
		if (rootnode.getRelevantObject() != null) {
			DynamicActionDefinition dynamicblankreport = new DynamicActionDefinition(
					"LAUNCHBLANKREPORTFOR" + this.getName(), true);
			dynamicblankreport.setButtonlabel(this.label);
			dynamicblankreport
					.addInputArgumentAsAccessCriteria(new ObjectIdArgument("PARENTID", rootnode.getRelevantObject()));
			blankreport = dynamicblankreport;
			UniqueIdentified rootobjectuniqueidentified = (UniqueIdentified) rootnode.getRelevantObject()
					.getPropertyByName("UNIQUEIDENTIFIED");
			String menuname = this.getMenuForObject();
			if (menuname!=null) {
				rootobjectuniqueidentified.addActionOnObjectId(dynamicblankreport,menuname);
					
			} else {
				rootobjectuniqueidentified.addActionOnObjectId(dynamicblankreport);
			}
			this.getParentModule().addAction(dynamicblankreport);
			dynamicblankreport.addOutputArgument(new ObjectIdArgument("PARENTID_THRU", rootnode.getRelevantObject()));
			dynamicblankreport.addOutputArgument(new StringArgument("PARENTOBJECTLABEL_THRU", 256));

			launchreport
					.addInputArgumentAsAccessCriteria(new ObjectIdArgument("PARENTID", rootnode.getRelevantObject()));
			launchexcelreport
					.addInputArgumentAsAccessCriteria(new ObjectIdArgument("PARENTID", rootnode.getRelevantObject()));
			launchreport.addInputArgument(new StringArgument("PARENTOBJECTLABEL", 256));
			launchexcelreport.addInputArgument(new StringArgument("PARENTOBJECTLABEL", 256));
			launchreport.addOutputArgument(new ObjectIdArgument("PARENTID_THRU", rootnode.getRelevantObject()));
			launchreport.addOutputArgument(new StringArgument("PARENTOBJECTLABEL_THRU", 256));
			launchexcelreport.addOutputArgument(new LargeBinaryArgument("EXCELREPORT", false));

		} else {
			StaticActionDefinition staticblankreport = new StaticActionDefinition(
					"LAUNCHBLANKREPORTFOR" + this.getName(), true);
			staticblankreport.setButtonlabel(this.label);
			blankreport = staticblankreport;

			this.getParentModule().addasMenuAction(staticblankreport);
		}

		// Definition here of criteria that are set by the user (Filter Elements)
		List<FilterElement<?>> filterelements = this.rootnode.getAllFilterElements();
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				// for not harcoded criteria, generate the input in blank
				logger.warning(
						" +---+ Smart Report " + label + " adding not hardcoded " + thisfilterelement.toString());
				ArgumentContent outputargumentforblank = thisfilterelement.getArgumentContent(null);
				ArgumentContent inputargumentforaction = thisfilterelement.getArgumentContent(null);
				ArgumentContent outputargumentforaction = thisfilterelement.getArgumentContent("THRU");
				blankreport.addOutputArgument(outputargumentforblank);
				launchreport.addInputArgument(inputargumentforaction);
				launchexcelreport.addInputArgument(inputargumentforaction);

				launchreport.addOutputArgument(outputargumentforaction);
				if (thisfilterelement.hasSuggestionValues()) {
					ArgumentContent outputsuggestionargumentforblank = thisfilterelement
							.getSuggestionArgumentContent(null);
					ArgumentContent inputsuggestionargumentforaction = thisfilterelement
							.getSuggestionArgumentContent(null);
					ArgumentContent outputsuggestionargumentforaction = thisfilterelement
							.getSuggestionArgumentContent("THRU");
					blankreport.addOutputArgument(outputsuggestionargumentforblank);
					launchreport.addInputArgument(inputsuggestionargumentforaction);
					launchreport.addOutputArgument(outputsuggestionargumentforaction);
					launchexcelreport.addInputArgument(inputsuggestionargumentforaction);
				}

			} else {
				logger.warning(
						" +---+ Smart Report " + label + " not added as hardcoded " + thisfilterelement.toString());
			}
		}

		this.getParentModule().addAction(launchreport);
		this.getParentModule().addAction(launchexcelreport);
		DynamicPageDefinition reportpage = new DynamicPageDefinition("REPORTFOR" + this.getName());

		this.getParentModule().AddPage(reportpage);

		DataObjectDefinition reportobject = new DataObjectDefinition("REPORTFOR" + this.getName(), this.label,
				this.getParentModule(), true);
		reportobject.addField(
				new StringField("LABEL", "Label", "Category or object name", 512, StringField.INDEXTYPE_NONE, 800));
		ArrayList<Field> fieldstoaddbefore = new ArrayList<Field>();
		rootnode.collectFieldsOnNodeAndChildren(fieldstoaddbefore, true);
		for (int i = 0; i < fieldstoaddbefore.size(); i++)
			reportobject.addField(fieldstoaddbefore.get(i));
		reportobject.addProperty(new FlexibleDecimalFields());
		// ---------------- proceed total columns ------------------------------------
		totalcolumns = new HashMap<String, MainReportValue>();
		rootnode.collectTotalColumns(totalcolumns, 1);
		totalkeys = new ArrayList<String>(totalcolumns.keySet());
		Collections.sort(totalkeys);
		for (int i = 0; i < totalkeys.size(); i++) {
			String label = totalkeys.get(i);
			MainReportValue thisreportvalue = totalcolumns.get(label);
			Field field = thisreportvalue.copyFieldForTotal("TOTAL" + i, "Total" + (label != null ? " " + label : ""));
			thisreportvalue.setTotalIndex(i);
			field.setDisplayPriority(100 - i);
			reportobject.addField(field);
		}
		ArrayList<Field> fieldstoaddafter = new ArrayList<Field>();
		rootnode.collectFieldsOnNodeAndChildren(fieldstoaddafter, false);
		for (int i = 0; i < fieldstoaddafter.size(); i++)
			reportobject.addField(fieldstoaddafter.get(i));

		DataObjectDefinition backtoobject = this.rootnode.getBackToObjet(0);
		if (backtoobject != null) {
			reportobject.addProperty(new TransientParent("PARENTFORCLICK", backtoobject));
		}
		// ---------------------------- add report output object

		blankreport.addOutputArgument(new NodeTreeArgument(new ObjectArgument("REPORTCONTENT", reportobject)));
		launchreport.addOutputArgument(new NodeTreeArgument(new ObjectArgument("REPORTCONTENT", reportobject)));
		reportpage.linkPageToAction(launchreport);
	}

	@Override
	public void generateActionsAndPagesToFile(String actionfolder, String pagefolder, String author, String version)
			throws IOException {
		String launchblankaction = actionfolder + "Atg"
				+ StringFormatter.formatForJavaClass("LAUNCHBLANKREPORTFOR" + this.getName()) + "Action.java";
		logger.info("generating file " + launchblankaction);
		this.generateLaunchBlankActionToFile(new SourceGenerator(new File(launchblankaction), author, version));
		String page = pagefolder + "Atg" + StringFormatter.formatForJavaClass("REPORTFOR" + this.getName())
				+ "Page.java";
		logger.info("generating file " + launchblankaction);
		this.generatePageToFile(new SourceGenerator(new File(page), author, version));
		String launchaction = actionfolder + "Atg"
				+ StringFormatter.formatForJavaClass("LAUNCHREPORTFOR" + this.getName()) + "Action.java";
		logger.info("generating file " + launchaction);
		generateActionToFile(new SourceGenerator(new File(launchaction), author, version));
		String launchexportaction = actionfolder + "Atg"
				+ StringFormatter.formatForJavaClass("LAUNCHEXCELREPORTFOR" + this.getName()) + "Action.java";
		logger.info("generating file " + launchexportaction);
		this.generateExcelExportActionToFile(new SourceGenerator(new File(launchexportaction), author, version));
	}

	private void generatePageToFile(SourceGenerator sg) throws IOException {
		String modulepath = this.getParentModule().getPath();
		String reportvariablename = StringFormatter.formatForAttribute(this.getName());
		List<FilterElement<?>> filterelements = this.rootnode.getAllFilterElements();
		boolean hasparentobject = false;
		DataObjectDefinition parentobject = rootnode.getRelevantObject();
		String parentobjectclass = null;
		String parentobjectvariable = null;
		if (rootnode.getRelevantObject() != null) {
			hasparentobject = true;
			parentobjectclass = StringFormatter.formatForJavaClass(parentobject.getName());
			parentobjectvariable = StringFormatter.formatForAttribute(parentobject.getName());

		}
		sg.wl("package " + modulepath + ".page.generated;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
		sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
		sg.wl("import org.openlowcode.server.graphic.widget.SChoiceTextField;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArrayField;");

		if (hasparentobject) {
			sg.wl("import " + parentobject.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
			sg.wl("import " + parentobject.getOwnermodule().getPath() + ".action.generated.AtgShow"
					+ parentobjectvariable + "Action;");
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				ArrayList<String> imports = thisargument.getImports();
				if (imports != null)
					for (int j = 0; j < imports.size(); j++)
						sg.wl(imports.get(j));
				String[] importclasses = thisfilterelement.getImportClasses();
				if (importclasses != null)
					for (int j = 0; j < importclasses.length; j++)
						sg.wl(importclasses[j]);
			}

		}
		DataObjectDefinition backtoobject = this.rootnode.getBackToObjet(0);
		sg.wl("import " + modulepath + ".data.Reportfor" + reportvariablename + ";");
		if (backtoobject != null) {
			sg.wl("import " + backtoobject.getOwnermodule().getPath() + ".action.generated.AtgShow"
					+ StringFormatter.formatForAttribute(backtoobject.getName()) + "Action;");
		}
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import org.openlowcode.server.graphic.widget.SFileDownloader;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.action.SInlineEchoActionRef;");
		sg.wl("import org.openlowcode.server.graphic.widget.SFieldSearcher;");
		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectTreeArray;");
		sg.wl("import " + modulepath + ".action.generated.AtgLaunchreportfor" + reportvariablename + "Action;");
		sg.wl("import " + modulepath + ".action.generated.AtgLaunchexcelreportfor" + reportvariablename + "Action;");

		sg.wl("");
		sg.wl("public class AtgReportfor" + reportvariablename + "Page extends AbsReportfor" + reportvariablename
				+ "Page {");
		sg.wl("");
		sg.wl("	public AtgReportfor" + reportvariablename + "Page(");
		if (hasparentobject)
			sg.wl("			DataObjectId<" + parentobjectclass + "> parentid_thru, String parentobjectlabel_thru,");
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				sg.wl("			" + thisargument.getType() + " " + thisargument.getName().toLowerCase() + "_thru,");
				if (thisfilterelement.hasSuggestionValues())
					sg.wl("			" + thisargument.getType() + "[] " + thisargument.getName().toLowerCase()
							+ "_suggestions_thru,");

			}
		}
		sg.wl("			NodeTree<Reportfor" + reportvariablename + "> reportcontent)  {");
		sg.wl("			super(" + (hasparentobject ? "parentid_thru, parentobjectlabel_thru," : ""));
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				sg.wl("				" + thisargument.getName().toLowerCase() + "_thru,");
				if (thisfilterelement.hasSuggestionValues())
					sg.wl("			" + thisargument.getName().toLowerCase() + "_suggestions_thru,");

			}
		}
		sg.wl("				reportcontent);");

		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(");
		if (hasparentobject)
			sg.wl("			DataObjectId<" + parentobjectclass + "> parentid_thru, String parentobjectlabel_thru,");
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				sg.wl("			" + thisargument.getType() + " " + thisargument.getName().toLowerCase() + "_thru,");
				if (thisfilterelement.hasSuggestionValues())
					sg.wl("			" + thisargument.getType() + "[] " + thisargument.getName().toLowerCase()
							+ "_suggestions_thru,");

			}
		}
		sg.wl("			NodeTree<Reportfor" + reportvariablename + "> reportcontent)  {");
		if (hasparentobject) {
			sg.wl("		return \"" + StringFormatter.escapeforjavastring(this.label) + " : \"+parentobjectlabel_thru;");
		} else {
			sg.wl("		return \"" + StringFormatter.escapeforjavastring(this.label) + "\";");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");

		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
		sg.wl("		mainband.addElement(new SPageText(\"" + StringFormatter.escapeforjavastring(this.label)
				+ "\",SPageText.TYPE_TITLE,this));");
		sg.wl("		// action for launching report");
		sg.wl("		AtgLaunchreportfor" + reportvariablename + "Action.ActionRef launchreport = AtgLaunchreportfor"
				+ reportvariablename + "Action.get().getActionRef();");
		sg.wl("		AtgLaunchexcelreportfor" + reportvariablename
				+ "Action.InlineActionRef launchexcelreport = AtgLaunchexcelreportfor" + reportvariablename
				+ "Action.get().getInlineActionRef();");
		if (hasparentobject) {
			sg.wl("		// element for parent");

			sg.wl("		SObjectIdStorage<" + parentobjectclass + "> reportparentid = new SObjectIdStorage<"
					+ parentobjectclass + ">(\"PARENTID\", this,this.getParentid_thru());");
			sg.wl("		mainband.addElement(reportparentid);");

			sg.wl("		STextField parentobjectlabel = new STextField(\"Context\",");
			sg.wl("				\"PARENTLABEL\", ");
			sg.wl("				\"Scope on which the report is executed\",");
			sg.wl("				256, null,true, this, true, false, false, null);");
			sg.wl("		parentobjectlabel.setTextBusinessData(this.getParentobjectlabel_thru());");
			sg.wl("		mainband.addElement(parentobjectlabel);");
			sg.wl("		launchreport.setParentid(reportparentid.getObjectIdInput()); ");
			sg.wl("		launchexcelreport.setParentid(reportparentid.getObjectIdInput()); ");

			sg.wl("		launchreport.setParentobjectlabel(parentobjectlabel.getTextInput()); ");
			sg.wl("		launchexcelreport.setParentobjectlabel(parentobjectlabel.getTextInput()); ");
		}

		sg.wl("		// choices for filter criteria");

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				sg.wl("		// ------- Filter criteria " + thisfilterelement.getClass().toString());
				thisfilterelement.writeFilterCriteria(sg, this.getName());
			}
		}

		sg.wl("		// ------------ Button Band");
		sg.wl("		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);");
		sg.wl("		");
		if (hasparentobject) {
			sg.wl("		AtgShow" + parentobjectvariable + "Action.ActionRef backtoparent = AtgShow"
					+ parentobjectvariable + "Action.get().getActionRef();");
			sg.wl("		backtoparent.setId(reportparentid.getObjectIdInput()); ");
			sg.wl("		SActionButton backtoparentbutton = new SActionButton(\"Back\",backtoparent,this);");
			sg.wl("		buttonband.addElement(backtoparentbutton);");

		}
		sg.wl("		SActionButton launchreportbutton = new SActionButton(\"Launch Report\", launchreport, this);");

		sg.wl("		SActionButton exportreporttoexcel = new SActionButton(\"Export Report\",launchexcelreport,this);");
		sg.wl("		buttonband.addElement(exportreporttoexcel);");
		sg.wl("		SFileDownloader exportreporttoexceldownload = new SFileDownloader(\"EXCELEXPORTDOWNLOADER\", ");
		sg.wl("				this, launchexcelreport,AtgLaunchexcelreportfor" + reportvariablename
				+ "Action.get().getExcelreportRef());");
		sg.wl("		buttonband.addElement(exportreporttoexceldownload);");

		sg.wl("		buttonband.addElement(launchreportbutton);");
		sg.wl("		mainband.addElement(buttonband);");
		sg.wl("		");

		sg.wl("		// --------------- Report result");
		sg.wl("		SObjectTreeArray<Reportfor" + reportvariablename
				+ "> reportresult = new  SObjectTreeArray<Reportfor" + reportvariablename + ">(");
		sg.wl("				\"REPORTRESULT\",");
		sg.wl("				this.getReportcontent(),");
		sg.wl("				Reportfor" + reportvariablename + ".getDefinition(),");
		sg.wl("				this);");
		sg.wl("		mainband.addElement(reportresult);");

		if (backtoobject != null) {
			String backtoobjectvariable = StringFormatter.formatForAttribute(backtoobject.getName());
			sg.wl("		AtgShow" + backtoobjectvariable + "Action.ActionRef show" + backtoobjectvariable
					+ "action = AtgShow" + backtoobjectvariable + "Action.get().getActionRef();");
			sg.wl("		show" + backtoobjectvariable + "action.setId(reportresult.getAttributeInput(Reportfor"
					+ reportvariablename + ".getTransientparentforparentforclickidMarker())); ");
			sg.wl("		reportresult.addDefaultAction(show" + backtoobjectvariable + "action);");

		}

		sg.wl("		return mainband;");

		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();

	}

	private void generateExcelExportActionToFile(SourceGenerator sg) throws IOException {
		String modulepath = this.getParentModule().getPath();
		String reportvariablename = StringFormatter.formatForAttribute(this.getName());
		List<FilterElement<?>> filterelements = this.rootnode.getAllFilterElements();
		boolean hasparentobject = false;
		DataObjectDefinition parentobject = rootnode.getRelevantObject();
		String parentobjectclass = null;
		if (rootnode.getRelevantObject() != null) {
			hasparentobject = true;
			parentobjectclass = StringFormatter.formatForJavaClass(parentobject.getName());

		}
		sg.wl("package " + modulepath + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import java.util.List;");
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileExtractor;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import org.openlowcode.server.action.utility.SmartReportUtility;");

		sg.wl("import " + modulepath + ".data.Reportfor" + reportvariablename + ";");
		sg.wl("");
		if (hasparentobject) {
			sg.wl("import " + parentobject.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
		}

		rootnode.printImports(sg);

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);

			ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
			ArrayList<String> imports = thisargument.getImports();
			if (imports != null)
				for (int j = 0; j < imports.size(); j++)
					sg.wl(imports.get(j));

			String[] importsforaction = thisfilterelement.getImportClassesForAction(this.getName());
			if (importsforaction != null)
				for (int j = 0; j < importsforaction.length; j++)
					sg.wl(importsforaction[j]);
		}

		sg.wl("public class AtgLaunchexcelreportfor" + reportvariablename + "Action extends AbsLaunchexcelreportfor"
				+ reportvariablename + "Action {");
		sg.wl("");
		sg.wl("	public AtgLaunchexcelreportfor" + reportvariablename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SFile executeActionLogic(");
		if (hasparentobject) {
			sg.wl("			DataObjectId<" + parentobjectclass + "> parentid,");
			sg.wl("			String parentobjectlabel,");
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				sg.wl("			" + thisargument.getType() + " " + thisargument.getName().toLowerCase() + ",");
				if (thisfilterelement.hasSuggestionValues()) {
					sg.wl("			String[] " + thisargument.getName().toLowerCase() + "_suggestions,");
				}
			}
		}

		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");

		StringBuffer attributes = new StringBuffer();
		if (hasparentobject)
			attributes.append("parentid,parentobjectlabel,");

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {

				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);

				attributes.append(thisargument.getName().toLowerCase());
				attributes.append(",");
				if (thisfilterelement.hasSuggestionValues()) {
					attributes.append(thisargument.getName().toLowerCase() + "_suggestions,");
				}
			}
		}
		sg.wl("		NodeTree<Reportfor" + reportvariablename + "> treeresult = AtgLaunchreportfor" + reportvariablename
				+ "Action.get()");
		sg.wl("				.executeActionLogic(" + attributes + " datafilter).getReportcontent();");
		sg.wl("		FlatFileExtractor<Reportfor" + reportvariablename + "> extractor = new FlatFileExtractor<Reportfor"
				+ reportvariablename + ">(Reportfor" + reportvariablename + ".getDefinition());");
		sg.wl("		return extractor.extractToExcel(treeresult);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(SFile excelreport)  {");
		sg.wl("		throw new RuntimeException(\"Should only be called as inline action\");");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	private void generateActionToFile(SourceGenerator sg) throws IOException {
		String modulepath = this.getParentModule().getPath();
		String reportvariablename = StringFormatter.formatForAttribute(this.getName());
		List<FilterElement<?>> filterelements = this.rootnode.getAllFilterElements();
		boolean hasparentobject = false;
		DataObjectDefinition parentobject = rootnode.getRelevantObject();
		String parentobjectclass = null;
		if (rootnode.getRelevantObject() != null) {
			hasparentobject = true;
			parentobjectclass = StringFormatter.formatForJavaClass(parentobject.getName());

		}

		sg.wl("package " + modulepath + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import java.util.List;");
		sg.wl("");
		if (hasparentobject) {
			sg.wl("import " + parentobject.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
		}

		rootnode.printImports(sg);

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);

			ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
			ArrayList<String> imports = thisargument.getImports();
			if (imports != null)
				for (int j = 0; j < imports.size(); j++)
					sg.wl(imports.get(j));

			String[] importsforaction = thisfilterelement.getImportClassesForAction(this.getName());
			if (importsforaction != null)
				for (int j = 0; j < importsforaction.length; j++)
					sg.wl(importsforaction[j]);
		}

		sg.wl("import " + modulepath + ".page.generated.AtgReportfor" + reportvariablename + "Page;");
		sg.wl("import " + modulepath + ".data.Reportfor" + reportvariablename + ";");

		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.action.utility.SmartReportUtility;");

		sg.wl("import org.openlowcode.server.data.helpers.ReportTree;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.data.properties.LinkedtoparentQueryHelper;");
		sg.wl("import org.openlowcode.server.data.properties.FlexibledecimalfieldsDefinitionDynamicHelper;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");

		sg.wl("import org.openlowcode.tools.misc.CompositeObjectMap;");
		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import java.util.ArrayList;");
		sg.wl("import java.math.BigDecimal;");
		sg.wl("");
		sg.wl("public class AtgLaunchreportfor" + reportvariablename + "Action extends AbsLaunchreportfor"
				+ reportvariablename + "Action {");
		sg.wl("");
		sg.wl("	public AtgLaunchreportfor" + reportvariablename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		if (hasparentobject) {
			sg.wl("			DataObjectId<" + parentobjectclass + "> parentid,");
			sg.wl("			String parentobjectlabel,");
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
				sg.wl("			" + thisargument.getType() + " " + thisargument.getName().toLowerCase() + ",");
				if (thisfilterelement.hasSuggestionValues()) {
					sg.wl("			String[] " + thisargument.getName().toLowerCase() + "_suggestions,");

				}
			}
		}

		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		// -------------------- Gather Data -----------------------------------------------");
		rootnode.gatherData(sg, parentobject, this.getName());
		sg.wl("");
		sg.wl("		// -------------------- Order Data ------------------------------------------------");
		rootnode.orderData(sg, parentobject, this.getName());
		sg.wl("		// -------------------- Column Generation -----------------------------------------");

		sg.wl("		SmartReportUtility.ColumnList columnlist = new SmartReportUtility.ColumnList();");
		rootnode.setColumns(sg, parentobject, this.getName());

		sg.wl("		columnlist.Order();");
		sg.wl("		FlexibledecimalfieldsDefinitionDynamicHelper<Reportfor" + reportvariablename + "> dynamichelper");
		sg.wl("			= new FlexibledecimalfieldsDefinitionDynamicHelper<Reportfor" + reportvariablename
				+ ">(Reportfor" + reportvariablename + ".getDefinition());");
		sg.wl("		for (int i=0;i<columnlist.getSize();i++)");
		sg.wl("			dynamichelper.addField(columnlist.getColumn(i),columnlist.getColumn(i),300-i);");
		StringBuffer totalconsolidators = new StringBuffer("");
		if (this.totalkeys != null)
			if (this.totalkeys.size() > 0) {
				sg.wl("// --------------------  Total Consolidators -----------------------------------");
				for (int i = 0; i < this.totalkeys.size(); i++) {
					totalconsolidators.append(",total");
					totalconsolidators.append(i);
					totalconsolidators.append("consolidator");

					sg.wl("		ReportTree.Consolidator<Reportfor" + reportvariablename + "> total" + i
							+ "consolidator = new ReportTree.Consolidator<Reportfor" + reportvariablename + ">() {");
					sg.wl("			@Override");
					sg.wl("			public void consolidate(Reportfor" + reportvariablename + " parent, Reportfor"
							+ reportvariablename + " child)  {");
					sg.wl("				parent.setTotal" + i + "(ReportTree.sumIfNotNull(parent.getTotal" + i
							+ "(),child.getTotal0()));");
					sg.wl("			}};");

				}
			}

		sg.wl("		// --------------------  Create Report Tree -----------------------------------");

		sg.wl("		ReportTree<Reportfor" + reportvariablename + "> reporttree = new ReportTree<Reportfor"
				+ reportvariablename + ">(");
		sg.wl("				Reportfor" + reportvariablename + ".getDefinition(),");
		sg.wl("				(object,name) -> object.setLabel(name),");
		sg.wl("				object -> object.getLabel(),");
		sg.wl("				new ReportTree.Consolidator[] {dynamichelper" + totalconsolidators.toString() + "},");
		sg.wl("				(object) -> object.setDynamicHelperForFlexibledecimalfields(dynamichelper),");
		sg.wl("				\"Grand Total\");");
		sg.wl("		ArrayList<String> rootclassification = new ArrayList<String>();");
		sg.wl("		BigDecimal rootmultiplier = new BigDecimal(1);");
		rootnode.buildReportTree(sg, parentobject, this.getName(), this);

		StringBuffer attributes = new StringBuffer();
		if (hasparentobject)
			attributes.append("parentid,parentobjectlabel,");

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {

				ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);

				attributes.append(thisargument.getName().toLowerCase());
				attributes.append(",");
				if (thisfilterelement.hasSuggestionValues()) {
					attributes.append(thisargument.getName().toLowerCase() + "_suggestions");
					attributes.append(",");
				}
			}
		}

		sg.wl("		return new ActionOutputData(" + attributes.toString());
		sg.wl("				reporttree.generateNodeTree(Reportfor" + reportvariablename + ".getDefinition()));");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new AtgReportfor" + reportvariablename + "Page(");
		if (hasparentobject) {
			sg.wl("				logicoutput.getParentid_thru(),");
			sg.wl("				logicoutput.getParentobjectlabel_thru(),");
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				// for not harcoded criteria, generate the input in blank

				ArgumentContent outputargumentforblank = thisfilterelement.getArgumentContent("THRU");
				sg.wl("				logicoutput.get"
						+ StringFormatter.formatForJavaClass(outputargumentforblank.getName()) + "(),");
				if (thisfilterelement.hasSuggestionValues()) {
					sg.wl("				logicoutput.get" + StringFormatter.formatForJavaClass(
							thisfilterelement.getSuggestionArgumentContent("THRU").getName()) + "(),");
				}
			}
		}
		sg.wl("				logicoutput.getReportcontent());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

	private void generateLaunchBlankActionToFile(SourceGenerator sg) throws IOException {
		String modulepath = this.getParentModule().getPath();
		String reportvariablename = StringFormatter.formatForAttribute(this.getName());
		List<FilterElement<?>> filterelements = this.rootnode.getAllFilterElements();
		boolean hasparentobject = false;
		DataObjectDefinition parentobject = rootnode.getRelevantObject();
		String parentobjectclass = null;
		String parentobjectvariable = null;
		if (rootnode.getRelevantObject() != null) {
			hasparentobject = true;
			parentobjectclass = StringFormatter.formatForJavaClass(parentobject.getName());
			parentobjectvariable = StringFormatter.formatForAttribute(parentobject.getName());

		}

		sg.wl("package " + modulepath + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");

		sg.wl("");
		if (hasparentobject) {
			sg.wl("import " + parentobject.getOwnermodule().getPath() + ".data." + parentobjectclass + ";");
		}

		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import " + modulepath + ".page.generated.AtgReportfor" + reportvariablename + "Page;");
		sg.wl("import " + modulepath + ".data.Reportfor" + reportvariablename + ";");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);

			ArgumentContent thisargument = thisfilterelement.getArgumentContent(null);
			ArrayList<String> imports = thisargument.getImports();
			if (imports != null)
				for (int j = 0; j < imports.size(); j++)
					sg.wl(imports.get(j));

			String[] importsforaction = thisfilterelement.getImportClassesForAction(this.getName());
			if (importsforaction != null)
				for (int j = 0; j < importsforaction.length; j++)
					sg.wl(importsforaction[j]);
		}

		sg.wl("");
		sg.wl("public class AtgLaunchblankreportfor" + reportvariablename + "Action extends AbsLaunchblankreportfor"
				+ reportvariablename + "Action {");
		sg.wl("");
		sg.wl("	public AtgLaunchblankreportfor" + reportvariablename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(");
		if (hasparentobject) {
			sg.wl("			DataObjectId<" + parentobjectclass + "> parentid,");
		}
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		if (hasparentobject) {
			sg.wl("		" + parentobjectclass + " " + parentobjectvariable + " = " + parentobjectclass
					+ ".readone(parentid);");
			sg.wl("		String label = \"" + parentobject.getLabel() + " \";");
			if (parentobject.hasNumbered()) {
				sg.wl("		label+=" + parentobjectvariable + ".getNr();");
			}
			if (parentobject.hasNamed()) {
				sg.wl("		label+=\" \";");
				sg.wl("		label+=" + parentobjectvariable + ".getObjectname();");
				sg.wl("");
			}
			if (parentobject.hasLifecycle()) {
				sg.wl("		label+=\" - \";");
				sg.wl("		label+=" + parentobjectvariable + ".getstateforchange().getDisplayValue();");
			}
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded())
				if (thisfilterelement.hasSuggestionValues()) {
					sg.wl("		String[] " + thisfilterelement.getArgumentContent(null).getName().toLowerCase()
							+ "_suggestions = "
							+ StringFormatter.formatForJavaClass(thisfilterelement.getParent().getName())
							+ ".getValuesForField"
							+ StringFormatter.formatForJavaClass(thisfilterelement.getFieldForSuggestion().getName())
							+ "(null);");
				}
		}

		sg.wl("		return new ActionOutputData(");
		if (hasparentobject) {
			sg.wl("				parentid,");
			sg.wl("				label,");
		}
		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				sg.wl("				" + thisfilterelement.getBlankValue() + ", // "
						+ thisfilterelement.getArgumentContent(null).getName());
				if (thisfilterelement.hasSuggestionValues())
					sg.wl("			" + thisfilterelement.getArgumentContent(null).getName().toLowerCase()
							+ "_suggestions,");

			}
		}
		sg.wl("				new NodeTree<Reportfor" + reportvariablename + ">(Reportfor" + reportvariablename
				+ ".getDefinition()));");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new AtgReportfor" + reportvariablename + "Page(");
		if (hasparentobject) {
			sg.wl("				logicoutput.getParentid_thru(),");
			sg.wl("				logicoutput.getParentobjectlabel_thru(),");
		}

		for (int i = 0; i < filterelements.size(); i++) {
			FilterElement<?> thisfilterelement = filterelements.get(i);
			if (!thisfilterelement.isHardCoded()) {
				// for not harcoded criteria, generate the input in blank
				ArgumentContent outputargumentforblank = thisfilterelement.getArgumentContent(null);
				sg.wl("				logicoutput.get"
						+ StringFormatter.formatForJavaClass(outputargumentforblank.getName()) + "(),");
				if (thisfilterelement.hasSuggestionValues())
					sg.wl("			logicoutput.get"
							+ StringFormatter.formatForJavaClass(
									thisfilterelement.getArgumentContent(null).getName().toLowerCase())
							+ "_suggestions(),");
			}
		}

		sg.wl("				logicoutput.getReportcontent());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");
		sg.close();

	}
}
