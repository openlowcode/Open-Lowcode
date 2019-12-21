/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic;

import java.util.logging.Logger;

import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * The significant path on a page is a way to make pages robust to changes of
 * layout. It allows to define an element as providing a significant path for
 * page data and business logic
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SPageSignifPath extends Named {
	private NamedList<SPageSignifPath> childrenitems;
	private SPageSignifPath parentpath;
	private static Logger logger = Logger.getLogger(SPageSignifPath.class.getName());

	private void addChildItem(SPageSignifPath child, SPageNode[] widgetpathtoroot) {
		try {
			childrenitems.add(child);
		} catch (RuntimeException e) {
			StringBuffer pathtoroot = new StringBuffer("Widget Path To Root : [");
			for (int i = 0; i < widgetpathtoroot.length; i++) {
				if (i > 0)
					pathtoroot.append(',');
				pathtoroot.append(widgetpathtoroot[i]);
			}
			pathtoroot.append(']');
			throw new RuntimeException(e.getMessage() + " path: " + printPath() + pathtoroot.toString());
		}
	}

	/**
	 * @param page             only needed if parent path not provided
	 * @param parentpath       parent in the path tree
	 * @param widgetpathtoroot the widget path to root for debugging purposes
	 */
	public SPageSignifPath(String name, SPage page, SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		super(name);
		this.parentpath = parentpath;
		childrenitems = new NamedList<SPageSignifPath>();
		logger.fine("Created significant path " + name + "page = " + page + " parentpath = " + parentpath
				+ " called from " + Thread.currentThread().getStackTrace()[2]);
		if (parentpath == null)
			if (page != null)
				page.getRootPath().addChildItem(this, widgetpathtoroot);
		if (parentpath != null) {
			parentpath.addChildItem(this, widgetpathtoroot);
		}
	}

	/**
	 * @return prints the path recursively to this element
	 */
	public String printPath() {
		if (parentpath == null)
			return "";
		return parentpath.printPath() + "/" + this.getName();
	}
}
