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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

/**
 * An OLc message writer writes to the communication stream a OLc Message. This
 * message should be sent to the communication stream without significant local
 * storage, as a OLc Message may be bigger than the available memory in the JVM.
 * 
 * @author Open Lowcode SAS
 *
 */
public abstract class MessageWriter {
	private final static int MAX_DEPTH = 255;
	public ArrayList<String> currentpath;
	public boolean active;
	private boolean isfirstelementinstructure = true;

	public boolean isIsfirstelementinstructure() {
		return isfirstelementinstructure;
	}

	public String getCurrentPadding() {

		return "";
	}

	/**
	 * @throws IOException if the connection has an issue
	 */
	public abstract void checkConnection() throws IOException;

	/**
	 * @param messageelement Element to be sent
	 * @throws IOException if any exception is received while sending the element.
	 */
	public abstract void sendMessageElement(MessageElement messageelement) throws IOException;

	public abstract void flushMessage() throws IOException;

	private final static String firstcharLabel = "AZERTYUIOPQSDFGHJKLMWXCVBN";
	private final static String followingcharLabel = "AZERTYUIOPQSDFGHJKLMWXCVBN1234567890_-";

	/**
	 * This method checks that a label (field name or structure name) is compliant
	 * to the rules of a OLc messages. If compliant, the method execute correctly.
	 * If an error is encountered, then an exception is thrown detailing the index
	 * of error, the character, and the list of authorized characters.
	 * 
	 * @param label the label to check
	 * 
	 */
	public static void checkLabelCorrect(String label, String context) {
		if (label == null)
			throw new RuntimeException("Label cannot be null");
		if (label.length() == 0)
			throw new RuntimeException("Label cannot be an empty string");
		char firstchar = label.charAt(0);
		if (firstcharLabel.indexOf(firstchar) == -1)
			throw new RuntimeException("First character '" + firstchar + "' is not valid, should be part of "
					+ firstcharLabel + ", context = " + context);
		for (int i = 1; i < label.length(); i++) {
			char character = label.charAt(i);
			if (followingcharLabel.indexOf(character) == -1)
				throw new RuntimeException("Character at index " + i + " '" + character
						+ "' is not valid, should be one of " + followingcharLabel + ", context = " + context);
		}

	}

	/**
	 * method to be implemented by a MessageWriter: ensures that a communication is
	 * open, reopen it if required, and start sending a message
	 * 
	 * @throws IOException if any problem happens with the communication
	 */

	public void startNewMessage() throws IOException {
		// check if possible to start new message. A writer can send only one message at
		// a time
		if (active) {
			throw new RuntimeException(
					String.format("cannot start a new message when another message already ongoing", currentpath()));
		}
		// everything OK, continue
		currentpath = new ArrayList<String>();
		active = true;
		checkConnection();
		sendMessageElement(new MessageStart());
	}

	/**
	 * @param structurename has to be a valid name (first character an upper case
	 *                      latine letter (A...Z), other characters either an
	 *                      upper-case latine letter (A...Z), a figure (0...9), a
	 *                      minus '-' or an underscore '_'
	 * @throws IOException if any problem happens with the communication
	 */
	public void startStructure(String structurename) throws IOException {
		if (!active)
			throw new RuntimeException(
					String.format("cannot start new structure '%s' when no active message being sent", structurename));
		if (currentpath.size() > MAX_DEPTH)
			throw new RuntimeException(
					String.format("reached maximum structure depth when creating structure %s at path %s",
							structurename, currentpath()));
		checkLabelCorrect(structurename,
				String.format("incorrect element structure name '%s' in context %s", structurename, currentpath()));
		currentpath.add(structurename);
		sendMessageElement(new MessageStartStructure(structurename));
		isfirstelementinstructure = true;
	}

