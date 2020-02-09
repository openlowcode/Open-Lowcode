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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

/**
 * A business calendar defines the opening period for business that will be
 * shown in the GANTT. It is defined typically by opening hours (e.g. 9 (AM) to
 * 18 (6PM)) and days of week that are open for business (e.g. monday to
 * friday). By default, it shows all days of week 24/24.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 *
 */
public class BusinessCalendar {

	private int daywindowhourstart;

	/**
	 * @return the first hour shown in a GANNT planning using this business calendar
	 */
	public int getDaywindowhourstart() {
		return daywindowhourstart;
	}

	/**
	 * @return the last hour shown in a GANNT planning using this business calendar
	 */
	public int getDaywindowhourend() {
		return daywindowhourend;
	}

	private int daywindowhourend;
	private ArrayList<DayOfWeek> daysoff;

	private long getBusinessDayLengthInMs() {
		return (daywindowhourend - daywindowhourstart) * 1000 * 3600;
	}

	/**
	 * creates a new business calendar with no day off and activity 24/7
	 */
	public BusinessCalendar() {
		daysoff = new ArrayList<DayOfWeek>();
		daywindowhourstart = 0;
		daywindowhourend = 24;
	}

	/**
	 * creates a new business calendar with no day off and activity between
	 * specified hours
	 * 
	 * @param daywindowhourstart start of business day
	 * @param daywindowhourend   end of business day
	 */
	public void setBusinessHoursWindow(int daywindowhourstart, int daywindowhourend) {
		this.daywindowhourstart = daywindowhourstart;
		this.daywindowhourend = daywindowhourend;
	}

	/**
	 * adds one week of the day as off
	 * 
	 * @param onedayoff week to add
	 */
	public void addHolidayDayOfWeek(DayOfWeek onedayoff) {
		this.daysoff.add(onedayoff);
	}

	/**
	 * A point in time, represented as an instant in the business calendar. This
	 * specifies the date, and the amount of time inside the opening day. It also
	 * has a flag to specify if it is valid or not. Not valid means we took the
	 * closest opening time, but the instant entered is not valid. E.g. if opening
	 * time is 9 to 18 (hours), when specifying 8:00 with "round after", what will
	 * be brought back is 9:00 the same day and invalid flag. If "round after" if
	 * false, then the day before at 18:00 is brought back.
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class BusinessTimeInstant
			implements
			Comparable<BusinessTimeInstant> {
		private LocalDate openingday;
		private long openingtimeinms;
		private boolean invalid = false;

		/**
		 * @return if the instant is invalid
		 */
		public boolean isInvalid() {
			return invalid;
		}

		private boolean isDayOff(LocalDate date) {
			DayOfWeek dayofweek = date.getDayOfWeek();
			boolean isdayoff = false;
			for (int i = 0; i < daysoff.size(); i++) {
				if (dayofweek.equals(daysoff.get(i))) {
					isdayoff = true;
					break;
				}
			}
			return isdayoff;
		}

		private LocalDate findNextValidLocalDate(LocalDate date, boolean roundafter) {
			long increment = 1; // round after: go forward
			if (!roundafter)
				increment = -1; // round before: go backward
			LocalDate datecursor = date.plusDays(increment);
			while (isDayOff(datecursor)) {
				datecursor = datecursor.plusDays(increment);
			}
			return datecursor;
		}

		private BusinessTimeInstant(LocalDate openingday, long openingtimeinms, boolean invalid) {
			this.openingday = openingday;
			this.openingtimeinms = openingtimeinms;
			this.invalid = invalid;
		}

		/**
		 * @return the start of day in the business calendar
		 */
		public BusinessTimeInstant getStartOfDay() {
			return new BusinessTimeInstant(openingday, 0, false);
		}

		/**
		 * @return the next start of day
		 */
		public BusinessTimeInstant getNextStartOfDay() {
			return new BusinessTimeInstant(findNextValidLocalDate(openingday, true), 0, false);
		}

