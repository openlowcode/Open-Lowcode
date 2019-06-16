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

import org.openlowcode.tools.pdf.PDFPageBand.PartialPrintFeedback;

/**
 * A section is text that will be printed in the appropriate place on the page
 * band in one go
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public interface PDFPageBandSection {

	/**
	 * @param pageband            pageband calling the method. That allows for
	 *                            recursive calling
	 * @param currentpage         the page to print into
	 * 
	 * @param mmfromtopforsection the cursor location to start printing at
	 * @param leftinmm            the index of the left most printable space of the
	 *                            printing section
	 * @param rightinmm           the index of the right most printable space of the
	 *                            printing section
	 * @throws IOException
	 */
	public void print(PDFPageBand pageband, PDFPage currentpage, float mmfromtopforsection, float leftinmm,
			float rightinmm) throws IOException;

	/**
	 * the height of section that needs to be printed
	 * 
	 * @param leftinmm  the index of the left most printable space of the printing
	 *                  section
	 * @param rightinmm the index of the right most printable space of the printing
	 *                  section
	 * @return the positive value in mm
	 * @throws IOException
	 */
	public float getSectionHeight(float leftinmm, float rightinmm) throws IOException;

	/**
	 * This method specifies if the section can be broken (and so printed across
	 * several pages.
	 * 
	 * @return true if section is breakable, false else
	 */
	public boolean breakableSection();

	/**
	 * @param pageband            pageband calling the method. That allows for
	 *                            recursive calling
	 * @param spaceleft           the space (height) in mm
	 * @param currentpage         the page to print into
	 * @param mmfromtopforsection the cursor location to start printing at
	 * @param leftinmm            the index of the left most printable space of the
	 *                            printing section
	 * @param rightinmm           the index of the right most printable space of the
	 *                            printing section
	 * @return a status of if print is finished and the space consumed (new cursor
	 *         is sent back)
	 * @throws IOException
	 */
	public PartialPrintFeedback printPartial(PDFPageBand pageband, float spaceleft, PDFPage currentpage,
			float mmfromtopforsection, float leftinmm, float rightinmm) throws IOException;

	/**
	 * this class drops a content sample for logging purposes.
	 * 
	 * @return a string sample of around 100 characters
	 */
	public String dropContentSample();

	/**
	 * Provides automatically the PDFDocument to the PDFPageBandSection. This is
	 * useful for sections that need full access to the whole document (today, only
	 * the DocumentTableOfContent needs it)
	 * 
	 * @param document the parent document to the PDFPageBand.
	 */
	public void setParentDocument(PDFDocument document);

	/**
	 * This method allows sections to initialize themselves knowning the full
	 * reference of the document before the page number is calculated
	 */
	public void initialize();
}
