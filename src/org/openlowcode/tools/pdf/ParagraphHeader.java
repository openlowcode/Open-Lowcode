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

import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;

/**
 * A low level header to separate paragraphs inside a section
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class ParagraphHeader implements PDFPageBandSection {
	private String text;
	private static Logger logger = Logger.getLogger(SectionText.class.getName());
	private String remainingtext;
	private boolean firstprint;
	private float mmleftmargin;

	public ParagraphHeader(float mmleftmargin, String text) {
		this.text = text;
		this.remainingtext = text;
		this.firstprint = true;
		this.mmleftmargin = mmleftmargin;
	}

	public ParagraphHeader(String text) {
		this(0, text);
	}

	@Override
	public void print(PDFPageBand pageband, PDFPage currentpage, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException {
		PDFPage.calculateBoxAndMaybeWriteText(leftinmm+mmleftmargin, mmfromtopforsection, rightinmm, text, true, currentpage,
				PDFPage.TEXTTYPE_PARAGRAPH_HEADER);

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		return PDFPage.calculateBoxAndMaybeWriteText(leftinmm+mmleftmargin, 0, rightinmm, text, false, null,
				PDFPage.TEXTTYPE_PARAGRAPH_HEADER).getHeight();
	}

	@Override
	public boolean breakableSection() {
		return true;
	}

	@Override
	public PartialPrintFeedback printPartial(PDFPageBand pageband, float spaceleft, PDFPage currentpage,
			float mmfromtopforsection, float leftinmm, float rightinmm) throws IOException {
		float heightforremaining = PDFPage.calculateBoxAndMaybeWriteText(leftinmm+mmleftmargin, 0, rightinmm, remainingtext, false,
				null, PDFPage.TEXTTYPE_PARAGRAPH_HEADER).getHeight();

		this.remainingtext = PDFPage.writeAsMuchTextAsPossible(leftinmm + mmleftmargin, mmfromtopforsection, rightinmm,
				spaceleft, remainingtext, currentpage, PDFPage.TEXTTYPE_PARAGRAPH_HEADER, !firstprint).getTextleftout();
		logger.fine("drop remaining textn space left " + spaceleft + "-----------------------------------------------");
		logger.fine("remaining text length = " + (this.remainingtext != null ? this.remainingtext.length() : "NULL"));
		firstprint = false;
		// note: do not care about new height as at end of page, that is why we give
		// back zero although it is dirty
		if (this.remainingtext.length() > 0)
			return new PartialPrintFeedback(0, false);
		return new PartialPrintFeedback(mmfromtopforsection + heightforremaining, true);
	}

	@Override
	public String dropContentSample() {
		return "Pargraph header sample: "
				+ (this.text != null ? (this.text.length() > 100 ? this.text.substring(0, 100) + "..." : this.text)
						: "NULLSTRING");
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
