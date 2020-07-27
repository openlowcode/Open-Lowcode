/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.multichild;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.tools.misc.StandardUtil;

/**
 * A helper managing one dimension of the multi-dimension child property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of child object
 * @param <F> payload of the field
 * @param <G> type of the parent object (or any other object to be used)
 */
public abstract class MultichildValueHelper<E extends DataObject<E>, F extends Object, G extends DataObject<G>> {
	
	private BiConsumer<E, F> setter;
	private Function<E, F> getter;

	
	public MultichildValueHelper(BiConsumer<E,F> setter,Function<E,F> getter) {
		this.setter = setter;
		this.getter = getter;
	}
	
	/**
	 * sets the context for the helper with the parent
	 * 
	 * @param parent parent object
	 */
	public abstract void setContext(G parent);
	
	/**
	 * sets the payload of the field on the child object
	 * 
	 * @param object  a child object
	 * @param payload the payload to set on the object
	 */
	public void set(E object, F payload) {
		setter.accept(object, payload);
	}

	/**
	 * gets the payload of the field on the child object
	 * 
	 * @param object the child object
	 * @return the extracted payload for the field
	 */
	public F get(E object) {
		return getter.apply(object);
	}

	/**
	 * gets the minimum values that have to exist for this field in the list of
	 * children
	 * 
	 * @return the minimum values required for this field
	 */
	public abstract F[] getMinimumvalues();

	/**
	 * gets the maximum values that have to exist for this field on the list of
	 * children. Users can choose which values would work
	 * 
	 * @return the maximum number of values required for this field
	 */

	public abstract F[] getMaximumvalues();

	/**
	 * @return true if free values are allowed
	 */
	public abstract boolean allowUserValue();

	/**
	 * @return true if other values present in legacy data (for a new version) are
	 *         to be kept. Else, they are consolidated if
	 */
	public abstract boolean allowothervalues();

	/**
	 * @return the value to consolidate other values in. Example, if you want to
	 *         have always time data from a 5 years time window, you may have a
	 *         value 'Before' that will consolidate values from previous year when
	 *         you get previous year data
	 */
	public abstract F getDefaultValueForOtherData();

	/**
	 * This method checks if the current value can be kept or an alternative should
	 * be used or the value should be discarded. Updates object passed as argument
	 * with the alternative value if required
	 * 
	 * @return true if value should be kept / consolidated, false if value should be
	 *         discarded
	 */
	public boolean replaceWithDefaultValue(E object) {
		if (allowothervalues())
			return true;
		if (allowUserValue())
			return true;
		F[] maximumvalues = getMaximumvalues();
		if (maximumvalues != null)
			for (int i = 0; i < maximumvalues.length; i++) {
				if (!StandardUtil.compareIncludesNull(maximumvalues[i], get(object)))
					return true;
			}
		F alternative = getDefaultValueForOtherData();
		if (alternative != null) {
			set(object, alternative);
			return true;
		}
		return false;
	}
}
