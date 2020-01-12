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

import org.openlowcode.tools.messages.SFile;
import org.openlowcode.module.system.data.Objattachment;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * interface that all data objects having file content property implement
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public interface FilecontentInterface<E extends DataObject<E>> extends UniqueidentifiedInterface<E> {
	/**
	 * adds the provided attachment to the object (performs persistence)
	 * 
	 * @param attachment attachment to add to the object (should not be persisted
	 *                   yet)
	 * @param file       binary content
	 */
	public void addattachment(Objattachment attachment, SFile file);

	/**
	 * deletes the attachment
	 * 
	 * @param attachmentid id of the attachment object
	 */
	public void deleteattachment(DataObjectId<Objattachment> attachmentid);

	/**
	 * update the attachment with the new binary content
	 * 
	 * @param attachment attachement to update
	 * @param newfile    new binary content
	 */
	public void updateattachment(Objattachment attachment, SFile newfile);

	/**
	 * gets all the attachments for the object
	 * 
	 * @param additionalcondition additional filter condition
	 * @return all the attachments
	 */
	public Objattachment[] getattachments(QueryFilter additionalcondition);

}
