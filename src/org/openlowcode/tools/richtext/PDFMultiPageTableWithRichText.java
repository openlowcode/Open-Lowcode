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

import org.openlowcode.tools.pdf.PDFMultiPageTable;
import org.openlowcode.tools.pdf.PDFPage;

/**
 * a table that can print on multiple pages with some columns being configured
 * as Open Lowcode Rich Text
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PDFMultiPageTableWithRichText
		extends
		PDFMultiPageTable {

	private boolean[] richtextcolumn;

	private void commonConstructorStuff(int columnnumber) {
		this.richtextcolumn = new boolean[columnnumber];
		for (int i = 0; i < richtextcolumn.length; i++)
			richtextcolumn[i] = false;
	}

	/**
	 * create a table with potentially rich text content
	 * 
	 * @param leftmargin margin left of the table
	 * @param rightmargin margin right of the table
	 * @param columnrelativewidth list of columns relative widths.
	 */
	public PDFMultiPageTableWithRichText(float leftmargin, float rightmargin, float[] columnrelativewidth) {
		super(leftmargin, rightmargin, columnrelativewidth);
		commonConstructorStuff(columnrelativewidth.length);
	}

	/**
	 * create a table with potentially rich text content with columns of similar widths
	 * 
	 * @param leftmargin margin left of the table
	 * @param rightmargin margin right of the table
	 * @param columnnumber 
	 */
	public PDFMultiPageTableWithRichText(float leftmargin, float rightmargin, int columnnumber) {
		super(leftmargin, rightmargin, columnnumber);
		commonConstructorStuff(columnnumber);

	}

	/**
	 * create a table with no left or right margin
	 * 
	 * @param columnrelativewidth relative widths of columns
	 */
	public PDFMultiPageTableWithRichText(float[] columnrelativewidth) {
		super(columnrelativewidth);
		commonConstructorStuff(columnrelativewidth.length);
	}

	public PDFMultiPageTableWithRichText(int columnnumber) {
		super(columnnumber);
	}

	@Override
	protected float getCellHeight(float columnwidth, int lineindex, int columnindex) throws IOException {
		if (this.richtextcolumn[columnindex] == true) {
			String text = this.getCellContent(lineindex, columnindex).getText();
			ArrayList<
					RichTextSection> richtextcontent = RichTextParser.simplifyforblack(RichTextParser.parseText(text));
			float cellheight = 0;

			for (int j = 0; j < richtextcontent.size(); j++) {
				RichTextSection thisrichtextelement = richtextcontent.get(j);
				int pagetype = PDFPage.TEXTTYPE_PLAIN;
				if (thisrichtextelement.isBullet())
					pagetype = PDFPage.TEXTTYPE_BULLET_L1;
				if (thisrichtextelement.isSectiontitle())
					pagetype = PDFPage.TEXTTYPE_PARAGRAPH_HEADER;
				if (j < richtextcontent.size() - 1) {
					cellheight += PDFPage.calculateBoxAndMaybeWriteText(0, 0, columnwidth,
							thisrichtextelement.getText(), false, null, pagetype).getHeight();
				} else {
					cellheight += PDFPage.calculateBoxAndMaybeWriteText(0, 0, columnwidth,
							thisrichtextelement.getText(), false, null, pagetype).getHeightWithoutFinalSpacing();
				}
			}
			return cellheight;
		}
		return super.getCellHeight(columnwidth, lineindex, columnindex);
	}

	@Override
	protected void printOneCell(
			PDFPage currentpage,
			int rowindex,
			int columnindex,
			float mmfromtopforsection,
			float currentleft,
			float currentcolumnwidth) throws IOException {
		if (this.richtextcolumn[columnindex]) {

			ArrayList<RichTextSection> richtextcontent = RichTextParser
					.simplifyforblack(RichTextParser.parseText(this.getCellContent(rowindex, columnindex).getText()));
			float currentopforrichtext = mmfromtopforsection;
			for (int j = 0; j < richtextcontent.size(); j++) {
				RichTextSection thisrichtextelement = richtextcontent.get(j);
				int pagetype = PDFPage.TEXTTYPE_PLAIN;
				if (thisrichtextelement.isBullet())
					pagetype = PDFPage.TEXTTYPE_BULLET_L1;
				if (thisrichtextelement.isSectiontitle())
					pagetype = PDFPage.TEXTTYPE_PARAGRAPH_HEADER;
				currentopforrichtext += PDFPage.calculateBoxAndMaybeWriteText(currentleft, currentopforrichtext,
						currentleft + currentcolumnwidth, thisrichtextelement.getText(), true, currentpage, pagetype)
						.getHeight();
			}

		} else {
			super.printOneCell(currentpage, rowindex, columnindex, mmfromtopforsection, currentleft,
					currentcolumnwidth);
		}
	}

	public void setColumnRichText(int columnindex) {
		if (columnindex < 0)
			throw new RuntimeException("columnindex need to be positive");
		if (columnindex >= this.richtextcolumn.length)
			throw new RuntimeException("Index out of range " + columnindex + ">=" + this.richtextcolumn.length);
		this.richtextcolumn[columnindex] = true;
	}

}
