/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import java.io.IOException;
import java.util.List;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A node link getting all the objects in the database
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AllObjectsNodeLink
		extends
		SmartReportNodeLink {

	/**
	 * Creates a node link gathering all objects of a given type
	 * 
	 * @param childnode the child node corresponding to the objects to select
	 */
	public AllObjectsNodeLink(SmartReportNode childnode) {
		super(childnode);

	}

	@Override
	public DataObjectDefinition getLeftObject() {
		return null;
	}

	@Override
	public List<FilterElement<?>> getFilterelement() {
		return null;
	}

	@Override
	public List<LineGroupingCriteria> getLineGroupingCriteria() {
		return null;
	}

	@Override
	protected void gatherData(
			SourceGenerator sg,
			SmartReportNode parent,
			String prefixparent,
			String prefixforlinkandchild,
			boolean firstobjectnode,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) throws IOException {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	protected void orderData(
			SourceGenerator sg,
			SmartReportNode smartReportNode,
			String prefix,
			String prefixforlink,
			boolean first,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) {
		throw new RuntimeException("Not yet implemented");

	}

}
