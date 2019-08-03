/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A message field storing a simple java date. This will work when both send and
 * receiving end are in the same timezone.
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageDateField extends MessageField<MessageFieldTypeDate> {
	public static final SimpleDateFormat sdf = new SimpleDateFormat("'D'yyyyMMdd'T'HHmmss");
	private Date payload;

	/**
	 * @param date
	 * @return the date in the serialization format used by OLc messages
	 */
	public static String serializeDate(Date date) {
		if (date != null)
			return sdf.format(date);
		return "D";
	}

	/**
	 * @param fieldname name of the field, should be checked at parsing time
	 * @param payload
	 */
	public MessageDateField(String fieldname, Date payload) {
		super(fieldname);
		this.payload = payload;
	}

	@Override
	public String serializepayload(String contextstring) {
		return serializeDate(payload);
	}

	/**
	 * @return the date payload, it can be null.
	 */
	public Date getFieldcontent() {
		return this.payload;
	}
}
