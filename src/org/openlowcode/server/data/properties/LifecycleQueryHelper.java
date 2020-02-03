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

import java.util.ArrayList;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * A utility class to perform queries based on lifecycle
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LifecycleQueryHelper {
	private static LifecycleQueryHelper singleton = new LifecycleQueryHelper();

	/**
	 * @return gets the singleton class (thread safe)
	 */
	public static LifecycleQueryHelper get() {
		return singleton;
	}

	/**
	 * generates a condition with all working (non final) states of the lifecycle
	 * 
	 * @param alias alias to generate condition for
	 * @param parentdefinition parent object
	 * @return the query condition
	 */
	@SuppressWarnings("rawtypes")
	public QueryCondition getAllNonFinalStatesQueryCondition(String alias, DataObjectDefinition parentdefinition) {
		LifecycleDefinition<?, ?> lifecycle = (LifecycleDefinition) parentdefinition.getProperty("LIFECYCLE");
		if (lifecycle == null)
			throw new RuntimeException("Object " + parentdefinition.getName() + " does not have a lifecycle");
		TransitionFieldChoiceDefinition<?> lifecycletransitionchoicedefinition = lifecycle.getLifecycleHelper();
		ChoiceValue<?>[] lifecyclestates = lifecycletransitionchoicedefinition.getChoiceValue();
		ArrayList<ChoiceValue<?>> nonfinalstates = new ArrayList<ChoiceValue<?>>();
		for (int i = 0; i < lifecyclestates.length; i++) {
			ChoiceValue thisvalue = lifecyclestates[i];
			@SuppressWarnings("unchecked")
			boolean finalchoice = lifecycletransitionchoicedefinition.isChoiceFinal(thisvalue);
			if (!finalchoice)
				nonfinalstates.add(thisvalue);
		}
		return getStateSelectionQueryCondition(parentdefinition.getAlias(alias),
				nonfinalstates.toArray(new ChoiceValue[0]), parentdefinition);
	}

	@SuppressWarnings("rawtypes")
	public QueryCondition getStateSelectionQueryCondition(TableAlias alias, ChoiceValue[] selectedvalues,
			DataObjectDefinition parentdefinition) {
		StoredFieldSchema<String> state = new StringStoredField("STATE", null, 64);
		OrQueryCondition statefilter = new OrQueryCondition();
		if (selectedvalues == null)
			throw new RuntimeException("selectedvalues cannot be null");
		if (selectedvalues.length == 0)
			throw new RuntimeException("selectedvalues cannot have zero element");
		for (int i = 0; i < selectedvalues.length; i++) {
			statefilter.addCondition(new SimpleQueryCondition<String>(alias, state, new QueryOperatorEqual<String>(),
					selectedvalues[i].getStorageCode()));
		}
		return statefilter;
	}

}
