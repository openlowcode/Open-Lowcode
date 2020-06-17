/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.tools.TimestampPicker;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DateDataEltType;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/**
 * A widget allowing to enter a timeslot (set of two dates)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CTimeslotField
		extends
		CPageNode {
	private static Logger logger = Logger.getLogger(CTimeslotField.class.getName());
	@SuppressWarnings("unused")
	private String widgetname;
	private String startfieldlabel;
	private String endfieldlabel;
	@SuppressWarnings("unused")
	private String startfieldhelper;
	@SuppressWarnings("unused")
	private String endfieldhelper;
	private int defaultvalue;
	private boolean hasstartdata;
	private boolean hasenddata;
	private CPageDataRef startdatedata;
	private CPageDataRef enddatedata;
	private Date startdateinputvalue;
	private Date enddateinputvalue;
	private DatePicker startdatepickerfield;
	private DatePicker enddatepickerfield;
	private boolean timeedit;
	private boolean currentintrigger;
	private TextField durationfield;

	/**
	 * Create a timeslot field from another widget
	 * 
	 * @param starttime  start time
	 * @param endtime    end time
	 * @param parentpath parent page of the widget
	 */
	public CTimeslotField(Date starttime, Date endtime, CPageSignifPath parentpath) {
		super(parentpath, null);
		this.startdateinputvalue = starttime;
		this.enddateinputvalue = endtime;
		this.startfieldlabel = "Start Time";
		this.endfieldlabel = "End Time";
		this.timeedit = true;

	}

	/**
	 * create a timeslot field from a server message
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CTimeslotField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.widgetname = reader.returnNextStringField("WGN");
		this.timeedit = reader.returnNextBooleanField("TME");
		this.startfieldlabel = reader.returnNextStringField("STL");
		this.endfieldlabel = reader.returnNextStringField("ENL");
		this.startfieldhelper = reader.returnNextStringField("STH");
		this.endfieldhelper = reader.returnNextStringField("ENH");
		this.defaultvalue = reader.returnNextIntegerField("DFV");
		hasstartdata = reader.returnNextBooleanField("HSD");
		if (hasstartdata)
			this.startdatedata = CPageDataRef.parseCPageDataRef(reader);
		hasenddata = reader.returnNextBooleanField("HED");
		if (hasenddata)
			this.enddatedata = CPageDataRef.parseCPageDataRef(reader);
		reader.returnNextEndStructure("TSF");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		HBox totalpane = new HBox(1);
		totalpane.setPadding(new Insets(1, 1, 1, 0));
		totalpane.setAlignment(Pos.TOP_LEFT);
		VBox fieldtable = new VBox(8);
		fieldtable.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		fieldtable.setPadding(new Insets(5, 5, 5, 0));
		fieldtable.setMinWidth(700);
		// add here manualy two Date Fields and the duration field
		if (this.startdatedata != null) {
			this.startdateinputvalue = getExternalContent(inputdata, startdatedata);
		}
		if (this.enddatedata != null) {
			this.enddateinputvalue = getExternalContent(inputdata, enddatedata);
		}

		// ----------------------------- START FIELD ----------------------------
		FlowPane thisstartflowpane = new FlowPane();
		thisstartflowpane.setRowValignment(VPos.TOP);
		Label thisstartlabel = new Label(this.startfieldlabel);
		thisstartlabel.setMinWidth(120);
		thisstartlabel.setWrapText(true);

		thisstartlabel.setFont(
				Font.font(thisstartlabel.getFont().getName(), FontPosture.ITALIC, thisstartlabel.getFont().getSize()));
		thisstartflowpane.getChildren().add(thisstartlabel);
		if (timeedit) {
			startdatepickerfield = new TimestampPicker();

		} else {
			startdatepickerfield = new DatePicker();
		}

		if (this.startdateinputvalue != null) {

			if (!timeedit)
				startdatepickerfield
						.setValue(startdateinputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			if (timeedit)
				((TimestampPicker) startdatepickerfield).setDateTimeValue(
						startdateinputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
		} else {
			if (this.defaultvalue == 1)
				startdatepickerfield.setValue(LocalDate.now());
		}
		thisstartflowpane.getChildren().add(this.startdatepickerfield);
		fieldtable.getChildren().add(thisstartflowpane);
		// ----------------------------- DURATION ----------------------------
		FlowPane durationflowpane = new FlowPane();
		durationflowpane.setRowValignment(VPos.TOP);
		Label durationlabel = new Label("Duration (m)");
		durationlabel.setMinWidth(120);
		durationlabel.setWrapText(true);
		durationlabel.setFont(
				Font.font(durationlabel.getFont().getName(), FontPosture.ITALIC, durationlabel.getFont().getSize()));
		durationflowpane.getChildren().add(durationlabel);
		durationfield = new TextField();
		if ((this.startdateinputvalue != null) && (this.enddateinputvalue != null)) {
			long durationinminutes = (this.enddateinputvalue.getTime() - this.startdateinputvalue.getTime()) / 60000;
			durationfield.setText("" + durationinminutes);
		}
		durationfield.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldvalue, String newvalue) {
				if (newvalue.length() > 0) {
					try {
						Integer integer = new Integer(newvalue);
						if (integer < 0)
							throw new NumberFormatException("");
					} catch (NumberFormatException e) {
						durationfield.setText(oldvalue);
					}
				}
			}

		});
		durationflowpane.getChildren().add(durationfield);
		fieldtable.getChildren().add(durationflowpane);
		// ----------------------------- END FIELD ----------------------------
		FlowPane thisendflowpane = new FlowPane();
		thisendflowpane.setRowValignment(VPos.TOP);
		Label thisendlabel = new Label(this.endfieldlabel);
		thisendlabel.setMinWidth(120);
		thisendlabel.setWrapText(true);

		thisendlabel.setFont(
				Font.font(thisendlabel.getFont().getName(), FontPosture.ITALIC, thisendlabel.getFont().getSize()));
		thisendflowpane.getChildren().add(thisendlabel);
		if (timeedit) {
			enddatepickerfield = new TimestampPicker();
		} else {
			enddatepickerfield = new DatePicker();
		}

		if (this.enddateinputvalue != null) {

			if (!timeedit)
				enddatepickerfield.setValue(enddateinputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
			if (timeedit)
				((TimestampPicker) enddatepickerfield).setDateTimeValue(
						enddateinputvalue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
		} else {
			if (this.defaultvalue == 1)
				enddatepickerfield.setValue(LocalDate.now());
		}
		thisendflowpane.getChildren().add(this.enddatepickerfield);
		fieldtable.getChildren().add(thisendflowpane);
		currentintrigger = false;
		if (timeedit) {
			TimestampPicker startpickercasted = (TimestampPicker) startdatepickerfield;
			TimestampPicker endpickercasted = (TimestampPicker) enddatepickerfield;
			startpickercasted.dateTimeValueProperty().addListener(new ChangeListener<LocalDateTime>() {

				@Override
				public void changed(
						ObservableValue<? extends LocalDateTime> event,
						LocalDateTime olddate,
						LocalDateTime newdate) {
					logger.info("trigger " + olddate + ", " + newdate + " end picker value "
							+ endpickercasted.getDateTimeValue());
					if (!currentintrigger) {
						currentintrigger = true;
						if (endpickercasted.getDateTimeValue() == null) {
							logger.info(" --- endpickercasted detected as null, refresh");
							if (durationfield.getText().length() == 0) {
								LocalDateTime newdateforend = newdate.plus(Duration.ofHours(1));
								endpickercasted.setDateTimeValue(newdateforend);
								durationfield.setText("60");
							} else {
								Integer duration = new Integer(durationfield.getText());
								LocalDateTime newdateforend = newdate.plusMinutes(duration.intValue());
								endpickercasted.setDateTimeValue(newdateforend);

							}
						} else {
							if (olddate != null) {
								Duration duration = Duration.between(olddate, endpickercasted.getDateTimeValue());

								LocalDateTime newendpickervalue = newdate.plus(duration);
								logger.info(
										"setting enddate at " + newdate + " + " + duration + " = " + newendpickervalue);
								endpickercasted.setDateTimeValue(newendpickervalue);
								endpickercasted.requestLayout();
							}
						}
						currentintrigger = false;
					}
				}
			});
			durationfield.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
					if (!currentintrigger) {
						currentintrigger = true;
						if (arg2.length() > 0) {
							Integer duration = new Integer(arg2);

							if (startpickercasted.getDateTimeValue() != null) {
								LocalDateTime startdate = startpickercasted.getDateTimeValue();
								LocalDateTime newendtime = startdate.plusMinutes(duration);
								endpickercasted.setDateTimeValue(newendtime);
							}
						} else {
							endpickercasted.setDateTimeValue(startpickercasted.getDateTimeValue());
						}
						currentintrigger = false;
					}

				}

			});
			endpickercasted.dateTimeValueProperty().addListener(new ChangeListener<LocalDateTime>() {

				@Override
				public void changed(
						ObservableValue<? extends LocalDateTime> event,
						LocalDateTime olddate,
						LocalDateTime newdate) {
					if (!currentintrigger) {
						currentintrigger = true;
						if (startpickercasted.getDateTimeValue() == null) {
							LocalDateTime newstartdate = newdate.plus(Duration.ofHours(-1));
							startpickercasted.setDateTimeValue(newstartdate);
							durationfield.setText("60");
						} else {
							if (startpickercasted.getDateTimeValue().isAfter(newdate)) {
								// keep same duration if new end date is chosen before start date
								Duration duration = Duration.between(startpickercasted.getDateTimeValue(), olddate);

								LocalDateTime newstartdate = newdate.minus(duration);
								startpickercasted.setDateTimeValue(newstartdate);
							} else {
								Duration duration = Duration.between(startpickercasted.getDateTimeValue(), newdate);
								long seconds = duration.getSeconds();
								long minutes = seconds / 60;
								durationfield.setText("" + minutes);
							}
						}
						currentintrigger = false;
					}
				}
			});

		}
		Pane contentpane = new VBox(8);
		contentpane.setPadding(new Insets(2, 2, 2, 0));
		contentpane.getChildren().add(fieldtable);
		totalpane.getChildren().add(contentpane);
		HBox.setHgrow(contentpane, Priority.ALWAYS);
		return totalpane;
	}

	public Date getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		DateDataElt thistextelement = (DateDataElt) thiselement;
		return thistextelement.getPayload();
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (!(type instanceof DateDataEltType))
			throw new RuntimeException(
					String.format("Only DateDataEltType can be extracted from CDateField, but request was %s ", type));

		if (objectfieldname.equals("STARTDATE")) {
			if (startdatepickerfield.getValue() == null)
				return new DateDataElt(eltname, null);

			if (!timeedit)
				return new DateDataElt(eltname,
						Date.from(startdatepickerfield.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)));
			return new DateDataElt(eltname, Date.from(((TimestampPicker) startdatepickerfield).getDateTimeValue()
					.atZone(ZoneId.systemDefault()).toInstant()));

		}

		if (objectfieldname.equals("ENDDATE")) {
			if (enddatepickerfield.getValue() == null)
				return new DateDataElt(eltname, null);

			if (!timeedit)
				return new DateDataElt(eltname,
						Date.from(enddatepickerfield.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)));
			return new DateDataElt(eltname, Date.from(((TimestampPicker) enddatepickerfield).getDateTimeValue()
					.atZone(ZoneId.systemDefault()).toInstant()));

		}
		throw new RuntimeException("objectfieldname has to be either 'STARTDATE' or 'ENDDATE'.");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");
	}

	@Override
	public void mothball() {
		// Do nothing yet
	}

}
