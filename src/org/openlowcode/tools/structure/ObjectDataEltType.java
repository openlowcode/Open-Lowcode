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
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageArrayEnd;
import org.openlowcode.tools.messages.MessageArrayLine;
import org.openlowcode.tools.messages.MessageArrayStart;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageFieldTypeBoolean;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;

/**
 * type of object data element
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectDataEltType extends DataEltType implements CompactArrayEltType<ObjectDataElt> {

	public String getObjectNameForStructure() {
		return null;
	}

	@Override
	public String printType() {
		// TODO Auto-generated method stub
		return "OBJ";
	}

	@Override
	public void writeCompactArray(ArrayList<ObjectDataElt> datatowrite, MessageWriter writer,
			HashMap<String, NamedInterface> hiddenfields) throws IOException {
		if (datatowrite.size() == 0)
			throw new RuntimeException("cannot write compact array of ObjectDataElt if size is smaller than 1");
		ObjectDataElt firstobject = datatowrite.get(0);

		ObjectDataElt firstobjectcasted = (ObjectDataElt) firstobject;

		writer.startStructure("FLDSPECS");
		firstobjectcasted.writeFieldSpecs(writer, hiddenfields);
		writer.endStructure("FLDSPECS");
		MessageArrayStart header = firstobjectcasted.generateHeader(hiddenfields);
		writer.sendMessageElement(header);

		for (int i = 0; i < datatowrite.size(); i++) {
			ObjectDataElt object = datatowrite.get(i);

			writer.sendMessageElement(object.generateLine(header, hiddenfields));
		}
		writer.sendMessageElement(new MessageArrayEnd());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void readCompactArray(ArrayList<ObjectDataElt> emptylisttofill, MessageReader reader)
			throws OLcRemoteException, IOException {
		ArrayList<SimpleDataElementCreator> fieldlist = new ArrayList<SimpleDataElementCreator>();

		reader.startStructureArray("FLDSPEC");
		while (reader.structureArrayHasNextElement("FLDSPEC")) {
			SimpleDataElementCreator thiscreator = SimpleDataElt.getEltCreator(reader);
			fieldlist.add(thiscreator);
		}

		MessageArrayStart start = (MessageArrayStart) reader.getNextElement();
		MessageElement nextelement = reader.getNextElement();
		int fieldnumber = start.getFieldSpecNr();
		if (fieldnumber != 4 + fieldlist.size())
			throw new RuntimeException(
					"Field Number should be " + (4 + fieldlist.size() + " but it is " + fieldnumber));
		int linesprocessed = 0;
		while (nextelement instanceof MessageArrayLine) {
			linesprocessed++;
			MessageArrayLine parsedline = (MessageArrayLine) nextelement;
			if (!start.getFieldSpecAt(0).getName().equals("NAM"))
				throw new RuntimeException(
						"Field index 0 in array should be NAM, it is " + start.getFieldSpecAt(0).getName());
			if (!start.getFieldSpecAt(0).getType().equals(MessageFieldTypeString.singleton))
				throw new RuntimeException(
						"Field index 0 in array is not a string " + start.getFieldSpecAt(0).getName());
			String objectname = (String) parsedline.getPayloadAt(0);
			if (!start.getFieldSpecAt(1).getName().equals("TYP"))
				throw new RuntimeException(
						"Field index 1 in array should be TYP, it is " + start.getFieldSpecAt(0).getName());
			if (!start.getFieldSpecAt(1).getType().equals(MessageFieldTypeString.singleton))
				throw new RuntimeException(
						"Field index 1 in array is not a string " + start.getFieldSpecAt(0).getName());

			// removed as apparently not used
			// String objecttype = (String) parsedline.getPayloadAt(1);

			if (!start.getFieldSpecAt(2).getName().equals("UID"))
				throw new RuntimeException(
						"Field index 2 in array should be UID, it is " + start.getFieldSpecAt(0).getName());
			if (!start.getFieldSpecAt(2).getType().equals(MessageFieldTypeString.singleton))
				throw new RuntimeException(
						"Field index 2 in array is not a string " + start.getFieldSpecAt(0).getName());

			String uid = (String) parsedline.getPayloadAt(2);
			if (!start.getFieldSpecAt(3).getName().equals("FRZ"))
				throw new RuntimeException(
						"Field index 3 in array should be FRZ, it is " + start.getFieldSpecAt(0).getName());
			if (!start.getFieldSpecAt(3).getType().equals(MessageFieldTypeBoolean.singleton))
				throw new RuntimeException(
						"Field index 3 in array is not a string " + start.getFieldSpecAt(0).getName());

			boolean frozen = ((Boolean) parsedline.getPayloadAt(3)).booleanValue();
			ObjectDataElt objectdataelt = new ObjectDataElt(objectname);
			objectdataelt.setUID(uid);
			emptylisttofill.add(objectdataelt);
			if (frozen)
				objectdataelt.setFrozen();
			for (int i = 0; i < fieldlist.size(); i++) {
				SimpleDataElementCreator spec = fieldlist.get(i);
				SimpleDataElt thisfield = spec.getBlankDataElt();
				spec.setPayload(thisfield, parsedline.getPayloadAt(i + 4));
				objectdataelt.addField(thisfield);

			}
			nextelement = reader.getNextElement();

		}
		if (!(nextelement instanceof MessageArrayEnd))
			throw new RuntimeException("Expecting MessageArrayEnd at the end of array, got " + nextelement.getClass()
					+ " after processing " + linesprocessed + " array lines");

	}

}
