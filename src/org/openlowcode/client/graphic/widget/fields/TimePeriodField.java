/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.fields;

import java.util.Calendar;
import java.util.logging.Logger;

import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.data.TimePeriod.PeriodType;
import org.openlowcode.tools.data.TimePeriod.Quarter;
import org.openlowcode.tools.data.TimePeriod.YearQualifier;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * The Time Period field is able to display and edit a timeperiod field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriodField extends HBox {
	private static Logger logger = Logger.getLogger(TimePeriodField.class.getName());
	private TimePeriod.PeriodType restrictionontype;
	private TimePeriod value;
	private TextField yearfield;
	private BigDecimalFormatValidator formatvalidator;
	private ChoiceBox<YearQualifier> yearqualifier;
	private boolean hasyearqualifier;

	public TimePeriodField(TimePeriod.PeriodType restrictionontype, TimePeriod value) {
		this(restrictionontype);
		// year qualifier exists if type is not STRICT_YEAR
		this.hasyearqualifier = (TimePeriod.PeriodType.STRICT_YEAR.equals(restrictionontype) ? false : true);

		logger.finest("Creating TimePeriodField with type = " + restrictionontype + " value = " + value
				+ ", has year qualifier = " + this.hasyearqualifier);
		this.value = value;

		// ------------------------ quarter or month or before or after or full year and
		// potentially ongoing) ------------
		if (hasyearqualifier) {
			this.yearqualifier = new ChoiceBox<YearQualifier>();
			ObservableList<YearQualifier> values = FXCollections.observableArrayList();
			values.add(new YearQualifier(true));
			if (this.restrictionontype == null || TimePeriod.PeriodType.YEAR.equals(this.restrictionontype)) {
				values.add(new YearQualifier(PeriodType.YEAR, false));

				values.add(new YearQualifier(PeriodType.YEAR, true));
			}

			if (this.restrictionontype == null || TimePeriod.PeriodType.QUARTER.equals(this.restrictionontype)) {
				values.add(new YearQualifier(Quarter.Q1, false));
				values.add(new YearQualifier(Quarter.Q1, true));
				values.add(new YearQualifier(Quarter.Q2, false));
				values.add(new YearQualifier(Quarter.Q2, true));
				values.add(new YearQualifier(Quarter.Q3, false));
				values.add(new YearQualifier(Quarter.Q3, true));
				values.add(new YearQualifier(Quarter.Q4, false));
				values.add(new YearQualifier(Quarter.Q4, true));
			}

			if (this.restrictionontype == null || TimePeriod.PeriodType.MONTH.equals(this.restrictionontype))
				for (int i = 1; i <= 12; i++) {
					values.add(new YearQualifier(i, false));
					values.add(new YearQualifier(i, true));

				}

			values.add(new YearQualifier(false));

			yearqualifier = new ChoiceBox<YearQualifier>(values);
			if (value != null)
				yearqualifier.setValue(value.getYearQualifier());
			yearqualifier.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<YearQualifier>() {

				@Override
				public void changed(ObservableValue<? extends YearQualifier> observable, YearQualifier oldvalue,
						YearQualifier newvalue) {
					if (newvalue.NeedsYear()) {
						yearfield.setEditable(true);
					} else {
						yearfield.setEditable(false);
						yearfield.setText("");
					}

				}

			});
			this.getChildren().add(yearqualifier);
		}
		// ------------------------ year field
		yearfield = new TextField();
		int year = (value != null ? value.getYear() : Calendar.getInstance().get(Calendar.YEAR));
		if (year >= 1970)
			yearfield.setText("" + year);
		this.formatvalidator = new BigDecimalFormatValidator(4, 0);
		yearfield.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldvalue, String newvalue) {
				String valueafterformatting = formatvalidator.valid(newvalue);
				if (valueafterformatting == null)
					yearfield.setText(oldvalue);
				if (valueafterformatting != null)
					yearfield.setText(valueafterformatting);

			}

		});
		this.getChildren().add(yearfield);
	}

	public TimePeriodField(TimePeriod.PeriodType restrictionontype) {
		super();
		this.restrictionontype = restrictionontype;
	}

	public void setTimePeriod(TimePeriod value) {
		this.value = value;
		if (this.hasyearqualifier)
			this.yearqualifier.setValue(value.getYearQualifier());
		this.yearfield.setText("" + value.getYear());
	}

	public TimePeriod getTimePeriod() {
		YearQualifier qualifier = (this.hasyearqualifier ? this.yearqualifier.getSelectionModel().getSelectedItem()
				: new TimePeriod.YearQualifier(PeriodType.YEAR, true));
		int year = 0;
		if (yearfield.getText() != null)
			if (yearfield.getText().trim().length() > 0)
				year = new Integer(yearfield.getText().replaceAll("\\s+", "")).intValue();
		this.value = new TimePeriod(qualifier, year);
		return this.value;
	}

}
