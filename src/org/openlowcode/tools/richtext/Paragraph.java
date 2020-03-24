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

import java.lang.reflect.InvocationTargetException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import com.sun.javafx.scene.text.GlyphList;
import com.sun.javafx.scene.text.HitInfo;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.PrismTextLayout;
import com.sun.javafx.text.TextLine;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.TextFlow;
import javafx.beans.binding.DoubleBinding;

/**
 * This class represents a paragraph inside a textflow. It may include text in
 * different formatting. However, the following features will be their own
 * paragraph:
 * <ul>
 * <li>title</li>
 * <li>each bullet point in a bullet list</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class Paragraph {
	private Path hightlightpath;

	/**
	 * provides the encoded text description of the paragraph following Open Lowcode
	 * rich text
	 * 
	 * @return the encoded string with the content of this paragraph
	 */
	public String dropText() {
		if (!richtext) {
			return textlist.get(0).getTextPayload();
		} else {
			StringBuffer richtext = new StringBuffer();
			for (int i = 0; i < textlist.size(); i++) {
				FormattedText thistext = textlist.get(i);
				if (bulletpoint) {
					richtext.append("[P]");
				}
				if (title) {
					richtext.append("[T]");
				}
				if ((!title) & (!bulletpoint)) {
					richtext.append("[");
					if (thistext.isBold())
						richtext.append("B");
					if (thistext.isItalic())
						richtext.append("I");
					Color color = thistext.getSpecialcolor();
					if (color != null) {
						richtext.append("C");
						richtext.append(String.format("%02X%02X%02X", (int) (color.getRed() * 255),
								(int) (color.getGreen() * 255), (int) (color.getBlue() * 255)));
					}
					richtext.append("]");
				}
				richtext.append(thistext.getTextPayload().replaceAll("\\[", "\\[\\["));
			}
			return richtext.toString();
		}
	}

	private static Logger logger = Logger.getLogger(Paragraph.class.getName());
	private TextFlow textflow;
	private ArrayList<FormattedText> textlist;
	private boolean editable;
	private boolean richtext;

	private boolean bulletpoint;
	private boolean title;

	/**
	 * this field will be true when a drag is active on the component. This is to
	 * avoid to lose the drag when focused is lost. This case happens when trying to
	 * select a second list of text while
	 * 
	 */
	private boolean dragactive = false;

	private int selectionintextflow = -1;
	/**
	 * This variable indicates what is the first character on which a click or drag
	 * start was done
	 */
	private int dragstartindex = -1;

	/**
	 * This variable indicates what is the last character on which a click or drag
	 * end was done. If drag start and drag end are different from -1 and different
	 * from each other, then a select shape is drawn.
	 */
	private int dragendindex = -1;

	/**
	 * @return true if there is a valid selection of text
	 */
	public boolean hasSelection() {
		if (dragstartindex >= 0)
			if (dragendindex >= 0)
				if (dragendindex >= dragstartindex)
					return true;
		return false;
	}

	/**
	 * @return just the selected text
	 */
	public String returnSelectedText() {
		StringBuffer fulltext = new StringBuffer();
		for (int i = 0; i < textlist.size(); i++)
			fulltext.append(textlist.get(i).getTextPayload());
		logger.finest("Full text = '" + fulltext + "' drag start/end=" + dragstartindex + "/" + dragendindex);
		String selection = fulltext.substring(dragstartindex, dragendindex);
		dragactive = false;
		return selection;
	}

	/**
	 * sets the carret at the first charater of this paragrahp
	 */
	void setCarretAtFirst() {
		textflow.requestFocus();
		selectionintextflow = 0;

		this.displayCaretAt(selectionintextflow);
		logger.finest("requested set Carret at First for paragraph with bulletpoint=" + bulletpoint + " and title = "
				+ title);
	}

	/**
	 * @return the number of characters in that paragraph
	 */
	public int getCharNb() {
		int characters = 0;
		for (int i = 0; i < textlist.size(); i++)
			characters += textlist.get(i).getTextPayload().length();
		return characters;
	}

	/**
	 * sets the carret after the last character of the paragraph
	 */
	void setCarretAtLast() {
		textflow.requestFocus();
		int characters = getCharNb();
		selectionintextflow = characters;
		this.displayCaretAt(selectionintextflow);
		logger.finest(
				"requested set Carret at Last for paragraph with bulletpoint=" + bulletpoint + " and title = " + title);
	}

	/**
	 * this is a workaround to use private API
	 */
	private static Method getTextLayout;
	private static Method getLines;

	static {
		try {
			getTextLayout = TextFlow.class.getDeclaredMethod("getTextLayout");
			getLines = PrismTextLayout.class.getDeclaredMethod("getLines");
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		getTextLayout.setAccessible(true);
		getLines.setAccessible(true);
	}

	void addText(FormattedText text) {
		textlist.add(text);
		textflow.getChildren().add(text.getNode());
	}

	private RichTextArea parent;

	/**
	 * creates a paragraph with the text in input (can have formatting if rich text)
	 * 
	 * @param simpletext simple text
	 * @param richtext   true if rich text formatting is possible
	 * @param editable   true if editable
	 * @param parent     parent rich text area
	 */
	public Paragraph(String simpletext, boolean richtext, boolean editable, RichTextArea parent) {
		this(richtext, editable, parent);

		if (!richtext) {

			addText(new FormattedText(new RichTextSection(simpletext), this));
		}
		if (richtext) {
			throw new RuntimeException("Not yet implemented");
		}

	}

	/**
	 * creates an empty paragraph
	 * 
	 * @param richtext true if richtext
	 * @param editable true if editable
	 * @param parent   the parent richtextarea
	 */
	public Paragraph(boolean richtext, boolean editable, RichTextArea parent) {
		this.richtext = richtext;
		this.editable = editable;
		this.bulletpoint = false;
		this.title = false;
		this.textflow = new TextFlow();
		textflow.setLineSpacing(1);

		textlist = new ArrayList<FormattedText>();
		this.parent = parent;
		if (editable)
			createFlowWriteListener();
		createSelectionListener();
	}

	/**
	 * sets the paragraph to bullet point
	 */
	public void setBulletParagraph() {
		this.bulletpoint = true;
		this.title = false;
		reformatTextFollowingParagraph();
	}

	/**
	 * sets the paragraph to title
	 */
	public void setTitleParagraph() {
		this.title = true;
		this.bulletpoint = false;
		reformatTextFollowingParagraph();
	}

	/**
	 * sets the paragraph to normal
	 */
	public void setNormalParagraph() {
		this.title = false;
		this.bulletpoint = false;
		reformatTextFollowingParagraph();
	}

	/**
	 * reformats the text
	 */
	public void reformatTextFollowingParagraph() {
		for (int i = 0; i < textlist.size(); i++) {
			FormattedText thistextlist = textlist.get(i);
			thistextlist.setTitle(title);
		}
	}

	/**
	 * @return gets the node representing this paragraph
	 */
	public Node getNode() {
		if (!bulletpoint)
			if (!title) {
				Label normalleftspace = new Label(" ");
				normalleftspace.setPadding(new Insets(2, 0, 2, 0));
				normalleftspace.setMinWidth(8);
				normalleftspace.setMaxWidth(8);
				HBox paragraphwithmargin = new HBox();
				paragraphwithmargin.setPadding(new Insets(0, 0, 0, 0));
				textflow.setMaxWidth(parent.getParagraphwidth());
				textflow.setMinWidth(parent.getParagraphwidth());
				textflow.setPadding(new Insets(0, 0, 0, 0));
				paragraphwithmargin.getChildren().add(normalleftspace);
				paragraphwithmargin.getChildren().add(textflow);
				Paragraph thisparagraph = this;
				normalleftspace.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent arg0) {
						thisparagraph.setCarretAtFirst();

					}

				});
				return paragraphwithmargin;

			}
		if (bulletpoint) {
			HBox paragraphwithbullet = new HBox();
			paragraphwithbullet.setPadding(new Insets(0, 0, 0, 0));
			Label bullet = new Label("\u2022");
			bullet.setPadding(new Insets(2, 4, 2, 16));
			bullet.setMinWidth(25);
			bullet.setMaxWidth(25);
			Paragraph thisparagraph = this;
			textflow.setPadding(new Insets(2, 0, 2, 5));
			bullet.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent arg0) {
					thisparagraph.setCarretAtFirst();

				}

			});
			paragraphwithbullet.getChildren().add(bullet);
			paragraphwithbullet.getChildren().add(textflow);
			textflow.setMaxWidth(parent.getParagraphwidth() - 20);
			textflow.setMinWidth(parent.getParagraphwidth() - 20);
			return paragraphwithbullet;

		}
		// title
		Label titleleftspace = new Label("  ");
		titleleftspace.setPadding(new Insets(6, 0, 5, 0));
		titleleftspace.setMinWidth(15);
		titleleftspace.setMaxWidth(15);
		HBox paragraphwithmargin = new HBox();
		paragraphwithmargin.setPadding(new Insets(0, 0, 0, 0));
		textflow.setMaxWidth(parent.getParagraphwidth());
		textflow.setMinWidth(parent.getParagraphwidth());
		textflow.setPadding(new Insets(6, 0, 5, 0));
		paragraphwithmargin.getChildren().add(titleleftspace);
		paragraphwithmargin.getChildren().add(textflow);
		Paragraph thisparagraph = this;
		titleleftspace.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				thisparagraph.setCarretAtFirst();

			}

		});
		return paragraphwithmargin;
	}

	/**
	 * @param selectionintextflow displays the caret at the given selection in the
	 *                            text flow
	 */
	public void displayCaretAt(int selectionintextflow) {
		this.parent.makeGlow();
		logger.finest(" --------------- Starting Display Caret at " + selectionintextflow + " -------------------");
		Paragraph thisparagraph = this;

		Task<Void> sleeper = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					Thread.sleep(5);
				} catch (InterruptedException e) {
				}
				return null;
			}
		};
		sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {

				Platform.runLater(new Runnable() {

					@Override
					public void run() {

						TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);

						GlyphList[] glyphlist = textlayout.getRuns();
						logger.finest("glyphlist found " + glyphlist.length);
						int partialselection = selectionintextflow;
						boolean nullglyphcount = false;
						for (int i = 0; i < glyphlist.length; i++) {
							GlyphList thisglyphlist = glyphlist[i];
							int glyphcount = thisglyphlist.getGlyphCount();
							if (glyphcount == 0) {
								glyphcount = 1; // this is for return carriage
								nullglyphcount = true;
							} else {
								nullglyphcount = false;
							}

							logger.finest("glyphlist " + thisglyphlist.getGlyphCount());
							if (thisglyphlist.getGlyphCount() >= partialselection) {

								if (!nullglyphcount)
									logger.finest("found hit (" + thisglyphlist.getPosX(partialselection) + ","
											+ thisglyphlist.getPosY(partialselection) + ","
											+ thisglyphlist.getCharOffset(partialselection) + ")");
								if (nullglyphcount)
									logger.finest("found hit on null glyphlist (0,0)");
								logger.finest("setting margin for paragraph layout");

								PathElement[] caret;
								caret = textlayout.getCaretShape(selectionintextflow, true, 0.0f, 0.0f);

								if (!nullglyphcount) {

									logger.finest(" --- *** displaying CaretShape with index = " + selectionintextflow
											+ " *** ---");

								} else {
									logger.finest(" *** --- for null glyphcount - Getting PosX for "
											+ thisglyphlist.toString());

								}

								logger.finest(" hit additional info " + thisglyphlist.getLocation().x + " -"
										+ thisglyphlist.getLocation().y);
								logger.finest("found caret = " + caret.length);

								Path caretpath = new Path(caret);

								double left = textflow.insetsProperty().getValue().getLeft();
								logger.finest("binding X to left = " + left + ", Insets binding left = "
										+ textflow.insetsProperty().getValue().getLeft());
								caretpath.layoutXProperty().bind(new DoubleBinding() {
									{
										super.bind(textflow.insetsProperty());
									}

									@Override
									protected double computeValue() {
										logger.finest("computing left in insets binding = "
												+ textflow.insetsProperty().getValue().getLeft());
										return textflow.insetsProperty().getValue().getLeft();
									}

								});

								caretpath.layoutYProperty().bind(new DoubleBinding() {
									{
										super.bind(textflow.insetsProperty());
									}

									@Override
									protected double computeValue() {
										return textflow.insetsProperty().getValue().getTop();
									}

								});

								caretpath.getStyleClass().add("caret");
								caretpath.setStrokeWidth(1);
								caretpath.setStroke(Color.BLACK);
								caretpath.setManaged(false);
								if (parent.caretpath != null) {
									parent.caretpath.setVisible(false);

								}
								parent.caretpath = caretpath;
								parent.activeparagraph = thisparagraph;
								textflow.getChildren().add(caretpath);
								textflow.requestLayout();
								textflow.requestFocus();
								parent.getPageActionManager().getClientDisplay().ensureNodeVisible(caretpath);

								break;
							}
							partialselection -= glyphcount;

						}
						// detect text layout

						FormattedText selectedtext = getTextAtCaret().selectedtext;
						parent.setSelection(selectedtext.isBold(), selectedtext.isItalic(),
								selectedtext.getSpecialcolor(), title, bulletpoint);

					}

				});
			}
		});
		new Thread(sleeper).start();

	}

	private class QualifiedCaretPosition {
		int localcaretindex;
		FormattedText selectedtext;
		int formattedtextindex;

		public QualifiedCaretPosition(int formattedtextindex, int localcaretindex, FormattedText selectedtext) {
			super();
			this.formattedtextindex = formattedtextindex;
			this.localcaretindex = localcaretindex;
			this.selectedtext = selectedtext;
		}

	}

	private QualifiedCaretPosition getTextAtTextFlowPosition(int textflowposition) {
		int currenttextlayoutcharacter = 0;
		int currenttextindex = 0;
		FormattedText selectedtext = textlist.get(0);
		logger.finest("-------------------------- getting qualified carret with selection = " + textflowposition
				+ "-----------------------");
		while ((currenttextlayoutcharacter < textflowposition) && (currenttextindex < textlist.size())) {
			selectedtext = textlist.get(currenttextindex);
			currenttextlayoutcharacter += selectedtext.getTextPayload().length();
			logger.finest("reviewing FormattedText " + currenttextindex + ", length = "
					+ selectedtext.getTextPayload().length() + ", total length = " + currenttextlayoutcharacter
					+ ", caretselection = " + textflowposition);
			currenttextindex++;
		}
		if (textflowposition == currenttextlayoutcharacter) {
			logger.finest("found end of FormattedText , text length = " + selectedtext.getTextPayload().length()
					+ " selection in flow = " + textflowposition + ", current text layout = "
					+ currenttextlayoutcharacter);

			// just at the end of text, adding all elements of size 0
			if (textlist.size() > currenttextindex)
				while (textlist.get(currenttextindex).getTextPayload().length() == 0) {
					logger.finest("adding zero formattedtext at index = " + currenttextindex);
					selectedtext = textlist.get(currenttextindex);
					currenttextindex++;
					if (textlist.size() == currenttextindex)
						break;
				}
		}
		logger.finest("-------------------------- qualified carret end (" + (currenttextindex - 1) + ","
				+ (textflowposition - currenttextlayoutcharacter + selectedtext.getTextPayload().length()) + ","
				+ selectedtext.getTextPayload() + ")-----------------------");
		// special case for first text of first paragraph, where it was adding letter at
		// the end.
		if (currenttextindex == 0)
			return new QualifiedCaretPosition(0, 0, selectedtext);
		return new QualifiedCaretPosition(currenttextindex - 1,
				textflowposition - currenttextlayoutcharacter + selectedtext.getTextPayload().length(), selectedtext);
	}

	private QualifiedCaretPosition getTextAtCaret() {
		return getTextAtTextFlowPosition(selectionintextflow);
	}

	/**
	 * @return true if the current selection is the first line, false else
	 */
	public boolean isSelectionAtFirstLine() {
		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		GlyphList[] glyphlist = textlayout.getRuns();
		if (glyphlist[0].getGlyphCount() >= selectionintextflow)
			return true;
		return false;
	}

	/**
	 * @return true if the current selection is the last line, false else
	 */
	public boolean isSelectionAtLastLine() {
		if (getLineForCarret() == getLastLine())
			return true;
		return false;
	}

	/**
	 * @return the number of lines in the text
	 */
	public int getLastLine() {
		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		GlyphList[] glyphlist = textlayout.getRuns();
		float lasty = 0;
		int lines = 0;
		for (int i = 0; i < glyphlist.length; i++) {
			GlyphList thisglyphlist = glyphlist[i];
			int glyphcount = thisglyphlist.getGlyphCount();
			if (glyphcount == 0)
				glyphcount = 1;
			if (lasty != thisglyphlist.getLocation().y) {
				lasty = thisglyphlist.getLocation().y;
				lines++;
			}

		}
		return lines;
	}

	/**
	 * @return the xoffset for the selection. Paragraphs, depending on size, have
	 *         differrent offsets (titles to the left, bullet points to the right)
	 */
	public float getXOffSetForSelection() {

		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		GlyphList[] glyphlist = textlayout.getRuns();
		int partialselection = selectionintextflow;

		for (int i = 0; i < glyphlist.length; i++) {
			GlyphList thisglyphlist = glyphlist[i];
			int glyphcount = thisglyphlist.getGlyphCount();
			if (glyphcount == 0)
				glyphcount = 1; // this is for return carriage

			if (thisglyphlist.getGlyphCount() >= partialselection) {
				float xmargin = 0;
				if (title) {
					xmargin = 10;
				}
				if (bulletpoint) {
					xmargin = 25;
				}

				float xoffsetforchar = (float) (thisglyphlist.getGlyphCount() > 0
						? thisglyphlist.getPosX(partialselection)
						: 0) + thisglyphlist.getLocation().x - 7 + xmargin;
				return xoffsetforchar;
			}
			partialselection -= glyphcount;
		}
		return -1;
	}

	/**
	 * sets the selection for the given line, with the given caret index
	 * 
	 * @param line       an integer between 0 and the index of last line
	 * @param caretindex actual position of thecaret in pixels
	 */
	public void setSelectionat(int line, float caretindex) {
		logger.finest(" -- set selection at " + line + " line for caretindex = " + caretindex);
		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		GlyphList[] glyphlist = textlayout.getRuns();

		float lasty = 0;
		int currentline = 0;
		int finalcharacterselection = 0;
		for (int i = 0; i < glyphlist.length; i++) {
			GlyphList thisglyphlist = glyphlist[i];
			int glyphcount = thisglyphlist.getGlyphCount();
			if (glyphcount == 0)
				glyphcount = 1;
			if (lasty != thisglyphlist.getLocation().y) {
				lasty = thisglyphlist.getLocation().y;
				currentline++;
			}

			if (currentline == line) {
				logger.finest("Found line for glyphindex = " + i);
				int previousindex = 0;
				for (int j = 0; j < thisglyphlist.getGlyphCount(); j++) {
					float xmargin = 0;
					if (title) {
						xmargin = 10;
					}
					if (bulletpoint) {
						xmargin = 25;
					}
					Float currentx = thisglyphlist.getPosX(j) + thisglyphlist.getLocation().x - 7 + xmargin;

					if (currentx > caretindex) {
						logger.finest("  -- !!! -- weird logic currentx>caretindex " + currentx + "/" + caretindex);
						finalcharacterselection += previousindex;
						selectionintextflow = finalcharacterselection;
						logger.finest("Selection in text flow = " + selectionintextflow);
						displayCaretAt(selectionintextflow);
						return;
					} else {
						logger.finest(" -- !!! -- weird logic currentx<=caretindex " + currentx + "/" + caretindex);
					}
					previousindex++;
				}

			}
			if (currentline > line) {
				selectionintextflow = finalcharacterselection - 1;
				logger.finest("Selection in text flow = " + selectionintextflow);
				displayCaretAt(selectionintextflow);
				return;
			}
			finalcharacterselection += glyphcount;
		}
		selectionintextflow = finalcharacterselection;
		logger.finest("Selection in text flow = " + selectionintextflow);
		displayCaretAt(selectionintextflow);
		return;
	}

	/**
	 * @return get the line on which the caret is
	 */
	public int getLineForCarret() {
		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		GlyphList[] glyphlist = textlayout.getRuns();
		int partialselection = selectionintextflow;
		float lasty = 0;
		int lines = 0;
		for (int i = 0; i < glyphlist.length; i++) {
			GlyphList thisglyphlist = glyphlist[i];
			int glyphcount = thisglyphlist.getGlyphCount();
			if (glyphcount == 0)
				glyphcount = 1;

			if (lasty != thisglyphlist.getLocation().y) {
				lasty = thisglyphlist.getLocation().y;
				lines++;
			}
			if (thisglyphlist.getGlyphCount() >= partialselection)
				return lines;
			partialselection -= glyphcount;

		}
		return -1;
	}

	/**
	 * selects the char corresponding to points x and y
	 * 
	 * @param x x mouse click coordinate
	 * @param y y mouse clock coordinate
	 * @return the closest character index if hit
	 */
	public int getCharSelectionOnCoordinates(double x, double y) {
		Point2D pointinscreen = new Point2D(x, y);
		Point2D localpoint = textflow.screenToLocal(pointinscreen);
		TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
		TextLine[] textlines = (TextLine[]) invoke(getLines, textlayout);
		logger.finest("found textlines number = " + textlines);

		HitInfo hit = textlayout.getHitInfo((float) (localpoint.getX() - textflow.getInsets().getLeft()),
				(float) (localpoint.getY() - textflow.getInsets().getLeft()));
		if (hit != null) {
			logger.finest("click detected and found hit " + hit.getCharIndex() + " - for (" + localpoint.getX() + ","
					+ localpoint.getY() + ")");

		} else {
			logger.finest("click detected and did not find hit");
		}
		if (getCharNb() > 0) {
			return hit.getCharIndex() + 1;
		} else {
			return hit.getCharIndex();
		}

	}

	private void createSelectionListener() {

		Paragraph thisparagraph = this;

		textflow.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				thisparagraph.hideSelection();
				int characterclickedon = thisparagraph.getCharSelectionOnCoordinates(event.getScreenX(),
						event.getScreenY());

				if (characterclickedon > 0)
					characterclickedon--;
				logger.finest("Started potential drag at " + characterclickedon);
				thisparagraph.dragstartindex = characterclickedon;
				thisparagraph.dragactive = true;
				// thisparagraph.parent.resetOtherSelections(thisparagraph);
			}

		});

		textflow.setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {

			@Override
			public void handle(MouseDragEvent event) {
				logger.finest("Detected mouse drag entered");

			}

		});

		textflow.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				int characterclickedon = thisparagraph.getCharSelectionOnCoordinates(event.getScreenX(),
						event.getScreenY());

				logger.finest("Potential drag continuing at  " + characterclickedon);
				thisparagraph.dragendindex = characterclickedon;
				thisparagraph.displaySelection();
			}

		});
		textflow.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue.booleanValue()) {
					if (!dragactive) {
						logger.finest(" ---- Lost focus new focus = " + newValue.booleanValue() + " old focus "
								+ oldValue.booleanValue());
						thisparagraph.hideSelectionAndResetIndex();
					} else {
						logger.finest(" ---- Lost focus discarded as drag active, new focus = "
								+ newValue.booleanValue() + " old focus " + oldValue.booleanValue());
					}
				} else {
					logger.finest(" ---- Focus new focus = " + newValue.booleanValue() + " old focus "
							+ oldValue.booleanValue());

				}

			}

		});
	}

	private void createFlowWriteListener() {
		textflow.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(
					ObservableValue<? extends Boolean> arg0,
					Boolean oldPropertyValue,
					Boolean newPropertyValue) {
				if (newPropertyValue) {
					logger.finest("Textfield on focus");
				} else {
					logger.finest("Textfield out focus");
				}
			}
		});
		Paragraph thisparagraph = this;
		textflow.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> property, Boolean oldvalue, Boolean newvalue) {
				if (newvalue.booleanValue() == false) {
					if (parent.caretpath != null) {
						if (parent.activeparagraph == thisparagraph) {
							// parent.activeparagraph=null;
							parent.caretpath.setVisible(false);
							parent.caretpath = null;
							parent.loseGlow();
						}
					} else {
						parent.activeparagraph = thisparagraph;
					}
				}

			}

		});

		textflow.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				double x = event.getScreenX();
				double y = event.getScreenY();
				selectionintextflow = thisparagraph.getCharSelectionOnCoordinates(x, y);
				textflow.requestFocus();

				displayCaretAt(selectionintextflow);
				logger.finest("Set active paragraph " + thisparagraph + " for richtextarea " + parent
						+ " for char index = " + selectionintextflow);
				parent.activeparagraph = thisparagraph;

			}

		});
		textflow.setOnKeyTyped(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent keyevent) {
				boolean istreated = false;
				if (keyevent.getCharacter().compareTo(KeyEvent.CHAR_UNDEFINED) != 0)
					if (!isNonPrintable(keyevent.getCharacter().charAt(0))) {
						logger.finest("Key typed '" + keyevent.getCharacter() + "'");
						if ((bulletpoint) || (title))
							if ((keyevent.getCode().equals(KeyCode.ENTER)) || (keyevent.getCharacter().equals("\r"))) {
								logger.finest("Special treatment for ENTER key in title or bullet point");
								// if bullet point and text before caret create new bullet point with all text
								// after caret
								if ((bulletpoint) && (selectionintextflow > 0)) {
									QualifiedCaretPosition caretposition = getTextAtCaret();

									if (caretposition.formattedtextindex != 0)
										throw new RuntimeException("several paragraphs not supported in bullet point");
									if (parent.okToAdd(5)) {
										FormattedText activetext = textlist.get(0);
										logger.finest(
												"    ** -- ** trace caretposition " + caretposition.localcaretindex);
										String texttoremain = activetext.getTextPayload().substring(0,
												caretposition.localcaretindex);
										String texttosendtonext = activetext.getTextPayload()
												.substring(caretposition.localcaretindex);
										activetext.setString(texttoremain);
										parent.moveTextToNext(texttosendtonext, true);
									} else {
										giveBackFocus();
									}
								}

								// if bullet point and no text before, move text to normal text
								if ((bulletpoint) && (selectionintextflow == 0)) {
									QualifiedCaretPosition caretposition = getTextAtCaret();
									if (caretposition.formattedtextindex != 0)
										throw new RuntimeException("several paragraphs not supported in bullet point");
									setNormalParagraph();
									parent.redrawActiveParagraph();
								}

								// if title, and text before caret and last paragraph, create new paragraph with
								// text
								if ((title) && (selectionintextflow > 0)) {
									QualifiedCaretPosition caretposition = getTextAtCaret();
									if (caretposition.formattedtextindex != 0)
										throw new RuntimeException("several paragraphs not supported in bullet point");
									if (parent.okToAdd(4)) {
										FormattedText activetext = textlist.get(0);
										String texttoremain = activetext.getTextPayload().substring(0,
												caretposition.localcaretindex);
										String texttosendtonext = activetext.getTextPayload()
												.substring(caretposition.localcaretindex);
										activetext.setString(texttoremain);
										parent.moveTextToNext(texttosendtonext, false);
									}
								}
								// if title, and no text before caret and last paragraph, create paragraph with
								// text
								if ((title) && (selectionintextflow == 0)) {
									QualifiedCaretPosition caretposition = getTextAtCaret();
									if (caretposition.formattedtextindex != 0)
										throw new RuntimeException("several paragraphs not supported in bullet point");
									setNormalParagraph();
									parent.redrawActiveParagraph();
								}
								istreated = true;
							}

						if (!istreated) {
							QualifiedCaretPosition caretposition = getTextAtCaret();
							if (parent.okToAdd(1)) {
								logger.finest("-- adding character at " + caretposition.localcaretindex + ", char="
										+ keyevent.getCharacter());
								FormattedText thistext = caretposition.selectedtext;
								String before = "";
								if (thistext.getTextPayload().length() > 0)
									before = thistext.getTextPayload().substring(0,
											(caretposition.localcaretindex > thistext.getTextPayload().length()
													? thistext.getTextPayload().length()
													: caretposition.localcaretindex));
								String after = "";
								logger.finest("thistext.length = " + thistext.getTextPayload().length()
										+ ", local caret index " + caretposition.localcaretindex);
								if (thistext.getTextPayload().length() > 0)
									if (caretposition.localcaretindex >= 0)
										if (caretposition.localcaretindex <= thistext.getTextPayload().length())
											after = thistext.getTextPayload().substring(caretposition.localcaretindex);
								thistext.setString(before + keyevent.getCharacter() + after);
								selectionintextflow++;
								displayCaretAt(selectionintextflow);
							}
						}
					}
			}

		});
		textflow.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent keyevent) {
				logger.finest("detected key event " + keyevent.getCode().getName() + " - " + keyevent.getCharacter());
				QualifiedCaretPosition caretposition = getTextAtCaret();

				int totallength = 0;

				for (int i = 0; i < textlist.size(); i++) {
					totallength += textlist.get(i).getTextPayload().length();
				}
				FormattedText thistext = caretposition.selectedtext;
				if (keyevent.getCode().equals(KeyCode.DELETE)) {

					if ((caretposition.localcaretindex >= 0)
							&& (caretposition.localcaretindex < thistext.getTextPayload().length() - 1)) {
						logger.finest("processing DELETE event at index " + selectionintextflow);

						thistext.setString(thistext.getTextPayload().substring(0, caretposition.localcaretindex)
								+ thistext.getTextPayload().substring(caretposition.localcaretindex + 1));
						keyevent.consume();

					}
				}
				if (keyevent.getCode().equals(KeyCode.BACK_SPACE)) {

					if ((caretposition.localcaretindex > 0)) {
						logger.finest(
								"processing BACKSPACE event for text '" + thistext.getTextPayload() + "' at index "
										+ selectionintextflow + ", localcaretindex = " + caretposition.localcaretindex);

						String textbefore = thistext.getTextPayload().substring(0, caretposition.localcaretindex - 1);

						String textafter = thistext.getTextPayload().substring(caretposition.localcaretindex);

						thistext.setString(textbefore + textafter);
						keyevent.consume();
						selectionintextflow--;
						displayCaretAt(selectionintextflow);

					}
					if ((caretposition.localcaretindex == 0)
							&& (caretposition.selectedtext.getTextPayload().length() == 0)) {

						int index = caretposition.formattedtextindex;
						logger.finest("processing BACKSPACE of empty section for index = " + index);
						if (textlist.size() > 1) {

							textlist.remove(index);
							textflow.getChildren().remove(index);

							displayCaretAt(selectionintextflow);
						}
						if (textlist.size() == 1) {
							parent.deleteActiveParagraphIfNotLast();
						}
					}
				}
				// simple right: move cursor
				if ((keyevent.getCode().equals(KeyCode.LEFT)) && (!keyevent.isControlDown())) {
					boolean treated = false;
					if (selectionintextflow > 0) {
						selectionintextflow--;
						displayCaretAt(selectionintextflow);
						logger.finest("processing LEFT event at index " + selectionintextflow);
						keyevent.consume();
						treated = true;
					}
					if (!treated)
						if (selectionintextflow == 0) {
							logger.finest("request caret to previous paragraph ");
							keyevent.consume();
							parent.requestCarretToPrevious();

						}

				}
				// control + left allows permutation between normal text and title. If title,
				// will
				// make normal text, if not title (even title), will make title
				if ((keyevent.getCode().equals(KeyCode.LEFT)) && (keyevent.isControlDown())) {
					if (title) {
						setNormalParagraph();
						parent.redrawActiveParagraph();
					} else {
						if (parent.okToAdd(9)) {
							parent.splitcurrentlineinparagraph();
							parent.activeparagraph.setTitleParagraph();
							parent.redrawActiveParagraph();
						} else {
							giveBackFocus();
						}
					}
				}
				// control + right allows permutation between normal text and bullet. If bullet,
				// will
				// make normal text, if not bullet (even title), will make bullet
				if ((keyevent.getCode().equals(KeyCode.RIGHT)) && (keyevent.isControlDown())) {
					if (bulletpoint) {
						setNormalParagraph();
						parent.redrawActiveParagraph();
					} else if (parent.okToAdd(7)) {
						parent.splitcurrentlineinparagraph();
						parent.activeparagraph.setBulletParagraph();
						parent.redrawActiveParagraph();
					}
				}
				if ((keyevent.getCode().equals(KeyCode.UP))) {
					logger.finest("pressed up");
					float xoffset = getXOffSetForSelection();
					if (!isSelectionAtFirstLine()) {
						logger.finest("not at first line");
						int currentline = getLineForCarret();
						currentline--;
						setSelectionat(currentline, xoffset);
					} else {
						logger.finest("first line");
						parent.requestCarretToPreviousAtOffset(xoffset);
					}
					keyevent.consume();

				}

				if ((keyevent.getCode().equals(KeyCode.DOWN))) {
					float xoffset = getXOffSetForSelection();
					if (!isSelectionAtLastLine()) {

						int currentline = getLineForCarret();
						currentline++;
						setSelectionat(currentline, xoffset);
					} else {
						parent.requestCarretToNextAtOffset(xoffset);
					}
					keyevent.consume();

				}
				// simple left: move cursor
				if ((keyevent.getCode().equals(KeyCode.RIGHT)) && (!keyevent.isControlDown())) {
					boolean treated = false;
					if (selectionintextflow < totallength) {
						selectionintextflow++;
						displayCaretAt(selectionintextflow);
						logger.finest("processing RIGHT event at index " + selectionintextflow);
						keyevent.consume();
						treated = true;
					}
					if (!treated)
						if (selectionintextflow >= totallength) {
							logger.finest("request caret to next paragraph ");
							keyevent.consume();
							parent.requestCarretToNext();

						}
				}
				if ((keyevent.getCode().equals(KeyCode.V)) && (keyevent.isControlDown())) {
					logger.finest("detecting PASTE event at index " + selectionintextflow);
					Clipboard clipboard = Clipboard.getSystemClipboard();
					String clipboardcontent = clipboard.getString();

					String currenttext = thistext.getTextPayload();

					if (clipboardcontent != null) {
						if (parent.okToAdd(clipboardcontent.length())) {
							String concatenatestring = currenttext.substring(0, caretposition.localcaretindex)
									+ clipboardcontent
									+ (currenttext.length() > caretposition.localcaretindex
											? currenttext.substring(caretposition.localcaretindex)
											: "");
							thistext.setString(concatenatestring);
							selectionintextflow += clipboardcontent.length();
							displayCaretAt(selectionintextflow);

						} else {
							parent.getPageActionManager().getClientSession().getActiveClientDisplay()
									.updateStatusBar("Warning: you tried to paste a string of "
											+ clipboardcontent.length()
											+ " characters that is too long for the field (maximum including formatting: "
											+ parent.getMaxTextLength() + " characters).", true);
						}
					}
				}

			}

		});

	}

	protected void hideSelection() {
		if (hightlightpath != null) {
			hightlightpath.setVisible(false);
			textflow.getChildren().remove(hightlightpath);
			hightlightpath = null;
		}
	}

	protected void hideSelectionAndResetIndex() {
		hideSelection();
		this.dragstartindex = -1;
		this.dragendindex = -1;
		this.dragactive = false;

	}

	protected void displaySelection() {
		logger.finest(" - DisplaySelection - Try to print " + dragstartindex + " - " + dragendindex);
		if (this.dragstartindex >= 0)
			if (this.dragendindex >= 0)
				if (this.dragendindex > this.dragstartindex) {
					hideSelection();
					TextLayout textlayout = (TextLayout) invoke(getTextLayout, textflow);
					PathElement[] highlight = textlayout.getRange(this.dragstartindex, this.dragendindex,
							TextLayout.TYPE_TEXT, 0, 0);
					hightlightpath = new Path(highlight);
					hightlightpath.setManaged(false);
					hightlightpath.setFill(Color.web("#222235", 0.2));
					hightlightpath.setStrokeWidth(0);
					if (title)
						hightlightpath.setTranslateY(6);
					if (bulletpoint) {
						hightlightpath.setTranslateY(2);
						hightlightpath.setTranslateX(5);

					}
					textflow.getChildren().add(hightlightpath);
					textflow.requestLayout();
					textflow.requestFocus();
				}
	}

	protected int getCharAt(int charIndex) {
		int currenttextsection = 0;
		int cumulatedchar = 0;
		while (currenttextsection < this.textlist.size()) {
			int currenttextsectionlength = this.textlist.get(currenttextsection).getTextPayload().length();
			if (cumulatedchar + currenttextsectionlength > charIndex)
				return this.textlist.get(currenttextsection).getTextPayload().charAt(charIndex - cumulatedchar);
			cumulatedchar += currenttextsectionlength;
		}
		return -1;
	}

	private static Object invoke(Method m, Object obj, Object... args) {
		try {
			return m.invoke(obj, args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private static boolean isNonPrintable(char character) {
		if (character == '\u0008')
			return true; // backspace
		if (character == '\u007F')
			return true; // delete
		return false;

	}

	void dropDescription() {
		logger.warning(" ++ paragraph (bullet=" + bulletpoint + ",title=" + title + " hasfocus = "
				+ this.textflow.isFocused() + " nbtext = " + this.textlist.size());

	}

	/**
	 * @return true if there is any significant text
	 */
	public boolean hasSignificantContent() {
		if (textlist.size() == 0)
			return false;
		return true;
	}

	/**
	 * gets back the focus on this item
	 */
	public void giveBackFocus() {
		if (this.selectionintextflow > -1) {
			this.textflow.requestFocus();
			this.displayCaretAt(selectionintextflow);
		}
		if (this.selectionintextflow == -1) {
			this.selectionintextflow = 0;
			this.textflow.requestFocus();
			this.displayCaretAt(selectionintextflow);
		}

	}

	/**
	 * specific hack for ASCII character 22
	 */
	public void dirtyhackcaretminus() {
		selectionintextflow--;
		displayCaretAt(selectionintextflow);
	}

	private FormattedText insertSectionAtSelectionIfRequired() {
		QualifiedCaretPosition startposition = this.getTextAtTextFlowPosition(dragstartindex);
		QualifiedCaretPosition endposition = this.getTextAtTextFlowPosition(dragendindex);
		FormattedText uniquetext = null;
		if (startposition.localcaretindex == startposition.selectedtext.getTextPayload().length())
			if (this.textlist.size() > startposition.formattedtextindex + 1) {
				QualifiedCaretPosition newstartposition = new QualifiedCaretPosition(
						startposition.formattedtextindex + 1, 0,
						this.textlist.get(startposition.formattedtextindex + 1));
				startposition = newstartposition;
			}

		if (startposition.selectedtext == endposition.selectedtext)
			uniquetext = startposition.selectedtext;

		if (uniquetext != null) {

			if (startposition.localcaretindex == 0) {
				// selection starts at beginning of section
				if (endposition.localcaretindex == uniquetext.getTextPayload().length()) {
					// return current, full section is selected
					return uniquetext;
				} else {
					// split in 2 and return the first section
					String starttext = uniquetext.getTextPayload().substring(0, endposition.localcaretindex);
					String endtext = uniquetext.getTextPayload().substring(endposition.localcaretindex);
					uniquetext.setString(starttext);
					FormattedText end = new FormattedText(uniquetext, this);
					end.setString(endtext);
					textlist.add(startposition.formattedtextindex + 1, end);
					textflow.getChildren().add(startposition.formattedtextindex + 1, end.getNode());
					return uniquetext;
				}
			} else {
				// selection starts at middle of section
				if (endposition.localcaretindex == uniquetext.getTextPayload().length()) {
					// split in 2 and return the second section
					String starttext = uniquetext.getTextPayload().substring(0, startposition.localcaretindex);
					String endtext = uniquetext.getTextPayload().substring(startposition.localcaretindex);
					uniquetext.setString(starttext);
					FormattedText end = new FormattedText(uniquetext, this);
					end.setString(endtext);
					textlist.add(startposition.formattedtextindex + 1, end);
					textflow.getChildren().add(startposition.formattedtextindex + 1, end.getNode());
					return end;
				} else {
					String starttext = uniquetext.getTextPayload().substring(0, startposition.localcaretindex);
					String middletext = uniquetext.getTextPayload().substring(startposition.localcaretindex,
							endposition.localcaretindex);
					String endtext = uniquetext.getTextPayload().substring(endposition.localcaretindex);
					uniquetext.setString(starttext);
					FormattedText middle = new FormattedText(uniquetext, this);
					FormattedText end = new FormattedText(uniquetext, this);
					middle.setString(middletext);
					end.setString(endtext);
					textlist.add(startposition.formattedtextindex + 1, middle);
					textflow.getChildren().add(startposition.formattedtextindex + 1, middle.getNode());
					textlist.add(startposition.formattedtextindex + 2, end);
					textflow.getChildren().add(startposition.formattedtextindex + 2, end.getNode());
					return middle;
				}
			}
		} else {
			return null;
		}
	}

	private FormattedText insertSectionAtCaretPosition(QualifiedCaretPosition position) {
		// if already created a zero length text, stick on it.
		if (position.selectedtext.getTextPayload().length() == 0) {
			logger.finer("current text has zero length, do not modify it");
			return position.selectedtext;
		}
		// longer text 3 cases to mange
		// at beginning of section, just insert text before
		if (position.localcaretindex == 0) {
			logger.finer("new text will be inserted at the beginning of current text");

			FormattedText formattedtext = new FormattedText(position.selectedtext, this);
			textlist.add(position.formattedtextindex, formattedtext);
			textflow.getChildren().add(position.formattedtextindex, formattedtext.getNode());
			return formattedtext;
		}
		// at end of section, insert text at the end (case last text last char of
		// paragraph
		if (position.localcaretindex == position.selectedtext.getTextPayload().length()) {
			logger.finer("new text will be inserted at the end of current text");
			FormattedText formattedtext = new FormattedText(position.selectedtext, this);
			textlist.add(position.formattedtextindex + 1, formattedtext);
			textflow.getChildren().add(position.formattedtextindex + 1, formattedtext.getNode());
			return formattedtext;
		}

		// at middle of section - step 1 - add new text
		FormattedText newformattedtext = new FormattedText(position.selectedtext, this);
		newformattedtext.setString("");
		textlist.add(position.formattedtextindex + 1, newformattedtext);
		textflow.getChildren().add(position.formattedtextindex + 1, newformattedtext.getNode());
		// at middle of section - step 2 - reduce current text
		String beginning = position.selectedtext.getTextPayload().substring(0, position.localcaretindex);
		String end = position.selectedtext.getTextPayload().substring(position.localcaretindex);
		position.selectedtext.setString(beginning);
		// at middle of section - step 3 - add reminder of existing text
		FormattedText endofcurrentext = new FormattedText(position.selectedtext, this);
		endofcurrentext.setString(end);
		logger.finer("splitting current text in 2 and inserting in the middle [" + beginning + "|" + end + "]");
		textlist.add(position.formattedtextindex + 2, endofcurrentext);
		textflow.getChildren().add(position.formattedtextindex + 2, endofcurrentext.getNode());
		textflow.requestLayout();
		dropDescription();
		return newformattedtext;
	}

	private FormattedText insertSectionAtCaretIfRequired() {

		QualifiedCaretPosition currentposition = this.getTextAtCaret();
		return insertSectionAtCaretPosition(currentposition);
	}

	/**
	 * inserts a color indicator at the current caret position
	 * 
	 * @param value color indicator
	 */
	public void insertColorIndicator(Color value) {
		boolean processed=false;
		if (this.dragactive) {
			if (this.dragstartindex > 0)
				if (this.dragendindex > 0)
					if (this.dragendindex > this.dragstartindex) {
						processed=true;
						FormattedText relevanttext = insertSectionAtSelectionIfRequired();
						if (relevanttext != null)
							relevanttext.setSpecialcolor(value);
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(5);
								} catch (Exception e) {
								}
								displaySelection();

							}
						});
					}
		} 
		if (!processed) {
			FormattedText relevanttext = insertSectionAtCaretIfRequired();
			relevanttext.setSpecialcolor(value);
		}
	}

	/**
	 * insert bold indicator
	 * 
	 * @param selected true to put bold, false, to put back to normal
	 */
	public void insertBoldIndicator(boolean selected) {
		boolean processed=false;
		if (this.dragactive) {
			if (this.dragstartindex > 0)
				if (this.dragendindex > 0)
					if (this.dragendindex > this.dragstartindex) {
						processed=true;
						FormattedText relevanttext = insertSectionAtSelectionIfRequired();
						if (relevanttext != null)
							relevanttext.setBold(!relevanttext.isBold());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(5);
								} catch (Exception e) {
								}
								displaySelection();

							}
						});
					}
		} 
		if (!processed) {
			FormattedText relevanttext = insertSectionAtCaretIfRequired();
			relevanttext.setBold(selected);

		}
	}

	/**
	 * insert italic indicator
	 * 
	 * @param selected true to start an italic section, false to end an italic
	 *                 section
	 */
	public void insertItalicIndicator(boolean selected) {
		boolean processed=false;
		if (this.dragactive) {
			if (this.dragstartindex > 0)
				if (this.dragendindex > 0)
					if (this.dragendindex > this.dragstartindex) {
						processed=true;
						FormattedText relevanttext = insertSectionAtSelectionIfRequired();
						if (relevanttext != null)
							relevanttext.setItalic(!relevanttext.isItalic());
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								try {
									Thread.sleep(5);
								} catch (Exception e) {
								}
								displaySelection();

							}
						});
					}
		}
		
		if (!processed) {
			FormattedText relevanttext = insertSectionAtCaretIfRequired();
			relevanttext.setItalic(selected);
		}
	}

	/**
	 * provides the first carriage return index on text after start index
	 * 
	 * @param text       input text
	 * @param startindex start index
	 * @return index of next carriage return
	 */
	private int indexOfFirstCarriageReturn(String text, int startindex) {
		int returncarriageindex = text.indexOf("\n", startindex);
		if ((returncarriageindex == -1) || (returncarriageindex > text.indexOf("\r", startindex) + 1))
			returncarriageindex = text.indexOf("\r", startindex);
		return returncarriageindex;
	}

	/**
	 * returns a paragraph with all formatted text before the carret, and, if
	 * existing, any text before the last carriage return in current formatted text
	 * before the caret
	 * 
	 * @return
	 */
	public Paragraph generateParagraphBeforePreviousBreak() {
		QualifiedCaretPosition caretposition = getTextAtCaret();
		Paragraph returnparagraph = null;
		logger.finer("	** --- starting generate paragraph before previous break ---");
		// getting all formatted text before
		if (caretposition.formattedtextindex != 0) {
			returnparagraph = new Paragraph(richtext, editable, parent);
			for (int i = 0; i < caretposition.formattedtextindex; i++) {
				returnparagraph.addText(this.textlist.get(i));
			}
		}
		// getting text before if relevant
		FormattedText selectedtext = caretposition.selectedtext;
		String text = selectedtext.getTextPayload();
		int returncarriageindex = indexOfFirstCarriageReturn(text, 0);
		logger.finest("trying to find return carriage before the position in local text of "
				+ caretposition.localcaretindex + ", first index found = " + returncarriageindex);
		int bestcarriageindex = -1;
		while ((returncarriageindex != -1) && (returncarriageindex < caretposition.localcaretindex)) {
			bestcarriageindex = returncarriageindex;
			returncarriageindex = indexOfFirstCarriageReturn(text, returncarriageindex + 1);
		}
		if (bestcarriageindex != -1) {
			if (returnparagraph == null)
				returnparagraph = new Paragraph(richtext, editable, parent);
			FormattedText cuttext = new FormattedText(selectedtext.getSection(), this);
			cuttext.setString(text.substring(0, bestcarriageindex));
			returnparagraph.addText(cuttext);
		}

		logger.finer("	** --- ending generate paragraph before previous break, return paragraph = "
				+ (returnparagraph == null ? "null" : "" + returnparagraph.textlist.size()) + " ---");

		return returnparagraph;
	}

	/**
	 * returns a paragraph with all formatted text after the carret, and, if
	 * existing, any text after the first carriage return in current formatted text
	 * 
	 * @return
	 */

	public Paragraph generateParagraphAfterNextBreak() {
		QualifiedCaretPosition caretposition = getTextAtCaret();
		Paragraph returnparagraph = null;
		// getting text after if relevant
		logger.finer("	** --- starting generate paragraph after previous break ---");
		FormattedText selectedtext = caretposition.selectedtext;
		String text = selectedtext.getTextPayload();
		int returncarriageindex = indexOfFirstCarriageReturn(text, caretposition.localcaretindex);
		logger.finest("trying to find return carriage after the position in local text of "
				+ caretposition.localcaretindex + ", first index found = " + returncarriageindex);

		int bestcarriageindex = returncarriageindex;

		if (bestcarriageindex != -1) {
			if (returnparagraph == null)
				returnparagraph = new Paragraph(richtext, editable, parent);
			FormattedText cuttext = new FormattedText(selectedtext.getSection(), this);
			cuttext.setString(text.substring(bestcarriageindex + 1));
			returnparagraph.addText(cuttext);
		}

		// getting formatted text after carret position
		if (caretposition.formattedtextindex < textlist.size() - 1) {
			if (returnparagraph == null)
				returnparagraph = new Paragraph(richtext, editable, parent);
			for (int i = caretposition.formattedtextindex + 1; i < textlist.size(); i++) {
				returnparagraph.addText(this.textlist.get(i));
			}
		}
		logger.finer("	** --- ending generate paragraph after previous break, return paragraph = "
				+ (returnparagraph == null ? "null" : "" + returnparagraph.textlist.size()) + " ---");
		return returnparagraph;
	}

	/**
	 * generates a paragraph between two carriage returns
	 * 
	 * @return the paragraph to generate
	 */
	public Paragraph generateParagraphbetweenBreak() {
		logger.finer("	** --- starting generate paragraph between breaks ---");
		QualifiedCaretPosition caretposition = getTextAtCaret();
		FormattedText selectedtext = caretposition.selectedtext;
		String text = selectedtext.getTextPayload();

		// get return carriage index before caret
		int startreturncarriageindex = indexOfFirstCarriageReturn(text, 0);
		int startbestcarriageindex = -1;
		while ((startreturncarriageindex != -1) && (startreturncarriageindex < caretposition.localcaretindex)) {
			startbestcarriageindex = startreturncarriageindex;
			startreturncarriageindex = indexOfFirstCarriageReturn(text, startreturncarriageindex + 1);
		}

		// get return carriage index after caret
		int endreturncarriageindex = indexOfFirstCarriageReturn(text, caretposition.localcaretindex);
		int endbestcarriageindex = endreturncarriageindex;

		FormattedText cuttext = new FormattedText(selectedtext.getSection(), this);
		if (startbestcarriageindex == -1)
			startbestcarriageindex = 0;
		if (startbestcarriageindex > 0)
			startbestcarriageindex++;
		if (endbestcarriageindex == -1)
			endbestcarriageindex = text.length();
		String cuttextpayload = text.substring(startbestcarriageindex, endbestcarriageindex);
		cuttext.setString(cuttextpayload);
		Paragraph returnparagraph = new Paragraph(richtext, editable, parent);
		returnparagraph.selectionintextflow = caretposition.localcaretindex - startbestcarriageindex;
		returnparagraph.addText(cuttext);
		logger.finer("	** --- ending generate paragraph between breaks, text = [" + cuttextpayload + "] ---");

		return returnparagraph;
	}

	/**
	 * @return true if paragraph is normal
	 */
	public boolean isNormal() {
		if (bulletpoint)
			return false;
		if (title)
			return false;
		return true;
	}

	/**
	 * inserts text at the beginning
	 * 
	 * @param text text to insert (can be encoded rich text)
	 */
	public void insertTextAtFirst(String text) {
		FormattedText thisformattedtext = this.textlist.get(0);
		thisformattedtext.setString(text + thisformattedtext.getTextPayload());
	}
}