		/**
		 * creates a business time instant inside the calendar
		 * 
		 * @param date       a date that can be outside of the business calendar
		 * @param roundafter if true, takes the first opening time after the date, if
		 *                   false, take the last opening time before the date (for
		 *                   cases where date is not a valid opening time)
		 */
		public BusinessTimeInstant(Date date, boolean roundafter) {
			LocalDate localdate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			if (!isDayOff(localdate)) {
				LocalDateTime localdatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
				int hour = localdatetime.getHour();
				int minute = localdatetime.getMinute();
				// before window
				if (hour < daywindowhourstart) {
					openingday = localdate;
					openingtimeinms = 0;
					invalid = true;
				}
				if (hour >= daywindowhourend) {
					openingday = localdate;
					openingtimeinms = getBusinessDayLengthInMs();
					invalid = true;
				}
				if (hour >= daywindowhourstart)
					if (hour < daywindowhourend) {
						openingday = localdate;
						openingtimeinms = (hour - daywindowhourstart) * 3600 * 1000 + minute * 60 * 1000;
						invalid = false;
					}
			} else {
				LocalDate nextvaliddate = findNextValidLocalDate(localdate, roundafter);
				if (!nextvaliddate.equals(localdate))
					if (roundafter) {
						// bring the first working time of the day
						openingday = nextvaliddate;
						openingtimeinms = 0;
						invalid = true;

					} else {
						// bring the last working time of the day
						openingday = nextvaliddate;
						openingtimeinms = getBusinessDayLengthInMs();
						invalid = true;

					}
			}

		}

		private long NumberOfBusinessDaysSince(BusinessTimeInstant reference) {
			if (reference.openingday.equals(openingday))
				return 0;
			int increment = 1;
			if (reference.openingday.compareTo(openingday) > 0)
				increment = -1;
			LocalDate cursor = reference.openingday;
			int circuitbreaker = 0;
			long businessdays = 0;
			while ((!cursor.equals(this.openingday)) && (circuitbreaker < 20000)) {
				cursor = cursor.plusDays(increment);
				if (!isDayOff(cursor))
					businessdays = businessdays + increment;
				circuitbreaker++;
			}
			if (circuitbreaker == 20000)
				throw new RuntimeException("Failed recursive algorithm");
			return businessdays;
		}

		/**
		 * Calculated the opening time in ms since the other business time instant. This
		 * is used to draw graphics on a GANNT
		 * 
		 * @param reference reference business time instant
		 * @return opening time in miliseconds
		 */
		public long OpeningTimeInMsSince(BusinessTimeInstant reference) {
			long numberofdays = NumberOfBusinessDaysSince(reference);
			return numberofdays * (3600 * 1000 * (long) (daywindowhourend - daywindowhourstart)) + openingtimeinms
					- reference.openingtimeinms;
		}

		/**
		 * calculates a business time instant by adding an amount of opening business
		 * time to this instant
		 * 
		 * @param timetoaddinms amount to add in ms (so 3,600,000 ms per hour)
		 * @return a new business time instant
		 */
		public BusinessTimeInstant addOpeningTimeInMs(long timetoaddinms) {
			LocalDate datecursor = openingday;
			if (timetoaddinms >= 0) {
				long remainingtimeinms = timetoaddinms;

				while (getBusinessDayLengthInMs() < remainingtimeinms + openingtimeinms) {
					datecursor = findNextValidLocalDate(datecursor, true);
					remainingtimeinms = remainingtimeinms - getBusinessDayLengthInMs();
				}
				return new BusinessTimeInstant(datecursor, openingtimeinms + remainingtimeinms, false);
			} else {
				long remainingtimeinms = timetoaddinms;
				while (openingtimeinms + remainingtimeinms < 0) {
					datecursor = findNextValidLocalDate(datecursor, false);
					remainingtimeinms = remainingtimeinms + getBusinessDayLengthInMs();
				}
				return new BusinessTimeInstant(datecursor, openingtimeinms + remainingtimeinms, false);
			}
		}

		/**
		 * @return the date of this business time instant
		 */
		public Date toDate() {
			return new Date(Date.from(openingday.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime()
					+ daywindowhourstart * 3600 * 1000 + openingtimeinms);
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof BusinessTimeInstant))
				return false;
			BusinessTimeInstant otherinstant = (BusinessTimeInstant) other;
			if (!this.openingday.equals(otherinstant.openingday))
				return false;
			if (this.openingtimeinms != otherinstant.openingtimeinms)
				return false;
			if (this.invalid != otherinstant.invalid)
				return false;
			return true;
		}

		@Override
		public int compareTo(BusinessTimeInstant other) {
			int comparedays = this.openingday.compareTo(other.openingday);
			if (comparedays != 0)
				return comparedays;
			if (this.openingtimeinms > other.openingtimeinms)
				return 1;
			if (this.openingtimeinms < other.openingtimeinms)
				return -1;
			if (this.invalid == other.invalid)
				return 0;
			if (this.openingtimeinms == 0) {
				if (this.invalid)
					return -1;
				return 1;
			}

			if (this.invalid)
				return -1;
			return 1;

		}

		@Override
		public String toString() {

			return "[" + openingday + "," + openingtimeinms + "," + invalid + "]";
		}

	}
}
