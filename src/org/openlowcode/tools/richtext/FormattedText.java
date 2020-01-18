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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.text.FontSmoothingType;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;

/**
 * One element of formatted text inside a rich text session
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FormattedText {
	private RichTextSection section;
	private Text text;
	private boolean bold;
	private boolean italic;
	private boolean title;
	private Color specialcolor;

	void setTitle(boolean title) {
		this.title = title;
		formatText();
	}

	RichTextSection getSection() {
		return section;
	}

	/**
	 * @param section
	 */
	public void setSection(RichTextSection section) {
		this.section = section;
	}

	/**
	 * @param bold
	 */
	public void setBold(boolean bold) {
		this.bold = bold;
		formatText();

	}

	/**
	 * @param italic
	 */
	public void setItalic(boolean italic) {
		this.italic = italic;
		formatText();
	}

	/**
	 * @param specialcolor
	 */
	public void setSpecialcolor(Color specialcolor) {
		this.specialcolor = specialcolor;
		formatText();
	}

	/**
	 * @return
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * @return
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * @return
	 */
	public Color getSpecialcolor() {
		return specialcolor;
	}

	private void formatText() {
		if (!this.title) {
			text.setUnderline(false);

			if (this.bold)
				if (!this.italic)
					text.setStyle("-fx-font-weight: bold");
			if (!this.bold)
				if (this.italic)
					text.setStyle("-fx-font-style: italic");
			if (this.bold)
				if (this.italic)
					text.setStyle("-fx-font-weight: bold;-fx-font-style: italic");
			if (!this.bold)
				if (!this.italic)
					text.setStyle("-fx-font-weight:normal;-fx-font-style:normal");
			if (this.specialcolor != null) {
				text.setFill(this.specialcolor);

			}
		}
		if (this.title) {
			text.setStyle("");
			text.setFont(Font.font(text.getFont().getName(), FontWeight.BOLD, text.getFont().getSize()));
			text.setUnderline(true);
		}
		text.setFontSmoothingType(FontSmoothingType.LCD);
	}

	/**
	 * creates a new formatted text
	 * 
	 * @param section section
	 * @param parent  parent paragraph
	 */
	public FormattedText(RichTextSection section, Paragraph parent) {
		this.section = section;
		this.text = new Text(section.getText());

		this.text.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String before, String after) {
				boolean changed = false;
				for (int i = 0; i < after.length(); i++) {
					if (after.charAt(i) == 22) {
						String reducedstring = after.substring(0, i)
								+ (after.length() > i + 1 ? after.substring(i + 1) : "");
						text.setText(reducedstring);
						parent.dirtyhackcaretminus();
						break;
					}
				}
				if (changed == true) {

				}

			}

		});
		this.bold = false;
		this.italic = false;
		this.title = false;
		this.specialcolor = null;
		if (this.section.isBold())
			this.bold = true;
		if (this.section.isItalic())
			this.italic = true;
		if (this.section.getSpecialcolor() != null) {
			java.awt.Color awtcolor = this.section.getSpecialcolor();
			this.specialcolor = Color.rgb(awtcolor.getRed(), awtcolor.getGreen(), awtcolor.getBlue());
		}
		if (this.section.isSectiontitle())
			this.title = true;
		formatText();

	}

	/**
	 * @param newtext change the text being displayed
	 */
	public void refreshText(String newtext) {
		this.text = new Text(newtext);
		section.setText(newtext);
		formatText();
	}

	/**
	 * this will create a formattedtext with empty text and formatting consistent
	 * with the selected text
	 * 
	 * @param selectedtext
	 */
	public FormattedText(FormattedText selectedtext, Paragraph parent) {
		this(new RichTextSection(selectedtext.section), parent);
	}

	/**
	 * @return the javafx node
	 */
	public Node getNode() {
		return text;
	}

	/**
	 * @return the text payload of this formatted text
	 */
	public String getTextPayload() {
		return text.getText();
	}

	/**
	 * changes the text of this formatted text
	 * 
	 * @param newtext new text
	 */
	public void setString(String newtext) {
		text.setText(newtext);
	}

	/**
	 * @return true if text is displayed
	 */
	public boolean isDisplayed() {
		if (text.getText() == null)
			return false;
		if (text.getText().length() == 0)
			return false;
		return true;
	}
}
