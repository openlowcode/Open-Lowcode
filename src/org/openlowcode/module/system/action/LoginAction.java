/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsLoginAction;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.module.system.page.SimpleloginPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Login action on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LoginAction
		extends
		AbsLoginAction {
	private static Logger logger = Logger.getLogger(LoginAction.class.getName());

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LoginAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(
			String user,
			String password,
			String contextaction,
			Function<TableAlias, QueryFilter> datafilter) {

		Usersession session = OLcServer.getServer().getSecuritymanager().createSession(
				OLcServer.getServer().getIpForConnection(), OLcServer.getServer().getCidForConnection(), user,
				password);

		if (session != null) {
			logger.info("succesfull login of user " + user + " session id = " + session.getId() + " client ip = "
					+ session.getClientip());
			return new ActionOutputData(true, contextaction);
		} else {

			return new ActionOutputData(false, contextaction);
		}
	}

	@Override
	public SPage choosePage(ActionOutputData outputdata) {
		if (outputdata.getIsloginok()) {
			// case loginOK and no context action specified
			if (outputdata.getContextactionthru() == null)
				return OLcServer.getServer().getMainmodule().getDefaultPage();
			if (outputdata.getContextactionthru().length() == 0)
				return OLcServer.getServer().getMainmodule().getDefaultPage();
			return null;

		} else {
			return new SimpleloginPage(outputdata.getContextactionthru());
		}
	}

}
