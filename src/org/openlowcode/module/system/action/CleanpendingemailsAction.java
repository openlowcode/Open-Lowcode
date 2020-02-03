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

import java.util.ArrayList;
import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsCleanpendingemailsAction;
import org.openlowcode.module.system.action.generated.AtgLaunchsearchemailAction;
import org.openlowcode.module.system.data.Email;
import org.openlowcode.module.system.data.choice.CleanemailprocessChoiceDefinition;
import org.openlowcode.module.system.data.choice.EmailstatusChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.LifecycleQueryHelper;
import org.openlowcode.server.data.properties.StoredobjectQueryHelper;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

import org.openlowcode.server.data.storage.QueryFilter;

/**
 * an action to remove all pending e-mails from the queue, can be triggered
 * after technical issue on the e-mail sending component of the server. It may
 * be useful to clean e-mails before replugging the e-mail sending on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CleanpendingemailsAction
		extends
		AbsCleanpendingemailsAction {

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public CleanpendingemailsAction(SModule parent) {
		super(parent);
	}

	@Override
	public SPage choosePage() {

		return AtgLaunchsearchemailAction.get().executeAndShowPage();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void executeActionLogic(
			Email[] selectedpendingemails,
			ChoiceValue<CleanemailprocessChoiceDefinition> newstatus,
			Function<TableAlias, QueryFilter> datafilter) {
		boolean data = false;
		if (selectedpendingemails != null)
			if (selectedpendingemails.length > 0)
				data = true;
		Email[] emailstodelete = selectedpendingemails;
		if (!data) {
			emailstodelete = Email
					.getallactive(QueryFilter.get(LifecycleQueryHelper.get().getStateSelectionQueryCondition(
							Email.getDefinition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive),
							new ChoiceValue[] { EmailstatusChoiceDefinition.getChoiceReadytosend() },
							Email.getDefinition())));
		}
		if (emailstodelete != null)
			if (emailstodelete.length > 0) {
				ArrayList<Email> emailtoprocess = new ArrayList<Email>();
				ArrayList<ChoiceValue<EmailstatusChoiceDefinition>> newstatusarray = new ArrayList<
						ChoiceValue<EmailstatusChoiceDefinition>>();
				ChoiceValue<CleanemailprocessChoiceDefinition> realnewstatus = newstatus;
				if (realnewstatus == null)
					realnewstatus = CleanemailprocessChoiceDefinition.get().DISCARD;
				for (int i = 0; i < emailstodelete.length; i++) {
					Email thisemail = emailstodelete[i];
					if (newstatus.equals(CleanemailprocessChoiceDefinition.get().DISCARD)) {
						if (thisemail.getstateforchange().equals(EmailstatusChoiceDefinition.getChoiceReadytosend())) {
							emailtoprocess.add(thisemail);
							newstatusarray.add(EmailstatusChoiceDefinition.getChoiceDiscarded());
						}
						if (thisemail.getstateforchange().equals(EmailstatusChoiceDefinition.getChoiceError())) {
							emailtoprocess.add(thisemail);
							newstatusarray.add(EmailstatusChoiceDefinition.getChoiceDiscarded());
						}
						if (thisemail.getstateforchange().equals(EmailstatusChoiceDefinition.getChoiceSending())) {
							emailtoprocess.add(thisemail);
							newstatusarray.add(EmailstatusChoiceDefinition.getChoiceDiscarded());
						}
					}
					if (newstatus.equals(CleanemailprocessChoiceDefinition.get().RELAUNCH)) {
						if (thisemail.getstateforchange().equals(EmailstatusChoiceDefinition.getChoiceError())) {
							emailtoprocess.add(thisemail);
							newstatusarray.add(EmailstatusChoiceDefinition.getChoiceReadytosend());
						}
						if (thisemail.getstateforchange().equals(EmailstatusChoiceDefinition.getChoiceSending())) {
							emailtoprocess.add(thisemail);
							newstatusarray.add(EmailstatusChoiceDefinition.getChoiceReadytosend());
						}
					}
				}

				Email.changestate(emailtoprocess.toArray(new Email[0]), newstatusarray.toArray(new ChoiceValue[0]));
			}

	}

}
