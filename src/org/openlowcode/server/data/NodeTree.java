/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.tools.structure.ObjectTreeDataElt;

/**
 * The node tree stores a hierarchy of objects
 *
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object represented in the node tree
 */

public class NodeTree<E extends DataObject<E>> {
	// note: parent can be null provided no children is null.
	private static Logger logger = Logger.getLogger(NodeTree.class.getName());
	// efficient structure to transport a tree, while not duplicating nodes
	// at several places in the structure
	private HashMap<String, E> objectregister;
	private String rootnodeid;
	private HashMap<String, ArrayList<String>> linkregister;
	private DataObjectDefinition<E> type;
	private int nexttransientid = 0;

	/**
	 * generates transient id for objects when required
	 * 
	 * @return a generated transient id local to the node tree
	 */
	public String generateNextTransientId() {
		String id = "NODETREE" + nexttransientid;
		nexttransientid++;
		return id;
	}

	/**
	 * creates an empty node tree for the given type
	 * 
	 * @param type type of data object definition
	 */
	public NodeTree(DataObjectDefinition<E> type) {

		this.type = type;
		this.objectregister = new HashMap<String, E>();
		this.linkregister = new HashMap<String, ArrayList<String>>();
		this.linkregister.put(null, new ArrayList<String>());
	}

	private String getObjectId(E object) {
		if (object == null)
			return null;
		String id = null;
		if (object instanceof UniqueidentifiedInterface) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			UniqueidentifiedInterface<E> objectui = ((UniqueidentifiedInterface) object);
			id = objectui.getId().getId();
		} else {
			if (object.getTransientid() == null)
				object.setTransientid(generateNextTransientId());
			id = object.getTransientid();
		}
		logger.fine("GETOBJECTID = " + id + " for " + object.dropToString());
		return id;
	}

	private void addNode(E object) {
		String id = getObjectId(object);

		if (this.objectregister.get(id) != null)
			throw new RuntimeException("object already exists in tree " + id);

		this.objectregister.put(id, object);
		logger.fine("ADD OBJECT ID=" + id + " for " + object.dropToString());
		this.linkregister.put(id, new ArrayList<String>());
	}

	/**
	 * creates a node tree with the given object as root node
	 * 
	 * @param parent root node of the tree
	 */
	public NodeTree(E parent) {
		if (parent == null)
			throw new RuntimeException(
					"when creating a nodetree with null content, you should use NodeTree(DataObjectDefinition) constructor");
		this.type = parent.getDefinitionFromObject();
		this.objectregister = new HashMap<String, E>();
		this.linkregister = new HashMap<String, ArrayList<String>>();

		addNode(parent);
		this.rootnodeid = getObjectId(parent);
		logger.fine("ADD ROOTNODEID = " + this.rootnodeid);
	}

	/**
	 * creates a link between the parent and the child. The parent should already be
	 * in the tree
	 * 
	 * @param parent parent object
	 * @param child  child object
	 * @return true if the object is added for the first time in the link, false if
	 *         the object is not yet added
	 */
	public boolean addChild(E parent, E child) {

		String parentid = getObjectId(parent);
		String childid = getObjectId(child);

		if (parent != null)
			if (this.objectregister.get(parentid) == null)
				throw new RuntimeException("object does not already exists in tree " + parentid);

		logger.fine("ADDING PARENT " + parentid + " CHILD " + childid);
		this.linkregister.get(parentid).add(childid);
		if (this.objectregister.get(childid) == null) {
			logger.fine("ADDING NEW CHILD IN OBJ REGISTER " + childid);
			this.addNode(child);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * get the number of children for the given parent
	 * 
	 * @param parent parent
	 * @return the number of children
	 */
	public int getChildrenNumber(E parent) {
		String parentid = getObjectId(parent);
		if (this.objectregister.get(parentid) == null)
			throw new RuntimeException("object does not already exists in tree " + parentid);
		return linkregister.get(parentid).size();
	}

	/**
	 * get the child at the given index for the given parent
	 * 
	 * @param parent parent
	 * @param index  index between 0 (included) and getChildrenNumber (excluded)
	 * @return the child at the given index
	 */
	public E getChild(E parent, int index) {
		String parentid = getObjectId(parent);
		if (this.objectregister.get(parentid) == null)
			throw new RuntimeException("object does not already exists in tree " + parentid);
		return objectregister.get(linkregister.get(parentid).get(index));
	}

	/**
	 * get the root object for the tree
	 * 
	 * @return the root object for the tree
	 */
	public E getRoot() {
		if (this.rootnodeid == null)
			return null;
		return objectregister.get(this.rootnodeid);
	}

	/**
	 * generates an object tree data element
	 * 
	 * @param name name of the data element
	 * @return and object tree data element
	 */
	public ObjectTreeDataElt<TObjectDataElt<E>> generateObjectTreeDataElt(String name) {
		logger.fine("generating object tree with objects = " + this.objectregister.size() + ", link = "
				+ this.linkregister.size() + ", rootid=" + this.rootnodeid);

		ObjectTreeDataElt<TObjectDataElt<E>> tree = new ObjectTreeDataElt<TObjectDataElt<E>>(name,
				new TObjectDataEltType<E>(type));

		Iterator<E> objectiterator = objectregister.values().iterator();
		while (objectiterator.hasNext()) {
			E object = objectiterator.next();
			String objectid = getObjectId(object);
			ArrayList<String> textlinkregister = this.linkregister.get(objectid);

			if (objectid.equals(rootnodeid)) {
				logger.fine(" - adding object tree as root with " + objectid);
				tree.addNodeAsRoot(new TObjectDataElt<E>(name, object), textlinkregister);
			} else {
				logger.fine(" - adding object tree " + objectid);

				tree.addNode(new TObjectDataElt<E>(name, object), textlinkregister);
			}

		}
		ArrayList<String> linksfornull = this.linkregister.get(null);

		if (rootnodeid != null)
			if (linksfornull != null)
				if (linksfornull.size() > 0)
					throw new RuntimeException("Non null root node exist, but link from nullid exist also");
		if (linksfornull != null)
			if (linksfornull.size() > 0) {
				ArrayList<String> linkchildrenidarray = new ArrayList<String>();
				for (int i = 0; i < linksfornull.size(); i++)
					linkchildrenidarray.add(linksfornull.get(i));
				tree.addLinksForNullRootNode(linkchildrenidarray);
			}
		return tree;
	}
}
