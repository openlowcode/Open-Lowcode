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
import java.util.Arrays;

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
import javafx.stage.Window;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.Region;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.paint.Color;

/**
 * a widget representing a tab pane, holding a page node in each tab
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CTabPane
		extends
		CPageNode {

	private ArrayList<CPageNode> elements;
	private ArrayList<String> tabtitles;

	/**
	 * create the tab pane from the server message
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CTabPane(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		elements = new ArrayList<CPageNode>();
		tabtitles = new ArrayList<String>();
		reader.startStructureArray("ELT");
		while (reader.structureArrayHasNextElement("ELT")) {
			String title = reader.returnNextStringField("TABNAME");
			tabtitles.add(title);
			CPageNode nodeelement = CPageNode.parseNode(reader, this.nodepath);
			elements.add(nodeelement);
			reader.returnNextEndStructure("ELT");
		}
		reader.returnNextEndStructure("TABPANE");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet Implemented");
	}

	private TabPane tabpane;

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {

		this.tabpane = new TabPane();
		this.tabpane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
		for (int i = 0; i < elements.size(); i++) {
			Tab tab = new Tab();

			tab.setText(tabtitles.get(i));
			TabPane[] newtabarray = Arrays.copyOf(parenttabpanes, parenttabpanes.length + 1, TabPane[].class);
			newtabarray[parenttabpanes.length] = this.tabpane;
			Node node = elements.get(i).getNode(actionmanager, inputdata, parentwindow, newtabarray);
			if (node instanceof Region) {
				Region region = (Region) node;
				region.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, Color.RED, Color.RED, Color.RED,
						BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
						CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
				region.heightProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue,
							Number newValue) {
						double newheightfortabpane = newValue.doubleValue() + 20;
						double oldheight = tabpane.getHeight();
						if (newheightfortabpane > oldheight) {
							logger.severe("Setting min height to " + newValue.doubleValue() + 20 + ", current height = "
									+ tabpane.getHeight());
							tabpane.setMinHeight(newValue.doubleValue() + 20);
							tabpane.setPrefHeight(newValue.doubleValue() + 20);
							tabpane.setMaxHeight(newValue.doubleValue() + 20);
							region.requestLayout();
							if (tabpane.getParent() != null)
								tabpane.getParent().requestLayout();
							logger.severe("Finished layout stuff");
						}

					}

				});

			}
			tab.setContent(node);
			tabpane.getTabs().add(tab);

		}
		return tabpane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException(
				String.format("request of action data of type %s, but CTabPane cannot provide any data", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public void mothball() {
		tabpane.getTabs().clear();

		for (int i = 0; i < elements.size(); i++)
			elements.get(i).mothball();

	}

}
