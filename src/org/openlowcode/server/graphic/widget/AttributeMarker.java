/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.SimpleDataEltType;

/**
 * Attribute Marker is a way to express to an action on a page which field to
 * get on an object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class AttributeMarker<E extends DataObject<E>, F extends SimpleDataEltType> {
	private String fieldname;

	public AttributeMarker(String fieldname) {
		this.fieldname = fieldname;
	}

	/**
	 * a helper method to generate an action data loc for the attribute marker
	 * 
	 * @param node         page node to reference
	 * @param inputdataref input attribute for the action
	 * @return action data location
	 */
	protected SActionDataLoc<F> getDataLoc(SPageNode node, SActionInputDataRef<F> inputdataref) {
		return new SActionDataLoc<F>(node, inputdataref, fieldname);
	}

	/**
	 * 
	 * a helper method to generate an action data loc for the attribute marker as a
	 * one element array. This is to use actions that require an array with a single
	 * element
	 * 
	 * @param node         page node to reference
	 * @param inputdataref input attribute for the action
	 * @return action data location
	 */
	protected SActionDataLoc<ArrayDataEltType<F>> getOneElementArrayDataLoc(SPageNode node,
			SActionInputDataRef<ArrayDataEltType<F>> inputdataref) {
		return new SActionDataLoc<ArrayDataEltType<F>>(node, inputdataref, fieldname);
	}

	/**
	 * a helper method to generate an action data loc for the attribute marker as a
	 * multi-element array.
	 * 
	 * @param node         page node to reference
	 * @param inputdataref input attribute for the action
	 * @return action data location
	 */
	protected SActionDataLoc<ArrayDataEltType<F>> getArrayDataLoc(SPageNode node,
			SActionInputDataRef<ArrayDataEltType<F>> inputdataref) {
		return new SActionDataLoc<ArrayDataEltType<F>>(node, inputdataref, fieldname);
	}
}
