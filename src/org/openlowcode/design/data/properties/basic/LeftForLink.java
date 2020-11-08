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

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.PageDefinition;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property is added to an object that is on the left of a
 * {@link org.openlowcode.design.data.properties.basic.LinkObject} <br>
 * <br>
 * Warning: this should not be added directly by developers <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class LeftForLink<E extends DataObjectDefinition, F extends DataObjectDefinition>
		extends
		Property<LeftForLink<E, F>> {

	private NamedList<DynamicActionDefinition> actionsonobjectid;
	private NamedList<DynamicActionDefinition> actionsonselectedlinkid;
	private NamedList<DynamicActionDefinition> actiononselectedrightobjectid;
	private NamedList<PageDefinition> popuppagesonobjectid;
	private UniqueIdentified uniqueidentified;
	private E linkdataobject;
	private F rightobjectforlink;
	private LinkObject<E, F> linkobject;
	private WidgetDisplayPriority linkfromleftpriority;

	/**
	 * @return the widget for link from left (table)
	 */
	public Widget generateLinkFromLeftTableWidget() {
		return new LinkFromLeftTableWidget(this);
	}

	/**
	 * creates a left for link property (note: this is called by the framework, and
	 * should not be called directly by the user of the designer)
	 * 
	 * @param linkdataobject       the data object holding the link
	 * @param rightobjectforlink   the right object for the link
	 * @param linkobject           the link property on the data object holding the
	 *                             link
	 * @param linkfromleftpriority widget display priority for the left for link
	 *                             widget on the object
	 */
	public LeftForLink(
			E linkdataobject,
			F rightobjectforlink,
			LinkObject<E, F> linkobject,
			WidgetDisplayPriority linkfromleftpriority) {
		super(linkdataobject.getName(), "LEFTFORLINK");
		this.linkdataobject = linkdataobject;
		this.rightobjectforlink = rightobjectforlink;
		this.linkobject = linkobject;
		this.linkfromleftpriority = linkfromleftpriority;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.addPropertyGenerics(new PropertyGenerics("LINKOBJECT", linkdataobject, linkobject));
		this.addPropertyGenerics(new PropertyGenerics("RIGHTOBJECTFORLINK", rightobjectforlink,
				rightobjectforlink.getPropertyByName("UNIQUEIDENTIFIED")));
		actionsonobjectid = new NamedList<DynamicActionDefinition>();
		actionsonselectedlinkid = new NamedList<DynamicActionDefinition>();
		actiononselectedrightobjectid = new NamedList<DynamicActionDefinition>();
		popuppagesonobjectid = new NamedList<PageDefinition>();
		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		MethodAdditionalProcessing deletelinkwithobject = new MethodAdditionalProcessing(false,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(deletelinkwithobject);
		MethodAdditionalProcessing controlinkconstraintbeforeupdate = new MethodAdditionalProcessing(true,
				uniqueidentified.getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(controlinkconstraintbeforeupdate);

	}

	/**
	 * @return the right object for the link
	 */
	public F getRightObjectForLink() {
		return this.rightobjectforlink;
	}

	@Override
	public String getJavaType() {
		return "#NOTIMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// nothing to do
	}

	/**
	 * @return the link object
	 */
	public DataObjectDefinition getLinkObjectDefinition() {

		return this.linkdataobject;
	}

	/**
	 * @return the link object property on the link object
	 */
	public LinkObject<E, F> getLinkObjectProperty() {
		DataObjectDefinition linkobject = linkdataobject;
		@SuppressWarnings("unchecked")
		LinkObject<E, F> linkobjectproperty = (LinkObject<E, F>) linkobject.getPropertyByName("LINKOBJECT");
		if (linkobjectproperty == null)
			throw new RuntimeException("link object does not have property");
		return linkobjectproperty;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(linkdataobject);
		dependencies.add(rightobjectforlink);

		return dependencies;
	}

	/**
	 * adds an action on the left object of a link (checks if it makes sense and is
	 * used. Github issue #16)
	 * 
	 * @param action action to be added
	 */
	public void addActionOnObjectId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() != 1)
			throw new RuntimeException("you can add an action on object id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(0);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != parent) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ parent.getOwnermodule().getName() + "/" + parent.getName());
		}

		actionsonobjectid.add(action);
	}

	/**
	 * shows a popup page on the right object of the link
	 * 
	 * @param popuppage a page to show
	 */
	public void addPopupOnObjectId(PageDefinition popuppage) {
		if (popuppage.getPageAttributes().getSize() != 1)
			throw new RuntimeException("you can add a popuppage on object id only if it has 1 argument, page "
					+ popuppage.getName() + " has " + popuppage.getPageAttributes().getSize() + ".");
		ArgumentContent uniqueinputargument = popuppage.getPageAttributes().get(0);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + popuppage.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != parent) {
			throw new RuntimeException("objectid should be of consistent type for popuppage " + popuppage.getName()
					+ ", actionid type = " + objectforid.getOwnermodule().getName() + "/" + objectforid.getName()
					+ ", object parentid type = " + parent.getOwnermodule().getName() + "/" + parent.getName());
		}

		popuppagesonobjectid.add(popuppage);
	}

	/**
	 * * this method will add an action on the id of the link object in the link
	 * table
	 * 
	 * @param action action the action to add (should have as only input data object
	 *               id of the link object
	 */
	public void addActionOnSelectedLinkId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException("you can add an action on selected link id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != linkdataobject) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ linkdataobject.getOwnermodule().getName() + "/" + linkdataobject.getName());
		}

		actionsonselectedlinkid.add(action);
	}

	/**
	 * this method will add an action on the id of the right object in the link
	 * table
	 * 
	 * @param action the action to add (should have as only input data object id of
	 *               the right object
	 */
	public void addActionOnSelectedRightObjectId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException(
					"you can add an action on selected link right object id only if it has 1 argument, action "
							+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != rightobjectforlink) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ rightobjectforlink.getOwnermodule().getName() + "/" + rightobjectforlink.getName());
		}

		actiononselectedrightobjectid.add(action);
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
		// do nothing in that case

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	/**
	 * the widget showing the links as a table on the left object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class LinkFromLeftTableWidget
			extends
			Widget {
		private LeftForLink<E, F> parentproperty;
		private Typed typed;
		@SuppressWarnings("rawtypes")
		private ConstraintOnLinkTypeRestrictionForLeft typerestrictionforleft;

		/**
		 * creaes a table widget for left for link
		 * 
		 * @param parentproperty parent property left for link
		 */
		@SuppressWarnings("rawtypes")
		LinkFromLeftTableWidget(LeftForLink<E, F> parentproperty) {
			super("LINKFROMLEFTTABLE");
			this.parentproperty = parentproperty;
			typed = (Typed) this.parentproperty.getParent().getPropertyByName("TYPED");
			typerestrictionforleft = (ConstraintOnLinkTypeRestrictionForLeft) parentproperty
					.getLinkObjectProperty().getBusinessRuleByName("TYPERESTRICTIONFORLEFT");
		}

		@Override
		public String[] getImportStatements() {
			ArrayList<String> importstatements = new ArrayList<String>();
			importstatements.add("import org.openlowcode.server.action.SInlineActionRef;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectStorage;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SFieldSearcher;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectArrayField;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SPopupButton;");
			if (typerestrictionforleft!=null) {
				importstatements.add("import java.util.ArrayList;");
			}
			String linkobjectvariable = StringFormatter
					.formatForAttribute(parentproperty.getLinkObjectDefinition().getName());
			String linkobjectclass = StringFormatter
					.formatForJavaClass(parentproperty.getLinkObjectDefinition().getName());
			Module linkobjectmodule = parentproperty.getLinkObjectDefinition().getOwnermodule();

			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgCreate"
					+ linkobjectvariable + "Action;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgDelete"
					+ linkobjectvariable + "andshowleftAction;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".data." + linkobjectclass + "Definition;");
			importstatements.add("import " + linkobjectmodule.getPath() + ".action.generated.AtgMassupdate"
					+ linkobjectvariable + "andshowleftAction;");
			LinkObject<E, F> linkobject = parentproperty.getLinkObjectProperty();
			Module module = parentproperty.getParent().getOwnermodule();
			if (linkobject.getBusinessRuleNumber() > 0) {
				importstatements.add("import " + module.getPath() + ".action.generated.AtgSearchright"
						+ linkobject.getRightobjectforlink().getName().toLowerCase() + "for"
						+ parentproperty.getLinkObjectDefinition().getName().toLowerCase() + "Action;");
			}

			return importstatements.toArray(new String[0]);
		}

		@Override
		public void generateWidgetCode(
				SourceGenerator sg,
				Module module,
				String locationname,
				DataObjectDefinition companion) throws IOException {
			DataObjectDefinition linkobject = parentproperty.getLinkObjectDefinition();
			String objectvariable = StringFormatter.formatForAttribute(parentproperty.getParent().getName());
			String objectclass = StringFormatter.formatForJavaClass(parentproperty.getParent().getName());

			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String rightobjectvariable = StringFormatter
					.formatForAttribute(parentproperty.getRightObjectForLink().getName());
			String rightobjectclass = StringFormatter
					.formatForJavaClass(parentproperty.getRightObjectForLink().getName());
			@SuppressWarnings("unchecked")
			DisplayLinkAsAttributeFromLeftObject<E, F> attributeasleft = (DisplayLinkAsAttributeFromLeftObject<
					E, F>) parentproperty.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

			if (attributeasleft == null) {
				// -------------------------------------------------------------------------------------------------
				// show link as table. This is the classical display
				sg.wl("");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("		// Display " + linkobjectclass);
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("");
				if (this.typerestrictionforleft!=null) {
					sg.wl("		ArrayList<SPageNode> left" + linkobjectvariable + "nodes = new ArrayList<SPageNode>();");
					sg.wl("		left" + linkobjectvariable + "nodes.add(new SPageText(\""
							+ parentproperty.getLinkObjectProperty().getLabelFromLeft()
							+ "\",SPageText.TYPE_TITLE,this));");
				} else {
				
				sg.wl("		" + locationname + ".addElement(new SPageText(\""
						+ parentproperty.getLinkObjectProperty().getLabelFromLeft()
						+ "\",SPageText.TYPE_TITLE,this));");
				}
				sg.wl("		SObjectArray<" + linkobjectclass + "> left" + linkobjectvariable + "s = new SObjectArray<"
						+ linkobjectclass + ">(\"LEFT" + linkobjectclass.toUpperCase() + "\",");
				sg.wl("				this.getLeftforlinkfor" + linkobjectvariable + "(),");
				sg.wl("				" + linkobjectclass + ".getDefinition(),");
				sg.wl("				this);");
				sg.wl("		left" + linkobjectvariable + "s.addDisplayProfile(" + linkobjectclass + "Definition.get"
						+ linkobjectclass + "Definition().getDisplayProfileHideleftobjectfields());");

				sg.wl("		left" + linkobjectvariable + "s.setWarningForUnsavedEdition();");
				sg.wl("		AtgMassupdate" + linkobjectvariable + "andshowleftAction.ActionRef updateleft"
						+ linkobjectvariable + "s = AtgMassupdate" + linkobjectvariable
						+ "andshowleftAction.get().getActionRef();");
				sg.wl("		updateleft" + linkobjectvariable + "s.set" + linkobjectclass + "(left" + linkobjectvariable
						+ "s.getActiveObjectArray()); ");
				if (linkobject.IsIterated())
					sg.wl("		updateleft" + linkobjectvariable + "s.setUpdatenote(left" + linkobjectvariable
							+ "s.getUpdateNoteInput());");
				sg.wl("		updateleft" + linkobjectvariable + "s.set" + objectclass
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		left" + linkobjectvariable + "s.addUpdateAction(updateleft" + linkobjectvariable
						+ "s, null);");
				sg.wl("		");

				sg.wl("		AtgShow" + rightobjectvariable + "Action.ActionRef showright" + rightobjectvariable + "for"
						+ linkobjectvariable + "action = AtgShow" + rightobjectvariable
						+ "Action.get().getActionRef();");
				sg.wl("		showright" + rightobjectvariable + "for" + linkobjectvariable + "action.setId(left"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getRgidMarker()));");
				sg.wl("		left" + linkobjectvariable + "s.addDefaultAction(showright" + rightobjectvariable + "for"
						+ linkobjectvariable + "action);");
				sg.wl("		");

				sg.wl("		SComponentBand addtoleft" + linkobjectvariable
						+ "s = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
				sg.wl("		");
				sg.wl("		// add link");
				sg.wl("		SPageText titleforleft" + linkobjectvariable + " = new  SPageText(\"Add a new "
						+ linkobject.getLabel() + " to the " + parentproperty.getParent().getLabel()
						+ "\",SPageText.TYPE_TITLE,this);");
				sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(titleforleft" + linkobjectvariable + ");");
				sg.wl("				");

				LinkObject<E, F> linkobjectproperty = parentproperty.getLinkObjectProperty();
				if (linkobjectproperty.getBusinessRuleNumber() > 0) {
					// if business rule on link, use specific object searcher for right object
					sg.wl("		SObjectSearcher<" + rightobjectclass + "> " + rightobjectvariable + "searchforaddtoleft"
							+ linkobjectvariable + "s = AtgSearch" + rightobjectvariable + "Page.getsearchpanel(");
					sg.wl("			((AtgSearchright" + rightobjectvariable + "for" + linkobjectvariable
							+ "Action)AtgSearchright" + rightobjectvariable + "for" + linkobjectvariable
							+ "Action.get()).new Specific" + rightobjectclass + "Searcher(");
					sg.wl("					objectdisplaydefinition.getAttributeInput(" + objectclass
							+ ".getIdMarker()),null),");
					sg.wl("			this,\"" + linkobject.getName().toUpperCase() + "SEARCH\");");
					sg.wl("		" + rightobjectvariable + "searchforaddtoleft" + linkobjectvariable
							+ "s.getresultarray().setAllowMultiSelect(); ");

					sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(" + rightobjectvariable
							+ "searchforaddtoleft" + linkobjectvariable + "s);");
					sg.wl("");
				} else {
					// if no business rule on link, use standard object searcher for right object
					sg.wl("		SObjectSearcher<" + rightobjectclass + "> " + rightobjectvariable + "searchforaddtoleft"
							+ linkobjectvariable + "s = AtgSearch" + rightobjectvariable + "Page.getsearchpanel(this,\""
							+ linkobject.getName().toUpperCase() + "SEARCH\");");
					sg.wl("		" + rightobjectvariable + "searchforaddtoleft" + linkobjectvariable
							+ "s.getresultarray().setAllowMultiSelect(); ");
					sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(" + rightobjectvariable
							+ "searchforaddtoleft" + linkobjectvariable + "s);");
					sg.wl("");
				}

				sg.wl("		SObjectDisplay<" + linkobjectclass + "> blanklinkforaddtoleft" + linkobjectvariable
						+ "s = new  SObjectDisplay<" + linkobjectclass + ">(\"BLANK"
						+ linkobject.getName().toUpperCase() + "FORADD\",");

				sg.wl("			this.getLeftforlinkfor" + linkobjectvariable + "blankforadd(), " + linkobjectclass
						+ ".getDefinition(),this, false);");
				sg.wl("		blanklinkforaddtoleft" + linkobjectvariable + "s.setHideReadOnly();");
				sg.wl("				");
				sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(blanklinkforaddtoleft" + linkobjectvariable
						+ "s);");
				sg.wl("				");
				sg.wl("		AtgCreate" + linkobjectvariable + "Action.ActionRef createlinkactionforaddtoleft"
						+ linkobjectvariable + "s = AtgCreate" + linkobjectvariable + "Action.get().getActionRef();");
				sg.wl("				");
				sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.setLeft" + objectvariable
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.set" + linkobjectclass
						+ "(blanklinkforaddtoleft" + linkobjectvariable + "s.getObjectInput());");
				sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.setRight" + rightobjectvariable
						+ "id(" + rightobjectvariable + "searchforaddtoleft" + linkobjectvariable
						+ "s.getresultarray().getAttributeArrayInput(" + rightobjectclass + ".getIdMarker()));");
				sg.wl("					");
				sg.wl("		SActionButton createlinkbuttonforaddtoleft" + linkobjectvariable
						+ " = new SActionButton(\"Add Link\", createlinkactionforaddtoleft" + linkobjectvariable
						+ "s, this);");
				sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(createlinkbuttonforaddtoleft"
						+ linkobjectvariable + ");");
				sg.wl("		");
				sg.wl("		SPopupButton addtoleft" + linkobjectvariable + "sbutton = new SPopupButton(this,addtoleft"
						+ linkobjectvariable + "s,\"Add\",\"you can link an existing " + rightobjectclass + " to this "
						+ objectclass + " through the " + linkobject.getLabel() + " Link\",createlinkactionforaddtoleft"
						+ linkobjectvariable + "s);");

				sg.wl("		SComponentBand left" + linkobjectvariable
						+ "buttonbar = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);");
				sg.wl("		left" + linkobjectvariable + "buttonbar.addElement(addtoleft" + linkobjectvariable
						+ "sbutton);");
				sg.wl("		");
				sg.wl("		AtgDelete" + linkobjectvariable + "andshowleftAction.ActionRef deleteoneofleft"
						+ linkobjectvariable + " = AtgDelete" + linkobjectvariable
						+ "andshowleftAction.get().getActionRef();");
				sg.wl("		deleteoneofleft" + linkobjectvariable + ".setLeft" + objectvariable
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		deleteoneofleft" + linkobjectvariable + ".set" + linkobjectclass + "id(left"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getIdMarker()));");
				sg.wl("		SActionButton deleteoneofleft" + linkobjectvariable
						+ "button = new SActionButton(\"Delete selected\", deleteoneofleft" + linkobjectvariable
						+ ", this);");
				sg.wl("		left" + linkobjectvariable + "buttonbar.addElement(deleteoneofleft" + linkobjectvariable
						+ "button);");
				if (this.typerestrictionforleft!=null) {
					sg.wl("			left" + linkobjectvariable + "nodes.add(left" + linkobjectvariable + "buttonbar);");
					sg.wl("			left" + linkobjectvariable + "nodes.add(left" + linkobjectvariable + "s);");
					ChoiceValue[] allowedtypes = this.typerestrictionforleft.getAllowedTypes();
					
					sg.wl("		mainband.addConditionalElements(this.getTypechoice(),");
					sg.wl("				new ChoiceValue[] { ");
					for (int t=0;t<allowedtypes.length;t++) {
						;
						sg.wl("					"+(t>0?",":"")+StringFormatter.formatForJavaClass(this.typed.getTypes().getName())+"ChoiceDefinition.get()."+allowedtypes[t].getName());
					}
					sg.wl("				}, left" + linkobjectvariable + "nodes.toArray(new SPageNode[0]));");


				} else {
					sg.wl("		" + locationname + ".addElement(left" + linkobjectvariable + "buttonbar);");
					sg.wl("		" + locationname + ".addElement(left" + linkobjectvariable + "s);");
			
				}
				} else {
				// -------------------------------------------------------------------------------------------------
				// show link as field array
			}

		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return parentproperty.linkfromleftpriority;
		}

	}
}
