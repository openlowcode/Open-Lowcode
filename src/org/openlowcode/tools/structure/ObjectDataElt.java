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
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageArrayLine;
import org.openlowcode.tools.messages.MessageArrayStart;
import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeBoolean;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;
import org.openlowcode.tools.misc.NamedList;

/**
 * an object storing several fields (simple data elements)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectDataElt extends DataElt {
	private static Logger logger = Logger.getLogger(ObjectDataElt.class.getName());
	/**
	 * if frozen is set to true, it means the object is not supposed to be updated
	 * in an update event on the client. This is meant to be used for help to data
	 * entry, not as a bullet proof mean of managing user and access rights
	 */
	private boolean frozen;

	/**
	 * @return value of the frozen flag
	 */
	public boolean isFrozen() {
		return this.frozen;
	}

	private NamedList<SimpleDataElt> objectfields;

	/**
	 * gets the element with the given name
	 * 
	 * @param name name of the element
	 * @return the element, or null if it does not exist
	 */
	public SimpleDataElt lookupEltByName(String name) {
		return objectfields.lookupOnName(name);
	}

	/**
	 * @return gives a quick string with all fields
	 */
	public String dropFieldNames() {
		return objectfields.dropNameList();
	}

	/**
	 * @return gets the number of fields
	 */
	public int fieldnumber() {
		return objectfields.getSize();
	}

	/**
	 * @param index index of the field
	 * @return the field at the index specified
	 */
	public SimpleDataElt getField(int index) {
		return objectfields.get(index);
	}

	private String uid;

	/**
	 * Creates an empty Object Data Element
	 * 
	 * @param name
	 */
	public ObjectDataElt(String name) {
		super(name, new ObjectDataEltType());
		objectfields = new NamedList<SimpleDataElt>();
		this.frozen = false;

	}

	/**
	 * sets the frozen flag
	 */
	public void setFrozen() {
		this.frozen = true;
	}

	protected ObjectDataElt(String name, ObjectDataEltType type, NamedList<SimpleDataElt> fieldlist) {
		super(name, type);
		this.objectfields = fieldlist;
		this.frozen = false;
	}

	/**
	 * adds a field
	 * 
	 * @param field field to add
	 */
	public void addField(SimpleDataElt field) {
		objectfields.add(field);
	}

	/**
	 * sets the unique id
	 * 
	 * @param uid the unique id of the object
	 */
	public void setUID(String uid) {
		this.uid = uid;
	}

	/**
	 * @return
	 */
	public String getUID() {
		return this.uid;
	}

	/**
	 * @param hiddenfields list of hidden fields
	 * @return generates an element to send a compact array
	 */
	public MessageArrayStart generateHeader(HashMap<String, NamedInterface> hiddenfields) {
		if (hiddenfields == null)
			hiddenfields = new HashMap<String, NamedInterface>();
		ArrayList<MessageFieldSpec> fields = new ArrayList<MessageFieldSpec>();
		fields.add(new MessageFieldSpec("NAM", MessageFieldTypeString.singleton));
		fields.add(new MessageFieldSpec("TYP", MessageFieldTypeString.singleton));
		fields.add(new MessageFieldSpec("UID", MessageFieldTypeString.singleton));
		fields.add(new MessageFieldSpec("FRZ", MessageFieldTypeBoolean.singleton));
		for (int i = 0; i < objectfields.getSize(); i++) {
			if (hiddenfields.get(objectfields.get(i).getName()) == null) {
				SimpleDataElt field = objectfields.get(i);
				fields.add(field.getMessageFieldSpec());
			}
		}
		MessageArrayStart arraystart = new MessageArrayStart(this.getName(), fields);
		return arraystart;
	}

	/**
	 * gernerates a compact line of array
	 * 
	 * @param header       relevant header
	 * @param hiddenfields hiden fields
	 * @return an array line
	 */
	public MessageArrayLine generateLine(MessageArrayStart header, HashMap<String, NamedInterface> hiddenfields) {
		if (hiddenfields == null)
			hiddenfields = new HashMap<String, NamedInterface>();
		ArrayList<Object> payload = new ArrayList<Object>();
		payload.add(this.getName());
		payload.add(this.getType().printType());
		payload.add(uid != null ? uid : "");
		payload.add(this.frozen);
		for (int i = 0; i < objectfields.getSize(); i++) {
			if (hiddenfields.get(objectfields.get(i).getName()) == null) {
				SimpleDataElt field = objectfields.get(i);
				payload.add(field.getMessageArrayValue());
			}
		}
		MessageArrayLine line = new MessageArrayLine(header, payload.toArray(new Object[0]));
		return line;
	}

	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		if (this.uid == null)
			this.uid = "";
		writer.addStringField("UID", this.uid);
		writer.addBooleanField("FRZ", this.frozen);
		writer.startStructure("FLDS");

		if (hiddenfields == null)
			hiddenfields = new HashMap<String, NamedInterface>();
		String trace = "hiddenfieldrop[";
		for (int z = 0; z < hiddenfields.size(); z++)
			trace += hiddenfields.keySet().toArray()[z];
		trace += "]";
		Logger.getLogger("").finest(trace);

		for (int i = 0; i < objectfields.getSize(); i++) {
			Logger.getLogger("").finest("Field name = " + objectfields.get(i).getName());
			if (hiddenfields.get(objectfields.get(i).getName()) == null) {
				writer.startStructure("FLD");
				objectfields.get(i).writeToMessage(writer);
				writer.endStructure("FLD");
			}
		}
		writer.endStructure("FLDS");
		writer.endStructure("DELT");

	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {

		this.uid = reader.returnNextStringField("UID");
		this.frozen = reader.returnNextBooleanField("FRZ");
		reader.startStructureArray("FLD");
		while (reader.structureArrayHasNextElement("FLD")) {
			DataElt thiselement = DataElt.readFromCML(reader);
			if (!(thiselement instanceof SimpleDataElt))
				throw new RuntimeException(String.format("expected a simple data element in object %s, got %s ",
						this.getName(), thiselement.toString()));
			objectfields.add((SimpleDataElt) thiselement);
			reader.returnNextEndStructure("FLD");
		}

	}

	@Override
	public boolean equals(Object other) {
		logger.fine(" ---------- Comparing objects ----- ");
		if (other == null)
			return false;
		if (!(other instanceof ObjectDataElt))
			return false;
		ObjectDataElt otherobject = (ObjectDataElt) other;
		// comparing objects starting by field on other object
		for (int i = 0; i < otherobject.fieldnumber(); i++) {
			SimpleDataElt otherobjectelement = otherobject.getField(i);
			SimpleDataElt thisobjectelement = this.objectfields.lookupOnName(otherobjectelement.getName());
			if (thisobjectelement == null)
				return false;
			if (!thisobjectelement.equals(otherobjectelement))
				return false;
			if (otherobjectelement != null)
				logger.fine("      - " + otherobjectelement.getName() + " is the same "
						+ thisobjectelement.defaultTextRepresentation() + " - "
						+ otherobjectelement.defaultTextRepresentation());
		}
		// comparing objects starting by fields on this object. Only check that fields
		// exist. Field that exist
		// have same value
		for (int i = 0; i < this.fieldnumber(); i++) {
			SimpleDataElt thisobjectelement = this.getField(i);
			SimpleDataElt otherobjectelement = otherobject.objectfields.lookupOnName(thisobjectelement.getName());
			if (otherobjectelement == null)
				return false;
		}

		// no dissimilarity found - returns true
		return true;
	}

	/**
	 * @param newname name of the deep copy
	 * @return a copy of the object
	 */
	public ObjectDataElt deepcopy(String newname) {
		NamedList<SimpleDataElt> copypayload = new NamedList<SimpleDataElt>();
		for (int i = 0; i < this.fieldnumber(); i++) {
			SimpleDataElt originalfield = this.getField(i);
			SimpleDataElt copyfield = originalfield.cloneElt();
			copypayload.add(copyfield);
		}
		String name = this.getName();
		if (newname != null)
			name = newname;
		ObjectDataElt returnobject = new ObjectDataElt(name, (ObjectDataEltType) this.getType(), copypayload);
		returnobject.setUID(this.getUID());
		return returnobject;
	}

	/**
	 * @return a depp copy of the object with the same name
	 */
	public ObjectDataElt deepcopy() {
		return deepcopy(null);

	}

	public void writeFieldSpecs(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		if (hiddenfields == null)
			hiddenfields = new HashMap<String, NamedInterface>();
		for (int i = 0; i < objectfields.getSize(); i++) {
			if (hiddenfields.get(objectfields.get(i).getName()) == null) {
				SimpleDataElt field = objectfields.get(i);
				field.WriteSpecToMessage(writer);
			}
		}

	}

}
