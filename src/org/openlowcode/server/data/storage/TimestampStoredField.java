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

import java.util.Date;


/**
 * A stored  field having a timestamp (java date....) as payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimestampStoredField extends StoredFieldSchema<Date> {
	private Date defaulttimestamp=null;
	/**
	 * creates a TimestampStoredField
	 * @param name name of the field
	 * @param parent table this field needs to be added in
	 */
	public TimestampStoredField(String name, StoredTableSchema parent) {
		super(name, parent);
	}
	/**
	 * creates a TimestampStoredField with a default value
	 * @param name name of the field
	 * @param parent table this field needs to be added in
	 * @param defaulttimestamp default date to enter
	 */
	public TimestampStoredField(String name, StoredTableSchema parent,Date defaulttimestamp) {
		super(name, parent);
		this.defaulttimestamp=defaulttimestamp;
	}
	

	@Override
	public QueryCondition buildQueryCondition(QueryOperator<Date> operator,
			Date value) {
		return null;
	}

	@Override
	public Date castToType(Object o)  {
		if (o instanceof Date) return (Date) o;
		if (o==null) return null;
		throw new RuntimeException("For object "+(this.getParent()!=null?this.getParent().getName():"null")+" for field "+this.getName()+", excepted Date type, got "+o.getClass().toString());
	}

	@Override
	public void accept(org.openlowcode.server.data.storage.StoredFieldSchema.Visitor visitor) {
		visitor.visit(this);
		
	}

	@Override
	public Date defaultValue() {
		return null;
	}

	@Override
	public Field<Date> initBlankField() {
		StoredField<Date> field = new StoredField<Date>(this);
		field.setPayload(defaultValue());
		return field;
	}

	@Override
	public Date defaultValueAtColumnCreation() {
		return this.defaulttimestamp;
	}
	
}
