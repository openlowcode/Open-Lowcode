/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.action.StaticActionDefinition;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.MultipleChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.properties.basic.AutolinkObject;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameParent;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.DynamicPageDefinition;
import org.openlowcode.design.pages.PageDefinition;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.module.system.design.SystemModule;

/**
 * This class regroups methods to generate search actions and pages
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionSearchPagesAndActions {
	private DataObjectDefinition object;

	/**
	 * creates a utility class to generate search actions and pages for the given
	 * data object
	 * 
	 * @param object data object
	 */
	public DataObjectDefinitionSearchPagesAndActions(DataObjectDefinition object) {
		this.object = object;
	}

	/**
	 * generates search action looking for a potential right object
	 * 
	 * @param linkobject link object property
	 * @return the search action for right object
	 */
	protected ActionDefinition generateSearchActionForRightObjectLink(LinkObject<?, ?> linkobject) {
		DataObjectDefinition rightobject = linkobject.getRightobjectforlink();
		DataObjectDefinition leftobject = linkobject.getLeftobjectforlink();
		DynamicActionDefinition searchactionforrightobjectlink = new DynamicActionDefinition(
				"SEARCHRIGHT" + rightobject.getName().toUpperCase() + "FOR" + object.getName().toUpperCase(), true);
		searchactionforrightobjectlink
				.addInputArgument(new ObjectIdArgument("LEFT" + leftobject.getName().toUpperCase(), leftobject));
		searchactionforrightobjectlink.forceNoAddress();

		// add normal attributes for search object
		SearchWidgetDefinition[] searchwidgets = rightobject.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						searchactionforrightobjectlink.addInputArgument(new StringArgument(widget.getFieldname(), 64));
					} else {
						searchactionforrightobjectlink.addInputArgument(
								new MultipleChoiceArgument(widget.getFieldname(), widget.getMultipleChoiceCategory()));
					}
				}
			}
		}
		searchactionforrightobjectlink.addOutputArgumentAsAccessCriteria(new ArrayArgument(
				new ObjectArgument("SEARCHRESULTFOR" + rightobject.getName().toUpperCase(), rightobject)));
		rightobject.addActionToLookupActionGroup(searchactionforrightobjectlink);
		rightobject.addActionToReadActionGroup(searchactionforrightobjectlink);

		return searchactionforrightobjectlink;
	}

	/**
	 * generate the search action looking for potential left object while building a
	 * link
	 * 
	 * @param linkobject link object property
	 * @return the search action for right object
	 */
	protected ActionDefinition generateSearchActionForLeftObjectLink(LinkObject<?, ?> linkobject) {
		DataObjectDefinition rightobject = linkobject.getRightobjectforlink();
		DataObjectDefinition leftobject = linkobject.getLeftobjectforlink();
		DynamicActionDefinition searchactionforleftobjectlink = new DynamicActionDefinition(
				"SEARCHLEFT" + leftobject.getName().toUpperCase() + "FOR" + object.getName().toUpperCase(), true);
		searchactionforleftobjectlink
				.addInputArgument(new ObjectIdArgument("RIGHT" + rightobject.getName().toUpperCase(), rightobject));
		searchactionforleftobjectlink.forceNoAddress();

		// add normal attributes for search object
		SearchWidgetDefinition[] searchwidgets = leftobject.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						searchactionforleftobjectlink.addInputArgument(new StringArgument(widget.getFieldname(), 64));
					} else {
						searchactionforleftobjectlink.addInputArgument(
								new MultipleChoiceArgument(widget.getFieldname(), widget.getMultipleChoiceCategory()));
					}
				}
			}
		}
		searchactionforleftobjectlink.addOutputArgumentAsAccessCriteria(new ArrayArgument(
				new ObjectArgument("SEARCHRESULTFOR" + leftobject.getName().toUpperCase(), leftobject)));
		leftobject.addActionToLookupActionGroup(searchactionforleftobjectlink);
		leftobject.addActionToReadActionGroup(searchactionforleftobjectlink);

		return searchactionforleftobjectlink;
	}

	/**
	 * generates a search action for potential right objects to build an auto-link
	 * 
	 * @param linkobject autolink object property
	 * @return the action definition
	 */
	protected ActionDefinition generateSearchActionForRightObjectAutolink(AutolinkObject<?> linkobject) {
		DataObjectDefinition linkedobject = linkobject.getObjectforlink();
		DynamicActionDefinition searchactionforrightobjectlink = new DynamicActionDefinition(
				"SEARCHRIGHT" + linkedobject.getName().toUpperCase() + "FOR" + object.getName().toUpperCase(), true);
		searchactionforrightobjectlink.forceNoAddress();
		searchactionforrightobjectlink
				.addInputArgument(new ObjectIdArgument("LEFT" + linkedobject.getName().toUpperCase(), linkedobject));

		// add normal attributes for search object
		SearchWidgetDefinition[] searchwidgets = linkedobject.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						searchactionforrightobjectlink.addInputArgument(new StringArgument(widget.getFieldname(), 64));
					} else {
						searchactionforrightobjectlink.addInputArgument(
								new MultipleChoiceArgument(widget.getFieldname(), widget.getMultipleChoiceCategory()));
					}
				}
			}
		}
		searchactionforrightobjectlink.addOutputArgumentAsAccessCriteria(new ArrayArgument(
				new ObjectArgument("SEARCHRESULTFOR" + linkedobject.getName().toUpperCase(), linkedobject)));
		linkedobject.addActionToLookupActionGroup(searchactionforrightobjectlink);
		linkedobject.addActionToReadActionGroup(searchactionforrightobjectlink);
		return searchactionforrightobjectlink;
	}

	/**
	 * general the search action for the data object
	 * 
	 * @return the search action
	 */
	protected ActionDefinition generateSearchAction() {
		DynamicActionDefinition searchaction = new DynamicActionDefinition("SEARCH" + object.getName(), true);
		searchaction.forceNoAddress();
		SearchWidgetDefinition[] searchwidgets = object.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						searchaction.addInputArgument(new StringArgument(widget.getFieldname(), 64));
					} else {
						searchaction.addInputArgument(
								new MultipleChoiceArgument(widget.getFieldname(), widget.getMultipleChoiceCategory()));
					}
				}
			}
		}

		searchaction.addOutputArgumentAsAccessCriteria(
				new ArrayArgument(new ObjectArgument("SEARCHRESULTFOR" + object.getName(), object)));
		object.addActionToReadActionGroup(searchaction);
		object.addActionToLookupActionGroup(searchaction);
		return searchaction;
	}

	/**
	 * generates the source code of the search action for a potential right object
	 * to build a link
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchActionWithParentToFile(SourceGenerator sg, Module module) throws IOException {
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) object.getPropertyByName("LINKOBJECT");
		@SuppressWarnings("rawtypes")
		ConstraintOnLinkObjectSameParent<?, ?> constraintonparent = (ConstraintOnLinkObjectSameParent) linkobject
				.getBusinessRuleByName("CONSTRAINTONLINKSAMEPARENT");
		boolean canorder = false;
		if ((linkobject.getRightobjectforlink().getPropertyByName("CREATIONLOG") != null)
				|| (linkobject.getRightobjectforlink().getPropertyByName("UPDATELOG") != null))
			canorder = true;

		String objectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String objectattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String parentinstancename = StringFormatter
				.formatForAttribute(constraintonparent.getRightobjectparentproperty().getInstancename());
		String parentobjectclass = StringFormatter.formatForJavaClass(
				constraintonparent.getRightobjectparentproperty().getParentObjectForLink().getName());

		boolean isaddress = false;
		String actionname = "Search" + objectattribute + "withparent" + parentinstancename + "Action";
		SearchWidgetDefinition[] searchwidgets = linkobject.getRightobjectforlink().getSearchWidgets();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		if (canorder) {
			sg.wl("import java.util.Arrays;");
			sg.wl("import java.util.Collections;");
			sg.wl("import java.util.List;");
		}
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("");

		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data." + objectclass + ";");
		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data." + objectclass
				+ "Definition;");
		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".page.generated.AtgSearch"
				+ objectattribute + "Page;");
		sg.wl("import "
				+ constraintonparent.getRightobjectparentproperty().getParentObjectForLink().getOwnermodule().getPath()
				+ ".data." + parentobjectclass + ";");
		sg.wl("import "
				+ constraintonparent.getRightobjectparentproperty().getParentObjectForLink().getOwnermodule().getPath()
				+ ".data." + parentobjectclass + "Definition;");

		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");
		sg.wl("import org.openlowcode.server.data.storage.OrQueryCondition;");

		sg.wl("import org.openlowcode.server.data.properties.LinkedtoparentQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.data.properties.StoredobjectQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
			}
		}

		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");

		sg.wl("");
		sg.wl("	public class Specific" + objectclass + "Searcher implements  AtgSearch" + objectattribute + "Page."
				+ objectclass + "Searcher<Abs" + actionname + ".InlineActionRef> {");
		sg.wl("		Atg" + actionname + " action;");
		sg.wl("		Function<SActionInputDataRef<TObjectIdDataEltType<" + parentobjectclass
				+ ">>, SActionDataLoc<TObjectIdDataEltType<" + parentobjectclass + ">>> id;");
		sg.wl("		public Specific" + objectclass + "Searcher(Function<SActionInputDataRef<TObjectIdDataEltType<"
				+ parentobjectclass + ">>, SActionDataLoc<TObjectIdDataEltType<" + parentobjectclass + ">>> id) {");
		sg.wl("			action = (Atg" + actionname + ")Atg" + actionname + ".get();");
		sg.wl("			this.id = id;");
		sg.wl("		}");
		sg.wl("");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function) { ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					} else {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function) {  ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					}
				}
			}
		}

		sg.wl("		@Override");
		sg.wl("		public SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<" + objectclass
				+ ">>> getSearchresultfor" + objectattribute + "Ref() {");
		sg.wl("			return this.getSearchresultfor" + objectattribute + "Ref();");
		sg.wl("		}");

		sg.wl("		@Override");
		sg.wl("		public Abs" + actionname + ".InlineActionRef getInlineActionRef() {");
		sg.wl("			return Abs" + actionname + ".get().getInlineActionRef();");
		sg.wl("		}");
		sg.wl("		@Override");
		sg.wl("		public void setExtraAttributes(InlineActionRef specificinlineactionref)  {");
		sg.wl("			specificinlineactionref.setParentid(id);");
		sg.wl("		}");
		sg.wl("");
		sg.wl("	}	");

		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	");
		sg.wl("");

		sg.wl("	@Override");
		if (isaddress) {
			sg.wl("	public SPage choosePage(ActionOutputData outputdata)  {");
		} else {
			sg.wl("	public SPage choosePage(" + objectclass + "[] " + objectattribute + ")  {");
		}
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		if (isaddress) {
			sg.w("	public ActionOutputData executeActionLogic(DataObjectId<" + parentobjectclass + "> parentid,");

		} else {
			sg.w("	public " + objectclass + "[] executeActionLogic(DataObjectId<" + parentobjectclass + "> parentid,");

		}

		boolean first = true;
		searchwidgets = linkobject.getRightobjectforlink().getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (first) {
						first = false;
					} else {
						sg.w(",");
					}
					if (widget.getMultipleChoiceCategory() == null) {
						sg.w("String " + searchwidgets[i].getFieldname().toLowerCase());
					} else {
						sg.wl("ChoiceValue<"
								+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
								+ "ChoiceDefinition>[] " + searchwidgets[i].getFieldname().toLowerCase());
					}
				}
			}
		}
		sg.wl("			" + (searchwidgets.length > 0 ? "," : "") + "Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("");
		sg.wl("		ArrayList<QueryCondition> andconditions = new ArrayList<QueryCondition>();");
		sg.wl("		TableAlias alias = " + objectclass + "Definition.get" + objectclass
				+ "Definition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);");

		sg.wl(" 		QueryCondition parentfilter = LinkedtoparentQueryHelper.get(\"LINKEDTOPARENTFOR"
				+ constraintonparent.getRightobjectparentproperty().getInstancename().toUpperCase()
				+ "\").getParentIdQueryCondition(alias, parentid, " + objectclass + ".getDefinition(),"
				+ parentobjectclass + ".getDefinition());");

		sg.wl("		andconditions.add(parentfilter);");

		this.generateSearchcriteria(searchwidgets, objectclass, sg);
		sg.wl("		QueryFilter additionalcondition = (datafilter!=null?datafilter.apply(alias):null);");
		sg.wl("		if (additionalcondition!=null) if (additionalcondition.getCondition()!=null) andconditions.add(datafilter.apply(alias).getCondition());");
		sg.wl("");
		sg.wl("		QueryCondition finalquerycondition=null;");
		sg.wl("		");
		sg.wl("		if (andconditions.size()>0) {");
		sg.wl("			AndQueryCondition andfinalquerycondition = new AndQueryCondition();");
		sg.wl("			for (int i=0;i<andconditions.size();i++) ");
		sg.wl("				andfinalquerycondition.addCondition(andconditions.get(i));");
		sg.wl("			finalquerycondition = andfinalquerycondition;");
		sg.wl("		}");
		sg.wl("			");
		sg.wl("		");
		sg.wl("");
		sg.wl("		" + objectclass + "[] result = " + objectclass
				+ ".getallactive(new QueryFilter(finalquerycondition,(additionalcondition!=null?additionalcondition.getAliases():null)));");
		sg.wl("		AtgMassupdate" + objectattribute + "Action.get().freezeUnauthorizedObjects(result);");
		if (canorder) {

			sg.wl("		List<" + objectclass + "> resultlist = Arrays.asList(result);");
			if (linkobject.getRightobjectforlink().getPropertyByName("UPDATELOG") != null) {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getUpdatetime().compareTo(a.getUpdatetime())));");
			} else {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getCreatetime().compareTo(a.getCreatetime())));");
			}
			sg.wl("		result = resultlist.toArray(new " + objectclass + "[0]);");

		}

		if (isaddress) {
			sg.wl("		return new ActionOutputData(result);");
		} else {
			sg.wl("		return result;");
		}
		sg.wl("	}");
		sg.wl("");

		sg.wl("}");

		sg.close();

	}

	/**
	 * Generates the search action source code for a potential right object
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchActionForRightObjectLinkToFile(SourceGenerator sg, Module module) throws IOException {
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) object.getPropertyByName("LINKOBJECT");
		boolean canorder = false;
		if ((linkobject.getRightobjectforlink().getPropertyByName("CREATIONLOG") != null)
				|| (linkobject.getRightobjectforlink().getPropertyByName("UPDATELOG") != null))
			canorder = true;
		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String linkobjectclass = StringFormatter.formatForJavaClass(object.getName());
		String linkobjectattribute = StringFormatter.formatForAttribute(object.getName());
		String actionname = "Searchright" + rightobjectattribute + "for" + linkobjectattribute + "Action";
		SearchWidgetDefinition[] searchwidgets = linkobject.getRightobjectforlink().getSearchWidgets();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		if (canorder) {
			sg.wl("import java.util.Arrays;");
			sg.wl("import java.util.Collections;");
			sg.wl("import java.util.List;");
		}
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("");

		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data." + rightobjectclass
				+ ";");
		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data." + rightobjectclass
				+ "Definition;");

		sg.wl("import " + linkobject.getLeftobjectforlink().getOwnermodule().getPath() + ".data." + leftobjectclass
				+ ";");
		sg.wl("import " + module.getPath() + ".data." + linkobjectclass + ";");
		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".page.generated.AtgSearch"
				+ rightobjectattribute + "Page;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.OrQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.data.properties.LinkobjectQueryHelper;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		boolean ischoice = false;
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
				ischoice = true;
			}
		}
		if (ischoice)
			sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");

		sg.wl("	public class Specific" + rightobjectclass + "Searcher implements  AtgSearch" + rightobjectattribute
				+ "Page." + rightobjectclass + "Searcher<Abs" + actionname + ".InlineActionRef> {");
		sg.wl("		Atg" + actionname + " action;");
		sg.wl("		Function<SActionInputDataRef<TObjectIdDataEltType<" + leftobjectclass
				+ ">>, SActionDataLoc<TObjectIdDataEltType<" + leftobjectclass + ">>> id;");
		sg.wl("		public Specific" + rightobjectclass + "Searcher(Function<SActionInputDataRef<TObjectIdDataEltType<"
				+ leftobjectclass + ">>, SActionDataLoc<TObjectIdDataEltType<" + leftobjectclass + ">>>  id) {");
		sg.wl("			action = (Atg" + actionname + ")Atg" + actionname + ".get();");
		sg.wl("			this.id = id;");
		sg.wl("		}");
		sg.wl("		");
		sg.wl("		@Override");
		sg.wl("		public InlineActionRef getInlineActionRef() {");
		sg.wl("			return Atg" + actionname + ".get().getInlineActionRef();");
		sg.wl("		}");
		sg.wl("");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function) { ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					} else {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function) {  ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					}
				}
			}
		}

		sg.wl("		@Override");
		sg.wl("		public SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<" + rightobjectclass
				+ ">>> getSearchresultfor" + rightobjectattribute + "Ref() {");
		sg.wl("			return action.getSearchresultfor" + rightobjectattribute + "Ref();");
		sg.wl("		}");
		sg.wl("");

		sg.wl("		@Override");
		sg.wl("		public void setExtraAttributes(InlineActionRef specificinlineactionref)  {");
		sg.wl("			specificinlineactionref.setLeft" + leftobjectattribute + "(id);			");
		sg.wl("		}	");
		sg.wl("");
		sg.wl("	}");

		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + rightobjectclass + "[] " + rightobjectattribute + ")  {");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.w("	public " + rightobjectclass + "[] executeActionLogic(DataObjectId<" + leftobjectclass + "> left"
				+ leftobjectattribute + "id");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					sg.wl(",");
					sg.w("							");

					if (widget.getMultipleChoiceCategory() == null) {
						sg.w("String " + widget.getFieldname().toLowerCase());
					} else {
						sg.wl("ChoiceValue<"
								+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
								+ "ChoiceDefinition>[] " + widget.getFieldname().toLowerCase());
					}
				}
			}
		}
		sg.wl(",Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("  // TODO data filter is not used");
		sg.wl("		ArrayList<QueryCondition> andconditions = new ArrayList<QueryCondition>();");
		sg.wl("		TableAlias alias = LinkobjectQueryHelper.getRightObjectAliasForPotentialRightObject("
				+ rightobjectclass + "Definition.get" + rightobjectclass + "Definition());");

		this.generateSearchcriteria(searchwidgets, rightobjectclass, sg);

		sg.wl("		QueryCondition finalquerycondition=null;");
		sg.wl("		");
		sg.wl("		if (andconditions.size()>0) {");
		sg.wl("			AndQueryCondition andfinalquerycondition = new AndQueryCondition();");
		sg.wl("			for (int i=0;i<andconditions.size();i++) ");
		sg.wl("				andfinalquerycondition.addCondition(andconditions.get(i));");
		sg.wl("			finalquerycondition = andfinalquerycondition;");
		sg.wl("		}");
		sg.wl("			");
		sg.wl("		");
		sg.wl("		" + rightobjectclass + "[] result = " + linkobjectclass + ".getpotentialrightobject(left"
				+ leftobjectattribute + "id,new QueryFilter(finalquerycondition,null));");
		if (canorder) {

			sg.wl("		List<" + rightobjectclass + "> resultlist = Arrays.asList(result);");
			if (linkobject.getRightobjectforlink().getPropertyByName("UPDATELOG") != null) {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getUpdatetime().compareTo(a.getUpdatetime())));");
			} else {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getCreatetime().compareTo(a.getCreatetime())));");
			}
			sg.wl("		result = resultlist.toArray(new " + rightobjectclass + "[0]);");

		}
		sg.wl("		return result;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the search action looking for a potential left object for a link
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchActionForLeftObjectLinkToFile(SourceGenerator sg, Module module) throws IOException {
		LinkObject<?, ?> linkobject = (LinkObject<?, ?>) object.getPropertyByName("LINKOBJECT");
		boolean canorder = false;
		if ((linkobject.getLeftobjectforlink().getPropertyByName("CREATIONLOG") != null)
				|| (linkobject.getLeftobjectforlink().getPropertyByName("UPDATELOG") != null))
			canorder = true;

		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		String rightobjectattribute = StringFormatter.formatForAttribute(linkobject.getRightobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String linkobjectclass = StringFormatter.formatForJavaClass(object.getName());
		String linkobjectattribute = StringFormatter.formatForAttribute(object.getName());
		String actionname = "Searchleft" + leftobjectattribute + "for" + linkobjectattribute + "Action";
		SearchWidgetDefinition[] searchwidgets = linkobject.getLeftobjectforlink().getSearchWidgets();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("");
		if (canorder) {
			sg.wl("import java.util.Arrays;");
			sg.wl("import java.util.Collections;");
			sg.wl("import java.util.List;");
		}
		sg.wl("import " + linkobject.getLeftobjectforlink().getOwnermodule().getPath() + ".data." + leftobjectclass
				+ ";");
		sg.wl("import " + linkobject.getLeftobjectforlink().getOwnermodule().getPath() + ".data." + leftobjectclass
				+ "Definition;");

		sg.wl("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data." + rightobjectclass
				+ ";");
		sg.wl("import " + module.getPath() + ".data." + linkobjectclass + ";");
		sg.wl("import " + linkobject.getLeftobjectforlink().getOwnermodule().getPath() + ".page.generated.AtgSearch"
				+ leftobjectattribute + "Page;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.OrQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.data.properties.LinkobjectQueryHelper;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		boolean ischoice = false;
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
				ischoice = true;
			}
		}
		if (ischoice)
			sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");

		sg.wl("	public class Specific" + leftobjectclass + "Searcher implements  AtgSearch" + leftobjectattribute
				+ "Page." + leftobjectclass + "Searcher<Abs" + actionname + ".InlineActionRef> {");
		sg.wl("		Atg" + actionname + " action;");
		sg.wl("		Function<SActionInputDataRef<TObjectIdDataEltType<" + rightobjectclass
				+ ">>, SActionDataLoc<TObjectIdDataEltType<" + rightobjectclass + ">>> id;");
		sg.wl("		public Specific" + leftobjectclass + "Searcher(Function<SActionInputDataRef<TObjectIdDataEltType<"
				+ rightobjectclass + ">>, SActionDataLoc<TObjectIdDataEltType<" + rightobjectclass + ">>> id) {");
		sg.wl("			action = (Atg" + actionname + ")Atg" + actionname + ".get();");
		sg.wl("			this.id = id;");
		sg.wl("		}");
		sg.wl("		");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function) { ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					} else {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function) {  ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					}
				}
			}
		}

		sg.wl("		@Override");
		sg.wl("		public SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<" + leftobjectclass
				+ ">>> getSearchresultfor" + leftobjectattribute + "Ref() {");
		sg.wl("			return action.getSearchresultfor" + leftobjectattribute + "Ref();");
		sg.wl("		}");
		sg.wl("");

		sg.wl("		@Override");
		sg.wl("		public Abs" + actionname + ".InlineActionRef getInlineActionRef() {");
		sg.wl("			return Abs" + actionname + ".get().getInlineActionRef();");
		sg.wl("		}");

		sg.wl("		@Override");
		sg.wl("		public void setExtraAttributes(InlineActionRef specificinlineactionref)  {");
		sg.wl("			specificinlineactionref.setRight" + rightobjectattribute + "(id);");
		sg.wl("		}");

		sg.wl("");
		sg.wl("	}");

		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + leftobjectclass + "[] " + leftobjectattribute + ")  {");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.w("	public " + leftobjectclass + "[] executeActionLogic(DataObjectId<" + rightobjectclass + "> right"
				+ rightobjectattribute + "id");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					sg.wl(",");
					sg.w("							");

					if (widget.getMultipleChoiceCategory() == null) {
						sg.w("String " + searchwidgets[i].getFieldname().toLowerCase());
					} else {
						sg.wl("ChoiceValue<"
								+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
								+ "ChoiceDefinition>[] " + widget.getFieldname().toLowerCase());
					}
				}
			}
		}
		sg.wl(",Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("  // TODO data filter is not used");
		sg.wl("		ArrayList<QueryCondition> andconditions = new ArrayList<QueryCondition>();");
		sg.wl("		TableAlias alias = LinkobjectQueryHelper.getLeftObjectAliasForPotentialLeftObject("
				+ leftobjectclass + "Definition.get" + leftobjectclass + "Definition());");

		this.generateSearchcriteria(searchwidgets, leftobjectclass, sg);

		sg.wl("		QueryCondition finalquerycondition=null;");
		sg.wl("		");
		sg.wl("		if (andconditions.size()>0) {");
		sg.wl("			AndQueryCondition andfinalquerycondition = new AndQueryCondition();");
		sg.wl("			for (int i=0;i<andconditions.size();i++) ");
		sg.wl("				andfinalquerycondition.addCondition(andconditions.get(i));");
		sg.wl("			finalquerycondition = andfinalquerycondition;");
		sg.wl("		}");
		sg.wl("			");
		sg.wl("		");
		sg.wl("		" + leftobjectclass + "[] result = " + linkobjectclass + ".getpotentialleftobject(right"
				+ rightobjectattribute + "id,new QueryFilter(finalquerycondition,null));");
		if (canorder) {

			sg.wl("		List<" + leftobjectclass + "> resultlist = Arrays.asList(result);");
			if (linkobject.getLeftobjectforlink().getPropertyByName("UPDATELOG") != null) {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getUpdatetime().compareTo(a.getUpdatetime())));");
			} else {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getCreatetime().compareTo(a.getCreatetime())));");
			}
			sg.wl("		result = resultlist.toArray(new " + leftobjectclass + "[0]);");

		}
		sg.wl("		return result;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the source code of search action for a potential right object for
	 * an auto-link
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchActionForRightObjectAutolinkToFile(SourceGenerator sg, Module module) throws IOException {
		AutolinkObject<?> autolinkobject = (AutolinkObject<?>) object.getPropertyByName("AUTOLINKOBJECT");
		boolean canorder = false;
		if ((autolinkobject.getObjectforlink().getPropertyByName("CREATIONLOG") != null)
				|| (autolinkobject.getObjectforlink().getPropertyByName("UPDATELOG") != null))
			canorder = true;

		String rightobjectclass = StringFormatter.formatForJavaClass(autolinkobject.getObjectforlink().getName());
		String rightobjectattribute = StringFormatter.formatForAttribute(autolinkobject.getObjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(autolinkobject.getObjectforlink().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(autolinkobject.getObjectforlink().getName());
		String linkobjectclass = StringFormatter.formatForJavaClass(object.getName());
		String linkobjectattribute = StringFormatter.formatForAttribute(object.getName());
		String actionname = "Searchright" + rightobjectattribute + "for" + linkobjectattribute + "Action";
		SearchWidgetDefinition[] searchwidgets = autolinkobject.getObjectforlink().getSearchWidgets();
		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		if (canorder) {
			sg.wl("import java.util.Arrays;");
			sg.wl("import java.util.Collections;");
			sg.wl("import java.util.List;");
		}
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("");

		sg.wl("import " + module.getPath() + ".data." + rightobjectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + rightobjectclass + "Definition;");
		sg.wl("import " + module.getPath() + ".data." + linkobjectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + rightobjectattribute + "Page;");

		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.OrQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.server.data.message.TObjectIdDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.properties.AutolinkobjectQueryHelper;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		boolean ischoice = false;
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
				ischoice = true;
			}
		}
		if (ischoice)
			sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");
		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " {");
		sg.wl("");

		sg.wl("	public class Specific" + rightobjectclass + "Searcher implements  AtgSearch" + rightobjectattribute
				+ "Page." + rightobjectclass + "Searcher<Abs" + actionname + ".InlineActionRef> {");
		sg.wl("		Atg" + actionname + " action;");
		sg.wl("		Function<SActionInputDataRef<TObjectIdDataEltType<" + rightobjectclass
				+ ">>, SActionDataLoc<TObjectIdDataEltType<" + rightobjectclass + ">>> id;");
		sg.wl("		public Specific" + rightobjectclass + "Searcher(Function<SActionInputDataRef<TObjectIdDataEltType<"
				+ rightobjectclass + ">>, SActionDataLoc<TObjectIdDataEltType<" + rightobjectclass + ">>> id) {");
		sg.wl("			action = (Atg" + actionname + ")Atg" + actionname + ".get();");
		sg.wl("			this.id = id;");
		sg.wl("		}");
		sg.wl("		");

		sg.wl("");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function) { ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					} else {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function) {  ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					}
				}
			}
		}

		sg.wl("		@Override");
		sg.wl("		public SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<" + rightobjectclass
				+ ">>> getSearchresultfor" + rightobjectattribute + "Ref() {");
		sg.wl("			return action.getSearchresultfor" + rightobjectattribute + "Ref();");
		sg.wl("		}");
		sg.wl("");

		sg.wl("		@Override");
		sg.wl("		public Abs" + actionname + ".InlineActionRef getInlineActionRef() {");
		sg.wl("			return Abs" + actionname + ".get().getInlineActionRef();");
		sg.wl("		}");

		sg.wl("		@Override");
		sg.wl("		public void setExtraAttributes(InlineActionRef specificinlineactionref)  {");
		sg.wl("			specificinlineactionref.setLeft" + rightobjectattribute + "(id);	");
		sg.wl("		}");

		sg.wl("	}");

		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + rightobjectclass + "[] " + rightobjectattribute + ")  {");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.w("	public " + rightobjectclass + "[] executeActionLogic(DataObjectId<" + leftobjectclass + "> left"
				+ leftobjectattribute + "id");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					sg.wl(",");
					sg.w("							");

					if (widget.getMultipleChoiceCategory() == null) {
						sg.w("String " + widget.getFieldname().toLowerCase());
					} else {
						sg.wl("ChoiceValue<"
								+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
								+ "ChoiceDefinition>[] " + widget.getFieldname().toLowerCase());
					}
				}
			}
		}
		sg.wl(",Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("  // TODO data filter is not used");
		sg.wl("		ArrayList<QueryCondition> andconditions = new ArrayList<QueryCondition>();");
		sg.wl("		TableAlias alias = AutolinkobjectQueryHelper.getRightObjectAliasForPotentialRightObject("
				+ rightobjectclass + "Definition.get" + rightobjectclass + "Definition());");

		this.generateSearchcriteria(searchwidgets, rightobjectclass, sg);

		sg.wl("		QueryCondition finalquerycondition=null;");
		sg.wl("		");
		sg.wl("		if (andconditions.size()>0) {");
		sg.wl("			AndQueryCondition andfinalquerycondition = new AndQueryCondition();");
		sg.wl("			for (int i=0;i<andconditions.size();i++) ");
		sg.wl("				andfinalquerycondition.addCondition(andconditions.get(i));");
		sg.wl("			finalquerycondition = andfinalquerycondition;");
		sg.wl("		}");
		sg.wl("			");
		sg.wl("		");
		sg.wl("		" + rightobjectclass + "[] result = " + linkobjectclass + ".getpotentialrightobject(left"
				+ leftobjectattribute + "id,new QueryFilter(finalquerycondition,null));");
		if (canorder) {

			sg.wl("		List<" + rightobjectclass + "> resultlist = Arrays.asList(result);");
			if (autolinkobject.getObjectforlink().getPropertyByName("UPDATELOG") != null) {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getUpdatetime().compareTo(a.getUpdatetime())));");
			} else {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getCreatetime().compareTo(a.getCreatetime())));");
			}
			sg.wl("		result = resultlist.toArray(new " + rightobjectclass + "[0]);");

		}
		sg.wl("		return result;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the source code for the normal search action for a data object
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchActionToFile(SourceGenerator sg, Module module) throws IOException {
		String actionname = "Search" + object.getName().toLowerCase() + "Action";
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		String objectattribute = StringFormatter.formatForAttribute(object.getName());
		boolean isaddress = false;
		SearchWidgetDefinition[] searchwidgets = object.getSearchWidgets();
		boolean canorder = false;
		if ((object.getPropertyByName("CREATIONLOG") != null) || (object.getPropertyByName("UPDATELOG") != null))
			canorder = true;

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		if (canorder) {
			sg.wl("import java.util.Arrays;");
			sg.wl("import java.util.Collections;");
			sg.wl("import java.util.List;");
		}
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.Abs" + actionname + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + objectattribute + "Page;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "Definition;");

		sg.wl("import org.openlowcode.server.data.storage.AndQueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorLike;");
		sg.wl("import org.openlowcode.server.data.storage.QueryOperatorEqual;");
		sg.wl("import org.openlowcode.server.data.storage.OrQueryCondition;");

		sg.wl("import org.openlowcode.server.data.storage.SimpleQueryCondition;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.data.properties.StoredobjectQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
			}
		}

		sg.wl("");
		sg.wl("public class Atg" + actionname + " extends Abs" + actionname + " implements AtgSearch" + objectattribute
				+ "Page." + objectclass + "Searcher<AbsSearch" + objectattribute + "Action.InlineActionRef> {");
		sg.wl("");
		sg.wl("	public Atg" + actionname + "(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	");
		sg.wl("");

		sg.wl("	@Override");
		if (isaddress) {
			sg.wl("	public SPage choosePage(ActionOutputData outputdata)  {");
		} else {
			sg.wl("	public SPage choosePage(" + objectclass + "[] " + objectattribute + ")  {");
		}
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		if (isaddress) {
			sg.w("	public ActionOutputData executeActionLogic(");

		} else {
			sg.w("	public " + objectclass + "[] executeActionLogic(");

		}

		boolean first = true;
		searchwidgets = object.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (first) {
						first = false;
					} else {
						sg.w(",");
					}
					if (widget.getMultipleChoiceCategory() == null) {
						sg.w("String " + searchwidgets[i].getFieldname().toLowerCase());
					} else {
						sg.wl("ChoiceValue<"
								+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
								+ "ChoiceDefinition>[] " + searchwidgets[i].getFieldname().toLowerCase());
					}
				}
			}
		}
		sg.wl("			" + (searchwidgets.length > 0 ? "," : "") + "Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("");
		sg.wl("		ArrayList<QueryCondition> andconditions = new ArrayList<QueryCondition>();");
		sg.wl("		TableAlias alias = " + objectclass + "Definition.get" + objectclass
				+ "Definition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);");

		this.generateSearchcriteria(searchwidgets, objectclass, sg);
		sg.wl("		QueryFilter additionalcondition = (datafilter!=null?datafilter.apply(alias):null);");
		sg.wl("		if (additionalcondition!=null) if (additionalcondition.getCondition()!=null) andconditions.add(datafilter.apply(alias).getCondition());");
		sg.wl("");
		sg.wl("		QueryCondition finalquerycondition=null;");
		sg.wl("		");
		sg.wl("		if (andconditions.size()>0) {");
		sg.wl("			AndQueryCondition andfinalquerycondition = new AndQueryCondition();");
		sg.wl("			for (int i=0;i<andconditions.size();i++) ");
		sg.wl("				andfinalquerycondition.addCondition(andconditions.get(i));");
		sg.wl("			finalquerycondition = andfinalquerycondition;");
		sg.wl("		}");
		sg.wl("			");
		sg.wl("		");
		sg.wl("");
		sg.wl("		" + objectclass + "[] result = " + objectclass
				+ ".getallactive(new QueryFilter(finalquerycondition,(additionalcondition!=null?additionalcondition.getAliases():null)));");
		sg.wl("		AtgMassupdate" + objectattribute + "Action.get().freezeUnauthorizedObjects(result);");
		if (canorder) {

			sg.wl("		List<" + objectclass + "> resultlist = Arrays.asList(result);");
			if (object.getPropertyByName("UPDATELOG") != null) {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getUpdatetime().compareTo(a.getUpdatetime())));");
			} else {
				sg.wl("		Collections.sort(resultlist,(a,b)->(b.getCreatetime().compareTo(a.getCreatetime())));");
			}
			sg.wl("		result = resultlist.toArray(new " + objectclass + "[0]);");

		}
		if (isaddress) {
			sg.wl("		return new ActionOutputData(result);");
		} else {
			sg.wl("		return result;");
		}
		sg.wl("	}");
		sg.wl("");

		sg.wl("	@Override");
		sg.wl("		public void setExtraAttributes(InlineActionRef specificinlineactionref)  {");
		sg.wl("			// do nothing");
		sg.wl("		}			");
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function) { ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					} else {
						sg.wl("		@Override");
						sg.wl("		public void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(InlineActionRef actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function) {  ");
						sg.wl("			actionref.set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(function);");
						sg.wl("			}");
					}
				}
			}
		}

		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	private void generateSearchcriteria(SearchWidgetDefinition[] searchwidgets, String objectclass, SourceGenerator sg)
			throws IOException {

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					String fieldname = StringFormatter.formatForAttribute(widget.getFieldname());
					String fieldclass = StringFormatter.formatForJavaClass(widget.getFieldname());
					if (widget.getMultipleChoiceCategory() == null) {

						sg.wl("		if (" + fieldname + "!=null) if (" + fieldname + ".length()>0) {");
						sg.wl("");
						sg.wl("			String " + fieldname + "extended = " + fieldname
								+ ".trim().replace('*','%')+\"%\";");
						sg.wl("			andconditions.add(new SimpleQueryCondition<String>(alias,");
						sg.wl("					" + objectclass + ".getDefinition().get" + fieldclass
								+ "FieldSchema(),");
						sg.wl("					new  QueryOperatorLike(),");
						sg.wl("					" + fieldname + "extended));");
						sg.wl("		}");
						sg.wl("		");
					} else {
						// --------------------- Choice Field in property ----------------------
						if (widget.isChoiceSearchInString()) {
							sg.wl("	if (" + fieldname + "!=null) if (" + fieldname + ".length>0) {");
							sg.wl("			OrQueryCondition " + fieldname + "orcondition = new OrQueryCondition();");
							sg.wl("			for (int i=0;i<" + fieldname + ".length;i++) {");
							sg.wl("				" + fieldname
									+ "orcondition.addCondition(new SimpleQueryCondition<String>(alias,");
							sg.wl("					" + objectclass + ".getDefinition().get" + fieldclass
									+ "FieldSchema(),");
							sg.wl("					new  QueryOperatorEqual(),");
							sg.wl("					" + fieldname + "[i].getStorageCode()));");
							sg.wl("			}");
							sg.wl("			andconditions.add(" + fieldname + "orcondition);");
							sg.wl("		}");
						} else {
							sg.wl("	if (" + fieldname + "!=null) if (" + fieldname + ".length>0) {");
							sg.wl("			OrQueryCondition " + fieldname + "orcondition = new OrQueryCondition();");
							sg.wl("			for (int i=0;i<" + fieldname + ".length;i++) {");
							sg.wl("				" + fieldname
									+ "orcondition.addCondition(new SimpleQueryCondition<ChoiceValue<"
									+ StringFormatter.formatForJavaClass(widget.getMultipleChoiceCategory().getName())
									+ "ChoiceDefinition>>(alias,");
							sg.wl("					" + objectclass + ".getDefinition().get" + fieldclass
									+ "FieldSchema(),");
							sg.wl("					new  QueryOperatorEqual(),");
							sg.wl("					" + fieldname + "[i]));");
							sg.wl("			}");
							sg.wl("			andconditions.add(" + fieldname + "orcondition);");
							sg.wl("		}");
						}
					}
				}

			}

		}
	}

	/**
	 * 
	 * 
	 * @return the search page for the given data object<br>
	 *         Note: the search page does not have any search attribute as input as
	 *         the search attributes are entered on the search page
	 */
	public PageDefinition generateSearchPage() {
		DynamicPageDefinition searchpagedefinition = new DynamicPageDefinition("SEARCH" + object.getName());
		searchpagedefinition.addInputParameter(
				new ChoiceArgument("PREFLANGUAGE", SystemModule.getSystemModule().getApplicationLocale()));
		searchpagedefinition.addInputParameter(
				new ChoiceArgument("PREFFILEENCODING", SystemModule.getSystemModule().getPreferedFileEncoding()));

		return searchpagedefinition;
	}

	/**
	 * generates the source code for the search page
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void generateSearchPageToFile(SourceGenerator sg, Module module) throws IOException {
		String pagename = "Search" + object.getName().toLowerCase() + "Page";
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		String objectattribute = StringFormatter.formatForAttribute(object.getName());
		SearchWidgetDefinition[] searchwidgets = object.getSearchWidgets();

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgFlatfileloaderfor" + objectattribute + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgGenerateflatfilesamplefor" + objectattribute
				+ "Action;");

		sg.wl("import " + module.getPath() + ".action.generated.AtgMassupdate" + objectattribute + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgPreparestandardcreate" + objectattribute
				+ "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgSearch" + objectattribute + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgPrepareupdate" + objectattribute + "Action;");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectattribute + "Action;");
		for (int i = 0; i < object.getActionOnSearchPageNumber(); i++) {
			StaticActionDefinition thisaction = object.getActionOnSeachPage(i);
			boolean generated = thisaction.isAutogenerated();
			sg.wl("import " + thisaction.getModule().getPath() + ".action." + (generated ? "generated.Atg" : "")
					+ StringFormatter.formatForJavaClass(thisaction.getName()) + "Action;");
		}
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AbsSearch" + objectattribute + "Page;");
		sg.wl("");
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");

		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectSCurve;");
		sg.wl("import org.openlowcode.server.graphic.widget.SMultipleChoiceTextField;");
		sg.wl("import org.openlowcode.tools.structure.ChoiceDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.MultipleChoiceDataEltType;");

		sg.wl("import org.openlowcode.server.data.SimpleFieldChoiceDefinition;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		sg.wl("import org.openlowcode.server.graphic.widget.SChoiceTextField;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SFileChooser;");
		sg.wl("import org.openlowcode.server.graphic.widget.SFileDownloader;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import org.openlowcode.server.graphic.widget.STabPane;");
		sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SCollapsibleBand;");

		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.tools.structure.ArrayDataEltType;");

		sg.wl("import org.openlowcode.server.data.message.TObjectDataEltType;");
		sg.wl("import org.openlowcode.tools.structure.TextDataEltType;");
		sg.wl("import org.openlowcode.tools.misc.NamedList;");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.getMultipleChoiceCategory() != null) {
				ChoiceCategory choicecategory = widget.getMultipleChoiceCategory();
				sg.wl("import " + choicecategory.getParentModule().getPath() + ".data.choice."
						+ StringFormatter.formatForJavaClass(choicecategory.getName()) + "ChoiceDefinition;");
			}
		}
		sg.wl("");
		sg.wl("");
		sg.wl("public class Atg" + pagename + " extends Abs" + pagename + " {");
		sg.wl("	private static Atg" + pagename + " singleton;");

		sg.wl("	/**");
		sg.wl("	 * this interface allows other actions to be used for search");
		sg.wl("	 *");
		sg.wl("	 */");
		sg.wl("	public interface " + objectclass + "Searcher<E extends SInlineActionRef> {");

		sg.wl("		E getInlineActionRef();");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						sg.wl("		void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(E actionref,Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> function); ");
					} else {
						sg.wl("		void set" + StringFormatter.formatForJavaClass(widget.getFieldname())
								+ "(E actionref,Function<SActionInputDataRef<MultipleChoiceDataEltType>, SActionDataLoc<MultipleChoiceDataEltType>> function);  ");
					}
				}
			}
		}

		// todo get all attributes from search action

		sg.wl("		SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<" + objectclass + ">>> getSearchresultfor"
				+ objectattribute + "Ref();");

		sg.wl("		/**");
		sg.wl("		 * Specifies if needed specific attributes (hardcoded) to the search action");
		sg.wl("		 * @param specificinlineactionref");
		sg.wl("		 */");
		sg.wl("");
		sg.wl("		void setExtraAttributes(E specificinlineactionref) ;");

		sg.wl("	}");

		sg.wl("	private class Search" + objectattribute + "Panel<E extends SInlineActionRef> extends SObjectSearcher<"
				+ objectclass + "> {");
		sg.wl("		private SObjectArray<" + objectclass + "> " + objectattribute + "searchresult;");
		sg.wl("		private E search" + objectattribute + "actionref;");
		sg.wl("		public Search" + objectattribute + "Panel(" + objectclass
				+ "Searcher<E> searcher,SPage parentpage,String name)  {");
		sg.wl("			this(searcher,parentpage,name,null);");
		sg.wl("		}");
		sg.wl("");
		sg.wl("	public Search" + objectattribute + "Panel(" + objectclass
				+ "Searcher<E> searcher,SPage parentpage,String name,SPageText titleofresultarray)  {");
		sg.wl("		this(searcher,parentpage,name,titleofresultarray,true);");
		sg.wl("	}");

		sg.wl("");
		sg.wl("		public Search" + objectattribute + "Panel(" + objectclass
				+ "Searcher<E> searcher,SPage parentpage,String name,SPageText titleofresultarray,boolean integrateresult)  {");
		sg.wl("			super(parentpage,name);");

		sg.wl("			SComponentBand searchcriteriapayload = new SComponentBand(SComponentBand.DIRECTION_DOWN, parentpage);");
		sg.wl("			SCollapsibleBand searchcriteria = new SCollapsibleBand(parentpage, searchcriteriapayload,\"Search criteria for a "
				+ object.getLabel() + "\", true);");
		sg.wl("			this.addElement(searchcriteria);		");

		sg.wl("			");
		sg.wl("			// Define Action");
		sg.wl("			search" + objectattribute + "actionref = searcher.getInlineActionRef();");
		sg.wl("			");
		sg.wl("			// Define search fields");
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition searchwidget = searchwidgets[i];
			if (searchwidget.isPrimary()) {
				Element element = searchwidget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (searchwidget.getMultipleChoiceCategory() == null) {
						sg.wl("			STextField " + searchwidget.getFieldname().toLowerCase()
								+ "entryfield = new STextField(\"" + searchwidget.getDisplayname() + "\",\"SEARCHFIELD"
								+ searchwidget.getFieldname().toUpperCase() + "\",\"Enter here the start of the "
								+ searchwidget.getDisplayname() + ", or empty to show all objects\",40, ");
						sg.wl("					\"\",true, parentpage, false, false, false,search" + objectattribute
								+ "actionref);");
						sg.wl("			searchcriteriapayload.addElement(" + searchwidget.getFieldname().toLowerCase()
								+ "entryfield);");
					} else {
						ChoiceCategory multiplechoice = searchwidget.getMultipleChoiceCategory();

						sg.wl("			SMultipleChoiceTextField " + searchwidget.getFieldname().toLowerCase()
								+ "entryfield = new SMultipleChoiceTextField(\"" + searchwidget.getDisplayname()
								+ "\",\"SEARCHFIELD" + searchwidget.getFieldname().toUpperCase()
								+ "\",\"Choose one or several of the " + searchwidget.getDisplayname() + "s\", ");
						sg.wl("			" + StringFormatter.formatForJavaClass(multiplechoice.getName())
								+ "ChoiceDefinition.get(),parentpage,false, false, false, false,null);");
						sg.wl("			searchcriteriapayload.addElement(" + searchwidget.getFieldname().toLowerCase()
								+ "entryfield);");

					}
				}

			}

		}
		sg.wl("			");
		sg.wl("			// Define button");
		sg.wl("			searcher.setExtraAttributes(search" + objectattribute + "actionref);");
		sg.wl("			");

		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition searchwidget = searchwidgets[i];
			if (searchwidget.isPrimary()) {
				Element element = searchwidget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (searchwidget.getMultipleChoiceCategory() == null) {
						sg.wl("			searcher.set" + StringFormatter.formatForJavaClass(searchwidget.getFieldname())
								+ "(search" + objectattribute + "actionref," + searchwidget.getFieldname().toLowerCase()
								+ "entryfield.getTextInput()); ");
					} else {
						sg.wl("			searcher.set" + StringFormatter.formatForJavaClass(searchwidget.getFieldname())
								+ "(search" + objectattribute + "actionref," + searchwidget.getFieldname().toLowerCase()
								+ "entryfield.getMultipleChoiceArrayInput());");

					}
				}
			}
		}

		sg.wl("			");
		sg.wl("			SActionButton searchbutton = new SActionButton(\"Search\", search" + objectattribute
				+ "actionref, parentpage);");
		sg.wl("			searchcriteriapayload.addElement(searchbutton);");
		sg.wl("			if (titleofresultarray!=null) this.addElement(titleofresultarray);");
		sg.wl("			");
		sg.wl("			// Define result table");
		sg.wl("			 " + objectattribute + "searchresult = new SObjectArray<" + objectclass + ">(\""
				+ objectattribute + "searchresult\",");
		sg.wl("					search" + objectattribute + "actionref,");
		sg.wl("					AtgSearch" + objectattribute + "Action.get().getSearchresultfor" + objectattribute
				+ "Ref(),");
		sg.wl("					" + objectclass + ".getDefinition(),");
		sg.wl("					parentpage);");
		sg.wl("			 " + objectattribute + "searchresult.setMinFieldPriority(-200);");
		sg.wl("			 " + objectattribute + "searchresult.setWarningForUnsavedEdition();");
		sg.wl("			 " + objectattribute + "searchresult.setAllowDataClear();");
		sg.wl("			 " + objectattribute + "searchresult.setAllowMultiSelect();");
		sg.wl("			if (integrateresult) this.addElement(" + objectattribute + "searchresult);");
		sg.wl("		}");
		sg.wl("");
		sg.wl("		public E getSearchInlineActionRef() {");
		sg.wl("			return this.search" + objectattribute + "actionref;");
		sg.wl("		}");
		sg.wl("");
		sg.wl("		@Override");
		sg.wl("		public SObjectArray<" + objectclass + "> getresultarray() {");
		sg.wl("			return " + objectattribute + "searchresult;");
		sg.wl("		}");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	public static SObjectSearcher<" + objectclass + "> getsearchpanel(SPage parentpage,String name)  {");
		sg.wl("		if (singleton==null) singleton = new AtgSearch" + objectattribute + "Page(null,null);");
		sg.wl("		return singleton.new Search" + objectattribute + "Panel((AtgSearch" + objectattribute
				+ "Action)(AtgSearch" + objectattribute + "Action.get()),parentpage,name);");
		sg.wl("	}");
		sg.wl("	");

		sg.wl("	public static SObjectSearcher<" + objectclass + "> getsearchpanel(" + objectclass
				+ "Searcher searcher,SPage parentpage,String name)  {");
		sg.wl("		if (singleton==null) singleton = new AtgSearch" + objectattribute + "Page(null,null);");
		sg.wl("		return singleton.new Search" + objectattribute + "Panel(searcher,parentpage,name);");
		sg.wl("	}");
		sg.wl("	");

		sg.wl("	@Override");
		sg.wl("	public String generateTitle(ChoiceValue<ApplocaleChoiceDefinition> preflanguage,ChoiceValue<PreferedfileencodingChoiceDefinition> preffileencoding) {");
		sg.wl("		String objectdisplay = \"Search " + object.getLabel() + "\";");
		sg.wl("		return objectdisplay;");
		sg.wl("	}");

		sg.wl("	public AtgSearch" + objectattribute
				+ "Page(ChoiceValue<ApplocaleChoiceDefinition> preflanguage,ChoiceValue<PreferedfileencodingChoiceDefinition> preffileencoding)  {");
		sg.wl("		super(preflanguage,preffileencoding);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand pageband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
		sg.wl("		");
		sg.wl("		pageband.addElement(new SPageText(\"Create new " + object.getLabel()
				+ "\",SPageText.TYPE_TITLE,this));");
		sg.wl("		SComponentBand createband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
		sg.wl("		pageband.addElement(createband);");

		if (!object.isNonOptionalContextForCreation()) {

			sg.wl("		AtgPreparestandardcreate" + objectattribute
					+ "Action.ActionRef preparestandardcreateactionref = AtgPreparestandardcreate" + objectattribute
					+ "Action.get().getActionRef();");

			// add null optional context for creation

			for (int i = 0; i < object.propertylist.getSize(); i++) {
				Property<?> thisproperty = object.propertylist.get(i);
				for (int j = 0; j < thisproperty.getContextDataForCreationSize(); j++) {

					sg.wl("		preparestandardcreateactionref.set"
							+ StringFormatter.formatForJavaClass(thisproperty.getContextDataForCreation(j).getName())
							+ "(null);");

				}
			}

			sg.wl("		SActionButton createbutton = new SActionButton(\"Create new " + object.getLabel()
					+ "\",\"Enter data directly in the tool\",preparestandardcreateactionref,this);");
			sg.wl("		createband.addElement(createbutton);");

		}

		sg.wl("		// popupprocessing");
		sg.wl("			AtgFlatfileloaderfor" + objectattribute + "Action.ActionRef flatfileload = AtgFlatfileloaderfor"
				+ objectattribute + "Action.get().getActionRef();");
		sg.wl("			SComponentBand csvloadpopup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("			csvloadpopup.addElement(new SPageText(\"Load from csv or excel file\",SPageText.TYPE_NORMAL, this));");
		sg.wl("			SFileChooser csvfilechooser = new SFileChooser(this, \"CSVFILECHOSER\",\"Select File\");");
		sg.wl("			csvloadpopup.addElement(csvfilechooser);");
		sg.wl("		SComponentBand cslvloadcbandcontent = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("		SCollapsibleBand csvloadcband = new SCollapsibleBand(this, cslvloadcbandcontent, \"Settings\", false);");

		sg.wl("		csvloadpopup.addElement(csvloadcband);");

		sg.wl("			SChoiceTextField<ApplocaleChoiceDefinition> csvloadlocale = new SChoiceTextField<ApplocaleChoiceDefinition>");
		sg.wl("				(\"Locale\",\"APPLOCALE\",\"determines csv and number format, default is US\", ApplocaleChoiceDefinition.get(),");
		sg.wl("				null, this, true, false, false, false, null);");
		sg.wl("			csvloadlocale.setLinkedData(this.getPreflanguage());");
		sg.wl("			cslvloadcbandcontent.addElement(csvloadlocale);");

		sg.wl("			SChoiceTextField<PreferedfileencodingChoiceDefinition> csvpreffileencoding = new SChoiceTextField<PreferedfileencodingChoiceDefinition>");
		sg.wl("				(\"File Encoding\",\"FILEENCODING\",\"the way files are read, default is ISO\", PreferedfileencodingChoiceDefinition.get(),");
		sg.wl("				null, this, true, false, false, false, null);");
		sg.wl("			csvpreffileencoding.setLinkedData(this.getPreffileencoding());");
		sg.wl("			cslvloadcbandcontent.addElement(csvpreffileencoding);");

		sg.wl("			flatfileload.setLocale(csvloadlocale.getChoiceInput());");
		sg.wl("			flatfileload.setEncoding(csvpreffileencoding.getChoiceInput());");
		sg.wl("			flatfileload.setFlatfile(csvfilechooser.getLargeBinaryInput());");
		sg.wl("			SActionButton flatfileloadbutton = new SActionButton(\"Load file\",flatfileload,true,this);");
		sg.wl("			csvloadpopup.addElement(flatfileloadbutton);");
		sg.wl("			SPopupButton flatfilepopupbutton = new SPopupButton(this, csvloadpopup,\"File Loader\",\"Load from flat file with supported format. Sample available on button just on the right\",false,flatfileload);");

		sg.wl("			AtgGenerateflatfilesamplefor" + objectattribute
				+ "Action.InlineActionRef flatfilesample = AtgGenerateflatfilesamplefor" + objectattribute
				+ "Action.get().getInlineActionRef();");
		sg.wl("			SActionButton flatfilesamplebutton = new SActionButton(\"Sample Loading File\",\"Provides information on the file format\",");
		sg.wl("					flatfilesample,this);");
		sg.wl("			SFileDownloader csvsampledownloader = new SFileDownloader(\"CSVSAMPLE\", this, flatfilesample, AtgGenerateflatfilesamplefor"
				+ objectattribute + "Action.get().getSamplefileRef());	");

		sg.wl("		createband.addElement(flatfilepopupbutton);");
		sg.wl("		createband.addElement(flatfilesamplebutton);");
		sg.wl("		createband.addElement(csvsampledownloader);");
		for (int i = 0; i < object.getActionOnSearchPageNumber(); i++) {
			StaticActionDefinition thisaction = object.getActionOnSeachPage(i);
			String thisactionattribute = StringFormatter.formatForAttribute(thisaction.getName());
			String thisactionclass = StringFormatter.formatForJavaClass(thisaction.getName());
			boolean generated = thisaction.isAutogenerated();
			String finalactionclass = (generated ? "Atg" : "") + thisactionclass + "Action";
			sg.wl("		" + finalactionclass + ".ActionRef " + thisactionattribute + "ref = " + finalactionclass
					+ ".get().getActionRef();");
			sg.wl("		SActionButton " + thisactionattribute + "button = new SActionButton(\""
					+ thisaction.getButtonlabel() + "\"," + thisactionattribute + "ref,this);");
			sg.wl("		createband.addElement(" + thisactionattribute + "button);");

		}

		sg.wl("		pageband.addElement(new SPageText(\"Search existing " + object.getLabel()
				+ "\",SPageText.TYPE_TITLE,this));");

		sg.wl("		SObjectSearcher<" + objectclass + "> objectsearcher = new Search" + objectattribute
				+ "Panel((AtgSearch" + objectattribute + "Action)(AtgSearch" + objectattribute
				+ "Action.get()),this,\"DEFAULT\",");
		sg.wl("				null,false);");
		sg.wl("		pageband.addElement(objectsearcher);");
		sg.wl("		SObjectArray<" + objectclass + "> " + objectattribute
				+ "searchresult = objectsearcher.getresultarray();");
		sg.wl("		" + objectattribute + "searchresult.setWarningForUnsavedEdition();");
		sg.wl("		// update in array");
		sg.wl("		" + objectattribute + "searchresult.setMinFieldPriority(-200); ");
		sg.wl("		AtgMassupdate" + objectattribute + "Action.InlineActionRef update" + objectattribute
				+ "actionref = AtgMassupdate" + objectattribute + "Action.get().getInlineActionRef(); ");
		sg.wl("		update" + objectattribute + "actionref.set" + objectclass + "(" + objectattribute
				+ "searchresult.getActiveObjectArray()); ");
		String updatenote = "";
		if (object.IsIterated()) {
			updatenote = ",false,true";
			sg.wl("		update" + objectattribute + "actionref.setUpdatenote(" + objectattribute
					+ "searchresult.getUpdateNoteInput()); ");
		}
		sg.wl("		" + objectattribute + "searchresult.addUpdateAction(update" + objectattribute
				+ "actionref,null,AtgMassupdate" + objectattribute + "Action.get().getUpdated" + objectattribute
				+ "Ref()" + updatenote + ");");

		sg.wl("		AtgShow" + objectattribute + "Action.ActionRef show" + objectattribute + "actionref = AtgShow"
				+ objectattribute + "Action.get().getActionRef();");
		sg.wl("		show" + objectattribute + "actionref.setId(" + objectattribute + "searchresult.getAttributeInput("
				+ objectclass + ".getIdMarker())); ");
		sg.wl("		" + objectattribute + "searchresult.addDefaultAction(show" + objectattribute + "actionref);");
		sg.wl("		");

		sg.wl("		");
		if ((object.hasLifecycle()) && (object.propertylist.lookupOnName("NUMBERED") != null)
				&& (!object.isSCurveHidden())) {

			sg.wl("		STabPane resulttabpane = new STabPane(this);");
			sg.wl("		SComponentBand arrayresult = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
			// sg.wl(" arrayresult.addElement(buttonband);");
			sg.wl("		arrayresult.addElement(" + objectattribute + "searchresult);");

			sg.wl("		resulttabpane.addElement(arrayresult,\"Result\");");

			sg.wl("		SComponentBand scurveband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
			sg.wl("		SObjectSCurve scurve = new SObjectSCurve<" + objectclass + ">(\"resultscurve\", ((Search"
					+ objectattribute + "Panel)objectsearcher).search" + objectattribute + "actionref,AtgSearch"
					+ objectattribute + "Action.get().getSearchresultfor" + objectattribute + "Ref()," + objectclass
					+ ".getDefinition(), this);");
			sg.wl("		scurve.addFieldToShow(" + objectclass + ".getNrFieldMarker());");
			if (object.propertylist.lookupOnName("NAMED") != null)
				sg.wl("		scurve.addFieldToShow(" + objectclass + ".getObjectnameFieldMarker());");
			sg.wl("		scurve.setRed(" + objectclass + ".getCreatetimeFieldMarker());");
			sg.wl("		scurve.setGreen(" + objectclass + ".getFinalstatetimeFieldMarker());");
			if (object.propertylist.lookupOnName("TARGETDATE") != null)
				sg.wl("		scurve.setGreenDotted(" + objectclass + ".getTargetdateFieldMarker());	");
			sg.wl("		AtgShow" + objectattribute + "Action.ActionRef show" + objectattribute + "fromcurve = AtgShow"
					+ objectattribute + "Action.get().getActionRef();");
			sg.wl("		scurve.addDefaultAction(show" + objectattribute + "fromcurve);");
			sg.wl("		show" + objectattribute + "fromcurve.setId(scurve.getAttributeInput(" + objectclass
					+ ".getIdMarker())); ");

			sg.wl("		scurveband.addElement(scurve);");
			sg.wl("		resulttabpane.addElement(scurveband,\"S-Curve\");");
			sg.wl("		pageband.addElement(resulttabpane);");

		} else {
			sg.wl("		pageband.addElement(" + objectattribute + "searchresult);");

		}
		sg.wl("		return pageband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

	/**
	 * generates a search action for all right objects with the same parents as the
	 * left object
	 * 
	 * @param linkobject                property of the link between left and right
	 *                                  objects
	 * @param rightobjectparentproperty the parent property on the right object to
	 *                                  consider
	 * @return the action definition class of the action
	 */
	public ActionDefinition generateSearchActionWithParent(
			LinkObject<?, ?> linkobject,
			LinkedToParent<?> rightobjectparentproperty) {
		DataObjectDefinition rightobject = linkobject.getRightobjectforlink();
		DataObjectDefinition parentobject = rightobjectparentproperty.getParentObjectForLink();

		DynamicActionDefinition searchactionforparent = new DynamicActionDefinition(
				"SEARCH" + rightobject.getName().toUpperCase() + "WITHPARENT"
						+ rightobjectparentproperty.getInstancename().toUpperCase(),
				true);
		searchactionforparent.addInputArgument(new ObjectIdArgument("PARENTID", parentobject));
		searchactionforparent.forceNoAddress();

		// add normal attributes for search object
		SearchWidgetDefinition[] searchwidgets = rightobject.getSearchWidgets();
		for (int i = 0; i < searchwidgets.length; i++) {
			SearchWidgetDefinition widget = searchwidgets[i];
			if (widget.isPrimary()) {
				Element element = widget.getElement();
				if (element instanceof ExternalElement) {
					ExternalElement externalelement = (ExternalElement) element;
					element = externalelement.getReferencedPropertyElement();
				}
				if (element instanceof StringStoredElement) {
					if (widget.getMultipleChoiceCategory() == null) {
						searchactionforparent.addInputArgument(new StringArgument(widget.getFieldname(), 64));
					} else {
						searchactionforparent.addInputArgument(
								new MultipleChoiceArgument(widget.getFieldname(), widget.getMultipleChoiceCategory()));
					}
				}
			}
		}
		searchactionforparent.addOutputArgumentAsAccessCriteria(new ArrayArgument(
				new ObjectArgument("SEARCHRESULTFOR" + rightobject.getName().toUpperCase(), rightobject)));

		return searchactionforparent;
	}

	/**
	 * @param name name of the data object
	 * @param sg        source generator
	 * @param module    parent module
	 * @throws IOException if anyting bad happens while writing the source code
	 */
	public static void generateLaunchSearchActionToFile(String name,SourceGenerator sg, Module module) throws IOException {
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + objectvariable + "Page;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("");
		sg.wl("public class AtgLaunchsearch" + objectvariable + "Action extends AbsLaunchsearch" + objectvariable
				+ "Action {");
		sg.wl("");
		sg.wl("	public AtgLaunchsearch" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		return new ActionOutputData(OLcServer.getServer().getCurrentUser().getPreflang(),OLcServer.getServer().getCurrentUser().getPreffileenc());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData outputdata)  {");
		sg.wl("		return new AtgSearch" + objectvariable
				+ "Page(outputdata.getPreflanguage(),outputdata.getPreffileencoding());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}
}
