/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;
import java.util.Date;

import org.openlowcode.module.system.data.choice.ReportingfrequencyChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;

/**
 * This class is grouping utilities shared between client and server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StandardUtil {
	/**
	 * returns true if both objects are null, returns false if one of the 2 objets
	 * only is null. If both objects are not null, returns the equal comparison of
	 * both objects for the class
	 * 
	 * @param object1 an object that can be null
	 * @param object2 another object that can be null
	 * @return true if both objects are either null or equal
	 */
	public static <E extends Object> boolean compareIncludesNull(E object1, E object2) {
		if (object1 == null) {
			if (object2 == null)
				return true;
			if (object2 != null)
				return false;
		}
		// from here, object 1 is not null
		if (object2 == null)
			return false;
		// from here object1 and object2 are not null
		return (object1.equals(object2));
	}

	/**
	 * Open Lowcode uses as default a number formatting that is non ambiguous for
	 * both English speaking and French speaking countries. Decimal separator is dot
	 * '.', thousands grouping separator is space ' '.
	 * 
	 * @return
	 */
	public static DecimalFormat getOLcDecimalFormatter() {
		String formatfordecimalstring = "###,###.###";
		DecimalFormat formatfordecimal = new DecimalFormat(formatfordecimalstring);
		DecimalFormatSymbols formatforsymbol = formatfordecimal.getDecimalFormatSymbols();
		formatforsymbol.setGroupingSeparator(' ');
		formatforsymbol.setDecimalSeparator('.');
		formatfordecimal.setDecimalFormatSymbols(formatforsymbol);
		formatfordecimal.setParseBigDecimal(true);
		return formatfordecimal;
	}

	/**
	 * generates noon in the given calendar for the date given
	 * 
	 * @param date     a date and time
	 * @param calendar a calendar
	 * @return a date at noon on the provided calendar
	 */
	private static Date getNoon(Date date, Calendar calendar) {
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		calendar.set(Calendar.MINUTE, calendar.getMinimum(Calendar.MINUTE));
		calendar.set(Calendar.SECOND, calendar.getMinimum(Calendar.SECOND));
		calendar.set(Calendar.MILLISECOND, calendar.getMinimum(Calendar.MILLISECOND));
		return calendar.getTime();
	}

	/**
	 * gets the next saturday noon from a date. This can be used to perform weekly
	 * reports
	 * 
	 * @param date     a date
	 * @param calendar a calendar
	 * @return the next saturday noon
	 */
	private static Date getSaturdayNoon(Date date, Calendar calendar) {
		LocalDateTime localdatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return getNoon(Date.from(localdatetime.with(TemporalAdjusters.next(DayOfWeek.SATURDAY))
				.atZone(ZoneId.systemDefault()).toInstant()), calendar);
	}

	/**
	 * gets the last day of the month at noon. This can be used to perform monthly
	 * reports
	 * 
	 * @param date     a date
	 * @param calendar a calendar
	 * @return last day of the month at noon
	 */
	private static Date getLastDayOfMonthNoon(Date date, Calendar calendar) {
		LocalDateTime localdatetime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return getNoon(Date.from(
				localdatetime.with(TemporalAdjusters.lastDayOfMonth()).atZone(ZoneId.systemDefault()).toInstant()),
				calendar);
	}

	/**
	 * get last date of the given period (at noon)
	 * 
	 * @param date      a date
	 * @param frequency a given frequency for reporting
	 * @param calendar  a calendar
	 * @return last date of the given period
	 */
	public static Date getLastDateOfPeriod(
			Date date,
			ChoiceValue<ReportingfrequencyChoiceDefinition> frequency,
			Calendar calendar) {

		if (frequency.equals(ReportingfrequencyChoiceDefinition.get().DAILY))
			return getNoon(date, calendar);
		if (frequency.equals(ReportingfrequencyChoiceDefinition.get().WEEKLY))
			return getSaturdayNoon(date, calendar);
		if (frequency.equals(ReportingfrequencyChoiceDefinition.get().MONTHLY))
			return getLastDayOfMonthNoon(date, calendar);
		return getNoon(date, calendar);

	}

}
