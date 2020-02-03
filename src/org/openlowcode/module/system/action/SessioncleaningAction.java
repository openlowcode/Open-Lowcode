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

import java.util.ArrayList;
import java.util.Date;
import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsSessioncleaningAction;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;
/**
 * Action to clean sessions older than a provided nmber of days
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SessioncleaningAction
		extends
		AbsSessioncleaningAction {
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public SessioncleaningAction(SModule parent) {
		super(parent);
	}

	@Override
	public void executeActionLogic(Integer cleansessionsolderthandays, Function<TableAlias, QueryFilter> datafilter) {
		Usersession[] usersession = Usersession.getallactive(null);
		ArrayList<Usersession> sessionstodelete = new ArrayList<Usersession>();
		Date now = new Date();
		int keephistory = cleansessionsolderthandays.intValue();
		for (int i = 0; i < usersession.length; i++) {
			Usersession thissession = usersession[i];
			int daysago = (int) ((now.getTime() - thissession.getStarttime().getTime()) / (86400 * 1000));
			if (daysago > keephistory)
				sessionstodelete.add(thissession);
		}
		Usersession[] sessionstodeletearray = sessionstodelete.toArray(new Usersession[0]);
		Usersession.delete(sessionstodeletearray);
	}

	@Override
	public SPage choosePage() {
		return LaunchsessioncleaningAction.get().executeAndShowPage();
	}

}
