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
public class Iteratedcompanion<E extends DataObject<E> & HasidInterface<E>>
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
		mnfirstiter = (StoredField<Integer>) this.field.lookupOnName("MNFIRSTITER");
		mnlastiter = (StoredField<Integer>) this.field.lookupOnName("MNLASTITER");
		
	}

	private StoredField<Integer> mnfirstiter;
	private StoredField<Integer> mnlastiter;
	/**
	 * @return the first iteration of the external object for which this object is valid
	 */
	public Integer getFirstiter() {
		return mnfirstiter.getPayload();
	}

	/**
	 * @return the last iteration of the external object for which this object is valid
	 */
	public Integer getLastiter() {
		return mnlastiter.getPayload();
	}

	protected void setFirstiter(Integer payload) {
		this.mnfirstiter.setPayload(payload);
	}
	
	protected void setLastiter(Integer payload) {
		this.mnlastiter.setPayload(payload);
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
		this.mnlastiter.setPayload(new Integer((int) leftobjectolditer));
		parentpayload.insert();
	}
	
	
	/**
	 * massive version of the archive iteration. It is optimized for massive
	 * treatment
	 * 
	 * @param objectbatch       batch of data objects
	 * @param iteratedlinkbatch corresponding batch of iterated link property
	 * @param leftobjectolditer old iterations of the left object for each data
	 *                          object provided
	 */
	public static <E extends DataObject<E> & HasidInterface<E>> void archivethisiteration(
			E[] objectbatch, Iteratedcompanion<E>[] iteratedlinkbatch, Integer[] leftobjectolditer) {
		for (int i = 0; i < objectbatch.length; i++) {
			iteratedlinkbatch[i].mnlastiter.setPayload(leftobjectolditer[i]);
		}
		DataObjectPayload[] payloads = new DataObjectPayload[objectbatch.length];
		for (int i = 0; i < iteratedlinkbatch.length; i++) {
			payloads[i] = iteratedlinkbatch[i].parentpayload;
		}
		DataObjectPayload.massiveinsert(payloads);
	}
}
