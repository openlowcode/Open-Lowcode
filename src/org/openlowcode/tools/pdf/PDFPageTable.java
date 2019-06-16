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

import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.tools.pdf.PDFPage.BoxTextContent;

/**
 * An utility class to draw a table of content on a single page
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class PDFPageTable {
	private static Logger logger = Logger.getLogger(PDFPageTable.class.getName());
	private float left;
	private PDFPage parentpage;
	private float[] columnwidth;

	// ------------- line info
	private int currentlinetextlines;
	private float topofline;
	private float bottomofline;

	/**
	 * Creates a table of columns with similar width taking the full page width
	 * 
	 * @param parentpage   parent PDFPage
	 * @param top          top of the page in usual coordinates
	 * @param columnnumber number of columns of same size
	 */
	public PDFPageTable(PDFPage parentpage, float top, int columnnumber) {
		this(parentpage, top, parentpage.getPageLeft(), parentpage.getPageRight(), columnnumber);
	}

	/**
	 * Creates a table of columns with different width taking the full page width
	 * 
	 * @param parentpage          parent PDFPage
	 * @param top                 top of the page in usual coordinates (milimeter)
	 * @param columnrelativewidth a table of strictly positive float numbers. It
	 *                            does not need to be the width of columns in
	 *                            milimeter as the utility will resize the columns
	 *                            according to width available, so you can also use
	 *                            percentage or another scale
	 */
	public PDFPageTable(PDFPage parentpage, float top, float[] columnrelativewidth) {
		this(parentpage, top, parentpage.getPageLeft(), parentpage.getPageRight(), columnrelativewidth);
	}

	/**
	 * @param parentpage   parent PDFPage
	 * @param top          top of the page in usual coordinates (milimeter)
	 * @param left         coordinates of left of table in milimeter
	 * @param right        coordinates of right of table in milimeter
	 * @param columnnumber number of columns of same size
	 */
	public PDFPageTable(PDFPage parentpage, float top, float left, float right, int columnnumber) {
		float[] columnrelativewidth = new float[columnnumber];
		for (int i = 0; i < columnnumber; i++)
			columnrelativewidth[i] = 1;
		constructorcontent(parentpage, top, left, right, columnrelativewidth);

	}

	/**
	 * @param parentpage          parent PDFPage
	 * @param top                 top of the page in usual coordinates (milimeter)
	 * @param left                coordinates of left of table in milimeter
	 * @param right               coordinates of right of table in milimeter
	 * @param columnrelativewidth a table of strictly positive float numbers. It
	 *                            does not need to be the width of columns in
	 *                            milimeter as the utility will resize the columns
	 *                            according to width available, so you can also use
	 */
	public PDFPageTable(PDFPage parentpage, float top, float left, float right, float[] columnrelativewidth) {
		constructorcontent(parentpage, top, left, right, columnrelativewidth);
	}

	private void constructorcontent(PDFPage parentpage, float top, float left, float right,
			float[] columnrelativewidth) {
		this.left = left;
		this.parentpage = parentpage;
		if (columnrelativewidth == null)
			throw new RuntimeException("The column relative width should not be a null array");
		if (columnrelativewidth.length == 0)
			throw new RuntimeException("The column number cannot be zero");
		float totalrelativewidth = 0;
		for (int i = 0; i < columnrelativewidth.length; i++) {
			float thiscolumnrelativewidth = columnrelativewidth[i];
			if (!(thiscolumnrelativewidth > 0))
				throw new RuntimeException("column index " + i + " has null or negative relative width");
			totalrelativewidth += thiscolumnrelativewidth;
		}
		float multiplier = (right - left) / totalrelativewidth;
		this.columnwidth = new float[columnrelativewidth.length];
		for (int i = 0; i < columnwidth.length; i++) {
			this.columnwidth[i] = columnrelativewidth[i] * multiplier;
		}
		this.currentlinetextlines = 0; // to mark a line has not yet been initiated;
		this.topofline = top;
		this.bottomofline = top;
	}

	/**
	 * @param columnindex index of column starting at zero
	 * @return coordinates of left of column in milimeter (on the horizontal axis)
	 */
	private float getLeftForColumn(int columnindex) {
		if (columnindex < 0)
			throw new RuntimeException("index has to be between 0 and " + (columnwidth.length - 1) + " included.");
		if (columnindex >= columnwidth.length)
			throw new RuntimeException("index has to be between 0 and " + (columnwidth.length - 1) + " included.");
		float leftcoordinates = left;
		for (int i = 0; i < columnindex; i++) {
			leftcoordinates += columnwidth[i];
		}
		return leftcoordinates;
	}

	/**
	 * @param columnindex index of column starting at zero
	 * @return coordinates of right of column in milimeter (on the horizontal axis)
	 */
	private float getRightForColumn(int columnindex) {
		if (columnindex < 0)
			throw new RuntimeException("index has to be between 0 and " + (columnwidth.length - 1) + " included.");
		if (columnindex >= columnwidth.length)
			throw new RuntimeException("index has to be between 0 and " + (columnwidth.length - 1) + " included.");
		float rightcoordinates = left;
		for (int i = 0; i < columnindex + 1; i++) {
			rightcoordinates += columnwidth[i];
		}
		return rightcoordinates;
	}

	/**
	 * @param nblinesoftext initiates a new table line able to hold this number of
	 *                      lines.
	 */
	public void setLine(int nblinesoftext) {
		setLine(nblinesoftext, 1);
	}

	public void setLine(int nblinesoftext, int nbofparagraphs) {
		if (nblinesoftext < 1)
			throw new RuntimeException("nblinesoftext should be 1 or more, not " + nblinesoftext);
		if (nbofparagraphs < 1)
			throw new RuntimeException("nbofparagraphs should be 1 or more, not " + nbofparagraphs);
		this.topofline = bottomofline;
		this.bottomofline = topofline
				+ (parentpage.isTopAtZero() ? 1 : -1) * ((nblinesoftext * PDFPage.LINE_HEIGHT_NORMAL_TEXT)
						+ ((nblinesoftext - 1) * PDFPage.LINE_SPACING_NORMAL_TEXT)
						+ ((nbofparagraphs + 1) * PDFPage.PARAGRAPH_SPACING_NORMAL_TEXT)) / PDFPage.MM_TO_POINT;

		this.currentlinetextlines = nblinesoftext;
	}

	public void setLine(BoxTextContent textcontent) {
		setLine(textcontent.getNblines(), textcontent.getNbparagraph());
	}

	/**
	 * draws a box around a single cell of table in the current line
	 * 
	 * @param fat    true if fat line
	 * @param column column index starting at zero
	 * @throws IOException
	 */
	public void drawBoxForCurrentLine(boolean fat, int column) throws IOException {
		drawBoxForCurrentLine(fat, column, column);
	}

	/**
	 * draws a box around a several consecutive cells of table in the current line
	 * 
	 * @param fat         true if fat line
	 * @param firstcolumn first column around which to draw the box
	 * @param lastcolumn  last column around which to draw the box
	 * @throws IOException
	 */
	public void drawBoxForCurrentLine(boolean fat, int firstcolumn, int lastcolumn) throws IOException {
		if (this.currentlinetextlines == 0)
			throw new RuntimeException("You need to use setLine before drawing box for current line");
		logger.fine("draw box for current line left=" + getLeftForColumn(firstcolumn) + ", top=" + topofline
				+ ", right = " + getRightForColumn(lastcolumn) + ", bottom = " + bottomofline);
		parentpage.drawBox(fat, getLeftForColumn(firstcolumn), topofline, getRightForColumn(lastcolumn), bottomofline);

	}

	/**
	 * @param text      text to write
	 * @param label     if true, uses label font, false uses plain (data) text
	 * @param column    column index starting at zero
	 * @param lineindex the index, starting at 1, of line in the cell
	 * @throws IOException
	 */
	public void drawTextForCurrentLine(String text, boolean label, int column, int lineindex) throws IOException {
		if (this.currentlinetextlines == 0)
			throw new RuntimeException("You need to use setLine before drawing box for current line");
		parentpage.drawSimpleTextAt(label, getLeftForColumn(column), topofline, lineindex, 0, text);
	}

	public void drawCalculatedTextforCurrentLine(TextAfterLayout calculatedtext, boolean label, int column,
			int lineindex) throws IOException {
		if (this.currentlinetextlines == 0)
			throw new RuntimeException("You need to use setLine before drawing box for current line");
		parentpage.drawCalculatedText(label, getLeftForColumn(column), topofline, lineindex, 0, true, calculatedtext);
	}

	public void DrawTextAsFigureForCurrentLine(String text, int column, int lineindex) throws IOException {
		if (this.currentlinetextlines == 0)
			throw new RuntimeException("You need to use setLine before drawing box for current line");
		parentpage.drawFixWidthRightAlignedTextAt(getRightForColumn(column), topofline, lineindex, 0, text);
	}

	public void drawTextWithWrapForCurrentLine(String text, boolean label, int column) throws IOException {
		if (this.currentlinetextlines == 0)
			throw new RuntimeException("You need to use setLine before drawing box for current line");
		parentpage.drawTextInBox(getLeftForColumn(column), topofline, getRightForColumn(column), text);
	}

	public BoxTextContent getLinesNeeded(String[] text, boolean label) throws IOException {
		if (text == null)
			throw new RuntimeException("text table needs to be non-null.");
		if (text.length != columnwidth.length)
			throw new RuntimeException("text table needs to have same number of columns (" + text.length
					+ ") than table column (" + columnwidth.length + ").");
		BoxTextContent returnvalue = null; // cannot be more than one
		for (int i = 0; i < text.length; i++)
			if (text[i] != null) {
				BoxTextContent boxforthiscolumn = parentpage.calculateTextSize(getLeftForColumn(i),
						getRightForColumn(i), text[i]);
				if (returnvalue == null) {
					returnvalue = boxforthiscolumn;
				} else {
					if (returnvalue.getHeight() < boxforthiscolumn.getHeight())
						returnvalue = boxforthiscolumn;
				}

			}
		return returnvalue;
	}
}
