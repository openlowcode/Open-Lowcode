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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This message element defines the format of a compact array. A compact array
 * allows sending of lines of data that share the same structure (columns).
 * Columns can be of any supported formats (described in subtypes of
 * MessageFieldType ):
 * <ul>
 * <li>Binary</li>
 * <li>Boolean</li>
 * <li>Date</li>
 * <li>Decimal</li>
 * <li>Boolean</li>
 * <li>Integer</li>
 * <li>String</li>
 * </ul>
 * Please note that binary is not yet supported.
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageArrayStart extends MessageElement {
	private String arrayname;

	private ArrayList<MessageFieldSpec> fieldlist;
	private HashMap<String, Integer> fieldlistperindex;
	private static String START_ARRAY = "(";
	private static String END_ARRAY = ")";

	/**
	 * @return the number of fields (columns) defined in the compact array format
	 */
	public int getFieldSpecNr() {
		return fieldlist.size();
	}

	/**
	 * @param index the index of field (columns), it should be between 0 and
	 *              strictly less than the number of Fields (see getFieldSpecNr)
	 * @return the field at index. Throws an exception if index is invalid
	 */

	public MessageFieldSpec getFieldSpecAt(int index) {
		return fieldlist.get(index);
	}

	/**
	 * @param arrayname name of the array structure
	 * @param fieldlist the definition of fields
	 */

	public MessageArrayStart(String arrayname, ArrayList<MessageFieldSpec> fieldlist) {
		this.arrayname = arrayname;
		this.fieldlist = fieldlist;
		if (fieldlist == null)
			throw new RuntimeException("field list is null for array " + arrayname);
		if (fieldlist.size() == 0)
			throw new RuntimeException("field list has zero element");
		fieldlistperindex = new HashMap<String, Integer>();
		for (int i = 0; i < fieldlist.size(); i++) {
			MessageFieldSpec spec = fieldlist.get(i);
			if (fieldlistperindex.get(spec.getName()) != null)
				throw new RuntimeException("Duplicate field for array with name" + spec.getName());
			fieldlistperindex.put(spec.getName(), new Integer(i));
		}
	}

	/**
	 * @return the name of the array
	 */
	public String getArrayName() {
		return this.arrayname;
	}

	@Override
	public String serialize(String padding, boolean firstattribute) {
		StringBuffer arraystartbuffer = new StringBuffer();
		arraystartbuffer.append("\n");
		arraystartbuffer.append(padding);
		arraystartbuffer.append(START_ARRAY);
		arraystartbuffer.append(START_ARRAY);
		arraystartbuffer.append(arrayname);
		arraystartbuffer.append(START_ARRAY);
		for (int i = 0; i < fieldlist.size(); i++) {
			if (i > 0)
				arraystartbuffer.append(',');
			MessageFieldSpec thisfieldspec = fieldlist.get(i);
			arraystartbuffer.append(thisfieldspec.getName());
			arraystartbuffer.append('=');
			arraystartbuffer.append('"');
			arraystartbuffer.append(thisfieldspec.getType().getMessageFieldAcronym());
			arraystartbuffer.append('"');

		}
		arraystartbuffer.append(END_ARRAY);
		return arraystartbuffer.toString();
	}

	/**
	 * Will analyze the given arrayline, and check that the number of columns of the
	 * line is correct, and there is either a valid value or null as payload for
	 * each column.
	 * 
	 * @param arrayline the array line to check
	 */
	public void validateArrayLine(MessageArrayLine arrayline) {
		if (fieldlist.size() != arrayline.getObjectNumber())
			throw new RuntimeException("Array data line has incorrect " + arrayline.getObjectNumber()
					+ " number of elements, while " + this.fieldlist.size() + " are expected.");
		for (int i = 0; i < fieldlist.size(); i++) {
			fieldlist.get(i).validatePayload(i, arrayline.getPayloadAt(i));
		}
		arrayline.setArrayStart(this);
	}

}
