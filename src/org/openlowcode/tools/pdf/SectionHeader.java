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

/***
 * A section header is a "big title" inside a document. This is a higher level
 * separation than the paragraph header
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class SectionHeader implements PDFPageBandSection, PagedLabel {
	private static Logger logger = Logger.getLogger(SectionHeader.class.getName());
	private float mmleftmargin;
	private String text;
	private PDFPage currentpage;

	public SectionHeader(float mmleftmargin, String text) {
		this.mmleftmargin = mmleftmargin;
		this.text = text;

	}

	@Override
	public void print(PDFPageBand pageband, PDFPage currentpage, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException {
		PDFPage.calculateBoxAndMaybeWriteText(leftinmm + mmleftmargin, mmfromtopforsection, rightinmm, text, true,
				currentpage, PDFPage.TEXTTYPE_SECTION_HEADER).getHeight();
		this.currentpage = currentpage;
		logger.finer("--> Adding current page  " + currentpage.getPageIndex());

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		return PDFPage.calculateBoxAndMaybeWriteText(leftinmm + mmleftmargin, 0, rightinmm, text, false, null,
				PDFPage.TEXTTYPE_SECTION_HEADER).getHeight();
	}

	@Override
	public boolean breakableSection() {
		return false;
	}

	@Override
	public PartialPrintFeedback printPartial(PDFPageBand pageband, float spaceleft, PDFPage currentpage,
			float mmfromtopforsection, float leftinmm, float rightinmm) throws IOException {
		throw new RuntimeException("Not implemented as section is not breakable.");
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

	@Override
	public String getLabel() {
		return text;
	}

	@Override
	public int getPageNumber() {

		return currentpage.getPageIndex();

	}

	@Override
	public float getOriginalOffset() {
		return this.mmleftmargin;
	}
}
