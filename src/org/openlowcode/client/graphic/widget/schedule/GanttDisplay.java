/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.schedule;

import javafx.scene.control.Label;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.event.EventHandler;
import javafx.scene.effect.DropShadow;
import javafx.scene.shape.Rectangle;
import javafx.scene.Cursor;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.schedule.BusinessCalendar.BusinessTimeInstant;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.graphic.widget.tools.ChoiceField;
import org.openlowcode.client.graphic.widget.tools.DateField;
import org.openlowcode.client.graphic.widget.tools.NicePopup;
import org.openlowcode.client.graphic.widget.tools.TimestampPicker;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.pdf.PDFDocument;
import org.openlowcode.tools.pdf.PDFPage;

import javafx.scene.shape.Circle;
import javafx.scene.Parent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;

/**
 * a GANTT display in javafx. The Gantt display can display
 * <ul>
 * <li>tasks</li>
 * <li>dependencies</li>
 * <li>two attributes on the task used for encoding task background color and a
 * color dot</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of GANTT Task
 */
public class GanttDisplay<E extends GanttTask<E>> {

	private static Logger logger = Logger.getLogger(GanttDisplay.class.getName());
	private EventHandler<MouseEvent> mouseclickedoutsideoftask;
	private GanttTaskMouseEventHandler<E> clickongantttaskhandler;
	private double grabstartx;
	private double grabstarty;
	private boolean grabisvresize;
	private double grabendx;
	private double grabendy;
	private static final SimpleDateFormat dateformat = new SimpleDateFormat("EE dd MM");
	private GanttPlanning<E> planningtoshow;
	private Date startdatedisplaywindow;
	private Date enddatedisplaywindow;

	private TimeScale timescale;
	private StackPane toppane;
	private ListView<GanttTaskDisplay<E>> listView;
	private double latestCellWidth;
	private String attributefortitle;
	private BusinessCalendar businesscalendar;

	private String attributemappingforcolor;
	private Color defaultcolorforattributemapping;
	private HashMap<String, Color> mappingcolorforattributes;
	private HashMap<String, Color> mappingfordotattributes;
	private double minwidth;
	private String attributemappingfordot;
	private BorderPane borderpane;
	private Window parentwindow;

	/**
	 * creates a GANTT display
	 * 
	 * @param planningtoshow   the planning to show
	 * @param businesscalendar the business calendar used to determine the business
	 *                         time
	 */
	public GanttDisplay(GanttPlanning<E> planningtoshow, BusinessCalendar businesscalendar) {
		this.planningtoshow = planningtoshow;

		this.latestCellWidth = 0;
		this.businesscalendar = businesscalendar;
		this.minwidth = 800;
	}

	/**
	 * sets the handler for clicking on a task in the GANNT display
	 * 
	 * @param clickongantttaskhandler mouse handler to add
	 */
	public void setGanttTaskOnMouseClicked(GanttTaskMouseEventHandler<E> clickongantttaskhandler) {
		this.clickongantttaskhandler = clickongantttaskhandler;
	}

	/**
	 * sets the handler for clicking outside a task in the GANNT display
	 * 
	 * @param mouseclickedoutsideoftask event handler to manage click events outside
	 *                                  a task
	 */
	public void setOnMouseClickedOutsideOfGanttTask(EventHandler<MouseEvent> mouseclickedoutsideoftask) {
		this.mouseclickedoutsideoftask = mouseclickedoutsideoftask;
	}

	/**
	 * 
	 * @return the mouse event handler for click on task
	 */
	GanttTaskMouseEventHandler<E> getEventHandlerOnGanttTaskMouseClicked() {
		return this.clickongantttaskhandler;
	}

	/**
	 * @return the mouse event handler for click outside of a task
	 */
	EventHandler<MouseEvent> getEventHandlerOnClickOutsideOfGanttTask() {
		return this.mouseclickedoutsideoftask;
	}

	/**
	 * @return the name of the attribute used for dot
	 */
	public String getAttributeMappingForDot() {
		return this.attributemappingfordot;
	}

	private void refreshListView() {
		// valid from javafx 8.0.60
		// listView.refresh();
		// unfortunately, for javafx in current customer, we need compatibility with
		// javafx 8.0.51, so we use hack below
		try {
			listView.getItems().add(null);
			listView.getItems().remove(listView.getItems().size() - 1);
		} catch (Throwable t) {
			logger.severe("Error in refreshing list view " + t.getMessage());
			for (int i = 0; i < t.getStackTrace().length; i++)
				logger.severe("  * " + t.getStackTrace()[i]);
		}
	}

	/**
	 * @param minwidth specify the minimum width for the component. By default, the
	 *                 width is 800 points.
	 */
	public void seMinWidth(double minwidth) {
		this.minwidth = minwidth;
	}

	/**
	 * @return the name of the attribute used for mapping for color
	 */
	public String getAttributeMappingForColor() {
		return this.attributemappingforcolor;
	}

	/**
	 * determines the background color of the task rectangle in the planning
	 * 
	 * @param attributevalue value of the attribute used for background color
	 * @return color assigned for this value
	 */
	public Color getColorForAttributeValue(String attributevalue) {
		Color mappingcolor = mappingcolorforattributes.get(attributevalue);
		if (mappingcolor != null)
			return mappingcolor;
		return defaultcolorforattributemapping;
	}

	/**
	 * determines the dot color of the task
	 * 
	 * @param attributevalue value of the attribute used for dot color
	 * @return color assigned for this value
	 */
	public Color getColorForDot(String attributevalue) {
		Color colorfordot = this.mappingfordotattributes.get(attributevalue);
		return colorfordot;
	}

