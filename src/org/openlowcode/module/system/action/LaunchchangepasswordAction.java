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

import org.openlowcode.module.system.action.generated.AbsLaunchchangepasswordAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.choice.AuthtypeChoiceDefinition;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.ChangepasswordPage;
import org.openlowcode.module.system.page.ShowkeymessagePage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Launch the change password page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LaunchchangepasswordAction
		extends
		AbsLaunchchangepasswordAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LaunchchangepasswordAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		DataObjectId<Appuser> embeddedactionuserid = OLcServer.getServer().getCurrentUserId();
		// normally useless as control is done before launching the action
		if (embeddedactionuserid == null)
			throw new RuntimeException("No active connection");
		Appuser user = Appuser.readone(embeddedactionuserid);
		boolean ldap = false;
		if (user.getAuthtype() != null)
			if (user.getAuthtype().getStorageCode().equals(AuthtypeChoiceDefinition.get().LDAP.getStorageCode()))
				ldap = true;
		if (ldap) {
			return new ActionOutputData(BooleanChoiceDefinition.get().NO,
					"Your user is managed by LDAP, you cannot change password");
		}
		return new ActionOutputData(BooleanChoiceDefinition.get().YES,
				"Please enter your old and new password below as specified");
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		if (logicoutput.getChangepasswordpossible().equals(BooleanChoiceDefinition.get().NO))
			return new ShowkeymessagePage(logicoutput.getMessage());
		return new ChangepasswordPage(logicoutput.getMessage());
	}

}
