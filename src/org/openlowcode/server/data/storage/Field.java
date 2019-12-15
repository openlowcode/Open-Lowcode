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

import java.math.BigDecimal;

import org.openlowcode.tools.misc.Named;


/**
 * This class stored an actual value from the database.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the class of the payload
 */
public abstract class Field<E extends Object> extends Named {
	protected FieldSchema<E> fieldschema;
	/**
	 * contains the last persisted value of the field before the current action.
	 * During persistence, referencepayload is compared with payload to see if formulas and
	 * other triggers should be launched
	 */
	protected E referencepayload;
	
	/**
	 * 
	 */
	protected E payload;
	/**
	 * creates a new field for defined field schema
	 * @param fieldschema the field schema to use for this field
	 */
	public Field(FieldSchema<E> fieldschema) {
		super(fieldschema.getName());
		this.fieldschema = fieldschema;
		this.referencepayload=null;
		this.payload = fieldschema.defaultValue();
	}
	/**
	 * @return the payload of the field
	 */
	public E getPayload() {
		return this.payload;
	}
	/**
	 * @return the field schema for this field
	 */
	public FieldSchema<E> getFieldSchema() {
		return this.fieldschema;
	}
	/**
	 * updates the payload compared to what is stored as a reference in the database persistent storage
	 * @param payload new payload
	 */
	public void setPayload(E payload) {
		this.payload = payload;
	}
	/**
	 * updates the payload and mentions this is the payload in the database persistent storage
	 * @param referencepayload
	 */
	public void setReferencePayload(E referencepayload) {
		this.referencepayload=referencepayload;
		this.payload=referencepayload;
	}
	/**
	 * specifies if the value has been updated compared to reference payload stored in the persistent storage
	 * @return 
	 */
	public boolean updated() {
		if (this.payload==null) if (this.referencepayload==null) return false;
		if (this.payload==null) return true;
		if (this.referencepayload==null) return true;
		if (payload instanceof BigDecimal) {
			BigDecimal decimalpayload = (BigDecimal) payload;
			BigDecimal decimalreferencepayload = (BigDecimal) referencepayload;
			if (decimalpayload.compareTo(decimalreferencepayload)==0) return false;
			
		} else {
			if (this.payload.equals(this.referencepayload)) return false;
					
		}
			
		return true;
	}
}
