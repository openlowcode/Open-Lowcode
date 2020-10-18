/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * this program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.data.properties.basic.DataControl;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * Generation of the update page for a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionUpdatePage
		implements
		GeneratedPages {

	private DataObjectDefinition dataobject;
	private DataObjectDefinition companionobject;

	/**
	 * creates the update page class for the provided data object
	 * 
	 * @param dataobject data object definition
	 */
	public DataObjectDefinitionUpdatePage(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
		this.companionobject = null;
	}

	/**
	 * creates the update page class for the provided data object
	 * 
	 * @param dataobject      data object definition
	 * @param companionobject companion data object definition (for typed object)
	 */
	public DataObjectDefinitionUpdatePage(DataObjectDefinition dataobject, DataObjectDefinition companionobject) {
		this.companionobject = companionobject;
		this.dataobject = dataobject;
	}

	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String pagename = "Update" + dataobject.getName().toLowerCase() + "Page";
		String companionclass = null;
		String companionvariable = null;

		if (companionobject != null) {
			pagename = "Update" + companionobject.getName().toLowerCase() + "Page";
			companionclass = StringFormatter.formatForJavaClass(companionobject.getName());
			companionvariable = StringFormatter.formatForAttribute(companionobject.getName());
		}
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		HashMap<String, String> importdeclaration = new HashMap<String, String>();
		StringBuffer pageattributedeclaration = new StringBuffer();
		StringBuffer pageattributeentry = new StringBuffer();
		boolean isdatacontrol = false;
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof DataControl)
				isdatacontrol = true;
			for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
				if (thisproperty.isDataInputUsedForUpdate()) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
					if (thisargument instanceof StringArgument) {
						String importtextfield = "import org.openlowcode.server.graphic.widget.STextField;";
						importdeclaration.put(importtextfield, importtextfield);
					}

					pageattributedeclaration.append(" , ");
					pageattributedeclaration
							.append(" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");
					pageattributeentry.append(" , ");
					pageattributeentry.append(" " + thisargument.getName().toLowerCase() + " ");

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {
						importdeclaration.put(imports.get(k), imports.get(k));
					}
				}
			}
		}
		if (isdatacontrol) {
			pageattributedeclaration.append(" , ");
			pageattributedeclaration.append(" String controlstatus ");
			pageattributeentry.append(" , ");
			pageattributeentry.append(" controlstatus ");
		}

		// ------------------------ Attributes for field suggestions
		// ---------------------
		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {

					pageattributedeclaration
							.append(", String[] suggestionsforfield" + stringfield.getName().toLowerCase() + " ");

					pageattributeentry.append(", suggestionsforfield" + stringfield.getName().toLowerCase() + " ");
				}
			}
		}

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		if (companionobject == null) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgUpdate" + objectvariable + "Action;");
		} else {
			sg.wl("import " + companionobject.getOwnermodule().getPath() + ".action.generated.AtgUpdate"
					+ companionvariable + "Action;");
		}
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		if (companionobject != null) {
			sg.wl("import " + companionobject.getOwnermodule().getPath() + ".data." + companionclass + ";");
		}
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectDisplay;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");

		sg.wl("");
		sg.wl("public class Atg" + pagename + " extends Abs" + pagename + " {");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + objectclass + " " + objectvariable + " "
				+ (companionobject != null ? ", " + companionclass + " " + companionvariable + " " : "")
				+ pageattributedeclaration.toString() + ") {");
		sg.wl("		String objectdisplay = \"Update "
				+ (companionobject != null ? companionobject.getLabel() : dataobject.getLabel()) + "\";");
		if (dataobject.getPropertyByName("NUMBERED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getNr();");
		}
		if (dataobject.getPropertyByName("NAMED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getName();");
		}

		sg.wl("		return objectdisplay;");
		sg.wl("	}");

		sg.wl("	public Atg" + pagename + "(" + objectclass + " " + objectvariable + " "
				+ (companionobject != null ? ", " + companionclass + " " + companionvariable + " " : "")
				+ pageattributedeclaration.toString() + ")  {");
		sg.wl("		super(" + objectvariable + (companionobject != null ? ", " + companionvariable + " " : "") + " "
				+ pageattributeentry.toString() + ");");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("		mainband.addElement(new SPageText(\"Update " + (companionobject!=null?companionobject.getLabel():dataobject.getLabel()) + "\",SPageText.TYPE_TITLE,this));");
		sg.wl("		AtgUpdate" + (companionobject != null ? companionvariable : objectvariable)
				+ "Action.ActionRef update" + objectvariable + "actionref = AtgUpdate"
				+ (companionobject != null ? companionvariable : objectvariable) + "Action.get().getActionRef();");
		if (isdatacontrol) {
			sg.wl("		STextField controlstatus = new STextField(\"Control Status\",\"CONTROLSTATUS\",\"\",20000, \"\",");
			sg.wl("				false,this, true, false, false,null, false);");
			sg.wl("		controlstatus.setTextBusinessData(this.getControlstatus());");
			sg.wl("		mainband.addElement(controlstatus);");

		}

		sg.wl("		SObjectDisplay<" + objectclass + "> objectupdatedefinition = new SObjectDisplay<" + objectclass
				+ ">(\"" + dataobject.getName().toUpperCase() + "\", this.get" + objectclass + "()," + objectclass
				+ ".getDefinition(),this, false);");
		sg.wl("		objectupdatedefinition.setReducedDisplay(false);");
		sg.wl("		update" + objectvariable + "actionref.set" + objectclass
				+ "(objectupdatedefinition.getObjectInput()); ");
		if (companionobject != null) {
			sg.wl("		SObjectDisplay<" + companionclass + "> companionupdatedefinition = new SObjectDisplay<"
					+ companionclass + ">(\"" + companionobject.getName().toUpperCase() + "\", this.get"
					+ companionclass + "()," + companionclass + ".getDefinition(),this, false);");
			sg.wl("		update" + objectvariable + "actionref.set" + companionclass
					+ "(companionupdatedefinition.getObjectInput()); ");
			sg.wl("		companionupdatedefinition.setReducedDisplay(false);");
		}

		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					sg.wl("			objectupdatedefinition.addTextFieldSuggestion(" + objectclass + ".get"
							+ StringFormatter.formatForJavaClass(stringfield.getName())
							+ "FieldMarker(),this.getSuggestionsforfield" + stringfield.getName().toLowerCase()
							+ "());");
				}
			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty.isDataInputUsedForUpdate())
				if (!thisproperty.isDatainputatbottom())
					for (int j = 0; j < thisproperty.getDataInputSize(); j++) {

						ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
						boolean treated = false;
						if (thisargument instanceof StringArgument) {
							StringArgument stringargument = (StringArgument) thisargument;
							String argumentvariable = thisargument.getName().toLowerCase();
							String argumentclass = StringFormatter.formatForJavaClass(thisargument.getName());
							sg.wl("			STextField " + argumentvariable + "entryfield = new STextField(\""
									+ thisargument.getDisplaylabel() + "\",\"" + thisargument.getName() + "\",\""
									+ thisargument.getName() + "\"," + stringargument.getMaxLength()
									+ ",\"\",false,this,false,false,false,null,false,true);");
							sg.wl("			" + argumentvariable + "entryfield.setTextBusinessData(this.get"
									+ argumentclass + "());");
							sg.wl("			mainband.addElement(" + argumentvariable + "entryfield);");
							sg.wl("			update" + objectvariable + "actionref.set" + argumentclass + "("
									+ argumentvariable + "entryfield.getTextInput());");
							treated = true;
						}
						if (!treated)
							throw new RuntimeException(" data type not supported " + thisargument.getType()
									+ ", name = " + thisargument.getName());

					}

		}
		sg.wl("		mainband.addElement(objectupdatedefinition);");
		if (companionobject != null)
			sg.wl("		mainband.addElement(companionupdatedefinition);");

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty.isDataInputUsedForUpdate())
				if (thisproperty.isDatainputatbottom())
					for (int j = 0; j < thisproperty.getDataInputSize(); j++) {

						ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
						boolean treated = false;
						if (thisargument instanceof StringArgument) {
							StringArgument stringargument = (StringArgument) thisargument;
							String argumentvariable = thisargument.getName().toLowerCase();
							String argumentclass = StringFormatter.formatForJavaClass(thisargument.getName());
							sg.wl("			STextField " + argumentvariable + "entryfield = new STextField(\""
									+ thisargument.getDisplaylabel() + "\",\"" + thisargument.getName() + "\",\""
									+ thisargument.getName() + "\"," + stringargument.getMaxLength()
									+ ",\"\",false,this,false,false,false,null);");
							sg.wl("			" + argumentvariable + "entryfield.setTextBusinessData(this.get"
									+ argumentclass + "());");
							sg.wl("			mainband.addElement(" + argumentvariable + "entryfield);");
							sg.wl("			update" + objectvariable + "actionref.set" + argumentclass + "("
									+ argumentvariable + "entryfield.getTextInput()); ");
							treated = true;
						}
						if (!treated)
							throw new RuntimeException(" data type not supported " + thisargument.getType()
									+ ", name = " + thisargument.getName());

					}

		}

		sg.wl("		SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
		sg.wl("		");
		sg.wl("		AtgShow" + objectvariable + "Action.ActionRef show" + objectvariable + "actionref = AtgShow"
				+ objectvariable + "Action.get().getActionRef();");
		sg.wl("		show" + objectvariable + "actionref.setId(objectupdatedefinition.getAttributeInput(" + objectclass
				+ ".getIdMarker())); ");
		sg.wl("		SActionButton cancelbutton = new SActionButton(\"Cancel\",show" + objectvariable
				+ "actionref,this);");
		sg.wl("		buttonband.addElement(cancelbutton);");
		sg.wl("		");

		sg.wl("		SActionButton updatebutton = new SActionButton(\"Update\",update" + objectvariable
				+ "actionref,this);");
		sg.wl("		buttonband.addElement(updatebutton);");
		sg.wl("		mainband.addElement(buttonband);");
		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

}
