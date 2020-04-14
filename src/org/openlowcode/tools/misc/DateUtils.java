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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utils to get convenient dates
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.6
 */
public class DateUtils {
	/**
	 * @return 15 minutes before current date
	 */
	public static Date get15MinutesBefore() {
		return new Date(new Date().getTime() - (1000 * 60 * 15));
	}

	/**
	 * @return start of day as per the current timezone at 0am
	 */
	public static Date getStartOfToday() {
		return Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * @return start of yesterday as per the current timezone at 0am
	 */
	public static Date getStartOfYesterday() {
		return Date.from(
				LocalDate.now().minus(1, ChronoUnit.DAYS).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * @return the start of week (sunday in the US, monday in France) at 0am in the
	 *         current timezone
	 */
	public static Date getStartOfThisWeek() {

		return Date.from(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue()).atStartOfDay()
				.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * @return the start of last week (sunday in most western calendars) at 0am in
	 *         the current timezone
	 */
	public static Date getStartOfLastWeek() {

		return Date.from(LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() + 7).atStartOfDay()
				.atZone(ZoneId.systemDefault()).toInstant());
	}

	/**
	 * @return the start of current month at 0am in the current timezone
	 */
	public static Date getStartOfThisMonth() {
		return Date.from(LocalDate.now().minusDays(LocalDate.now().getDayOfMonth() - 1).atStartOfDay()
				.atZone(ZoneId.systemDefault()).toInstant());

	}

	/**
	 * @return the start of last month at 0am in the current timezone
	 */
	public static Date getStartOfLastMonth() {
		LocalDate now = LocalDate.now();
		int daysthismonth = now.getDayOfMonth();
		int dayslastmonth = now.minusDays(daysthismonth).getDayOfMonth();
		return Date.from(LocalDate.now().minusDays(daysthismonth + dayslastmonth - 1).atStartOfDay()
				.atZone(ZoneId.systemDefault()).toInstant());

	}

	/**
	 * @return the start of the current year at 0am in the current timezone
	 */
	public static Date getStartOfThisYear() {
		LocalDate now = LocalDate.now();
		int daysthisyear = now.getDayOfYear();
		return Date.from(
				LocalDate.now().minusDays(daysthisyear - 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());

	}

	/**
	 * @return the start of last year at 0am in the current timezone
	 */
	public static Date getStartOfLastYear() {
		LocalDate now = LocalDate.now();
		int daysthisyear = now.getDayOfYear();
		int dayslastyear = now.minusDays(daysthisyear).getDayOfYear();
		return Date.from(LocalDate.now().minusDays(daysthisyear + dayslastyear - 1).atStartOfDay()
				.atZone(ZoneId.systemDefault()).toInstant());

	}
}
