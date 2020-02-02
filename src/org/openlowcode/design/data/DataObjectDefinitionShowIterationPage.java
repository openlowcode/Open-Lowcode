/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * this program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.data.properties.basic.DisplayLinkAsAttributeFromLeftObject;
import org.openlowcode.design.data.properties.basic.HasAutolink;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * Generation of the page to show an old iteration
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionShowIterationPage implements
GeneratedPages {
	private DataObjectDefinition dataobject;
	/**
	 * Creates the utility to generate the show iteration page
	 * 
	 * @param dataobject parent data object
	 */
	public DataObjectDefinitionShowIterationPage(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
	}
	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		boolean hasunreleasedwarnings = false;
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		ArrayList<String> additionalattributestype = new ArrayList<String>();
		ArrayList<String> additionalattributesname = new ArrayList<String>();
		ArrayList<String> importstatement = new ArrayList<String>();
		Lifecycle lifecycleproperty = (Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"));
		if (lifecycleproperty != null)
			if (lifecycleproperty.getUnreleasedWarning() != null)
				hasunreleasedwarnings = true;
		if (hasunreleasedwarnings) {
			additionalattributestype.add("String");
			additionalattributesname.add("unreleasedwarning");

		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				additionalattributestype.add("SFile");
				additionalattributesname.add(imagecontent.getInstancename().toLowerCase() + "tbn");
				additionalattributestype.add("DataObjectId<Binaryfile>");
				additionalattributesname.add(imagecontent.getInstancename().toLowerCase() + "fullimgid");
				importstatement.add("import org.openlowcode.tools.messages.SFile;");
				importstatement.add("import org.openlowcode.module.system.data.Binaryfile;");
				importstatement.add("import org.openlowcode.server.data.properties.DataObjectId;");
				importstatement.add("import org.openlowcode.server.action.SInlineActionRef;");
				importstatement.add("import org.openlowcode.module.system.action.GetfileAction;");
				importstatement.add("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
				importstatement.add("import org.openlowcode.server.graphic.widget.SImageDisplay;");

			}

		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				if (thislinkedfromchildren.getOriginObjectProperty().getBusinessRuleByName("SUBOJECT") != null) {
					additionalattributestype
							.add(StringFormatter.formatForJavaClass(thislinkedfromchildren.getChildObject().getName())
									+ "[]");
					additionalattributesname.add(thislinkedfromchildren.getName().toLowerCase());
					importstatement.add("import " + thislinkedfromchildren.getChildObject().getOwnermodule().getPath()
							+ ".data."
							+ StringFormatter.formatForJavaClass(thislinkedfromchildren.getChildObject().getName())
							+ ";");
					importstatement.add("import " + thislinkedfromchildren.getChildObject().getOwnermodule().getPath()
							+ ".action.generated.AtgShow"
							+ StringFormatter.formatForAttribute(thislinkedfromchildren.getChildObject().getName())
							+ "Action;");

					importstatement.add("import org.openlowcode.server.graphic.widget.SObjectBand;");
				}
			}
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) thisproperty;
				additionalattributestype.add(
						StringFormatter.formatForJavaClass(thisleftforlink.getLinkObjectDefinition().getName()) + "[]");
				additionalattributesname.add(thisleftforlink.getName().toLowerCase());
				importstatement.add("import " + thisleftforlink.getLinkObjectDefinition().getOwnermodule().getPath()
						+ ".data."
						+ StringFormatter.formatForJavaClass(thisleftforlink.getLinkObjectDefinition().getName())
						+ ";");
				importstatement.add("import "
						+ thisleftforlink.getLinkObjectProperty().getRightobjectforlink().getOwnermodule().getPath()
						+ ".action.generated.AtgShow"
						+ thisleftforlink.getLinkObjectProperty().getRightobjectforlink().getName().toLowerCase()
						+ "Action;");
			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				// leftforlink
				if (!hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					additionalattributestype.add(
							StringFormatter.formatForJavaClass(hasautolink.getLinkObjectDefinition().getName()) + "[]");
					additionalattributesname.add(hasautolink.getRelatedAutolinkProperty().getName().toLowerCase());
					importstatement.add("import " + hasautolink.getLinkObjectDefinition().getOwnermodule().getPath()
							+ ".data."
							+ StringFormatter.formatForJavaClass(hasautolink.getLinkObjectDefinition().getName())
							+ ";");

				}
			}

		}

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + objectclass + "Definition;");
		for (int i = 0; i < importstatement.size(); i++)
			sg.wl(importstatement.get(i));
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectDisplay;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArrayField;");
		sg.wl("");
		sg.wl("public class AtgShow" + objectvariable + "iterationPage extends AbsShow" + objectvariable
				+ "iterationPage {");
		sg.wl("");
		StringBuffer extraattributes = new StringBuffer("");
		for (int i = 0; i < additionalattributestype.size(); i++) {
			extraattributes.append(',');
			extraattributes.append(additionalattributestype.get(i));
			extraattributes.append(' ');
			extraattributes.append(additionalattributesname.get(i));

		}

		StringBuffer extraattributesname = new StringBuffer("");
		for (int i = 0; i < additionalattributestype.size(); i++) {
			extraattributesname.append(',');
			extraattributesname.append(additionalattributesname.get(i));

		}

		sg.wl("	public AtgShow" + objectvariable + "iterationPage(" + objectclass + " " + objectvariable
				+ extraattributes.toString() + ")  {");
		sg.wl("		super(" + objectvariable + extraattributesname.toString() + ");");
		sg.wl("");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + objectclass + " " + objectvariable + extraattributes.toString()
				+ ")  {");
		sg.wl("		String objectdisplay = \"" + dataobject.getLabel() + "\";");
		if (dataobject.getPropertyByName("NUMBERED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getNr();");
		}
		if (dataobject.getPropertyByName("NAMED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getName();");
		}

		sg.wl("		return objectdisplay;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);");
		sg.wl("		SObjectDisplay<" + objectclass + "> mainobjectdisplay = new SObjectDisplay<" + objectclass
				+ ">(\"MAINOBJECT\", this.get" + objectclass + "(),");
		sg.wl("				" + objectclass + "Definition.get" + objectclass + "Definition(), this, true);");
		sg.wl("		AtgShow" + objectvariable + "Action.ActionRef gotolatestaction = AtgShow" + objectvariable
				+ "Action.get().getActionRef();");
		sg.wl("		gotolatestaction.setId(mainobjectdisplay.getAttributeInput(" + objectclass + ".getIdMarker()));");
		sg.wl("		SActionButton gotolatestbutton = new SActionButton(\"Go to latest\", gotolatestaction, this);");
		sg.wl("		mainobjectdisplay.addPageNodeRightOfTitle(gotolatestbutton);");
		sg.wl("		mainband.addElement(mainobjectdisplay);");
		if (hasunreleasedwarnings) {

			sg.wl("		SComponentBand extratitle = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
			sg.wl("		extratitle.addElement(new SPageText(this.getUnreleasedwarning(), SPageText.TYPE_WARNING,this));");
			sg.wl("		mainobjectdisplay.addPageNodeRightOfTitle(extratitle);");

		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagename = StringFormatter.formatForAttribute(imagecontent.getInstancename());
				String imageclass = StringFormatter.formatForJavaClass(imagecontent.getInstancename());

				sg.wl("		// Display image " + imageclass + "");
				sg.wl("		GetfileAction.InlineActionRef " + imagename
						+ "fulldisplayaction = GetfileAction.get().getInlineActionRef();");
				sg.wl("		SObjectIdStorage<Binaryfile> " + imagename
						+ "fullidstorage = new SObjectIdStorage<Binaryfile>(\""
						+ imagecontent.getInstancename().toUpperCase() + "FULLID\",this,this.get" + imageclass
						+ "fullimgid());");
				sg.wl("		mainband.addElement(" + imagename + "fullidstorage);");
				sg.wl("		" + imagename + "fulldisplayaction.setFileid(" + imagename
						+ "fullidstorage.getObjectIdInput());	");
				sg.wl("		SImageDisplay " + imagename + "thumbnaildisplay = new SImageDisplay(\""
						+ imagecontent.getInstancename().toUpperCase() + "THUMBNAIL\", this,this.get" + imageclass
						+ "tbn(), " + imagename + "fulldisplayaction,GetfileAction.get().getFileRef(),\""
						+ StringFormatter.formatForJavaClass(imagecontent.getInstancename()) + "\");");
				sg.wl("		mainband.addElement(" + imagename + "thumbnaildisplay);");
				sg.wl("");

			}

		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> leftlinkedproperty = (LeftForLink<?, ?>) thisproperty;
				DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
				String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
				String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
				String rightobjectvariable = StringFormatter
						.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
				@SuppressWarnings("rawtypes")
				DisplayLinkAsAttributeFromLeftObject<
						?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
								.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

				if (attributeasleft == null) {
					sg.wl("		// show link as field for " + linkobject.getName().toUpperCase());
					sg.wl("		mainband.addElement(new SPageText(\""
							+ leftlinkedproperty.getLinkObjectProperty().getLabelFromLeft()
							+ "\",SPageText.TYPE_TITLE,this));");
					sg.wl("		SObjectArray<" + linkobjectclass + "> left" + linkobjectvariable
							+ "field = new SObjectArray<" + linkobjectclass + ">(\""
							+ linkobject.getName().toUpperCase() + "\",");
					sg.wl("				this.getLeftforlinkfor" + linkobjectvariable + "()," + linkobjectclass
							+ ".getDefinition(),this);");
					sg.wl("		AtgShow" + rightobjectvariable + "Action.ActionRef showright" + rightobjectvariable
							+ "for" + linkobjectvariable + "action = AtgShow" + rightobjectvariable
							+ "Action.get().getActionRef();");
					sg.wl("		showright" + rightobjectvariable + "for" + linkobjectvariable + "action.setId(left"
							+ linkobjectvariable + "field.getAttributeInput(" + linkobjectclass
							+ ".getRgidMarker())); ");
					sg.wl("		left" + linkobjectvariable + "field.addDefaultAction(showright" + rightobjectvariable
							+ "for" + linkobjectvariable + "action);");
					sg.wl("		mainband.addElement(left" + linkobjectvariable + "field);");

				}
			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> leftlinkedproperty = (LeftForLink<?, ?>) thisproperty;
				DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
				String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
				String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
				String rightobjectvariable = StringFormatter
						.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
				@SuppressWarnings("rawtypes")
				DisplayLinkAsAttributeFromLeftObject<
						?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
								.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

				if (attributeasleft != null) {
					sg.wl("		// show link as field for " + linkobject.getName().toUpperCase());
					sg.wl("		SObjectArrayField<" + linkobjectclass + "> left" + linkobjectvariable
							+ "field = new SObjectArrayField<" + linkobjectclass + ">(\""
							+ linkobject.getName().toUpperCase() + "\",\""
							+ leftlinkedproperty.getLinkObjectProperty().getLabelFromLeft() + "\",\"\",");
					sg.wl("				this.getLeftforlinkfor" + linkobjectvariable + "()," + linkobjectclass
							+ ".getDefinition(),");
					sg.wl("				" + linkobjectclass
							+ ".getDefinition().getLinkobjectrightnrFieldMarker(),this);");
					sg.wl("		AtgShow" + rightobjectvariable + "Action.ActionRef showright" + rightobjectvariable
							+ "for" + linkobjectvariable + "action = AtgShow" + rightobjectvariable
							+ "Action.get().getActionRef();");
					sg.wl("		showright" + rightobjectvariable + "for" + linkobjectvariable + "action.setId(left"
							+ linkobjectvariable + "field.getAttributeInput(" + linkobjectclass
							+ ".getRgidMarker())); ");
					sg.wl("		left" + linkobjectvariable + "field.addDefaultAction(showright" + rightobjectvariable
							+ "for" + linkobjectvariable + "action);");
					sg.wl("		mainband.addElement(left" + linkobjectvariable + "field);");

				}
			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thischildproperty = (LinkedFromChildren) thisproperty;
				String childclassname = StringFormatter
						.formatForJavaClass(thischildproperty.getChildObject().getName());
				String childclassattribute = StringFormatter
						.formatForAttribute(thischildproperty.getChildObject().getName());
				String linknameclass = StringFormatter.formatForJavaClass(thischildproperty.getName());
				String linknameattribute = StringFormatter.formatForAttribute(thischildproperty.getName());

				if (thischildproperty.getOriginObjectProperty().getBusinessRuleByName("SUBOJECT") != null) {
					boolean displayascomponentband = false;
					if (!thischildproperty.getChildObject().getSubObject().isShowastable())
						displayascomponentband = true;
					sg.wl("		mainband.addElement(new SPageText(\"Related " + thischildproperty.getChildObject().getLabel()
							+ "\",SPageText.TYPE_TITLE,this));");

					if (displayascomponentband == true) {
						sg.wl("		SObjectBand<" + childclassname + "> arrayfor" + linknameattribute
								+ " = new SObjectBand<" + childclassname + ">(\"CHILDTICKETCOMMENT\",");
						sg.wl("				this.get" + linknameclass + "(),");
						sg.wl("				" + childclassname + ".getDefinition(),");
						sg.wl("				this);");
						sg.wl("		mainband.addElement(arrayfor" + linknameattribute + ");");
					} else {
						sg.wl("		SObjectArray<" + childclassname + "> arrayfor" + linknameattribute
								+ " = new SObjectArray<" + childclassname + ">(\"CHILD" + childclassname.toUpperCase()
								+ "\",");
						sg.wl("				this.get" + linknameclass + "(),");
						sg.wl("				" + childclassname + ".getDefinition(),");
						sg.wl("				this);");
						sg.wl("		mainband.addElement(arrayfor" + linknameattribute + ");");
						sg.wl("");
						sg.wl("		AtgShow" + childclassattribute + "Action.ActionRef actionfor" + linknameattribute
								+ " = AtgShow" + childclassattribute + "Action.get().getActionRef();");
						sg.wl("		actionfor" + linknameattribute + ".setId(arrayfor" + linknameattribute
								+ ".getAttributeInput(" + childclassname + ".getIdMarker())); ");
						sg.wl("		arrayfor" + linknameattribute + ".addDefaultAction(actionfor" + linknameattribute
								+ ");");
						sg.wl("");
					}

				}
			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);

			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				String autolinkobjbectvariablename = hasautolink.getLinkObjectDefinition().getName().toLowerCase();
				String autolinkobjbectvariableclass = StringFormatter
						.formatForJavaClass(hasautolink.getLinkObjectDefinition().getName().toLowerCase());

				// leftforlink
				if (!hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {

					sg.wl("		mainband.addElement(new SPageText(\""
							+ hasautolink.getRelatedAutolinkProperty().getLabelFromLeft()
							+ "\",SPageText.TYPE_TITLE,this));");
					sg.wl("		SObjectArray<" + autolinkobjbectvariableclass + "> " + autolinkobjbectvariablename
							+ "array = new SObjectArray<" + autolinkobjbectvariableclass + ">(\""
							+ hasautolink.getParent().getName().toUpperCase() + "\",this.getLefthasautolinkfor"
							+ autolinkobjbectvariablename + "()," + autolinkobjbectvariableclass
							+ ".getDefinition(),this);");
					sg.wl("		AtgShow" + objectvariable + "Action.ActionRef showrightfo" + autolinkobjbectvariablename
							+ " = AtgShow" + objectvariable + "Action.get().getActionRef();");
					sg.wl("		showrightfo" + autolinkobjbectvariablename + ".setId(" + autolinkobjbectvariablename
							+ "array.getAttributeInput(" + autolinkobjbectvariableclass + ".getRgidMarker())); ");
					sg.wl("		" + autolinkobjbectvariablename + "array.addDefaultAction(showrightfo"
							+ autolinkobjbectvariablename + ");");
					sg.wl("		mainband.addElement(" + autolinkobjbectvariablename + "array);");

				}
			}

		}

		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
		
	}


}