	/**
	 * @return the business calendar used for display of opening time on this Gannt
	 *         display
	 */
	public BusinessCalendar getBusinessCalendar() {
		return this.businesscalendar;
	}

	/**
	 * @return the attribute shown for the task title
	 */
	public String getAttributefortitle() {
		return attributefortitle;
	}

	/**
	 * @return the planning to shown in this widget
	 */
	public GanttPlanning<E> getPlanningtoshow() {
		return planningtoshow;
	}

	/**
	 * @return the first date shown in the planning
	 */
	public Date getStartdatedisplaywindow() {
		return startdatedisplaywindow;
	}

	/**
	 * @return the last date shown in the planning
	 */
	public Date getEnddatedisplaywindow() {
		return enddatedisplaywindow;
	}

	/**
	 * sets the display window for this display
	 * 
	 * @param startdatedisplaywindow first date shown in the display window
	 * @param enddatedisplaywindow   last date shown in the display window
	 */
	public void setDisplayWindow(Date startdatedisplaywindow, Date enddatedisplaywindow) {
		this.startdatedisplaywindow = startdatedisplaywindow;
		this.enddatedisplaywindow = enddatedisplaywindow;
	}

	/**
	 * @param attributename the name of the attribute used for task label
	 */
	public void setTitleAttribute(String attributename) {
		if (!planningtoshow.isAttributeValid(attributename))
			throw new RuntimeException("Invalid Attribute " + attributename + " setup for display");
		this.attributefortitle = attributename;
	}

	/**
	 * @param attributename sets the name of the attribute used for dot field
	 *                      mapping
	 */
	public void setDotFieldMapping(String attributename) {
		this.attributemappingfordot = attributename;
		mappingfordotattributes = new HashMap<String, Color>();
	}

	/**
	 * sets a new mapping between a value of the attribute used for dot, and a color
	 * 
	 * @param attributevalue value of the attribute
	 * @param valuecolor     color to shown
	 */
	public void addAttributeDotValueMapping(String attributevalue, Color valuecolor) {

		mappingfordotattributes.put(attributevalue, valuecolor);
	}

	/**
	 * sets the attribute name used for displaying background color of the GANNT task
	 * 
	 * @param attributename name of the attribute
	 * @param defaultcolor default color. If nothing specified, will be light gray
	 */
	public void SetAttributeColorMapping(String attributename, Color defaultcolor) {
		if (this.attributemappingforcolor != null)
			throw new RuntimeException("Attribute mapping can be set for only one attribute, got a request for '"
					+ attributename + "- while '" + this.attributemappingforcolor + "' already set");
		this.attributemappingforcolor = attributename;
		this.defaultcolorforattributemapping = defaultcolor;
		if (defaultcolor == null)
			this.defaultcolorforattributemapping = Color.LIGHTGRAY;
		mappingcolorforattributes = new HashMap<String, Color>();
	}

	/**
	 * adds a mapping for the task background color
	 * 
	 * @param attributevalue value of the attribute
	 * @param valuecolor specific color
	 */
	public void addAttributeColorValueMapping(String attributevalue, Color valuecolor) {
		mappingcolorforattributes.put(attributevalue, valuecolor);
	}

	private void zoomDatePercent(long percentzoom) {

		changeByPercentage(percentzoom, true, false);

	}

	private void changeByPercentage(long percent, boolean plusstart, boolean plusend) {
		BusinessTimeInstant starttimedisplaywindow = businesscalendar.new BusinessTimeInstant(startdatedisplaywindow,
				true);
		BusinessTimeInstant endtimedisplaywindow = businesscalendar.new BusinessTimeInstant(enddatedisplaywindow,
				false);
		long interval = endtimedisplaywindow.OpeningTimeInMsSince(starttimedisplaywindow);

		starttimedisplaywindow = starttimedisplaywindow
				.addOpeningTimeInMs((plusstart ? +1 : -1) * interval / 2 * percent / 100);
		endtimedisplaywindow = endtimedisplaywindow
				.addOpeningTimeInMs((plusend ? +1 : -1) * interval / 2 * percent / 100);

		startdatedisplaywindow = starttimedisplaywindow.toDate();
		enddatedisplaywindow = endtimedisplaywindow.toDate();
		logger.fine(" |-- new display window " + startdatedisplaywindow + " - " + enddatedisplaywindow
				+ " (after change by percentage " + percent + " " + plusstart + "/" + plusend
				+ " ), interval definition " + interval);
	}

	private void widenDatePercent(long percentzoom) {
		changeByPercentage(percentzoom, false, true);
	}

	private void backDatePercent(long percent) {
		changeByPercentage(percent, false, false);
	}

	private void advanceDatePercent(long percent) {
		changeByPercentage(percent, true, true);
	}

