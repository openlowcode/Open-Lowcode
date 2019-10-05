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

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * a data element that stores the uniqueid of an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectIdDataElt extends SimpleDataElt {
	private String id;
	private String objectid;

	/**
	 * @return the id of the individual object
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @return the type of the object
	 */
	public String getObjectId() {
		return this.objectid;
	}

	/**
	 * creates an empty objectid data element
	 * 
	 * @param name name of the element
	 */
	public ObjectIdDataElt(String name) {
		super(name, new ObjectIdDataEltType());
	}

	/**
	 * @param name     name of the element
	 * @param id       id of the individual (instance) object
	 * @param objectid type of object (id of the class of object)
	 */
	public ObjectIdDataElt(String name, String id, String objectid) {
		super(name, new ObjectIdDataEltType());
		this.id = id;
		this.objectid = objectid;
	}

	/**
	 * @param name name of the element
	 * @param id   if of the invidicual (instance) object
	 */
	public ObjectIdDataElt(String name, String id) {
		super(name, new ObjectIdDataEltType());
		this.id = id;

	}

	/**
	 * @param name     name of the element
	 * @param objectid a specific object with the interface of the Object Id
	 */
	public ObjectIdDataElt(String name, ObjectIdInterface objectid) {
		super(name, new ObjectIdDataEltType());
		this.id = objectid.getId();
		this.objectid = objectid.getObjectId();
	}

	private ObjectIdDataElt(String name, ObjectIdDataEltType type, String id) {
		super(name, type);
		this.id = id;
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addStringField("PLD", this.id);
		writer.addStringField("OBI", this.objectid);

	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.id = reader.returnNextStringField("PLD");
		this.objectid = reader.returnNextStringField("OBI");

	}

	@Override
	public String defaultTextRepresentation() {
		return this.id;
	}

	@Override
	public ObjectIdDataElt cloneElt() {
		return new ObjectIdDataElt(this.getName(), (ObjectIdDataEltType) this.getType(), this.id);
	}

	@Override
	public void forceContent(String constraintvalue) {
		throw new RuntimeException("not yet implemented");

	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (!(other instanceof ObjectIdDataElt))
			return false;
		ObjectIdDataElt parseddataelt = (ObjectIdDataElt) other;
		if (!this.id.equals(parseddataelt.id))
			return false;
		if (!this.objectid.equals(parseddataelt.objectid))
			return false;
		return true;
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(), MessageFieldTypeString.singleton);
	}

	@Override
	protected Object getMessageArrayValue() {
		return this.id;
	}
}
