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
 * This constraint will prevent from having several links for the same left and
 * right object. This uses the object id, and does not prevent from having a
 * link with several different versions of the left or right object;
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class ConstraintOnLinkUniqueForLeftAndRight
		extends
		PropertyBusinessRule<LinkObject<?, ?>> {
	private LinkObject<?, ?> parentproperty;
	private boolean replaceifmorethanone;

	/**
	 * creates a constraint on link that it is unique for left and right
	 * 
	 * @param parentproperty       the link object on which to apply the constraint
	 * @param replaceifmorethanone if false, an error will be thrown when trying to
	 *                             create a second link. if true, the old link will
	 *                             be removed
	 */
	public ConstraintOnLinkUniqueForLeftAndRight(LinkObject<?, ?> parentproperty, boolean replaceifmorethanone) {
		super("UNIQUEFORLEFTANDRIGHT", false);
		this.parentproperty = parentproperty;
		this.replaceifmorethanone = replaceifmorethanone;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		sg.wl("		linkobject.setUniqueForLeftAndRight(" + this.replaceifmorethanone + ");");

	}

	@Override
	public String[] getImportstatements() {
		return new String[0];
	}

	/**
	 * @return get the parent link object property
	 */
	public LinkObject<?, ?> getParentproperty() {
		return parentproperty;
	}

	/**
	 * @return if false, an error will be thrown when trying to create a second
	 *         link. if true, the old link will be removed
	 */
	public boolean isReplaceifmorethanone() {
		return replaceifmorethanone;
	}

}
