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
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.stage.Window;
import javafx.scene.layout.Border;

/**
 * A collapsible band is a widget that displays a title and a section below that
 * can be collapsed (hidden)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CCollapsibleBand
		extends
		CPageNode
		implements
		CollapsibleNode {
	private String title;
	private boolean openbydefault;
	private CPageNode payload;
	private TitledPane collapsiblepane;
	private boolean closewheninlineactioninside;

	/**
	 * Creates a collapsible band from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath path of the parent of this widget in the page widget node
	 * @throws OLcRemoteException
	 * @throws IOException
	 */
	public CCollapsibleBand(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.title = reader.returnNextStringField("TTL");
		this.openbydefault = reader.returnNextBooleanField("OBD");
		this.closewheninlineactioninside = reader.returnNextBooleanField("CII");
		reader.returnNextStartStructure("PLD");
		payload = CPageNode.parseNode(reader, this.nodepath);
		reader.returnNextEndStructure("PLD");
		reader.returnNextEndStructure("CLB");
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
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		Node payloadnode = payload.getNode(actionmanager, inputdata, parentwindow, parenttabpanes,
				(closewheninlineactioninside ? this : null));
		collapsiblepane = new TitledPane(this.title, payloadnode);
		collapsiblepane.setCollapsible(true);
		collapsiblepane.setExpanded(this.openbydefault);
		collapsiblepane.setBorder(Border.EMPTY);
		collapsiblepane.setAnimated(false);

		return collapsiblepane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void mothball() {
		collapsiblepane.setContent(null);
		payload.mothball();
	}

	@Override
	public void collapse() {
		collapsiblepane.setExpanded(false);

	}

}
