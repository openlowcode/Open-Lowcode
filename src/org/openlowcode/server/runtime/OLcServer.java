/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.tools.enc.OLcEncrypter;
import org.openlowcode.tools.encrypt.EncrypterHolder;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.misc.TimeLogger;
import org.openlowcode.tools.trace.ConsoleFormatter;
import org.openlowcode.tools.trace.FileFormatter;
import org.openlowcode.OLcVersion;
import org.openlowcode.module.system.Systemmodule;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.formula.TriggerToExecute;
import org.openlowcode.server.data.properties.AdminIdDefaultValueGenerator;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.jdbcpool.ConnectionPool;
import org.openlowcode.server.data.storage.jdbcpool.SimpleConnectionPool;
import org.openlowcode.server.runtime.email.MailDaemon;
import org.openlowcode.server.security.SecurityManager;
import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * The main class of the Open Lowcode Server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcServer {

	private String getServerVersion() {
		return OLcVersion.version + ", client version requested: " + OLcVersion.referenceclientversion + ", stable = "
				+ OLcVersion.stable + ", generated on " + OLcVersion.versiondate;
	}

	public final static String DBTYPE_DERBY = "DERBY";
	public final static String DBTYPE_MARIA10_2 = "MARIA10.2";
	private ConnectionPool connectionpool;
	private SecurityManager securitymanager;
	private OLcServerLogFilter consolelogfilter;
	private OLcServerLogFilter filelogfilter;

	private NamedList<SModule> moduledirectory;
	private SModule mainmodule;
	private static OLcServer serversingleton;
	private InetAddress localhost;
	private ConnectionListener connectionlisterner;
	private String clientjar;

	public String getClientJar() {
		return this.clientjar;
	}

	/**
	 * gets the internet address of the server
	 * 
	 * @return the address of the local host
	 */
	public InetAddress getLocalhost() {
		return this.localhost;
	}

	/**
	 * gets the connection pool of the server
	 * 
	 * @return the connection pool of the server
	 */
	public ConnectionPool getConnectionpool() {
		return connectionpool;
	}

	/**
	 * gets the number of modules registered on the server
	 * 
	 * @return the number of modules
	 */
	public int getModuleNumber() {
		return moduledirectory.getSize();
	}

	/**
	 * gets the module at given index
	 * 
	 * @param index the index of the module between 0 (included) and getModuleNumber
	 *              (excluded)
	 * @return the module at given index
	 */
	public SModule getModule(int index) {
		return moduledirectory.get(index);
	}

	/**
	 * gest the module by name
	 * 
	 * @param name name of the module
	 * @return the module corresponding to the given name, or null if no module
	 *         exists by that name
	 */
	public SModule getModuleByName(String name) {
		return moduledirectory.lookupOnName(name);
	}

	/**
	 * gets the security manager of the server
	 * 
	 * @return the security manager
	 */
	public SecurityManager getSecuritymanager() {
		return securitymanager;
	}

	/**
	 * gets the main module on the server
	 * 
	 * @return the main module on the server (the first one to be entered at launch
	 *         of the server
	 */
	public SModule getMainmodule() {
		return mainmodule;
	}

	private Logger mainlogger;

	/**
	 * Creates the server with all the attributes provided in the configuration file
	 * 
	 * @param configurationfilepath path to the configuration file (either absolute
	 *                              or from the current server location)
	 */
	public OLcServer(String configurationfilepath) {
		try {

			serversingleton = this; // to make sure singleton is initiated for what happens at launch (inside
									// server)

			OLcServerConfig serverconfig = new OLcServerConfig(configurationfilepath);
			serverconfig.parseConfigFile();
			
			this.clientjar = serverconfig.getCompulsoryValue("CLIENTJAR");

			EncrypterHolder.InitEncrypterHolder(OLcEncrypter.getEncrypter());

			String initmessage = "Startup of server version " + getServerVersion();
			System.out.println(initmessage);
			TimeLogger serverstartuptimer = new TimeLogger();
			TimeLogger allserverstartuptimer = new TimeLogger();
			localhost = InetAddress.getLocalHost();

			// ------------------------ INITIATE LOGGING --------------------------------
			String logfolder = serverconfig.getCompulsoryValue("LOGFOLDER");
			boolean reducedlogs = serverconfig.getOptionalBooleanValue("REDUCEDLOGS", true);
			int port = serverconfig.getCompulsoryIntegerValue("PORT");
			try {
				mainlogger = Logger.getLogger(OLcServer.class.getName());
				initiateLogger(logfolder, reducedlogs, port);

				System.out.println("Log file initiatized on file with path '" + logfolder
						+ "'. Please report to log file for detailed server proceedings.");
				mainlogger.info(initmessage);
			} catch (Throwable t) {
				System.out.println("could not initiate logging on pattern = '" + logfolder + "'. error is "
						+ t.getClass().getCanonicalName() + ":" + t.getMessage());
				t.printStackTrace(System.out);
				System.exit(1);
			}
			mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 1: Log file initialization"));

			// ------------------------------- INITIATE CONNECTION POOL -------------------
			String dbtype = serverconfig.getCompulsoryValue("DBTYPE");
			String jdbcurl = serverconfig.getCompulsoryValue("JDBC.URL");
			if (dbtype.equals(DBTYPE_DERBY))
				if (jdbcurl.indexOf("jdbc:derby:") == -1)
					jdbcurl = "jdbc:derby:" + jdbcurl;
			boolean hasadvanceddatabase = validateDBType(dbtype);
			String jdbcuser = null;
			String jdbcpassword = null;
			int minconnection = 1;
			int maxconnection = 1;
			if (hasadvanceddatabase) {
				jdbcuser = serverconfig.getCompulsoryValue("JDBC.USER");
				jdbcpassword = serverconfig.getCompulsoryValue("JDBC.PASSWORD");
				maxconnection = serverconfig.getCompulsoryIntegerValue("JDBC.MAXCONNECTIONS");
			}
			connectionpool = new SimpleConnectionPool(jdbcurl, jdbcuser, jdbcpassword, minconnection, maxconnection);
			// connection pool and persistence gateway seem to be inconsistent / redundant
			PersistenceGateway.setconnectionpool(dbtype, connectionpool);
			PersistentStorage storage = PersistenceGateway.getStorage();
			storage.technicalInit();
			PersistenceGateway.checkinStorage(storage);
			mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 2: JDBC connection pool"));

			// ------------------------------- INITIATE PAGE DIRECTORY --------------------
			String modulelist = serverconfig.getOptionalValue("MODULES");
			String[] modulenamelist = new String[0];
			if (modulelist != null)
				if (modulelist.length() > 0)
					modulenamelist = modulelist.split(";");
			mainlogger.info(
					"starting to process module directory - " + modulenamelist.length + " modules planned to be added");
			moduledirectory = new NamedList<SModule>();
			Systemmodule systemmodule = new Systemmodule();
			moduledirectory.add(systemmodule);
			this.mainmodule = systemmodule; // if later no module is set, system is the main module
			mainlogger.info("succesfully added system module to the module directory");

			// ------------------------------- FIRST START BY INIT SYSTEM MODULES
			// --------------
			int systemmoduleendindex = processModules(0, serverstartuptimer);

			// ------------------------------- ADD OTHER MODULES

			for (int i = 0; i < modulenamelist.length; i++) {
				String module = modulenamelist[i];
				// using reflection as a way to load classes on the server. Normally, reflection
				// is forbidden in project, but
				// this is an agreed exception
				mainlogger.info("start adding module " + module);
				Class<?> thismoduleclass = Class.forName(module);
				Constructor<?> thismoduleconstructor = thismoduleclass.getConstructor();
				Object thismodule = thismoduleconstructor.newInstance();
				SModule thismodulecasted = (SModule) thismodule;
				// reflection horror ended
				moduledirectory.add(thismodulecasted);
				String mainmodule = "";
				if (i == 0) {
					mainmodule = " (as main module)";
					this.mainmodule = thismodulecasted;
				}
				mainlogger.info("succesfully added module " + thismodulecasted.getName() + " to the module directory "
						+ mainmodule);
			}

			mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 3: Action Directory initialization"));

			// ------------------------------- UPDATE DATA MODEL ------------------------
			processModules(systemmoduleendindex, serverstartuptimer);

			// ------------------------------- INITIATE SECURITY ------------------------
			String ldapconnectionstring = serverconfig.getOptionalValue("LDAP.CONNECTION");
			String ldapuser = null;
			String ldappassword = null;
			if (ldapconnectionstring != null) {
				ldapuser = serverconfig.getCompulsoryValue("LDAP.USER");
				ldappassword = serverconfig.getCompulsoryValue("LDAP.PASSWORD");
			}

			securitymanager = new SecurityManager(ldapconnectionstring, ldapuser, ldappassword);
			securitymanager.start();
			mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 6: initiate security engine"));

			// ------------------------------- INITIATE LISTENER ------------------------
			// ConnectionGateway.initSingle(connectionpool.getConnection());

			boolean messageaudit = serverconfig.getOptionalBooleanValue("MESSAGE.AUDIT", false);

			connectionlisterner = new ConnectionListener(port, this, messageaudit);
			mainlogger.severe(serverstartuptimer.logTimer(" STARTUP STEP 7: all port listeners initiated"));
			String smtpurl = serverconfig.getOptionalValue("SMTP.URL");
			if (smtpurl != null) {
				String smtpuser = serverconfig.getOptionalValue("SMPT.USER");
				String smtppassword = serverconfig.getOptionalValue("SMTP.PASSWORD");
				int smtpport = serverconfig.getOptionalIntegerValue("SMTP.PORT", 0);
				MailDaemon daemon = null;
				mainlogger.warning(" starting mail daemon with url = '" + smtpurl + "', port ='" + smtpport
						+ "', smtpuser = '" + smtpuser + "'");
				if (smtpuser != null) {

					daemon = new MailDaemon(smtpurl, smtpport, smtpuser, smtppassword);
				} else {
					if (smtpport > 0) {
						daemon = new MailDaemon(smtpurl, smtpport);
					} else {
						daemon = new MailDaemon(smtpurl);
					}
				}
				if (!daemon.isAlive())
					daemon.start();
				mainlogger.severe(" STARTUP STEP 8: mail daemon initiated");

			}
			mainlogger.severe(allserverstartuptimer.logTimer(" STARTUP -- Total time "));

		} catch (Throwable t) {
			if (mainlogger != null) {
				mainlogger.severe("unexpected error during execution, exception details below. Server Shutdown");
				mainlogger.severe(" * " + t.getClass().getCanonicalName() + " - " + t.getMessage());
				if (t instanceof SQLException) {
					SQLException se = (SQLException) t;
					mainlogger.severe(" * SQLException extra info Error code " + se.getErrorCode() + " - SQLState "
							+ se.getSQLState());
					SQLException ne = se.getNextException();
					if (ne != null)
						mainlogger.severe(ne.getClass().getCanonicalName() + " - error code " + ne.getErrorCode()
								+ " - error message " + ne.getMessage());
				}
				StackTraceElement[] ste = t.getStackTrace();
				for (int i = 0; i < ste.length; i++) {
					mainlogger.severe("   - " + ste[i].toString());
				}
				if (t instanceof ExceptionInInitializerError) {
					ExceptionInInitializerError initerror = (ExceptionInInitializerError) t;
					mainlogger.severe("  Cause of ExceptionInInitializer error " + initerror.getCause());
					for (int i = 0; i < initerror.getCause().getStackTrace().length; i++) {
						mainlogger.severe("   	- " + initerror.getCause().getStackTrace()[i].toString());
					}
				}
				Throwable current = t;
				int circuitbreaker = 0;
				while ((current.getCause() != null) && (circuitbreaker < 20)) {
					current = current.getCause();
					mainlogger.severe("---------- Cause of error detailed below for level " + circuitbreaker);
					mainlogger.severe(" * " + current.getClass().getCanonicalName() + " - " + current.getMessage());
					StackTraceElement[] stec = current.getStackTrace();
					for (int i = 0; i < stec.length; i++) {
						mainlogger.severe("   - " + stec[i].toString());
					}
				}
			} else {
				System.err.println(" Unexpected error before main logger setup "+t.getClass().getName()+" "+t.getMessage());
				for (int i=0;i<t.getStackTrace().length;i++) System.err.println("   * "+t.getStackTrace()[i]);
			}
		}
	}

	public static boolean validateDBType(String dbtype) {
		if (dbtype.equals(DBTYPE_DERBY))
			return false;
		if (dbtype.equals(DBTYPE_MARIA10_2))
			return true;
		throw new RuntimeException("Database type not supported " + dbtype);
	}

	private int processModules(int startindex, TimeLogger serverstartuptimer) {
		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).updateDataModelStep1();
		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).updateDataModelStep2();
		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).updateDataModelStep3();

		// ----------- intermediate step init generators
		AdminIdDefaultValueGenerator.get().computeValue();

		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).updateDataModelStep4();
		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).updateDataModelStep5();

		mainlogger.warning(serverstartuptimer.logTimer(" STARTUP STEP 4: update data model"));

		// ------------------------------- INITIATE DATA ------------------------

		for (int i = startindex; i < this.moduledirectory.getSize(); i++) {
			SModule thismodule = this.moduledirectory.get(i);
			thismodule.initiateData();
			mainlogger.info("Initialized data for module = " + thismodule.getName());
		}

		mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 5: initiate data"));

		for (int i = startindex; i < moduledirectory.getSize(); i++)
			moduledirectory.get(i).DataUpdateStep1();

		mainlogger.info(serverstartuptimer.logTimer(" STARTUP STEP 5A: data migrations finished"));
		return moduledirectory.getSize();
	}

	/**
	 * gets the connection pool to the database
	 * 
	 * @return the connection pool to the database
	 */
	public ConnectionPool getConnectionPool() {
		return this.connectionpool;
	}

	public static void main(String args[]) {
		if (args.length != 1) {
			System.err.println("Error : syntax java OLcServer configfilepath");
			System.err.println("   Where configfilepath is a valid path to an Open Lowcode server");
			System.err.println("         configuration file");
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.err.println("starting shutdown sequence");
				try {

					PersistenceGateway.getStorage().closeConnections();

				} catch (RuntimeException e) {
					System.err.println("Error during exit connection close " + e.getMessage());
				}
				Logger logger = Logger.getLogger("");
				Handler[] handler = logger.getHandlers();
				for (int i = 0; i < handler.length; i++) {
					handler[i].close();
					logger.removeHandler(handler[i]);

				}
				System.err.println("logger closed. shutting down gracefully");
			}
		});

		serversingleton = new OLcServer(args[0]);
	}

	/**
	 * adds an exception to the normal log level, either for file log or console log
	 * 
	 * @param path    path of the class for which exceptional log level should be
	 *                set, or an alias.
	 * @param level
	 * @param filelog true if exception to be added to file log, false if exception
	 *                to be added to console log
	 */
	public void addException(String path, Level level, boolean filelog) {
		if (filelog) {
			this.filelogfilter.addException(path, level);
		} else {
			this.consolelogfilter.addException(path, level);
		}
	}

	/**
	 * get log level for this path, considering exception list and normal log level
	 * 
	 * @param path full class path
	 * @return the minimum level for logging
	 */
	public Level minimumLevelForClass(String path) {
		Level filelevel = this.filelogfilter.getLevelForPath(path);
		Level consolelevel = this.consolelogfilter.getLevelForPath(path);
		if (filelevel.intValue() < consolelevel.intValue())
			return filelevel;
		return consolelevel;
	}

	/**
	 * initiates the logger (console and files)
	 * 
	 * @param logfilepath the logfile path
	 * @param reducedlogs true if reduced logs
	 * @param port        port of the server
	 * @throws IOxception if any error is encountered creating logging files
	 */
	private void initiateLogger(String logfilepath, boolean reducedlogs, int port) throws IOException {
		File logfolder = new File(logfilepath);

		if (!logfolder.exists()) {
			boolean result = logfolder.mkdir();
			if (!result)
				throw new RuntimeException("Trying to create log folder " + logfilepath + ", does not work");
		}
		logfolder = new File(logfilepath);
		if (!logfolder.isDirectory())
			throw new RuntimeException("Path " + logfilepath + " is not a folder.");
		String logcleanpath = logfolder.getCanonicalPath() + File.separator;

		// ----- File Handler Setup -------------------------
		FileHandler logfilehandler = new FileHandler(logcleanpath + "ServerLog-" + port + "-%g.log", 10000000, 1000,
				true);
		this.filelogfilter = new OLcServerLogFilter((reducedlogs ? Level.WARNING : Level.INFO), "File log",
				logfilehandler);
		logfilehandler.setLevel(Level.ALL);
		logfilehandler.setFilter(filelogfilter);
		logfilehandler.setFormatter(new FileFormatter(false));

		ConsoleHandler consolehandler = new ConsoleHandler();
		consolehandler.setFormatter(new ConsoleFormatter());
		this.consolelogfilter = new OLcServerLogFilter((reducedlogs ? Level.SEVERE : Level.WARNING), "Console log",
				consolehandler);
		consolehandler.setLevel(Level.ALL);
		consolehandler.setFilter(consolelogfilter);

		// LOGGER.setUseParentHandlers(false);
		Logger anonymouslogger = Logger.getLogger("");
		for (int i = 0; i < anonymouslogger.getHandlers().length; i++) {
			anonymouslogger.removeHandler(anonymouslogger.getHandlers()[i]);
		}
		anonymouslogger.addHandler(logfilehandler);

		anonymouslogger.addHandler(consolehandler);
		anonymouslogger.setUseParentHandlers(false);
		anonymouslogger.setLevel(Level.ALL);
		// --------------------------------------------------------------
		Logger rootlogger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
		for (int i = 0; i < rootlogger.getHandlers().length; i++) {
			rootlogger.removeHandler(rootlogger.getHandlers()[i]);
		}
		rootlogger.addHandler(logfilehandler);
		rootlogger.setLevel(Level.ALL);

		rootlogger.addHandler(consolehandler);
		rootlogger.setUseParentHandlers(false);
		Logger connectionlogger = Logger.getLogger(SimpleConnectionPool.class.getCanonicalName());

		FileHandler connectionhandler = new FileHandler(logcleanpath + "ConnectionPool-" + port + "-%g.log", 10000000,
				1000, true);
		connectionhandler.setLevel(Level.FINEST);
		connectionhandler.setFormatter(new FileFormatter(false));
		connectionlogger.addHandler(connectionhandler);
	}

	/**
	 * gets the server singleton
	 * 
	 * @return the server singleton
	 */
	public static OLcServer getServer() {
		return serversingleton;
	}

	/**
	 * reset the list of triggers for the given thread (this method will manipulate
	 * different data for each thread)
	 */
	public void resetTriggersList() {
		currentriggerexecution.set(new Integer(-1));
		triggerstoexecute.set(new ArrayList<TriggerToExecute<?>>());
		triggersobjectbuffer.set(new HashMap<String, UniqueidentifiedInterface<?>>());
	}

	/**
	 * accesses to a buffer specific to the thread calling to get objects based on
	 * their data object ids
	 * 
	 * @param id id of the data object
	 * @return the data object
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>> E getObjectInTriggerUpdateBuffer(
			DataObjectId<E> id) {
		UniqueidentifiedInterface<?> object = triggersobjectbuffer.get().get(id.getObjectId());
		if (object == null)
			return null;
		return (E) (object);
	}

	/**
	 * adds a data object to put in the buffer specific to the thread calling
	 * 
	 * @param object the object to put in buffer
	 */
	public void setObjectInTriggerUpdateBuffer(UniqueidentifiedInterface<?> object) {
		mainlogger.fine(" -+-+- Add object in trigger object buffer " + object.getId());
		triggersobjectbuffer.get().put(object.getId().getObjectId(), object);
	}

	/**
	 * adds a trigger to the list of triggers to execute for the thread
	 * 
	 * @param newtrigger the trigger to add in the list of triggers to execute
	 */
	@SuppressWarnings("unchecked")
	public void addTriggerToList(TriggerToExecute<?> newtrigger) {
		// this is costly traceability. Removed for normal operation, may be needed for
		// debugging
		Thread currentThread = Thread.currentThread();
		StackTraceElement[] stacktrace = currentThread.getStackTrace();
		mainlogger.fine("Adding Trigger to list at ------------------------");
		for (int i = 0; i < stacktrace.length; i++) {
			mainlogger.fine("   " + stacktrace[i]);
		}

		DataObject<?> newtriggerobject = newtrigger.getContextobject();
		DataObjectId<?> newtriggerid = null;
		if (newtriggerobject instanceof UniqueidentifiedInterface)
			newtriggerid = ((UniqueidentifiedInterface<?>) newtriggerobject).getId();
		mainlogger.fine("		NEW TRIGGER " + newtrigger + ", DataObjectId = " + newtriggerid);
		mainlogger.fine("		NEW TRIGGER Object" + newtriggerobject.dropToString());
		// will look at all triggers after the one under execution, and only add the
		// object if not already present
		// if object already present, replaces by the newer version
		int executionindex = currentriggerexecution.get();
		boolean objecttriggeralreadypresent = false;
		ArrayList<TriggerToExecute<?>> currenttriggers = triggerstoexecute.get();
		mainlogger.fine(" getting triggers from currentriggers list = " + currenttriggers.toString() + " for Thread "
				+ Thread.currentThread().getId());

		for (int i = executionindex + 1; i < currenttriggers.size(); i++) {
			mainlogger.fine("----------------- LOOP IN TRIGGER --- " + i + " ----");
			@SuppressWarnings("rawtypes")
			TriggerToExecute thistrigger = currenttriggers.get(i);
			DataObject<?> thisobject = thistrigger.getContextobject();

			if (thisobject instanceof UniqueidentifiedInterface) {
				DataObjectId<?> thisobjectid = ((UniqueidentifiedInterface<?>) thisobject).getId();
				mainlogger
						.fine("		Existing Trigger [" + i + "] " + thistrigger + ", DataObjectId = " + thisobjectid);
				mainlogger.fine("		Existing trigger object = " + thisobject.dropToString());
				if (thisobjectid.equals(newtriggerid)) {
					// replace object

					mainlogger.fine(" --**--- [EXIND=" + executionindex + ",IND=" + i + "] OLDTRIG="
							+ thistrigger.toString() + " NEWTRIG=" + newtrigger.toString()
							+ " replace object with id = " + thisobjectid + " by object with same id " + newtriggerid);
					thistrigger.replacetrigger(newtriggerobject);
					if (thistrigger.getName().equals(newtrigger.getName())) {
						objecttriggeralreadypresent = true;
					} else {
						mainlogger.fine("Does not replace both triggers as different classes " + thistrigger.getName()
								+ " - " + newtrigger.getName());
					}
					// suppressing break as there could be several classes of triggers
					// break;
				}
			}
		}

		if (!objecttriggeralreadypresent) {
			DataObject<?> thisobject = newtrigger.getContextobject();
			if (thisobject instanceof UniqueidentifiedInterface) {
				DataObjectId<?> thisobjectid = ((UniqueidentifiedInterface<?>) thisobject).getId();
				mainlogger.fine("   --- *** [EXIND=" + executionindex + "]-- adding normally trigger "
						+ newtrigger.toString() + " ID=" + thisobjectid + " at index " + currenttriggers.size());

			}
			mainlogger.fine(" adding triggers from currentriggers list = " + currenttriggers.toString() + " for Thread "
					+ Thread.currentThread().getId());

			currenttriggers.add(newtrigger);
		} else {
			if (executionindex >= 0)
				mainlogger.fine("During execution [EXIND=" + executionindex + "] of trigger " + executionindex
						+ ", did not add object " + newtriggerid + " as already present in further event");
			if (executionindex == -1)
				mainlogger.fine("During initial configuration [EXIND=" + executionindex + "], did not add object "
						+ newtriggerid + " as already present in further event");
		}
	}

	/**
	 * This method will execute triggers for the calling thread with the following
	 * order
	 * <ul>
	 * <li>first execute the internal triggers</li>
	 * <li>then persist objects</li>
	 * <li>then execute custom triggers</li>
	 * </ul>
	 * 
	 */
	public void executeTriggerList() {
		ArrayList<TriggerToExecute<?>> triggerforthread = triggerstoexecute.get();
		int index = 0;
		if (triggerforthread != null)
			while (index < triggerforthread.size()) {
				if (index > 1024)
					throw new RuntimeException("Breaker: more than 1024 external threads for an action");
				TriggerToExecute<?> thistrigger = triggerforthread.get(index);
				if (!thistrigger.isCustomTrigger()) {
					mainlogger.info(" ------ ** -- ** -- executing trigger index " + index + " out of current size "
							+ triggerforthread.size());
					@SuppressWarnings({ "rawtypes", "unchecked" })
					NamedList<TriggerToExecute> newtriggers = (NamedList) triggerforthread.get(index).execute(true);
					for (int j = 0; j < newtriggers.getSize(); j++) {
						triggerforthread.add(newtriggers.get(j));
						mainlogger.info("				-*- adding a new trigger ");
					}
				}
				index++;
			}
		HashMap<String, UniqueidentifiedInterface<?>> buffer = triggersobjectbuffer.get();
		mainlogger.info(" ----------- ** ** -- updating objects in the trigger buffer, buffer size =  "
				+ (buffer != null ? buffer.size() : "empty"));
		if (buffer != null) {
			Iterator<Entry<String, UniqueidentifiedInterface<?>>> bufferlist = buffer.entrySet().iterator();
			while (bufferlist.hasNext()) {
				bufferlist.next().getValue().update();
			}
			mainlogger.info(" ----------- ** ** -- updated object buffer ------------ ** ** --  ");
		}
		// note on second wave: if second wave generates non custom triggers, they will
		// not be executed.
		// note 2 on second wave: there is no buffer. Not sure why, it makes things very
		// slow
		index = 0;
		if (triggerforthread != null)
			while (index < triggerforthread.size()) {
				currentriggerexecution.set(new Integer(index));
				if (index > 1024)
					throw new RuntimeException("Breaker: more than 1024 external threads for an action");
				TriggerToExecute<?> thistrigger = triggerforthread.get(index);
				if (thistrigger.isCustomTrigger()) {
					mainlogger.info(" ------ ** -- ** -- executing custom trigger index " + index
							+ " out of current size " + triggerforthread.size());
					@SuppressWarnings({ "rawtypes", "unchecked" })
					NamedList<TriggerToExecute> newtriggers = (NamedList) triggerforthread.get(index).execute(true);
					for (int j = 0; j < newtriggers.getSize(); j++) {
						triggerforthread.add(newtriggers.get(j));
						mainlogger.info("				-*- adding a new trigger ");
					}
				}
				index++;
			}

	}

	private ThreadLocal<String> connectionip = new ThreadLocal<String>();
	private ThreadLocal<String> connectioncid = new ThreadLocal<String>();
	private ThreadLocal<DataObjectId<Appuser>> connectionuserid = new ThreadLocal<DataObjectId<Appuser>>();
	private ThreadLocal<Integer> currentriggerexecution = new ThreadLocal<Integer>();
	private ThreadLocal<Long> sequenceperthread = new ThreadLocal<Long>();
	private ThreadLocal<
			ArrayList<TriggerToExecute<?>>> triggerstoexecute = new ThreadLocal<ArrayList<TriggerToExecute<?>>>();
	private ThreadLocal<HashMap<String, UniqueidentifiedInterface<?>>> triggersobjectbuffer = new ThreadLocal<
			HashMap<String, UniqueidentifiedInterface<?>>>();

	/**
	 * gets a transient sequence for the given thread
	 * 
	 * @return the transient sequence
	 */
	public long getNextSequence() {
		Long lastsequence = sequenceperthread.get();
		if (lastsequence == null)
			lastsequence = new Long(0);
		Long nextsequence = new Long(lastsequence.intValue() + 1);
		sequenceperthread.set(nextsequence);
		return nextsequence.longValue();
	}

	/**
	 * sets the ip for the connection for the calling thread (client ip)
	 * 
	 * @param ip ip calling for the thread
	 */
	public void setIpForConnection(String ip) {
		connectionip.set(ip);
	}

	/**
	 * gets the ip for the connection for the calling thread (client ip)
	 * 
	 * @return
	 */
	public String getIpForConnection() {
		return connectionip.get();
	}

	/**
	 * removes the reference to the ip address of the client
	 */
	public void removeIpConnectionReference() {
		connectionip.remove();
	}

	/**
	 * sets the user if for the connection for the calling thread
	 * 
	 * @param appuserid userid for the calling thread
	 */
	public void setUserIdForConnection(DataObjectId<Appuser> appuserid) {
		connectionuserid.set(appuserid);
	}

	/**
	 * @return the appuser id that was registered the last time for this thread.
	 *         Note: this does not make a check that the session is not expired, and
	 *         so this shouldnot be used by security mechanism at the beginning of a
	 *         transaction
	 */
	public DataObjectId<Appuser> getUserIdForConnection() {
		return connectionuserid.get();
	}

	/**
	 * sets the client id for the connection for the calling thread
	 * 
	 * @param cid client id
	 */
	public void setCidForConnection(String cid) {
		connectioncid.set(cid);
	}

	/**
	 * @return gets the client id for the connection for the calling thread
	 */
	public String getCidForConnection() {
		return connectioncid.get();
	}

	/**
	 * remove the reference to the client id for the connection for the calling
	 * thread
	 */
	public void removeCidConnectionReference() {
		connectioncid.remove();
	}

	/**
	 * generates a client id
	 * 
	 * @return the client id
	 */
	public String generateCid() {
		long time = new Date().getTime();
		return "" + time;
	}

	/**
	 * This method returns in an efficient manner the current user id. It will not
	 * check if the user still have a valid session (timeout management)
	 * 
	 * @return the current user id for thread
	 */
	public DataObjectId<Appuser> getCurrentUserId() {
		return getUserIdForConnection();
	}

	/**
	 * This method returns in a high performance manner the current user. It will
	 * not check if the user still have a valid session (timeout management).
	 * 
	 * @return the current user for thread
	 */
	public Appuser getCurrentUser() {
		return ServerSecurityBuffer.getUniqueInstance().getUserPerUserId(getCurrentUserId());
	}

	/**
	 * checks if the current user is admin for the given module (either directly
	 * overlord of the module or sovereign on the server)
	 * 
	 * @param module the name of the module to check for
	 * @return true if the current user if admin for the module as of rules set
	 *         above
	 */
	public boolean isCurrentUserAdmin(String modulename) {

		SModule module = this.getModuleByName(modulename);
		if (module == null)
			throw new RuntimeException("Could not find module for name = " + modulename);
		DataObjectId<Appuser> currentuserid = getCurrentUserId();
		if (currentuserid == null)
			throw new RuntimeException("Could not find user for id = " + currentuserid);
		Authority[] userauthorities = ServerSecurityBuffer.getUniqueInstance().getAuthoritiesForUser(currentuserid);
		for (int i = 0; i < userauthorities.length; i++) {
			if (module.isAuthorityAdmin(userauthorities[i].getNr()))
				return true;
			if ("SOVEREIGN".equals(userauthorities[i].getNr()))
				return true;
		}
		return false;
	}

	/**
	 * sends a shutdown request to all connections
	 */
	public void sendShutdownToAllConnections() {
		connectionlisterner.sendShutdownToAllConnections();

	}

	/**
	 * reports that the thread with the following id is finished
	 * 
	 * @param id reports that a thread with
	 */
	public void reportThreadFinished(long id) {
		connectionlisterner.reportThreadFinished(id);

	}

	/**
	 * removes the all the log exceptions
	 * 
	 * @param filelog true if exceptions are removed from the file log, false if
	 *                exceptions are removed from the console log
	 */
	public void removeLogExceptions(boolean filelog) {
		if (filelog)
			this.filelogfilter.cleanException();
		if (!filelog)
			this.consolelogfilter.cleanException();

	}

}
