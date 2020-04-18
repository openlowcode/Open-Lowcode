/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.math.BigDecimal;

/**
 * A stored field in the database having a decimal precise payload (not a
 * floating point).
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DecimalStoredField extends StoredFieldSchema<BigDecimal> {
	private int precision;
	private int scale;

	/**
	 * @param name      name of the field in the databse
	 * @param parent    table in the database
	 * @param precision precision in the sense of java BigDecimal (total number of
	 *                  figures in the decimal, e.g. 5 for '332.55'
	 * @param scale     scale in the sense of java BigDecimal (number of figures
	 *                  after the dot, e.g. 2 for '332.55'
	 */
	public DecimalStoredField(String name, StoredTableSchema parent, int precision, int scale) {
		super(name, parent);
		this.precision = precision;
		this.scale = scale;
	}

	/**
	 * @return precision in the sense of java BigDecimal (total number of figures in
	 *         the decimal, e.g. 5 for '332.55'
	 */
	public int getPrecision() {
		return precision;
	}

	/**
	 * @return scale in the sense of java BigDecimal (number of figures after the
	 *         dot, e.g. 2 for '332.55'
	 */
	public int getScale() {
		return scale;
	}

	@Override
	public QueryCondition buildQueryCondition(QueryOperator<BigDecimal> operator, BigDecimal value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(org.openlowcode.server.data.storage.StoredFieldSchema.Visitor visitor) {
		visitor.visit(this);

	}

	@Override
	public BigDecimal defaultValue() {
		return null;
	}

	@Override
	public BigDecimal castToType(Object o) {
		if (o instanceof BigDecimal)
			return (BigDecimal) o;
		if (o == null)
			return null;
		throw new RuntimeException("For object " + (this.getParent() != null ? this.getParent().getName() : "null")
				+ " for field " + this.getName() + ", excepted BigDecimal type, got " + o.getClass().toString());

	}

	@Override
	public Field<BigDecimal> initBlankField() {
		StoredField<BigDecimal> field = new StoredField<BigDecimal>(this);
		field.setPayload(defaultValue());
		return field;
	}

	@Override
	public BigDecimal defaultValueAtColumnCreation() {
		return null;
	}

	@Override
	public <F> F accept(TestVisitor<F> visitor) {
		return visitor.visit(this);
	}

}
