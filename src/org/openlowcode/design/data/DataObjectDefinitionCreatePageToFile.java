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

import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Session;
import org.openlowcode.design.data.properties.basic.TimeSlot;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;


/**
 * Generation of the creation page for a Data Object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class DataObjectDefinitionCreatePageToFile implements
GeneratedPages {
	private DataObjectDefinition dataobject;
	/**
	 * creates the utility class to generate the creation page
	 * 
	 * @param dataobject data object
	 */
	public DataObjectDefinitionCreatePageToFile(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
	}
	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		boolean isextra = false;
		HashMap<String, String> importdeclaration = new HashMap<String, String>();

		StringBuffer pageattributedeclaration = new StringBuffer();
		StringBuffer pageattributeentry = new StringBuffer();
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {

				ArgumentContent thisargument = thisproperty.getContextDataForCreation(j);

				if (pageattributedeclaration.length() > 0)
					pageattributedeclaration.append(" , ");
				pageattributedeclaration
						.append(" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");
				if (pageattributeentry.length() > 0)
					pageattributeentry.append(" , ");
				pageattributeentry.append(" " + thisargument.getName().toLowerCase() + " ");

				isextra = true;

				ArrayList<String> imports = thisargument.getImports();
				for (int k = 0; k < imports.size(); k++) {
					importdeclaration.put(imports.get(k), imports.get(k));
				}

			}
			if (!thisproperty.isDataInputHiddenForCreation())
				for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
					if (!thisproperty.isDataInputHiddenForCreation()) {
						isextra = true;

						if (thisargument instanceof StringArgument) {
							String importtextfield = "import org.openlowcode.server.graphic.widget.STextField;";
							importdeclaration.put(importtextfield, importtextfield);
						}
						if (thisargument instanceof TimestampArgument) {
							String importdatefield = "import org.openlowcode.server.graphic.widget.SDateField;";
							String importdate = "import java.util.Date;";
							importdeclaration.put(importdatefield, importdatefield);
							importdeclaration.put(importdate, importdate);
						}
						if (pageattributedeclaration.length() > 0)
							pageattributedeclaration.append(" , ");
						pageattributedeclaration.append(
								" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");
						if (pageattributeentry.length() > 0)
							pageattributeentry.append(" , ");
						pageattributeentry.append(" " + thisargument.getName().toLowerCase() + " ");

						ArrayList<String> imports = thisargument.getImports();
						for (int k = 0; k < imports.size(); k++) {
							importdeclaration.put(imports.get(k), imports.get(k));
						}
					}

				}
		}
		
		// ------------------------ Attributes for field suggestions ---------------------
		for (int i=0;i<dataobject.fieldlist.getSize();i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield  = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					if (pageattributedeclaration.length() > 0)
						pageattributedeclaration.append(" , ");
					pageattributedeclaration
							.append(" String[] suggestionsforfield" + stringfield.getName().toLowerCase()+ " ");
					if (pageattributeentry.length() > 0)
						pageattributeentry.append(" , ");
					pageattributeentry.append(" suggestionsforfield" + stringfield.getName().toLowerCase() + " ");
				}
			}
		}
		
		String objectimport = "import " + dataobject.getOwnermodule().getPath() + ".data." + objectclass + ";";
		importdeclaration.put(objectimport, objectimport);

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) thisproperty;
				if (!linkedtoparent.getParentObjectForLink().getOwnermodule().getName()
						.equals(dataobject.getOwnermodule().getName())) {
					sg.wl("import " + linkedtoparent.getParentObjectForLink().getOwnermodule().getPath()
							+ ".page.generated.AtgSearch"
							+ linkedtoparent.getParentObjectForLink().getName().toLowerCase() + "Page;");
				}
			}
			if (thisproperty instanceof TimeSlot) {
				sg.wl("import org.openlowcode.server.graphic.widget.STimeslotField;");
			}
			if (thisproperty instanceof Session) {
				sg.wl("import org.openlowcode.server.graphic.widget.STimeslotField;");
				sg.wl("import org.openlowcode.server.graphic.widget.SIntegerField;");

			}
		}

		for (int i = 0; i < importdeclaration.size(); i++) {
			sg.wl(importdeclaration.get(importdeclaration.keySet().toArray()[i]));
		}
		LinkedToParent<?> subobject = dataobject.isSubObject();

		sg.wl("import " + module.getPath() + ".action.generated.AtgStandardcreate" + objectvariable + "Action;");
		if (subobject != null) {
			sg.wl("import " + subobject.getParentObjectForLink().getOwnermodule().getPath()
					+ ".action.generated.AtgShow"
					+ StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName()) + "Action;");
		} else {
			sg.wl("import " + module.getPath() + ".action.generated.AtgLaunchsearch" + objectvariable + "Action;");
		}

		sg.wl("import org.openlowcode.server.action.SActionRef;");

		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectDisplay;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("");
		sg.wl("public class AtgStandardcreate" + objectvariable + "Page extends");
		sg.wl("		AbsStandardcreate" + objectvariable + "Page {");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++)
			for (int j = 0; j < dataobject.propertylist.get(i).getContextDataForCreationSize(); j++) {
				ArgumentContent contextfordatacreation = dataobject.propertylist.get(i).getContextDataForCreation(j);
				if (contextfordatacreation.isOptional())
					if (contextfordatacreation instanceof ObjectIdArgument) {
						String namevariable = StringFormatter
								.formatForAttribute(((ObjectIdArgument) contextfordatacreation).getName());
						sg.wl("	boolean is" + namevariable + ";");
					}
			}
		sg.wl("");
		if (isextra)
			pageattributedeclaration.append(',');

		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + pageattributedeclaration.toString());
		sg.wl("			" + objectclass + " object)  {");
		sg.wl("		return \"Create " + dataobject.getLabel() + "\";");
		sg.wl("		}");

		sg.wl("	public AtgStandardcreate" + objectvariable + "Page(" + pageattributedeclaration.toString());
		sg.wl("			" + objectclass + " object)  {");
		if (isextra)
			pageattributeentry.append(',');
		sg.wl("		super(" + pageattributeentry.toString());
		sg.wl("			  object);");
		sg.wl("		");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++)
			for (int j = 0; j < dataobject.propertylist.get(i).getContextDataForCreationSize(); j++) {
				ArgumentContent contextfordatacreation = dataobject.propertylist.get(i).getContextDataForCreation(j);
				if (contextfordatacreation.isOptional())
					if (contextfordatacreation instanceof ObjectIdArgument) {
						String namevariable = StringFormatter
								.formatForAttribute(((ObjectIdArgument) contextfordatacreation).getName());
						sg.wl("		is" + namevariable + "=true;");
						sg.wl("		if (" + namevariable + ".getId()==null) is" + namevariable + "=false;");
					}
			}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		");
		sg.wl("			SComponentBand mainband = new  SComponentBand(SComponentBand.DIRECTION_DOWN,this);");

		sg.wl("			AtgStandardcreate" + objectvariable + "Action.ActionRef create" + objectvariable
				+ "actionref = AtgStandardcreate" + objectvariable + "Action.get().getActionRef();");
		if (subobject != null) {
			sg.wl("	AtgShow" + StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName())
					+ "Action.ActionRef back = AtgShow"
					+ StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName())
					+ "Action.get().getActionRef();");
		} else {
			sg.wl("	AtgLaunchsearch" + objectvariable + "Action.ActionRef back = AtgLaunchsearch" + objectvariable
					+ "Action.get().getActionRef();");
		}
