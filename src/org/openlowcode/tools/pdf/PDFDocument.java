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
import java.io.OutputStream;
import java.util.ArrayList;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A document to be printed in PDF. This document is made of PDF Parts
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class PDFDocument {
	private ArrayList<PDFPart> documentparts;
	private int documentpagenumber = -1;

	/**
	 * @return the number of parts in the PDF Document
	 */
	int getPartsNumber() {
		return documentparts.size();
	}

	PDFPart getPart(int index) {
		return documentparts.get(index);
	}

	/**
	 * Adds a PDFPart to be printed in order (first parts added are printed first in
	 * the document)
	 * 
	 * @param newpart
	 */
	public void addPDFPart(PDFPart newpart) {
		documentparts.add(newpart);
		newpart.setParentPDFDocument(this);
	}

	/**
	 * This method returns the total number of pages of the document. It should only
	 * be called during final printing. You should use it in the
	 * PDFPage.drawCalculatedText
	 * 
	 * @return
	 * 
	 */
	public int getDocumentPageNumber() {
		if (this.documentpagenumber == -1)
			throw new RuntimeException(
					"This method should only be called in final print. You may want to use the method PDFPage.drawCalculatedText.");
		return this.documentpagenumber;
	}

	public void PrintAndSave(OutputStream outputstream) throws IOException {
		PDDocument document = new PDDocument();

		for (int i = 0; i < documentparts.size(); i++) {
			documentparts.get(i).initialize();
		}

		int pagesbefore = 0;
		for (int i = 0; i < documentparts.size(); i++) {
			PDFPart thispart = documentparts.get(i);
			thispart.layoutPages(pagesbefore);
			pagesbefore += thispart.getPageNumber();
		}
		this.documentpagenumber = pagesbefore;
		for (int i = 0; i < documentparts.size(); i++) {
			documentparts.get(i).print(document);
			pagesbefore += documentparts.get(i).getPageNumber();
		}
		document.save(outputstream);
		document.close();
	}

	public PDFDocument() {
		documentparts = new ArrayList<PDFPart>();
	}
}
