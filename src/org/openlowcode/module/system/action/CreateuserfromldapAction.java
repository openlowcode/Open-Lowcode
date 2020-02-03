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

import org.openlowcode.module.system.action.generated.AbsCreateuserfromldapAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Groupmemberlink;
import org.openlowcode.module.system.data.Ldapuser;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.module.system.data.choice.AuthtypeChoiceDefinition;
import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;
import org.openlowcode.module.system.page.CreateuserfromldapPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * Creates a user in the application based on LDAP data, and using the LDAP to
 * check password. This allows users to login with their enterprise password
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CreateuserfromldapAction
		extends
		AbsCreateuserfromldapAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public CreateuserfromldapAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			Ldapuser[] selectedldapuser,
			DataObjectId<Usergroup> group,
			String grouplabel,
			String userid,
			String userpasword,
			String ldapbase,
			ChoiceValue<ApplocaleChoiceDefinition> locale,
			ChoiceValue<PreferedfileencodingChoiceDefinition> encoding,
			Function<TableAlias, QueryFilter> datafilter) {
		int created = 0;
		int updated = 0;
		for (int i = 0; i < selectedldapuser.length; i++) {
			Ldapuser thisldapuser = selectedldapuser[i];
			String id = thisldapuser.getId().toLowerCase();
			Appuser[] existinguser = Appuser.getobjectbynumber(id);
			boolean newuser = true;
			Appuser usertotreat = new Appuser();
			if (existinguser != null)
				if (existinguser.length == 1) {
					newuser = false;
					usertotreat = existinguser[0];
				}
			if (newuser) {
				usertotreat.setobjectnumber(id);
				usertotreat.setAuthtype(AuthtypeChoiceDefinition.get().LDAP);

			}
			if (usertotreat.getPreflang() == null)
				usertotreat.setPreflang(locale);
			if (usertotreat.getPreffileenc() == null)
				usertotreat.setPreffileenc(encoding);
			usertotreat.setEmail(thisldapuser.getMail());
			usertotreat.setDepartment(thisldapuser.getDepartment());
			usertotreat.setFirstname(thisldapuser.getFirstname());
			usertotreat.setLastname(thisldapuser.getLastname());
			usertotreat.setLdapfullname(thisldapuser.getDistinguishedname());
			if (newuser) {
				usertotreat.insert();
				created++;
			} else {
				usertotreat.update();
				updated++;
			}
			// check if user already linked to group
			Groupmemberlink[] existinglinks = Groupmemberlink.getalllinksfromleftandrightid(group, usertotreat.getId(),
					null);
			if (existinglinks.length == 0) {
				Groupmemberlink newlink = new Groupmemberlink();
				newlink.setleftobject(group);
				newlink.setrightobject(usertotreat.getId());
				newlink.insert();

			}
			// else create it

		}

		return new ActionOutputData(userid, group, grouplabel, userpasword, ldapbase, locale, encoding,
				"" + created + " User(s) created and " + updated + " User(s) updated;");
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new CreateuserfromldapPage(logicoutput.getGroupid_thru(), logicoutput.getGrouplabel_thru(),
				logicoutput.getUserid_thru(), logicoutput.getUserpasword_thru(), logicoutput.getLdapbase_thru(),
				logicoutput.getLocale_thru(), logicoutput.getEncoding_thru(), logicoutput.getMessage());
	}

}
