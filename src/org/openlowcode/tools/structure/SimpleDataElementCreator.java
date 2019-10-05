/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

/**
 * an interface to create a simple data element.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the simple data element type
 * @param <F> the payload format
 */
public interface SimpleDataElementCreator<E extends SimpleDataElt, F extends Object> {
	public E getBlankDataElt();

	public void setPayload(E dataelt, F payload);

}