//add context data. 
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			boolean issubobject = false;
			if (thisproperty == subobject)
				issubobject = true;
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {
				ArgumentContent contextdata = thisproperty.getContextDataForCreation(j);
				boolean treated = false;
				if (contextdata instanceof ObjectIdArgument) {
					ObjectIdArgument contextobjectid = (ObjectIdArgument) contextdata;
					String objectidclass = StringFormatter.formatForJavaClass(contextobjectid.getObjectType());
					String objectidvariable = StringFormatter.formatForAttribute(contextobjectid.getObjectType());
					String namevariable = StringFormatter.formatForAttribute(contextobjectid.getName());
					String nameclass = StringFormatter.formatForJavaClass(contextobjectid.getName());
					if (contextdata.isOptional())
						sg.wl("			if (is" + namevariable + ") {");
					sg.wl("			SObjectIdStorage<" + objectidclass + "> " + namevariable
							+ " = new SObjectIdStorage<" + objectidclass + ">(\"PARENTID\",this, this.get" + nameclass
							+ "());");
					sg.wl("			mainband.addElement(" + namevariable + ");");
					sg.wl("			create" + objectvariable + "actionref.set" + nameclass + "(" + namevariable
							+ ".getObjectIdInput());");
					if (issubobject)
						sg.wl("			back.setId(" + namevariable + ".getObjectIdInput());");
					if (contextdata.isOptional()) {
						sg.wl("			} else {");
						sg.wl("				SObjectSearcher<" + objectidclass + "> " + objectidvariable
								+ "searcher = AtgSearch" + objectidvariable + "Page.getsearchpanel(this,\""
								+ thisproperty.getInstancename().toUpperCase() + "\");");
						sg.wl("				mainband.addElement(new SPageText(\"Select relevant "
								+ contextobjectid.getObject().getLabel() + "\",SPageText.TYPE_TITLE,this));");
						sg.wl("				mainband.addElement(" + objectidvariable + "searcher);");
						sg.wl("				create" + objectvariable + "actionref.set" + nameclass + "("
								+ objectidvariable + "searcher.getresultarray().getAttributeInput(" + objectidclass
								+ ".getIdMarker())); ");
						sg.wl("			}");

					}
					treated = true;
				}
				if (!treated)
					throw new RuntimeException(
							" data type not supported " + contextdata.getType() + ", name = " + contextdata.getName());
			}

		}
		sg.wl("			SPageText title = new SPageText(\"Enter data for new " + dataobject.getLabel()
				+ "\",SPageText.TYPE_TITLE,this);");

		sg.wl("			mainband.addElement(title);");
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
				if (!thisproperty.isDataInputHiddenForCreation())
					if (!thisproperty.isDatainputatbottom()) {
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
							sg.wl("			create" + objectvariable + "actionref.set" + argumentclass + "("
									+ argumentvariable + "entryfield.getTextInput()); ");
							treated = true;
						}
						if (thisargument instanceof TimestampArgument)
							if (!(thisproperty instanceof TimeSlot))
								if (!(thisproperty instanceof Session)) {
									TimestampArgument timestampargument = (TimestampArgument) thisargument;
									String argumentvariable = thisargument.getName().toLowerCase();
									String argumentclass = StringFormatter.formatForJavaClass(thisargument.getName());
									sg.wl("			SDateField " + argumentvariable + "entryfield = new SDateField(\""
											+ thisargument.getDisplaylabel() + "\",\"" + thisargument.getName()
											+ "\",\"" + thisargument.getName() + "\",SDateField.DEFAULT_EMPTY,false,"
											+ timestampargument.isDefaultDisplayUseHour()
											+ ",this,false,false,false,null);");
									sg.wl("			" + argumentvariable + "entryfield.setDateBusinessData(this.get"
											+ argumentclass + "());");
									sg.wl("			mainband.addElement(" + argumentvariable + "entryfield);");
									sg.wl("			create" + objectvariable + "actionref.set" + argumentclass + "("
											+ argumentvariable + "entryfield.getDateInput());");
									treated = true;
									treated = true;
								}

						if (!(thisproperty instanceof TimeSlot))
							if (!(thisproperty instanceof Session))
								if (!treated)
									throw new RuntimeException(" data type not supported " + thisargument.getType()
											+ ", name = " + thisargument.getName());
					}

			}
			if (thisproperty instanceof TimeSlot) {

				sg.wl("			STimeslotField timeslotfield = new STimeslotField(\"ORIGINTIMESLOT\",\"Start Time\",\"End Time\",\"Start Time\",\"End Time\", STimeslotField.DEFAULT_EMPTY,");
				sg.wl("					this.getStarttime(),this.getEndtime(), true, this);");
				sg.wl("			mainband.addElement(timeslotfield);");
				sg.wl("			create" + objectvariable
						+ "actionref.setStarttime(timeslotfield.getStartDateInput());");
				sg.wl("			create" + objectvariable + "actionref.setEndtime(timeslotfield.getEndDateInput()); ");

			}
			if (thisproperty instanceof Session) {
				sg.wl("			STimeslotField timeslotfield = new STimeslotField(\"ORIGINTIMESLOT\",\"Start Time\",\"End Time\",\"Start Time\",\"End Time\", STimeslotField.DEFAULT_EMPTY,");
				sg.wl("					this.getStarttime(),this.getEndtime(), true, this);");
				sg.wl("			mainband.addElement(timeslotfield);");
				sg.wl("			SIntegerField sequencefield = new SIntegerField(\"Sequence Number\",\"SEQUENCEFIELD\", \"\", new Integer(1), true,this, false,false,false, null);");
				sg.wl("			mainband.addElement(sequencefield);");
				sg.wl("			create" + objectvariable
						+ "actionref.setStarttime(timeslotfield.getStartDateInput()); ");
				sg.wl("			create" + objectvariable + "actionref.setEndtime(timeslotfield.getEndDateInput()); ");
				sg.wl("			create" + objectvariable + "actionref.setSequence(sequencefield.getIntegerInput());  ");

				sg.wl("			");

			}
		}

		sg.wl("			SObjectDisplay<" + objectclass + "> " + objectvariable + "display = new SObjectDisplay<"
				+ objectclass + ">(\"OBJECTDISPLAY\", this.getObject()," + objectclass
				+ ".getDefinition(),this, false);");
		for (int i=0;i<dataobject.fieldlist.getSize();i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield  = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					sg.wl("			" + objectvariable + "display.addTextFieldSuggestion("+objectclass+".get"+StringFormatter.formatForJavaClass(stringfield.getName())+"FieldMarker(),this.getSuggestionsforfieldbudgetowner());");		
				}
			}
		}
		sg.wl("			" + objectvariable + "display.setHideReadOnly();");
		sg.wl("			mainband.addElement(" + objectvariable + "display);");

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
				if (!thisproperty.isDataInputHiddenForCreation())
					if (thisproperty.isDatainputatbottom()) {
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
							sg.wl("			create" + objectvariable + "actionref.set" + argumentclass + "("
									+ argumentvariable + "entryfield.getActionDataInput());");
							treated = true;
						}
						if (!treated)
							throw new RuntimeException(" data type not supported " + thisargument.getType()
									+ ", name = " + thisargument.getName());
					}

			}
		}

		sg.wl("			create" + objectvariable + "actionref.setObject(" + objectvariable
				+ "display.getObjectInput()); ");

		sg.wl("			SActionButton create = new SActionButton(\"Create\", create" + objectvariable
				+ "actionref, this);");
		sg.wl("			SActionButton backbutton = new SActionButton(\"Back\",back,this);");
		sg.wl("			");
		sg.wl("			SComponentBand buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
		sg.wl("			buttonband.addElement(backbutton);");
		sg.wl("			buttonband.addElement(create);");
		sg.wl("			mainband.addElement(buttonband);");

		sg.wl("			return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

		
	}

}
