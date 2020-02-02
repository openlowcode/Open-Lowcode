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

import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * utility class to generate the show history page for data objects that are
 * either versioned or iterated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionShowHistoryPage
		implements
		GeneratedPages {
	private DataObjectDefinition dataobject;

	public DataObjectDefinitionShowHistoryPage(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
	}

	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());

		String attributedeclaration = "";
		String attributelist = "";

		if (dataobject.IsIterated()) {
			attributedeclaration += objectclass + "[] " + objectvariable + "iterations,";
			attributelist += objectvariable + "iterations,";
		}
		if (dataobject.isVersioned()) {
			attributedeclaration += objectclass + "[] " + objectvariable + "versions,";
			attributelist += objectvariable + "versions,";
		}

		sg.wl("package " + module.getPath() + ".page.generated;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "Action;");
		if (dataobject.isVersioned())
			sg.wl("import " + module.getPath() + ".action.generated.AtgForceversionaslastfor" + objectvariable
					+ "Action;");

		if (dataobject.IsIterated())
			sg.wl("import " + module.getPath() + ".action.generated.AtgShow" + objectvariable + "iterationAction;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectBand;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("");
		sg.wl("public class AtgShowhistoryfor" + objectvariable + "Page extends");
		sg.wl("		AbsShowhistoryfor" + objectvariable + "Page {");
		sg.wl("");
		sg.wl("	public AtgShowhistoryfor" + objectvariable + "Page(" + attributedeclaration);
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable + "id)  {");
		sg.wl("		super(" + attributelist + objectvariable + "id);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + attributedeclaration);
		sg.wl("			DataObjectId<" + objectclass + "> " + objectvariable + "id)  {");
		sg.wl("		");
		sg.wl("		String objectdisplay = \"History for " + dataobject.getLabel() + "\";");
		sg.wl("		// assuming last iteration is given first");

		if (dataobject.IsIterated()) {
			if (dataobject.getPropertyByName("NUMBERED") != null) {
				sg.wl("		objectdisplay+=\" \";");
				sg.wl("		objectdisplay+=" + objectvariable + "iterations[0].getNr();");
			}
			if (dataobject.getPropertyByName("NAMED") != null) {
				sg.wl("		objectdisplay+=\" \";");
				sg.wl("		objectdisplay+=" + objectvariable + "iterations[0].getName();");
			}
		} else {
			if (dataobject.getPropertyByName("NUMBERED") != null) {
				sg.wl("		objectdisplay+=\" \";");
				sg.wl("		objectdisplay+=" + objectvariable + "versions[0].getNr();");
			}
			if (dataobject.getPropertyByName("NAMED") != null) {
				sg.wl("		objectdisplay+=\" \";");
				sg.wl("		objectdisplay+=" + objectvariable + "versions[0].getName();");
			}
		}
		sg.wl("		return objectdisplay;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("");
		sg.wl("		SObjectIdStorage mainobjecidstorage = new SObjectIdStorage(\"" + dataobject.getName().toUpperCase()
				+ "ID\",this,this.get" + objectclass + "id());");
		sg.wl("		mainband.addElement(mainobjecidstorage);");
		sg.wl("		AtgShow" + objectvariable + "Action.ActionRef backtomainpage = AtgShow" + objectvariable
				+ "Action.get().getActionRef();");
		sg.wl("		backtomainpage.setId(mainobjecidstorage.getObjectIdInput());");
		sg.wl("		mainband.addElement(new SActionButton(\"back\",\"go back to full page for last element\", backtomainpage, this));");

		if (dataobject.IsIterated()) {
			sg.wl("		// Display Object Iteration");
			sg.wl("		mainband.addElement(new SPageText(\"" + dataobject.getLabel()
					+ " Iterations for current version\",SPageText.TYPE_TITLE,this));");
			sg.wl("		SObjectArray<" + objectclass + "> history = new SObjectArray<" + objectclass
					+ ">(\"OBJECTHISTORY\",this.get" + objectclass + "iterations()," + objectclass
					+ ".getDefinition(),this);");

			sg.wl("		AtgShow" + objectvariable + "iterationAction.ActionRef showthisiteration = AtgShow"
					+ objectvariable + "iterationAction.get().getActionRef();");
			sg.wl("		showthisiteration.setId(history.getAttributeInput(" + objectclass
					+ ".getDefinition().getIdMarker())); ");
			sg.wl("		showthisiteration.setIteration(history.getAttributeInput(" + objectclass
					+ ".getDefinition().getIterationMarker())); ");
			sg.wl("		history.addDefaultAction(showthisiteration);");

			sg.wl("		history.setMinFieldPriority(-100);");
			sg.wl("		mainband.addElement(history);");
		}
		if (dataobject.isVersioned()) {

			sg.wl("		mainband.addElement(new SPageText(\"" + dataobject.getLabel()
					+ " Version History\",SPageText.TYPE_TITLE,this));");

			sg.wl("		SObjectArray<" + objectclass + "> versiontable = new SObjectArray<" + objectclass
					+ ">(\"VERSIONSTABLE\",this.get" + objectclass + "versions()," + objectclass
					+ ".getDefinition(),this);");
			sg.wl("		AtgShow" + objectvariable + "Action.ActionRef showthisversion = AtgShow" + objectvariable
					+ "Action.get().getActionRef();");
			sg.wl("		showthisversion.setId(versiontable.getAttributeInput(" + objectclass
					+ ".getDefinition().getIdMarker())); ");
			sg.wl("		versiontable.addDefaultAction(showthisversion);");
			sg.wl("		mainband.addElement(versiontable);");
			sg.wl("		AtgForceversionaslastfor" + objectvariable
					+ "Action.ActionRef setaslast = AtgForceversionaslastfor" + objectvariable
					+ "Action.get().getActionRef();");
			sg.wl("		setaslast.setId(versiontable.getAttributeInput(" + objectclass
					+ ".getDefinition().getIdMarker())); ");
			sg.wl("		mainband.addElement(new SActionButton(\"Force as last version\", setaslast, this));");

		}
		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();

	}

}
