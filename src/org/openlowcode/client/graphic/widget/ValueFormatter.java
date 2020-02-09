/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import javafx.scene.Node;

/**
 * An interface that creates a javafx node from a given type of data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object processed
 */
public interface ValueFormatter<E> {
	/**
	 * transforms a data object payload into a javafx node
	 * 
	 * @param value data object
	 * @return corresponding java node
	 */
	public Node getWidget(E value);
}
