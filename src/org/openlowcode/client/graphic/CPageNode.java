/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic;

import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;

import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.stage.Window;

/**
 * A graphical element of the page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class CPageNode {
	protected static Logger logger = Logger.getLogger(CPageNode.class.getName());
	private static CPageNodeCatalog catalog;

	/**
	 * before using the CPageNode methods, especially the static method parseNode
	 * 
	 * @param catalog the widget catalog to use.
	 */
	public static void setPageCatelog(CPageNodeCatalog catalog) {
		CPageNode.catalog = catalog;
	}

	/**
	 * @param callback the callback method, may be null
	 * @return a deep copy of the CPageNode with callback specified set
	 */
	public abstract CPageNode deepcopyWithCallback(Callback callback);

	protected CPageSignifPath nodepath;

	/**
	 *
	 * @param actionmanager  the action manager on which to register all widgets
	 *                       that trigger an action
	 * @param inputdata      the list of input data of the page
	 * @param parentwindow   the javafx window this widget will be drawn in
	 * @param parenttabpanes parenttabpanes that should be triggered a layout when
	 *                       this component resizes
	 * @return a displayable node (in JAVAFX)
	 */
	public abstract Node getNode(PageActionManager actionmanager, CPageData inputdata, Window parentwindow,
			TabPane[] parenttabpanes);

	/**
	 * Returns a filled data element of the specified data element type. If there is
	 * a format mismatch, an exception is thrown
	 * 
	 * @param type          requested type (will be tested by the widget to see if
	 *                      valid)
	 * @param eltname       the name that will be put to the data element
	 * @param objectdataloc
	 * @return
	 */
	public abstract DataElt getDataElt(DataEltType type, String eltname, String objectdataloc);

	private CPageSignifPath parentpath;
	private String significantpath;

	/**
	 * @return parent path to the node (the path is used to link widgets (nodes) and
	 *         data
	 */
	public CPageSignifPath getParentpath() {
		return parentpath;
	}

	/**
	 * @return the path of this element
	 */
	public CPageSignifPath getPath() {
		return this.nodepath;
	}

	/**
	 * @return the string output of the significant path of this node (element in
	 *         the path, equivalent to current folder name)
	 */
	public String getSignificantpath() {
		return significantpath;
	}

	/**
	 * creates a page node
	 * 
	 * @param parentpath      parent path for the node
	 * @param significantpath if this node has a significant path, a non null string
	 */
	public CPageNode(CPageSignifPath parentpath, String significantpath) {
		this.parentpath = parentpath;
		this.significantpath = significantpath;
		if (significantpath != null)
			if (significantpath.length() > 0) {
				CPageSignifPath newpath = new CPageSignifPath(significantpath, null, parentpath, this);

				nodepath = newpath;
			} else {
				nodepath = parentpath;
			}

	}

	/**
	 * @param reader     message reader
	 * @param parentpath parent path element
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if anything bad happens during the transmission
	 */
	public CPageNode(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		this(parentpath, reader.returnNextStringField("SPT"));
	}

	/**
	 * @param reader     reader
	 * @param parentpath path element
	 * @return the parsed page node
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if anything bad happens during the transmission
	 */
	public static CPageNode parseNode(MessageReader reader, CPageSignifPath parentpath)
			throws OLcRemoteException, IOException {
		if (catalog == null)
			throw new RuntimeException(
					"PageNode catalog is not initialized. Please call the method CPageCatalog.setPageCatelog()");
		String structure = reader.returnNextStartStructure();
		return catalog.getNodeFromCode(structure, reader, parentpath);

	}

	/**
	 * @param dataelt adds data element to the node
	 */
	public abstract void forceUpdateData(DataElt dataelt);

	/**
	 * cleans the page node. Due to the complex structure, some unlinking of
	 * different classes is needed to facilitate garbage collection after page is
	 * discarded
	 */
	public abstract void mothball();

}
