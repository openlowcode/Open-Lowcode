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

import java.util.ArrayList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.ComputeddecimalDefinition;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.tools.misc.NamedList;

/**
 * A trigger where the calculation of one field (on an origin object) triggers
 * the calculation of another field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> origin object of the trigger
 * @param <F> target object of the trigger
 */
public class CalculatedFieldTrigger<E extends DataObject<E>, F extends DataObject<F>> extends DataUpdateTrigger<E> {
	private ComputeddecimalDefinition<F> computeddecimal;
	private PathToCalculatedField<E, ?, F> path;

	/**
	 * A calculated field trigger
	 * 
	 * @param computeddecimal the target field for this trigger
	 * @param path            the path to go from object E to object F
	 */
	public CalculatedFieldTrigger(ComputeddecimalDefinition<F> computeddecimal, PathToCalculatedField<E, ?, F> path) {
		super(computeddecimal.getTriggerName());
		this.computeddecimal = computeddecimal;
		this.path = path;
	}

	/**
	 * @return the path from object E to object F
	 */
	public PathToCalculatedField<E, ?, F> getPathToCalculatedField() {
		return path;
	}

	@SuppressWarnings("unchecked")
	public NamedList<TriggerToExecute<E>> compute(E contextobject, boolean forcelocalupdate) {
		ArrayList<F> targetobjects = path.navigatetosourceobject(contextobject);
		NamedList<TriggerToExecute<E>> targetlocaltriggers = new NamedList<TriggerToExecute<E>>();
		for (int i = 0; i < targetobjects.size(); i++) {
			F targetobject = targetobjects.get(i);
			NamedList<DataUpdateTrigger<F>> triggers = computeddecimal.getFormula().compute(targetobject);
			if (!isLocal() || (isLocal() && forcelocalupdate)) {
				OLcServer.getServer().setObjectInTriggerUpdateBuffer((UniqueidentifiedInterface<F>) (targetobject));
				// ((UniqueidentifiedInterface<F>)(targetobject)).update();
				for (int j = 0; j < triggers.getSize(); j++) {
					OLcServer.getServer().addTriggerToList(new TriggerToExecute<F>(triggers.get(j), targetobject));
				}
			} else {
				for (int j = 0; j < triggers.getSize(); j++) {

					targetlocaltriggers
							.add(new TriggerToExecute<E>((DataUpdateTrigger<E>) (triggers.get(j)), contextobject));
				}
			}
		}
		return targetlocaltriggers;

	}

	@Override
	public boolean isLocal() {
		return path.local();
	}

	@Override
	public boolean isCustomTrigger() {

		return false;
	}
}
