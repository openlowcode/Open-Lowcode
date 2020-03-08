/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.richtext;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.tools.pdf.PDFPage;

/**
 * This is an extension of PDFPage, adding a method to write rich text in a box.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PDFPageWithRichText
		extends
		PDFPage {

	/**
	 * creates a PDF page allowing rich text in default A4 format.
	 * 
	 * @param portrait if true, page is portrait, if false, page is landscape 
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @throws IOException
	 */
	public PDFPageWithRichText(boolean portrait, float leftrightmargin, float topbottommargin) throws IOException {
		super(portrait, leftrightmargin, topbottommargin);
	}

	/**
	 * creates a PDF page with Rich Text in A4
	 * @param portrait if true, page is portrait, if false, page is landscape 
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @param topatzeroif true, top coordinates are zero (natural), if false, top is at bottom (PDF)
	 * @throws IOException if any issue related to file system happens creating the PDF page
	 */
	public PDFPageWithRichText(boolean portrait, float leftrightmargin, float topbottommargin, boolean topatzero)
			throws IOException {
		super(portrait, leftrightmargin, topbottommargin, topatzero);
	}

	/**
	 * creates a PDF Page with Rich text of custom dimensions
	 * @param customwidth custom width in mm
	 * @param customheight custom height in mm
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @param topatzero if true, top coordinates are zero (natural), if false, top is at bottom (PDF)
	 * @throws IOException if any issue related to file system happens creating the PDF page
	 */
	public PDFPageWithRichText(
			float customwidth,
			float customheight,
			float leftrightmargin,
			float topbottommargin,
			boolean topatzero) throws IOException {
		super(customwidth, customheight, leftrightmargin, topbottommargin, topatzero);
	}

	/**
	 * draw rich text in a box
	 * 
	 * @param left     the left limit in mm (note: margins are inside)
	 * @param top      the top of the box in mm (note: margins are inside)
	 * @param right    the right limit in mm (note: margins are inside)
	 * @param richtext the Open Lowcode richtext to print
	 * @return a list of BoxTextContent with number of lines and paragraphs written
	 * @throws IOException if any issue related to file system happens creating the PDF page
	 */
	public ArrayList<BoxTextContent> drawRichTextInBox(float left, float top, float right, String richtext)
			throws IOException {
		ArrayList<RichTextSection> structuredtext = RichTextParser.simplifyforblack(RichTextParser.parseText(richtext));
		ArrayList<BoxTextContent> textcontent = new ArrayList<BoxTextContent>();
		float currenttop = top;
		for (int i = 0; i < structuredtext.size(); i++) {
			RichTextSection thistextsection = structuredtext.get(i);
			int texttype = PDFPage.TEXTTYPE_PLAIN;
			if (thistextsection.isBullet())
				texttype = PDFPage.TEXTTYPE_BULLET_L1;
			if (thistextsection.isSectiontitle())
				texttype = PDFPage.TEXTTYPE_SECTION_HEADER;
			BoxTextContent feedback = this.drawTextInBox(left, currenttop, right, thistextsection.getText(), texttype);
			if (this.isTopAtZero())
				currenttop = currenttop + feedback.getHeight();
			if (!this.isTopAtZero())
				currenttop = currenttop - feedback.getHeight();
			textcontent.add(feedback);
		}
		return textcontent;
	}

	/**
	 * draw rich text in a box
	 * 
	 * @param left     the left limit in mm (note: margins are inside)
	 * @param top      the top of the box in mm (note: margins are inside)
	 * @param right    the right limit in mm (note: margins are inside)
	 * @param richtext the Open Lowcode richtext to print
	 * @return the height of the section in mm
	 * @throws IOException if any issue related to file system happens creating the PDF page
	 */
	public float drawRichTextInBoxAndGetHeight(float left, float top, float right, String richtext) throws IOException {
		ArrayList<BoxTextContent> layoutdetails = drawRichTextInBox(left, top, right, richtext);
		float totalheight = 0;
		for (int i = 0; i < layoutdetails.size(); i++)
			totalheight += layoutdetails.get(i).getHeight();
		return totalheight;
	}
}
