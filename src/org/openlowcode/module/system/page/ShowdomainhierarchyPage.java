/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.page.generated.AbsShowdomainhierarchyPage;
import org.openlowcode.server.data.NodeTree;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectTreeArray;

/**
 * A page showing a node tree of domain objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowdomainhierarchyPage
		extends
		AbsShowdomainhierarchyPage {

	/**
	 * creates the page showing a node tree of domains
	 * 
	 * @param domaintree the tree of domains
	 */
	public ShowdomainhierarchyPage(NodeTree<Domain> domaintree) {
		super(domaintree);
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainnode = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainnode.addElement(
				new SObjectTreeArray<Domain>("PAGEDOMAINTREE", this.getDomaintree(), Domain.getDefinition(), this));
		return mainnode;
	}

	@Override
	public String generateTitle(NodeTree<Domain> domaintree) {
		return "Show domain hierarchy for Domain " + domaintree.getRoot().getNr() + " "
				+ domaintree.getRoot().getName();
	}

}
