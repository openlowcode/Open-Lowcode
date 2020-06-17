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

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.LargeBinaryDataElt;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

/**
 * a widget to download a file from the server when an inline action is
 * triggered on the page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CFileDownload
		extends
		CPageNode {
	private static Logger logger = Logger.getLogger(CFileDownload.class.getName());
	private SFile largefile;
	@SuppressWarnings("unused")
	private String id;
	@SuppressWarnings("unused")
	private CPageInlineAction filedownloadaction;
	private CInlineActionDataRef inlineactiondataref;
	@SuppressWarnings("unused")
	private Window parentwindow;

	/**
	 * creates a CFileDownload widget from the message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CFileDownload(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.id = reader.returnNextStringField("ID");
		reader.returnNextStartStructure("INLINEACTION");
		filedownloadaction = new CPageInlineAction(reader);
		this.inlineactiondataref = new CInlineActionDataRef(reader, this);
		reader.returnNextEndStructure("FLD");
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
		this.parentwindow = parentwindow;
		inputdata.addInlineActionDataRef(inlineactiondataref);
		return null;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		logger.fine(" --- FileDownload triggered --- ");
		// ------------------------- get data
		if (dataelt == null)
			throw new RuntimeException("data element is null :");
		if (!dataelt.getType().printType().equals(inlineactiondataref.getType()))
			throw new RuntimeException(
					String.format("inline data with name = %s does not have expected %s type, actually found %s",
							inlineactiondataref.getName(), inlineactiondataref.getType(), dataelt.getType()));
		LargeBinaryDataElt largefileelt = (LargeBinaryDataElt) dataelt;
		largefile = largefileelt.getPayload();

		// ------------------ Alternative: open file
		String defaultfilepath = System.getProperty("java.io.tmpdir") + largefile.getFileName();
		File defaultfile = new File(defaultfilepath);
		try {

			FileOutputStream fos = new FileOutputStream(defaultfile, false);
			fos.write(largefile.getContent());
			fos.close();
			Desktop.getDesktop().open(defaultfile);

		} catch (Exception e) {
			String message = "Error writing or opening file at path " + defaultfilepath + ", error = " + e.getMessage();
			logger.warning(message);
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning("    * " + e.getStackTrace()[i]);
			throw new RuntimeException(message);
		}

	}

	@Override
	public void mothball() {
	}

}
