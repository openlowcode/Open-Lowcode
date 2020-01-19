/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import java.util.ArrayList;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Workflow;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LifecycleInterface;
import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * a selection step allowing users to select a person for a role of the workflow
 * inside a constant authority
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the worfklow is about
 * @param <F> transition choice used for the lifecycle of the object
 */
public class SimpleAuthorityUserSelectionForTask<
		E extends DataObject<E> & LifecycleInterface<E, F>,
		F extends TransitionFieldChoiceDefinition<F>>
		extends
		UserSelectionForTask<E, F> {
	private String authoritynumber;

	/**
	 * allows users to select one user inside a constant authority
	 * 
	 * @param authoritynumber constant authority to choose users inside
	 */
	public SimpleAuthorityUserSelectionForTask(String authoritynumber) {
		this.authoritynumber = authoritynumber;
	}

	@Override
	public ArrayList<DataObjectId<Appuser>> select(DataObjectId<Workflow> workflowid, E object) {
		Appuser[] authorityusers = ServerSecurityBuffer.getUniqueInstance()
				.getUsersForAuthority(Authority.getobjectbynumber(authoritynumber)[0].getId());
		ArrayList<DataObjectId<Appuser>> appuserlist = new ArrayList<DataObjectId<Appuser>>();
		if (authorityusers != null)
			for (int i = 0; i < authorityusers.length; i++) {
				appuserlist.add(authorityusers[i].getId());
			}
		return appuserlist;
	}

}
