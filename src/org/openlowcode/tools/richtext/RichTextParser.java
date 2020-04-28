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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * parser for Open Lowcode rich-text encoded as a String
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class RichTextParser {
	private static Logger logger = Logger.getLogger(RichTextParser.class.getName());
	private static char TAG_OPENER = '[';
	private static char TAG_CLOSER = ']';
	private static char TAG_BOLD = 'B';
	private static char TAG_ITALIC = 'I';
	private static char TAG_COLOR = 'C';
	private static char TAG_BULLETPOINT = 'P';
	private static char TAG_SECTIONTITLE = 'T';

	/**
	 * This method collides all normal text sections with color, bold and italic all
	 * put into a black simple text
	 * 
	 * @return a simplified version of the rich text removing colors
	 */
	public static ArrayList<RichTextSection> simplifyforblack(ArrayList<RichTextSection> parsedrichtextwithcolors) {
		ArrayList<RichTextSection> parsedrichtextinblack = new ArrayList<RichTextSection>();
		if (parsedrichtextwithcolors.size() == 0)
			return parsedrichtextinblack;
		RichTextSection lastrichttext = parsedrichtextwithcolors.get(0);
		parsedrichtextinblack.add(lastrichttext);
		for (int i = 1; i < parsedrichtextwithcolors.size(); i++) {
			RichTextSection currenttext = parsedrichtextwithcolors.get(i);
			if ((currenttext.isNormalText()) && (lastrichttext.isNormalText())) {
				// merge
				lastrichttext.setText(lastrichttext.getText() + currenttext.getText());
			} else {
				// does not merge
				lastrichttext = currenttext;
				parsedrichtextinblack.add(currenttext);
			}
		}

		return parsedrichtextinblack;
	}

	/**
	 * parses text encoded in OpenLowcode Rich-text
	 * 
	 * @param richtext text encoding some rich-text
	 * @return a list of rich text sections
	 */
	public static ArrayList<RichTextSection> parseText(String richtext) {
		try {
			ArrayList<RichTextSection> parsedrichtext = new ArrayList<RichTextSection>();
			if (richtext == null)
				richtext = "";
			StringReader reader = new StringReader(richtext);
			StringBuffer currentsectiontext = new StringBuffer();
			RichTextSection currentsection = null;
			logger.finest("--- starting parsing of rich-text " + richtext);
			int character = reader.read();
			while (character != -1) {
				boolean treated=false;
				if (character == TAG_OPENER) {
					character = reader.read();
					// double tag opener. This is escape character
					if (character == TAG_OPENER) {
						currentsectiontext.append(TAG_OPENER);
						if (currentsection == null)
							currentsection = new RichTextSection();
						treated=true;
					}
					// processing real formatting
					else {
						if (currentsection != null) {
							if (currentsectiontext.length() > 0) {
								currentsection.setText(currentsectiontext.toString());
								parsedrichtext.add(currentsection);

							}
						}
						currentsectiontext = new StringBuffer();
						currentsection = new RichTextSection();
						logger.finest("adding new section");

						while (character != TAG_CLOSER) {
							if (character == TAG_BOLD)
								currentsection.setBold();
							if (character == TAG_ITALIC)
								currentsection.setItalic();
							if (character == TAG_BULLETPOINT)
								currentsection.setBullet();
							if (character == TAG_SECTIONTITLE)
								currentsection.setSectiontitle();
							if (character == TAG_COLOR) {
								StringBuffer colorcode = new StringBuffer("#");
								for (int i = 0; i < 6; i++) {
									int colorchar = reader.read();
									colorcode.append((char) colorchar);
								}
								logger.finer("Looking up color " + colorcode.toString());

								currentsection.setSpecialcolor(java.awt.Color.decode(colorcode.toString()));
							}
							character = reader.read();

						}
						treated=true;
					}
				}

				if (!treated) if (character != -1)
					if (character != TAG_OPENER) {
						currentsectiontext.append((char) (character));
						if (currentsection == null)
							currentsection = new RichTextSection();
					}
				character = reader.read();
			}
			// finishing the loop
			if (currentsection != null) {
				if (currentsectiontext.length() > 0) {
					currentsection.setText(currentsectiontext.toString());
					parsedrichtext.add(currentsection);
					logger.finest("adding new section");

				}
			}
			// adding an empty section if nothing
			if (parsedrichtext.size() == 0) {
				currentsection = new RichTextSection();
				currentsection.setText("");
				parsedrichtext.add(currentsection);
				logger.finest("");
			}

			return parsedrichtext;
		} catch (IOException e) {
			logger.severe("Error while parsing rich text" + e.getMessage());
			e.printStackTrace(System.err);
			throw new RuntimeException("error in parsing a rich text " + e.getMessage());
		}

	}
}
