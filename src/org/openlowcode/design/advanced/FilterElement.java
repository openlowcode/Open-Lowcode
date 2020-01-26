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

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A filter element will limit data in the report. The filter can be set as
 * hard-coded, or as values provided in the design phase.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the object on which the filter element is set
 */
public abstract class FilterElement<E extends Object> {
	private boolean hardcoded;
	protected boolean fatherisroot;
	protected SmartReportNodeLink linktoparent;

	/**
	 * sets the context of this filter element
	 * 
	 * @param fatherisroot true if the filter is done on a node directly child to
	 *                     the parent. This changes a little bit the generated code
	 * @param linktoparent link from parent to current node
	 */
	public void setContext(boolean fatherisroot, SmartReportNodeLink linktoparent) {
		this.fatherisroot = fatherisroot;
		this.linktoparent = linktoparent;
	}

	/**
	 * Creates a filter element
	 * 
	 * @param hardcoded if true, the filter element will be build by the developer,
	 *                  not by the framework
	 */
	public FilterElement(boolean hardcoded) {
		this.hardcoded = hardcoded;
		this.fatherisroot = false;
	}

	/**
	 * @return true if the filter is hardcoded
	 */
	public boolean isHardCoded() {
		return this.hardcoded;
	}

	/**
	 * @return the parent of the filter (current data object node)
	 */
	public abstract DataObjectDefinition getParent();

	/**
	 * should return an argument content with as base the name of object, the name
	 * of field, and the suffix
	 * 
	 * @param suffix suffix of the current node in the report
	 * @return the argument content
	 */
	public abstract ArgumentContent getArgumentContent(String suffix);

	/**
	 * The filter criteria for the page of the smart report
	 * 
	 * @param sg         source generator for the given file
	 * @param reportname name of the report
	 * @throws IOException if anything bad happens while writing the file
	 */
	public abstract void writeFilterCriteria(SourceGenerator sg, String reportname) throws IOException;

	/**
	 * provides the import classes to be put in the page
	 * 
	 * @return an array of import statements
	 */
	public abstract String[] getImportClasses();

	/**
	 * provides he import classes to be put in the action
	 * 
	 * @param reportname name of the report
	 * @return an array of import statements
	 */
	public abstract String[] getImportClassesForAction(String reportname);

	/**
	 * A filter is considered as 'before' if it is a condition to be put in the
	 * query to get children objects in the navigation
	 * 
	 * @return true if before
	 */
	protected abstract boolean hasfilterbefore();

	/**
	 * A filter is considered as 'after' if it is a filter after the query was
	 * performed
	 * 
	 * @return true if after
	 */
	protected abstract boolean hasfilterafter();

	/**
	 * writes the filter code in the data gathering phase of the report
	 * 
	 * @param sg         source generator
	 * @param stepsuffix step suffix
	 * @param reportroot root object of the report
	 * @param reportname report name
	 * @throws IOException if anything bad happens while writing the file
	 */
	public abstract void writeFilterInDataGathering(
			SourceGenerator sg,
			String stepsuffix,
			DataObjectDefinition reportroot,
			String reportname) throws IOException;

	/**
	 * @return get the blank value for the filter (typically empty object, null
	 *         string...) in java code for code generation
	 */
	protected abstract String getBlankValue();
}
