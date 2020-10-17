/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.design.access.ActionGroup;
import org.openlowcode.design.access.ModuleDomain;
import org.openlowcode.design.access.ModuleDomainAuthority;
import org.openlowcode.design.access.ModuleDomainGroup;
import org.openlowcode.design.access.Privilege;
import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.design.access.UnconditionalPrivilege;
import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.action.StaticActionDefinition;
import org.openlowcode.design.advanced.AdvancedDesignFeature;
import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DataObjectDefinitionCreatePageToFile;
import org.openlowcode.design.data.DataObjectDefinitionDeleteAndUpdate;
import org.openlowcode.design.data.DataObjectDefinitionFileActions;
import org.openlowcode.design.data.DataObjectDefinitionOtherActions;
import org.openlowcode.design.data.DataObjectDefinitionSearchPagesAndActions;
import org.openlowcode.design.data.DataObjectDefinitionShowAction;
import org.openlowcode.design.data.DataObjectDefinitionShowHistoryPage;
import org.openlowcode.design.data.DataObjectDefinitionShowIterationPage;
import org.openlowcode.design.data.DataObjectDefinitionShowPage;
import org.openlowcode.design.data.DataObjectDefinitionUpdatePage;
import org.openlowcode.design.data.DataObjectDefinitionWorkflowAndSchedule;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.SequenceDefinition;
import org.openlowcode.design.data.migrator.DataMigrator;
import org.openlowcode.design.data.properties.basic.AutolinkObject;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameParent;
import org.openlowcode.design.data.properties.basic.FileContent;
import org.openlowcode.design.data.properties.basic.HasMultiDimensionalChild;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkObjectToMaster;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.PrintOut;
import org.openlowcode.design.data.properties.basic.Schedule;
import org.openlowcode.design.data.properties.basic.SubObject;
import org.openlowcode.design.data.properties.basic.Trigger;
import org.openlowcode.design.data.properties.basic.Typed;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.roles.Group;
import org.openlowcode.design.module.roles.User;
import org.openlowcode.design.pages.AddonPageDefinition;
import org.openlowcode.design.pages.PageDefinition;
import org.openlowcode.design.utility.MultiFieldConstraint;
import org.openlowcode.design.utility.SystemAttributeInit;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.OLcVersionGenerator;
import org.openlowcode.module.system.design.SystemModule;
import org.openlowcode.tools.trace.ConsoleFormatter;

