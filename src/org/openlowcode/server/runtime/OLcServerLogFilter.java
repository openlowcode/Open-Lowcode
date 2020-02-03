
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

import java.util.logging.Logger;
import java.util.ArrayList;


import java.util.logging.Handler;
import java.util.logging.Level;
import org.openlowcode.tools.misc.OpenLowcodeLogFilter;

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
 * </li> Exceptions can be entered as a classpath, or as aliases. Aliases
 * supported are:
 * <li>
 * <ul>
 * 
 * @PERSISTENCE: all classes performing actions in the database (excludes
 *               connection pool that has its own log class)
 *               </ul>
 *               <ul>
 * @SECURITY: all security handlers
 *            </ul>
 *            </li>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcServerLogFilter extends OpenLowcodeLogFilter {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(OLcServerLogFilter.class.getName());

	/**
	 * Creates a Server log filter with presetting for persistence and security
	 * (keywords @PERSISTENCE and @SECURITY)
	 * 
	 * @param defaultlevel   default level to filter log
	 * @param label          label of the lof filter
	 * @param relatedhandler related log handler
	 */
	public OLcServerLogFilter(Level defaultlevel, String label, Handler relatedhandler) {
		super(defaultlevel, label, relatedhandler);
	}

	@Override
	protected String[] getPathesForAlias(String path) {
		if (path.equals("@PERSISTENCE")) {
			ArrayList<String> pathes = new ArrayList<String>();
			pathes.add("org.openlowcode.server.data.storage.derbyjdbc.DerbyJDBCStorage");
			pathes.add("org.openlowcode.server.data.storage.mariadbjdbc.MariaDBJDBCStorage");
			pathes.add("org.openlowcode.server.data.storage.standardjdbc.BaseJDBCStorage");
			pathes.add("org.openlowcode.server.data.storage.standardjdbc.JDBCRow");
			pathes.add("org.openlowcode.server.data.storage.standardjdbc.SQLQueryConditionGenerator");
			pathes.add("org.openlowcode.server.data.storage.standardjdbc.SQLQueryPSFiller");
			return pathes.toArray(new String[0]);
		}
		if (path.equals("@SECURITY")) {
			ArrayList<String> pathes = new ArrayList<String>();
			pathes.add("org.openlowcode.server.security.ActionAnarchySecurityManager");
			pathes.add("org.openlowcode.server.security.ActionAuthorization");
			pathes.add("org.openlowcode.server.security.ActionObjectDomainSecurityManager");
			pathes.add("org.openlowcode.server.security.ActionObjectSecurityManager");
			pathes.add("org.openlowcode.server.security.ActionObjectStateDomainSecurityManager");
			pathes.add("org.openlowcode.server.security.ActionObjectStateSecurityManager");
			pathes.add("org.openlowcode.server.security.ActionSecurityManager");
			pathes.add("org.openlowcode.server.security.ActionTotalSecurityManager");
			pathes.add("org.openlowcode.server.security.SecurityBuffer");
			pathes.add("org.openlowcode.server.security.SecurityManager");
			pathes.add("org.openlowcode.server.security.OLcServerSecurityBuffer");
			pathes.add("org.openlowcode.server.security.OLcServerSession");
			return pathes.toArray(new String[0]);
		}
		return null;
	}

}
