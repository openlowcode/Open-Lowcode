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
import java.util.logging.Logger;

/**
 * The message reader abstract class performs all consistency controls of the
 * message, ensuring it makes sense (e.g. that only structures that were opened
 * are closed, and that they are closed in the correct sequence).
 * 
 * @author Open Lowcode SAS
 *
 */
public abstract class MessageReader {
	private static final int BUFFER_ELEMENT_NR = 15;
	private static Logger logger = Logger.getLogger(MessageReader.class.getName());
	private ArrayList<String> path = new ArrayList<String>();
	private CircularBuffer<MessageElement> lastelementbuffer = new CircularBuffer<MessageElement>(BUFFER_ELEMENT_NR);
	private boolean inmessage = false; // gives information about if a message is open or not

	private boolean throwerror;

	/**
	 * Returns the last elements read in order to be used in error handling.
	 * 
	 * @return the last elements read as a compact STring.
	 */
	public String returnBufferTrace() {
		return lastelementbuffer.getCompactBufferTrace();
	}

	/**
	 * creates a message reader that by default will throw an OLcRemoteException if
	 * a message error is sent on the server. This allows the decoder to have much
	 * more compact code.
	 */
	public MessageReader() {
		
		this.throwerror = true;
		logger.finest("Message reader created with throwerror = "+throwerror);
	}

	/**
	 * creates a message reader
	 * 
	 * @param throwerror true if the reader will throw an error when encountering a
	 *                   MessageError Element,
	 */
	public MessageReader(boolean throwerror) {
		
		this.throwerror = throwerror;
		logger.finest("Message reader created with throwerror = "+throwerror);

	}

	/***************** MEMORY FOR COMPACT ARRAY ***************/

	private MessageArrayStart activearraystart;

	/***************** PROCESSING OF STRUCTURE ARRAY *******************/

	public void startStructureArray(String arrayname) throws OLcRemoteException, IOException {
		this.returnNextStartStructure(arrayname + "S");
	}

