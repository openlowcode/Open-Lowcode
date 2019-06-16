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

/**
 * A PDFPageBandHeaders is the constant part of a page band, typically the
 * headers and footers
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public interface PDFPageBandHeaders {

	/**
	 * Document parent to the page band
	 * 
	 * @param currentpage         the page on which to print the header
	 * @param leftprintableinmm   coordinate to start printing header on (page size
	 *                            and margin taken into account)
	 * @param topprintableinmm    coordinate to start printing header on (page size
	 *                            and margin taken into account)
	 * @param rightprintableinmm  coordinate to start printing header on (page size
	 *                            and margin taken into account)
	 * @param bottomprintableinmm coordinate to start printing header on (page size
	 *                            and margin taken into account)
	 * @throws IOException
	 */
	public void printHeaders(PDFPage currentpage, float leftprintableinmm, float topprintableinmm,
			float rightprintableinmm, float bottomprintableinmm) throws IOException;

	/**
	 * the header space at top of page in addition to margins declared in the page
	 * band
	 * 
	 * @return the positive value in mm
	 */
	public float getTopHeaderSpace();

	/**
	 * the header space at bottom of page in addition to margins declared in the
	 * page band
	 * 
	 * @return the positive value in mm
	 */
	public float getBottomHeaderSpace();

	/**
	 * the header space at left of page in addition to margins declared in the page
	 * band
	 * 
	 * @return the positive value in mm
	 */
	public float getLeftHeaderSpace();

	/**
	 * the header space at right of page in addition to margins declared in the page
	 * band
	 * 
	 * @return the positive value in mm
	 */
	public float getRightHeaderSpace();

}
