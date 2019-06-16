/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.samples.pdf;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openlowcode.tools.pdf.BulletText;
import org.openlowcode.tools.pdf.DocumentTableOfContent;
import org.openlowcode.tools.pdf.PDFDocument;
import org.openlowcode.tools.pdf.PDFMultiPageTable;
import org.openlowcode.tools.pdf.PDFPage;
import org.openlowcode.tools.pdf.PDFPage.BoxTextContent;
import org.openlowcode.tools.pdf.PDFPageBand;
import org.openlowcode.tools.pdf.PDFPageBandHeaders;
import org.openlowcode.tools.pdf.ParagraphHeader;
import org.openlowcode.tools.pdf.SectionHeader;
import org.openlowcode.tools.pdf.SectionText;

/**
 * Provides an example of using the Open Lowcode PDF framework to build a
 * multi-page PDF document. This example aims at using all features of the
 * framework except images (as this is more complex to run). This generates a
 * file called Openlowcodepdf-sample.pdf in the root execution folder of the
 * application
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class SampleOpenLowcodePDFDocument {

	public static void main(String[] args) {
		try {
			// first step: declare the document
			PDFDocument document = new PDFDocument();
			// second step: add content
			feedcontent(document);
			// third step: output to file
			File outputfile = new File("Openlowcodepdf-sample.pdf");
			FileOutputStream outputstream = new FileOutputStream(outputfile);

			document.PrintAndSave(outputstream);
			outputstream.close();
			System.out.println("PDF generated at " + outputfile.getAbsolutePath());
		} catch (Exception e) {
			System.err.println("Exception encountered " + e.getClass().getName() + " " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}

	}

	private static void feedcontent(PDFDocument document) throws IOException {
		// declare headers
		PageHeaders header = new PageHeaders();

		// declare a coverpage
		PDFPage coverpage = new PDFPage(true, 15, 10);
		coverpage.addHeader(header);
		coverpage.drawCenteredTextAt(true, coverpage.getPagetop() + 100, 0, "Open Lowcode PDF");
		coverpage.drawCenteredTextAt(false, coverpage.getPagetop() + 130, 0, "automatically generated document");
		document.addPDFPart(coverpage);
		// demonstrate facilities to write text in  box
		BoxTextContent boxsize = coverpage.drawTextInBox(coverpage.getPageLeft(),coverpage.getPagetop()+160, coverpage.getPageLeft()+60,"A real example showing main features of the library (excluding images)");
		float bottomofbox1 = coverpage.drawBoxWithLineNumber(false,coverpage.getPageLeft(),coverpage.getPagetop()+160, coverpage.getPageLeft()+60, boxsize.getNblines(),boxsize.getNbparagraph());
		// second box is put just 5 mm after first box
		BoxTextContent boxsize2 = coverpage.drawTextInBox(coverpage.getPageLeft(),bottomofbox1+5, coverpage.getPageLeft()+60,"All you see is quick to code.");
		coverpage.drawBoxWithLineNumber(false,coverpage.getPageLeft(),bottomofbox1+5, coverpage.getPageLeft()+60, boxsize2.getNblines(),boxsize2.getNbparagraph());

		
		// create document body
		PDFPageBand documentbody = new PDFPageBand(header, true, 15, 10);
		document.addPDFPart(documentbody);

		// title for table of content
		documentbody.printNewSection(new SectionHeader(15, "A - Table of Content"));
		documentbody.printNewSection(new DocumentTableOfContent());
		// paragraph on installation
		documentbody.printNewSection(new SectionHeader(15, "1 - Install Open Lowcode PDF on your project"));
		documentbody.printNewSection(new ParagraphHeader("Prerequisites"));
		documentbody.printNewSection(new SectionText("Before you can use Open Lowcode PDF, you"
				+ " need make sure the following prerequisites are available:"));
		documentbody.printNewSection(
				new BulletText("Install Apache PDF Box jar (please refer to Apache site for details)", 1));
		documentbody.printNewSection(new BulletText(
				"Open Lowcode has been tested on PDFBox 4.0.7, although we expect no problem if you run it on a different version",
				2));
		documentbody.printNewSection(new BulletText(
				"Download and install the JAR from Open Lowcode PDF and make sure it is available in your classpath",
				1));
		documentbody.printNewSection(new BulletText(
				"Ensure you have prepared your favorite soft drink. We recommend also eating slightly-toasted bread with fresh basil, tomatoes, olive oil and garlic while performing programming on Open Lowcode PDF",
				1));
		documentbody.printNewSection(new BulletText("Ensure you have started playing your favourite music", 1));
		documentbody.printNewSection(new BulletText(
				"If someone is watching TV in your house, please ensure that you have your noise-canceling headset switched-on",
				1));
		documentbody
				.printNewSection(new BulletText("Ensure you have a development environment with java 8 or higher", 1));
		documentbody.printNewSection(new BulletText(
				"Open Lowcode PDF was compiled with java 8 Zulu JVM, although it should work with further versions of java.",
				2));

		documentbody.printNewSection(new ParagraphHeader("Write your first document"));
		documentbody.printNewSection(new SectionText(
				"We recommend you start from the SampleOpenLowcodePDFDocument file that provides you an overview of the capabilities of the toolset. Javadoc is also available"));
		documentbody.printNewSection(new SectionHeader(15, "2 - Open Lowcode design principles"));
		documentbody.printNewSection(new SectionText(
				"Open Lowcode PDF was designed to help you generate as quickly as possible standard corporate documents."));

		PDFMultiPageTable detailedtable = new PDFMultiPageTable(new float[] { 25, 55, 20 });

		detailedtable.setHeader("Value", 0);
		detailedtable.setHeader("Rationale", 1);
		detailedtable.setHeader("Since", 2);

		detailedtable.addOneLineContent(new String[] { "Metric System",
				"Open Lowcode PDF has been designed with the metric system", "the beginning" });

		detailedtable.addOneLineContent(new String[] { "Coordinates",
				"Open Lowcode PDF is using the most common type of coordinates system in programming, meaning (0,0) is at the top-left.",
				"the beginning" });

		detailedtable.addOneLineContent(new String[] { "Paper Size",
				"Standard paper size is ISO A4. Other ISO sizes are available. ", "the beginning" });
		detailedtable.addOneLineContent(new String[] { "Letters",
				"Open Low code PDF is using default Helvetica-like font on your system. Font sizes are based on classical layout of corporate documents."
						+ "Objective of the framework is to give good enough default font choices so that you do not have to worry about it.",
				"the beginning" });
		documentbody.printNewSection(detailedtable);
		documentbody.printNewSection(new SectionHeader(15, "3 - Legal stuff"));
		documentbody.printNewSection(new SectionText(
				"This is not legal advice, however, you can use Open Lowcode PDF even in a proprietary software, as long as you are just using the library, not modifying it."));

	}

	private static class PageHeaders implements PDFPageBandHeaders {

		private float topheaderspace = 18; // in mm
		private float bottomheaderspace = 10; // in mm

		@Override
		public void printHeaders(PDFPage currentpage, float leftprintableinmm, float topprintableinmm,
				float rightprintableinmm, float bottomprintableinmm) throws IOException {
			currentpage.drawLine(false, currentpage.getPageLeft(), currentpage.getPagetop() + topheaderspace-5,
					currentpage.getPageRight(), currentpage.getPagetop() + topheaderspace-5, Color.BLACK);
			currentpage.drawFreeFontTextAt(currentpage.getPageRight() - 50, currentpage.getPagetop(), Color.RED, true,
					false, false, 13, "EXAMPLE ONLY");
			
			currentpage.drawBox(true, currentpage.getPageRight() - 53, currentpage.getPagetop() - 1,
					currentpage.getPageRight() - 10, currentpage.getPagetop() + 6, Color.RED);
			currentpage.drawLine(false, currentpage.getPageLeft(), currentpage.getPageBottom() - bottomheaderspace,
					currentpage.getPageRight(), currentpage.getPageBottom() - bottomheaderspace, Color.BLACK);

			currentpage.drawCenteredTextAt(false, currentpage.getPagetop(), 0, "Open Lowcode PDF");
			currentpage.drawCenteredTextAt(false, currentpage.getPagetop(), 1, "Sample file");
			currentpage.drawSimpleTextAt(true, currentpage.getPageLeft(),
					currentpage.getPageBottom() - bottomheaderspace, 0, 0,
					"This document is just an example of the Open Lowcode PDF framework. It may contain inaccurate or obsolete information");
			currentpage.drawCalculatedText(currentpage.getPageRight() - 20,
					currentpage.getPageBottom() - bottomheaderspace, 0, 0, true,
					// important to use calculated text when printing section with page number
					() -> ("page " + currentpage.getPageIndex() + "/"
							+ currentpage.getParent().getDocumentPageNumber()));

		}

		@Override
		public float getTopHeaderSpace() {

			return topheaderspace;
		}

		@Override
		public float getBottomHeaderSpace() {
			return bottomheaderspace;
		}

		@Override
		public float getLeftHeaderSpace() {
			return 0;
		}

		@Override
		public float getRightHeaderSpace() {
			return 0;
		}

	}

}
