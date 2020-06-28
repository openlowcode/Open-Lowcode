/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/


package org.openlowcode.module.system.action;

import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsEnterotpAction;
import org.openlowcode.module.system.page.EnterotpPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;
/**
 * Check if the user has an OTP, and if not, launches the page to enter an OTP
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class EnterotpAction
		extends
		AbsEnterotpAction {

	/**
	 * Create the "Enter OTP action"
	 * 
	 * @param parent parent module
	 */
	public EnterotpAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		if (OLcServer.getServer().getOTPSecurityManager()==null) throw new RuntimeException("OTP Security is not activated on this server.");
		return new ActionOutputData();
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new EnterotpPage();
	}

}
