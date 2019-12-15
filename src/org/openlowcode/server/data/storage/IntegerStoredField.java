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



/**
 * A stored field having as payload an integer
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IntegerStoredField extends StoredFieldSchema<Integer> {
	private Integer hardcodeddefaultvalueatcreation=null;
	/**
	 * Creates an IntegerStoredField without default value
	 * @param name name of the field in the database
	 * @param parent the stored table schema
	 */
	public IntegerStoredField(String name, StoredTableSchema parent) {
		super(name, parent);
	}
	
	/**
	 * Creates an IntegerStoredField without default value
	 * @param name name of the field in the database
	 * @param parent the stored table schema
	 * @param hardcodeddefaultvalueatcreation value set by ddfault
	 */
	public IntegerStoredField(String name, StoredTableSchema parent,Integer hardcodeddefaultvalueatcreation) {
		super(name, parent);
		this.hardcodeddefaultvalueatcreation = hardcodeddefaultvalueatcreation;
	}

	@Override
	public QueryCondition buildQueryCondition(QueryOperator<Integer> operator,
			Integer value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(org.openlowcode.server.data.storage.StoredFieldSchema.Visitor visitor) {
		visitor.visit(this);
		
	}

	@Override
	public Integer defaultValue() {
		return new Integer(0);
	}

	@Override
	public Integer castToType(Object o)  {
		if (o instanceof Integer) return (Integer)o;
		if (o==null) return null;
		throw new RuntimeException("For object "+(this.getParent()!=null?this.getParent().getName():"null")+" for field "+this.getName()+", excepted Integer type, got "+o.getClass().toString());

	}

	@Override
	public Field<Integer> initBlankField() {
		StoredField<Integer> field = new StoredField<Integer>(this);
		field.setPayload(defaultValue());
		return field;
	}

	@Override
	public Integer defaultValueAtColumnCreation() {
		return hardcodeddefaultvalueatcreation;
	}

}
