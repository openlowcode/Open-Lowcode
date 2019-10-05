/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

import java.io.IOException;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageEndStructure;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedInterface;

/***
 * A Data Element allows to transport some application data.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public abstract class DataElt extends Named {

	DataEltType type;

	/**
	 * A data element is an element of business information that is transported
	 * between the client and the server
	 * 
	 * @param name name for the element
	 * @param type type of the element
	 */
	public DataElt(String name, DataEltType type) {
		super(name);
		this.type = type;

	}

	/**
	 * @return the type of the element
	 */
	public DataEltType getType() {
		return this.type;
	}

	/**
	 * writes a reference to a CML data (identified for now as name and type, may
	 * become more complex)
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void writeReferenceToCML(MessageWriter writer) throws IOException {
		writer.startStructure("DATAREF");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.type.printType());
		writer.endStructure("DATAREF");
	}

	/**
	 * @param writer
	 * @param hiddenfields if the data is made of Data Objects, it is possible to
	 *                     hide fields
	 * @throws IOException
	 */
	public abstract void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields)
			throws IOException;

	/**
	 * for each element, should read and add content for all elements between the
	 * "TYP" field (excluded) and the "DELT" structure end (also excluded)
	 * 
	 * @param reader
	 * @throws GalliumException
	 */
	public abstract void addPayload(MessageReader reader) throws OLcRemoteException, IOException;

	@SuppressWarnings("rawtypes")
	public static DataElt readFromCML(MessageReader reader) throws OLcRemoteException, IOException {
		reader.returnNextStartStructure("DELT");
		String name = reader.returnNextStringField("NAM");
		String type = reader.returnNextStringField("TYP").substring(0, 3);
		DataElt answer;
		switch (type) {
		case "ARR":
			String arraysecondarytype = reader.returnNextStringField("STP");
			switch (arraysecondarytype) {
			case "OBJ":
				answer = new ArrayDataElt<ObjectDataElt>(name, new ObjectDataEltType());
				break;
			case "DAT":
				answer = new ArrayDataElt<DateDataElt>(name, new DateDataEltType());
				break;
			case "TXT":
				answer = new ArrayDataElt<TextDataElt>(name, new TextDataEltType());
				break;
			case "CHT":
				answer = new ArrayDataElt<ChoiceDataElt>(name, new ChoiceDataEltType());
				break;
			case "OID":
				answer = new ArrayDataElt<ObjectIdDataElt>(name, new ObjectIdDataEltType());
				break;
			case "OMI":
				answer = new ArrayDataElt<ObjectIdDataElt>(name, new ObjectMasterIdDataEltType());
				break;

			default:
				throw new RuntimeException(
						String.format(" secondary Type %s not supported for data element %s at path %s ",
								arraysecondarytype, name, reader.getCurrentElementPath()));
			}

			break;
		case "OBT":
			String objecttreesecondarytype = reader.returnNextStringField("STP");
			switch (objecttreesecondarytype) {
			case "OBJ":
				answer = new ObjectTreeDataElt<ObjectDataElt>(name, new ObjectDataEltType());
				break;

			default:
				throw new RuntimeException(
						String.format(" secondary Type %s not supported for data element %s at path %s ",
								objecttreesecondarytype, name, reader.getCurrentElementPath()));

			}
			break;
		case "NDT":
			String nodetreesecondarytype = reader.returnNextStringField("STP");
			switch (nodetreesecondarytype) {
			case "OBJ":
				answer = new NodeTreeDataElt<ObjectDataElt>(name, new ObjectDataEltType());
				break;
			default:
				throw new RuntimeException(
						String.format(" secondary Type %s not supported for data element %s at path %s ", type, name,
								reader.getCurrentElementPath()));
			}
			break;
		case "OBJ":
			answer = new ObjectDataElt(name);
			break;
		case "DAT":
			answer = new DateDataElt(name);
			break;
		case "TXT":
			answer = new TextDataElt(name);
			break;
		case "CHT":
			answer = new ChoiceDataElt(name);
			break;
		case "OID":
			answer = new ObjectIdDataElt(name);
			break;
		case "OMI":
			answer = new ObjectMasterIdDataElt(name);
			break;

		case "ETX":
			answer = new EncryptedTextDataElt(name);
			break;
		case "DEC":
			answer = new DecimalDataElt(name);
			break;
		case "INT":
			answer = new IntegerDataElt(name);
			break;
		case "LBN":
			answer = new LargeBinaryDataElt(name);
			break;

		default:
			throw new RuntimeException(String.format(" Type %s not supported for data element %s at path %s ", type,
					name, reader.getCurrentElementPath()));

		}
		answer.addPayload(reader);
		// trying to detect a property name
		MessageElement element = reader.getNextElement();
		if (element instanceof MessageStringField) {
			MessageStringField property = (MessageStringField) element;
			// not good name, exception
			if (property.getFieldName().compareTo("PTN") != 0)
				throw new RuntimeException(
						String.format(" Expected 'PTN' as a field, got %s ", property.getFieldName()));
			String propertyname = property.getFieldcontent();
			if (answer instanceof SimpleDataElt) {
				((SimpleDataElt) answer).setPropertyname(propertyname);
			} else {
				throw new RuntimeException(
						String.format(" Have a property 'PTN' tag, but DataElt %s is not a simple Data Elt, but a %s ",
								name, answer.getClass()));
			}

			element = reader.getNextElement();
		}
		// checks that we close DELT
		if (!(element instanceof MessageEndStructure)) {
			throw new RuntimeException(String.format("excepting closing structure element DELT, got element %s at %s",
					element.toString(), reader.returnBufferTrace()));
		}
		if (((MessageEndStructure) element).getName().compareTo("DELT") != 0) {
			throw new RuntimeException(String.format("Closing structure should be calleds DELT, but is called ",
					((MessageEndStructure) element).getName()));
		}
		return answer;
	}

	@Override
	public String toString() {

		return "[" + this.type.printType() + ":" + this.getName() + "]";
	}
}
