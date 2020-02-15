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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.action.generated.AbsSpecificmoduleusagesummaryAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Basicdiagramrecord;
import org.openlowcode.module.system.data.Moduleusage;
import org.openlowcode.module.system.data.ModuleusageDefinition;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.ReportingfrequencyChoiceDefinition;
import org.openlowcode.module.system.page.ModuleusagesummaryPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.StoredobjectQueryHelper;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorGreaterThan;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.server.tools.StandardUtil;

/**
 * generates a specific module usage summary according to defined settings
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SpecificmoduleusagesummaryAction
		extends
		AbsSpecificmoduleusagesummaryAction {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SpecificmoduleusagesummaryAction.class.getName());

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public SpecificmoduleusagesummaryAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(
			ChoiceValue<BooleanChoiceDefinition> excludeadmin,
			ChoiceValue<ReportingfrequencyChoiceDefinition> frequency,
			Integer history,
			Function<TableAlias, QueryFilter> datafilter) {
		return new ActionOutputData(generatedata(excludeadmin, frequency, history).toArray(new Basicdiagramrecord[0]),
				excludeadmin, frequency, history);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ModuleusagesummaryPage(logicoutput.getStattable(), logicoutput.getExcludeadmin_thru(),
				logicoutput.getFrequency_thru(), logicoutput.getHistory_thru());
	}

	/**
	 * generate a module usage summary
	 * 
	 * @param excludeadmin if true, exclude admin
	 * @param frequency    frequency (day, week, month...)
	 * @param history      history in days shown
	 * @return
	 */
	public static List<Basicdiagramrecord> generatedata(
			ChoiceValue<BooleanChoiceDefinition> excludeadmin,
			ChoiceValue<ReportingfrequencyChoiceDefinition> frequency,
			Integer history) {
		Calendar calendar = Calendar.getInstance();
		SimpleQueryCondition<Date> timelimit = null;
		if (history != null)
			if (history.intValue() > 0) {
				timelimit = new SimpleQueryCondition<Date>(
						ModuleusageDefinition.getModuleusageDefinition()
								.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive),
						ModuleusageDefinition.getModuleusageDefinition().getDayFieldSchema(),
						new QueryOperatorGreaterThan<Date>(),
						new Date(new Date().getTime() - 86400000l * history.intValue()));
			}
		Moduleusage[] allusages = Moduleusage.getallactive(QueryFilter.get(timelimit));
		ArrayList<Basicdiagramrecord> record = new ArrayList<Basicdiagramrecord>();
		HashMap<String, Basicdiagramrecord> recordbykey = new HashMap<String, Basicdiagramrecord>();

		for (int i = 0; i < allusages.length; i++) {
			Moduleusage currentusage = allusages[i];
			Date relevantdate = StandardUtil.getLastDateOfPeriod(currentusage.getDay(), frequency, calendar);
			String key = "" + relevantdate.getTime() + "/" + currentusage.getModule();
			boolean discard = false;
			if (excludeadmin != null)
				if (excludeadmin.equals(BooleanChoiceDefinition.get().YES)) {
					Appuser user = currentusage.getparentforsessionuser();
					if (user.getNr().endsWith("admin")) {

						discard = true;
					}
				}
			if (!discard) {
				Basicdiagramrecord relevantrecord = recordbykey.get(key);
				if (relevantrecord == null) {
					relevantrecord = new Basicdiagramrecord();
					record.add(relevantrecord);
					recordbykey.put(key, relevantrecord);
					relevantrecord.setRecorddate(relevantdate);
					relevantrecord.setCategory(currentusage.getModule());
					relevantrecord.setValue(new BigDecimal(0));

				}
				relevantrecord.setValue(relevantrecord.getValue().add(new BigDecimal(currentusage.getActionnr())));
			}
		}

		return record;
	}
}
