/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

/**
 * A widget showing a band of objects one behind the other, similarly to
 * comments on a web page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectBand
		extends
		CPageNode {

	@SuppressWarnings("unused")
	private String name;
	private ArrayList<CBusinessField<?>> payloadlist;
	private CPageNode actiongroup;
	private CPageDataRef datareference;
	private ArrayDataElt<ObjectDataElt> dataarray;
	/**
	 * active object in the band for which an action will be received soon;
	 */
	private int index = -1;

	/**
	 * creates an object band from the message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectBand(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		String startstructure = reader.returnNextStartStructure();
		if (startstructure.compareTo("ACNGRP") == 0) {
			actiongroup = CPageNode.parseNode(reader, this.nodepath);
			reader.returnNextEndStructure("ACNGRP");
			startstructure = reader.returnNextStartStructure();
		}
		if (startstructure.compareTo("ATTRS") != 0)
			throw new RuntimeException("Error, invalid structure in message, as expecting ATTRS, got " + startstructure
					+ ", " + reader.getCurrentElementPath());
		payloadlist = new ArrayList<CBusinessField<?>>();
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		this.datareference = CPageDataRef.parseCPageDataRef(reader);

		reader.returnNextEndStructure("OBJBND");

	}

	/**
	 * get an array of object elements
	 * 
	 * @param inputdata page data
	 * @param dataref   reference to the page data element that should be parsed
	 * @return the parsed array of object data elements
	 */
	public ArrayDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thiselementarray;
	}

	private class CObjectBandCallback
			implements
			Callback {
		private int index;

		public CObjectBandCallback(int index) {
			this.index = index;
		}

		@Override
		public void callback() {
			CObjectBand.this.setActionIndex(index);
		}

	}

	/**
	 * index in the object band
	 * 
	 * @param index the index of the current object
	 */
	public void setActionIndex(int index) {
		this.index = index;
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		dataarray = getExternalContent(inputdata, datareference);
		Pane objectbandpane = CComponentBand.returnBandPane(CComponentBand.DIRECTION_DOWN);

		for (int i = 0; i < this.dataarray.getObjectNumber(); i++) {
			ObjectDataElt thisobject = this.dataarray.getObjectAtIndex(i);
			Node thisobjectnode = CObjectDisplay.generateObjectDisplay(thisobject, this.nodepath, payloadlist, false,
					true, actionmanager, null, null, inputdata, parentwindow, parenttabpanes);
			objectbandpane.getChildren().add(thisobjectnode);
			Callback callback = new CObjectBandCallback(i);
			if (actiongroup != null) {
				CPageNode thisactiongroup = actiongroup.deepcopyWithCallback(callback);
				objectbandpane.getChildren()
						.add(thisactiongroup.getNode(actionmanager, inputdata, parentwindow, parenttabpanes));
			}
		}

		return objectbandpane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof ObjectIdDataEltType) {
			if (objectdataloc == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			ObjectDataElt object = this.dataarray.getObjectAtIndex(index);
			SimpleDataElt field = object.lookupEltByName(objectdataloc);
			if (field == null)
				throw new RuntimeException(
						"field not found " + objectdataloc + ", available fields = " + object.dropFieldNames());
			if (!(field instanceof TextDataElt))
				throw new RuntimeException("field for name = " + objectdataloc + " is not text");
			TextDataElt textfield = (TextDataElt) field;
			ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
			return objectid;
		}
		throw new RuntimeException("Unsupported extraction type  " + type);
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {

	}
}
