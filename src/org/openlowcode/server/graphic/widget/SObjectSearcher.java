/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.graphic.SPage;

/**
 * a common class to all object searches. An object searcher is a widget able to
 * provide an array of objects as output
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public abstract class SObjectSearcher<E extends DataObject<E>>
		extends
		SComponentBand {

	/**
	 * creates an object searcher
	 * 
	 * @param parentpage  parent page for the widget
	 * @param nameforpath a name unique to the significant parent widget
	 */
	public SObjectSearcher(SPage parentpage, String nameforpath) {
		super(SComponentBand.DIRECTION_DOWN, parentpage, nameforpath);

	}

	/**
	 * gets the widget with the object result, allowing to add specific logic to
	 * your page on what to do with the results
	 * 
	 * @return the array holding the results
	 */
	public abstract SObjectArray<E> getresultarray();

}
