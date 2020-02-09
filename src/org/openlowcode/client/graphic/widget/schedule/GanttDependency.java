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

/**
 * A dependency between two tasks in the Gantt chart
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of GanntTask dependencies are put on
 */
class GanttDependency<E extends GanttTask<E>> {

	@SuppressWarnings("unused")
	private GanttPlanning<E> parent;
	private E predecessor;
	private E successor;

	/**
	 * @return the predecessor
	 */
	public E getPredecessor() {
		return predecessor;
	}

	/**
	 * @return the successor
	 */
	public E getSuccessor() {
		return successor;
	}

	/**
	 * creates a dependency
	 * 
	 * @param ganttPlanning parent planning
	 * @param predecessor   predecessor task
	 * @param successor     successor task
	 */
	public GanttDependency(GanttPlanning<E> ganttPlanning, E predecessor, E successor) {
		super();
		parent = ganttPlanning;
		this.predecessor = predecessor;
		this.successor = successor;
	}

}