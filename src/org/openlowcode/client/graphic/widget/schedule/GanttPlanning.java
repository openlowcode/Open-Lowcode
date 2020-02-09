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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * create a GANTT planning made of tasks and potentially dependencies
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of GANNT task used in the planning
 */
public class GanttPlanning<E extends GanttTask<E>> {
	private ArrayList<String> allowedattributes;
	private HashMap<String, String> allowedattributessetting;
	private ArrayList<E> orderedtasklist;
	private HashMap<Integer, ArrayList<GanttDependency<E>>> dependenciesbypredecessor;
	private HashMap<Integer, ArrayList<GanttDependency<E>>> dependenciesbysuccessor;
	private HashMap<Integer, ArrayList<GanttDependency<E>>> dependenciesbytasksinbetween;
	private HashMap<Integer, E> taskbyindex;
	private ArrayList<GanttDependency<E>> dependencylist;

	/**
	 * creates an empty GANTT planning
	 */
	public GanttPlanning() {
		allowedattributes = new ArrayList<String>();
		allowedattributessetting = new HashMap<String, String>();
		orderedtasklist = new ArrayList<E>();
		dependenciesbypredecessor = new HashMap<Integer, ArrayList<GanttDependency<E>>>();
		dependenciesbysuccessor = new HashMap<Integer, ArrayList<GanttDependency<E>>>();
		dependenciesbytasksinbetween = new HashMap<Integer, ArrayList<GanttDependency<E>>>();
		taskbyindex = new HashMap<Integer, E>();
		dependencylist = new ArrayList<GanttDependency<E>>();
	}

	/**
	 * gets the tasks that are in between the predecessor and the successor of the
	 * dependency
	 * 
	 * @param index index of the dependency
	 * @return all the GANNT tasks that will have the depency arrow go through the
	 *         task horizontal band
	 */
	protected ArrayList<GanttDependency<E>> getDependenciesInBetween(int index) {
		return dependenciesbytasksinbetween.get(new Integer(index));
	}

	/**
	 * @return the list of allowed attributes
	 */
	public String[] getAllowedAttributeList() {
		return allowedattributes.toArray(new String[0]);
	}

	/**
	 * gets all the dependencies having the given task as successor
	 * 
	 * @param index index of a task
	 * @return all dependencies that have this task as successor
	 */
	protected ArrayList<GanttDependency<E>> getDependenciesBySuccessor(int index) {
		return dependenciesbysuccessor.get(index);
	}

	/**
	 * gets all the dependencies having the given task as predecessor
	 * 
	 * @param index index of a task
	 * @return all dependencies that have this task as predecessor
	 */
	protected ArrayList<GanttDependency<E>> getDependenciesByPredecessor(int index) {
		return dependenciesbypredecessor.get(index);
	}

	/**
	 * @return the total number of tasks
	 */
	public int getTaskNr() {
		return orderedtasklist.size();
	}

	/**
	 * gets the task at the given index
	 * 
	 * @param index a number between 0 (included) and getTaskNr() (excluded)
	 * @return the task at the given index
	 */
	public E getTaskAt(int index) {
		return orderedtasklist.get(index);
	}

	/**
	 * checks if an attribute name is valid for a task
	 * 
	 * @param attribute attribute name
	 * @return true if the attribute is valid
	 */
	public boolean isAttributeValid(String attribute) {
		if (allowedattributessetting.containsKey(attribute))
			return true;
		return false;

	}

	/**
	 * adds the name of a valid attribute for the GANNT task
	 * 
	 * @param attribute name of the attributes
	 */
	public void addAttribute(String attribute) {
		allowedattributes.add(attribute);
		allowedattributessetting.put(attribute, attribute);
	}

	/**
	 * adds a list of tasks to the planning
	 * 
	 * @param gantttasks tasks to add
	 */
	public void addTasks(List<E> gantttasks) {
		if (gantttasks == null)
			return;
		for (int i = 0; i < gantttasks.size(); i++)
			addTask(gantttasks.get(i));
	}

	/**
	 * adds a task to the planning
	 * 
	 * @param gantttask task to add
	 */
	public void addTask(E gantttask) {
		int index = orderedtasklist.size();
		orderedtasklist.add(gantttask);
		gantttask.registerParent(this, index);
		taskbyindex.put(new Integer(index), gantttask);
	}

	/**
	 * @return the number of dependencies
	 */
	public int getDependencyNumber() {
		return dependencylist.size();
	}

	/**
	 * get dependency at the given index
	 * 
	 * @param index a number between 0 (included) and getDependencyNumber (excluded)
	 * @return the dependency at the given index
	 */
	public GanttDependency<E> getDependency(int index) {
		return dependencylist.get(index);
	}

	/**
	 * adds an end start dependency between two tasks
	 * 
	 * @param predecessor predecessor task (should be already in the planning)
	 * @param successor   successor task (should be already in the planning)
	 */
	public void addEndStartDependency(E predecessor, E successor) {
		if (predecessor.getParent() != this)
			throw new RuntimeException("Predecessor does not have this planning a parent");
		if (successor.getParent() != this)
			throw new RuntimeException("Successor does not have this planning as parent");
		GanttDependency<E> dependency = new GanttDependency<E>(this, predecessor, successor);
		dependencylist.add(dependency);
		Integer predecessorindex = new Integer(predecessor.getSequenceInPlanning());
		ArrayList<GanttDependency<E>> dependencyforthepredecessor = dependenciesbypredecessor.get(predecessorindex);
		if (dependencyforthepredecessor == null) {
			dependencyforthepredecessor = new ArrayList<GanttDependency<E>>();
			dependenciesbypredecessor.put(predecessorindex, dependencyforthepredecessor);
		}
		dependencyforthepredecessor.add(dependency);

		Integer successorindex = new Integer(successor.getSequenceInPlanning());
		ArrayList<GanttDependency<E>> dependencyforthesuccessor = dependenciesbysuccessor.get(successorindex);
		if (dependencyforthesuccessor == null) {
			dependencyforthesuccessor = new ArrayList<GanttDependency<E>>();
			dependenciesbysuccessor.put(successorindex, dependencyforthesuccessor);
		}
		dependencyforthesuccessor.add(dependency);

		// ----------- add in between on which the task has to be drawn
		int min = predecessorindex.intValue();
		int max = successorindex.intValue();
		if (successorindex.intValue() < predecessorindex.intValue()) {
			max = predecessorindex.intValue();
			min = successorindex.intValue();
		}
		for (int i = min + 1; i < max; i++) {
			Integer index = new Integer(i);
			ArrayList<GanttDependency<E>> inbetweentasks = dependenciesbytasksinbetween.get(index);
			if (inbetweentasks == null) {
				inbetweentasks = new ArrayList<GanttDependency<E>>();
				dependenciesbytasksinbetween.put(index, inbetweentasks);
			}
			inbetweentasks.add(dependency);
		}
	}

}
