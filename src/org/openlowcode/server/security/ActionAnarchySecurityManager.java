/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.security;

import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

import org.openlowcode.server.data.storage.QueryFilter;

import org.openlowcode.server.data.storage.TableAlias;

/**
 * The anarchy security manager gives access to any connected user to the
 * action. As rights given by security manager are permissive, it is not
 * possible to restrain access to an action where anarchy has been declared.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ActionAnarchySecurityManager extends ActionSecurityManager {
	private static Logger logger = Logger.getLogger(ActionAnarchySecurityManager.class.getName());

	@Override
	public boolean queryObjectData() {
		return false;
	}

	@Override
	public boolean isAuthorizedForCurrentUser(String context, SActionData input, SecurityBuffer buffer) {
		logger.info("authorized for " + context + " through total security manager");
		return true;
	}

	@Override
	public boolean filterObjectData() {

		return false;
	}

	@Override
	public Function<TableAlias, QueryFilter> getOutputFilterCondition() {
		return null;
	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer) {
		if (dataarray != null)
			for (int i = 0; i < dataarray.length; i++)
				dataarray[i].setUnfrozen();

	}

	@Override
	public String toString() {

		return "GalliumActionAnarchySecurityManager";
	}

}
