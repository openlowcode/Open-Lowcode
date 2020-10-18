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

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.TwoObjectsArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * A Companion is a secondary data object linked to a main data object. It
 * provides extra data for some types of the main data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public class Companion
		extends
		Property<Companion> {

	private DataObjectDefinition maintypedobject;
	private ChoiceValue[] types;
	private Typed mainobjecttypedproperty;
	private HasId hasid;

	public Companion(DataObjectDefinition maintypedobject, ChoiceValue[] types) {
		super("COMPANION");
		this.maintypedobject = maintypedobject;
		this.types = types;

	}

	@Override
	public void controlAfterParentDefinition() {

		mainobjecttypedproperty = (Typed) maintypedobject.getPropertyByName("TYPED");
		if (mainobjecttypedproperty == null)
			throw new RuntimeException("Main object " + maintypedobject.getName()
					+ " should have the Typed property added before this statement.");
		mainobjecttypedproperty.addCompanionObject(this.getParent(), types);

		// READ THE TYPED OBJECTS
		DataAccessMethod readtyped = new DataAccessMethod("READTYPED", new TwoObjectsArgument("TYPED",
				new ObjectArgument("MAIN", maintypedobject), new ObjectArgument("COMPANION", this.getParent())), false,
				false);
		readtyped.addInputArgument(new MethodArgument("OBJECTID", new ObjectIdArgument("OBJECT", maintypedobject)));
		this.addDataAccessMethod(readtyped);
		// UPDATE THE TYPED OBJECT
		DataAccessMethod updatetyped = new DataAccessMethod("UPDATETYPED", null, false, false);
		updatetyped.addInputArgument(
				new MethodArgument("THISCOMPANION", new ObjectArgument("COMPANION", this.getParent())));
		updatetyped
				.addInputArgument(new MethodArgument("MAINOBJECT", new ObjectArgument("MAINOBJECT", maintypedobject)));

		this.addDataAccessMethod(updatetyped);
		// CREATE A TYPED OBJECT
		DataAccessMethod createtyped = new DataAccessMethod("CREATETYPED", null, false, false);
		createtyped.addInputArgument(
				new MethodArgument("THISCOMPANION", new ObjectArgument("COMPANION", this.getParent())));
		createtyped
				.addInputArgument(new MethodArgument("MAINOBJECT", new ObjectArgument("MAINOBJECT", maintypedobject)));
		createtyped.addInputArgument(
				new MethodArgument("TYPE", new ChoiceArgument("TYPE", mainobjecttypedproperty.getTypes())));
		this.addDataAccessMethod(createtyped);

		// INSERT AFTER TYPED OBJECT CREATION
		DataAccessMethod insertcompanion = new DataAccessMethod("INSERTCOMPANION", null, false, false);
		insertcompanion.addInputArgument(
				new MethodArgument("THISCOMPANION", new ObjectArgument("COMPANION", this.getParent())));
		insertcompanion
				.addInputArgument(new MethodArgument("MAINOBJECT", new ObjectArgument("MAINOBJECT", maintypedobject)));
		this.addDataAccessMethod(insertcompanion);

		// put the main typed object as related to this property
		this.addChoiceCategoryHelper("TYPE", ((Typed) maintypedobject.getPropertyByName("TYPED")).getTypes());
		this.addPropertyGenerics(
				new PropertyGenerics("MAINTYPEDOBJECT", maintypedobject, maintypedobject.getPropertyByName("TYPED")));
		hasid = (HasId) (this.getParent().getPropertyByName("HASID"));
		this.addDependentProperty(hasid);
	}

	@Override
	public String[] getPropertyInitMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		// TODO Auto-generated method stub
		return null;
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
		SimpleChoiceCategory typechoice = ((Typed) maintypedobject.getPropertyByName("TYPED")).getTypes();
		sg.wl("import " + typechoice.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(typechoice.getName()) + "ChoiceDefinition;");

	}

	/**
	 * @return the list of menus
	 */
	public ArrayList<String> getSpecificMenuList() {
		return hasid.getSpecificMenuList();
	}

	/**
	 * get actions assigned on a specific menu
	 * 
	 * @param specific menu
	 * @return actions
	 */
	public ArrayList<DynamicActionDefinition> getActionsOnSpecificMenu(String specifictabname) {
		return hasid.getActionsOnSpecificMenu(specifictabname);
	}

	/**
	 * @return the list of actions on object id to be added to button band of the
	 *         object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectId() {
		return hasid.getActionListonObjectId();
	}

	/**
	 * @return the list of actions on object id to be added to the manage menu of
	 *         the object page
	 */
	public NamedList<DynamicActionDefinition> getActionListonObjectIdForManageMenu() {
		return hasid.getActionListonObjectIdForManageMenu();
	}

	/**
	 * @return the list of menu comment to print
	 */
	public ArrayList<String> getMenuCommentListOnActionForManageTab() {
		return hasid.getMenuCommentListOnActionForManageTab();
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
		hasid.addActionOnObjectId(action, specialmenuname);
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
		hasid.addActionOnObjectId(action, actioninmanagetab);
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
		hasid.addActionOnObjectIdOnManageMenu(action, comment);
	}

}
