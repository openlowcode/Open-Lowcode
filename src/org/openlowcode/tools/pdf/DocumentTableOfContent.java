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
import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;

/**
 * This component prints a table of content made of all the SectionHeaders of
 * all the PDFPageBandSections of the document
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class DocumentTableOfContent implements PDFPageBandSection {

	private ArrayList<PagedLabel> content;

	private PDFDocument parentdocument;

	private int currentindex;

	public DocumentTableOfContent() {
		currentindex = 0;
		content = new ArrayList<PagedLabel>();
	}

	@Override
	public void print(PDFPageBand pageband, PDFPage currentpage, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException {
		for (int i = 0; i < content.size(); i++) {
			PagedLabel label = content.get(i);
			float textsize = PDFPage.getTextSize(new String[] {label.getLabel()}, PDFPage.TEXTTYPE_PLAIN);
			currentpage.drawLine(false, leftinmm+10+textsize+ label.getOriginalOffset() / 3,mmfromtopforsection+i*PDFPage.LINE_SPACING_NORMAL_TEXT+PDFPage.PARAGRAPH_MARGIN_VERTICAL,rightinmm-25,mmfromtopforsection+i*PDFPage.LINE_SPACING_NORMAL_TEXT+PDFPage.PARAGRAPH_MARGIN_VERTICAL,Color.GRAY);
			currentpage.drawSimpleTextAt(false, leftinmm+ label.getOriginalOffset() / 3, mmfromtopforsection, i, 0, label.getLabel());
			currentpage.drawCalculatedText(rightinmm, mmfromtopforsection, i, 0, false,
					() -> "" + label.getPageNumber());
		}

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		return new PDFPage.BoxTextContent(content.size(), 1, PDFPage.TEXTTYPE_PLAIN).getHeight();
	}

	@Override
	public boolean breakableSection() {
		return true;
	}

	@Override
	public PartialPrintFeedback printPartial(PDFPageBand pageband, float spaceleft, PDFPage currentpage,
			float mmfromtopforsection, float leftinmm, float rightinmm) throws IOException {
		int numberoflines = PDFPage.getNumberOfLinesAvailableForParagraph(spaceleft);
		int linestoprint = content.size() - currentindex;
		boolean full = true;
		if (numberoflines < linestoprint) {
			linestoprint = numberoflines;
			full = false;
		}
		for (int i = 0; i < linestoprint; i++) {
			PagedLabel label = content.get(i + currentindex);
			float textsize = PDFPage.getTextSize(new String[] {label.getLabel()}, PDFPage.TEXTTYPE_PLAIN);
			currentpage.drawSimpleTextAt(false, leftinmm+ label.getOriginalOffset() / 3, mmfromtopforsection, i,
					0, label.getLabel());
			currentpage.drawCalculatedText(rightinmm-20, mmfromtopforsection, i, 0, false,
					() -> "" + label.getPageNumber());
			currentpage.drawLine(false, leftinmm+5+PDFPage.PARAGRAPH_MARGIN_HORIZONTAL/PDFPage.MM_TO_POINT+textsize+ label.getOriginalOffset() / 3,mmfromtopforsection+PDFPage.getTextVerticalOffsetInMM(i,0),rightinmm-25,mmfromtopforsection+PDFPage.getTextVerticalOffsetInMM(i,0),Color.GRAY);
		}
		currentindex = currentindex + linestoprint;
		return new PartialPrintFeedback(
				mmfromtopforsection + new PDFPage.BoxTextContent(linestoprint, 1, PDFPage.TEXTTYPE_PLAIN).getHeight(),
				full);
	}

	@Override
	public String dropContentSample() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParentDocument(PDFDocument document) {
		this.parentdocument = document;

	}

	@Override
	public void initialize() {
		if (parentdocument==null) throw new RuntimeException("Table of content has not been added correctly to Page Band.");
		for (int i = 0; i < parentdocument.getPartsNumber(); i++) {
			PDFPart thispart = parentdocument.getPart(i);
			if (thispart instanceof PDFPageBand) {
				PDFPageBand thisband = (PDFPageBand) thispart;
				for (int j = 0; j < thisband.getSectionNumber(); j++) {
					PDFPageBandSection section = thisband.getSectionAt(j);
					if (section instanceof SectionHeader)
						this.content.add((SectionHeader) (section));
				}
			}
			if (thispart instanceof PDFPage) {
				PDFPage thispage = (PDFPage) thispart;
				PagedLabel[] labelsforpage = thispage.getTOCLabelsForPage();
				if (labelsforpage!=null) for (int j=0;j<labelsforpage.length;j++) {
					this.content.add(labelsforpage[j]);
					
				}
			}
		}

	}

}