	/**
	 * @param fieldname    has to be a valid name (first character an upper case
	 *                     latine letter (A...Z), other characters either an
	 *                     upper-case latine letter (A...Z), a figure (0...9), a
	 *                     minus '-' or an underscore '_'
	 * @param fieldcontent payload (potentially null string)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addStringField(String fieldname, String fieldcontent) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add String Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + fieldcontent));
		if (currentpath.size() == 0)
			throw new RuntimeException(
					String.format("cannot add String Field when no structure open, fieldname = %s, field content = %s ",
							fieldname, "" + fieldcontent));
		checkLabelCorrect(fieldname,
				String.format("incorrect String field name '%s' in path %s ", fieldname, currentpath()));
		sendMessageElement(new MessageStringField(fieldname, fieldcontent));
		isfirstelementinstructure = false;
	}

	/**
	 * @param fieldname    has to be a valid name (first character an upper case
	 *                     latine letter (A...Z), other characters either an
	 *                     upper-case latine letter (A...Z), a figure (0...9), a
	 *                     minus '-' or an underscore '_'
	 * @param fieldcontent payload (potentially null date)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addDateField(String fieldname, Date fieldcontent) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add date Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + fieldcontent));
		if (currentpath.size() == 0)
			throw new RuntimeException(
					String.format("cannot add date Field when no structure open, fieldname = %s, field content = %s ",
							fieldname, "" + fieldcontent));
		checkLabelCorrect(fieldname,
				String.format("incorrect date field name '%s' in path %s ", fieldname, currentpath()));
		sendMessageElement(new MessageDateField(fieldname, fieldcontent));
		isfirstelementinstructure = false;
	}

	/**
	 * @param fieldname    has to be a valid name (first character an upper case
	 *                     latine letter (A...Z), other characters either an
	 *                     upper-case latine letter (A...Z), a figure (0...9), a
	 *                     minus '-' or an underscore '_'
	 * @param fieldcontent payload (boolean true or false)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addBooleanField(String fieldname, boolean fieldcontent) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add boolean Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + fieldcontent));
		if (currentpath.size() == 0)
			throw new RuntimeException(String.format(
					"cannot add boolean Field when no structure open, fieldname = %s, field content = %s ", fieldname,
					"" + fieldcontent));
		checkLabelCorrect(fieldname,
				String.format("incorrect boolean field name '%s' in path %s ", fieldname, currentpath()));
		sendMessageElement(new MessageBooleanField(fieldname, fieldcontent));
		isfirstelementinstructure = false;
	}

	/**
	 * @param fieldname    has to be a valid name (first character an upper case
	 *                     latine letter (A...Z), other characters either an
	 *                     upper-case latine letter (A...Z), a figure (0...9), a
	 *                     minus '-' or an underscore '_'
	 * @param fieldcontent payload (non null integer)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addIntegerField(String fieldname, int fieldcontent) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add integer Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + fieldcontent));
		if (currentpath.size() == 0)
			throw new RuntimeException(String.format(
					"cannot add integer Field when no structure open, fieldname = %s, field content = %s ", fieldname,
					"" + fieldcontent));
		checkLabelCorrect(fieldname,
				String.format("incorrect integer field name '%s' in path %s ", fieldname, currentpath()));
		sendMessageElement(MessageIntegerField.getCSPMessageIntegerField(fieldname, fieldcontent));
		isfirstelementinstructure = false;
	}

	/**
	 * @param fieldname    has to be a valid name (first character an upper case
	 *                     latine letter (A...Z), other characters either an
	 *                     upper-case latine letter (A...Z), a figure (0...9), a
	 *                     minus '-' or an underscore '_'
	 * @param fieldcontent payload (potentially null BigDecimal)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addDecimalField(String fieldname, BigDecimal fieldcontent) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add integer Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + fieldcontent));
		if (currentpath.size() == 0)
			throw new RuntimeException(String.format(
					"cannot add integer Field when no structure open, fieldname = %s, field content = %s ", fieldname,
					"" + fieldcontent));
		checkLabelCorrect(fieldname,
				String.format("incorrect integer field name '%s' in path %s ", fieldname, currentpath()));
		sendMessageElement(new MessageDecimalField(fieldname, fieldcontent));
		isfirstelementinstructure = false;
	}

	/**
	 * @param fieldname  has to be a valid name (first character an upper case
	 *                   latine letter (A...Z), other characters either an
	 *                   upper-case latine letter (A...Z), a figure (0...9), a minus
	 *                   '-' or an underscore '_'
	 * @param binaryfile payload (potentially null SFile)
	 * @throws IOException if any problem happens with the communication
	 */
	public void addLongBinaryField(String fieldname, SFile binaryfile) throws IOException {
		if (!active)
			throw new RuntimeException(String.format(
					"cannot add binary Field when no active message in sending, field name = %s, field content = %s",
					fieldname, "" + binaryfile));
		if (currentpath.size() == 0)
			throw new RuntimeException(
					String.format("cannot add binary Field when no structure open, fieldname = %s, field content = %s ",
							fieldname, "" + binaryfile));
		checkLabelCorrect(fieldname,
				String.format("incorrect binary field name '%s' in path %s ", fieldname, currentpath()));
		if (binaryfile == null) {
			sendMessageElement(new MessageBinaryField(fieldname));
		}
		if (binaryfile != null)
			if (binaryfile.isEmpty()) {
				sendMessageElement(new MessageBinaryField(fieldname));
			} else {
				sendMessageElement(new MessageBinaryField(fieldname, binaryfile));
			}
	}

