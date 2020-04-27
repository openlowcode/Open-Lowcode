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
import java.util.List;
import java.util.logging.Logger;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * Starting from a parent, this node link will navigate to all children of the
 * object. There is no filter on this nodelink, as the filters have to be put,
 * if needed, on the child object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LinkedToChildrenNodeLink
		extends
		SmartReportNodeLink {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LinkedToChildrenNodeLink.class.getName());

	private LinkedToParent<?> linkedtoparent;

	/**
	 * @return the related linked to parent property
	 */
	public LinkedToParent<?> getLinkedToparent() {
		return this.linkedtoparent;
	}

	/**
	 * 
	 * 
	 * @param childnode      child node to the link
	 * @param linkedtoparent linked to parent property used to navigate from parent
	 *                       to child
	 */
	public LinkedToChildrenNodeLink(SmartReportNode childnode, LinkedToParent<?> linkedtoparent) {
		super(childnode);
		if (linkedtoparent == null)
			throw new RuntimeException("LinkedToParent cannot be null");
		if (this.getChildNode().getRelevantObject() == null)
			throw new RuntimeException("Node should have a relevant object");
		if (!this.getChildNode().getRelevantObject().equals(linkedtoparent.getParent()))
			throw new RuntimeException(
					"Inconsistent Objects for child node " + this.getChildNode().getRelevantObject().getName()
							+ " and linkedtoparent parent " + linkedtoparent.getParent().getName());
		this.linkedtoparent = linkedtoparent;
	}

	@Override
	public List<FilterElement<?>> getFilterelement() {
		return null;
	}

	@Override
	public DataObjectDefinition getLeftObject() {
		return linkedtoparent.getParentObjectForLink();
	}

	@Override
	public List<LineGroupingCriteria> getLineGroupingCriteria() {
		return null;
	}

	@Override
	protected void gatherData(
			SourceGenerator sg,
			SmartReportNode parent,
			String prefixparent,
			String prefixforlinkandchild,
			boolean first,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) throws IOException {
		String linkedtoparentinstancename = linkedtoparent.getInstancename().toLowerCase();
		String childclass = StringFormatter.formatForJavaClass(linkedtoparent.getParent().getName());
		String childattribute = StringFormatter.formatForAttribute(linkedtoparent.getParent().getName());
		String parentattribute = StringFormatter.formatForAttribute(linkedtoparent.getParentObjectForLink().getName());
		String parentclass = StringFormatter.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());

		String queryattribute="null";
		// no need to be recursive here
		List<FilterElement<?>> filterelements = this.getChildNode().getFilterelement();
		List<LineGroupingCriteria> groupingelements = this.getChildNode().getLineGroupingCriteria();
		boolean hasfilterbefore = false;

		if (filterelements != null)
			for (int i = 0; i < filterelements.size(); i++) {
				FilterElement<?> thiselement = filterelements.get(i);
				if (thiselement.hasfilterbefore())
					hasfilterbefore = true;

			}
		sg.wl("			// ----- data gathering step " + prefixforlinkandchild);
		if (hasfilterbefore) {
			queryattribute = childattribute + "_step" + prefixforlinkandchild + "_query";

			sg.wl("		AndQueryCondition " + queryattribute + " = new AndQueryCondition();");
			
			if (filterelements != null)
				for (int i = 0; i < filterelements.size(); i++) {
					FilterElement<?> thiselement = filterelements.get(i);
					if (thiselement.hasfilterbefore()) {
						thiselement.writeFilterInDataGathering(sg, prefixforlinkandchild, rootobject, reportname);
					}

				}
			
		}
		if (first) {
			sg.wl("		" + childclass + "[] " + childattribute + "_step" + prefixforlinkandchild + " = " + childclass
					+ ".getallchildrenfor" + linkedtoparentinstancename + "(parentid,QueryFilter.get(" + queryattribute + "));");
		} else {

			sg.wl("		" + childclass + "[] " + childattribute + "_step" + prefixforlinkandchild + " = " + parentclass
					+ ".getallchildrenfor" + linkedtoparentinstancename + "for" + childattribute + "(" + parentattribute
					+ "_step" + prefixparent + ",QueryFilter.get(" + queryattribute + "));");

		}
		if (filterelements != null)
			for (int i = 0; i < filterelements.size(); i++) {
				FilterElement<?> thiselement = filterelements.get(i);
				if (thiselement.hasfilterafter()) {

					thiselement.writeFilterInDataGathering(sg, prefixforlinkandchild, rootobject, reportname);
				}
			}

		for (int i = 0; i < groupingelements.size(); i++) {
			LineGroupingCriteria thisgroupingelement = groupingelements.get(i);
			if (thisgroupingelement.hasDataGathering()) {
				thisgroupingelement.writeDataGathering(sg, prefixforlinkandchild);
			}
		}

		this.getChildNode().gatherData(sg, circuitbreaker + 1, prefixforlinkandchild, rootobject, reportname);
	}

	@Override
	protected void orderData(
			SourceGenerator sg,
			SmartReportNode smartReportNode,
			String prefix,
			String prefixforlinkandchild,
			boolean first,
			int circuitbreaker,
			DataObjectDefinition rootobject,
			String reportname) throws IOException {
		String linkedtoparentinstancename = linkedtoparent.getInstancename().toLowerCase();
		String childclass = StringFormatter.formatForJavaClass(linkedtoparent.getParent().getName());
		String childattribute = StringFormatter.formatForAttribute(linkedtoparent.getParent().getName());
		String parentclass = StringFormatter.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());
		List<LineGroupingCriteria> groupingcriteria = this.getChildNode().getLineGroupingCriteria();
		sg.wl("		CompositeObjectMap<" + parentclass + "," + childclass + "> " + childattribute + "_step"
				+ prefixforlinkandchild + "_map");
		sg.w("			= new CompositeObjectMap<" + parentclass + "," + childclass + ">((a)->(a.getLinkedtoparentfor"
				+ linkedtoparentinstancename + "id())");
		for (int i = 0; i < groupingcriteria.size(); i++) {
			LineGroupingCriteria thiscriteria = groupingcriteria.get(i);
			String extractstring = thiscriteria.getExtractorFromobject(prefixforlinkandchild);
			sg.bl();
			if (extractstring != null)
				sg.w("				," + extractstring);
		}
		sg.wl(");");
		sg.wl("		" + childattribute + "_step" + prefixforlinkandchild + "_map.classifyObjects(" + childattribute
				+ "_step" + prefixforlinkandchild + ");");
	}

}
