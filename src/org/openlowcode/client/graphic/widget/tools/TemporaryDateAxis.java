/*
 * The MIT License (MIT) 
 * 
 * Original work Copyright (c) 2013, Christian Schudt  
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal 
 * in the Software without restriction, including without limitation the rights 
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell 
 * copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions: 
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software. 
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 * THE SOFTWARE. 
 */

/********************************************************************************
 * Modified work Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.tools;

import com.sun.javafx.charts.ChartLayoutAnimator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.chart.Axis;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * An axis that displays date and time values. it is a temporary implementation
 * pending for a date axis to be integrated in javafx. 
 */

public final class TemporaryDateAxis
		extends
		Axis<Date> {

	/**
	 * These property are used for animation.
	 */
	private final LongProperty currentLowerBound = new SimpleLongProperty(this, "currentLowerBound");

	private final LongProperty currentUpperBound = new SimpleLongProperty(this, "currentUpperBound");

	private final ObjectProperty<
			StringConverter<Date>> ticklabelstringconverter = new ObjectPropertyBase<StringConverter<Date>>() {
				@Override
				protected void invalidated() {
					if (!isAutoRanging()) {
						invalidateRange();
						requestAxisLayout();
					}
				}

				@Override
				public Object getBean() {
					return TemporaryDateAxis.this;
				}

				@Override
				public String getName() {
					return "tickLabelFormatter";
				}
			};

	/**
	 * Stores the min and max date of the list of dates which is used. If
	 * autoranging is true, these values are used as lower and upper bounds.
	 */
	private Date minDate, maxDate;

	private ObjectProperty<Date> lowerBound = new ObjectPropertyBase<Date>() {
		@Override
		protected void invalidated() {
			if (!isAutoRanging()) {
				invalidateRange();
				requestAxisLayout();
			}
		}

		@Override
		public Object getBean() {
			return TemporaryDateAxis.this;
		}

		@Override
		public String getName() {
			return "lowerBound";
		}
	};

	private ObjectProperty<Date> upperBound = new ObjectPropertyBase<Date>() {
		@Override
		protected void invalidated() {
			if (!isAutoRanging()) {
				invalidateRange();
				requestAxisLayout();
			}
		}

		@Override
		public Object getBean() {
			return TemporaryDateAxis.this;
		}

		@Override
		public String getName() {
			return "upperBound";
		}
	};

	private ChartLayoutAnimator animator = new ChartLayoutAnimator(this);

	private Object currentAnimationID;

	private TimeInterval actualInterval = TimeInterval.DECADE;

	/**
	 * Default constructor. By default the lower and upper bound are calculated by
	 * the data.
	 */
	public TemporaryDateAxis() {
	}

	/**
	 * Constructs a date axis with fix lower and upper bounds.
	 * 
	 * @param lowerBound The lower bound.
	 * @param upperBound The upper bound.
	 */
	public TemporaryDateAxis(Date lowerBound, Date upperBound) {
		this();
		setAutoRanging(false);
		this.lowerBound.set(lowerBound);
		this.upperBound.set(upperBound);
	}

	/**
	 * Constructs a date axis with a label and fix lower and upper bounds.
	 * 
	 * @param xaxislabel The label for the axis.
	 * @param lowerBound The lower bound.
	 * @param upperBound The upper bound.
	 */
	public TemporaryDateAxis(String xaxislabel, Date lowerBound, Date upperBound) {
		this(lowerBound, upperBound);
		setLabel(xaxislabel);
	}

	@Override
	public void invalidateRange(List<Date> list) {
		super.invalidateRange(list);

		Collections.sort(list);
		if (list.isEmpty()) {
			minDate = maxDate = new Date();
		} else if (list.size() == 1) {
			minDate = maxDate = list.get(0);
		} else if (list.size() > 1) {
			minDate = list.get(0);
			maxDate = list.get(list.size() - 1);
		}
	}

	@Override
	protected Object autoRange(double length) {
		if (isAutoRanging()) {
			return new Object[] { minDate, maxDate };
		} else {
			if (getLowerBound() == null || getUpperBound() == null) {
				throw new IllegalArgumentException("If autoRanging is false, a lower and upper bound must be set.");
			}
			return getRange();
		}
	}

	@Override
	protected void setRange(Object range, boolean animating) {
		Object[] r = (Object[]) range;
		Date oldLowerBound = getLowerBound();
		Date oldUpperBound = getUpperBound();
		Date lower = (Date) r[0];
		Date upper = (Date) r[1];
		lowerBound.set(lower);
		upperBound.set(upper);

		if (animating) {

			animator.stop(currentAnimationID);
			currentAnimationID = animator.animate(
					new KeyFrame(Duration.ZERO, new KeyValue(currentLowerBound, oldLowerBound.getTime()),
							new KeyValue(currentUpperBound, oldUpperBound.getTime())),
					new KeyFrame(Duration.millis(700), new KeyValue(currentLowerBound, lower.getTime()),
							new KeyValue(currentUpperBound, upper.getTime())));

		} else {
			currentLowerBound.set(getLowerBound().getTime());
			currentUpperBound.set(getUpperBound().getTime());
		}
	}

	@Override
	protected Object getRange() {
		return new Object[] { getLowerBound(), getUpperBound() };
	}

	@Override
	public double getZeroPosition() {
		return 0;
	}

	@Override
	public double getDisplayPosition(Date date) {
		final double length = getSide().isHorizontal() ? getWidth() : getHeight();

		// Get the difference between the max and min date.
		double diff = currentUpperBound.get() - currentLowerBound.get();

		// Get the actual range of the visible area.
		// The minimal date should start at the zero position, that's why we subtract
		// it.
		double range = length - getZeroPosition();

		// Then get the difference from the actual date to the min date and divide it by
		// the total difference.
		// We get a value between 0 and 1, if the date is within the min and max date.
		double d = (date.getTime() - currentLowerBound.get()) / diff;

		// Multiply this percent value with the range and add the zero offset.
		if (getSide().isVertical()) {
			return getHeight() - d * range + getZeroPosition();
		} else {
			return d * range + getZeroPosition();
		}
	}

	@Override
	public Date getValueForDisplay(double displayPosition) {
		final double length = getSide().isHorizontal() ? getWidth() : getHeight();

		// Get the difference between the max and min date.
		double diff = currentUpperBound.get() - currentLowerBound.get();

		// Get the actual range of the visible area.
		// The minimal date should start at the zero position, that's why we subtract
		// it.
		double range = length - getZeroPosition();

		if (getSide().isVertical()) {
			// displayPosition = getHeight() - ((date - lowerBound) / diff) * range +
			// getZero
			// date = displayPosition - getZero - getHeight())/range * diff + lowerBound
			return new Date((long) ((displayPosition - getZeroPosition() - getHeight()) / -range * diff
					+ currentLowerBound.get()));
		} else {
			// displayPosition = ((date - lowerBound) / diff) * range + getZero
			// date = displayPosition - getZero)/range * diff + lowerBound
			return new Date((long) ((displayPosition - getZeroPosition()) / range * diff + currentLowerBound.get()));
		}
	}

	@Override
	public boolean isValueOnAxis(Date date) {
		return date.getTime() > currentLowerBound.get() && date.getTime() < currentUpperBound.get();
	}

	@Override
	public double toNumericValue(Date date) {
		return date.getTime();
	}

	@Override
	public Date toRealValue(double v) {
		return new Date((long) v);
	}

	@Override
	protected List<Date> calculateTickValues(double v, Object range) {
		Object[] r = (Object[]) range;
		Date lower = (Date) r[0];
		Date upper = (Date) r[1];

		List<Date> dateList = new ArrayList<Date>();
		Calendar calendar = Calendar.getInstance();

		// The preferred gap which should be between two tick marks.
		double averageTickGap = 100;
		double averageTicks = v / averageTickGap;

		List<Date> previousDateList = new ArrayList<Date>();

		TimeInterval previousInterval = TimeInterval.values()[0];

		// Starting with the greatest interval, add one of each calendar unit.
		for (TimeInterval interval : TimeInterval.values()) {
			// Reset the calendar.
			calendar.setTime(lower);
			// Clear the list.
			dateList.clear();
			previousDateList.clear();
			actualInterval = interval;

			// Loop as long we exceeded the upper bound.
			while (calendar.getTime().getTime() <= upper.getTime()) {
				dateList.add(calendar.getTime());
				calendar.add(interval.interval, interval.amount);
			}
			// Then check the size of the list. If it is greater than the amount of ticks,
			// take that list.
			if (dateList.size() > averageTicks) {
				calendar.setTime(lower);
				// Recheck if the previous interval is better suited.
				while (calendar.getTime().getTime() <= upper.getTime()) {
					previousDateList.add(calendar.getTime());
					calendar.add(previousInterval.interval, previousInterval.amount);
				}
				break;
			}

			previousInterval = interval;
		}
		if (previousDateList.size() - averageTicks > averageTicks - dateList.size()) {
			dateList = previousDateList;
			actualInterval = previousInterval;
		}

		// At last add the upper bound.
		dateList.add(upper);

		List<Date> evenDateList = respectTimePeriods(dateList, calendar);
		// If there are at least three dates, check if the gap between the lower date
		// and the second date is at least half the gap of the second and third date.
		// Do the same for the upper bound.
		// If gaps between dates are to small, remove one of them.
		// This can occur, e.g. if the lower bound is 25.12.2013 and years are shown.
		// Then the next year shown would be 2014 (01.01.2014) which would be too narrow
		// to 25.12.2013.
		if (evenDateList.size() > 2) {

			Date secondDate = evenDateList.get(1);
			Date thirdDate = evenDateList.get(2);
			Date lastDate = evenDateList.get(dateList.size() - 2);
			Date previousLastDate = evenDateList.get(dateList.size() - 3);

			// If the second date is too near by the lower bound, remove it.
			if (secondDate.getTime() - lower.getTime() < (thirdDate.getTime() - secondDate.getTime()) / 2) {
				evenDateList.remove(secondDate);
			}

			// If difference from the upper bound to the last date is less than the half of
			// the difference of the previous two dates,
			// we better remove the last date, as it comes to close to the upper bound.
			if (upper.getTime() - lastDate.getTime() < (lastDate.getTime() - previousLastDate.getTime()) / 2) {
				evenDateList.remove(lastDate);
			}
		}

		return evenDateList;
	}

	@Override
	protected void layoutChildren() {
		if (!isAutoRanging()) {
			currentLowerBound.set(getLowerBound().getTime());
			currentUpperBound.set(getUpperBound().getTime());
		}
		super.layoutChildren();
	}

	@Override
	protected String getTickMarkLabel(Date date) {

		StringConverter<Date> converter = ticklabelstringconverter.getValue();
		if (converter != null) {
			return converter.toString(date);
		}

		DateFormat dateFormat;
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);

		if (actualInterval.interval == Calendar.YEAR && calendar.get(Calendar.MONTH) == 0
				&& calendar.get(Calendar.DATE) == 1) {
			dateFormat = new SimpleDateFormat("yyyy");
		} else if (actualInterval.interval == Calendar.MONTH && calendar.get(Calendar.DATE) == 1) {
			dateFormat = new SimpleDateFormat("MMM yy");
		} else {
			switch (actualInterval.interval) {
			case Calendar.DATE:
			case Calendar.WEEK_OF_YEAR:
			default:
				dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
				break;
			case Calendar.HOUR:
			case Calendar.MINUTE:
				dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
				break;
			case Calendar.SECOND:
				dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM);
				break;
			case Calendar.MILLISECOND:
				dateFormat = DateFormat.getTimeInstance(DateFormat.FULL);
				break;
			}
		}
		return dateFormat.format(date);
	}

	/**
	 * make dates start at sensible time: years always begin in January, months
	 * always begin on the 1st and days always at midnight.
	 * 
	 * @param dates The list of dates.
	 * @return The new list of dates.
	 */
	private List<Date> respectTimePeriods(List<Date> dates, Calendar calendar) {
		// make intermediate dates significant dates (first day of the month...)
		if (dates.size() > 2) {
			List<Date> niceDates = new ArrayList<Date>();

			// For each interval, modify the date slightly by a few millis, to make sure
			// they are different days.
			// This is because Axis stores each value and won't update the tick labels, if
			// the value is already known.
			// This happens if you display days and then add a date many years in the future
			// the tick label will still be displayed as day.
			for (int i = 0; i < dates.size(); i++) {
				calendar.setTime(dates.get(i));
				switch (actualInterval.interval) {
				case Calendar.YEAR:
					// If its not the first or last date (lower and upper bound), make the year
					// begin with first month and let the months begin with first day.
					if (i != 0 && i != dates.size() - 1) {
						calendar.set(Calendar.MONTH, 0);
						calendar.set(Calendar.DATE, 1);
					}
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND,10);
					break;
				case Calendar.MONTH:
					// If its not the first or last date (lower and upper bound), make the months
					// begin with first day.
					if (i != 0 && i != dates.size() - 1) {
						calendar.set(Calendar.DATE, 1);
					}
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 5);
					break;
				case Calendar.WEEK_OF_YEAR:
					// Make weeks begin with first day of week?
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 4);
					break;
				case Calendar.DATE:
					calendar.set(Calendar.HOUR_OF_DAY, 0);
					calendar.set(Calendar.MINUTE, 0);
					calendar.set(Calendar.SECOND, 0);
					calendar.set(Calendar.MILLISECOND, 3);
					break;
				case Calendar.HOUR:
					if (i != 0 && i != dates.size() - 1) {
						calendar.set(Calendar.MINUTE, 0);
						calendar.set(Calendar.SECOND, 0);
					}
					calendar.set(Calendar.MILLISECOND, 2);
					break;
				case Calendar.MINUTE:
					if (i != 0 && i != dates.size() - 1) {
						calendar.set(Calendar.SECOND, 0);
					}
					calendar.set(Calendar.MILLISECOND, 1);
					break;
				case Calendar.SECOND:
					calendar.set(Calendar.MILLISECOND, 0);
					break;

				}
				niceDates.add(calendar.getTime());
			}

			return niceDates;
		} else {
			return dates;
		}
	}

	/**
	 * Gets the lower bound of the axis.
	 * 
	 * @return The lower bound.
	 */
	public final Date getLowerBound() {
		return lowerBound.get();
	}

	/**
	 * Gets the upper bound of the axis.
	 * 
	 * @return The upper bound.
	 */
	public final Date getUpperBound() {
		return upperBound.get();
	}

	/**
	 * The intervals, which are used for the tick labels. Beginning with the largest
	 * interval, the axis tries to calculate the tick values for this interval. If a
	 * smaller interval is better suited for, that one is taken.
	 */
	private enum TimeInterval {
		DECADE(Calendar.YEAR, 10), YEAR(Calendar.YEAR, 1), QUARTER(Calendar.MONTH, 3), MONTH(Calendar.MONTH, 1),
		WEEK(Calendar.WEEK_OF_YEAR, 1), DAY(Calendar.DATE, 1), HOURS4(Calendar.HOUR, 4), HOUR(Calendar.HOUR, 1),
		MINUTE_15(Calendar.MINUTE, 15), MINUTE_5(Calendar.MINUTE, 5), MINUTE_1(Calendar.MINUTE, 1),
		SECOND_15(Calendar.SECOND, 15), SECOND_1(Calendar.SECOND, 1), MILLISECOND(Calendar.MILLISECOND, 1);

		private final int amount;

		private final int interval;

		private TimeInterval(int interval, int amount) {
			this.interval = interval;
			this.amount = amount;
		}
	}
}