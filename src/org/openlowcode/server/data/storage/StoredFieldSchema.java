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
 * definition of a field in a table
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class StoredFieldSchema<E extends Object> extends FieldSchema<E> {
	private StoredTableSchema parent;

	/**
	 * the table schema definition for this field
	 * @return this field
	 */
	protected StoredTableSchema getParent() {
		return parent;
	}

	/**
	 * @param name
	 * @param parent
	 */
	public StoredFieldSchema(String name, StoredTableSchema parent) {
		super(name);
		this.parent = parent;
	}

	/**
	 * build a simple query condition with this field
	 * @param operator condition operator '='
	 * @param value value of the query condition
	 * @return
	 */
	public abstract QueryCondition buildQueryCondition(QueryOperator<E> operator, E value);

	/**
	 * A visitor for stored field schema
     * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
     *         SAS</a>
	 *
	 */
	public interface Visitor {
		public void visit(StringStoredField stringfield);

		public void visit(TimestampStoredField timestampfield);

		public void visit(DecimalStoredField decimalStoredField);

		public void visit(IntegerStoredField integerStoredField);

		public void visit(LargeBinaryStoredField largebinarystoredfield);

	}

	/**
	 * gateway for a visitor
	 * @param visitor visitor
	 */
	public abstract void accept(StoredFieldSchema.Visitor visitor);

	/**
	 * returns default value of the field
	 * @return the default value
	 */
	public abstract E defaultValueAtColumnCreation();

	@Override
	public abstract E defaultValue();
	
	@Override
	public abstract E castToType(Object o);
}
