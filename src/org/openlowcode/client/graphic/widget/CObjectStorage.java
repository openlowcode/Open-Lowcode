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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectDataEltType;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

/**
 * Creates a widget holding object data while being invisible
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectStorage
		extends
		CPageNode {
	private CPageDataRef datareference;
	private ObjectDataElt payload;
	@SuppressWarnings("unused")
	private String name;

	/**
	 * create an object storage widget from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectStorage(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAM");
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		reader.returnNextEndStructure("OBJSTO");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		DataElt thiselement = inputdata.lookupDataElementByName(datareference.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + datareference.getName());
		if (!thiselement.getType().equals(datareference.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							datareference.getName(), datareference.getType(), thiselement.getType()));
		ObjectDataElt thisobjectelement = (ObjectDataElt) thiselement;
		this.payload = thisobjectelement;
		// returns null as a widget
		return null;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof ObjectDataEltType) {
			if (objectdataloc != null)
				throw new RuntimeException("indicated objectfieldname = '" + objectdataloc
						+ "', but the field is not supporting this parameter");
			ObjectDataElt object = new ObjectDataElt(eltname);
			object.setUID(this.payload.getUID());
			for (int i = 0; i < payload.fieldnumber(); i++) {
				object.addField(payload.getField(i));
			}
			return object;
		}
		throw new RuntimeException("Unsupported extraction type  " + type);
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public void mothball() {

	}

}
