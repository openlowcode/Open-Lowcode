/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.schedule;

import javafx.scene.input.MouseEvent;

/**
 * An event handler that will perform a defined processing on mouse event on a
 * given gantt task
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of gantt task
 */
@FunctionalInterface
public interface GanttTaskMouseEventHandler<E extends GanttTask<E>> {
	/**
	 * handles the mouse event for the defined gantt task
	 * 
	 * @param event             mouse event
	 * @param selectedgantttask gantt task concerned by the mouse event
	 */
	public void handle(MouseEvent event, E selectedgantttask);
}
