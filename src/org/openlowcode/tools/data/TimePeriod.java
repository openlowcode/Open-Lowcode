/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.data;

import java.time.LocalDate;
import java.util.logging.Logger;


/**
 * A time period represents a common calendar period typically used for
 * management reporting and financial forecasting. Today, the following types
 * are managed
 * <ul>
 * <li>Month: a calendar month</li>
 * <li>Quarter: the European quarter, with Q1 from January to March</li>
 * <li>Year: a calendar year in the Julian calendar</li>
 * </ul>
 * A period is by default full. There is the possibility to define a period as
 * 'current', to reports values so far for the period, typically actuals
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriod implements Comparable<TimePeriod> {

	private static Logger logger = Logger.getLogger(TimePeriod.class.getName());

	private YearQualifier yq;
	private int year;

	public static final TimePeriod BEFORE = new TimePeriod(true);
	public static final TimePeriod AFTER = new TimePeriod(false);

	/**
	 * @param quarter a quarter in the European sense (Q1 = January to March)
	 * @param year    a Julian Calendar year
	 */
	public TimePeriod(Quarter quarter, int year) {
		this(quarter, year, true);

	}

	/**
	 * @param quarter a quarter in the European sense (Q1 = January to March)
	 * @param year    a Julian Calendar year
	 * @param full    true if full period, false, to store partial (todate) value
	 */
	public TimePeriod(Quarter quarter, int year, boolean full) {
		if (quarter == null)
			throw new RuntimeException("Quarter cannot be null, please create TimePeriod as year only");
		this.yq = new YearQualifier(quarter, full);
		this.year = year;

	}

	/**
	 * @param yq   qualifier information
	 * @param year year in the Julian calendar
	 */
	public TimePeriod(YearQualifier yq, int year) {
		this.yq = yq;
		this.year = year;
	}

	public YearQualifier getYearQualifier() {
		return this.yq;
	}

	/**
	 * @param year a Julian Calendar year
	 */
	public TimePeriod(int year) {
		this(year, true);

	}

	public TimePeriod(int year, boolean full) {
		this.year = year;
		this.yq = new YearQualifier(PeriodType.YEAR, full);
	}

	/**
	 * @param month a month between 1 (January) and 12 (December)
	 * @param year  A Julian Calendar year
	 */
	public TimePeriod(int month, int year) {
		this(month, year, true);

	}

	/**
	 * @param month a month between 1 (January) and 12 (December)
	 * @param year  A Julian Calendar year
	 * @param full  true if full period, false, to store partial (todate) value
	 */
	public TimePeriod(int month, int year, boolean full) {
		if ((month < 1) || (month > 12))
			throw new RuntimeException("Month needs to be between 1 (January) and 12 (December)");
		this.year = year;
		this.yq = new YearQualifier(month, full);
	}

	/**
	 * A timeperiod representing either before or after the specified values. For
	 * example, if the application manages values on the 2018-2023 period, 'before'
	 * is 2017 or before, 'after' is 2024 or after
	 * 
	 * @param before if true, creates a time period before, if false, creates a time
	 *               period after
	 */
	public TimePeriod(boolean before) {
		this.yq = new YearQualifier(before);
		if (before)
			this.year = Integer.MIN_VALUE / 2;
		if (!before)
			this.year = Integer.MAX_VALUE / 2;
	}

	/**
	 * @return the year if the TimePeriod is different from BEFORE or AFTER,
	 *         Integer.MIN_VALUE/2 if before, INTEGER.MAXVALUE/2 if after (using
	 *         MIN_VALUE/2 or MAX_VALUE/2 ensures that even adding a few units in
	 *         comparisons to those values will get a result that makes sense
	 */
	public int getYear() {
		return this.year;
	}

	public void setPartial() {
		this.yq.full = false;
		logger.finest("   +++-> Set partial for Time Period " + this + " (" + this.hashCode() + ")");
	}

	/**
	 * @return true if period is full, false if period is to date
	 */
	public boolean isFull() {
		return this.yq.full;
	}

	/**
	 * to be called only if period type is month
	 * 
	 * @return month in number (1 = January, 12 = December)
	 */
	public int getMonth() {
		return this.yq.month;
	}

	/**
	 * to be called only if period type is quarter
	 * 
	 * @return a quarter
	 */
	public Quarter getQuarter() {
		return this.yq.quarter;
	}

	public static enum PeriodType {
		MONTH, QUARTER, YEAR, STRICT_YEAR; // year than can not have qualifier 'to date' or BEFORE or AFTER
	}

	public static enum Quarter {
		Q1, Q2, Q3, Q4
	}

	/**
	 * @param quarter
	 * @return a value between 1 (for Q1) and 4 (for Q4)
	 */
	public static int QuarterToInt(Quarter quarter) {
		if (Quarter.Q1.equals(quarter))
			return 1;
		if (Quarter.Q2.equals(quarter))
			return 2;
		if (Quarter.Q3.equals(quarter))
			return 3;
		if (Quarter.Q4.equals(quarter))
			return 4;
		throw new RuntimeException("Quarter should not be null");
	}

	/**
	 * @param integer a number between 1 and 4
	 * @return the quarter corresponding to the number, or a RuntimeException if an
	 *         invalid number is given
	 */
	public static Quarter intToQuarter(int integer) {
		if (integer == 1)
			return Quarter.Q1;
		if (integer == 2)
			return Quarter.Q2;
		if (integer == 3)
			return Quarter.Q3;
		if (integer == 4)
			return Quarter.Q4;
		throw new RuntimeException("Quarter cannot be " + integer);
	}

	/**
	 * This method is used to order period.
	 * 
	 * @return a LocalDate for the last day of the period
	 */
	public LocalDate getPeriodEnd() {
		LocalDate date = null;
		if (this.yq.before)
			date = LocalDate.of(1970, 1, 1);
		if (this.yq.after)
			date = LocalDate.of(2999, 1, 1);

		int year = this.year;
		int month = 12;
		if (this.yq.type != null)
			if (this.yq.type.equals(PeriodType.QUARTER)) {
				month = 3 * QuarterToInt(this.yq.quarter);
			}
		if (this.yq.type != null)
			if (this.yq.type.equals(PeriodType.MONTH))
				month = this.yq.month;

		if (date == null) {
			date = LocalDate.of(year, month, 1);
			// generates date at end of month
			date = LocalDate.of(year, month, date.lengthOfMonth());
		}
		return date;
	}

	@Override
	public int compareTo(TimePeriod o) {
		logger.finest("Comparing " + this + " with " + o);
		if (o == null)
			throw new NullPointerException("Provided a null TimePeriod for comparison with " + this.toString());
		LocalDate thisendperiod = this.getPeriodEnd();
		LocalDate otherendperiod = o.getPeriodEnd();
		if (thisendperiod.equals(otherendperiod)) {
			if (this.yq.full == o.yq.full)
				return 0;
			if ((this.yq.full) && (!o.yq.full))
				return 1;
			return -1;
		}
		return thisendperiod.compareTo(otherendperiod);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof TimePeriod))
			return false;
		TimePeriod othertp = (TimePeriod) obj;
		if (this.yq.full != othertp.yq.full)
			return false;
		if (this.yq.before != othertp.yq.before)
			return false;
		if (this.yq.before == true && othertp.yq.before == true)
			return true;
		if (this.yq.after != othertp.yq.after)
			return false;
		if (this.yq.after == true && othertp.yq.after == true)
			return true;
		if (!this.yq.type.equals(othertp.yq.type))
			return false;
		if (this.year != othertp.year)
			return false;
		if (this.yq.type.equals(PeriodType.YEAR))
			return true;
		if (this.yq.type.equals(PeriodType.MONTH))
			return (this.yq.month == othertp.yq.month);
		return this.yq.quarter.equals(othertp.yq.quarter);
	}

	/**
	 * @param month a number between 1 (included) and 12 (included)
	 * @return a 3 digits summary value for the month (e.g. 'Jan' to January)
	 */
	public static String generateShortMonth(int month) {
		if (month == 1)
			return "Jan";
		if (month == 2)
			return "Feb";
		if (month == 3)
			return "Mar";
		if (month == 4)
			return "Apr";
		if (month == 5)
			return "May";
		if (month == 6)
			return "Jun";
		if (month == 7)
			return "Jul";
		if (month == 8)
			return "Aug";
		if (month == 9)
			return "Sep";
		if (month == 10)
			return "Oct";
		if (month == 11)
			return "Nov";
		if (month == 12)
			return "Dec";
		throw new RuntimeException("Month is not valid " + month);

	}

	/**
	 * returns a String representation of the object, to be transported, typically
	 * over a network, before being generated on the other side as an object;
	 * 
	 * @return
	 */
	public String encode() {
		if (this.yq.before)
			return "BEFORE";
		if (this.yq.after)
			return "AFTER";
		String suffix = "";
		if (!this.yq.full)
			suffix = "(TD)";
		if (this.yq.type.equals(PeriodType.YEAR))
			return "Y" + this.year + suffix;
		if (this.yq.type.equals(PeriodType.QUARTER))
			return "Q" + QuarterToInt(this.yq.quarter) + "-" + this.year + suffix;
		if (this.yq.type.equals(PeriodType.MONTH))
			return "M" + (this.yq.month < 10 ? "0" : "") + this.yq.month + "-" + this.year + suffix;
		throw new RuntimeException("Illegal Period Type");
	}

	/**
	 * @param value
	 * @return the TimePeriod (null if value is null or zero length).
	 */
	public static TimePeriod generateFromString(String value) {
		if (value == null)
			return null;
		if (value.length() == 0)
			return null;
		if (value.equals("BEFORE"))
			return new TimePeriod(true);
		if (value.equals("AFTER"))
			return new TimePeriod(false);
		char firstchar = value.charAt(0);

		if (firstchar == 'Y') {
			if (value.length() < 5)
				throw new RuntimeException(
						"Cannot generate year from String '" + value + "' as it is of smaller length than 5");
			String yearstr = value.substring(1, 5);
			int year = new Integer(yearstr).intValue();
			boolean full = true;
			if (value.length() > 5)
				full = false;
			TimePeriod period = new TimePeriod(year);
			if (!full)
				period.setPartial();
			return period;

		}
		if (firstchar == 'Q') {
			if (value.length() < 7)
				throw new RuntimeException(
						"Cannot generate year from String '" + value + "' as it is not of smaller length than 7");
			int quarternumeric = new Integer(value.substring(1, 2)).intValue();
			int year = new Integer(value.substring(3, 7)).intValue();
			boolean full = true;
			if (value.length() > 7)
				full = false;
			TimePeriod period = new TimePeriod(intToQuarter(quarternumeric), year);
			if (!full)
				period.setPartial();
			return period;
		}
		if (firstchar == 'M') {
			int monthnumeric = new Integer(value.substring(1, 3)).intValue();
			int yearnumeric = new Integer(value.substring(4, 8)).intValue();
			boolean full = true;
			if (value.length() > 8)
				full = false;
			TimePeriod period = new TimePeriod(monthnumeric, yearnumeric);
			if (!full)
				period.setPartial();
			return period;
		}
		throw new RuntimeException("Cannot generate time value from string " + value);
	}

	@Override
	public String toString() {
		if (this.yq.before)
			return "Before";
		if (this.yq.after)
			return "After";
		if (this.yq.type.equals(PeriodType.YEAR))
			return "" + year + (this.yq.full ? "" : " (To Date)");
		if (this.yq.type.equals(PeriodType.QUARTER))
			return "" + this.yq.quarter + " " + year + (this.yq.full ? "" : " (To Date)");
		if (this.yq.type.equals(PeriodType.MONTH))
			return "" + generateShortMonth(this.yq.month) + " " + year + (this.yq.full ? "" : " (To Date)");
		throw new RuntimeException("");

	}

	/**
	 * Contains all information of a time period except the year. This is a separate
	 * class to allow for edition of this information in a separate widget from the
	 * year
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 */
	public static class YearQualifier {
		private boolean before;
		private boolean after;
		private TimePeriod.PeriodType type;
		private int month;
		private TimePeriod.Quarter quarter;
		private boolean full;

		/**
		 * creates YearQualifier before or after;
		 * 
		 * @param before
		 */
		public YearQualifier(boolean before) {
			this.before = before;
			this.after = !before;
			this.full = true;
		}

		public YearQualifier(TimePeriod.PeriodType type, boolean full) {
			this.type = type;
			this.full = full;
		}

		/**
		 * @param month a month between 1 and 12
		 * @param full  true if full month, false if to-date
		 */
		public YearQualifier(int month, boolean full) {
			this.type = PeriodType.MONTH;
			this.month = month;
			this.full = full;
		}

		/**
		 * @param quarter a quarter
		 * @param full    true if full month, false if to-date
		 */
		public YearQualifier(TimePeriod.Quarter quarter, boolean full) {
			this.type = PeriodType.QUARTER;
			this.quarter = quarter;
			this.full = full;
		}

		/**
		 * @return true if value if anything except before or after
		 */
		public boolean NeedsYear() {
			if (before)
				return false;
			if (after)
				return false;
			return true;
		}

		@Override
		public String toString() {
			if (before)
				return "Before";
			if (after)
				return "After";
			String suffix = "";
			if (!full)
				suffix = " (todate)";
			if (PeriodType.YEAR.equals(type)) {
				return "Full Year" + suffix;
			}
			if (PeriodType.QUARTER.equals(type)) {
				return quarter.toString() + suffix;
			}
			if (PeriodType.MONTH.equals(type)) {
				return TimePeriod.generateShortMonth(month) + suffix;
			}
			return "#ERROR";
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof YearQualifier))
				return false;
			YearQualifier otheryq = (YearQualifier) obj;
			return this.toString().equals(otheryq.toString());
		}

	}

	/**
	 * @return an identical clone of the TimePeriod
	 */
	public TimePeriod publicClone() {
		return TimePeriod.generateFromString(this.encode());
	}

	@Override
	protected TimePeriod clone() throws CloneNotSupportedException {
		return TimePeriod.generateFromString(this.encode());
	}

}
