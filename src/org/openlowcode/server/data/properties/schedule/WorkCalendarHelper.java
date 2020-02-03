/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Holiday;
import org.openlowcode.module.system.data.Holidayset;
import org.openlowcode.module.system.data.Weeklyslot;
import org.openlowcode.module.system.data.Workcalendar;
import org.openlowcode.module.system.data.choice.WeeklyslotChoiceDefinition;
import org.openlowcode.module.system.data.choice.WesternmonthsChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;

/**
 * This class performs scheduling functions around a calendar object. It
 * especially provides:
 * <ul>
 * <li>a caching system to avoid requesting data in database several times</li>
 * <li>logic to provide services based on calendar data</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class WorkCalendarHelper {
	private static Logger logger = Logger.getLogger(WorkCalendarHelper.class.getName());
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.M.dd HH:mm:ss.SSS");
	private Weeklyslot[] weeklyslots;
	private Holidayset holidayset;
	private Holiday[] holidays;
	private ArrayList<CalendarTimeSlot> calendartimeslots;
	private Date firstdateinbuffer = null; // start of day of first date in buffer
	private Date lastdateinbuffer = null; // end of day of last date in buffer
	private boolean hasdata = true;

	/**
	 * This method returns a compact String encoding a work calendar. This can be
	 * used on the client side whenever knowing the work calendar is required (e.g.
	 * to display schedule). The String is made of a succession of weekly slots and
	 * then of holidays<br>
	 * Weekly Slot is defined with following format:
	 * <ul>
	 * <li>3 Digits code for definition of days:
	 * <ul>
	 * <li>D01: Monday</li>
	 * <li>D02: Tuesday</li>
	 * <li>D03: Wednesday</li>
	 * <li>D04: Thursday</li>
	 * <li>D05: Friday</li>
	 * <li>D06: Saturday</li>
	 * <li>D07: Sunday</li>
	 * <li>D15: Monday to Friday</li>
	 * <li>D16: Monday to Saturday</li>
	 * <li>D17: Everyday</li>
	 * </ul>
	 * </li>
	 * <li>start hours in two digits</li>
	 * <li>start minutes in two digits</li>
	 * <li>end hour in two digits</li>
	 * <li>end minutes in two digits</li>
	 * </ul>
	 * After weekly slot, a pipe '|' is sent in the String The holidays are encoded
	 * as followed:
	 * <ul>
	 * <li>day in two digits</li>
	 * <li>months in two digits</li>
	 * <li>year in 4 digits. If year is not set, NNNN is returned</li>
	 * </ul>
	 * 
	 * @return
	 */
	public String getWorkCalendarString() {
		StringBuffer workcalendarstring = new StringBuffer();
		for (int i = 0; i < weeklyslots.length; i++) {
			Weeklyslot thisweeklyslot = weeklyslots[i];
			workcalendarstring.append(thisweeklyslot.getDaysinweeek().getStorageCode());
			int hourstart = thisweeklyslot.getHourstart().intValue();
			int minutestart = 0;
			if (thisweeklyslot.getMinutestart() != null)
				minutestart = thisweeklyslot.getMinutestart().intValue();
			int hourend = thisweeklyslot.getHourend().intValue();
			int minuteend = 0;
			if (thisweeklyslot.getMinuteend() != null)
				minuteend = thisweeklyslot.getMinuteend().intValue();
			workcalendarstring.append(String.format("%02d", hourstart));
			workcalendarstring.append(String.format("%02d", minutestart));
			workcalendarstring.append(String.format("%02d", hourend));
			workcalendarstring.append(String.format("%02d", minuteend));
		}
		workcalendarstring.append('|');
		for (int i = 0; i < holidays.length; i++) {
			Holiday thisholiday = holidays[i];
			int day = thisholiday.getHlday().intValue();
			String daystring = String.format("%02d", day);
			String month = thisholiday.getHlmonth().getStorageCode().substring(1);
			String year = "NNNN";
			if (thisholiday.getHlyear() != null)
				if (thisholiday.getHlyear().intValue() > 0)
					year = String.format("%04d", thisholiday.getHlyear().intValue());
			workcalendarstring.append(daystring);
			workcalendarstring.append(month);
			workcalendarstring.append(year);
		}
		return workcalendarstring.toString();
	}

	/**
	 * a utility class to store a timeslot in time. Timeslots may be on a valid
	 * schedule or outside it
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class CalendarTimeSlot implements Comparable<CalendarTimeSlot> {
		private Date start;
		private Date end;
		private boolean outofschedule;

		/**
		 * @return true if timeslot is out of schedule, false else
		 */
		public boolean isOutOfSchedule() {
			return this.outofschedule;
		}

		/**
		 * @return start time of the timeslot
		 */
		public Date getStartTime() {
			return this.start;

		}

		/**
		 * @return end time of the timeslot
		 */
		public Date getEndTime() {
			return this.end;
		}

		/**
		 * Creates a valid CalendarTimeSlot
		 * 
		 * @param slotstart start of the slot
		 * @param slotend   end of the slot
		 */
		public CalendarTimeSlot(Date slotstart, Date slotend) {
			this.start = slotstart;
			this.end = slotend;
			this.outofschedule = false;
		}

		/**
		 * Creates a CalendarTimeSlot that may be valid or not
		 * 
		 * @param slotstart     start of the slot
		 * @param slotend       end of the slot
		 * @param outofschedule true if the slot is outside of authorized schedule
		 */
		public CalendarTimeSlot(Date slotstart, Date slotend, boolean outofschedule) {
			this.start = slotstart;
			this.end = slotend;
			this.outofschedule = outofschedule;
		}

		@Override
		public int compareTo(CalendarTimeSlot o) {
			if (start == null)
				return 1;
			return start.compareTo(o.start);
		}
	}

	private int getMonthInCalendar(ChoiceValue<WesternmonthsChoiceDefinition> month) {
		if (month.equals(WesternmonthsChoiceDefinition.get().M01))
			return Calendar.JANUARY;
		if (month.equals(WesternmonthsChoiceDefinition.get().M02))
			return Calendar.FEBRUARY;
		if (month.equals(WesternmonthsChoiceDefinition.get().M03))
			return Calendar.MARCH;
		if (month.equals(WesternmonthsChoiceDefinition.get().M04))
			return Calendar.APRIL;
		if (month.equals(WesternmonthsChoiceDefinition.get().M05))
			return Calendar.MAY;
		if (month.equals(WesternmonthsChoiceDefinition.get().M06))
			return Calendar.JUNE;
		if (month.equals(WesternmonthsChoiceDefinition.get().M07))
			return Calendar.JULY;
		if (month.equals(WesternmonthsChoiceDefinition.get().M08))
			return Calendar.AUGUST;
		if (month.equals(WesternmonthsChoiceDefinition.get().M09))
			return Calendar.SEPTEMBER;
		if (month.equals(WesternmonthsChoiceDefinition.get().M10))
			return Calendar.OCTOBER;
		if (month.equals(WesternmonthsChoiceDefinition.get().M11))
			return Calendar.NOVEMBER;
		if (month.equals(WesternmonthsChoiceDefinition.get().M12))
			return Calendar.DECEMBER;

		return -1;

	}

	private boolean isSlotValidForDay(ChoiceValue<WeeklyslotChoiceDefinition> weeklyslot, int dayofweekforcalendar) {
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D01)) {
			if (dayofweekforcalendar == Calendar.MONDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D02)) {
			if (dayofweekforcalendar == Calendar.TUESDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D03)) {
			if (dayofweekforcalendar == Calendar.WEDNESDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D04)) {
			if (dayofweekforcalendar == Calendar.THURSDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D05)) {
			if (dayofweekforcalendar == Calendar.FRIDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D06)) {
			if (dayofweekforcalendar == Calendar.SATURDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D07)) {
			if (dayofweekforcalendar == Calendar.SUNDAY)
				return true;
			return false;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D17)) {
			return true;

		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D15)) {
			if (dayofweekforcalendar == Calendar.SATURDAY)
				return false;
			if (dayofweekforcalendar == Calendar.SUNDAY)
				return false;
			return true;
		}
		if (weeklyslot.equals(WeeklyslotChoiceDefinition.get().D16)) {
			if (dayofweekforcalendar == Calendar.SUNDAY)
				return false;
			return true;
		}
		return false;
	}

	/**
	 * creates a buffer of periods that are 1 year before and 2 years after the date
	 * given. This is the range on which the planning will be managed correctly
	 * 
	 * @param date the start date
	 */
	public void createbufferarounddate(Date date) {
		logger.fine("start creating buffer around date " + sdf.format(date));
		Calendar daynavigator = Calendar.getInstance();
		daynavigator.setTime(date);
		daynavigator.add(Calendar.DAY_OF_YEAR, -365);

		if ((firstdateinbuffer == null) && (lastdateinbuffer == null)) {
			// by default create buffer for one year before and two years after
			for (int i = -365; i < 730; i++) {
				int dayofweek = daynavigator.get(Calendar.DAY_OF_WEEK);
				int dayofmonth = daynavigator.get(Calendar.DAY_OF_MONTH);
				int month = daynavigator.get(Calendar.MONTH);
				int year = daynavigator.get(Calendar.YEAR);
				boolean holiday = false;
				for (int j = 0; j < holidays.length; j++) {

					Holiday thisholiday = holidays[j];
					if (dayofmonth == thisholiday.getHlday().intValue())
						if (month == getMonthInCalendar(thisholiday.getHlmonth())) {
							if (thisholiday.getHlyear() == null) {
								holiday = true;
								break;
							}
							if (thisholiday.getHlyear().intValue() == 0) {
								holiday = true;
								break;
							}
							if (year == thisholiday.getHlyear().intValue()) {
								holiday = true;
								break;
							}
							// not sure this is usefull
							if (year == 0) {
								holiday = true;
								break;
							}
						}
				}
				if (!holiday) {
					for (int j = 0; j < weeklyslots.length; j++) {
						Weeklyslot thisslot = weeklyslots[j];
						if (isSlotValidForDay(thisslot.getDaysinweeek(), dayofweek)) {
							// create slot
							Calendar slotstarttime = Calendar.getInstance();
							slotstarttime.set(Calendar.DAY_OF_MONTH, dayofmonth);
							slotstarttime.set(Calendar.MONTH, month);
							slotstarttime.set(Calendar.YEAR, year);
							slotstarttime.set(Calendar.HOUR_OF_DAY, thisslot.getHourstart().intValue());
							// minute null is interpreted as minute 0
							int startminute = 0;
							if (thisslot.getMinutestart() != null)
								startminute = thisslot.getMinutestart().intValue();
							slotstarttime.set(Calendar.MINUTE, startminute);
							slotstarttime.set(Calendar.SECOND, 0);
							slotstarttime.set(Calendar.MILLISECOND, 0);

							Date slotstart = slotstarttime.getTime();

							Calendar slotendtime = Calendar.getInstance();
							slotendtime.set(Calendar.DAY_OF_MONTH, dayofmonth);
							slotendtime.set(Calendar.MONTH, month);
							slotendtime.set(Calendar.YEAR, year);
							slotendtime.set(Calendar.HOUR_OF_DAY, thisslot.getHourend().intValue());
							int endminute = 0;
							if (thisslot.getMinuteend() != null)
								endminute = thisslot.getMinuteend().intValue();

							slotendtime.set(Calendar.MINUTE, endminute);
							slotendtime.set(Calendar.SECOND, 0);
							slotendtime.set(Calendar.MILLISECOND, 0);
							Date slotend = slotendtime.getTime();
							CalendarTimeSlot calendartimeslot = new CalendarTimeSlot(slotstart, slotend);
							calendartimeslots.add(calendartimeslot);

						}
					}
				}
				daynavigator.add(Calendar.DAY_OF_YEAR, 1);
			}
			logger.fine("sorting calendar buffer " + calendartimeslots.size());
			Collections.sort(calendartimeslots);
			this.firstdateinbuffer = calendartimeslots.get(0).start;
			this.lastdateinbuffer = calendartimeslots.get(calendartimeslots.size() - 1).end;

		}
	}

	/**
	 * Finds the first timeslot that is either containing the time or starts just
	 * after the time specified;
	 * 
	 * @param time
	 * @return
	 */
	private int getNextRelevantSlotIndex(Date time) {
		int length = calendartimeslots.size();
		int minrange = 0;
		int maxrange = length - 1;
		int loop = 0;
		while (maxrange - minrange > 1) {
			loop++;
			int currentindex = (maxrange + 1 + minrange) / 2;

			CalendarTimeSlot currenttimeslot = calendartimeslots.get(currentindex);
			if (loop < 100) {
				logger.finest("min=" + minrange + ", max=" + maxrange + ", currentindex=" + currentindex);
				logger.finest("slot at index " + currentindex + " [" + sdf.format(currenttimeslot.start) + ","
						+ sdf.format(currenttimeslot.end) + "]");
			}
			if (currenttimeslot.end.compareTo(time) <= 0)
				minrange = currentindex;
			if (currenttimeslot.end.compareTo(time) > 0) {
				if (currenttimeslot.start.compareTo(time) <= 0)
					return currentindex;
				if (currenttimeslot.start.compareTo(time) > 0)
					maxrange = currentindex;
			}
		}

		if (maxrange < length)
			return maxrange;
		throw new RuntimeException("no suitable slot found");
	}

	/**
	 * Creates a new helper for the specified work calendar
	 * 
	 * @param calendar the calendar to use
	 */
	public WorkCalendarHelper(Workcalendar calendar) {
		weeklyslots = calendar.getallchildrenforworkcalendarforweeklyslot(null);
		DataObjectId<Holidayset> holidaysetid = calendar.getLinkedtoparentforholidaysetid();
		holidayset = Holidayset.readone(holidaysetid);
		holidays = holidayset.getallchildrenforholidaysetforholiday(null);
		calendartimeslots = new ArrayList<CalendarTimeSlot>();
		if (weeklyslots.length == 0)
			this.hasdata = false;
	}

	/**
	 * get next start date valid for the timeslot.
	 * 
	 * @param endtime endtime of the previous slot
	 * @return the next valid date. It is typically either end-date, or the start
	 *         time of the next working slot
	 * 
	 */
	public Date getNextStartDate(Date endtime) {
		if (endtime == null)
			throw new RuntimeException("Date entered is null");
		if (!hasdata)
			return endtime;
		createbufferarounddate(endtime);
		CalendarTimeSlot timeslot = calendartimeslots.get(getNextRelevantSlotIndex(endtime));
		if (timeslot.start.compareTo(endtime) > 0)
			return timeslot.start;
		return endtime;

	}

	/**
	 * this method provides the active duration in minutes between startime and
	 * endtime. Only time in an active timeslot is counted. For example, if active
	 * timeslot is 8am to 6pm, the active timeslot between 5pm and 9am the next day
	 * is 2 hours (5 to 6pm on the first day, 8 to 9am on the second day
	 * 
	 * @param starttime
	 * @param endtime
	 * @return
	 * 
	 */
	public long getActiveDurationInMinutes(Date starttime, Date endtime) {
		if (!hasdata)
			return (endtime.getTime() - starttime.getTime()) / (60000);
		createbufferarounddate(endtime);
		CalendarTimeSlot[] alltimeslots = getAllSlotsForTimeSlot(starttime, endtime);
		long totalactiveinminutes = 0;
		for (int i = 0; i < alltimeslots.length; i++) {
			CalendarTimeSlot thistimeslot = alltimeslots[i];
			totalactiveinminutes += (thistimeslot.end.getTime() - thistimeslot.start.getTime()) / (60000);
		}
		return totalactiveinminutes;
	}

	/**
	 * gets the end date for the period
	 * 
	 * @param starttime      the start of the slot (typically got through
	 *                       getNextStartDate)
	 * @param lengthinminute length of the slot in minutes
	 * @return the endDate. Note: if the slot includes several working slots, the
	 *         endDate may NOT be be in the same working slot as the start date
	 * 
	 */
	public Date getEndDate(Date starttime, long lengthinminute) {
		createbufferarounddate(starttime);
		int relevanttimeslot = getNextRelevantSlotIndex(starttime);
		CalendarTimeSlot currenttimeslot = calendartimeslots.get(relevanttimeslot);
		long minutesleftinslot = (currenttimeslot.end.getTime() - starttime.getTime()) / (1000 * 60);
		long timeremainingforsession = lengthinminute;
		while (minutesleftinslot < timeremainingforsession) {
			timeremainingforsession = timeremainingforsession - minutesleftinslot;
			relevanttimeslot++;
			currenttimeslot = calendartimeslots.get(relevanttimeslot);
			minutesleftinslot = (currenttimeslot.end.getTime() - currenttimeslot.start.getTime()) / (1000 * 60);
		}
		return new Date(currenttimeslot.start.getTime() + timeremainingforsession * 1000 * 60);
	}

	/**
	 * creates slots as followed:
	 * <ul>
	 * <li>one slot per intersection of the timeslot and business open period</li>
	 * <li>if a slot ends outside business hours, it should be created as between
	 * the end of the last business slot and the invalid date</li>
	 * <li>if a slot starts outside business houlrs, it should be created as between
	 * the invalid date and the start of the business day</li>
	 * </ul>
	 * 
	 * @param starttime start of the timeslot
	 * @param endtime   end of the timeslot
	 * @return an array of calendar time slots, according to the given rule.
	 * 
	 */
	public CalendarTimeSlot[] getAllSlotsForTimeSlot(Date starttime, Date endtime) {
		ArrayList<CalendarTimeSlot> timeslots = new ArrayList<CalendarTimeSlot>();
		createbufferarounddate(starttime);
		int relevanttimeslot = getNextRelevantSlotIndex(starttime);
		CalendarTimeSlot currenttimeslot = calendartimeslots.get(relevanttimeslot);
		Date firstvalidstart = starttime;
		if (currenttimeslot.start.getTime() > starttime.getTime()) {
			timeslots.add(new CalendarTimeSlot(starttime, currenttimeslot.start, true));
			firstvalidstart = currenttimeslot.start;
		}

		if (currenttimeslot.end.getTime() >= endtime.getTime()) {
			// in one slot, returns only one slot with starttime and endtime
			timeslots.add(new CalendarTimeSlot(firstvalidstart, endtime));
		} else {
			// first timeslot is one starting as slot start time and ending at the calendar
			// slot end
			timeslots.add(new CalendarTimeSlot(firstvalidstart, currenttimeslot.end));
			relevanttimeslot++;
			currenttimeslot = calendartimeslots.get(relevanttimeslot);
			while (currenttimeslot.end.getTime() < endtime.getTime()) {
				timeslots.add(currenttimeslot);
				relevanttimeslot++;
				currenttimeslot = calendartimeslots.get(relevanttimeslot);
			}
			// last timeslot with endtime of the full slot as endtime
			if (currenttimeslot.start.getTime() <= endtime.getTime()) {
				timeslots.add(new CalendarTimeSlot(currenttimeslot.start, endtime));
			} else {
				relevanttimeslot--;
				currenttimeslot = calendartimeslots.get(relevanttimeslot);
				timeslots.add(new CalendarTimeSlot(currenttimeslot.end, endtime, true));
			}
		}
		return timeslots.toArray(new CalendarTimeSlot[0]);
	}

	/**
	 * @param newstartdate start date of the new period
	 * @param l            length in minute
	 * @return the end date
	 */
	public Date getNextEndDate(Date newstartdate, long l) {
		if (!hasdata) {
			return new Date(newstartdate.getTime() + 60000 * l);
		}
		if (l < 0)
			throw new RuntimeException("duration is zero, duration cannot be managed for zero");
		if (l == 0)
			return newstartdate;
		logger.fine(" +-+-+ get next end date for " + sdf.format(newstartdate) + " " + l + "m");
		createbufferarounddate(newstartdate);
		int relevanttimeslot = getNextRelevantSlotIndex(newstartdate);
		CalendarTimeSlot currenttimeslot = calendartimeslots.get(relevanttimeslot);
		logger.fine(" +-+-+ found relevant timeslot " + sdf.format(currenttimeslot.start) + "-"
				+ sdf.format(currenttimeslot.end));
		long minutesleft = (currenttimeslot.end.getTime() - newstartdate.getTime()) / (1000 * 60);
		if (minutesleft >= l) {
			logger.fine(" +-+-+ get next end date, return new date " + sdf.format(newstartdate.getTime() + 60000 * l));
			return new Date(newstartdate.getTime() + 60000 * l);
		}
		return currenttimeslot.end;
	}
}
