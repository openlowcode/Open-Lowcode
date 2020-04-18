/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.derbyjdbc;

import java.text.SimpleDateFormat;

import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.LargeBinaryStoredField;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;


/**
 * A visitor to define the fields in the Derby database
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DerbySQLTableFieldDefinition implements StoredFieldSchema.Visitor {
	private static SimpleDateFormat derbytimestamp = new SimpleDateFormat("yyyyMMddHHmmss");
	private StringBuffer buffer;
	
	/**
	 * creates a visitor (can be used only once).
	 * @param buffer the string buffer to feed
	 */
	public DerbySQLTableFieldDefinition(StringBuffer buffer) {
		this.buffer = buffer;
	}
	@Override
	public  void visit(StringStoredField stringfield) {
		buffer.append(" VARCHAR(");
		buffer.append(stringfield.getMaximumLength());
		buffer.append(") ");
		if (stringfield.defaultValueAtColumnCreation()!=null) {
			buffer.append(" DEFAULT '");
			buffer.append(stringfield.defaultValueAtColumnCreation().replace("'", "''"));
			buffer.append("' ");
		}
	}

	
	
	@Override
	public  void visit(TimestampStoredField timestampfield)
			 {
		buffer.append(" TIMESTAMP ");
		if (timestampfield.defaultValueAtColumnCreation()!=null) {
			buffer.append(" DEFAULT TIMESTAMP('");
			buffer.append(derbytimestamp.format(timestampfield.defaultValueAtColumnCreation()));
			buffer.append("')");
		}
		
		
	}

	@Override
	public void visit(DecimalStoredField decimalStoredField) {
		buffer.append(" DECIMAL(");
		buffer.append(decimalStoredField.getPrecision());
		buffer.append(",");
		buffer.append(decimalStoredField.getScale());
		
		buffer.append(") ");
		
	}
	@Override
	public void visit(IntegerStoredField integerStoredField) {
		buffer.append(" INTEGER");
		if (integerStoredField.defaultValueAtColumnCreation()!=null) {
			buffer.append(" DEFAULT ");
			buffer.append(integerStoredField.defaultValueAtColumnCreation().intValue());
			buffer.append(" ");
		
		}
	}
	@Override
	public void visit(LargeBinaryStoredField largebinarystoredfield)  {
		buffer.append(" BLOB ");
		if (largebinarystoredfield.getMaxFileSize()>0) {
			if (largebinarystoredfield.getMaxFileSize()>=1024*1024*2) 
				throw new RuntimeException(" field "+largebinarystoredfield.getName()+" is too long for Derby, max size is 2GB, size specified = "+largebinarystoredfield.getMaxFileSize()+"Kb");
		buffer.append("(");
		buffer.append(largebinarystoredfield.getMaxFileSize());
		buffer.append("K) ");
		
		}
		
	}

}
