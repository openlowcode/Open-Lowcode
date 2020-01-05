/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Objattachment;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.tools.pdf.PDFDocument;

/**
 * an abstract framework for print-out generator. Sub-classes should be
 * implemented with the name specified by the Open Lowcode framework
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public abstract class PrintOutGenerator<E extends DataObject<E> & FilecontentInterface<E>> {
	private static Logger logger = Logger.getLogger(PrintOutGenerator.class.getName());

	/**
	 * generates the content for the given data object in the document provided
	 * 
	 * @param object   the father object of the printout
	 * @param document Apache document used for the print
	 * @throws IOException if any error happens writing the file
	 */
	public abstract void generateContent(E object, PDFDocument document) throws IOException;

	/**
	 * wrapper that creates the binary file around the core generate content method
	 * 
	 * @param object object to process
	 * @param label  label of the file
	 * @return a binary file
	 */
	public SFile generateContent(E object, String label) {
		try {
			logger.fine("starting generating pdf document with generator " + this.getClass().toString() + " for object "
					+ object.getName() + " id =" + object.getId());
			PDFDocument document = new PDFDocument();
			generateContent(object, document);
			ByteArrayOutputStream documentinmemory = new ByteArrayOutputStream();
			logger.fine("generated document of size " + documentinmemory.size());
			document.PrintAndSave(documentinmemory);
			SFile generatedfile = new SFile(label + ".pdf", documentinmemory.toByteArray());
			return generatedfile;

		} catch (IOException e) {
			logger.severe(" ---- Original IO Exception in generating content " + e.getMessage());
			int minithreaddumpindex = (e.getStackTrace().length < 5 ? e.getStackTrace().length : 5);
			for (int i = 0; i < minithreaddumpindex; i++)
				logger.severe("       - " + e.getStackTrace()[i]);
			throw new RuntimeException("Filesystem error in generating pdfcontent " + e.getMessage());
		}
	}

	/**
	 * adds the print-out content as file attachment to the object
	 * 
	 * @param object data object
	 * @param label  label of the file
	 */
	public void addContentAsAttachment(E object, String label) {
		try {
			logger.fine("starting generating pdf document with generator " + this.getClass().toString() + " for object "
					+ object.getName() + " id =" + object.getId());
			PDFDocument document = new PDFDocument();
			generateContent(object, document);
			ByteArrayOutputStream documentinmemory = new ByteArrayOutputStream();
			logger.fine("generated document of size " + documentinmemory.size());
			document.PrintAndSave(documentinmemory);
			Objattachment[] attachments = object.getattachments(null);
			for (int i = 0; i < attachments.length; i++) {
				Objattachment thisattachment = attachments[i];
				if (thisattachment.getComment().equals(label)) {
					object.deleteattachment(thisattachment.getId());
				}
			}

			Objattachment newattachment = new Objattachment();
			newattachment.setComment(label);
			SFile generatedfile = new SFile(label + ".pdf", documentinmemory.toByteArray());
			object.addattachment(newattachment, generatedfile);

		} catch (IOException e) {
			throw new RuntimeException("Filesystem error in generating pdfcontent " + e.getMessage());
		}
	}
}
