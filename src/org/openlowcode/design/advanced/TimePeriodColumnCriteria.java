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

import org.openlowcode.design.data.TimePeriodField;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A column criteria based on time period field on the object holding the main
 * report value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimePeriodColumnCriteria
		extends
		ColumnCriteria {

	private TimePeriodField timeperiodfield;

	/**
	 * Creates a time period column criteria
	 * 
	 * @param node            node holding the same value
	 * @param timeperiodfield the field used as column criteria
	 * @param suffix          suffix used for the node for code genreation
	 */
	public TimePeriodColumnCriteria(SmartReportNode node, TimePeriodField timeperiodfield, String suffix) {
		super(node, suffix);
		this.timeperiodfield = timeperiodfield;
	}
	/**
	 * Creates a time period column criteria
	 * 
	 * @param node            node holding the same value
	 * @param timeperiodfield the field used as column criteria
	 * @param columnindex            index for ordering columns. Columns with same
	 *                               index are ordered together
	 */
	public TimePeriodColumnCriteria(SmartReportNode node, TimePeriodField timeperiodfield, String suffix,int columnindex) {
		super(node, suffix,columnindex);
		this.timeperiodfield = timeperiodfield;
	}
	
	/**
	 * Creates a time period column criteria without suffix specified
	 * 
	 * @param node            node holding the same value
	 * @param timeperiodfield the field used as column criteria
	 */
	public TimePeriodColumnCriteria(SmartReportNode node, TimePeriodField timeperiodfield) {
		super(node);
		this.timeperiodfield = timeperiodfield;
	}

	@Override
	public String generateLabelExtractor() {
		String fieldclassname = StringFormatter.formatForJavaClass(this.timeperiodfield.getName());
		return "(a)->(a.get" + fieldclassname + "().toString())";
	}

	@Override
	public String generatePayloadExtractor() {
		String fieldclassname = StringFormatter.formatForJavaClass(this.timeperiodfield.getName());
		return "(a)->(a.get" + fieldclassname + "())";
	}
	
	@Override
	protected void writeColumnValueGenerator(SourceGenerator sg, ObjectReportNode objectReportNode, String prefix)
			throws IOException {
		String suffixdef = "";
		if (this.getSuffix() != null)
			suffixdef = "+\"" + this.getSuffix() + "\"";
		sg.wl("			String columnvalue = this"
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step" + prefix
				+ ".get" + StringFormatter.formatForJavaClass(timeperiodfield.getName()) + "().toString()" + suffixdef
				+ ";");

	}

	@Override
	public String getColumnPayloadClass() {
		
		return "TimePeriod";
	}
	@Override
	protected String[] getImportStatements() {
		return new String[] {"import org.openlowcode.tools.data.TimePeriod;"};
	}

}
