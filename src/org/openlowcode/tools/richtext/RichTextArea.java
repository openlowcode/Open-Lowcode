/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.richtext;

import javafx.scene.effect.InnerShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.text.FontPosture;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.openlowcode.client.runtime.PageActionManager;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.shape.Path;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.Parent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.input.MouseButton;

/**
 * A text area widget potentially holding rich text
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class RichTextArea {
	private static Logger logger = Logger.getLogger(RichTextArea.class.getName());
	private ArrayList<Paragraph> document;
	private String textcontent;
	private boolean richtext;
	private boolean editable;
	private BorderPane area;
	private VBox paragraphbox;
	private HBox toolbar;
	private ColorPicker colorpicker;
//	Popup caret;
	Path caretpath;
	Paragraph activeparagraph;
	private RichText richtextpayload;
	private ToggleButton boldbutton;
	private ToggleButton italicbutton;
	private ToggleButton bulletpointbutton;
	private ToggleButton titlebutton;

	/**
	 * helper method generating a read-only text area
	 * 
	 * @param actionmanager page action manager
	 * @param inputvalue    payload in text
	 * @param width         width of the widget
	 * @return a rich text area that is neither rich text nor editable
	 */
	public static RichTextArea getReadOnlyTextArea(PageActionManager actionmanager, String inputvalue, int width) {
		RichTextArea richtextarea = new RichTextArea(actionmanager, false, false, 400);
		richtextarea.setTextInput(inputvalue);
		return richtextarea;
	}

	/**
	 * This will return the text inside the text area. If the text area uses plain
	 * text, then the text entered is exactly returned. If the text area uses
	 * richtext, then richtext is returned in the format specific to this widget.
	 * 
	 * @return the text inside the area
	 */
	public String generateText() {
		if (!richtext) {
			return document.get(0).dropText();
		} else {
			StringBuffer answer = new StringBuffer();
			for (int i = 0; i < document.size(); i++) {
				answer.append(document.get(i).dropText());
			}
			return answer.toString();
		}
	}

	/**
	 * this method will perform the following:
	 * <ul>
	 * <li>if bullet is set, will create a new bullet point paragraph with the text
	 * just after text paragraph</li>
	 * <li>if bullet is not set and the active paragraph is the last, or next
	 * paragraph has special formatting, create a new paragraph</li>
	 * <li>if bullet is not set and the next paragraph is normal, add text with a
	 * carriage return to next paragraph</li>
	 * </ul>
	 * In any case, put the carret at the beginning of the paragraph
	 * 
	 * @param text
	 * @param bullet
	 */
	void moveTextToNext(String text, boolean bullet) {
		if (bullet) {
			// create new bullet
			int activeparagraphindex = getActiveParagraphIndex();
			Paragraph newparagraph = new Paragraph(true, this.editable, this);
			newparagraph.setBulletParagraph();
			RichTextSection textsection = new RichTextSection();
			textsection.setText(text);
			textsection.setBullet();
			newparagraph.addText(new FormattedText(textsection, newparagraph));
			document.add(activeparagraphindex + 1, newparagraph);
			paragraphbox.getChildren().add(activeparagraphindex + 1, newparagraph.getNode());
			paragraphbox.requestLayout();
			activeparagraph = newparagraph;
			activeparagraph.setCarretAtFirst();
		} else {
			int activeparagraphindex = getActiveParagraphIndex();
			boolean treated = false;
			if (activeparagraphindex < document.size() - 1)
				if (document.get(activeparagraphindex + 1).isNormal()) {
					// if there exists a text in next paragraph
					activeparagraph = document.get(activeparagraphindex + 1);
					activeparagraph.insertTextAtFirst(text + "\n");
					activeparagraph.displayCaretAt(0);
					treated = true;
				}
			if (!treated) {
				// else create new text paragraph
				Paragraph newparagraph = new Paragraph(true, this.editable, this);
				RichTextSection textsection = new RichTextSection();
				textsection.setText(text);
				newparagraph.addText(new FormattedText(textsection, newparagraph));
				document.add(activeparagraphindex + 1, newparagraph);

				paragraphbox.getChildren().add(activeparagraphindex + 1, newparagraph.getNode());
				paragraphbox.requestLayout();
				activeparagraph = newparagraph;
				activeparagraph.setCarretAtFirst();

			}

		}
	}

	void requestCarretToNextAtOffset(float offset) {
		if (activeparagraph == null)
			logger.finest("cannot process RequestCarretToNext() as activeparagraph is null");
		int activeparagraphindex = getActiveParagraphIndex();
		if (activeparagraphindex < document.size() - 1) {
			activeparagraph = document.get(activeparagraphindex + 1);
			activeparagraph.setSelectionat(0, offset);
		} else {
			logger.finest(
					"cannot process RequestCarretToNext() as activeparagraph is last, index = " + activeparagraphindex);
		}
	}

	void requestCarretToNext() {
		if (activeparagraph == null)
			logger.finest("cannot process RequestCarretToNext() as activeparagraph is null");
		int activeparagraphindex = getActiveParagraphIndex();
		if (activeparagraphindex < document.size() - 1) {
			activeparagraph = document.get(activeparagraphindex + 1);
			activeparagraph.setCarretAtFirst();
		} else {
			logger.finest(
					"cannot process RequestCarretToNext() as activeparagraph is last, index = " + activeparagraphindex);
		}
	}

	/**
	 * @return the index of the active paragraph
	 */
	public int getActiveParagraphIndex() {
		int activeparagraphindex = -1;
		for (int i = 0; i < document.size(); i++) {
			// logger.finest(" - comparing paragraphs "+document.get(i)+"("+i+") with
			// "+activeparagraph);
			if (document.get(i) == activeparagraph)
				activeparagraphindex = i;
			logger.finest("    got active paragraph index = " + activeparagraphindex);
		}
		return activeparagraphindex;
	}

	/**
	 * @param offset brings the caret to the specified offset in the active line
	 */
	void requestCarretToPreviousAtOffset(float offset) {
		int activeparagraphindex = getActiveParagraphIndex();
		if (activeparagraphindex > 0) {
			activeparagraph = document.get(activeparagraphindex - 1);
			logger.finest(
					"asking paragraph index = " + (activeparagraphindex - 1) + " to display carret at last position");
			activeparagraph.setSelectionat(activeparagraph.getLastLine(), offset);
		} else {
			logger.finest("cannot process RequestCarretToPrevious() as activeparagraph is 0");
		}
	}

	void requestCarretToPrevious() {
		int activeparagraphindex = getActiveParagraphIndex();

		if (activeparagraphindex > 0) {
			activeparagraph = document.get(activeparagraphindex - 1);
			logger.fine(
					"asking paragraph index = " + (activeparagraphindex - 1) + " to display carret at last position");
			activeparagraph.setCarretAtLast();
		} else {
			logger.fine("cannot process RequestCarretToPrevious() as activeparagraph is 0");
		}
	}

	/**
	 * gets the paragraph width in pixel
	 * 
	 * @return the paragraph width in pixel
	 */
	public double getParagraphwidth() {
		return paragraphwidth;
	}

	private final double LEFT_MARGIN = 8;
	private final double RIGHT_MARGIN = 5;
	private double paragraphwidth = 400 - LEFT_MARGIN - RIGHT_MARGIN;
	private PageActionManager pageactionmanager;
	private ContextMenu contextmenu;

	/**
	 * @return the page action manager
	 */
	public PageActionManager getPageActionManager() {
		return this.pageactionmanager;
	}

	void setSelection(boolean bold, boolean italic, Color color, boolean title, boolean bullet) {
		boolean formatdisabled = false;
		if (title)
			formatdisabled = true;
		if (bullet)
			formatdisabled = true;
		if ((richtext) && (editable)) {
			boldbutton.setSelected(bold);
			italicbutton.setSelected(italic);
			colorpicker.setValue(color);
			titlebutton.setSelected(title);
			bulletpointbutton.setSelected(bullet);
			if (formatdisabled) {
				boldbutton.setDisable(true);
				italicbutton.setDisable(true);
				colorpicker.setDisable(true);
			} else {
				boldbutton.setDisable(false);
				italicbutton.setDisable(false);
				colorpicker.setDisable(false);
			}
			logger.finest("bold " + bold + " italic " + italic + " colorpicker "
					+ (color != null ? color.toString() : "null") + " title " + title + " bullet " + bullet);
		}
	}

	/**
	 * creates a rich text area
	 * 
	 * @param actionmanager action manager of the page
	 * @param content       content (payload)
	 * @param richtext      true if rich text
	 * @param editable      true if editable
	 * @param width         width of the widget in pixels
	 */
	public RichTextArea(
			PageActionManager actionmanager,
			String content,
			boolean richtext,
			boolean editable,
			float width) {
		this(actionmanager, richtext, editable, width);
		this.setTextInput(content);
	}

	/**
	 * this method will analyse current paragraph, and find, in the active text the
	 * previous "break" (either carriage return or change of formatting) and the
	 * next break (either carriage return or change of formatting). Then, it does
	 * the following: - create a paragraph with everything before the previous
	 * breaker - create and insert in graphic a new paragraph with the selected text
	 * - create a paragraph with everything after the next breaker
	 */
	void splitcurrentlineinparagraph() {
		logger.finer(" -------------------------- split paragraph ---------------------------");
		Paragraph newprevious = activeparagraph.generateParagraphBeforePreviousBreak();
		Paragraph newnext = activeparagraph.generateParagraphAfterNextBreak();

		if ((newprevious != null) || (newnext != null)) {

			Paragraph newactive = activeparagraph.generateParagraphbetweenBreak();
			int activeparagraphindex = getActiveParagraphIndex();
			document.remove(activeparagraphindex);
			paragraphbox.getChildren().remove(activeparagraphindex);
			if (newnext != null) {
				logger.finer("adding new paragraph");

				document.add(activeparagraphindex, newnext);

				paragraphbox.getChildren().add(activeparagraphindex, newnext.getNode());

			}
			document.add(activeparagraphindex, newactive);
			paragraphbox.getChildren().add(activeparagraphindex, newactive.getNode());
			activeparagraph = newactive;
			if (newprevious != null) {
				logger.finer("adding previous paragraph");
				document.add(activeparagraphindex, newprevious);
				paragraphbox.getChildren().add(activeparagraphindex, newprevious.getNode());

			}

		} else {
			logger.finest(" keeping current paragraph as active on split");

		}
		logger.finer(" -------------------------- end of split paragraph ---------------------------");
	}

	/**
	 * hides any selections on paragraphs other than the paragraph provided
	 * 
	 * @param paragraph paragraph provided (other paragraphs will be cleaned of any
	 *                  selection)
	 */
	public void resetOtherSelections(Paragraph paragraph) {
		for (int i = 0; i < this.document.size(); i++) {
			Paragraph currentparagraph = document.get(i);
			if (currentparagraph != paragraph) {
				currentparagraph.hideSelectionAndResetIndex();
			}
		}
	}

	/**
	 * a control that allows to display and edit a text, potentially with specific
	 * rich-text file. It always displays the full size of the text
	 * 
	 * @param richtext true if rich text
	 * @param editable true if editable
	 * @param width    area width in points
	 */
	public RichTextArea(PageActionManager pageactionmanager, boolean richtext, boolean editable, float width) {
		this.paragraphwidth = width - LEFT_MARGIN - RIGHT_MARGIN;
		this.pageactionmanager = pageactionmanager;
		this.richtext = richtext;
		this.editable = editable;
		document = new ArrayList<Paragraph>();
		this.textcontent = null;
		area = new BorderPane();
		if (!editable) {

			area.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent mouseevent) {
					if (mouseevent.getButton().equals(MouseButton.PRIMARY)) {
						Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						String contenttocopy = null;
						for (int i = 0; i < document.size(); i++) {
							Paragraph currentparagraph = document.get(i);
							if (currentparagraph.hasSelection()) {
								contenttocopy = currentparagraph.returnSelectedText();

							}
						}
						// no selected text
						if (contenttocopy == null) {
							if (richtextpayload != null) {
								contenttocopy = richtextpayload.generatePlainString();

							} else {
								contenttocopy = textcontent;

							}
						}
						if (contenttocopy == null)
							contenttocopy = "";
						content.putString(contenttocopy);
						clipboard.setContent(content);
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("Content of field copied to clipboard : "
										+ (contenttocopy.length() > 30 ? (contenttocopy.substring(0, 30) + "...")
												: contenttocopy));
					}

				}

			});
		}
		area.setMinWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		area.setMaxWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		area.setPrefWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		area.requestLayout();
		if ((richtext) && (editable)) {
			toolbar = new HBox();
			toolbar.setSpacing(3);
			toolbar.setPadding(new Insets(2, 10, 3, 10));
			toolbar.setBackground(new Background(new BackgroundFill(Color.web("0xECECF3"), null, null)));
			colorpicker = new ColorPicker(Color.BLACK);
			colorpicker.setStyle("-fx-color-label-visible: false ;");
			colorpicker.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					logger.finest("Stroke color picker value = " + colorpicker.getValue());
					if (activeparagraph != null) {
						if (okToAdd(11)) {
							activeparagraph.insertColorIndicator(colorpicker.getValue());
							activeparagraph.giveBackFocus();
						} else {
							activeparagraph.giveBackFocus();
						}
					} else {
						logger.info("stroke color picker while no active paragraph, does not give back focus");
					}
				}

			});
			toolbar.getChildren().add(colorpicker);
			boldbutton = new ToggleButton("B");
			boldbutton.setFont(
					Font.font(boldbutton.getFont().getName(), FontWeight.BOLD, boldbutton.getFont().getSize()));
			boldbutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					logger.finest("Stroke bold button value = " + boldbutton.isSelected());
					if (activeparagraph != null) {
						if (okToAdd(5)) {
							activeparagraph.insertBoldIndicator(boldbutton.isSelected());
							activeparagraph.giveBackFocus();
						} else {
							activeparagraph.giveBackFocus();
						}
					} else {
						logger.info("stroke bold button while no active paragraph, does not give back focus");
					}
				}

			});
			toolbar.getChildren().add(boldbutton);
			italicbutton = new ToggleButton("I");
			italicbutton.setFont(
					Font.font(italicbutton.getFont().getName(), FontPosture.ITALIC, boldbutton.getFont().getSize()));
			italicbutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					logger.finest("Stroke italicbutton button value = " + italicbutton.isSelected());
					if (activeparagraph != null) {
						if (okToAdd(5)) {
							activeparagraph.insertItalicIndicator(italicbutton.isSelected());
							activeparagraph.giveBackFocus();
						} else {
							activeparagraph.giveBackFocus();
						}
					} else {
						logger.info("stroke italicbutton while no active paragraph, does not give back focus");
					}
				}

			});
			toolbar.getChildren().add(italicbutton);
			bulletpointbutton = new ToggleButton("\u2022");
			bulletpointbutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {

					if (bulletpointbutton.isSelected()) {
						logger.info(
								"Stroke bulletpoint button, transforming current line in paragraph in a bullet point");
						if (okToAdd(9)) {
							splitcurrentlineinparagraph();
							activeparagraph.setBulletParagraph();
							redrawActiveParagraph();
						} else {
							activeparagraph.giveBackFocus();
						}
					} else {
						logger.info("Stroke bulletpoint button as inactive,returning to normal paragraph");

						activeparagraph.setNormalParagraph();
						redrawActiveParagraph();
					}
				}

			});
			toolbar.getChildren().add(bulletpointbutton);
			titlebutton = new ToggleButton("Title");
			titlebutton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					if (titlebutton.isSelected()) {
						logger.info("Stroke title button, transforming current line in paragraph in a title point");
						if (okToAdd(9)) {
							splitcurrentlineinparagraph();
							activeparagraph.setTitleParagraph();
							redrawActiveParagraph();
						} else {
							activeparagraph.giveBackFocus();
						}
					} else {
						logger.info("Stroke title button as inactive,returning to normal paragraph");

						activeparagraph.setNormalParagraph();
						redrawActiveParagraph();
					}

				}

			});

			toolbar.getChildren().add(titlebutton);

			contextmenu = new ContextMenu();
			contextmenu.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
					if (!newvalue)
						contextmenu.hide();

				}

			});
			toolbar.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					contextmenu.show(toolbar, event.getScreenX(), event.getScreenY());

				}

			});

			MenuItem clear = new MenuItem("Clear");
			clear.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						document.clear();
						paragraphbox.getChildren().clear();
						setTextInput("");
					} catch (Exception e) {
						logger.warning("Error while executing clear " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("  " + e.getStackTrace()[i]);
						;
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("Error while executing clear " + e.getMessage(), true);
					}
				}

			});
			contextmenu.getItems().add(clear);

			MenuItem exportsource = new MenuItem("Export Source");
			exportsource.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						String source = generateText();
						final ClipboardContent content = new ClipboardContent();
						content.putString(source);
						Clipboard.getSystemClipboard().setContent(content);
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("text source copied to clipboard, size = " + source.length() + "ch");
					} catch (Exception e) {
						logger.warning("Error while executing export source " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("  " + e.getStackTrace()[i]);
						;
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("Error while executing export source " + e.getMessage(), true);
					}

				}

			});
			contextmenu.getItems().add(exportsource);

			MenuItem exportsourceescape = new MenuItem("Export Source (Dev)");
			exportsourceescape.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					try {
						String source = generateText();
						source = RichTextArea.escapeforjavasource(source);
						final ClipboardContent content = new ClipboardContent();
						content.putString(source);
						Clipboard.getSystemClipboard().setContent(content);
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("text source copied to clipboard, size = " + source.length() + "ch");
					} catch (Exception e) {
						logger.warning("Error while executing export source " + e.getMessage());
						for (int i = 0; i < e.getStackTrace().length; i++)
							logger.warning("  " + e.getStackTrace()[i]);
						;
						pageactionmanager.getClientSession().getActiveClientDisplay()
								.updateStatusBar("Error while executing export source " + e.getMessage(), true);
					}

				}

			});
			contextmenu.getItems().add(exportsourceescape);

			area.setTop(toolbar);

		}
		if (editable) {
			area.setStyle("-fx-background-color: #FFFFFF;");
			area.setBorder(new Border(new BorderStroke(Color.web("#AAAAAA"), BorderStrokeStyle.SOLID,
					new CornerRadii(2), BorderWidths.DEFAULT, new Insets(0, 0, 0, 0))));
			area.setEffect(new InnerShadow(BlurType.THREE_PASS_BOX, Color.GRAY, 2, 0, 0, 0));

		}
		paragraphbox = new VBox();
		if (editable) {
			paragraphbox.setPadding(new Insets(5, this.RIGHT_MARGIN, 3, 0));
		} else {
			paragraphbox.setPadding(new Insets(0, 0, 0, 0));
		}
		paragraphbox.setMinWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);

		paragraphbox.setMaxWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		paragraphbox.setPrefWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);

		area.setCenter(paragraphbox);
		area.setMinWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		area.setMaxWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
		area.setPrefWidth(paragraphwidth + this.RIGHT_MARGIN + this.LEFT_MARGIN);
	}

	/**
	 * redraw the active paragraph
	 */
	public void redrawActiveParagraph() {
		int activeparagraphindex = getActiveParagraphIndex();
		paragraphbox.getChildren().remove(activeparagraphindex);
		if (activeparagraphindex == paragraphbox.getChildren().size()) {
			paragraphbox.getChildren().add(activeparagraph.getNode());
		} else {
			paragraphbox.getChildren().add(activeparagraphindex, activeparagraph.getNode());
		}
		paragraphbox.requestLayout();

		activeparagraph.giveBackFocus();
	}

	/**
	 * @return get the javafx node representing the rich text area
	 */
	public Parent getNode() {
		return area;
	}

	/**
	 * @return true if area is rich text
	 */
	public boolean isRichtext() {
		return richtext;
	}

	/**
	 * @return true if area if editable
	 */
	public boolean isEditable() {
		return editable;
	}

	private void refreshdisplay() {
		if (!richtext) {
			Paragraph paragraph = new Paragraph(this.textcontent, richtext, editable, this);
			document.add(paragraph);
			paragraphbox.getChildren().add(paragraph.getNode());
			paragraphbox.requestLayout();

		}
		if (richtext) {
			richtextpayload = new RichText(this.textcontent);
			Paragraph[] allparagraphs = richtextpayload.generateAllParagraphs(editable, this);
			for (int i = 0; i < allparagraphs.length; i++) {
				paragraphbox.getChildren().add(allparagraphs[i].getNode());
				document.add(allparagraphs[i]);
			}
		}
	}

	/**
	 * reset the text content of this rich text area
	 * 
	 * @param input the text to put inside this rich text area
	 */
	public void setTextInput(String input) {
		this.textcontent = input;
		refreshdisplay();
	}

	void dropDescription() {
		logger.warning(" -------------------- DROP RICHTEXTARA DESCRIPTION START -----------");
		logger.warning(" richtextarea, number of paragraphs = " + document.size());
		for (int i = 0; i < document.size(); i++) {
			Paragraph thisparagraph = document.get(i);
			String activeparagraphmarker = "";
			if (thisparagraph == activeparagraph)
				activeparagraphmarker = " <<<ACTIVE>>>";
			logger.warning(" ++ dropping description of paragraph " + i + activeparagraphmarker);
			thisparagraph.dropDescription();
		}
		logger.warning(" -------------------- DROP RICHTEXTARA DESCRIPTION END -----------");
	}

	/**
	 * make the widget glow (blue glow showing the active widget)
	 */
	public void makeGlow() {
		Border border = new Border(new BorderStroke(Color.web("#039ED3"), BorderStrokeStyle.SOLID, new CornerRadii(2),
				BorderWidths.DEFAULT, new Insets(0, 0, 0, 0)));

		area.setBorder(border);

	}

	/**
	 * make the widget lose the blue glow
	 */
	public void loseGlow() {
		area.setBorder(new Border(new BorderStroke(Color.web("#AAAAAA"), BorderStrokeStyle.SOLID, new CornerRadii(2),
				BorderWidths.DEFAULT, new Insets(0, 0, 0, 0))));
	}

	private int maxlength = -1;

	/**
	 * sets the max text length allowed (length of the text including formatting
	 * 
	 * @param maxlength maximum length of the text
	 */
	public void setMaxTextLength(int maxlength) {
		this.maxlength = maxlength;

	}

	/**
	 * @return the max text length allowed
	 */
	public int getMaxTextLength() {
		return this.maxlength;
	}

	boolean okToAdd(int nbcar) {
		if (maxlength == -1)
			return true;
		String text = this.generateText();
		logger.finest("  ---- ** -- Check text length, existing = " + text.length() + ", to add = " + nbcar + ", max = "
				+ maxlength);
		if (text.length() + nbcar < maxlength)
			return true;
		return false;
	}

	/**
	 * As all meaningful content has disappeared in this paragraph, delete it if it
	 * is not the last paragraph inside the rich text area
	 */
	public void deleteActiveParagraphIfNotLast() {
		if (document.size() > 1) {
			int activeparagraphindex = this.getActiveParagraphIndex();
			document.remove(activeparagraphindex);
			paragraphbox.getChildren().remove(activeparagraphindex);
			paragraphbox.requestLayout();
			if (activeparagraphindex == 0) {
				activeparagraph = document.get(0);
				activeparagraph.setCarretAtFirst();
			} else {
				activeparagraph = document.get(activeparagraphindex - 1);
				activeparagraph.setCarretAtLast();
			}

		}

	}

	/**
	 * a utility method to escape a string
	 * 
	 * @param source
	 * @return
	 */
	public static String escapeforjavasource(String source) {
		StringBuffer returnstring = new StringBuffer();
		returnstring.append('"');
		if (source != null)
			for (int i = 0; i < source.length(); i++) {
				char currentchar = source.charAt(i);
				if (currentchar == '\n') {
					returnstring.append("\\n");
					continue;
				}
				if (currentchar == '\\') {
					returnstring.append("\\\\");
					continue;
				}
				if (currentchar == '\r') {
					returnstring.append("\\r");
					continue;
				}
				if (currentchar == '"') {
					returnstring.append("\\\"");
					continue;
				}
				if (currentchar == '\u2022') {
					returnstring.append("\\u2022");
					continue;
				}
				if (currentchar == '\u0009') {
					returnstring.append("\\u0009");
					continue;
				}
				if (currentchar == '\u0003') {
					returnstring.append("\\u0003");
					continue;
				}
				returnstring.appendCodePoint(currentchar);

			}
		returnstring.append('"');
		return returnstring.toString();
	}

}
