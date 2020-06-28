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

import org.openlowcode.module.system.action.generated.AbsConfirmotpAction;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.page.EnterotpPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

/**
 * Action to check the validity of the current user OTP
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConfirmotpAction
		extends
		AbsConfirmotpAction {

	/**
	 * create the confirm OTP action
	 * 
	 * @param parent
	 */
	public ConfirmotpAction(SModule parent) {
		super(parent);
	}

	@Override
	public ChoiceValue<BooleanChoiceDefinition> executeActionLogic(String otp, Function<TableAlias, QueryFilter> datafilter) {
	boolean valid = OLcServer.getServer().getSecuritymanager().checkandregisterOTP(otp);
		if (valid) return BooleanChoiceDefinition.get().YES;
		return BooleanChoiceDefinition.get().NO;
	}

	
	
	@Override
	public SPage choosePage(ChoiceValue<BooleanChoiceDefinition> valid) {
		if (valid.equals(BooleanChoiceDefinition.get().NO)) return new EnterotpPage();
		return OLcServer.getServer().getMainmodule().getDefaultPage();
	}

	

}
