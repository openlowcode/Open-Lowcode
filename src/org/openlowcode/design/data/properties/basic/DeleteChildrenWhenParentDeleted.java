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
 * a business rule that deletes children when parent is deleted. This is
 * especially recommended for 'appendix' objects that do not have their own
 * independent life.<br>
 * <i>objects are actually deleted by the property
 * DeleteChildrenWhenParentDeletedFinal that is created on the parent</i>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DeleteChildrenWhenParentDeleted
		extends
		PropertyBusinessRule<LinkedToParent<? super DataObjectDefinition>> {

	/**
	 * creates the property to delete children when parent is deleted
	 */
	public DeleteChildrenWhenParentDeleted() {
		super("DELETECHILDRENWHENPARENTDELETED", false);

	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		// this does nothing at it is the actual property on linkedfromchildren that
		// will perform the action.
	}

	@Override
	public String[] getImportstatements() {
		return new String[] {};
	}

}
