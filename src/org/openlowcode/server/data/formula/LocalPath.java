/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.formula;

import java.util.ArrayList;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;

/**
 * A local path is a path for a calculation on the current object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class LocalPath<E extends DataObject<E>> extends PathToCalculatedField<E, E, E> {

	/**
	 * Creates a local path for the provided object
	 * @param definition data object the local path is on
	 */
	public LocalPath(DataObjectDefinition<E> definition) {
		super(null, null);
	}

	@Override
	public boolean local() {
		return true;
	}

	@Override
	public ArrayList<E> navigatetosourceobject(E source) {
		ArrayList<E> result = new ArrayList<E>();
		result.add(source);
		return result;
	}

	@Override
	protected ArrayList<E> navigatetosourceobjectwithbreaker(E source, int breaker) {
		ArrayList<E> result = new ArrayList<E>();
		result.add(source);
		return result;
	}

}
