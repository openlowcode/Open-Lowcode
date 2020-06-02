/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;



/**
 * A Node link allowing navigation from the left object of a link to the right
 * object of a link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.8
 *
 */
public class LinkObjectNodeLink
		extends
		SmartReportNodeLink {

	private LinkObject<?, ?> linkobject;

	/**
	 * Create a new LinkObjectNodeLink
	 * 
	 * @param childnode  child node (should be right object for the link)
	 * @param linkobject link object where left is the parent, and right is the link
	 *                   object
	 */
	public LinkObjectNodeLink(SmartReportNode childnode, LinkObject<?, ?> linkobject) {
		super(childnode);
		this.linkobject = linkobject;
		if (linkobject.getRightobjectforlink() != childnode.getRelevantObject())
			throw new RuntimeException(
					"Incompatible objects, link right object = " + linkobject.getRightobjectforlink().getName()
							+ ", childnode object = " + childnode.getRelevantObject().getName());
	}

	
	
	@Override
	protected void generateImports(SourceGenerator sg) throws IOException {
		DataObjectDefinition linkparentobject = linkobject.getParent();
		sg.wl("import "+linkparentobject.getOwnermodule().getPath()+".data."+StringFormatter.formatForJavaClass(linkparentobject.getName())+";");
		sg.wl("import org.openlowcode.server.data.TwoDataObjects;");
		sg.wl("import org.openlowcode.tools.misc.ObjectUtilities;");
		
	}



	@Override
	public List<FilterElement<?>> getFilterelement() {
		return null;
	}

	@Override
	public List<LineGroupingCriteria> getLineGroupingCriteria() {
		return null;
	}

	@Override
	public DataObjectDefinition getLeftObject() {
		return linkobject.getLeftobjectforlink();
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

		String childclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String childattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String parentattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String parentclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String linkclass = StringFormatter.formatForJavaClass(linkobject.getParent().getName());

		String queryattribute = "null";
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
			sg.wl("		TwoDataObjects<" + linkclass + "," + childclass + ">[] " + childattribute + "_step"
					+ prefixforlinkandchild + " = " + linkclass + ".getlinksandrightobject(parentid,QueryFilter.get("
					+ queryattribute + "));");
		} else {
			sg.wl("		DataObjectId<"+parentclass+">[] "+parentattribute+"_step"+prefixparent+"_id = ObjectUtilities.generateIdTable("+parentattribute+"_step"+prefixparent+");");
			sg.wl("		TwoDataObjects<" + linkclass + "," + childclass + ">[] " + childattribute + "_step" + prefixforlinkandchild + "_links = " + linkclass
					+ ".getlinksandrightobject(" + parentattribute + "_step" + prefixparent + "_id,QueryFilter.get("
					+ queryattribute + "));");
		}
		sg.wl("		"+childclass+"[] "+childattribute+"_step"+prefixforlinkandchild+" = ObjectUtilities.extractUniqueObjectTable("+childattribute+"_step1_1_links, ((a)->(a.getObjectTwo())), ((a)->(a.getId().getId()))).toArray(new "+childclass+"[0]);");
		
		boolean hasidtable = false;
		if (filterelements!=null) for (int i=0;i<filterelements.size();i++) if (filterelements.get(i).needArrayOfObjectId()) hasidtable=true;
		if (groupingelements!=null) for (int i=0;i<groupingelements.size();i++) if (groupingelements.get(i).needArrayOfObjectId()) hasidtable=true;
		
		if (hasidtable) {
			sg.wl("		DataObjectId<" + childclass + ">[] " + childattribute + "_step" + prefixforlinkandchild
					+ "_id = new DataObjectId[" + childattribute + "_step" + prefixforlinkandchild + ".length];");
			sg.wl("		for (int i=0;i<" + childattribute + "_step" + prefixforlinkandchild + ".length;i++) "
					+ childattribute + "_step" + prefixforlinkandchild + "_id[i] = " + childattribute + "_step"
					+ prefixforlinkandchild + "[i].getId();");
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
		String childclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String childattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		@SuppressWarnings("unused")
		String parentattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String parentclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String linkclass = StringFormatter.formatForJavaClass(linkobject.getParent().getName());

		List<LineGroupingCriteria> groupingcriteria = this.getChildNode().getLineGroupingCriteria();
		sg.wl("		CompositeObjectMap<" + parentclass + "," + childclass + ",TwoDataObjects<" + linkclass + "," + childclass + ">> " + childattribute + "_step"
				+ prefixforlinkandchild + "_map");
		sg.w("			= new CompositeObjectMap<" + parentclass + "," + childclass + ",TwoDataObjects<" + linkclass + "," + childclass + ">>(((a)->(a.getObjectOne().getLfid())),((a)->(a.getObjectTwo()))");
		for (int i = 0; i < groupingcriteria.size(); i++) {
			LineGroupingCriteria thiscriteria = groupingcriteria.get(i);
			String extractstring = thiscriteria.getExtractorFromobject(prefixforlinkandchild);
			sg.bl();
			if (extractstring != null)
				sg.w("				," + extractstring);
		}
		sg.w(");");
		sg.wl("		" + childattribute + "_step" + prefixforlinkandchild + "_map.classifyObjects(" + childattribute
				+ "_step" + prefixforlinkandchild + "_links);");

	}

}
