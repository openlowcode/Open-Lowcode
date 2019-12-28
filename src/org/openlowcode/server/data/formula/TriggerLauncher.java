/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.formula;

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;

import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.tools.misc.NamedList;

/**
 * a launcher for all triggers on an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the trigger is on
 */
public class TriggerLauncher<E extends DataObject<E>> {
	private static Logger logger = Logger.getLogger(TriggerLauncher.class.getName());
	private NamedList<DataUpdateTrigger<E>> localtriggerlist;
	private NamedList<DataUpdateTrigger<E>> remotetriggerlist;

	/**
	 * creates a new TriggerLauncher
	 * 
	 * @param triggerlist the list of relevant triggers
	 */
	public TriggerLauncher(NamedList<DataUpdateTrigger<E>> triggerlist) {
		localtriggerlist = new NamedList<DataUpdateTrigger<E>>();
		remotetriggerlist = new NamedList<DataUpdateTrigger<E>>();
		if (triggerlist != null)
			for (int i = 0; i < triggerlist.getSize(); i++) {
				DataUpdateTrigger<E> thistrigger = triggerlist.get(i);
				if (thistrigger.isLocal()) {
					localtriggerlist.add(thistrigger);
				} else {
					remotetriggerlist.add(thistrigger);
				}
			}

	}

	/**
	 * Executes the triggers on the object
	 * 
	 * @param originaltriggerlist the original triggers on the object
	 * @param contextobject       context object for trigger execution
	 * @param breaker             trigger to avoid server crash if recursive
	 *                            structure is encountered (1024 navigations are
	 *                            allowed)
	 */
	public void executeTriggerList(NamedList<DataUpdateTrigger<E>> originaltriggerlist, E contextobject, int breaker) {
		if (breaker > 1024)
			throw new RuntimeException(
					"recursive formula structure is too deep for formula calculation, current for object "
							+ contextobject.dropToString());
		for (int i = 0; i < originaltriggerlist.getSize(); i++) {
			DataUpdateTrigger<E> trigger = originaltriggerlist.get(i);

			if (trigger.isLocal()) {
				logger.info("Executing calculation for local trigger " + trigger.getName());
				NamedList<TriggerToExecute<E>> newtriggers = trigger.compute(contextobject);
				NamedList<DataUpdateTrigger<E>> newlocaltriggers = new NamedList<DataUpdateTrigger<E>>();
				int newremotetrigger = 0;
				if (newtriggers.getSize() > 0)
					for (int j = 0; j < newtriggers.getSize(); j++) {
						DataUpdateTrigger<E> thisnewtrigger = newtriggers.get(j).getTrigger();
						if (thisnewtrigger.isLocal()) {
							newlocaltriggers.add(thisnewtrigger);
						} else {
							remotetriggerlist.addIfNew(thisnewtrigger);
							newremotetrigger++;
						}

					}
				logger.info(
						"After execution of local trigger " + trigger.getName() + ", got a list of new triggers, local "
								+ newlocaltriggers.getSize() + ", new remote triggers = " + newremotetrigger);
				executeTriggerList(newlocaltriggers, contextobject, breaker + 1);

			}

		}
	}

	/**
	 * Executes trigger list for the context object
 	 * @param contextobject context object
	 */
	public void executeTriggerList(E contextobject) {
		logger.info("Starts execution of trigger list - step 0 - local triggers with list length = "
				+ localtriggerlist.getSize() + " for object of class " + contextobject.getName());
		executeTriggerList(this.localtriggerlist, contextobject, 0);
		logger.info("Starts execution of trigger list - step 1 - remote triggers with list length = "
				+ remotetriggerlist.getSize() + " for object of class " + contextobject.getName());

		for (int i = 0; i < this.remotetriggerlist.getSize(); i++) {
			DataUpdateTrigger<E> thistrigger = remotetriggerlist.get(i);
			logger.info("Stores remote Trigger - step 1." + i + " - processing trigger = " + thistrigger.getName()
					+ " for origin object " + contextobject.getName());
			OLcServer.getServer().addTriggerToList(new TriggerToExecute<E>(this.remotetriggerlist.get(i), contextobject));

		}

	}

}