	/**
	 * @param structurename has to be a valid name (first character an upper case
	 *                      latine letter (A...Z), other characters either an
	 *                      upper-case latine letter (A...Z), a figure (0...9), a
	 *                      minus '-' or an underscore '_'
	 * @throws IOException if any problem happens with the communication
	 */
	public void endStructure(String structurename) throws IOException {
		if (!active)
			throw new RuntimeException(String.format("cannot close new structure when no active message being sent"));
		if (currentpath.size() == 0)
			throw new RuntimeException(String.format("cannot close structure when no structure open"));
		String lastopenstructurename = currentpath.get(currentpath.size() - 1);
		if (lastopenstructurename.compareTo(structurename) != 0)
			throw new RuntimeException(
					String.format("incorrect name for structure close, was expecting %s, order was %s at path %s  ",
							lastopenstructurename, structurename, currentpath()));
		currentpath.remove(currentpath.size() - 1);
		sendMessageElement(new MessageEndStructure());
	}

	/**
	 * @throws IOException if any problem happens with the communication
	 */
	public void endMessage() throws IOException {
		if (!active)
			throw new RuntimeException(String.format("cannot close end message when no active message being sent"));
		if (currentpath.size() != 0)
			throw new RuntimeException(
					String.format("cannot end message as structures not closed at path %s ", currentpath()));
		active = false;

		sendMessageElement(new MessageEnd());
		currentpath.clear();
		this.flushMessage();
	}

	/**
	 * @param errorcode    code of the error (0 to signal error code is not used /
	 *                     not significant)
	 * @param errormessage human readable message
	 * @throws IOException if any problem happens with the communication
	 */
	public void sendMessageError(int errorcode, String errormessage) throws IOException {
		if (active)
			active = false;
		currentpath.clear();
		sendMessageElement(new MessageError(errorcode, errormessage));
	}

	/**
	 * @return
	 */
	protected String currentpath() {
		StringBuffer currentpathbuffer = new StringBuffer();
		currentpathbuffer.append('[');
		if (currentpath != null)
			for (int i = 0; i < currentpath.size(); i++) {
				currentpathbuffer.append(currentpath.get(i));
				if (i < currentpath.size() - 1)
					currentpathbuffer.append('/');

			}
		currentpathbuffer.append(']');
		return currentpathbuffer.toString();
	}

	public MessageWriter() {
		currentpath = new ArrayList<String>();
	}
}
