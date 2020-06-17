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
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

/**
 * An invisible widget to store text on a page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CTextStorage extends CPageNode {
	@SuppressWarnings("unused")
	private String name;
	private CPageDataRef datareference;
	public TextDataElt payload;
	/**
	 * create a text storage from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CTextStorage(MessageReader reader, CPageSignifPath parentpath)
			throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		reader.returnNextEndStructure("TEXTST"); 
	}

	@Override
	public Node getNode(PageActionManager actionmanager, CPageData inputdata, Window parentwindow,TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		// gets id data
		DataElt thiselement = inputdata.lookupDataElementByName(datareference.getName());
		if (thiselement==null) throw new RuntimeException("could not find any page data with name = "+datareference.getName());
		if (!thiselement.getType().equals(datareference.getType())) throw new RuntimeException(String.format("page data with name = %s does not have expected %s type, actually found %s",datareference.getName(),datareference.getType(),thiselement.getType()));
		TextDataElt thisidelement = (TextDataElt) thiselement;
		this.payload = thisidelement;
		// returns null as a widget
		return null;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname,
			String objectdataloc)  {
		if (type instanceof TextDataEltType) {
			if (objectdataloc!=null) throw new RuntimeException("indicated objectfieldname = '"+objectdataloc+"', but the field is not supporting this parameter");
			return new TextDataElt(eltname,payload.getPayload());
		}
		throw new RuntimeException(String.format("Unsupported extraction type %s ",type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt)  {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}
	@Override
	public CPageNode deepcopyWithCallback(
			Callback callback)  {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball()  {
	}
}
