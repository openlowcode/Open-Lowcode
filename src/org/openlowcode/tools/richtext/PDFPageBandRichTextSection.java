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

import org.openlowcode.tools.pdf.BulletText;
import org.openlowcode.tools.pdf.PDFDocument;
import org.openlowcode.tools.pdf.PDFPage;
import org.openlowcode.tools.pdf.PDFPageBand;
import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;
import org.openlowcode.tools.pdf.PDFPageBandSection;
import org.openlowcode.tools.pdf.ParagraphHeader;
import org.openlowcode.tools.pdf.SectionText;

/**
 * A section of text in a multi-page band with potentially rich text content
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PDFPageBandRichTextSection
		implements
		PDFPageBandSection {

	private ArrayList<RichTextSection> structuredtext;
	private ArrayList<PDFPageBandSection> sections;

	/**
	 * creates a rich text section with the provided rich text encoded in a string
	 * 
	 * @param sourcetext rich text encoded in a string
	 */
	public PDFPageBandRichTextSection(String sourcetext) {
		this.structuredtext = RichTextParser.simplifyforblack(RichTextParser.parseText(sourcetext));
		parseSections();
	}

	private void parseSections() {
		this.sections = new ArrayList<PDFPageBandSection>();
		for (int i = 0; i < structuredtext.size(); i++) {
			RichTextSection thistextsection = structuredtext.get(i);
			if (thistextsection.isNormalText()) {
				SectionText thissectiontext = new SectionText(thistextsection.getText());
				sections.add(thissectiontext);
			}
			if (thistextsection.isBullet()) {
				BulletText thisbullettext = new BulletText(thistextsection.getText(), 1);
				sections.add(thisbullettext);
			}
			if (thistextsection.isSectiontitle()) {
				ParagraphHeader header = new ParagraphHeader(thistextsection.getText());
				sections.add(header);
			}
		}
	}

	@Override
	public void print(
			PDFPageBand pageband,
			PDFPage currentpage,
			float mmfromtopforsection,
			float leftinmm,
			float rightinmm) throws IOException {
		for (int i = 0; i < sections.size(); i++) {
			PDFPageBandSection thissection = sections.get(i);
			pageband.printNewSection(thissection);
		}
	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		float cumulatedsectionheight = 0;
		for (int i = 0; i < sections.size(); i++) {
			PDFPageBandSection thissection = sections.get(i);
			cumulatedsectionheight += thissection.getSectionHeight(leftinmm, rightinmm);
		}
		return cumulatedsectionheight;
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
		for (int i = 0; i < sections.size(); i++) {
			PDFPageBandSection thissection = sections.get(i);
			pageband.printNewSection(thissection);

		}
		return null;
	}

	@Override
	public String dropContentSample() {

		return "Rich Text";
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
