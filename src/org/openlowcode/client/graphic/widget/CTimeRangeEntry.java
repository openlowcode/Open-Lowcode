/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Supplier;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.tools.TimestampPicker;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.misc.DateUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Window;

/**
 * A field to enter a range in time, typically when searching objects created or
 * updated during this range in time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.6
 */
public class CTimeRangeEntry
		extends
		CPageNode {

	private String label;
	@SuppressWarnings("unused")
	private String datafieldname;
	private String helper;
	private ChoiceBox<TimeRangeSlot> defaultchoicebox;
	private Button plusbutton;
	private Button minusbutton;
	private boolean compacthelper;
	private TimestampPicker startdatepicker;
	private TimestampPicker enddatepicker;
	private Label to;

	/**
	 * Creates a TimeRangeEntry widget
	 * 
	 * @param reader     message reader
	 * @param parentpath path of the widget
	 * @throws OLcRemoteException if anything bad happens on the server during the
	 *                            transmission
	 * @throws IOException        if any error transmitting the message is
	 *                            encountered
	 */
	public CTimeRangeEntry(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		reader.returnNextEndStructure("TRE");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		return null;
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		HBox thispane = new HBox();
		thispane.setSpacing(8);

		if (label != null)
			if (label.length() > 0) {
				Label thislabel = new Label(label);
				if (helper != null)
					if (helper.length() > 0)
						thislabel.setTooltip(new Tooltip(helper));
				thislabel.setFont(
						Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
				thislabel.setMinWidth(112);
				thislabel.setWrapText(true);
				thislabel.setMaxWidth(112);
				thispane.getChildren().add(thislabel);
			}

		ObservableList<TimeRangeSlot> values = FXCollections.observableArrayList();
		for (int i = 0; i < CTimeRangeEntry.alltimeranges.length; i++)
			values.add(CTimeRangeEntry.alltimeranges[i]);
		this.defaultchoicebox = new ChoiceBox<TimeRangeSlot>(values);
		this.defaultchoicebox.setStyle(" -fx-opacity: 1; 	-fx-base: #ffffff;   ");
		thispane.getChildren().add(defaultchoicebox);
		plusbutton = new Button("+");
		thispane.getChildren().add(plusbutton);
		compacthelper = true;
		startdatepicker = new TimestampPicker();
		to = new Label("to");
		enddatepicker = new TimestampPicker();

		minusbutton = new Button("-");
		plusbutton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				compacthelper = false;
				thispane.getChildren().remove(defaultchoicebox);
				thispane.getChildren().remove(plusbutton);
				thispane.getChildren().add(startdatepicker);
				thispane.getChildren().add(to);
				thispane.getChildren().add(enddatepicker);
				thispane.getChildren().add(minusbutton);
				if (defaultchoicebox.getValue() != null) {
					TimeRangeSlot selectedvalue = defaultchoicebox.getValue();
					startdatepicker.setDateTimeValue(LocalDateTime
							.from(selectedvalue.startdategenerator.get().toInstant().atZone(ZoneId.systemDefault())));
					enddatepicker.setDateTimeValue(LocalDateTime
							.from(selectedvalue.enddategenerator.get().toInstant().atZone(ZoneId.systemDefault())));
				}
			}

		});

		minusbutton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				compacthelper = true;
				defaultchoicebox.setValue(null);
				thispane.getChildren().remove(startdatepicker);
				thispane.getChildren().remove(to);
				thispane.getChildren().remove(enddatepicker);
				thispane.getChildren().remove(minusbutton);
				thispane.getChildren().add(defaultchoicebox);
				thispane.getChildren().add(plusbutton);

			}

		});

		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if ("START".equals(objectdataloc)) {
			if (compacthelper) {
				TimeRangeSlot compactvalue = defaultchoicebox.getValue();
				if (compactvalue != null)
					return new DateDataElt(eltname, compactvalue.startdategenerator.get());
				return new DateDataElt(eltname,null);
			} else {
				if (startdatepicker.getDateTimeValue() == null)
					return new DateDataElt(eltname,null);
				return new DateDataElt(eltname,
						Date.from(startdatepicker.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant()));
			}
		}
		if ("END".equals(objectdataloc)) {
			if (compacthelper) {
				TimeRangeSlot compactvalue = defaultchoicebox.getValue();
				if (compactvalue != null)
					return new DateDataElt(eltname, compactvalue.enddategenerator.get());
				return new DateDataElt(eltname,null);
			} else {
				if (enddatepicker.getDateTimeValue() == null)
					return new DateDataElt(eltname,null);
				return new DateDataElt(eltname,
						Date.from(enddatepicker.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant()));
			}
		}
		throw new RuntimeException("Object Data Loc should be 'START' or 'END', it is " + objectdataloc);
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
	}

	@Override
	public void mothball() {
	}

	public final static TimeRangeSlot LAST15MINUTES = new TimeRangeSlot("Last 15 minutes",
			() -> (DateUtils.get15MinutesBefore()), () -> (null));
	public final static TimeRangeSlot TODAY = new TimeRangeSlot("Today", () -> (DateUtils.getStartOfToday()),
			() -> (null));
	public final static TimeRangeSlot YESTERDAY = new TimeRangeSlot("Yesterday",
			() -> (DateUtils.getStartOfYesterday()), () -> (DateUtils.getStartOfToday()));
	public final static TimeRangeSlot THISWEEK = new TimeRangeSlot("This week", () -> (DateUtils.getStartOfThisWeek()),
			() -> (null));
	public final static TimeRangeSlot LASTWEEK = new TimeRangeSlot("Last week", () -> (DateUtils.getStartOfLastWeek()),
			() -> (DateUtils.getStartOfThisWeek()));
	public final static TimeRangeSlot THISMONTH = new TimeRangeSlot("This month",
			() -> (DateUtils.getStartOfThisMonth()), () -> (null));
	public final static TimeRangeSlot LASTMONTH = new TimeRangeSlot("Last month",
			() -> (DateUtils.getStartOfLastMonth()), () -> (DateUtils.getStartOfThisMonth()));
	public final static TimeRangeSlot THISYEAR = new TimeRangeSlot("This year", () -> (DateUtils.getStartOfThisYear()),
			() -> (null));
	public final static TimeRangeSlot LASTYEAR = new TimeRangeSlot("Last year", () -> (DateUtils.getStartOfLastYear()),
			() -> (DateUtils.getStartOfThisYear()));

	public static final TimeRangeSlot[] alltimeranges = new TimeRangeSlot[] { LAST15MINUTES, TODAY, YESTERDAY, THISWEEK,
			LASTWEEK, THISMONTH, LASTMONTH, THISYEAR, LASTYEAR };

	static class TimeRangeSlot {
		private String label;

		private Supplier<Date> startdategenerator;
		private Supplier<Date> enddategenerator;

		@Override
		public String toString() {
			return label;
		}

		public TimeRangeSlot(String label, Supplier<Date> startdategenerator, Supplier<Date> enddategenerator) {
			super();
			this.label = label;
			this.startdategenerator = startdategenerator;
			this.enddategenerator = enddategenerator;
		}

	}
}
