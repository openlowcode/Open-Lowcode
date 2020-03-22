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
 * Plain text in a document
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SectionText
		implements
		PDFPageBandSection {
	private String text;
	private static Logger logger = Logger.getLogger(SectionText.class.getName());
	private String remainingtext;
	private boolean firstprint;
	private boolean compactprint;

	/**
	 * creates a section text, to be added in a page band
	 * 
	 * @param text         text to print. it can be several lines, including '\r'
	 *                     (small carriage return) and '\n' (big carriage return)
	 * @param compactprint if true, print in a compact form, if false print in
	 *                     normal (non-compact) way
	 */
	public SectionText(String text, boolean compactprint) {
		this.text = text;
		this.remainingtext = text;
		this.firstprint = true;
		this.compactprint = compactprint;
	}

	/**
	 * create a section text with normal (not compact) print, to be added in a page
	 * band
	 * 
	 * @param text text to print. it can be several lines, including '\r' (small
	 *             carriage return) and '\n' (big carriage return)
	 */
	public SectionText(String text) {
		this.text = text;
		this.remainingtext = text;
		this.firstprint = true;
		this.compactprint = false;
	}

	@Override
	public void print(
			PDFPageBand pageband,
			PDFPage currentpage,
			float mmfromtopforsection,
			float leftinmm,
			float rightinmm) throws IOException {
		PDFPage.calculateBoxAndMaybeWriteText(leftinmm, mmfromtopforsection, rightinmm, text, true, false, 0,
				currentpage, PDFPage.TEXTTYPE_PLAIN, false, compactprint);

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		return PDFPage.calculateBoxAndMaybeWriteText(leftinmm, 0, rightinmm, text, false, false, 0, null,
				PDFPage.TEXTTYPE_PLAIN, false, compactprint).getHeight();
	}

	@Override
	public boolean breakableSection() {
		return true;
	}

	@Override
	public PartialPrintFeedback printPartial(
			PDFPageBand pageband,
			float spaceleft,
			PDFPage currentpage,
			float mmfromtopforsection,
			float leftinmm,
			float rightinmm) throws IOException {
		float heightforremaining = PDFPage.calculateBoxAndMaybeWriteText(leftinmm, 0, rightinmm, remainingtext, false,
				false,0,null, PDFPage.TEXTTYPE_PLAIN,false,compactprint).getHeight();

		this.remainingtext = PDFPage.writeAsMuchTextAsPossible(leftinmm, mmfromtopforsection, rightinmm, spaceleft,
				remainingtext, currentpage, PDFPage.TEXTTYPE_PLAIN, !firstprint,compactprint).getTextleftout();
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
		return "Section header sample: "
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
