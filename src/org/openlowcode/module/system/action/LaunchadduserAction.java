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

import org.openlowcode.module.system.action.generated.AbsLaunchadduserAction;
import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;
import org.openlowcode.module.system.page.CreateuserfromldapPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * Launching the create user from LDAP page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LaunchadduserAction
		extends
		AbsLaunchadduserAction {
	public static final String LDAPBASEPARAMETER = "S0.LDAPBASESEARCH";
	public static final String DEFAULTLOCALE = "S0.PREFEREDLOCALE";
	public static final String DEFAULTENCODING = "S0.PREFEREDENCODING";

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LaunchadduserAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(
			DataObjectId<Usergroup> groupid,
			Function<TableAlias, QueryFilter> datafilter) {
		String userid = "";
		String password = "";
		String searchbase = "";
		Usergroup group = Usergroup.readone(groupid);
		String grouplabel = "Adding user to group " + group.getNr() + " - " + group.getDescription();
		if (grouplabel.length() > 128)
			grouplabel = grouplabel.substring(0, 124) + "...";
		Systemattribute[] basesearchattr = Systemattribute.getobjectbynumber(LDAPBASEPARAMETER);
		if (basesearchattr != null)
			if (basesearchattr.length == 1)
				searchbase = basesearchattr[0].getValue();
		ChoiceValue<ApplocaleChoiceDefinition> preferedlocale = null;
		Systemattribute preferedlocaleattr = Systemattribute.getuniqueobjectbynumber(DEFAULTLOCALE, null);
		if (preferedlocaleattr != null)
			preferedlocale = ApplocaleChoiceDefinition.get().parseValueFromStorageCode(preferedlocaleattr.getValue());
		ChoiceValue<PreferedfileencodingChoiceDefinition> defaultencoding = null;
		Systemattribute defaultencodingattr = Systemattribute.getuniqueobjectbynumber(DEFAULTENCODING, null);
		if (defaultencodingattr != null)
			defaultencoding = PreferedfileencodingChoiceDefinition.get()
					.parseValueFromStorageCode(defaultencodingattr.getValue());

		return new ActionOutputData(groupid, grouplabel, userid, password, searchbase, preferedlocale, defaultencoding);

	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new CreateuserfromldapPage(logicoutput.getGroupid_thru(), logicoutput.getGrouplabel_thru(),
				logicoutput.getUserid(), logicoutput.getPassword(), logicoutput.getLdapbase(), logicoutput.getLocale(),
				logicoutput.getEncoding(), "Please start search to add users");
	}

}
