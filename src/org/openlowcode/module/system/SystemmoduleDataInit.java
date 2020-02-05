/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system;

import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.data.Groupadminlink;
import org.openlowcode.module.system.data.Groupmemberlink;
import org.openlowcode.module.system.data.Groupswithauthority;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.server.runtime.SModule.ModuleDataInit;

/**
 * Initialization of data for the system module
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SystemmoduleDataInit
		implements
		ModuleDataInit {
	private static final String ADMIN_USER = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private static final String ADMIN_LASTNAME = "Server Administrator";
	private static final String ADMIN_GROUP = "SERVERADMIN";
	private static final String ADMIN_GROUP_DESCRIPTION = "users with sovereign authority over the whole empire";
	private static final String COMMONS_DOMAIN = "COMMONS";
	private static final String ROOT_DOMAIN = "EMPIRE";
	private static final String SOVEREIGN_AUTHORITY = "SOVEREIGN";

	private static final String TEST_USER = "test";
	private static final String TEST_PASSWORD = "test";

	private Logger logger = Logger.getLogger(SystemmoduleDataInit.class.getName());

	@Override
	public void initiateData() {
		logger.info("Starting Initialization of data for System module");
		Domain[] empires = Domain.getobjectbynumber(ROOT_DOMAIN);
		Domain empire = null;
		if (empires.length == 0) {
			empire = new Domain();
			empire.setobjectnumber(ROOT_DOMAIN);
			empire.insert();
			empire.setparentforhierarchy(empire.getId());
			empire.update();
			logger.info("create domain EMPIRE");
		} else {
			empire = empires[0];
		}
		Domain[] commonses = Domain.getobjectbynumber(COMMONS_DOMAIN);
		Domain commons = null;
		if (commonses.length == 0) {
			commons = new Domain();
			commons.setobjectnumber(COMMONS_DOMAIN);
			commons.setparentforhierarchy(empire.getId());
			commons.insert();
			logger.info("create domain COMMONS");
		} else {
			commons = commonses[0];
		}
		Authority[] sovereignes = Authority.getobjectbynumber(SOVEREIGN_AUTHORITY);
		Authority sovereign = null;
		if (sovereignes.length == 0) {
			sovereign = new Authority();
			sovereign.setobjectnumber(SOVEREIGN_AUTHORITY);
			sovereign.setparentforscope(empire.getId());
			sovereign.insert();
		} else {
			sovereign = sovereignes[0];
		}

		Usergroup[] admingroups = Usergroup.getobjectbynumber(ADMIN_GROUP);
		Usergroup admingroup = null;
		if (admingroups.length == 0) {
			admingroup = new Usergroup();
			admingroup.setobjectnumber(ADMIN_GROUP);
			admingroup.setDescription(ADMIN_GROUP_DESCRIPTION);
			admingroup.insert();
		} else {
			admingroup = admingroups[0];
		}

		Groupswithauthority adminsovereignlink[] = Groupswithauthority.getalllinksfromleftandrightid(sovereign.getId(),
				admingroup.getId(), null);

		if (adminsovereignlink.length == 0) {
			Groupswithauthority newadminsovereignlink = new Groupswithauthority();
			newadminsovereignlink.setleftobject(sovereign.getId());
			newadminsovereignlink.setrightobject(admingroup.getId());
			newadminsovereignlink.insert();
		}

		Appuser[] admins = Appuser.getobjectbynumber(ADMIN_USER);
		Appuser admin = null;
		if (admins.length == 0) {
			admin = new Appuser();
			admin.setobjectnumber(ADMIN_USER);
			admin.setLastname(ADMIN_LASTNAME);
			admin.setPassword(ADMIN_PASSWORD);
			admin.insert();
		} else {
			admin = admins[0];
		}

		Groupmemberlink sovereignmember[] = Groupmemberlink.getalllinksfromleftandrightid(admingroup.getId(),
				admin.getId(), null);

		if (sovereignmember.length == 0) {
			Groupmemberlink newsovereignmember = new Groupmemberlink();
			newsovereignmember.setleftobject(admingroup.getId());
			newsovereignmember.setrightobject(admin.getId());
			newsovereignmember.insert();
		}

		Groupadminlink sovereignadmin[] = Groupadminlink.getalllinksfromleftandrightid(admingroup.getId(),
				admin.getId(), null);
		if (sovereignadmin.length == 0) {
			Groupadminlink newsovereignadmin = new Groupadminlink();
			newsovereignadmin.setleftobject(admingroup.getId());
			newsovereignadmin.setrightobject(admin.getId());
			newsovereignadmin.insert();
		}

	}

}
