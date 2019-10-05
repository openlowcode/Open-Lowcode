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
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageArrayEnd;
import org.openlowcode.tools.messages.MessageArrayLine;
import org.openlowcode.tools.messages.MessageArrayStart;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;

/**
 * A tree of objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class ObjectTreeDataElt<E extends ObjectDataElt> extends DataElt {
	private static Logger logger = Logger.getLogger(ObjectTreeDataElt.class.getName());
	private HashMap<String, E> objectregister;
	private String rootnodeid;
	private HashMap<String, ArrayList<String>> linkregister;
	private ObjectDataEltType treeobjecttype;
	private HashMap<String, NamedInterface> hiddenelement;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectTreeDataElt(String name, ObjectDataEltType treetype) {
		super(name, new ObjectTreeDataEltType(treetype));
		this.treeobjecttype = treetype;
		this.objectregister = new HashMap<String, E>();
		this.linkregister = new HashMap<String, ArrayList<String>>();
		hiddenelement = new HashMap<String, NamedInterface>();

	}

	private void addObjectElt(E object) {

		String objectid = object.getUID();
		if (objectregister.get(objectid) != null)
			throw new RuntimeException("Node already exists id=" + objectid);
		objectregister.put(objectid, object);
		linkregister.put(objectid, new ArrayList<String>());
	}

	/**
	 * @param name     name of the element
	 * @param treetype type of objects in the tree
	 * @param rootnode root of the tree
	 */
	public ObjectTreeDataElt(String name, ObjectDataEltType treetype, E rootnode) {
		this(name, treetype);

		addObjectElt(rootnode);
		this.rootnodeid = rootnode.getUID();
	}

	@Override
	public void changeName(String name) {
		super.changeName(name);
		Iterator<E> objectiterator = objectregister.values().iterator();
		while (objectiterator.hasNext()) {
			objectiterator.next().changeName(name);
		}
	}

	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		writer.addStringField("STP", this.treeobjecttype.printType());
		writePayloadToCML(writer, hiddenfields);
		writer.endStructure("DELT");
	}

	private void writePayloadToCML(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields)
			throws IOException {
		writer.addStringField("RNI", this.rootnodeid);
		if (objectregister.values().size() == 0) {
			writer.addBooleanField("HPL", false);
		} else {
			writer.addBooleanField("HPL", true);
			treeobjecttype.writeCompactArray(new ArrayList<ObjectDataElt>(objectregister.values()), writer,
					hiddenfields);

		}

		Iterator<Entry<String, ArrayList<String>>> linkiterator = this.linkregister.entrySet().iterator();

		ArrayList<MessageFieldSpec> linkfieldspec = new ArrayList<MessageFieldSpec>();
		linkfieldspec.add(new MessageFieldSpec("LEFTID", MessageFieldTypeString.singleton));
		linkfieldspec.add(new MessageFieldSpec("RIGHTID", MessageFieldTypeString.singleton));

		MessageArrayStart linkstart = new MessageArrayStart("LINKS", linkfieldspec);
		writer.sendMessageElement(linkstart);

		while (linkiterator.hasNext()) {
			Entry<String, ArrayList<String>> thislinktable = linkiterator.next();
			ArrayList<String> rightlinks = thislinktable.getValue();
			for (int i = 0; i < rightlinks.size(); i++) {
				writer.sendMessageElement(
						new MessageArrayLine(linkstart, new Object[] { thislinktable.getKey(), rightlinks.get(i) }));
			}
		}
		writer.sendMessageElement(new MessageArrayEnd());

	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.rootnodeid = reader.returnNextStringField("RNI");
		boolean haspayload = reader.returnNextBooleanField("HPL");
		if (haspayload) {
			ArrayList<ObjectDataElt> objects = new ArrayList<ObjectDataElt>();
			this.treeobjecttype.readCompactArray(objects, reader);
			for (int i = 0; i < objects.size(); i++) {
				this.objectregister.put(objects.get(i).getUID(), (E) objects.get(i));
				this.linkregister.put(objects.get(i).getUID(), new ArrayList<String>());
			}
		}

		if (this.rootnodeid != null)
			if (this.objectregister.get(rootnodeid) == null)
				throw new RuntimeException("no object provided for root id " + rootnodeid);
		if (this.rootnodeid == null)
			this.linkregister.put(null, new ArrayList<String>());
		MessageElement element = reader.getNextElement();
		if (!(element instanceof MessageArrayStart))
			throw new RuntimeException("Expecting an array message start");

		element = reader.getNextElement();
		while (element instanceof MessageArrayLine) {
			MessageArrayLine thisline = (MessageArrayLine) element;
			if (thisline.getObjectNumber() != 2)
				throw new RuntimeException("Expecting array lines with 2 elements, got " + thisline.getObjectNumber());
			String lfid = (String) thisline.getPayloadAt(0);
			String rgid = (String) thisline.getPayloadAt(1);
			if (lfid != null)
				if (this.objectregister.get(lfid) == null)
					throw new RuntimeException("no object is provided for this left id " + lfid);
			if (this.objectregister.get(rgid) == null)
				throw new RuntimeException("no object is provided for this right id " + rgid + " for leftid " + lfid);
			ArrayList<String> rgidforlfid = linkregister.get(lfid);
			rgidforlfid.add(rgid);
			element = reader.getNextElement();
		}
		if (!(element instanceof MessageArrayEnd))
			throw new RuntimeException("Expects a MessageArrayEnd, got a " + element.getClass());

	}

	/**
	 * @param tObjectDataElt object data element
	 * @param childrenid     id of the children
	 */
	public void addNodeAsRoot(E tObjectDataElt, ArrayList<String> childrenid) {
		this.addNode(tObjectDataElt, childrenid);
		this.rootnodeid = tObjectDataElt.getUID();
	}

	/**
	 * @param tObjectDataElt object data element
	 * @param childrenid     id of the children
	 */
	public void addNode(E tObjectDataElt, ArrayList<String> childrenid) {
		if (!(this.objectregister.containsKey(tObjectDataElt.getUID()))) {
			logger.fine("adding object " + tObjectDataElt.getUID() + ", with links = "
					+ (childrenid != null ? childrenid.size() : "null"));
			this.addObjectElt(tObjectDataElt);
			linkregister.put(tObjectDataElt.getUID(), childrenid);
		} else {
			logger.fine("Skipping object " + tObjectDataElt.getUID());
		}
	}

	/**
	 * @param childrenid adds children to a tree without a root node
	 */
	public void addLinksForNullRootNode(ArrayList<String> childrenid) {
		linkregister.put(null, childrenid);
	}

	/**
	 * @param marker marker for fields to hide
	 */
	public void hideElement(NamedInterface marker) {
		hiddenelement.put(marker.toString(), marker);

	}

	/**
	 * @return the id of the root element
	 */
	public String getRootId() {
		return this.rootnodeid;
	}

	/**
	 * @param objectid the id of the object instance
	 * @return the object data element
	 */
	public ObjectDataElt getObject(String objectid) {
		return this.objectregister.get(objectid);
	}

	/**
	 * @param parentid id of the parent
	 * @return the chidren of the parent as provided by the id
	 */
	public ArrayList<String> getChildrenId(String parentid) {
		return this.linkregister.get(parentid);
	}

	/**
	 * @return the number of objects stored
	 */
	public int getObjectNr() {
		return this.objectregister.size();
	}

	/**
	 * @return an iterator to all of of the objects
	 */
	public Iterator<E> getAllObjectIterator() {
		return this.objectregister.values().iterator();
	}
}
