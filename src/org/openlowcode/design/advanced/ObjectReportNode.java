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
import java.util.List;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A node on an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectReportNode
		extends
		SmartReportNode
		implements
		FilterItemGenerator {
	private DataObjectDefinition object;
	private ArrayList<LineGroupingCriteria> groupingcriterias;
	private ArrayList<FilterElement<?>> filterelement;
	private ArrayList<CalculationElement> calculationelement;

	/**
	 * creates an object report node for the given data object
	 * 
	 * @param object data object to create the node for
	 */
	public ObjectReportNode(DataObjectDefinition object) {
		super();
		this.object = object;
		this.groupingcriterias = new ArrayList<LineGroupingCriteria>();
		this.filterelement = new ArrayList<FilterElement<?>>();
		this.calculationelement = new ArrayList<CalculationElement>();
	}

	/**
	 * adding a grouping criteria to show lines in the report below. This creates an
	 * intermediate level in the report tree
	 * 
	 * @param a criteria line grouping criteria to be added
	 */
	public void addGroupingCriteria(LineGroupingCriteria criteria) {
		if (criteria == null)
			throw new RuntimeException("A criteria cannot be null");
		if (object != criteria.getObject())
			throw new RuntimeException("Inconsistent LineGroupingCriteria addition on object " + object.getName()
					+ ", criteria is for " + criteria.getObject());
		this.groupingcriterias.add(criteria);
	}

	/**
	 * adds a filter element on the object report node. This is a criteria to filter
	 * data to show on the report, based on data entered by the user
	 * 
	 * @param element filter element to add
	 */
	public void addFilterElement(FilterElement<?> element) {
		if (element == null)
			throw new RuntimeException("element cannot be null!");
		if (element.getParent() != object)
			throw new RuntimeException("Objects not consistent for filter criteria, filter object = "
					+ (element.getParent() != null ? element.getParent().getName() : "NULL") + ", node object = "
					+ (object != null ? object.getName() : "NULL"));
		this.filterelement.add(element);
	}

	/**
	 * adds a calculation element. This is data present on the object node that will
	 * be used to calculate the value to show
	 * 
	 * @param element calculation element to be added
	 */
	public void addCalculationElement(CalculationElement element) {
		if (element == null)
			throw new RuntimeException("element cannot be null!");
		if (element.getParent() != object)
			throw new RuntimeException("Objects not consistent for filter criteria, filter object = "
					+ (element.getParent() != null ? element.getParent().getName() : "NULL") + ", node object = "
					+ (object != null ? object.getName() : "NULL"));
		this.calculationelement.add(element);
	}

	@Override
	public DataObjectDefinition getRelevantObject() {
		return object;
	}

	@Override
	public List<FilterElement<?>> getFilterelement() {
		return this.filterelement;
	}

	@Override
	public List<LineGroupingCriteria> getLineGroupingCriteria() {
		return groupingcriterias;
	}

	@Override
	public String toString() {

		return object.getName();
	}

	@Override
	public void printImportsForAction(SourceGenerator sg) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		sg.wl("import " + object.getOwnermodule().getPath() + ".data." + objectclass + ";");
		sg.wl("import " + object.getOwnermodule().getPath() + ".data." + objectclass + "Definition;");

	}

	@Override
	public void setColumnsForNode(
			SourceGenerator sg,
			DataObjectDefinition rootobject,
			String reportname,
			String prefix,
			int circuitbreaker) throws IOException {
		ColumnCriteria columncriteria = this.getColumnCriteria();
		if (columncriteria != null) {
			String objectvariable = StringFormatter.formatForAttribute(object.getName());
			sg.wl("		String[] columns_" + objectvariable + "_step" + prefix
					+ " = SmartReportUtility.getColumnValues(" + objectvariable + "_step" + prefix + ", ("
					+ columncriteria.generateExtractor() + "),"
					+ (columncriteria.getSuffix() == null ? "null" : "\"" + columncriteria.getSuffix() + "\"") + ");");
			sg.wl("		columnlist.addColumns(columns_" + objectvariable + "_step" + prefix + ");");
		}

	}

	@Override
	protected void buildReportTreeForNode(
			SourceGenerator sg,
			SmartReportNode parentnode,
			String prefixforparent,
			String prefix,
			SmartReport smartReport,
			int level) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		String objectvariable = StringFormatter.formatForAttribute(object.getName());
		String reportvariablename = StringFormatter.formatForAttribute(smartReport.getName());
		boolean isdataback = false;
		for (int i = 0; i < this.groupingcriterias.size(); i++)
			if (this.groupingcriterias.get(i).isbacktobject())
				isdataback = true;
		String parent = "parentid"; // if level = 1
		if (level > 1)
			parent = "this" + StringFormatter.formatForAttribute(parentnode.getRelevantObject().getName()) + "step"
					+ prefixforparent + ".getId()";
		StringBuffer extraindentbuffer = new StringBuffer();
		for (int i = 1; i < level; i++)
			extraindentbuffer.append("	");
		String extraindent = extraindentbuffer.toString();
		sg.wl(extraindent + "			List<" + objectclass + "> " + objectvariable + "s_step" + prefix + " = "
				+ objectvariable + "_step" + prefix + "_map.getObjectsForRootParentId(" + parent + ");");
		sg.wl(extraindent + "			if (" + objectvariable + "s_step" + prefix + "!=null) for (int index" + prefix
				+ "=0;index" + prefix + "<" + objectvariable + "s_step" + prefix + ".size();index" + prefix + "++) {");
		sg.wl(extraindent + "				" + objectclass + " this" + objectvariable + "step" + prefix + " = "
				+ objectvariable + "s_step" + prefix + ".get(index" + prefix + ");");
		String parentmultiplier = "rootmultiplier";
		if (level > 1)
			parentmultiplier = "step" + prefixforparent + "multiplier";

		sg.wl(extraindent + " 			BigDecimal step" + prefix + "multiplier = " + parentmultiplier + ";");
		for (int i = 0; i < this.calculationelement.size(); i++) {
			CalculationElement thiscalculationelement = this.calculationelement.get(i);
			thiscalculationelement.writeMultiplier(sg, extraindent, prefix);
		}
		String parentclassification = "rootclassification";
		if (level > 1)
			parentclassification = "step" + prefixforparent + "classification";
		sg.wl(extraindent + "				ArrayList<String> step" + prefix + "classification = new ArrayList<String>("
				+ parentclassification + ");");
		for (int i = 0; i < groupingcriterias.size(); i++) {
			LineGroupingCriteria thisgroupingcriteria = groupingcriterias.get(i);
			thisgroupingcriteria.writeClassification(sg, this, prefix, extraindent);
		}
		ColumnCriteria columncriteria = this.getColumnCriteria();
		if (columncriteria != null)
			columncriteria.writeColumnValueGenerator(sg, this, prefix);
		MainReportValue mainreportvalue = this.getMainReportValue();
		if ((mainreportvalue != null) || (isdataback)) {
			sg.wl(extraindent + " 			Reportfor" + reportvariablename + " newreportitem" + prefix
					+ " = new Reportfor" + reportvariablename + "();");
			sg.wl(extraindent + "				newreportitem" + prefix
					+ ".setDynamicHelperForFlexibledecimalfields(dynamichelper);");
			if (isdataback) {
				sg.wl(extraindent + "				newreportitem" + prefix + ".setparentforparentforclick(this"
						+ objectvariable + "step" + prefix + ".getId());");
			}

			for (int i = 0; i < groupingcriterias.size(); i++) {
				LineGroupingCriteria criteria = groupingcriterias.get(i);
				criteria.writeFields(sg, prefix);
			}

			if (mainreportvalue != null) {

				sg.wl(extraindent + "				newreportitem" + prefix
						+ ".addflexibledecimalvalue(columnvalue,ReportTree.multiplyIfNotNull("
						+ mainreportvalue.printExtractor(" this" + objectvariable + "step" + prefix) + ",step" + prefix
						+ "multiplier));");
				if (mainreportvalue.getTotalIndex() >= 0)
					sg.wl(extraindent + "				newreportitem" + prefix + ".setTotal"
							+ mainreportvalue.getTotalIndex() + "(ReportTree.sumIfNotNull(newreportitem" + prefix
							+ ".getTotal" + mainreportvalue.getTotalIndex() + "(),ReportTree.multiplyIfNotNull("
							+ mainreportvalue.printExtractor(" this" + objectvariable + "step" + prefix) + ",step"
							+ prefix + "multiplier)));");
			}
			sg.wl(extraindent + "				reporttree.addNode(step" + prefix
					+ "classification.toArray(new String[0]),newreportitem" + prefix + ");			");
		}

	}

	@Override
	public DataObjectDefinition getBackToObjet(int circuitbreaker) {
		if (circuitbreaker > 1024)
			throw new RuntimeException("CircuitBreaker is reached for object report node " + this.object.getName());
		DataObjectDefinition returnvalue = null;
		for (int i = 0; i < this.linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = this.linktochildrennode.get(i);
			DataObjectDefinition thisbacktoobject = thislink.getChildNode().getBackToObjet(circuitbreaker + 1);
			if (thisbacktoobject != null) {
				if (returnvalue != null)
					throw new RuntimeException("several back to objects defined for object " + returnvalue.getName()
							+ " and object " + thisbacktoobject.getName());
				returnvalue = thisbacktoobject;
			}
		}
		for (int i = 0; i < this.groupingcriterias.size(); i++) {
			LineGroupingCriteria thisgroupingcriteria = this.groupingcriterias.get(i);
			if (thisgroupingcriteria.isbacktobject()) {
				if (returnvalue != null)
					throw new RuntimeException("several back to objects defined for object " + returnvalue.getName()
							+ " and object " + this.object.getName());
				returnvalue = this.object;
			}
		}
		return returnvalue;
	}

	@Override
	protected void collectFieldsToAdd(ArrayList<Field> fieldstoadd, boolean before) {

		for (int i = 0; i < groupingcriterias.size(); i++) {
			LineGroupingCriteria thiscriteria = groupingcriterias.get(i);
			thiscriteria.feedfields(fieldstoadd, before);
		}

	}

}
