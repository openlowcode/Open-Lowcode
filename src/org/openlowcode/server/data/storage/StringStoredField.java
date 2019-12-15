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
 * A stored field with a payload is a String
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
public class StringStoredField extends StoredFieldSchema<String> {
	private int maximumlength;
	private String hardcodedvalueatcreation = null;
	private DefaultValueGenerator<String> defaultvaluegenerator = null;

	/**
	 * creates a new string StoredField
	 * 
	 * @param name          name of the field
	 * @param parent        the table schema in which to create the field
	 * @param maximumlength maximum number of characters in the String
	 */
	public StringStoredField(String name, StoredTableSchema parent, int maximumlength) {
		super(name, parent);
		this.maximumlength = maximumlength;
	}

	/**
	 * creates a new string StoredField with a static default value at creation
	 * 
	 * @param name                     name of the field
	 * @param parent                   the table schema in which to create the field
	 * @param maximumlength            maximum number of characters in the String
	 * @param hardcodedvalueatcreation default value at creation
	 */
	public StringStoredField(String name, StoredTableSchema parent, int maximumlength,
			String hardcodedvalueatcreation) {
		super(name, parent);
		this.maximumlength = maximumlength;
		this.hardcodedvalueatcreation = hardcodedvalueatcreation;
	}

	/**
	 * creates a new string StoredField with a smart default value at creation
	 * 
	 * @param name                  name of the field
	 * @param parent                the table schema in which to create the field
	 * @param maximumlength         maximum number of characters in the String
	 * @param defaultvaluegenerator generator for default value
	 */
	public StringStoredField(String name, StoredTableSchema parent, int maximumlength,
			DefaultValueGenerator<String> defaultvaluegenerator) {
		super(name, parent);
		this.maximumlength = maximumlength;
		this.defaultvaluegenerator = defaultvaluegenerator;
	}

	/**
	 * gets the maximum length of the string in the field
	 * 
	 * @return the length of the string stored
	 */
	public int getMaximumLength() {
		return this.maximumlength;
	}

	@Override
	public String castToType(Object o) {
		if (o instanceof String)
			return (String) o;
		if (o == null)
			return null;
		throw new RuntimeException("For object " + (this.getParent() != null ? this.getParent().getName() : "null")
				+ " for field " + this.getName() + ", excepted String type, got " + o.getClass().toString());

	}

	@Override
	public QueryCondition buildQueryCondition(QueryOperator<String> operator, String value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(org.openlowcode.server.data.storage.StoredFieldSchema.Visitor visitor) {
		visitor.visit(this);

	}

	@Override
	public String defaultValue() {

		return "";
	}

	@Override
	public Field<String> initBlankField() {
		StoredField<String> field = new StoredField<String>(this);
		field.setPayload(defaultValue());
		return field;
	}

	@Override
	public String defaultValueAtColumnCreation() {
		if (this.hardcodedvalueatcreation != null)
			return this.hardcodedvalueatcreation;
		if (this.defaultvaluegenerator != null)
			return defaultvaluegenerator.generateDefaultvalue();
		return null;
	}

}
