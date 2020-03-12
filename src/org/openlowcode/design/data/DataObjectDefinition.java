/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openlowcode.design.access.ActionGroup;
import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.action.StaticActionDefinition;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.NodeTreeArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.data.formula.CalculatedFieldTriggerPath;
import org.openlowcode.design.data.formula.FormulaDefinitionElement;
import org.openlowcode.design.data.properties.basic.AutolinkObject;
import org.openlowcode.design.data.properties.basic.AutonumberingRule;
import org.openlowcode.design.data.properties.basic.ComplexWorkflow;
import org.openlowcode.design.data.properties.basic.ComputedDecimal;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameParent;
import org.openlowcode.design.data.properties.basic.DataControl;
import org.openlowcode.design.data.properties.basic.FileContent;
import org.openlowcode.design.data.properties.basic.HasAutolink;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Numbered;
import org.openlowcode.design.data.properties.basic.PrintOut;
import org.openlowcode.design.data.properties.basic.RightForLink;
import org.openlowcode.design.data.properties.basic.Schedule;
import org.openlowcode.design.data.properties.basic.SimpleTaskWorkflow;
import org.openlowcode.design.data.properties.basic.SubObject;
import org.openlowcode.design.data.properties.basic.TargetDate;
import org.openlowcode.design.data.properties.basic.TimeSlot;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.data.properties.basic.Versioned;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.DynamicPageDefinition;
import org.openlowcode.design.pages.PageDefinition;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.design.utility.MultiFieldConstraint;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.module.system.design.SystemModule;

