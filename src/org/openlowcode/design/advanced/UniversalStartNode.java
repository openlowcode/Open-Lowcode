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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * An universal start node that will not have a specific data context. It can be
 * a start for a report that uses all data in the system (to be completed, github issue #18)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UniversalStartNode
		extends
		SmartReportNode {
	/**
	 * Creates an universal start node
	 */
	public UniversalStartNode() {

	}

	@Override
	public DataObjectDefinition getRelevantObject() {
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
	public void printImportsForAction(SourceGenerator sg) throws IOException {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void setColumnsForNode(
			SourceGenerator sg,
			HashMap<Integer, Integer> columnindexescreated,
			DataObjectDefinition rootobject,
			String reportname,
			String prefix,
			int circuitbreaker) throws IOException {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	protected void buildReportTreeForNode(
			SourceGenerator sg,
			SmartReportNode parentnode,
			String prefixforparent,
			String prefix,
			SmartReport smartReport,
			int level) throws IOException {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public DataObjectDefinition getBackToObjet(int circuitbreaker) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	protected void collectFieldsToAdd(ArrayList<Field> fieldstoaddbefore, boolean before) {
		// nothing to do
	}

}
