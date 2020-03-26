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

import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.LargeBinaryDataElt;
import org.openlowcode.tools.structure.LargeBinaryDataEltType;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * A widget to choose an image. It allows to get an image from the clipboard
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CImageChooser
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String id;
	private int thumbnailsize;
	private String title;
	private CPageAction actiontolaunch;
	private boolean filesgenerated;

	private byte[] fullimage;
	private byte[] thumbnail;
	private ScrollPane mainscrollpane;
	private Image referenceimage = null;
	private double minx;
	private double miny;
	private double maxx;
	private double maxy;
	private double ratio = 1.0;
	private Pane imagepane;

	/**
	 * creates an image chooser with a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CImageChooser(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		id = reader.returnNextStringField("ID");
		thumbnailsize = reader.returnNextIntegerField("TBNSIZ");
		title = reader.returnNextStringField("TTL");
		reader.returnNextStartStructure("ACTION");
		actiontolaunch = new CPageAction(reader);
		filesgenerated = false;
		reader.returnNextEndStructure("IMC");

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
		BorderPane borderpane = new BorderPane();
		Button launchaction = new Button(title+" (selection)");
		Button launchactionfull = new Button(title+" (full)");
		launchaction.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		launchactionfull.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		actionmanager.registerEvent(launchaction, actiontolaunch);
		actionmanager.registerEvent(launchactionfull, actiontolaunch);
		
		Pane buttonbox = CComponentBand.returnBandPane(CComponentBand.DIRECTION_RIGHT);
		mainscrollpane = new ScrollPane();
		mainscrollpane.setMinHeight(500 + 16);
		mainscrollpane.setMinWidth(700);
		mainscrollpane.hbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.ALWAYS);
		mainscrollpane.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);

		launchaction.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				if (referenceimage != null) {
					try {
						logger.finer("image is loaded, trying to save it to file with clipping " + (int) minx + "-"
								+ (int) (miny) + "-" + (int) (int) (maxx - minx) + "-" + (int) (maxy - miny));
						logger.finer("");
						PixelReader reader = referenceimage.getPixelReader();

						WritableImage newImage = new WritableImage(reader, (int) minx, (int) miny, (int) (maxx - minx),
								(int) (maxy - miny));
						BufferedImage bImage = SwingFXUtils.fromFXImage(newImage, null);

						ByteArrayOutputStream bigimagestream = new ByteArrayOutputStream();
						ImageIO.write(bImage, "PNG", bigimagestream);
						fullimage = bigimagestream.toByteArray();
						ByteArrayInputStream bigimageinputstream = new ByteArrayInputStream(fullimage);
						Image Thumbnail = new Image(bigimageinputstream, thumbnailsize, thumbnailsize, true, true);
						BufferedImage thumbnailimage = SwingFXUtils.fromFXImage(Thumbnail, null);
						ByteArrayOutputStream thumbnailstream = new ByteArrayOutputStream();
						ImageIO.write(thumbnailimage, "PNG", thumbnailstream);
						thumbnail = thumbnailstream.toByteArray();
						filesgenerated = true;
						actionmanager.handle(event);
					} catch (Exception e) {
						logger.warning("Error in file generation " + e.getClass().getName() + " - " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("   " + e.getStackTrace()[i]);
					}
				}

			}

		});

		launchactionfull.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (referenceimage != null) {
					try {
					BufferedImage referenceimagebuffered = SwingFXUtils.fromFXImage(referenceimage, null);
					ByteArrayOutputStream referenceimageoutputstream = new ByteArrayOutputStream();
					ImageIO.write(referenceimagebuffered, "PNG", referenceimageoutputstream);
					fullimage = referenceimageoutputstream.toByteArray();
					ByteArrayInputStream bigimageinputstream = new ByteArrayInputStream(fullimage);
					Image Thumbnail = new Image(bigimageinputstream, thumbnailsize, thumbnailsize, true, true);
					BufferedImage thumbnailimage = SwingFXUtils.fromFXImage(Thumbnail, null);
					ByteArrayOutputStream thumbnailstream = new ByteArrayOutputStream();
					ImageIO.write(thumbnailimage, "PNG", thumbnailstream);
					thumbnail = thumbnailstream.toByteArray();
					filesgenerated = true;
					actionmanager.handle(event);
					
					} catch (Exception e) {
						logger.warning("Error in file generation " + e.getClass().getName() + " - " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("   " + e.getStackTrace()[i]);
					}
					
				}
			}
		});
		
		Button button = new Button("get Image from clipboard");
		button.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				logger.finer("-------------------------- Clipboard dump -------------");

				Clipboard clipboard = Clipboard.getSystemClipboard();
				Iterator<DataFormat> contenttype = clipboard.getContentTypes().iterator();
				while (contenttype.hasNext()) {
					DataFormat dataformat = contenttype.next();
					logger.finer(" dataformat = " + dataformat.toString());
				}
				logger.finer("-------------------------- Clipboard dump end-------------");
				if (clipboard.hasImage()) {
					try {
						java.awt.Image image = getImageFromClipboard();
						referenceimage = awtImageToFX(image);

						generateImageViewFromAwtImage(referenceimage, 500);
						mainscrollpane.setMaxWidth(mainscrollpane.getWidth());
						mainscrollpane.setContent(imagepane);

					} catch (Exception e) {
						logger.warning("screwed-up error " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("   " + e.getStackTrace()[i]);
					}
				}
			}

		});
		Button loadfromfile = new Button("Load from File");
		loadfromfile.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		loadfromfile.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				FileChooser fileChooser = new FileChooser();

				// Set extension filter
				FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("JPG files (*.jpg)",
						"*.JPG");
				FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("PNG files (*.png)",
						"*.PNG");
				FileChooser.ExtensionFilter extFilterGIF = new FileChooser.ExtensionFilter("GIF files (*.gif)",
						"*.GIF");
				FileChooser.ExtensionFilter extFilterjpeg = new FileChooser.ExtensionFilter("JPG files (*.jpeg)",
						"*.jpeg");
				FileChooser.ExtensionFilter extFilterjpg = new FileChooser.ExtensionFilter("JPG files (*.jpg)",
						"*.jpg");
				FileChooser.ExtensionFilter extFilterpng = new FileChooser.ExtensionFilter("PNG files (*.png)",
						"*.png");
				FileChooser.ExtensionFilter extFiltergif = new FileChooser.ExtensionFilter("PNG files (*.gif)",
						"*.gif");
				FileChooser.ExtensionFilter extFilterJPEG = new FileChooser.ExtensionFilter("PNG files (*.JPEG)",
						"*.JPEG");
				fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG, extFilterGIF, extFilterjpeg,
						extFilterjpg, extFilterpng, extFiltergif, extFilterJPEG);
				File file = fileChooser.showOpenDialog(null);
				try {
					BufferedImage bufferedImage = ImageIO.read(file);
					referenceimage = SwingFXUtils.toFXImage(bufferedImage, null);
					generateImageViewFromAwtImage(referenceimage, 500);
					mainscrollpane.setMaxWidth(mainscrollpane.getWidth());
					mainscrollpane.setContent(imagepane);
				} catch (Exception e) {
					logger.warning("screwed-up error " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.warning("   " + e.getStackTrace()[i]);
				}

			}

		});

		buttonbox.getChildren().add(button);
		buttonbox.getChildren().add(loadfromfile);
		buttonbox.getChildren().add(launchaction);
		buttonbox.getChildren().add(launchactionfull);

		borderpane.setTop(buttonbox);
		borderpane.setCenter(mainscrollpane);
		return borderpane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof LargeBinaryDataEltType) {
			if (!this.filesgenerated)
				throw new RuntimeException("Files not yet generated.");
			if (eltname.compareTo("FULLIMAGE") == 0) {
				return new LargeBinaryDataElt(eltname, new SFile("fullimage.PNG", fullimage));
			}
			if (eltname.compareTo("THUMBNAIL") == 0) {
				return new LargeBinaryDataElt(eltname, new SFile("thumbnail.PNG", thumbnail));
			}
		}
		throw new RuntimeException(
				String.format("Unsupported extraction type %s for element name = %s and object data loc = ", type,
						eltname, objectdataloc));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {

	}

	@Override
	public void mothball() {
	}
	// -----------------------------------------------------------------------------------
	// image utilities
	// -----------------------------------------------------------------------------------

	private static java.awt.Image getImageFromClipboard() {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				return (java.awt.Image) transferable.getTransferData(DataFlavor.imageFlavor);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private static javafx.scene.image.Image awtImageToFX(java.awt.Image image) throws Exception {
		if (!(image instanceof RenderedImage)) {
			BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),
					BufferedImage.TYPE_INT_ARGB);
			Graphics g = bufferedImage.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();

			image = bufferedImage;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write((RenderedImage) image, "png", out);
		out.flush();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		return new javafx.scene.image.Image(in);
	}

	private void generateImageViewFromAwtImage(Image referenceimage, int maximumheight) throws Exception {

		final ImageView imageview = new ImageView();
		imageview.setImage(referenceimage);
		logger.finer("got image without error ");

		if (referenceimage.getHeight() > maximumheight) {
			logger.finer("resized to 600 height");
			ratio = maximumheight / referenceimage.getHeight();
			double width = ratio * referenceimage.getWidth();

			imageview.setFitWidth(width);
			imageview.setFitHeight(maximumheight);
			logger.finer("resized to 600 height ");

		}
		Rectangle rectangle = new Rectangle(0, 0, imageview.getFitWidth() / 5, imageview.getFitHeight() / 5);
		rectangle.setFill(Color.WHITE.deriveColor(1, 1, 1, 0.2));
		rectangle.setStroke(Color.BLACK);
		imagepane = new Pane(imageview, rectangle);

		imageview.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mousevent) {
				logger.finer("pressed");
				rectangle.setX(mousevent.getX());
				rectangle.setY(mousevent.getY());
				rectangle.setHeight(2);
				rectangle.setWidth(2);

			}

		});
		imageview.setOnMouseDragExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mousevent) {
				logger.finer("released");
			}

		});
		imageview.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mousevent) {

				double x = mousevent.getX();
				double y = mousevent.getY();
				if (x > rectangle.getX()) {
					rectangle.setWidth(x - rectangle.getX());
				} else {
					double width = rectangle.getX() - x;
					double newx = x;
					rectangle.setX(newx);
					rectangle.setWidth(width);
				}
				if (y > rectangle.getY()) {
					rectangle.setHeight(y - rectangle.getY());
				} else {
					double height = rectangle.getY() - y;
					double newy = y;
					rectangle.setY(newy);
					rectangle.setHeight(height);
				}

				minx = rectangle.getX() / ratio;
				miny = rectangle.getY() / ratio;
				maxx = (rectangle.getX() + rectangle.getWidth()) / ratio;
				maxy = (rectangle.getY() + rectangle.getHeight()) / ratio;
				logger.finer("moved [" + minx + "," + miny + "," + maxx + "," + maxy + "]");
			}

		});

	}
}
