/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.pdf;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * A framework to generate pdfpages. This uses Apache PDFBOX. All dimensions are
 * in mm.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class PDFPage extends PDFPart {

	/**
	 * This code will be executed when printing the page. It is defined when widget
	 * is entered on the page
	 * 
	 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
	 *
	 */
	@FunctionalInterface
	private interface PageExecutable {

		/**
		 * prints on the relevant page the component
		 * 
		 * @throws IOException
		 */
		public void printComponent() throws IOException;
	}

	private ArrayList<PageExecutable> widgetstoprint;
	private boolean portrait;
	private float topbottommargin;
	private float leftrightmargin;
	private float height;
	private float width;
	public static final float A4_LONGSIDE_IN_MM = 297f;
	public static final float A4_SHORTSIDE_IN_MM = 210f;

	static final float MM_TO_POINT = 2.834645669f;

	static final float LINE_HEIGHT_NORMAL_TEXT = 10f; // in point
	static final float LINE_HEIGHT_TITLE_TEXT = 18f; // in point
	static final float LINE_HEIGHT_SECTION_HEADER = 12f; // in point
	static final float PARAGRAPH_MARGIN_VERTICAL = 2f; // in point
	static final float PARAGRAPH_MARGIN_HORIZONTAL = 8f; // in point
	static final float PICTURE_MARGIN = 3f; // in point

	static final float NORMAL_LINE_WIDTH = 0.3f; // in point
	static final float FAT_LINE_WIDTH = 1.1f; // in point
	static final float PARAGRAPH_HEADER_UNDERLINE_WIDTH = 0.6f;
	static final float LINE_SPACING_NORMAL_TEXT = 3f; // in point, valid also for label
	static final float LINE_SPACING_SECTION_HEADER = 4f; // in point
	static final float LINE_SPACING_TITLE_TEXT = 4f; // in point

	static final float PARAGRAPH_SPACING_NORMAL_TEXT = 5f; // in point
	static final float PARAGRAPH_SPACING_TITLE_TEXT = 7f; // in point
	static final float PARAGRAPH_SPACING_SECTION_HEADER = 6f; // in point

	static final float LABEL_SIZE_REDUCTION = 0.8f;

	public static final int TEXTTYPE_PLAIN = 0;
	public static final int TEXTTYPE_LABEL = 1;
	public static final int TEXTTYPE_SECTION_HEADER = 2;
	public static final int TEXTTYPE_TITLE = 3;
	public static final int TEXTTYPE_BULLET_L1 = 4;
	public static final int TEXTTYPE_BULLET_L2 = 5;
	public static final int TEXTTYPE_BULLET_L3 = 6;
	public static final int TEXTTYPE_PARAGRAPH_HEADER = 7;

	static final PDFont labelfont = PDType1Font.HELVETICA_OBLIQUE;
	static final PDFont datafont = PDType1Font.HELVETICA;
	static final PDFont titlefont = PDType1Font.HELVETICA;
	static final PDFont sectionheaderfont = PDType1Font.HELVETICA_BOLD;
	static final PDFont paragraphheaderfont = PDType1Font.HELVETICA_BOLD;

	private static Logger logger = Logger.getLogger(PDFPage.class.getName());

	private PDPage page;
	private PDPageContentStream contentStream;
	private PDDocument document;

	/**
	 * This method should only be called during the final layout. Please use the
	 * method PDFPage.drawCalculatedText
	 * 
	 * @return the index of pages, starting at 1
	 */
	public int getPageIndex() {
		return this.pageindex;
	}

	/**
	 * @return
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @return
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * An utility class allowing to store font and font size in a same object
	 * 
	 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
	 *
	 */
	public static class FontAndSize {
		private PDFont font;
		private float fontsize;

		public PDFont getFont() {
			return font;
		}

		public float getFontsize() {
			return fontsize;
		}

		public FontAndSize(PDFont font, float fontsize) {

			this.font = font;
			this.fontsize = fontsize;
		}

	}

	/**
	 * @param texttype
	 * @return a number between 0 and 1 indicating the ratio of left margin to be
	 *         left for printing special widget (e.g. bullet point)
	 */
	static float getLeftMarginRatio(int texttype) {
		if (texttype == TEXTTYPE_BULLET_L1)
			return 0.075f;
		if (texttype == TEXTTYPE_BULLET_L2)
			return 0.15f;
		if (texttype == TEXTTYPE_BULLET_L3)
			return 0.225f;

		return 0f;
	}

	static boolean isUnderLined(int texttype) {
		if (texttype == TEXTTYPE_PARAGRAPH_HEADER)
			return true;
		return false;
	}

	/**
	 * @param texttype
	 * @return the minimum left margin for the text type in mm
	 */
	static float getLeftMarginMinimum(int texttype) {
		if (texttype == TEXTTYPE_BULLET_L1)
			return 5f;
		if (texttype == TEXTTYPE_BULLET_L2)
			return 10f;
		if (texttype == TEXTTYPE_BULLET_L3)
			return 15f;
		return 0f;
	}

	static PDFont getFont(int texttype) {
		if (texttype == TEXTTYPE_PLAIN)
			return datafont;
		if (texttype == TEXTTYPE_LABEL)
			return labelfont;
		if (texttype == TEXTTYPE_SECTION_HEADER)
			return sectionheaderfont;
		if (texttype == TEXTTYPE_PARAGRAPH_HEADER)
			return paragraphheaderfont;

		if (texttype == TEXTTYPE_TITLE)
			return titlefont;
		if (texttype == TEXTTYPE_BULLET_L1)
			return datafont;
		if (texttype == TEXTTYPE_BULLET_L2)
			return datafont;
		if (texttype == TEXTTYPE_BULLET_L3)
			return datafont;

		throw new RuntimeException("Text type not supported " + texttype);
	}

	static float getLineHeight(int texttype) {
		if (texttype == TEXTTYPE_PLAIN)
			return LINE_HEIGHT_NORMAL_TEXT;
		if (texttype == TEXTTYPE_LABEL)
			return LINE_HEIGHT_NORMAL_TEXT * LABEL_SIZE_REDUCTION;
		if (texttype == TEXTTYPE_SECTION_HEADER)
			return LINE_HEIGHT_SECTION_HEADER;
		if (texttype == TEXTTYPE_TITLE)
			return LINE_HEIGHT_TITLE_TEXT;
		if (texttype == TEXTTYPE_BULLET_L1)
			return LINE_HEIGHT_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L2)
			return LINE_HEIGHT_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L3)
			return LINE_HEIGHT_NORMAL_TEXT;
		if (texttype == TEXTTYPE_PARAGRAPH_HEADER)
			return LINE_HEIGHT_NORMAL_TEXT;
		throw new RuntimeException("Text type not supported " + texttype);
	}

	static float getLineSpacing(int texttype) {
		if (texttype == TEXTTYPE_PLAIN)
			return LINE_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_LABEL)
			return LINE_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_SECTION_HEADER)
			return LINE_SPACING_SECTION_HEADER;
		if (texttype == TEXTTYPE_TITLE)
			return LINE_SPACING_TITLE_TEXT;
		if (texttype == TEXTTYPE_BULLET_L1)
			return LINE_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L2)
			return LINE_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L3)
			return LINE_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_PARAGRAPH_HEADER)
			return LINE_SPACING_NORMAL_TEXT;
		throw new RuntimeException("Text type not supported " + texttype);
	}

	static float getParagraphSpacing(int texttype) {
		if (texttype == TEXTTYPE_PLAIN)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_LABEL)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_SECTION_HEADER)
			return PARAGRAPH_SPACING_SECTION_HEADER;
		if (texttype == TEXTTYPE_TITLE)
			return PARAGRAPH_SPACING_TITLE_TEXT;
		if (texttype == TEXTTYPE_BULLET_L1)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L2)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_BULLET_L3)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		if (texttype == TEXTTYPE_PARAGRAPH_HEADER)
			return PARAGRAPH_SPACING_NORMAL_TEXT;
		throw new RuntimeException("Text type not supported " + texttype);
	}

	/**
	 * @param texttype a texttype as declared in this page
	 * @return Font and font size in the adhoc inner class
	 */
	static FontAndSize getFontAndSize(int texttype) {
		PDFont font = getFont(texttype);
		float fontsize = getLineHeight(texttype);
		return new FontAndSize(font, fontsize);
	}

	/**
	 * @param lines    lines of text, the function returns the maximum of text sizes
	 * @param texttype texttype as defined in PDFBox
	 * @return
	 * @throws IOException
	 * 
	 */
	public static float getTextSize(String[] lines, int texttype) throws IOException {
		FontAndSize fontandsize = getFontAndSize(texttype);
		float maxwidth = 0f;
		for (int i = 0; i < lines.length; i++) {
			float thiswidth = fontandsize.getFont().getStringWidth(lines[i]) / (MM_TO_POINT * 1000)
					* fontandsize.getFontsize();
			if (thiswidth > maxwidth)
				maxwidth = thiswidth;
		}
		return maxwidth;
	}

	/**
	 * @param smallfonttype the smallest font type
	 * @param bigfonttype   the biggest font type
	 * @return the difference between both fonts in mm
	 */
	static float getHeightDifference(int smallfonttype, int bigfonttype) {
		float smallfontheight = getFontAndSize(smallfonttype).getFontsize() / (MM_TO_POINT);
		float bigfontheight = getFontAndSize(bigfonttype).getFontsize() / (MM_TO_POINT);
		return bigfontheight - smallfontheight;
	}

	private boolean topatzero;
	private int pageindex = -1;

	/**
	 * @return true if top of page is zero, and bottom of page is positive height of
	 *         page in milimeter
	 */
	public boolean isTopAtZero() {
		return this.topatzero;
	}

	/**
	 * Generates an A4 format page with dimensions indicated in mm
	 * @param portrait
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @param topatzero
	 * @throws IOException
	 */
	public PDFPage(boolean portrait, float leftrightmargin, float topbottommargin, boolean topatzero)
			throws IOException {

		this.portrait = portrait;
		this.topbottommargin = topbottommargin;
		this.leftrightmargin = leftrightmargin;
		if (this.portrait) {
			height = A4_LONGSIDE_IN_MM;
			width = A4_SHORTSIDE_IN_MM;

		} else {
			width = A4_LONGSIDE_IN_MM;
			height = A4_SHORTSIDE_IN_MM;

		}

		this.topatzero = topatzero;
		widgetstoprint = new ArrayList<PageExecutable>();
	}

	public void addHeader(PDFPageBandHeaders header) throws IOException {
		if (!this.topatzero)
			throw new RuntimeException("AddHeader function only available for page with topatzero = true");
		header.printHeaders(this, leftrightmargin, topbottommargin, this.width - this.leftrightmargin,
				this.height - topbottommargin);
	}

	/**
	 * @param customwidth custom width in mm
	 * @param customheight custom height in mm
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @param topatzero
	 * @throws IOException if any issue related to file system happens creating the PDF page
	 */
	public PDFPage(float customwidth, float customheight, float leftrightmargin, float topbottommargin,
			boolean topatzero) throws IOException {
		width = customwidth;
		height = customheight;
		this.topbottommargin = topbottommargin;
		this.leftrightmargin = leftrightmargin;
		this.topatzero = topatzero;

		widgetstoprint = new ArrayList<PageExecutable>();
	}

	/**
	 * generates an A4 page with margin size indicated in mm, with topatzero = true
	 * @param portrait        true if portrait, false is landscape
	 * @param leftrightmargin margin at left and right in mm
	 * @param topbottommargin margin at top and bottom in mm
	 * @throws IOException
	 */
	public PDFPage(boolean portrait, float leftrightmargin, float topbottommargin) throws IOException {
		this(portrait, topbottommargin, leftrightmargin, true);
	}

	/**
	 * this needs to be called at the end the work on the page. Else, at execution,
	 * an exception is thrown.
	 * 
	 * @throws IOException
	 */
	public void closepage() throws IOException {

	}

	@Override
	protected void print(PDDocument document) throws IOException {
		// create the page
		this.document = document;
		page = new PDPage(new PDRectangle(width * MM_TO_POINT, height * MM_TO_POINT));
		document.addPage(page);
		contentStream = new PDPageContentStream(document, page);
		// print the widgets
		for (int i = 0; i < widgetstoprint.size(); i++) {
			PageExecutable thiswidget = widgetstoprint.get(i);
			thiswidget.printComponent();
		}
		// close the page
		contentStream.close();

	}

	/**
	 * @param fatline fatline true if line is fat, false if line is standard
	 *                (thinner)
	 * @param left    left coordinates in mm (note: in PDF, left starts at zero)
	 * @param top     top coordinates in mm (note: in PDF, bottom starts at zero
	 *                except if topatzero is set)
	 * @param width   width in mm, a positive number
	 * @param height  height in mm, a positive number
	 * @throws IOException
	 */
	public void drawBoxWithWidthAndHeight(boolean fatline, float left, float top, float width, float height)
			throws IOException {

		float right = left + width;
		float bottom = top - height;
		if (this.topatzero) {
			bottom = top + height;
		}
		drawBox(fatline, left, top, right, bottom);
	}

	/**
	 * @param bordercolor
	 * @param fillcolor
	 * @param centerx
	 * @param centery
	 * @param radius
	 * @throws IOException
	 */
	public void drawCircle(Color bordercolor, Color fillcolor, float centerx, float centery, float radius)
			throws IOException {
		this.widgetstoprint.add(() -> {
			contentStream.setLineWidth(NORMAL_LINE_WIDTH);
			contentStream.setStrokingColor(bordercolor);
			contentStream.setNonStrokingColor(fillcolor);
			final float k = 0.552284749831f;
			float pointradius = radius * MM_TO_POINT;
			float pointcentery = (topatzero ? height - centery : centery) * MM_TO_POINT;
			float pointcenterx = centerx * MM_TO_POINT;
			logger.warning("writing point at " + centerx + "-" + centery + "-" + radius);
			logger.warning("writing point at " + pointcenterx + "-" + pointcentery + "-" + pointradius + " (point)");

			contentStream.moveTo(pointcenterx - pointradius, pointcentery);
			contentStream.curveTo(pointcenterx - pointradius, pointcentery + k * pointradius,
					pointcenterx - k * pointradius, pointcentery + pointradius, pointcenterx,
					pointcentery + pointradius);
			contentStream.curveTo(pointcenterx + k * pointradius, pointcentery + pointradius,
					pointcenterx + pointradius, pointcentery + k * pointradius, pointcenterx + pointradius,
					pointcentery);
			contentStream.curveTo(pointcenterx + pointradius, pointcentery - k * pointradius,
					pointcenterx + k * pointradius, pointcentery - pointradius, pointcenterx,
					pointcentery - pointradius);
			contentStream.curveTo(pointcenterx - k * pointradius, pointcentery - pointradius,
					pointcenterx - pointradius, pointcentery - k * pointradius, pointcenterx - pointradius,
					pointcentery);
			contentStream.closeAndFillAndStroke();
		});

	}

	/**
	 * @param fatline true if line is fat, false if line is standard (thinner)
	 * @param left    left coordinates in mm (note: in PDF, left starts at zero)
	 * @param top     top coordinates in mm (note: in PDF, bottom starts at zero
	 *                except if topatzero is set)
	 * @param right   right coordinates in mm (note: in PDF, left starts at zero)
	 * @param bottom  bottom coordinates in mm (note: in PDF, bottom starts at zero
	 *                except if topatzero is set)
	 * @param color   color of the box.
	 * @throws IOException
	 */
	public void drawBox(boolean fatline, float left, float top, float right, float bottom, Color color)
			throws IOException {
		this.widgetstoprint.add(() -> {
			contentStream.setStrokingColor(color);
			contentStream.setNonStrokingColor(color);

			if (!fatline)
				contentStream.setLineWidth(NORMAL_LINE_WIDTH);
			if (fatline)
				contentStream.setLineWidth(FAT_LINE_WIDTH);

			float newtop = top;
			float newbottom = bottom;
			if (this.topatzero) {
				newtop = height - top;
				newbottom = height - bottom;
			}

			contentStream.moveTo(left * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.lineTo(right * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.lineTo(right * MM_TO_POINT, newbottom * MM_TO_POINT);
			contentStream.lineTo(left * MM_TO_POINT, newbottom * MM_TO_POINT);
			contentStream.lineTo(left * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.closeAndStroke();
			contentStream.setStrokingColor(Color.BLACK);
			contentStream.setNonStrokingColor(Color.BLACK);
		});

	}

	/**
	 * Draws a rectangular box
	 * 
	 * @param fatline true if line is fat, false if line is standard (thinner)
	 * @param left    left coordinates in mm (note: in PDF, left starts at zero)
	 * @param top     top coordinates in mm (note: in PDF, bottom starts at zero
	 *                except if topatzero is set)
	 * @param right   right coordinates in mm (note: in PDF, left starts at zero)
	 * @param bottom  bottom coordinates in mm (note: in PDF, bottom starts at zero
	 *                except if topatzero is set)
	 * @throws IOException
	 */
	public void drawBox(boolean fatline, float left, float top, float right, float bottom) throws IOException {
		drawBox(fatline, left, top, right, bottom, Color.BLACK);
	}

	/**
	 * @param fatline
	 * @param startx
	 * @param starty
	 * @param endx
	 * @param endy
	 * @param color
	 * @throws IOException
	 */
	public void drawLine(boolean fatline, float startx, float starty, float endx, float endy, Color color)
			throws IOException {
		this.widgetstoprint.add(() -> {
			contentStream.setStrokingColor(color);
			contentStream.setNonStrokingColor(color);
			if (!fatline)
				contentStream.setLineWidth(NORMAL_LINE_WIDTH);
			if (fatline)
				contentStream.setLineWidth(FAT_LINE_WIDTH);
			float newstarty = (topatzero ? height - starty : starty);
			float newendy = (topatzero ? height - endy : endy);
			contentStream.moveTo(startx * MM_TO_POINT, newstarty * MM_TO_POINT);
			contentStream.lineTo(endx * MM_TO_POINT, newendy * MM_TO_POINT);
			contentStream.closeAndStroke();

		});

	}

	/**
	 * @param label true if shown as label, false if shown as normal text
	 * @param line  the line of text
	 * @return the text width.
	 * @throws IOException
	 */
	public static float getTextWidth(boolean label, String line) throws IOException {
		return getTextWidth(label, new String[] { line });
	}

	/**
	 * @param label            true if shown as label, false if shown as normal text
	 * @param line             the line of text
	 * @param paragraphspacing if true, 2 times the paragraph left margin will be
	 *                         added.
	 * @return the text width.
	 * @throws IOException
	 */
	public static float getTextWidth(boolean label, String line, boolean paragraphspacing) throws IOException {
		float calculatedspacing = (paragraphspacing ? 2 * PARAGRAPH_MARGIN_HORIZONTAL / MM_TO_POINT : 0f);
		if (line == null)
			return calculatedspacing;
		return getTextWidth(label, line) + calculatedspacing;
	}

	/**
	 * returns the maximum length of the lines provided
	 * 
	 * @param label true if shown as label, false if shown as normal text
	 * @param lines the lines of text
	 * @return the text width.
	 * @throws IOException
	 */
	private static float getTextWidth(boolean label, String[] lines) throws IOException {
		PDFont font = datafont;
		float fontSize = LINE_HEIGHT_NORMAL_TEXT;
		if (label) {
			font = labelfont;
			fontSize = LINE_HEIGHT_NORMAL_TEXT * LABEL_SIZE_REDUCTION;
		}
		float maxwidth = 0f;
		for (int i = 0; i < lines.length; i++) {
			float thiswidth = font.getStringWidth(lines[i]) / (MM_TO_POINT * 1000) * fontSize;
			if (thiswidth > maxwidth)
				maxwidth = thiswidth;
		}
		return maxwidth;
	}

	/**
	 * returns the maximum length of the lines provided with paragraphheaderfont
	 * 
	 * @param lines the lines of text
	 * @return the lines of text
	 * @throws IOException
	 */
	public float getParagraphHeaderTextWidth(String[] lines) throws IOException {
		PDFont font = PDFPage.sectionheaderfont;
		float fontSize = PDFPage.LINE_HEIGHT_SECTION_HEADER;
		float maxwidth = 0f;
		for (int i = 0; i < lines.length; i++) {
			float thiswidth = font.getStringWidth(lines[i]) / (MM_TO_POINT * 1000) * fontSize;
			if (thiswidth > maxwidth)
				maxwidth = thiswidth;
		}
		return maxwidth;
	}

	/**
	 * @param fatline
	 * @param left
	 * @param top
	 * @param right
	 * @param numberoflines
	 * @return the mm position of bottom of the box. You can use it to draw a box
	 *         below that will touch this box
	 * @throws IOException
	 */
	public float drawBoxWithLineNumber(boolean fatline, float left, float top, float right, int numberoflines)
			throws IOException {
		return drawBoxWithLineNumber(fatline, left, top, right, numberoflines, 1);
	}

	/**
	 * @param fatline
	 * @param left
	 * @param top
	 * @param right
	 * @param numberoflines
	 * @param numberofparagraphs
	 * @return
	 * @throws IOException
	 * 
	 */
	public float drawBoxWithLineNumber(boolean fatline, float left, float top, float right, int numberoflines,
			int numberofparagraphs) throws IOException {
		float bottom = top - (this.topatzero ? -1 : 1)
				* new BoxTextContent(numberoflines, numberofparagraphs, PDFPage.TEXTTYPE_PLAIN).getHeight();
		drawBox(fatline, left, top, right, bottom);
		return bottom;
	}

	public float fillBoxWithLineNumber(Color color, float left, float top, float right, int numberoflines,
			int numberofparagraphs) throws IOException {
		float bottom = top - (this.topatzero ? -1 : 1)
				* new BoxTextContent(numberoflines, numberofparagraphs, PDFPage.TEXTTYPE_PLAIN).getHeight();
		fillBox(color, left, top, right, bottom);
		return bottom;
	}

	public void fillBox(Color color, float left, float top, float right, float bottom) throws IOException {
		this.widgetstoprint.add(() -> {
			contentStream.setNonStrokingColor(color);
			contentStream.setStrokingColor(color);

			float newtop = top;
			float newbottom = bottom;
			if (this.topatzero) {
				newtop = height - top;
				newbottom = height - bottom;
			}

			contentStream.moveTo(left * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.lineTo(right * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.lineTo(right * MM_TO_POINT, newbottom * MM_TO_POINT);
			contentStream.lineTo(left * MM_TO_POINT, newbottom * MM_TO_POINT);
			contentStream.lineTo(left * MM_TO_POINT, newtop * MM_TO_POINT);
			contentStream.closeAndFillAndStroke();

		});

	}

	/**
	 * @param left        left coordinates for the text (left of the margin)
	 * @param top         top coordinates for the text (top of the margin)
	 * @param color       Color expressed as java.awt.Color
	 * @param bold        true if bold
	 * @param italic      true if italic
	 * @param serif       if true, police is times, if false, police is helvetica
	 * @param fontinpoint the font size in point
	 * @param text        Text
	 * @throws IOException
	 */
	public void drawFreeFontTextAt(float left, float top, Color color, boolean bold, boolean italic, boolean serif,
			float fontinpoint, String text) throws IOException {
		this.widgetstoprint.add(() -> {
			float newtop = top;
			if (this.topatzero) {
				newtop = height - top;
			}

			contentStream.beginText();
			contentStream.newLineAtOffset(left * MM_TO_POINT, newtop * MM_TO_POINT-fontinpoint);

			PDFont font = PDType1Font.HELVETICA;
			if (!serif) {
				if (bold)
					if (italic)
						font = PDType1Font.HELVETICA_BOLD_OBLIQUE;
				if (bold)
					if (!italic)
						font = PDType1Font.HELVETICA_BOLD;
				if (!bold)
					if (italic)
						font = PDType1Font.HELVETICA_OBLIQUE;
			}
			if (serif) {
				if (!bold)
					if (!italic)
						font = PDType1Font.TIMES_ROMAN;
				if (bold)
					if (italic)
						font = PDType1Font.TIMES_BOLD_ITALIC;
				if (bold)
					if (!italic)
						font = PDType1Font.TIMES_BOLD;
				if (!bold)
					if (italic)
						font = PDType1Font.TIMES_ITALIC;
			}
			contentStream.setFont(font, fontinpoint);
			contentStream.setNonStrokingColor(color);
			securedShowText(contentStream, text);
			contentStream.endText();
			contentStream.setNonStrokingColor(Color.BLACK);
		});

	}

	public void drawParagraphHeaderAt(float left, float top, int linenumber, int paragraphnumber, String text)
			throws IOException {
		this.widgetstoprint.add(() -> {
			float newtop = top;
			if (this.topatzero) {
				newtop = height - top;

			}
			contentStream.setStrokingColor(Color.BLACK);

			contentStream.beginText();
			contentStream.newLineAtOffset(left * MM_TO_POINT + PARAGRAPH_MARGIN_HORIZONTAL,
					newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL - (linenumber) * (LINE_SPACING_SECTION_HEADER)
							- LINE_HEIGHT_SECTION_HEADER * (linenumber + 1)
							- (paragraphnumber) * PARAGRAPH_SPACING_SECTION_HEADER);

			contentStream.setFont(datafont, LINE_HEIGHT_SECTION_HEADER);
			securedShowText(contentStream, text);
			contentStream.endText();
		});

	}

	/**
	 * @param label
	 * @param xcoordinates    leftcoordinates if alignleft = true, else right
	 *                        coordinates
	 * @param top             top of the box
	 * @param linenumber      line number of text (starting at 0)
	 * @param paragraphnumber paragraph number of text (starting at 0)
	 * @param alignleft       if true, align on left, if false, align on right
	 * @param calculatedtext  text to write
	 * @throws IOException
	 */
	public void drawCalculatedText(boolean label, float xcoordinates, float top, int linenumber, int paragraphnumber,
			boolean alignleft, TextAfterLayout calculatedtext) throws IOException {
		this.widgetstoprint.add(() -> {
			float newtop = top;

			if (this.topatzero) {
				newtop = height - top;

			}
			String text = calculatedtext.generateText();
			boolean horizontalparagraphmargin = true;
			float textoffset = (horizontalparagraphmargin ? PARAGRAPH_MARGIN_HORIZONTAL : 0);
			if (!alignleft) {
				textoffset = -PDFPage.getTextSize(new String[] { text },
						(label ? PDFPage.TEXTTYPE_LABEL : PDFPage.TEXTTYPE_PLAIN)) * MM_TO_POINT
						- (horizontalparagraphmargin ? PARAGRAPH_MARGIN_HORIZONTAL : 0);
				logger.finer("calculating offsetinmm = " + textoffset + " for text " + text);
			}
			float left = xcoordinates;
			// TODO align right
			contentStream.setNonStrokingColor(Color.BLACK);
			contentStream.setStrokingColor(Color.BLACK);

			contentStream.beginText();
			contentStream.newLineAtOffset(left * MM_TO_POINT + textoffset,
					newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL - (linenumber) * (LINE_SPACING_NORMAL_TEXT)
							- LINE_HEIGHT_NORMAL_TEXT * (linenumber + 1)
							- (paragraphnumber) * PARAGRAPH_SPACING_NORMAL_TEXT);

			if (label)
				contentStream.setFont(labelfont, LINE_HEIGHT_NORMAL_TEXT * LABEL_SIZE_REDUCTION);
			if (!label)
				contentStream.setFont(datafont, LINE_HEIGHT_NORMAL_TEXT);
			securedShowText(contentStream, text);
			contentStream.endText();
		});
	}

	/**
	 * @param xcoordinates    leftcoordinates if alignleft = true, else right
	 *                        coordinates
	 * @param top             top of the box
	 * @param linenumber      line number of text (starting at 0)
	 * @param paragraphnumber paragraph number of text (starting at 0)
	 * @param alignleft       if true, align on left, if false, align on right
	 * @param calculatedtext  text to write
	 * @throws IOException
	 */
	public void drawCalculatedText(float xcoordinates, float top, int linenumber, int paragraphnumber,
			boolean alignleft, TextAfterLayout calculatedtext) throws IOException {
		drawCalculatedText(false, xcoordinates, top, linenumber, paragraphnumber, alignleft, calculatedtext);
	}

	public static int getNumberOfLinesAvailableForParagraph(float spaceinmm) {
		float spaceavailableinpoint = spaceinmm * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL;
		float spaceforoneline = LINE_SPACING_NORMAL_TEXT + LINE_HEIGHT_NORMAL_TEXT;
		return (int) (spaceavailableinpoint / spaceforoneline);

	}

	/**
	 * Draws a single text from the point of origin (adding necessary margins).
	 * Typically, the function is used entering directly in left and top the
	 * left-top of a box. The margins will adjust correctly
	 * 
	 * @param label           true of label, right if normal text
	 * @param left            left coordinates for the text (left of the margin)
	 * @param top             top coordinates for the text (top of the margin)
	 * @param linenumber      line number of text (starting at 0)
	 * @param paragraphnumber paragraph number of text (starting at 0)
	 * @param text            the text to write
	 * @throws IOException
	 */
	public void drawSimpleTextAt(boolean label, float left, float top, int linenumber, int paragraphnumber, String text)
			throws IOException {

		if (text != null)
			drawSimpleTextAt(label, left, top, linenumber, paragraphnumber, text, true);
		if (text == null)
			drawSimpleTextAt(label, left, top, linenumber, paragraphnumber, "", true);
	}

	/**
	 * Draws a single text from the point of origin (adding necessary margins).
	 * Typically, the function is used entering directly in left and top the
	 * left-top of a box. The margins will adjust correctly
	 * 
	 * @param label                     true of label, right if normal text
	 * @param left                      left coordinates for the text (left of the
	 *                                  margin)
	 * @param top                       top coordinates for the text (top of the
	 *                                  margin)
	 * @param linenumber                line number of text (starting at 0)
	 * @param paragraphnumber           paragraph number of text (starting at 0)
	 * @param text                      the text to write
	 * @param horizontalparagraphmargin if true, write with normal margin, if false,
	 *                                  write text exactly at point specified. The
	 *                                  false option is especially suitable for
	 *                                  diagram axis
	 * @throws IOException
	 */
	public void drawSimpleTextAt(boolean label, float left, float top, int linenumber, int paragraphnumber, String text,
			boolean horizontalparagraphmargin) throws IOException {
		this.widgetstoprint.add(() -> {
			float newtop = top;

			if (this.topatzero) {
				newtop = height - top;

			}
			contentStream.setNonStrokingColor(Color.BLACK);
			contentStream.setStrokingColor(Color.BLACK);

			contentStream.beginText();
			contentStream.newLineAtOffset(
					left * MM_TO_POINT + (horizontalparagraphmargin ? PARAGRAPH_MARGIN_HORIZONTAL : 0),
					newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL - (linenumber) * (LINE_SPACING_NORMAL_TEXT)
							- LINE_HEIGHT_NORMAL_TEXT * (linenumber + 1)
							- (paragraphnumber) * PARAGRAPH_SPACING_NORMAL_TEXT);
			if (label)
				contentStream.setFont(labelfont, LINE_HEIGHT_NORMAL_TEXT * LABEL_SIZE_REDUCTION);
			if (!label)
				contentStream.setFont(datafont, LINE_HEIGHT_NORMAL_TEXT);
			securedShowText(contentStream, text);
			contentStream.endText();
		});

	}

	/**
	 * @param texttype
	 * @param left
	 * @param top
	 * @param linenumber
	 * @param paragraphnumber
	 * @throws IOException
	 * 
	 */
	public void drawTextWidget(int texttype, float left, float top, float newleft, int linenumber, int paragraphnumber,
			boolean newparagraph) throws IOException {
		if (newparagraph) {
			logger.finest("--- new  paragraph");
			if (texttype == PDFPage.TEXTTYPE_BULLET_L1) {
				// drawLeftPaddedTextAt(PDFPage.TEXTTYPE_PLAIN,left,top,linenumber,paragraphnumber,"\u2022");
				drawLeftPaddedTextAt(PDFPage.TEXTTYPE_PLAIN, left + (newleft - left) * 2 / 3, top, linenumber,
						paragraphnumber, "\u2022");

			}
			if (texttype == PDFPage.TEXTTYPE_BULLET_L2) {
				drawLeftPaddedTextAt(PDFPage.TEXTTYPE_PLAIN, left + (newleft - left) * 5 / 6, top, linenumber,
						paragraphnumber, "\u2022");

			}
			if (texttype == PDFPage.TEXTTYPE_BULLET_L3) {
				drawLeftPaddedTextAt(PDFPage.TEXTTYPE_PLAIN, left + (newleft - left) * 8 / 9, top, linenumber,
						paragraphnumber, "\u2022");
			}
		} else {
			logger.finest("--- not new  paragraph");
		}

	}

	/**
	 * @param texttype
	 * @param left
	 * @param top
	 * @param linenumber
	 * @param paragraphnumber
	 * @param text
	 * @throws IOException
	 */
	public void drawLeftPaddedTextAt(int texttype, float left, float top, int linenumber, int paragraphnumber,
			String text) throws IOException {
		this.widgetstoprint.add(() -> {
			float newtop = top;
			boolean underline = isUnderLined(texttype);
			if (this.topatzero) {
				newtop = height - top;

			}

			contentStream.setStrokingColor(Color.BLACK);
			contentStream.beginText();
			float yinpoint = newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL
					- (linenumber) * (PDFPage.getLineSpacing(texttype))
					- PDFPage.getLineHeight(texttype) * (linenumber + 1)
					- (paragraphnumber) * PDFPage.getParagraphSpacing(texttype);
			contentStream.newLineAtOffset(left * MM_TO_POINT + PARAGRAPH_MARGIN_HORIZONTAL,
					newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL - (linenumber) * (PDFPage.getLineSpacing(texttype))
							- PDFPage.getLineHeight(texttype) * (linenumber + 1)
							- (paragraphnumber) * PDFPage.getParagraphSpacing(texttype));
			FontAndSize fontandsize = PDFPage.getFontAndSize(texttype);
			contentStream.setFont(fontandsize.getFont(), fontandsize.getFontsize());
			securedShowText(contentStream, text);

			contentStream.endText();
			if (underline) {
				contentStream.setStrokingColor(Color.BLACK);
				contentStream.setLineWidth(PARAGRAPH_HEADER_UNDERLINE_WIDTH);

				contentStream.moveTo(left * MM_TO_POINT + PARAGRAPH_MARGIN_HORIZONTAL,
						yinpoint - fontandsize.getFontsize() * 0.15f);
				contentStream.lineTo(
						left * MM_TO_POINT + PARAGRAPH_MARGIN_HORIZONTAL
								+ getTextSize(new String[] { text }, texttype) * MM_TO_POINT,
						yinpoint - fontandsize.getFontsize() * 0.15f);
				contentStream.closeAndStroke();
			}
		});

	}

	/**
	 * @param title           if true, write big title text, false writes normal
	 *                        text
	 * @param top             top coordinated (top of the margin)
	 * @param paragraphnumber starts with 0, and allows to write several lines
	 *                        without calculating coordinates
	 * @param text            the text to write
	 * @throws IOException
	 */
	public void drawCenteredTextAt(boolean title, float top, int paragraphnumber, String text) throws IOException {
		this.widgetstoprint.add(() -> {
			PDFont font = datafont;
			float newtop = top;

			if (this.topatzero) {
				newtop = height - top;

			}
			float fontSize = LINE_HEIGHT_NORMAL_TEXT;
			float verticaloffset = (LINE_HEIGHT_NORMAL_TEXT) * (paragraphnumber+1)
					+ (PARAGRAPH_SPACING_NORMAL_TEXT * paragraphnumber);
			if (title) {
				font = titlefont;
				fontSize = LINE_HEIGHT_TITLE_TEXT;
				verticaloffset = (LINE_HEIGHT_TITLE_TEXT) * (paragraphnumber+1)
						+ (PARAGRAPH_SPACING_TITLE_TEXT * paragraphnumber);

			}
			float thiswidth = font.getStringWidth(text) / (1000) * fontSize;
			logger.fine("textwidthinpoint " + thiswidth + " page width in point" + (width * MM_TO_POINT));
			float left = (width * MM_TO_POINT - thiswidth) / 2;
			contentStream.setStrokingColor(Color.BLACK);

			contentStream.beginText();
			contentStream.newLineAtOffset(left, newtop * MM_TO_POINT - verticaloffset);
			contentStream.setFont(font, fontSize);
			securedShowText(contentStream, text);
			contentStream.endText();
		});

	}

	/**
	 * @return page top usable coordinates, leaving out page margin
	 */
	public float getPagetop() {
		float pagetop = height - topbottommargin;
		if (this.topatzero)
			pagetop = height - pagetop;

		return pagetop;
	}

	/**
	 * @return page bottom usable coordinates, leaving out page margin
	 */
	public float getPageBottom() {
		float pagebottom = topbottommargin;
		if (this.topatzero)
			pagebottom = height - pagebottom;
		return pagebottom;
	}

	/**
	 * @return page left coordinates, leaving out page margin
	 */
	public float getPageLeft() {
		return leftrightmargin;
	}

	/**
	 * @return page right coordinates, leaving out page margin
	 */
	public float getPageRight() {
		return width - leftrightmargin;
	}

	/**
	 * @param left				left of the printing zone in mm
	 * @param top				top of the prining zone in mm
	 * @param right				right of the printing zone in mm
	 * @param remainingheight	the maximum height to print
	 * @param text				 the full text to write
	 * @param page				page to print in
	 * @param texttype			text type as defined in PDFPage constant
	 * @param                 splitparagraph: true if this is the non-first part of
	 *                        a split paragraph. Typically, widget does not have to
	 *                        be printed
	 * @return
	 * @throws IOException
	 */
	public static BoxTextContent writeAsMuchTextAsPossible(float left, float top, float right, float remainingheight,
			String text, PDFPage page, int texttype, boolean splitparagraph) throws IOException {
		return calculateBoxAndMaybeWriteText(left, top, right, text, true, true, remainingheight, page, texttype,
				splitparagraph);
	}

	/**
	 * @param left            left of the printing zone in mm
	 * @param top             top of the prining zone in mm
	 * @param right           right of the printing zone in mm
	 * @param remainingheight the maximum height to print
	 * @param text            the full text to write
	 * @param page            page to print in
	 * @param write			  true to write the text, false just to calculate
	 * @param texttype        text type as defined in PDFPage constant
	 * @return the remaining text that could not be printed
	 * @throws IOException
	 */
	public static BoxTextContent calculateBoxAndMaybeWriteText(float left, float top, float right, String text, boolean write,
			PDFPage page, int texttype) throws IOException {
		return calculateBoxAndMaybeWriteText(left, top, right, text, write, false, 0, page, texttype, false);
	}

	/**
	 * @param left 			left of the printing zone in mm
	 * @param top			top of the prining zone in mm
	 * @param right			right of the printing zone in mm
	 * @param text			the full text to write
	 * @param write         true to write the text, false just to calculate
	 * @param partial       true to write only partial content within the maximum height limit specified
	 * @param maxheight		max height to print partial content
	 * @param page           page to print in
	 * @param texttype       one of the constants prefixed by 'TEXTTYPE_' in the
	 *                       class
	 * @param splitparagraph : true if this is the non-first part of a section split
	 *                       across pages. Typically bullet character is not printed
	 *                       if this argument is true.
	 * @return
	 * @throws IOException
	 */
	public static BoxTextContent calculateBoxAndMaybeWriteText(float left, float top, float right, String text, boolean write,
			boolean partial, float maxheight, PDFPage page, int texttype, boolean splitparagraph) throws IOException {
		boolean currentsplitparagraph = splitparagraph;
		if (write)
			if (page == null)
				throw new RuntimeException("For write mode, specifying a PDFPage is compulsory");
		String[] paragraphs;
		if (text != null) {
			paragraphs = text.split("\n");
		} else {
			paragraphs = new String[0];
		}
		// newleft is left taking into account left margin for special display (e.g.
		// bullet);
		float margin = (right - left) * getLeftMarginRatio(texttype);
		if (margin < getLeftMarginMinimum(texttype))
			margin = getLeftMarginMinimum(texttype);
		float newleft = left + margin;
		boolean paragraphcut = false;
		float boxwidthinmm = ((right - newleft) * MM_TO_POINT - 2 * PARAGRAPH_MARGIN_HORIZONTAL) / MM_TO_POINT;

		int totallinecounter = 0;
		boolean inlimitedbox = true;
		StringBuffer textnotprinted = new StringBuffer();
		for (int i = 0; i < paragraphs.length; i++) {
			// split paragraph limit is for first paragraph only.
			if (i > 0)
				currentsplitparagraph = false;
			String paragraphtext = paragraphs[i];
			ArrayList<String> paragraphlines = new ArrayList<String>();
			int lastspace = -1;
			while (paragraphtext.length() > 0) {
				int spaceIndex = paragraphtext.indexOf(' ', lastspace + 1);
				if (spaceIndex < 0)
					spaceIndex = paragraphtext.length();
				String subString = paragraphtext.substring(0, spaceIndex);
				float sizeinmm = PDFPage.getTextSize(new String[] { subString }, texttype);

				if (sizeinmm > boxwidthinmm) {
					if (lastspace < 0)
						lastspace = spaceIndex;
					subString = paragraphtext.substring(0, lastspace);
					paragraphlines.add(subString);
					paragraphtext = paragraphtext.substring(lastspace).trim();
					lastspace = -1;
				} else if (spaceIndex == paragraphtext.length()) {
					paragraphlines.add(paragraphtext);
					paragraphtext = "";
				} else {
					lastspace = spaceIndex;
				}
			}

			for (int j = 0; j < paragraphlines.size(); j++) {
				if (write) {
					if (!partial) {
						page.drawLeftPaddedTextAt(texttype, newleft, top, totallinecounter + j, i,
								paragraphlines.get(j));
						if (!currentsplitparagraph)
							page.drawTextWidget(texttype, left, top, newleft, totallinecounter + j, i,
									(j == 0 ? true : false));
					} else {
						if (!inlimitedbox) {
							// do nothing, archive the stuff

							textnotprinted.append(paragraphlines.get(j));
							if (j < paragraphlines.size() - 1)
								textnotprinted.append(' ');
							if (j == paragraphlines.size() - 1)
								if (i < paragraphs.length - 1)
									textnotprinted.append('\n');
						} else {
							// check if still in box
							int partialparagraphcount = i + 1;
							int partiallinecount = totallinecounter + j + 1;
							float heightsofar = new BoxTextContent(partiallinecount, partialparagraphcount, texttype)
									.getHeight();
							if (heightsofar <= maxheight) {
								// if so, print
								page.drawLeftPaddedTextAt(texttype, newleft, top, totallinecounter + j, i,
										paragraphlines.get(j));
								if (!currentsplitparagraph)
									page.drawTextWidget(texttype, left, top, newleft, totallinecounter + j, i,
											(j == 0 ? true : false));

							} else {
								// if not, do nothing and archive the stuff
								inlimitedbox = false;
								if (j == 0) {
									paragraphcut = false;
								} else {
									paragraphcut = true;
								}
								textnotprinted.append(paragraphlines.get(j));
								if (j < paragraphlines.size() - 1)
									textnotprinted.append(' ');
								if (j == paragraphlines.size() - 1)
									if (i < paragraphs.length - 1)
										textnotprinted.append('\n');
							}

						}
					}
				}
			}
			totallinecounter += paragraphlines.size();
		}
		return new BoxTextContent(totallinecounter, paragraphs.length + 1, texttype, textnotprinted.toString(),
				paragraphcut);
	}

	/**
	 * @param left  the left limit in mm (note: margins are inside)
	 * @param top   the top of the box in mm (note: margins are inside)
	 * @param right the right limit in mm (note: margins are inside)
	 * @param text  the text to print
	 * @return a BoxTextContent with number of lines and paragraphs written
	 * @throws IOException
	 */
	public BoxTextContent drawTextInBox(float left, float top, float right, String text) throws IOException {
		return calculateBoxAndMaybeWriteText(left, top, right, text, true, this, PDFPage.TEXTTYPE_PLAIN);
	}

	/**
	 * This method writes on the left a label (with label type of text), and the
	 * text content on the right, making sure not to get outside of the specified
	 * box.<br>
	 * Note: this will align on the bottom the label line with the first line
	 * 
	 * @param left    the left limit in mm (note: margins are inside)
	 * @param top     the top of the box in mm (note: margins are inside)
	 * @param right   the right limit in mm (note: margins are inside)
	 * @param label   label text
	 * @param text    payload text
	 * @param maxline : zero if the number of lines of text should not be limited ,
	 *                or a strictly positive figure to limit the number of lines in
	 *                total. If text is cut, it will get "..." in the end
	 * @return
	 * @throws IOException
	 */
	public BoxTextContent drawTextWithLabelInBox(float left, float top, float right, String label, String text,
			int maxline) throws IOException {
		float labelwidth = PDFPage.getTextSize(new String[] { label }, PDFPage.TEXTTYPE_LABEL);
		this.drawTextInBox(left,
				top + (this.topatzero ? 1 : -1)
						* PDFPage.getHeightDifference(PDFPage.TEXTTYPE_LABEL, PDFPage.TEXTTYPE_PLAIN),
				right, label, PDFPage.TEXTTYPE_LABEL);
		float newleft = left + labelwidth + PDFPage.PARAGRAPH_MARGIN_HORIZONTAL / PDFPage.MM_TO_POINT;
		if (newleft > right)
			throw new RuntimeException(
					"Label width is more than box space for label = '" + label + "' and text = '" + text + "'");

		return this.drawTextInBox(newleft, top, right, text, PDFPage.TEXTTYPE_PLAIN, maxline);
	}

	/**
	 * @param left     the left limit in mm (note: margins are inside)
	 * @param top      the top of the box in mm (note: margins are inside)
	 * @param right    the right limit in mm (note: margins are inside)
	 * @param text     the text to print
	 * @param texttype the text type as defined in PDFPAGE as TEXTTYPE...
	 * @return a BoxTextContent with number of lines and paragraphs written
	 * @throws IOException
	 * 
	 */
	public BoxTextContent drawTextInBox(float left, float top, float right, String text, int texttype)
			throws IOException {
		return calculateBoxAndMaybeWriteText(left, top, right, text, true, this, texttype);
	}

	/**
	 * @param left     the left limit in mm (note: margins are inside)
	 * @param top      the top of the box in mm (note: margins are inside)
	 * @param right    the right limit in mm (note: margins are inside)
	 * @param text     the text to print
	 * @param texttype the text type as defined in PDFPAGE as TEXTTYPE...
	 * @param maxline  the maximum number of lines to write, or zero if no limit.
	 * @return a BoxTextContent with number of lines and paragraphs written
	 * @throws IOException
	 * 
	 */
	public BoxTextContent drawTextInBox(float left, float top, float right, String text, int texttype, int maxline)
			throws IOException {
		boolean partial = false;
		float maxheight = 0;
		if (maxline > 0) {
			partial = true;
			maxheight = new BoxTextContent(maxline, 1, texttype, "", false).getHeight();
		}
		return calculateBoxAndMaybeWriteText(left, top, right, text, true, partial, maxheight, this, texttype, false);
	}

	/**
	 * @param left  left the left limit in mm (note: margins are inside)
	 * @param right top the top of the box in mm (note: margins are inside)
	 * @param text  the text to print
	 * @return a BoxTextContent with number of lines and paragraphs written
	 * @throws IOException
	 */
	public BoxTextContent calculateTextSize(float left, float right, String text) throws IOException {
		return calculateBoxAndMaybeWriteText(left, -1, right, text, false, null, TEXTTYPE_PLAIN);
	}

	/**
	 * This class stores the way a text was calculated or printed inside a box. The
	 * number of lines and number of paragraphs used can be accessed. The method
	 * getHeight can provide the height of the text in mm to allow positioning of
	 * elements below this text
	 * 
	 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
	 *
	 */
	public static class BoxTextContent {
		private int nblines;
		private int nbparagraph;
		private int texttype;
		private String textleftout;

		public String getTextleftout() {
			return textleftout;
		}

		public boolean isCutinsideparagraph() {
			return cutinsideparagraph;
		}

		private boolean cutinsideparagraph;

		/**
		 * @param nblines
		 * @param nbparagraph
		 * @param texttype
		 */
		public BoxTextContent(int nblines, int nbparagraph, int texttype) {
			this.nblines = nblines;
			this.nbparagraph = nbparagraph;
			this.texttype = texttype;
		}

		public BoxTextContent(int nblines, int nbparagraph, int texttype, String textleftout,
				boolean cutinsideparagraph) {
			this.nblines = nblines;
			this.nbparagraph = nbparagraph;
			this.texttype = texttype;
			this.textleftout = textleftout;
			this.cutinsideparagraph = cutinsideparagraph;
		}

		/**
		 * 
		 * @return The total number of lines printed inside all paragraphs (e.g. for a
		 *         paragraph with 3 lines and a paragraph with 2 lines, 5 is returned)
		 */
		public int getNblines() {
			return nblines;
		}

		/**
		 * 
		 * @return The total number of paragraphs printed or to be printed (e.g. for a
		 *         paragraph with 3 lines and a paragraph with 2 lines, 2 is returned)
		 */
		public int getNbparagraph() {
			return nbparagraph;
		}

		/**
		 * this method will show the height in mm, without the final spacing. This is
		 * especially useful to print in a table, where there is no spacing in the end
		 * @return height without final spacing
		 */
		public float getHeightWithoutFinalSpacing() {
			return getHeight(false);
		}

		private float getHeight(boolean finalspacing) {
			if ((texttype == PDFPage.TEXTTYPE_PLAIN) || (texttype == PDFPage.TEXTTYPE_BULLET_L1)
					|| (texttype == PDFPage.TEXTTYPE_BULLET_L2) || (texttype == PDFPage.TEXTTYPE_BULLET_L3)
					|| (texttype == PDFPage.TEXTTYPE_PARAGRAPH_HEADER))
				return ((nblines * LINE_HEIGHT_NORMAL_TEXT) + ((nblines - 1) * LINE_SPACING_NORMAL_TEXT)
						+ (PARAGRAPH_SPACING_NORMAL_TEXT * (nbparagraph - 1))
						+ PARAGRAPH_SPACING_NORMAL_TEXT * (finalspacing ? 2 : 1)) / MM_TO_POINT;

			if (texttype == PDFPage.TEXTTYPE_SECTION_HEADER)
				return ((nblines * LINE_HEIGHT_SECTION_HEADER) + ((nblines - 1) * LINE_SPACING_SECTION_HEADER)
						+ (PARAGRAPH_SPACING_SECTION_HEADER * (nbparagraph - 1))
						+ PARAGRAPH_SPACING_SECTION_HEADER * (finalspacing ? 2 : 1)) / MM_TO_POINT;

			if (texttype == PDFPage.TEXTTYPE_TITLE)
				return ((nblines * PDFPage.LINE_HEIGHT_TITLE_TEXT) + ((nblines - 1) * LINE_SPACING_TITLE_TEXT)
						+ (PARAGRAPH_SPACING_TITLE_TEXT * (nbparagraph - 1))
						+ PARAGRAPH_SPACING_TITLE_TEXT * (finalspacing ? 2 : 1)) / MM_TO_POINT;

			if (texttype == PDFPage.TEXTTYPE_LABEL)
				return ((nblines * LINE_HEIGHT_NORMAL_TEXT * PDFPage.LABEL_SIZE_REDUCTION)
						+ ((nblines - 1) * LINE_SPACING_NORMAL_TEXT)
						+ (PARAGRAPH_SPACING_NORMAL_TEXT * (nbparagraph - 1))
						+ PARAGRAPH_SPACING_NORMAL_TEXT * (finalspacing ? 2 : 1)) / MM_TO_POINT;

			throw new RuntimeException("Text type not supported " + texttype);
		}

		/**
		 * @return the height of the text in mm, margins included
		 */
		public float getHeight() {
			return getHeight(true);

		}
	}

	/**
	 * @param left        in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param top         in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param imagepath   a valid path for server execution (either relative to
	 *                    classpath home, or absolute)
	 * @param targetwidth the width of the image in mm. Height will be scaled
	 *                    automatically to respect aspect ratio.
	 * 
	 */
	public void drawImageAt(float left, float top, String imagepath, float targetwidth) {
		File file = new File(imagepath);
		if (!file.exists())
			throw new RuntimeException("File " + imagepath + " does not exist");
		this.widgetstoprint.add(() -> {
			float newtop = top;

			if (this.topatzero) {
				newtop = height - top;

			}
			try {
				if (document == null)
					throw new RuntimeException("Document is null when printing image " + imagepath);
				PDImageXObject pdImage = PDImageXObject.createFromFile(imagepath, document);
				float scaling = (targetwidth * MM_TO_POINT) / pdImage.getWidth();
				contentStream.drawImage(pdImage, left * MM_TO_POINT, newtop * MM_TO_POINT, pdImage.getWidth() * scaling,
						pdImage.getHeight() * scaling);
			} catch (RuntimeException e) {
				int stacktracesummaryindex = 5;

				if (e.getStackTrace().length < stacktracesummaryindex)
					stacktracesummaryindex = e.getStackTrace().length;
				logger.severe(" Runtime Exception " + e.getMessage());
				for (int i = 0; i < stacktracesummaryindex; i++)
					logger.severe("    - " + e.getStackTrace()[i]);

				throw new RuntimeException("Error while printing " + imagepath + " at left = " + left + " top = " + top
						+ " targetwidth = " + targetwidth + " original error = " + e.getMessage());

			}

		});

	}

	/**
	 * will automatically scale the image respecting aspect ratio to fit inside the
	 * box. The box is using the normal margins of the PDFPage printer.
	 * 
	 * @param left        in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param top         in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param right       in page coordinates (right reference for image in
	 *                    milimeter (0 is left of page)
	 * @param bottom      in page coordinates (right reference for image in
	 *                    milimeter (0 is left of page)
	 * @param filecontent a byte array with the file content
	 * @param filename    the name of the file with suffix
	 */
	public void drawImageInsideBox(float left, float top, float right, float bottom, byte[] filecontent,
			String filename) {
		this.widgetstoprint.add(() -> {
			float newtop = top;
			float newbottom = bottom;
			if (this.topatzero) {
				newtop = height - top;
				newbottom = height - bottom;
			}
			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, filecontent, filename);
			float rawimagewidthinmm = pdImage.getWidth() / MM_TO_POINT;
			float rawimageheightinmm = pdImage.getHeight() / MM_TO_POINT;
			float boxwidthinmm = (right - left - 2 * PICTURE_MARGIN / MM_TO_POINT);
			float boxheightinmm = (newtop - newbottom) - 2 * PICTURE_MARGIN / MM_TO_POINT;
			float widthratio = rawimagewidthinmm / boxwidthinmm;
			float heightratio = rawimageheightinmm / boxheightinmm;
			logger.fine("		** --- *** debugging rawimagewidth = " + rawimagewidthinmm + " rawimageheightnmm = "
					+ rawimageheightinmm + " boxwidthinmm = " + boxwidthinmm + " boxheightinmm = " + boxheightinmm);
			if (widthratio > heightratio) {
				// scale on width and put margin on height
				float ratio = 1 / widthratio;
				float topinmm = newtop - (PICTURE_MARGIN / MM_TO_POINT
						+ (boxheightinmm - pdImage.getHeight() * ratio / MM_TO_POINT) / 2);
				logger.fine(" ** --- *** CALCULATING TOP = " + topinmm + ", reminder left = " + left);
				contentStream.drawImage(pdImage, left * MM_TO_POINT + PICTURE_MARGIN,
						topinmm * MM_TO_POINT - pdImage.getHeight() * ratio, pdImage.getWidth() * ratio,
						pdImage.getHeight() * ratio);
			} else {
				// scale on height and put margin on width
				float ratio = 1 / heightratio;
				float leftinmm = left + (PICTURE_MARGIN / MM_TO_POINT
						+ (boxwidthinmm - pdImage.getWidth() * ratio / MM_TO_POINT) / 2);
				logger.fine(" ** --- *** CALCULATING LEFT = " + leftinmm + ", reminder top = " + top);

				contentStream.drawImage(pdImage, leftinmm * MM_TO_POINT,
						newtop * MM_TO_POINT - PICTURE_MARGIN - pdImage.getHeight() * ratio, pdImage.getWidth() * ratio,
						pdImage.getHeight() * ratio);

			}
		});
	}

	/**
	 * @param left        in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param top         in page coordinates. This attribute and next defines the
	 *                    top-left point for the image.
	 * @param filecontent a byte array with the file content
	 * @param filename    the name of the file with suffix
	 * @param targetwidth the width the image will be scaled to, preserving aspect
	 *                    ratio
	 */
	public void drawImageAt(float left, float top, byte[] filecontent, String filename, float targetwidth) {
		this.widgetstoprint.add(() -> {

			float newtop = top;

			if (this.topatzero) {
				newtop = height - top;

			}

			PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, filecontent, filename);
			// PDImageXObject.createFromFile(imagepath, document);
			float scaling = (targetwidth * MM_TO_POINT) / pdImage.getWidth();
			contentStream.drawImage(pdImage, left * MM_TO_POINT,
					newtop * MM_TO_POINT - 1 * pdImage.getHeight() * scaling, pdImage.getWidth() * scaling,
					pdImage.getHeight() * scaling);
		});
	}

	/**
	 * This method will write with current HELVETICA font a fix width per character
	 * text (typically number) padded on the right considering usual margins.
	 * 
	 * @param rightForColumn
	 * @param topofline
	 * @param lineindex
	 * @param text
	 * @throws IOException
	 */
	public void drawFixWidthRightAlignedTextAt(float rightForColumn, float topofline, int lineindex, int paragraphindex,
			String text) throws IOException {
		this.widgetstoprint.add(() -> {

			float newtop = topofline;

			if (this.topatzero) {
				newtop = height - topofline;

			}
			if (text != null) {
				int nbcar = text.length();
				float charspacinginmm = PDFPage.getTextSize(new String[] { "0" }, PDFPage.TEXTTYPE_PLAIN);
				float left = rightForColumn - nbcar * charspacinginmm;
				contentStream.setNonStrokingColor(Color.BLACK);
				contentStream.setStrokingColor(Color.BLACK);
				for (int i = 0; i < nbcar; i++) {
					contentStream.beginText();
					contentStream.newLineAtOffset(
							left * MM_TO_POINT - PARAGRAPH_MARGIN_HORIZONTAL + i * charspacinginmm * MM_TO_POINT,
							newtop * MM_TO_POINT - PARAGRAPH_MARGIN_VERTICAL - (lineindex) * (LINE_SPACING_NORMAL_TEXT)
									- LINE_HEIGHT_NORMAL_TEXT * (lineindex + 1)
									- (paragraphindex) * PARAGRAPH_SPACING_NORMAL_TEXT);
					contentStream.setFont(datafont, LINE_HEIGHT_NORMAL_TEXT);
					securedShowText(contentStream, "" + text.charAt(i));
					contentStream.endText();
				}
			}
		});

	}

	public static boolean isValid(char currentchar) {
		Character.UnicodeBlock block = Character.UnicodeBlock.of(currentchar);
		return (!Character.isISOControl(currentchar)) &&
		// modification from quora answer to avoid dependency on awt.
		// currentchar != KeyEvent.CHAR_UNDEFINED &&
				block != null && block != Character.UnicodeBlock.SPECIALS;
	}

	/**
	 * Removes all non win-ansi characters. Note: this probably does not manage
	 * non-european unicode
	 * 
	 * @param text
	 * @return
	 */
	public static String remove(String text) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			if (isValid(text.charAt(i)))
				b.append(text.charAt(i));
			// if (WinAnsiEncoding.INSTANCE.contains(text.charAt(i))) {
			// b.append(text.charAt(i));
			// }
		}
		return b.toString();
	}

	public static void securedShowText(PDPageContentStream contentStream, String text) {
		try {
			contentStream.showText(remove(text));
		} catch (Exception e) {
			throw new RuntimeException("Error in printing text " + text + ", original exception "
					+ e.getClass().toString() + " = " + e.getMessage());
		}
	}

	@Override
	protected int getPageNumber() {
		// only a page is used by a PDFPage
		return 1;
	}

	@Override
	protected void initialize() {
		// in current version, nothing to do in classical PDFPage

	}

	@Override
	protected void layoutPages(int pagesbefore) {
		this.pageindex = pagesbefore + 1;

	}

}
