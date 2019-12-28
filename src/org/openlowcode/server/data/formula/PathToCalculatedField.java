/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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

/**
 * A step in the path on a multi-object formula
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> origin object
 * @param <F> next step in the navigation
 * @param <G> final step in the navigation
 */
public class PathToCalculatedField<E extends DataObject<E>, F extends DataObject<F>, G extends DataObject<G>> {
	public boolean local() {
		return false;
	}

	private ObjectNavigator<F, G> navigator;
	private PathToCalculatedField<E, ?, F> nextpathelement;

	/**
	 * @param navigator navigator to reach final step in the navigation (to go from F to G)
	 * @param nextpathelement the next element (to go from E to F)
	 */
	public PathToCalculatedField(ObjectNavigator<F, G> navigator, PathToCalculatedField<E, ?, F> nextpathelement) {
		if (!(this instanceof LocalPath))
			if (navigator == null)
				throw new RuntimeException("Navigator is null for non local path to calculated field");
		this.navigator = navigator;
		this.nextpathelement = nextpathelement;
	}

	/**
	 * navigates from the source to the final object
	 * @param source source object (E)
	 * @return all the target objects
	 */
	public ArrayList<G> navigatetosourceobject(E source) {
		return navigatetosourceobjectwithbreaker(source, 0);
	}

	protected ArrayList<G> navigatetosourceobjectwithbreaker(E source, int breaker) {
		if (breaker > 1024)
			throw new RuntimeException("Breaker exception on recursive algorithm for source " + source.dropToString());
		if (nextpathelement == null) {
			// if next path element is null F is equal to E
			@SuppressWarnings("unchecked")
			F originobjectcasted = (F) source;
			ArrayList<G> resultarray = new ArrayList<G>();
			if (navigator == null)
				throw new RuntimeException("navigator is null for element " + this.getClass().getName()
						+ " next path element = " + nextpathelement);
			G[] thisresult = navigator.navigate(originobjectcasted);
			if (thisresult != null)
				for (int j = 0; j < thisresult.length; j++)
					resultarray.add(thisresult[j]);
			return resultarray;

		} else {
			ArrayList<G> resultarray = new ArrayList<G>();
			ArrayList<F> intermediateresult = nextpathelement.navigatetosourceobjectwithbreaker(source, breaker + 1);
			if (intermediateresult != null)
				for (int i = 0; i < intermediateresult.size(); i++) {
					G[] thisresult = navigator.navigate(intermediateresult.get(i));
					if (thisresult != null)
						for (int j = 0; j < thisresult.length; j++)
							resultarray.add(thisresult[j]);
				}
			return resultarray;
		}
	}
}
