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
 * A thin wrapper class containing a task and its associated display
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class GanttTaskDisplay<E extends GanttTask<E>> {
	private E gantttask;
	private GanttDisplay<E> display;
	
	/**
	 * @return the task
	 */
	public E getGantttask() {
		return gantttask;
	}
	
	/**
	 * @return the display
	 */
	public GanttDisplay <E>getGanttDisplay() {
	
		return this.display;
	}
	
	/**
	 * creates a gantt task display object
	 * 
	 * @param gantttask gantt task
	 * @param display corresponding graphical display
	 */
	public GanttTaskDisplay(E gantttask, GanttDisplay<E> display) {
		super();
		this.gantttask = gantttask;
		this.display = display;
		
	}

	
	
}