	/**
	 * this function needs to be called after the last inner closing delimiter has
	 * been called
	 * 
	 * @param arrayname name of the main delimiter of the array
	 * @return true if there is a next element, false if the next element was eaten
	 * @throws OLcRemoteException if an error is sent by the other party
	 * @throws IOException        if the communication fails for another reason
	 */
	/**
	 * @param arrayname
	 * @return
	 * @throws OLcRemoteException
	 * @throws IOException
	 */
	public boolean structureArrayHasNextElement(String arrayname) throws OLcRemoteException, IOException {
		MessageElement element = this.getNextElement();
		if (element instanceof MessageStartStructure) {
			// candidate for new element
			MessageStartStructure startstructure = (MessageStartStructure) element;
			if (startstructure.getStructurename().compareTo(arrayname) != 0)
				throw new RuntimeException(String.format(
						"incorrect name of start element, expected '%s', received %s \n       path: %s\n    message: %s",
						arrayname, startstructure.getStructurename(), this.getCurrentElementPath(),
						lastelementbuffer.getCompactBufferTrace()));
			return true;
		}
		if (element instanceof MessageEndStructure) {
			MessageEndStructure endstructure = (MessageEndStructure) element;
			if (endstructure.getName().compareTo(arrayname + "S") != 0)
				throw new RuntimeException(String.format(
						"incorrect name of end element, expected '%s', received %s \n       path: %s\n    message: %s",
						arrayname + "S", endstructure.getName(), this.getCurrentElementPath(),
						lastelementbuffer.getCompactBufferTrace()));
			return false;
		}
		throw new RuntimeException(String.format(
				"while scanning array '%s' next element, found invalid element %s \n       path: %s\n    message: %s",
				arrayname, element, this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/***************** END OF PROCESSING OF ARRAY *******************/

	public MessageElement getNextElement() throws OLcRemoteException, IOException {
		MessageElement currentelement = parseNextElement();

		lastelementbuffer.addElement(currentelement);
		// --------------------- MESSAGE START CHECK --------------------------
		if (currentelement instanceof MessageStart) {
			if (inmessage)
				throw new RuntimeException(String.format(
						"trying to open a message while one is already open \n       path: %s\n    message: %s",
						this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
			inmessage = true;

		}

		// --------------------- MESSAGE END CHECK --------------------------
		if (currentelement instanceof MessageEnd) {
			if (!inmessage)
				throw new RuntimeException(String.format("trying to close a message but none is open"));
			if (path.size() != 0)
				throw new RuntimeException(String.format(
						"trying to close a message while not at root level \n       path: %s\n    message: %s",
						this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
			inmessage = false;
			lastelementbuffer.reset();
		}

		if (currentelement instanceof MessageError) {
			inmessage = false;
			lastelementbuffer.reset();
			path.clear();
			if (throwerror) {
				MessageError error = (MessageError) currentelement;
				throw new OLcRemoteException(error.getErrorcode(), "remote server error: " + error.getErrormessage());
			}
		}

		// --------------------- START STRUCTURE CHECK --------------------------
		if (currentelement instanceof MessageStartStructure) {
			String structurename = ((MessageStartStructure) currentelement).getStructurename();
			path.add(structurename);
		}

		if (currentelement instanceof MessageArrayStart) {
			MessageArrayStart arraystart = (MessageArrayStart) currentelement;
			if (this.activearraystart != null)
				throw new RuntimeException(
						"Incorrect message, received  a message array start " + arraystart.getArrayName()
								+ ", while other array is active " + this.activearraystart.getArrayName());
			this.activearraystart = arraystart;
			this.arraylinebuffer = null;
		}

		if (currentelement instanceof MessageArrayEnd) {
			if (this.activearraystart == null)
				throw new RuntimeException("Trying to close an array while none is open");
			this.activearraystart = null;
		}

		if (currentelement instanceof MessageArrayLine) {
			MessageArrayLine arrayline = (MessageArrayLine) currentelement;
			if (this.activearraystart == null)
				throw new RuntimeException("Received a message array line while no array start is active");
			this.activearraystart.validateArrayLine(arrayline);
		}

		// --------------------- END STRUCTURE CHECK --------------------------
		if (currentelement instanceof MessageEndStructure) {
			if (path.size() == 0)
				throw new RuntimeException("trying to close a structure while already at root level");
			String lastpathelement = path.get(path.size() - 1);
			MessageEndStructure thisendstructure = (MessageEndStructure) currentelement;
			thisendstructure.setName(lastpathelement);
			path.remove(path.size() - 1); // removes last element;
		}

		return currentelement;
	}

	/**
	 * This method will execute correctly if the next element is a message start, or
	 * else,throw a RuntimeException
	 * 
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public void returnNextMessageStart() throws OLcRemoteException, IOException {
		logger.finest("Start getting message start");
		MessageElement nextelement = getNextElement();
		logger.finest("Got one element");
		if (nextelement == null)
			throw new RuntimeException("parser returned null element");
		if (nextelement instanceof MessageStart) {
			logger.finest("message start OK");
			return;
		}
		logger.finest("message start KO");
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a start message element , found %s \n       path: %s\n    message: %s",
				nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));

	}

	/**
	 * This method will execute correctly if the next element is a message end, or
	 * else,throw a RuntimeException
	 * 
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public void returnNextEndMessage() throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement == null)
			throw new RuntimeException(String.format("parser returned null element \n       path: %s\n    message: %s",
					this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		if (nextelement instanceof MessageEnd) {
			return;
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a end message element, found %s  \n       path: %s\n    message: %s",
				nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));

	}

	protected abstract MessageElement parseNextElement() throws OLcRemoteException, IOException;

	/**
	 * This method will execute correctly if the next element is a structure start,
	 * or else,throw a RuntimeException
	 * 
	 * @return the name of the start structure element
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public String returnNextStartStructure() throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageStartStructure) {
			MessageStartStructure sselement = (MessageStartStructure) nextelement;
			return sselement.getStructurename();
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a start element, found %s \n       path: %s\n    message: %s",
				nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * This method will execute correctly if the next element is a structure start
	 * if the given structurename, or else,throw a RuntimeException
	 * 
	 * @param structurename name of the element
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public void returnNextStartStructure(String structurename) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement == null)
			throw new RuntimeException(String.format("parser returned null element \n       path: %s\n    message: %s",
					this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		if (nextelement instanceof MessageStartStructure) {
			MessageStartStructure sselement = (MessageStartStructure) nextelement;
			if (sselement.getStructurename().compareTo(structurename) != 0)
				throw new RuntimeException(String.format(
						"incorrect name of start element, expected '%s'n received %s \n       path: %s\n    message: %s",
						structurename, sselement.getStructurename(), this.getCurrentElementPath(),
						lastelementbuffer.getCompactBufferTrace()));
		} else {
			throw new RuntimeException(String.format(
					"incorrect syntax, looking for a start structure element, found %s \n       path: %s\n    message: %s",
					nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
	}

	/**
	 * This method will execute correctly if the next element is a structure end of
	 * the specified name, or else,throw a RuntimeException
	 * 
	 * @param structuretoclose name of the element
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public void returnNextEndStructure(String structuretoclose) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement == null)
			throw new RuntimeException(String.format("parser returned null element \n       path: %s\n    message: %s",
					this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		if (nextelement instanceof MessageEndStructure) {
			MessageEndStructure thisendstructure = (MessageEndStructure) nextelement;
			if (thisendstructure.getName().compareTo(structuretoclose) != 0)
				throw new RuntimeException(String.format(
						"incorrect name specified for close end structure, specified %s, found in message %s \n       path: %s\n    message: %s",
						structuretoclose, thisendstructure.getName(), this.getCurrentElementPath(),
						lastelementbuffer.getCompactBufferTrace()));
			return;
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for an end structure element %s, found %s \n       path: %s\n    message: %s",
				structuretoclose, nextelement.toString(), this.getCurrentElementPath(),
				lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * This method will execute correctly if the next element is a date field of the
	 * given name, or else,throw a RuntimeException
	 * 
	 * @param name name of the element
	 * @return payload of the field
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public Date returnNextDateField(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageDateField) {
			MessageDateField dfield = (MessageDateField) nextelement;
			if (dfield.getFieldName().compareTo(name) == 0) {
				return dfield.getFieldcontent();
			}
			throw new RuntimeException(String.format(
					"incorrect date field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					dfield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a date element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * This method will execute correctly if the next element is a String field of
	 * the given name, or else,throw a RuntimeException
	 * 
	 * @param name name of the element
	 * @return payload of the field
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public String returnNextStringField(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageStringField) {
			MessageStringField sfield = (MessageStringField) nextelement;
			if (sfield.getFieldName().compareTo(name) == 0) {
				return sfield.getFieldcontent();
			}
			throw new RuntimeException(String.format(
					"incorrect string field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					sfield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a string element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * This method will execute correctly if the next element is a LargeBinary field
	 * of the given name, or else,throw a RuntimeException
	 * 
	 * @param name name of the element
	 * @return payload of the field
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public SFile returnNextLargeBinary(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageBinaryField) {
			MessageBinaryField sfield = (MessageBinaryField) nextelement;
			if (sfield.getFieldName().compareTo(name) == 0) {
				return sfield.getFieldContent();
			}
			throw new RuntimeException(String.format(
					"incorrect binary field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					sfield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a binary element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));

	}

	/**
	 * This method will execute correctly if the next element is an Integer field of
	 * the given name, or else,throw a RuntimeException
	 * 
	 * @param name name of the element
	 * @return payload of the field
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public int returnNextIntegerField(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageIntegerField) {
			MessageIntegerField ifield = (MessageIntegerField) nextelement;
			if (ifield.getFieldName().compareTo(name) == 0) {
				return ifield.getFieldContent();
			}
			throw new RuntimeException(String.format(
					"incorrect integer field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					ifield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a integer element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * This method will execute correctly if the next element is a boolean field of
	 * the given name, or else,throw a RuntimeException
	 * 
	 * @param name name of the element
	 * @return payload of the field
	 * @throws OLcRemoteException if an error is received from the other party
	 * @throws IOException        for any communication issue with the other party
	 */
	public boolean returnNextBooleanField(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageBooleanField) {
			MessageBooleanField bfield = (MessageBooleanField) nextelement;
			if (bfield.getFieldName().compareTo(name) == 0) {
				return bfield.getFieldContent();
			}
			throw new RuntimeException(String.format(
					"incorrect boolean field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					bfield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a boolean element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	private String getCurrentElementPath(ArrayList<String> thispath) {
		StringBuffer spath = new StringBuffer();
		spath.append("CMLPATH=");
		for (int i = 0; i < thispath.size(); i++) {
			spath.append('/');
			spath.append(thispath.get(i));
		}
		return spath.toString();
	}

	/**
	 * @return a String represnetation of the path of the current element (the
	 *         hierarchical of structure names the current element is part of)
	 */
	public String getCurrentElementPath() {
		return getCurrentElementPath(this.path);
	}

	/**
	 * @return true if a message is currently open, false else
	 */
	public boolean isInMessage() {
		return this.inmessage;
	}

	/**
	 * @return the number of characters received since last start message
	 */
	public abstract long charcountsinceStartMessage();

	public BigDecimal returnNextDecimalField(String name) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageDecimalField) {
			MessageDecimalField dfield = (MessageDecimalField) nextelement;
			if (dfield.getFieldName().compareTo(name) == 0) {
				return dfield.getFieldcontent();
			}
			throw new RuntimeException(String.format(
					"incorrect decimal field name, expected %s, found %s \n       path: %s\n    message: %s", name,
					dfield.getFieldName(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		}
		throw new RuntimeException(String.format(
				"incorrect syntax, looking for a decimal element called %s, found %s \n       path: %s\n    message: %s",
				name, nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * starts recording the message being received. If recording is ongoing, the
	 * current recording is thrown away and new recording starts
	 */
	public abstract void startrecord();

	/**
	 * stops recording and sends back a string of all the message received since the
	 * beginning of the recording
	 * 
	 * @return
	 */
	public abstract String endrecord();

	/**
	 * @param arrayname name of the compact array structure
	 * @return the MessageArrayStart element, or throws an exception if the next
	 *         element does not correspond
	 * @throws OLcRemoteException if the remote party encounters an error while
	 *                            processing the message
	 * @throws IOException        if any communication issue is encountered
	 */
	public MessageArrayStart returnNextMessageStartArray(String arrayname) throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (!(nextelement instanceof MessageArrayStart))
			throw new RuntimeException(String.format(
					"Expecting a MessageArrayStart, got  %s \n       path: %s\n    message: %s", nextelement.toString(),
					this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		MessageArrayStart arraystart = (MessageArrayStart) nextelement;
		if (!arraystart.getArrayName().equals(arrayname))
			throw new RuntimeException(String.format(
					"Message Array Start does not have the good name, expecting %s, got %s \n       path: %s\n    message: %s",
					arrayname, nextelement.toString(), this.getCurrentElementPath(),
					lastelementbuffer.getCompactBufferTrace()));
		return arraystart;
	}

	private MessageArrayLine arraylinebuffer = null;

	/**
	 * This convenience method should be called inside the while statement of a loop
	 * that reads all lines of the compact array. It reads the next element, and if
	 * this is an arrayline, stores it into a buffer so that it can be retrieved
	 * with the getArrayNextLine method
	 * 
	 * @return true if a MessageArrayLine could be found, false if the array is
	 *         finished
	 * @throws OLcRemoteException if the remote party encounters an error while
	 *                            processing the message
	 * @throws IOException        if any communication issue is encountered
	 */
	public boolean hasArrayNextLine() throws OLcRemoteException, IOException {
		MessageElement nextelement = getNextElement();
		if (nextelement instanceof MessageArrayEnd)
			return false;
		if (nextelement instanceof MessageArrayLine) {
			arraylinebuffer = (MessageArrayLine) nextelement;
			return true;
		}
		throw new RuntimeException(String.format(
				"Did not find either a MessageArrayEnd or MessageArrayLine got %s \n       path: %s\n    message: %s",
				nextelement.toString(), this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
	}

	/**
	 * @return the MessageArrayLine stored in a local buffer after hasArrayNextLine has
	 * been called.
	 */
	public MessageArrayLine getArrayNextLine() {
		if (arraylinebuffer == null)
			throw new RuntimeException(String.format(
					"Only call this method after a call to hasArrayNextLine has returned true,  path: %s\n    message: %s",
					this.getCurrentElementPath(), lastelementbuffer.getCompactBufferTrace()));
		MessageArrayLine linetoreturn = arraylinebuffer;
		arraylinebuffer = null;
		return linetoreturn;
	}

}
