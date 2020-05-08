/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import java.io.IOException;

import org.openlowcode.design.access.ActionGroup;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * An advanced feature can be set by the designer, and will generate specific
 * pages and actions.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class AdvancedDesignFeature
		extends
		Named
		implements
		ActionGroup {

	private String menuname = null;

	/**
	 * @param menuname for advanced design features that are launched from an object
	 *                page, the action is put in a menu of the following name
	 */
	public void setTabForObject(String menuname) {
		this.menuname = menuname;
	}

	/**
	 * @return
	 */
	public String getMenuForObject() {
		return this.menuname;
	}

	private Module parentmodule;

	/**
	 * creates an advanced design feature with the provided name (should be unique
	 * amongst advanced design features for the module
	 * 
	 * @param name unique name for the module
	 */
	public AdvancedDesignFeature(String name) {
		super(name);

	}

	/**
	 * set the parent module
	 * 
	 * @param module parent module
	 */
	public void setParentModule(Module module) {
		this.parentmodule = module;
	}

	/**
	 * This method will generate the signature (interface) of actions and pages for
	 * the advanced design feature
	 */
	public abstract void generateActionsAndPages();

	/**
	 * This method will generate the content of actions and pages for the advanced
	 * design feature
	 * 
	 * @param actionfolder folder to create actions into
	 * @param pagefolder   folder to create pages into
	 * @param author       author of the module
	 * @param version      version of the module
	 * @throws IOException if anything bad happens while writing the module
	 */
	public abstract void generateActionsAndPagesToFile(
			String actionfolder,
			String pagefolder,
			String author,
			String version) throws IOException;

	/**
	 * @return the parent module
	 */
	public Module getParentModule() {
		return this.parentmodule;
	}
}
