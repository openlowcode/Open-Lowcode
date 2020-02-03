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

import org.openlowcode.module.system.action.generated.AbsLaunchsessioncleaningAction;
import org.openlowcode.module.system.data.Usersession;
import org.openlowcode.module.system.page.SessioncleaningPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;
/**
 * Launching the session cleaning action
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LaunchsessioncleaningAction extends AbsLaunchsessioncleaningAction {
	private static Logger logger = Logger.getLogger(LaunchsessioncleaningAction.class.getName());
	
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public LaunchsessioncleaningAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		Usersession[] usersession = Usersession.getallactive(null);
		Date oldestdate = null;
		for (int i=0;i<usersession.length;i++) {
			Usersession thissession = usersession[i];
			if (oldestdate==null) oldestdate = thissession.getStarttime();
			if (oldestdate.compareTo(thissession.getStarttime())>0) oldestdate=thissession.getStarttime();
		}
		long gapms = new Date().getTime() - oldestdate.getTime();
		long numberofdaysl = gapms/(86400*1000);
		int numberofdays = (int) (numberofdaysl);
		logger.severe("   --- Log oldest date = "+oldestdate+" number of days ="+numberofdays);
		return new ActionOutputData(new Integer(usersession.length),new Integer(numberofdays));
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput)  {
		return new SessioncleaningPage(logicoutput.getNumberofitems(),logicoutput.getOldestlog());
	}

}
