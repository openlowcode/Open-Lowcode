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
import java.util.logging.Logger;

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameParent;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A filter element based on the objects having link to another object or not.
 * List of valid right users is added on the report launching page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the object on which the filter element is set
 */
public class FilterElementLink<E extends Object>
		extends
		FilterElement<E> {
	private LinkObject<?, ?> linkobject;
	private DataObjectDefinition uniquelinkobject;
	private static Logger logger = Logger.getLogger(FilterElementLink.class.getName());

	/**
	 * creates a filter element based on link
	 * 
	 * @param linkobject link object
	 */

	public FilterElementLink(DataObjectDefinition linkobject) {
		super(false);
		this.uniquelinkobject = linkobject;
		if (linkobject == null)
			throw new RuntimeException("Uniquelinkobject cannot be null");
		if (linkobject.getPropertyByName("LINKOBJECT") == null)
			throw new RuntimeException("Object " + this.uniquelinkobject.getName() + " is not a link object");
		this.linkobject = (LinkObject<?, ?>) linkobject.getPropertyByName("LINKOBJECT");

	}

	@Override
	public DataObjectDefinition getParent() {
		return linkobject.getLeftobjectforlink();
	}

	@Override
	public ArgumentContent getArgumentContent(String suffix) {
		String argumentname = uniquelinkobject.getName().toUpperCase()
				+ (suffix != null ? "_" + suffix.toUpperCase() : "");
		return new ArrayArgument(new ObjectArgument(argumentname, linkobject.getRightobjectforlink()));
	}

	@Override
	public void writeFilterCriteria(SourceGenerator sg, String reportname) throws IOException {
		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectvariable = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String linkobjectvariable = StringFormatter.formatForAttribute(uniquelinkobject.getName());
		String linkobjectclass = StringFormatter.formatForJavaClass(uniquelinkobject.getName());
		boolean searchwithconstraintonparent = false;
		LinkedToParent<?> linkedtoparentforconstraint = null;
		if (linkobject.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT") != null) {

			if (fatherisroot) {
				logger.severe("					* father is root   ");
				@SuppressWarnings("rawtypes")
				ConstraintOnLinkObjectSameParent<?, ?> constraint = (ConstraintOnLinkObjectSameParent) linkobject
						.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT");
				if (linktoparent instanceof LinkedToChildrenNodeLink) {
					logger.severe("					* LinkToChildrenNodeLink   ");
					LinkedToChildrenNodeLink linkedtochildren = (LinkedToChildrenNodeLink) linktoparent;
					logger.severe("					* comparing " + linkedtochildren.getLinkedToparent() + " - "
							+ constraint.getLeftobjectparentproperty());
					if (linkedtochildren.getLinkedToparent().equals(constraint.getLeftobjectparentproperty())) {
						searchwithconstraintonparent = true;
						linkedtoparentforconstraint = constraint.getRightobjectparentproperty();
					}

				}
			}

		}
		logger.severe("        * Filter Element link summary, constraint on link same parent = "
				+ linkobject.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT") + ", final assessment = "
				+ searchwithconstraintonparent);
		sg.wl("		SObjectArrayField<" + rightobjectclass + "> " + linkobjectvariable
				+ "_arrayfield = new SObjectArrayField<" + rightobjectclass + ">(");
		sg.wl("				\"" + linkobjectvariable.toUpperCase() + "\",\""
				+ StringFormatter.escapeforjavastring(linkobject.getRightobjectforlink().getLabel()) + "\",");
		sg.wl("				\"Only select " + linkobject.getLeftobjectforlink().getName() + " with provided "
				+ linkobject.getRightobjectforlink().getName() + "\",");
		sg.wl("				this.get" + linkobjectclass + "_thru(), ");
		sg.wl("				" + rightobjectclass + ".getDefinition(),");
		sg.wl("				" + rightobjectclass + ".getDefinition().getNrFieldMarker(), ");
		sg.wl("				this);");
		sg.wl("		mainband.addElement(" + linkobjectvariable + "_arrayfield);");

		if (searchwithconstraintonparent) {
			sg.wl("		AtgSearch" + rightobjectvariable + "withparent"
					+ linkedtoparentforconstraint.getInstancename().toLowerCase() + "Action.InlineActionRef "
					+ linkobjectvariable + "_arrayfield_searchaction =");
			sg.wl("				AtgSearch" + rightobjectvariable + "withparent"
					+ linkedtoparentforconstraint.getInstancename().toLowerCase()
					+ "Action.get().getInlineActionRef();");
			sg.wl("		" + linkobjectvariable
					+ "_arrayfield_searchaction.setParentid(reportparentid.getObjectIdInput()); ");
		} else {
			sg.wl("		AtgSearch" + rightobjectvariable + "ActionInlineActionRef " + linkobjectvariable
					+ "_arrayfield_searchaction =");
			sg.wl("				AtgSearch" + rightobjectvariable + "Action.get().getInlineActionRef();");
		}
		sg.wl("		SInlineEchoActionRef<TObjectDataEltType<" + rightobjectclass + ">> " + linkobjectvariable
				+ "_arrayfield_resultechoaction =");
		sg.wl("				new SInlineEchoActionRef<TObjectDataEltType<" + rightobjectclass
				+ ">>(new TObjectDataEltType<" + rightobjectclass + ">(" + rightobjectclass + ".getDefinition()));");
		sg.wl("		SFieldSearcher<" + rightobjectclass + "> " + linkobjectvariable + "_arrayfield_searcher =");
		sg.wl("				new SFieldSearcher<" + rightobjectclass + ">(\"WITOTAG_SEARCHER\",");
		sg.wl("				\"add\",");
		sg.wl("				\"close\",");
		sg.wl("				\"enter the start of the number of the "
				+ StringFormatter.escapeforjavastring(linkobject.getRightobjectforlink().getLabel()) + "\",");
		sg.wl("				" + linkobjectvariable + "_arrayfield_searchaction,");
		sg.wl("				" + linkobjectvariable + "_arrayfield_resultechoaction,");
		sg.wl("				" + rightobjectclass + ".getDefinition(),");
		sg.wl("				" + rightobjectclass + ".getNrFieldMarker(),");
		sg.wl("				this);");

		sg.wl("		" + linkobjectvariable + "_arrayfield_searchaction.setNr(" + linkobjectvariable
				+ "_arrayfield_searcher.getSearchTextInput()); ");

		sg.wl("		" + linkobjectvariable + "_arrayfield_searcher.setSearchInlineOutput(AtgSearch"
				+ rightobjectvariable + "Action.get().getSearchresultfor" + rightobjectvariable + "Ref());");
		sg.wl("		" + linkobjectvariable + "_arrayfield.addNodeAtEndOfFieldData(" + linkobjectvariable
				+ "_arrayfield_searcher);");
		sg.wl("		" + linkobjectvariable + "_arrayfield_resultechoaction.setInputData(" + linkobjectvariable
				+ "_arrayfield_searcher.getObjectInput()); ");
		sg.wl("		" + linkobjectvariable + "_arrayfield.addFeedingInlineAction(" + linkobjectvariable
				+ "_arrayfield_resultechoaction, " + linkobjectvariable
				+ "_arrayfield_resultechoaction.getOutputActionDataRef());");

		sg.wl("		launchreport.set" + StringFormatter.formatForJavaClass(uniquelinkobject.getName()) + "("
				+ linkobjectvariable + "_arrayfield.getObjectArrayInput()); ");
		sg.wl("		launchexcelreport.set" + StringFormatter.formatForJavaClass(uniquelinkobject.getName()) + "("
				+ linkobjectvariable + "_arrayfield.getObjectArrayInput()); ");

	}

	@Override
	public String[] getImportClasses() {
		String rightobjectvariable = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		if (linkobject.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT") != null) {
			@SuppressWarnings("rawtypes")
			ConstraintOnLinkObjectSameParent<?, ?> constraint = (ConstraintOnLinkObjectSameParent) linkobject
					.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT");
			return new String[] {
					"import " + linkobject.getRightobjectforlink().getOwnermodule().getPath()
							+ ".action.generated.AtgSearch" + rightobjectvariable + "Action;",
					"import " + linkobject.getRightobjectforlink().getOwnermodule().getPath()
							+ ".action.generated.AtgSearch" + rightobjectvariable + "withparent"
							+ constraint.getRightobjectparentproperty().getInstancename().toLowerCase() + "Action;" };
		}
		return new String[] { "import " + linkobject.getRightobjectforlink().getOwnermodule().getPath()
				+ ".action.generated.AtgSearch" + rightobjectvariable + "Action;" };
	}

	@Override
	protected boolean hasfilterbefore() {
		return false;
	}

	@Override
	protected boolean hasfilterafter() {
		return true;
	}

	@Override
	public void writeFilterInDataGathering(
			SourceGenerator sg,
			String prefixforobject,
			DataObjectDefinition reportroot,
			String reportname) throws IOException {

		String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getParent().getName());
		String linkobjectattribute = StringFormatter.formatForAttribute(linkobject.getParent().getName());

		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		logger.severe("Writing filter in data gathering for node " + prefixforobject + " for FilterElementLink");
		sg.wl("		DataObjectId<" + leftobjectclass + ">[] " + leftobjectattribute + "_step" + prefixforobject
				+ "_id = new DataObjectId[" + leftobjectattribute + "_step" + prefixforobject + ".length];");
		sg.wl("		for (int i=0;i<" + leftobjectattribute + "_step" + prefixforobject + ".length;i++) "
				+ leftobjectattribute + "_step" + prefixforobject + "_id[i] = " + leftobjectattribute + "_step"
				+ prefixforobject + "[i].getId();");
		sg.wl("		TwoDataObjects<" + linkobjectclass + "," + rightobjectclass + ">[] " + leftobjectattribute + "_step"
				+ prefixforobject + "_filteron" + rightobjectattribute + " = " + linkobjectclass
				+ ".getlinksandrightobject(" + leftobjectattribute + "_step" + prefixforobject + "_id, null);");
		sg.wl("		" + leftobjectattribute + "_step" + prefixforobject
				+ " = SmartReportUtility.filterByLinkRightObject(" + leftobjectattribute + "_step" + prefixforobject
				+ ", " + linkobjectattribute + ", " + leftobjectattribute + "_step" + prefixforobject + "_filteron"
				+ rightobjectattribute + ", " + leftobjectclass + ".getDefinition());");
	}

	@Override
	public String[] getImportClassesForAction(String reportname) {
		ArrayList<String> imports = new ArrayList<String>();
		logger.severe("putting in imports " + linkobject.getParent().getName());
		imports.add("import org.openlowcode.server.data.TwoDataObjects;");
		imports.add("import " + linkobject.getParent().getOwnermodule().getPath() + ".data."
				+ StringFormatter.formatForJavaClass(linkobject.getParent().getName()) + ";");
		imports.add("import org.openlowcode.server.action.utility.SmartReportUtility;");
		return imports.toArray(new String[0]);
	}

	@Override
	protected String getBlankValue() {
		return "new " + StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName()) + "[0]";
	}

	@Override
	public boolean hasSuggestionValues() {
		return false;
	}

	@Override
	public ArgumentContent getSuggestionArgumentContent(String suffix) {
		return null;
	}

}
