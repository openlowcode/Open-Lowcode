/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.design;

import org.openlowcode.OLcVersionGenerator;
import org.openlowcode.design.access.Anarchy;
import org.openlowcode.design.access.ObjectPersonalPrivilege;
import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.design.access.UnconditionalPrivilege;
import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.action.StaticActionDefinition;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalField;
import org.openlowcode.design.data.EncryptedStringField;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.IntegerField;
import org.openlowcode.design.data.LargeBinaryField;
import org.openlowcode.design.data.SequenceDefinition;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.data.TimestampField;
import org.openlowcode.design.data.TransitionChoiceCategory;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.BooleanArgument;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.FaultyStringArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.NodeTreeArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.properties.basic.AutoNamingRule;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkUniqueForLeftAndRight;
import org.openlowcode.design.data.properties.basic.CreationLog;
import org.openlowcode.design.data.properties.basic.GenericLink;
import org.openlowcode.design.data.properties.basic.Iterated;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedToDefaultParent;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Named;
import org.openlowcode.design.data.properties.basic.Numbered;
import org.openlowcode.design.data.properties.basic.Personal;
import org.openlowcode.design.data.properties.basic.StoredObject;
import org.openlowcode.design.data.properties.basic.TargetDate;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.data.properties.basic.UpdateLog;
import org.openlowcode.design.data.stringpattern.ConditionStringNotEmpty;
import org.openlowcode.design.data.stringpattern.ConditionalElement;
import org.openlowcode.design.data.stringpattern.ConstantElement;
import org.openlowcode.design.data.stringpattern.StringFieldElement;
import org.openlowcode.design.data.stringpattern.StringPattern;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.AddonPageDefinition;
import org.openlowcode.design.pages.DynamicPageDefinition;
import org.openlowcode.design.pages.StaticPageDefinition;
import org.openlowcode.design.utility.SystemAttributeInit;

