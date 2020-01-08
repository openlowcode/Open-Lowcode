/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;

/**
 * A utility class checking if a number already exists. This is part of unicity
 * controls of the Numbered property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent object type
 */
public class CheckExistingNumber<E extends DataObject<E> & UniqueidentifiedInterface<E>> {
	private static Logger logger = Logger.getLogger(CheckExistingNumber.class.getName());
	private NumberedDefinition<E> numbereddefinition;

	/**
	 * creates the utility class
	 * 
	 * @param numbereddefinition definition of the numbered property
	 */
	public CheckExistingNumber(NumberedDefinition<E> numbereddefinition) {
		this.numbereddefinition = numbereddefinition;
	}

	/**
	 * checks if the number already exists
	 * 
	 * @param nr     number to check
	 * @param object object to be created (not used, may be suppressed)
	 * @return true if the number already exists
	 */
	@SuppressWarnings("unchecked")
	public boolean exists(String nr, E object) {
		E existingobject[] = NumberedQueryHelper.get().getobjectbynumber(nr, numbereddefinition.getParentObject(),
				numbereddefinition);
		if (existingobject.length > 0) {
			for (int i = 0; i < existingobject.length; i++) {
				E thisobject = existingobject[i];
				if (thisobject instanceof VersionedInterface) {
					VersionedInterface<E> thisobjectversioned = (VersionedInterface<E>) thisobject;
					if (thisobjectversioned.getLastversion().equals("Y")) {
						logger.warning(" --> Simple number check: versioned object " + nr + " - id "
								+ thisobject.getId() + " - version " + thisobjectversioned.getVersion());
						return true;
					}
				} else {
					logger.warning(" --> Simple number check: versioned object " + nr + " - id " + thisobject.getId());

					return true;
				}
			}

		}
		return false;
	}
}
