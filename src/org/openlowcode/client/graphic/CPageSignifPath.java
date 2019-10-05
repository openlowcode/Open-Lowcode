/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * 
 * The significant path of a widget inside a page. It is designed so that you
 * can, to some extend, change the layout of a page, and, if the function stays
 * the same, the significant path will not change. <br>
 * A significant path is a tree structure where each widget should have a unique
 * name amongst siblings (children of the same parent). <br>
 * This class is used for nodes and leaves of the tree
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class CPageSignifPath extends Named {
	private NamedList<CPageSignifPath> childrenitems;
	private CPageSignifPath parentpath;
	private CPageNode pagenode;

	/**
	 * @return a compact string with the name = of all children
	 */
	public String traceChildren() {
		StringBuffer childrentrace = new StringBuffer();
		childrentrace.append('[');
		for (int i = 0; i < childrenitems.getSize(); i++) {
			if (i > 0)
				childrentrace.append(',');
			childrentrace.append(childrenitems.get(i).getName());
		}
		childrentrace.append(']');
		return childrentrace.toString();
	}

	/**
	 * Add a child item that should have a unique name for this parent
	 * 
	 * @param child the child path to add in this node
	 */
	public void addChildItem(CPageSignifPath child) {
		try {
			childrenitems.add(child);
		} catch (Exception e) {
			throw new RuntimeException(String.format(
					"Tried to insert a duplicate item %s in significant path, parent = %s, parentchildren = %s ",
					child.getName(), this.getName(), traceChildren()));
		}
	}

	/**
	 * Generates the root node of the significant path for a page;
	 * 
	 * @param page
	 */
	public CPageSignifPath(CPage page) {
		super("");
		this.pagenode = null;
		this.parentpath = null;
		childrenitems = new NamedList<CPageSignifPath>();
	}

	/**
	 * Adds a path element for a page node (widget)
	 * 
	 * @param name       name of the path element
	 * @param page       parent page of the widget
	 * @param parentpath parent path in the page hierarchy
	 * @param pagenode   widget
	 */
	public CPageSignifPath(String name, CPage page, CPageSignifPath parentpath, CPageNode pagenode) {
		super(name);
		this.parentpath = parentpath;
		childrenitems = new NamedList<CPageSignifPath>();
		if (parentpath == null)
			if (page != null)
				page.getRootPath().addChildItem(this);
		if (parentpath != null) {
			parentpath.addChildItem(this);
		}
		this.pagenode = pagenode;
	}

	/**
	 * @return the path in text form. This is a succession of names of elements
	 * separated by character '/' and starting with a '/'
	 */
	public String printPath() {
		if (parentpath == null)
			return this.getName();
		return parentpath.printPath() + "/" + this.getName();
	}

	@Override
	public String toString() {
		return printPath();
	}

	private CPageNode getNodeAtPath(String path,int circuitbreaker) {
		if (circuitbreaker>1024) throw new RuntimeException("Recursive circuit breaker found, path = "+path);
		String nextpathtoken = getPathNextToken(path);
		if (nextpathtoken == null)
			return this.pagenode;
		CPageSignifPath child = childrenitems.lookupOnName(nextpathtoken);
		if (child == null)
			throw new RuntimeException(
					String.format("did not find path token %s at item with path %s", nextpathtoken, printPath()));
		return child.getNodeAtPath(getPathBelow(path),circuitbreaker++);

	}
	/**
	 * a recursive method to parse the path tree and get the page node
	 * at the given path. A circuit breaker is implemented so that potential
	 * recursive structures do not cause the whole JVM to to fail
	 * @param path the path as a string
	 * @return the node
	 */
	public CPageNode getNodeAtPath(String path) {
		return getNodeAtPath(path,0);
		}

	/** 
	 * @param path a path as a string (e.g. /MAINBAND/BUTTONBAND/OKBUTTON)
	 * @return the next element of the path (in example above would be MAINBAND)
	 */
	public static String getPathNextToken(String path) {
		if (path == null)
			return null;
		if (path.trim().length() == 0)
			return null;
		if (path.charAt(0) != '/')
			throw new RuntimeException(
					String.format("invalid path, a path should start by /, path to parse = %s", path));
		if (path.length() == 1)
			throw new RuntimeException(String.format("no content after path separator", path));
		String pathwithoutleadseparator = path.substring(1);
		int nextseparator = pathwithoutleadseparator.indexOf('/');
		if (nextseparator == -1)
			return pathwithoutleadseparator;
		return pathwithoutleadseparator.substring(0, nextseparator);
	}

	/**
	 * utility function to get a path without the head element.
	 * e.g. /MAINBAND/BUTTONBAND/OKBUTTON -> /BUTTONBAND/OKBUTTON
	 * @param path origin path
	 * @return the path without the head element
	 */
	public static String getPathBelow(String path) {
		if (path.trim().length() == 0)
			return null;
		if (path.charAt(0) != '/')
			throw new RuntimeException(String.format("invalid path, a path should start by /", path));
		if (path.length() == 1)
			throw new RuntimeException(String.format("no content after path separator", path));
		String pathwithoutleadseparator = path.substring(1);

		int nextseparator = pathwithoutleadseparator.indexOf('/');
		if (nextseparator == -1)
			return null;
		return pathwithoutleadseparator.substring(nextseparator);

	}
}
