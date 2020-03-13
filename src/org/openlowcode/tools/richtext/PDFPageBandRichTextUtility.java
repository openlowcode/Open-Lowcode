/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
import org.openlowcode.tools.pdf.PDFPageBand;
import org.openlowcode.tools.pdf.PDFPageBandSection;
import org.openlowcode.tools.pdf.ParagraphHeader;
import org.openlowcode.tools.pdf.SectionText;

/**
 * An utility to print in a page band a rich text content
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PDFPageBandRichTextUtility {
	
	/**
	 * Inserts in the page band the rich text content
	 * 
	 * @param pageband page band to print into
	 * @param sourcetext source text formatted as rich text
	 * @throws IOException
	 */
	public static void insertRichTextInPageBand(PDFPageBand pageband,String sourcetext) throws IOException {
		PDFPageBandSection[] sectionsforrichtext = PDFPageBandRichTextUtility.generateSectionFromRichText(sourcetext);
		if (sectionsforrichtext!=null) for (int i=0;i<sectionsforrichtext.length;i++) 
			pageband.printNewSection(sectionsforrichtext[i]);
	}
	
	/**
	 * Parses the rich text and generates section for printing
	 * @param sourcetext source text formatted in rich text
	 * @return list of page band sections
	 */
	public static PDFPageBandSection[] generateSectionFromRichText(String sourcetext) {
		ArrayList<RichTextSection> structuredtext = RichTextParser.simplifyforblack(RichTextParser.parseText(sourcetext));
		return parseSections(structuredtext).toArray(new PDFPageBandSection[0]);
	}
	
	
	private static ArrayList<PDFPageBandSection> parseSections(ArrayList<RichTextSection> structuredtext) {
		ArrayList<PDFPageBandSection> sections = new ArrayList<PDFPageBandSection>();
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
		return sections;
	}
}

