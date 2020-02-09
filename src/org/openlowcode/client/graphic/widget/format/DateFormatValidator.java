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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.openlowcode.client.graphic.widget.fields.FormatValidator;

/**
 * A format validator parsing a date in the format yyyy-MM-dd HH:mm
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DateFormatValidator
		implements
		FormatValidator {
	/**
	 * format used for this date format validator
	 */
	public static final String DEV_TIMESTAMP = "yyyy-MM-dd HH:mm";

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DEV_TIMESTAMP);

	@Override
	public String valid(String valueasstring) {
		try {
			LocalDateTime.parse(valueasstring, formatter);
			return valueasstring;
		} catch (DateTimeParseException e) {
			return null;
		}

	}

}
