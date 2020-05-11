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

import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * This criteria groups lines which have the same value for the provided choice
 * field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LineGroupingCriteriaChoice
		extends
		LineGroupingCriteria {
	private ChoiceField choicefield;
	private DataObjectDefinition definition;

	/**
	 * Creates a line grouping criteria for the given choice field
	 * 
	 * @param choicefield choice field to be used as grouping criteria
	 */
	public LineGroupingCriteriaChoice(ChoiceField choicefield) {
		this.choicefield = choicefield;
		if (this.choicefield == null)
			throw new RuntimeException("Choice Field cannot be null");

		if (this.choicefield.getParentObject() == null)
			throw new RuntimeException(
					"Choice Field " + choicefield.getName() + " does not have a parent DataObjectDefinition");
		this.definition = this.choicefield.getParentObject();
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
		return "(a)->((a.get" + StringFormatter.formatForJavaClass(choicefield.getName()) + "()!=null?a.get"
				+ StringFormatter.formatForJavaClass(choicefield.getName()) + "().getDisplayValue():\"\"))";
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
				+ ".get" + StringFormatter.formatForJavaClass(this.choicefield.getName()) + "()!=null?this"
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step" + prefix
				+ ".get" + StringFormatter.formatForJavaClass(this.choicefield.getName())
				+ "().getDisplayValue():\"\"));");

	}

	@Override
	public boolean isbacktobject() {
		return false;
	}

	@Override
	protected void feedfields(ArrayList<Field> fieldlist, boolean before) {
	}

	@Override
	protected void writeFields(SourceGenerator sg, String prefix) {

	}

	@Override
	public boolean needArrayOfObjectId() {
		return false;
	}
}
