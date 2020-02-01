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

import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * This property is created on the parent when the children have the business
 * rule DeleteChildrenWhenParentDeleted.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DeleteChildrenWhenParentDeletedFinal
		extends
		PropertyBusinessRule<LinkedFromChildren> {

	private LinkedFromChildren parentlinkedfromchildren;

	/**
	 * creates the business rule to delete children when parent is deleted. This
	 * business rule is added on the parent
	 * 
	 * @param parentlinkedfromchildren linked from children property on the parent
	 */
	public DeleteChildrenWhenParentDeletedFinal(LinkedFromChildren parentlinkedfromchildren) {
		super("DELETECHILDREN", false);
		this.parentlinkedfromchildren = parentlinkedfromchildren;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String instancename = this.parentlinkedfromchildren.getInstancename().toLowerCase();
		sg.wl("		linkedfromchildrenfor" + instancename + ".setDeleteChildren();");
	}

	@Override
	public String[] getImportstatements() {
		return new String[] {};
	}

}
