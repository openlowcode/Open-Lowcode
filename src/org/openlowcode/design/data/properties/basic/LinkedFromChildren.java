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
import java.util.logging.Logger;

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;


/**
 * This property is added to objects that are parents for a parent to child
 * relation
 * 
 * <br>
 * Warning: this should not be added directly by developer
 * 
 * <br>
 * <br>
 * the child object has the property.
 * {@link org.openlowcode.design.data.properties.basic.LinkedToParent}
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class LinkedFromChildren
		extends
		Property<LinkedFromChildren> {
	private static Logger logger = Logger.getLogger(LinkedFromChildren.class.getName());
	@SuppressWarnings("unused")
	private NamedList<DynamicActionDefinition> actionsonobjectid;
	private NamedList<DynamicActionDefinition> actionsonselectedchildid;
	private UniqueIdentified uniqueidentified;
	private boolean displaychildrenasgrid;
	private String linedisplayforgrid;
	private String columndisplayforgrid;
	private String[] cellfieldsforgrid;
	private DataObjectDefinition childobject;
	private LinkedToParent<?> originobjectproperty;
	private WidgetDisplayPriority linkedfromchildrenwidgetdisplaypriority;
	private String secondarycolumndisplayforgrid;
	private String specifictitleforchildrentable = null;
	private String[] infofieldforreverseshow;
	private boolean reversetree;
	private String[] exceptionsforinfofieldconsolidation;

	/**
	 * @return the related linked to parent property on the child object
	 */
	public LinkedToParent<?> getRelatedLinkedToParent() {
		return this.originobjectproperty;
	}

	/**
	 * the table showing all children on the parent object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class ChildrenTable
			extends
			Widget {

		private LinkedFromChildren linkedfromchildren;

		/**
		 * creates the widget showing all children on the parent object
		 * 
		 * @param linkedfromchildren related linked from children property
		 */
		ChildrenTable(LinkedFromChildren linkedfromchildren) {
			super("CHILDRENTABLE");
			this.linkedfromchildren = linkedfromchildren;
		}

		@Override
		public String[] getImportStatements() {
			ArrayList<String> importstatements = new ArrayList<String>();
			String childclassattribute = StringFormatter
					.formatForAttribute(linkedfromchildren.getChildObject().getName());
			Module linkobjectmodule = linkedfromchildren.getChildObject().getOwnermodule();
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgPreparestandardcreate"
					+ childclassattribute + "Action;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgPrepareupdate"
					+ childclassattribute + "Action;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgLoadchildren"
					+ StringFormatter.formatForAttribute(linkedfromchildren.getInstancename()) + "for"
					+ StringFormatter.formatForAttribute(linkedfromchildren.getParent().getName()) + "Action;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgExportchildren"
					+ StringFormatter.formatForAttribute(linkedfromchildren.getInstancename()) + "for"
					+ StringFormatter.formatForAttribute(linkedfromchildren.getParent().getName()) + "Action;");

			importstatements.add(
					"import " + linkobjectmodule.getPath() + ".action.generated.AtgMassivedelete" + childclassattribute
							+ "andshowparent" + originobjectproperty.getInstancename().toLowerCase() + "Action;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SFileChooser;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SFileDownloader;");

			importstatements.add("import org.openlowcode.server.graphic.widget.SChoiceTextField;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SPopupButton;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SCollapsibleBand;");
			importstatements.add("import org.openlowcode.server.data.DataObjectFieldMarker;");
			if (linkedfromchildren.getChildObject().getCategoryForExtractor() != null) {
				ChoiceCategory categoryforextractor = linkedfromchildren.getChildObject().getCategoryForExtractor();
				importstatements.add("import " + categoryforextractor.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(categoryforextractor.getName()) + "ChoiceDefinition;");
			}
			LinkedToParent<?> subobject = linkedfromchildren.getChildObject().isSubObject();
			String relatedlinkedtoparentinstancename = linkedfromchildren.getOriginObjectProperty().getInstancename()
					.toLowerCase();
			if (subobject != null) {
				importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgDelete"
						+ childclassattribute + "andshowparentAction;");

				importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgMassupdate"
						+ childclassattribute + "andshowparent" + relatedlinkedtoparentinstancename + "Action;");
			} else {
				importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgMassupdate"
						+ childclassattribute + "andshowparent" + relatedlinkedtoparentinstancename + "Action;");

			}
			if (linkedfromchildren.displaychildrenasgrid) {
				importstatements.add("import org.openlowcode.server.graphic.widget.SGrid;");
				importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgMassupdate"
						+ childclassattribute + "Action;");

			}
			return importstatements.toArray(new String[0]);
		}

		@Override
		public void generateWidgetCode(SourceGenerator sg, Module module, String locationname) throws IOException {
			String objectclass = StringFormatter.formatForJavaClass(linkedfromchildren.getParent().getName());
			String objectvariable = StringFormatter.formatForAttribute(linkedfromchildren.getParent().getName());

			String childclassname = StringFormatter.formatForJavaClass(linkedfromchildren.getChildObject().getName());
			String childclassattribute = StringFormatter
					.formatForAttribute(linkedfromchildren.getChildObject().getName());
			String linknameclass = StringFormatter.formatForJavaClass(linkedfromchildren.getName());
			String linknameattribute = StringFormatter.formatForAttribute(linkedfromchildren.getName());
			String linknameshortname = StringFormatter.formatForAttribute(linkedfromchildren.getInstancename());
			if (!linkedfromchildren.displaychildrenasgrid) {
				sg.wl("");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("		// Display all children objects of type " + childclassname + " in " + objectclass
						+ "--------");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("");
				LinkedToParent<?> subobject = linkedfromchildren.getChildObject().isSubObject();
				boolean displayascomponentband = false;
				if (subobject != null)
					if (!linkedfromchildren.getChildObject().getSubObject().isShowastable())
						displayascomponentband = true;
				String label = "Related " + linkedfromchildren.getChildObject().getLabel();
				if (specifictitleforchildrentable != null)
					label = specifictitleforchildrentable;
				sg.wl("		" + locationname + ".addElement(new SPageText(\"" + label
						+ "\",SPageText.TYPE_TITLE,this));");

				if (!displayascomponentband) {
					// case object should be shown as array
					sg.wl("		SObjectArray<" + childclassname + "> arrayfor" + linknameattribute
							+ " = new SObjectArray<" + childclassname + ">(\"CHILD" + childclassname.toUpperCase()
							+ "\",");
					sg.wl("				this.get" + linknameclass + "(),");
					sg.wl("				" + childclassname + ".getDefinition(),");
					sg.wl("				this);");

					sg.w("		arrayfor" + linknameattribute + ".setWarningForUnsavedEdition();");
					sg.wl("");

					sg.wl("		arrayfor" + linknameattribute + ".addDisplayProfile(" + childclassname
							+ "Definition.get" + childclassname + "Definition().getDisplayProfileHide"
							+ originobjectproperty.getName().toLowerCase() + "());");
					sg.wl("		arrayfor" + linknameattribute + ".setMinFieldPriority(0);");
					sg.wl("		arrayfor" + linknameattribute + ".setAllowMultiSelect();");

					String relatedlinkedtoparentinstancename = linkedfromchildren.getOriginObjectProperty()
							.getInstancename().toLowerCase();
					sg.wl("		// add update in table as a full action ");
					sg.wl("		AtgMassupdate" + childclassattribute + "andshowparent"
							+ relatedlinkedtoparentinstancename + "Action.ActionRef update" + linknameattribute
							+ " = AtgMassupdate" + childclassattribute + "andshowparent"
							+ relatedlinkedtoparentinstancename + "Action.get().getActionRef();");
					sg.wl("		update" + linknameattribute + ".set" + childclassname + "(arrayfor" + linknameattribute
							+ ".getActiveObjectArray());");
					sg.wl("		update" + linknameattribute + ".set" + objectclass
							+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass
							+ ".getDefinition().getIdMarker()));");
					String updatenote = "";
					if (childobject.IsIterated()) {
						updatenote = ",false,true";
						sg.wl("	update" + linknameattribute + ".setUpdatenote(arrayfor" + linknameattribute
								+ ".getUpdateNoteInput());");
					}
					sg.wl("		arrayfor" + linknameattribute + ".addUpdateAction(update" + linknameattribute + ", null"
							+ updatenote + ");");

					sg.wl("		AtgShow" + childclassattribute + "Action.ActionRef actionfor" + linknameattribute
							+ " = AtgShow" + childclassattribute + "Action.get().getActionRef();");
					sg.wl("		actionfor" + linknameattribute + ".setId(arrayfor" + linknameattribute
							+ ".getAttributeInput(" + childclassname + ".getIdMarker())); ");
					sg.wl("		arrayfor" + linknameattribute + ".addDefaultAction(actionfor" + linknameattribute
							+ ");");
					sg.wl("");

					sg.wl("		SComponentBand childrenactionbandfor" + childclassattribute
							+ " = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");

					sg.wl("		AtgPreparestandardcreate" + childclassattribute + "Action.ActionRef create"
							+ childclassattribute + "withparentaction = AtgPreparestandardcreate" + childclassattribute
							+ "Action.get().getActionRef();");

					DataObjectDefinition childobject = linkedfromchildren.getChildObject();
					for (int j = 0; j < linkedfromchildren.getParent().getPropertySize(); j++) {
						Property<?> thisproperty = linkedfromchildren.getParent().getPropertyAt(j);
						boolean done = false;
						sg.wl("      // item "+j );
						if (thisproperty instanceof LinkedFromChildren) {
							LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
							if (thislinkedfromchildren.getName().compareTo(linkedfromchildren.getName()) == 0) {
								sg.wl("		create" + childclassattribute + "withparentaction.set"
										+ StringFormatter.formatForJavaClass(
												thislinkedfromchildren.getOriginObjectProperty().getName())
										+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass
										+ ".getIdMarker()));");
								done = true;
							}

						}
						// not clear if needed, see github issue #26
						
						if (!done) {
							for (int k = 0; k < thisproperty.getContextDataForCreationSize(); k++) {
								// treats specific case of recursive linked to parent
								boolean exception = false;
								if (thisproperty instanceof LinkedToParent) 
									exception = true;
								
								if (!exception)
								sg.wl("		create"
										+ childclassattribute + "withparentaction.set" + StringFormatter
												.formatForJavaClass(thisproperty.getContextDataForCreation(k).getName())
										+ "();");

							}
						}

					}

					for (int j = 0; j < childobject.getPropertySize(); j++) {
						Property<?> thisproperty = childobject.getPropertyAt(j);
						if (thisproperty instanceof LinkedToParent) {
							LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) thisproperty;
							logger.finest("   --- ** --- LINKEDTOPARENTEXTRAPROCESSING "
									+ linkedtoparent.getLinkedFromChildrenName() + " - "
									+ linkedfromchildren.getInstancename());
							if (!linkedtoparent.getLinkedFromChildrenName()
									.equals(linkedfromchildren.getInstancename().toUpperCase())) {
								sg.wl("		create" + childclassattribute + "withparentaction.set"
										+ StringFormatter.formatForJavaClass(linkedtoparent.getName().toLowerCase())
										+ "id(null);");
							}

						}
					}

					sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(new SActionButton(\"Create "
							+ linkedfromchildren.getChildObject().getLabel() + "\",create" + childclassattribute
							+ "withparentaction,this));");

					sg.wl("		// --------------- load children");
					sg.wl("		AtgLoadchildren" + linknameshortname + "for" + objectvariable
							+ "Action.ActionRef loadchildrenfor" + linknameshortname + " = AtgLoadchildren"
							+ linknameshortname + "for" + objectvariable + "Action.get().getActionRef();");
					sg.wl("		loadchildrenfor" + linknameshortname
							+ ".setParentid(objectdisplaydefinition.getAttributeInput(" + objectclass
							+ ".getIdMarker()));");
					sg.wl("		SComponentBand csvloadpopupfor" + linknameshortname
							+ " = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
					sg.wl("		csvloadpopupfor" + linknameshortname
							+ ".addElement(new SPageText(\"Load from csv or excel file\",SPageText.TYPE_NORMAL, this));");
					sg.wl("		SFileChooser csvfilechooser" + linknameshortname
							+ " = new SFileChooser(this, \"CSVFILECHOSERFOR" + linknameshortname.toUpperCase()
							+ "\",\"Select File\");");
					sg.wl("		csvloadpopupfor" + linknameshortname + ".addElement(csvfilechooser" + linknameshortname
							+ ");");
					sg.wl("		SComponentBand cslvloadcbandcontentfor" + linknameshortname
							+ " = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
					sg.wl("		SCollapsibleBand csvloadcbandfor" + linknameshortname
							+ " = new SCollapsibleBand(this, cslvloadcbandcontentfor" + linknameshortname
							+ ", \"Settings\", false);");
					sg.wl("		csvloadpopupfor" + linknameshortname + ".addElement(csvloadcbandfor" + linknameshortname
							+ ");");

					sg.wl("		SChoiceTextField<ApplocaleChoiceDefinition> csvloadlocalefor" + linknameshortname
							+ " = new SChoiceTextField<ApplocaleChoiceDefinition>");
					sg.wl("			(\"Locale\",\"APPLOCALE" + linknameshortname.toUpperCase()
							+ "\",\"determines csv and number format, default is US\", ApplocaleChoiceDefinition.get(),");
					sg.wl("			null, this, true, false, false, false, null);");
					sg.wl("		csvloadlocalefor" + linknameshortname + ".setLinkedData(this.getUserlocale());");
					sg.wl("		cslvloadcbandcontentfor" + linknameshortname + ".addElement(csvloadlocalefor"
							+ linknameshortname + ");");
					sg.wl("		SChoiceTextField<PreferedfileencodingChoiceDefinition> csvloadpreffileencodingfor"
							+ linknameshortname + " = new SChoiceTextField<PreferedfileencodingChoiceDefinition>");
					sg.wl("			(\"Locale\",\"FILEENCODING" + linknameshortname.toUpperCase()
							+ "\",\"determines file encoding, default is ANSI / Windows CP1522\", PreferedfileencodingChoiceDefinition.get(),");
					sg.wl("			null, this, true, false, false, false, null);");
					sg.wl("		csvloadpreffileencodingfor" + linknameshortname
							+ ".setLinkedData(this.getPrefencoding());");
					sg.wl("		cslvloadcbandcontentfor" + linknameshortname + ".addElement(csvloadpreffileencodingfor"
							+ linknameshortname + ");");

					sg.wl("		loadchildrenfor" + linknameshortname + ".setLocale(csvloadlocalefor" + linknameshortname
							+ ".getChoiceInput()); ");
					sg.wl("		loadchildrenfor" + linknameshortname + ".setFileencoding(csvloadpreffileencodingfor"
							+ linknameshortname + ".getChoiceInput()); ");
					sg.wl("		loadchildrenfor" + linknameshortname + ".setFlatfile(csvfilechooser" + linknameshortname
							+ ".getLargeBinaryInput()); ");
					sg.wl("		SActionButton flatfileloadbuttonfor" + linknameshortname
							+ " = new SActionButton(\"Load Children File\",loadchildrenfor" + linknameshortname
							+ ",true,this);");
					sg.wl("		csvloadpopupfor" + linknameshortname + ".addElement(flatfileloadbuttonfor"
							+ linknameshortname + ");");
					sg.wl("		SPopupButton flatfilepopupbuttonfor" + linknameshortname
							+ " = new SPopupButton(this, csvloadpopupfor" + linknameshortname
							+ ",\"File Loader\",\"Load from csv or excel file\",false,loadchildrenfor"
							+ linknameshortname + ");");
					sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(flatfilepopupbuttonfor"
							+ linknameshortname + ");			");
					if (linkedfromchildren.getChildObject().hasAlias()) {
						sg.wl("		// --------------- Export children");

						sg.wl("		AtgExportchildren" + linknameshortname + "for" + objectvariable
								+ "Action.InlineActionRef exportchildrenfor" + linknameshortname
								+ " = AtgExportchildren" + linknameshortname + "for" + objectvariable
								+ "Action.get().getInlineActionRef();");
						sg.wl("		exportchildrenfor" + linknameshortname
								+ ".setParentid(objectdisplaydefinition.getAttributeInput(" + objectclass
								+ ".getIdMarker()));");

						if (linkedfromchildren.getChildObject().getCategoryForExtractor() == null) {
							sg.wl("		SActionButton exportchildrenfor" + linknameshortname
									+ "button = new SActionButton(\"Export Loadable File\",exportchildrenfor"
									+ linknameshortname + ",true,this); ");
							sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(exportchildrenfor"
									+ linknameshortname + "button);			");
						} else {
							ChoiceCategory category = linkedfromchildren.getChildObject().getCategoryForExtractor();
							String choiceclass = StringFormatter.formatForJavaClass(category.getName());
							sg.wl("		SComponentBand exportchildrenfor" + linknameshortname
									+ "popup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
							sg.wl("		SChoiceTextField<" + choiceclass + "ChoiceDefinition> exportchildrenfor"
									+ linknameshortname + "choice");
							sg.wl("			= new SChoiceTextField<" + choiceclass
									+ "ChoiceDefinition>(\"Export Type\", \"" + linknameshortname.toUpperCase()
									+ "CHOICE\", \"\",");
							sg.wl("				" + choiceclass + "ChoiceDefinition.get(),null,  this, false, null);");
							sg.wl("		exportchildrenfor" + linknameshortname + ".setExporttype(exportchildrenfor"
									+ linknameshortname + "choice.getChoiceInput());");
							sg.wl("		exportchildrenfor" + linknameshortname + "popup.addElement(exportchildrenfor"
									+ linknameshortname + "choice);");
							sg.wl("		SActionButton exportchildrenfor" + linknameshortname
									+ "button = new SActionButton(\"Export\",exportchildrenfor" + linknameshortname
									+ ",true,this);");
							sg.wl("		exportchildrenfor" + linknameshortname + "popup.addElement(exportchildrenfor"
									+ linknameshortname + "button);");
							sg.wl("		SPopupButton exportchildrenfor" + linknameshortname
									+ "popupbutton = new SPopupButton(this, exportchildrenfor" + linknameshortname
									+ "popup, \"Export Loadable File\", \"\");");
							sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(exportchildrenfor" + linknameshortname
									+ "popupbutton);			");

						}
						sg.wl("		SFileDownloader exportchildrenfor" + linknameshortname
								+ "downloader = new SFileDownloader(\"DOWNLOADERFOR" + linknameshortname.toUpperCase()
								+ "\", this, exportchildrenfor" + linknameshortname + ",AtgExportchildren"
								+ linknameshortname + "for" + objectvariable + "Action.get().getFlatfileRef());");
						sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(exportchildrenfor"
								+ linknameshortname + "downloader);");

					}

					sg.wl("		");

					sg.wl("		// --------------- delete several --------------------- ");
					String linkedtoparentnickname = originobjectproperty.getInstancename().toLowerCase();
					sg.wl("		AtgMassivedelete" + childclassattribute + "andshowparent" + linkedtoparentnickname
							+ "Action.ActionRef massivedelete" + childclassattribute + "andshow"
							+ linkedtoparentnickname + "action = AtgMassivedelete" + childclassattribute
							+ "andshowparent" + linkedtoparentnickname + "Action.get().getActionRef();");
					sg.wl("		massivedelete" + childclassattribute + "andshow" + linkedtoparentnickname
							+ "action.setParent" + objectvariable + "id(objectdisplaydefinition.getAttributeInput("
							+ objectclass + ".getIdMarker()));");
					sg.wl("		massivedelete" + childclassattribute + "andshow" + linkedtoparentnickname + "action.set"
							+ childclassname + "id(arrayfor" + linknameattribute + ".getAttributeArrayInput("
							+ childclassname + ".getIdMarker())); ");
					sg.wl("		SActionButton massivedelete" + childclassattribute + "andshow" + linkedtoparentnickname
							+ "actionbutton = new SActionButton(\"Delete Selected\",massivedelete" + childclassattribute
							+ "andshow" + linkedtoparentnickname + "action,true,this);");
					sg.wl("		massivedelete" + childclassattribute + "andshow" + linkedtoparentnickname
							+ "actionbutton.setConfirmationMessage(\"Do you want to delete all selected "
							+ originobjectproperty.getParent().getLabel() + " ?\");");
					sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(massivedelete"
							+ childclassattribute + "andshow" + linkedtoparentnickname + "actionbutton);");

					sg.wl("		" + locationname + ".addElement(childrenactionbandfor" + childclassattribute + ");");
					sg.wl("		" + locationname + ".addElement(arrayfor" + linknameattribute + ");");
				} else {
					// show children objects as an object Band, not object array

					sg.wl("		SObjectBand<" + childclassname + "> arrayfor" + linknameattribute
							+ " = new SObjectBand<" + childclassname + ">(\"CHILDTICKETCOMMENT\",");
					sg.wl("				this.get" + linknameclass + "(),");
					sg.wl("				" + childclassname + ".getDefinition(),");
					sg.wl("				this);");
					sg.wl("		AtgPrepareupdate" + childclassattribute + "Action.ActionRef update"
							+ childclassattribute + " = AtgPrepareupdate" + childclassattribute
							+ "Action.get().getActionRef();");
					sg.wl("		update" + childclassattribute + ".setId(arrayfor" + linknameattribute
							+ ".getAttributeInput(" + childclassname + ".getIdMarker())); ");
					sg.wl("		SActionButton update" + childclassattribute
							+ "button = new SActionButton(\"Update\",\"Update the selected "
							+ linkedfromchildren.getChildObject().getLabel() + "\", update" + childclassattribute
							+ ", this);");
					sg.wl("		");

					sg.wl("		AtgDelete" + childclassattribute + "andshowparentAction.ActionRef delete"
							+ childclassattribute + " = AtgDelete" + childclassattribute
							+ "andshowparentAction.get().getActionRef();");
					sg.wl("		delete" + childclassattribute + ".set" + childclassname + "id(arrayfor"
							+ linknameattribute + ".getAttributeInput(" + childclassname + ".getIdMarker()));");
					sg.wl("		SActionButton delete" + childclassattribute
							+ "button = new SActionButton(\"Delete\",\"Delete this "
							+ linkedfromchildren.getChildObject().getLabel() + "\",delete" + childclassattribute
							+ ",this);");
					sg.wl("		SComponentBand " + childclassattribute
							+ "specificbuttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
					sg.wl("		" + childclassattribute + "specificbuttonband.addElement(update" + childclassattribute
							+ "button);");
					sg.wl("		" + childclassattribute + "specificbuttonband.addElement(delete" + childclassattribute
							+ "button);");

					sg.wl("		arrayfor" + linknameattribute + ".setActionGroup(" + childclassattribute
							+ "specificbuttonband);");
					sg.wl("		" + locationname + ".addElement(arrayfor" + linknameattribute + ");");
					sg.wl("");
					sg.wl("		");
					sg.wl("		// lower buttons");
					sg.wl("		SComponentBand childrenactionbandfor" + childclassattribute
							+ " = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
					sg.wl("		AtgPreparestandardcreate" + childclassattribute + "Action.ActionRef create"
							+ childclassattribute + "withparentaction = AtgPreparestandardcreate" + childclassattribute
							+ "Action.get().getActionRef();");
					sg.wl("		create" + childclassattribute + "withparentaction.set"
							+ StringFormatter.formatForJavaClass(subobject.getName())
							+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker())); ");
					sg.wl("		childrenactionbandfor" + childclassattribute + ".addElement(new SActionButton(\"Create "
							+ linkedfromchildren.getChildObject().getLabel() + "\",create" + childclassattribute
							+ "withparentaction,this));");

					sg.wl("		" + locationname + ".addElement(childrenactionbandfor" + childclassattribute + ");");

				}
			} else {
				// display grid
				sg.wl("");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("		// Display all children objects of type " + childclassname + " in " + objectclass
						+ " as GRID-, reverse = "+LinkedFromChildren.this.reversetree);
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("");

				String label = "Related " + linkedfromchildren.getChildObject().getLabel();
				if (specifictitleforchildrentable != null)
					label = specifictitleforchildrentable;
				sg.wl("		" + locationname + ".addElement(new SPageText(\"" + label
						+ "\",SPageText.TYPE_TITLE,this));");

				sg.wl("		AtgMassupdate" + childclassattribute + "Action.InlineActionRef massupdate"
						+ linknameattribute + " = AtgMassupdate" + childclassattribute
						+ "Action.get().getInlineActionRef();");
				sg.wl("");
				sg.wl("		AtgShow" + childclassattribute + "Action.ActionRef show" + linknameattribute
						+ "detail = AtgShow" + childclassattribute + "Action.get().getActionRef();");
				sg.wl("");
				sg.wl("");
				if (linkedfromchildren.cellfieldsforgrid.length > 1) {
					sg.wl("		ArrayList<DataObjectFieldMarker<" + childclassname + ">> " + linknameattribute
							+ "values = new ArrayList<DataObjectFieldMarker<" + childclassname + ">>();");
					for (int i = 0; i < linkedfromchildren.cellfieldsforgrid.length; i++)
						sg.wl("		values.add(" + childclassname + "Definition.get" + childclassname
								+ "Definition().get"
								+ StringFormatter.formatForJavaClass(linkedfromchildren.cellfieldsforgrid[i])
								+ "FieldMarker());");
				}
				sg.wl("		SGrid<" + childclassname + "> " + linknameattribute + "grid = new SGrid<" + childclassname
						+ ">(\"TESTGRID\",");
				sg.wl("				this,");
				sg.wl("				this.get" + linknameclass + "(),");

				sg.wl("				" + childclassname + "Definition.get" + childclassname + "Definition().get"
						+ StringFormatter.formatForJavaClass(linkedfromchildren.linedisplayforgrid) + "FieldMarker(),");
				sg.wl("				" + childclassname + "Definition.get" + childclassname + "Definition().get"
						+ StringFormatter.formatForJavaClass(linkedfromchildren.columndisplayforgrid)
						+ "FieldMarker(),");
				if (linkedfromchildren.secondarycolumndisplayforgrid != null)
					sg.wl("				" + childclassname + "Definition.get" + childclassname + "Definition().get"
							+ StringFormatter.formatForJavaClass(linkedfromchildren.secondarycolumndisplayforgrid)
							+ "FieldMarker(),");

				if (linkedfromchildren.cellfieldsforgrid.length == 1) {
					sg.wl("				" + childclassname + "Definition.get" + childclassname + "Definition().get"
							+ StringFormatter.formatForJavaClass(linkedfromchildren.cellfieldsforgrid[0])
							+ "FieldMarker(),");
				} else {
					sg.wl("				" + linknameattribute + "values,");
				}
				sg.wl("				" + childclassname + "Definition.get" + childclassname + "Definition());");
				
				// ------------------- Reverse tree display
				
				if (LinkedFromChildren.this.reversetree) {
					StringBuffer readonlyfields = new StringBuffer();
					for (int i=0;i<LinkedFromChildren.this.infofieldforreverseshow.length;i++) {
						String field = LinkedFromChildren.this.infofieldforreverseshow[i];
						if (i>0) readonlyfields.append(',');
						readonlyfields.append(childclassname+".get"+StringFormatter.formatForJavaClass(field)+"FieldMarker()");
						
					}
					StringBuffer readonlyfieldsexceptions = new StringBuffer();
					if (LinkedFromChildren.this.exceptionsforinfofieldconsolidation!=null)
						for (int i=0;i<LinkedFromChildren.this.exceptionsforinfofieldconsolidation.length;i++) {
							String exception = LinkedFromChildren.this.exceptionsforinfofieldconsolidation[i];
							if (i>0) readonlyfieldsexceptions.append(',');
							if (exception==null) {
								readonlyfieldsexceptions.append("null");
							} else {
								readonlyfieldsexceptions.append("\"");
								readonlyfieldsexceptions.append(StringFormatter.escapeforjavastring(exception));
								readonlyfieldsexceptions.append("\"");
							}
						}
					sg.wl("		" + linknameattribute + "grid.setReverseTree((DataObjectFieldMarker<" + childclassname + ">[])");
					sg.wl("				(new DataObjectFieldMarker<?>[] {"+readonlyfields+"}),new String[]{"+readonlyfieldsexceptions.toString()+"});");
					
					
					
				}
				
				sg.w("		" + linknameattribute + "grid.setWarningForUnsavedEdition();");
				sg.wl("");
				sg.wl("		massupdate" + linknameattribute + ".set" + childclassname + "(" + linknameattribute
						+ "grid.getUpdatedObjectArrayInput()); ");
				String updatenote = "";
				if (childobject.IsIterated()) {
					updatenote = ",true";
					sg.wl("	massupdate" + linknameattribute + ".setUpdatenote(" + linknameattribute
							+ "grid.getUpdateNoteInput()); ");

				}
				sg.wl("		" + linknameattribute + "grid.addUpdateAction(massupdate" + linknameattribute
						+ ", null,AtgMassupdate" + childclassattribute + "Action.get().getUpdated" + childclassattribute
						+ "Ref()" + updatenote + ");");
				sg.wl("		show" + linknameattribute + "detail.setId(" + linknameattribute + "grid.getAttributeInput("
						+ childclassname + ".getIdMarker())); ");
				sg.wl("		" + linknameattribute + "grid.setDefaultAction(show" + linknameattribute + "detail);");
				sg.wl("		" + locationname + ".addElement(" + linknameattribute + "grid);");

			}
		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return linkedfromchildren.linkedfromchildrenwidgetdisplaypriority;
		}
	}

	/**
	 * provides the widget showing the children table to allow to give it a
	 * different priority
	 * 
	 * @return the widget of the children table
	 */
	public Widget generateChildrenTableWidget() {
		return new ChildrenTable(this);
	}

	/**
	 * creates a linked from children property with specified display priority for
	 * the child table
	 * 
	 * @param name                                    unique name amongst linked
	 *                                                from children property of this
	 *                                                object
	 * @param childobjectforlink                      definition of the child object
	 * @param originobjectproperty                    LinkedToParent property on the
	 *                                                child object
	 * @param linkedfromchildrenwidgetdisplaypriority specific widget display
	 *                                                priority for the child table
	 *                                                widget
	 */
	public LinkedFromChildren(
			String name,
			DataObjectDefinition childobjectforlink,
			LinkedToParent<?> originobjectproperty,
			WidgetDisplayPriority linkedfromchildrenwidgetdisplaypriority) {
		this(name, childobjectforlink, originobjectproperty);
		this.linkedfromchildrenwidgetdisplaypriority = linkedfromchildrenwidgetdisplaypriority;
		this.displaychildrenasgrid = false;
	}

	/**
	 * creates a linked from children property with default display
	 * 
	 * @param name                 unique name amongst linked from children property
	 *                             of this object
	 * @param childobjectforlink   definition of the child object
	 * @param originobjectproperty LinkedToParent property on the child object
	 */
	public LinkedFromChildren(
			String name,
			DataObjectDefinition childobjectforlink,
			LinkedToParent<?> originobjectproperty) {
		super(name, "LINKEDFROMCHILDREN");
		this.addPropertyGenerics(new PropertyGenerics("CHILDOBJECTFORLINK", childobjectforlink, originobjectproperty));
		this.childobject = childobjectforlink;
		this.originobjectproperty = originobjectproperty;
		if (this.childobject == null)
			throw new RuntimeException("for Linked from children name = " + name + " parent = " + parent.getName()
					+ ", childobject is null");
		this.displaychildrenasgrid = false;
	}

	/**
	 * creates a linked from children property with grid display with specific
	 * display priority
	 * 
	 * @param name                                    unique name amongst linked
	 *                                                from children property of this
	 *                                                object
	 * @param childobjectforlink                      definition of the child object
	 * @param originobjectproperty                    LinkedToParent property on the
	 *                                                child object
	 * @param linkedfromchildrenwidgetdisplaypriority widget display priority
	 * @param linedisplayforgrid                      the column used for line
	 *                                                display
	 * @param columndisplayforgrid                    the column used for column
	 *                                                display
	 * @param secondarycolumndisplayforgrid           the column used for secondary
	 *                                                column display (leave it null
	 *                                                if not used)
	 * @param cellfieldsforgrid                       the name of the fields to show
	 *                                                as content in the grid
	 */
	public LinkedFromChildren(
			String name,
			DataObjectDefinition childobjectforlink,
			LinkedToParent<?> originobjectproperty,
			WidgetDisplayPriority linkedfromchildrenwidgetdisplaypriority,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String[] cellfieldsforgrid) {
		this(name, childobjectforlink, originobjectproperty);
		this.linkedfromchildrenwidgetdisplaypriority = linkedfromchildrenwidgetdisplaypriority;
		this.displaychildrenasgrid = true;
		this.linedisplayforgrid = linedisplayforgrid;
		this.columndisplayforgrid = columndisplayforgrid;
		this.secondarycolumndisplayforgrid = secondarycolumndisplayforgrid;
		this.cellfieldsforgrid = cellfieldsforgrid;
	}

	/**
	 * creates a linked from children property with grid display
	 * 
	 * @param name                          unique name amongst linked from children
	 *                                      property of this object
	 * @param childobjectforlink            definition of the child object
	 * @param originobjectproperty          LinkedToParent property on the child
	 *                                      object
	 * @param linedisplayforgrid            the column used for line display
	 * @param columndisplayforgrid          the column used for column display
	 * @param secondarycolumndisplayforgrid the column used for secondary column
	 *                                      display (leave it null if not used)
	 * @param cellfieldsforgrid             the name of the fields to show as
	 *                                      content in the grid
	 */
	public LinkedFromChildren(
			String name,
			DataObjectDefinition childobjectforlink,
			LinkedToParent<?> originobjectproperty,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String[] cellfieldsforgrid) {
		super(name, "LINKEDFROMCHILDREN");
		this.addPropertyGenerics(new PropertyGenerics("CHILDOBJECTFORLINK", childobjectforlink, originobjectproperty));
		this.childobject = childobjectforlink;
		this.originobjectproperty = originobjectproperty;
		if (this.childobject == null)
			throw new RuntimeException("for Linked from children name = " + name + " parent = " + parent.getName()
					+ ", childobject is null");
		this.displaychildrenasgrid = true;
		this.linedisplayforgrid = linedisplayforgrid;
		this.columndisplayforgrid = columndisplayforgrid;
		this.secondarycolumndisplayforgrid = secondarycolumndisplayforgrid;
		this.cellfieldsforgrid = cellfieldsforgrid;
		this.reversetree=false;
	}

	public LinkedFromChildren(
			String name,
			DataObjectDefinition childobjectforlink,
			LinkedToParent<?> originobjectproperty,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String[] cellfieldsforgrid,
			String[] infofieldforreverseshow,
			String[] exceptionsforinfofieldconsolidation) {
		this(name,childobjectforlink,originobjectproperty,linedisplayforgrid,columndisplayforgrid,secondarycolumndisplayforgrid,cellfieldsforgrid);
		this.infofieldforreverseshow = infofieldforreverseshow;
		this.exceptionsforinfofieldconsolidation = exceptionsforinfofieldconsolidation;
		this.reversetree=true;
	}

	@Override
	public void controlAfterParentDefinition() {
		DataAccessMethod getallchildren = new DataAccessMethod("GETALLCHILDREN",
				new ArrayArgument(new ObjectArgument("children", this.childobject)), true, true);
		getallchildren.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		this.addDataAccessMethod(getallchildren);

		actionsonobjectid = new NamedList<DynamicActionDefinition>();
		actionsonselectedchildid = new NamedList<DynamicActionDefinition>();
		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		MethodAdditionalProcessing deleteobjectcheck = new MethodAdditionalProcessing(true,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(deleteobjectcheck);
	}

	public LinkedToParent<?> getOriginObjectProperty() {
		return this.originobjectproperty;
	}

	public DataObjectDefinition getChildObject() {
		return childobject;
	}

	/**
	 * sets a specific label for the children table widget on the parent
	 * 
	 * @param specifictitleforchildrentable specific title
	 */
	protected void setSpecificTitleForChildrenTable(String specifictitleforchildrentable) {
		this.specifictitleforchildrentable = specifictitleforchildrentable;

	}

	/**
	 * adds an action button on the parent object page to launch an action on the
	 * selected child object in the children table
	 * 
	 * @param action an action with a unique input argument being the object id of
	 *               the child
	 */
	public void addActionOnSelectedChildId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException("you can add an action on selected child id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != childobject) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ childobject.getOwnermodule().getName() + "/" + childobject.getName());
		}
		actionsonselectedchildid.add(action);
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
	public void setFinalSettings() {

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	@Override
	public String getJavaType() {

		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.formula.LinkedToChildrenNavigator;");

	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(childobject);
		return dependencies;
	}

}
