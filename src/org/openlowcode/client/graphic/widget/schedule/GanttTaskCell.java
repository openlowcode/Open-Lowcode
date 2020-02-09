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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.schedule.DateUtils.CoordinatesWithFlag;
import org.openlowcode.client.runtime.PageActionManager;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * the graphical component of a Gantt task. It is implemented as a list cell for
 * performance purposes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of Gantt task
 */
public class GanttTaskCell<E extends GanttTask<E>>
		extends
		ListCell<GanttTaskDisplay<E>> {

	private static Logger logger = Logger.getLogger(GanttTaskCell.class.getName());
	public static Color SCALE_GRAY = Color.rgb(193, 193, 199);
	public static Color SCALE_LIGHT_GRAY = Color.rgb(212, 212, 217);
	private GanttTaskDisplay<E> display;
	private GanttCanvas canvas;
	private double minxforclick;
	private double maxxforclick;
	private double minyforclick;
	private double maxyforclick;

	/**
	 * @return get hte parent gantt display widget
	 */
	public GanttDisplay<E> getParentGanttDisplay() {
		return display.getGanttDisplay();
	}

	/**
	 * creates a gannt task cell with the Open Lowcode client action manager as
	 * parent
	 * 
	 * @param actionmanager parent action manager
	 */
	public GanttTaskCell(PageActionManager actionmanager) {

		setStyle("-fx-padding: 0px;");
		canvas = new GanttCanvas();
		canvas.widthProperty().bind(widthProperty());
		canvas.heightProperty().bind(heightProperty());
		StackPane pane = new StackPane();
		pane.getChildren().addAll(canvas);
		setGraphic(pane);
		setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

		this.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (display != null) {
					logger.finer("Click On Item " + display.getGantttask().getStarttime());
					Bounds bounds = localToScene(getBoundsInLocal());
					double xcoordinates = event.getSceneX() - bounds.getMinX();
					double ycoordinates = event.getSceneY() - bounds.getMinY();

					logger.finer("X,Y coordinates = " + xcoordinates + " - " + ycoordinates);
					logger.finer("Box X (" + minxforclick + "-" + maxxforclick + ") Box Y (" + minyforclick + "-"
							+ maxyforclick + ")");
					if ((xcoordinates >= minxforclick) && (xcoordinates <= maxxforclick)
							&& (ycoordinates >= minyforclick) && (ycoordinates <= maxyforclick)) {
						logger.finer("Impact");
						GanttTaskMouseEventHandler<
								E> handlerontask = display.getGanttDisplay().getEventHandlerOnGanttTaskMouseClicked();
						if (handlerontask != null)
							handlerontask.handle(event, display.getGantttask());
					} else {
						logger.finer("Shoot outside");
						if (event.getButton().equals(MouseButton.SECONDARY)) {
							display.getGanttDisplay().printGantt(actionmanager, event);
						} else {
							EventHandler<MouseEvent> handleroutside = display.getGanttDisplay()
									.getEventHandlerOnClickOutsideOfGanttTask();
							if (handleroutside != null)
								handleroutside.handle(event);
						}
					}
				}
			}

		});
	}

	@Override
	protected void updateItem(GanttTaskDisplay<E> display, boolean empty) {
		this.display = display;
		canvas.setDisplay(display);
		canvas.draw();
	}

	private class GanttCanvas
			extends
			Canvas {

		@SuppressWarnings("unused")
		private String name;
		private Date startdate;
		private Date enddate;
		private Date displaywindowstart;
		private Date displaywindowend;
		private boolean hasdata;
		private String tasktitle;

		private void setDisplay(GanttTaskDisplay<E> display) {
			if (display == null) {
				hasdata = false;
			} else {
				this.startdate = display.getGantttask().getStarttime();
				this.enddate = display.getGantttask().getEndtime();
				if (display.getGanttDisplay().getAttributefortitle() != null) {
					tasktitle = display.getGantttask().getAttribute(display.getGanttDisplay().getAttributefortitle());
				}
				this.displaywindowstart = display.getGanttDisplay().getStartdatedisplaywindow();
				this.displaywindowend = display.getGanttDisplay().getEnddatedisplaywindow();

				hasdata = true;

			}

		}

		private GanttCanvas() {
			widthProperty().addListener(event -> draw());
			heightProperty().addListener(event -> draw());
			widthProperty().addListener(new ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
					if (display != null)
						if (display.getGanttDisplay() != null)
							display.getGanttDisplay().redrawTitleIfChanged(newValue);
				}

			});
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
			return getHeight();
		}

		private CoordinatesWithFlag dateToCoordinates(Date date) {
			return DateUtils.genericDateToCoordinates(date, displaywindowstart, displaywindowend,
					display.getGanttDisplay().getBusinessCalendar());

		}

		private boolean isInDisplayWindow(double startratio, double endratio) {
			if ((startratio >= 0) && (startratio <= 1))
				return true;
			if ((endratio >= 0) && (endratio <= 1))
				return true;
			return false;
		}

		/*
		 * Draw a chart based on the data provided by the model.
		 */
		private void draw() {

			GraphicsContext gc = getGraphicsContext2D();

			gc.setFill(Color.WHITE);

			gc.fillRect(0, 0, getWidth(), getHeight());

			if (hasdata) {

				// Step 1 - strikes here separator

				GanttTaskCell.drawSeparators(gc, displaywindowstart, displaywindowend, 0, 0, getHeight(), getWidth(),
						display.getGanttDisplay().getBusinessCalendar(), 0);

				// Step 2A - strikes first separator on passing dependencies;
				CoordinatesWithFlag startratiowithflag = dateToCoordinates(startdate);
				CoordinatesWithFlag endratiowithflag = dateToCoordinates(enddate);

				gc.setStroke(Color.web("#8A7BBE"));
				gc.setLineWidth(1);
				gc.setLineDashes(0);

				ArrayList<GanttDependency<E>> dependenciesinbetween = display.getGanttDisplay().getPlanningtoshow()
						.getDependenciesInBetween(display.getGantttask().getSequenceInPlanning());

				if (dependenciesinbetween != null)
					for (int i = 0; i < dependenciesinbetween.size(); i++) {
						GanttDependency<E> dependency = dependenciesinbetween.get(i);
						CoordinatesWithFlag dependencyverticalratio = dateToCoordinates(
								dependency.getSuccessor().getStarttime());
						boolean validwithmargin = true;
						if (dependencyverticalratio.getValue() < -0.2)
							validwithmargin = false;
						if (dependencyverticalratio.getValue() > 1.2)
							validwithmargin = false;
						if (validwithmargin) {
							gc.strokeLine((long) (dependencyverticalratio.getValue() * getWidth() + 4), 0,
									(long) (dependencyverticalratio.getValue() * getWidth() + 4), (long) (getHeight()));
						}
					}

				double startratio = startratiowithflag.getValue();
				double endratio = endratiowithflag.getValue();

				// Step 2B - draw dependency on predecessor

				ArrayList<GanttDependency<E>> dependenciesfortaskaspredecessor = display.getGanttDisplay()
						.getPlanningtoshow()
						.getDependenciesByPredecessor(display.getGantttask().getSequenceInPlanning());
				double minpredecessordependratio = endratio;
				double maxpredecessordependratio = endratio;
				boolean predecessordependencyexists = false;
				boolean maxtouched = false;
				boolean mintouched = false;
				if (dependenciesfortaskaspredecessor != null)
					for (int i = 0; i < dependenciesfortaskaspredecessor.size(); i++) {
						GanttDependency<E> dependency = dependenciesfortaskaspredecessor.get(i);
						int thisindex = display.getGantttask().getSequenceInPlanning();
						int successorindex = dependency.getSuccessor().getSequenceInPlanning();
						double successorstarttratio = dateToCoordinates(dependency.getSuccessor().getStarttime())
								.getValue();
						if (successorstarttratio > maxpredecessordependratio) {
							maxpredecessordependratio = successorstarttratio;
							maxtouched = true;
						}
						if (successorstarttratio < minpredecessordependratio) {
							minpredecessordependratio = successorstarttratio;
							mintouched = true;
						}
						predecessordependencyexists = true;
						// arrow will go up
						if (thisindex > successorindex) {
							gc.strokeLine((long) (successorstarttratio * getWidth() + 4), (long) (getHeight() / 2),
									(long) (successorstarttratio * getWidth() + 4), 0);

						}
						// arrow will go down
						if (thisindex < successorindex) {
							gc.strokeLine((long) (successorstarttratio * getWidth() + 4), (long) (getHeight() / 2),
									(long) (successorstarttratio * getWidth() + 4), (long) (getHeight()));

						}
					}
				if (predecessordependencyexists) {

					gc.strokeLine((long) (minpredecessordependratio * getWidth() + (mintouched ? 4 : 0)),
							(long) (getHeight() / 2),
							(long) (maxpredecessordependratio * getWidth() + (maxtouched ? 4 : 0)),
							(long) (getHeight() / 2));

				}

				// Step 2C - draw dependency on successor

				ArrayList<GanttDependency<E>> dependenciesfortaskassuccessor = display.getGanttDisplay()
						.getPlanningtoshow().getDependenciesBySuccessor(display.getGantttask().getSequenceInPlanning());
				boolean hastoparrow = false;
				boolean hasbottomarrow = false;
				if (dependenciesfortaskassuccessor != null)
					for (int i = 0; i < dependenciesfortaskassuccessor.size(); i++) {
						GanttDependency<E> dependency = dependenciesfortaskassuccessor.get(i);
						int thisindex = display.getGantttask().getSequenceInPlanning();
						int predecessorindex = dependency.getPredecessor().getSequenceInPlanning();
						if (thisindex > predecessorindex)
							hastoparrow = true;
						if (thisindex < predecessorindex)
							hasbottomarrow = true;
						if (thisindex == predecessorindex)
							logger.warning("  -- found inconsistent dependency " + thisindex
									+ (display.getGanttDisplay().getAttributefortitle() != null
											? " this = "
													+ display.getGantttask().getAttribute(
															display.getGanttDisplay().getAttributefortitle())
													+ ", precedessor = "
													+ dependency.getPredecessor().getAttribute(
															display.getGanttDisplay().getAttributefortitle())
											: " NO LABEL"));
						logger.fine(" Found dependency for this task as successor");
					}
				if (hastoparrow) {
					logger.fine("  - write top arrow for start arrow " + startratio);
					gc.strokeLine((long) (startratio * getWidth() + 4), 0, (long) (startratio * getWidth() + 4),
							(long) (getHeight() * 0.2));
					gc.strokeLine((long) (startratio * getWidth() + 3), 0, (long) (startratio * getWidth() + 4),
							(long) (getHeight() * 0.2));
					gc.strokeLine((long) (startratio * getWidth() + 5), 0, (long) (startratio * getWidth() + 4),
							(long) (getHeight() * 0.2));
				}
				if (hasbottomarrow) {
					logger.fine("  - write bottom arrow for start arrow " + startratio);

					gc.strokeLine((long) (startratio * getWidth() + 4), (long) (getHeight()),
							(long) (startratio * getWidth() + 4), (long) (getHeight() * 0.8));
					gc.strokeLine((long) (startratio * getWidth() + 3), (long) (getHeight()),
							(long) (startratio * getWidth() + 4), (long) (getHeight() * 0.8));
					gc.strokeLine((long) (startratio * getWidth() + 5), (long) (getHeight()),
							(long) (startratio * getWidth() + 4), (long) (getHeight() * 0.8));
				}

				// Next Step - draw task itself

				boolean valid = true;
				if (startratiowithflag.isOutofrange())
					valid = false;
				if (endratiowithflag.isOutofrange())
					valid = false;
				gc.setLineWidth(0.5);
				Font font = Font.font(12);
				gc.setFont(font);
				gc.setStroke(Color.rgb(255, 255, 255, 0.7));
				gc.setFill(Color.rgb(255, 255, 255, 0.7));
				double maxratio = endratio;
				double minratio = startratio;
				if (startratio > endratio) {
					maxratio = startratio;
					minratio = endratio;
				}

				if (tasktitle != null) {
					if (maxratio > 0.7) {
						Text label = new Text(tasktitle);
						label.setFont(font);
						double labelwidth = label.getBoundsInLocal().getWidth();
						long starttext = (long) (minratio * getWidth() - labelwidth - 5);
						gc.fillText(tasktitle, starttext - 1, 14);
						gc.fillText(tasktitle, starttext + 1, 14);

					} else {
						gc.fillText(tasktitle, (long) (maxratio * getWidth() + 5 - 1), 14);
						gc.fillText(tasktitle, (long) (maxratio * getWidth() + 5 + 1), 14);

					}
				}
				gc.setStroke(Color.rgb(0, 0, 0, 1));
				gc.setFill(Color.rgb(0, 0, 0, 1));

				if (tasktitle != null) {

					if (maxratio > 0.7) {
						Text label = new Text(tasktitle);
						label.setFont(font);
						double labelwidth = label.getBoundsInLocal().getWidth();
						long starttext = (long) (minratio * getWidth() - labelwidth - 5);
						gc.fillText(tasktitle, starttext, 14);

					} else {
						gc.fillText(tasktitle, (long) (maxratio * getWidth() + 5), 14);

					}
				}

				if (isInDisplayWindow(startratio, endratio)) {
					if (startratio < 0)
						startratio = 0;
					if (startratio > 1)
						startratio = 1;
					if (endratio < 0)
						endratio = 0;
					if (endratio > 1)
						endratio = 1;
					Color color = Color.SKYBLUE;
					String attributetomapforcolor = display.getGanttDisplay().getAttributeMappingForColor();
					if (attributetomapforcolor != null) {
						String taskattributevalue = display.getGantttask().getAttribute(attributetomapforcolor);
						Color specialcolor = display.getGanttDisplay().getColorForAttributeValue(taskattributevalue);
						if (specialcolor != null)
							color = specialcolor;
					}

					Stop[] stops = new Stop[] { new Stop(0, color), new Stop(1, color.darker().darker()) };
					LinearGradient gradient = new LinearGradient(0, 0, 0, 300, false, CycleMethod.NO_CYCLE, stops);

					gc.setFill(gradient);

					if (valid) {
						gc.setStroke(Color.BLACK);
					} else {
						gc.setStroke(Color.RED);
					}
					gc.setLineWidth(0.5);

					boolean small = false;
					double lengthinpc = Math.abs(startratio - endratio);
					if (lengthinpc < 0.002)
						small = true;

					if (small) {

						double centerx = startratio * getWidth();
						double centery = getHeight() * 0.5;
						double diamondradius = getHeight() * 0.2;
						minxforclick = centerx - diamondradius;
						maxxforclick = centerx + diamondradius;
						minyforclick = centery - diamondradius;
						maxyforclick = centery + diamondradius;
						double[] polyx = new double[] { centerx - diamondradius, centerx, centerx + diamondradius,
								centerx };
						double[] polyy = new double[] { centery, centery - diamondradius, centery,
								centery + diamondradius };
						gc.fillPolygon(polyx, polyy, 4);
						gc.strokePolygon(polyx, polyy, 4);

					} else {
						if (endratio > startratio) {
							gc.fillRoundRect((long) (startratio * getWidth()), (long) (getHeight() * 0.2),
									(long) ((endratio - startratio) * getWidth()), (long) (getHeight() * 0.6), 5, 5);

							gc.strokeRoundRect((long) (startratio * getWidth()), (long) (getHeight() * 0.2),
									(long) ((endratio - startratio) * getWidth()), (long) (getHeight() * 0.6), 5, 5);
							minxforclick = startratio * getWidth();
							maxxforclick = endratio * getWidth();
							minyforclick = getHeight() * 0.2;
							maxyforclick = getHeight() * 0.8;
						} else {
							double oldendratio = endratio;
							endratio = startratio;
							startratio = oldendratio;
							gc.setStroke(Color.RED);
							gc.fillRoundRect((long) (startratio * getWidth()), (long) (getHeight() * 0.2),
									(long) ((endratio - startratio) * getWidth()), (long) (getHeight() * 0.6), 5, 5);
							gc.strokeRoundRect((long) (startratio * getWidth()), (long) (getHeight() * 0.2),
									(long) ((endratio - startratio) * getWidth()), (long) (getHeight() * 0.6), 5, 5);
							minxforclick = startratio * getWidth();
							maxxforclick = endratio * getWidth();
							minyforclick = getHeight() * 0.2;
							maxyforclick = getHeight() * 0.8;

						}
					}

					// build color dot if required
					String attributetomapfordot = display.getGanttDisplay().getAttributeMappingForDot();
					if (attributetomapfordot != null) {
						String taskattributevalue = display.getGantttask().getAttribute(attributetomapfordot);
						Color specialcolor = display.getGanttDisplay().getColorForDot(taskattributevalue);
						if (specialcolor != null) {
							long circlecenterx = (long) (startratio * getWidth());
							long circlecentery = (long) (getHeight() * 0.2);
							gc.setFill(specialcolor);
							gc.setStroke(Color.BLACK);
							gc.fillOval(circlecenterx - 2, circlecentery - 2, 5, 5);
							gc.strokeOval(circlecenterx - 2, circlecentery - 2, 5, 5);

						}
					}
				}

			}
		}
	}

	/**
	 * true if the date is a monday
	 * 
	 * @param date date to analyze
	 * @return true if the date is a monday
	 */
	public static boolean isMonday(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
			return true;
		return false;
	}

	/**
	 * return true if lines should not be drawn for hours in the scales of the
	 * planning
	 * 
	 * @param startsofdates days of start of days
	 * @return true id reduced display (less than 30 days shown)
	 */
	public static boolean isReducedDisplay(Date[] startsofdates) {
		if (startsofdates.length > 30)
			return true;
		return false;
	}

	/**
	 * draw all separators in the gantt task cell
	 * 
	 * @param gc                     graphics context
	 * @param startdatedisplaywindow start date (first day) of the display window
	 * @param enddatedisplaywindow   end date (last day) of the display window
	 * @param ystarthour             start hour of the planning
	 * @param ystartday              start day of the planning
	 * @param yend                   end of the cell in pixel
	 * @param totalwidth             total width in pixel
	 * @param businesscalendar       business calendar to use for displaying of
	 *                               opening hours
	 * @param extraxoffset           extra offset in the display
	 */
	public static void drawSeparators(
			GraphicsContext gc,
			Date startdatedisplaywindow,
			Date enddatedisplaywindow,
			double ystarthour,
			double ystartday,
			double yend,
			double totalwidth,
			BusinessCalendar businesscalendar,
			float extraxoffset) {
		Date[] separatorstoconsider = DateUtils.getAllStartOfDays(startdatedisplaywindow, enddatedisplaywindow,
				businesscalendar);
		boolean isreduceddisplay = isReducedDisplay(separatorstoconsider);

		for (int i = 0; i < separatorstoconsider.length; i++) {
			if (isreduceddisplay) {
				gc.setLineWidth(0.5);

				gc.setStroke(GanttTaskCell.SCALE_LIGHT_GRAY);
			}
			if (!isreduceddisplay) {
				gc.setLineWidth(1);

				gc.setStroke(GanttTaskCell.SCALE_GRAY);
			}

			gc.setEffect(null);
			Date separatortoprint = separatorstoconsider[i];
			if (isMonday(separatortoprint)) {
				gc.setStroke(GanttTaskCell.SCALE_GRAY);
				if (!isreduceddisplay)
					gc.setLineWidth(2);
				if (isreduceddisplay)
					gc.setLineWidth(1);

			}
			double separatorratio = DateUtils.genericDateToCoordinates(separatortoprint, startdatedisplaywindow,
					enddatedisplaywindow, businesscalendar).getValue();
			gc.strokeLine((long) (separatorratio * totalwidth + extraxoffset), (long) ystartday,
					(long) (separatorratio * totalwidth + extraxoffset), (long) (yend));
			if (!isreduceddisplay)
				if (separatorstoconsider.length < 30) {
					for (int j = businesscalendar.getDaywindowhourstart() + 1; j < businesscalendar
							.getDaywindowhourend(); j++) {
						Date hour = new Date(separatortoprint.getTime()
								+ (j - businesscalendar.getDaywindowhourstart()) * 3600 * 1000);
						double hourratio = DateUtils.genericDateToCoordinates(hour, startdatedisplaywindow,
								enddatedisplaywindow, businesscalendar).getValue();
						gc.setStroke(GanttTaskCell.SCALE_LIGHT_GRAY);
						gc.setLineWidth(0.5);

						gc.setEffect(null);
						gc.strokeLine((long) (hourratio * totalwidth + extraxoffset), (long) (ystarthour),
								(long) (hourratio * totalwidth + extraxoffset), (long) (yend));

					}
				}
		}

	}
}
