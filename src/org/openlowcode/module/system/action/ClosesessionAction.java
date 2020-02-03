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

import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsClosesessionAction;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.module.system.page.SimpleloginPage;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * an action to close the session for the current thread
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClosesessionAction
		extends
		AbsClosesessionAction {
	private static Logger logger = Logger.getLogger(ClosesessionAction.class.getName());

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ClosesessionAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		AndQueryCondition selectactiveonipandcid = new AndQueryCondition();
		String ip = OLcServer.getServer().getIpForConnection();
		String cid = OLcServer.getServer().getCidForConnection();
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<String>(null,
				Usersession.getDefinition().getClientipFieldSchema(), new QueryOperatorEqual<String>(), ip));
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<String>(null,
				Usersession.getDefinition().getClientpidFieldSchema(), new QueryOperatorEqual<String>(), cid));
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<Date>(null,
				Usersession.getDefinition().getEndtimeFieldSchema(), new QueryOperatorEqual<Date>(), null));
		Usersession[] relevantsessions = Usersession.getallactive(QueryFilter.get(selectactiveonipandcid));
		if (relevantsessions.length > 1) {
			Logger.getLogger("").info("several sessions open for uclient '" + ip + "' and cid = '" + cid
					+ "'. closing all sessions, and ask to log again. This is not application normal behaviour");
			for (int i = 0; i < relevantsessions.length; i++) {
				Usersession thissession = relevantsessions[i];
				thissession.setEndtime(new Date());
				thissession.update();
			}

		}
		if (relevantsessions.length == 1) {
			Usersession thissession = relevantsessions[0];
			thissession.setEndtime(new Date());
			thissession.update();
		}
		if (relevantsessions.length == 0) {
			logger.warning("no session open for client '" + ip + "' and cid = '" + cid + "'.");
		}
		return null;
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new SimpleloginPage("");
	}

}
