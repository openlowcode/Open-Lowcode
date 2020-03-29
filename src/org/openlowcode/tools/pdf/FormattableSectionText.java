/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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
import java.util.logging.Logger;

import org.openlowcode.tools.pdf.PDFPage.BoxTextContent;
import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;


/**
 * A section of text with portions being potentially formatted with color, bold
 * or italic
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.3
 */
public class FormattableSectionText
		implements
		PDFPageBandSection {
	private boolean firstprint;
	private boolean compactprint;
	private ArrayList<FormattedText> sectionstoprint;
	
	private int activesectionindex;
	private String activesectionremainingtext;
	
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(FormattableSectionText.class.getName());
	
	/**
	 * create a formattable section text with provided sections
	 * @param sections sections to add in the formattable section text
	 * @param compactprint if true, margins between paragraphs are reduced
	 */
	public FormattableSectionText(FormattedText[] sections, boolean compactprint) {
		sectionstoprint = new ArrayList<FormattedText>();
		if (sections != null)
			for (int i = 0; i < sections.length; i++)
				sectionstoprint.add(sections[i]);
		this.firstprint = true;
		this.compactprint = compactprint;
		if (sectionstoprint.size()>0) {
			activesectionindex=0;
			activesectionremainingtext = sectionstoprint.get(0).getText();
		}
		
	}

	/**
	 * create a formattable section text with provided sections
	 * @param compactprint if true, margins between paragraphs are reduced
	 */
	public FormattableSectionText(boolean compactprint) {
		sectionstoprint = new ArrayList<FormattedText>();
		this.firstprint = true;
		this.compactprint = compactprint;
	}

	/**
	 * adds a FormattedText
	 * @param texttoadd
	 */
	public void addFormattedText(FormattedText texttoadd) {
		logger.finest("   >> Adding formattted text ");
		this.sectionstoprint.add(texttoadd);
		if (sectionstoprint.size()==1) {
			logger.finest("            >> index 1, adding text to print = "+sectionstoprint.get(0).getText());
			activesectionindex=0;
			activesectionremainingtext = sectionstoprint.get(0).getText();
		}
	}

	@Override
	public void print(
			PDFPageBand pageband,
			PDFPage currentpage,
			float mmfromtopforsection,
			float leftinmm,
			float rightinmm) throws IOException {
		
		BoxTextContent lastfeedback=null;
		for (int i=0;i<sectionstoprint.size();i++) {
			FormattedText text = sectionstoprint.get(i);
			logger.finest("   >>> starting print full , mm offset = "+(lastfeedback!=null?lastfeedback.getMmWrittenOnLastLine():0));
			lastfeedback = PDFPage.calculateBoxAndMaybeWriteText(leftinmm,mmfromtopforsection, rightinmm,text.getText(), true,false, 
					0, currentpage, text.getTextType(), false,this.compactprint,text.getColor(),
					(lastfeedback!=null?lastfeedback.getMmWrittenOnLastLine():0),
					(lastfeedback!=null?lastfeedback.getNblines()-1:0),
					(lastfeedback!=null?lastfeedback.getNbparagraph()-1:0));
			logger.finest("launch feedback "+lastfeedback.getNblines()+" lines, "+lastfeedback.getNbparagraph()+" paragraphs");
		}

	}

	@Override
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException {
		if (sectionstoprint.size()==0) return 0;
		BoxTextContent lastfeedback=null;
		for (int i=0;i<sectionstoprint.size();i++) {
			FormattedText text = sectionstoprint.get(i);
			
			lastfeedback = PDFPage.calculateBoxAndMaybeWriteText(leftinmm,0, rightinmm,text.getText(), false,false, 
					0, null, text.getTextType(), false,this.compactprint,text.getColor(),
					(lastfeedback!=null?lastfeedback.getMmWrittenOnLastLine():0),
					(lastfeedback!=null?lastfeedback.getNblines()-1:0),
					(lastfeedback!=null?lastfeedback.getNbparagraph()-1:0));
			logger.finest("launch feedback "+lastfeedback.getNblines()+" lines, "+lastfeedback.getNbparagraph()+" paragraphs");
		}
		logger.finest(" >>> Finished section, consumed "+lastfeedback.getNblines()+" lines, "+lastfeedback.getNbparagraph()+" paragraphes");
		return lastfeedback.getHeight();		
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
		logger.finest("  >>>> print partial");
		
		
		BoxTextContent lastfeedback = null;
		for (int i=this.activesectionindex;i<this.sectionstoprint.size();i++) {
			FormattedText activetext = this.sectionstoprint.get(i);
			lastfeedback = PDFPage.calculateBoxAndMaybeWriteText(leftinmm,mmfromtopforsection, rightinmm,this.activesectionremainingtext, true,true, 
					spaceleft, currentpage, activetext.getTextType(), !firstprint,this.compactprint,activetext.getColor(),
					(lastfeedback!=null?lastfeedback.getMmWrittenOnLastLine():0),
					(lastfeedback!=null?lastfeedback.getNblines()-1:0),
					(lastfeedback!=null?lastfeedback.getNbparagraph()-1:0));
			logger.finest("    ----> printed one text, nblines = "+lastfeedback.getNblines()+", nbparagraph = "+lastfeedback.getNbparagraph()+", mm = "+lastfeedback.getMmWrittenOnLastLine());
			firstprint=false;
			if (lastfeedback.getTextleftout().length()>0) {
				this.activesectionremainingtext=lastfeedback.getTextleftout();
				return new PartialPrintFeedback(0, false);
			}
			
			if (i<this.sectionstoprint.size()-1) {
				this.activesectionindex++;
				this.activesectionremainingtext=this.sectionstoprint.get(activesectionindex).getText();
			}
		}
		return new PartialPrintFeedback(mmfromtopforsection+lastfeedback.getHeight(),true);
	}

	@Override
	public String dropContentSample() {
		return "FormattableSectionText";
	}

	@Override
	public void setParentDocument(PDFDocument document) {
		// do nothing

	}

	@Override
	public void initialize() {
		// do nothing

	}

	/**
	 * A text with formatting indicators (bold, italic or color)
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 * @since 1.3
	 *
	 */
	public static class FormattedText {
		private boolean italic;
		private boolean bold;
		private Color color;
		private String text;

		/**
		 * creates a formatted text
		 * @param italic if true, italic
		 * @param bold if true, bold
		 * @param color selected color (if null, will be black)
		 * @param text text to print with the given formatting
		 */
		public FormattedText(boolean italic, boolean bold, Color color, String text) {
			super();
			this.italic = italic;
			this.bold = bold;
			this.color = color;
			this.text = text;
		}

		/**
		 * @return generates the text type as defined in  PDFPage
		 */
		public int getTextType() {
			if (italic) if (bold) return PDFPage.TEXTTYPE_PLAIN_BOLD_ITALIC;
			if (italic) return PDFPage.TEXTTYPE_PLAIN_ITALIC;
			if (bold) return PDFPage.TEXTTYPE_PLAIN_BOLD;
			return PDFPage.TEXTTYPE_PLAIN;
			
			
		}

		/**
		 * @return true if italic
		 */
		public boolean isItalic() {
			return italic;
		}

		/**
		 * @return true if bold
		 */
		public boolean isBold() {
			return bold;
		}

		/**
		 * @return true if color
		 */
		public Color getColor() {
			return color;
		}

		/**
		 * @return the text to print
		 */
		public String getText() {
			return text;
		}

	}
}
