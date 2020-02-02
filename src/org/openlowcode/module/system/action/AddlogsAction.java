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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsAddlogsAction;
import org.openlowcode.module.system.data.choice.LoglevelChoiceDefinition;
import org.openlowcode.module.system.page.AddlogsPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;



import org.openlowcode.server.data.storage.QueryFilter;
/**
 * add specific logging to the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AddlogsAction extends AbsAddlogsAction {

	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(AddlogsAction.class.getName());
	/**
	 * Creates the action 
	 * 
	 * @param parent parent module
	 */
	public AddlogsAction(SModule parent) {
		super(parent);
		
		
	}

	public static Level levelconverter(ChoiceValue<LoglevelChoiceDefinition> logvalue)  {
		if (logvalue.equals(LoglevelChoiceDefinition.get().SEVERE)) return Level.SEVERE;
		if (logvalue.equals(LoglevelChoiceDefinition.get().WARNING)) return Level.WARNING;
		if (logvalue.equals(LoglevelChoiceDefinition.get().INFO)) return Level.INFO;
		if (logvalue.equals(LoglevelChoiceDefinition.get().CONFIG)) return Level.CONFIG;
		if (logvalue.equals(LoglevelChoiceDefinition.get().FINE)) return Level.FINE;
		if (logvalue.equals(LoglevelChoiceDefinition.get().FINER)) return Level.FINER;
		if (logvalue.equals(LoglevelChoiceDefinition.get().FINEST)) return Level.FINEST;
		throw new RuntimeException("Log Level Invalid "+logvalue);
		
	}
	
	
	
	@Override
	public void executeActionLogic(String path, ChoiceValue<LoglevelChoiceDefinition> textlog,
			ChoiceValue<LoglevelChoiceDefinition> consolelog, Function<TableAlias, QueryFilter> datafilter)
			 {
		
		if (textlog!=null) OLcServer.getServer().addException(path,levelconverter(textlog),true);
		if (consolelog!=null) OLcServer.getServer().addException(path,levelconverter(consolelog),false);
		
		

	}

	@Override
	public SPage choosePage()  {
		return new AddlogsPage();
	}

}
