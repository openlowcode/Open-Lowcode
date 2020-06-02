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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A link between two report nodes. It provides indications to the smart report
 * engine how to navigate from the parent node to the child node
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SmartReportNodeLink
		implements
		FilterItemGenerator {
	private SmartReportNode childnode;

	/**
	 * @return get the data object on which the left object (parent for the report)
	 *         is
	 */
	public abstract DataObjectDefinition getLeftObject();

	/**
	 * create a new smart report link pointing to the child node
	 * 
	 * @param childnode child node for the report link
	 */
	public SmartReportNodeLink(SmartReportNode childnode) {
		this.childnode = childnode;
		if (this.childnode == null)
			throw new RuntimeException("Child Node cannot be null");

	}

	/**
	 * gets the child node of the link for the report
	 * 
	 * @return the child node of the link
	 */
	public SmartReportNode getChildNode() {
		return this.childnode;
	}

	/**
	 * Adds imports that are not present by default in the report, to be implemented
	 * by subclass when relevant
	 * 
	 * @param sg source generator
	 * @throws IOException if there is any communication breakdown
	 * @since 1.8
	 */
	protected void generateImports(SourceGenerator sg) throws IOException{

	}

	/**
	 * writes the data gathering code
	 * 
	 * @param sg                    the source generator to write data gathering
	 *                              code to
	 * @param parent                parent node parent node
	 * @param prefixparent          prefix for parent
	 * @param prefixforlinkandchild prefix for link and child
	 * @param first                 true if first element of the recurive data
	 *                              ordering
	 * @param circuitbreaker        recursive circuit breaker
	 * @param rootobject            root object for the data report
	 * @param reportname            report name
	 * @throws IOException if any error is encountered during the writing of data
	 *                     gathering
	 */
	protected abstract void gatherData(
			SourceGenerator sg,
			SmartReportNode parent,
			String prefixparent,
			String prefixforlinkandchild,
			boolean first,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) throws IOException;

	/**
	 * writes the data ordering code
	 * 
	 * @param sg              the source generator to write data gathering code to
	 * @param smartReportNode parent node
	 * @param prefix          prefix for parent
	 * @param prefixforlink   prefix for link and child
	 * @param first           true if first element of the recursive data ordering
	 * @param circuitbreaker  recursive circuit breaker
	 * @param rootobject      root object for the data report
	 * @param reportname      report name
	 * @throws IOException if any error is encountered during the ordering of data
	 *                     gathering
	 */
	protected abstract void orderData(
			SourceGenerator sg,
			SmartReportNode smartReportNode,
			String prefix,
			String prefixforlink,
			boolean first,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) throws IOException;
}
