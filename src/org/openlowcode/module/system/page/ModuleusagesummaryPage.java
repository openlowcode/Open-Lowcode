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

import org.openlowcode.module.system.action.SpecificmoduleusagesummaryAction;
import org.openlowcode.module.system.data.Basicdiagramrecord;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.ReportingfrequencyChoiceDefinition;
import org.openlowcode.module.system.page.generated.AbsModuleusagesummaryPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SAreaChart;
import org.openlowcode.server.graphic.widget.SChoiceTextField;
import org.openlowcode.server.graphic.widget.SCollapsibleBand;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SIntegerField;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * this page shows the summary of recent module usage in a graph
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ModuleusagesummaryPage
		extends
		AbsModuleusagesummaryPage {

	/**
	 * Creates a module usage summary page
	 * 
	 * @param stattable    the basic records to show
	 * @param excludeadmin true if admin is excluded from the stats
	 * @param frequency    frequency of reporting (day, week, month)
	 * @param history      number of days of history
	 */
	public ModuleusagesummaryPage(
			Basicdiagramrecord[] stattable,
			ChoiceValue<BooleanChoiceDefinition> excludeadmin,
			ChoiceValue<ReportingfrequencyChoiceDefinition> frequency,
			Integer history) {
		super(stattable, excludeadmin, frequency, history);

	}

	@Override
	public String generateTitle(
			Basicdiagramrecord[] stattable,
			ChoiceValue<BooleanChoiceDefinition> excludeadmin,
			ChoiceValue<ReportingfrequencyChoiceDefinition> frequency,
			Integer history) {
		return "Module Usage Summary";

	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Module Usage Summary", SPageText.TYPE_TITLE, this));
		SComponentBand detailband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SCollapsibleBand details = new SCollapsibleBand(this, detailband, "Detailed Settings", false);

		SChoiceTextField<
				BooleanChoiceDefinition> excludeadmin = new SChoiceTextField<BooleanChoiceDefinition>("Excludes Admin",
						"EXLCADMIN", "", BooleanChoiceDefinition.get(), this.getExcludeadmin(), this, false, null);

		detailband.addElement(excludeadmin);
		SChoiceTextField<ReportingfrequencyChoiceDefinition> reportfrequency = new SChoiceTextField<
				ReportingfrequencyChoiceDefinition>("Report Frequency", "REPORTFREQ", "",
						ReportingfrequencyChoiceDefinition.get(), this.getFrequency(), this, false, null);

		detailband.addElement(reportfrequency);
		SIntegerField history = new SIntegerField("History", "HISTORY", "", this.getHistory(), true, this, false, false,
				false, null);
		detailband.addElement(history);
		SpecificmoduleusagesummaryAction.ActionRef reportwithparameter = SpecificmoduleusagesummaryAction.get()
				.getActionRef();
		reportwithparameter.setExcludeadmin(excludeadmin.getChoiceInput());
		reportwithparameter.setFrequency(reportfrequency.getChoiceInput());
		reportwithparameter.setHistory(history.getIntegerInput());
		detailband.addElement(new SActionButton("Relaunch Report", reportwithparameter, this));

		mainband.addElement(details);
		SAreaChart<Basicdiagramrecord> usagechart = new SAreaChart<Basicdiagramrecord>("CHART", this.getStattable(),
				Basicdiagramrecord.getDefinition(), Basicdiagramrecord.getRecorddateFieldMarker(),
				Basicdiagramrecord.getCategoryFieldMarker(), Basicdiagramrecord.getValueFieldMarker(), this);
		mainband.addElement(usagechart);
		return mainband;
	}

}
