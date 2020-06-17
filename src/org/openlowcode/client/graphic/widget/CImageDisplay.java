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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.LargeBinaryDataElt;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.stage.Window;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;

/**
 * An image display showing a small thumbnail in the main page and the full
 * image in a popup
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CImageDisplay
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String id;
	private boolean popup;
	private CPageDataRef datareference;
	private CPageInlineAction fullimageaction;
	private CInlineActionDataRef inlineactiondataref;
	@SuppressWarnings("unused")
	private Popup imagepopup;
	private ImageView thumbnailview;
	private SFile largefile;
	private boolean islabel;
	private String label;
	private PageActionManager actionmanager;

	/**
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CImageDisplay(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.id = reader.returnNextStringField("ID");
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		if (!this.datareference.getType().equals(new LargeBinaryDataEltType()))
			throw new RuntimeException(String.format(
					"Invalid external data reference named %s, excepted LargeBinaryDataEltType, got %s in CPage ",
					datareference.getName(), datareference));
		this.popup = reader.returnNextBooleanField("PUP");
		if (this.popup) {
			reader.returnNextStartStructure("INLINEACTION");
			fullimageaction = new CPageInlineAction(reader);

			this.inlineactiondataref = new CInlineActionDataRef(reader, this);
		}
		this.islabel = reader.returnNextBooleanField("ISL");
		if (this.islabel)
			this.label = reader.returnNextStringField("LBL");
		reader.returnNextEndStructure("IMD");
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
		inputdata.addInlineActionDataRef(this.inlineactiondataref);
		this.actionmanager = actionmanager;
		SFile thumbnail = getThumbnail(inputdata, datareference);
		if (!thumbnail.isEmpty()) {
			ByteArrayInputStream imagedata = new ByteArrayInputStream(thumbnail.getContent());
			Image image = new Image(imagedata);
			double imagewidth = image.getWidth();
			double imageheight = image.getHeight();
			thumbnailview = new ImageView(image);
			thumbnailview.setFitWidth(imagewidth);
			thumbnailview.setFitHeight(imageheight);
			thumbnailview.setOnMousePressed(actionmanager.getMouseHandler());
			actionmanager.registerInlineAction(thumbnailview, fullimageaction);
			BorderPane border = new BorderPane();
			border.setBorder(new Border(
					new BorderStroke(Color.web("#ddeeff"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
							new BorderWidths(3)),
					new BorderStroke(Color.web("#bbccdd"), BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
							new BorderWidths(1))));
			border.setCenter(thumbnailview);
			border.setMaxHeight(imageheight + 6);
			border.setMaxWidth(imagewidth + 6);
			DropShadow ds = new DropShadow();
			ds.setRadius(3.0);
			ds.setOffsetX(1.5);
			ds.setOffsetY(1.5);
			ds.setColor(Color.color(0.2, 0.2, 0.2));
			border.setEffect(ds);
			if (this.islabel) {
				FlowPane thispane = new FlowPane();
				Label thislabel = new Label(label);
				thislabel.setFont(
						Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
				thislabel.setMinWidth(120);
				thislabel.setWrapText(true);
				thislabel.setMaxWidth(120);
				thispane.setRowValignment(VPos.TOP);
				thispane.getChildren().add(thislabel);
				thispane.getChildren().add(border);
				return thispane;
			} else {
				return border;
			}

		}

		return null;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {

		if (dataelt == null)
			throw new RuntimeException(String.format("data element is null"));
		if (!dataelt.getType().printType().equals(inlineactiondataref.getType()))
			throw new RuntimeException(
					String.format("inline data with name = %s does not have expected %s type, actually found %s",
							inlineactiondataref.getName(), inlineactiondataref.getType(), dataelt.getType()));
		LargeBinaryDataElt largefileelt = (LargeBinaryDataElt) dataelt;
		largefile = largefileelt.getPayload();
		if (!largefile.isEmpty()) {
			final Stage imagepopup = new Stage();
			imagepopup.initModality(Modality.APPLICATION_MODAL);
			imagepopup.initOwner(actionmanager.getClientSession().getMainFrame().getPrimaryStage());

			Image image = new Image(new ByteArrayInputStream(largefile.getContent()));
			ImageView imageview = new ImageView(image);
			imageview.setFitWidth(image.getWidth());
			imageview.setFitHeight(image.getHeight());
			imageview.setOnMousePressed(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					FileChooser filechooser = new FileChooser();
					filechooser.setTitle("Save image to file");
					filechooser.setInitialFileName(largefile.getFileName());
					filechooser.getExtensionFilters()
							.add(new FileChooser.ExtensionFilter("PNG files (*.PNG)", "*.PNG"));
					File file = filechooser.showSaveDialog(imagepopup);

					if (file != null) {
						try {
							FileOutputStream fos = new FileOutputStream(file, false);
							fos.write(largefile.getContent());
							fos.close();
							imagepopup.close();

						} catch (IOException e) {
							logger.warning("Error writing file " + e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++)
								logger.warning("   " + e.getStackTrace()[i]);
						}
					}

				}

			});
			ScrollPane scrollpane = new ScrollPane();
			scrollpane.setContent(imageview);
			Scene dialogscene = new Scene(scrollpane);
			imagepopup.setScene(dialogscene);
			imagepopup.show();
			imageview.requestFocus();

		}

	}

	@Override
	public void mothball() {

	}

	public SFile getThumbnail(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", dataref.getName()));
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		LargeBinaryDataElt largebinary = (LargeBinaryDataElt) thiselement;
		return largebinary.getPayload();
	}
}
