/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * this user selector will send a potential task to all users part of an
 * authority
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AuthoritySelector
		extends
		TaskUserSelector {
	private TotalAuthority authority;

	/**
	 * creates an authority selection for the provided authority
	 * 
	 * @param authority tasks will be sent for users that have this authority
	 */
	public AuthoritySelector(TotalAuthority authority) {
		this.authority = authority;
	}

	@Override
	public DataObjectDefinition getObjectForQuery() {

		return null;
	}

	@Override
	public void writeSelectorDeclaration(
			SourceGenerator sg,
			Module module,
			String parentobjectclass,
			String lifecycleclass,
			String taskcodelowercase) throws IOException {

		sg.wl("		SimpleAuthorityUserSelectionForTask<" + parentobjectclass + "," + lifecycleclass
				+ "ChoiceDefinition> " + taskcodelowercase + "userselection = ");
		sg.wl("				new SimpleAuthorityUserSelectionForTask<" + parentobjectclass + "," + lifecycleclass
				+ "ChoiceDefinition>(\"" + authority.getName().toUpperCase() + "\");");

	}
}
