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
import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsActivetaskcomplexqueryAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.AppuserDefinition;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.TaskDefinition;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.TaskuserDefinition;
import org.openlowcode.module.system.data.choice.TasklifecycleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.ThreeDataObjects;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.GenericlinkQueryHelper;
import org.openlowcode.server.data.properties.LifecycleQueryHelper;
import org.openlowcode.server.data.properties.LinkobjectQueryHelper;
import org.openlowcode.server.data.properties.HasidQueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;



import org.openlowcode.server.data.storage.QueryFilter;
/**
 * gets all the tasks for a given data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ActivetaskcomplexqueryAction extends AbsActivetaskcomplexqueryAction {
	/**
	 * Create the action
	 * 
	 * @param parent parent module
	 */
	public ActivetaskcomplexqueryAction(SModule parent) {
		super(parent);
	
	}

	@Override
	public ActionOutputData executeActionLogic(@SuppressWarnings("rawtypes") DataObjectId objectid,Function<TableAlias,QueryFilter> datafilter)  {
		DataObjectId<Appuser> userid =  OLcServer.getServer().getCurrentUserId();
		if (userid==null) throw new RuntimeException("Object with creationlog cannot be used in context with ip = "+OLcServer.getServer().getIpForConnection()+", cid = "+OLcServer.getServer().getCidForConnection());
		// get generic ID
		
		// -----------------------------------------------------------------------------------------------
		ArrayList<ChoiceValue<TasklifecycleChoiceDefinition>> selectedchoices = new ArrayList<ChoiceValue<TasklifecycleChoiceDefinition>>();
		selectedchoices.add(TasklifecycleChoiceDefinition.getChoiceOpen());
		selectedchoices.add(TasklifecycleChoiceDefinition.getChoiceInwork());
		
		QueryCondition objectcondition = null;
		if (objectid!=null) {
			objectcondition = GenericlinkQueryHelper.get("GENERICLINKFORTASKOBJECT").getGenericIdQueryCondition(
				new TableAlias(Task.getDefinition().getTableschema(),LinkobjectQueryHelper.LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS), 
				objectid, 
				Task.getDefinition());
		}
		QueryCondition statecondition = LifecycleQueryHelper.get().getStateSelectionQueryCondition(
				new TableAlias(Task.getDefinition().getTableschema(),LinkobjectQueryHelper.LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS),
				selectedchoices.toArray(new ChoiceValue[0]),
				Task.getDefinition());
		
		QueryCondition usercondition = HasidQueryHelper.getIdQueryCondition(
				new TableAlias(Appuser.getDefinition().getTableschema(),LinkobjectQueryHelper.LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS),
				userid.getId(), Appuser.getDefinition());
		
	
		
		
		AndQueryCondition fullfilter = null;
		if (objectcondition!=null) {
		fullfilter = new AndQueryCondition(statecondition,usercondition,objectcondition);
		} else {
			fullfilter = new AndQueryCondition(statecondition,usercondition);
		}
		ThreeDataObjects<Task,Taskuser,Appuser>[] queryresult = LinkobjectQueryHelper.get().getlinksandbothobjects(
				QueryFilter.get(fullfilter), 
				TaskuserDefinition.getTaskuserDefinition(),
				TaskDefinition.getTaskDefinition(),
				AppuserDefinition.getAppuserDefinition(), 
				TaskuserDefinition.getTaskuserDefinition().getLinkobjectDefinition());
		
		ArrayList<Task> taskresult = new ArrayList<Task>();
		for(int i=0;i<queryresult.length;i++) {
			taskresult.add(queryresult[i].getObjectOne());
		}
		return new ActionOutputData(taskresult.toArray(new Task[0]));	
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput)  {
		throw new RuntimeException("choose page not implemented for this action");
	}

}
