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
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property allows unique storage and retrieval independently from the
 * database by giving it a unique persistent technical id. It is necessary for
 * most other properties.
 * 
 * * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.StoredObject}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UniqueIdentified
		extends
		Property<UniqueIdentified> {

	private StoredObject storedobject;
	private HasId hasid;

	/**
	 * @return get the related property stored object
	 */
	public StoredObject getStoredObject() {
		return this.storedobject;
	}

	/**
	 * creates the unique identified property
	 */
	public UniqueIdentified() {
		super("UNIQUEIDENTIFIED");

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	@Override
	public void controlAfterParentDefinition() {
		// Manage StoredObject
		this.storedobject = (StoredObject) parent.getPropertyByName("STOREDOBJECT");
		if (this.storedobject == null) {
			this.storedobject = new StoredObject();
			this.addPropertyOnSameObject(storedobject);
		}
			
		this.addDependentProperty(storedobject);
		// Manage HasId
		this.hasid = (HasId) parent.getPropertyByName("HASID");
		if (this.hasid == null) {
			this.hasid = new HasId();
			this.addPropertyOnSameObject(hasid);
		}
			
		this.addDependentProperty(hasid);
		
		MethodAdditionalProcessing insertidgeneration = new MethodAdditionalProcessing(true,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(insertidgeneration);


		// get Id

		// DELETE
		DataAccessMethod delete = new DataAccessMethod("DELETE", null, false, true);
		delete.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(delete);

		// UPDATE
		DataAccessMethod update = new DataAccessMethod("UPDATE", null, false, true);
		update.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(update);

		// REFRESH
		DataAccessMethod refresh = new DataAccessMethod("REFRESH", null, false);
		refresh.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(refresh);

		// Field


	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		return dependencies;
	}

	

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}
	/**
	 * @return the list of menus
	 */
	public ArrayList<String> getSpecificMenuList() {
		return hasid.getSpecificMenuList();
	}
	/**
	 * get actions assigned on a specific menu
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
	 * @return the list of actions on object id to be added to the manage menu of the
	 *         object page
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

	public void addActionOnObjectId(DynamicActionDefinition action, String specialmenuname) {
		hasid.addActionOnObjectId(action,specialmenuname);
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
		hasid.addActionOnObjectId(action,actioninmanagetab);
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
		hasid.addActionOnObjectIdOnManageMenu(action,comment);
	}
	
	
}
