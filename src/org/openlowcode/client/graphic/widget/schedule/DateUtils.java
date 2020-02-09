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
import java.util.Date;

/**
 * Utilities for date manipulation used in Open Lowcode schedule / GANNT
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DateUtils {

	/**
	 * A double value with a flag specifying it is out of range
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class CoordinatesWithFlag {
		private double value;
		private boolean outofrange;

		/**
		 * @return get the value
		 */
		public double getValue() {
			return value;
		}

		/**
		 * @return get the out of range flag
		 */
		public boolean isOutofrange() {
			return outofrange;
		}

		/**
		 * creates a coordinates with flag
		 * 
		 * @param value      double value
		 * @param outofrange out of range flag
		 */
		public CoordinatesWithFlag(double value, boolean outofrange) {
			super();
			this.value = value;
			this.outofrange = outofrange;
		}

	}

	/**
	 * generates the coordinates of a date inside a GANNT chart with the given start
	 * date, end date, and business calendar. This can be marked as out of range if
	 * specifying a date outside of specified business hours
	 * 
	 * @param date                   date to generate coordinates for
	 * @param startdatedisplaywindow first day shown in the GANNT chart
	 * @param enddatedisplaywindow   last day shown in the GANNT chart
	 * @param businesscalendar       business calendar
	 * @return a coordinates with a potential out of range flag
	 */
	public static CoordinatesWithFlag genericDateToCoordinates(
			Date date,
			Date startdatedisplaywindow,
			Date enddatedisplaywindow,
			BusinessCalendar businesscalendar) {
		BusinessCalendar.BusinessTimeInstant windowstart = businesscalendar.new BusinessTimeInstant(
				startdatedisplaywindow, true);
		BusinessCalendar.BusinessTimeInstant windowend = businesscalendar.new BusinessTimeInstant(enddatedisplaywindow,
				false);
		BusinessCalendar.BusinessTimeInstant dateforratop = businesscalendar.new BusinessTimeInstant(date, false);
		double windowinms = windowend.OpeningTimeInMsSince(windowstart);
		double dateinms = dateforratop.OpeningTimeInMsSince(windowstart);
		return new CoordinatesWithFlag(dateinms / windowinms, dateforratop.isInvalid());
	}

	/**
	 * gets all starts of days according to business calendars between two dates
	 * 
	 * @param startdatedisplaywindow first day shown in the display window
	 * @param enddatedisplaywindow   last day shown in thedisplay window
	 * @param businesscalendar       business calendar used
	 * @return a list of all relevant starts of business days
	 */
	public static Date[] getAllStartOfDays(
			Date startdatedisplaywindow,
			Date enddatedisplaywindow,
			BusinessCalendar businesscalendar) {
		BusinessCalendar.BusinessTimeInstant windowstart = businesscalendar.new BusinessTimeInstant(
				startdatedisplaywindow, true);
		BusinessCalendar.BusinessTimeInstant windowend = businesscalendar.new BusinessTimeInstant(enddatedisplaywindow,
				false);
		ArrayList<Date> allstartofdays = new ArrayList<Date>();
		BusinessCalendar.BusinessTimeInstant cursor = windowstart.getStartOfDay();
		int circuitbreaker = 0;
		while (cursor.compareTo(windowend) < 0) {

			if (circuitbreaker > 20000)
				throw new RuntimeException("Circuit Breaker exceeded");
			allstartofdays.add(cursor.toDate());
			cursor = cursor.getNextStartOfDay();
			circuitbreaker++;
		}
		return allstartofdays.toArray(new Date[0]);
	}

}
