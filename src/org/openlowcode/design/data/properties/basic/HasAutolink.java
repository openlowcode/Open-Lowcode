/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.TwoObjectsArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * This property will be added to objects that are targets of an
 * {@link org.openlowcode.design.data.properties.basic.AutolinkObject}.
 * 
 * Warning: this should not be added manually by the developer. <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the object of the property
 */
public class HasAutolink<E extends DataObjectDefinition>
		extends
		Property<HasAutolink<E>> {

	private NamedList<DynamicActionDefinition> actionsonselectedlinkid;
	private NamedList<DynamicActionDefinition> actiononselectedrightobjectid;
	private UniqueIdentified uniqueidentified;
	private E linkdataobject;
	private DataObjectDefinition objectthathaslink;
	private AutolinkObject<E> autolinkobject;
	private WidgetDisplayPriority commonorfromlefttablepriority;
	private WidgetDisplayPriority righttablepriority;

	/**
	 * @return the widget for the auto-link as seen from the left, or the single
	 *         widget if links are symetric
	 */
	public Widget getWidgetForLeftOrCommonAutolink() {
		return new WidgetForLeftOrCommonAutolink(this);
	}

	/**
	 * @return the widget for the auto-link widget as seen from the right
	 */
	public Widget getWidgetForRightAutolink() {
		return new WidgetForRightAutolink(this);
	}

	/**
	 * The widget for the auto-link as seen from the left, or the single common
	 * widget for auto-link. This will print the code for the widget in the object
	 * page
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class WidgetForLeftOrCommonAutolink
			extends
			Widget {
		private HasAutolink<E> hasautolink;

		/**
		 * creates a widget for left or common auto-link
		 * 
		 * @param hasautolink has autolink property on this object
		 */
		WidgetForLeftOrCommonAutolink(HasAutolink<E> hasautolink) {
			super("WIDGETFORLEFTORCOMMON");
			this.hasautolink = hasautolink;
		}

		@Override
		public String[] getImportStatements() {
			Module module = hasautolink.getParent().getOwnermodule();
			ArrayList<String> importstatements = new ArrayList<String>();
			importstatements.add("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SPopupButton;");
			String linkobjectvariable = StringFormatter
					.formatForAttribute(hasautolink.getLinkObjectDefinition().getName());
			String linkobjectclass = StringFormatter
					.formatForJavaClass(hasautolink.getLinkObjectDefinition().getName());
			importstatements
					.add("import " + module.getPath() + ".action.generated.AtgCreate" + linkobjectvariable + "Action;");
			importstatements.add("import " + module.getPath() + ".action.generated.AtgDeleteautolink"
					+ linkobjectvariable + "andshowobjectAction;");

			importstatements.add("import " + module.getPath() + ".data." + linkobjectclass + "Definition;");
			AutolinkObject<?> autolinkobject = hasautolink.getLinkObjectProperty();
			if (autolinkobject.getBusinessRuleNumber() > 0) {
				importstatements.add("import " + module.getPath() + ".action.generated.AtgSearchright"
						+ hasautolink.getParent().getName().toLowerCase() + "for"
						+ hasautolink.getLinkObjectDefinition().getName().toLowerCase() + "Action;");
			}
			return importstatements.toArray(new String[0]);
		}

		@Override
		public void generateWidgetCode(SourceGenerator sg, Module module, String locationname) throws IOException {
			DataObjectDefinition linkobject = hasautolink.getLinkObjectDefinition();
			DataObjectDefinition dataobject = hasautolink.getParent();
			String objectvariable = StringFormatter.formatForAttribute(hasautolink.getParent().getName());
			String objectclass = StringFormatter.formatForJavaClass(hasautolink.getParent().getName());

			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String leftandrightobjectvariable = StringFormatter.formatForAttribute(dataobject.getName());
			String leftandrightobjectclass = StringFormatter.formatForJavaClass(dataobject.getName());
			sg.wl("");
			sg.wl("		// ------------------------------------------------------------------------------------------");
			sg.wl("		// Display " + linkobjectclass + " from left");
			sg.wl("		// ------------------------------------------------------------------------------------------");
			sg.wl("");
			if ((hasautolink.getRelatedAutolinkProperty().isSymetricLink())
					|| (!hasautolink.getRelatedAutolinkProperty().isShowLinkTree())) {
				sg.wl("		" + locationname + ".addElement(new SPageText(\""
						+ hasautolink.getRelatedAutolinkProperty().getLabelFromLeft()
						+ "\",SPageText.TYPE_TITLE,this));");

				sg.wl("		SObjectArray<" + linkobjectclass + "> left" + linkobjectvariable + "s = new SObjectArray<"
						+ linkobjectclass + ">(\"LEFT" + linkobjectclass.toUpperCase() + "\",");
				sg.wl("				this.getLefthasautolinkfor" + linkobjectvariable + "(),");
				sg.wl("				" + linkobjectclass + ".getDefinition(),");
				sg.wl("				this);");
			} else {
				sg.wl("		" + locationname + ".addElement(new SPageText(\""
						+ hasautolink.getRelatedAutolinkProperty().getLabelFromLeft()
						+ "\",SPageText.TYPE_TITLE,this));");

				sg.wl("		SObjectTreeArray<" + linkobjectclass + "> left" + linkobjectvariable
						+ "s = new SObjectTreeArray<" + linkobjectclass + ">(\"LEFT" + linkobjectclass.toUpperCase()
						+ "\",");
				sg.wl("				this.getLefthasautolinkfor" + linkobjectvariable + "(),");
				sg.wl("				" + linkobjectclass + ".getDefinition(),");
				sg.wl("				this);");
			}
			sg.wl("		left" + linkobjectvariable + "s.addDisplayProfile(" + linkobjectclass + "Definition.get"
					+ linkobjectclass + "Definition().getDisplayProfileHideleftobjectfields());");
			sg.wl("		" + locationname + ".addElement(left" + linkobjectvariable + "s);");
			sg.wl("		");
			sg.wl("		AtgShow" + leftandrightobjectvariable + "Action.ActionRef showright"
					+ leftandrightobjectvariable + "for" + linkobjectvariable + "action = AtgShow"
					+ leftandrightobjectvariable + "Action.get().getActionRef();");
			sg.wl("		showright" + leftandrightobjectvariable + "for" + linkobjectvariable + "action.setId(left"
					+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getRgidMarker()));");
			sg.wl("		left" + linkobjectvariable + "s.addDefaultAction(showright" + leftandrightobjectvariable + "for"
					+ linkobjectvariable + "action);");
			sg.wl("		");

			sg.wl("		SComponentBand addtoleft" + linkobjectvariable
					+ "s = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
			sg.wl("		");
			sg.wl("		// add link");
			sg.wl("		SPageText titleforleft" + linkobjectvariable + " = new  SPageText(\"Add a new "
					+ linkobject.getLabel() + " to the " + dataobject.getLabel() + "\",SPageText.TYPE_TITLE,this);");
			sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(titleforleft" + linkobjectvariable + ");");
			sg.wl("				");

			AutolinkObject<?> autolinkobjectproperty = hasautolink.getLinkObjectProperty();
			if (autolinkobjectproperty.getBusinessRuleNumber() > 0) {
				// if business rule on link, use specific object searcher for right object
				sg.wl("		SObjectSearcher<" + leftandrightobjectclass + "> " + leftandrightobjectvariable
						+ "searchforaddtoleft" + linkobjectvariable + "s = AtgSearch" + leftandrightobjectvariable
						+ "Page.getsearchpanel(");
				sg.wl("			((AtgSearchright" + leftandrightobjectvariable + "for" + linkobjectvariable
						+ "Action)AtgSearchright" + leftandrightobjectvariable + "for" + linkobjectvariable
						+ "Action.get()).new Specific" + leftandrightobjectclass + "Searcher(");
				sg.wl("					objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getIdMarker())),");
				sg.wl("			this,\"" + linkobject.getName().toUpperCase() + "SEARCHLEFT\");");
				sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(" + leftandrightobjectvariable
						+ "searchforaddtoleft" + linkobjectvariable + "s);");
				sg.wl("");
			} else {
				// if no business rule on link, use standard object searcher for right object
				sg.wl("		SObjectSearcher<" + leftandrightobjectclass + "> " + leftandrightobjectvariable
						+ "searchforaddtoleft" + linkobjectvariable + "s = AtgSearch" + leftandrightobjectvariable
						+ "Page.getsearchpanel(this,\"" + leftandrightobjectclass + "SEARCHFORADDLEFT" + linkobjectclass
						+ "\");");
				sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(" + leftandrightobjectvariable
						+ "searchforaddtoleft" + linkobjectvariable + "s);");
				sg.wl("");
			}
			sg.wl("		" + leftandrightobjectvariable + "searchforaddtoleft" + linkobjectvariable
					+ "s.getresultarray().setAllowMultiSelect();");

			sg.wl("		SObjectDisplay<" + linkobjectclass + "> blanklinkforaddtoleft" + linkobjectvariable
					+ "s = new  SObjectDisplay<" + linkobjectclass + ">(\"LEFTBLANK"
					+ linkobject.getName().toUpperCase() + "FORADD\",");

			sg.wl("			this.getHasautolinkfor" + linkobjectvariable + "blankforadd(), " + linkobjectclass
					+ ".getDefinition(),this, false);");
			sg.wl("		blanklinkforaddtoleft" + linkobjectvariable + "s.setHideReadOnly();");
			sg.wl("				");
			sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(blanklinkforaddtoleft" + linkobjectvariable
					+ "s);");
			sg.wl("				");
			sg.wl("		AtgCreate" + linkobjectvariable + "Action.ActionRef createlinkactionforaddtoleft"
					+ linkobjectvariable + "s = AtgCreate" + linkobjectvariable + "Action.get().getActionRef();");
			sg.wl("				");
			sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.setLeft" + objectvariable
					+ "id(objectdisplaydefinition.getOneElementArrayInput(" + objectclass + ".getIdMarker())); ");
			sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.set" + linkobjectclass
					+ "(blanklinkforaddtoleft" + linkobjectvariable + "s.getObjectInput()); ");
			sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.setRight" + leftandrightobjectvariable
					+ "id(" + leftandrightobjectvariable + "searchforaddtoleft" + linkobjectvariable
					+ "s.getresultarray().getAttributeArrayInput(" + leftandrightobjectclass + ".getIdMarker())); ");
			sg.wl("		createlinkactionforaddtoleft" + linkobjectvariable + "s.set" + objectclass
					+ "idobjecttoshow(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker())); ");

			sg.wl("					");
			sg.wl("		SActionButton createlinkbuttonforaddtoleft" + linkobjectvariable
					+ " = new SActionButton(\"Add Link\", createlinkactionforaddtoleft" + linkobjectvariable
					+ "s, this);");
			sg.wl("		addtoleft" + linkobjectvariable + "s.addElement(createlinkbuttonforaddtoleft"
					+ linkobjectvariable + ");");
			sg.wl("		");
			sg.wl("		SPopupButton addtoleft" + linkobjectvariable + "sbutton = new SPopupButton(this,addtoleft"
					+ linkobjectvariable + "s,\"Add\",\"you can link an existing " + dataobject.getLabel() + " to this "
					+ dataobject.getLabel() + " through the "
					+ hasautolink.getRelatedAutolinkProperty().getParent().getLabel()
					+ " Link\",createlinkactionforaddtoleft" + linkobjectvariable + "s);");

			sg.wl("		SComponentBand left" + linkobjectvariable
					+ "buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
			sg.wl("		left" + linkobjectvariable + "buttonband.addElement(addtoleft" + linkobjectvariable
					+ "sbutton);");
			sg.wl("		AtgDeleteautolink" + linkobjectvariable + "andshowobjectAction.ActionRef deleteoneofleft"
					+ linkobjectvariable + " = AtgDeleteautolink" + linkobjectvariable
					+ "andshowobjectAction.get().getActionRef();");
			sg.wl("		deleteoneofleft" + linkobjectvariable + ".set" + linkobjectclass + "id(left"
					+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getIdMarker())); ");

			sg.wl("		");
			sg.wl("		deleteoneofleft" + linkobjectvariable + ".set" + objectclass
					+ "idtoshow(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
			sg.wl("		SActionButton deleteoneofleft" + linkobjectvariable
					+ "button = new SActionButton(\"Delete selected link\", deleteoneofleft" + linkobjectvariable
					+ ", this);");
			sg.wl("		left" + linkobjectvariable + "buttonband.addElement(deleteoneofleft" + linkobjectvariable
					+ "button);");
			sg.wl("		");
			sg.wl("		" + locationname + ".addElement(left" + linkobjectvariable + "buttonband);");

			sg.wl("");

		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return this.hasautolink.commonorfromlefttablepriority;
		}

	}

	/**
	 * the widget for auto-link seen from the right
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class WidgetForRightAutolink
			extends
			Widget {
		private HasAutolink<E> hasautolink;

		/**
		 * creates the widget for auto-link as seen from the right
		 * 
		 * @param hasautolink has auto-link property on the current object (the object
		 *                    being linked to)
		 */
		WidgetForRightAutolink(HasAutolink<E> hasautolink) {
			super("WIDGETFORLEFTORCOMMON");
			this.hasautolink = hasautolink;
		}

		@Override
		public String[] getImportStatements() {
			return new String[0];
		}

		@Override
		public void generateWidgetCode(SourceGenerator sg, Module module, String locationname) throws IOException {
			DataObjectDefinition linkobject = hasautolink.getLinkObjectDefinition();
			DataObjectDefinition dataobject = hasautolink.getParent();
			String objectvariable = StringFormatter.formatForAttribute(hasautolink.getParent().getName());
			String objectclass = StringFormatter.formatForJavaClass(hasautolink.getParent().getName());

			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String leftandrightobjectvariable = StringFormatter.formatForAttribute(dataobject.getName());
			String leftandrightobjectclass = StringFormatter.formatForJavaClass(dataobject.getName());
			AutolinkObject<?> autolinkobjectproperty = hasautolink.getRelatedAutolinkProperty();
			// -------------------------------------- treat autolink as seen from right only
			// if not autolink
			if (!autolinkobjectproperty.isSymetricLink()) {
				sg.wl("");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("		// Display " + linkobjectclass + " from right");
				sg.wl("		// ------------------------------------------------------------------------------------------");
				sg.wl("");
				sg.wl("		" + locationname + ".addElement(new SPageText(\""
						+ hasautolink.getRelatedAutolinkProperty().getLabelFromRight()
						+ "\",SPageText.TYPE_TITLE,this));");
				sg.wl("		SObjectArray<" + linkobjectclass + "> right" + linkobjectvariable + "s = new SObjectArray<"
						+ linkobjectclass + ">(\"RIGHT" + linkobjectclass.toUpperCase() + "\",");
				sg.wl("				this.getRighthasautolinkfor" + linkobjectvariable + "(),");
				sg.wl("				" + linkobjectclass + ".getDefinition(),");
				sg.wl("				this);");
				sg.wl("		right" + linkobjectvariable + "s.addDisplayProfile(" + linkobjectclass + "Definition.get"
						+ linkobjectclass + "Definition().getDisplayProfileHiderightobjectfields());");

				sg.wl("		" + locationname + ".addElement(right" + linkobjectvariable + "s);");
				sg.wl("		");
				sg.wl("		AtgShow" + leftandrightobjectvariable + "Action.ActionRef showleft"
						+ leftandrightobjectvariable + "for" + linkobjectvariable + "action = AtgShow"
						+ leftandrightobjectvariable + "Action.get().getActionRef();");
				sg.wl("		showleft" + leftandrightobjectvariable + "for" + linkobjectvariable + "action.setId(right"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getLfidMarker())); ");
				sg.wl("		right" + linkobjectvariable + "s.addDefaultAction(showleft" + leftandrightobjectvariable
						+ "for" + linkobjectvariable + "action);");
				sg.wl("		");
				sg.wl("		SComponentBand addtoright" + linkobjectvariable
						+ "s = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");

				if (autolinkobjectproperty.getBusinessRuleNumber() > 0) {
					// if business rule on link, use specific object searcher for right object
					sg.wl("		SObjectSearcher<" + leftandrightobjectclass + "> " + leftandrightobjectvariable
							+ "searchforaddtoright" + linkobjectvariable + "s = AtgSearch" + leftandrightobjectvariable
							+ "Page.getsearchpanel(");

					sg.wl("			((AtgSearchright" + leftandrightobjectvariable + "for" + linkobjectvariable
							+ "Action)AtgSearchright" + leftandrightobjectvariable + "for" + linkobjectvariable
							+ "Action.get()).new Specific" + leftandrightobjectclass + "Searcher(");
					sg.wl("					objectdisplaydefinition.getAttributeInput(" + objectclass
							+ ".getIdMarker())),");
					sg.wl("			this,\"" + linkobject.getName().toUpperCase() + "SEARCHRIGHT\");");
					sg.wl("		addtoright" + linkobjectvariable + "s.addElement(" + leftandrightobjectvariable
							+ "searchforaddtoright" + linkobjectvariable + "s);");
					sg.wl("");
				} else {
					// if no business rule on link, use standard object searcher for right object
					sg.wl("		SObjectSearcher<" + leftandrightobjectclass + "> " + leftandrightobjectvariable
							+ "searchforaddtoright" + linkobjectvariable + "s = AtgSearch" + leftandrightobjectvariable
							+ "Page.getsearchpanel(this,\"" + leftandrightobjectclass + "SEARCHFORADDRIGHT"
							+ linkobjectclass + "\");");
					sg.wl("		addtoright" + linkobjectvariable + "s.addElement(" + leftandrightobjectvariable
							+ "searchforaddtoright" + linkobjectvariable + "s);");
					sg.wl("");
				}
				sg.wl("		" + leftandrightobjectvariable + "searchforaddtoright" + linkobjectvariable
						+ "s.getresultarray().setAllowMultiSelect();");

				sg.wl("		SObjectDisplay<" + linkobjectclass + "> blanklinkforaddtoright" + linkobjectvariable
						+ "s = new  SObjectDisplay<" + linkobjectclass + ">(\"RIGHTBLANK"
						+ linkobject.getName().toUpperCase() + "FORADDTORIGHT\",");

				sg.wl("			this.getHasautolinkfor" + linkobjectvariable + "blankforadd(), " + linkobjectclass
						+ ".getDefinition(),this, false);");
				sg.wl("		blanklinkforaddtoright" + linkobjectvariable + "s.setHideReadOnly();");
				sg.wl("				");
				sg.wl("		addtoright" + linkobjectvariable + "s.addElement(blanklinkforaddtoright"
						+ linkobjectvariable + "s);");
				sg.wl("				");
				sg.wl("		AtgCreate" + linkobjectvariable + "Action.ActionRef createlinkactionforaddtoright"
						+ linkobjectvariable + "s = AtgCreate" + linkobjectvariable + "Action.get().getActionRef();");
				sg.wl("				");
				sg.wl("		createlinkactionforaddtoright" + linkobjectvariable + "s.setLeft"
						+ leftandrightobjectvariable + "id(" + leftandrightobjectvariable + "searchforaddtoright"
						+ linkobjectvariable + "s.getresultarray().getAttributeArrayInput(" + leftandrightobjectclass
						+ ".getIdMarker())); ");
				sg.wl("		createlinkactionforaddtoright" + linkobjectvariable + "s.set" + linkobjectclass
						+ "(blanklinkforaddtoright" + linkobjectvariable + "s.getObjectInput()); ");
				sg.wl("		createlinkactionforaddtoright" + linkobjectvariable + "s.setRight" + objectvariable
						+ "id(objectdisplaydefinition.getOneElementArrayInput(" + objectclass + ".getIdMarker())); ");
				sg.wl("		createlinkactionforaddtoright" + linkobjectvariable + "s.set" + objectclass
						+ "idobjecttoshow(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getIdMarker())); ");

				sg.wl("					");
				sg.wl("		SActionButton createlinkbuttonforaddtoright" + linkobjectvariable
						+ " = new SActionButton(\"Add Link\", createlinkactionforaddtoright" + linkobjectvariable
						+ "s, this);");
				sg.wl("		addtoright" + linkobjectvariable + "s.addElement(createlinkbuttonforaddtoright"
						+ linkobjectvariable + ");");
				sg.wl("		");
				sg.wl("		SPopupButton addtoright" + linkobjectvariable + "sbutton = new SPopupButton(this,addtoright"
						+ linkobjectvariable + "s,\"Add\",\"you can link an existing " + dataobject.getLabel()
						+ " to this " + dataobject.getLabel() + " through the "
						+ hasautolink.getRelatedAutolinkProperty().getParent().getLabel()
						+ " Link\",createlinkactionforaddtoleft" + linkobjectvariable + "s);");

				sg.wl("		SComponentBand right" + linkobjectvariable
						+ "buttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
				sg.wl("		right" + linkobjectvariable + "buttonband.addElement(addtoright" + linkobjectvariable
						+ "sbutton);");
				sg.wl("		AtgDeleteautolink" + linkobjectvariable + "andshowobjectAction.ActionRef deleteoneofright"
						+ linkobjectvariable + " = AtgDeleteautolink" + linkobjectvariable
						+ "andshowobjectAction.get().getActionRef();");
				sg.wl("		deleteoneofright" + linkobjectvariable + ".set" + linkobjectclass + "id(right"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getIdMarker())); ");

				sg.wl("		");
				sg.wl("		deleteoneofright" + linkobjectvariable + ".set" + objectclass
						+ "idtoshow(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker())); ");

				sg.wl("		SActionButton deleteoneofright" + linkobjectvariable
						+ "button = new SActionButton(\"Delete selected link\", deleteoneofright" + linkobjectvariable
						+ ", this);");
				sg.wl("		right" + linkobjectvariable + "buttonband.addElement(deleteoneofright" + linkobjectvariable
						+ "button);");
				sg.wl("		");
				sg.wl("		" + locationname + ".addElement(right" + linkobjectvariable + "buttonband);");

				sg.wl("");

			}

		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return this.hasautolink.righttablepriority;
		}

	}

	/**
	 * @return get the auto-link property on the related object holding the
	 *         auto-link
	 */
	public AutolinkObject<E> getRelatedAutolinkProperty() {
		return this.autolinkobject;
	}

	/**
	 * creates an has-autolink property on the data object being linked by the
	 * auto-link
	 * 
	 * @param linkdataobject                the data object holding the auto-link
	 * @param autolinkobject                the auto-link object
	 * @param commonorfromlefttablepriority the priority of the left or common
	 *                                      widget
	 * @param righttablepriority            the priority of the right table widget
	 */
	public HasAutolink(
			E linkdataobject,
			AutolinkObject<E> autolinkobject,
			WidgetDisplayPriority commonorfromlefttablepriority,
			WidgetDisplayPriority righttablepriority) {
		super(linkdataobject.getName(), "HASAUTOLINK");
		this.linkdataobject = linkdataobject;
		this.autolinkobject = autolinkobject;
		this.commonorfromlefttablepriority = commonorfromlefttablepriority;
		this.righttablepriority = righttablepriority;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.objectthathaslink = this.getParent();
		actionsonselectedlinkid = new NamedList<DynamicActionDefinition>();
		actiononselectedrightobjectid = new NamedList<DynamicActionDefinition>();
		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		this.addPropertyGenerics(new PropertyGenerics("AUTOLINKOBJECT", linkdataobject, autolinkobject));
		MethodAdditionalProcessing deleteobjectcheck = new MethodAdditionalProcessing(true,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(deleteobjectcheck);
		MethodAdditionalProcessing deletelinkwithobjects = new MethodAdditionalProcessing(false,
				uniqueidentified.getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(deletelinkwithobjects);
		MethodAdditionalProcessing controlinkconstraintbeforeupdate = new MethodAdditionalProcessing(true,
				uniqueidentified.getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(controlinkconstraintbeforeupdate);

		DataAccessMethod getautolinksandchildren = new DataAccessMethod("GETAUTOLINKSANDCHILDREN",
				new ArrayArgument(new TwoObjectsArgument("LINKANDRIGHT",
						new ObjectArgument("links", this.getLinkObjectDefinition()),
						new ObjectArgument("rightobject", this.parent))),
				true);
		getautolinksandchildren
				.addInputArgument(new MethodArgument("LEFTID", new ObjectIdArgument("LEFTOBJECTID", this.parent)));
		this.addDataAccessMethod(getautolinksandchildren);
	}

	@Override
	public String getJavaType() {
		return "#NOTIMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	/**
	 * @return get the data object holding the link
	 */
	public E getLinkObjectDefinition() {

		return this.linkdataobject;
	}

	/**
	 * @return the autolink object property on the link object
	 */
	public AutolinkObject<?> getLinkObjectProperty() {
		DataObjectDefinition linkobject = linkdataobject;
		AutolinkObject<?> linkobjectproperty = (AutolinkObject<?>) linkobject.getPropertyByName("AUTOLINKOBJECT");
		if (linkobjectproperty == null)
			throw new RuntimeException("link object does not have property");
		return linkobjectproperty;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(linkdataobject);

		return dependencies;
	}

	/**
	 * adds an action on the link id. A button will shown below the link from left
	 * widget
	 * 
	 * @param action the action to add (should have the link id as unique argument)
	 */
	public void addActionOnSelectedLinkId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException("you can add an action on selected link id only if it has 1 argument, action "
					+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != linkdataobject) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ linkdataobject.getOwnermodule().getName() + "/" + linkdataobject.getName());
		}

		actionsonselectedlinkid.add(action);
	}

	/**
	 * adds an action on the right object when selecting a line in the link from
	 * left widget
	 * 
	 * @param action an action with the object id as unique input argument
	 */
	public void addActionOnSelectedRightObjectId(DynamicActionDefinition action) {
		if (action.getInputArguments().getSize() == 1)
			throw new RuntimeException(
					"you can add an action on selected link right object id only if it has 1 argument, action "
							+ action.getName() + " has " + action.getInputArguments().getSize() + ".");
		ArgumentContent uniqueinputargument = action.getInputArguments().get(1);
		if (!(uniqueinputargument instanceof ObjectIdArgument))
			throw new RuntimeException("the first argument of " + action.getName()
					+ " should be ObjectidArgument, it is actually " + uniqueinputargument.getClass().getName() + ".");
		ObjectIdArgument objectidargument = (ObjectIdArgument) uniqueinputargument;
		DataObjectDefinition objectforid = objectidargument.getObject();
		if (objectforid != this.objectthathaslink) {
			throw new RuntimeException("objectid should be of consistent type, actionid type = "
					+ objectforid.getOwnermodule().getName() + "/" + objectforid.getName() + ", object parentid type = "
					+ objectthathaslink.getOwnermodule().getName() + "/" + objectthathaslink.getName());
		}

		actiononselectedrightobjectid.add(action);
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

}
