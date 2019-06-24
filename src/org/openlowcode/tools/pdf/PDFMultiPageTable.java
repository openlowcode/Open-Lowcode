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
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;

/**
 * A multiple-page table that will by default print the header on each page. The
 * columns will not be separated.
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class PDFMultiPageTable implements PDFPageBandSection {

	/**
	 * Holds information about a single cell display
	 * 
	 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
	 *
	 */
	protected class CellText {

		/**
		 * A cell with text not displayed as number
		 * 
		 * @param text the text content
		 */
		public CellText(String text) {
			this.text = text;
			this.displayasNumber = false;
		}

		/**
		 * @param text            the text to display
		 * @param displayasNumber true to display the cell as number
		 */
		public CellText(String text, boolean displayasNumber) {
			this.text = text;
			this.displayasNumber = displayasNumber;
		}

		private boolean displayasNumber;
		private String text;

		/**
		 * @return true if the text should be displayed as number
		 */
		public boolean isDisplayasNumber() {
			return displayasNumber;
		}

		/**
		 * @return the text of the cell
		 */
		public String getText() {
			return text;
		}

	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(PDFPageTable.class.getName());
	private float leftmargin;
	private float rightmargin;
	private float[] columnrelativewidth;
	private String[] headertexts;
	private ArrayList<CellText[]> cellscontent;
	protected float[] columnwidthinmm;
	private int columnnumber;
	private int indexlastlineprinted;

	/**
	 * @return the number of columns in the table
	 */
	public int getColumnNumber() {
		return columnnumber;
	}

	/**
	 * @param row    index of row, starting by 0
	 * @param column index of column, starting by 0
	 * @return
	 */
	public CellText getCellContent(int row, int column) {
		return cellscontent.get(row)[column];
	}

	/**
	 * Creates a table of columns with similar width taking the full page width
	 * 
	 * @param columnnumber number of columns of same size
	 */

	public PDFMultiPageTable(int columnnumber) {
		this(0, 0, columnnumber);
	}

	/**
	 * Creates a table of columns with different width taking the full page width
	 * 
	 * @param columnrelativewidth a table of strictly positive float numbers. It
	 *                            does not need to be the width of columns in
	 *                            milimeter as the utility will resize the columns
	 *                            according to width available, so you can also use
	 *                            percentage or another scale
	 */
	public PDFMultiPageTable(float[] columnrelativewidth) {
		this(0, 0, columnrelativewidth);
	}

	/**
	 * @param leftmargin   space left blank on the left size of the table (betweeen
	 *                     left border of the page band and table)
	 * @param rightmargin  space left blank on the right size of the table (betweeen
	 *                     table and left border of the page band)
	 * @param columnnumber number of columns of same size
	 */
	public PDFMultiPageTable(float leftmargin, float rightmargin, int columnnumber) {
		float[] columnrelativewidth = new float[columnnumber];
		for (int i = 0; i < columnnumber; i++)
			columnrelativewidth[i] = 1;
		constructorcontent(leftmargin, rightmargin, columnrelativewidth);

	}

	/**
	 * @param leftmargin          space left blank on the left size of the table
	 *                            (betweeen left border of the page band and table)
	 * @param rightmargin         space left blank on the right size of the table
	 *                            (betweeen table and left border of the page band)
	 * @param columnrelativewidth a table of strictly positive float numbers. It
	 *                            does not need to be the width of columns in
	 *                            milimeter as the utility will resize the columns
	 *                            according to width available, so you can also use
	 */

	public PDFMultiPageTable(float leftmargin, float rightmargin, float[] columnrelativewidth) {
		constructorcontent(leftmargin, rightmargin, columnrelativewidth);
	}

	private void constructorcontent(float leftmargin, float rightmargin, float[] columnrelativewidth) {
		this.leftmargin = leftmargin;
		this.rightmargin = rightmargin;
		this.columnrelativewidth = columnrelativewidth;
		if (this.columnrelativewidth == null)
			throw new RuntimeException("Column Relative Width should not be null");
		if (this.columnrelativewidth.length == 0)
			throw new RuntimeException("Column Relative Width table should have at least one value");
		for (int i = 0; i < columnrelativewidth.length; i++)
			if (columnrelativewidth[i] <= 0f)
				throw new RuntimeException("Column Relative Width should be strictly positive");
		this.columnnumber = columnrelativewidth.length;
		this.cellscontent = new ArrayList<CellText[]>();
		this.indexlastlineprinted = -1;

	}

	/**
	 * @param headertexts the list of header texts (should be consistent with the
	 *                    column number)
	 */
	public void setHeaders(String[] headertexts) {
		if (headertexts == null)
			throw new RuntimeException("Header texts cannot be null");
		if (headertexts.length != columnnumber)
			throw new RuntimeException("Header texts length (" + headertexts.length
					+ ") has to be the same as column number (" + columnnumber + ")");
		this.headertexts = headertexts;
	}

	/**
	 * @param headertext  the text to print as header
	 * @param columnindex index of column between 0 (included) and columnnumber
	 *                    (excluded)
	 */
	public void setHeader(String headertext, int columnindex) {
		if (this.headertexts == null)
			this.headertexts = new String[columnnumber];
		if ((columnindex < 0) || (columnindex >= columnnumber))
			throw new RuntimeException("columnindex is to be between 0 (included) and columnnumber (excluded).");
		this.headertexts[columnindex] = headertext;
	}

	/**
	 * @param linecontent
	 */
	public void addOneLineContent(String[] linecontent) {
		if (linecontent.length != columnnumber)
			throw new RuntimeException("Line content table length (" + linecontent.length
					+ ") has to be the same as column number (" + columnnumber + ")");
		CellText[] celltexts = new CellText[linecontent.length];
		for (int i = 0; i < linecontent.length; i++) {
			celltexts[i] = new CellText(linecontent[i]);
		}
		this.cellscontent.add(celltexts);
	}

	/**
	 * Creates a new line without content. Content can then be added by
	 * setCellContent
	 */
	public void addBlankLine() {
		CellText[] blankline = new CellText[columnnumber];
		for (int i = 0; i < blankline.length; i++)
			blankline[i] = new CellText("");
		this.cellscontent.add(blankline);
	}

	/**
	 * @param cellcontent  text to include in the cell
	 * @param columnindex  index of the column, starting with zero
	 * @param showasnumber if true, will display as right aligned constant space
	 *                     number
	 */
	public void setCellContent(String cellcontent, int columnindex, boolean showasnumber) {
		if (this.cellscontent.size() == 0)
			this.addBlankLine();
		CellText[] activeline = this.cellscontent.get(this.cellscontent.size() - 1);
		if ((columnindex < 0) || (columnindex >= columnnumber))
			throw new RuntimeException(
					"columnindex is to be between 0 (included) and columnnumber -" + columnnumber + "- (excluded).");
		activeline[columnindex] = new CellText(cellcontent, showasnumber);
	}

	/**
	 * @param cellcontent
	 * @param columnindex
	 */
	public void setCellContent(String cellcontent, int columnindex) {
		setCellContent(cellcontent, columnindex, false);
	}

	private float getHeadersHeight(float leftinmm, float rightinmm) throws IOException {
		if (this.columnwidthinmm == null)
			initColumnWidthInMm(leftinmm, rightinmm);
		float maxheight = 0;
		for (int i = 0; i < this.columnnumber; i++) {
			float columnwidth = this.columnwidthinmm[i];
			String text = headertexts[i];
			float cellheight = PDFPage
					.calculateBoxAndMaybeWriteText(0, 0, columnwidth, text, false, null, PDFPage.TEXTTYPE_LABEL)
					.getHeight();
			if (cellheight > maxheight)
				maxheight = cellheight;
		}
		return maxheight;

	}

	private void initColumnWidthInMm(float leftinmm, float rightinmm) {
		this.columnwidthinmm = new float[columnnumber];
		float totalrelativewidth = 0;
		for (int i = 0; i < columnrelativewidth.length; i++)
			totalrelativewidth += columnrelativewidth[i];
		for (int i = 0; i < columnnumber; i++) {
			this.columnwidthinmm[i] = (rightinmm - leftinmm - this.leftmargin - this.rightmargin)
					* columnrelativewidth[i] / totalrelativewidth;
		}

	}

	private float getLineHeight(float leftinmm, float rightinmm, int lineindex) throws IOException {
		float maxheight = 0;

		for (int i = 0; i < this.columnnumber; i++) {
			float columnwidth = this.columnwidthinmm[i];
			float cellheight = getCellHeight(columnwidth, lineindex, i);
			if (cellheight > maxheight)
				maxheight = cellheight;
		}
		return maxheight;
	}

	/**
	 * calculates the height of the cell. This method can be overloaded by
	 * subclasses with specific layout (rich-text...)
	 * 
	 * @param columnwidth width of the table column
	 * @param lineindex   line index starting with 0
	 * @param columnindex column index starting with 0
	 * @return the height of the cell
	 * @throws IOException
	 */
	protected float getCellHeight(float columnwidth, int lineindex, int columnindex) throws IOException {
		String text = this.cellscontent.get(lineindex)[columnindex].text;
		return PDFPage.calculateBoxAndMaybeWriteText(0, 0, columnwidth, text, false, null, PDFPage.TEXTTYPE_PLAIN)
				.getHeightWithoutFinalSpacing();

	}

	/**
	 * @param currentpage
	 * @param pagenumber
	 * @param mmfromtopforsection
	 * @param leftinmm
	 * @param rightinmm
	 * @return the new top for printing the next element
	 * @throws IOException
	 */
	private float printHeader(PDFPage currentpage, float mmfromtopforsection, float leftinmm, float rightinmm)
			throws IOException {

		float headerheight = getHeadersHeight(leftinmm, rightinmm);
		float currentleft = leftinmm;
		for (int i = 0; i < columnwidthinmm.length; i++) {
			float currentcolumnwidth = columnwidthinmm[i];
			currentpage.drawBoxWithWidthAndHeight(false, currentleft, mmfromtopforsection, currentcolumnwidth,
					headerheight);
			currentpage.drawTextInBox(currentleft, mmfromtopforsection, currentleft + currentcolumnwidth,
					this.headertexts[i], PDFPage.TEXTTYPE_LABEL);
			currentleft += currentcolumnwidth;

		}
		return mmfromtopforsection + headerheight;
	}

	/**
	 * @param currentpage
	 * @param pagenumber
	 * @param rowindex
	 * @param mmfromtopforsection
	 * @param leftinmm
	 * @param rightinmm
	 * @return the new top for printing the next element
	 * @throws IOException
	 */
	private float printOneRow(PDFPage currentpage, int rowindex, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException {

		float rowheight = this.getLineHeight(leftinmm, rightinmm, rowindex);
		float currentleft = leftinmm;
		for (int i = 0; i < columnwidthinmm.length; i++) {
			float currentcolumnwidth = columnwidthinmm[i];
			currentpage.drawBoxWithWidthAndHeight(false, currentleft, mmfromtopforsection, currentcolumnwidth,
					rowheight);
			printOneCell(currentpage, rowindex, i, mmfromtopforsection, currentleft, currentcolumnwidth);
			currentleft += currentcolumnwidth;

		}
		// add spacing if last column
		return (mmfromtopforsection + rowheight);
	}

	/**
	 * This method prints the content of one row of a table. It can be overriden by
	 * subclasses wishing to add their own formatting
	 * 
	 * @param currentpage         page to print in
	 * @param rowindex            index of the row (starting with 0)
	 * @param columnindex         index of the column (starting with 0)
	 * @param mmfromtopforsection vertical position for printing as per pageband
	 *                            mechanism
	 * @param currentleft         left of the cell
	 * @param currentcolumnwidth  with of the cell
	 * @throws IOException if any exception is raised regarding printing of the
	 *                     content
	 */
	protected void printOneCell(PDFPage currentpage, int rowindex, int columnindex, float mmfromtopforsection,
			float currentleft, float currentcolumnwidth) throws IOException {
		CellText currenttextcell = this.cellscontent.get(rowindex)[columnindex];

		if (!currenttextcell.displayasNumber) {
			currentpage.drawTextInBox(currentleft, mmfromtopforsection, currentleft + currentcolumnwidth,
					currenttextcell.text, PDFPage.TEXTTYPE_PLAIN);
		} else {
			currentpage.drawFixWidthRightAlignedTextAt(currentleft + currentcolumnwidth, mmfromtopforsection, 0, 0,
					currenttextcell.text);
		}
	}

	@Override
	public void print(PDFPageBand pageband, PDFPage currentpage, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException {
		throw new RuntimeException("Not implemented as this component can be on several pages");
		// adding margin at the top
		/*
		 * float currenttop = mmfromtopforsection+PDFPage.LINE_SPACING_NORMAL_TEXT;
		 * currenttop = printHeader(currentpage, pagenumber, currenttop, leftinmm,
		 * rightinmm); for (int i=0;i<this.cellscontent.size();i++) { currenttop =
		 * printOneRow(currentpage,pagenumber,i,currenttop,leftinmm,rightinmm); }
		 */
	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		float totalheight = this.getHeadersHeight(leftinmm, rightinmm);
		for (int i = 0; i < this.cellscontent.size(); i++)
			totalheight += this.getLineHeight(leftinmm, rightinmm, i);
		return totalheight + PDFPage.LINE_SPACING_NORMAL_TEXT * 2;
	}

	@Override
	public boolean breakableSection() {
		return true;
	}

	@Override
	public PartialPrintFeedback printPartial(PDFPageBand pageband, float spaceleft, PDFPage currentpage,
			float mmfromtopforsection, float leftinmm, float rightinmm) throws IOException {
		float currenttop = mmfromtopforsection;
		currenttop = currenttop + PDFPage.LINE_SPACING_NORMAL_TEXT;
		if (this.indexlastlineprinted == -1) {
			// check if space to print at least a header and a cell
			float topafterfirstdataline = currenttop + this.getHeadersHeight(leftinmm, rightinmm)
			+ (this.cellscontent.size() > 0 ? this.getLineHeight(leftinmm, rightinmm, 0) : 0);
			// T-816 correction on line above
			if (topafterfirstdataline - mmfromtopforsection > spaceleft)
				return new PartialPrintFeedback(topafterfirstdataline, false);
		}
		currenttop = printHeader(currentpage, currenttop, leftinmm, rightinmm);
		int nextindextotry = this.indexlastlineprinted + 1;
		while (nextindextotry < this.cellscontent.size()) {
			float nextbottom = currenttop + this.getLineHeight(leftinmm, rightinmm, nextindextotry);
			if (nextbottom + PDFPage.LINE_SPACING_NORMAL_TEXT - mmfromtopforsection > spaceleft)
				return new PartialPrintFeedback(nextbottom, false);
			currenttop = this.printOneRow(currentpage, nextindextotry, currenttop, leftinmm, rightinmm);
			this.indexlastlineprinted = nextindextotry;
			nextindextotry = this.indexlastlineprinted + 1;
		}
		return new PartialPrintFeedback(currenttop + PDFPage.LINE_SPACING_NORMAL_TEXT, true);

	}

	@Override
	public String dropContentSample() {
		return "PDFMultiPageTable, first column title: "
				+ (headertexts != null ? (headertexts.length > 0 ? headertexts[0] : "NO HEADER") : "NO HEADER")
				+ ", First cell content : "
				+ (cellscontent.size() > 0 ? (cellscontent.get(0) != null ? cellscontent.get(0)[0] : "NO DATA")
						: "NO DATA");
	}

	@Override
	public void setParentDocument(PDFDocument document) {
		// do nothing

	}

	@Override
	public void initialize() {
		// do nothing

	}
}
