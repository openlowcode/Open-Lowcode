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

import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.storage.DefaultValueGenerator;

/**
 * A default value generator used when the property Lifecycle is added when data
 * objects are already existing.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LifecycleDefaultValueGenerator implements DefaultValueGenerator<String> {
	private TransitionFieldChoiceDefinition<?> lifecyclevalues;

	/**
	 * Creates a default value generator for the lifecycle
	 * 
	 * @param lifecyclevalues the transition choice definition for the lifecycle
	 */
	public LifecycleDefaultValueGenerator(TransitionFieldChoiceDefinition<?> lifecyclevalues) {
		this.lifecyclevalues = lifecyclevalues;
	}

	@Override
	public String generateDefaultvalue() {
		return lifecyclevalues.getDefaultChoice().getStorageCode();
	}

}
