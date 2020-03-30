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
import java.util.logging.Logger;

import org.openlowcode.tools.pdf.BulletText;
import org.openlowcode.tools.pdf.FormattableSectionText;
import org.openlowcode.tools.pdf.PDFPageBand;
import org.openlowcode.tools.pdf.PDFPageBandSection;
import org.openlowcode.tools.pdf.ParagraphHeader;

/**
 * An utility to print in a page band a rich text content
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PDFPageBandRichTextUtility {
	private static Logger logger = Logger.getLogger(PDFPageBandRichTextUtility.class.getName());
	/**
	 * Inserts in the page band the rich text content
	 * 
	 * @param pageband   page band to print into
	 * @param sourcetext source text formatted as rich text
	 * @param simplifyinblack if true, will simplify the text, removing bold, italic and color
	 * @throws IOException
	 */
	public static void insertRichTextInPageBand(PDFPageBand pageband, String sourcetext, boolean simplifyinblack)
			throws IOException {
		if (simplifyinblack) {
			PDFPageBandSection[] sectionsforrichtext = PDFPageBandRichTextUtility
					.generateBlackSectionFromRichText(sourcetext);
			if (sectionsforrichtext != null)
				for (int i = 0; i < sectionsforrichtext.length; i++)
					pageband.printNewSection(sectionsforrichtext[i]);
		} else {
			ArrayList<RichTextSection> parsedtext = RichTextParser.parseText(sourcetext);
			logger.finest("Text parsed with sections = "+parsedtext.size());
			ArrayList<PDFPageBandSection> sections = parseSections(parsedtext);
			logger.finest("Printing sections without simplify in black, sections = "+sections.size());
			for (int i=0;i<sections.size();i++) {
				pageband.printNewSection(sections.get(i));
			}
			
		}
	}
	/**
	 * Inserts in the page band the rich text content
	 * 
	 * @param pageband   page band to print into
	 * @param sourcetext source text formatted as rich text
	 * @throws IOException
	 */
	public static void insertRichTextInPageBand(PDFPageBand pageband, String sourcetext) throws IOException {
		insertRichTextInPageBand(pageband,sourcetext,false);
	}
	
	/**
	 * Parses the rich text and generates section for printing
	 * 
	 * @param sourcetext source text formatted in rich text
	 * @return list of page band sections
	 */
	public static PDFPageBandSection[] generateBlackSectionFromRichText(String sourcetext) {
		ArrayList<
				RichTextSection> structuredtext = RichTextParser.simplifyforblack(RichTextParser.parseText(sourcetext));
		return parseSections(structuredtext).toArray(new PDFPageBandSection[0]);
	}

	
	
	private static ArrayList<PDFPageBandSection> parseSections(ArrayList<RichTextSection> structuredtext) {
		ArrayList<PDFPageBandSection> sections = new ArrayList<PDFPageBandSection>();
		ArrayList<RichTextSection> accumulatednormaltext = new ArrayList<RichTextSection>();

		for (int i = 0; i < structuredtext.size(); i++) {
			RichTextSection thistextsection = structuredtext.get(i);
			if (thistextsection.isNormalText()) {
	
				accumulatednormaltext.add(thistextsection);
			}

			if (thistextsection.isBullet() || thistextsection.isSectiontitle()) {
				if (accumulatednormaltext.size() > 0) {
					// generate the FormattableSectionText
					sections.add(generateFormattableSectionText(accumulatednormaltext));
					// empty the buffer
					accumulatednormaltext = new ArrayList<RichTextSection>();
				}
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
		if (accumulatednormaltext.size() > 0) 
			sections.add(generateFormattableSectionText(accumulatednormaltext));
		
		return sections;
	}

	/**
	 * @param accumulatednormaltext a list of rich text sections that are neither
	 *                              bullet nor title
	 * @return the FormattableSectionText generated from list of normal texts
	 */
	public static FormattableSectionText generateFormattableSectionText(
			ArrayList<RichTextSection> accumulatednormaltext) {
		FormattableSectionText sectiontext = new FormattableSectionText(false);
		if (accumulatednormaltext != null)
			for (int i = 0; i < accumulatednormaltext.size(); i++) {
				RichTextSection currentsection = accumulatednormaltext.get(i);
				if (currentsection.isBullet())
					throw new RuntimeException("Error, section is bullet, only normal text authorized here");
				if (currentsection.isSectiontitle())
					throw new RuntimeException("Error, section is title, only normal text authorized here");
				sectiontext.addFormattedText(new FormattableSectionText.FormattedText(currentsection.isItalic(),
						currentsection.isBold(), currentsection.getSpecialcolor(), currentsection.getText()));
			}
		return sectiontext;
	}

}
