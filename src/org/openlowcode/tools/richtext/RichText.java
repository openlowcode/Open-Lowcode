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

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.SplitString;

/**
 * A rich-text is an element of text with formatting on sections of text.
 * Formatting includes the following options (with the codes):
 * <ul>
 * <li>B: bold</li>
 * <li>I: italic</li>
 * <li>CFF0000: color with the color precised in RGB hexa (here full red, no
 * green or blue)</li>
 * <li>P: bullet point</li>
 * <li>T: section title</li>
 * </ul>
 * To enter rich text, you should add tabs between share brackets with the style
 * precised (or nothing if you want to display as standard). There can be
 * several styles. Example: "This is a word with an [BCFF0000]highlighted[]
 * word. If you need to enter opening curly bracket, you have to double it (e.g.
 * "The formula [[x=y] is not correct" will display on screen 'The formula [x=y]
 * is not correct.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class RichText {
	private static Logger logger = Logger.getLogger(RichText.class.getName());
	private ArrayList<RichTextSection> section;

	/**
	 * creates a rich text with the given input
	 * 
	 * @param richtext the rich text as encoded
	 */
	public RichText(String richtext) {
		section = RichTextParser.parseText(richtext);
	}
	
	/**
	 * 
	 * @param sectiontobreak the section to analyze for strong carriage returns and break
	 * @return the splitted sections if split is needed, or the original section if no carriage return
	 * @since 1.5
	 */
	public static RichTextSection[] breakForParagraphs(RichTextSection sectiontobreak) {
		String textbeforebreak = sectiontobreak.getText();
		SplitString splitformajor = new SplitString(textbeforebreak, true);
		if (splitformajor.getNumberOfSections()==1) return new RichTextSection[] {sectiontobreak};
		ArrayList<RichTextSection> paragraphs = new ArrayList<RichTextSection>();
		for (int i=0;i<splitformajor.getNumberOfSections();i++) {
			RichTextSection thissection = new RichTextSection(sectiontobreak);
			thissection.setText(splitformajor.getSplitStringAt(i));
			paragraphs.add(thissection);
		}
		return paragraphs.toArray(new RichTextSection[0]);
	}
	
	/**
	 * generates all paragraphes in this rich text. This method should only be
	 * called on the client with javafx package in the classpath. The rest of rich
	 * text class can be used on the server without javafx
	 * 
	 * @param editable true if editable
	 * @param parent   parent rich text area (widget)
	 * @return a list of paragraphs
	 */
	public Paragraph[] generateAllParagraphs(boolean editable, RichTextArea parent) {

		ArrayList<Paragraph> answer = new ArrayList<Paragraph>();
		boolean lastisstandard = false;
		Paragraph currentparagraph = null;
		for (int i = 0; i < section.size(); i++) {
			RichTextSection thissection = section.get(i);
			RichTextSection[] sectionbrokeninparagraphs = breakForParagraphs(thissection);
			for (int j=0;j<sectionbrokeninparagraphs.length;j++) {
				RichTextSection thisbrokensection = sectionbrokeninparagraphs[j];
			logger.finest("starting formatting of section " + i);
			
			if (j>0) {
				if (currentparagraph.hasSignificantContent())
					answer.add(currentparagraph);
				currentparagraph = new Paragraph(true, editable, parent);
			}
			
			if (!lastisstandard) {
				if (currentparagraph != null)
					if (currentparagraph.hasSignificantContent())
						answer.add(currentparagraph);
				logger.finest("adding paragraph " + i);
				currentparagraph = new Paragraph(true, editable, parent);

			}

			if (!thisbrokensection.isBullet())
				if (!thisbrokensection.isSectiontitle()) {
					lastisstandard = true;

				}
			if (thisbrokensection.isSectiontitle()) {
				lastisstandard = false;
				if (currentparagraph.hasSignificantContent())
					answer.add(currentparagraph);
				currentparagraph = new Paragraph(true, editable, parent);
				currentparagraph.setTitleParagraph();
			}
			if (thisbrokensection.isBullet()) {
				lastisstandard = false;
				lastisstandard = false;
				if (currentparagraph.hasSignificantContent())
					answer.add(currentparagraph);
				currentparagraph = new Paragraph(true, editable, parent);
				currentparagraph.setBulletParagraph();

			}
			FormattedText formattedtext = new FormattedText(thisbrokensection, currentparagraph);
			currentparagraph.addText(formattedtext);
			logger.finest("adding text to paragraph " + i);
			}
		}
		if (currentparagraph != null)
			if (currentparagraph.hasSignificantContent())
				answer.add(currentparagraph);
		return answer.toArray(new Paragraph[0]);
	}

	/**
	 * this method generates a plain text representation of the rich text for
	 * display in places where rich text cannot be displayed
	 * 
	 * @return a plain string representation
	 */
	public String generatePlainString() {
		StringBuffer plaintext = new StringBuffer();
		// if plain section before and then another section is requested, inserts
		// carriage return
		boolean plainsection = false;
		for (int i = 0; i < this.section.size(); i++) {
			RichTextSection currentsection = this.section.get(i);
			if (((currentsection.isBullet()) || (currentsection.isSectiontitle())) && (plainsection == true)) {
				plaintext.append("\n");
			}
			if (currentsection.isBullet())
				plaintext.append(" -");
			if (currentsection.isSectiontitle())
				plaintext.append("  ");
			plaintext.append(currentsection.getText());
			if ((currentsection.isBullet()) || (currentsection.isSectiontitle())) {
				plaintext.append("\n");
				plainsection = false;
			} else {
				plainsection = true;
			}
		}
		return plaintext.toString();
	}

	/**
	 * generates an HTML representation of this rich text
	 * 
	 * @return text in html
	 */
	public String generateHtmlString() {
		StringBuffer htmltext = new StringBuffer();
		boolean plainsection = true;
		for (int i = 0; i < this.section.size(); i++) {
			RichTextSection currentsection = this.section.get(i);

			if (((currentsection.isSectiontitle()) || (currentsection.isBullet())) || (!plainsection))
				htmltext.append("<br style=\"mso-data-placement:same-cell;\" />");
			if (currentsection.isBullet())
				htmltext.append("  - ");
			htmltext.append(escapetoHTML(currentsection.getText()));
			if ((currentsection.isSectiontitle()) || (currentsection.isBullet())) {
				plainsection = false;
			} else {
				plainsection = true;
			}
		}

		return htmltext.toString();
	}

	/**
	 * @param input
	 * @return
	 */
	public static String escapetoHTML(String input) {
		if (input == null)
			return "";
		return input.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("(\\r\\n|\\n|\\r)",
				"<br style=\"mso-data-placement:same-cell;\" />");
	}

}