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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DisplayProfile;
import org.openlowcode.design.data.ExternalElement;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.TimePeriodField;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property provides a link to a parent object. In parent / children
 * relations, a parent can have several children, but the child will have only
 * one parent.
 * 
 * <br>
 * <br>
 * Parent object will get automatically the property
 * {@link org.openlowcode.design.data.properties.basic.LinkedFromChildren}
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LinkedToParent<E extends DataObjectDefinition>
		extends
		Property<LinkedToParent<E>> {
	private static Logger logger = Logger.getLogger(LinkedToParent.class.getName());
	private E parentobjectforlink;
	private NamedList<ActionDefinition> actionsonparentid;
	private boolean displaychildrenasgrid;
	private String linedisplayforgrid;
	private String columndisplayforgrid;
	private String[] cellvaluestoshow;
	private UniqueIdentified uniqueidentified;
	private WidgetDisplayPriority priorityforlinkedfromparent;
	private String secondarycolumndisplayforgrid;
	private String[] infofieldforreverseshow;
	private String specifictitleforchildrentable = null;
	private HashMap<String, ArrayList<Field>> compositeindexlist;
	private boolean reversetreegrid;
	private String[] exceptionsforinfofieldconsolidation;
	private boolean freenumberofcolumns = false;
	private String[] freecolumnsdisplayforgrid;

	/**
	 * gets the unique identified property on the child object the linked to parent
	 * property is on
	 * 
	 * @return the related unique identified property on the current object (child
	 *         for the linkedtoparent)
	 */
	public UniqueIdentified getUniqueIdentified() {
		return uniqueidentified;
	}

	/**
	 * @return the name of the related property LinkedFromChildren on the parent
	 *         object
	 */
	public String getLinkedFromChildrenName() {
		return (this.getInstancename() + "for" + parent.getName()).toUpperCase();
	}

	/**
	 * creates a linked to parent property
	 * 
	 * @param name                unique name amongst the linked to parent
	 *                            properties of this object. Should be a valid java
	 *                            field name
	 * @param parentobjectforlink the parent object the main object is linked to
	 */
	public LinkedToParent(String name, E parentobjectforlink) {
		super(name, "LINKEDTOPARENT");
		this.parentobjectforlink = parentobjectforlink;
		displaychildrenasgrid = false;
		compositeindexlist = new HashMap<String, ArrayList<Field>>();
	}

	/**
	 * creates a linked to parent property with specific widget priority
	 * 
	 * @param name                        unique name amongst the linked to parent
	 *                                    properties of this object. Should be a
	 *                                    valid java field name
	 * @param parentobjectforlink         the parent object the main object is
	 *                                    linked to
	 * @param priorityforlinkedfromparent widget priority on the parent object for
	 *                                    the children table
	 */
	public LinkedToParent(String name, E parentobjectforlink, WidgetDisplayPriority priorityforlinkedfromparent) {
		super(name, "LINKEDTOPARENT");
		this.parentobjectforlink = parentobjectforlink;
		this.priorityforlinkedfromparent = priorityforlinkedfromparent;
		if (this.priorityforlinkedfromparent != null)
			this.priorityforlinkedfromparent.checkIfValidForObject(parentobjectforlink);
		displaychildrenasgrid = false;
		compositeindexlist = new HashMap<String, ArrayList<Field>>();
	}

	/**
	 * @param specifictitleforchildrentable specifies the title for the children
	 *                                      table in parent object
	 */
	public void setSpecificTitleForChildrenTable(String specifictitleforchildrentable) {
		this.specifictitleforchildrentable = specifictitleforchildrentable;
	}

	/**
	 * creates a linked to parent property with the children objects shown from the
	 * parent as a grid
	 * 
	 * @param name                          unique name amongst the linked to parent
	 *                                      properties of this object. Should be a
	 *                                      valid java field name
	 * @param parentobjectforlink           the parent object the main object is
	 *                                      linked to
	 * @param linedisplayforgrid            the column used for line display
	 * @param columndisplayforgrid          the column used for column display
	 * @param secondarycolumndisplayforgrid the column used for secondary column
	 *                                      display (leave it null if not used)
	 * @param cellvaluestoshow              the name of the fields to show as
	 *                                      content in the grid
	 */
	public LinkedToParent(
			String name,
			E parentobjectforlink,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String[] cellvaluestoshow) {
		super(name, "LINKEDTOPARENT");
		this.parentobjectforlink = parentobjectforlink;
		displaychildrenasgrid = true;
		this.linedisplayforgrid = linedisplayforgrid;
		this.columndisplayforgrid = columndisplayforgrid;
		this.secondarycolumndisplayforgrid = secondarycolumndisplayforgrid;
		this.cellvaluestoshow = cellvaluestoshow;
		compositeindexlist = new HashMap<String, ArrayList<Field>>();
		reversetreegrid = false;
	}

	/**
	 * creates a linked to parent property with the children objects shown from the
	 * parent as a grid with a specific widget display priority
	 * 
	 * @param name                          unique name amongst the linked to parent
	 *                                      properties of this object. Should be a
	 *                                      valid java field name
	 * @param parentobjectforlink           the parent object the main object is
	 *                                      linked to
	 * @param priorityforlinkedfromparent   the priority for the grid display on the
	 *                                      parent object
	 * @param linedisplayforgrid            the column used for line display
	 * @param columndisplayforgrid          the column used for column display
	 * @param secondarycolumndisplayforgrid the column used for secondary column
	 *                                      display (leave it null if not used)
	 * @param cellvaluestoshow              the name of the fields to show as
	 *                                      content in the grid
	 */
	public LinkedToParent(
			String name,
			E parentobjectforlink,
			WidgetDisplayPriority priorityforlinkedfromparent,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String[] cellvaluestoshow) {
		super(name, "LINKEDTOPARENT");
		this.parentobjectforlink = parentobjectforlink;
		this.priorityforlinkedfromparent = priorityforlinkedfromparent;
		if (this.priorityforlinkedfromparent != null)
			this.priorityforlinkedfromparent.checkIfValidForObject(parentobjectforlink);
		displaychildrenasgrid = true;
		this.linedisplayforgrid = linedisplayforgrid;
		this.columndisplayforgrid = columndisplayforgrid;
		this.secondarycolumndisplayforgrid = secondarycolumndisplayforgrid;
		this.cellvaluestoshow = cellvaluestoshow;
		compositeindexlist = new HashMap<String, ArrayList<Field>>();
		reversetreegrid = false;
	}

	/**
	 * creates a linked to parent property with the children objects shown from the
	 * parent as a grid with a specific widget display priority. The grid is
	 * reversed, and shown as a tree
	 * 
	 * @param name                          unique name amongst the linked to parent
	 *                                      properties of this object. Should be a
	 *                                      valid java field name
	 * @param parentobjectforlink           the parent object the main object is
	 *                                      linked to
	 * @param priorityforlinkedfromparent   the priority for the grid display on the
	 *                                      parent object
	 * @param linedisplayforgrid            the column used for line display
	 * @param columndisplayforgrid          the column used for column display
	 * @param secondarycolumndisplayforgrid the column used for secondary column
	 *                                      display (leave it null if not used)
	 * @param cellvaluestoshow              the name of the fields to show as
	 *                                      content in the grid
	 * @param infofieldforreverseshow       info fields added in the reverse tree.
	 *                                      Will be shown if all values are the same
	 *                                      for all elements in the same line
	 */
	public LinkedToParent(
			String name,
			E parentobjectforlink,
			WidgetDisplayPriority priorityforlinkedfromparent,
			String linedisplayforgrid,
			String columndisplayforgrid,
			String secondarycolumndisplayforgrid,
			String cellvaluestoshow,
			String[] infofieldforreverseshow,
			String[] exceptionsforinfofieldconsolidation) {
		super(name, "LINKEDTOPARENT");
		this.parentobjectforlink = parentobjectforlink;
		this.priorityforlinkedfromparent = priorityforlinkedfromparent;
		if (this.priorityforlinkedfromparent != null)
			this.priorityforlinkedfromparent.checkIfValidForObject(parentobjectforlink);
		displaychildrenasgrid = true;
		this.linedisplayforgrid = linedisplayforgrid;
		this.columndisplayforgrid = columndisplayforgrid;
		this.secondarycolumndisplayforgrid = secondarycolumndisplayforgrid;
		this.cellvaluestoshow = new String[] { cellvaluestoshow };

		this.infofieldforreverseshow = infofieldforreverseshow;
		this.exceptionsforinfofieldconsolidation = exceptionsforinfofieldconsolidation;
		compositeindexlist = new HashMap<String, ArrayList<Field>>();
		reversetreegrid = true;
	}

	/**
	 * Sets the display as hierarchical editable tree widget with one payload value,
	 * one criteria to show as column, and several criterias, in a set order to show
	 * as lines of the hierarchical tree widget
	 * 
	 * @param columndisplayforgrid                the field to use as column
	 * @param linesdisplayforgrid                 the fields to use as lines for a
	 *                                            hierarchical tree
	 * @param cellvaluestoshow                    the unique payload field (should
	 *                                            be big decimal
	 * @param infofieldforreverseshow             a field to use to filter what to
	 *                                            show
	 * @param exceptionsforinfofieldconsolidation the values of the field defined
	 *                                            above
	 * @since 1.11
	 */
	public void setReverseTreeGrid(
			String linedisplayforgrid,
			String[] columnsdisplayforgrid,
			String cellvaluestoshow,
			String[] infofieldforreverseshow,
			String[] exceptionsforinfofieldconsolidation) {
		this.columndisplayforgrid = linedisplayforgrid;
		this.cellvaluestoshow = new String[] { cellvaluestoshow };
		this.freecolumnsdisplayforgrid = columnsdisplayforgrid;
		this.freenumberofcolumns = true;
		this.reversetreegrid = true;
		this.displaychildrenasgrid = true;
		if (this.linkedfromchildren != null)
			this.linkedfromchildren.setReverseTreeGrid(linedisplayforgrid, columnsdisplayforgrid, cellvaluestoshow,
					infofieldforreverseshow, exceptionsforinfofieldconsolidation);
	}

	private LinkedFromChildren linkedfromchildren;
	private MultiDimensionChild<E> multiDimensionchild;

	/**
	 * @return the related linked from children property on the parent object
	 */
	public LinkedFromChildren getLinkedFromChildren() {
		return this.linkedfromchildren;
	}

	@Override
	public void controlAfterParentDefinition() {
		actionsonparentid = new NamedList<ActionDefinition>();
		if (parent == null)
			throw new RuntimeException("parent is null for property with name = " + this.getName());
		if (parentobjectforlink == null)
			throw new RuntimeException("parentobjectforlink is null for property with name = " + this.getName());
		if (displaychildrenasgrid) {
			Field linefield = parent.lookupFieldByName(linedisplayforgrid);
			if (linefield == null)
				throw new RuntimeException("LinkedToParent Showasgrid did not find line field " + linedisplayforgrid);
			if (!((linefield instanceof ChoiceField) || (linefield instanceof TimePeriodField)))
				throw new RuntimeException("LinkedToParent Showasgrid : line field " + linedisplayforgrid
						+ " is not a choice field or timeperiod field but " + linefield.getClass().getName());
			Field columnfield = parent.lookupFieldByName(columndisplayforgrid);
			if (columnfield == null)
				throw new RuntimeException(
						"LinkedToParent Showasgrid did not find column field " + columndisplayforgrid);
			if (!((columnfield instanceof ChoiceField) || (columnfield instanceof TimePeriodField)))
				throw new RuntimeException("LinkedToParent Showasgrid : column field " + columndisplayforgrid
						+ " is not a choice field or timeperiod field but " + linefield.getClass().getName());

			if (cellvaluestoshow == null)
				throw new RuntimeException("LinkedToParent cellvaluestoshow is null");
			if (cellvaluestoshow.length == 0)
				throw new RuntimeException("LinkedToParent cellvaluestoshow has zero length");
			for (int i = 0; i < cellvaluestoshow.length; i++) {
				Field thiscolumnfield = parent.lookupFieldByName(cellvaluestoshow[i]);
				if (thiscolumnfield == null)
					throw new RuntimeException("LinkedToParent Showasgrid did not find cellsvaluetoshow field(" + i
							+ ") " + cellvaluestoshow[i]);

			}
		}
		this.addPropertyGenerics(
				new PropertyGenerics("PARENTOBJECTFORLINK", parentobjectforlink, new UniqueIdentified()));
		boolean deletechildren = false;
		if (this.getBusinessRuleByName("DELETECHILDRENWHENPARENTDELETED") != null)
			deletechildren = true;
		if (this.priorityforlinkedfromparent == null) {
			if (!this.displaychildrenasgrid) {
				linkedfromchildren = new LinkedFromChildren(
						this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this);
				if (this.specifictitleforchildrentable != null)
					linkedfromchildren.setSpecificTitleForChildrenTable(specifictitleforchildrentable);
			} else {
				if (!this.reversetreegrid) {

					linkedfromchildren = new LinkedFromChildren(
							this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
							this.linedisplayforgrid, this.columndisplayforgrid, this.secondarycolumndisplayforgrid,
							this.cellvaluestoshow);

				} else {
					if (!this.freenumberofcolumns) {
						linkedfromchildren = new LinkedFromChildren(
								this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
								this.linedisplayforgrid, this.columndisplayforgrid, this.secondarycolumndisplayforgrid,
								this.cellvaluestoshow, this.infofieldforreverseshow,
								this.exceptionsforinfofieldconsolidation);
					} else {
						linkedfromchildren = new LinkedFromChildren(
								this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
								linedisplayforgrid, this.freecolumnsdisplayforgrid, this.cellvaluestoshow[0],
								this.infofieldforreverseshow, this.exceptionsforinfofieldconsolidation);
					}
				}
				if (this.specifictitleforchildrentable != null)
					linkedfromchildren.setSpecificTitleForChildrenTable(specifictitleforchildrentable);
			}
			if (deletechildren) {
				DeleteChildrenWhenParentDeletedFinal deletechildrenfinal = new DeleteChildrenWhenParentDeletedFinal(
						linkedfromchildren);
				linkedfromchildren.addBusinessRule(deletechildrenfinal);
			}
			this.addExternalObjectProperty(parentobjectforlink, linkedfromchildren);
		} else {
			if (!this.displaychildrenasgrid) {
				linkedfromchildren = new LinkedFromChildren(
						this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
						this.priorityforlinkedfromparent);
				if (this.specifictitleforchildrentable != null)
					linkedfromchildren.setSpecificTitleForChildrenTable(specifictitleforchildrentable);
			} else {
				if (!this.reversetreegrid) {
					linkedfromchildren = new LinkedFromChildren(
							this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
							this.priorityforlinkedfromparent, this.linedisplayforgrid, this.columndisplayforgrid,
							this.secondarycolumndisplayforgrid, this.cellvaluestoshow);

				} else {
					linkedfromchildren = new LinkedFromChildren(
							this.getInstancename() + "for" + parent.getName().toLowerCase(), this.parent, this,
							this.priorityforlinkedfromparent, this.linedisplayforgrid, this.columndisplayforgrid,
							this.secondarycolumndisplayforgrid, this.cellvaluestoshow, this.infofieldforreverseshow,
							this.exceptionsforinfofieldconsolidation);
				}
				if (this.specifictitleforchildrentable != null)
					linkedfromchildren.setSpecificTitleForChildrenTable(specifictitleforchildrentable);
			}
			if (deletechildren) {
				DeleteChildrenWhenParentDeletedFinal deletechildrenfinal = new DeleteChildrenWhenParentDeletedFinal(
						linkedfromchildren);
				linkedfromchildren.addBusinessRule(deletechildrenfinal);
			}
			this.addExternalObjectProperty(parentobjectforlink, linkedfromchildren);
		}

		DataAccessMethod getparent = new DataAccessMethod("GETPARENT",
				new ObjectArgument("parent", parentobjectforlink), false);
		getparent.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		DataAccessMethod setparent = new DataAccessMethod("SETPARENT", null, false);
		setparent.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setparent.addInputArgument(
				new MethodArgument("parent", new ObjectIdArgument("parentobject", this.parentobjectforlink)));

		this.addDataAccessMethod(getparent);
		this.addDataAccessMethod(setparent);
		DataAccessMethod setparentwithoutupdate = new DataAccessMethod("SETPARENTWITHOUTUPDATE", null, false);
		setparentwithoutupdate
				.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setparentwithoutupdate.addInputArgument(
				new MethodArgument("parent", new ObjectIdArgument("parentobject", this.parentobjectforlink)));
		this.addDataAccessMethod(setparentwithoutupdate);

		DataAccessMethod getallchildrenofparent = new DataAccessMethod("GETALLCHILDREN",
				new ArrayArgument(new ObjectArgument("objectlist", this.parent)), true);
		getallchildrenofparent.addInputArgument(
				new MethodArgument("parentid", new ObjectIdArgument("parentid", this.parentobjectforlink)));
		this.addDataAccessMethod(getallchildrenofparent);
		// --------------------------- Fields ------------------------------------
		String idname = this.getName() + "ID";
		StoredElement parentid = new ObjectIdStoredElement(idname, parentobjectforlink);
		parentid.setGenericsName("ID");
		this.addElement(parentid);

		// ------------------------------- Index
		// -----------------------------------------------
		ArrayList<StoredElement> arrayfields = new ArrayList<StoredElement>();
		arrayfields.add(parentid);
		this.addIndex(new Index(idname, arrayfields, false));

		// Check composite index validity
		Iterator<Entry<String, ArrayList<Field>>> indexlistiterator = this.compositeindexlist.entrySet().iterator();
		while (indexlistiterator.hasNext()) {
			Entry<String, ArrayList<Field>> thisindex = indexlistiterator.next();
			ArrayList<Field> indexextrafields = thisindex.getValue();
			String thisindexname = thisindex.getKey();
			ArrayList<StoredElement> arrayfieldsforcompositeindex = new ArrayList<StoredElement>();
			arrayfieldsforcompositeindex.add(parentid);
			for (int i = 0; i < indexextrafields.size(); i++) {
				Field thisfield = indexextrafields.get(i);
				if (!thisfield.getParentObject().equals(this.getParent()))
					throw new RuntimeException("field '" + thisfield.getName() + "' for composite index '"
							+ thisindexname + "' for object '" + this.getParent().getName()
							+ "' does not belong to the object but to '" + thisfield.getParentObject().getName()
							+ "'.");
				arrayfieldsforcompositeindex.add(thisfield.getMainStoredElementForCompositeIndex());
			}
			this.addIndex(new Index(thisindexname, arrayfieldsforcompositeindex, false));

		}

		logger.fine("Parent object for link = " + parentobjectforlink.getName());
		if (parentobjectforlink.getPropertyByName("NAMED") != null) {

			Named namedproperty = (Named) parentobjectforlink.getPropertyByName("NAMED");

			ExternalElement refnameelement = new ExternalElement(this, parentobjectforlink, namedproperty, false,
					(StoredElement) namedproperty.getElements()[0]);
			// note select array on 0 is not clean. Should be replaced by a lookup on name.
			this.addElement(refnameelement);
		}
		if (parentobjectforlink.getPropertyByName("NUMBERED") != null) {

			Numbered numberedproperty = (Numbered) parentobjectforlink.getPropertyByName("NUMBERED");

			ExternalElement refnumberelement = new ExternalElement(this, parentobjectforlink, numberedproperty, false,
					(StoredElement) numberedproperty.getElements()[0]);
			// note select array on 0 is not clean. Should be replaced by a lookup on name.
			this.addElement(refnumberelement);
		}

		if (parentobjectforlink.getPropertyByName("LOCATED") != null) {
			Located located = (Located) parentobjectforlink.getPropertyByName("LOCATED");
			ExternalElement reflocationelement = new ExternalElement(this, parentobjectforlink, located, false,
					(StoredElement) located.getElements()[0]);

			// note select array on 0 is not clean. Should be replaced by a lookup on name.
			this.addElement(reflocationelement);

		}

		for (int i = 0; i < this.getElements().length; i++) {
			logger.fine("Element in property " + this.getElements()[i].getName());
		}
		ArgumentContent parentidargument = new ObjectIdArgument(this.getName() + "ID", parentobjectforlink);
		parentidargument.setOptional(true);
		this.addContextForDataCreation(parentidargument);
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		if (uniqueidentified == null)
			throw new RuntimeException("linkobject property needs the object to have property uniqueidentified");
		this.addDependentProperty(uniqueidentified);
		this.addDisplayProfileForProperty(new DisplayProfile("HIDE" + this.getName()));
		StoredObject storedobject = (StoredObject) parent.getPropertyByName("STOREDOBJECT");
		MethodAdditionalProcessing defaultparentprocessing = new MethodAdditionalProcessing(true,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(defaultparentprocessing);
		for (int i = 0; i < this.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<LinkedToParent<E>> businessrule = this.getBusinessRule(i);
			if (businessrule instanceof LinkedToDefaultParent) {
				logger.fine("---------------->>>> Business rule Linked to Default Parent");
				if (!this.getParentObjectForLink().hasNumbered())
					throw new RuntimeException("Linked to Default Parent cannot be created as '"
							+ this.getParentObjectForLink().getName() + "' is not numbered");
			} else {
				logger.fine("---------------->>>> Business rule not Linked to Default Parent");
			}
		}
	}

	/**
	 * @return the paret object for the linked to parent property
	 */
	public E getParentObjectForLink() {
		return this.parentobjectforlink;
	}

	/**
	 * @return true if the child object is a sub-object, meaning it will be part of
	 *         the parent object data. E.g. changes the update log of the parent
	 *         object
	 */
	public boolean isSubobject() {
		for (int i = 0; i < this.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<?> businessrule = this.getBusinessRule(i);
			if (businessrule instanceof SubObject)
				return true;
		}
		return false;
	}

	/**
	 * adds an action that will display as a button in the object page
	 * 
	 * @param action an action with a unique input argument being the parent data
	 *               object id
	 */
	public void addActionOnParentId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException("you can add an action on parent id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != parentobjectforlink) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ parentobjectforlink.getOwnermodule().getName() + "/" + parentobjectforlink.getName());
		}
		actionsonparentid.add(action);
	}

	/**
	 * This method will create in the database a composite index for search
	 * including as first field the parent id, and the specified fields in order
	 * 
	 * @param indexname
	 * @param fieldstoask fields to add in the composite index
	 */
	public void addCompositeIndex(String indexname, Field[] fieldstoask) {
		if (fieldstoask == null)
			throw new RuntimeException("List of fields is null");
		if (fieldstoask.length == 0)
			throw new RuntimeException("List of fields has zero elements");
		if (indexname.length() > 10)
			throw new RuntimeException("index name should be of maximum 10 characters, indexname = " + indexname);
		String cleanindexname = Named.cleanName(indexname);
		if (this.compositeindexlist.containsKey(cleanindexname))
			throw new RuntimeException("Index with name = " + cleanindexname + " already exists.");
		ArrayList<Field> fieldlist = new ArrayList<Field>();
		for (int i = 0; i < fieldstoask.length; i++)
			fieldlist.add(fieldstoask[i]);
		this.compositeindexlist.put(cleanindexname, fieldlist);
	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[1];
		returnvalues[0] = ".setparentwithoutupdatefor" + this.getInstancename().toLowerCase() + "("
				+ StringFormatter.formatForAttribute(this.getName()) + "id);";
		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {
		if (isSubobject())
			if (this.parentobjectforlink.getPropertyByName("ITERATED") != null) {
				parent.addProperty(new IteratedSubobject(this));
			}

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return new String[] {
				"		deepcopy." + this.getName().toLowerCase() + ".setparentwithoutupdate(deepcopy,this.get"
						+ StringFormatter.formatForJavaClass(this.getName().toLowerCase()) + "id());" };
	}

	@Override
	public void writeAdditionalDefinition(SourceGenerator sg) throws IOException {
		Iterator<Entry<String, ArrayList<Field>>> iterator = compositeindexlist.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, ArrayList<Field>> entry = iterator.next();
			StringBuffer compositeindexdefinition = new StringBuffer();
			compositeindexdefinition.append("		" + StringFormatter.formatForAttribute(this.getName())
					+ ".setCompositeIndex(\"" + entry.getKey() + "\",new DataObjectFieldDefinition[]{");
			for (int i = 0; i < entry.getValue().size(); i++) {
				Field thisfield = entry.getValue().get(i);
				if (i > 0)
					compositeindexdefinition.append(',');
				compositeindexdefinition.append(thisfield.getName().toLowerCase());
				compositeindexdefinition.append("field");

			}
			compositeindexdefinition.append("});");
			sg.wl(compositeindexdefinition.toString());
		}
	}

	@Override
	public String getJavaType() {
		return "#NOTIMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.formula.LinkedToParentNavigator;");
		if (parentobjectforlink.getPropertyByName("LOCATED") != null) {
			sg.wl("import org.openlowcode.module.system.data.Domain;");
		}
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(parentobjectforlink);
		return dependencies;
	}

	public MultiDimensionChild<E> getMultiDimensionChild() {
		return this.multiDimensionchild;
	}
	
	public void setMultiDimensionChild(MultiDimensionChild<E> multiDimensionchild) {
		this.multiDimensionchild = multiDimensionchild;
		
	}

}
