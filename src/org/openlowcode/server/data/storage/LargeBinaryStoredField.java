/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import org.openlowcode.tools.messages.SFile;


/**
 * A stored field to store a large binary file
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LargeBinaryStoredField extends StoredFieldSchema<SFile> {
	private int maxfilesize;
	
	/**
	 * the maximum file size, expressed in kilobytes, zero if the file size is unlimited
	 * @return
	 */
	public int getMaxFileSize() {
		return this.maxfilesize;
	}
	/**
	 * Constructor of the field
	 * @param name name of the field in the persistent storage database
	 * @param parent the table this field is part of 
	 * @param maxfilesize maximum file size, expressed in kilobytes, zero if the file size is unlimited
	 */
	public LargeBinaryStoredField(String name,StoredTableSchema parent,int maxfilesize) {
		super(name,parent);
		this.maxfilesize=maxfilesize;
	}
	/**
	 * Constructor of the field with no maximum size specified
	 * @param name name of the field in the persistent storage database
	 * @param parent the table this field is part of 
	 */
	public LargeBinaryStoredField(String name, StoredTableSchema parent) {
		super(name, parent);
		this.maxfilesize=0;
	}

	@Override
	public QueryCondition buildQueryCondition(QueryOperator<SFile> operator, SFile value) {
		
		return null;
	}

	@Override
	public void accept(org.openlowcode.server.data.storage.StoredFieldSchema.Visitor visitor)  {
		visitor.visit(this);
		
	}

	@Override
	public SFile defaultValueAtColumnCreation() {
		
		return new SFile();
	}

	@Override
	public SFile defaultValue() {
		
		return new SFile();
	}

	@Override
	public SFile castToType(Object o)  {
		return (SFile)o;
	}

	@Override
	public Field<SFile> initBlankField() {
		StoredField<SFile> field = new StoredField<SFile>(this);
		field.setPayload(defaultValue());
		return field;
	}

}
