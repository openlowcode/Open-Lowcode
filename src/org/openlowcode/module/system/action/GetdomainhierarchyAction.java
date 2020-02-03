/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsGetdomainhierarchyAction;
import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.page.ShowdomainhierarchyPage;
import org.openlowcode.server.data.NodeTree;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * an action to show the hierarchy of domains. This was developped as a way to
 * test the node tree widget
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GetdomainhierarchyAction
		extends
		AbsGetdomainhierarchyAction {
	private static Logger logger = Logger.getLogger("");

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public GetdomainhierarchyAction(SModule parent) {
		super(parent);

	}

	private void fillTreeforParent(NodeTree<Domain> nodetree, Domain current, Domain[] alldomains, int circuitbreaker) {
		if (circuitbreaker > 9999)
			throw new RuntimeException("recursive circuitbreaker on currentdomain = " + current.getId());
		DataObjectId<Domain> parentid = current.getId();
		for (int i = 0; i < alldomains.length; i++) {
			Domain scanneddomain = alldomains[i];
			if (scanneddomain.getLinkedtoparentforhierarchyid().equals(parentid)) {
				boolean newnode = nodetree.addChild(current, scanneddomain);
				if (newnode)
					fillTreeforParent(nodetree, scanneddomain, alldomains, circuitbreaker + 1);
			}
		}
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		Domain[] alldomains = Domain.getallactive(null);
		logger.info("found domains, number = " + alldomains.length);
		Domain root = null;
		int i = 0;
		while ((i < alldomains.length) && (root == null)) {
			Domain scanneddomain = alldomains[i];
			if (scanneddomain.getLinkedtoparentforhierarchyid().getId().equals("")) {
				logger.info("found root domain for index " + i + ", name = " + scanneddomain.getNr());
				root = scanneddomain;
			} else {
				logger.info("this domain is not root, index =" + i + ", name = " + scanneddomain.getNr() + ", parent = "
						+ scanneddomain.getLinkedtoparentforhierarchyid());
			}
			i++;
		}
		NodeTree<Domain> answertree = new NodeTree<Domain>(root);
		fillTreeforParent(answertree, root, alldomains, 0);
		return new ActionOutputData(answertree);
	}

	@Override
	public SPage choosePage(ActionOutputData outputdata) {

		return new ShowdomainhierarchyPage(outputdata.getDomaintree());
	}

}
