/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.tools.messages.SFile;
import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * A data object field of a data object where payload is a binary content
 * (typically a file). Files can be big. It is recommended to use data objects
 * with binary content only to store binary, as accessing data objects with
 * binary content to perform processing not involving those binary files is
 * likely to be sub-optimal
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class LargeBinaryDataObjectField<E extends DataObject<E>>
		extends
		DataObjectField<LargeBinaryDataObjectFieldDefinition<E>, E> {

	private StoredField<SFile> largebinaryfield;

	/**
	 * creates a large binary data object field
	 * 
	 * @param definition    definition of the binary object field
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public LargeBinaryDataObjectField(
			LargeBinaryDataObjectFieldDefinition<E> definition,
			DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.largebinaryfield = (StoredField<SFile>) this.field.get(0);
	}

	/**
	 * gets the payload of the field
	 * 
	 * @return a SFile holding the payload of the field
	 */
	public SFile getValue() {

		return largebinaryfield.getPayload();
	}

	/**
	 * sets the payload of the field
	 * 
	 * @param field a binary file
	 */
	public void setValue(SFile field) {
		this.largebinaryfield.setPayload(field);

	}

	@Override
	public SimpleDataElt getDataElement() {
		throw new RuntimeException("Not yet implemented");
	}

}
