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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A line grouping criteria on an object node will regroup the objects according
 * to the following criteria, and consolidate values for all objects having the
 * same value for the criteria
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class LineGroupingCriteria {
	/**
	 * @return the object the line criteria applies to
	 */
	public abstract DataObjectDefinition getObject();

	/**
	 * @return true if data gathering is required, typically if objects are grouped
	 *         according to criteria depending from linked objects
	 */
	public abstract boolean hasDataGathering();

	/**
	 * writes the data generic logic
	 * 
	 * @param sg           source generator
	 * @param objectprefix prefix for the object for code generation
	 * @throws IOException if anything bad happens during source generation
	 */
	public abstract void writeDataGathering(SourceGenerator sg, String objectprefix) throws IOException;

	/**
	 * generates the code to extract the information for line grouping criteria from
	 * the data object
	 * 
	 * @param objectprefix object prefix in the smart report node
	 * @return the code generated
	 */
	protected abstract String getExtractorFromobject(String objectprefix);

	/**
	 * generates required import statements
	 * 
	 * @return a list of import statements
	 */
	public abstract String[] getImportStatements();

	/**
	 * generates the classification code
	 * 
	 * @param sg               source generator
	 * @param objectReportNode relevant object report node
	 * @param prefix           prefix of the object node
	 * @param extraindent      space indent to generate nice code
	 * @throws IOException if anything bad happens during the writing
	 */
	protected abstract void writeClassification(
			SourceGenerator sg,
			ObjectReportNode objectReportNode,
			String prefix,
			String extraindent) throws IOException;

	/**
	 * @return true if there should be a back to object at this step
	 */
	public abstract boolean isbacktobject();

	/**
	 * feed the list of fields that will be shown around the main values
	 * 
	 * @param fieldlist list of fields
	 * @param before    if true, feed the list of fields shown before the main
	 *                  value, if false feed the list of fields shown after the main
	 *                  value
	 */
	protected abstract void feedfields(ArrayList<Field> fieldlist, boolean before);

	/**
	 * write the fields in the source code
	 * 
	 * @param sg     source generation
	 * @param prefix prefix for this node
	 * @throws IOException if anything bad
	 */
	protected abstract void writeFields(SourceGenerator sg, String prefix) throws IOException;

	/**
	 * @return true if the grouping element needs to have an array of object ids
	 *         queried before
	 */
	public abstract boolean needArrayOfObjectId();
	
	/**
	 * @return true if additional fields should be processed as part of this line grouping criteria
	 * 
	 */
	public boolean hasAdditionalField() {
		return false;
	}

	/**
	 * gathers extra consolidators for the grouping
	 * 
	 * @param sg source generator for the smart report action
	 * @param parentobject parent data object
	 * @param name name of the report
	 * @return the list of extra consolidators
	 * @throws IOException  if something bad happens generating the file
	 * @since 1.9
	 */
	public ArrayList<String> gatherExtraConsolidatorsForThisGrouping(
			SourceGenerator sg,
			DataObjectDefinition parentobject,
			String name) throws IOException {
		return null;
	}
	
}
