/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.specificstorage;

import java.math.BigDecimal;

import org.openlowcode.server.data.storage.Field;

/**
 * a transient big decimal is a transient field holding a BigDecimal payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TransientBigDecimal
		extends
		TransientFieldSchema<BigDecimal> {

	/**
	 * creates a transient big decimal field with the provided name
	 * 
	 * @param name unique name for the transient big decimal field
	 */
	public TransientBigDecimal(String name) {
		super(name);

	}

	@Override
	public BigDecimal defaultValue() {
		return null;
	}

	@Override
	public BigDecimal castToType(Object o) {
		return (BigDecimal) o;
	}

	@Override
	public Field<BigDecimal> initBlankField() {
		return null;
	}

}
