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
 * This constraint only allows one link from the left object. This is
 * recommended to be used where there can be zero or one link, whereas
 * LinkedToParent property imposes there is always one parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstraintOnLinkMaxOneFromLeft
		extends
		PropertyBusinessRule<LinkObject<?, ?>> {

	private LinkObject<?, ?> parentproperty;
	private boolean replaceifmorethanone;

	/**
	 * creates a constraint allowing only one link from left
	 * 
	 * @param parentproperty       link object property
	 * @param replaceifmorethanone if true, if an old link existed, it will be
	 *                             removed, if false, if a link already exists, an
	 *                             error is thrown
	 */
	public ConstraintOnLinkMaxOneFromLeft(LinkObject<?, ?> parentproperty, boolean replaceifmorethanone) {
		super("MAXONELINKFROMLEFT", false);
		this.parentproperty = parentproperty;
		this.replaceifmorethanone = replaceifmorethanone;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		sg.wl("		linkobject.setMaxOneLinkFromLeft(" + this.replaceifmorethanone + ");");

	}

	@Override
	public String[] getImportstatements() {
		return new String[0];
	}

	/**
	 * @return get parent property
	 */
	public LinkObject<?, ?> getParentproperty() {
		return parentproperty;
	}

	/**
	 * @return if true, if an old link existed, it will be removed, if false, if a
	 *         link already exists, an error is thrown
	 */
	public boolean isReplaceifmorethanone() {
		return replaceifmorethanone;
	}

}
