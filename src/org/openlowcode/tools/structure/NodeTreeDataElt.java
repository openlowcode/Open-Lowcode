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

/**
 * An element that can store a hierarchical tree. It is actually recursive
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E>
 */
public class NodeTreeDataElt<E extends DataElt> extends DataElt {
	private E payload;
	private ArrayList<NodeTreeDataElt<E>> children;
	private HashMap<String, NamedInterface> hiddenelement;
	private DataEltType nodetreeelementtype;

	@SuppressWarnings("rawtypes")
	public NodeTreeDataElt(String name, DataEltType type, E payload) {
		super(name, new NodeTreeDataEltType());
		this.payload = payload;
		this.children = new ArrayList<NodeTreeDataElt<E>>();
		hiddenelement = new HashMap<String, NamedInterface>();
		this.nodetreeelementtype = type;
	}

	@SuppressWarnings("rawtypes")
	public NodeTreeDataElt(String name, DataEltType dataelttype) {
		super(name, new NodeTreeDataEltType());
		this.children = new ArrayList<NodeTreeDataElt<E>>();
		hiddenelement = new HashMap<String, NamedInterface>();
		this.nodetreeelementtype = dataelttype;

	}

	/**
	 * @return payload the node payload
	 */
	public E getPayload() {
		return payload;
	}

	/**
	 * @return the number of children
	 */
	public int getChildrenNumber() {
		return children.size();
	}

	/**
	 * @param i
	 * @return the child element at the given index
	 */
	public NodeTreeDataElt<E> getChild(int i) {
		return children.get(i);
	}

	/**
	 * @param child adds a child to this node
	 */
	public void addChild(NodeTreeDataElt<E> child) {
		this.children.add(child);
	}

	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		writer.addStringField("STP", this.nodetreeelementtype.printType());
		writePayloadToMessage(writer, hiddenfields, 0);

		writer.endStructure("DELT");

	}

	private void writePayloadToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields,
			int circuitbreaker) throws IOException {
		if (circuitbreaker > 9999)
			throw new RuntimeException("recursive loops detected");
		if (payload == null) {
			writer.startStructure("NULPLD");
			writer.endStructure("NULPLD");

		} else {
			writer.startStructure("PLD");
			payload.writeToMessage(writer, hiddenfields);
			writer.endStructure("PLD");

		}

		writer.startStructure("CHDS");
		for (int i = 0; i < children.size(); i++) {
			writer.startStructure("CHD");
			children.get(i).writePayloadToMessage(writer, hiddenelement, circuitbreaker + 1);
			writer.endStructure("CHD");
		}
		writer.endStructure("CHDS");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		String start = reader.returnNextStartStructure();
		boolean treated = false;
		if (start.compareTo("PLD") == 0) {

			DataElt payloadelt = DataElt.readFromCML(reader);
			if (payloadelt.getName().compareTo(this.getName()) != 0)
				throw new RuntimeException(String.format(
						"try to add objects with incompatible names in an array, array name : %s, object name: %s",
						this.getName(), payloadelt.getName()));
			if (!payloadelt.getType().equals(nodetreeelementtype))
				throw new RuntimeException(String.format(
						"try to add objects with incompatible types in an array with name %s, array type: %s, object type: %s ",
						this.getName(), this.getType(), payloadelt.getType()));
			this.payload = (E) payloadelt;

			reader.returnNextEndStructure("PLD");
			treated = true;
		}
		if (start.compareTo("NULPLD") == 0) {
			reader.returnNextEndStructure("NULPLD");
			treated = true;
		}
		if (!treated)
			throw new RuntimeException("Label authorized is 'PLD' or 'NULPLD'");

		reader.startStructureArray("CHD");
		while (reader.structureArrayHasNextElement("CHD")) {
			NodeTreeDataElt thischild = new NodeTreeDataElt(this.getName(), nodetreeelementtype);
			this.children.add(thischild);
			thischild.addPayload(reader);
			reader.returnNextEndStructure("CHD");
		}

	}

	/**
	 * @param marker marker for fields to be hidden
	 */
	public void hideElement(NamedInterface marker) {
		hiddenelement.put(marker.toString(), marker);
	}

	@Override
	public void changeName(String name) {
		super.changeName(name);
		if (payload != null)
			payload.changeName(name);
		for (int i = 0; i < children.size(); i++)
			children.get(i).changeName(name);
	}

}