/**
 * The System module performs all common administrative tasks on the server,
 * including security, e-mail sending, workflows...
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SystemModule
		extends
		Module {

	private SimpleChoiceCategory loglevelcategory;
	private SimpleChoiceCategory booleanchoice;
	private SimpleChoiceCategory controllevel;
	private SimpleChoiceCategory applicationlocale;
	private SimpleChoiceCategory preferedfileencoding;
	private SimpleChoiceCategory reportingfrequency;
	private DataObjectDefinition appuser;
	private DataObjectDefinition usersession;
	private DataObjectDefinition usergroup;
	private DataObjectDefinition groupmemberlink;
	private DataObjectDefinition groupadministratorlink;
	private DataObjectDefinition authority;
	private DataObjectDefinition groupswithauthority;
	private DataObjectDefinition domain;
	private DataObjectDefinition binaryfile;
	private DataObjectDefinition objectattachment;
	private DataObjectDefinition task;
	private DataObjectDefinition workflow;
	private DataObjectDefinition taskchoice;
	private DataObjectDefinition emailqueue;
	private DataObjectDefinition emailrecipient;
	private DataObjectDefinition workcalendar;
	private DataObjectDefinition holidayset;
	private DataObjectDefinition holiday;
	private DataObjectDefinition weeklyworkingslots;
	private DataObjectDefinition csvloadererror;
	private DataObjectDefinition moduleusage;
	private DataObjectDefinition basicdiagramrecord;
	private TotalAuthority sovereignauthority;

	private TransitionChoiceCategory defaultlifecycle;

	/**
	 * @return the sovereign (supreme) authority of the server
	 */
	public TotalAuthority getSovereign() {
		return sovereignauthority;
	}

	/**
	 * @return boolean choice category
	 */
	public SimpleChoiceCategory getBooleanChoice() {
		return booleanchoice;
	}

	/**
	 * @return control level choice category
	 */
	public SimpleChoiceCategory getControlLevel() {
		return controllevel;
	}

	/**
	 * @return application locale choice category
	 */
	public SimpleChoiceCategory getApplicationLocale() {
		return applicationlocale;
	}

	/**
	 * @return the log level choice category (from java logging)
	 */
	public SimpleChoiceCategory getLogLevel() {
		return this.loglevelcategory;
	}

	/**
	 * @return the reporting frequency choice category
	 */
	public SimpleChoiceCategory getReportingFrequency() {
		return this.reportingfrequency;
	}

	/**
	 * @return the prefered file encoding choice category
	 */
	public SimpleChoiceCategory getPreferedFileEncoding() {
		return this.preferedfileencoding;
	}

	/**
	 * @return the default lifecycle of the application
	 */
	public TransitionChoiceCategory getDefaultLifecycle() {
		return defaultlifecycle;
	}

	/**
	 * @return the module usage data object
	 */
	public DataObjectDefinition getModuleUsage() {
		return this.moduleusage;
	}

	/**
	 * @return the basic diagram record data object
	 */
	public DataObjectDefinition getBasicDiagramRecord() {
		return this.basicdiagramrecord;
	}

	/**
	 * @return the data object loading flat file (actually CSV and spreadsheet)
	 */
	public DataObjectDefinition getCSVLoaderError() {
		return this.csvloadererror;
	}

	/**
	 * @return the work calendar data object (used for Schedule property)
	 */
	public DataObjectDefinition getWorkcalendar() {
		return this.workcalendar;
	}

	/**
	 * @return the data object holding weekly working slot (used for Schedule
	 *         property)
	 */
	public DataObjectDefinition getWeeklyworkingslots() {
		return this.weeklyworkingslots;
	}

	/**
	 * @return the data object holding holiday sets (used for Schedule property)
	 */
	public DataObjectDefinition getHolidaySet() {
		return this.holidayset;
	}

	/**
	 * @return the data object holding a holiday (used for Schedule property)
	 */
	public DataObjectDefinition getHoliday() {
		return this.holiday;
	}

	/**
	 * @return the data object storing object attachments (file content is stored in
	 *         a dedicated binary file data object, while owning data object will
	 *         just have the meta-data). ObjectAttachment is the link between all
	 */
	public DataObjectDefinition getObjectAttachment() {
		return this.objectattachment;
	}

	/**
	 * @return the task choice data object (the possibilities given for a user in a
	 *         workflow task)
	 */
	public DataObjectDefinition getTaskChoice() {
		return this.taskchoice;
	}

	/**
	 * @return the binary file data object
	 */
	public DataObjectDefinition getBinaryFile() {
		return binaryfile;
	}

	/**
	 * Provides the system package app-user. Every interaction with the application
	 * requires to identify with a app-user
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getAppuser() {
		return appuser;
	}

	/**
	 * A single connection of an user to the application. The application stores a
	 * line per user connection with status and connection date
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getUsersession() {
		return usersession;
	}

	/**
	 * A group gathering users.
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getUsergroup() {
		return usergroup;
	}

	/**
	 * a link between an appuser and a group, defining all members of the group
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getGroupmemberlink() {
		return groupmemberlink;
	}

	/**
	 * a link between an appuser and a group, defining all administrators of the
	 * group
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getGroupadministratorlink() {
		return groupadministratorlink;
	}

	/**
	 * an authority provides the right to perform some actions for the objects
	 * related to the domain
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getAuthority() {
		return authority;
	}

	/**
	 * a link between groups and authorities, defining all groups that are granted
	 * the autority defined
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getGroupswithauthority() {
		return groupswithauthority;
	}

	/**
	 * A domain is an area of the application that is subject to the same user
	 * access rights
	 * 
	 * @return the definition object
	 */
	public DataObjectDefinition getDomain() {
		return domain;
	}

	/**
	 * @return
	 */
	public DataObjectDefinition getTask() {
		return task;
	}

	/**
	 * @return
	 */
	public DataObjectDefinition getWorkflow() {
		return workflow;
	}

	public final ChoiceValue DEF_OPEN = new ChoiceValue("OPEN", "Open",
			"Task has been created but actual work has not started");
	public final ChoiceValue DEF_INWORK = new ChoiceValue("INWORK", "In work", "Work on the task has started");
	public final ChoiceValue DEF_COMPLETED = new ChoiceValue("COMPLETED", "Completed",
			"Task is finished according to original description");
	public final ChoiceValue DEF_CANCELED = new ChoiceValue("CANCELED", "Canceled",
			"Task has been canceled as the original request did not make sense");
	private TotalAuthority calendarmanager;
	private TotalAuthority calendarviewer;
	private SimpleChoiceCategory lastversion;

	/**
	 * @return the choice category specifying if the version is the last or not
	 */
	public SimpleChoiceCategory getLastVersionChoice() {
		return this.lastversion;
	}

	/**
	 * @return the Calendar Manager total authority (to add this authority to other
	 *         modules groups)
	 */
	public TotalAuthority getCalendarmanager() {
		return calendarmanager;
	}

	/**
	 * @return the Calendar member total authority (to add this authority to other
	 *         modules groups)
	 */
	public TotalAuthority getCalendarviewer() {
		return calendarviewer;
	}

	/**
	 * Creates the System Module of the application
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SystemModule() {
		super("SYSTEMMODULE", "S0", "System", "org.openlowcode.module.system", "Open Lowcode SAS", OLcVersionGenerator.version,
				"On this server, only the system module has been setup. To build your application, you should create your own module "
						+ "(subclass of org.openlowcode.design.module.Module) and run the code generation tool.");
		defineSystemAttributes();
		this.sovereignauthority = new TotalAuthority("SOVEREIGN", "Sovereign",
				"Sovereign authority gets full access to all the modules in the platform. Some actions may result in inconsistent or lost data.");

		reportingfrequency = new SimpleChoiceCategory("REPORTINGFREQUENCY", 8);
		reportingfrequency.addValue(new ChoiceValue("DAILY", "Daily", ""));
		reportingfrequency.addValue(new ChoiceValue("WEEKLY", "Weekly", ""));
		reportingfrequency.addValue(new ChoiceValue("MONTHLY", "Monthly", ""));
		this.addChoiceCategory(reportingfrequency);

		lastversion = new SimpleChoiceCategory("LATESTVERSION", 1);

		lastversion.addValue(new ChoiceValue("Y", "Last", "Only selects last version"));

		this.addChoiceCategory(lastversion);

		booleanchoice = new SimpleChoiceCategory("BOOLEAN", 5);
		ChoiceValue yes = new ChoiceValue("YES", "Yes", "");
		ChoiceValue no = new ChoiceValue("NO", "No", "");
		ChoiceValue unknown = new ChoiceValue("UNKN", "Unknown", "");

		booleanchoice.addValue(yes);
		booleanchoice.addValue(no);
		booleanchoice.addValue(unknown);

		this.addChoiceCategory(booleanchoice);

		applicationlocale = new SimpleChoiceCategory("APPLOCALE", 8);
		applicationlocale.addValue(new ChoiceValue("US", "United States",
				"US locale. Manages ',' as csv separator and '.' as decimal separator"));
		applicationlocale.addValue(new ChoiceValue("FR", "France and Germany",
				"French and German locale. Manages ';' as csv separator and ',' as decimal separator"));

		this.addChoiceCategory(applicationlocale);

		preferedfileencoding = new SimpleChoiceCategory("PREFEREDFILEENCODING", 8);
		preferedfileencoding.addValue(new ChoiceValue("UTF8", "UTF-8", "Standard Unicode"));
		preferedfileencoding.addValue(
				new ChoiceValue("CP1522", "Windows Ansi (CP1522)", "Standard Windows West European encoding"));
		this.addChoiceCategory(preferedfileencoding);

		controllevel = new SimpleChoiceCategory("CONTROLLEVEL", 8);
		ChoiceValue okcontrol = new ChoiceValue("VALID", "Valid", "");
		ChoiceValue warningcontrol = new ChoiceValue("WARNING", "Warning", "");
		ChoiceValue errorcontrol = new ChoiceValue("ERROR", "Error", "");
		controllevel.addValue(okcontrol);
		controllevel.addValue(warningcontrol);
		controllevel.addValue(errorcontrol);
		this.addChoiceCategory(controllevel);

		Module.setSystemModule(this);
		this.setDataInitializationFlag(true);

		// --- create objects that are referenced in properties
		this.appuser = new DataObjectDefinition("appuser", "Application User", this, false);
		this.addPrivilege(new Anarchy(appuser.getLookupActionGroup()));
		this.binaryfile = new DataObjectDefinition("BINARYFILE", "Binary File", this, true);

		StringField lastname = new StringField("LASTNAME", "Last Name",
				"field to store the last name. This field allows fast reseatch", 80, StringField.INDEXTYPE_EASYSEARCH,
				920);
		// replaced by name pattern
		// lastname.setShowInTitle();
		appuser.addField(lastname);
		StringField firstname = new StringField("FIRSTNAME", "First Name",
				"field to store first name but also if necessary middle name", 80, StringField.INDEXTYPE_NONE, 910);
		// replaced by name pattern
		// firstname.setShowInTitle();
		appuser.addField(firstname);

		appuser.addField(new StringField("EMAIL", "E-Mail",
				"e-mail at which the user can be reached. The system may send e-mails at this address", 80,
				StringField.INDEXTYPE_EASYSEARCH));
		appuser.addField(new StringField("COMPANY", "Company", "Legal Entity the user belongs to", 80,
				StringField.INDEXTYPE_NONE));
		appuser.addField(new StringField("DEPARTMENT", "Department",
				"department the user belongs to. This field is a key to reporting", 80, StringField.INDEXTYPE_NONE));
		appuser.addField(new EncryptedStringField("PASSWORD", "Password",
				"the password of the user, that will be stored as encrypted into the database", 30,
				EncryptedStringField.ENCRYPTION_ONEWAY));
		appuser.addField(new StringField("OTPUSER", "OTP User Id", "The user id to use for otp connection if different from user id", 80,
				StringField.INDEXTYPE_NONE));
		appuser.addProperty(new StoredObject());
		appuser.addProperty(new UniqueIdentified());
		appuser.addProperty(new Numbered("Personal Id"));
		Named appusername = new Named();
		StringPattern appusernamestringpattern = new StringPattern();
		appusernamestringpattern.addPatternElement(new StringFieldElement(lastname));
		appusernamestringpattern.addPatternElement(new ConditionalElement(new ConditionStringNotEmpty(firstname),
				new ConstantElement(", "), new ConstantElement("")));
		appusernamestringpattern.addPatternElement(new StringFieldElement(firstname));
		appusername.addBusinessRule(new AutoNamingRule(appusernamestringpattern, appusername));
		appuser.addProperty(appusername);
		appuser.addProperty(new CreationLog());
		appuser.addProperty(new UpdateLog());
		appuser.addProperty(new Iterated());
		appuser.addField(new ChoiceField("PREFLANG", "Prefered Language",
				"User prefered language for import and export", applicationlocale));
		appuser.addField(new ChoiceField("PREFFILEENC", "Prefered File Encoding",
				"Prefered file encoding for csv import", preferedfileencoding));
		SimpleChoiceCategory authtype = new SimpleChoiceCategory("AUTHTYPE", 16);
		authtype.addValue(new ChoiceValue("LOCAL", "Local authentication", "will use the password on the database"));
		authtype.addValue(new ChoiceValue("LDAP", "Enterprise LDAP", "password is stored on the enterprise LDAP"));
		this.addChoiceCategory(authtype);
		appuser.addField(
				new ChoiceField("AUTHTYPE", "Authentication type", "the process used to check the password", authtype));
		appuser.addField(new StringField("LDAPFULLNAME", "Qualified LDAP Name",
				"when LDAP authentication is used, this stores the fully qualified LDAP name", 512,
				StringField.INDEXTYPE_NONE));
		appuser.setForcedRowHeightForTable(1);

		// binary file storage

		binaryfile.addProperty(new StoredObject());
		binaryfile.addProperty(new UniqueIdentified());
		binaryfile.addProperty(new CreationLog());
		binaryfile.addProperty(new UpdateLog());
		binaryfile.addField(new StringField("FILENAME", "File name",
				"the name of the file as will be created in the file system", 255, StringField.INDEXTYPE_NONE));
		binaryfile.addField(new DecimalField("FILESIZE", "File Size", "the file size in bytes", 12, 0,
				DecimalField.INDEXTYPE_NONE));
		binaryfile.addField(new LargeBinaryField("FILECONTENT", "File Content",
				"the file binary content, as provided by a client. No provision is made for difference of return carriage between UNIX and Windows"));

		objectattachment = new DataObjectDefinition("OBJATTACHMENT", "Attachments", this, true);
		objectattachment.addProperty(new StoredObject());
		objectattachment.addProperty(new UniqueIdentified());
		objectattachment.addProperty(new CreationLog());
		objectattachment.addProperty(new UpdateLog());
		objectattachment.addProperty(new GenericLink("OWNER"));
		objectattachment.addProperty(new LinkedToParent("CONTENT", binaryfile));
		objectattachment.addField(new StringField("FILENAME", "File name",
				"the name of the file as will be created in the file system", 255, StringField.INDEXTYPE_NONE));
		objectattachment.addField(new StringField("FILESIZE", "File Size", "the file size in readable format", 12, 0,
				DecimalField.INDEXTYPE_NONE));
		objectattachment.addField(new StringField("COMMENT", "Comment",
				"A comment entered by the user or the process generating the attachment", 800,
				StringField.INDEXTYPE_NONE));

		DynamicActionDefinition getfile = new DynamicActionDefinition("GETFILE", "gets the file from fileid");
		getfile.addInputArgument(new ObjectIdArgument("FILEID", binaryfile));
		getfile.addOutputArgument(new LargeBinaryArgument("FILE", false));

		this.addAction(getfile);

		// usersession

		this.usersession = new DataObjectDefinition("usersession", "User Session", this, true);
		usersession.addField(new TimestampField("STARTTIME", "Session start", "the server time the session was opened",
				TimestampField.INDEXTYPE_RAWINDEXWITHSEARCH));
		usersession.addField(new TimestampField("ENDTIME", "Session end", "the server time the session was ended",
				TimestampField.INDEXTYPE_NONE));
		usersession.addField(new TimestampField("LASTACTION", "Last action",
				"the last time a request was made to server for this session", TimestampField.INDEXTYPE_RAWINDEXWITHSEARCH));
		usersession
				.addField(new IntegerField("ACTIONS", "Action number", "the number of interactions with the server"));
		usersession.addField(new StringField("CLIENTIP", "Client IP", "the IP of the client as visible from the server",
				80, StringField.INDEXTYPE_RAWINDEX));
		usersession.addField(new StringField("CLIENTPID", "Client PID",
				"the PID of the client. This is generated by the server the first time a client connects", 80,
				StringField.INDEXTYPE_RAWINDEX));
		usersession.addProperty(new StoredObject());
		usersession.addProperty(new UniqueIdentified());
		LinkedToParent sessionuserlinkedtoparent = new LinkedToParent("sessionuser", appuser);
		usersession.addProperty(sessionuserlinkedtoparent);

		this.moduleusage = new DataObjectDefinition("MODULEUSAGE", "Module Usage", this);
		moduleusage.addProperty(new StoredObject());
		moduleusage.addProperty(new UniqueIdentified());
		TimestampField dayformoduleusage = new TimestampField("DAY", "Day", "Day of the usage",
				TimestampField.INDEXTYPE_RAWINDEX, 400, false);
		StringField moduleformoduleusage = new StringField("MODULE", "Module", "Name of the module", 80,
				StringField.INDEXTYPE_RAWINDEX, 300);
		moduleusage.addField(dayformoduleusage);
		moduleusage.addField(moduleformoduleusage);
		moduleusage.addField(new IntegerField("ACTIONNR", "Number of Actions",
				"counts each interaction with the server on the module", 200));
		LinkedToParent moduleusagelinkedtoparent = new LinkedToParent("sessionuser", appuser);
		moduleusage.addProperty(moduleusagelinkedtoparent);
		moduleusagelinkedtoparent.addCompositeIndex("USERTIME", new Field[] { dayformoduleusage });

		this.basicdiagramrecord = new DataObjectDefinition("BASICDIAGRAMRECORD", "Basic Diagram Record", this, true);
		basicdiagramrecord.addField(new TimestampField("RECORDDATE", "Date", "Date of the record",
				TimestampField.INDEXTYPE_NONE, 400, false));
		basicdiagramrecord
				.addField(new StringField("CATEGORY", "Category", "Category", 80, StringField.INDEXTYPE_NONE, 300));
		basicdiagramrecord.addField(new DecimalField("VALUE", "Value", "Value", 36, 12, DecimalField.INDEXTYPE_NONE));

		StaticActionDefinition showmoduleusagestat = new StaticActionDefinition("MODULEUSAGESUMMARY");
		showmoduleusagestat.addOutputArgument(new ArrayArgument(new ObjectArgument("STATTABLE", basicdiagramrecord)));
		showmoduleusagestat.addOutputArgument(new ChoiceArgument("EXCLUDEADMIN", this.booleanchoice));
		showmoduleusagestat.addOutputArgument(new ChoiceArgument("FREQUENCY", reportingfrequency));
		showmoduleusagestat.addOutputArgument(new IntegerArgument("HISTORY"));

		this.addAction(showmoduleusagestat);

		DynamicActionDefinition specificmoduleusagestat = new DynamicActionDefinition("SPECIFICMODULEUSAGESUMMARY");
		specificmoduleusagestat.addInputArgument(new ChoiceArgument("EXCLUDEADMIN", this.booleanchoice));
		specificmoduleusagestat.addInputArgument(new ChoiceArgument("FREQUENCY", reportingfrequency));
		specificmoduleusagestat.addInputArgument(new IntegerArgument("HISTORY"));

		specificmoduleusagestat
				.addOutputArgument(new ArrayArgument(new ObjectArgument("STATTABLE", basicdiagramrecord)));
		specificmoduleusagestat.addOutputArgument(new ChoiceArgument("EXCLUDEADMIN_THRU", this.booleanchoice));
		specificmoduleusagestat.addOutputArgument(new ChoiceArgument("FREQUENCY_THRU", reportingfrequency));
		specificmoduleusagestat.addOutputArgument(new IntegerArgument("HISTORY_THRU"));
		this.addAction(specificmoduleusagestat);

		DynamicPageDefinition showmoduleusage = new DynamicPageDefinition("MODULEUSAGESUMMARY");
		showmoduleusage.linkPageToAction(showmoduleusagestat);
		this.AddPage(showmoduleusage);

		StaticActionDefinition closesession = new StaticActionDefinition("closesession");
		this.addAction(closesession);

		StaticActionDefinition showcurrentuser = new StaticActionDefinition("showcurrentuser",
				"show current user stats and groups");
		showcurrentuser.addOutputArgument(new ObjectIdArgument("ownuser", appuser));

		this.addAction(showcurrentuser);

		DynamicActionDefinition createsessionforuser = new DynamicActionDefinition("createsessionforuser",
				"creates a session registered with client location if the user is a valid user");
		createsessionforuser
				.addBusinessRule("returns null if there is no valid user corresponding with given user and password");
		createsessionforuser.addBusinessRule(
				"if a valid user and password is provided, it will delete all other sessions with the same user and password");
		createsessionforuser.addBusinessRule(
				"if a valid user and password is provided, it will create a new session with client reference (ip and cid) provided");

		createsessionforuser.addInputArgument(new StringArgument("user", 64));
		createsessionforuser.addInputArgument(new StringArgument("password", 64));
		createsessionforuser.addInputArgument(new StringArgument("clientip", 64));
		createsessionforuser.addInputArgument(new StringArgument("clientcid", 64));
		createsessionforuser.addOutputArgument(new ObjectArgument("usersession", usersession));
		this.addAction(createsessionforuser);

		DynamicActionDefinition getsessionforclient = new DynamicActionDefinition("getsessionforclient",
				"looks up if there is an open session for the provided client reference");
		// note: should allow to define properties for methods
		getsessionforclient.addBusinessRule(
				"an open session is a session with the corresponding client cid and ip touched less than 'TIMEOUT' away");
		getsessionforclient.addBusinessRule(
				"whenever this method is called, the session last action date is refreshed to the current date ");

		getsessionforclient.addInputArgument(new StringArgument("clientip", 64));
		getsessionforclient.addInputArgument(new StringArgument("clientcid", 64));
		getsessionforclient.addOutputArgument(new ObjectArgument("usersession,", usersession));

		this.addAction(getsessionforclient);

		// ------------------------------------------ GROUP
		// -----------------------------

		usergroup = new DataObjectDefinition("usergroup", "User Group", this, false);
		usergroup.addProperty(new StoredObject());
		UniqueIdentified uniqueidentifiedusergroup = new UniqueIdentified();
		usergroup.addProperty(uniqueidentifiedusergroup);
		usergroup.addProperty(new Numbered());
		usergroup.addField(new StringField("DESCRIPTION", "Description", "group responsibilities and privileges", 1000,
				StringField.INDEXTYPE_NONE));

		groupmemberlink = new DataObjectDefinition("groupmemberlink", "Group Members", this);
		groupmemberlink.addProperty(new StoredObject());
		groupmemberlink.addProperty(new UniqueIdentified());
		groupmemberlink
				.addProperty(new LinkObject(usergroup, appuser, "Members of the group", "Groups user is a member of"));

		groupadministratorlink = new DataObjectDefinition("groupadminlink", "Group Administrators", this);
		groupadministratorlink.addProperty(new StoredObject());
		groupadministratorlink.addProperty(new UniqueIdentified());
		LinkObject groupadminlinkproperty = new LinkObject(usergroup, appuser, "Administrators of the group",
				"Groups administered by user");
		groupadministratorlink.addProperty(groupadminlinkproperty);
		groupadminlinkproperty.addBusinessRule(new ConstraintOnLinkUniqueForLeftAndRight(groupadminlinkproperty, true));
		Personal usergroupersonal = new Personal(groupadministratorlink);
		usergroup.addProperty(usergroupersonal);

		// ------------------------------------------ Domain
		// -----------------------------

		domain = new DataObjectDefinition("domain", "Domain", this);
		domain.addProperty(new StoredObject());
		domain.addProperty(new UniqueIdentified());
		domain.addProperty(new Numbered());
		domain.addProperty(new LinkedToParent("hierarchy", domain));



		StaticActionDefinition getdomainhierarchy = new StaticActionDefinition("GETDOMAINHIERARCHY",
				"Get all domains from the root one");
		getdomainhierarchy.addOutputArgument(new NodeTreeArgument(new ObjectArgument("DOMAINTREE", domain)));
		this.addAction(getdomainhierarchy);

		DynamicPageDefinition showdomainhierarchy = new DynamicPageDefinition("SHOWDOMAINHIERARCHY");
		showdomainhierarchy.addInputParameter(new NodeTreeArgument(new ObjectArgument("DOMAINTREE", domain)));
		this.AddPage(showdomainhierarchy);

		// ------------------------------------------- AUTHORITY

		authority = new DataObjectDefinition("authority", "Authority", this, true);
		authority.addProperty(new StoredObject());
		authority.addProperty(new UniqueIdentified());
		authority.addProperty(new Numbered());
		authority.addProperty(new Named());
		authority.addField(new StringField("SCOPE", "Scope", "The scope and privileges the authority", 512, 300));
		authority.addProperty(new LinkedToParent("scope", domain));

		// ------------------------------------------- PROFILE TO GROUP

		groupswithauthority = new DataObjectDefinition("groupswithauthority", "Groups with Authority", this);
		groupswithauthority.addProperty(new StoredObject());
		groupswithauthority.addProperty(new UniqueIdentified());
		groupswithauthority.addProperty(new LinkObject(authority, usergroup, "Groups assigned authority privilege",
				"Authority privileges for the group"));

		// ------------------------- Define login pages pages

		DynamicActionDefinition login = new DynamicActionDefinition("login");
		login.addInputArgument(new StringArgument("user", 64));
		login.addInputArgument(new StringArgument("password", 64));
		login.addInputArgument(new StringArgument("otp",64));
		login.addInputArgument(new StringArgument("contextaction", 640000000));
		login.addOutputArgument(new BooleanArgument("isloginok"));
		login.addOutputArgument(new StringArgument("contextactionthru", 64000000));
		this.addAction(login);
		this.addPrivilege(new Anarchy(login));
		DynamicPageDefinition simpleloginpage = new DynamicPageDefinition("simplelogin");
		simpleloginpage.addInputParameter(new StringArgument("contextaction", 640000000));
		simpleloginpage.setNoAddOn();
		this.AddPage(simpleloginpage);

		csvloadererror = new DataObjectDefinition("CSVLOADERERROR", "CSV Loader Error", this);
		csvloadererror.addField(new IntegerField("LINENR", "Line Number", "The Error", 100));
		csvloadererror
				.addField(new StringField("ERROR", "Error", "Error", 2000, StringField.INDEXTYPE_NONE, 200, false));
		csvloadererror.addField(new StringField("LINESOURCE", "Line Source", "The loader line in error", 20000,
				StringField.INDEXTYPE_NONE, 400, false));
		csvloadererror.addLoaderAlias("AAAA\\", "BBBBB\\\"");

		DynamicPageDefinition showloadingreport = new DynamicPageDefinition("SHOWLOADINGREPORT");
		showloadingreport.addInputParameter(new StringArgument("LOADINGCONTEXT", 500));
		showloadingreport.addInputParameter(new IntegerArgument("INSERTED"));
		showloadingreport.addInputParameter(new IntegerArgument("UPDATED"));
		showloadingreport.addInputParameter(new IntegerArgument("ERRORS"));
		showloadingreport.addInputParameter(new IntegerArgument("POSTPROCERRORS"));

		showloadingreport.addInputParameter(new IntegerArgument("LOADINGTIME"));
		showloadingreport.addInputParameter(new ArrayArgument(new ObjectArgument("ERRORDETAIL", csvloadererror)));
		this.AddPage(showloadingreport);

		DynamicPageDefinition showloadingreportforchildren = new DynamicPageDefinition("SHOWLOADINGREPORTFORCHILDREN");
		showloadingreportforchildren.addInputParameter(new StringArgument("LOADINGCONTEXT", 500));
		showloadingreportforchildren.addInputParameter(new IntegerArgument("INSERTED"));
		showloadingreportforchildren.addInputParameter(new IntegerArgument("UPDATED"));
		showloadingreportforchildren.addInputParameter(new IntegerArgument("ERRORS"));
		showloadingreportforchildren.addInputParameter(new IntegerArgument("POSTPROCERRORS"));

		showloadingreportforchildren.addInputParameter(new IntegerArgument("LOADINGTIME"));
		showloadingreportforchildren
				.addInputParameter(new ArrayArgument(new ObjectArgument("ERRORDETAIL", csvloadererror)));
		showloadingreportforchildren.addInputParameter(new ObjectIdArgument("OBJECTID", null));
		this.AddPage(showloadingreportforchildren);

		DataObjectDefinition systemattribute = new DataObjectDefinition("SYSTEMATTRIBUTE", "System Attribute", this);
		systemattribute.addProperty(new StoredObject());
		systemattribute.addProperty(new UniqueIdentified());
		systemattribute.addProperty(new Numbered(256));
		systemattribute.addProperty(new CreationLog());
		systemattribute.addProperty(new UpdateLog());
		systemattribute.addProperty(new Iterated());
		// not indexed. Properties should be
		systemattribute.addField(
				new StringField("VALUE", "Value", "value of the system attribute", 128, StringField.INDEXTYPE_NONE));
		systemattribute.addField(new StringField("COMMENT", "Comment", "explains how to use the system attribute", 2000,
				StringField.INDEXTYPE_NONE));

		workflow = new DataObjectDefinition("WORKFLOW", "Workflow", this);
		workflow.addProperty(new StoredObject());
		workflow.addProperty(new UniqueIdentified());
		workflow.addProperty(new Named("Workflow Type"));
		workflow.addProperty(new CreationLog());
		workflow.addProperty(new UpdateLog());
		workflow.addProperty(new Iterated());
		TransitionChoiceCategory workflowlifecycle = new TransitionChoiceCategory("WORKFLOWLIFECYCLE", 32);
		ChoiceValue RUNNING = new ChoiceValue("RUNNING", "Running", "Workflow is active");
		ChoiceValue FINISHED = new ChoiceValue("FINISHED", "Finished", "Workflow is finished");
		workflowlifecycle.addValue(RUNNING);
		workflowlifecycle.addValue(FINISHED);
		workflowlifecycle.setDefaultChoice(RUNNING);
		workflowlifecycle.setChoiceAsFinal(FINISHED);
		workflowlifecycle.DefineTransition(RUNNING, FINISHED);
		this.addChoiceCategory(workflowlifecycle);
		workflow.addProperty(new Lifecycle(workflowlifecycle, "Workflow Status"));
		workflow.addProperty(new TargetDate());
		workflow.addProperty(new GenericLink("WORKFLOWOBJECT"));

		task = new DataObjectDefinition("TASK", "Task", this, true);
		task.addProperty(new StoredObject());
		task.addProperty(new UniqueIdentified());
		task.addProperty(new Named());
		task.addProperty(new LinkedToParent("WORKFLOW", workflow));
		task.addProperty(new CreationLog());
		task.addProperty(new UpdateLog());
		task.addProperty(new Iterated());
		task.addProperty(new GenericLink("TASKOBJECT"));
		task.addField(new StringField("DESCRIPTION", "Description", "description of actions to be conducted", 1000,
				StringField.INDEXTYPE_NONE, 900, true));
		task.addField(new StringField("COMMENT", "Assignee Comment", "comment of the person who validated", 2000,
				StringField.INDEXTYPE_NONE, 890, true));
		task.addField(new StringField("SELECTEDCHOICE", "Selected Choice", "label of the choice made by the user", 80,
				StringField.INDEXTYPE_NONE, 880, true));
		task.addField(new StringField("SUBJECT", "Subject", "Description of the object concerned by the task", 200,
				StringField.INDEXTYPE_NONE, 870, true));
		// task.addField(new ChoiceField("ALLOWSABANDON","Allows Abandon","precises if
		// the task can be given back to a group", booleanchoice, -10));

		task.addField(
				new StringField("CODE", "Task Code", "Unique Code of the task in the workflow (for complex workflows)",
						16, StringField.INDEXTYPE_NONE, -800, true));
		task.addField(
				new ChoiceField("GROUPTASK", "Group", "Is the task sent to a single user or group", booleanchoice));
		task.addField(
				new StringField("COMPLETEDBY", "Assignee", "the name of the user who completed or reassigned the task",
						96, StringField.INDEXTYPE_EASYSEARCH, 905, true));
		task.addField(new TimestampField("COMPLETEDDATE", "Completed Date", "Date the task was completed",
				TimestampField.INDEXTYPE_NONE, 902, true));

		TransitionChoiceCategory tasklifecycle = new TransitionChoiceCategory("TASKLIFECYCLE", 32);

		ChoiceValue OPEN = new ChoiceValue("OPEN", "Open", "Task is assigned to a group of people for processing");
		ChoiceValue INWORK = new ChoiceValue("INWORK", "In work",
				"Task is assigned to a person actively working on it");
		ChoiceValue REASSIGNED = new ChoiceValue("REASSIGNED", "Reassigned",
				"Task has been reassigned to a special person");
		ChoiceValue COMPLETED = new ChoiceValue("COMPLETED", "Completed",
				"Task is finished according to original description");
		ChoiceValue CANCELED = new ChoiceValue("CANCELED", "Canceled",
				"After all necessary enquiries, assignee has confirmed there is nothing relevant to do for the original assignment");

		tasklifecycle.addValue(OPEN);
		tasklifecycle.addValue(INWORK);
		tasklifecycle.addValue(REASSIGNED);
		tasklifecycle.addValue(COMPLETED);
		tasklifecycle.addValue(CANCELED);

		tasklifecycle.DefineTransition(OPEN, INWORK);
		tasklifecycle.DefineTransition(INWORK, OPEN);
		tasklifecycle.DefineTransition(OPEN, REASSIGNED);
		tasklifecycle.DefineTransition(INWORK, REASSIGNED);
		tasklifecycle.DefineTransition(INWORK, COMPLETED);
		tasklifecycle.DefineTransition(OPEN, COMPLETED);
		tasklifecycle.DefineTransition(INWORK, CANCELED);

		tasklifecycle.setDefaultChoice(OPEN);
		tasklifecycle.setChoiceAsFinal(COMPLETED);
		tasklifecycle.setChoiceAsFinal(CANCELED);
		tasklifecycle.setChoiceAsFinal(REASSIGNED);

		this.addChoiceCategory(tasklifecycle);
		task.addProperty(new Lifecycle(tasklifecycle));
		task.addProperty(new TargetDate());
		taskchoice = new DataObjectDefinition("TASKCHOICE", "Task Choice", this, true);
		taskchoice.addProperty(new StoredObject());
		taskchoice.addProperty(new UniqueIdentified());
		taskchoice.addProperty(new Named("Choice"));

		taskchoice.addProperty(new LinkedToParent("TASK", task));
		taskchoice.addField(new ChoiceField("SELECTED", "Selected", "", booleanchoice, 750));
		taskchoice.addField(new StringField("CODE", "Code", "Code", 64, StringField.INDEXTYPE_NONE, -95));

		DataObjectDefinition taskuser = new DataObjectDefinition("TASKUSER", "Task User", this, true);
		taskuser.addProperty(new StoredObject());
		taskuser.addProperty(new UniqueIdentified());
		taskuser.addProperty(new LinkObject(task, appuser, "Users Assigned", "Task assignments"));

		// FRONT PAGE ACTION
		StaticActionDefinition getfrontpagedata = new StaticActionDefinition("GETFRONTPAGEDATA",
				"gets active task for user");
		getfrontpagedata.setButtonlabel("Home");
		getfrontpagedata.addOutputArgument(new StringArgument("SPECIFICMESSAGE", 4000));
		getfrontpagedata.addOutputArgument(new ArrayArgument(new ObjectArgument("ACTIVETASKS", task)));
		this.addasMenuAction(getfrontpagedata);
		this.addPrivilege(new Anarchy(getfrontpagedata));
		// USER STAT

	
		StaticActionDefinition prepareuserstat = new StaticActionDefinition("showuserstat");
		prepareuserstat.addOutputArgument(new ObjectIdArgument("userid", appuser));
		prepareuserstat.setButtonlabel("Your connections");
		this.addasMenuAction(prepareuserstat);
		this.addPrivilege(new Anarchy(prepareuserstat));
		DynamicPageDefinition showuserstat = new DynamicPageDefinition("showuserstat");
		showuserstat.addInputParameter(new ObjectArgument("appuser", appuser));
		showuserstat.addInputParameter(new ArrayArgument(new ObjectArgument("usersession", usersession)));

		this.AddPage(showuserstat);

		DynamicActionDefinition preparecopyuserstoothergroup = new DynamicActionDefinition("PREPARECOPYUSERSTOGROUP");
		preparecopyuserstoothergroup.setButtonlabel("Copy All Users");
		preparecopyuserstoothergroup.addInputArgumentAsAccessCriteria(new ObjectIdArgument("GROUPID", usergroup));
		preparecopyuserstoothergroup.addOutputArgument(new ObjectIdArgument("GROUPID_THRU", usergroup));
		preparecopyuserstoothergroup.addOutputArgument(new StringArgument("ORIGINGROUPSUMMARY", 500));

		this.addAction(preparecopyuserstoothergroup);
		uniqueidentifiedusergroup.addActionOnObjectId(preparecopyuserstoothergroup);

		DynamicPageDefinition copyuserstoothergrouppage = new DynamicPageDefinition("COPYUSERSTOGROUP");
		copyuserstoothergrouppage.linkPageToAction(preparecopyuserstoothergroup);
		this.AddPage(copyuserstoothergrouppage);

		DynamicActionDefinition copyuserstoothergroup = new DynamicActionDefinition("COPYUSERSTOGROUP");
		copyuserstoothergroup.addInputArgumentAsAccessCriteria(new ObjectIdArgument("ORIGINGROUPID", usergroup));
		copyuserstoothergroup.addInputArgument(new ObjectIdArgument("TARGETGROUPID", usergroup));
		copyuserstoothergroup.addOutputArgument(new ObjectIdArgument("TARGETGROUPID_THRU", usergroup));

		this.addAction(copyuserstoothergroup);

		StaticActionDefinition seeuserprivilege = new StaticActionDefinition("SEEPRIVILEGES");
		seeuserprivilege.addOutputArgument(new StringArgument("USERSUMMARY", 200));
		seeuserprivilege.addOutputArgument(new ArrayArgument(new ObjectArgument("USERAUTHORITIES", authority)));
		seeuserprivilege.setButtonlabel("Your privileges");
		this.addasMenuAction(seeuserprivilege);
		this.addPrivilege(new Anarchy(seeuserprivilege));
		DynamicPageDefinition seeuserprivilegepage = new DynamicPageDefinition("SEEPRIVILEGES");
		seeuserprivilegepage.linkPageToAction(seeuserprivilege);
		this.AddPage(seeuserprivilegepage);

		StaticActionDefinition launchchangepassword = new StaticActionDefinition("LAUNCHCHANGEPASSWORD");
		launchchangepassword.addOutputArgument(
				new ChoiceArgument("CHANGEPASSWORDPOSSIBLE", SystemModule.getSystemModule().getBooleanChoice()));
		launchchangepassword.addOutputArgument(new StringArgument("MESSAGE", 300));
		launchchangepassword.setButtonlabel("Change Password");
		this.addasMenuAction(launchchangepassword);
		this.addPrivilege(new Anarchy(launchchangepassword));

		DynamicPageDefinition showkeymessage = new DynamicPageDefinition("SHOWKEYMESSAGE");
		showkeymessage.addInputParameter(new StringArgument("MESSAGE", 300));
		this.AddPage(showkeymessage);

		DynamicPageDefinition changepassword = new DynamicPageDefinition("CHANGEPASSWORD");
		changepassword.addInputParameter(new StringArgument("MESSAGE", 300));
		this.AddPage(changepassword);
		DynamicActionDefinition changepasswordaction = new DynamicActionDefinition("CHANGEPASSWORD");
		changepasswordaction.addInputArgument(new StringArgument("OLDPASSWORD", 64));
		changepasswordaction.addInputArgument(new StringArgument("NEWPASSWORD", 64));
		changepasswordaction.addInputArgument(new StringArgument("NEWPASSWORDREPEAT", 64));
		changepasswordaction.addOutputArgument(
				new ChoiceArgument("CHANGEPASSWORDDONE", SystemModule.getSystemModule().getBooleanChoice()));
		changepasswordaction.addOutputArgument(new StringArgument("MESSAGE", 300));
		this.addAction(changepasswordaction);
		this.addPrivilege(new Anarchy(changepasswordaction));
		// FRONT PAGE
		DynamicPageDefinition frontpage = new DynamicPageDefinition("FRONTPAGE");
		frontpage.linkPageToAction(getfrontpagedata);
		this.AddPage(frontpage);

		DynamicActionDefinition displaygenericobject = new DynamicActionDefinition("DISPLAYGENERICOBJECT",
				"Universal action to display an object");
		displaygenericobject.addInputArgument(new ObjectIdArgument("GENERICID", null));
		displaygenericobject.addOutputArgument(new ObjectIdArgument("GENERICID_THRU", null));
		this.addAction(displaygenericobject);
		this.addPrivilege(new Anarchy(displaygenericobject));
		DynamicActionDefinition accepttask = new DynamicActionDefinition("ACCEPTTASK",
				"Universal action to accept a task, independently of workflow task");
		accepttask.addInputArgument(new ObjectIdArgument("TASKID", task));
		accepttask.addOutputArgument(new ObjectIdArgument("TASKIDTHRU", task));

		this.addAction(accepttask);
		this.addPrivilege(new Anarchy(accepttask));
		DynamicActionDefinition rejecttask = new DynamicActionDefinition("REJECTTASK",
				"Universal action to reject a task, independently of workflow task");
		rejecttask.addInputArgument(new ObjectIdArgument("TASKID", task));
		rejecttask.addOutputArgument(new ObjectIdArgument("OBJECTID", null));

		this.addAction(rejecttask);
		this.addPrivilege(new Anarchy(rejecttask));
		DynamicActionDefinition completetask = new DynamicActionDefinition("COMPLETETASK",
				"Universal action to complete a task, independently of workflow task");
		completetask.addInputArgument(new ObjectIdArgument("TASKID", task));
		completetask.addInputArgument(new ObjectIdArgument("TASKCHOICEID", taskchoice));
		completetask.addInputArgument(new StringArgument("COMMENT", 2000));
		completetask.addOutputArgument(new ObjectIdArgument("OBJECTID", null));
		this.addAction(completetask);
		this.addPrivilege(new Anarchy(completetask));
		DynamicActionDefinition reassigntask = new DynamicActionDefinition("REASSIGNTASK",
				"Universal action to reassign a task, independently of workflow task");
		reassigntask.addInputArgument(new ObjectIdArgument("TASKID", task));
		reassigntask.addInputArgument(new StringArgument("COMMENT", 2000));
		reassigntask.addInputArgument(new ObjectIdArgument("NEWUSERID", this.appuser));
		reassigntask.addOutputArgument(new ObjectIdArgument("OBJECTID", null));
		this.addAction(reassigntask);
		this.addPrivilege(new Anarchy(reassigntask));

		DynamicActionDefinition savetaskcomment = new DynamicActionDefinition("SAVETASKCOMMENT", "Save task comment");
		savetaskcomment.addInputArgument(new ObjectIdArgument("TASKID", task));
		savetaskcomment.addInputArgument(new StringArgument("COMMENT", 2000));
		savetaskcomment.addOutputArgument(new ObjectIdArgument("TASKIDTHRU", task));
		this.addAction(savetaskcomment);
		this.addPrivilege(new Anarchy(savetaskcomment));
		DynamicActionDefinition canaccepttask = new DynamicActionDefinition("CANACCEPTTASK",
				"Universal action to check if a task can be accepted");
		canaccepttask.addInputArgument(new ObjectIdArgument("TASKID", task));
		canaccepttask.addOutputArgument(new BooleanArgument("RESULT"));
		this.addAction(canaccepttask);

		DynamicActionDefinition canrejecttask = new DynamicActionDefinition("CANREJECTTASK",
				"Universal action to check if a task can be rejected");
		canrejecttask.addInputArgument(new ObjectIdArgument("TASKID", task));
		canrejecttask.addOutputArgument(new BooleanArgument("RESULT"));
		this.addAction(canrejecttask);

		DynamicActionDefinition showactivetask = new DynamicActionDefinition("SHOWACTIVETASK", "show active task");
		showactivetask.addInputArgument(new ObjectIdArgument("TASKID", task));
		showactivetask.addOutputArgument(new ObjectArgument("TASK", task));
		showactivetask.addOutputArgument(new ObjectIdArgument("OBJECTID", null));
		showactivetask.addOutputArgument(new ArrayArgument(new ObjectArgument("TASKCHOICE", taskchoice)));
		showactivetask.addOutputArgument(new ArrayArgument(new ObjectArgument("TASKUSERS", taskuser)));
		showactivetask.addOutputArgument(new ChoiceArgument("CANACCEPT", booleanchoice));
		showactivetask.addOutputArgument(new ChoiceArgument("CANREJECT", booleanchoice));
		showactivetask.addOutputArgument(new StringArgument("COMMENT", 2000));

		this.addAction(showactivetask);
		this.addPrivilege(new Anarchy(showactivetask));
		DynamicActionDefinition activetaskcomplexquery = new DynamicActionDefinition("ACTIVETASKCOMPLEXQUERY",
				"Active task complex query");
		activetaskcomplexquery.addBusinessRule("Returns all active tasks for current user.");
		activetaskcomplexquery.addBusinessRule("If object id is specified, only tasks for object are specified.");
		activetaskcomplexquery.addInputArgument(new ObjectIdArgument("OBJECTID", null));
		activetaskcomplexquery.addOutputArgument(new ArrayArgument(new ObjectArgument("TASK", task)));

		this.addAction(activetaskcomplexquery);

		DynamicPageDefinition showactivetaskpage = new DynamicPageDefinition("SHOWACTIVETASK");
		showactivetaskpage.linkPageToAction(showactivetask);
		this.AddPage(showactivetaskpage);

		AddonPageDefinition addon = new AddonPageDefinition("DEFAULTMENU");
		this.AddPage(addon);

		defaultlifecycle = new TransitionChoiceCategory("DEFAULTLC", 16);
		defaultlifecycle.addValue(DEF_OPEN);
		defaultlifecycle.addValue(DEF_INWORK);
		defaultlifecycle.addValue(DEF_COMPLETED);
		defaultlifecycle.addValue(DEF_CANCELED);

		defaultlifecycle.DefineTransition(DEF_OPEN, DEF_INWORK);
		defaultlifecycle.DefineTransition(DEF_INWORK, DEF_OPEN);
		defaultlifecycle.DefineTransition(DEF_INWORK, DEF_COMPLETED);
		defaultlifecycle.DefineTransition(DEF_INWORK, DEF_CANCELED);
		defaultlifecycle.DefineTransition(DEF_OPEN, DEF_CANCELED);
		defaultlifecycle.setDefaultChoice(DEF_OPEN);
		defaultlifecycle.setDefaultWorkingChoice(DEF_INWORK);
		defaultlifecycle.setDefaultFinalChoice(DEF_COMPLETED);
		defaultlifecycle.setChoiceAsFinal(DEF_CANCELED);
		this.addChoiceCategory(defaultlifecycle);

		// e-mail generated by process. It will be sent by a background process, and
		// consolidated if required.

		emailqueue = new DataObjectDefinition("EMAIL", "e-mail", this, false);
		emailqueue.addProperty(new StoredObject());
		emailqueue.addProperty(new UniqueIdentified());

		emailqueue.addProperty(new Named());
		emailqueue.addProperty(new Iterated());
		emailqueue.addField(new StringField("TITLE", "Title", "Title up to the object reference", 128,
				StringField.INDEXTYPE_RAWINDEX, false));
		emailqueue.addField(new StringField("BODYTEXT", "Body Text", "Body of the text without object reference", 4000,
				StringField.INDEXTYPE_NONE, false));
		emailqueue.addField(new StringField("OBJECTID", "Object id",
				"Object id up to 60 characters (typically number and revision)", 64, StringField.INDEXTYPE_NONE));
		emailqueue.addField(new StringField("OBJECTLABEL", "Object label", "label of the object (typically name)", 200,
				StringField.INDEXTYPE_NONE));
		emailqueue.addField(new StringField("OBJECTDETAIL", "Object detail", "more detail about the object", 4000,
				StringField.INDEXTYPE_NONE));
		emailqueue.addField(new ChoiceField("DETAILISHTML", "Details in HTML",
				"if true, text is assumed to be valid HTML", booleanchoice));
		emailqueue.addField(
				new StringField("SENDER", "Sender", "Sender of the email", 128, StringField.INDEXTYPE_NONE, false));

		TransitionChoiceCategory emaillifecycle = new TransitionChoiceCategory("EMAILSTATUS", 16);
		ChoiceValue CREATING = new ChoiceValue("CREATING", "Creating e-mail", "");
		ChoiceValue READYTOSEND = new ChoiceValue("READYTOSEND", "E-mail ready to send", "");
		ChoiceValue SENDING = new ChoiceValue("SENDING", "E-mail under processing ", "");
		ChoiceValue SENT = new ChoiceValue("SENT", "E-mail successfully sent", "");
		ChoiceValue ERROR = new ChoiceValue("ERROR", "E-mail could not be sent", "");
		ChoiceValue DISCARDED = new ChoiceValue("DISCARDED", "E-mail discarded", "");

		emaillifecycle.addValue(CREATING);
		emaillifecycle.addValue(READYTOSEND);
		emaillifecycle.addValue(SENT);
		emaillifecycle.addValue(ERROR);
		emaillifecycle.addValue(DISCARDED);

		emaillifecycle.addValue(SENDING);
		emaillifecycle.setDefaultChoice(CREATING);
		emaillifecycle.setDefaultFinalChoice(SENT);
		emaillifecycle.setChoiceAsFinal(DISCARDED);
		emaillifecycle.DefineTransition(CREATING, READYTOSEND);
		emaillifecycle.DefineTransition(READYTOSEND, SENDING);
		emaillifecycle.DefineTransition(READYTOSEND, DISCARDED);

		emaillifecycle.DefineTransition(SENDING, SENT);
		emaillifecycle.DefineTransition(SENDING, ERROR);
		emaillifecycle.DefineTransition(SENDING, DISCARDED);
		emaillifecycle.DefineTransition(ERROR, READYTOSEND);
		emaillifecycle.DefineTransition(ERROR, DISCARDED);

		emaillifecycle.DefineTransition(SENDING, READYTOSEND);

		emaillifecycle.DefineTransition(ERROR, SENDING);
		this.addChoiceCategory(emaillifecycle);
		emailqueue.addProperty(new Lifecycle(emaillifecycle));

		SimpleChoiceCategory delaytype = new SimpleChoiceCategory("DELAYTYPE", 5);
		ChoiceValue immediate = new ChoiceValue("NOW", "Immediate Sending", "");
		ChoiceValue fifteenminutes = new ChoiceValue("D15M", "Consolidate sending every 15 minutes", "");
		ChoiceValue twohours = new ChoiceValue("D2H", "Consolidate sending every 2 hours", "");
		ChoiceValue daily = new ChoiceValue("DAY", "Consolidate sending every day at 5am night", "");
		ChoiceValue weekly = new ChoiceValue("WKLY", "Consolidate sending every week on monday morning 5am", "");
		delaytype.addValue(immediate);
		delaytype.addValue(fifteenminutes);
		delaytype.addValue(twohours);
		delaytype.addValue(daily);
		delaytype.addValue(weekly);
		this.addChoiceCategory(delaytype);
		emailqueue.addField(new ChoiceField("DELAYTYPE", "Delay Type", "", delaytype));
		emailqueue.addField(new StringField("MODULE", "Module", "name of the module that created the e-mail.", 64,
				StringField.INDEXTYPE_NONE, false));

		emailqueue.addProperty(new UpdateLog());

		// meeting types

		emailqueue.addField(new ChoiceField("MEETING", "Meeting", "if yes, meeting, else just notification",
				SystemModule.getSystemModule().booleanchoice, 50, ChoiceField.INDEXTYPE_RAWINDEX));
		emailqueue.addField(new ChoiceField("CANCELATION", "Cancelation", "if yes, meeting is sent as a cancelation",
				SystemModule.getSystemModule().booleanchoice, 50, ChoiceField.INDEXTYPE_NONE));

		emailqueue.addField(new ChoiceField("ACTION", "Action in System", "if the mail triggers an action",
				SystemModule.getSystemModule().booleanchoice));
		emailqueue.addField(new StringField("LOCATION", "Location", "", 128, StringField.INDEXTYPE_NONE));
		emailqueue
				.addField(new TimestampField("STARTTIME", "Start time", "", TimestampField.INDEXTYPE_NONE, 230, true));
		emailqueue.addField(new TimestampField("ENDTIME", "End time", "", TimestampField.INDEXTYPE_NONE, 229, true));
		emailqueue.addField(new StringField("MEETINGUID", "Meeting UID", "", 255, StringField.INDEXTYPE_NONE));

		emailrecipient = new DataObjectDefinition("EMAILRECIPIENT", "e-mail recipient", this, true);
		emailrecipient.addProperty(new StoredObject());
		emailrecipient.addProperty(new UniqueIdentified());
		emailrecipient.addField(new StringField("RECIPIENT", "Recipient", "Recipient of the e-mail", 128,
				StringField.INDEXTYPE_NONE, false));

		SimpleChoiceCategory recipienttype = new SimpleChoiceCategory("RECIPIENTTYPE", 5);
		ChoiceValue to = new ChoiceValue("TO", "To", "");
		ChoiceValue cc = new ChoiceValue("CC", "Carbon Copy", "");
		ChoiceValue bcc = new ChoiceValue("BCC", "Blind Carbon Copy", "");
		recipienttype.addValue(to);
		recipienttype.addValue(cc);
		recipienttype.addValue(bcc);
		this.addChoiceCategory(recipienttype);
		emailrecipient.addField(new ChoiceField("RECIPIENTTYPE", "Recipient Type", "", recipienttype));
		emailrecipient.addProperty(new LinkedToParent("OWNEREMAIL", emailqueue));

		DataObjectDefinition triggervalue = new DataObjectDefinition("TRIGGERVALUE", "Trigger Value", this);
		triggervalue.addProperty(new StoredObject());
		triggervalue.addProperty(new UniqueIdentified());

		triggervalue.addProperty(new CreationLog());
		triggervalue.addProperty(new UpdateLog());
		triggervalue.addProperty(new GenericLink("OWNER"));
		triggervalue.addField(new StringField("TRIGGERNAME", "Trigger Name", "The name of the trigger", 64,
				StringField.INDEXTYPE_NONE));

		triggervalue.addField(new StringField("OBJECTSUMMARY", "Object Summary",
				"A unique string that changes only when the trigger needs to be executed again, it has a limit to 4000 characters",
				4000, StringField.INDEXTYPE_NONE));
		DynamicPageDefinition technicaltools = new DynamicPageDefinition("TECHNICALTOOLS");
		this.AddPage(technicaltools);

		StaticActionDefinition launchtechnicaltools = new StaticActionDefinition("LAUNCHTECHNICALTOOLS");
		launchtechnicaltools.setButtonlabel("Tech tools");
		this.addasMenuAction(launchtechnicaltools);

		StaticActionDefinition generatefaultymessage = new StaticActionDefinition("GENERATEFAULTYMESSAGE");
		generatefaultymessage.addOutputArgument(new FaultyStringArgument("FAULTYARGUMENT", 2000));
		this.addAction(generatefaultymessage);
		DynamicPageDefinition faultymessagepage = new DynamicPageDefinition("FAULTYMESSAGE");
		faultymessagepage.linkPageToAction(generatefaultymessage);
		this.AddPage(faultymessagepage);

		DynamicActionDefinition textaudit = new DynamicActionDefinition("AUDITTEXT");
		textaudit.addInputArgument(new StringArgument("TEXTFROMCLIENTBIGFIELD", 4000));
		textaudit.addInputArgument(new StringArgument("TEXTFROMCLIENTSMALLFIELD", 4000));

		textaudit.addOutputArgument(new StringArgument("STOREDTEXT", 4000));
		textaudit.addOutputArgument(new StringArgument("UNSTOREDTEXT", 4000));
		this.addAction(textaudit);
		DynamicPageDefinition textauditdisplay = new DynamicPageDefinition("SHOWAUDITTEXT");
		textauditdisplay.linkPageToAction(textaudit);
		this.AddPage(textauditdisplay);

		this.holidayset = new DataObjectDefinition("HOLIDAYSET", "Holiday Set", this);
		holidayset.addProperty(new StoredObject());
		holidayset.addProperty(new UniqueIdentified());
		holidayset.addProperty(new Named());
		holidayset.addProperty(new Numbered());

		this.workcalendar = new DataObjectDefinition("WORKCALENDAR", "Work Calendar", this);
		workcalendar.addProperty(new StoredObject());
		workcalendar.addProperty(new UniqueIdentified());
		LinkedToParent workcalendartoholidayset = new LinkedToParent("HOLIDAYSET", holidayset);
		workcalendar.addProperty(workcalendartoholidayset);
		LinkedToDefaultParent workcalendartoholidaysetdefault = new LinkedToDefaultParent("Default", true,
				workcalendartoholidayset);
		workcalendartoholidayset.addBusinessRule(workcalendartoholidaysetdefault);
		workcalendar.addProperty(new Named());
		workcalendar.addProperty(new Numbered());

		SimpleChoiceCategory weeklyslot = new SimpleChoiceCategory("WEEKLYSLOT", 16);
		weeklyslot.addValue(new ChoiceValue("D01", "Monday", ""));
		weeklyslot.addValue(new ChoiceValue("D02", "Tuesday", ""));
		weeklyslot.addValue(new ChoiceValue("D03", "Wednesday", ""));
		weeklyslot.addValue(new ChoiceValue("D04", "Thursday", ""));
		weeklyslot.addValue(new ChoiceValue("D05", "Friday", ""));
		weeklyslot.addValue(new ChoiceValue("D06", "Saturday", ""));
		weeklyslot.addValue(new ChoiceValue("D07", "Sunday", ""));
		weeklyslot.addValue(new ChoiceValue("D15", "Monday to Friday", ""));
		weeklyslot.addValue(new ChoiceValue("D16", "Monday to Saturday", ""));
		weeklyslot.addValue(new ChoiceValue("D17", "Everyday", ""));

		this.addChoiceCategory(weeklyslot);

		this.weeklyworkingslots = new DataObjectDefinition("WEEKLYSLOT", "Weekly Slot", this);
		weeklyworkingslots.addProperty(new StoredObject());
		weeklyworkingslots.addProperty(new UniqueIdentified());
		weeklyworkingslots.addProperty(new LinkedToParent("WORKCALENDAR", workcalendar));
		weeklyworkingslots.addField(new ChoiceField("DAYSINWEEEK", "Days in Week", "", weeklyslot));
		weeklyworkingslots
				.addField(new DecimalField("HOURSTART", "Hour Start", "0 to 23", 2, 0, DecimalField.INDEXTYPE_NONE));
		weeklyworkingslots.addField(
				new DecimalField("MINUTESTART", "Minute Start", "0 to 59", 2, 0, DecimalField.INDEXTYPE_NONE));
		weeklyworkingslots
				.addField(new DecimalField("HOUREND", "Hour End", "0 to 23", 2, 0, DecimalField.INDEXTYPE_NONE));
		weeklyworkingslots
				.addField(new DecimalField("MINUTEEND", "Minute End", "0 to 59", 2, 0, DecimalField.INDEXTYPE_NONE));

		this.holiday = new DataObjectDefinition("HOLIDAY", "Holiday", this);
		holiday.addProperty(new StoredObject());
		holiday.addProperty(new UniqueIdentified());
		holiday.addProperty(new LinkedToParent("HOLIDAYSET", holidayset));
		holiday.addField(
				new DecimalField("HLDAY", "Day", "Day of the month (1 to 31)", 2, 0, DecimalField.INDEXTYPE_NONE));

		SimpleChoiceCategory month = new SimpleChoiceCategory("WESTERNMONTHS", 16);
		month.addValue(new ChoiceValue("M01", "January", ""));
		month.addValue(new ChoiceValue("M02", "February", ""));
		month.addValue(new ChoiceValue("M03", "March", ""));
		month.addValue(new ChoiceValue("M04", "April", ""));
		month.addValue(new ChoiceValue("M05", "May", ""));
		month.addValue(new ChoiceValue("M06", "June", ""));
		month.addValue(new ChoiceValue("M07", "July", ""));
		month.addValue(new ChoiceValue("M08", "August", ""));
		month.addValue(new ChoiceValue("M09", "September", ""));
		month.addValue(new ChoiceValue("M10", "October", ""));
		month.addValue(new ChoiceValue("M11", "November", ""));
		month.addValue(new ChoiceValue("M12", "December", ""));
		this.addChoiceCategory(month);
		holiday.addField(new ChoiceField("HLMONTH", "Month", "", month));
		holiday.addField(new DecimalField("HLYEAR", "Year", "Year (empty it means every year", 4, 0,
				DecimalField.INDEXTYPE_NONE));

		calendarmanager = new TotalAuthority("WorkCalendarManager", "Work Calendar Manager",
				"Can define and change work calendars. They are used for scheduling functions in the applications");
		this.addAuthority(calendarmanager);
		calendarviewer = new TotalAuthority("WorkCalendarViewer", "Work Calendar Viewer",
				"Can view work calendars. They are used for scheduling functions in the applications");
		this.addAuthority(calendarviewer);

		this.addPrivilege(new UnconditionalPrivilege(holiday.getCreateNewActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holidayset.getCreateNewActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(weeklyworkingslots.getCreateNewActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(workcalendar.getCreateNewActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holiday.getModifyActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holidayset.getModifyActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(weeklyworkingslots.getModifyActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(workcalendar.getModifyActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holiday.getReadActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holidayset.getReadActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(weeklyworkingslots.getReadActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(workcalendar.getReadActionGroup(), calendarmanager));
		this.addPrivilege(new UnconditionalPrivilege(holiday.getReadActionGroup(), calendarviewer));
		this.addPrivilege(new UnconditionalPrivilege(holidayset.getReadActionGroup(), calendarviewer));
		this.addPrivilege(new UnconditionalPrivilege(weeklyworkingslots.getReadActionGroup(), calendarviewer));
		this.addPrivilege(new UnconditionalPrivilege(workcalendar.getReadActionGroup(), calendarviewer));

		SequenceDefinition taskschedulesequence = new SequenceDefinition("TASKRESCHEDULE");
		this.addSequenceDefinition(taskschedulesequence);

		SequenceDefinition objectidseed = new SequenceDefinition("OBJECTIDSEED");
		this.addSequenceDefinition(objectidseed);

		DataObjectDefinition modulereport = new DataObjectDefinition("MODULEREPORT", "Module Report", this, true);
		modulereport.addField(new StringField("CODE", "Code", "", 5, StringField.INDEXTYPE_NONE));
		modulereport.addField(new StringField("NAME", "Name", "", 80, StringField.INDEXTYPE_NONE));
		modulereport.addField(new StringField("VERSION", "Framework Version", "", 20, StringField.INDEXTYPE_NONE));
		modulereport.addField(new ChoiceField("STABLE", "Stable", "", this.booleanchoice));
		modulereport.addField(new StringField("MODULEVERSION", "Module Version", "", 20, StringField.INDEXTYPE_NONE));
		modulereport.addField(new TimestampField("COMPILEDATE", "Compile Date", "", TimestampField.INDEXTYPE_RAWINDEX));

		StaticActionDefinition showmodules = new StaticActionDefinition("SHOWMODULESTAT");
		showmodules.addOutputArgument(new ArrayArgument(new ObjectArgument("MODULEREPORT", modulereport)));
		showmodules.setButtonlabel("Modules Stat");
		this.addasMenuAction(showmodules);
		DynamicPageDefinition showmodulespage = new DynamicPageDefinition("SHOWMODULESTAT");
		showmodulespage.linkPageToAction(showmodules);
		this.AddPage(showmodulespage);

		StaticPageDefinition addlogs = new StaticPageDefinition("ADDLOGS");

		this.AddPage(addlogs);

		StaticActionDefinition launchaddlogs = new StaticActionDefinition("LAUNCHADDLOGS");
		launchaddlogs.setButtonlabel("Manage logs");
		this.addasMenuAction(launchaddlogs);

		this.loglevelcategory = new SimpleChoiceCategory("LOGLEVEL", 16);
		loglevelcategory.addValue(new ChoiceValue("SEVERE", "Severe", ""));
		loglevelcategory.addValue(new ChoiceValue("WARNING", "Warning", ""));
		loglevelcategory.addValue(new ChoiceValue("INFO", "Info", ""));
		loglevelcategory.addValue(new ChoiceValue("CONFIG", "Config", ""));
		loglevelcategory.addValue(new ChoiceValue("FINE", "Fine", ""));
		loglevelcategory.addValue(new ChoiceValue("FINER", "Finer", ""));
		loglevelcategory.addValue(new ChoiceValue("FINEST", "Finest", ""));
		this.addChoiceCategory(loglevelcategory);

		DynamicActionDefinition addlogsaction = new DynamicActionDefinition("ADDLOGS");
		addlogsaction.addInputArgument(new StringArgument("PATH", 256));
		addlogsaction.addInputArgument(new ChoiceArgument("TEXTLOG", loglevelcategory));
		addlogsaction.addInputArgument(new ChoiceArgument("CONSOLELOG", loglevelcategory));
		this.addAction(addlogsaction);

		DynamicActionDefinition resetextralogs = new DynamicActionDefinition("RESETEXTRALOGS");
		this.addAction(resetextralogs);

		// ---------------------------------------------- CREATE USER FROM LDAP
		// ----------------------

		DynamicActionDefinition launchadduser = new DynamicActionDefinition("LAUNCHADDUSER");
		launchadduser.addInputArgumentAsAccessCriteria(new ObjectIdArgument("GROUPID", usergroup));

		launchadduser.addOutputArgument(new ObjectIdArgument("GROUPID_THRU", usergroup));
		launchadduser.addOutputArgument(new StringArgument("GROUPLABEL_THRU", 128));
		launchadduser.addOutputArgument(new StringArgument("USERID", 64));
		launchadduser.addOutputArgument(new StringArgument("PASSWORD", 64));
		launchadduser.addOutputArgument(new StringArgument("LDAPBASE", 64));
		launchadduser.addOutputArgument(new ChoiceArgument("LOCALE", applicationlocale));
		launchadduser.addOutputArgument(new ChoiceArgument("ENCODING", this.preferedfileencoding));

		uniqueidentifiedusergroup.addActionOnObjectId(launchadduser);
		launchadduser.setButtonlabel("Add new User");
		this.addAction(launchadduser);

		DataObjectDefinition ldapuser = new DataObjectDefinition("LDAPUSER", "LDAP User", this, true);
		ldapuser.addField(
				new StringField("LASTNAME", "Last Name", "LDAP Field 'sn'", 100, StringField.INDEXTYPE_NONE, 810));
		ldapuser.addField(new StringField("FIRSTNAME", "First Name", "LDAP Field 'givenName'", 100,
				StringField.INDEXTYPE_NONE, 809));
		ldapuser.addField(new StringField("DEPARTMENT", "Department", "LDAP Field 'department'", 100,
				StringField.INDEXTYPE_NONE, 808));
		ldapuser.addField(new StringField("LOCATION", "Location", "LDAP Field 'roomNumber'", 100,
				StringField.INDEXTYPE_NONE, 807));
		ldapuser.addField(
				new StringField("ID", "ID", "LDAP Field 'mailNickname'", 100, StringField.INDEXTYPE_NONE, 806));
		ldapuser.addField(new StringField("MAIL", "E-Mail", "LDAP Field 'mail'", 100, StringField.INDEXTYPE_NONE, 805));
		ldapuser.addField(
				new StringField("FULLNAME", "Full Name", "LDAP Field 'cn'", 100, StringField.INDEXTYPE_NONE, 804));
		ldapuser.addField(new StringField("DISTINGUISHEDNAME", "Distinguished Name", "LDAP Field 'distinguishedName'",
				300, StringField.INDEXTYPE_NONE, 803));
		ldapuser.addField(new StringField("FIXPHONE", "Fix Phone", "LDAP Field 'homePhone'", 100,
				StringField.INDEXTYPE_NONE, 802));
		ldapuser.addField(new StringField("MOBILEPHONE", "Mobile Phone", "LDAP Field 'mobile'", 100,
				StringField.INDEXTYPE_NONE, 801));
		ldapuser.setForcedRowHeightForTable(1);

		DynamicActionDefinition queryforuser = new DynamicActionDefinition("QUERYFORUSER");
		queryforuser.addInputArgument(new StringArgument("USERID", 64));
		queryforuser.addInputArgument(new StringArgument("USERPASWORD", 64));
		queryforuser.addInputArgument(new StringArgument("LDAPBASE", 64));
		queryforuser.addInputArgument(new StringArgument("LASTNAMESTARTSBY", 64));
		queryforuser.addInputArgument(new StringArgument("IDSTARTSBY", 64));
		queryforuser.addOutputArgument(new ArrayArgument(new ObjectArgument("LDAPUSERS", ldapuser)));
		this.addAction(queryforuser);

		DynamicActionDefinition createuserfromldap = new DynamicActionDefinition("CREATEUSERFROMLDAP");
		createuserfromldap.addInputArgument(new ArrayArgument(new ObjectArgument("SELECTEDLDAPUSER", ldapuser)));
		createuserfromldap.addInputArgumentAsAccessCriteria(new ObjectIdArgument("GROUP", usergroup));
		createuserfromldap.addInputArgument(new StringArgument("GROUPLABEL", 128));
		createuserfromldap.addInputArgument(new StringArgument("USERID", 64));
		createuserfromldap.addInputArgument(new StringArgument("USERPASWORD", 64));
		createuserfromldap.addInputArgument(new StringArgument("LDAPBASE", 64));
		createuserfromldap.addInputArgument(new ChoiceArgument("LOCALE", applicationlocale));
		createuserfromldap.addInputArgument(new ChoiceArgument("ENCODING", this.preferedfileencoding));
		createuserfromldap.addOutputArgument(new StringArgument("USERID_THRU", 64));
		createuserfromldap.addOutputArgument(new ObjectIdArgument("GROUPID_THRU", usergroup));
		createuserfromldap.addOutputArgument(new StringArgument("GROUPLABEL_THRU", 128));
		createuserfromldap.addOutputArgument(new StringArgument("USERPASWORD_THRU", 64));
		createuserfromldap.addOutputArgument(new StringArgument("LDAPBASE_THRU", 64));
		createuserfromldap.addOutputArgument(new ChoiceArgument("LOCALE_THRU", applicationlocale));
		createuserfromldap.addOutputArgument(new ChoiceArgument("ENCODING_THRU", this.preferedfileencoding));
		createuserfromldap.addOutputArgument(new StringArgument("MESSAGE", 256));

		this.addAction(createuserfromldap);

		DynamicPageDefinition createuserfromldappage = new DynamicPageDefinition("CREATEUSERFROMLDAP");
		createuserfromldappage.addInputParameter(new ObjectIdArgument("GROUP", usergroup));
		createuserfromldappage.addInputParameter(new StringArgument("GROUPLABEL", 128));
		createuserfromldappage.addInputParameter(new StringArgument("USERID", 64));
		createuserfromldappage.addInputParameter(new StringArgument("USERPASWORD", 64));
		createuserfromldappage.addInputParameter(new StringArgument("LDAPBASE", 64));
		createuserfromldappage.addInputParameter(new ChoiceArgument("LOCALE", applicationlocale));
		createuserfromldappage.addInputParameter(new ChoiceArgument("ENCODING", this.preferedfileencoding));
		createuserfromldappage.addInputParameter(new StringArgument("MESSAGE", 64));

		this.AddPage(createuserfromldappage);

		StaticActionDefinition launchsessioncleaning = new StaticActionDefinition("LAUNCHSESSIONCLEANING");
		launchsessioncleaning.addOutputArgument(new IntegerArgument("NUMBEROFITEMS"));
		launchsessioncleaning.addOutputArgument(new IntegerArgument("OLDESTLOG"));

		launchsessioncleaning.setButtonlabel("Session Cleaning");
		this.addasMenuAction(launchsessioncleaning);

		DynamicPageDefinition launchsessioncleaningpage = new DynamicPageDefinition("SESSIONCLEANING");
		launchsessioncleaningpage.linkPageToAction(launchsessioncleaning);
		this.AddPage(launchsessioncleaningpage);

		DynamicActionDefinition sessioncleaning = new DynamicActionDefinition("SESSIONCLEANING");
		sessioncleaning.addInputArgument(new IntegerArgument("CLEANSESSIONSOLDERTHANDAYS"));
		this.addAction(sessioncleaning);

		SimpleChoiceCategory cleanemailsprocess = new SimpleChoiceCategory("CLEANEMAILPROCESS", 16);
		cleanemailsprocess.addValue(new ChoiceValue("DISCARD", "Discard", "E-mail will be archived (not sent)"));
		cleanemailsprocess.addValue(new ChoiceValue("RELAUNCH", "Relaunch", "E-mail will be relaunched"));
		this.addChoiceCategory(cleanemailsprocess);

		StaticActionDefinition launchcleanpendingemails = new StaticActionDefinition("LAUNCHCLEANPENDINGEMAILS");
		this.addAction(launchcleanpendingemails);
		launchcleanpendingemails.setButtonlabel("Clean E-mails");

		launchcleanpendingemails.addOutputArgument(new ArrayArgument(new ObjectArgument("PENDINGEMAILS", emailqueue)));
		emailqueue.addActionOnSearchPage(launchcleanpendingemails);
		DynamicActionDefinition cleanpendingemails = new DynamicActionDefinition("CLEANPENDINGEMAILS");
		cleanpendingemails.addInputArgument(new ArrayArgument(new ObjectArgument("SELECTEDPENDINGEMAILS", emailqueue)));
		cleanpendingemails.addInputArgument(new ChoiceArgument("NEWSTATUS", cleanemailsprocess));
		this.addAction(cleanpendingemails);

		DynamicPageDefinition cleanpendingemailspage = new DynamicPageDefinition("CLEANPENDINGEMAILS");
		cleanpendingemailspage.linkPageToAction(launchcleanpendingemails);
		this.AddPage(cleanpendingemailspage);

		StaticActionDefinition enterotp = new StaticActionDefinition("ENTEROTP");
		enterotp.setButtonlabel("Enter OTP");
		this.addasMenuAction(enterotp);
		
		StaticPageDefinition enterotppage = new StaticPageDefinition("ENTEROTPPAGE");
		this.AddPage(enterotppage);
		
		StaticPageDefinition otpok = new  StaticPageDefinition("OTPOK");
		this.AddPage(otpok);
		
		DynamicActionDefinition confirmotp = new DynamicActionDefinition("CONFIRMOTP");
		confirmotp.addInputArgument(new StringArgument("OTP", 64));
		confirmotp.addOutputArgument(new ChoiceArgument("VALID",this.booleanchoice));
		this.addAction(confirmotp);
		DataObjectDefinition otpcheck = new DataObjectDefinition("OTPCHECK","OTP Checks",this);
		otpcheck.addProperties(new StoredObject(),new UniqueIdentified(),new LinkedToParent("USER",appuser));
		otpcheck.addField(new StringField("CLIENTIP", "Client IP", "the IP of the client as visible from the server",
				80, StringField.INDEXTYPE_RAWINDEX));
		otpcheck.addField(new StringField("CLIENTPID", "Client PID",
				"the PID of the client. This is generated by the server the first time a client connects", 80,
				StringField.INDEXTYPE_RAWINDEX));
		otpcheck.addField(new TimestampField("CREATED", "Created", "Date of validation of the OTP", TimestampField.INDEXTYPE_RAWINDEXWITHSEARCH));
		this.addPrivilege(new Anarchy(enterotp));
		this.addPrivilege(new Anarchy(confirmotp));
		
		this.addPrivilege(new ObjectPersonalPrivilege(usergroup.getReadActionGroup(), usergroupersonal));
		this.addPrivilege(new ObjectPersonalPrivilege(launchadduser, usergroupersonal));
		this.addPrivilege(new ObjectPersonalPrivilege(createuserfromldap, usergroupersonal));
		this.addPrivilege(new Anarchy(queryforuser));

	}

	private void defineSystemAttributes() {
		this.addSystemAttribute(new SystemAttributeInit("MAILSENDING", "HOLD",
				"Immediately changes mail daemon behaviour. Choose between SEND, HOLD, AND DISCARD. Default is HOLD"));
		this.addSystemAttribute(new SystemAttributeInit("SERVERLABEL", "",
				"A label displayed on each page and mail sent. It is typically used to specify a server is a test server"));
		this.addSystemAttribute(
				new SystemAttributeInit("PREFEREDLOCALE", "FR", "Prefered locale for new  users (values FR or US)"));
		this.addSystemAttribute(new SystemAttributeInit("PREFEREDENCODING", "CP1522",
				"Prefered file encoding for new users (values CP1522 or UTF8)"));
		this.addSystemAttribute(new SystemAttributeInit("LDAPBASESEARCH", "", "Base Seach for LDAP"));

	}

}
