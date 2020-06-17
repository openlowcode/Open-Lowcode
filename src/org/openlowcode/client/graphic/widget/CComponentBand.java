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
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CComponentBand
		extends
		CPageNode {
	private int direction;
	private ArrayList<CPageNode> elements;
	/**
	 * horizontal band, adding widgets from left to right
	 */
	public static final int DIRECTION_RIGHT = 1;
	/**
	 * vertical band, adding widgets from top to bottom
	 */
	public static final int DIRECTION_DOWN = 2;
	/**
	 * vertical band, adding widgets from bottom to top
	 */
	public static final int DIRECTION_UP = 3;
	/**
	 * horizontal band, adding widgets from right to left
	 */
	public static final int DIRECTION_LEFT = 4;
	/**
	 * a component band with a direction right and no upper line in display
	 */
	public static final int DIRECTION_RIGHT_NOLINE = 5;
	
	private int minwidth = 0;

	/**
	 * sets a minimum width in pixels for the component band
	 * 
	 * @param minwidth minimum width in pixels
	 */
	public void setMinWidth(int minwidth) {
		this.minwidth = minwidth;
	}

	/**
	 * creates a component band with given direction and parent parent
	 * 
	 * @param direction  direction defined as static int on this class
	 * @param parentpath path of the parent widget
	 */
	public CComponentBand(int direction, CPageSignifPath parentpath) {
		super(parentpath, null);
		this.direction = direction;
		elements = new ArrayList<CPageNode>();

	}

	/**
	 * adds a node in the component band
	 * 
	 * @param node node to add
	 */
	public void addNode(CPageNode node) {
		this.elements.add(node);
	}

	/**
	 * Creates a component band from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath path of the parent widget
	 * @throws OLcRemoteException if anything happens on the server while sending
	 *                            the message
	 * @throws IOException        if any transmission error occurs during the
	 *                            message sending
	 */
	public CComponentBand(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		elements = new ArrayList<CPageNode>();
		String directionstring = reader.returnNextStringField("DIR");
		direction = new Integer(directionstring).intValue();
		reader.startStructureArray("ELT");
		while (reader.structureArrayHasNextElement("ELT")) {
			CPageNode nodeelement = CPageNode.parseNode(reader, this.nodepath);
			elements.add(nodeelement);
			reader.returnNextEndStructure("ELT");
		}

		reader.returnNextEndStructure("COMPONENTBAND"); // consumes the closing the component band

	}

	/**
	 * returns a pane similar to a CComponentBand (with similar spacing and insets)
	 * 
	 * @param direction direction as defined in the static ints in that class
	 * @return a pane with the given direction
	 */
	public static Pane returnBandPane(int direction) {
		Pane thispane=null;
		if (direction == DIRECTION_DOWN) {
			thispane = new VBox(8);
			thispane.setPadding(new Insets(5, 5, 5, 0));
			thispane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			return thispane;
		} 
		if (direction == DIRECTION_RIGHT) {
			
			thispane = new HBox(8);
			thispane.setPadding(new Insets(2, 0, 0, 0));
			((HBox)thispane).setAlignment(Pos.CENTER_LEFT);
			thispane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			thispane.setBorder(new Border(new BorderStroke(Color.LIGHTGREY, Color.LIGHTGREY, Color.LIGHTGREY, Color.LIGHTGREY,
			            BorderStrokeStyle.SOLID, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE, BorderStrokeStyle.NONE,
			            CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY)));
			return thispane;
		}
		
		if (direction == DIRECTION_RIGHT_NOLINE) {
			
			thispane = new HBox(8);
			((HBox)thispane).setAlignment(Pos.CENTER_LEFT);
			thispane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			
			return thispane;
		}
		
		throw new RuntimeException("Direction "+direction+" not supported");
		
	}

	private Pane bandpane;

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		Pane thispane = CComponentBand.returnBandPane(direction);
		this.bandpane = thispane;
		if (this.minwidth != 0)
			thispane.setMinWidth(this.minwidth);
		for (int i = 0; i < elements.size(); i++) {
			Node currentnode = elements.get(i).getNode(actionmanager, inputdata, parentwindow, parenttabpanes,nodetocollapsewhenactiontriggered);
			// if node is null (e.g. CObjectIdStorage), then do not add it to the graphic
			if (currentnode != null)
				thispane.getChildren().add(currentnode);

		}
		
		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		throw new RuntimeException(
				String.format("request of action data of type %s, but CComponentBand cannot provide any data", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		CComponentBand deepcopy = new CComponentBand(direction, this.getParentpath());
		for (int i = 0; i < this.elements.size(); i++) {
			CPageNode thiselement = this.elements.get(i);
			deepcopy.addNode(thiselement.deepcopyWithCallback(callback));
		}
		return deepcopy;

	}

	@Override
	public void mothball() {
		this.bandpane.getChildren().clear();
		for (int i = 0; i < elements.size(); i++)
			elements.get(i).mothball();

	}

}
