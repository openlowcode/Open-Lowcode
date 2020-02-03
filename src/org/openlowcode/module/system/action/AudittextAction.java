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

import org.openlowcode.module.system.action.generated.AbsAudittextAction;
import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.module.system.page.ShowaudittextPage;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

import org.openlowcode.server.data.storage.QueryFilter;

/**
 * This action allows to perform a round-trip from the client to the database,
 * through the server. This helps to detect encoding issues
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AudittextAction
		extends
		AbsAudittextAction {
	private static Logger logger = Logger.getLogger(AudittextAction.class.getName());

	/**
	 * Creates the action 
	 * 
	 * @param parent parent module
	 */
	public AudittextAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			String textfromclientbigfield,
			String textfromclientsmallfield,
			Function<TableAlias, QueryFilter> datafilter) {
		String textfromclient = textfromclientbigfield + " " + textfromclientsmallfield;
		logger.severe("Text from client = " + textfromclient);
		String textauditattrname = "SYSTEM.TEXTAUDIT";
		Systemattribute textauditattribute = Systemattribute.getobjectbynumber(textauditattrname)[0];
		if (textauditattribute == null) {
			textauditattribute = new Systemattribute();
			textauditattribute.setobjectnumber(textauditattrname);
			textauditattribute.insert();
		}
		textauditattribute.setValue(textfromclient);
		textauditattribute.update();

		Systemattribute textauditattributeafterstorage = Systemattribute.getobjectbynumber(textauditattrname)[0];
		String textafterstorage = textauditattributeafterstorage.getValue();

		return new ActionOutputData(textafterstorage, textfromclient);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ShowaudittextPage(logicoutput.getStoredtext(), logicoutput.getUnstoredtext());
	}

}