/**
 * A Data Object Definition is the declaration of an object type in the
 * application. An object is persisted, and is made of a list of:
 * <ul>
 * <li>fields: a piece of information that is directly entered by users</li>
 * <li>properties: a feature of the object that is mostly accessed through
 * value-added methods (as opposed to simple read / update for fields). A
 * property is typically supported by stored information.</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinition
		extends
		Named {

	private String preferedspreadsheettabname = null;

	private Logger logger = Logger.getLogger("");
	private NamedList<Field> fieldlist;
	NamedList<Property<?>> propertylist;
	private NamedList<Property<?>> propertylistincludinglegacy;
	private NamedList<DisplayProfile> displayprofiles;
	private NamedList<MultiFieldConstraint> constraintsforobject;
	// private NamedList<DynamicActionDefinition> actionsonobjectid;
	private String label;
	private Module ownermodule;
	private boolean forcehideobject = false;
	private HashMap<String, String> loaderalias;
	private ArrayList<String> aliaslist;
	private HashMap<String, ChoiceValue[]> restrictionforalias;
	private DataObjectDefinition aliasfilteronparent = null;
	private ArrayList<StaticActionDefinition> actionsonsearchpage;
	private int forcedrowheight = 0;
	private DataObjectDefinitionSearchPagesAndActions searchpagesandactions;

	private NamedList<ObjectTab> objecttablist;
	private StandardActionGroup readactiongroup;
	private StandardActionGroup modifyactiongroup;
	private StandardActionGroup steeractiongroup;
	private StandardActionGroup scheduleactiongroup;
	private StandardActionGroup fullactiongroup;
	private StandardActionGroup lookupactiongroup;
	private StandardActionGroup createnewactiongroup;
	private StandardActionGroup dataadminactiongroup;
	private StandardActionGroup executivereadactiongroup;
	private StandardActionGroup businessadminactiongroup;

	/**
	 * if a prefered spreadsheet tab name is specified, the flat file loader will
	 * try to open a spreadsheet file on the tab with the provided name, so that the
	 * import works even if the file was closed with another tab displayed
	 * 
	 * @param preferedspreadsheettabname name of the prefered tab for data loading
	 */
	public void setPreferedSpreadSheetTabName(String preferedspreadsheettabname) {
		this.preferedspreadsheettabname = preferedspreadsheettabname;
	}

	private ChoiceCategory categoryforextractor = null;

	/**
	 * @return
	 */
	public ChoiceCategory getCategoryForExtractor() {
		return this.categoryforextractor;
	}

	/**
	 * @param category a valid choice category for the module
	 */
	public void setExtractorConditionChoice(ChoiceCategory category) {
		if (category.getParentModule() != this.ownermodule)
			throw new RuntimeException("category " + category.getName() + " is not defined for the module");
		this.categoryforextractor = category;
	}

	/**
	 * adds an alias in the loading file. The alias is the column name in the
	 * loading file, and it maps to the Open Lowcode syntax column name
	 * 
	 * @param alias        alias to be used in loading and export file
	 * @param fullpath     the full path, typically name of the field or property
	 *                     and parameters
	 * @param onlyforvalue a list of selected options for the loading for which the
	 *                     field is valid
	 */
	public void addLoaderAlias(String alias, String fullpath, ChoiceValue[] onlyforvalue) {
		addLoaderAlias(alias, fullpath);
		setOnlyValueRestriction(alias, onlyforvalue);
	}

	/**
	 * restricts a table alias to a list of values that can be imported
	 * 
	 * @param alias        alias for flat file loading
	 * @param onlyforvalue only those values are authorized
	 */
	private void setOnlyValueRestriction(String alias, ChoiceValue[] onlyforvalue) {
		if (categoryforextractor == null)
			throw new RuntimeException("for object " + this.getName() + " for alias " + alias
					+ ", condition choice needs to be defined to add alias with restriction");
		if (onlyforvalue == null)
			throw new RuntimeException("ChoiceValue onlyforvalue cannot be null");
		if (onlyforvalue.length == 0)
			throw new RuntimeException("onlyforvalue needs to have a lengyh of not ");
		for (int i = 0; i < onlyforvalue.length; i++)
			if (!categoryforextractor.isKeyPresent(onlyforvalue[i].getName()))
				throw new RuntimeException("Value " + onlyforvalue[i].getName()
						+ " is not present in category for extractor " + categoryforextractor.getName());
		restrictionforalias.put(alias, onlyforvalue);
	}

	/**
	 * allows to add an alias with condition on parent
	 * 
	 * @param parent
	 */
	public void setAliasConditionOnParent(DataObjectDefinition parent) {
		if (parent == null)
			throw new RuntimeException(
					"for object " + this.getName() + ", Parent needs to be defined to add alias with restriction");
		this.aliasfilteronparent = parent;
	}

	/**
	 * adds a flat file loader alias
	 * 
	 * @param alias    alias to be used in loading and export file
	 * @param fullpath the full path, typically name of the field or property and
	 *                 parameters
	 */
	public void addLoaderAlias(String alias, String fullpath) {

		this.loaderalias.put(alias, fullpath);
		this.aliaslist.add(alias);
	}

	public DataObjectDefinitionSearchPagesAndActions getSearchPagesAndActions() {
		return searchpagesandactions;
	}

	private class StandardActionGroup
			implements
			ActionGroup {
		private String name;
		private ArrayList<ActionDefinition> actions;

		public StandardActionGroup(String name) {
			this.actions = new ArrayList<ActionDefinition>();
			this.name = name;
		}

		@Override
		public ActionDefinition[] getActionsInGroup() {
			return actions.toArray(new ActionDefinition[0]);
		}

		public void addAction(ActionDefinition action) {
			actions.add(action);
		}

		@Override
		public String toString() {
			return "(" + this.name + "-" + actions.size() + "actions)";
		}

		@Override
		public String getName() {
			return this.name;
		}

	}

	/**
	 * get the lookup action group for this data object to grant privileges to an
	 * authority for this action group
	 * 
	 * @return the lookup action group
	 */
	public StandardActionGroup getLookupActionGroup() {
		return this.lookupactiongroup;
	}

	/**
	 * adds an action to the lookup action group
	 * 
	 * @param action action to add to the lookup action group
	 */
	public void addActionToLookupActionGroup(ActionDefinition action) {
		lookupactiongroup.addAction(action);
	}

	/**
	 * @return true of the data object has loading aliases
	 */
	public boolean hasAlias() {
		if (this.aliaslist.size() > 0)
			return true;
		return false;
	}

	/**
	 * adds an action to the schedule action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToScheduleActionGroup(ActionDefinition action) {
		scheduleactiongroup.addAction(action);
	}

	/**
	 * adds an action to the steer action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToSteerActionGroup(ActionDefinition action) {
		steeractiongroup.addAction(action);
	}

	/**
	 * adds an action to the read action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToReadActionGroup(ActionDefinition action) {
		readactiongroup.addAction(action);
	}

	/**
	 * adds an action to the modify action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToModifyGroup(ActionDefinition action) {
		modifyactiongroup.addAction(action);
	}

	/**
	 * adds an action to the create new action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToCreateNewGroup(ActionDefinition action) {
		createnewactiongroup.addAction(action);
	}

	/**
	 * adds an action to the executive read action group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToExecutiveReadGroup(ActionDefinition action) {
		executivereadactiongroup.addAction(action);
	}

	/**
	 * adds an action to the business admin group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToBusinessAdminGroup(ActionDefinition action) {
		businessadminactiongroup.addAction(action);
	}

	/**
	 * adds an action to the data admin group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToDataAdminActionGroup(ActionDefinition action) {
		dataadminactiongroup.addAction(action);
	}

	/**
	 * adds an action to the full group for the data object
	 * 
	 * @param action action to add to the action group
	 */
	public void addActionToFullGroup(ActionDefinition action) {
		fullactiongroup.addAction(action);
	}

	/**
	 * get the data admin action group for this data object to grant privileges to
	 * an authority for this action group
	 * 
	 * @return the data admin action group
	 */
	public ActionGroup getDataAdminActionGroup() {
		return dataadminactiongroup;
	}

	/**
	 * get the read action group for this data object to grant privileges to an
	 * authority for this action group
	 * 
	 * @return the read action group
	 */
	public ActionGroup getReadActionGroup() {
		return readactiongroup;
	}

	/**
	 * get the modify action group for this data object to grant privileges to an
	 * authority for this action group
	 * 
	 * @return the modify action group
	 */
	public ActionGroup getModifyActionGroup() {
		return modifyactiongroup;
	}

	/**
	 * get the steer action group for this data object to grant privileges to an
	 * authority for this action group
	 * 
	 * @return the steer action group
	 */
	public ActionGroup getSteerActionGroup() {
		return steeractiongroup;
	}

	/**
	 * get the create new action group for this data object to grant privileges to
	 * an authority for this action group
	 * 
	 * @return the create new action group
	 */
	public ActionGroup getCreateNewActionGroup() {
		return this.createnewactiongroup;
	}

	/**
	 * get the schedule action group for this data object to grant privileges to an
	 * authority for this action group
	 * 
	 * @return the schedule action group
	 */
	public ActionGroup getScheduleActionGroup() {
		return this.scheduleactiongroup;
	}

	/**
	 * get the business administration action group for this data object to grant
	 * privileges to an authority for this action group
	 * 
	 * @return the business administration action group
	 */
	public ActionGroup getBusinessAdminActionGroup() {
		return this.businessadminactiongroup;
	}

	/**
	 * get the executive read action group for this data object to grant privileges
	 * to an authority for this action group
	 * 
	 * @return the executive read action group
	 */
	public ActionGroup getExecutiveReadActionGroup() {
		return this.executivereadactiongroup;
	}

	/**
	 * @return the plain language label of the data object
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * forces all tables in the user interface that show this object to have a row
	 * height as specified (number of lines of text shown per row)
	 * 
	 * @param rowheightinline number of lines of text shown per row
	 */
	public void setForcedRowHeightForTable(int rowheightinline) {
		this.forcedrowheight = rowheightinline;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof DataObjectDefinition))
			return false;
		DataObjectDefinition other = (DataObjectDefinition) obj;
		String thisid = this.getOwnermodule().getName() + "/" + this.getName();
		String otherid = other.getOwnermodule().getName() + "/" + other.getName();
		return (thisid.equals(otherid));
	}

	private boolean hidescurve = false;

	/**
	 * Allows to hide the SCurve component in the start page of the object if it
	 * does not make sense for the object.
	 */
	public void hideSCurve() {
		this.hidescurve = true;
	}

	/**
	 * @return true if the S-Curve shound not be hidden
	 */
	public boolean isSCurveHidden() {
		return this.hidescurve;
	}

	/**
	 * @return get the data object for which there are alias filters
	 */
	public DataObjectDefinition getAliasFilterOnParent() {
		return this.aliasfilteronparent;
	}

	/**
	 * get the field by name
	 * 
	 * @param name name of the field (java name)
	 * @return the field if it exists
	 */
	public Field lookupFieldByName(String name) {
		return fieldlist.lookupOnName(name);
	}

	/**
	 * @return the parent module of this data object
	 */
	public Module getOwnermodule() {
		return this.ownermodule;
	}

	/**
	 * adds a display profile to this data object
	 * 
	 * @param displayprofile display profile to add
	 */
	public void setDisplayProfile(DisplayProfile displayprofile) {
		this.displayprofiles.add(displayprofile);
	}

	/**
	 * @return the number of properties of the object
	 */
	public int getPropertySize() {
		return propertylist.getSize();
	}

	/**
	 * get the property at the given index
	 * 
	 * @param index a number between 0 (included) and getPropertySize (excluded)
	 * @return the property at the given index
	 */
	public Property<?> getPropertyAt(int index) {
		return propertylist.get(index);
	}

	/**
	 * checks if the following field belongs to the object
	 * 
	 * @param field field
	 * @return true if the field belongs to the object
	 */
	public boolean isFieldInObject(Field field) {
		for (int i = 0; i < fieldlist.getSize(); i++)
			if (fieldlist.get(i) == field)
				return true;
		return false;
	}

	/**
	 * Create a new Data Object Definition
	 * 
	 * @param name            java name of the data object definition (less than 16
	 *                        characters recommended)
	 * @param label           default language plain label of the data object for
	 *                        display in the user interface
	 * @param ownermodule     parent module of the data object
	 * @param forcehideobject hide the object (does not generate a search page in
	 *                        the module menu
	 */
	public DataObjectDefinition(String name, String label, Module ownermodule, boolean forcehideobject) {
		this(name, label, ownermodule);
		this.forcehideobject = forcehideobject;
	}

	/**
	 * Create a new Data Object Definition
	 * 
	 * @param name        java name of the data object definition (less than 16
	 *                    characters recommended)
	 * @param label       default language plain label of the data object for
	 *                    display in the user interface
	 * @param ownermodule parent module of the data object
	 */
	public DataObjectDefinition(String name, String label, Module ownermodule) {
		super(name);
		this.label = label;
		fieldlist = new NamedList<Field>();
		propertylist = new NamedList<Property<?>>();
		propertylistincludinglegacy = new NamedList<Property<?>>();
		displayprofiles = new NamedList<DisplayProfile>();
		constraintsforobject = new NamedList<MultiFieldConstraint>();
		// actionsonobjectid = new NamedList<DynamicActionDefinition>();
		this.ownermodule = ownermodule;
		ownermodule.addObject(this);
		readactiongroup = new StandardActionGroup("Read");
		modifyactiongroup = new StandardActionGroup("Modify");
		fullactiongroup = new StandardActionGroup("Full");
		steeractiongroup = new StandardActionGroup("Steer");
		scheduleactiongroup = new StandardActionGroup("Schedule");
		lookupactiongroup = new StandardActionGroup("Lookup");
		createnewactiongroup = new StandardActionGroup("CreateNew");
		dataadminactiongroup = new StandardActionGroup("DataAdmin");
		executivereadactiongroup = new StandardActionGroup("ExecutiveRead");
		businessadminactiongroup = new StandardActionGroup("BusinessAdmin");
		objecttablist = new NamedList<ObjectTab>();
		searchpagesandactions = new DataObjectDefinitionSearchPagesAndActions(this);
		this.loaderalias = new HashMap<String, String>();
		this.aliaslist = new ArrayList<String>();
		this.restrictionforalias = new HashMap<String, ChoiceValue[]>();

		this.actionsonsearchpage = new ArrayList<StaticActionDefinition>();

	}

	/**
	 * This method should only be used from the ObjectTab constructor
	 * 
	 * @param objecttab tab to add
	 */
	void setObjectTab(ObjectTab objecttab) {
		objecttablist.add(objecttab);
	}

	/**
	 * Provides the list of additional object tabs in addition to the object details
	 * tab. Widgets can be assigned to an object tab, they can also be left on the
	 * default tab. The default tab will be named "Details" in case there are other
	 * tabs.
	 * 
	 * @return
	 */
	public ObjectTab[] getExtraTabs() {
		return objecttablist.getFullList().toArray(new ObjectTab[0]);
	}

	/**
	 * @return true if the data object has the TargetDate property
	 */
	public boolean hasTargetDate() {
		for (int i = 0; i < this.getPropertySize(); i++)
			if (this.getPropertyAt(i) instanceof TargetDate)
				return true;
		return false;
	}

	/**
	 * @return the linked to parent property if this object is a subobject
	 */
	public LinkedToParent<?> isSubObject() {
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) property;
				for (int j = 0; j < linkedtoparent.getBusinessRuleNumber(); j++) {
					PropertyBusinessRule<?> thisbusinessrule = linkedtoparent.getBusinessRule(j);
					if (thisbusinessrule instanceof SubObject) {
						return linkedtoparent;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @return the subobject business rule of the object
	 */
	public SubObject getSubObject() {
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) property;
				for (int j = 0; j < linkedtoparent.getBusinessRuleNumber(); j++) {
					PropertyBusinessRule<?> thisbusinessrule = linkedtoparent.getBusinessRule(j);
					if (thisbusinessrule instanceof SubObject) {
						SubObject subobject = (SubObject) thisbusinessrule;
						return subobject;
					}
				}
			}
		}
		return null;
	}

	/**
	 * @return true if the search for this object should be shown in the module menu
	 *         in the application
	 */
	public boolean showSearchInMenu() {
		if (this.forcehideobject)
			return false;
		// do not show search in menu if object is not stored
		if (!this.isStoredobject())
			return false;
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.getPropertyAt(i);

			if (thisproperty instanceof LinkObject) {

				if (this.IsIterated())
					return true;
				return false;
			}
			if (thisproperty instanceof AutolinkObject)
				return false;
			if (thisproperty instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) thisproperty;
				for (int j = 0; j < linkedtoparent.getBusinessRuleNumber(); j++) {
					PropertyBusinessRule<?> thisbusinessrule = linkedtoparent.getBusinessRule(j);
					if (thisbusinessrule instanceof SubObject)
						return false;
				}
			}
		}
		return true;
	}

	/**
	 * adds a multi-field constraint to this data object
	 * 
	 * @param thisconstraint constraint to add
	 */
	public void addMultiFieldConstraint(MultiFieldConstraint thisconstraint) {
		constraintsforobject.add(thisconstraint);

	}

	/**
	 * Shortcut method performing the following
	 * <ul>
	 * <li>Adding action to the module</li>
	 * <li>adding action on the unique identified property of the data object,
	 * making it appear in the object page</li>
	 * </ul>
	 * This method will fail if a unique identified property has not been added at
	 * the point it is used.
	 * 
	 * @param actiontoadd a dynamic action with as single entry argument the object
	 *                    id of the Data Object.
	 */
	public void addActionOnObjectPage(DynamicActionDefinition actiontoadd) {
		this.getOwnermodule().addAction(actiontoadd);
		Property<?> property = this.getPropertyByName("UNIQUEIDENTIFIED");
		if (property == null)
			throw new RuntimeException("UniqueIdentified property not found on object " + this.getName());
		if (!(property instanceof UniqueIdentified))
			throw new RuntimeException("Property with name 'UniqueIdentified' is of incorrect class");
		UniqueIdentified uniqueidentified = (UniqueIdentified) property;
		uniqueidentified.addActionOnObjectId(actiontoadd);
	}

	/**
	 * adds a static action on the search page of the object
	 * 
	 * @param actiontoadd action to add
	 */
	public void addActionOnSearchPage(StaticActionDefinition actiontoadd) {
		this.actionsonsearchpage.add(actiontoadd);
	}

	/**
	 * @return the number of actions on search page
	 */
	public int getActionOnSearchPageNumber() {
		return this.actionsonsearchpage.size();
	}

	/**
	 * the action on search page at the given index
	 * 
	 * @param index a number between 0 (included) and getActionOnSearchPageNumber
	 *              (excluded)
	 * @return the action on search page at the following index
	 */
	public StaticActionDefinition getActionOnSeachPage(int index) {
		return this.actionsonsearchpage.get(index);
	}

	/**
	 * adds a field to the data object
	 * 
	 * @param field field to add
	 */
	public void addField(Field field) {
		fieldlist.add(field);
		field.setDataObjectDefinition(this);
	}

	/**
	 * adds a field to the data object with a search widget (not activated, see
	 * github issue #24)
	 * 
	 * @param field        field to add
	 * @param searchwidget search widget to add
	 */
	public void addFieldasSearchElement(Field field, SearchWidgetDefinition searchwidget) {
		fieldlist.add(field);
	}

	private void checkCallForAddProperty() {
		@SuppressWarnings("rawtypes")
		Class propertyclass = Property.class;
		@SuppressWarnings("rawtypes")
		Class moduleclass = Module.class;
		logger.finest("   ---------***--- making check on class ---***--------------");
		for (int i = 0; i < Thread.currentThread().getStackTrace().length; i++) {
			StackTraceElement currentstacktrace = Thread.currentThread().getStackTrace()[i];
			logger.finest("		***---*** analyzing stack trace " + currentstacktrace.toString());
			String methodname = currentstacktrace.getMethodName();
			boolean exception = false;
			if (methodname.compareTo("setFinalSettings") == 0)
				exception = true;
			@SuppressWarnings("rawtypes")
			Class classstack;
			try {
				classstack = Class.forName(currentstacktrace.getClassName());

				while (classstack != null) {
					logger.finest("			*** class hierarchy " + classstack.getName());
					// found first call from module, this is OK
					if (classstack.getName().compareTo(moduleclass.getName()) == 0)
						return;
					if (classstack.getName().compareTo(propertyclass.getName()) == 0)
						if (!exception)
							throw new RuntimeException(
									"Called add Property from property. This is illegal and you should use addExternalObjectProperty or in finalize settings. Location: "
											+ currentstacktrace);
					classstack = classstack.getSuperclass();
				}
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Deep technical error " + e.getClass() + ":" + e.getMessage());
			}

		}
	}

	/**
	 * a convenience method to add several properties to the data object definition
	 * 
	 * @param properties properties
	 */
	public void addProperties(Property<?>...properties) {
		if (properties!=null) for (int i=0;i<properties.length;i++) this.addProperty(properties[i]);
	}
	
	/**
	 * adds a new property to the data object
	 * 
	 * @param property property to add
	 */
	public void addProperty(Property<?> property) {
		// control to check this is not called from a property, as this may cause
		// regression
		checkCallForAddProperty();
		property.setParent(this);
		propertylist.add(property);
		propertylistincludinglegacy.add(property);

		for (int i = 0; i < property.getDisplayProfileForPropertyNumber(); i++) {
			this.displayprofiles.add(property.getDisplayProfileForProperty(i));
		}
		for (int i = 0; i < property.getExternalObjectPropertySize(); i++) {
			DataObjectDefinition externalobject = property.getExternalObject(i);
			Property<?> externalproperty = property.getExternalObjectProperty(i);
			externalobject.addProperty(externalproperty);

		}
	}

	/**
	 * adds a new property to the data object, removing it from the GUI, but still
	 * keeping the data accessible for a migrator
	 * 
	 * @param property property to add
	 */
	public void addPropertyAsLegacy(Property<?> property) {
		checkCallForAddProperty();
		property.setParent(this);
		propertylistincludinglegacy.add(property);
		for (int i = 0; i < property.getExternalObjectPropertySize(); i++) {
			DataObjectDefinition externalobject = property.getExternalObject(i);
			Property<?> externalproperty = property.getExternalObjectProperty(i);
			externalobject.addPropertyAsLegacy(externalproperty);

		}
		property.setAsLegacy();
	}

	/**
	 * gets the property by name of property
	 * 
	 * @param name java name of the property
	 * @return the property if it exists, or null
	 */
	public Property<?> getPropertyByName(String name) {
		return propertylist.lookupOnName(name);
	}

	/**
	 * @return the interfaces (input argument) of the automatically generated update
	 *         page
	 */
	private PageDefinition generateUpdatePage() {
		String updatepagename = "UPDATE" + this.getName();
		DynamicPageDefinition updatepage = new DynamicPageDefinition(updatepagename);
		updatepage.addInputParameter(new ObjectArgument(this.getName(), this));
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property.isDataInputUsedForUpdate()) {
				for (int j = 0; j < property.getDataInputSize(); j++) {

					updatepage.addInputParameter(property.getDataInputForCreation(j));

				}
			}
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property instanceof DataControl) {
				updatepage.addInputParameter(new StringArgument("CONTROLSTATUS", 20000));
			}
		}
		return updatepage;
	}

	/**
	 * @return the interfaces (input argument) of the automatically generated show
	 *         page
	 */
	private PageDefinition generateShowPage() {
		String showpagename = "SHOW" + this.getName();
		DynamicPageDefinition showpage = new DynamicPageDefinition(showpagename);
		showpage.addInputParameter(new ObjectArgument(this.getName(), this));
		showpage.addInputParameter(
				new ChoiceArgument("USERLOCALE", SystemModule.getSystemModule().getApplicationLocale()));
		showpage.addInputParameter(
				new ChoiceArgument("PREFENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if ((thisproperty instanceof SimpleTaskWorkflow) || (thisproperty instanceof ComplexWorkflow)) {
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument("YOURACTIVETASKS", SystemModule.getSystemModule().getTask())));
				showpage.addInputParameter(
						new ArrayArgument(new ObjectArgument("ALLTASKS", SystemModule.getSystemModule().getTask())));

			}
		}

		// lifecycle if exists
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof Lifecycle) {
				Lifecycle lifecycle = (Lifecycle) thisproperty;
				showpage.addInputParameter(
						new ChoiceArgument("POTENTIALSTATES", lifecycle.getTransitionChoiceCategory()));
				if (lifecycle.getUnreleasedWarning() != null)
					showpage.addInputParameter(new StringArgument("UNRELEASEDWARNING", 256));
			}
		}
		// timeslot
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof TimeSlot) {
				showpage.addInputParameter(new TimestampArgument("STARTTIME"));
				showpage.addInputParameter(new TimestampArgument("ENDTIME"));

			}
		}
		// schedule
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof Schedule) {
				if (this.hasNumbered())
					if (!this.isAutoNumbered())
						showpage.addInputParameter(new StringArgument("INSERTAFTERNR", 64));
				if (this.hasNamed())
					showpage.addInputParameter(new StringArgument("INSERTAFTERNAME", 64));

				showpage.addInputParameter(new ObjectArgument("BLANKFORINSERTAFTER", this));
				showpage.addInputParameter(new TimestampArgument("INSERTAFTERSTART"));
				showpage.addInputParameter(new TimestampArgument("INSERTAFTEREND"));

			}
		}

		// image
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				showpage.addInputParameter(new LargeBinaryArgument(imagecontent.getInstancename() + "TBN", false));
				showpage.addInputParameter(new ObjectIdArgument(imagecontent.getInstancename() + "FULLIMGID",
						SystemModule.getSystemModule().getBinaryFile()));
			}

		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof FileContent) {
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument("ATTACHMENTS", SystemModule.getSystemModule().getObjectAttachment())));
			}
		}
		if (this.hasNumbered())
			showpage.addInputParameter(new StringArgument("NUMBERFORRENUMBER", 64));
		// adding special displays for properties step 3 - links
		// arrays
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);

			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument(thislinkedfromchildren.getName(), thislinkedfromchildren.getChildObject())));
			}
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) thisproperty;
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument(thisleftforlink.getName(), thisleftforlink.getLinkObjectDefinition())));
				showpage.addInputParameter(new ObjectArgument(thisleftforlink.getName() + "BLANKFORADD",
						thisleftforlink.getLinkObjectDefinition()));
			}
			if (thisproperty instanceof RightForLink) {
				RightForLink<?, ?> thisrightforlink = (RightForLink<?, ?>) thisproperty;
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument(thisrightforlink.getName(), thisrightforlink.getLinkObjectDefinition())));
				showpage.addInputParameter(new ObjectArgument(thisrightforlink.getName() + "BLANKFORADD",
						thisrightforlink.getLinkObjectDefinition()));

			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> thisautolink = (HasAutolink<?>) thisproperty;
				// autolink as left
				if (thisautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					// symetric: nodetree is not supported
					showpage.addInputParameter(new ArrayArgument(new ObjectArgument("LEFT" + thisautolink.getName(),
							thisautolink.getLinkObjectDefinition())));
				} else {
					// not symetric: we can display nodetree
					if (thisautolink.getLinkObjectProperty().isShowLinkTree()) {
						showpage.addInputParameter(new NodeTreeArgument(new ObjectArgument(
								"LEFT" + thisautolink.getName(), thisautolink.getLinkObjectDefinition())));
					} else {
						showpage.addInputParameter(new ArrayArgument(new ObjectArgument("LEFT" + thisautolink.getName(),
								thisautolink.getLinkObjectDefinition())));

					}

				}
				showpage.addInputParameter(new ObjectArgument(thisautolink.getName() + "BLANKFORADD",
						thisautolink.getLinkObjectDefinition()));
				// autolink as right
				showpage.addInputParameter(new ArrayArgument(
						new ObjectArgument("RIGHT" + thisautolink.getName(), thisautolink.getLinkObjectDefinition())));
			}

		}

		return showpage;
	}

	private DynamicActionDefinition generatePrepareUpdateAction() {
		String prepareupdateactionname = "PREPAREUPDATE" + this.getName();
		DynamicActionDefinition prepareupdateaction = new DynamicActionDefinition(prepareupdateactionname, true);
		prepareupdateaction.setButtonlabel("Update");
		prepareupdateaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		prepareupdateaction.addOutputArgument(new ObjectArgument(this.getName(), this));
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property.isDataInputUsedForUpdate()) {
				for (int j = 0; j < property.getDataInputSize(); j++) {
					prepareupdateaction.addOutputArgument(property.getDataInputForCreation(j));
				}
			}
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property instanceof DataControl) {
				prepareupdateaction.addOutputArgument(new StringArgument("CONTROLSTATUS", 20000));
			}
		}
		this.addActionToModifyGroup(prepareupdateaction);
		return prepareupdateaction;
	}

	private ActionDefinition generateDeleteAction() {
		String deleteactionname = "DELETE" + this.getName();
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction.addInputArgument(new ObjectIdArgument(this.getName() + "ID", this));
		this.addActionToDataAdminActionGroup(deleteaction);
		return deleteaction;
	}

	private ActionDefinition generateShowHistoryAction() {
		String showhistoryname = "SHOWHISTORYFOR" + this.getName();
		DynamicActionDefinition showhistory = new DynamicActionDefinition(showhistoryname, true);
		showhistory.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName() + "ID", this));
		if (this.IsIterated())
			showhistory.addOutputArgument(new ArrayArgument(new ObjectArgument(this.getName() + "ITERATIONS", this)));
		if (this.isVersioned())
			showhistory.addOutputArgument(new ArrayArgument(new ObjectArgument(this.getName() + "VERSIONS", this)));

		showhistory.addOutputArgument(new ObjectIdArgument(this.getName() + "IDTHRU", this));
		this.addActionToReadActionGroup(showhistory);
		return showhistory;
	}

	private PageDefinition generateShowHistoryPage() {
		String showalliterationspagename = "SHOWHISTORYFOR" + this.getName();
		DynamicPageDefinition showalliterationspage = new DynamicPageDefinition(showalliterationspagename);
		if (this.IsIterated())
			showalliterationspage
					.addInputParameter(new ArrayArgument(new ObjectArgument(this.getName() + "ITERATIONS", this)));
		if (this.isVersioned())
			showalliterationspage
					.addInputParameter(new ArrayArgument(new ObjectArgument(this.getName() + "VERSIONS", this)));
		showalliterationspage.addInputParameter(new ObjectIdArgument(this.getName() + "ID", this));
		return showalliterationspage;
	}

	private ActionDefinition generateDeleteActionAndShowParent(LinkedToParent<?> relevantlinkedtoparent) {
		// security done
		String deleteactionname = "DELETE" + this.getName() + "ANDSHOWPARENT";
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName() + "ID", this));
		deleteaction.addOutputArgument(
				new ObjectIdArgument("PARENT" + relevantlinkedtoparent.getParentObjectForLink().getName() + "ID",
						relevantlinkedtoparent.getParentObjectForLink()));
		this.addActionToSteerActionGroup(deleteaction);
		return deleteaction;
	}

	private ActionDefinition generateMassiveDeleteActionAndShowParent(LinkedToParent<?> relevantlinkedtoparent) {
		String deleteactionname = "MASSIVEDELETE" + this.getName() + "ANDSHOWPARENT"
				+ relevantlinkedtoparent.getInstancename();
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction
				.addInputArgumentAsAccessCriteria(new ArrayArgument(new ObjectIdArgument(this.getName() + "ID", this)));
		deleteaction.addInputArgument(
				new ObjectIdArgument("PARENT" + relevantlinkedtoparent.getParentObjectForLink().getName() + "ID",
						relevantlinkedtoparent.getParentObjectForLink()));

		deleteaction.addOutputArgument(
				new ObjectIdArgument("PARENT" + relevantlinkedtoparent.getParentObjectForLink().getName() + "ID_THRU",
						relevantlinkedtoparent.getParentObjectForLink()));
		this.addActionToSteerActionGroup(deleteaction);
		return deleteaction;
	}

	private ActionDefinition generateDeleteLinkAndShowLeft() {
		String deleteactionname = "DELETE" + this.getName() + "ANDSHOWLEFT";
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) this.getPropertyByName("LINKOBJECT");
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction.addInputArgumentAsAccessCriteria(
				new ObjectIdArgument("LEFT" + linkobject.getLeftobjectforlink().getName() + "ID",
						linkobject.getLeftobjectforlink()));
		deleteaction.addInputArgument(new ObjectIdArgument(this.getName() + "ID", this));
		
		deleteaction.addOutputArgument(new ObjectIdArgument(
				"PARENT" + linkobject.getLeftobjectforlink().getName() + "ID", linkobject.getLeftobjectforlink()));
		linkobject.getLeftobjectforlink().addActionToModifyGroup(deleteaction);
		this.addActionToCreateNewGroup(deleteaction);
		return deleteaction;
	}

	private ActionDefinition generateDeleteLinkAndShowRight() {
		String deleteactionname = "DELETE" + this.getName() + "ANDSHOWRIGHT";
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction.addInputArgument(new ObjectIdArgument(this.getName() + "ID", this));
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) this.getPropertyByName("LINKOBJECT");
		deleteaction.addOutputArgument(new ObjectIdArgument(
				"PARENT" + linkobject.getRightobjectforlink().getName() + "ID", linkobject.getRightobjectforlink()));
		this.addActionToCreateNewGroup(deleteaction);
		return deleteaction;
	}

	private ActionDefinition generateDeleteAutolinkAndShowObject() {
		// security done
		String deleteactionname = "DELETEAUTOLINK" + this.getName() + "ANDSHOWOBJECT";
		DynamicActionDefinition deleteaction = new DynamicActionDefinition(deleteactionname, true);
		deleteaction.addInputArgument(new ObjectIdArgument(this.getName() + "ID", this));
		AutolinkObject<?> autolink = (AutolinkObject<?>) this.getPropertyByName("AUTOLINKOBJECT");
		deleteaction.addInputArgumentAsAccessCriteria(
				new ObjectIdArgument(autolink.getObjectforlink().getName() + "IDTOSHOW", autolink.getObjectforlink()));
		deleteaction.addOutputArgument(new ObjectIdArgument(autolink.getObjectforlink().getName() + "IDTOSHOWTHRU",
				autolink.getObjectforlink()));
		autolink.getObjectforlink().addActionToModifyGroup(deleteaction);

		return deleteaction;
	}

	private DynamicActionDefinition generatePrintOutPreview(PrintOut printoutproperty) {
		String previewactionname = "PREVIEWPRINTOUTFOR" + this.getName().toUpperCase() + "FOR"
				+ printoutproperty.getInstancename().toUpperCase();
		DynamicActionDefinition previewaction = new DynamicActionDefinition(previewactionname, true);
		previewaction.addInputArgument(new ObjectIdArgument(this.getName() + "ID", this));
		previewaction.addOutputArgument(new LargeBinaryArgument("PREVIEW", false));
		this.addActionToReadActionGroup(previewaction);
		return previewaction;
	}

	private ActionDefinition generateFlatFileLoader() {
		// security done, nothing to do
		String flatfileloaderactionname = "FLATFILELOADERFOR" + this.getName();
		DynamicActionDefinition flatfileloaderaction = new DynamicActionDefinition(flatfileloaderactionname, true);
		flatfileloaderaction
				.addInputArgument(new ChoiceArgument("LOCALE", SystemModule.getSystemModule().getApplicationLocale()));
		flatfileloaderaction.addInputArgument(
				new ChoiceArgument("ENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));
		flatfileloaderaction.addInputArgument(new LargeBinaryArgument("FLATFILE", false));
		flatfileloaderaction.addOutputArgument(new StringArgument("LOADINGCONTEXT", 500));
		flatfileloaderaction.addOutputArgument(new IntegerArgument("INSERTED"));
		flatfileloaderaction.addOutputArgument(new IntegerArgument("UPDATED"));
		flatfileloaderaction.addOutputArgument(new IntegerArgument("ERRORS"));
		flatfileloaderaction.addOutputArgument(new IntegerArgument("POSTPROCERRORS"));

		flatfileloaderaction.addOutputArgument(new IntegerArgument("LOADINGTIME"));
		flatfileloaderaction.addOutputArgument(new ArrayArgument(
				new ObjectArgument("ERRORDETAIL", SystemModule.getSystemModule().getCSVLoaderError())));
		this.addActionToDataAdminActionGroup(flatfileloaderaction);

		return flatfileloaderaction;
	}

	private ActionDefinition generateFlatFileSample() {
		// security done, nothing to do
		String generateflatfilesampleactionname = "GENERATEFLATFILESAMPLEFOR" + this.getName();
		DynamicActionDefinition generateflatfilesampleaction = new DynamicActionDefinition(
				generateflatfilesampleactionname, true);
		generateflatfilesampleaction.addOutputArgument(new LargeBinaryArgument("SAMPLEFILE", false));
		this.addActionToDataAdminActionGroup(generateflatfilesampleaction);

		return generateflatfilesampleaction;
	}

	private ActionDefinition generateUpdateAction() {
		// security done
		String updateactionname = "UPDATE" + this.getName();
		DynamicActionDefinition updateaction = new DynamicActionDefinition(updateactionname, true);

		updateaction.addInputArgumentAsAccessCriteria(new ObjectArgument(this.getName(), this));
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property.isDataInputUsedForUpdate()) {
				for (int j = 0; j < property.getDataInputSize(); j++) {
					updateaction.addInputArgument(property.getDataInputForCreation(j));
				}
			}
		}
		updateaction.addOutputArgument(new ObjectIdArgument("ID", this));
		this.addActionToModifyGroup(updateaction);
		return updateaction;

	}

	private StaticActionDefinition generateSimpleWorkflowCockpitAction() {
		StaticActionDefinition workflowcockpit = new StaticActionDefinition(this.getName() + "WORKFLOWCOCKPIT", true);
		workflowcockpit.addOutputArgument(new ArrayArgument(new ObjectArgument(this.getName() + "WORKFLOW",
				((SimpleTaskWorkflow) (this.getPropertyByName("SIMPLETASKWORKFLOW"))).getWorkflowReportObject())));
		workflowcockpit.setButtonlabel(this.getLabel() + " cockpit");
		this.addActionToExecutiveReadGroup(workflowcockpit);
		return workflowcockpit;
	}

	private DynamicActionDefinition generateTaskReassignAction() {
		DynamicActionDefinition admintaskreassign = new DynamicActionDefinition(this.getName() + "ADMINREASSIGN", true);
		admintaskreassign.addInputArgument(new ObjectIdArgument("TASKID", SystemModule.getSystemModule().getTask()));
		admintaskreassign.addInputArgument(new StringArgument("ADMINCOMMENT", 64));
		admintaskreassign
				.addInputArgument(new ObjectIdArgument("NEWUSERID", SystemModule.getSystemModule().getAppuser()));
		this.addActionToBusinessAdminGroup(admintaskreassign);
		return admintaskreassign;
	}

	private DynamicPageDefinition generateSimpleWorkflowCockpitPage() {
		DynamicPageDefinition workflowcockpitpage = new DynamicPageDefinition(this.getName() + "WORKFLOWCOCKPIT");
		workflowcockpitpage.addInputParameter(new ArrayArgument(new ObjectArgument(this.getName() + "WORKFLOW",
				((SimpleTaskWorkflow) (this.getPropertyByName("SIMPLETASKWORKFLOW"))).getWorkflowReportObject())));

		return workflowcockpitpage;
	}

	private ActionDefinition generateMassUpdateAction() {
		// security done
		String massupdateactionname = "MASSUPDATE" + this.getName();
		DynamicActionDefinition massupdateaction = new DynamicActionDefinition(massupdateactionname, true);

		massupdateaction.addInputArgumentAsAccessCriteria(new ArrayArgument(new ObjectArgument(this.getName(), this)));
		if (this.IsIterated())
			massupdateaction.addInputArgument(new StringArgument("UPDATENOTE", 200));
		massupdateaction.addOutputArgument(new ArrayArgument(new ObjectArgument("UPDATED" + this.getName(), this)));
		this.addActionToModifyGroup(massupdateaction);
		return massupdateaction;

	}

	private ActionDefinition generateMassUpdateAndShowLeftAction(LinkObject<?, ?> linkobjecttyped) {
		String massupdateactionname = "MASSUPDATE" + this.getName() + "ANDSHOWLEFT";
		DynamicActionDefinition massupdateaction = new DynamicActionDefinition(massupdateactionname, true);

		massupdateaction.addInputArgumentAsAccessCriteria(new ArrayArgument(new ObjectArgument(this.getName(), this)));
		if (this.IsIterated())
			massupdateaction.addInputArgument(new StringArgument("UPDATENOTE", 200));

		massupdateaction.addInputArgument(new ObjectIdArgument(linkobjecttyped.getLeftobjectforlink().getName() + "ID",
				linkobjecttyped.getLeftobjectforlink()));
		massupdateaction.addOutputArgument(new ObjectIdArgument(
				linkobjecttyped.getLeftobjectforlink().getName() + "IDTHRU", linkobjecttyped.getLeftobjectforlink()));
		this.addActionToModifyGroup(massupdateaction);
		return massupdateaction;
	}

	private ActionDefinition generateMassUpdateAndShowParentAction(LinkedToParent<?> thislinkedtoparent) {

		String massupdateactionname = "MASSUPDATE" + this.getName() + "ANDSHOWPARENT"
				+ thislinkedtoparent.getInstancename().toUpperCase();
		DynamicActionDefinition massupdateaction = new DynamicActionDefinition(massupdateactionname, true);

		massupdateaction.addInputArgumentAsAccessCriteria(new ArrayArgument(new ObjectArgument(this.getName(), this)));
		if (this.IsIterated())
			massupdateaction.addInputArgument(new StringArgument("UPDATENOTE", 200));

		massupdateaction
				.addInputArgument(new ObjectIdArgument(thislinkedtoparent.getParentObjectForLink().getName() + "ID",
						thislinkedtoparent.getParentObjectForLink()));
		massupdateaction.addOutputArgument(
				new ObjectIdArgument(thislinkedtoparent.getParentObjectForLink().getName() + "IDTHRU",
						thislinkedtoparent.getParentObjectForLink()));
		this.addActionToModifyGroup(massupdateaction);
		return massupdateaction;
	}

	private ActionDefinition generateCreateLinkActionAndShowRight(LinkObject<?, ?> linkobjectproperty) {

		String createlinkactionname = "CREATE" + this.getName() + "ANDSHOWRIGHT"
				+ linkobjectproperty.getRightobjectforlink().getName().toUpperCase();
		DynamicActionDefinition createlinkaction = new DynamicActionDefinition(createlinkactionname, true);
		createlinkaction.addInputArgumentAsAccessCriteria(new ArrayArgument(
				new ObjectIdArgument("LEFT" + linkobjectproperty.getLeftobjectforlink().getName() + "ID",
						linkobjectproperty.getLeftobjectforlink())));
		createlinkaction.addInputArgument(new ObjectArgument(this.getName(), this));
		createlinkaction.addInputArgument(
				new ObjectIdArgument("RIGHT" + linkobjectproperty.getRightobjectforlink().getName() + "ID",
						linkobjectproperty.getRightobjectforlink()));
		createlinkaction.addOutputArgument(new ArrayArgument(new ObjectIdArgument("NEWLINKID", this)));
		linkobjectproperty.getLeftobjectforlink().addActionToModifyGroup(createlinkaction);
		this.addActionToCreateNewGroup(createlinkaction);
		return createlinkaction;
	}

	private ActionDefinition generateCreateLinkAction(LinkObject<?, ?> linkobjectproperty) {
		// security done
		String createlinkactionname = "CREATE" + this.getName();
		DynamicActionDefinition createlinkaction = new DynamicActionDefinition(createlinkactionname, true);
		createlinkaction.addInputArgumentAsAccessCriteria(
				new ObjectIdArgument("LEFT" + linkobjectproperty.getLeftobjectforlink().getName() + "ID",
						linkobjectproperty.getLeftobjectforlink()));
		createlinkaction.addInputArgument(new ObjectArgument(this.getName(), this));
		createlinkaction.addInputArgument(new ArrayArgument(
				new ObjectIdArgument("RIGHT" + linkobjectproperty.getRightobjectforlink().getName() + "ID",
						linkobjectproperty.getRightobjectforlink())));
		createlinkaction.addOutputArgument(new ArrayArgument(new ObjectIdArgument("NEWLINKID", this)));
		linkobjectproperty.getLeftobjectforlink().addActionToModifyGroup(createlinkaction);
		this.addActionToCreateNewGroup(createlinkaction);

		return createlinkaction;
	}

	private ActionDefinition generateShowAutoLinkTree(AutolinkObject<?> autolinkobject) {
		// security done
		String showautolinktree = "SHOWAUTOLINKTREEFOR" + this.getName();
		DynamicActionDefinition showautolinktreeaction = new DynamicActionDefinition(showautolinktree, true);
		showautolinktreeaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument(
				autolinkobject.getObjectforlink().getName() + "ID", autolinkobject.getObjectforlink()));
		showautolinktreeaction
				.addOutputArgument(new NodeTreeArgument(new ObjectArgument(this.getName() + "TREE", this)));
		autolinkobject.getObjectforlink().addActionToReadActionGroup(showautolinktreeaction);
		return showautolinktreeaction;
	}

	private ActionDefinition generateCreateAutolinkAction(AutolinkObject<?> linkobjectproperty) {
		// security done
		String createlinkactionname = "CREATE" + this.getName();
		DynamicActionDefinition createlinkaction = new DynamicActionDefinition(createlinkactionname, true);
		createlinkaction.addInputArgumentAsAccessCriteria(
				new ArrayArgument(new ObjectIdArgument("LEFT" + linkobjectproperty.getObjectforlink().getName() + "ID",
						linkobjectproperty.getObjectforlink())));
		createlinkaction.addInputArgument(new ObjectArgument(this.getName(), this));
		createlinkaction.addInputArgument(
				new ArrayArgument(new ObjectIdArgument("RIGHT" + linkobjectproperty.getObjectforlink().getName() + "ID",
						linkobjectproperty.getObjectforlink())));
		createlinkaction.addInputArgument(
				new ObjectIdArgument(linkobjectproperty.getObjectforlink().getName() + "IDOBJECTTOSHOW",
						linkobjectproperty.getObjectforlink()));
		createlinkaction.addOutputArgument(
				new ObjectIdArgument(linkobjectproperty.getObjectforlink().getName() + "IDOBJECTTOSHOW_THRU",
						linkobjectproperty.getObjectforlink()));
		linkobjectproperty.getObjectforlink().addActionToModifyGroup(createlinkaction);
		return createlinkaction;
	}

	private ActionDefinition generateChangeParent(LinkedToParent<?> linkedtoparent) {
		// security done
		String changeparentactionname = "CHANGEPARENTFOR" + linkedtoparent.getInstancename() + "OF" + this.getName();
		DynamicActionDefinition changeparentaction = new DynamicActionDefinition(changeparentactionname, true);
		changeparentaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("OBJECTID", this));
		changeparentaction
				.addInputArgument(new ObjectIdArgument("NEWPARENTID", linkedtoparent.getParentObjectForLink()));
		changeparentaction.addOutputArgument(new ObjectIdArgument("OUTPUTID", this));
		this.addActionToModifyGroup(changeparentaction);
		return changeparentaction;
	}

	private ActionDefinition generateChangeStateAction() {
		// security done
		Lifecycle lifecycle = (Lifecycle) this.getPropertyByName("LIFECYCLE");
		String changestateactionname = "CHANGESTATE" + this.getName();
		DynamicActionDefinition changestateaction = new DynamicActionDefinition(changestateactionname, true);
		changestateaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		changestateaction.addInputArgument(new ChoiceArgument("NEWSTATE", lifecycle.getTransitionChoiceCategory()));
		changestateaction.addOutputArgument(new ObjectIdArgument("OUTPUTID", this));
		this.addActionToSteerActionGroup(changestateaction);
		return changestateaction;

	}

	private ActionDefinition generateRescheduleAction() {
		// security done

		String rescheduleaction = "RESCHEDULE" + this.getName();
		DynamicActionDefinition rescheduleactiondef = new DynamicActionDefinition(rescheduleaction, true);
		rescheduleactiondef.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		rescheduleactiondef.addInputArgument(new TimestampArgument("STARTTIME"));
		rescheduleactiondef.addInputArgument(new TimestampArgument("ENDTIME"));
		rescheduleactiondef.addOutputArgument(new ObjectIdArgument("OUTPUTID", this));
		this.addActionToScheduleActionGroup(rescheduleactiondef);
		return rescheduleactiondef;

	}

	private ActionDefinition generateInsertAfterAction() {
		String insertafter = "INSERTAFTER" + this.getName();
		DynamicActionDefinition insertafteraction = new DynamicActionDefinition(insertafter, true);
		insertafteraction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ORIGINID", this));
		if (this.hasNumbered())
			if (!this.isAutoNumbered())
				insertafteraction.addInputArgument(new StringArgument("SUCCESSORNR", 64));
		if (this.hasNamed())
			insertafteraction.addInputArgument(new StringArgument("SUCCESSORNAME", 64));
		insertafteraction.addInputArgument(new ObjectArgument("SUCCESSOR", this));
		insertafteraction.addInputArgument(new TimestampArgument("SUCCESSORSTARTDATE"));
		insertafteraction.addInputArgument(new TimestampArgument("SUCCESSORENDDATE"));
		insertafteraction.addOutputArgument(new ObjectIdArgument("ORIGINIDTHRU", this));
		this.addActionToScheduleActionGroup(insertafteraction);
		return insertafteraction;
	}

	private ActionDefinition generatePrepareShowPlanningAction() {
		String prepareshowplanning = "PREPARESHOWPLANNINGFOR" + this.getName();
		Schedule schedule = (Schedule) this.getPropertyByName("SCHEDULE");
		DynamicActionDefinition prepareshowplanningaction = new DynamicActionDefinition(prepareshowplanning, true);
		prepareshowplanningaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		prepareshowplanningaction.addOutputArgument(new ArrayArgument(new ObjectArgument("ALLTASKS", this)));

		prepareshowplanningaction.addOutputArgument(
				new ArrayArgument(new ObjectArgument("ALLDEPENDENCIES", schedule.getDependencyObject())));

		prepareshowplanningaction.addOutputArgument(new IntegerArgument("SCHEDULEDAYSTART"));
		prepareshowplanningaction.addOutputArgument(new IntegerArgument("SCHEDULEDAYEND"));
		this.addActionToReadActionGroup(prepareshowplanningaction);
		return prepareshowplanningaction;
	}

	private ActionDefinition generateRescheduleAndShowPlanning() {
		String rescheduleandshowplanning = "RESCHEDULEANDSHOWPLANNINGFOR" + this.getName();
		DynamicActionDefinition rescheduleandshowplanningaction = new DynamicActionDefinition(rescheduleandshowplanning,
				true);

		rescheduleandshowplanningaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("RESCHEDULEID", this));
		rescheduleandshowplanningaction.addInputArgument(new TimestampArgument("STARTTIME"));
		rescheduleandshowplanningaction.addInputArgument(new TimestampArgument("ENDTIME"));
		rescheduleandshowplanningaction.addOutputArgument(new ObjectIdArgument("RESCHEDULEID_THRU", this));

		this.addActionToScheduleActionGroup(rescheduleandshowplanningaction);
		return rescheduleandshowplanningaction;
	}

	private PageDefinition generateShowPlanningPage() {
		DynamicPageDefinition showplanningpage = new DynamicPageDefinition(
				"SHOWPLANNINGFOR" + this.getName().toUpperCase());
		Schedule schedule = (Schedule) this.getPropertyByName("SCHEDULE");

		showplanningpage.addInputParameter(new ArrayArgument(new ObjectArgument("ALLTASKS", this)));
		showplanningpage.addInputParameter(
				new ArrayArgument(new ObjectArgument("ALLDEPENDENCIES", schedule.getDependencyObject())));
		showplanningpage.addInputParameter(new IntegerArgument("SCHEDULEDAYSTART"));
		showplanningpage.addInputParameter(new IntegerArgument("SCHEDULEDAYEND"));

		return showplanningpage;
	}

	private ActionDefinition generateSetTargetDateAction() {
		// security done
		String settargetdateactionname = "SETTARGETDATEFOR" + this.getName();
		DynamicActionDefinition settargetdate = new DynamicActionDefinition(settargetdateactionname, true);
		settargetdate.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		settargetdate.addInputArgument(new TimestampArgument("NEWTARGETDATE"));
		settargetdate.addOutputArgument(new ObjectIdArgument("OUTPUTID", this));
		this.addActionToScheduleActionGroup(settargetdate);
		return settargetdate;
	}

	private ActionDefinition generateShowActionForIteration() {
		String showactionforiterationname = "SHOW" + this.getName() + "ITERATION";
		DynamicActionDefinition showactionforiteration = new DynamicActionDefinition(showactionforiterationname, true);
		showactionforiteration.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		showactionforiteration.addInputArgument(new IntegerArgument("ITERATION"));
		showactionforiteration.addOutputArgument(new ObjectArgument(this.getName(), this));
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				showactionforiteration
						.addOutputArgument(new LargeBinaryArgument(imagecontent.getInstancename() + "TBN", false));
				showactionforiteration.addOutputArgument(new ObjectIdArgument(
						imagecontent.getInstancename() + "FULLIMGID", SystemModule.getSystemModule().getBinaryFile()));

			}

		}
		this.addActionToReadActionGroup(showactionforiteration);

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof Lifecycle) {
				Lifecycle lifecycle = (Lifecycle) thisproperty;

				if (lifecycle.getUnreleasedWarning() != null)
					showactionforiteration.addOutputArgument(new StringArgument("UNRELEASEDWARNING", 256));
			}
		}

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				if (thislinkedfromchildren.getOriginObjectProperty().getBusinessRuleByName("SUBOJECT") != null) {
					showactionforiteration
							.addOutputArgument(new ArrayArgument(new ObjectArgument(thislinkedfromchildren.getName(),
									thislinkedfromchildren.getChildObject())));
				}
			}
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) thisproperty;
				showactionforiteration.addOutputArgument(new ArrayArgument(
						new ObjectArgument(thisleftforlink.getName(), thisleftforlink.getLinkObjectDefinition())));
			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				// leftforlink
				if (!hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					// symetric: nodetree is not supported
					showactionforiteration.addOutputArgument(new ArrayArgument(
							new ObjectArgument("LEFT" + hasautolink.getName(), hasautolink.getLinkObjectDefinition())));
				}
			}

		}
		return showactionforiteration;
	}

	private ActionDefinition generateNewVersionAction(boolean restrictsnewversion) {
		String newversionactionname = "NEWVERSIONFOR" + this.getName();
		DynamicActionDefinition newversionaction = new DynamicActionDefinition(newversionactionname, true);
		newversionaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		newversionaction.addOutputArgument(new ObjectIdArgument("NEWERSIONID", this));
		if (!restrictsnewversion)
			this.addActionToCreateNewGroup(newversionaction);
		return newversionaction;
	}

	private ActionDefinition generateforceVersionAsLast() {
		String forceversionatlast = "FORCEVERSIONASLASTFOR" + this.getName();
		DynamicActionDefinition forceversionatlastaction = new DynamicActionDefinition(forceversionatlast, true);
		forceversionatlastaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		forceversionatlastaction.addOutputArgument(new ObjectIdArgument("ID_THRU", this));
		return forceversionatlastaction;
	}

	private ActionDefinition generateRenumberAction() {
		String renumberactionname = "RENUMBER" + this.getName();
		DynamicActionDefinition renumberaction = new DynamicActionDefinition(renumberactionname, true);
		renumberaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		renumberaction.addInputArgument(new StringArgument("newnumber", 64));
		renumberaction.addOutputArgument(new ObjectIdArgument("IDTHRU", this));
		if (!this.isAutoNumbered())
			this.addActionToSteerActionGroup(renumberaction);
		return renumberaction;
	}

	private ActionDefinition generateShowAction() {
		String showactionname = "SHOW" + this.getName();
		DynamicActionDefinition showaction = new DynamicActionDefinition(showactionname, true);
		showaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ID", this));
		showaction.addOutputArgument(new ObjectArgument(this.getName(), this));
		showaction.addOutputArgument(
				new ChoiceArgument("USERLOCALE", SystemModule.getSystemModule().getApplicationLocale()));
		showaction.addOutputArgument(
				new ChoiceArgument("PREFENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));
		// adding special displays for properties step 0 - workflow
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if ((thisproperty instanceof SimpleTaskWorkflow) || (thisproperty instanceof ComplexWorkflow)) {
				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument("YOURACTIVETASKS", SystemModule.getSystemModule().getTask())));
				showaction.addOutputArgument(
						new ArrayArgument(new ObjectArgument("ALLTASKS", SystemModule.getSystemModule().getTask())));

			}

		}
		this.addActionToReadActionGroup(showaction);
		// adding special displays for properties step 1 - lifecycle
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof Lifecycle) {
				Lifecycle lifecycle = (Lifecycle) thisproperty;
				showaction.addOutputArgument(
						new ChoiceArgument("POTENTIALSTATES", lifecycle.getTransitionChoiceCategory()));
				if (lifecycle.getUnreleasedWarning() != null)
					showaction.addOutputArgument(new StringArgument("UNRELEASEDWARNING", 256));
			}
		}

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof TimeSlot) {

				showaction.addOutputArgument(new TimestampArgument("STARTTIME"));
				showaction.addOutputArgument(new TimestampArgument("ENDTIME"));
			}
		}
		// schedule
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof Schedule) {
				if (this.hasNumbered())
					if (!this.isAutoNumbered())
						showaction.addOutputArgument(new StringArgument("INSERTAFTERNR", 64));
				if (this.hasNamed())
					showaction.addOutputArgument(new StringArgument("INSERTAFTERNAME", 64));

				showaction.addOutputArgument(new ObjectArgument("BLANKFORINSERTAFTER", this));
				showaction.addOutputArgument(new TimestampArgument("INSERTAFTERSTART"));
				showaction.addOutputArgument(new TimestampArgument("INSERTAFTEREND"));

			}
		}
		// adding special displays for properties step 2 - images
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				showaction.addOutputArgument(new LargeBinaryArgument(imagecontent.getInstancename() + "TBN", false));
				showaction.addOutputArgument(new ObjectIdArgument(imagecontent.getInstancename() + "FULLIMGID",
						SystemModule.getSystemModule().getBinaryFile()));

			}

		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof FileContent) {
				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument("ATTACHMENTS", SystemModule.getSystemModule().getObjectAttachment())));

			}
		}

		if (this.hasNumbered())
			showaction.addOutputArgument(new StringArgument("NUMBERFORRENUMBER", 64));
		// adding special displays for properties step 3 - links

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);

			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument(thislinkedfromchildren.getName(), thislinkedfromchildren.getChildObject())));
			}
			if (thisproperty instanceof LeftForLink) {
				@SuppressWarnings("rawtypes")
				LeftForLink<?, ?> thisleftforlink = (LeftForLink) thisproperty;

				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument(thisleftforlink.getName(), thisleftforlink.getLinkObjectDefinition())));
				showaction.addOutputArgument(new ObjectArgument(thisleftforlink.getName() + "BLANKFORADD",
						thisleftforlink.getLinkObjectDefinition()));
			}
			if (thisproperty instanceof RightForLink) {
				RightForLink<?, ?> thisrightforlink = (RightForLink<?, ?>) thisproperty;
				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument(thisrightforlink.getName(), thisrightforlink.getLinkObjectDefinition())));
				showaction.addOutputArgument(new ObjectArgument(thisrightforlink.getName() + "BLANKFORADD",
						thisrightforlink.getLinkObjectDefinition()));

			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				// leftforlink
				if (hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					// symetric: nodetree is not supported
					showaction.addOutputArgument(new ArrayArgument(
							new ObjectArgument("LEFT" + hasautolink.getName(), hasautolink.getLinkObjectDefinition())));
				} else {
					// not symetric: we can display nodetree
					if (hasautolink.getLinkObjectProperty().isShowLinkTree()) {
						showaction.addOutputArgument(new NodeTreeArgument(new ObjectArgument(
								"LEFT" + hasautolink.getName(), hasautolink.getLinkObjectDefinition())));
					} else {
						showaction
								.addOutputArgument(new ArrayArgument(new ObjectArgument("LEFT" + hasautolink.getName(),
										hasautolink.getLinkObjectDefinition())));

					}
				}
				showaction.addOutputArgument(new ObjectArgument(hasautolink.getName() + "BLANKFORADD",
						hasautolink.getLinkObjectDefinition()));

				// rightforlink
				showaction.addOutputArgument(new ArrayArgument(
						new ObjectArgument("RIGHT" + hasautolink.getName(), hasautolink.getLinkObjectDefinition())));

			}

		}

		return showaction;
	}

	/**
	 * This method adds to the module automatically generated actions and pages.<br>
	 * Pages:
	 * <ul>
	 * <li>show:displays the attributes and allows access to other actions</li>
	 * <li>edit: allows to update the attributes of the object</li>
	 * </ul>
	 * Actions:
	 * <ul>
	 * <li>prepare blank object</li>
	 * <li>create object</li>
	 * <li>show object</li>
	 * <li>update object</li>
	 * </ul>
	 * 
	 * @param module current module of the object
	 */
	public void generateAutomaticPagesAndActions(Module module) {
		if (this.hasLifecycle())
			if (this.isShowActionAutomaticallyGenerated()) {
				module.addAction(this.generateChangeStateAction());

			}
		if (this.hasTimeslot()) {
			module.addAction(this.generateRescheduleAction());

		}
		if (this.hasSchedule()) {
			module.addAction(this.generatePrepareShowPlanningAction());
			module.addAction(this.generateRescheduleAndShowPlanning());
			module.AddPage(this.generateShowPlanningPage());
			module.addAction(this.generateInsertAfterAction());
		}

		if (this.propertylist.lookupOnName("TARGETDATE") != null) {
			module.addAction(this.generateSetTargetDateAction());
		}
		LinkedToParent<?>[] linkedtoparents = this.getParents();
		if (linkedtoparents != null)
			if (linkedtoparents.length > 0)
				logger.info(" adding " + linkedtoparents.length + " for object " + this.getName());
		if (linkedtoparents != null)
			for (int i = 0; i < linkedtoparents.length; i++) {
				module.addAction(this.generateChangeParent(linkedtoparents[i]));
				module.addAction(this.generateMassUpdateAndShowParentAction(linkedtoparents[i]));
			}
		if (this.isUniqueIdentified()) {
			boolean subobject = false;
			LinkedToParent<?> relevantlinkedtoparent = null;
			boolean autolink = false;

			boolean linkobject = false;
			if (this.getPropertyByName("LINKOBJECT") != null)
				linkobject = true;
			if (this.getPropertyByName("AUTOLINKOBJECT") != null)
				autolink = true;
			if (linkedtoparents != null)
				for (int i = 0; i < linkedtoparents.length; i++) {
					LinkedToParent<?> thislinkedtoparent = linkedtoparents[i];
					module.addAction(generateMassiveDeleteActionAndShowParent(thislinkedtoparent));

					for (int j = 0; j < thislinkedtoparent.getBusinessRuleNumber(); j++) {

						PropertyBusinessRule<?> thisbusinesrule = thislinkedtoparent.getBusinessRule(j);
						if (thisbusinesrule instanceof SubObject) {
							if (subobject)
								throw new RuntimeException(
										"Object should have only one linkedtoparent property defined as subobject "
												+ this.getName());
							subobject = true;
							relevantlinkedtoparent = thislinkedtoparent;
						}
					}
				}
			if (((!subobject) && (!autolink) && (!linkobject)) || ((linkobject) && (this.IsIterated()))) {
				module.addAction(generateDeleteAction());
			}
			if (autolink) {
				module.addAction(this.generateDeleteAutolinkAndShowObject());
			}
			if (subobject) {
				module.addAction(this.generateDeleteActionAndShowParent(relevantlinkedtoparent));
			}
			if (linkobject) {
				module.addAction(this.generateDeleteLinkAndShowLeft());
				module.addAction(this.generateDeleteLinkAndShowRight());
			}
		}

		if ((this.IsIterated()) || (this.isVersioned())) {
			module.addAction(this.generateShowHistoryAction());
			module.AddPage(this.generateShowHistoryPage());

		}

		if (this.hasSimpleWorkflow()) {
			module.addasMenuAction(this.generateSimpleWorkflowCockpitAction());
			module.addAction(this.generateTaskReassignAction());
			module.AddPage(this.generateSimpleWorkflowCockpitPage());
		}

		if (isShowActionAutomaticallyGenerated()) {
			module.addAction(generateShowAction());
			module.AddPage(generateShowPage());
			if (this.IsIterated()) {
				ActionDefinition showactionforiteration = generateShowActionForIteration();
				module.addAction(showactionforiteration);
				DynamicPageDefinition showpageforiteration = new DynamicPageDefinition(
						showactionforiteration.getName());
				showpageforiteration.linkPageToAction(showactionforiteration);
				module.AddPage(showpageforiteration);
			}
			if (this.hasNumbered()) {
				module.addAction(generateRenumberAction());
			}
			if (this.isVersioned()) {
				module.addAction(generateNewVersionAction(
						((Versioned) (this.getPropertyByName("VERSIONED"))).isNewVersionRestricted()));
				module.addAction(generateforceVersionAsLast());

			}

			logger.info(" -- declared automatically generated show action / page for object " + this.getName());
			DynamicActionDefinition prepareupdateaction = generatePrepareUpdateAction();
			UniqueIdentified uniqueidentifiedproperty = (UniqueIdentified) this.getPropertyByName("UNIQUEIDENTIFIED");
			uniqueidentifiedproperty.addActionOnObjectId(prepareupdateaction);
			module.addAction(prepareupdateaction);
			module.addAction(generateUpdateAction());
			module.addAction(generateMassUpdateAction());
			module.addAction(generateFlatFileLoader());
			module.addAction(generateFlatFileSample());
			module.AddPage(generateUpdatePage());

			module.addAction(generateSearchAction());
			module.addAction(generateLaunchSearchAction());
			module.AddPage(generateSearchPage());
			logger.info(" -- declared automatically generated update action incl. prepare / page for object "
					+ this.getName() + " and search action / page");

		} else {
			logger.info(" -- did not generate show action and page for object " + this.getName()
					+ " because it is not unique identified or is a link");
		}
		Property<?> linkobject = this.getPropertyByName("LINKOBJECT");
		Property<?> autolinkobject = this.getPropertyByName("AUTOLINKOBJECT");
		if (linkobject != null) {
			LinkObject<?, ?> linkobjecttyped = (LinkObject<?, ?>) linkobject;
			module.addAction(this.generateCreateLinkAction(linkobjecttyped));
			module.addAction(this.generateCreateLinkAndRightObjectAction(linkobjecttyped));
			module.addAction(this.generateCreateLinkActionAndShowRight(linkobjecttyped));
			// only creates this action if the object is not normal show.
			if (!this.isShowActionAutomaticallyGenerated())
				module.addAction(this.generateMassUpdateAction());
			module.addAction(this.generateMassUpdateAndShowLeftAction(linkobjecttyped));
			// only create specific action if the link object has business rules
			if (linkobjecttyped.getBusinessRuleNumber() > 0) {
				module.addAction(generateSearchActionForRightObjectLink(linkobjecttyped));
				module.addAction(generateSearchActionForLeftObjectLink(linkobjecttyped));
			}
			if (linkobjecttyped.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT") != null) {
				@SuppressWarnings("rawtypes")
				ConstraintOnLinkObjectSameParent<?, ?> constraint = (ConstraintOnLinkObjectSameParent) linkobjecttyped
						.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT");
				// action may be added several times due to several links with constraints
				ActionDefinition searchactionwithparents = generateSearchActionWithParent(linkobjecttyped,
						constraint.getRightobjectparentproperty());
				boolean isreallyadded = module.addActionIfNotExists(searchactionwithparents);
				if (isreallyadded) {
					linkobjecttyped.getRightobjectforlink().addActionToLookupActionGroup(searchactionwithparents);
					linkobjecttyped.getRightobjectforlink().addActionToReadActionGroup(searchactionwithparents);
				}
			}
		}
		if (autolinkobject != null) {
			AutolinkObject<?> autolinkobjecttyped = (AutolinkObject<?>) autolinkobject;
			module.addAction(this.generateCreateAutolinkAction(autolinkobjecttyped));
			if (autolinkobjecttyped.getBusinessRuleNumber() > 0)
				module.addAction(generateSearchActionForRightObjectAutolink(autolinkobjecttyped));
			if (!autolinkobjecttyped.isSymetricLink()) {
				module.addAction(this.generateShowAutoLinkTree(autolinkobjecttyped));
			}

		}
		if (this.isStoredobject())
			if (((linkobject == null) && (autolinkobject == null)) || (this.isShowActionAutomaticallyGenerated())) {
				module.addAction(this.generatePrepareStandardCreateAction());
				module.addAction(this.generateStandardCreateAction());
				this.addActionOnObjectPage(this.generateSaveAsAction());
				module.AddPage(this.generateStandardCreatePage());
			}

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren linkedfromchildren = (LinkedFromChildren) thisproperty;
				String loadchildrenactioname = "LOADCHILDREN" + linkedfromchildren.getInstancename() + "FOR"
						+ this.getName();
				DynamicActionDefinition loadchildren = new DynamicActionDefinition(loadchildrenactioname, true);
				loadchildren.addInputArgument(new ObjectIdArgument("PARENTID", this));
				loadchildren.addInputArgument(
						new ChoiceArgument("LOCALE", SystemModule.getSystemModule().getApplicationLocale()));
				loadchildren.addInputArgument(
						new ChoiceArgument("FILEENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));
				loadchildren.addInputArgument(new LargeBinaryArgument("FLATFILE", false));
				loadchildren.addOutputArgument(new StringArgument("LOADINGCONTEXT", 500));
				loadchildren.addOutputArgument(new IntegerArgument("INSERTED"));
				loadchildren.addOutputArgument(new IntegerArgument("UPDATED"));
				loadchildren.addOutputArgument(new IntegerArgument("ERRORS"));
				loadchildren.addOutputArgument(new IntegerArgument("POSTPROCERRORS"));

				loadchildren.addOutputArgument(new IntegerArgument("LOADINGTIME"));
				loadchildren.addOutputArgument(new ArrayArgument(
						new ObjectArgument("ERRORDETAIL", SystemModule.getSystemModule().getCSVLoaderError())));
				loadchildren.addOutputArgument(new ObjectIdArgument("PARENTID_THRU", this));
				module.addAction(loadchildren);
				linkedfromchildren.getChildObject().addActionToDataAdminActionGroup(loadchildren);

				String exportchildrenactionname = "EXPORTCHILDREN" + linkedfromchildren.getInstancename() + "FOR"
						+ this.getName();
				DynamicActionDefinition exportchildren = new DynamicActionDefinition(exportchildrenactionname, true);
				exportchildren.addInputArgument(new ObjectIdArgument("PARENTID", this));
				DataObjectDefinition childobject = linkedfromchildren.getChildObject();
				if (childobject.categoryforextractor != null) {
					exportchildren.addInputArgument(new ChoiceArgument("EXPORTTYPE", childobject.categoryforextractor));
				}
				exportchildren.addOutputArgument(new LargeBinaryArgument("FLATFILE", false));
				module.addAction(exportchildren);
				linkedfromchildren.getChildObject().addActionToDataAdminActionGroup(exportchildren);
			}

			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String setimagecontentname = "SETIMAGECONTENTFOR" + imagecontent.getInstancename() + "FOR"
						+ this.getName();
				DynamicActionDefinition imagecontentaction = new DynamicActionDefinition(setimagecontentname, true);

				imagecontentaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName(), this));
				imagecontentaction.addInputArgument(new LargeBinaryArgument("FULLIMAGE", false));
				imagecontentaction.addInputArgument(new LargeBinaryArgument("THUMBNAIL", false));
				imagecontentaction.addOutputArgument(new ObjectIdArgument("ID", this));
				module.addAction(imagecontentaction);
				this.addActionToModifyGroup(imagecontentaction);

				String getfullimagename = "GETFULLIMAGEFOR" + imagecontent.getInstancename() + "FOR" + this.getName();
				DynamicActionDefinition getfullimagecontentaction = new DynamicActionDefinition(getfullimagename, true);
				getfullimagecontentaction
						.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName() + "ID", this));
				getfullimagecontentaction.addOutputArgument(new LargeBinaryArgument("FULLIMAGE", false));
				module.addAction(getfullimagecontentaction);
				this.addActionToReadActionGroup(getfullimagecontentaction);

			}
			if (thisproperty instanceof FileContent) {
				String addnewattachmentname = "ADDNEWATTACHMENTFOR" + this.getName();
				DynamicActionDefinition addnewattachmentaction = new DynamicActionDefinition(addnewattachmentname,
						true);
				addnewattachmentaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName(), this));
				addnewattachmentaction.addInputArgument(new StringArgument("COMMENT", 800));
				addnewattachmentaction.addInputArgument(new LargeBinaryArgument("FILE", false));
				addnewattachmentaction.addOutputArgument(new ObjectIdArgument("ID", this));
				this.addActionToModifyGroup(addnewattachmentaction);
				module.addAction(addnewattachmentaction);

				String downloadattachment = "DOWNLOADATTACHMENTFOR" + this.getName();
				DynamicActionDefinition downloadattachmentaction = new DynamicActionDefinition(downloadattachment,
						true);
				downloadattachmentaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument(this.getName(), this));
				downloadattachmentaction.addInputArgument(
						new ObjectIdArgument("ATTACHMENTID", SystemModule.getSystemModule().getObjectAttachment()));
				downloadattachmentaction.addOutputArgument(new LargeBinaryArgument("FILE", false));
				this.addActionToReadActionGroup(downloadattachmentaction);
				module.addAction(downloadattachmentaction);

				String deleteattachment = "DELETEATTACHMENTFOR" + this.getName();
				DynamicActionDefinition deleteattachmentaction = new DynamicActionDefinition(deleteattachment, true);
				deleteattachmentaction.addInputArgumentAsAccessCriteria(new ObjectIdArgument("OBJECTID", this));
				deleteattachmentaction.addInputArgument(
						new ObjectIdArgument("OBJATTACHMENTID", SystemModule.getSystemModule().getObjectAttachment()));
				deleteattachmentaction.addOutputArgument(new ObjectIdArgument("OBJECTID_THRU", this));
				this.addActionToModifyGroup(deleteattachmentaction);
				module.addAction(deleteattachmentaction);

			}
			if (thisproperty instanceof PrintOut) {
				PrintOut printout = (PrintOut) thisproperty;
				DynamicActionDefinition printoutpreview = generatePrintOutPreview(printout);
				module.addAction(printoutpreview);

			}
		}
	}

	private ActionDefinition generateSearchActionWithParent(
			LinkObject<?, ?> linkobjecttyped,
			LinkedToParent<?> rightobjectparentproperty) {
		return this.searchpagesandactions.generateSearchActionWithParent(linkobjecttyped, rightobjectparentproperty);
	}

	private ActionDefinition generateLaunchSearchAction() {
		StaticActionDefinition launchsearch = new StaticActionDefinition("LAUNCHSEARCH" + this.getName().toUpperCase(),
				true);
		launchsearch.addOutputArgument(
				new ChoiceArgument("PREFLANGUAGE", SystemModule.getSystemModule().getApplicationLocale()));
		launchsearch.addOutputArgument(
				new ChoiceArgument("PREFFILEENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));

		this.addActionToReadActionGroup(launchsearch);
		return launchsearch;
	}

	private PageDefinition generateStandardCreatePage() {
		DynamicPageDefinition standardcreatepage = new DynamicPageDefinition(
				"STANDARDCREATE" + this.getName().toUpperCase());
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			for (int j = 0; j < property.getContextDataForCreationSize(); j++) {

				standardcreatepage.addInputParameter(property.getContextDataForCreation(j));
			}
			if (!property.isDataInputHiddenForCreation())
				for (int j = 0; j < property.getDataInputSize(); j++) {
					standardcreatepage.addInputParameter(property.getDataInputForCreation(j));
				}
		}

		standardcreatepage.addInputParameter(new ObjectArgument("object", this));
		return standardcreatepage;
	}

	private ActionDefinition generateCreateLinkAndRightObjectAction(LinkObject<?, ?> linkobjectproperty) {
		DynamicActionDefinition createlinkandrightobject = new DynamicActionDefinition(
				"CREATELINKANDRIGHTOBJECTFOR" + this.getName().toUpperCase(), true);

		addAttributesToCreateobject(createlinkandrightobject, linkobjectproperty.getRightobjectforlink(), true);
		createlinkandrightobject
				.addInputArgument(new ObjectIdArgument("LEFTOBJECTID", linkobjectproperty.getLeftobjectforlink()));
		createlinkandrightobject.addOutputArgument(
				new ObjectIdArgument("LEFTOBJECTID_THRU", linkobjectproperty.getLeftobjectforlink()));
		linkobjectproperty.getRightobjectforlink().addActionToCreateNewGroup(createlinkandrightobject);
		return createlinkandrightobject;
	}

	private void addAttributesToCreateobject(DynamicActionDefinition action, DataObjectDefinition object) {
		addAttributesToCreateobject(action, object, false);
	}

	private void addAttributesToCreateobject(
			DynamicActionDefinition action,
			DataObjectDefinition object,
			boolean objectisoptional) {
		for (int i = 0; i < object.propertylist.getSize(); i++) {
			Property<?> property = object.propertylist.get(i);
			for (int j = 0; j < property.getContextDataForCreationSize(); j++) {

				action.addInputArgument(property.getContextDataForCreation(j));

			}
			if (!property.isDataInputHiddenForCreation())
				for (int j = 0; j < property.getDataInputSize(); j++) {
					action.addInputArgument(property.getDataInputForCreation(j));
				}
		}
		ObjectArgument mainobject = new ObjectArgument("object", object);
		mainobject.setOptional(objectisoptional);
		action.addInputArgumentAsAccessCriteria(mainobject);
	}

	private DynamicActionDefinition generateSaveAsAction() {
		DynamicActionDefinition saveasaction = new DynamicActionDefinition("SAVEAS" + this.getName().toUpperCase(),
				true);
		saveasaction.addInputArgument(new ObjectIdArgument("ORIGINID", this));
		saveasaction.addOutputArgument(new ObjectArgument("COPYOBJECT", this));
		saveasaction.setButtonlabel("Save as");
		this.addActionToCreateNewGroup(saveasaction);

		return saveasaction;
	}

	private ActionDefinition generateStandardCreateAction() {
		DynamicActionDefinition standardcreationaction = new DynamicActionDefinition(
				"STANDARDCREATE" + this.getName().toUpperCase(), true);
		addAttributesToCreateobject(standardcreationaction, this);
		standardcreationaction.addOutputArgument(new ObjectIdArgument("createdobjectid", this));
		this.addActionToCreateNewGroup(standardcreationaction);
		return standardcreationaction;
	}

	private ActionDefinition generatePrepareStandardCreateAction() {
		DynamicActionDefinition preparestandardcreationaction = new DynamicActionDefinition(
				"PREPARESTANDARDCREATE" + this.getName().toUpperCase(), true);
		// -- contextattributes
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			for (int j = 0; j < property.getContextDataForCreationSize(); j++) {

				preparestandardcreationaction.addInputArgument(property.getContextDataForCreation(j));

			}
		}

		// -- output arguments
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			for (int j = 0; j < property.getContextDataForCreationSize(); j++) {
				ArgumentContent argument = property.getContextDataForCreation(j);
				// note: need to look at if optional attributes are necessary for output and for
				// pages

				ArgumentContent newargument = argument.generateCopy("COPY" + argument.getName());
				preparestandardcreationaction.addOutputArgument(newargument);
			}
			if (!property.isDataInputHiddenForCreation())
				for (int j = 0; j < property.getDataInputSize(); j++) {
					preparestandardcreationaction.addOutputArgument(property.getDataInputForCreation(j));
				}
		}

		preparestandardcreationaction.addOutputArgument(new ObjectArgument("object", this));
		this.addActionToCreateNewGroup(preparestandardcreationaction);
		return preparestandardcreationaction;
	}

	private PageDefinition generateSearchPage() {
		return this.searchpagesandactions.generateSearchPage();

	}

	SearchWidgetDefinition[] getSearchWidgets() {
		ArrayList<SearchWidgetDefinition> searchwidgets = new ArrayList<SearchWidgetDefinition>();
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			SearchWidgetDefinition[] widgettable = thisproperty.getSearchWidgetList();
			for (int j = 0; j < widgettable.length; j++) {
				searchwidgets.add(widgettable[j]);

			}
		}
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			SearchWidgetDefinition[] widgettable = thisfield.getSearchWidgetList();
			for (int j = 0; j < widgettable.length; j++) {
				searchwidgets.add(widgettable[j]);

			}
		}
		return searchwidgets.toArray(new SearchWidgetDefinition[0]);
	}

	private ActionDefinition generateSearchActionForRightObjectLink(LinkObject<?, ?> linkobject) {
		return searchpagesandactions.generateSearchActionForRightObjectLink(linkobject);
	}

	private ActionDefinition generateSearchActionForLeftObjectLink(LinkObject<?, ?> linkobject) {
		return searchpagesandactions.generateSearchActionForLeftObjectLink(linkobject);
	}

	private ActionDefinition generateSearchActionForRightObjectAutolink(AutolinkObject<?> linkobject) {
		return searchpagesandactions.generateSearchActionForRightObjectAutolink(linkobject);
	}

	private ActionDefinition generateSearchAction() {
		return searchpagesandactions.generateSearchAction();

	}

	/**
	 * @return true if show action should be generated
	 */
	public boolean isShowActionAutomaticallyGenerated() {
		// -- generate show action if object is uniquely identified and is not a link
		if (this.getPropertyByName("UNIQUEIDENTIFIED") == null)
			return false;
		if (this.getPropertyByName("LINKOBJECT") != null) {
			if (this.getPropertyByName("ITERATED") == null)
				return false;
			if (this.getPropertyByName("ITERATED") != null)
				return true;

		}
		if (this.getPropertyByName("AUTOLINKOBJECT") != null)
			return false;
		return true;
	}

	/**
	 * @return true if the data object has the property located
	 */
	public boolean isLocated() {
		if (this.getPropertyByName("LOCATED") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Lifecycle
	 */
	public boolean hasLifecycle() {
		if (this.getPropertyByName("LIFECYCLE") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Timeslot
	 */
	public boolean hasTimeslot() {
		if (this.getPropertyByName("TIMESLOT") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Schedule
	 */
	public boolean hasSchedule() {
		if (this.getPropertyByName("SCHEDULE") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Numbered
	 */
	public boolean hasNumbered() {
		if (this.getPropertyByName("NUMBERED") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Versioned
	 */
	public boolean isVersioned() {
		if (this.getPropertyByName("VERSIONED") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Named
	 */
	public boolean hasNamed() {
		if (this.getPropertyByName("NAMED") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has a Numbered property where the number is
	 *         not automatically generated
	 */
	public boolean isManualNumbered() {
		if (this.getPropertyByName("NUMBERED") != null) {
			Numbered numbered = (Numbered) this.getPropertyByName("NUMBERED");
			for (int i = 0; i < numbered.getBusinessRuleNumber(); i++) {
				PropertyBusinessRule<Numbered> businessrule = numbered.getBusinessRule(0);
				if (businessrule instanceof AutonumberingRule)
					return false;
			}
			return true;
		}
		return false;

	}

	/**
	 * @return true if the number for this object (property NUMBERED) is
	 *         automatically generated
	 */
	public boolean isAutoNumbered() {
		if (this.getPropertyByName("NUMBERED") != null) {
			Numbered numbered = (Numbered) this.getPropertyByName("NUMBERED");
			for (int i = 0; i < numbered.getBusinessRuleNumber(); i++) {
				PropertyBusinessRule<Numbered> businessrule = numbered.getBusinessRule(0);
				if (businessrule instanceof AutonumberingRule)
					return true;
			}
			return false;
		}
		return false;

	}

	/**
	 * @return true if the data object has the property Stored Object
	 */
	public boolean isStoredobject() {
		if (this.getPropertyByName("STOREDOBJECT") == null)
			return false;
		return true;
	}

	/**
	 * @return true if the data object has the property Iterated
	 */
	public boolean IsIterated() {
		if (this.getPropertyByName("ITERATED") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property SimpleWorkflow
	 */
	public boolean hasSimpleWorkflow() {
		if (this.getPropertyByName("SIMPLETASKWORKFLOW") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property FileContent
	 */
	public boolean hasFileContent() {
		if (this.getPropertyByName("FILECONTENT") != null)
			return true;
		return false;
	}

	/**
	 * @return true if the data object has the property Uniqueidentified
	 */
	public boolean isUniqueIdentified() {
		if (this.getPropertyByName("UNIQUEIDENTIFIED") != null)
			return true;
		return false;
	}

	/**
	 * @return all the LinkedToParent properties on this object
	 */
	public LinkedToParent<?>[] getParents() {

		ArrayList<LinkedToParent<?>> listofparents = new ArrayList<LinkedToParent<?>>();
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof LinkedToParent) {
				listofparents.add(((LinkedToParent<?>) thisproperty));
			}
		}
		return listofparents.toArray(new LinkedToParent[0]);
	}

	/**
	 * generate the preprocessing for massive data access method
	 * 
	 * @param method         data access method
	 * @param originproperty property linked to the data access method
	 * @return the code for pre-processing
	 */
	public String getPreprocessingForMethodForMassive(DataAccessMethod method, Property<?> originproperty) {
		String allpreprocessing = "";
		boolean ispreprocessing = false;
		for (int i = 0; i < propertylist.getSize(); i++) {

			Property<?> thisproperty = propertylist.get(i);
			String propertyclass = StringFormatter.formatForJavaClass(thisproperty.getName());
			if (thisproperty.getPropertyclassname() != null)
				propertyclass = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());
			String propertyattribute = StringFormatter.formatForAttribute(thisproperty.getName());

			// first pass - preprocessing method with data access
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {
					if (additionalprocessing.isPreprocessing())
						if (additionalprocessing.isPreliminaryDataAccess()) {
							allpreprocessing += "			" + propertyclass + "<"
									+ thisproperty.getPropertyGenericsString() + ">[] preproc" + propertyattribute
									+ "batch = new " + propertyclass + "[object.length];\n";
							allpreprocessing += "			for (int i=0;i<object.length;i++) preproc"
									+ propertyattribute + "batch [i] = object[i]." + propertyattribute + ";\n";
							allpreprocessing += "			" + propertyclass + ".preproc"
									+ StringFormatter.formatForJavaClass(originproperty.getPropertyclassname())
									+ StringFormatter.formatForJavaClass(method.getName()) + "(definition";
							String attributes = method.generateMethodInternalArgumentsForMassive(originproperty,
									StringFormatter.formatForJavaClass(originproperty.parent.getName()), true);

							allpreprocessing += attributes;
							allpreprocessing += ",preproc" + propertyattribute
									+ "batch); // preliminary data access \n";
						}
					ispreprocessing = true;
				}
			}
		}
		for (int i = 0; i < propertylist.getSize(); i++) {

			Property<?> thisproperty = propertylist.get(i);
			String propertyclass = StringFormatter.formatForJavaClass(thisproperty.getName());
			String propertyattribute = StringFormatter.formatForAttribute(thisproperty.getName());
			if (thisproperty.getPropertyclassname() != null)
				propertyclass = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());

			// second pass - other preprocessing methods
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {

					if (additionalprocessing.isPreprocessing())
						if (!additionalprocessing.isPreliminaryDataAccess()) {
							String attributes = method.generateMethodInternalArgumentsForMassive(originproperty,
									StringFormatter.formatForJavaClass(originproperty.parent.getName()), false);
							allpreprocessing += "			" + propertyclass + "<"
									+ thisproperty.getPropertyGenericsString() + ">[] preproc" + propertyattribute
									+ "batch = new " + propertyclass + "[object.length];\n";
							allpreprocessing += "			for (int i=0;i<object.length;i++) preproc"
									+ propertyattribute + "batch [i] = object[i]." + propertyattribute + ";\n";
							allpreprocessing += "			" + propertyclass + ".preproc"
									+ StringFormatter.formatForJavaClass(originproperty.getPropertyclassname()) +

									StringFormatter.formatForJavaClass(method.getName()) + "(" + attributes + ",preproc"
									+ propertyattribute + "batch);\n";
						}
					ispreprocessing = true;
				}
			}
		}
		if (ispreprocessing)
			return allpreprocessing;
		return null;
	}

	/**
	 * generate the preprocessing for method
	 * 
	 * @param method         data access method
	 * @param originproperty property linked to the data access method
	 * @return the code for pre-processing
	 */
	public String getPreprocessingForMethod(DataAccessMethod method, Property<?> originproperty) {
		boolean ispreprocessing = false;
		String allpreprocessing = "";

		for (int i = 0; i < propertylist.getSize(); i++) {

			Property<?> thisproperty = propertylist.get(i);
			// first pass - preprocessing method with data access
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {
					if (additionalprocessing.isPreprocessing())
						if (additionalprocessing.isPreliminaryDataAccess()) {
							allpreprocessing += "			" +

									StringFormatter.formatForAttribute(thisproperty.getName()) + ".preproc" +

									StringFormatter.formatForJavaClass(originproperty.getPropertyclassname())
									+ StringFormatter.formatForJavaClass(method.getName()) + "(definition";
							String attributes = method.generateMethodInternalArguments(originproperty,
									StringFormatter.formatForJavaClass(originproperty.parent.getName()), true);

							allpreprocessing += attributes;
							allpreprocessing += "); // preliminary data access \n";
						}
					ispreprocessing = true;
				}
			}
		}
		for (int i = 0; i < propertylist.getSize(); i++) {

			Property<?> thisproperty = propertylist.get(i);
			// second pass - other preprocessing methods
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {

					if (additionalprocessing.isPreprocessing())
						if (!additionalprocessing.isPreliminaryDataAccess()) {
							String attributes = method.generateMethodInternalArguments(originproperty,
									StringFormatter.formatForJavaClass(originproperty.parent.getName()), false);
							allpreprocessing += "			"
									+ StringFormatter.formatForAttribute(thisproperty.getName()) + ".preproc"
									+ StringFormatter.formatForJavaClass(originproperty.getPropertyclassname()) +

									StringFormatter.formatForJavaClass(method.getName()) + "(" + attributes + ");\n";
						}
					ispreprocessing = true;
				}
			}
		}
		if (ispreprocessing)
			return allpreprocessing;
		return null;
	}

	/**
	 * generate the post-processing for method
	 * 
	 * @param method         data access method
	 * @param originproperty property linked to the data access method
	 * @return the code for post-processing
	 */
	public String getPostprocessingForMethod(DataAccessMethod method, Property<?> originproperty) {
		String allpostprocessing = null;
		for (int i = 0; i < propertylist.getSize(); i++) {
			Property<?> thisproperty = propertylist.get(i);
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {
					if (!additionalprocessing.isPreprocessing()) {
						String attributes = method.generateMethodInternalArguments(originproperty,
								StringFormatter.formatForJavaClass(originproperty.parent.getName()), false);

						if (allpostprocessing == null)
							allpostprocessing = "";
						allpostprocessing += "			" + StringFormatter.formatForAttribute(thisproperty.getName())
								+ ".postproc" + StringFormatter.formatForJavaClass(originproperty.getName())
								+ StringFormatter.formatForJavaClass(method.getName()) + "(" + attributes + ");";
					}
				}
			}
		}
		return allpostprocessing;
	}

	/**
	 * generate the post-processing for massive method
	 * 
	 * @param method         data access method
	 * @param originproperty property linked to the data access method
	 * @return the code for post-processing
	 */
	public String getPostprocessingForMethodForMassive(DataAccessMethod method, Property<?> originproperty) {
		String allpostprocessing = null;
		for (int i = 0; i < propertylist.getSize(); i++) {
			Property<?> thisproperty = propertylist.get(i);
			String propertyclass = StringFormatter.formatForJavaClass(thisproperty.getName());
			String propertyattribute = StringFormatter.formatForAttribute(thisproperty.getName());
			if (thisproperty.getPropertyclassname() != null)
				propertyclass = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());
			for (int j = 0; j < thisproperty.getMethodAdditionalProcessingnumber(); j++) {
				MethodAdditionalProcessing additionalprocessing = thisproperty.getMethodAdditionalProcessing(j);
				if (method.equals(additionalprocessing.getDependentmethod())) {
					if (!additionalprocessing.isPreprocessing()) {
						String attributes = method.generateMethodInternalArgumentsForMassive(originproperty,
								StringFormatter.formatForJavaClass(originproperty.parent.getName()), false);

						if (allpostprocessing == null)
							allpostprocessing = "";
						allpostprocessing += "			" + propertyclass + "<"
								+ thisproperty.getPropertyGenericsString() + ">[] postproc" + propertyattribute
								+ "batch = new " + propertyclass + "[object.length];\n";
						allpostprocessing += "			for (int i=0;i<object.length;i++) postproc" + propertyattribute
								+ "batch [i] = object[i]." + propertyattribute + ";\n";

						allpostprocessing += "			" + propertyclass + ".postproc"
								+ StringFormatter.formatForJavaClass(originproperty.getName())
								+ StringFormatter.formatForJavaClass(method.getName()) + "(" + attributes + ",postproc"
								+ propertyattribute + "batch);";
					}
				}
			}
		}
		return allpostprocessing;
	}

	// -------------------------------------------------------------------------------------------------
	// AUTOMATICALLY GENERATED PAGES AND ACTIONS - HELPERS
	// (Chapter 3)
	// -------------------------------------------------------------------------------------------------

	/**
	 * @return true if there is non optional context for data creation
	 */
	public boolean isNonOptionalContextForCreation() {
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {
				ArgumentContent thiscontextdata = thisproperty.getContextDataForCreation(j);
				if (!thiscontextdata.isOptional())
					return true;
			}
		}
		return false;
	}

	/**
	 * generate the extra attributes for the creation action
	 * 
	 * @param object data object
	 * @return a string buffer holding the extra
	 */
	StringBuffer generateCreateObjectExtraAttributes(DataObjectDefinition object) {
		StringBuffer extraattributesdeclaration = new StringBuffer();
		for (int i = 0; i < object.propertylist.getSize(); i++) {
			Property<?> thisproperty = object.propertylist.get(i);
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {

				ArgumentContent thisargument = thisproperty.getContextDataForCreation(j);
				if (extraattributesdeclaration.length() > 0)
					extraattributesdeclaration.append(" , ");
				extraattributesdeclaration
						.append(" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");

				ArrayList<String> imports = thisargument.getImports();
				for (int k = 0; k < imports.size(); k++) {

				}
			}
			for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
				if (!thisproperty.isDataInputHiddenForCreation()) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);

					if (extraattributesdeclaration.length() > 0)
						extraattributesdeclaration.append(" , ");
					extraattributesdeclaration
							.append(" " + thisargument.getType() + " " + thisargument.getName().toLowerCase() + " ");

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {

					}
				}
			}
		}
		return extraattributesdeclaration;
	}

	/**
	 * get the import declaration for creation page
	 * 
	 * @param object data object
	 * @return a list of import declarations
	 */
	HashMap<String, String> getImportDeclarationForCreation(DataObjectDefinition object) {
		HashMap<String, String> importdeclaration = new HashMap<String, String>();
		for (int i = 0; i < object.propertylist.getSize(); i++) {
			Property<?> thisproperty = object.propertylist.get(i);
			for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {

				ArgumentContent thisargument = thisproperty.getContextDataForCreation(j);

				ArrayList<String> imports = thisargument.getImports();
				for (int k = 0; k < imports.size(); k++) {
					importdeclaration.put(imports.get(k), imports.get(k));
				}
			}
			for (int j = 0; j < thisproperty.getDataInputSize(); j++) {
				if (!thisproperty.isDataInputHiddenForCreation()) {
					ArgumentContent thisargument = thisproperty.getDataInputForCreation(j);

					ArrayList<String> imports = thisargument.getImports();
					for (int k = 0; k < imports.size(); k++) {
						importdeclaration.put(imports.get(k), imports.get(k));
					}
				}
			}
		}
		return importdeclaration;
	}

	/**
	 * as part of the application generation, will finalize the properties settings
	 */
	public void finalizemodel() {
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			this.propertylist.get(i).setFinalSettings();
		}

	}

	// ------------------------------------------------------------------------------------------------
	// DATA OBJECT AUTOMATICALLY GENERATED FILES
	// Chapter 4
	// kept in the same file as the rest of the Data Object Definition code for now
	// ------------------------------------------------------------------------------------------------

	/**
	 * generates the definition of the data object to file
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during code generation
	 */
	public void generateDefinitionToFile(SourceGenerator sg, Module module) throws IOException {
		String classname = StringFormatter.formatForJavaClass(this.getName());

		NamedList<DataObjectDefinition> linkedobjects = new NamedList<DataObjectDefinition>();

		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			ArrayList<DataObjectDefinition> dependentobjects = thisproperty.getExternalObjectDependence();
			if (dependentobjects != null)
				for (int j = 0; j < dependentobjects.size(); j++) {
					linkedobjects.addIfNew(dependentobjects.get(j));
					logger.finest("for object " + this.getName() + ", for property " + thisproperty.getName()
							+ ", adding dependent object " + dependentobjects.get(j).getName());
				}
		}

		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("");
		sg.wl("import java.math.BigDecimal;");
		sg.wl("import java.util.Date;");
		sg.wl("import org.openlowcode.server.data.formula.FormulaElement;");
		sg.wl("import org.openlowcode.server.data.*;");
		sg.wl("import org.openlowcode.server.data.storage.Row;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.widget.AttributeMarker;");
		sg.wl("import org.openlowcode.server.graphic.widget.SDecimalFormatter;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.IntegerDataEltType;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.server.data.storage.StoredFieldSchema;");
		sg.wl("import org.openlowcode.server.data.properties.UniqueidentifiedInterface;");
		if (this.aliasfilteronparent != null) {
			sg.wl("import org.openlowcode.server.data.loader.FlatFileExtractorParentFilter;");
			sg.wl("import " + module.getPath() + ".utility."
					+ StringFormatter.formatForJavaClass(aliasfilteronparent.getName()) + "For" + classname
					+ "AliasFilter;");
		}

		if (this.categoryforextractor != null) {
			sg.wl("import java.util.HashMap;");
			sg.wl("import java.util.ArrayList;");

			sg.wl("import " + this.categoryforextractor.getParentModule().getPath() + ".data.choice."
					+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName()) + "ChoiceDefinition;");
		} else {
			if (this.aliasfilteronparent!=null) sg.wl("import java.util.ArrayList;");
		}
		for (int i = 0; i < this.constraintsforobject.getSize(); i++) {
			MultiFieldConstraint thisconstraint = this.constraintsforobject.get(i);
			String constraintclass = StringFormatter.formatForJavaClass(thisconstraint.getName());
			sg.wl("import " + this.getOwnermodule().getPath() + ".utility." + constraintclass
					+ "MultiFieldConstraint;");
		}
		for (int i = 0; i < linkedobjects.getSize(); i++) {
			DataObjectDefinition linkedobject = linkedobjects.get(i);
			String linkedobjectclassname = StringFormatter.formatForJavaClass(linkedobject.getName());
			Module currentmodule = linkedobject.getOwnermodule();
			if (!linkedobjectclassname.equals(classname)) { // only adds if not already main object
				sg.wl("import " + currentmodule.getPath() + ".data." + linkedobjectclassname + ";");
				sg.wl("import " + currentmodule.getPath() + ".data." + linkedobjectclassname + "Definition;");

			}

		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			sg.wl("import org.openlowcode.server.data.properties."
					+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "Definition;");
			for (int j = 0; j < thisproperty.getBusinessRuleNumber(); j++) {
				PropertyBusinessRule<?> businessrule = thisproperty.getBusinessRule(j);
				String[] importstatements = businessrule.getImportstatements();
				if (importstatements != null)
					for (int k = 0; k < importstatements.length; k++)
						sg.wl(importstatements[k]);
			}
		}
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			thisfield.writeDependentClass(sg, module);
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			thisproperty.writeDependentClass(sg, module);
		}
		sg.wl("");
		String interfacestring = "";
		if (this.categoryforextractor != null) {
			interfacestring = "\n		implements SpecificAliasList<"
					+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName()) + "ChoiceDefinition> ";
			if (this.aliasfilteronparent != null)
				interfacestring += ",\n			SpecificAliasListWithParent<"
						+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName()) + "ChoiceDefinition,"
						+ StringFormatter.formatForJavaClass(aliasfilteronparent.getName()) + ">";
		} else {
			if (this.aliasfilteronparent != null)
				interfacestring += "\n		implements SpecificAliasListWithParentWithoutParameter<"
	
						+ StringFormatter.formatForJavaClass(aliasfilteronparent.getName()) + ">";			
		}
		
		sg.wl("public class " + classname + "Definition extends DataObjectDefinition<" + classname + "> "
				+ interfacestring + " {");
		sg.wl("");
		if (this.aliasfilteronparent != null) {
			sg.wl("		// filter on parent");
			String parentclass = StringFormatter.formatForJavaClass(this.aliasfilteronparent.getName());
			sg.wl("	private FlatFileExtractorParentFilter<" + classname + "," + parentclass
					+ "> parentaliasfilter = new " + parentclass + "For" + classname + "AliasFilter();");
		}
		sg.wl("		// all fields");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);

			sg.wl("	" + thisfield.getDataObjectFieldName() + "Definition " + thisfield.getName().toLowerCase()
					+ "field;");
		}
		sg.wl("");
		sg.wl("		// all properties");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			String genericsstring = thisproperty.getPropertyGenericsString();

			sg.wl("	" + StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "Definition<"
					+ genericsstring + "> " + StringFormatter.formatForAttribute(thisproperty.getName()) + ";");
		}
		for (int i = 0; i < this.constraintsforobject.getSize(); i++) {
			MultiFieldConstraint thisconstraint = this.constraintsforobject.get(i);
			String constraintvariable = StringFormatter.formatForAttribute(thisconstraint.getName());
			String constraintclass = StringFormatter.formatForJavaClass(thisconstraint.getName());
			sg.wl("		" + constraintclass + "MultiFieldConstraint " + constraintvariable + "constraint;");
		}
		sg.wl("");
		sg.wl("	private static " + classname + "Definition singleton = new " + classname + "Definition();");
		sg.wl("");
		sg.wl("	// addition of FieldMarker");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];
				if (element instanceof ObjectIdStoredElement) {
					ObjectIdStoredElement idelement = (ObjectIdStoredElement) element;
					String referencedclass = null;
					if (idelement.getReferencedObject() != null) {
						referencedclass = StringFormatter.formatForJavaClass(idelement.getReferencedObject().getName());
						sg.wl("	private AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType<" + referencedclass + ">> "
								+ StringFormatter.formatForAttribute(idelement.getName()) + "marker;");
					} else {
						sg.wl("	private AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType> " + StringFormatter.formatForAttribute(idelement.getName())
								+ "marker;");

					}
				}
				if (element instanceof IntegerStoredElement) {
					IntegerStoredElement integerelement = (IntegerStoredElement) element;
					sg.wl("	private AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
							+ ",IntegerDataEltType> " + StringFormatter.formatForAttribute(integerelement.getName())
							+ "marker;");
				}
			}
		}
		sg.wl("");
		sg.wl("	public static " + classname + "Definition get" + classname + "Definition() {");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	private " + classname + "Definition() {");
		sg.wl("		super(\"" + this.getName() + "\",\"" + this.getOwnermodule().getCode() + "\",\"" + this.label
				+ "\");");
		// initiates attributemarker
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];
				if (element instanceof ObjectIdStoredElement) {
					ObjectIdStoredElement idelement = (ObjectIdStoredElement) element;
					String referencedclass = null;
					if (idelement.getReferencedObject() != null) {
						referencedclass = StringFormatter.formatForJavaClass(idelement.getReferencedObject().getName());
						sg.wl("		" + StringFormatter.formatForAttribute(idelement.getName())
								+ "marker = new AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType<" + referencedclass + ">>(\"" + idelement.getName() + "\");");
					} else {
						sg.wl("		" + StringFormatter.formatForAttribute(idelement.getName())
								+ "marker = new AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType>(\"" + idelement.getName() + "\");");

					}
				}
				if (element instanceof IntegerStoredElement) {
					IntegerStoredElement integerelement = (IntegerStoredElement) element;
					sg.wl("		" + StringFormatter.formatForAttribute(integerelement.getName())
							+ "marker = new AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
							+ ",IntegerDataEltType>(\"" + integerelement.getName() + "\");");

				}
			}
		}

		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			for (int j = 0; j < thisproperty.getDisplayProfileForPropertyNumber(); j++) {
				sg.wl("		this.defineDisplayProfile(\""
						+ thisproperty.getDisplayProfileForProperty(j).getName().toUpperCase() + "\");");
			}
		}
		if (aliaslist.size() > 0)
			sg.wl("		// Alias List");
		for (int i = 0; i < aliaslist.size(); i++) {
			String alias = aliaslist.get(i);
			String value = this.loaderalias.get(alias);
			alias = StringFormatter.escapeforjavastring(alias);
			sg.wl("		this.setAlias(\"" + alias + "\", \"" + value + "\");");
		}
		if (this.preferedspreadsheettabname != null)
			sg.wl("		this.setPreferedSpreadsheetTab(\""
					+ StringFormatter.escapeforjavastring(this.preferedspreadsheettabname) + "\");");

		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void initFields()  {");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);
			sg.wl("		" + field.getName().toLowerCase() + "field = new " + field.getDataObjectFieldName()
					+ "Definition(" + field.getDataObjectConstructorAttributes() + ",this);");
			sg.wl("		this.addFieldDefinition(" + field.getName().toLowerCase() + "field);	");
		}
		sg.wl("		");
		for (int i = 0; i < this.constraintsforobject.getSize(); i++) {
			MultiFieldConstraint thisconstraint = this.constraintsforobject.get(i);
			String constraintvariable = StringFormatter.formatForAttribute(thisconstraint.getName());
			String constraintclass = StringFormatter.formatForJavaClass(thisconstraint.getName());

			sg.wl("		" + constraintvariable + "constraint = new " + constraintclass + "MultiFieldConstraint();");
			sg.wl("		this.addMultiFieldConstraint(" + constraintvariable + "constraint.getStorage());");

		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void initProperties()  {");
		sg.wl("		// create property definitions");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {

			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);

			sg.wl("		" + StringFormatter.formatForAttribute(thisproperty.getName()) + " = new "
					+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "Definition("
					+ thisproperty.getDataObjectConstructorAttributes() + ");");
			for (int j = 0; j < thisproperty.getBusinessRuleNumber(); j++) {
				PropertyBusinessRule<?> thisbusinessrule = thisproperty.getBusinessRule(j);
				thisbusinessrule.writeInitialization(sg);
			}

			for (int j = 0; j < thisproperty.getFieldOverridesNumber(); j++) {
				FieldOverrideForProperty fieldoverrides = thisproperty.getFieldOverridesat(j);
				if ((fieldoverrides.getNewcomment() == null)
						&& (fieldoverrides.getNewpriority() == FieldOverrideForProperty.NO_PRIORITY)) {
					sg.wl("		" + StringFormatter.formatForAttribute(thisproperty.getName()) + ".overrides"
							+ StringFormatter.formatForJavaClass(fieldoverrides.getFieldcode()) + "Label(\""
							+ fieldoverrides.getNewlabel() + "\");");
				}
			}

			if (thisproperty.isLegacy()) {
				sg.wl("		this.addPropertyDefinitionAsLegacy("
						+ StringFormatter.formatForAttribute(thisproperty.getName()) + ");");
			} else {
				sg.wl("		this.addPropertyDefinition(" + StringFormatter.formatForAttribute(thisproperty.getName())
						+ ");");
			}

		}
		sg.wl("		// add dependency between property definitions");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {

			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			Property<?>[] dependentpropertiesonobject = thisproperty.getDependentProperties();
			if (dependentpropertiesonobject != null)
				for (int j = 0; j < dependentpropertiesonobject.length; j++) {
					Property<?> dependentproperty = dependentpropertiesonobject[j];
					sg.wl("		" + StringFormatter.formatForAttribute(thisproperty.getName())
							+ ".setDependentDefinition"
							+ StringFormatter.formatForJavaClass(dependentproperty.getPropertyclassname()) + "("
							+ StringFormatter.formatForAttribute(dependentproperty.getName()) + ");");
				}
		}
		sg.wl("		// add extra property definition");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			thisproperty.writeAdditionalDefinition(sg);
		}
		if (this.categoryforextractor != null) {
			sg.wl("		this.initConditionalAliasList();");
		}
		sg.wl("	}");

		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			sg.wl("");
			StringBuffer genericsstring = new StringBuffer("<");
			genericsstring.append(classname);
			for (int j = 0; j < thisproperty.getPropertyGenericsSize(); j++) {
				PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(j);
				genericsstring.append(',');
				genericsstring.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
			}
			Iterator<String> choicekey = thisproperty.getChoiceCategoryKeyNumber();
			while (choicekey.hasNext()) {
				genericsstring.append(',');
				genericsstring.append(StringFormatter.formatForJavaClass(
						thisproperty.getChoiceCategoryByKey(choicekey.next()).getName()) + "ChoiceDefinition");
			}
			genericsstring.append('>');
			sg.wl("	public " + StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "Definition"
					+ genericsstring.toString() + " get" + StringFormatter.formatForJavaClass(thisproperty.getName())
					+ "Definition() {");
			sg.wl("		return " + StringFormatter.formatForAttribute(thisproperty.getName()) + ";");
			sg.wl("	}");
		}

		sg.wl("	@Override");
		sg.wl("	public void initPropertyGenericLinks()  {");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			for (int j = 0; j < thisproperty.getPropertyGenericsSize(); j++) {
				PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(j);
				sg.wl("		" + StringFormatter.formatForAttribute(thisproperty.getName()) + ".setGenerics"
						+ StringFormatter.formatForJavaClass(thisgenerics.getName()) + "Property("
						+ StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()) + "Definition.get"
						+ StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName())
						+ "Definition().get"
						+ StringFormatter.formatForJavaClass(thisgenerics.getOtherObjectsignificantproperty().getName())
						+ "Definition());");
			}
		}
		sg.wl("	}");

		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public " + classname + " generateFromRow(Row row, TableAlias alias)");
		sg.wl("			 {");
		sg.wl("		return new " + classname + "(row,alias);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public " + classname + "[] generateArrayTemplate()  {");
		sg.wl("		return new " + classname + "[0];");
		sg.wl("	}");
		sg.wl("		");
		sg.wl("	@Override");
		sg.wl("	public DataObjectId<" + classname + ">[] generateIdArrayTemplate()  {");
		sg.wl("		return (DataObjectId<" + classname + ">[])(new DataObjectId[0]);");
		sg.wl("	}");
		sg.wl("		");
		// attributemarker getter
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];
				if (element instanceof ObjectIdStoredElement) {
					ObjectIdStoredElement idelement = (ObjectIdStoredElement) element;
					String referencedclass = null;
					if (idelement.getReferencedObject() != null) {
						referencedclass = StringFormatter.formatForJavaClass(idelement.getReferencedObject().getName());
						sg.wl("	public AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ", TObjectIdDataEltType<" + referencedclass + ">> get"
								+ StringFormatter.formatForJavaClass(idelement.getName()) + "Marker() {");
					} else {
						sg.wl("	public AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ", TObjectIdDataEltType> get"
								+ StringFormatter.formatForJavaClass(idelement.getName()) + "Marker() {");

					}
					sg.wl("		return " + StringFormatter.formatForAttribute(idelement.getName()) + "marker;");
					sg.wl("	}");

				}
				if (element instanceof IntegerStoredElement) {
					IntegerStoredElement integerelement = (IntegerStoredElement) element;
					sg.wl("	public AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
							+ ", IntegerDataEltType> get" + StringFormatter.formatForJavaClass(integerelement.getName())
							+ "Marker() {");

					sg.wl("		return " + StringFormatter.formatForAttribute(integerelement.getName()) + "marker;");
					sg.wl("	}");
				}
			}
		}

		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);
			sg.wl("");
			sg.wl("	public DataObjectFieldMarker<" + classname + "> get"
					+ StringFormatter.formatForJavaClass(field.getName()) + "FieldMarker()  {");
			sg.wl("		return this.getFieldMarker(\"" + field.getName() + "\");");
			sg.wl("	}");

		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];

				// diplay only is either actual property (legacy) or stored
				boolean display = false;
				if (!thisproperty.isLegacy())
					display = true;
				if (element instanceof StoredElement)
					display = true;
				if (display) {
					sg.wl("");
					sg.wl("	public DataObjectFieldMarker<" + classname + "> get"
							+ StringFormatter.formatForJavaClass(element.getName()) + "FieldMarker()  {");
					sg.wl("		return this.getPropertyFieldMarker(\"" + thisproperty.getName() + "\",\""
							+ element.getGenericNameForProperty().toUpperCase() + "\");");
					sg.wl("	}");
				}
			}
		}
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);
			sg.wl("");

			sg.wl("	public StoredFieldSchema<" + field.getJavaType() + "> get"
					+ StringFormatter.formatForJavaClass(field.getName()) + "FieldSchema()  {");
			sg.wl("		 return (StoredFieldSchema<" + field.getJavaType()
					+ ">) this.getTableschema().lookupFieldByName(\"" + field.getName() + "\");");
			sg.wl("	}");
		}
		sg.wl("");
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			for (int j = 0; j < thisproperty.getDisplayProfileForPropertyNumber(); j++) {
				DisplayProfile thisprofile = thisproperty.getDisplayProfileForProperty(j);
				String profileclasname = StringFormatter.formatForJavaClass(thisprofile.getName());
				String profileupper = thisprofile.getName().toUpperCase();
				sg.wl("	public DisplayProfile<" + classname + "> getDisplayProfile" + profileclasname + "() {");
				sg.wl("		return this.getDisplayProfileByName(\"" + profileupper + "\");");
				sg.wl("	}");

			}
		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			for (int j = 0; j < thisproperty.getElements().length; j++) {
				Element thiselement = thisproperty.getElements()[j];

				// diplay only is either actual property (legacy) or stored
				boolean display = false;
				if (!thisproperty.isLegacy())
					display = true;
				if (thiselement instanceof StoredElement)
					display = true;
				if (display) {
					String elementname = thiselement.getName();
					String elementtype = thiselement.getJavaFieldName();
					sg.wl("	public StoredFieldSchema<" + elementtype + "> get"
							+ StringFormatter.formatForJavaClass(elementname) + "FieldSchema()  {");
					sg.wl("		 return (StoredFieldSchema<" + elementtype
							+ ">) this.getTableschema().lookupFieldByName(\"" + elementname + "\");");
					sg.wl("	}");
				}
			}
		}

		sg.wl("	@Override");
		sg.wl("	public " + classname + " generateBlank()  {");
		sg.wl("		return new " + classname + "();");
		sg.wl("	}");

		sg.wl("	@Override");
		sg.wl("	public String getModuleName() {");
		sg.wl("		");
		sg.wl("		return \"" + this.getOwnermodule().getName().toUpperCase() + "\";");
		sg.wl("	}		");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			if (thisfield instanceof DecimalField) {
				DecimalField thisdecimalfield = (DecimalField) thisfield;
				String thisfieldclass = StringFormatter.formatForJavaClass(thisdecimalfield.getName());

				sg.wl("	public FormulaElement<" + classname + "> get" + thisfieldclass + "FormulaElement() {");
				sg.wl("		return ((" + classname + " object) -> (object.get" + thisfieldclass + "()));");
				sg.wl("	}		");

			}
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof ComputedDecimal) {
				sg.wl("	public FormulaElement<" + classname + "> get"
						+ StringFormatter.formatForJavaClass(thisproperty.getName()) + "FormulaElement() {");
				sg.wl("		return ((" + classname + " object) -> (object.get"
						+ StringFormatter.formatForJavaClass(thisproperty.getName()) + "()));");
				sg.wl("	}");
			}
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren linkedfromchildren = (LinkedFromChildren) thisproperty;
				String childclass = StringFormatter.formatForJavaClass(linkedfromchildren.getChildObject().getName());
				sg.wl("	public LinkedToChildrenNavigator<" + classname + "," + childclass + "> get"
						+ StringFormatter.formatForJavaClass(linkedfromchildren.getName()) + "Navigator() {");
				sg.wl("		return ((LinkedToChildrenNavigator<" + classname + "," + childclass + ">)((" + classname
						+ " object) -> (ArrayBufferReplacement.replaceBufferDataInArray(object.getallchildrenfor"
						+ linkedfromchildren.getInstancename().toLowerCase() + "(null)))));");
				sg.wl("	}");

			}
			if (thisproperty instanceof LinkedToParent) {
				LinkedToParent<?> linkedtoparent = (LinkedToParent<?>) thisproperty;
				String parentclass = StringFormatter
						.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());
				sg.wl("	public LinkedToParentNavigator<" + classname + "," + parentclass + "> getParentfor"
						+ linkedtoparent.getInstancename().toLowerCase() + "Navigator() {");
				sg.wl("		return ((LinkedToParentNavigator<" + classname + "," + parentclass + ">)((" + classname
						+ " object) -> (new " + parentclass + "[]");
				sg.wl("				{(OLcServer.getServer().getObjectInTriggerUpdateBuffer(object.getLinkedtoparentfor"
						+ linkedtoparent.getInstancename().toLowerCase() + "id())!=null?");
				sg.wl("						OLcServer.getServer().getObjectInTriggerUpdateBuffer(object.getLinkedtoparentfor"
						+ linkedtoparent.getInstancename().toLowerCase() + "id()):object.getparentfor"
						+ linkedtoparent.getInstancename().toLowerCase() + "())})));	");
				sg.wl("	}");

			}

			if (thisproperty instanceof LinkObject) {
				LinkObject<?, ?> thislink = (LinkObject<?, ?>) thisproperty;
				String leftobjectclass = StringFormatter.formatForJavaClass(thislink.getLeftobjectforlink().getName());
				String rightobjectclass = StringFormatter
						.formatForJavaClass(thislink.getRightobjectforlink().getName());
				sg.wl("	public LinkNavigator<" + leftobjectclass + "," + classname + "," + rightobjectclass
						+ "> getLinkNavigator() {");
				sg.wl("		return ((LinkNavigator<" + leftobjectclass + "," + classname + "," + rightobjectclass
						+ ">)((" + leftobjectclass
						+ " object)->(ArrayBufferReplacement.replaceBufferDataInTwoObjectsArray(" + classname
						+ ".getlinksandrightobject(object.getId(),null)))));");
				sg.wl("	}");
				sg.wl("	public LinkReverseNavigator<" + leftobjectclass + "," + classname + "," + rightobjectclass
						+ "> getLinkReverseNavigator() {");
				sg.wl("		return ((LinkReverseNavigator<" + leftobjectclass + "," + classname + "," + rightobjectclass
						+ ">)((" + rightobjectclass + " object)->(ArrayBufferReplacement.replaceBufferDataInArray("
						+ classname + ".getleftobjectsfromright(object.getId(),null)))));");
				sg.wl("	}");

				sg.wl("	public LinkToLeftReverseNavigator<" + leftobjectclass + "," + classname + "," + rightobjectclass
						+ "> getLinkToLeftReverseNavigator() {");

				sg.wl("		return ((LinkToLeftReverseNavigator<" + leftobjectclass + "," + classname + ","
						+ rightobjectclass + ">)((" + classname + " object)->(new " + leftobjectclass + "[]{");
				sg.wl("				(OLcServer.getServer().getObjectInTriggerUpdateBuffer(object.getLfid())!=null?");
				sg.wl("						OLcServer.getServer().getObjectInTriggerUpdateBuffer(object.getLfid()):");
				sg.wl("							" + leftobjectclass + ".readone(object.getLfid()))})));");

				sg.wl("	}");

			}

		}
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected void setCalculatedFieldTriggers()  {");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);
			if (field instanceof FormulaDefinitionElement) {
				FormulaDefinitionElement formuladefelement = (FormulaDefinitionElement) field;
				CalculatedFieldTriggerPath[] triggers = formuladefelement.getAllTriggerPaths();
				for (int j = 0; j < triggers.length; j++) {
					CalculatedFieldTriggerPath path = triggers[j];
					String objectclass = StringFormatter
							.formatForJavaClass(path.getOriginField().getParent().getName());
					sg.wl("		" + field.getName().toLowerCase()
							+ "field.setTriggerOnUpdate(new CalculatedFieldTrigger(" + objectclass + "Definition.get"
							+ objectclass + "Definition()." + path.getOriginField().getName().toLowerCase() + ","
							+ path.generatePath() + "));");

				}

			}
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> property = this.propertylist.get(i);
			if (property instanceof FormulaDefinitionElement) {
				FormulaDefinitionElement formuladefelement = (FormulaDefinitionElement) property;
				CalculatedFieldTriggerPath[] triggers = formuladefelement.getAllTriggerPaths();
				for (int j = 0; j < triggers.length; j++) {
					CalculatedFieldTriggerPath path = triggers[j];
					String objectclass = StringFormatter
							.formatForJavaClass(path.getOriginField().getParent().getName());

					sg.wl("		" + property.getName().toLowerCase() + ".setTriggerOnUpdate(new CalculatedFieldTrigger("
							+ objectclass + "Definition.get" + objectclass + "Definition()."
							+ path.getOriginField().getName().toLowerCase() + "," + path.generatePath() + "));");

				}

			}
		}

		sg.wl("	}");
		sg.wl("");
		if (this.forcedrowheight > 0) {

			sg.wl("	@Override");
			sg.wl("	public int getPreferedTableRowHeight() {");
			sg.wl("		return " + this.forcedrowheight + ";");
			sg.wl("	}");

		}

		if (this.categoryforextractor != null) {
			sg.wl("");
			sg.wl("	private HashMap<String,ChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
					+ "ChoiceDefinition>[]> conditionalaliaslist;");
			sg.wl("");
			sg.wl("	public void initConditionalAliasList()  {");
			sg.wl("		conditionalaliaslist = new HashMap<String,ChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
					+ "ChoiceDefinition>[]>();");
			Iterator<Entry<String, ChoiceValue[]>> restrictionset = restrictionforalias.entrySet().iterator();
			while (restrictionset.hasNext()) {
				Entry<String, ChoiceValue[]> thisrestriction = restrictionset.next();
				sg.wl("		conditionalaliaslist.put(\"" + StringFormatter.escapeforjavastring(thisrestriction.getKey())
						+ "\",");
				sg.wl("			new ChoiceValue[]{");
				for (int i = 0; i < thisrestriction.getValue().length; i++) {
					ChoiceValue thisvalue = thisrestriction.getValue()[i];
					sg.wl("				" + (i == 0 ? "" : ",")
							+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
							+ "ChoiceDefinition.get()." + thisvalue.getName().toUpperCase());
				}
				sg.wl("				});");
			}
			sg.wl("		}");

			sg.wl("");
			sg.wl("	@Override");
			sg.wl("	public String[] getSpecificAliasList(ChoiceValue<"
					+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
					+ "ChoiceDefinition> selectedvalue)  {");
			sg.wl("		ArrayList<String> specificaliaslist = new ArrayList<String>();");
			sg.wl("		for (int i=0;i<this.getAliasNumber();i++) {");
			sg.wl("			String thisalias = this.getAliasat(i);");
			sg.wl("			ChoiceValue<" + StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
					+ "ChoiceDefinition>[] conditionalforthisalias = conditionalaliaslist.get(thisalias);");
			sg.wl("			if (conditionalforthisalias==null) {");
			sg.wl("				specificaliaslist.add(thisalias);");
			sg.wl("			} else {");
			sg.wl("				boolean isvalid = this.isAliasValid(thisalias,selectedvalue,conditionalaliaslist);");
			sg.wl("				if (isvalid) specificaliaslist.add(thisalias);");
			sg.wl("			}");
			sg.wl("		}");
			sg.wl("		return specificaliaslist.toArray(new String[0]);");
			sg.wl("	}			");

			if (this.aliasfilteronparent != null)  {

				String parentclass = StringFormatter.formatForJavaClass(aliasfilteronparent.getName());

				sg.wl("	@Override");
				sg.wl("	public String[] getSpecificAliasList(ChoiceValue<"
						+ StringFormatter.formatForJavaClass(this.categoryforextractor.getName())
						+ "ChoiceDefinition> selectedvalue,DataObjectId<" + parentclass + "> parentid)  {");
				sg.wl("		String[] aliasforchoice = getSpecificAliasList(selectedvalue);");
				sg.wl("		ArrayList<String> aliasfilteredforparent = new ArrayList<String>();");
				sg.wl("		" + parentclass + " parent = " + parentclass + ".readone(parentid);");
				sg.wl("		for (int i=0;i<aliasforchoice.length;i++) {");
				sg.wl("			if (parentaliasfilter.isvalid(this,aliasforchoice[i],parent)) aliasfilteredforparent.add(aliasforchoice[i]);");
				sg.wl("		}");
				sg.wl("		return aliasfilteredforparent.toArray(new String[0]);");
				sg.wl("	}	");

			}
			
			

		} else {
			if (this.aliasfilteronparent != null)  {
				String parentclass = StringFormatter.formatForJavaClass(aliasfilteronparent.getName());

				sg.wl("	@Override");
				sg.wl("	public String[] getSpecificAliasList(DataObjectId<" + parentclass + "> parentid)  {");
				sg.wl("		ArrayList<String> aliasfilteredforparent = new ArrayList<String>();");
				sg.wl("		" + parentclass + " parent = " + parentclass + ".readone(parentid);");
				sg.wl("		for (int i=0;i<this.getAliasNumber();i++) {");
				sg.wl("			if (parentaliasfilter.isvalid(this,this.getAliasat(i),parent)) aliasfilteredforparent.add(this.getAliasat(i));");
				sg.wl("		}");
				sg.wl("		return aliasfilteredforparent.toArray(new String[0]);");
				sg.wl("	}	");				
			}
		}


		sg.wl("}");
		sg.close();
	}

	/**
	 * generates the source code of the data object to file
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during code generation
	 */
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String classname = StringFormatter.formatForJavaClass(this.getName());
		NamedList<DataObjectDefinition> linkedobjects = new NamedList<DataObjectDefinition>();

		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			ArrayList<DataObjectDefinition> dependentobjects = thisproperty.getExternalObjectDependence();
			if (dependentobjects != null)
				for (int j = 0; j < dependentobjects.size(); j++) {
					linkedobjects.addIfNew(dependentobjects.get(j));
					logger.finest("for object " + this.getName() + ", for property " + thisproperty.getName()
							+ ", adding dependent object " + dependentobjects.get(j).getName());
				}
		}
		sg.wl("package " + module.getPath() + ".data;");
		sg.bl();
		sg.wl("import java.math.BigDecimal;");
		sg.wl("import java.util.ArrayList;");
		sg.wl("import java.util.Date;");
		sg.wl("import org.openlowcode.server.data.*;");
		sg.wl("import org.openlowcode.server.data.storage.*;");
		sg.wl("import org.openlowcode.tools.misc.NamedList;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.graphic.widget.AttributeMarker;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.tools.structure.IntegerDataEltType;");
		sg.wl("import org.openlowcode.server.action.SecurityInDataMethod;");
		sg.wl("import org.openlowcode.server.security.ActionAuthorization;");
		sg.wl("import org.openlowcode.server.action.ActionExecution;");
		sg.wl("import org.openlowcode.server.runtime.ServerConnection;");
		String implementedproperties = "";
		int propertyinterface = 0;

		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> thisproperty = this.propertylistincludinglegacy.get(i);
			sg.wl("import org.openlowcode.server.data.properties."
					+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + ";");
			if (thisproperty.hasDynamicDefinitionHelper())
				sg.wl("import org.openlowcode.server.data.properties."
						+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname())
						+ "DefinitionDynamicHelper;");

			if (thisproperty.hasStaticQuery())
				sg.wl("import org.openlowcode.server.data.properties."
						+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "QueryHelper;");
			if (thisproperty.hasInterface()) {
				if (propertyinterface == 0)
					implementedproperties = " implements ";
				if (propertyinterface > 0)
					implementedproperties += ",";
				sg.wl("import org.openlowcode.server.data.properties."
						+ StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname()) + "Interface;");
				implementedproperties += StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname())
						+ "Interface<" + classname;
				for (int j = 0; j < thisproperty.getPropertyGenericsSize(); j++) {
					PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(j);
					implementedproperties += ","
							+ StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
				}
				Iterator<String> iterator = thisproperty.getChoiceCategoryKeyNumber();
				while (iterator.hasNext()) {
					ChoiceCategory propertychoice = thisproperty.getChoiceCategoryByKey(iterator.next());
					implementedproperties += "," + StringFormatter.formatForJavaClass(propertychoice.getName())
							+ "ChoiceDefinition";
				}
				implementedproperties += ">";
				propertyinterface++;
			}
			thisproperty.writeDependentClass(sg, module);
		}
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			thisfield.writeDependentClass(sg, module);

		}
		for (int i = 0; i < linkedobjects.getSize(); i++) {
			DataObjectDefinition linkedobject = linkedobjects.get(i);
			String linkedobjectclassname = StringFormatter.formatForJavaClass(linkedobject.getName());
			Module currentmodule = linkedobject.getOwnermodule();
			if (!linkedobjectclassname.equals(classname)) { // only adds if not already main object
				sg.wl("import " + currentmodule.getPath() + ".data." + linkedobjectclassname + ";");
				sg.wl("import " + currentmodule.getPath() + ".data." + linkedobjectclassname + "Definition;");

			}
		}
		sg.bl();
		sg.wl("public class " + classname + " extends DataObject<" + classname + "> " + implementedproperties + "{");
		sg.wl("	private static " + classname + "Definition definition= " + classname + "Definition.get" + classname
				+ "Definition();");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);
			sg.wl("	private " + field.getDataObjectFieldName() + " "
					+ StringFormatter.formatForAttribute(field.getName()) + "field;");

		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> property = this.propertylistincludinglegacy.get(i);
			String generics = "<" + classname;
			for (int j = 0; j < property.getPropertyGenericsSize(); j++) {
				PropertyGenerics thisgenerics = property.getPropertyGenerics(j);
				generics = generics + "," + StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
			}
			// **** write choice categories for property

			Iterator<String> choicekey = property.getChoiceCategoryKeyNumber();
			while (choicekey.hasNext()) {
				ChoiceCategory propertychoice = property.getChoiceCategoryByKey(choicekey.next());
				generics = generics + "," + StringFormatter.formatForJavaClass(propertychoice.getName())
						+ "ChoiceDefinition";
			}

			// **** End write choice categories
			generics = generics + ">";
			sg.wl("	private " + StringFormatter.formatForJavaClass(property.getPropertyclassname()) + generics + " "
					+ StringFormatter.formatForAttribute(property.getName()) + ";");

		}
		sg.wl("	");
		sg.wl("	public static " + classname + "Definition getDefinition() {");
		sg.wl("		return definition;");
		sg.wl("	}");

		sg.wl("	");
		sg.wl("	public static void updatePersistenceStorage()  {");
		sg.wl("		definition.updatePersistenceStorage();");
		sg.wl("	}");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			sg.wl("	public " + thisfield.getJavaType() + " get"
					+ StringFormatter.formatForJavaClass(thisfield.getName()) + "() {");
			sg.wl("		return this." + StringFormatter.formatForAttribute(thisfield.getName()) + "field.getValue();");
			sg.wl("	}");
			sg.wl("	");
			sg.wl("	public void set" + StringFormatter.formatForJavaClass(thisfield.getName()) + "("
					+ thisfield.getJavaType() + " field)  {");
			sg.wl("		this." + StringFormatter.formatForAttribute(thisfield.getName()) + "field.setValue(field);");
			sg.wl("	}");
			sg.bl();
		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> property = this.propertylistincludinglegacy.get(i);

			for (int j = 0; j < property.getElements().length; j++) {
				Element thiselement = property.getElements()[j];

				// diplay only is either actual property (legacy) or stored
				boolean display = false;
				if (!property.isLegacy())
					display = true;
				if (thiselement instanceof StoredElement)
					display = true;
				if (display) {
					String methodelement = StringFormatter.formatForJavaClass(thiselement.getName());
					String genericsmethodelement = StringFormatter
							.formatForJavaClass(thiselement.getGenericNameForProperty());
					sg.wl("	public " + thiselement.getJavaFieldName() + " get" + methodelement + "() {");
					sg.wl("		return " + StringFormatter.formatForAttribute(property.getName()) + ".get"
							+ genericsmethodelement + "();");
					sg.wl("	}");
					sg.bl();
				}
			}
			if (property.hasDynamicDefinitionHelper()) {
				sg.wl("	public void setDynamicHelperFor"
						+ StringFormatter.formatForJavaClass(
								property.getInstancename() != null ? property.getInstancename() : property.getName())
						+ "(" + StringFormatter.formatForJavaClass(property.getPropertyclassname())
						+ "DefinitionDynamicHelper dynamichelper )  {");
				sg.wl("		" + StringFormatter.formatForAttribute(property.getName())
						+ ".setDynamicHelper(dynamichelper);");
				sg.wl("	}");
				sg.bl();
			}
		}
		sg.wl("	public void initFieldAlias()  {");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = fieldlist.get(i);
			sg.wl("		" + StringFormatter.formatForAttribute(thisfield.getName()) + "field = ("
					+ thisfield.getDataObjectFieldName() + ") this.payload.lookupSimpleFieldOnName(\""
					+ thisfield.getName() + "\");");
		}
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> property = propertylistincludinglegacy.get(i);
			// calculate generics
			String generics = "<" + classname;
			for (int j = 0; j < property.getPropertyGenericsSize(); j++) {
				PropertyGenerics thisgenerics = property.getPropertyGenerics(j);
				generics = generics + "," + StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
			}
			// **** write choice categories for property

			Iterator<String> choicekey = property.getChoiceCategoryKeyNumber();
			while (choicekey.hasNext()) {
				ChoiceCategory propertychoice = property.getChoiceCategoryByKey(choicekey.next());
				generics = generics + "," + StringFormatter.formatForJavaClass(propertychoice.getName())
						+ "ChoiceDefinition";
			}

			// **** End write choice categories
			generics = generics + ">";

			sg.wl("		" + StringFormatter.formatForAttribute(property.getName()) + " = ("
					+ StringFormatter.formatForJavaClass(property.getPropertyclassname()) + generics
					+ ") this.payload.lookupPropertyOnName(\"" + property.getName() + "\");");
		}
		// setup of dependent properties
		for (int i = 0; i < this.propertylistincludinglegacy.getSize(); i++) {
			Property<?> property = propertylistincludinglegacy.get(i);
			if (property.getDependentProperties() != null)
				for (int j = 0; j < property.getDependentProperties().length; j++) {
					Property<?> dependentproperty = property.getDependentProperties()[j];
					sg.wl("		" + StringFormatter.formatForAttribute(property.getName()) + ".setDependentProperty"
							+ StringFormatter.formatForJavaClass(dependentproperty.getPropertyclassname()) + "("
							+ StringFormatter.formatForAttribute(dependentproperty.getName()) + ");");
				}
		}
		sg.wl("	}");
		sg.bl();
		sg.wl("	public " + classname + "()  {");
		sg.wl("		super(definition);");
		sg.wl("		initFieldAlias();");
		sg.wl("	}");

		sg.wl("	public " + classname + "(Row row,TableAlias alias)  {");
		sg.wl("		super(definition);");
		sg.wl("		payload.initFromDB(row, alias);");
		sg.wl("		initFieldAlias();");
		sg.wl("	}");

		sg.wl("");
		sg.wl("	public " + classname + " deepcopy()  {");
		sg.wl("		" + classname + " deepcopy = new " + classname + "();");
		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field thisfield = this.fieldlist.get(i);
			String field = StringFormatter.formatForAttribute(thisfield.getName()) + "field";
			sg.wl("		deepcopy." + field + ".setValue(this." + field + ".getValue());");

		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			String[] deepcopystatements = thisproperty.getPropertyDeepCopyStatement();
			if (deepcopystatements != null)
				for (int j = 0; j < deepcopystatements.length; j++) {
					sg.wl(deepcopystatements[j]);
				}
		}
		sg.wl("		return deepcopy;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public <Z extends DataObjectProperty<" + classname
				+ ">> Z getPropertyForObject(Z otherobjectproperty)");
		sg.wl("			 {");
		sg.wl("		if (otherobjectproperty==null) throw new RuntimeException(\"Other object property cannot be null\");");
		for (int i = 0; i < this.getPropertySize(); i++) {
			Property<?> thisproperty = this.getPropertyAt(i);
			sg.wl("		if (otherobjectproperty.getName().equals(\"" + thisproperty.getName().toUpperCase()
					+ "\")) return (Z) (" + thisproperty.getName().toLowerCase() + "); ");
		}

		sg.wl("		throw new RuntimeException(\"Not supported property \"+otherobjectproperty.getName());");
		sg.wl("	}");

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			String thispropertyname = StringFormatter.formatForAttribute(thisproperty.getName());
			String thispropertyclassname = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());
			for (int j = 0; j < thisproperty.getDataAccessMethodnumber(); j++) {
				DataAccessMethod thisdataaccessmethod = thisproperty.getDataAccessMethod(j);
				// ----------------------------------------------------- NORMAL METHOD
				// ----------------
				/* DETECT IF PREPROCESSING SHOULD BE ADDED */
				String preprocessinginstruction = this.getPreprocessingForMethod(thisdataaccessmethod, thisproperty);
				String postprocessinginstruction = this.getPostprocessingForMethod(thisdataaccessmethod, thisproperty);
				/* END DETECTING IF METHOD PREPROCESSING SHOULD BE ADDED */
				String methodname = StringFormatter.formatForAttribute(thisdataaccessmethod.getName());
				String methoclassname = StringFormatter.formatForJavaClass(thisdataaccessmethod.getName());
				String outermethodname = methodname;
				if (thisproperty.getInstancename() != null) {
					outermethodname = outermethodname + "for"
							+ StringFormatter.formatForAttribute(thisproperty.getInstancename());
				}
				boolean methodstatic = thisdataaccessmethod.isStatic();
				// ----------------------------------------- UNIQUE METHOD FOR STATIC
				// --------------
				if (methodstatic) {
					sg.w("	public ");

					sg.w("static ");
					ArgumentContent outputarg = thisdataaccessmethod.getoutputargument();
					if (outputarg != null) {
						sg.w(outputarg.getType() + " ");
					} else {
						sg.w("void ");
					}
					sg.w(outermethodname + "(");
					sg.w(thisdataaccessmethod.generateMethodArguments(false));

					sg.wl(")  {");

					if (preprocessinginstruction != null)
						sg.wl(preprocessinginstruction);
					if (outputarg != null) {
						sg.w("		return ");
					} else {
						sg.w("		");
					}
					if (thisdataaccessmethod.isStatic()) {
						// static we call the Queryhelper
						String helperattribute = "";
						if (thisproperty.getName().compareTo(thisproperty.getPropertyclassname()) != 0)
							helperattribute = "\"" + thisproperty.getName().toUpperCase() + "\"";
						sg.w(thispropertyclassname + "QueryHelper.get(" + helperattribute + ").");
					} else {
						sg.w("this." + thispropertyname + ".");
					}
					sg.w(methodname + "(");
					sg.w(thisdataaccessmethod.generateMethodInternalArguments(thisproperty, classname, false));
					sg.wl(");");

					if (postprocessinginstruction != null)
						sg.wl(postprocessinginstruction);
					sg.wl("}");

				}
				// ----------------------------------------- UNIQUE METHOD FOR NON STATIC WITH
				// ADDITIONAL ACCESS RIGHTS--------------
				else {
					// ---- method with no additional security check calling the full method
					sg.w("	public ");

					ArgumentContent outputarg = thisdataaccessmethod.getoutputargument();
					if (outputarg != null) {
						sg.w(outputarg.getType() + " ");
					} else {
						sg.w("void ");
					}
					sg.w(outermethodname + "(");
					sg.w(thisdataaccessmethod.generateMethodArguments(false));

					sg.wl(")  {");

					if (outputarg != null) {
						sg.w("		return ");
					} else {
						sg.w("		");
					}
					sg.w("this.");

					sg.w(outermethodname + "(");
					sg.w(thisdataaccessmethod.generateMethodPassThroughArguments());
					sg.wl(");");

					sg.wl("}");
					// ---- full method with additional security checks
					sg.w("	public ");

					if (outputarg != null) {
						sg.w(outputarg.getType() + " ");
					} else {
						sg.w("void ");
					}
					sg.w(outermethodname + "(");
					sg.w(thisdataaccessmethod.generateMethodArguments(true));

					sg.wl(")  {");

					sg.wl("			boolean execute=true;");

					sg.wl("			if (!SecurityInDataMethod.NONE.equals(method)) {");
					sg.wl("				ActionAuthorization executeresult = ServerConnection.isAuthorized(contextaction, this);");
					sg.wl("				if (executeresult.getAuthorization()==ActionAuthorization.NOT_AUTHORIZED) execute=false;");
					sg.wl("			}");
					sg.wl("			if (execute) { 					");
					if (preprocessinginstruction != null)
						sg.wl(preprocessinginstruction);
					if (outputarg != null) {
						sg.w("				return ");
					} else {
						sg.w("				");
					}
					if (thisdataaccessmethod.isStatic()) {
						// static we call the Queryhelper
						String helperattribute = "";
						if (thisproperty.getName().compareTo(thisproperty.getPropertyclassname()) != 0)
							helperattribute = "\"" + thisproperty.getName().toUpperCase() + "\"";
						sg.w(thispropertyclassname + "QueryHelper.get(" + helperattribute + ").");
					} else {
						sg.w("this." + thispropertyname + ".");
					}
					sg.w(methodname + "(");
					sg.w(thisdataaccessmethod.generateMethodInternalArguments(thisproperty, classname, false));
					sg.wl(");");

					if (postprocessinginstruction != null)
						sg.wl(postprocessinginstruction);

					sg.wl("			} else {");
					if (outputarg == null)
						sg.wl("				if (SecurityInDataMethod.FAIL_IF_NOT_AUTHORIZED.equals(method)) throw new RuntimeException(\"You are not authorized for this action. You may want to check object data impacting security.\");");
					if (outputarg != null)
						sg.wl("				throw new RuntimeException(\"You are not authorized for this action. You may want to check object data impacting security.\");");
					sg.wl("			}					");
					sg.wl("}");
				}

				// ----------------------------------------------------- MASSIVE METHOD
				// ----------------
				if (thisdataaccessmethod.isMassive()) {

					sg.w("	public static ");
					ArgumentContent outputarg = thisdataaccessmethod.getoutputargument();
					if (outputarg != null) {
						if (outputarg instanceof ArrayArgument) {
							sg.w(outputarg.getType() + " ");
						} else {
							sg.w(outputarg.getType() + "[] ");
						}

					} else {
						sg.w("void ");
					}
					sg.w(outermethodname + "(");
					sg.w(thisdataaccessmethod.generateMethodArgumentsForMassive());
					sg.wl(")  {");
					if (!thisdataaccessmethod.isStatic())
						sg.wl("			if (object!=null) if (object.length>0) {");
					String preprocessinginstructionformassive = this
							.getPreprocessingForMethodForMassive(thisdataaccessmethod, thisproperty);
					if (preprocessinginstructionformassive != null)
						sg.wl(preprocessinginstructionformassive);
					if (!thisdataaccessmethod.isStatic()) {
						sg.wl("				" + thispropertyclassname + "<" + thisproperty.getPropertyGenericsString()
								+ ">[] " + thispropertyname + "arrayformethod = new " + thispropertyclassname
								+ "[object.length];");
						sg.wl("				for (int i=0;i<object.length;i++) " + thispropertyname
								+ "arrayformethod[i] = object[i]." + thispropertyname + ";");
					}
					// as part of T-921, removes this constraint, as needed now
					// if (thisdataaccessmethod.isStatic()) throw new RuntimeException("Massive not
					// supported for static method");
					if (outputarg != null) {
						sg.w("			return ");
					} else {
						sg.w("			");
					}

					if (thisdataaccessmethod.isStatic()) {
						// static we call the Queryhelper
						String helperattribute = "";
						if (thisproperty.getName().compareTo(thisproperty.getPropertyclassname()) != 0)
							helperattribute = "\"" + thisproperty.getName().toUpperCase() + "\"";
						sg.w(thispropertyclassname + "QueryHelper.get(" + helperattribute + ").");
					} else {
						sg.w(thispropertyclassname + ".");
					}

					sg.w(methodname + "(");
					sg.w(thisdataaccessmethod.generateMethodInternalArgumentsForMassive(thisproperty, classname,
							false));
					if (!thisdataaccessmethod.isStatic()) {
						sg.wl("," + thispropertyname + "arrayformethod);");
					} else {
						sg.wl(");");
					}

					String postprocessinginstructionformassive = this
							.getPostprocessingForMethodForMassive(thisdataaccessmethod, thisproperty);
					if (postprocessinginstructionformassive != null)
						sg.wl(postprocessinginstructionformassive);
					if (!thisdataaccessmethod.isStatic()) {
						sg.wl(" 	}");
						if (outputarg != null)
							if (outputarg instanceof ArrayArgument) {

								ArrayArgument arrayargument = (ArrayArgument) outputarg;
								sg.wl("		return new " + arrayargument.getPayload().getType() + "[0];");
							} else {
								sg.wl("		return new " + outputarg.getType() + "[0];");
							}
					}
					sg.wl("	}");
					String secondarygenerics = "";
					for (int t = 0; t < thisproperty.getPropertyGenericsSize(); t++) {
						PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(t);
						secondarygenerics += ","
								+ StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
					}
					Iterator<String> iterator = thisproperty.getChoiceCategoryKeyNumber();
					while (iterator.hasNext()) {
						ChoiceCategory propertychoice = thisproperty.getChoiceCategoryByKey(iterator.next());
						secondarygenerics += "," + StringFormatter.formatForJavaClass(propertychoice.getName())
								+ "ChoiceDefinition";
					}
					if (thisproperty.hasInterface()) {
						sg.wl("	@Override");
						sg.wl("	public " + thispropertyclassname + "Interface.Massive" + methoclassname + "<"
								+ classname + secondarygenerics + "> getMassive" + methoclassname + "() {");
						sg.wl("		return new Massive" + methoclassname + "<" + classname + secondarygenerics
								+ ">() {");
						sg.wl("");
						sg.wl("			@Override");
						sg.w("			public ");
						if (outputarg != null) {
							if (outputarg instanceof ArrayArgument) {
								sg.w(outputarg.getType() + " ");
							} else {
								sg.w(outputarg.getType() + "[] ");
							}

						} else {
							sg.w("void ");
						}
						sg.wl(methodname + "(" + thisdataaccessmethod.generateMethodArgumentsForMassive() + ")  {");

						if (outputarg != null)
							sg.wl("				return " + classname + "." + methodname + "(" + thisdataaccessmethod
									.generateMethodInternalArgumentsForMassive(thisproperty, classname, false, false)
									+ ");");
						if (outputarg == null)
							sg.wl("				" + classname + "." + methodname + "(" + thisdataaccessmethod
									.generateMethodInternalArgumentsForMassive(thisproperty, classname, false, false)
									+ ");");

						sg.wl("				");
						sg.wl("			}");
						sg.wl("			");
						sg.wl("		};");
						sg.wl("	}");
					}

				}

			}

		}

		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];
				if (element instanceof ObjectIdStoredElement) {
					ObjectIdStoredElement idelement = (ObjectIdStoredElement) element;
					String referencedclass = null;
					if (idelement.getReferencedObject() != null) {
						referencedclass = StringFormatter.formatForJavaClass(idelement.getReferencedObject().getName());
						sg.wl("	public static AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType<" + referencedclass + ">> get"
								+ StringFormatter.formatForJavaClass(idelement.getName()) + "Marker() {");
					} else {
						sg.wl("	public static AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
								+ ",TObjectIdDataEltType> get" + StringFormatter.formatForJavaClass(idelement.getName())
								+ "Marker() {");

					}
					sg.wl("		return definition.get" + StringFormatter.formatForJavaClass(idelement.getName())
							+ "Marker();");
					sg.wl("	}");

				}
				if (element instanceof IntegerStoredElement) {
					IntegerStoredElement integerelement = (IntegerStoredElement) element;
					sg.wl("	public static AttributeMarker<" + StringFormatter.formatForJavaClass(this.getName())
							+ ",IntegerDataEltType> get" + StringFormatter.formatForJavaClass(integerelement.getName())
							+ "Marker() {");
					sg.wl("		return definition.get" + StringFormatter.formatForJavaClass(integerelement.getName())
							+ "Marker();");
					sg.wl("	}");
				}
			}
		}

		for (int i = 0; i < this.fieldlist.getSize(); i++) {
			Field field = this.fieldlist.get(i);

			sg.wl("	public static DataObjectFieldMarker<" + classname + "> get"
					+ StringFormatter.formatForJavaClass(field.getName()) + "FieldMarker()  {");
			sg.wl("		return definition.get" + StringFormatter.formatForJavaClass(field.getName())
					+ "FieldMarker();");
			sg.wl("	}");

		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			Element[] propertyelements = thisproperty.getElements();
			for (int j = 0; j < propertyelements.length; j++) {
				Element element = propertyelements[j];
				sg.wl("");
				sg.wl("	public static DataObjectFieldMarker<" + classname + "> get"
						+ StringFormatter.formatForJavaClass(element.getName()) + "FieldMarker()  {");
				sg.wl("		return definition.get" + StringFormatter.formatForJavaClass(element.getName())
						+ "FieldMarker();");
				sg.wl("	}");
			}
		}
		for (int i = 0; i < this.propertylist.getSize(); i++) {
			Property<?> thisproperty = this.propertylist.get(i);
			if (thisproperty instanceof ComputedDecimal) {
				ComputedDecimal computeddecimal = (ComputedDecimal) thisproperty;
				String instancevariable = computeddecimal.getInstancename().toLowerCase();

				sg.wl("	static CalculatedFieldExtractor<" + classname + "> getComputeddecimalfor" + instancevariable
						+ "Extractor() {");
				sg.wl("		CalculatedFieldExtractor<" + classname + "> extractor = ((" + classname
						+ " object) -> (object.computeddecimalfor" + instancevariable + "));");
				sg.wl("		return extractor;");
				sg.wl("	}");

			}
		}
		sg.wl("}");
		sg.close();
	}

}
