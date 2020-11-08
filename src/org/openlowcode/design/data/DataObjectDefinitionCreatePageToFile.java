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

import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkTypeRestrictionForLeft;
import org.openlowcode.design.data.properties.basic.DisplayLinkAsAttributeFromLeftObject;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Session;
import org.openlowcode.design.data.properties.basic.TimeSlot;
import org.openlowcode.design.data.properties.basic.Typed;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.module.designer.data.Dataobjectdef;
import org.openlowcode.server.action.SInlineEchoActionRef;
import org.openlowcode.server.data.message.TObjectDataEltType;

/**
 * Generation of the creation page for a Data Object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class DataObjectDefinitionCreatePageToFile
		implements
		GeneratedPages {
	private DataObjectDefinition dataobject;
	private DataObjectDefinition companion;

	/**
	 * creates the utility class to generate the creation page
	 * 
	 * @param dataobject data object
	 */
	public DataObjectDefinitionCreatePageToFile(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
		this.companion = null;
	}

	/**
	 * creates the utility class to generate the creation page
	 * 
	 * @param dataobject data object
	 */
	public DataObjectDefinitionCreatePageToFile(DataObjectDefinition dataobject, DataObjectDefinition companion) {
		this.dataobject = dataobject;
		this.companion = companion;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String pagename = objectvariable;
		String companionclass = null;
		ArrayList<LeftForLink<?, ?>> leftlinkedproperties = new ArrayList<LeftForLink<?, ?>>();
		for (int i = 0; i < dataobject.getPropertySize(); i++) {
			if (dataobject.getPropertyAt(i) instanceof LeftForLink)
				leftlinkedproperties.add((LeftForLink) dataobject.getPropertyAt(i));
		}

		if (companion != null) {
			pagename = StringFormatter.formatForAttribute(companion.getName());
			companionclass = StringFormatter.formatForJavaClass(companion.getName());
		}

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");

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

				ArrayList<String> imports = thisargument.getImports();
				for (int k = 0; k < imports.size(); k++) {
					importdeclaration.put(imports.get(k), imports.get(k));
				}

			}
			if (!thisproperty.isDataInputHiddenForCreation())
				for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);
					if (!thisproperty.isDataInputHiddenForCreation()) {

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
		boolean hasleftlinkasattribute = false;
		for (int i = 0; i < leftlinkedproperties.size(); i++) {
			LeftForLink<?, ?> leftlinkedproperty = leftlinkedproperties.get(i);
			DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String rightobjectvariable = StringFormatter
					.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
			String rightobjectclass = StringFormatter
					.formatForJavaClass(leftlinkedproperty.getRightObjectForLink().getName());
			@SuppressWarnings("rawtypes")
			DisplayLinkAsAttributeFromLeftObject<
					?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
							.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

			if (attributeasleft != null) {
				hasleftlinkasattribute = true;
				sg.wl("import " + linkobject.getOwnermodule().getPath() + ".action.generated.AtgSearchright"
						+ rightobjectvariable + "for" + linkobjectvariable + "Action;");
				sg.wl("import " + linkobject.getOwnermodule().getPath() + ".data." + rightobjectclass + ";");
			}
		}
		if (hasleftlinkasattribute) {
			sg.wl("import java.util.ArrayList;");
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectArrayField;");
			sg.wl("import org.openlowcode.server.graphic.widget.SFieldSearcher;");
			sg.wl("import org.openlowcode.server.action.SInlineEchoActionRef;");
			sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");

		}

		// ------------------------ Attributes for field suggestions
		// ---------------------
		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					if (pageattributedeclaration.length() > 0)
						pageattributedeclaration.append(" , ");
					pageattributedeclaration
							.append(" String[] suggestionsforfield" + stringfield.getName().toLowerCase() + " ");
					if (pageattributeentry.length() > 0)
						pageattributeentry.append(" , ");
					pageattributeentry.append(" suggestionsforfield" + stringfield.getName().toLowerCase() + " ");
				}
			}
		}

		String objectimport = "import " + dataobject.getOwnermodule().getPath() + ".data." + objectclass + ";";
		importdeclaration.put(objectimport, objectimport);

		if (companion != null) {
			String companionimport = "import " + companion.getOwnermodule().getPath() + ".data." + companionclass + ";";

			importdeclaration.put(companionimport, companionimport);
			String companioncreateaction = "import  " + companion.getOwnermodule().getPath()
					+ ".action.generated.AtgStandardcreate" + companion.getName().toLowerCase() + "Action;";
			importdeclaration.put(companioncreateaction, companioncreateaction);
		}

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
			if (thisproperty instanceof Typed) {
				sg.wl("import org.openlowcode.server.graphic.widget.SChoiceTextField;");

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
		sg.wl("public class AtgStandardcreate" + pagename + "Page extends");
		sg.wl("		AbsStandardcreate" + pagename + "Page {");
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
		if (pageattributedeclaration.length() > 0)
			pageattributedeclaration.append(',');
		if (pageattributeentry.length() > 0)
			pageattributeentry.append(',');

		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + pageattributedeclaration.toString());
		sg.wl("			" + objectclass + " object" + (companion != null ? ", " + companionclass + " companion" : "")
				+ ")  {");
		sg.wl("		return \"Create " + dataobject.getLabel() + "\";");
		sg.wl("		}");

		sg.wl("	public AtgStandardcreate" + pagename + "Page(" + pageattributedeclaration.toString());
		sg.wl("			" + objectclass + " object" + (companion != null ? ", " + companionclass + " companion" : "")
				+ ")  {");

		sg.wl("		super(" + pageattributeentry.toString());
		sg.wl("			  object" + (companion != null ? ",companion" : "") + ");");
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
		sg.wl("			SPageText title = new SPageText(\"Enter data for new "
				+ (companion != null ? companion.getLabel() : dataobject.getLabel())
				+ "\",SPageText.TYPE_TITLE,this);");

		sg.wl("			mainband.addElement(title);");
		sg.wl("			AtgStandardcreate" + pagename + "Action.ActionRef create" + pagename
				+ "actionref = AtgStandardcreate" + pagename + "Action.get().getActionRef();");
		if (subobject != null) {
			sg.wl("			AtgShow" + StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName())
					+ "Action.ActionRef back = AtgShow"
					+ StringFormatter.formatForAttribute(subobject.getParentObjectForLink().getName())
					+ "Action.get().getActionRef();");
		} else {
			sg.wl("			AtgLaunchsearch" + objectvariable + "Action.ActionRef back = AtgLaunchsearch" + objectvariable
					+ "Action.get().getActionRef();");
		}
		ArrayList<String> parentidsetters = new ArrayList<String>();
		for (int i = 0; i < leftlinkedproperties.size(); i++) {
			LeftForLink<?, ?> leftlinkedproperty = leftlinkedproperties.get(i);
			DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String rightobjectvariable = StringFormatter
					.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
			DisplayLinkAsAttributeFromLeftObject<
					?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
							.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

			if (attributeasleft != null) {
			sg.wl("				AtgSearchright" + rightobjectvariable + "for" + linkobjectvariable
					+ "Action.InlineActionRef addtoleft" + linkobjectvariable + "ssearchaction = AtgSearchright"
					+ rightobjectvariable + "for" + linkobjectvariable + "Action.get().getInlineActionRef();");
			sg.wl("				");
			parentidsetters.add("				addtoleft" + linkobjectvariable + "ssearchaction.set");
			}
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
					sg.wl("			create" + pagename + "actionref.set" + nameclass + "(" + namevariable
							+ ".getObjectIdInput());");
					for (int k=0;k<parentidsetters.size();k++) {
						sg.wl(parentidsetters.get(k)+nameclass+"("+namevariable
								+ ".getObjectIdInput());");
					}
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
						sg.wl("				create" + pagename + "actionref.set" + nameclass + "(" + objectidvariable
								+ "searcher.getresultarray().getAttributeInput(" + objectidclass
								+ ".getIdMarker())); ");
						for (int k=0;k<parentidsetters.size();k++) {
							sg.wl(parentidsetters.get(k)+nameclass+"("+
									objectidvariable
									+ "searcher.getresultarray().getAttributeInput(" + objectidclass
									+ ".getIdMarker())); ");
						}
						sg.wl("			}");

					}
					treated = true;
				}
				if (contextdata instanceof ChoiceArgument) {
					ChoiceArgument choiceargument = (ChoiceArgument) contextdata;
					sg.wl("		SChoiceTextField<" + choiceargument.getChoiceCategoryClass()
							+ "> typefield = new SChoiceTextField<" + choiceargument.getChoiceCategoryClass() + ">(");
					sg.wl("				\"Type\", \"TYPE\",\"Type\", " + choiceargument.getChoiceCategoryClass()
							+ ".get(), this.getType(), this, true, back);");
					sg.wl("		mainband.addElement(typefield);");

					treated = true;
				}
				if (!treated)
					throw new RuntimeException(
							" data type not supported " + contextdata.getType() + ", name = " + contextdata.getName());
			}

		}

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
							sg.wl("			create" + pagename + "actionref.set" + argumentclass + "("
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
									sg.wl("			create" + pagename + "actionref.set" + argumentclass + "("
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
				sg.wl("			create" + pagename + "actionref.setStarttime(timeslotfield.getStartDateInput());");
				sg.wl("			create" + objectvariable + "actionref.setEndtime(timeslotfield.getEndDateInput()); ");

			}
			if (thisproperty instanceof Session) {
				sg.wl("			STimeslotField timeslotfield = new STimeslotField(\"ORIGINTIMESLOT\",\"Start Time\",\"End Time\",\"Start Time\",\"End Time\", STimeslotField.DEFAULT_EMPTY,");
				sg.wl("					this.getStarttime(),this.getEndtime(), true, this);");
				sg.wl("			mainband.addElement(timeslotfield);");
				sg.wl("			SIntegerField sequencefield = new SIntegerField(\"Sequence Number\",\"SEQUENCEFIELD\", \"\", new Integer(1), true,this, false,false,false, null);");
				sg.wl("			mainband.addElement(sequencefield);");
				sg.wl("			create" + pagename + "actionref.setStarttime(timeslotfield.getStartDateInput()); ");
				sg.wl("			create" + pagename + "actionref.setEndtime(timeslotfield.getEndDateInput()); ");
				sg.wl("			create" + pagename + "actionref.setSequence(sequencefield.getIntegerInput());  ");

				sg.wl("			");

			}
		}

		sg.wl("			SObjectDisplay<" + objectclass + "> " + objectvariable + "display = new SObjectDisplay<"
				+ objectclass + ">(\"OBJECTDISPLAY\", this.getObject()," + objectclass
				+ ".getDefinition(),this, false);");
		for (int i = 0; i < dataobject.fieldlist.getSize(); i++) {
			if (dataobject.fieldlist.get(i) instanceof StringField) {
				StringField stringfield = (StringField) dataobject.fieldlist.get(i);
				if (stringfield.hasListOfValuesHelper()) {
					sg.wl("			" + objectvariable + "display.addTextFieldSuggestion(" + objectclass + ".get"
							+ StringFormatter.formatForJavaClass(stringfield.getName())
							+ "FieldMarker(),this.getSuggestionsforfield" + stringfield.getName().toLowerCase()
							+ "());");
				}
			}
		}
		sg.wl("			" + objectvariable + "display.setHideReadOnly();");
		sg.wl("			mainband.addElement(" + objectvariable + "display);");
		if (companion != null) {
			sg.wl("			SObjectDisplay<" + companionclass + "> " + pagename + "display = new SObjectDisplay<"
					+ companionclass + ">(\"COMPANIONDISPLAY\", this.getCompanion()," + companionclass
					+ ".getDefinition(),this, false);");
			sg.wl("			" + pagename + "display.setHideReadOnly();");
			sg.wl("			mainband.addElement(" + pagename + "display);");
		}
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
							sg.wl("			create" + pagename + "actionref.set" + argumentclass + "("
									+ argumentvariable + "entryfield.getActionDataInput());");
							treated = true;
						}
						if (!treated)
							throw new RuntimeException(" data type not supported " + thisargument.getType()
									+ ", name = " + thisargument.getName());
					}

			}
		}

		sg.wl("			create" + pagename + "actionref.setObject(" + objectvariable + "display.getObjectInput()); ");
		if (companion != null) {
			sg.wl("			create" + pagename + "actionref.setCompanion(" + pagename + "display.getObjectInput()); ");
		}
		if (dataobject.getPropertyByName("TYPED") != null) {
			sg.wl("			create" + pagename + "actionref.setType(typefield.getChoiceInput());");
		}

		/// --------------- attributes as link if needed

		for (int i = 0; i < leftlinkedproperties.size(); i++) {
			LeftForLink<?, ?> leftlinkedproperty = leftlinkedproperties.get(i);
			DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String rightobjectvariable = StringFormatter
					.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
			String rightobjectclass = StringFormatter
					.formatForJavaClass(leftlinkedproperty.getRightObjectForLink().getName());
			@SuppressWarnings("rawtypes")
			DisplayLinkAsAttributeFromLeftObject<
					?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
							.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

			if (attributeasleft != null) {
				Typed typed = (Typed) leftlinkedproperty.getParent().getPropertyByName("TYPED");
				@SuppressWarnings("rawtypes")
				ConstraintOnLinkTypeRestrictionForLeft typerestrictionforleft = (ConstraintOnLinkTypeRestrictionForLeft) leftlinkedproperty
						.getLinkObjectProperty().getBusinessRuleByName("TYPERESTRICTIONFORLEFT");
				sg.wl("// ---------------- Display " + linkobjectclass + " as object array field -------------------");
				sg.wl("// ----------------");

				sg.wl("	");
				sg.wl("");
				if (typerestrictionforleft != null) {
					sg.wl("		ArrayList<SPageNode> left" + linkobjectvariable
							+ "nodes = new ArrayList<SPageNode>();");
					sg.wl("		left" + linkobjectvariable + "nodes.add(new SPageText(\""
							+ leftlinkedproperty.getLinkObjectProperty().getLabelFromLeft()
							+ "\",SPageText.TYPE_TITLE,this));");
				}
				sg.wl("		SObjectArrayField<" + rightobjectclass + "> left" + linkobjectvariable
						+ "s = new SObjectArrayField<" + rightobjectclass + ">(\"" + linkobjectclass.toUpperCase()
						+ "\",");
				sg.wl("				\"" + leftlinkedproperty.getLinkObjectProperty().getLabelFromLeft() + "\",\""
						+ linkobject.getLabel() + "\", null,");
				sg.wl("				" + rightobjectclass + ".getDefinition(),");
				sg.wl("				" + rightobjectclass + ".getDefinition().getNrFieldMarker(), this);");

				if (typerestrictionforleft != null) {
					sg.wl("		left" + linkobjectvariable + "nodes.add(left" + linkobjectvariable + "s);");

				} else {

					sg.wl("			mainband.addElement(left" + linkobjectvariable + "s);");
				}

				sg.wl("				");


				sg.wl("				SInlineEchoActionRef<TObjectDataEltType<" + rightobjectclass + ">> addtoleft"
						+ linkobjectvariable + "s_resultechoaction =");
				sg.wl("						new SInlineEchoActionRef<TObjectDataEltType<" + rightobjectclass
						+ ">>(new TObjectDataEltType<" + rightobjectclass + ">(" + rightobjectclass
						+ ".getDefinition()));");

				sg.wl("				SFieldSearcher<" + rightobjectclass + "> addtoleft" + linkobjectvariable
						+ "ssearcher = ");
				sg.wl("						new SFieldSearcher<" + rightobjectclass + ">(\"ADDTOLEFT"
						+ linkobjectvariable.toUpperCase() + "SSEARCHER\", ");
				sg.wl("						\"add\", ");
				sg.wl("						\"close\", ");
				sg.wl("						\"enter the start of the number of the " + rightobjectvariable + "\", ");
				sg.wl("						addtoleft" + linkobjectvariable + "ssearchaction, ");
				sg.wl("						addtoleft" + linkobjectvariable + "s_resultechoaction, ");
				sg.wl("						" + rightobjectclass + ".getDefinition(), ");
				sg.wl("						" + rightobjectclass + ".getNrFieldMarker(), ");
				sg.wl("						this);");
				sg.wl("				");

				// ------------------------------------------------------------------------------------
				// create right object
				// ------------------------------------------------------------------------------------

				sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.setLeft" + objectvariable + "("
						+ objectvariable + "display.getObjectInput());");
				sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.setLeft" + objectvariable
						+ "id(null);");

				sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.setNr(addtoleft"
						+ linkobjectvariable + "ssearcher.getSearchTextInput());");
				for (int j = 0; j < leftlinkedproperty.getRightObjectForLink().getSearchWidgets().length; j++) {
					SearchWidgetDefinition searchwidget = leftlinkedproperty.getRightObjectForLink()
							.getSearchWidgets()[j];
					if (searchwidget.getFieldname().compareTo("NR") != 0)
						if (searchwidget.getType() != SearchWidgetDefinition.TYPE_DATE) {
							sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
									+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "(null);");
						} else {
							sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
									+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "from(null);");
							sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
									+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "to(null);");
						}
				}

				sg.wl("				addtoleft" + linkobjectvariable + "ssearcher.setSearchInlineOutput(AtgSearchright"
						+ rightobjectvariable + "for" + linkobjectvariable + "Action.get().getSearchresultfor"
						+ rightobjectvariable + "Ref());");
				sg.wl("left" + linkobjectvariable + "s.addFeedingInlineAction(addtoleft" + linkobjectvariable + "s_resultechoaction, addtoleft" + linkobjectvariable + "s_resultechoaction.getOutputActionDataRef());");
				sg.wl("				left" + linkobjectvariable + "s.addNodeAtEndOfFieldData(addtoleft"
						+ linkobjectvariable + "ssearcher);");
				sg.wl("				addtoleft" + linkobjectvariable + "s_resultechoaction.setInputData(addtoleft"
						+ linkobjectvariable + "ssearcher.getObjectInput()); ");
				if (typerestrictionforleft != null) {
					ChoiceValue[] allowedtypes = typerestrictionforleft.getAllowedTypes();

					sg.wl("		mainband.addConditionalElements(this.getType(),");
					sg.wl("				new ChoiceValue[] { ");
					for (int t = 0; t < allowedtypes.length; t++) {
						;
						sg.wl("					" + (t > 0 ? "," : "")
								+ StringFormatter.formatForJavaClass(typed.getTypes().getName())
								+ "ChoiceDefinition.get()." + allowedtypes[t].getName());
					}
					sg.wl("				}, left" + linkobjectvariable + "nodes.toArray(new SPageNode[0]));");

					sg.wl("				left" + linkobjectvariable + "s.addFeedingInlineAction(addtoleft"
							+ linkobjectvariable + "ssearchaction,addtoleft" + linkobjectvariable
							+ "s_resultechoaction.getOutputActionDataRef());");

				}
				sg.wl("			create" + pagename + "actionref.setLeft" + linkobjectvariable + "(left"
						+ linkobjectvariable + "s.getObjectArrayInput());");
			}

		}

		/// --------------- end of attributes as link

		sg.wl("			SActionButton create = new SActionButton(\"Create\", create" + pagename + "actionref, this);");
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
