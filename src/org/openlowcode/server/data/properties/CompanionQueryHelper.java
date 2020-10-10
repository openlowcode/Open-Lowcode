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
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.TwoDataObjects;

/**
 * QueryHelper for an object with companion property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.13
 *
 */
public class CompanionQueryHelper {
	private static CompanionQueryHelper singleton = new CompanionQueryHelper();
	@SuppressWarnings("unused")
	private static final int BATCH_QUERY_SIZE = 20;
	@SuppressWarnings("unused")
	private static final String BLANK_ID = "NEVERLAND";

	/**
	 * @return the singleton query helper
	 */
	public static CompanionQueryHelper get() {
		return singleton;
	}

	public <
			E extends DataObject<E> & CompanionInterface<E, F, G> & HasidInterface<E>,
			F extends DataObject<F> & TypedInterface<F, G>,
			G extends FieldChoiceDefinition<G>> TwoDataObjects<F, E> readtyped(
					DataObjectId<E> companionobjectid,
					DataObjectDefinition<E> companiondefinition,
					DataObjectDefinition<F> maintypedobject,
					CompanionDefinition<E, F, G> companionproperty) {
		E companion = HasidQueryHelper.get().readone(companionobjectid, companiondefinition,
				companionproperty.getHasidDefinition());
		DataObjectId<F> mainobjectid = new DataObjectId<F>(companionobjectid.getId(), maintypedobject);
		F mainobject = HasidQueryHelper.get().readone(mainobjectid, maintypedobject,
				companionproperty.getMainObjectTypedDefinition().getDependentUniqueIdentified().getDependentDefinitionHasid());
		return new TwoDataObjects<F,E>(mainobject,companion);
	}
}