/**
 * A module is the basic unit of an Open Lowcode application. It is made of a
 * group of data objects (
 * {@link org.openlowcode.design.data.DataObjectDefinition}) tightly linked
 * together.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Module
		extends
		Named {
	private String code;
	private ArrayList<Privilege> privileges;
	private NamedList<PageDefinition> pages;
	private NamedList<ActionDefinition> actions;
	private NamedList<StaticActionDefinition> menuactions;
	private NamedList<DataObjectDefinition> objects;
	private NamedList<MultiFieldConstraint> constraints;
	private NamedList<SystemAttributeInit> attributes;
	private NamedList<ChoiceCategory> choices;
	private NamedList<SequenceDefinition> sequences;
	private NamedList<DataMigrator> migrators;

	private NamedList<TotalAuthority> authorities;
	private NamedList<ModuleDomainAuthority> moduledomainauthorities;
	private NamedList<ModuleDomainGroup> moduledomaingroups;
	private NamedList<AdvancedDesignFeature> advanceddesignfeatures;
	private NamedList<Group> groups;
	private ArrayList<User> users;
	private NamedList<ModuleDomain> moduledomains;
	private String path;
	private String author;
	private AddonPageDefinition defaultaddon;
	private static Logger logger = Logger.getLogger("");
	private StaticActionDefinition actionfordefaultpage;
	private static SystemModule systemmodule = null;
	private String emailmessage;
	private String versionid;
	private String frontpagemessage;
	private String label;
	private boolean secure = false;

	/**
	 * @return the number of sequences for this module
	 */
	private int getSequenceNumber() {
		return sequences.getSize();
	}

	/**
	 * get the definition of the sequence at the given index
	 * 
	 * @param i index between 0 (included) and getSequenceNumber (excluded)
	 * @return
	 */
	public SequenceDefinition getSequence(int i) {
		return sequences.get(i);
	}

	/**
	 * stores whether a data initialization routine exists for this module
	 */
	private boolean hasdatainitialization;

	/**
	 * allows to specify the action launching a default page for the application. If
	 * nothing is set, the default page is the page showing the user task.
	 * 
	 * @param actionfordefaultpage a static action to launch the default page
	 */
	public void addAsDefaultPageAction(StaticActionDefinition actionfordefaultpage) {
		this.actionfordefaultpage = actionfordefaultpage;
		this.addAction(actionfordefaultpage);
	}

	/**
	 * registers a choice category with the module. A choice category needs to be
	 * registered before it is used on an object.
	 * 
	 * @param choice the choicecategory to add
	 */
	public void addChoiceCategory(ChoiceCategory choice) {
		this.choices.add(choice);
		choice.setParentModule(this);
	}

	/**
	 * returns the action for the given name
	 * 
	 * @param name name of the action
	 * @return the action for the given name if it exists
	 */
	public ActionDefinition lookupActionDefinition(String name) {
		return this.actions.lookupOnName(name);
	}

	/**
	 * The sequence is a counter incremented in the database, typically used to
	 * generate numbers for objects.
	 * 
	 * @param sequence sequence to add in the module
	 */
	public void addSequenceDefinition(SequenceDefinition sequence) {
		this.sequences.add(sequence);
		sequence.setModule(this);
	}

	/**
	 * validate the product code is valid (2 digits, starting by upper case letter,
	 * second digit can be number or upper case letter)
	 * 
	 * @param code code of the module
	 */
	public void validateProductCode(String code) {
		if (code.length() != 2)
			throw new RuntimeException("code " + code + " should be 2 digits for module " + this.getName() + ".");
		if (code.charAt(0) > 'Z')
			throw new RuntimeException(
					"code " + code + " should start with upper case latine letter for module " + this.getName());
		if (code.charAt(0) < 'A')
			throw new RuntimeException(
					"code " + code + " should start with upper case latine letter for module " + this.getName());

		char secondchar = code.charAt(1);
		boolean valid = false;
		if ((secondchar <= 'Z') && (secondchar >= 'A'))
			valid = true;
		if ((secondchar <= '9') && (secondchar >= '0'))
			valid = true;
		if (!valid)
			throw new RuntimeException("code " + code
					+ " second character should be latine upper case or digit for module " + this.getName() + ".");
		if (!this.getClass().getName().equals("org.openlowcode.module.system.design.SystemModule")) {
			if (this.code.equals("S0"))
				throw new RuntimeException("Code S0 is reserved for System Module, module " + this.getName()
						+ " should have another code");
		}
	}

	/**
	 * adds a system attribute to this module
	 * 
	 * @param thisattribute
	 */
	public void addSystemAttribute(SystemAttributeInit thisattribute) {
		this.attributes.add(thisattribute);
	}

	/**
	 * registers with the module a multi-field constraint for an object. This is
	 * special logic specifying valid combinations of attributes.<br>
	 * <br>
	 * The multifield constraint needs to be implemented by the developer after
	 * generation
	 * 
	 * @param constraint the multifield constraint
	 */
	public void addMultiFieldConstraint(MultiFieldConstraint constraint) {
		this.constraints.add(constraint);
	}

	/**
	 * @param user adds a hardcoded user to the module
	 */
	public void addUser(User user) {
		this.users.add(user);
	}

	/**
	 * @param authority adds a total authority for the module.
	 */
	public void addAuthority(TotalAuthority authority) {
		this.authorities.add(authority);
		authority.setParentModule(this);
	}

	/**
	 * @param group adds a group to the module
	 */
	public void addGroup(Group group) {
		this.groups.add(group);
	}

	/**
	 * allows to define that there should be a data initialization class defined by
	 * the user. By default, there is no data initialization class
	 * 
	 * @param hasdatainitialization true if the user should define a class to init
	 *                              data
	 */
	public void setDataInitializationFlag(boolean hasdatainitialization) {
		this.hasdatainitialization = hasdatainitialization;
	}

	/**
	 * @return true if the module has a data initialization
	 */
	public boolean hasDataInitialization() {
		return this.hasdatainitialization;
	}

	/**
	 * @return the default page add-on. This is added on all pages. By default,
	 *         there is a page add-on with a menu bar having one menu per module
	 */
	public AddonPageDefinition getDefaultaddon() {
		return defaultaddon;
	}

	/**
	 * @return get the author of the module
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * adds a data migrator to the module. Data Migrators are executed once
	 * 
	 * @param datamigrator data migrator
	 */
	public void addMigrator(DataMigrator datamigrator) {
		this.migrators.add(datamigrator);
	}

	/**
	 * Allows to specify an alternative to the default Open Lowcode add-on page. The
	 * default Open Lowcode add-on page has a menu with one menu per module.
	 * 
	 * @param defaultaddon an addon page
	 */
	public void setDefaultAddonPageDefinition(AddonPageDefinition defaultaddon) {
		this.defaultaddon = defaultaddon;
	}

	/**
	 * @return the number of pages of the module
	 */
	public int getPageNumber() {
		return pages.getSize();
	}

	/**
	 * @return the number of choices of the module
	 */
	public int getChoiceNumber() {
		return choices.getSize();
	}

	/**
	 * gets the choice category at the given index
	 * 
	 * @param i a number between 0 (included) and getChoiceNumber (excluded)
	 * @return the given choice category
	 */
	public ChoiceCategory getChoice(int i) {
		return choices.get(i);
	}

	/**
	 * gets the page at the given index in the module
	 * 
	 * @param i a number bebtween 0 (included) and getPageNumber (excluded)
	 * @return the page at the given index
	 */
	public PageDefinition getPage(int i) {
		return pages.get(i);

	}

	/**
	 * @return the version id of the module
	 */
	public String getVersionid() {
		return versionid;
	}

	/**
	 * sets a specific e-mail message for workflow notifications
	 * 
	 * @param emailmessage e-mail message for workflow notifications
	 */
	public void setEmailMessage(String emailmessage) {
		this.emailmessage = emailmessage;
	}

	/**
	 * set the module as secure. If an OTP is setup on the server, OTP secure login
	 * will be required to access any action in the module
	 * 
	 * @since 1.10
	 */
	public void setSecure() {
		this.secure = true;
	}

	/**
	 * Creates a new module in the application
	 * 
	 * @param name             technical name of the module (e.g. 'BUGMGT' ), only
	 *                         upper-case letters and numbers (do not start by
	 *                         number)
	 * @param code             a 2 letters and digit code that should be unique for
	 *                         the server
	 * @param label            the plain English name of the module (e.g. 'Bug
	 *                         Management')
	 * @param path             a valid java path (e.g. 'com.maboite.monapp')
	 * @param author           the name of author that will be put in auto-generated
	 *                         code
	 * @param versionid        the version of application that will be put in
	 *                         auto-generated code
	 * @param frontpagemessage allows to set a specific message on the default front
	 *                         page of the application
	 */
	public Module(
			String name,
			String code,
			String label,
			String path,
			String author,
			String versionid,
			String frontpagemessage) {
		super(name);
		this.code = code;
		validateProductCode(code);
		this.label = label;
		this.path = path;
		this.versionid = versionid;
		this.author = author;
		this.emailmessage = null;
		pages = new NamedList<PageDefinition>();
		actions = new NamedList<ActionDefinition>();
		menuactions = new NamedList<StaticActionDefinition>();
		objects = new NamedList<DataObjectDefinition>();
		this.moduledomains = new NamedList<ModuleDomain>();
		this.moduledomainauthorities = new NamedList<ModuleDomainAuthority>();
		this.moduledomaingroups = new NamedList<ModuleDomainGroup>();
		this.advanceddesignfeatures = new NamedList<AdvancedDesignFeature>();
		this.choices = new NamedList<ChoiceCategory>();
		this.sequences = new NamedList<SequenceDefinition>();
		this.migrators = new NamedList<DataMigrator>();
		this.authorities = new NamedList<TotalAuthority>();
		this.groups = new NamedList<Group>();
		this.users = new ArrayList<User>();
		constraints = new NamedList<MultiFieldConstraint>();
		initStandardAuthoritiesAndGroup();
		this.frontpagemessage = frontpagemessage;
		this.privileges = new ArrayList<Privilege>();
		this.triggerlist = new ArrayList<Trigger>();
		this.attributes = new NamedList<SystemAttributeInit>();
	}

	private TotalAuthority moduleoverlord;
	private Group moduleoverlordgroup;
	private User moduleadmin;
	private int privilegeaddedinmodule = 0;

	/**
	 * Defines a domain for the module. This will allow to define separated access
	 * rights
	 * 
	 * @param thisdomain
	 */
	public void addModuleDomain(ModuleDomain thisdomain) {
		this.moduledomains.add(thisdomain);
	}

	/**
	 * creates a set of groups having similar roles on all the domains of the module
	 * 
	 * @param thismoduledomaingroup the module domain group
	 */
	public void addModuleDomainGroup(ModuleDomainGroup thismoduledomaingroup) {
		this.moduledomaingroups.add(thismoduledomaingroup);
	}

	/**
	 * Define a similar authority for each module domain
	 * 
	 * @param moduledomainauthority
	 */
	public void addModuleDomainAuthority(ModuleDomainAuthority moduledomainauthority) {
		this.moduledomainauthorities.add(moduledomainauthority);
	}

	/**
	 * registers the privilege with the module
	 * 
	 * @param privilege the privilege to add
	 */

	public void addPrivilege(Privilege privilege) {

		privileges.add(privilege);
		logger.finest(" -+ adding privilege " + privilege);
		privilegeaddedinmodule++;
	}

	/**
	 * checks the content of all action groups
	 */
	private void controlPrivileges() {
		for (int i = 0; i < privileges.size(); i++) {
			Privilege privilege = privileges.get(i);
			ActionGroup actiongroup = privilege.getActiongroup();

			ActionDefinition[] actions = actiongroup.getActionsInGroup();
			if (actions != null)
				for (int j = 0; j < actions.length; j++) {
					ActionDefinition action = actions[j];
					if (action == null)
						throw new RuntimeException("Action index " + i + " in privilege " + privilege.getClass()
								+ " is null for action group " + actiongroup.getName());
					if (action.getModule() == null)
						throw new RuntimeException(
								"Action index " + i + ", name = " + action.getName() + " does not have parent module");
					if (!action.getModule().equals(this))
						throw new RuntimeException("Action " + action.getName() + " is not part of module "
								+ this.getName() + " so cannot be added to the policy");
				}
		}
	}

	/**
	 * @return the code of the module
	 */
	public String getCode() {
		return this.code;
	}

	/**
	 * gets all the privileges defined for an action
	 * 
	 * @param thisaction an action of the module
	 * @return all the privileges
	 */
	public Privilege[] getPrivilegesForAction(ActionDefinition thisaction) {
		if (!thisaction.getModule().equals(this))
			throw new RuntimeException("can only be called from action own module, actionname = " + thisaction.getName()
					+ ", actionmodule = " + thisaction.getModule().getName() + " privilege module = " + this.getName());
		ArrayList<Privilege> privilegelist = new ArrayList<Privilege>();
		int hit = 0;
		int miss = 0;
		int privilegecount = 0;
		for (int i = 0; i < privileges.size(); i++) {
			privilegecount++;
			Privilege privilege = privileges.get(i);
			ActionDefinition[] actionsingroup = privilege.getActiongroup().getActionsInGroup();
			if (actionsingroup != null)
				for (int j = 0; j < actionsingroup.length; j++) {
					ActionDefinition actioningroup = actionsingroup[j];

					if (thisaction.equals(actioningroup)) {
						privilegelist.add(privilege);
						hit++;
					} else {
						miss++;
					}
				}
		}
		logger.severe("     -- Check privileges for action " + thisaction.getName()
				+ " hit/miss/privilege/privilegeentered = " + hit + "/" + miss + "/" + privilegecount + "/"
				+ privilegeaddedinmodule);
		return privilegelist.toArray(new Privilege[0]);
	}

	/**
	 * inits the administrator user of the module and the related group and
	 * authority
	 */
	public void initStandardAuthoritiesAndGroup() {
		String modulenameclass = StringFormatter.formatForJavaClass(this.getName());
		String modulevariable = this.getName().toLowerCase();
		moduleoverlord = new TotalAuthority(modulenameclass + "Overlord", this.label + " Overlord",
				"the highest authority for the module " + this.label
						+ ". All actions on the module are available. Warning: Some actions may result in inconsistent data.");
		moduleoverlordgroup = new Group(modulenameclass + "Overlord", moduleoverlord);
		moduleadmin = new User(modulevariable + "admin", modulevariable + "admin", "welcome@openlowcode.com", null,
				"Administrator for " + modulenameclass);
		this.addAuthority(moduleoverlord);
		this.addGroup(moduleoverlordgroup);
		this.addUser(moduleadmin);
	}

	/**
	 * @return the module overlord authority giving privileges to all actions on the
	 *         module
	 */
	public TotalAuthority getModuleOverlord() {
		return this.moduleoverlord;
	}

	/**
	 * @return the number of data objects in the module
	 */
	public int getObjectNumber() {
		return objects.getSize();
	}

	/**
	 * returns the data object at the given index
	 * 
	 * @param index a number between 0 (included) and getObjectNumber (excluded)
	 * @return data object at the given index
	 */
	public DataObjectDefinition getObject(int index) {
		return objects.get(index);
	}

	/**
	 * adds a page to the module
	 * 
	 * @param thispage the page to add
	 */
	public void AddPage(PageDefinition thispage) {
		pages.add(thispage);
	}

	/**
	 * registers the action in the package
	 * 
	 * @param thisaction action to add
	 */
	public void addAction(ActionDefinition thisaction) {
		actions.add(thisaction);
		thisaction.setModule(this);
		// by default, an action is authorized for soreveign privilege and module
		// overlord
		this.addPrivilege(new UnconditionalPrivilege(thisaction, SystemModule.getSystemModule().getSovereign()));
		this.addPrivilege(new UnconditionalPrivilege(thisaction, this.moduleoverlord));
	}

	/**
	 * registers an action if it does not exist yet
	 * 
	 * @param thisaction action to add
	 * @return true if action was added, false if action was not added
	 */
	public boolean addActionIfNotExists(ActionDefinition thisaction) {
		String name = thisaction.getName();
		if (actions.lookupOnName(name) == null) {
			logger.info(
					"   ----> Add action if not exists : " + name + " added normally at index " + actions.getSize());
			addAction(thisaction);
			return true;
		} else {
			logger.severe(" ----> Discarded action " + name
					+ " as already exists through call of AddActionIfNotExists. It may be normal");
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			for (int i = 0; i < stacktrace.length; i++)
				logger.severe("   - " + stacktrace[i].toString());
			logger.severe(" ---------------------------------------------------------------------- ");
			return false;
		}
	}

	/**
	 * registers the action in the package and adds a link from the menu of the
	 * module to the action.
	 * 
	 * @param thisaction
	 */
	public void addasMenuAction(StaticActionDefinition thisaction) {
		addAction(thisaction);
		menuactions.add(thisaction);

	}

	/**
	 * registers the object in the package. Note: this does not need to be called by
	 * the user
	 * 
	 * @param object data object
	 */
	public void addObject(DataObjectDefinition object) {
		objects.add(object);
	}

	/**
	 * adds an advanced feature. This includes smart reports
	 * 
	 * @param advancedfeature the advanced feature to be added
	 */
	public void addAdvancedFeature(AdvancedDesignFeature advancedfeature) {
		advanceddesignfeatures.add(advancedfeature);
		advancedfeature.setParentModule(this);
		advancedfeature.generateActionsAndPages();
	}

	/**
	 * @return the path of the module for the java code
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * @return the number of actions in the module
	 */
	public int getActionNumber() {
		return actions.getSize();
	}

	/**
	 * gets action at the given index in the module
	 * 
	 * @param index a number between 0 (included) and getActionNumber() (excluded)
	 * @return the action at the given index
	 */
	public ActionDefinition getAction(int index) {
		return actions.get(index);
	}

	/**
	 * during code generation, this method will clear all folders that should have
	 * only automatically generated files
	 * 
	 * @param path the path to clean (a folder)
	 * @throws IOException if anything bad happens while clearing the file
	 */
	public void CleanDirectoryOfJavaFiles(String path) throws IOException {
		if (path.length() < 3)
			throw new IOException("Method does not support short pathes to avoid mistakes");
		File f = new File(path);
		if (f.isDirectory()) {

			File[] filesindirectory = f.listFiles();
			if (filesindirectory != null)
				for (int i = 0; i < filesindirectory.length; i++) {
					File currentchild = filesindirectory[i];
					if (!currentchild.isDirectory())
						if (currentchild.getName().indexOf(".java") > -1) {
							logger.info("deletes file " + currentchild.getCanonicalPath());
							currentchild.delete();
						}
				}
		} else { // not directory
			logger.warning("Path indicated is not a directory " + path);
		}
	}

	/**
	 * generates the module to file
	 * 
	 * @param sg source generator
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateToFile(SourceGenerator sg) throws IOException {

		sg.wl("package " + this.getPath() + ";");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("import java.util.logging.Logger;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");

		sg.wl("import org.openlowcode.module.system.data.Appuser;");
		sg.wl("import org.openlowcode.module.system.action.GetfrontpagedataAction;");
		sg.wl("import org.openlowcode.module.system.data.Domain;");
		sg.wl("import org.openlowcode.module.system.data.Authority;");
		sg.wl("import org.openlowcode.module.system.data.Usergroup;");
		sg.wl("import org.openlowcode.module.system.data.Groupswithauthority;");
		sg.wl("import org.openlowcode.module.system.data.Groupadminlink;");
		sg.wl("import org.openlowcode.module.system.data.Groupmemberlink;");
		sg.wl("import org.openlowcode.module.system.page.DefaultmenuPage;");
		sg.wl("import org.openlowcode.server.data.DataObject;");
		sg.wl("import org.openlowcode.server.graphic.widget.SMenu;");
		sg.wl("import org.openlowcode.server.graphic.widget.SMenuItem;");
		sg.wl("import org.openlowcode.server.runtime.SModuleHelper;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectMasterId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.action.ActionExecution;");
		sg.wl("import org.openlowcode.server.graphic.SPageAddon;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");

		if (this.attributes.getSize() > 0) {
			sg.wl("import java.util.HashMap;");
			sg.wl("import org.openlowcode.module.system.data.Systemattribute;");
			sg.wl("import org.openlowcode.module.system.data.SystemattributeDefinition;");
			sg.wl("import org.openlowcode.server.data.properties.StoredobjectQueryHelper;");
			sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
			sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");

		}
		if (this.getDefaultaddon() != null) {
			sg.wl("import " + this.getPath() + ".page."
					+ StringFormatter.formatForJavaClass(this.getDefaultaddon().getName()) + "Page;");
		}
		for (int i = 0; i < this.migrators.getSize(); i++) {
			DataMigrator thismigration = this.migrators.get(i);
			sg.wl("import " + this.getPath() + ".data.migrator." + thismigration.getClassName() + ";");
		}
		for (int i = 0; i < this.actions.getSize(); i++) {
			ActionDefinition thisaction = this.actions.get(i);
			if (thisaction.isAutogenerated()) {
				sg.wl("import " + this.getPath() + ".action.generated.Atg"
						+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action;");
			} else {
				sg.wl("import " + this.getPath() + ".action." + StringFormatter.formatForJavaClass(thisaction.getName())
						+ "Action;");
			}
			sg.wl("import " + this.getPath() + ".action.generated.Abs"
					+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action;");
		}
		for (int i = 0; i < this.getObjectNumber(); i++) {

			DataObjectDefinition objectdef = this.getObject(i);
			sg.wl("import " + this.getPath() + ".data."
					+ StringFormatter.formatForJavaClass(objectdef.getName() + ";"));

		}
		for (int i = 0; i < this.getSequenceNumber(); i++) {
			SequenceDefinition sequence = this.getSequence(i);
			sg.wl("import " + this.getPath() + ".data.sequence."
					+ StringFormatter.formatForJavaClass(sequence.getName()) + "Sequence;");
		}
		sg.bl();
		sg.wl("public class " + StringFormatter.formatForJavaClass(this.getName()) + " extends SModule {");
		sg.bl();

		sg.wl("	private static Logger mainlogger = Logger.getLogger(\"\");");

		sg.bl();

		sg.wl("	public " + StringFormatter.formatForJavaClass(this.getName()) + "()  {");
		sg.wl("		super(\"" + this.getName().toUpperCase() + "\",\"" + this.getCode() + "\",\"" + this.label

				+ "\",\"" + this.getVersionid() + "\",\"" + OLcVersionGenerator.version + "\","
				+ OLcVersionGenerator.stable + ",new Date(" + (new Date()).getTime() + "L));");
		if (this.emailmessage != null)
			sg.wl("		this.setEmailMessage(\""
					+ emailmessage.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + "\");");
		if (this.secure) {
			sg.wl("		this.setSecure();");
		}
		sg.wl("	}");
		sg.wl("");

		sg.wl("	public ActionExecution getAction(String name) {");
		for (int i = 0; i < this.actions.getSize(); i++) {
			ActionDefinition thisaction = this.actions.get(i);
			if (thisaction.isAutogenerated()) {
				sg.wl("		if (name.equals(\"" + thisaction.getName().toUpperCase() + "\")) return  new Atg"
						+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action(this);");
			} else {
				sg.wl("		if (name.equals(\"" + thisaction.getName().toUpperCase() + "\")) return  new "
						+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action(this);");

			}

		}
		sg.wl("		return null;");
		sg.wl("	}");

		sg.wl("	protected ActionExecution getActionForDefaultPage() {");
		if (this.actionfordefaultpage == null) {
			sg.wl("		return new GetfrontpagedataAction(OLcServer.getServer().getModuleByName(\"SYSTEMMODULE\"));");
		} else {
			sg.wl("		return new " + StringFormatter.formatForJavaClass(this.actionfordefaultpage.getName())
					+ "Action(this); ");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void updateDataModelStep1()  {");
		sg.wl("		mainlogger.info(\"starting updating data-model step 1 - initiate definition \");");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition objectdef = this.getObject(i);
			sg.wl("		this.addObjectDefinition(" + StringFormatter.formatForJavaClass(objectdef.getName())
					+ ".getDefinition());	");
			sg.wl("		mainlogger.info(\"   * " + StringFormatter.formatForJavaClass(objectdef.getName())
					+ " Step 1 performed\");");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void updateDataModelStep2()  {");
		sg.wl("		mainlogger.info(\"starting updating data-model step 2 - fields for all objects \");");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition objectdef = this.getObject(i);

			sg.wl("		" + StringFormatter.formatForJavaClass(objectdef.getName())
					+ ".getDefinition().setFieldsAndAttributes();");
			sg.wl("		mainlogger.info(\"  * " + StringFormatter.formatForJavaClass(objectdef.getName())
					+ " Step 2 performed\");	");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public void updateDataModelStep3()  {");
		sg.wl("		mainlogger.info(\"starting updating data-model step 3 - calculated fields for all objects \");");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition objectdef = this.getObject(i);
			sg.wl("		" + StringFormatter.formatForJavaClass(objectdef.getName())
					+ ".getDefinition().setExternalFields();");
			sg.wl("		mainlogger.info(\"  * " + StringFormatter.formatForJavaClass(objectdef.getName())
					+ " Step 3 performed\");	");
		}
		sg.wl("	}");
		sg.wl("");

		sg.wl("		@Override");
		sg.wl("		public void updateDataModelStep4()  {");
		sg.wl("		mainlogger.info(\"starting updating data-model step 4 - update persistence \");");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition objectdef = this.getObject(i);
			sg.wl("			" + StringFormatter.formatForJavaClass(objectdef.getName())
					+ ".getDefinition().updatePersistenceStorage();");
			sg.wl("			mainlogger.info(\"  * " + StringFormatter.formatForJavaClass(objectdef.getName())
					+ " Step 4 performed\");		");
		}
		sg.wl("	}");
		sg.wl("");

		sg.wl("		@Override");
		sg.wl("		public void updateDataModelStep5()  {");
		sg.wl("			mainlogger.info(\"starting updating data-model step 5 - update sequence \");");
		for (int i = 0; i < this.getSequenceNumber(); i++) {
			SequenceDefinition sequence = this.getSequence(i);
			sg.wl("			" + StringFormatter.formatForJavaClass(sequence.getName())
					+ "Sequence.get().updatepersistence();");
			sg.wl("			mainlogger.info(\"  * " + StringFormatter.formatForJavaClass(sequence.getName())
					+ "Sequence Step 5 performed\");		");
		}
		sg.wl("		}");

		sg.wl("			@Override");
		sg.wl("			public void initiateData() {");
		if (this.hasdatainitialization) {
			sg.wl("				SModule.ModuleDataInit initializer = new "
					+ StringFormatter.formatForJavaClass(this.getName()) + "DataInit();");
			sg.wl("				initializer.initiateData();");

		}

		for (int i = 0; i < this.users.size(); i++) {
			User user = this.users.get(i);
			String uservariable = user.getName().toLowerCase();
			sg.wl("				Appuser " + uservariable + "=SModuleHelper.createUserIfNotExists(\"" + user.getName()
					+ "\",\"" + user.getPassword() + "\",\"" + user.getEmail() + "\",\""
					+ (user.getLastname() != null ? user.getLastname() : "") + "\",\""
					+ (user.getFirstname() != null ? user.getFirstname() : "") + "\");");

		}

		sg.wl("				Domain moduledomain=SModuleHelper.createDomainIfNotExists(\"" + this.getName().toUpperCase()
				+ "\",Domain.getobjectbynumber(\"EMPIRE\")[0]);");

		for (int i = 0; i < this.authorities.getSize(); i++) {

			String authorityvariable = this.authorities.get(i).getName().toLowerCase();

			sg.wl("				Authority " + authorityvariable + " = SModuleHelper.createAuthorityIfNotExists(\""
					+ this.getCode() + "_" + this.authorities.get(i).getName() + "\",\""
					+ this.authorities.get(i).getLabel() + "\",\"" + this.authorities.get(i).getScope()
					+ "\",moduledomain);");

		}

		for (int i = 0; i < this.groups.getSize(); i++) {
			Group thisgroup = this.groups.get(i);
			String groupvariable = thisgroup.getName().toLowerCase() + "group";
			String groupnumber = this.getCode() + "_" + thisgroup.getName().toUpperCase();
			String groupdescription = thisgroup.getDescription();
			sg.wl("				Usergroup " + groupvariable + " = SModuleHelper.createUserGroupIfNotExists(\""
					+ groupnumber + "\",\"" + groupdescription + "\");");

			sg.wl("				SModuleHelper.createAdminLinkForGroupIfNotExists(" + groupvariable + ","
					+ this.moduleadmin.getName().toLowerCase() + ");");
			sg.wl("				SModuleHelper.createMemberLinkForGroupIfNotExists(" + groupvariable + ","
					+ this.moduleadmin.getName().toLowerCase() + ");");

			for (int j = 0; j < this.groups.get(i).getAuthoritiesindex(); j++) {
				TotalAuthority thisauthority = this.groups.get(i).getAuthority(j);
				if (thisauthority.getModule().equals(this)) {
					String authorityname = this.groups.get(i).getAuthority(j).getName().toLowerCase();
					sg.wl("				SModuleHelper.createGroupWithAuthorityIfNotExists(" + authorityname + ","
							+ groupvariable + ");");
				} else {
					String authorityvariable = thisauthority.getModule().getName().toLowerCase() + "_"
							+ thisauthority.getName().toLowerCase();
					String authoritynr = thisauthority.getModule().getCode().toUpperCase() + "_"
							+ thisauthority.getName().toUpperCase();

					sg.wl("				Authority[] " + authorityvariable + " = Authority.getobjectbynumber(\""
							+ authoritynr + "\");");
					sg.wl("				if (" + authorityvariable
							+ ".length==1) SModuleHelper.createGroupWithAuthorityIfNotExists(" + authorityvariable
							+ "[0]," + groupvariable + ");	");
					sg.wl("				if (" + authorityvariable
							+ ".length!=1) mainlogger.severe(\"Could not find authority in other module " + authoritynr
							+ " to link it to group " + groupvariable + ".\");	");

				}

			}

		}

		for (int i = 0; i < moduledomains.getSize(); i++) {
			ModuleDomain thismoduledomain = moduledomains.get(i);
			String domainname = this.getCode() + "_" + thismoduledomain.getName();
			String variablename = Named.cleanName(thismoduledomain.getName()).toLowerCase();
			sg.wl("				Domain " + variablename + "=SModuleHelper.createDomainIfNotExists(\"" + domainname
					+ "\",moduledomain);");

			for (int j = 0; j < this.moduledomainauthorities.getSize(); j++) {
				ModuleDomainAuthority thismoduledomainauthority = this.moduledomainauthorities.get(j);
				String domainauthorityname = this.getCode() + "_" + thismoduledomain.getName() + "_"
						+ thismoduledomainauthority.getName();
				String authorityvariablename = Named.cleanName(domainauthorityname).toLowerCase();

				sg.wl("				Authority " + authorityvariablename
						+ " = SModuleHelper.createAuthorityIfNotExists(\"" + domainauthorityname + "\",\""
						+ thismoduledomainauthority.getLabel() + "(" + thismoduledomain.getOriginalName() + "\",\""
						+ thismoduledomainauthority.getScope() + " (for domain " + thismoduledomain.getOriginalName()
						+ "\"," + variablename + ");");

			}
			for (int j = 0; j < this.moduledomaingroups.getSize(); j++) {
				ModuleDomainGroup thismoduledomaingroup = this.moduledomaingroups.get(j);
				String domaingroupname = this.getCode() + "_" + thismoduledomain.getName() + "_"
						+ thismoduledomaingroup.getName();
				String domaingroupvariablename = Named.cleanName(domaingroupname).toLowerCase();
				sg.wl("				Usergroup " + domaingroupvariablename
						+ " = SModuleHelper.createUserGroupIfNotExists(\"" + domaingroupname + "\",\""
						+ thismoduledomaingroup.getDescription() + "\");");

				sg.wl("				SModuleHelper.createAdminLinkForGroupIfNotExists(" + domaingroupvariablename + ","
						+ this.moduleadmin.getName().toLowerCase() + ");");
				sg.wl("				SModuleHelper.createMemberLinkForGroupIfNotExists(" + domaingroupvariablename + ","
						+ this.moduleadmin.getName().toLowerCase() + ");");

				for (int k = 0; k < thismoduledomaingroup.getAuthoritySize(); k++) {
					ModuleDomainAuthority moduledomainauthority = thismoduledomaingroup.getModuleDomainAuthority(k);
					String domainauthorityname = this.getCode() + "_" + thismoduledomain.getName() + "_"
							+ moduledomainauthority.getName();
					String authorityvariablename = Named.cleanName(domainauthorityname).toLowerCase();
					sg.wl("				SModuleHelper.createGroupWithAuthorityIfNotExists(" + authorityvariablename
							+ "," + domaingroupvariablename + ");");

				}
				for (int k = 0; k < thismoduledomaingroup.getTotalAuthoritySize(); k++) {
					TotalAuthority thistotalauthority = thismoduledomaingroup.getTotalAuthority(k);
					String authorityname = thistotalauthority.getName().toLowerCase();
					sg.wl("				SModuleHelper.createGroupWithAuthorityIfNotExists(" + authorityname + ","
							+ domaingroupvariablename + ");");

				}
			}
		}

// Link Groups total group to domain authorities
		for (int i = 0; i < this.groups.getSize(); i++) {
			Group thisgroup = this.groups.get(i);
			String groupvariable = thisgroup.getName().toLowerCase() + "group";
			for (int j = 0; j < thisgroup.getDomainAuthoritiesIndex(); j++) {
				ModuleDomainAuthority thismoduledomainauthority = thisgroup.getDomainAuthorityAt(j);
				for (int k = 0; k < moduledomains.getSize(); k++) {
					ModuleDomain thismoduledomain = moduledomains.get(i);
					String domainauthorityname = this.getCode() + "_" + thismoduledomain.getName() + "_"
							+ thismoduledomainauthority.getName();
					String authorityvariablename = Named.cleanName(domainauthorityname).toLowerCase();
					sg.wl("				SModuleHelper.createGroupWithAuthorityIfNotExists(" + authorityvariablename
							+ "," + groupvariable + ");");
				}
			}
		}

		if (this.attributes.getSize() > 0) {

			sg.wl("				Systemattribute[] moduleattributes = Systemattribute.getallactive(QueryFilter.get(new SimpleQueryCondition<String>(SystemattributeDefinition.getSystemattributeDefinition().getAlias(");
			sg.wl("					StoredobjectQueryHelper.maintablealiasforgetallactive), Systemattribute.getDefinition().getNrFieldSchema(),new QueryOperatorLike(), \""
					+ this.code + ".%\")));");

			sg.wl("				HashMap<String,Systemattribute> attributesbynr = new HashMap<String,Systemattribute>();");
			sg.wl("				for (int i=0;i<moduleattributes.length;i++) {");
			sg.wl("					Systemattribute thisattribute = moduleattributes[i];");
			sg.wl("					attributesbynr.put(thisattribute.getNr(),thisattribute);");
			sg.wl("				}");

			for (int i = 0; i < this.attributes.getSize(); i++) {
				SystemAttributeInit thisattribute = this.attributes.get(i);
				String name = this.code + "." + thisattribute.getName().toUpperCase();
				sg.wl("				Systemattribute attribute" + i + " = attributesbynr.get(\""
						+ StringFormatter.escapeforjavastring(name) + "\");");
				sg.wl("				if (attribute" + i + "==null) {");

				sg.wl("					attribute" + i + " = new Systemattribute();");
				sg.wl("					attribute" + i + ".setobjectnumber(\""
						+ StringFormatter.escapeforjavastring(name) + "\");");
				sg.wl("					attribute" + i + ".setValue(\""
						+ StringFormatter.escapeforjavastring(thisattribute.getDefaultvalue()) + "\");");
				sg.wl("					attribute" + i + ".setComment(\""
						+ StringFormatter.escapeforjavastring(thisattribute.getComment()) + "\");");
				sg.wl("					attribute" + i + ".insert();	");

				sg.wl("					}");
			}
		}

		sg.wl("			}");

		sg.wl("			@Override");
		sg.wl("			public SPageAddon getPageAddonForModule()  {");
		if (this.getDefaultaddon() != null) {
			sg.wl("				return  new " + StringFormatter.formatForJavaClass(this.getDefaultaddon().getName())
					+ "Page();");
		} else {
			sg.wl("				return new DefaultmenuPage();");
		}
		sg.wl("			}");

		sg.wl("@Override");
		sg.wl("public void DataUpdateStep1()  {");
		sg.wl("	mainlogger.info(\"starting data update Step 1 - DataMigrators \");");
		for (int i = 0; i < this.migrators.getSize(); i++) {
			DataMigrator migrator = this.migrators.get(i);
			sg.wl(" new " + migrator.getClassName() + "(this).executeMigration(); ");
		}
		sg.wl("	mainlogger.info(\"Data update Step 1 - performed \");");
		sg.wl("}");

		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String getFrontPageMessage() {");
		sg.wl("		return \"" + this.frontpagemessage.replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"")
				+ "\";");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public SPage getShowPageBasedOnObjectId(DataObjectId genericid)  {");
		sg.wl("		// Get Object");
		sg.wl("		String [] splitobjectid = genericid.getObjectId().split(\":\");");
		sg.wl("		if (splitobjectid.length!=2) throw new  RuntimeException(\"objectid should have two components separated by ':', but does not have  \"+genericid.getObjectId());");
		sg.wl("		String modulename = splitobjectid[0];");
		sg.wl("		if (modulename.compareTo(this.getName())!=0) throw new  RuntimeException(\"wrong module, expected \"+this.getName()+\", got \"+modulename+\" for objectid \"+genericid);");
		sg.wl("		String objectname = splitobjectid[1];");
		sg.wl("		// Mapping for every object");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			if (thisobject.isShowActionAutomaticallyGenerated()) {
				String uppercasename = thisobject.getName().toUpperCase();
				String classname = StringFormatter.formatForJavaClass(thisobject.getName());
				String lowercasename = thisobject.getName().toLowerCase();
				sg.wl("		if (objectname.equals(\"" + uppercasename + "\")) return AtgShow" + lowercasename
						+ "Action.get().executeAndShowPage(DataObjectId.castDataObjectId(genericid," + classname
						+ ".getDefinition()));");
			}
		}
		sg.wl("		// Exception if not found");
		sg.wl("		throw new  RuntimeException(\"Object \"+objectname+\" not found in module \"+this.getName());");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("	public DataObject getDataObjectBasedOnGenericMasterId(DataObjectMasterId genericmasterid) {");
		sg.wl("		String [] splitobjectid = genericmasterid.getObjectId().split(\":\");");
		sg.wl("		if (splitobjectid.length!=2) throw new  RuntimeException(\"objectid should have two components separated by ':', but does not have  \"+genericmasterid.getObjectId());");
		sg.wl("		String modulename = splitobjectid[0];");
		sg.wl("		if (modulename.compareTo(this.getName())!=0) throw new  RuntimeException(\"wrong module, expected \"+this.getName()+\", got \"+modulename+\" for objectid \"+genericmasterid);");
		sg.wl("		String objectname = splitobjectid[1];");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			if (thisobject.getPropertyByName("VERSIONED") != null) {
				String uppercasename = thisobject.getName().toUpperCase();
				String classname = StringFormatter.formatForJavaClass(thisobject.getName());
				sg.wl("		if (objectname.equals(\"" + uppercasename + "\")) return " + classname
						+ ".getlastversion(DataObjectMasterId.castDataObjectMasterId(genericmasterid," + classname
						+ ".getDefinition()));");
			}
		}
		sg.wl("		// Exception if not found");
		sg.wl("		throw new  RuntimeException(\"Object (with property versioned) \"+objectname+\" not found in module \"+this.getName());");
		sg.wl("	}");

		sg.wl("	@Override");
		sg.wl("	public DataObject getDataObjectBasedOnGenericId(DataObjectId genericid)  {");
		sg.wl("		// Get Object");
		sg.wl("		String [] splitobjectid = genericid.getObjectId().split(\":\");");
		sg.wl("		if (splitobjectid.length!=2) throw new  RuntimeException(\"objectid should have two components separated by ':', but does not have  \"+genericid.getObjectId());");
		sg.wl("		String modulename = splitobjectid[0];");
		sg.wl("		if (modulename.compareTo(this.getName())!=0) throw new  RuntimeException(\"wrong module, expected \"+this.getName()+\", got \"+modulename+\" for objectid \"+genericid);");
		sg.wl("		String objectname = splitobjectid[1];");
		sg.wl("		// Mapping for every object");
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			if (thisobject.getPropertyByName("UNIQUEIDENTIFIED") != null) {
				String uppercasename = thisobject.getName().toUpperCase();
				String classname = StringFormatter.formatForJavaClass(thisobject.getName());
				sg.wl("		if (objectname.equals(\"" + uppercasename + "\")) return " + classname
						+ ".readone(DataObjectId.castDataObjectId(genericid," + classname + ".getDefinition()));");
			}
		}
		sg.wl("		// Exception if not found");
		sg.wl("		throw new  RuntimeException(\"Object (with property UniqueIdentified) \"+objectname+\" not found in module \"+this.getName());");
		sg.wl("	}");

		sg.wl("	@Override");
		sg.wl("	public SMenu getModuleMenu(SPage parentaddonpage)  {");
		sg.wl("		SMenu modulemenu = new SMenu(parentaddonpage, \"" + this.label + "\");");
		sg.wl("		setIcon(modulemenu);");
		for (int i = 0; i < this.menuactions.getSize(); i++) {
			StaticActionDefinition thisaction = this.menuactions.get(i);
			String actionname = (thisaction.isAutogenerated() ? "Atg" : "")
					+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action";
			sg.wl("		" + actionname + ".ActionRef " + thisaction.getName().toLowerCase() + "ref = " + actionname
					+ ".get().getActionRef();");
			sg.wl("		SMenuItem " + thisaction.getName().toLowerCase() + "menuitem = new SMenuItem(parentaddonpage,\""
					+ (thisaction.getButtonlabel() != null ? thisaction.getButtonlabel()
							: thisaction.getName().toLowerCase())
					+ "\"," + thisaction.getName().toLowerCase() + "ref);");
			sg.wl("		modulemenu.addMenuItem(" + thisaction.getName().toLowerCase() + "menuitem);");
		}
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			if (thisobject.showSearchInMenu()) {
				String actionname = "AtgLaunchsearch" + thisobject.getName().toLowerCase() + "Action";
				sg.wl("		" + actionname + ".ActionRef " + thisobject.getName().toLowerCase() + "searchref = "
						+ actionname + ".get().getActionRef();");
				sg.wl("		SMenuItem " + thisobject.getName().toLowerCase()
						+ "searchmenuitem = new SMenuItem(parentaddonpage,\"Start with " + thisobject.getLabel() + "\","
						+ thisobject.getName().toLowerCase() + "searchref);");
				sg.wl("		modulemenu.addMenuItem(" + thisobject.getName().toLowerCase() + "searchmenuitem);");
			}
		}
		sg.wl("		");
		sg.wl("		return modulemenu;");
		sg.wl("	}");

		sg.wl("}");
		sg.close();

	}

	/**
	 * generates all the sources for the module
	 * 
	 * @throws IOException if anything bad happens while writing the files
	 */
	public void generateSources() throws IOException {

		String srcbasefolder = "." + File.separator + "src" + File.separator;
		String packagename = this.getPath().replace('.', File.separatorChar);
		String modulepath = srcbasefolder + packagename + File.separator;

		// path for automatically generated files
		String srcactionfolder = srcbasefolder + packagename + File.separator + "action" + File.separator + "generated"
				+ File.separator;
		String srcautoactionfolder = srcbasefolder + packagename + File.separator + "action" + File.separator
				+ "generated" + File.separator;
		String srcobjectfolder = srcbasefolder + packagename + File.separator + "data" + File.separator;
		String srcchoicefolder = srcbasefolder + packagename + File.separator + "data" + File.separator + "choice"
				+ File.separator;
		String srcmigratorfolder = srcbasefolder + packagename + File.separator + "data" + File.separator + "migrator"
				+ File.separator;
		String srcautopagefolder = srcbasefolder + packagename + File.separator + "page" + File.separator + "generated"
				+ File.separator;
		String srcpagefolder = srcbasefolder + packagename + File.separator + "page" + File.separator + "generated"
				+ File.separator;
		String srcsequencefolder = srcbasefolder + packagename + File.separator + "data" + File.separator + "sequence"
				+ File.separator;
		String srcutilityfolder = srcbasefolder + packagename + File.separator + "utility" + File.separator
				+ "generated" + File.separator;

		logger.warning("Step 0 ----- cleaning automatic filed --------- ");
		// this.CleanDirectoryOfJavaFiles(modulepath); need to solve datainit first
		this.CleanDirectoryOfJavaFiles(srcactionfolder);
		this.CleanDirectoryOfJavaFiles(srcautoactionfolder);
		this.CleanDirectoryOfJavaFiles(srcobjectfolder);
		this.CleanDirectoryOfJavaFiles(srcchoicefolder);
		this.CleanDirectoryOfJavaFiles(srcmigratorfolder);
		this.CleanDirectoryOfJavaFiles(srcautopagefolder);
		this.CleanDirectoryOfJavaFiles(srcpagefolder);
		this.CleanDirectoryOfJavaFiles(srcsequencefolder);
		this.CleanDirectoryOfJavaFiles(srcutilityfolder);

		logger.warning("Step 1 ----- generating automatic actions and pages for objects --------- ");

		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			logger.info("generating actions and pages for object " + thisobject.getName());
			thisobject.generateAutomaticPagesAndActions(this);
		}

		logger.severe("Step 1b ---------------- Controling privileges ----------------");
		controlPrivileges();

		logger.warning("Step 2 -------------------- Starting File Generation -------------------- ");

		// **************************** MODULE

		this.generateToFile(
				new SourceGenerator(new File(modulepath + StringFormatter.formatForJavaClass(this.getName()) + ".java"),
						this.getAuthor(), this.getVersionid()));
		logger.info("generating file : " + modulepath + StringFormatter.formatForJavaClass(this.getName()) + ".java");

		// **************************** MIGRATOR

		for (int i = 0; i < this.migrators.getSize(); i++) {
			DataMigrator thismigrator = this.migrators.get(i);
			logger.info("generating file " + srcmigratorfolder + thismigrator.getClassName() + ".java");
			thismigrator.generateMigratorToFile(
					new SourceGenerator(new File(srcmigratorfolder + thismigrator.getClassName() + ".java"),
							this.getAuthor(), this.getVersionid()),
					this);
		}

		// **************************** ACTIONS
		for (int i = 0; i < this.getActionNumber(); i++) {
			ActionDefinition thisaction = this.getAction(i);
			logger.info("generating file " + srcactionfolder + "Abs"
					+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action.java");
			thisaction
					.generateToFile(
							new SourceGenerator(
									new File(srcactionfolder + "Abs"
											+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action.java"),
									this.getAuthor(), this.getVersionid()),
							this);
		}
		// AUTOMATICALLY GENERATED AUTO ACTIONS
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition currentobject = this.getObject(i);

			for (int l = 0; l < currentobject.getPropertySize(); l++) {
				Property<?> thisproperty = currentobject.getPropertyAt(l);
				if (thisproperty instanceof LinkedFromChildren) {
					LinkedFromChildren linkedfromchildren = (LinkedFromChildren) thisproperty;
					String loadchildrenactioname = "LOADCHILDREN" + linkedfromchildren.getInstancename() + "FOR"
							+ currentobject.getName();

					String fullfilepath = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(loadchildrenactioname) + "Action.java";
					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionFileActions.generateLoadChildrenToFile(currentobject.getName(),
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this,
							linkedfromchildren);

					String exportchildrentofile = "EXPORTCHILDREN" + linkedfromchildren.getInstancename() + "FOR"
							+ currentobject.getName();
					String fullfilepathforexport = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(exportchildrentofile) + "Action.java";
					logger.info("generating file " + fullfilepathforexport);
					DataObjectDefinitionFileActions.generateExportChildrenToFile(currentobject.getName(),
							new SourceGenerator(new File(fullfilepathforexport), this.getAuthor(), this.getVersionid()),
							this, linkedfromchildren);

				}
				if (thisproperty instanceof HasMultiDimensionalChild) {
					HasMultiDimensionalChild hasmultidimensionchild = (HasMultiDimensionalChild) thisproperty;
					String repairaction = "REPAIRLINESFOR" + thisproperty.getInstancename();
					String fullfilepath = srcautoactionfolder + "Atg" + StringFormatter.formatForJavaClass(repairaction)
							+ "Action.java";
					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generateRepairMultiDimensionChildrenToFile(currentobject,
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this,
							hasmultidimensionchild);
					
					String addlinesaction = "ADDLINESFOR" + thisproperty.getInstancename();
					String addlinesactionfullfilepath = srcautoactionfolder + "Atg" + StringFormatter.formatForJavaClass(addlinesaction)
							+ "Action.java";
					logger.info("generating file " + addlinesactionfullfilepath);
					DataObjectDefinitionOtherActions.generateAddLinesActionToFile(currentobject,
							new SourceGenerator(new File(addlinesactionfullfilepath), this.getAuthor(), this.getVersionid()), this,
							hasmultidimensionchild);
					
					String addlinespagefullfilepath = srcautopagefolder + "Atg" + StringFormatter.formatForJavaClass(addlinesaction)
					+ "Page.java";
					logger.info("generating file " + addlinespagefullfilepath);
					DataObjectDefinitionOtherActions.generateAddLinesPageToFile(currentobject,
							new SourceGenerator(new File(addlinespagefullfilepath), this.getAuthor(), this.getVersionid()), this,
							hasmultidimensionchild);
					
					String prepareaddlinesaction = "PREPAREADDLINESFOR" + thisproperty.getInstancename();
					String prepareaddlinesactionfilepath = srcautoactionfolder + "Atg" + StringFormatter.formatForJavaClass(prepareaddlinesaction)
					+ "Action.java";
					logger.info("generating file " + prepareaddlinesactionfilepath);
					DataObjectDefinitionOtherActions.generatePrepareAddLinesActionToFile(currentobject,
							new SourceGenerator(new File(prepareaddlinesactionfilepath), this.getAuthor(), this.getVersionid()), this,
							hasmultidimensionchild);
				}
				if (thisproperty instanceof ImageContent) {
					ImageContent imagecontent = (ImageContent) thisproperty;
					String fullfilepath = srcautoactionfolder + "Atg" + StringFormatter.formatForJavaClass(
							"SETIMAGECONTENTFOR" + imagecontent.getInstancename() + "FOR" + currentobject.getName())
							+ "Action.java";

					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generateSetImageToFile(currentobject.getName(), imagecontent,
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

					String fullfilepathforgetfull = srcautoactionfolder + "Atg" + StringFormatter.formatForJavaClass(
							"GETFULLIMAGEFOR" + imagecontent.getInstancename() + "FOR" + currentobject.getName())
							+ "Action.java";
					DataObjectDefinitionOtherActions.generateGetFullImageToFile(currentobject.getName(), imagecontent,
							new SourceGenerator(new File(fullfilepathforgetfull), this.getAuthor(),
									this.getVersionid()),
							this);

				}
				if (thisproperty instanceof PrintOut) {
					PrintOut printout = (PrintOut) thisproperty;
					String fullfilepath = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(
									"PREVIEWPRINTOUTFOR" + currentobject.getName() + "FOR" + printout.getInstancename())
							+ "Action.java";
					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generatePrintOutPreviewToFile(currentobject.getName(),
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this,
							printout);
				}
				if (thisproperty instanceof FileContent) {
					String fullfilepath = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("ADDNEWATTACHMENTFOR" + currentobject.getName())
							+ "Action.java";

					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generateaddAttachment(currentobject.getName(),
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

					String fullfilepathdownload = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DOWNLOADATTACHMENTFOR" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepathdownload);
					DataObjectDefinitionOtherActions.generateDownloadAttachment(currentobject.getName(),
							new SourceGenerator(new File(fullfilepathdownload), this.getAuthor(), this.getVersionid()),
							this);

					String fullfilepathdeleteattachment = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETEATTACHMENTFOR" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeleteattachment);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteAttachment(currentobject.getName(),
							new SourceGenerator(new File(fullfilepathdeleteattachment), this.getAuthor(),
									this.getVersionid()),
							this);
				}
			}

			if (currentobject.hasLifecycle())
				if (currentobject.isShowActionAutomaticallyGenerated()) {
					String fullfilepath = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("CHANGESTATE" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generateChangeStateActionToFile(currentobject.getName(),
							(Lifecycle) currentobject.getPropertyByName("LIFECYCLE"),
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

				}
			if (currentobject.hasTimeslot()) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("RESCHEDULE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionWorkflowAndSchedule.generateRescheduleActionToFile(currentobject.getName(),
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}
			if (currentobject.hasNumbered()) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("RENUMBER" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionOtherActions.generateRenumberActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}

			if (currentobject.isVersioned()) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("NEWVERSIONFOR" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionOtherActions.generateNewVersionActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}
			if (currentobject.isVersioned()) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("FORCEVERSIONASLASTFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepath);

				DataObjectDefinitionOtherActions.generateForceAsLatestVersionToFile(currentobject,
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}

			if (currentobject.getPropertyByName("TARGETDATE") != null) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("SETTARGETDATEFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionOtherActions.generateSetTargetDateActionToFile(currentobject.getName(),
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}

			LinkedToParent<?>[] linkedtoparents = currentobject.getParents();

			if (linkedtoparents != null)
				for (int j = 0; j < linkedtoparents.length; j++) {
					LinkedToParent<?> linktoparent = linkedtoparents[j];
					String fullfilepath = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(
									"CHANGEPARENTFOR" + linktoparent.getInstancename() + "OF" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepath);
					DataObjectDefinitionOtherActions.generateChangeParentActionToFile(currentobject.getName(),
							new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this,
							linktoparent);

					String fullfilepathmassupdateshowparent = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName()
									+ "ANDSHOWPARENT" + linktoparent.getInstancename())
							+ "Action.java";
					logger.info("generating file " + fullfilepathmassupdateshowparent);

					DataObjectDefinitionDeleteAndUpdate.generateMassUpdateAndShowParentToFile(currentobject,
							new SourceGenerator(new File(fullfilepathmassupdateshowparent), this.getAuthor(),
									this.getVersionid()),
							this, linktoparent);

					String deletechildrenactioname = "MASSIVEDELETE" + currentobject.getName() + "ANDSHOWPARENT"
							+ linktoparent.getInstancename();

					String fullfilepathdelete = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(deletechildrenactioname) + "Action.java";
					logger.info("generating file " + fullfilepathdelete);

					DataObjectDefinitionDeleteAndUpdate.generateMassiveDeleteAndShowParentActionToFile(
							new SourceGenerator(new File(fullfilepathdelete), this.getAuthor(), this.getVersionid()),
							this, currentobject.getName(), linktoparent);
				}

			if (currentobject.isShowActionAutomaticallyGenerated()) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOW" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepath);

				DataObjectDefinitionShowAction.generateShowActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

				if (currentobject.getPropertyByName("TYPED")!=null) {
					Typed typed = (Typed) (currentobject.getPropertyByName("TYPED"));
					for (int c=0;c<typed.getCompanionNumber();c++) {
						DataObjectDefinition companion = typed.getCompanion(c);
						String fullfilepathforcompanion = srcautoactionfolder + "Atg"
								+ StringFormatter.formatForJavaClass("SHOW" + companion.getName()) + "Action.java";
						logger.info("generating file " + fullfilepathforcompanion);

						DataObjectDefinitionShowAction.generateShowActionToFile(currentobject,companion,
								new SourceGenerator(new File(fullfilepathforcompanion), this.getAuthor(), this.getVersionid()), this);
					}
				}
				
				String fullfilepathsearch = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("SEARCH" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathsearch);
				currentobject.getSearchPagesAndActions().generateSearchActionToFile(
						new SourceGenerator(new File(fullfilepathsearch), this.getAuthor(), this.getVersionid()), this);
				String fullfilepathlaunchsearch = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("LAUNCHSEARCH" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathlaunchsearch);

				DataObjectDefinitionSearchPagesAndActions.generateLaunchSearchActionToFile(currentobject.getName(),
						new SourceGenerator(new File(fullfilepathlaunchsearch), this.getAuthor(), this.getVersionid()),
						this);

			}

			if ((currentobject.IsIterated()) || (currentobject.isVersioned())) {
				String fullfilepath = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOWHISTORYFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionOtherActions.generateShowHistoryActionToFile(currentobject.getName(),
						currentobject.IsIterated(), currentobject.isVersioned(),
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);
			}
			if (currentobject.IsIterated()) {

				String fullfilepathshowoneiteration = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOW" + currentobject.getName() + "ITERATION")
						+ "Action.java";
				logger.info("generating file " + fullfilepathshowoneiteration);
				DataObjectDefinitionOtherActions.generateShowActionForIterationToFile(currentobject,
						new SourceGenerator(new File(fullfilepathshowoneiteration), this.getAuthor(),
								this.getVersionid()),
						this);

				String fullfilepathshowoneiterationpage = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOW" + currentobject.getName() + "ITERATION")
						+ "Page.java";
				logger.info("generating file " + fullfilepathshowoneiterationpage);
				new DataObjectDefinitionShowIterationPage(currentobject).generateToFile(new SourceGenerator(
						new File(fullfilepathshowoneiterationpage), this.getAuthor(), this.getVersionid()), this);

			}

			if (currentobject.hasSimpleWorkflow()) {
				String fullfilepathsimplecockpitaction = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass(currentobject.getName() + "WORKFLOWCOCKPIT")
						+ "Action.java";
				logger.info("generating file " + fullfilepathsimplecockpitaction);

				DataObjectDefinitionWorkflowAndSchedule.generateSimpleWorkflowCockpitActionToFile(
						currentobject.getName(), new SourceGenerator(new File(fullfilepathsimplecockpitaction),
								this.getAuthor(), this.getVersionid()),
						this);

				String fillfilepathadminreassignaction = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass(currentobject.getName() + "ADMINREASSIGN") + "Action.java";
				logger.info("generating file " + fillfilepathadminreassignaction);
				DataObjectDefinitionWorkflowAndSchedule.generateTaskReassignActionToFile(currentobject.getName(),
						new SourceGenerator(new File(fillfilepathadminreassignaction), this.getAuthor(),
								this.getVersionid()),
						this);

				String fullfilepathsimplecockpitpage = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass(currentobject.getName() + "WORKFLOWCOCKPIT") + "Page.java";
				logger.info("generating file " + fullfilepathsimplecockpitpage);

				DataObjectDefinitionWorkflowAndSchedule.generateSimpleWorkflowCockpitPageToFile(currentobject.getName(),
						currentobject.getLabel(), new SourceGenerator(new File(fullfilepathsimplecockpitpage),
								this.getAuthor(), this.getVersionid()),
						this);
			}

			if (currentobject.hasSchedule()) {
				String fullfilepathprepareshowschedule = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("PREPARESHOWPLANNINGFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathprepareshowschedule);
				DataObjectDefinitionWorkflowAndSchedule.generateShowPlanningActionToFile(currentobject.getName(),
						(Schedule) currentobject.getPropertyByName("SCHEDULE"),
						new SourceGenerator(new File(fullfilepathprepareshowschedule), this.getAuthor(),
								this.getVersionid()),
						this);

				String fullfilepathrescheduleandshowplanning = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("RESCHEDULEANDSHOWPLANNINGFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathrescheduleandshowplanning);

				DataObjectDefinitionWorkflowAndSchedule.generateRescheduleandShowPlanningActionToFile(
						currentobject.getName(), new SourceGenerator(new File(fullfilepathrescheduleandshowplanning),
								this.getAuthor(), this.getVersionid()),
						this);

				String fullfilepathinsertafter = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("INSERTAFTER" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathinsertafter);

				DataObjectDefinitionWorkflowAndSchedule.generateInsertafterActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathinsertafter), this.getAuthor(), this.getVersionid()),
						this);

				String fullfilepathshowschedulepage = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOWPLANNINGFOR" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilepathshowschedulepage);

				DataObjectDefinitionWorkflowAndSchedule.generateShowPlanningPageToFile(currentobject.getName(),
						(Schedule) currentobject.getPropertyByName("SCHEDULE"), new SourceGenerator(
								new File(fullfilepathshowschedulepage), this.getAuthor(), this.getVersionid()),
						this);

			}

			if (currentobject.isUniqueIdentified()) {
				boolean subobject = false;
				LinkedToParent<?> relevantlinkedtoparent = null;
				boolean autolink = false;

				boolean linkobject = false;
				boolean linkobjecttomaster = false;

				if (currentobject.getPropertyByName("LINKOBJECT") != null)
					linkobject = true;
				if (currentobject.getPropertyByName("AUTOLINKOBJECT") != null)
					autolink = true;
				if (currentobject.getPropertyByName("LINKOBJECTTOMASTER") != null)
					linkobjecttomaster = true;
				if (linkedtoparents != null)
					for (int i1 = 0; i1 < linkedtoparents.length; i1++) {
						LinkedToParent<?> thislinkedtoparent = linkedtoparents[i1];
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
				if (((!subobject) && (!autolink) && (!linkobject) && (!linkobjecttomaster))
						|| ((linkobject) && (currentobject.IsIterated()))) {
					String fullfilepathdelete = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName()) + "Action.java";
					logger.info("generating file " + fullfilepathdelete);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteActionToFile(currentobject,
							new SourceGenerator(new File(fullfilepathdelete), this.getAuthor(), this.getVersionid()),
							this);

				}
				if (autolink) {
					String fullfilepathdeleteautolinkandshowobject = srcautoactionfolder + "Atg" + StringFormatter
							.formatForJavaClass("DELETEAUTOLINK" + currentobject.getName() + "ANDSHOWOBJECT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeleteautolinkandshowobject);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteAutolinkAndShowObjectActionToFile(
							currentobject.getName(),
							(AutolinkObject<?>) currentobject.getPropertyByName("AUTOLINKOBJECT"),
							new SourceGenerator(new File(fullfilepathdeleteautolinkandshowobject), this.getAuthor(),
									this.getVersionid()),
							this);

				}
				if (subobject) {
					String fullfilepathdeleteandshowobject = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName() + "ANDSHOWPARENT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeleteandshowobject);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteAndShowParentActionToFile(currentobject,
							relevantlinkedtoparent, new SourceGenerator(new File(fullfilepathdeleteandshowobject),
									this.getAuthor(), this.getVersionid()),
							this);

				}
				if (linkobject) {
					String fullfilepathdeletelinkandshowleft = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName() + "ANDSHOWLEFT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeletelinkandshowleft);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteLinkAndShowLeftToFile(currentobject,
							new SourceGenerator(new File(fullfilepathdeletelinkandshowleft), this.getAuthor(),
									this.getVersionid()),
							this);

					String fullfilepathdeletelinkandshowright = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName() + "ANDSHOWRIGHT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeletelinkandshowright);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteLinkAndShowRightToFile(currentobject,
							new SourceGenerator(new File(fullfilepathdeletelinkandshowright), this.getAuthor(),
									this.getVersionid()),
							this);

				}
				if (linkobjecttomaster) {
					String fullfilepathdeletelinkandshowleft = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName() + "ANDSHOWLEFT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeletelinkandshowleft);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteLinkToMasterAndShowLeftToFile(currentobject,
							new SourceGenerator(new File(fullfilepathdeletelinkandshowleft), this.getAuthor(),
									this.getVersionid()),
							this);

					String fullfilepathdeletelinkandshowright = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DELETE" + currentobject.getName() + "ANDSHOWRIGHT")
							+ "Action.java";
					logger.info("generating file " + fullfilepathdeletelinkandshowright);
					DataObjectDefinitionDeleteAndUpdate.generateDeleteLinkToMasterAndShowRightToFile(currentobject,
							new SourceGenerator(new File(fullfilepathdeletelinkandshowright), this.getAuthor(),
									this.getVersionid()),
							this);
				}
			}

			if (currentobject.isShowActionAutomaticallyGenerated()) {
				// prepare update action
				String fullfilepathprepareupdate = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("PREPAREUPDATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathprepareupdate);
				DataObjectDefinitionDeleteAndUpdate.generatePrepareupdateActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathprepareupdate), this.getAuthor(), this.getVersionid()),
						this);
				
				if (currentobject.getPropertyByName("TYPED")!=null) {
					Typed typed = (Typed) (currentobject.getPropertyByName("TYPED"));
					for (int c=0;c<typed.getCompanionNumber();c++) {
						DataObjectDefinition companion = typed.getCompanion(c);
						String fullfilepathprepareupdateforcompanion = srcautoactionfolder + "Atg"
								+ StringFormatter.formatForJavaClass("PREPAREUPDATE" + companion.getName()) + "Action.java";
						logger.info("generating file " + fullfilepathprepareupdateforcompanion);
						DataObjectDefinitionDeleteAndUpdate.generatePrepareupdateActionToFile(currentobject,companion,
								new SourceGenerator(new File(fullfilepathprepareupdateforcompanion), this.getAuthor(), this.getVersionid()),
								this);
					}
				}
				
				// update action
				String fullfilepathupdate = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("UPDATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathupdate);
				DataObjectDefinitionDeleteAndUpdate.generateUpdateActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathupdate), this.getAuthor(), this.getVersionid()), this);
				if (currentobject.getPropertyByName("TYPED")!=null) {
					Typed typed = (Typed) (currentobject.getPropertyByName("TYPED"));
					for (int c=0;c<typed.getCompanionNumber();c++) {
						DataObjectDefinition companion = typed.getCompanion(c);
						String fullfilepathcompanionupdate = srcautoactionfolder + "Atg"
								+ StringFormatter.formatForJavaClass("UPDATE" + companion.getName()) + "Action.java";
						logger.info("generating file " + fullfilepathcompanionupdate);
						DataObjectDefinitionDeleteAndUpdate.generateUpdateActionToFile(currentobject,companion,
								new SourceGenerator(new File(fullfilepathcompanionupdate), this.getAuthor(), this.getVersionid()), this);
					}
				}
				// flat file loading
				String fullfilepathflatfile = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("FLATFILELOADERFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathflatfile);
				DataObjectDefinitionFileActions.generateFlatFileLoaderToFile(currentobject.getName(),
						new SourceGenerator(new File(fullfilepathflatfile), this.getAuthor(), this.getVersionid()),
						this);
				// flat file sample generation

				String fullfilepathflatfilesample = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("GENERATEFLATFILESAMPLEFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathflatfilesample);
				DataObjectDefinitionFileActions.generateFlatFileSampleToFile(currentobject.getName(),
						new SourceGenerator(new File(fullfilepathflatfilesample), this.getAuthor(),
								this.getVersionid()),
						this);

				// mass update action
				String fullfilepathmassupdate = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathmassupdate);
				DataObjectDefinitionDeleteAndUpdate.generateMassUpdateActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathmassupdate), this.getAuthor(), this.getVersionid()),
						this);
			}
			LinkObject<?, ?> linkproperty = (LinkObject<?, ?>) currentobject.getPropertyByName("LINKOBJECT");
			AutolinkObject<?> autolinkproperty = (AutolinkObject<?>) currentobject.getPropertyByName("AUTOLINKOBJECT");
			LinkObjectToMaster<?, ?> linktomasterproperty = (LinkObjectToMaster<?, ?>) currentobject
					.getPropertyByName("LINKOBJECTTOMASTER");
			if (linkproperty != null) {
				// create link
				String fullfilepathcreatelink = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathcreatelink);
				DataObjectDefinitionOtherActions.generateCreateLinkActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathcreatelink), this.getAuthor(), this.getVersionid()),
						this);

				String fullfilepathcreatelinkandrightobject = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATELINKANDRIGHTOBJECTFOR" + currentobject.getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathcreatelink);
				DataObjectDefinitionOtherActions.generateCreateLinkAndRightObjectActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathcreatelinkandrightobject), this.getAuthor(),
								this.getVersionid()),
						this, linkproperty);

				String fullfilepathmassupdateandshowleft = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName() + "ANDSHOWLEFT")
						+ "Action.java";
				logger.info("generating file " + fullfilepathmassupdateandshowleft);
				DataObjectDefinitionDeleteAndUpdate.generateMassUpdateAndShowLeftToFile(currentobject,
						new SourceGenerator(new File(fullfilepathmassupdateandshowleft), this.getAuthor(),
								this.getVersionid()),
						this, linkproperty);

				String fullfilepathcreatelinkandshowright = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATE" + currentobject.getName() + "ANDSHOWRIGHT"
								+ linkproperty.getRightobjectforlink().getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathcreatelinkandshowright);

				DataObjectDefinitionOtherActions.generateCreateLinkActionAndShowRightToFile(currentobject,
						new SourceGenerator(new File(fullfilepathcreatelinkandshowright), this.getAuthor(),
								this.getVersionid()),
						this);
				if (!currentobject.isShowActionAutomaticallyGenerated()) {
					String fullfilepathmassupdate = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepathmassupdate);
					DataObjectDefinitionDeleteAndUpdate.generateMassUpdateActionToFile(currentobject,
							new SourceGenerator(new File(fullfilepathmassupdate), this.getAuthor(),
									this.getVersionid()),
							this);
				}

				// search action for right object, taking into account constraints if necessary
				if (linkproperty.getBusinessRuleNumber() > 0) {
					String fullfilepathsearchactionforrightobject = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(
									"SEARCHRIGHT" + linkproperty.getRightobjectforlink().getName().toUpperCase() + "FOR"
											+ currentobject.getName().toUpperCase())
							+ "Action.java";
					logger.info("generating file " + fullfilepathsearchactionforrightobject);
					currentobject.getSearchPagesAndActions().generateSearchActionForRightObjectLinkToFile(
							new SourceGenerator(new File(fullfilepathsearchactionforrightobject), this.getAuthor(),
									this.getVersionid()),
							this);
					String fullfilepathsearchactionforleftobject = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(
									"SEARCHLEFT" + linkproperty.getLeftobjectforlink().getName().toUpperCase() + "FOR"
											+ currentobject.getName().toUpperCase())
							+ "Action.java";
					logger.info("generating file " + fullfilepathsearchactionforleftobject);

					currentobject.getSearchPagesAndActions().generateSearchActionForLeftObjectLinkToFile(
							new SourceGenerator(new File(fullfilepathsearchactionforleftobject), this.getAuthor(),
									this.getVersionid()),
							this);

				}
				if (linkproperty.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT") != null) {
					@SuppressWarnings("rawtypes")
					ConstraintOnLinkObjectSameParent<?, ?> constraint = (ConstraintOnLinkObjectSameParent) linkproperty
							.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT");

					String fullfilepathsearchactionforrightobjectparent = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("SEARCH"
									+ linkproperty.getRightobjectforlink().getName().toUpperCase() + "WITHPARENT"
									+ constraint.getRightobjectparentproperty().getInstancename().toUpperCase())
							+ "Action.java";
					logger.info("generating file " + fullfilepathsearchactionforrightobjectparent);
					currentobject.getSearchPagesAndActions().generateSearchActionWithParentToFile(

							new SourceGenerator(new File(fullfilepathsearchactionforrightobjectparent),
									this.getAuthor(), this.getVersionid()),
							this);

				}

			}

			if (linktomasterproperty != null) {
				String fullfilepathcreatelink = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathcreatelink);
				DataObjectDefinitionOtherActions.generateCreateLinkToMasterActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathcreatelink), this.getAuthor(), this.getVersionid()),
						this);

				String fullfilepathcreatelinkandshowright = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATE" + currentobject.getName() + "ANDSHOWRIGHT"
								+ linktomasterproperty.getRightobjectforlink().getName())
						+ "Action.java";
				logger.info("generating file " + fullfilepathcreatelinkandshowright);

				DataObjectDefinitionOtherActions.generateCreateLinkToMasterActionAndShowRightToFile(currentobject,
						new SourceGenerator(new File(fullfilepathcreatelinkandshowright), this.getAuthor(),
								this.getVersionid()),
						this);

				if (!currentobject.isShowActionAutomaticallyGenerated()) {
					String fullfilepathmassupdate = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepathmassupdate);
					DataObjectDefinitionDeleteAndUpdate.generateMassUpdateActionToFile(currentobject,
							new SourceGenerator(new File(fullfilepathmassupdate), this.getAuthor(),
									this.getVersionid()),
							this);
				}
				String fullfilepathmassupdateandshowleft = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("MASSUPDATE" + currentobject.getName() + "ANDSHOWLEFT")
						+ "Action.java";
				logger.info("generating file " + fullfilepathmassupdateandshowleft);
				DataObjectDefinitionDeleteAndUpdate.generateMassUpdateLinkToMasterAndShowLeftToFile(currentobject,
						new SourceGenerator(new File(fullfilepathmassupdateandshowleft), this.getAuthor(),
								this.getVersionid()),
						this, linktomasterproperty);

			}

			if (autolinkproperty != null) {
				// create link
				String fullfilepathprepareupdate = srcautoactionfolder + "Atg"
						+ StringFormatter.formatForJavaClass("CREATE" + currentobject.getName()) + "Action.java";
				logger.info("generating file " + fullfilepathprepareupdate);
				DataObjectDefinitionOtherActions.generateCreateAutolinkActionToFile(currentobject,
						new SourceGenerator(new File(fullfilepathprepareupdate), this.getAuthor(), this.getVersionid()),
						this);
				// search action for right object, taking into account constraints if necessary
				if (autolinkproperty.getBusinessRuleNumber() > 0) {
					String fullfilepathsearchactionforrightobject = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass(
									"SEARCHRIGHT" + autolinkproperty.getObjectforlink().getName().toUpperCase() + "FOR"
											+ currentobject.getName().toUpperCase())
							+ "Action.java";
					logger.info("generating file " + fullfilepathsearchactionforrightobject);
					currentobject.getSearchPagesAndActions().generateSearchActionForRightObjectAutolinkToFile(
							new SourceGenerator(new File(fullfilepathsearchactionforrightobject), this.getAuthor(),
									this.getVersionid()),
							this);
				}

				if (!autolinkproperty.isSymetricLink()) {
					String fullfilepathshowautolinktree = srcautoactionfolder + "Atg" + StringFormatter
							.formatForJavaClass("SHOWAUTOLINKTREEFOR" + currentobject.getName().toUpperCase())
							+ "Action.java";
					DataObjectDefinitionOtherActions.generateShowAutoLinkTreeToFile(currentobject.getName(),
							new SourceGenerator(new File(fullfilepathshowautolinktree), this.getAuthor(),
									this.getVersionid()),
							this, autolinkproperty);
				}
			}

			if (currentobject.isUniqueIdentified())
				if (((linkproperty == null) && (autolinkproperty == null) && (linktomasterproperty == null))
						|| (currentobject.isShowActionAutomaticallyGenerated())) {
					String fullfilepathpreparestandardcreateaction = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("PREPARESTANDARDCREATE" + currentobject.getName())
							+ "Action.java";
					logger.info("generating file " + fullfilepathpreparestandardcreateaction);
					DataObjectDefinitionOtherActions.generatePrepareStandardCreateActionToFile(currentobject,
							new SourceGenerator(new File(fullfilepathpreparestandardcreateaction), this.getAuthor(),
									this.getVersionid()),
							this);

					String fullfilepathstandardcreateaction = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("STANDARDCREATE" + currentobject.getName())
							+ "Action.java";
					;
					logger.info("generating file " + fullfilepathstandardcreateaction);
					DataObjectDefinitionOtherActions.generateStandardCreateActionToFile(currentobject,
							new SourceGenerator(new File(fullfilepathstandardcreateaction), this.getAuthor(),
									this.getVersionid()),
							this);

					String fullfilepathduplicateaction = srcautoactionfolder + "Atg"
							+ StringFormatter.formatForJavaClass("DUPLICATE" + currentobject.getName()) + "Action.java";
					;
					logger.info("generating file " + fullfilepathduplicateaction);
					DataObjectDefinitionOtherActions.generateDuplicateActionToFile(currentobject, new SourceGenerator(
							new File(fullfilepathduplicateaction), this.getAuthor(), this.getVersionid()), this);

				}

		}

		// *************************** OBJECTS
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			logger.info("generating file for object " + thisobject.getName());
			thisobject.generateToFile(new SourceGenerator(
					new File(srcobjectfolder + StringFormatter.formatForJavaClass(thisobject.getName()) + ".java"),
					this.getAuthor(), this.getVersionid()), this);
			thisobject.generateDefinitionToFile(new SourceGenerator(new File(
					srcobjectfolder + StringFormatter.formatForJavaClass(thisobject.getName()) + "Definition.java"),
					this.getAuthor(), this.getVersionid()), this);
			logger.info("generated file " + "." + srcobjectfolder
					+ StringFormatter.formatForJavaClass(thisobject.getName()) + ".java");
			for (int j = 0; j < thisobject.getPropertySize(); j++) {
				Property<?> thisproperty = thisobject.getPropertyAt(j);
				if (thisproperty.getPropertyHelperName() != null) {
					String helpername = thisproperty.getPropertyHelperName();
					logger.info("generating helper class " + helpername + " for object " + thisobject.getName());
					thisproperty.generatePropertyHelperToFile(
							new SourceGenerator(new File(srcobjectfolder + helpername + ".java"), this.getAuthor(),
									this.getVersionid()),
							this);
				}
			}
		}
		// *************************** SEQUENCES
		for (int i = 0; i < this.getSequenceNumber(); i++) {
			SequenceDefinition thissequence = this.getSequence(i);
			logger.info("generating file for sequence " + thissequence.getName());
			String path = srcsequencefolder + StringFormatter.formatForJavaClass(thissequence.getName())
					+ "Sequence.java";
			SourceGenerator sg = new SourceGenerator(new File(path), this.getAuthor(), this.getVersionid());
			thissequence.generateLaunchSearchActionToFile(sg, this);
			logger.info("generated file " + path);
		}

		// *************************** LIST OF VALUES
		for (int i = 0; i < this.getChoiceNumber(); i++) {
			ChoiceCategory thischoice = this.getChoice(i);
			logger.info("generating file for choice " + thischoice.getName() + " of class "
					+ thischoice.getDefinitionClass());
			thischoice.generatetoFile(new SourceGenerator(new File(srcchoicefolder
					+ StringFormatter.formatForJavaClass(thischoice.getName()) + "ChoiceDefinition.java"),
					this.getAuthor(), this.getVersionid()), this);
			logger.info("generated file " + srcchoicefolder + StringFormatter.formatForJavaClass(thischoice.getName())
					+ ".java");
		}
		// *************************** PAGE
		for (int i = 0; i < this.getPageNumber(); i++) {
			PageDefinition thispage = this.getPage(i);
			logger.info("generating file for page " + thispage.getName());
			thispage.generateToFile(new SourceGenerator(new File(
					srcpagefolder + "Abs" + StringFormatter.formatForJavaClass(thispage.getName()) + "Page.java"),
					this.getAuthor(), this.getVersionid()), this);
			logger.info("generating file for page " + thispage.getName());

		}
		// AUTOMATICALLY GENERATED AUTO PAGES
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition currentobject = this.getObject(i);
			if (currentobject.isShowActionAutomaticallyGenerated()) {
				String fullfilepath = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOW" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilepath);
				DataObjectDefinitionShowPage showpage = new DataObjectDefinitionShowPage(currentobject);
				showpage.generateToFile(
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

				if (currentobject.getPropertyByName("TYPED")!=null) {
					Typed typed = (Typed) currentobject.getPropertyByName("TYPED");
					for (int c=0;c<typed.getCompanionNumber();c++) {
						DataObjectDefinition companion = typed.getCompanion(c);
						String fullfilecompanionpath = srcautopagefolder + "Atg"
								+ StringFormatter.formatForJavaClass("SHOW" + companion.getName()) + "Page.java";
						logger.info("generating file " + fullfilecompanionpath);
						DataObjectDefinitionShowPage showcompanionpage = new DataObjectDefinitionShowPage(currentobject,companion);
						showcompanionpage.generateToFile(
								new SourceGenerator(new File(fullfilecompanionpath), this.getAuthor(), this.getVersionid()), this);
					}
				}
				
				String fullfilepathsearch = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("SEARCH" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilepathsearch);
				currentobject.getSearchPagesAndActions().generateSearchPageToFile(
						new SourceGenerator(new File(fullfilepathsearch), this.getAuthor(), this.getVersionid()), this);

				String fullfilestandardcreate = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("STANDARDCREATE" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilestandardcreate);
				(new DataObjectDefinitionCreatePageToFile(currentobject)).generateToFile(
						new SourceGenerator(new File(fullfilestandardcreate), this.getAuthor(), this.getVersionid()),
						this);
			}
			if (currentobject.isShowActionAutomaticallyGenerated()) {
				String fullfilepath = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("UPDATE" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilepath);
				(new DataObjectDefinitionUpdatePage(currentobject)).generateToFile(
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);
				if (currentobject.getPropertyByName("TYPED")!=null) {
					Typed typed = (Typed) (currentobject.getPropertyByName("TYPED"));
					for (int c=0;c<typed.getCompanionNumber();c++) {
						DataObjectDefinition companion = typed.getCompanion(c);
						String fullfilepathcompanion = srcautopagefolder + "Atg"
								+ StringFormatter.formatForJavaClass("UPDATE" + companion.getName()) + "Page.java";
						logger.info("generating file " + fullfilepathcompanion);
						(new DataObjectDefinitionUpdatePage(currentobject,companion)).generateToFile(
								new SourceGenerator(new File(fullfilepathcompanion), this.getAuthor(), this.getVersionid()), this);
					}
				}
			}

			if ((currentobject.IsIterated()) || (currentobject.isVersioned())) {
				String fullfilepath = srcautopagefolder + "Atg"
						+ StringFormatter.formatForJavaClass("SHOWHISTORYFOR" + currentobject.getName()) + "Page.java";
				logger.info("generating file " + fullfilepath);
				new DataObjectDefinitionShowHistoryPage(currentobject).generateToFile(
						new SourceGenerator(new File(fullfilepath), this.getAuthor(), this.getVersionid()), this);

			}

		}
		for (int i = 0; i < this.constraints.getSize(); i++) {
			MultiFieldConstraint thisconstraint = this.constraints.get(i);
			logger.info("generating file for constraint " + thisconstraint.getName());
			String path = srcutilityfolder + "Abs" + StringFormatter.formatForJavaClass(thisconstraint.getName())
					+ "MultiFieldConstraint.java";
			logger.info("generating file " + path);
			SourceGenerator sg = new SourceGenerator(new File(path), this.getAuthor(), this.getVersionid());
			thisconstraint.generateFile(sg, this);

		}
		for (int i = 0; i < this.triggerlist.size(); i++) {
			Trigger thistrigger = triggerlist.get(i);
			logger.info("generating file for trigger " + thistrigger.getName());
			String path = srcutilityfolder + "Abs" + StringFormatter.formatForJavaClass(thistrigger.getInstancename())
					+ "Trigger.java";
			logger.info("generating file " + path);
			SourceGenerator sg = new SourceGenerator(new File(path), this.getAuthor(), this.getVersionid());
			thistrigger.generatePropertyHelperToFile(sg, this);
		}
		logger.info(
				"-------------------------------- generating pages and actions for advanced features -----------------");
		for (int i = 0; i < this.advanceddesignfeatures.getSize(); i++) {
			AdvancedDesignFeature thisadvancedfeature = this.advanceddesignfeatures.get(i);
			thisadvancedfeature.generateActionsAndPagesToFile(srcautoactionfolder, srcpagefolder, this.author,
					this.versionid);
		}

	}

	/**
	 * This allows to launch from command line the generation of an application
	 * 
	 * @param args an aray of strings with one element indicating the full path of
	 *             the main module of the application (e.g.
	 *             com.mycompany.myapp.Mymodule )
	 */
	public static void main(String args[]) {
		long starttime = new Date().getTime();
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(new ConsoleFormatter());
		handler.setLevel(Level.INFO);
		Logger mainlogger = Logger.getLogger("");
		for (int i = 0; i < mainlogger.getHandlers().length; i++) {
			mainlogger.removeHandler(mainlogger.getHandlers()[i]);
		}
		mainlogger.addHandler(handler);

		if (args.length == 0) {
			logger.severe("Error : syntax java org.openlowcode.design.module.Module class1 [class2] ...");
			logger.severe(
					"where class1, class2 and following  are the full class (with path) of the main module of your application");
			System.exit(1);
		}
		String[] classpathlist = args;
		ArrayList<String> successfullmodules = new ArrayList<String>();
		ArrayList<String> errormodules = new ArrayList<String>();
		for (int i = 0; i < classpathlist.length; i++) {
			String classpath = classpathlist[i];
			try {
				logger.info("will try to launch generation for class = " + classpath);
				Class<?> moduleclass = Class.forName(classpath);
				logger.info("Class generated");

				Object module = moduleclass.newInstance();
				logger.info("Object generated");

				Module castedmodule = (Module) module;
				castedmodule.finalizemodel();
				castedmodule.generateSources();
				successfullmodules.add(classpath);
			} catch (Throwable e) {
				errormodules.add(classpath + " - " + e.getMessage() + "\n    - " + e.getStackTrace()[0] + "\n    - "
						+ (e.getStackTrace().length > 1 ? e.getStackTrace()[1] : ""));
				logger.severe("Exception " + e.getMessage());
				for (int s = 0; s < e.getStackTrace().length; s++) {
					logger.severe(" - " + e.getStackTrace()[s].toString());
				}

			}
		}
		long endtime = new Date().getTime();
		long executioninsec = (endtime - starttime) / 1000;
		System.err.println(" *** Generation Report *** " + args.length + " modules in " + executioninsec + "s");
		for (int i = 0; i < successfullmodules.size(); i++)
			System.err.println(" SUCCESS " + successfullmodules.get(i));
		for (int i = 0; i < errormodules.size(); i++)
			System.err.println(" ERROR " + errormodules.get(i));
	}

	private void finalizemodel() {
		for (int i = 0; i < this.getObjectNumber(); i++) {
			DataObjectDefinition thisobject = this.getObject(i);
			thisobject.finalizemodel();
		}

	}

	/**
	 * sets the system module. THis is used only during the definition of the system
	 * module
	 * 
	 * @param systemmodulefromconstructor the System Module (itself)
	 */
	public static void setSystemModule(SystemModule systemmodulefromconstructor) {
		systemmodule = systemmodulefromconstructor;
	}

	/**
	 * gets the system module
	 * 
	 * @return the system module
	 */
	public static SystemModule getSystemModule() {
		if (systemmodule == null) {
			synchronized (SystemModule.class) {
				if (systemmodule == null) {
					new SystemModule();
				}
			}
		}
		return systemmodule;
	}

	private ArrayList<Trigger> triggerlist;

	/**
	 * adds a trigger to the module
	 * 
	 * @param trigger trigger to add
	 */
	public void addTrigger(Trigger trigger) {
		this.triggerlist.add(trigger);

	}
}
