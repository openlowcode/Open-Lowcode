/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * An iterated companion is a data object belonging to a main object. This
 * includes:
 * <ul>
 * <li>Link objets that belong to the left objet</li>
 * <li>Companion objects for sub-types</li>
 * </ul>
 * The property stores the iterations from the main object for which the
 * companion is valid. <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.HasId}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 */
public class Iteratedcompanion<E extends DataObject<E>>
		extends
		DataObjectProperty<E> {

	private IteratedcompanionDefinition<E> casteddefinition;

	/**
	 * @param definition
	 * @param parentpayload
	 */
	@SuppressWarnings("unchecked")
	public Iteratedcompanion(IteratedcompanionDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.casteddefinition = definition;
		firstiter = (StoredField<Integer>) this.field.lookupOnName(casteddefinition.getPrefix() + "FIRSTITER");
		lastiter = (StoredField<Integer>) this.field.lookupOnName(casteddefinition.getPrefix() + "LASTITER");
		
	}

	private StoredField<Integer> firstiter;
	private StoredField<Integer> lastiter;
	/**
	 * @return the first iteration of the external object for which this object is valid
	 */
	public Integer getFirstiter() {
		return firstiter.getPayload();
	}

	/**
	 * @return the last iteration of the external object for which this object is valid
	 */
	public Integer getLastiter() {
		return lastiter.getPayload();
	}

	/**
	 * archive this iteration
	 * 
	 * @param object            data object
	 * @param leftobjectolditer last iteration for which the link was valid
	 *                          (iteration of the left object before the link
	 *                          update)
	 */
	public void archivethisiteration(E object, long leftobjectolditer) {
		this.lastiter.setPayload(new Integer((int) leftobjectolditer));
		parentpayload.insert();
	}
}
