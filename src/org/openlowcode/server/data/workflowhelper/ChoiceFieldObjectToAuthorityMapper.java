/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import java.util.HashMap;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A component mapping a choice field to an authority for the workflow
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object of the workflow
 * @param <F> choice definition of the choice field used to map to the authority
 */
public class ChoiceFieldObjectToAuthorityMapper<E extends DataObject<E>, F extends FieldChoiceDefinition<F>>
		extends ObjectToAuthorityMapper<E> {

	private DataObjectId<Authority> defaultauthority;
	private ChoiceFieldExtractor<E, F> choicefieldextractor;
	private HashMap<ChoiceValue<F>, DataObjectId<Authority>> specificmappings;

	/**
	 * creates an authority mapper specifying the default authority
	 * 
	 * @param defaultauthority     default authority if not mapping is created
	 * @param choicefieldextractor the extractor to get the paylaod in the choice
	 *                             field of the object
	 */
	public ChoiceFieldObjectToAuthorityMapper(DataObjectId<Authority> defaultauthority,
			ChoiceFieldExtractor<E, F> choicefieldextractor) {
		this.defaultauthority = defaultauthority;
		this.choicefieldextractor = choicefieldextractor;
		this.specificmappings = new HashMap<ChoiceValue<F>, DataObjectId<Authority>>();
	}

	/**
	 * adds a specific mapping from a choice value to an authority. For choice
	 * values for which no specific mapping exists, the default authority will be
	 * used
	 * 
	 * @param choicevalue choice value
	 * @param authority   authority to put on objects having the given choice value.
	 */
	public void addSpecificMapping(ChoiceValue<F> choicevalue, DataObjectId<Authority> authority) {
		this.specificmappings.put(choicevalue, authority);
	}

	@Override
	public DataObjectId<Authority> getAuthority(E object) {
		ChoiceValue<F> value = choicefieldextractor.extractChoiceValue(object);
		DataObjectId<Authority> specificauthority = specificmappings.get(value);
		if (specificauthority != null)
			return specificauthority;
		return defaultauthority;
	}

}
