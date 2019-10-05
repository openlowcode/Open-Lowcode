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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;

/***
 * An ArrayDataElement is a list of data elements with identical type and name
 * for batch processing.This is the most basic way to send an array. In case of
 * data objects, it is typically much more efficient to use the
 * CompactArrayElement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ArrayDataElt<E extends DataElt> extends DataElt {
	private DataEltType arrayelementtype;
	private ArrayList<E> arraycontent;
	private HashMap<String, NamedInterface> hiddenelement;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayDataElt(String name, DataEltType arrayelementtype) {
		super(name, new ArrayDataEltType(arrayelementtype));
		this.arrayelementtype = arrayelementtype;
		this.arraycontent = new ArrayList<E>();
		hiddenelement = new HashMap<String, NamedInterface>();
	}

	/**
	 * @return the payload type for this array
	 */
	public DataEltType getArrayPayloadEltType() {
		return this.arrayelementtype;
	}

	@Override
	public void changeName(String name) {
		super.changeName(name);
		for (int i = 0; i < arraycontent.size(); i++) {
			arraycontent.get(i).changeName(name);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayDataElt(String name, DataEltType arrayelementtype, E[] objectarray) {
		super(name, new ArrayDataEltType(arrayelementtype));
		this.arrayelementtype = arrayelementtype;
		this.arraycontent = new ArrayList<E>();
		for (int i = 0; i < objectarray.length; i++) {
			this.addElement(objectarray[i]);
		}
		hiddenelement = new HashMap<String, NamedInterface>();
	}

	/**
	 * @return the number of objects in the array
	 */
	public int getObjectNumber() {
		return this.arraycontent.size();
	}

	/**
	 * @param index
	 * @return
	 */
	public E getObjectAtIndex(int index) {
		return this.arraycontent.get(index);
	}

	/**
	 * @param arrayelt adds an element to the array
	 */
	public void addElement(E arrayelt) {
		if (arrayelt.getName().compareTo(this.getName()) != 0)
			throw new RuntimeException(String.format(
					"try to add objects with incompatible names in an array, array name : %s, object name: %s",
					this.getName(), arrayelt.getName()));
		if (!arrayelt.getType().equals(arrayelementtype))
			throw new RuntimeException(String.format(
					"try to add objects with incompatible types in an array with name %s, array type: %s, object type: %s ",
					this.getName(), this.getType(), arrayelt.getType()));
		arraycontent.add(arrayelt);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		writer.addStringField("STP", this.arrayelementtype.printType());
		if (arraycontent.size() == 0) {
			writer.addBooleanField("HPL", false);
		} else {
			writer.addBooleanField("HPL", true);

			if (this.getType() instanceof CompactArrayEltType) {
				writer.addBooleanField("CPT", true);
				CompactArrayEltType castedtype = (CompactArrayEltType) this.getType();
				castedtype.writeCompactArray(arraycontent, writer, hiddenfields);

			} else {
				writer.addBooleanField("CPT", false);
				writer.startStructure("FLDS");
				for (int i = 0; i < arraycontent.size(); i++) {
					writer.startStructure("FLD");
					E thisobject = arraycontent.get(i);
					if (thisobject instanceof ObjectDataElt) {
						ObjectDataElt thisobjectasparsed = (ObjectDataElt) thisobject;
						// treatment for transient objects.
						if (thisobjectasparsed.getUID() == null)
							thisobjectasparsed.setUID("ARRAY" + i);
					}
					thisobject.writeToMessage(writer, hiddenelement);
					writer.endStructure("FLD");

				}
				writer.endStructure("FLDS");
			}
		}

		writer.endStructure("DELT");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		boolean haspayload = reader.returnNextBooleanField("HPL");
		if (haspayload) {
			boolean compactsending = reader.returnNextBooleanField("CPT");
			if (compactsending) {
				if (this.arrayelementtype instanceof CompactArrayEltType) {
					CompactArrayEltType<E> castedtype = (CompactArrayEltType<E>) arrayelementtype;
					castedtype.readCompactArray(arraycontent, reader);
				} else
					throw new RuntimeException(
							"Received a compact array type for type not supported compact array, type = "
									+ this.getType().getClass().toString());

			} else {
				reader.startStructureArray("FLD");
				while (reader.structureArrayHasNextElement("FLD")) {
					DataElt arrayelt = DataElt.readFromCML(reader);
					if (arrayelt.getName().compareTo(this.getName()) != 0)
						throw new RuntimeException(String.format(
								"try to add objects with incompatible names in an array, array name : %s, object name: %s",
								this.getName(), arrayelt.getName()));
					if (!arrayelt.getType().equals(arrayelementtype))
						throw new RuntimeException(String.format(
								"try to add objects with incompatible types in an array with name %s, array type: %s, object type: %s ",
								this.getName(), this.getType(), arrayelt.getType()));
					arraycontent.add((E) arrayelt);
					reader.returnNextEndStructure("FLD");
				}
			}

		}
	}

	/**
	 * specifies an hidden element in the array
	 * 
	 * @param marker
	 */
	public void hideElement(NamedInterface marker) {
		hiddenelement.put(marker.toString(), marker);
	}

}
