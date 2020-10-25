/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property is a split of the original UniqueIdentified property to store
 * only the fact that the object has a unique id that can be used for links and
 * other references
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 2.0
 *
 */
public class HasId
		extends
		Property<HasId> {
	private NamedList<DynamicActionDefinition> actionsonobjectid;
	private NamedList<DynamicActionDefinition> actionsonobjectidonmanagemenu;
	private ArrayList<String> menucommentinmanagetabs;
	private HashMap<String, ArrayList<DynamicActionDefinition>> actionsonspecificmenu;
	private ArrayList<String> specificmenus;

	public HasId() {
		super("HASID");
		this.actionsonobjectid = new NamedList<DynamicActionDefinition>();
		this.actionsonobjectidonmanagemenu = new NamedList<DynamicActionDefinition>();
		this.menucommentinmanagetabs = new ArrayList<String>();
		this.actionsonspecificmenu = new HashMap<String, ArrayList<DynamicActionDefinition>>();
		this.specificmenus = new ArrayList<String>();

	}

	@Override
	public void controlAfterParentDefinition() {
		StoredElement id = new ObjectIdStoredElement("ID", parent);
		this.addElement(id, "Id", "technical identification", Property.FIELDDISPLAY_NORMAL, -50, 25);
		this.addIndex(new Index("ID", id, true));

		StoredElement deleted = new StringStoredElement("DELETED", 3);
		this.addElement(deleted, "Deleted", "Deleted element", Property.FIELDDISPLAY_BOTTOMNOTES, -900, 15);
		this.addIndex(new Index("DELETED", deleted, false));

		
		
		DataAccessMethod read = new DataAccessMethod("READONE", new ObjectArgument("OBJECT", parent), false);
		read.addInputArgument(new MethodArgument("ID", new ObjectIdArgument("ID", parent)));
		this.addDataAccessMethod(read);

		DataAccessMethod readseveral = new DataAccessMethod("READSEVERAL",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		readseveral.addInputArgument(new MethodArgument("ID", new ArrayArgument(new ObjectIdArgument("ID", parent))));
		this.addDataAccessMethod(readseveral);
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
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	@Override
	public void setFinalSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}


	/**
	 * @return the list of menus
	 */
	public ArrayList<String> getSpecificMenuList() {
		return this.specificmenus;
	}

	/**
	 * get actions assigned on a specific menu
	 * @param specific menu
	 * @return actions 
	 */
	public ArrayList<DynamicActionDefinition> getActionsOnSpecificMenu(String specifictabname) {
		return this.actionsonspecificmenu.get(specifictabname);
	}

	/**
	 * @return the list of actions on object id to be added to button band of the
	 *         object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectId() {
		return actionsonobjectid;
	}

	/**
	 * @return the list of actions on object id to be added to the manage menu of the
	 *         object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectIdForManageMenu() {
		return actionsonobjectidonmanagemenu;
	}
	
	/**
	 * @return the list of menu comment to print
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<String> getMenuCommentListOnActionForManageTab() {
		return (ArrayList<String>)(this.menucommentinmanagetabs.clone());
	}

	
	
	/**
	 * adds action in the main button band of the object. The action should have a
	 * single input attribute being the data object id
	 * 
	 * @param action adds an action on the object id. The action should have a
	 *               single input attribute being the data object id
	 */
	public void addActionOnObjectId(DynamicActionDefinition action) {
		addActionOnObjectId(action, false);
	}

	/**
	 * adds action in the main button band of the object. The action should have a
	 * single input attribute being the data object id
	 * 
	 * @param action          adds an action on the object id. The action should
	 *                        have a single input attribute being the data object id
	 * @param specialmenuname special menu for the action
	 */
	public void addActionOnObjectId(DynamicActionDefinition action, String specialmenuname) {
		validateActionOnObjectId(action);
		ArrayList<DynamicActionDefinition> actionsforspecialmenu = this.actionsonspecificmenu.get(specialmenuname);
		if (actionsforspecialmenu == null) {
			actionsforspecialmenu = new ArrayList<DynamicActionDefinition>();
			this.actionsonspecificmenu.put(specialmenuname, actionsforspecialmenu);
			this.specificmenus.add(specialmenuname);
		}
		actionsforspecialmenu.add(action);
	}

	private void validateActionOnObjectId(DynamicActionDefinition action) {
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
	}

	/**
	 * adds an action on the object, either in the main button band, or in the
	 * manage tab
	 * 
	 * @param action            adds an action on the object id. The action should
	 *                          have a single input attribute being the data object
	 *                          id
	 * @param actioninmanagetab if true, action is put in manage tabs, if false,
	 *                          action is directly in the action button
	 */
	public void addActionOnObjectId(DynamicActionDefinition action, boolean actioninmanagetab) {
		validateActionOnObjectId(action);

		if (actioninmanagetab) {
			this.actionsonobjectidonmanagemenu.add(action);
			this.menucommentinmanagetabs.add(null);
		} else {
			actionsonobjectid.add(action);

		}
	}

	/**
	 * adds an action on the object, either in the main button band, or in the
	 * manage tab
	 * 
	 * @param action  adds an action on the object id. The action should have a
	 *                single input attribute being the data object id
	 * @param comment a comment to add before the content in manage menu
	 */
	public void addActionOnObjectIdOnManageMenu(DynamicActionDefinition action, String comment) {
		this.actionsonobjectidonmanagemenu.add(action);
		this.menucommentinmanagetabs.add(comment);
	}
	
	
}
