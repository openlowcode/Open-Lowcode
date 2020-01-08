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

import java.math.BigDecimal;
import java.util.logging.Logger;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.module.system.data.Binaryfile;
import org.openlowcode.module.system.data.Objattachment;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * This property gives the data object the possibility to store attached file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class Filecontent<E extends DataObject<E> & UniqueidentifiedInterface<E>> extends DataObjectProperty<E> {

	/**
	 * creates a file content property for the given data object
	 * 
	 * @param definition    definition of the file content property
	 * @param parentpayload parent object payload
	 */
	public Filecontent(FilecontentDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
	}

	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Filecontent.class.getName());

	/**
	 * set dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * adds an attachment to the object
	 * 
	 * @param object     object to add attachment to
	 * @param attachment attachment object
	 * @param file       file to store on the attachment object
	 */
	public void addattachment(E object, Objattachment attachment, SFile file) {
		// link attachment to object
		attachment.setlinkedobjectidforowner(object.getId());

		// create binary file
		Binaryfile fileobject = new Binaryfile();
		fileobject.setFilecontent(file);
		fileobject.setFilename(file.getFileName());
		fileobject.setFilesize(new BigDecimal(file.getLength()));
		fileobject.insert();

		// link attachment to binary file and insert it
		DataObjectId<Binaryfile> fileid = fileobject.getId();
		attachment.setparentforcontent(fileid);
		attachment.setlinkedobjectidforowner(object.getId());
		attachment.setFilename(file.getFileName());

		attachment.setFilesize(niceFileSize(file.getLength()));
		attachment.insert();

	}

	/**
	 * a helper method that generates a nice file size (in bytes, KB, MB)
	 * 
	 * @param bytes the file size in bytes
	 * @return a nice string representation (in English)
	 */
	public static String niceFileSize(long bytes) {
		if (bytes >= 1024)
			if (bytes < 1024 * 1024) {
				long kb = bytes / 1024;
				long kbcomma = (bytes % 1024 * 10) / 1024;
				if (kbcomma == 0)
					return "" + kb + " KB";
				if (kbcomma != 0)
					return "" + kb + "." + kbcomma + " KB";
			}
		if (bytes >= 1024 * 1024) {
			long mb = bytes / (1024 * 1024);
			long mbcomma = (bytes % (1024 * 1024) * 10) / (1024 * 1024);
			if (mbcomma == 0)
				return "" + mb + " MB";
			if (mbcomma != 0)
				return "" + mb + "." + mbcomma + " MB";
		}
		return "" + bytes + " B";
	}

	/**
	 * delete the attachment for the object
	 * 
	 * @param object       data object
	 * @param attachmentid id of the attachment to delete
	 */
	public void deleteattachment(E object, DataObjectId<Objattachment> attachmentid) {
		Objattachment attachment = Objattachment.readone(attachmentid);
		Binaryfile file = Binaryfile.readone(attachment.getLinkedtoparentforcontentid());
		attachment.delete();
		file.delete();
	}

	/**
	 * update the object attachment content (binary file)
	 * 
	 * @param object     object to update
	 * @param attachment attachment data object
	 * @param newfile    new file to store on the attachment record
	 */
	public void updateattachment(E object, Objattachment attachment, SFile newfile) {
		if (newfile != null) {
			Binaryfile oldfile = Binaryfile.readone(attachment.getLinkedtoparentforcontentid());
			oldfile.delete();
			Binaryfile fileobject = new Binaryfile();
			fileobject.setFilecontent(newfile);
			fileobject.setFilename(newfile.getFileName());
			fileobject.setFilesize(new BigDecimal(newfile.getLength()));
			fileobject.insert();
			DataObjectId<Binaryfile> newfileid = fileobject.getId();
			attachment.setparentforcontent(newfileid);
			attachment.setFilename(newfile.getFileName());
			attachment.setFilesize(niceFileSize(newfile.getLength()));
			attachment.update();
		}

	}

	public Objattachment[] getattachments(E object, QueryFilter additionalcondition) {
		Objattachment[] allattachments = Objattachment.getallforgenericidforowner(object.getId(), additionalcondition);
		return allattachments;
	}

}
