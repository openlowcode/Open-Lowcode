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
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * This line grouping criteria will regroup objects that have an identical
 * content in a free text value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LineGroupingCriteriaText
		extends
		LineGroupingCriteria {
	private StringField stringfield;
	private DataObjectDefinition definition;

	/**
	 * creates a new Line grouping criteria for the given string field
	 * 
	 * @param stringfield a string field already assigned to a data object
	 */
	public LineGroupingCriteriaText(StringField stringfield) {
		this.stringfield = stringfield;
		if (this.stringfield == null)
			throw new RuntimeException("Choice Field cannot be null");

		if (this.stringfield.getParentObject() == null)
			throw new RuntimeException(
					"String Field " + stringfield.getName() + " does not have a parent DataObjectDefinition");
		this.definition = this.stringfield.getParentObject();
	}

	@Override
	public DataObjectDefinition getObject() {
		return this.definition;
	}

	@Override
	public boolean hasDataGathering() {

		return false;
	}

	@Override
	public void writeDataGathering(SourceGenerator sg, String objectprefix) {
		throw new RuntimeException("Not implemented");

	}

	@Override
	protected String getExtractorFromobject(String objectprefix) {
		return "(a)->((a.get" + StringFormatter.formatForJavaClass(stringfield.getName()) + "()!=null?a.get"
				+ StringFormatter.formatForJavaClass(stringfield.getName()) + "():\"\"))";
	}

	@Override
	public String[] getImportStatements() {

		return null;
	}

	@Override
	protected void writeClassification(
			SourceGenerator sg,
			ObjectReportNode objectReportNode,
			String prefix,
			String extraindent) throws IOException {
		sg.wl(extraindent + "				step" + prefix + "classification.add((this"
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step" + prefix
				+ ".get" + StringFormatter.formatForJavaClass(this.stringfield.getName()) + "()!=null?this"
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step" + prefix
				+ ".get" + StringFormatter.formatForJavaClass(this.stringfield.getName()) + "():\"\"));");
	}

	@Override
	public boolean isbacktobject() {
		return false;
	}

	@Override
	protected void feedfields(ArrayList<Field> fieldlist, boolean before) {
		// donothing
	}

	@Override
	protected void writeFields(SourceGenerator sg, String prefix) {
		// donothing
	}

	@Override
	public boolean needArrayOfObjectId() {
		return false;
	}
}
