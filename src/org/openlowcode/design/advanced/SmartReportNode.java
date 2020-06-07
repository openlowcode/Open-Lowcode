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
import org.openlowcode.tools.misc.StandardUtil;

/**
 * A node in a smart report (a data object)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SmartReportNode
		implements
		FilterItemGenerator {
	public ArrayList<SmartReportNodeLink> linktochildrennode;
	private ColumnCriteria columncriteria;
	private MainReportValue mainreportvalue;

	/**
	 * creates a smart report node
	 */
	public SmartReportNode() {
		this.linktochildrennode = new ArrayList<SmartReportNodeLink>();
	}

	/**
	 * add a child node, providing the link for navigation to the child node of the
	 * report
	 * 
	 * @param linktochild
	 */
	public void addChildNode(SmartReportNodeLink linktochild) {
		if (linktochild == null)
			throw new RuntimeException("Cannot add null SmartReportNodeLink");
		if (!StandardUtil.compareIncludesNull(getRelevantObject(), linktochild.getLeftObject()))
			throw new RuntimeException("Adding inconsisent node link, node object = "
					+ (getRelevantObject() != null ? getRelevantObject().getName() : "Null Object")
					+ ", left object for Node Link ="
					+ (linktochild.getLeftObject() != null ? linktochild.getLeftObject().getName() : "Null Object"));
		this.linktochildrennode.add(linktochild);
	}

	/**
	 * sets a column criteria on this node. This means the report is split into
	 * columns based on information on this node
	 * 
	 * @param columncriteria the column criteria related to this node
	 */
	public void setColumnCriteria(ColumnCriteria columncriteria) {
		this.columncriteria = columncriteria;
	}

	/**
	 * get the column criteria on this node
	 * 
	 * @return the column criteria if it exists on this map
	 */
	public ColumnCriteria getColumnCriteria() {
		return this.columncriteria;
	}

	/**
	 * sets the main report value as an element on this node. The main element value
	 * is the value shown in the main cells of the reports, consolidated as
	 * specified (sum, sum-product, average...)
	 * 
	 * @param mainreportvalue the main report value
	 */
	public void setMainReportValue(MainReportValue mainreportvalue) {
		if (mainreportvalue == null)
			throw new RuntimeException("mainreportvalue cannot be null");
		if (mainreportvalue.getParentObject() != getRelevantObject())
			throw new RuntimeException("Main report value object "
					+ (mainreportvalue.getParentObject() != null ? mainreportvalue.getParentObject().getName() : "NULL")
					+ " is inconsistent with node object "
					+ (getRelevantObject() != null ? getRelevantObject().getName() : "NULL"));
		this.mainreportvalue = mainreportvalue;
	}

	public abstract DataObjectDefinition getRelevantObject();

	/**
	 * Consolidates the list of filter elements across all children of this node.
	 * 
	 * @return a consolidated list of filter element
	 * 
	 */
	public List<FilterElement<?>> getAllFilterElements() {
		return getAllFilterElements(0);
	}

	/**
	 * gets all filter elements on this node and children nodes
	 * 
	 * @param circuitbreaker recursive circuit breakers
	 * @return the list of filter elements
	 */
	private List<FilterElement<?>> getAllFilterElements(int circuitbreaker) {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Recursive loop");
		ArrayList<FilterElement<?>> consolidatedlist = new ArrayList<FilterElement<?>>();
		List<FilterElement<?>> locallist = this.getFilterelement();
		if (locallist != null)
			consolidatedlist.addAll(locallist);
		for (int i = 0; i < linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = linktochildrennode.get(i);
			List<FilterElement<?>> filterelementsforlink = thislink.getFilterelement();
			if (filterelementsforlink != null)
				consolidatedlist.addAll(filterelementsforlink);
			SmartReportNode childnode = thislink.getChildNode();
			List<FilterElement<?>> filterelementsfornode = childnode.getAllFilterElements(circuitbreaker + 1);
			for (int j = 0; j < filterelementsfornode.size(); j++)
				filterelementsfornode.get(j).setContext((circuitbreaker == 0 ? true : false), thislink);
			consolidatedlist.addAll(filterelementsfornode);
		}
		return consolidatedlist;
	}

	/**
	 * writes the data gathering code
	 * 
	 * @param sg         the source generator to write data gathering code to
	 * @param rootobject root object for the data report
	 * @param reportname report name
	 * @throws IOException if any error happens during a data gathering
	 */
	public void gatherData(SourceGenerator sg, DataObjectDefinition rootobject, String reportname) throws IOException {
		gatherData(sg, 0, null, rootobject, reportname);
	}

	/**
	 * writes the data gathering code
	 * 
	 * @param sg             the source generator to write data gathering code to
	 * @param circuitbreaker recursive circuit breaker
	 * @param prefix         prefix for the node
	 * @param rootobject     root object for the data report
	 * @param reportname     report name
	 * @throws IOException if any error happens during a data gathering
	 */
	protected void gatherData(
			SourceGenerator sg,
			int circuitbreaker,
			String prefix,
			DataObjectDefinition rootobject,
			String reportname) throws IOException {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Circuitbreaker for node " + this.getClass() + " - " + this.toString());
		for (int i = 0; i < linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = linktochildrennode.get(i);
			String prefixforlink = (prefix != null ? prefix + "_" + (i + 1) : "" + (i + 1));
			thislink.gatherData(sg, this, prefix, prefixforlink, (prefix == null ? true : false), circuitbreaker,
					rootobject, reportname);
		}
	}

	/**
	 * print imports that are needed for the page
	 * 
	 * @param sg source generator
	 * @throws IOException if any error happens during a data gathering
	 */
	protected void printImports(SourceGenerator sg) throws IOException {
		printImports(sg, 0);
	}

	private void printImports(SourceGenerator sg, int circuitbreaker) throws IOException {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Circuit Breaker for recursive algorithm");
		printImportsForAction(sg);
		for (int i = 0; i < this.linktochildrennode.size(); i++) {
			SmartReportNode childnode = this.linktochildrennode.get(i).getChildNode();
			SmartReportNodeLink childnodelink = this.linktochildrennode.get(i);
			childnodelink.generateImports(sg);
			childnode.printImports(sg, circuitbreaker + 1);
			for (int j = 0; j < childnode.getLineGroupingCriteria().size(); j++) {
				LineGroupingCriteria thisgroupingcriteria = childnode.getLineGroupingCriteria().get(j);
				String[] specificimports = thisgroupingcriteria.getImportStatements();
				if (specificimports != null)
					for (int k = 0; k < specificimports.length; k++)
						sg.wl(specificimports[k]);

			}
			ColumnCriteria thiscolumncriteria = childnode.getColumnCriteria();
			if (thiscolumncriteria != null) {
				String[] specificimportsforcolumn = thiscolumncriteria.getImportStatements();
				if (specificimportsforcolumn != null)
					for (int k = 0; k < specificimportsforcolumn.length; k++)
						sg.wl(specificimportsforcolumn[k]);
			}
		}
	}

	/**
	 * @param sg source generator for the action
	 * @throws IOException if any error happens during a data gathering
	 */
	public abstract void printImportsForAction(SourceGenerator sg) throws IOException;

	/**
	 * This method will write the order of data into compositeobjectmaps according
	 * to the grouping criteria defined
	 * 
	 * @param sg
	 * @param rootobject
	 * @param reportname
	 * 
	 */

	public void orderData(SourceGenerator sg, DataObjectDefinition rootobject, String reportname) throws IOException {
		orderData(sg, rootobject, reportname, null, 0);
	}

	private void orderData(
			SourceGenerator sg,
			DataObjectDefinition rootobject,
			String reportname,
			String prefix,
			int circuitbreaker) throws IOException {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Circuitbreaker for node " + this.getClass() + " - " + this.toString());
		for (int i = 0; i < linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = linktochildrennode.get(i);
			String prefixforlink = (prefix != null ? prefix + "_" + (i + 1) : "" + (i + 1));
			thislink.orderData(sg, this, prefix, prefixforlink, (prefix == null ? true : false), circuitbreaker,
					rootobject, reportname);
			thislink.getChildNode().orderData(sg, rootobject, reportname, prefixforlink, circuitbreaker + 1);
		}
	}

	/**
	 * write the columns in the smart report
	 * 
	 * @param sg                   source generator
	 * @param columnindexescreated already created column indexes
	 * @param parentobject         parent object for the whole report
	 * @param name                 name of the report
	 * @throws IOException if anything has happened while generating the source code
	 */
	public void setColumns(
			SourceGenerator sg,
			HashMap<Integer, Integer> columnindexescreated,
			DataObjectDefinition parentobject,
			String name) throws IOException {
		setColumns(sg, columnindexescreated, parentobject, name, null, 0);
	}

	private void setColumns(
			SourceGenerator sg,
			HashMap<Integer, Integer> columnindexescreated,
			DataObjectDefinition rootobject,
			String reportname,
			String prefix,
			int circuitbreaker) throws IOException {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Circuitbreaker for node " + this.getClass() + " - " + this.toString());
		setColumnsForNode(sg, columnindexescreated, rootobject, reportname, prefix, circuitbreaker);
		for (int i = 0; i < linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = linktochildrennode.get(i);
			String prefixforlink = (prefix != null ? prefix + "_" + (i + 1) : "" + (i + 1));
			thislink.getChildNode().setColumns(sg, columnindexescreated, rootobject, reportname, prefixforlink,
					circuitbreaker + 1);
		}
	}

	/**
	 * writes the column generator for this node recursively
	 * 
	 * @param sg                   source generator
	 * @param columnindexescreated already created column indexes. When creating a
	 *                             new column index, the Integer should be added in
	 *                             the hashmap to avoid other nodes creating the
	 *                             corresponding column grouping again
	 * @param rootobject           parent / root object for the whole report
	 * @param reportname           name of the report
	 * @param prefix               prefix for the workflow step
	 * @param circuitbreaker       recursive circuit breaker
	 * @throws IOException if anything has happened while generating the source code
	 */
	public abstract void setColumnsForNode(
			SourceGenerator sg,
			HashMap<Integer, Integer> columnindexescreated,
			DataObjectDefinition rootobject,
			String reportname,
			String prefix,
			int circuitbreaker) throws IOException;

	/**
	 * builds the report tree for this node
	 * 
	 * @param sg           source generator for the smart report action
	 * @param parentobject parent data object
	 * @param name         name of the report
	 * @param smartReport  the smart report object
	 * @throws IOException if anything bad has happened while writing the file
	 */
	public void buildReportTree(
			SourceGenerator sg,
			DataObjectDefinition parentobject,
			String name,
			SmartReport smartReport) throws IOException {
		buildReportTree(sg, parentobject, name, smartReport, null, 0);
	}

	private void buildReportTree(
			SourceGenerator sg,
			DataObjectDefinition rootobject,
			String reportname,
			SmartReport smartReport,
			String prefix,
			int circuitbreaker) throws IOException {
		if (circuitbreaker > 1000)
			throw new RuntimeException("Circuitbreaker for node " + this.getClass() + " - " + this.toString());

		for (int i = 0; i < linktochildrennode.size(); i++) {
			SmartReportNodeLink thislink = linktochildrennode.get(i);
			String prefixforlink = (prefix != null ? prefix + "_" + (i + 1) : "" + (i + 1));

			thislink.getChildNode().buildReportTreeForNode(sg, this, prefix, prefixforlink, smartReport,
					circuitbreaker + 1);
			thislink.getChildNode().buildReportTree(sg, rootobject, reportname, smartReport, prefixforlink,
					circuitbreaker + 1);
			sg.wl("		}");
		}
	}

	/**
	 * generates the writing the the report tree in smart report action
	 * 
	 * @param sg              source generator for the smart report action
	 * @param parentobject    parent data object
	 * @param prefixforparent prefix of the parent node
	 * @param prefix          prefix of the node
	 * @param smartReport     smart report
	 * @param level           level (how deep)
	 * @throws IOException if anything bad happens while writing the file
	 */
	protected abstract void buildReportTreeForNode(
			SourceGenerator sg,
			SmartReportNode parentnode,
			String prefixforparent,
			String prefix,
			SmartReport smartReport,
			int level) throws IOException;

	/**
	 * prints the extra consolidators for the node
	 * 
	 * @param sg source generator for the smart report action
	 * @param parentobject parent data object
	 * @param name name of the report
	 * @return the list of extra consolidators
	 * @throws IOException if something bad happens writing the file
	 * @since 1.9
	 */
	protected abstract ArrayList<
			String> gatherExtraConsolidatorsforthisnode(
					SourceGenerator sg, 
					DataObjectDefinition parentobject, 
					String name) throws IOException;

	/**
	 * recursive print the extra consolidators for the node
	 * 
	 * @param sg source generator for the smart report action
	 * @param parentobject parent data object
	 * @param name name of the report
	 * @return the list of extra consolidators
	 * @throws IOException if something bad happens writing the file
	 * @since 1.9
	 */
	protected ArrayList<String> gatherExtraConsolidators(
					SourceGenerator sg, 
					DataObjectDefinition parentobject, 
					String name,int circuitbreaker) throws IOException {
		ArrayList<String> valuesforreturn = new ArrayList<String>();
		if (circuitbreaker>1024) throw new RuntimeException("Circuit breaker recursive node path in report ");
		ArrayList<String> valuesforreturnfornode = this.gatherExtraConsolidatorsforthisnode(sg, parentobject, name);
		if (valuesforreturnfornode!=null) valuesforreturn.addAll(valuesforreturnfornode);
		for (int i=0;i<this.linktochildrennode.size();i++) {
			SmartReportNode childnode = this.linktochildrennode.get(i).getChildNode();
			ArrayList<String> thosevalues = childnode.gatherExtraConsolidators(sg, parentobject, name,circuitbreaker+1);
			if (thosevalues!=null) valuesforreturn.addAll(thosevalues);
		}
		return valuesforreturn;
	}
	
	/**
	 * recursive process to find a back to object clause on the tree (generates an
	 * action to show the object)
	 * 
	 * @param circuitbreaker level for recursing processing
	 * @return a data object definition if there is a single backtoobject clause in
	 *         the report, null if there is no such back to object, or an exception
	 *         if there are several backtoobject clauses, which is currently not
	 *         supported
	 * 
	 */
	public abstract DataObjectDefinition getBackToObjet(int circuitbreaker);

	/**
	 * @return the main report value if defined in this node
	 */
	public MainReportValue getMainReportValue() {
		return this.mainreportvalue;
	}

	/**
	 * collects all the fields to add on this node and children. Those fields are
	 * shown in a column.
	 * 
	 * @param fieldstoadd list of fields that will be filled by the method
	 * @param before      true to get fields before main value, false to get fields
	 *                    after main value
	 */
	public void collectFieldsOnNodeAndChildren(ArrayList<Field> fieldstoadd, boolean before) {
		collectFieldsOnNodeAndChildren(fieldstoadd, before, 0);
	}

	private void collectFieldsOnNodeAndChildren(ArrayList<Field> fieldstoadd, boolean before, int circuitbreaker) {
		if (circuitbreaker > 1024)
			throw new RuntimeException("Recursive Circuit breaker");
		this.collectFieldsToAdd(fieldstoadd, before);
		for (int i = 0; i < this.linktochildrennode.size(); i++) {
			SmartReportNodeLink child = this.linktochildrennode.get(i);
			child.getChildNode().collectFieldsOnNodeAndChildren(fieldstoadd, before, circuitbreaker + 1);
		}
	}

	/**
	 * method to implement to collect fields to add on this node
	 * 
	 * @param fieldstoaddbefore list of fields to fill
	 * @param before            true if fields to show before main value are
	 *                          collected, false if fields to show after main value
	 *                          are collected
	 */
	protected abstract void collectFieldsToAdd(ArrayList<Field> fieldstoaddbefore, boolean before);

	/**
	 * collects all the column criteria
	 * 
	 * @param columnswithtotalbysuffix map to fill
	 * @param circuitbreaker           recursive circuit breaker
	 */
	protected void collectTotalColumns(HashMap<String, MainReportValue> columnswithtotalbysuffix, int circuitbreaker) {
		if (circuitbreaker > 1024)
			throw new RuntimeException("Recursive tree is too deep");
		for (int i = 0; i < linktochildrennode.size(); i++)
			linktochildrennode.get(i).getChildNode().collectTotalColumns(columnswithtotalbysuffix, circuitbreaker + 1);
		if (mainreportvalue != null)
			if (mainreportvalue.hasTotal())
				columnswithtotalbysuffix.put((columncriteria != null ? columncriteria.getSuffix() : null),
						mainreportvalue);
	}

}