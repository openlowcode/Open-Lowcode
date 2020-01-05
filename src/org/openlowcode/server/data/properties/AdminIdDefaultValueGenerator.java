/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.AppuserDefinition;
import org.openlowcode.server.data.storage.DefaultValueGenerator;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;

/**
 * A utility class generating the admin id to be used in creating the default
 * objects of the platform
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AdminIdDefaultValueGenerator implements DefaultValueGenerator<String> {
	private static AdminIdDefaultValueGenerator singleton = new AdminIdDefaultValueGenerator();
	private String answer = null;
	private static Logger logger = Logger.getLogger(AdminIdDefaultValueGenerator.class.getName());

	@Override
	public String generateDefaultvalue() {
		return answer;

	}

	private AdminIdDefaultValueGenerator() {

	}

	/**
	 * getter of the singleton
	 * 
	 * @return the singleton class for this helper. It is not safe for multi-thread
	 *         at first use, however, this is not a problem, as first use will be by
	 *         the single thread main server
	 */
	public static AdminIdDefaultValueGenerator get() {
		return singleton;
	}

	/**
	 * gets the id of the admin user
	 */
	public void computeValue() {
		logger.info(" - startingcompute value for admin -");
		String defaultcreateuserid = null;
		try {

			PersistentStorage storage = PersistenceGateway.getStorage();
			boolean usertableexists = storage
					.DoesObjectExist(AppuserDefinition.getAppuserDefinition().getTableschema());
			PersistenceGateway.checkinStorage(storage);
			if (usertableexists) {

				Appuser[] appuser = Appuser.getobjectbynumber("admin");
				if (appuser.length != 1)
					throw new RuntimeException("appuser is null");
				defaultcreateuserid = appuser[0].getId().getId();
			}
		} catch (Exception e) {
			logger.info(
					"tried to specified admin as default createuserid, but could not find admin. This is normal if first application execution");
			logger.info("exception got = " + e.getClass() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.info(e.getStackTrace()[i].toString());
			}
			PersistenceGateway.releaseForThread();
		}
		this.answer = defaultcreateuserid;
	}

}
