/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.client.graphic.widget.tools;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.richtext.RichTextArea;

import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;

/**
 * A date field used across several widgets
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DateField {
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static SimpleDateFormat fulldateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS z");

	/**
	 * An utility to format a date in a nice format in English
	 * 
	 * @param date     date to show
	 * @param showdate if true, show date and time, if false, show only data
	 * @return a string with the date formatted
	 */
	public static String formatNiceDate(Date date, boolean showdate) {
		String stringdate = "";
		if (date != null) {
			String openbracket = "";
			String closebracket = "";
			if (showdate) {
				stringdate += dateformat.format(date);
				openbracket = "(";
				closebracket = ")";
			}
			long days = (new Date().getTime() - date.getTime()) / (24 * 3600 * 1000);
			if (days > 1)
				stringdate += " " + openbracket + "" + days + " days ago" + closebracket + "";
			if (days == 1)
				stringdate += " " + openbracket + "one day ago" + closebracket + "";
			if (days == 0) {

				long minutes = (new Date().getTime() - date.getTime()) / (60 * 1000);
				if (minutes > 1)
					stringdate += " " + openbracket + "" + minutes + " minutes ago" + closebracket + "";
				if (minutes == 1)
					stringdate += "" + openbracket + "1 minute ago" + closebracket + "";
				if (minutes == 0)
					stringdate += " " + openbracket + "now" + closebracket + "";
				if (minutes == -1)
					stringdate += "" + openbracket + "in 1 minute" + closebracket + "";

				if (minutes < -1)
					stringdate += " " + openbracket + "in " + (-minutes) + " minutes" + closebracket + "";

			}
			if (days == -1)
				stringdate += "" + openbracket + "in one day" + closebracket + "";
			if (days < -1)
				stringdate += " " + openbracket + "in " + (-days) + " days" + closebracket + "";
		}
		return stringdate;

	}

	/**
	 * formats a date in nice format showing both date and time
	 * 
	 * @param date date to show
	 * @return the date as formatted string
	 */
	public static String formatNiceDate(Date date) {
		return formatNiceDate(date, true);
	}

	private boolean compactshow;
	private boolean twolines;
	private String label;
	private String helper;
	private boolean readonly;
	private boolean isactive;
	private boolean timeedit;
	private DatePicker datepickerfield;
	private Date inputvalue;
	private boolean defaultvalue;
	private PageActionManager actionmanager;

	/**
	 * creates a date field
	 * 
	 * @param actionmanager action manager on the field
	 * @param compactshow   if true, choice field is shown as compact as possible,
	 *                      if false, displayed to be aligned to other business
	 *                      fields of a data object
	 * @param twolines      if true, show in two lines (for display in a menu)
	 * @param label         label of the choice field
	 * @param helper        multi-line helper explaining the business usage of the
	 *                      choice field
	 * @param readonly      if true, field is read-only, if false, field is
	 *                      read-write
	 * @param isactive      true if active
	 * @param timeedit      if true, possible to edit time, if false only possible
	 *                      to edit date
	 * @param inputvalue    input value
	 * @param defaultvalue  if true, default value is current time, if false,
	 *                      default value is empty
	 */
	public DateField(
			PageActionManager actionmanager,
			boolean compactshow,
			boolean twolines,
			String label,
			String helper,
			boolean readonly,
			boolean isactive,
			boolean timeedit,
			Date inputvalue,
			boolean defaultvalue) {
		this.compactshow = compactshow;
		this.twolines = twolines;
		this.label = label;
		this.helper = helper;
		this.readonly = readonly;
		this.isactive = isactive;
		this.timeedit = timeedit;
		this.inputvalue = inputvalue;
		this.defaultvalue = defaultvalue;
		this.actionmanager = actionmanager;
	}

	/**
	 * @return the datepickerfield. Note: this is only filled after the generate
	 *         method is called.
	 */
	public DatePicker getDatePicker() {
		return datepickerfield;
	}

	/**
	 * generate the javafx pane corresponding to that field
	 * 
	 * @return the javafx pane
	 */
	public Pane generate() {
		Pane thispane;
		if (this.compactshow) {
			Pane thisboxpane;
			if (this.twolines) {
				thisboxpane = new VBox();
			} else {
				thisboxpane = new HBox();
			}

			thisboxpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			thisboxpane.setPadding(new Insets(0, 0, 0, 0));
			thispane = thisboxpane;
		} else {
			FlowPane thisflowpane = new FlowPane();
			thisflowpane.setRowValignment(VPos.TOP);
			thispane = thisflowpane;
		}
		Label thislabel = new Label(label);
		if (!this.compactshow) {
			thislabel.setMinWidth(120);
		} else {
			thislabel.setPadding(new Insets(4, 3, 0, 3));
			thislabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		}
		thislabel.setWrapText(true);

		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thispane.getChildren().add(thislabel);

		if ((!readonly) && (this.isactive)) {
			if (timeedit) {
				datepickerfield = new TimestampPicker();
			} else {
				datepickerfield = new DatePicker();
			}
			if (this.readonly)
				datepickerfield.setEditable(!readonly);
			if (helper.length() > 0)
				datepickerfield.setTooltip(new Tooltip(helper));

			if (this.inputvalue != null) {

				if (!timeedit)
					datepickerfield.setValue(inputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
				if (timeedit)
					((TimestampPicker) datepickerfield)
							.setDateTimeValue(inputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
			} else {
				if (this.defaultvalue)
					datepickerfield.setValue(LocalDate.now());
			}

			thispane.getChildren().add(this.datepickerfield);

		} else {
			String stringdate = formatNiceDate(inputvalue);

			Text readonlytext = new Text();

			readonlytext.setText(stringdate);
			readonlytext.setWrappingWidth(400);
			thispane.getChildren().add(RichTextArea.getReadOnlyTextArea(actionmanager, stringdate, 400).getNode());
		}

		return thispane;
	}

}
