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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsGetsessionforclientAction;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * this action gets the session already registered for the current client
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GetsessionforclientAction
		extends
		AbsGetsessionforclientAction {
	private Logger logger = Logger.getLogger(GetsessionforclientAction.class.getName());
	public final static long TIMEOUT_SECOND = 1800;

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public GetsessionforclientAction(SModule parent) {
		super(parent);
	}

	@Override
	public Usersession executeActionLogic(
			String clientip,
			String clientpid,
			Function<TableAlias, QueryFilter> datafilter) {

		AndQueryCondition selectactiveonipandcid = new AndQueryCondition();
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<String>(null,
				Usersession.getDefinition().getClientipFieldSchema(), new QueryOperatorEqual<String>(), clientip));
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<String>(null,
				Usersession.getDefinition().getClientpidFieldSchema(), new QueryOperatorEqual<String>(), clientpid));
		selectactiveonipandcid.addCondition(new SimpleQueryCondition<Date>(null,
				Usersession.getDefinition().getEndtimeFieldSchema(), new QueryOperatorEqual<Date>(), null));
		Usersession[] relevantsessions = Usersession.getallactive(QueryFilter.get(selectactiveonipandcid));
		if (relevantsessions.length == 0) {
			logger.info("no session open for client '" + clientip + "' and cid = '" + clientpid
					+ "'. Pingopensession returns null");
			return null;
		}
		if (relevantsessions.length > 1) {
			logger.info("several sessions open for uclient '" + clientip + "' and cid = '" + clientpid
					+ "'. closing all sessions, and ask to log again. This is not application normal behaviour");
			for (int i = 0; i < relevantsessions.length; i++) {
				Usersession thissession = relevantsessions[i];
				thissession.setEndtime(new Date());
				thissession.update();
			}
			return null;
		}
		Usersession thissession = relevantsessions[0];
		long lasttouch = thissession.getLastaction().getTime();
		if ((new Date()).getTime() - lasttouch > TIMEOUT_SECOND * 1000) {
			thissession.setEndtime(thissession.getLastaction());
			thissession.update();
			logger.info("closing session for user '" + clientip + "' and cid = '" + clientpid + "' as timeout ");
			return null;
		}
		if (OLcServer.getServer().minimumLevelForClass(GetsessionforclientAction.class.getName())
				.intValue() <= Level.INFO.intValue()) {
			logger.info(" --------- Temp Log for session " + (thissession.getActions().intValue() + 1));
			StackTraceElement[] threads = Thread.currentThread().getStackTrace();
			for (int i = 1; i < Integer.min(15, threads.length); i++)
				logger.info("           * " + threads[i].toString());
		}
		thissession.setActions(thissession.getActions().intValue() + 1);
		thissession.setLastaction(new Date());
		thissession.update();
		return thissession;
	}

	@Override
	public SPage choosePage(Usersession usersession) {
		throw new RuntimeException("Choose Page not implemented  for this action");
	}

}
