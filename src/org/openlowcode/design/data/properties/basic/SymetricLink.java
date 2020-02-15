/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * This business rule specifies that an auto-link is symetric, meaning the link
 * has no direction.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SymetricLink
		extends
		PropertyBusinessRule<AutolinkObject<? super DataObjectDefinition>> {

	/**
	 * creates a property business rule symetric link to be added to the
	 * AutolinkObject property
	 */
	public SymetricLink() {
		super("SYMETRICLINK", false);

	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		sg.wl("		autolinkobject.setSymetricLink();");

	}

	@Override
	public String[] getImportstatements() {
		return new String[0];
	}

}
