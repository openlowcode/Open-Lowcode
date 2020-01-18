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

import java.awt.Color;

/**
 * a section of a rich text
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class RichTextSection {
	boolean bold;
	boolean italic;
	Color specialcolor;
	boolean bullet;
	boolean sectiontitle;
	private String text;

	/**
	 * @return get the text payload
	 */
	public String getText() {
		return text;
	}

	/**
	 * @param text set the text payload
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * creates a default text rich text section with given text
	 * 
	 * @param text text
	 */
	public RichTextSection(String text) {
		this();
		this.setText(text);
	}

	/**
	 * creates a default text rich text section
	 */
	public RichTextSection() {
		this.bold = false;
		this.italic = false;
		this.specialcolor = null;
		this.bullet = false;
		this.sectiontitle = false;
		this.text = "";
	}

	/**
	 * creates a rich text section as a template with the same type as the given
	 * template
	 * 
	 * @param template section used as template
	 */
	public RichTextSection(RichTextSection template) {
		this.bold = template.bold;
		this.italic = template.italic;
		this.specialcolor = template.specialcolor;
		this.bullet = template.bullet;
		this.sectiontitle = template.sectiontitle;
		this.text = "";
	}

	/**
	 * sets the section to bold
	 */
	public void setBold() {
		this.bold = true;
	}

	/**
	 * sets the section to italic
	 */
	public void setItalic() {
		this.italic = true;
	}

	/**
	 * @param specialcolor set the text to special color
	 */
	public void setSpecialcolor(Color specialcolor) {
		this.specialcolor = specialcolor;
	}

	/**
	 * sets the text as bullet
	 */
	public void setBullet() {
		this.bullet = true;
	}

	/**
	 * sets the text as title
	 */
	public void setSectiontitle() {
		this.sectiontitle = true;
	}

	/**
	 * @return true if bold
	 */
	public boolean isBold() {
		return bold;
	}

	/**
	 * @return true if italic
	 */
	public boolean isItalic() {
		return italic;
	}

	/**
	 * @return the color if specified
	 */
	public Color getSpecialcolor() {
		return specialcolor;
	}

	/**
	 * @return true if bullet
	 */
	public boolean isBullet() {
		return bullet;
	}

	/**
	 * @return true if section title
	 */
	public boolean isSectiontitle() {
		return sectiontitle;
	}

	/**
	 * @return true if normal text
	 */
	public boolean isNormalText() {
		if (bullet)
			return false;
		if (sectiontitle)
			return false;
		return true;
	}
}