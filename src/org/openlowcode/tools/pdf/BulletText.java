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
import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;

/**
 * A paragraph with a bullet point. 3 levels are managed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class BulletText
		implements
		PDFPageBandSection {
	private String text;
	private static Logger logger = Logger.getLogger(SectionText.class.getName());
	private String remainingtext;
	private int texttype;
	private boolean firstprint;
	private boolean compactprint;

	/**
	 * Create a bullet text with specified layout
	 * 
	 * @param text         Text to display. The text should not have big carriage
	 *                     return (\n), but can have small carriage return (\r)
	 * @param level        an integer from 1 to 3 for the level of bullet
	 * @param compactprint if true, compact display, if false, use normal paragraph
	 *                     spacing around bullets
	 */
	public BulletText(String text, int level, boolean compactprint) {
		this.compactprint = compactprint;
		if (level < 1)
			throw new RuntimeException("level should be between 1 (included) and 3 (included)");
		if (level > 3)
			throw new RuntimeException("level should be between 1 (included) and 3 (included)");

		this.text = text;
		this.remainingtext = text;
		texttype = PDFPage.TEXTTYPE_BULLET_L1;
		if (level == 2)
			texttype = PDFPage.TEXTTYPE_BULLET_L2;
		if (level == 3)
			texttype = PDFPage.TEXTTYPE_BULLET_L3;
		this.firstprint = true;
	}

	/**
	 * Creates a bullet text with normal layout (not compact)
	 * 
	 * @param text  Text to display. The text should not have big carriage return
	 *              (\n), but can have small carriage return (\r)
	 * @param level an integer from 1 to 3 for the level of bullet
	 */
	public BulletText(String text, int level) {
		this(text, level, false);
	}

	@Override
	public void print(
			PDFPageBand pageband,
			PDFPage currentpage,
			float mmfromtopforsection,
			float leftinmm,
			float rightinmm) throws IOException {
		PDFPage.calculateBoxAndMaybeWriteText(leftinmm, mmfromtopforsection, rightinmm, text, true,false, 0, currentpage,
				texttype,false,this.compactprint);

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		return PDFPage.calculateBoxAndMaybeWriteText(leftinmm, 0, rightinmm, text, false, false, 0, null, texttype, false,this.compactprint).getHeight();
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
		float heightforremaining = PDFPage
				.calculateBoxAndMaybeWriteText(leftinmm, 0, rightinmm, remainingtext, false, false, 0, null, texttype,false,this.compactprint)
				.getHeight();
		BoxTextContent feedback = PDFPage.writeAsMuchTextAsPossible(leftinmm, mmfromtopforsection, rightinmm, spaceleft,
				remainingtext, currentpage, texttype, !firstprint,this.compactprint);
		this.remainingtext = feedback.getTextleftout();
		logger.fine("drop remaining textn space left " + spaceleft + "-----------------------------------------------");
		logger.fine("remaining text length = " + (this.remainingtext != null ? this.remainingtext.length() : "NULL"));
		if (feedback.isCutinsideparagraph())
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
		// Do nothing

	}

	@Override
	public void initialize() {
		// do nothing

	}
}
