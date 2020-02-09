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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This is a task of a GANTT. It has a start date and an end date.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of GANTT task
 */
public class GanttTask<E extends GanttTask<E>> {
	private Date starttime;
	private Date endtime;
	private HashMap<String, String> labelvalues;
	private GanttPlanning<E> parent;
	private int sequence;

	public int getSequence() {
		return sequence;
	}

	/**
	 * Create a task with start-time and end-time
	 * 
	 * @param starttime start of the task
	 * @param endtime   end of the task
	 */
	public GanttTask(Date starttime, Date endtime) {

		this.starttime = starttime;
		this.endtime = endtime;
		this.labelvalues = new HashMap<String, String>();

	}

	/**
	 * registers the parent
	 * 
	 * @param planning planning of the task
	 * @param sequence sequence of the task in planning
	 */
	void registerParent(GanttPlanning<E> planning, int sequence) {
		if (this.parent != null)
			throw new RuntimeException("task already has a parent plannning");
		this.parent = planning;
		this.sequence = sequence;
		Iterator<String> attributekeys = labelvalues.keySet().iterator();
		while (attributekeys.hasNext()) {
			String attributename = attributekeys.next();
			if (!parent.isAttributeValid(attributename))
				throw new RuntimeException("Attribute not valid for GANTT planning " + attributename);
		}
	}

	/**
	 * sets an attribute for the task
	 * 
	 * @param attributename  name of the attribute
	 * @param attributevalue value of the attribute
	 */
	public void setAttribute(String attributename, String attributevalue) {
		if (parent != null)
			if (!parent.isAttributeValid(attributename))
				throw new RuntimeException("Attribute not valid for GANTT planning " + attributename);
		labelvalues.put(attributename, attributevalue);
	}

	/**
	 * @return get the parent planning
	 */
	public GanttPlanning<E> getParent() {
		return this.parent;
	}

	/**
	 * @return get the sequence of the task in planning
	 */
	public int getSequenceInPlanning() {
		return this.sequence;
	}

	/**
	 * @return get the start time of the task
	 */
	public Date getStarttime() {
		return starttime;
	}

	/**
	 * @return get the end time of the task
	 */
	public Date getEndtime() {
		return endtime;
	}

	/**
	 * get the attribute value for the specified attribute name
	 * 
	 * @param attributename name of the attribute name
	 * @return the attribute value
	 */
	public String getAttribute(String attributename) {
		return labelvalues.get(attributename);
	}

}
