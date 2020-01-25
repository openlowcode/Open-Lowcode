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

import org.openlowcode.design.generation.SourceGenerator;

/**
 * A column criteria allows to classify data depending on the value of the
 * column criteria
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ColumnCriteria {
	private SmartReportNode node;
	private String suffix;

	/**
	 * creates a column criteria for the given smart report node
	 * 
	 * @param node smart report node the column criteria is
	 */
	public ColumnCriteria(SmartReportNode node) {
		if (node == null)
			throw new RuntimeException("Node cannot be null for ColumnCriteria");
		this.node = node;
		this.suffix = null;

	}

	/**
	 * creates a column criteria for the given smart report node
	 * 
	 * @param node   smart report node the column criteria is
	 * @param suffix suffix added to the column title
	 */
	public ColumnCriteria(SmartReportNode node, String suffix) {
		this(node);
		this.suffix = suffix;

	}

	/**
	 * @return the node this column criteria is attached to
	 */
	public SmartReportNode getNode() {
		return this.node;
	}

	/**
	 * @return the suffix added to the column title
	 */
	public String getSuffix() {
		return this.suffix;
	}

	/**
	 * @return the extractor to be used as source file
	 */
	public abstract String generateExtractor();

	/**
	 * writes the column value generator in the smart report code
	 * 
	 * @param sg               source generator
	 * @param objectReportNode main node of the report
	 * @param prefix           prefix of the node
	 * @throws IOException if anything bad happens while generating the code
	 */
	protected abstract void writeColumnValueGenerator(
			SourceGenerator sg,
			ObjectReportNode objectReportNode,
			String prefix) throws IOException;

}
