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

import java.math.BigDecimal;

/**
 * A field for OLc Message containing as payload a potentially null BigDecimal.
 * The format is a X+the plain string representation of the BigDecimal
 * 
 * @author Open Lowcode SAS
 * @see java.math.BigDecimal
 */
public class MessageDecimalField extends MessageField<MessageFieldTypeDecimal> {
	private BigDecimal fieldcontent;

	/**
	 * @param decimal a bigdecimal
	 * @return the serialized big decimal according to OLc format
	 */
	public static String serializeDecimal(BigDecimal decimal) {
		if (decimal == null)
			return "X";
		return "X" + decimal.toPlainString();
	}

	/**
	 * @return the payload as a big decimal
	 */
	public BigDecimal getFieldcontent() {
		return fieldcontent;
	}

	/**
	 * Creates the MessageDecimalField with specified fieldname and payload
	 * 
	 * @param fieldname    the name of the field.
	 * @param fieldcontent a big decimal (can be null)
	 */
	public MessageDecimalField(String fieldname, BigDecimal fieldcontent) {
		super(fieldname);
		this.fieldcontent = fieldcontent;
	}

	@Override
	public String serializepayload(String contextstring) {
		return serializeDecimal(fieldcontent);
	}

}
