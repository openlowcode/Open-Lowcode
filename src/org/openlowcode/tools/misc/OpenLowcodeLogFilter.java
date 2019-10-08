/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * The filter will let the following elements pass:
 * <li>
 * <ul>
 * All elements with level equal or more than the default level (e.g. WARNING)
 * </ul>
 * <ul>
 * All elements with a path in the exception list, and more than the exception
 * level (e.g. INFO)
 * </ul>
 * </li> Exceptions can be entered as a classpath, or as aliases.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class OpenLowcodeLogFilter implements Filter {

	private Level defaultlevel;
	private ConcurrentHashMap<String, Level> exceptionlist;
	private String label;
	private Handler relatedhandler;
	private static Logger logger = Logger.getLogger(OpenLowcodeLogFilter.class.getName());

	/**
	 * Creates a filter. You just need to create the filter by setting the fields
	 * above, and the filter will be active
	 * 
	 * @param defaultlevel   by default, logs up to this level will be
	 * @param label          label for logging purposes
	 * @param relatedhandler handler that will be used for the filter
	 */
	public OpenLowcodeLogFilter(Level defaultlevel, String label, Handler relatedhandler) {
		this.defaultlevel = defaultlevel;
		exceptionlist = new ConcurrentHashMap<String, Level>();
		this.label = label;
		this.relatedhandler = relatedhandler;
	}

	/**
	 * @param path  a path or an alias. Supported Aliases are explained in the class
	 *              main description above.
	 * @param level log level for the exception. Typically, this is a very detailed
	 *              level, and it needs to be more than the normal log level to have
	 *              an effect
	 */
	public void addException(String path, Level level) {
		String[] pathesforalias = getPathesForAlias(path);
		if (pathesforalias == null) {
			exceptionlist.put(path, level);
			Logger.getLogger(path).addHandler(relatedhandler);
			Logger.getLogger(path).setLevel(Level.ALL);
			logger.severe(" adding directly exception path " + path + " for logger " + label + " for level "
					+ level.getName());
		} else {
			logger.severe(" adding exception path for alias " + path + " for logger " + label + " for level "
					+ level.getName());
			for (int i = 0; i < pathesforalias.length; i++) {
				exceptionlist.putIfAbsent(pathesforalias[i], level);
				Logger.getLogger(pathesforalias[i]).addHandler(relatedhandler);
				Logger.getLogger(pathesforalias[i]).setLevel(Level.ALL);
				logger.severe("    * " + pathesforalias[i]);
			}
		}
	}

	/**
	 * This method allows a subclass to define aliases. Those are shortcuts to add
	 * extra logs to a number of classes. By default, this method does not have any
	 * aliases, but it can be overridden by a subclass.
	 * 
	 * @param alias a defined alias for several pathes
	 * @return null if this is not a valid alias, or the list of pathes
	 */
	protected String[] getPathesForAlias(String alias) {
		return null;
	}

	/**
	 * cleans all exceptions to the defined level.
	 */
	public void cleanException() {
		logger.severe("Cleaning logger exception list " + this.label);
		Iterator<String> exceptionloggers = exceptionlist.keySet().iterator();
		while (exceptionloggers.hasNext()) {
			String thisloggername = exceptionloggers.next();
			Logger thislogger = Logger.getLogger(thisloggername);
			thislogger.removeHandler(relatedhandler);
			thislogger.setLevel(defaultlevel);
		}
		exceptionlist = new ConcurrentHashMap<String, Level>();
	}

	@Override
	public boolean isLoggable(LogRecord record) {

		Level level = record.getLevel();
		if (level.intValue() >= defaultlevel.intValue())
			return true;
		String loggername = record.getLoggerName();
		Level exceptionlevel = exceptionlist.get(loggername);
		if (exceptionlevel != null)
			if (level.intValue() >= exceptionlevel.intValue())
				return true;
		return false;
	}

	/**
	 * @param path the path of a class
	 * @return the logging level for the path, taking into account if it is part of
	 *         the exception list
	 */
	public Level getLevelForPath(String path) {
		Level exceptionlevel = exceptionlist.get(path);
		if (exceptionlevel != null)
			return exceptionlevel;
		return defaultlevel;
	}

}
