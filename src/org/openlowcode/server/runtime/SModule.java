
/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.util.Date;

import org.openlowcode.server.action.ActionExecution;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageAddon;
import org.openlowcode.server.graphic.widget.SMenu;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A Smodule is an application deployed on the server. This application is made
 * of a number of artifacts, the main ones being actions (ActionExecution) and
 * pages (SPage).<b>Subclasses of this class are automatically generated</b>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings("rawtypes")
public abstract class SModule extends Named {

	private NamedList<SPage> pages;
	private NamedList<DataObjectDefinition> moduleobjects;
	private String code;
	private String emailmessage = null;
	private String moduleversion;
	private String frameworkversion;
	private boolean frameworkfinalversion;
	private Date generationdate;
	private String label;

	/**
	 * @return the front page message to show on the module home page
	 */
	public abstract String getFrontPageMessage();

	/**
	 * @return the code of the module
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * @return the label of the module (plain text in the main language)
	 */
	public String getLabel() {
		return this.label;
	}

	/**
	 * @param name                  unique name of the module (for the server). It
	 *                              is recommended enterprises set-up a naming
	 *                              convention to ensure uniqueness
	 * @param code                  unique code of the module (for the server). It
	 *                              is a two letters (3 letters at most) unique code
	 *                              for the module. It should be kept short as it
	 *                              will be added to the name of databse artifacts
	 *                              (table, indexes...), and they typically have a
	 *                              limited number of characters in the name
	 * @param label                 a plain language short label of the module (in
	 *                              the main language)
	 * @param moduleversion         version of the module version of the module
	 * @param frameworkversion      the version of OpenLowcode framework this module
	 *                              was compiled with
	 * @param frameworkfinalversion was it compiled with a released version of the
	 *                              framework
	 * @param generationdate        date the module was generated.
	 */
	public SModule(String name, String code, String label, String moduleversion, String frameworkversion,
			boolean frameworkfinalversion, Date generationdate) {
		super(name);
		this.label = label;
		this.moduleversion = moduleversion;
		this.frameworkversion = frameworkversion;
		this.frameworkfinalversion = frameworkfinalversion;
		this.generationdate = generationdate;
		this.code = code;
		pages = new NamedList<SPage>();
		moduleobjects = new NamedList<DataObjectDefinition>();
		this.emailmessage = null;
	}

	/**
	 * @return the module version
	 */
	public String getModuleversion() {
		return moduleversion;
	}

	/**
	 * @return the framework version the module was generated with
	 */
	public String getFrameworkversion() {
		return frameworkversion;
	}

	/**
	 * @return if the framework the module was generated with is final
	 */
	public boolean isFrameworkfinalversion() {
		return frameworkfinalversion;
	}

	/**
	 * @return generation time
	 */
	public Date getGenerationdate() {
		return generationdate;
	}

	/**
	 * @return an e-mail message to put in all mails sent with this module
	 */
	public String getEmailMessage() {
		return emailmessage;
	}

	/**
	 * @param emailmessage a default e-mail message to put in all e-mails sent with
	 *                     this module
	 */
	protected void setEmailMessage(String emailmessage) {
		this.emailmessage = emailmessage;
	}

	/**
	 * This method allows to get an action from the name, typically when a client
	 * requests a specific action
	 * 
	 * @param name name of the action
	 * @return an action
	 */
	public abstract ActionExecution getAction(String name);

	/**
	 * This methods allows to generically call show page on an object.
	 * 
	 * @return the page generated
	 * 
	 */
	public abstract SPage getShowPageBasedOnObjectId(DataObjectId genericid);

	/**
	 * @param genericid a data object of unprecised type
	 * @return the data object
	 */
	public abstract DataObject getDataObjectBasedOnGenericId(DataObjectId genericid);

	/**
	 * adds a page to this module
	 * 
	 * @param page the page to add
	 */
	protected void addPage(SPage page) {
		this.pages.add(page);
	}

	/**
	 * this step should generate definition of all objects
	 * 
	 */
	public abstract void updateDataModelStep1();

	/**
	 * this step should generate fields for all objects
	 * 
	 */
	public abstract void updateDataModelStep2();

	/**
	 * this step should generate calculated fields for all objects
	 * 
	 */
	public abstract void updateDataModelStep3();

	/**
	 * this step should update data module for all objects
	 * 
	 */
	public abstract void updateDataModelStep4();

	/**
	 * generates comments
	 * 
	 */
	public abstract void updateDataModelStep5();

	/**
	 * launches all the migrators of the module after checking if they have been
	 * executed or not
	 * 
	 */
	public abstract void DataUpdateStep1();

	/**
	 * @return the action defining the default page. If no action is defined for the
	 *         package the default page of system module is called
	 */
	public SPage getDefaultPage() {
		ActionExecution defaultactionformodule = getActionForDefaultPage();
		if (defaultactionformodule == null)
			defaultactionformodule = OLcServer.getServer().getModuleByName("SYSTEMMODULE").getActionForDefaultPage();

		return defaultactionformodule.executeActionFromGUI(new SActionData());
	}

	/**
	 * 
	 * @return null if there is a default action for the page, or the default page
	 *         if defined
	 */
	protected abstract ActionExecution getActionForDefaultPage();

	/**
	 * This method allows a module to generate basic data for the module once the
	 * objects have been created. This method will be run each time the application
	 * is launched, so should be designed to check first if data exists before
	 * creating it
	 * 
	 */
	public abstract void initiateData();

	/**
	 * provides the add-on for the module. If no add-on defined, null shall be
	 * brought back
	 * 
	 * @return the addon for the module
	 * 
	 */
	public abstract SPageAddon getPageAddonForModule();

	public interface ModuleDataInit {
		public void initiateData();
	}

	/**
	 * adds a DataObjectDefinition to this module
	 * 
	 * @param objectdefinition the object to add
	 */
	public void addObjectDefinition(DataObjectDefinition objectdefinition) {
		moduleobjects.add(objectdefinition);
	}

	/**
	 * gets the DataObjectDefinition
	 * 
	 * @param objectname the name of object
	 * @return the object if exists, null if does not exist
	 */
	public DataObjectDefinition getObjectDefinition(String objectname) {
		return moduleobjects.lookupOnName(objectname);
	}

	/**
	 * defines the menu for this module
	 * 
	 * @param parentaddonpage the page to generate the menu for
	 * @return the SMenu widget
	 */
	public abstract SMenu getModuleMenu(SPage parentaddonpage);

}
