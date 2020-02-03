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

import org.openlowcode.module.system.action.generated.AbsChangepasswordAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.choice.AuthtypeChoiceDefinition;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.ChangepasswordPage;
import org.openlowcode.module.system.page.ShowkeymessagePage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.tools.enc.OLcEncrypter;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * action to change the password for a user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChangepasswordAction
		extends
		AbsChangepasswordAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ChangepasswordAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			String oldpassword,
			String newpassword,
			String newpasswordrepeat,
			Function<TableAlias, QueryFilter> datafilter) throws RuntimeException {
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
			return new ActionOutputData(BooleanChoiceDefinition.get().YES,
					"Your user is managed by LDAP, you cannot change password");
		}
		if (newpassword.compareTo(newpasswordrepeat) != 0)
			return new ActionOutputData(BooleanChoiceDefinition.get().NO,
					"You did not enter consistent new passwords. Please try again.");
		if (user.getPassword().compareTo(OLcEncrypter.getEncrypter().encryptStringOneWay(oldpassword)) != 0)
			return new ActionOutputData(BooleanChoiceDefinition.get().NO,
					"Old password is not correct, please try again.");
		user.setPassword(newpassword);
		user.setupdatenote("change of password initiated by user");
		user.update();
		return new ActionOutputData(BooleanChoiceDefinition.get().YES, "Your password was changed succesfully.");
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		if (logicoutput.getChangepassworddone().equals(BooleanChoiceDefinition.get().YES))
			return new ShowkeymessagePage(logicoutput.getMessage());
		return new ChangepasswordPage(logicoutput.getMessage());
	}

}
