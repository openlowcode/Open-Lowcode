/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.format;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.CDateField;
import org.openlowcode.client.graphic.widget.CDateField.LockableDate;

import javafx.util.StringConverter;

/**
 * A string converter providing dates in the format "yyyy-MM-dd HH:mm" and
 * transforming them into Lockable dates
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NiceLockableDateStringConverter
		extends
		StringConverter<CDateField.LockableDate> {
	/**
	 * the format of the date
	 */
	public static final String DEV_TIMESTAMP = "yyyy-MM-dd HH:mm";
	private static Logger logger = Logger.getLogger(NiceLockableDateStringConverter.class.getName());
	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEV_TIMESTAMP);

	@Override
	public LockableDate fromString(String arg0) {
		if (arg0 == null)
			return new LockableDate(false, null);
		if (arg0.trim().length() == 0)
			return new LockableDate(false, null);
		try {
			LocalDateTime localdatetime = LocalDateTime.parse(arg0, formatter);
			Date date = Date.from(localdatetime.atZone(ZoneId.systemDefault()).toInstant());
			logger.fine("generated date correctly");
			return new LockableDate(false, date);
		} catch (DateTimeParseException e) {
			logger.warning("got exception " + e.getMessage());
			return new LockableDate(false, null);
		}
	}

	@Override
	public String toString(LockableDate value) {
		if (value == null)
			return "";
		if (value.getValue() == null)
			return "";
		LocalDateTime datetime = LocalDateTime.ofInstant(value.getValue().toInstant(), ZoneId.systemDefault());
		return formatter.format(datetime);
	}

}
