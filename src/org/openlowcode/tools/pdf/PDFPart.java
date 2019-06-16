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

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A PDF Part is a component of a PDF Document. The PDF Part is first defined in
 * memory, then evaluated for the number of pages, then actually printed.<br>
 * A Part is composed of one or several full pages.
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public abstract class PDFPart {
	private PDFDocument parent;

	/**
	 * @return the parent PDFDocument
	 */
	public PDFDocument getParent() {
		return parent;
	}

	protected void setParentPDFDocument(PDFDocument parent) {
		this.parent = parent;
	}

	/**
	 * This method actually prints the document. It is called after initialize and
	 * layoutPages for the whole document
	 * 
	 * @param document
	 * @throws IOException
	 */
	protected abstract void print(PDDocument document) throws IOException;

	/**
	 * This method should be called at the end of the layout phase or during the
	 * printing.
	 * 
	 * @return
	 */
	protected abstract int getPageNumber();

	/**
	 * This method is called just before the actual printing in order to initialize
	 * widgets that depend from the content of the full document. This is typically
	 * needed for table of content. Please note that at this point, pages may not
	 * have been layout yet
	 */
	protected abstract void initialize();

	/**
	 * This method is called just after initalize and before print. All PDFParts
	 * should perform their page layout at this point. At the end, all pages should
	 * be setup, and the index of each page should be known.
	 * 
	 * @param pagesbefore the number of pages layed-out before.
	 * @throws IOException
	 */
	protected abstract void layoutPages(int pagesbefore) throws IOException;
}
