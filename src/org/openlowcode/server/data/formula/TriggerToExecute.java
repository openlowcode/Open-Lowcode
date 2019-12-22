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
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A wrapping class representing a trigger to execute on the server during
 * formulas. It stores the DataUpdateTrigger (formula) and the object on which
 * to execute it
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object this calculation applies to
 */
public class TriggerToExecute<E extends DataObject<E>> extends Named {
	private static Logger logger = Logger.getLogger(TriggerToExecute.class.getName());
	private DataUpdateTrigger<E> trigger;
	private E contextobject;

	public void replacetrigger(E newcontextobject) {
		logger.info("Replacing trigger " + this.getClass().toString() + " for objectid = " + this);
		this.contextobject = newcontextobject;
	}

	/**
	 * @return true if the trigger is custom (user-implemented)
	 */
	public boolean isCustomTrigger() {
		return trigger.isCustomTrigger();
	}

	/**
	 * @param trigger       the data update trigger
	 * @param contextobject object to execute the trigger on
	 */
	@SuppressWarnings("unchecked")
	public TriggerToExecute(DataUpdateTrigger<E> trigger, E contextobject) {
		super(trigger.getName());
		this.trigger = trigger;
		this.contextobject = contextobject;
		DataObjectId<E> contextobjectid = null;
		if (contextobject instanceof UniqueidentifiedInterface)
			contextobjectid = ((UniqueidentifiedInterface<E>) contextobject).getId();
		logger.info(" -- Create Trigger To Execute " + this + " -- objectid = " + contextobjectid);
	}

	/**
	 * @return execute the trigger and returns the triggers raised by the
	 *         calculation
	 */
	public NamedList<TriggerToExecute<E>> execute() {
		return execute(false);
	}

	/**
	 * @param forcelocalupdate true to trigger calculations on other objects even if
	 *                         trigger is local
	 * @return the triggers raised by this calculation
	 */
	public NamedList<TriggerToExecute<E>> execute(boolean forcelocalupdate) {
		logger.info(" - executing trigger to execute on object " + contextobject.getName());
		NamedList<TriggerToExecute<E>> newtriggers = trigger.compute(contextobject, forcelocalupdate);
		return newtriggers;

	}

	/**
	 * @return the DataUpdateTrigger (calculation formula) this object wraps around
	 */
	public DataUpdateTrigger<E> getTrigger() {
		return trigger;
	}

	/**
	 * @return the object this trigger wraps around
	 */
	public E getContextobject() {
		return contextobject;
	}

}