	/**
	 * gets a javafx node to be used in the Open Lowcode client
	 * 
	 * @param actionmanager page action manager
	 * @param parentwindow parent window
	 * @return the javafx node
	 */
	public Parent getNode(PageActionManager actionmanager, Window parentwindow) {
		this.parentwindow = parentwindow;
		if ((this.startdatedisplaywindow == null) || (this.enddatedisplaywindow == null))
			setDefaultDisplayWindow();

		ObservableList<GanttTaskDisplay<E>> data = FXCollections.observableArrayList();

		for (int i = 0; i < planningtoshow.getTaskNr(); i++) {
			E thistask = planningtoshow.getTaskAt(i);
			GanttTaskDisplay<E> display = new GanttTaskDisplay<E>(thistask, this);
			data.add(display);
		}

		listView = new ListView<GanttTaskDisplay<E>>(data);
		listView.setCellFactory(param -> new GanttTaskCell<E>(actionmanager));
		listView.setFixedCellSize(20);
		listView.setMinWidth(this.minwidth);
		listView.setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (!event.isPrimaryButtonDown()) {
					double y = event.getY();
					if (y > listView.getHeight() - 10) {
						listView.getScene().setCursor(Cursor.V_RESIZE);
					} else {
						listView.getScene().setCursor(Cursor.DEFAULT);
					}
				}

			}

		});
		listView.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				listView.getScene().setCursor(Cursor.DEFAULT);
			}
		});
		listView.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				if (event.isPrimaryButtonDown()) {

					grabstartx = event.getX();
					grabstarty = event.getY();

					if (grabstarty > listView.getHeight() - 10) {
						listView.getScene().setCursor(Cursor.V_RESIZE);
						grabisvresize = true;
					} else {
						listView.getScene().setCursor(Cursor.CLOSED_HAND);
						grabisvresize = false;
					}
				}
			}

		});

		listView.setOnMouseReleased(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				listView.getScene().setCursor(Cursor.DEFAULT);
				grabendx = event.getX();
				grabendy = event.getY();
				if (grabisvresize) {
					double deltaheight = grabendy - grabstarty;
					double newHeight = listView.getHeight() + deltaheight;
					listView.setMinHeight(newHeight);
					listView.setMaxHeight(newHeight);

				} else {
					if (grabstartx != 0) {
						double xdrag = grabstartx - grabendx;
						double ydrag = grabstarty - grabendy;
						long percent = (long) ((100 * xdrag) / (listView.getWidth()));
						if (percent > 0)
							advanceDatePercent(2 * percent);
						if (percent < 0)
							backDatePercent(-2 * percent);
						long percentzoom = (long) (ydrag / 3);
						if (percentzoom < 0) {
							widenDatePercent(-percentzoom);
						} else {
							zoomDatePercent(percentzoom);
						}
						refreshListView();
						timescale.draw();
						grabstartx = 0;
						grabstarty = 0;
					}
				}
			}

		});

		borderpane = new BorderPane();
		borderpane.setCenter(listView);
		HBox topbuttonpane = new HBox(8);
		topbuttonpane.setAlignment(Pos.BASELINE_RIGHT);

		toppane = new StackPane();

		timescale = new TimeScale();
		// toppane.getChildren().add(timescale);
		toppane.getChildren().add(timescale);
		toppane.getChildren().add(topbuttonpane);

		borderpane.setTop(toppane);

		timescale.draw();
		topbuttonpane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
		topbuttonpane.setPadding(new Insets(0, 0, 0, 0));
		Button left = new Button("<");
		left.setPadding(new Insets(1, 1, 1, 1));
		topbuttonpane.getChildren().add(left);
		Button right = new Button(">");
		right.setPadding(new Insets(1, 1, 1, 1));
		topbuttonpane.getChildren().add(right);
		Button plus = new Button("+");
		plus.setPadding(new Insets(1, 1, 1, 1));
		topbuttonpane.getChildren().add(plus);
		Button minus = new Button("-");
		minus.setPadding(new Insets(1, 1, 1, 1));
		topbuttonpane.getChildren().add(minus);
		plus.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				zoomDatePercent(10);
				refreshListView();
				timescale.draw();
			}
		});
		minus.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				widenDatePercent(10);
				refreshListView();
				timescale.draw();

			}
		});

		left.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				backDatePercent(10);
				refreshListView();
				timescale.draw();
			}
		});
		right.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				advanceDatePercent(10);
				refreshListView();
				timescale.draw();
			}
		});
		if ((this.attributemappingforcolor != null) || (this.attributemappingfordot != null)) {
			FlowPane legendflowpane = new FlowPane();
			legendflowpane.setVgap(4);
			legendflowpane.setHgap(6);

			if (this.attributemappingforcolor != null)
				if (this.attributemappingforcolor.length() > 0) {

					Iterator<Entry<String, Color>> colormapiterator = mappingcolorforattributes.entrySet().iterator();
					while (colormapiterator.hasNext()) {
						Entry<String, Color> colormapentry = colormapiterator.next();
						Rectangle rectangle = new Rectangle(0, 0, 20, 8);
						rectangle.setArcHeight(2.0);
						rectangle.setArcWidth(2.0);
						rectangle.setFill(colormapentry.getValue());
						rectangle.setStroke(Color.BLACK);
						DropShadow ds = new DropShadow();
						ds.setRadius(1.);
						ds.setOffsetX(1.);
						ds.setOffsetY(1.);
						ds.setColor(Color.color(0.8, 0.8, 0.8));
						rectangle.setEffect(ds);
						legendflowpane.getChildren().add(rectangle);
						legendflowpane.getChildren().add(new Label(colormapentry.getKey() + "   "));
					}

				}
			if (this.attributemappingfordot != null)
				if (this.attributemappingfordot.length() > 0) {
					Iterator<
							Entry<String, Color>> colormapiterator = this.mappingfordotattributes.entrySet().iterator();
					while (colormapiterator.hasNext()) {
						Entry<String, Color> colormapentry = colormapiterator.next();
						Circle circle = new Circle(3, 3, 3);
						circle.setFill(colormapentry.getValue());
						circle.setStroke(Color.BLACK);
						DropShadow ds = new DropShadow();
						ds.setRadius(1.);
						ds.setOffsetX(1.);
						ds.setOffsetY(1.);
						ds.setColor(Color.color(0.8, 0.8, 0.8));
						circle.setEffect(ds);
						legendflowpane.getChildren().add(circle);
						legendflowpane.getChildren().add(new Label(colormapentry.getKey() + "   "));
					}
				}
			borderpane.setBottom(legendflowpane);
		}
		return borderpane;
	}

	private void setDefaultDisplayWindow() {
		int index = planningtoshow.getTaskNr();
		if (index > 20)
			index = 20;
		Date minstartdate = planningtoshow.getTaskAt(0).getStarttime();
		Date maxenddate = planningtoshow.getTaskAt(0).getEndtime();

		for (int i = 1; i < index; i++) {
			GanttTask<E> task = planningtoshow.getTaskAt(i);
			Date startdate = task.getStarttime();
			Date enddate = task.getEndtime();
			if (minstartdate.compareTo(startdate) < 0)
				minstartdate = startdate;
			if (maxenddate.compareTo(enddate) > 0)
				maxenddate = enddate;
		}
		if (maxenddate.getTime() - minstartdate.getTime() < 14 * 86400 * 1000)
			maxenddate = new Date(minstartdate.getTime() + 14 * 86400 * 1000);
		this.startdatedisplaywindow = minstartdate;
		this.enddatedisplaywindow = maxenddate;

	}

	private class TimeScale
			extends
			Canvas {

		public TimeScale() {
			super(600, 30);
			widthProperty().bind(listView.widthProperty());
			widthProperty().addListener(event -> draw());
			heightProperty().addListener(event -> draw());
		}

		@Override
		public boolean isResizable() {
			return true;
		}

		@Override
		public double prefWidth(double height) {
			return getWidth();
		}

		@Override
		public double prefHeight(double width) {
			return 30;
		}

		private void draw() {

			GraphicsContext gc = getGraphicsContext2D();
			gc.setFill(Color.WHITE);

			gc.fillRect(0, 0, getWidth(), getHeight());

			Date[] allstartsofday = DateUtils.getAllStartOfDays(startdatedisplaywindow, enddatedisplaywindow,
					businesscalendar);
			logger.fine("   |-- treating " + allstartsofday.length + " starts of days");
			GanttTaskCell.drawSeparators(gc, startdatedisplaywindow, enddatedisplaywindow, getHeight() / 2, 0,
					getHeight(), latestCellWidth, businesscalendar, 1);

			Font font = Font.font(10);
			gc.setFont(font);
			gc.setLineDashes(null);
			gc.setStroke(Color.BLACK);
			gc.setFill(Color.BLACK);
			double lasthourx = -50;
			double lastdayx = -200;
			for (int i = 0; i < allstartsofday.length; i++) {

				Date separatortoprint = allstartsofday[i];
				double separatorratio = DateUtils.genericDateToCoordinates(separatortoprint, startdatedisplaywindow,
						enddatedisplaywindow, businesscalendar).getValue();
				double xdate = separatorratio * latestCellWidth + 2;
				if (xdate - lastdayx > 50) {
					// gc.strokeText(dateformat.format(separatortoprint),xdate, 14);
					gc.fillText(dateformat.format(separatortoprint), xdate, 14);

					lastdayx = xdate;
				}
				if (!GanttTaskCell.isReducedDisplay(allstartsofday))
					for (int j = businesscalendar.getDaywindowhourstart() + 1; j < businesscalendar
							.getDaywindowhourend(); j++) {
						Date hour = new Date(separatortoprint.getTime()
								+ (j - businesscalendar.getDaywindowhourstart()) * 3600 * 1000);
						double hourratio = DateUtils.genericDateToCoordinates(hour, startdatedisplaywindow,
								enddatedisplaywindow, businesscalendar).getValue();
						double x = hourratio * latestCellWidth - 3;
						if (x - lasthourx > 15) {
							// gc.strokeText(""+j,x, 28);
							gc.fillText("" + j, x, 28);
							lasthourx = x;
						}

					}
			}
		}
	}

	/**
	 * redraw title after the timescale has been scaled
	 * 
	 * @param newValue new cell width
	 */
	public void redrawTitleIfChanged(Number newValue) {
		if (this.latestCellWidth != newValue.doubleValue()) {
			this.latestCellWidth = newValue.doubleValue();
			timescale.draw();
		}

	}

	/**
	 * print the GANNT as a PDF and downloads it on the user machine
	 * 
	 * @param actionmanager page action manager
	 * @param event mouse event 
	 */
	protected void printGantt(PageActionManager actionmanager, MouseEvent event) {

		/*
		 * proposed printing format, in order: - A3 landscape - A2 landscape - A1
		 * landscape - A0 landscape - A0 portrait
		 */

		try {
			Date minplanningdate = null;
			Date maxplanningdate = null;

			for (int i = 0; i < planningtoshow.getTaskNr(); i++) {
				GanttTask<E> thistask = planningtoshow.getTaskAt(i);
				if (minplanningdate == null)
					minplanningdate = thistask.getStarttime();
				if (maxplanningdate == null)
					maxplanningdate = thistask.getEndtime();
				if (thistask.getStarttime().compareTo(minplanningdate) < 0)
					minplanningdate = thistask.getStarttime();
				if (thistask.getEndtime().compareTo(maxplanningdate) > 0)
					maxplanningdate = thistask.getEndtime();
			}

			Pane dateselectionpopup = new VBox(8);

			dateselectionpopup.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			dateselectionpopup.setPadding(new Insets(5, 5, 5, 0));
			DateField startdatefield = new DateField(actionmanager, false, true, "Start date",
					"Start for the print window for the GANTT chart", false, true, true, minplanningdate, false);
			DateField enddatefield = new DateField(actionmanager, false, true, "End date",
					"End for the print window for the GANTT chart", false, true, true, maxplanningdate, false);
			dateselectionpopup.getChildren().add(startdatefield.generate());
			dateselectionpopup.getChildren().add(enddatefield.generate());
			ChoiceField colorattributefield = null;
			if (this.attributemappingforcolor != null) {

				Set<String> allcolorvalues = this.mappingcolorforattributes.keySet();
				List<String> allcolorlist = new ArrayList<String>(allcolorvalues);
				allcolorlist.sort(String.CASE_INSENSITIVE_ORDER);
				if (allcolorlist.size() > 1) {

					ObservableList<CChoiceFieldValue> colorlist = FXCollections.observableArrayList();
					for (int i = 0; i < allcolorlist.size(); i++) {
						CChoiceFieldValue newvalue = new CChoiceFieldValue(allcolorlist.get(i), allcolorlist.get(i),
								allcolorlist.get(i), i);
						colorlist.add(newvalue);
					}
					colorattributefield = new ChoiceField(actionmanager, false, false, attributemappingforcolor,
							attributemappingforcolor, true, true, false, colorlist, null, null);
					dateselectionpopup.getChildren().add(colorattributefield.getNode());
				}

			}
			ChoiceField dotattributefield = null;
			if (this.attributemappingfordot != null) {
				Set<String> alldotvalues = this.mappingfordotattributes.keySet();
				List<String> alldotlist = new ArrayList<String>(alldotvalues);
				alldotlist.sort(String.CASE_INSENSITIVE_ORDER);
				if (alldotlist.size() > 1) {
					ObservableList<CChoiceFieldValue> dotlist = FXCollections.observableArrayList();
					for (int i = 0; i < alldotlist.size(); i++) {
						CChoiceFieldValue newvalue = new CChoiceFieldValue(alldotlist.get(i), alldotlist.get(i),
								alldotlist.get(i), i);
						dotlist.add(newvalue);
					}
					dotattributefield = new ChoiceField(actionmanager, false, false, attributemappingfordot,
							attributemappingfordot, true, true, false, dotlist, null, null);
					dateselectionpopup.getChildren().add(dotattributefield.getNode());
				}
			}
			Button button = new Button("Print");
			button.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
			button.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			dateselectionpopup.getChildren().add(button);
			// First format based on number of tasks
			NicePopup nicepopup = new NicePopup(borderpane, dateselectionpopup, parentwindow, false, false, false);

			ChoiceField finaldotattributefield = dotattributefield;
			ChoiceField finalcolorattributefield = colorattributefield;
			button.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {

						Date minplanningdate = Date.from(((TimestampPicker) startdatefield.getDatePicker())
								.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant());
						Date maxplanningdate = Date.from(((TimestampPicker) enddatefield.getDatePicker())
								.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant());

						nicepopup.getSubScene().close();

						final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

						float[] formatwidthinmm = { 420, 594, 840, 1188, 840 };
						float[] formatheightinmm = { 297, 420, 594, 840, 1188 };

						int tasknumber = planningtoshow.getTaskNr();

						Integer selectedtaskindex[] = new Integer[tasknumber];
						int selectedtasknumber = 0;
						for (int i = 0; i < tasknumber; i++) {
							GanttTask<E> task = planningtoshow.getTaskAt(i);
							boolean istaskshown = true;
							// - condition on start and end time
							if (task.getEndtime().compareTo(minplanningdate) < 0)
								istaskshown = false;
							if (task.getStarttime().compareTo(maxplanningdate) > 0)
								istaskshown = false;
							// - condition on dot field
							if (finaldotattributefield != null) {
								ArrayList<CChoiceFieldValue> result = finaldotattributefield.getSelectedValues();
								// only filter if there is a value selected
								if (result.size() > 0) {
									boolean isvalid = false;
									String currentaskattributevalue = task.getAttribute(attributemappingfordot);
									for (int j = 0; j < result.size(); j++)
										if (result.get(j).getDisplayvalue().equals(currentaskattributevalue))
											isvalid = true;
									if (!isvalid)
										istaskshown = false;
								}
							}
							// - condition on color encoding for task
							if (finalcolorattributefield != null) {
								ArrayList<CChoiceFieldValue> result = finalcolorattributefield.getSelectedValues();
								if (result.size() > 0) {
									boolean isvalid = false;
									String currentaskattributevalue = task.getAttribute(attributemappingforcolor);
									for (int j = 0; j < result.size(); j++)
										if (result.get(j).getDisplayvalue().equals(currentaskattributevalue))
											isvalid = true;
									if (!isvalid)
										istaskshown = false;
								}
							}
							if (istaskshown) {
								selectedtaskindex[i] = new Integer(selectedtasknumber);
								selectedtasknumber++;
							} else {
								selectedtaskindex[i] = null;
							}

						}

						float linesizeinmm = (new PDFPage.BoxTextContent(1, 1, PDFPage.TEXTTYPE_PLAIN)).getHeight();
						logger.warning("Found line size = " + linesizeinmm);
						int headersizeinmm = 50;
						int sizeselected = -1;
						for (int i = 0; i < formatheightinmm.length; i++) {
							if (linesizeinmm * selectedtasknumber + headersizeinmm < formatheightinmm[i]) {
								sizeselected = i;
								break;
							}
						}
						if (sizeselected == -1)
							throw new Exception("Too many tasks in the planning for printing even in A0 portrait");
						logger.warning("Size decided after task review= " + sizeselected);

						// second format based on attributes to display in the left table. All
						// attributes except the one shown
						// as task label are shown in the table

						String[] attributes = planningtoshow.getAllowedAttributeList();

						ArrayList<String> finalattributestoshowintable = new ArrayList<String>();

						for (int i = 0; i < attributes.length; i++) {
							String thisattribute = attributes[i];
							boolean select = true;
							if (thisattribute.equals(attributefortitle))
								select = false;
							if (select) {
								finalattributestoshowintable.add(thisattribute);

							}
						}

						// calculate column width

						float[] columnwidthinmm = new float[finalattributestoshowintable.size()];

						float totalcolumnwidth = 0;

						for (int i = 0; i < finalattributestoshowintable.size(); i++) {
							float thiscolumnwidth = 0;
							String biggeststring = "unset";
							for (int j = 0; j < planningtoshow.getTaskNr(); j++) {
								Integer index = selectedtaskindex[j];
								if (index != null) {
									GanttTask<E> thistask = planningtoshow.getTaskAt(j);
									String attribute = thistask.getAttribute(finalattributestoshowintable.get(i));

									float thisattributewidth = PDFPage.getTextWidth(false, attribute, true);
									if (thiscolumnwidth < thisattributewidth) {
										thiscolumnwidth = thisattributewidth;
										biggeststring = attribute;
									}
								}
							}
							String columntitle = finalattributestoshowintable.get(i);
							float titleattributewidth = PDFPage.getTextWidth(false, columntitle, true);
							if (thiscolumnwidth < titleattributewidth) {
								thiscolumnwidth = titleattributewidth;
								biggeststring = columntitle;
							}

							columnwidthinmm[i] = thiscolumnwidth;

							logger.warning("    addding width for column " + i + ":"
									+ finalattributestoshowintable.get(i) + " - longest:" + biggeststring);
							totalcolumnwidth += thiscolumnwidth;
						}
						// then check if larger paper should be used due to lots of columns

						for (int i = sizeselected; i < formatwidthinmm.length; i++) {
							logger.warning("    - analyzing format " + i);
							double widthforsize = formatwidthinmm[i];
							// ideally, total column width is less than 40%
							if (totalcolumnwidth < (float) (widthforsize * 0.4)) {
								sizeselected = i;
								break;
							}
							if (i >= formatwidthinmm.length - 2) {
								logger.warning("    - checking exception for index " + i);
								if (totalcolumnwidth < (float) (widthforsize * 0.6)) {
									logger.warning("Exception " + widthforsize + " can take column width "
											+ totalcolumnwidth + " with 60% or less");

									// as an exception, we can accept 60% of size of two largest format (AO
									// landscape and A0 portrait)
									sizeselected = i;
									break;
								}
								if (i == formatwidthinmm.length - 1)
									throw new Exception(
											"Cannot display printout, as columns to display take more than 60% of largest format");
							}
						}
						logger.warning("Size decided after attribute review= " + sizeselected + " column width = "
								+ totalcolumnwidth + ", total format width = " + formatwidthinmm[sizeselected]);

						PDFDocument document = new PDFDocument();
						PDFPage page = new PDFPage(formatwidthinmm[sizeselected], formatheightinmm[sizeselected], 25,
								25, true);
						document.addPDFPart(page);

						// ---------------- write attributes header
						float currentleft = page.getPageLeft();
						float currenttop = page.getPagetop();
						float nexttop = page.getPagetop();
						for (int i = 0; i < finalattributestoshowintable.size(); i++) {
							nexttop = page.drawBoxWithLineNumber(false, currentleft, currenttop,
									currentleft + columnwidthinmm[i], 2);
							page.drawSimpleTextAt(false, currentleft, currenttop, 1, 0,
									finalattributestoshowintable.get(i));
							currentleft += columnwidthinmm[i];
						}

						float planningtop = nexttop;
						currenttop = nexttop;
						// ---------------- write table of attributes
						for (int a = 0; a < planningtoshow.getTaskNr(); a++) {
							Integer index = selectedtaskindex[a];
							if (index != null) {
								int i = index.intValue();
								GanttTask<E> thistask = planningtoshow.getTaskAt(a);
								currentleft = page.getPageLeft();
								for (int j = 0; j < finalattributestoshowintable.size(); j++) {
									String valuetoprint = thistask.getAttribute(finalattributestoshowintable.get(j));
									if (i % 5 == 4)
										page.fillBoxWithLineNumber(new java.awt.Color(248, 248, 248), currentleft,
												currenttop, currentleft + columnwidthinmm[j], 1, 1);
									nexttop = page.drawBoxWithLineNumber(false, currentleft, currenttop,
											currentleft + columnwidthinmm[j], 1);
									page.drawSimpleTextAt(false, currentleft, currenttop, 0, 0, valuetoprint);
									currentleft = currentleft + columnwidthinmm[j];
								}
								if (i % 5 == 4)
									page.fillBoxWithLineNumber(new java.awt.Color(248, 248, 248), currentleft,
											currenttop, page.getPageRight(), 1, 1);
								currenttop = nexttop;
							}
						}
						float planningbottom = nexttop;
						float planningleft = currentleft;
						float planningright = page.getPageRight();

						// -------------------- Start writing task -----------

						// lengthen the window by 5% before first task, and 5% after last task

						BusinessTimeInstant starttimedisplaywindow = businesscalendar.new BusinessTimeInstant(
								minplanningdate, true);
						BusinessTimeInstant endtimedisplaywindow = businesscalendar.new BusinessTimeInstant(
								maxplanningdate, false);
						long interval = endtimedisplaywindow.OpeningTimeInMsSince(starttimedisplaywindow);

						starttimedisplaywindow = starttimedisplaywindow.addOpeningTimeInMs(-interval * 5 / 100);
						endtimedisplaywindow = endtimedisplaywindow.addOpeningTimeInMs(+interval * 5 / 100);
						minplanningdate = starttimedisplaywindow.toDate();
						maxplanningdate = endtimedisplaywindow.toDate();

						// write graduations

						Date[] allstartsofday = DateUtils.getAllStartOfDays(minplanningdate, maxplanningdate,
								businesscalendar);
						float lasttextendinmm = 0;
						float lasthourtextendinmm = 0;

						Date sampledate1 = new Date(
								allstartsofday[0].getTime() + businesscalendar.getDaywindowhourstart() * 3600 * 1000);
						Date sampledate2 = new Date(allstartsofday[0].getTime()
								+ (businesscalendar.getDaywindowhourstart() + 1) * 3600 * 1000);

						double hourratioinmm = (DateUtils
								.genericDateToCoordinates(sampledate2, minplanningdate, maxplanningdate,
										businesscalendar)
								.getValue()
								- DateUtils.genericDateToCoordinates(sampledate1, minplanningdate, maxplanningdate,
										businesscalendar).getValue())
								* (planningright - planningleft);
						logger.warning(" hour ratio in mm = " + hourratioinmm);
						boolean showhours = true;
						if (hourratioinmm < 3)
							showhours = false;

						for (int i = 0; i < allstartsofday.length; i++) {
							Date thisstartofday = allstartsofday[i];

							double thisstartofdayratio = DateUtils.genericDateToCoordinates(thisstartofday,
									minplanningdate, maxplanningdate, businesscalendar).getValue();
							float startdayxinmm = (float) (planningleft
									+ (planningright - planningleft) * thisstartofdayratio);

							if (thisstartofdayratio > 0)
								if (thisstartofdayratio < 1) {
									page.drawLine(true, startdayxinmm, planningtop, startdayxinmm, planningbottom,
											new java.awt.Color(232, 232, 232));
									String datetext = dateformat.format(thisstartofday);
									if (startdayxinmm > lasttextendinmm) {

										float textlength = PDFPage.getTextWidth(false, datetext, true);
										lasttextendinmm = startdayxinmm + textlength;
										if (lasttextendinmm < planningright) {
											page.drawLine(true, startdayxinmm, planningtop, startdayxinmm,
													(page.getPagetop() + planningtop) / 2,
													new java.awt.Color(232, 232, 232));
											page.drawSimpleTextAt(false, startdayxinmm, page.getPagetop(), 0, 0,
													datetext, false);
										}
									}
								}
							if (showhours) {
								int hourstart = businesscalendar.getDaywindowhourstart();
								int hourend = businesscalendar.getDaywindowhourend();
								for (int j = hourstart + 1; j < hourend; j++) {
									Date hour = new Date(thisstartofday.getTime()
											+ (j - businesscalendar.getDaywindowhourstart()) * 3600 * 1000);
									double hourratio = DateUtils.genericDateToCoordinates(hour, minplanningdate,
											maxplanningdate, businesscalendar).getValue();
									if (hourratio > 0)
										if (hourratio < 1) {
											float starthourxinmm = (float) (planningleft
													+ (planningright - planningleft) * hourratio);

											page.drawLine(false, starthourxinmm, planningtop, starthourxinmm,
													planningbottom, new java.awt.Color(232, 232, 232));
											if (starthourxinmm > lasthourtextendinmm) {

												float textlength = PDFPage.getTextWidth(false, "" + j, true);
												lasthourtextendinmm = starthourxinmm + textlength;
												if (lasthourtextendinmm < planningright) {
													page.drawSimpleTextAt(false, starthourxinmm, page.getPagetop(), 1,
															0, "" + j, false);
												}
											}
										}
								}
							}
						}

						// -------------- write planning zone border --------

						page.drawBox(false, planningleft, planningtop, planningright, planningbottom);
						page.drawBox(false, planningleft, page.getPagetop(), planningright, planningtop);

						// write dependencies

						for (int i = 0; i < planningtoshow.getDependencyNumber(); i++) {
							GanttDependency<E> thisdependency = planningtoshow.getDependency(i);
							E predecessor = thisdependency.getPredecessor();
							E successor = thisdependency.getSuccessor();
							int predecessorindex = predecessor.getSequence();
							int successorindex = successor.getSequence();
							Integer predecessorselectionindex = selectedtaskindex[predecessorindex];
							Integer successorselectionindex = selectedtaskindex[successorindex];
							if ((predecessorselectionindex != null) && (successorselectionindex != null)) {
								predecessorindex = predecessorselectionindex.intValue();
								successorindex = successorselectionindex.intValue();
								boolean down = true;
								if (successorindex < predecessorindex)
									down = false;
								// draw horizontal line from end of predecessor task to start of successor task
								// + 2 mm margin

								boolean isvalid = true;
								if (predecessor.getEndtime().compareTo(minplanningdate) < 0)
									isvalid = false;
								if (successor.getStarttime().compareTo(maxplanningdate) > 0)
									isvalid = false;
								if (isvalid) {
									double predecessorendratio = DateUtils
											.genericDateToCoordinates(predecessor.getEndtime(), minplanningdate,
													maxplanningdate, businesscalendar)
											.getValue();
									double successorstartratio = DateUtils
											.genericDateToCoordinates(successor.getStarttime(), minplanningdate,
													maxplanningdate, businesscalendar)
											.getValue();
									float predecessorendxinmm = (float) (planningleft
											+ (planningright - planningleft) * predecessorendratio);
									float successorstartxinmm = (float) (planningleft
											+ (planningright - planningleft) * successorstartratio) + 2;
									float predecessormiddleyinmm = (float) (((double) predecessorindex + 0.5)
											* (planningbottom - planningtop) / (selectedtasknumber) + planningtop);
									float successortopinmm = (float) (((double) successorindex + 0.2)
											* (planningbottom - planningtop) / (selectedtasknumber) + planningtop);
									float successorbottominmm = (float) (((double) successorindex + 0.8)
											* (planningbottom - planningtop) / (selectedtasknumber) + planningtop);
									java.awt.Color dependencygrey = new java.awt.Color(155, 120, 140);
									page.drawLine(true, predecessorendxinmm, predecessormiddleyinmm,
											successorstartxinmm, predecessormiddleyinmm, dependencygrey);
									if (down) {
										page.drawLine(true, successorstartxinmm, predecessormiddleyinmm,
												successorstartxinmm, successortopinmm, dependencygrey);
										page.drawLine(false, successorstartxinmm - 1f, successortopinmm - 2.5f,
												successorstartxinmm, successortopinmm, dependencygrey);
										page.drawLine(false, successorstartxinmm + 1f, successortopinmm - 2.5f,
												successorstartxinmm, successortopinmm, dependencygrey);

									} else {
										page.drawLine(true, successorstartxinmm, successorbottominmm,
												successorstartxinmm, successorbottominmm, dependencygrey);
										page.drawLine(false, successorstartxinmm - 1f, successorbottominmm + 2.5f,
												successorstartxinmm, successorbottominmm, dependencygrey);
										page.drawLine(false, successorstartxinmm + 1f, successorbottominmm + 2.5f,
												successorstartxinmm, successorbottominmm, dependencygrey);
									}
								}
							}
						}

						// ------------------ Actually write tasks
						for (int a = 0; a < planningtoshow.getTaskNr(); a++) {
							Integer indexinselection = selectedtaskindex[a];
							if (indexinselection != null) {
								int i = indexinselection.intValue();
								GanttTask<E> thistask = planningtoshow.getTaskAt(a);
								Date startdate = thistask.getStarttime();
								Date enddate = thistask.getEndtime();

								double startratio = DateUtils.genericDateToCoordinates(startdate, minplanningdate,
										maxplanningdate, businesscalendar).getValue();
								double endratio = DateUtils.genericDateToCoordinates(enddate, minplanningdate,
										maxplanningdate, businesscalendar).getValue();

								if (startratio > endratio) {
									double oldendratio = endratio;
									endratio = startratio;
									startratio = oldendratio;
								}

								if (startratio < 0)
									startratio = 0;
								if (startratio > 1)
									startratio = 1;
								if (endratio > 1)
									endratio = 1;
								if (endratio < 0)
									endratio = 0;
								if (startratio != endratio) {
									float starttaskxinmm = (float) (planningleft
											+ (planningright - planningleft) * startratio);
									float endtaskxinmm = (float) (planningleft
											+ (planningright - planningleft) * endratio);
									float toptaskinmm = (float) (((double) i + 0.2) * (planningbottom - planningtop)
											/ (selectedtasknumber) + planningtop);
									float bottomtaskinmm = (float) (((double) i + 0.8) * (planningbottom - planningtop)
											/ (selectedtasknumber) + planningtop);

									Color color = Color.SKYBLUE;
									String attributetomapforcolor = getAttributeMappingForColor();
									if (attributetomapforcolor != null) {
										String taskattributevalue = thistask.getAttribute(attributetomapforcolor);
										Color specialcolor = getColorForAttributeValue(taskattributevalue);
										if (specialcolor != null)
											color = specialcolor;
									}

									page.fillBox(
											new java.awt.Color((int) (color.getRed() * 255),
													(int) (color.getGreen() * 255), (int) (color.getBlue() * 255)),
											starttaskxinmm, toptaskinmm, endtaskxinmm, bottomtaskinmm);
									page.drawBox(false, starttaskxinmm, toptaskinmm, endtaskxinmm, bottomtaskinmm);
									// --------------- draw dot

									if (attributemappingfordot != null) {
										String taskattributevalue = thistask.getAttribute(attributemappingfordot);
										Color specialcolor = getColorForDot(taskattributevalue);
										if (specialcolor != null) {
											logger.warning("writing dot for task " + i + " - color = " + specialcolor);
											java.awt.Color fillcolor = new java.awt.Color(
													(int) (specialcolor.getRed() * 255),
													(int) (specialcolor.getGreen() * 255),
													(int) (specialcolor.getBlue() * 255));
											page.drawCircle(java.awt.Color.BLACK, fillcolor, starttaskxinmm,
													toptaskinmm, 1);

										}
									}

									// --------------- draw Label
									String tasklabel = thistask.getAttribute(attributefortitle);
									if (endratio < 0.7) {
										page.drawSimpleTextAt(false, endtaskxinmm, (float) (((double) i)
												* (planningbottom - planningtop) / (selectedtasknumber) + planningtop),
												0, 0, tasklabel);
									} else {
										double textlength = PDFPage.getTextWidth(false, tasklabel, true);
										page.drawSimpleTextAt(false, (float) (starttaskxinmm - textlength),
												(float) (((double) i) * (planningbottom - planningtop)
														/ (selectedtasknumber) + planningtop),
												0, 0, tasklabel);
									}
								}
							}
						}

						page.closepage();

						String pdffilename = System.getProperty("java.io.tmpdir") + "ganntplanning"
								+ sdf.format(new Date()) + ".pdf";
						File pdffile = new File(pdffilename);
						pdffile.createNewFile();
						FileOutputStream tempfile = new FileOutputStream(pdffile);
						document.PrintAndSave(tempfile);
						tempfile.close();
						Desktop.getDesktop().open(pdffile);

					} catch (Throwable t) {
						logger.warning("Exception in printing gantt " + t.getClass() + " - " + t.getMessage());
						for (int i = 0; i < t.getStackTrace().length; i++)
							logger.warning("  " + t.getStackTrace()[i]);

					}

				}

			});
			nicepopup.show();
		} catch (Throwable t) {
			logger.warning("Exception in printing gantt " + t.getClass() + " - " + t.getMessage());
			for (int i = 0; i < t.getStackTrace().length; i++)
				logger.warning("  " + t.getStackTrace()[i]);

		}

	}
}
