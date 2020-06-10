/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.action;

import java.io.IOException;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.Named;

/**
 * An Action Data Location on a page. When transmitting a page from server to
 * client, each action specifies the type and location of data to take as
 * attribute for the action.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CActionDataLoc extends Named {
	private String type;
	private String path;
	public static final String TEXT_TYPE = "TXT";
	public static final String OBJ_TYPE = "OBJ";
	public static final String OBJ_ID_TYPE = "OID";
	public static final String OBJ_MS_ID_TYPE = "OMI";
	public static final String CHOICE_TYPE = "CHT";
	public static final String MULTI_CHOICE_TYPE = "MLC";
	public static final String ARRAY_PREFIX = "ARR/";
	public static final String DATE_TYPE = "DAT";
	public static final String BINARY_TYPE = "LBN";
	public static final String INTEGER_TYPE = "INT";
	public static final String TIMEPERIOD_TYPE = "TPE";

	private String objectfield = null;

	/**
	 * @param reader A Message reader
	 * @throws OLcRemoteException in case the server launches an exception while
	 *                            processing the request
	 * @throws IOException        in case of IO error (connection broken...)
	 */
	public CActionDataLoc(MessageReader reader) throws OLcRemoteException, IOException {
		super(reader.returnNextStringField("NAM"));
		this.type = reader.returnNextStringField("TYP");
		boolean typesupported = false;
		if (this.type.compareTo(TEXT_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(OBJ_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(OBJ_ID_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(OBJ_MS_ID_TYPE) == 0)
			typesupported=true;
		if (this.type.compareTo(CHOICE_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(MULTI_CHOICE_TYPE) == 0)
			typesupported = true;
		
		if (this.type.compareTo(DATE_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(BINARY_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(INTEGER_TYPE) == 0)
			typesupported = true;
		if (this.type.compareTo(TIMEPERIOD_TYPE)==0)
			typesupported=true;
		if (this.type.startsWith(ARRAY_PREFIX)) {
			String subtype = this.type.substring(4);
			if (subtype.compareTo(TEXT_TYPE) == 0)
				typesupported = true;
			if (subtype.compareTo(OBJ_TYPE) == 0)
				typesupported = true;
			if (subtype.compareTo(OBJ_ID_TYPE) == 0)
				typesupported = true;
			if (subtype.compareTo(CHOICE_TYPE) == 0)
				typesupported = true;
			if (subtype.compareTo(OBJ_MS_ID_TYPE) == 0)
				typesupported = true;
		}
		if (!typesupported)
			throw new RuntimeException("the type put in the action field " + this.getName()
					+ " is not supported by the application : " + this.type + " - " + reader.getCurrentElementPath());
		MessageStringField object = (MessageStringField) reader.getNextElement();
		if (object.getFieldName().compareTo("OBF") == 0) {
			this.objectfield = object.getFieldcontent();
			this.path = reader.returnNextStringField("PTH");
		} else {
			if (object.getFieldName().compareTo("PTH") == 0) {
				this.path = object.getFieldcontent();
			} else {
				throw new RuntimeException("expected 'PTH' in the action field " + this.getName()
						+ ", got a String field called " + object.getFieldName());
			}
		}
	}

	/**
	 * @return the text representation of the type of data of the attribute
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the path on the page. This is the hierarchy of widgets in the page
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @return in case only one field should be taken from a full object (displayed
	 *         as unit, or on an array), this specifies the field to take
	 */
	public String getObjectField() {
		return this.objectfield;
	}

}
