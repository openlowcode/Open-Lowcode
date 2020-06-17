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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.LargeBinaryDataElt;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.scene.control.TextField;

/**
 * A widget allowing to choose and upload a file to the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CFileChooser
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String id;
	private String title;

	/**
	 * creates a file chooser from the server message
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CFileChooser(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		id = reader.returnNextStringField("ID");
		title = reader.returnNextStringField("TTL");
		reader.returnNextEndStructure("FLC");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	File selectedfile = null;
	private TextInputControl filepathfield;

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {

		FlowPane thispane = new FlowPane();
		Label thislabel = new Label(title);
		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thislabel.setMinWidth(120);
		thislabel.setWrapText(true);
		thislabel.setMaxWidth(120);
		thispane.setRowValignment(VPos.TOP);
		thispane.getChildren().add(thislabel);

		filepathfield = new TextField();
		Button loadfromfile = new Button("Select");
		loadfromfile.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		thispane.getChildren().add(filepathfield);
		thispane.getChildren().add(loadfromfile);
		loadfromfile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				FileChooser fileChooser = new FileChooser();
				selectedfile = fileChooser.showOpenDialog(null);
				if (selectedfile != null)
					filepathfield.setText(selectedfile.getAbsolutePath());
			}
		});

		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof LargeBinaryDataEltType) {
			if (selectedfile == null) {
				File file = new File(filepathfield.getText());
				selectedfile = file;
			}
			if (!selectedfile.exists())
				throw new RuntimeException("File does not exist " + filepathfield.getText());
			if (selectedfile.isDirectory())
				throw new RuntimeException("path indicated a directory, not a file " + filepathfield.getText());
			try {
				byte[] array = Files.readAllBytes(selectedfile.toPath());

				return new LargeBinaryDataElt(eltname, new SFile(selectedfile.getName(), array));
			} catch (IOException e) {
				throw new RuntimeException("Error while reading file " + e.getMessage());
			}
		}
		throw new RuntimeException(
				String.format("Unsupported extraction type %s for element name = %s and object data loc = ", type,
						eltname, objectdataloc));

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {
		// nothing to do

	}

}
