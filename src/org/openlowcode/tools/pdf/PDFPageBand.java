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
import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A PDF Page Band is made of
 * <ul>
 * <li>a constant part (header and footer)</li>
 * <li>a variable part that will be printed on several pages, the application
 * managing page return</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class PDFPageBand extends PDFPart {

	private PDFPageBandHeaders headers;
	private boolean portrait;
	private float leftrightmargin;
	private float topbottommargin;

	private float height;
	private float width;
	private PDFPage currentpage;
	private float currentcursor;

	private float maxindexforcursor;
	private static int MAX_CIRCUITBREAKER = 50;
	private static Logger logger = Logger.getLogger(PDFPageBand.class.getName());
	private ArrayList<PDFPage> pagestoprint;
	private ArrayList<PDFPageBandSection> sections;

	int getSectionNumber() {
		return sections.size();
	}

	PDFPageBandSection getSectionAt(int index) {
		return sections.get(index);
	}

	private void addNewPage(int pagesbefore) throws IOException {
		if (currentpage != null)
			currentpage.closepage();

		currentpage = new PDFPage(portrait, leftrightmargin, topbottommargin, true);
		currentpage.setParentPDFDocument(this.getParent());
		currentpage.layoutPages(pagesbefore);
		this.height = currentpage.getHeight();
		this.width = currentpage.getWidth();
		currentcursor = topbottommargin + headers.getTopHeaderSpace();
		maxindexforcursor = height - topbottommargin - headers.getBottomHeaderSpace();

		pagestoprint.add(currentpage);
	}

	/**
	 * @param headers         the header function that will write any header and
	 *                        footer on the document
	 * @param portrait        true if portrait, false if landscape.
	 * @param leftrightmargin margin on page at left and right in mm
	 * @param topbottommargin margin on page at top and bottom in mm
	 * @throws IOException
	 */
	public PDFPageBand(PDFPageBandHeaders headers, boolean portrait, float leftrightmargin, float topbottommargin)
			throws IOException {
		this.headers = headers;
		this.portrait = portrait;
		this.leftrightmargin = leftrightmargin;
		this.topbottommargin = topbottommargin;

		pagestoprint = new ArrayList<PDFPage>();
		sections = new ArrayList<PDFPageBandSection>();

	}

	@Override
	protected void print(PDDocument document) throws IOException {

		for (int i = 0; i < pagestoprint.size(); i++) {
			if (headers != null)
				this.headers.printHeaders(pagestoprint.get(i), leftrightmargin, topbottommargin,
						this.width - this.leftrightmargin, this.height - topbottommargin);
			pagestoprint.get(i).print(document);
		}

	}

	@Override
	protected int getPageNumber() {

		return pagestoprint.size();
	}

	@Override
	protected void layoutPages(int pagesbefore) throws IOException {
		int internalpagesbefore = pagesbefore;

		addNewPage(internalpagesbefore);
		internalpagesbefore++;
		for (int i = 0; i < sections.size(); i++) {
			PDFPageBandSection sectiontoprint = sections.get(i);
			float heightofsection = sectiontoprint.getSectionHeight(leftrightmargin + this.headers.getLeftHeaderSpace(),
					this.width - leftrightmargin - this.headers.getRightHeaderSpace());

			float maxheightforsection = height - topbottommargin - headers.getBottomHeaderSpace() - topbottommargin
					- headers.getTopHeaderSpace();
			if (heightofsection > maxheightforsection)
				if (sectiontoprint.breakableSection() == false)
					throw new RuntimeException("This sesion is too large for page: height of section = "
							+ heightofsection + ", max height for page = " + maxheightforsection);
			// section not breakable, if does not fit in left space, go to next page
			if (sectiontoprint.breakableSection() == false) {
				if (currentcursor + heightofsection > maxindexforcursor) {
					// create new page and print
					addNewPage(internalpagesbefore);
					internalpagesbefore++;
				}
				sectiontoprint.print(this, currentpage, currentcursor, leftrightmargin + headers.getLeftHeaderSpace(),
						width - leftrightmargin - headers.getRightHeaderSpace());
				currentcursor += heightofsection;
			}
			// section breakable, print by bits if needed
			else {
				boolean fullyprinted = false;
				int circuitbreaker = 0;
				while ((!fullyprinted) && (circuitbreaker < MAX_CIRCUITBREAKER)) {
					PartialPrintFeedback partialreturn = sectiontoprint.printPartial(this,
							maxindexforcursor - currentcursor, currentpage, currentcursor,
							leftrightmargin + headers.getLeftHeaderSpace(),
							width - leftrightmargin - headers.getRightHeaderSpace());
					fullyprinted = true;
					if (partialreturn != null)
						fullyprinted = partialreturn.isFinished();
					if (!fullyprinted) {
						addNewPage(internalpagesbefore);
						internalpagesbefore++;
					} else {
						/// very dirty hack in order not to calculate again the height in case of
						/// RichTextSection
						if (partialreturn != null)
							currentcursor = partialreturn.newcursor;
					}
					circuitbreaker++;
				}
				if (circuitbreaker == MAX_CIRCUITBREAKER) {
					logger.severe("--------------------------------------------------------------");
					logger.severe("Recursive alert circuit breaker on PDFPageBand partial print");
					logger.severe("		class " + sectiontoprint.getClass());
					logger.severe("		content drop " + sectiontoprint.dropContentSample());
				}
			}
		}

	}

	/**
	 * @param sectiontoprint
	 */
	public void printNewSection(PDFPageBandSection sectiontoprint) throws IOException {
		if (sectiontoprint==null) throw new RuntimeException("Adding null section in page band ");
		this.sections.add(sectiontoprint);
	}

	/**
	 * provides the left coordinates of the page band available for content taking
	 * into account page margin and page band header left size
	 * 
	 * @return value in mm with zero on left
	 */
	public float getBandLeft() {
		return this.leftrightmargin + headers.getLeftHeaderSpace();
	}

	/**
	 * provides the left coordinates of the page band available for content taking
	 * into account page margin and page band header right size
	 * 
	 * @return value in mm with zero on left
	 */
	public float getBandRight() {
		return this.width - this.leftrightmargin - headers.getRightHeaderSpace();
	}

	/**
	 * This class provides the feedback on partial print of a PDFPageBandSection.
	 * The new cursor (vertical position after printing) is only returned if the
	 * printing of the section is finished.
	 * 
	 * @author Open Lowcode SAS
	 *
	 */
	public static class PartialPrintFeedback {
		private float newcursor;
		private boolean finished;

		/**
		 * @return the vertical position in mm (top at zero) after the printing of this
		 *         component. Only filled if finished is true.
		 */
		public float getNewcursor() {
			return newcursor;
		}

		/**
		 * @return true if section printing is finished, false is a new page should be
		 *         opened to finish printing the section
		 */
		public boolean isFinished() {
			return finished;
		}

		public PartialPrintFeedback(float newcursor, boolean finished) {

			this.newcursor = newcursor;
			this.finished = finished;
		}

	}

	@Override
	protected void initialize() {
		for (int i = 0; i < this.sections.size(); i++)
			this.sections.get(i).setParentDocument(this.getParent());
		for (int i = 0; i < this.sections.size(); i++)
			this.sections.get(i).initialize();
	}

}
