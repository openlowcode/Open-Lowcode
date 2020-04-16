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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.openlowcode.design.action.DynamicActionDefinition;
import org.openlowcode.design.data.autopages.GeneratedPages;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkObjectSameParent;
import org.openlowcode.design.data.properties.basic.DisplayLinkAsAttributeFromLeftObject;
import org.openlowcode.design.data.properties.basic.FileContent;
import org.openlowcode.design.data.properties.basic.HasAutolink;
import org.openlowcode.design.data.properties.basic.ImageContent;
import org.openlowcode.design.data.properties.basic.LeftForLink;
import org.openlowcode.design.data.properties.basic.Lifecycle;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Named;
import org.openlowcode.design.data.properties.basic.Numbered;
import org.openlowcode.design.data.properties.basic.ObjectWithWorkflow;
import org.openlowcode.design.data.properties.basic.PrintOut;
import org.openlowcode.design.data.properties.basic.RightForLink;
import org.openlowcode.design.data.properties.basic.Schedule;
import org.openlowcode.design.data.properties.basic.TimeSlot;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.data.properties.basic.Widget;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.module.system.design.SystemModule;
import org.openlowcode.tools.misc.NamedList;


/**
 * this class generates the show object page. This is the biggest automatically
 * generated class in the framework
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionShowPage
		implements
		GeneratedPages {
	private DataObjectDefinition dataobject;
	private HashMap<String, String> importstatements;
	private ArrayList<Widget> widgets = new ArrayList<Widget>();

	/**
	 * creates a utility code generation class for the show object page for the data
	 * object given in argument
	 * 
	 * @param dataobject Data object to generate code for
	 */
	public DataObjectDefinitionShowPage(DataObjectDefinition dataobject) {
		this.dataobject = dataobject;
		importstatements = new HashMap<String, String>();
	}

	@Override
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String lifecycleclass = null;
		ChoiceCategory lifecycle = null;
		if (dataobject.hasLifecycle()) {
			lifecycle = ((Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"))).getTransitionChoiceCategory();
			lifecycleclass = StringFormatter.formatForJavaClass(lifecycle.getName());
		}
		boolean hasfilecontent = false;
		boolean hasworkflow = false;
		String pagename = "Show" + dataobject.getName().toLowerCase() + "Page";
		String objectclass = StringFormatter.formatForJavaClass(dataobject.getName());
		String objectvariable = StringFormatter.formatForAttribute(dataobject.getName());
		boolean islinkedobject = false;
		NamedList<DataObjectDefinition> linkedobjects = new NamedList<DataObjectDefinition>();
		NamedList<DataObjectDefinition> objectstoshow = new NamedList<DataObjectDefinition>();
		ArrayList<String> arraytypes = new ArrayList<String>();
		ArrayList<String> arraynames = new ArrayList<String>();
		ArrayList<Boolean> hasblankobject = new ArrayList<Boolean>();
		ArrayList<String> blankobjectname = new ArrayList<String>();
		ArrayList<Boolean> arrayistree = new ArrayList<Boolean>();
		ArrayList<LinkedFromChildren> childproperties = new ArrayList<LinkedFromChildren>();
		ArrayList<LeftForLink<?, ?>> leftlinkedproperties = new ArrayList<LeftForLink<?, ?>>();
		ArrayList<RightForLink<?, ?>> rightlinkedproperties = new ArrayList<RightForLink<?, ?>>();
		ArrayList<HasAutolink<?>> hasautolinkproperties = new ArrayList<HasAutolink<?>>();
		NamedList<Widget> uniquewidgetlist = new NamedList<Widget>();
		LinkedToParent<?> parentlink = null;
		ArrayList<LinkedToParent<?>> allparentlinks = new ArrayList<LinkedToParent<?>>();
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);

			if (thisproperty instanceof ObjectWithWorkflow) {
				ObjectWithWorkflow objectwithworkflow = (ObjectWithWorkflow) thisproperty;
				objectstoshow.addIfNew(SystemModule.getSystemModule().getTask());
				objectstoshow.addIfNew(SystemModule.getSystemModule().getWorkflow());
				hasworkflow = true;
				Widget workflowtablewidget = objectwithworkflow.getObjectWorkflowTable();
				if (uniquewidgetlist.lookupOnName(workflowtablewidget.getName()) == null) {
					this.widgets.add(workflowtablewidget);
					uniquewidgetlist.add(workflowtablewidget);
				}

			}

			if (thisproperty instanceof LinkedToParent) {
				parentlink = (LinkedToParent<?>) thisproperty;
				objectstoshow.addIfNew(parentlink.getParentObjectForLink());
				allparentlinks.add(parentlink);
			}
			if (thisproperty instanceof LinkedFromChildren) {
				LinkedFromChildren thislinkedfromchildren = (LinkedFromChildren) thisproperty;
				DataObjectDefinition childobject = thislinkedfromchildren.getChildObject();
				linkedobjects.addIfNew(childobject);
				objectstoshow.addIfNew(childobject);
				islinkedobject = true;
				arraytypes.add(StringFormatter.formatForJavaClass(childobject.getName()));
				arraynames.add(StringFormatter.formatForAttribute(thislinkedfromchildren.getName()));
				hasblankobject.add(false);
				blankobjectname.add(null);
				arrayistree.add(Boolean.FALSE);
				childproperties.add(thislinkedfromchildren);
				Widget childtable = thislinkedfromchildren.generateChildrenTableWidget();
				this.widgets.add(childtable);
			}
			if (thisproperty instanceof LeftForLink) {

				LeftForLink<?, ?> thisleftforlink = (LeftForLink<?, ?>) thisproperty;
				Widget linktable = thisleftforlink.generateLinkFromLeftTableWidget();
				this.widgets.add(linktable);

				DataObjectDefinition linkedobject = thisleftforlink.getLinkObjectDefinition();
				linkedobjects.addIfNew(linkedobject);
				linkedobjects.addIfNew(thisleftforlink.getRightObjectForLink());
				objectstoshow.addIfNew(thisleftforlink.getRightObjectForLink());
				islinkedobject = true;
				arraytypes.add(StringFormatter.formatForJavaClass(linkedobject.getName()));
				arraynames.add("leftforlink" + StringFormatter.formatForAttribute(linkedobject.getName()));
				hasblankobject.add(true);
				blankobjectname.add("blankforaddfor" + StringFormatter.formatForAttribute(linkedobject.getName()));
				arrayistree.add(Boolean.FALSE);
				leftlinkedproperties.add(thisleftforlink);
			}
			if (thisproperty instanceof RightForLink) {
				RightForLink<?, ?> thisrightforlink = (RightForLink<?, ?>) thisproperty;
				Widget linktablefromright = thisrightforlink.getLinkFromRightTableWidget();
				this.widgets.add(linktablefromright);
				DataObjectDefinition linkedobject = thisrightforlink.getLinkObjectDefinition();
				islinkedobject = true;
				linkedobjects.addIfNew(linkedobject);
				linkedobjects.addIfNew(thisrightforlink.getLeftObjectForLink());
				objectstoshow.addIfNew(thisrightforlink.getLeftObjectForLink());
				arraytypes.add(StringFormatter.formatForJavaClass(linkedobject.getName()));
				arraynames.add("rightforlink" + StringFormatter.formatForAttribute(linkedobject.getName()));
				hasblankobject.add(true);
				blankobjectname.add("blankforaddfor" + StringFormatter.formatForAttribute(linkedobject.getName()));
				arrayistree.add(Boolean.FALSE);
				rightlinkedproperties.add(thisrightforlink);
			}
			if (thisproperty instanceof HasAutolink) {
				HasAutolink<?> hasautolink = (HasAutolink<?>) thisproperty;
				Widget autolinkwidgetfromleftorcommon = hasautolink.getWidgetForLeftOrCommonAutolink();
				this.widgets.add(autolinkwidgetfromleftorcommon);
				if (!(hasautolink.getRelatedAutolinkProperty().isSymetricLink())) {
					Widget autolinkwidgetfromright = hasautolink.getWidgetForRightAutolink();
					this.widgets.add(autolinkwidgetfromright);
				}

				DataObjectDefinition linkedobject = hasautolink.getLinkObjectDefinition();
				islinkedobject = true;
				linkedobjects.addIfNew(linkedobject);
				linkedobjects.addIfNew(hasautolink.getLinkObjectDefinition());
				objectstoshow.addIfNew(dataobject);
				// process as left link
				islinkedobject = true;
				arraytypes.add(StringFormatter.formatForJavaClass(linkedobject.getName()));
				arraynames.add("lefthasautolink" + StringFormatter.formatForAttribute(linkedobject.getName()));
				hasblankobject.add(true);
				blankobjectname.add("blankforaddfor" + StringFormatter.formatForAttribute(linkedobject.getName()));
				if (hasautolink.getRelatedAutolinkProperty().isSymetricLink()) {
					arrayistree.add(Boolean.FALSE);
				} else {
					if (hasautolink.getRelatedAutolinkProperty().isShowLinkTree()) {
						arrayistree.add(Boolean.TRUE);
					} else {
						arrayistree.add(Boolean.FALSE);
					}
				}
				// process as right link
				arraytypes.add(StringFormatter.formatForJavaClass(linkedobject.getName()));
				arraynames.add("righthasautolink" + StringFormatter.formatForAttribute(linkedobject.getName()));
				hasblankobject.add(false);
				blankobjectname.add(null);
				arrayistree.add(Boolean.FALSE);
				// add the autolink
				hasautolinkproperties.add(hasautolink);

			}
			if (thisproperty instanceof ImageContent)
				hasfilecontent = true;
			if (thisproperty instanceof FileContent) {
				hasfilecontent = true;
				FileContent filecontent = (FileContent) thisproperty;
				Widget filetablewidget = filecontent.generateAttachmentTableWidget();
				this.widgets.add(filetablewidget);
			}
		}

		// buttons

		UniqueIdentified uniqueidentifiedproperty = (UniqueIdentified) dataobject.getPropertyByName("UNIQUEIDENTIFIED");
		NamedList<DynamicActionDefinition> actionlistonobjectid = uniqueidentifiedproperty.getActionListonObjectId();
		NamedList<DynamicActionDefinition> actionlistonobjectidinmanagetab = uniqueidentifiedproperty
				.getActionListonObjectIdForManageTab();

		boolean objectbuttonband = false;
		objectbuttonband = true; // added as anyways we have the link to search page
		if (parentlink != null)
			objectbuttonband = true;

		if (actionlistonobjectid.getSize() > 0)
			objectbuttonband = true;

		// write file

		sg.wl("package " + module.getPath() + ".page.generated;");

		// -------------------------------------------------- Get all widgets import
		// statement
		for (int i = 0; i < this.widgets.size(); i++) {
			Widget thiswidget = this.widgets.get(i);
			String[] importstatements = thiswidget.getImportStatements();
			if (importstatements != null)
				for (int j = 0; j < importstatements.length; j++) {
					this.importstatements.put(importstatements[j], importstatements[j]);
				}
		}

		// -------------------------------- Print uniqueimports
		Iterator<String> uniqueimports = importstatements.keySet().iterator();
		while (uniqueimports.hasNext())
			sg.wl(uniqueimports.next());

		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.graphic.SPageNode;");
		sg.wl("import org.openlowcode.server.graphic.widget.SComponentBand;");
		sg.wl("import org.openlowcode.server.graphic.SPageSignifPath;");
		sg.wl("import org.openlowcode.server.data.NodeTree;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");
		sg.wl("import org.openlowcode.server.graphic.widget.SSeparator;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");

		if (hasfilecontent) {
			sg.wl("import org.openlowcode.tools.messages.SFile;");
			sg.wl("import org.openlowcode.module.system.data.Binaryfile;");
			sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
			sg.wl("import org.openlowcode.module.system.action.GetfileAction;");
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectIdStorage;");
			sg.wl("import org.openlowcode.server.graphic.widget.SImageDisplay;");
			sg.wl("import org.openlowcode.server.graphic.widget.SImageChooser;");
			sg.wl("import org.openlowcode.server.graphic.widget.SFileChooser;");
			sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");

		}
		if (hasworkflow) {

			sg.wl("import org.openlowcode.module.system.action.ShowactivetaskAction;");
			sg.wl("import org.openlowcode.module.system.action.generated.AtgShowworkflowAction;");
			sg.wl("import org.openlowcode.module.system.data.Task;");
			sg.wl("import org.openlowcode.module.system.data.TaskDefinition;");
			sg.wl("import org.openlowcode.module.system.data.Workflow;");
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectArrayField;");
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");

		}
		if (dataobject.isVersioned()) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgNewversionfor" + objectvariable + "Action;");
		}
		if (dataobject.hasLifecycle()) {
			sg.wl("import org.openlowcode.server.runtime.OLcServer;");
			sg.wl("import org.openlowcode.server.data.ChoiceValue;");
			sg.wl("import " + lifecycle.getParentModule().getPath() + ".data.choice." + lifecycleclass
					+ "ChoiceDefinition;");
			sg.wl("import org.openlowcode.server.graphic.widget.SChoiceTextField;");
			sg.wl("import " + module.getPath() + ".action.generated.AtgChangestate" + objectvariable + "Action;");
		}
		if (dataobject.hasTimeslot()) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgReschedule" + objectvariable + "Action;");
			sg.wl("import org.openlowcode.server.graphic.widget.SDateField;");
			sg.wl("import org.openlowcode.server.graphic.widget.STimeslotField;");

			sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");
		}
		if (dataobject.hasSchedule()) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgPrepareshowplanningfor" + objectvariable
					+ "Action;");
			sg.wl("import " + module.getPath() + ".action.generated.AtgInsertafter" + objectvariable + "Action;");
		}

		if (dataobject.propertylist.lookupOnName("TARGETDATE") != null) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgSettargetdatefor" + objectvariable + "Action;");
			sg.wl("import org.openlowcode.server.graphic.widget.SDateField;");
		}
		if ((dataobject.IsIterated()) || (dataobject.isVersioned())) {
			sg.wl("import " + module.getPath() + ".action.generated.AtgShowhistoryfor" + objectvariable + "Action;");
		}
		if (islinkedobject)
			sg.wl("import org.openlowcode.server.graphic.widget.SObjectArray;");
		if (objectbuttonband)
			sg.wl("import org.openlowcode.server.graphic.widget.SActionButton;");

		for (int i = 0; i < allparentlinks.size(); i++) {
			LinkedToParent<?> linkedtoparent = allparentlinks.get(i);
			String changeparentvariable = StringFormatter.formatForAttribute(
					"CHANGEPARENTFOR" + linkedtoparent.getInstancename() + "OF" + dataobject.getName());
			String changeparentobject = StringFormatter.formatForJavaClass(changeparentvariable);
			String parentclass = StringFormatter.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());

			sg.wl("import " + linkedtoparent.getParentObjectForLink().getOwnermodule().getPath() + ".data."
					+ parentclass + ";");

			sg.wl("import " + dataobject.getOwnermodule().getPath() + ".action.generated.Atg" + changeparentobject
					+ "Action;");
			DataObjectDefinition parent = linkedtoparent.getParentObjectForLink();

			sg.wl("import " + parent.getOwnermodule().getPath() + ".page.generated.AtgSearch"
					+ StringFormatter.formatForAttribute(parent.getName()) + "Page;");

			sg.wl("import org.openlowcode.server.graphic.widget.SObjectSearcher;");
			sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");
		}

		if (dataobject.hasNumbered()) {
			sg.wl("import " + dataobject.getOwnermodule().getPath() + ".action.generated.AtgRenumber" + objectvariable
					+ "Action;");
			sg.wl("import org.openlowcode.server.graphic.widget.STextField;");
			sg.wl("import org.openlowcode.server.graphic.widget.SPopupButton;");
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				sg.wl("import " + module.getPath() + ".action.generated.AtgSetimagecontentfor"
						+ imagecontent.getInstancename().toLowerCase() + "for" + objectvariable + "Action;");
				sg.wl("import " + module.getPath() + ".action.generated.AtgGetfullimagefor"
						+ imagecontent.getInstancename().toLowerCase() + "for" + objectvariable + "Action;");

			}
			if (thisproperty instanceof LeftForLink) {
				LeftForLink<?, ?> leftforlink = (LeftForLink<?, ?>) thisproperty;
				LinkObject<?, ?> linkobject = leftforlink.getLinkObjectProperty();
				sg.wl("import " + linkobject.getParent().getOwnermodule().getPath()
						+ ".action.generated.AtgCreatelinkandrightobjectfor"
						+ StringFormatter.formatForAttribute(linkobject.getParent().getName()) + "Action;");
			}

			if (thisproperty instanceof PrintOut) {
				PrintOut printout = (PrintOut) thisproperty;
				sg.wl("import " + module.getPath() + ".action.generated.AtgPreviewprintoutfor" + objectvariable + "for"
						+ printout.getInstancename().toLowerCase() + "Action;");
			}
		}

		sg.wl("import org.openlowcode.server.graphic.widget.SObjectBand;");
		for (int i = 0; i < childproperties.size(); i++) {

		}
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		ObjectTab[] extratabs = this.dataobject.getExtraTabs();
		if (extratabs.length > 0)
			sg.wl("import org.openlowcode.server.graphic.widget.STabPane;");

		sg.wl("import org.openlowcode.server.graphic.widget.SObjectDisplay;");
		sg.wl("import org.openlowcode.server.graphic.widget.SObjectTreeArray;");
		sg.wl("import org.openlowcode.server.graphic.widget.SPageText;");
		sg.wl("import java.util.Date;");
		for (int i = 0; i < objectstoshow.getSize(); i++) {
			Module objectmodule = objectstoshow.get(i).getOwnermodule();
			sg.wl("import " + objectmodule.getPath() + ".action.generated.AtgShow"
					+ objectstoshow.get(i).getName().toLowerCase() + "Action;"); // because tables link to action
		}

		for (int i = 0; i < actionlistonobjectid.getSize(); i++) {
			DynamicActionDefinition thisaction = actionlistonobjectid.get(i);
			String path = "import " + thisaction.getModule().getPath() + ".action.";
			if (thisaction.isAutogenerated())
				path = path + "generated.Atg";
			path = path + StringFormatter.formatForJavaClass(thisaction.getName()) + "Action;";
			sg.wl(path);
		}

		for (int i = 0; i < actionlistonobjectidinmanagetab.getSize(); i++) {
			DynamicActionDefinition thisaction = actionlistonobjectidinmanagetab.get(i);
			String path = "import " + thisaction.getModule().getPath() + ".action.";
			if (thisaction.isAutogenerated())
				path = path + "generated.Atg";
			path = path + StringFormatter.formatForJavaClass(thisaction.getName()) + "Action;";
			sg.wl(path);
		}

		sg.wl("import " + module.getPath() + ".action.generated.AtgLaunchsearch" + objectvariable + "Action;");
		String deleteactionsuffix = "";
		if (dataobject.isSubObject() != null)
			deleteactionsuffix = "andshowparent";
		sg.wl("import " + module.getPath() + ".action.generated.AtgDelete" + objectvariable + deleteactionsuffix
				+ "Action;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";"); // main object
		for (int i = 0; i < linkedobjects.getSize(); i++) {
			Module objectmodule = linkedobjects.get(i).getOwnermodule();
			sg.wl("import " + objectmodule.getPath() + ".data."
					+ StringFormatter.formatForJavaClass(linkedobjects.get(i).getName()) + ";"); // link
			sg.wl("import " + objectmodule.getPath() + ".data."
					+ StringFormatter.formatForJavaClass(linkedobjects.get(i).getName()) + "Definition;");
			// this is a bidouille to manage only search for real objects.
			if (linkedobjects.get(i).isShowActionAutomaticallyGenerated())
				sg.wl("import " + objectmodule.getPath() + ".page.generated.AtgSearch"
						+ StringFormatter.formatForAttribute(linkedobjects.get(i).getName()) + "Page;");

		}
		sg.wl("import " + module.getPath() + ".page.generated.Abs" + pagename + ";");

		sg.wl("public class Atg" + pagename + " extends Abs" + pagename + " {");
		sg.wl("");
		if (parentlink != null) {
			sg.wl("	private boolean hasparent;");
		}
		boolean hasunreleasedwarnings = false;
		Lifecycle lifecycleproperty = (Lifecycle) (dataobject.getPropertyByName("LIFECYCLE"));
		if (lifecycleproperty != null)
			if (lifecycleproperty.getUnreleasedWarning() != null)
				hasunreleasedwarnings = true;
		String hasunreleasedwarningstring = "";
		if (hasunreleasedwarnings)
			hasunreleasedwarningstring = ",String unreleasedwarning";
		sg.wl("	@Override");
		sg.wl("	public String generateTitle(" + objectclass + " " + objectvariable
				+ ", ChoiceValue<ApplocaleChoiceDefinition> userlocale");
		sg.wl("			,ChoiceValue<PreferedfileencodingChoiceDefinition> preffileencooding");
		if (hasworkflow) {
			sg.wl("			,Task[] activetasks");
			sg.wl("			,Task[] alltasks");

		}
		if (dataobject.hasLifecycle())
			sg.wl("			,ChoiceValue<" + lifecycleclass + "ChoiceDefinition> potentialstates"
					+ hasunreleasedwarningstring);
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof TimeSlot) {
				sg.wl("			,Date starttime");
				sg.wl("			,Date endtime");
			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof Schedule) {
				if (dataobject.hasNumbered())
					if (!dataobject.isAutoNumbered()) {
						sg.wl("			,String insertafternr");
					}
				if (dataobject.hasNamed()) {
					sg.wl("			,String insertaftername");
				}
				sg.wl("			," + objectclass + " blankforinsertafter");
				sg.wl("			,Date insertafterstart");
				sg.wl("			,Date insertafterend");

			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagecontentname = imagecontent.getInstancename().toLowerCase();
				sg.wl("			,SFile " + imagecontentname + "tbn");
				sg.wl("			,DataObjectId<Binaryfile> " + imagecontentname + "fullimgid");

			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof FileContent) {
				sg.wl("			,Objattachment[] attachments");
			}
		}
		if (dataobject.hasNumbered()) {
			sg.wl("			,String numberforrenumber");
		}
		for (int i = 0; i < arraytypes.size(); i++) { // dirty, but 4 arraylist have the same size
			if (!arrayistree.get(i).booleanValue()) {
				sg.wl("			," + arraytypes.get(i) + "[] " + arraynames.get(i));
			} else {
				sg.wl("			,NodeTree<" + arraytypes.get(i) + "> " + arraynames.get(i));
			}
			if (hasblankobject.get(i))
				sg.wl("			," + arraytypes.get(i) + " " + blankobjectname.get(i));
		}
		sg.wl("			) {");
		sg.wl("		String objectdisplay = \"\";");
		if (dataobject.getPropertyByName("NUMBERED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getNr();");
		}
		if (dataobject.isVersioned()) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getVersion();");
		}

		if (dataobject.getPropertyByName("NAMED") != null) {
			sg.wl("		objectdisplay+=\" \"+" + objectvariable + ".getObjectname();");
		}
		sg.wl("		objectdisplay+=\" (" + dataobject.getLabel() + ")\";");
		sg.wl("		return objectdisplay;");
		sg.wl("	}");

		sg.wl("	public AtgShow" + objectvariable + "Page(" + objectclass + " " + objectvariable
				+ ", ChoiceValue<ApplocaleChoiceDefinition> userlocale");
		sg.wl("			,ChoiceValue<PreferedfileencodingChoiceDefinition> preffileencooding");
		if (hasworkflow) {
			sg.wl("			,Task[] activetasks");
			sg.wl("			,Task[] alltasks");

		}

		if (dataobject.hasLifecycle())
			sg.wl("			,ChoiceValue<" + lifecycleclass + "ChoiceDefinition> potentialstates"
					+ hasunreleasedwarningstring);
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof TimeSlot) {
				sg.wl("			,Date starttime");
				sg.wl("			,Date endtime");
			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof Schedule) {
				if (dataobject.hasNumbered())
					if (!dataobject.isAutoNumbered()) {
						sg.wl("			,String insertafternr");
					}
				if (dataobject.hasNamed()) {
					sg.wl("			,String insertaftername");
				}
				sg.wl("			," + objectclass + " blankforinsertafter");
				sg.wl("			,Date insertafterstart");
				sg.wl("			,Date insertafterend");

			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagecontentname = imagecontent.getInstancename().toLowerCase();
				sg.wl("			,SFile " + imagecontentname + "tbn");
				sg.wl("			,DataObjectId<Binaryfile> " + imagecontentname + "fullimgid");

			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof FileContent) {
				sg.wl("			,Objattachment[] attachments");
			}
		}

		if (dataobject.hasNumbered()) {
			sg.wl("			,String numberforrenumber");
		}

		for (int i = 0; i < arraytypes.size(); i++) { // dirty, but 4 arraylist have the same size
			if (!arrayistree.get(i).booleanValue()) {
				sg.wl("			," + arraytypes.get(i) + "[] " + arraynames.get(i));
			} else {
				sg.wl("			,NodeTree<" + arraytypes.get(i) + "> " + arraynames.get(i));

			}
			if (hasblankobject.get(i))
				sg.wl("			," + arraytypes.get(i) + " " + blankobjectname.get(i));
		}
		sg.wl("			)   {");
		sg.wl("		super(" + objectvariable + ",userlocale");
		sg.wl("			,preffileencooding");
		if (hasworkflow) {
			sg.wl("			,activetasks");
			sg.wl("			,alltasks");

		}
		if (dataobject.hasLifecycle()) {
			sg.wl("			,potentialstates");
			if (hasunreleasedwarnings)
				sg.wl("			,unreleasedwarning");
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof TimeSlot) {
				sg.wl("			,starttime");
				sg.wl("			,endtime");
			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof Schedule) {
				if (dataobject.hasNumbered())
					if (!dataobject.isAutoNumbered()) {
						sg.wl("			,insertafternr");
					}
				if (dataobject.hasNamed()) {
					sg.wl("			,insertaftername");
				}
				sg.wl("			,blankforinsertafter");
				sg.wl("			,insertafterstart");
				sg.wl("			,insertafterend");

			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagecontentname = imagecontent.getInstancename().toLowerCase();
				sg.wl("			," + imagecontentname + "tbn");
				sg.wl("			," + imagecontentname + "fullimgid");

			}
		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof FileContent) {
				sg.wl("			,attachments");
			}
		}

		if (dataobject.hasNumbered()) {
			sg.wl("			,numberforrenumber");
		}

		for (int i = 0; i < arraytypes.size(); i++) {// dirty, but 4 arraylist have the same size
			sg.wl(" 			," + arraynames.get(i));
			if (hasblankobject.get(i))
				sg.wl("			," + blankobjectname.get(i));
		}
		sg.wl("			);");
		if (parentlink != null) {
			sg.wl("		hasparent=true;");
			sg.wl("		if (" + objectvariable + ".getLinkedtoparentfor" + parentlink.getInstancename().toLowerCase()
					+ "id().getId()==null) hasparent=false;");
			sg.wl("		if (hasparent) if (" + objectvariable + ".getLinkedtoparentfor"
					+ parentlink.getInstancename().toLowerCase() + "id().getId().length()==0) hasparent=false;");

		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	protected SPageNode getContent()  {");
		sg.wl("		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
		sg.wl("");

		if (hasworkflow) {

			sg.wl("		SObjectArrayField<Task> activetaskband = new SObjectArrayField<Task>(\"ACTIVETASKS\", ");
			sg.wl("				null,");
			sg.wl("				null, ");
			sg.wl("				this.getYouractivetasks(), ");
			sg.wl("				TaskDefinition.getTaskDefinition(), ");
			sg.wl("				Task.getObjectnameFieldMarker(), ");
			sg.wl("				this);");
			sg.wl("		ShowactivetaskAction.ActionRef showactivetaskref = ShowactivetaskAction.get().getActionRef();");
			sg.wl("		showactivetaskref.setTaskid(activetaskband.getAttributeInput(Task.getIdMarker()));");
			sg.wl("		activetaskband.addDefaultAction(showactivetaskref);");

		}

		String mainobjectlocation = "mainband";

		if (extratabs.length > 0) {
			mainobjectlocation = "detailstab";
			sg.wl("		SObjectDisplay<" + objectclass + "> titleobjectdisplaydefinition = new SObjectDisplay<"
					+ objectclass + ">(\"" + objectclass.toUpperCase() + "TITLE\", this.get" + objectclass + "(),"
					+ objectclass + ".getDefinition(),this, true);");
			sg.wl("		titleobjectdisplaydefinition.setReducedDisplay(true);");
			sg.wl("		mainband.addElement(titleobjectdisplaydefinition);");

			if ((hasworkflow) || (hasunreleasedwarnings)) {
				sg.wl("		SComponentBand extratitle = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
			}
			if (hasunreleasedwarnings)
				sg.wl("		extratitle.addElement(new SPageText(this.getUnreleasedwarning(), SPageText.TYPE_WARNING,this));");
			if (hasworkflow)
				sg.wl("		extratitle.addElement(activetaskband);");

			if ((hasworkflow) || (hasunreleasedwarnings)) {
				sg.wl("		titleobjectdisplaydefinition.addPageNodeRightOfTitle(extratitle);");
			}

			sg.wl("");
			sg.wl("		STabPane maintabpane = new STabPane(this,\"MAINPAGETABPANE\");");
			sg.wl("		mainband.addElement(maintabpane);");
			sg.wl("		SComponentBand detailstab = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
			sg.wl("		maintabpane.addElement(detailstab,\"Details\");");
			sg.wl("		SObjectDisplay<" + objectclass + "> objectdisplaydefinition = new SObjectDisplay<" + objectclass
					+ ">(\"" + objectclass.toUpperCase() + "\", this.get" + objectclass + "()," + objectclass
					+ ".getDefinition(),this, true);");
			sg.wl("		objectdisplaydefinition.setReducedDisplay(false);");
			sg.wl("		detailstab.addElement(objectdisplaydefinition);");

			for (int i = 0; i < extratabs.length; i++) {
				ObjectTab thistab = extratabs[i];
				sg.wl("		SComponentBand " + thistab.getWidgetName()
						+ " = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
				sg.wl("		maintabpane.addElement(" + thistab.getWidgetName() + ",\"" + thistab.getLabel() + "\");");
			}
		} else {
			sg.wl("		SObjectDisplay<" + objectclass + "> objectdisplaydefinition = new SObjectDisplay<" + objectclass
					+ ">(\"" + objectclass.toUpperCase() + "\", this.get" + objectclass + "()," + objectclass
					+ ".getDefinition(),this, true);");
			sg.wl("		mainband.addElement(objectdisplaydefinition);");
			if (hasworkflow)
				sg.wl("		objectdisplaydefinition.addPageNodeRightOfTitle(activetaskband);");
			sg.wl("");

		}

		sg.wl("		// Display Object");

		// -------------------------------- Process Image Content

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagename = StringFormatter.formatForAttribute(imagecontent.getInstancename());
				String imageclass = StringFormatter.formatForJavaClass(imagecontent.getInstancename());

				sg.wl("		// Display image " + imageclass + "");
				sg.wl("		AtgGetfullimagefor" + imagename + "for" + objectvariable + "Action.InlineActionRef "
						+ imagename + "fulldisplayaction = AtgGetfullimagefor" + imagename + "for" + objectvariable
						+ "Action.get().getInlineActionRef();");
				sg.wl("		SObjectIdStorage<Binaryfile> " + imagename
						+ "fullidstorage = new SObjectIdStorage<Binaryfile>(\""
						+ imagecontent.getInstancename().toUpperCase() + "FULLID\",this,this.get" + imageclass
						+ "fullimgid());");
				sg.wl("		" + mainobjectlocation + ".addElement(" + imagename + "fullidstorage);");
				sg.wl("		" + imagename + "fulldisplayaction.set" + objectclass
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		SImageDisplay " + imagename + "thumbnaildisplay = new SImageDisplay(\""
						+ imagecontent.getInstancename().toUpperCase() + "THUMBNAIL\", this,this.get" + imageclass
						+ "tbn(), " + imagename + "fulldisplayaction,GetfileAction.get().getFileRef(),\""
						+ StringFormatter.formatForJavaClass(imagecontent.getInstancename()) + "\");");
				sg.wl("		" + mainobjectlocation + ".addElement(" + imagename + "thumbnaildisplay);");
				sg.wl("");
			}
		}

		// -------------------------------- Button band under object ------------

		if (objectbuttonband) {
			sg.wl("		// --------------------------------------- BUTTON BAND -----------------------------------");
			sg.wl("");
			sg.wl("		SComponentBand objectbuttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT,this);");
			sg.wl("");
			// button to go back to parent if parent
			if (parentlink != null) {
				String parentname = parentlink.getParentObjectForLink().getName().toLowerCase();
				String linkname = parentlink.getName().toLowerCase();
				String parentextralabel = parentlink.getDisplayname();
				boolean hasextralabel=false;
				if (parentextralabel!=null) if (parentextralabel.trim().length()>0) hasextralabel=true;
				if (!hasextralabel) parentextralabel = parentlink.getParentObjectForLink().getLabel();
				sg.wl("		if (hasparent) {");
				sg.wl("			AtgShow" + parentname + "Action.ActionRef showparent" + parentname + " = AtgShow"
						+ parentname + "Action.get().getActionRef();");
				sg.wl("			showparent" + parentname + ".setId(objectdisplaydefinition.getAttributeInput("
						+ objectclass + ".get" + StringFormatter.formatForJavaClass(linkname) + "idMarker()));");
				sg.wl("			SActionButton gotoparent" + parentname + " = new SActionButton(\"parent "
						+ parentextralabel + "\",\"opens the parent "
						+ parentlink.parent.getName().toLowerCase() + " for this " + objectclass + "\",showparent"
						+ parentname + ",this);");
				sg.wl("			objectbuttonband.addElement(gotoparent" + parentname + ");");
				sg.wl("		}");
				sg.wl("");
			}

			for (int i = 0; i < actionlistonobjectid.getSize(); i++) {
				DynamicActionDefinition thisaction = actionlistonobjectid.get(i);
				String actionclassname = StringFormatter.formatForJavaClass(thisaction.getName());
				if (thisaction.isAutogenerated())
					actionclassname = "Atg" + actionclassname;
				String actionattributename = StringFormatter.formatForAttribute(thisaction.getName());
				ArgumentContent uniqueinputargument = thisaction.getInputArguments().get(0);
				String inputargumentclass = StringFormatter.formatForJavaClass(uniqueinputargument.getName());
				String buttonname = actionclassname + "Action.get().getName().toLowerCase()";
				if (thisaction.getButtonlabel() != null)
					buttonname = "\"" + thisaction.getButtonlabel() + "\"";

				sg.wl("		" + actionclassname + "Action.ActionRef " + actionattributename + "forobjectbandaction = "
						+ actionclassname + "Action.get().getActionRef();");
				sg.wl("		" + actionattributename + "forobjectbandaction.set" + inputargumentclass
						+ "(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		SActionButton " + actionattributename + "forobjectbandbutton = new SActionButton("
						+ buttonname + ",\"\"," + actionattributename + "forobjectbandaction,this);");
				sg.wl("		objectbuttonband.addElement(" + actionattributename + "forobjectbandbutton);	");
				sg.wl("");
			}

			boolean first = true;
			sg.wl("		// Manage Band");
			sg.wl("");
			sg.wl("		SComponentBand managepopup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
			sg.wl("");
			if (dataobject.hasNumbered()) {

				sg.wl("		AtgRenumber" + objectvariable + "Action.ActionRef renumberaction = AtgRenumber"
						+ objectvariable + "Action.get().getActionRef();");
				sg.wl("		STextField newnumber = new STextField(\"New number\",\"NEWNUMBER\", \"unique business identifier for the object\",64,");
				sg.wl("			\"\",false, this, false,false, false, renumberaction);");
				sg.wl("		newnumber.setTextBusinessData(this.getNumberforrenumber());");
				sg.wl("		newnumber.setCompactShow(true);");
				sg.wl("		renumberaction.setId(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("		renumberaction.setNewnumber(newnumber.getTextInput());");
				sg.wl("		managepopup.addElement(newnumber);");
				sg.wl("		SActionButton renumberbutton = new SActionButton(\"Change Number\", renumberaction, true,this);");
				sg.wl("		managepopup.addElement(renumberbutton);");
				sg.wl("");
				first = false;

			}
			if (dataobject.isVersioned()) {
				if (!first)
					sg.wl("		managepopup.addElement(new SSeparator(this,true));");
				sg.wl("		AtgNewversionfor" + objectvariable + "Action.ActionRef newversionaction = AtgNewversionfor"
						+ objectvariable + "Action.get().getActionRef();");
				sg.wl("		newversionaction.setId(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("		SActionButton newversionbutton = new SActionButton(\"New Version\",newversionaction,true,this);");
				sg.wl("		managepopup.addElement(newversionbutton);");
				sg.wl("");
				first = false;

			}

			if (dataobject.hasLifecycle()) {

				sg.wl("		// change state");
				if (!first)
					sg.wl("		managepopup.addElement(new SSeparator(this,true));");
				sg.wl("		AtgChangestate" + objectvariable + "Action.ActionRef changestateaction = AtgChangestate"
						+ objectvariable + "Action.get().getActionRef();");
				sg.wl("		SChoiceTextField newstatesfield = new SChoiceTextField(\"new state\",\"newstate\",\"change state of object\", ");
				sg.wl("				" + lifecycleclass
						+ "ChoiceDefinition.get(),null,this,true, false, false, false, changestateaction);");
				sg.wl("		newstatesfield.setCompactShow(true);");
				
			
				sg.wl("		if (!OLcServer.getServer().isCurrentUserAdmin("+objectclass+".getDefinition().getModuleName())) {");
				sg.wl("			newstatesfield.setLinkedData(this.getPotentialstates());");
				sg.wl("		} else {");
				sg.wl("			managepopup.addElement(new SPageText(\"Warning: admin access\",SPageText.TYPE_WARNING,this));");
				sg.wl("		}");
				
				sg.wl("		changestateaction.setId(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("		changestateaction.setNewstate(newstatesfield.getChoiceInput());");
				sg.wl("		SActionButton changestatebutton = new SActionButton(\"Change state\",changestateaction,this);");
				sg.wl("		managepopup.addElement(newstatesfield);");
				sg.wl("		managepopup.addElement(changestatebutton);");
				sg.wl("");
				first = false;
			}

			if (dataobject.propertylist.lookupOnName("TARGETDATE") != null) {

				sg.wl("		// set targetdate");
				if (!first)
					sg.wl("		managepopup.addElement(new SSeparator(this,true));");
				sg.wl("		AtgSettargetdatefor" + objectvariable
						+ "Action.ActionRef settargetdateaction = AtgSettargetdatefor" + objectvariable
						+ "Action.get().getActionRef();");
				sg.wl("		SDateField newtargetdatefield = new SDateField(\"Target date\",\"TARGETDATE\",\"set new targetdate\", 0,");
				sg.wl("			false,this, false, false,false, settargetdateaction);	");
				sg.wl("		newtargetdatefield.setCompactShow(true);");
				sg.wl("		settargetdateaction.setId(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("		settargetdateaction.setNewtargetdate(newtargetdatefield.getDateInput());");
				sg.wl("		managepopup.addElement(newtargetdatefield);");
				sg.wl("");
				first = false;

			}

			if (actionlistonobjectidinmanagetab.getSize() > 0) {
				if (!first) {
					sg.wl("		managepopup.addElement(new SSeparator(this,true));");
					first = false;
				}
				for (int i = 0; i < actionlistonobjectidinmanagetab.getSize(); i++) {
					DynamicActionDefinition thisaction = actionlistonobjectidinmanagetab.get(i);
					String actionclassname = StringFormatter.formatForJavaClass(thisaction.getName());
					if (thisaction.isAutogenerated())
						actionclassname = "Atg" + actionclassname;
					String actionattributename = StringFormatter.formatForAttribute(thisaction.getName());
					ArgumentContent uniqueinputargument = thisaction.getInputArguments().get(0);
					String inputargumentclass = StringFormatter.formatForJavaClass(uniqueinputargument.getName());
					String buttonname = actionclassname + "Action.get().getName().toLowerCase()";
					if (thisaction.getButtonlabel() != null)
						buttonname = "\"" + thisaction.getButtonlabel() + "\"";

					sg.wl("		" + actionclassname + "Action.ActionRef " + actionattributename
							+ "forobjectbandaction = " + actionclassname + "Action.get().getActionRef();");
					sg.wl("		" + actionattributename + "forobjectbandaction.set" + inputargumentclass
							+ "(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
					sg.wl("		SActionButton " + actionattributename + "forobjectbandbutton = new SActionButton("
							+ buttonname + ",\"\"," + actionattributename + "forobjectbandaction,this);");
					sg.wl("		managepopup.addElement(" + actionattributename + "forobjectbandbutton);	");
					sg.wl("");
				}
			}

			if (!first)
				sg.wl("		managepopup.addElement(new SSeparator(this,true));");
			for (int i = 0; i < allparentlinks.size(); i++) {
				LinkedToParent<?> linkedtoparent = allparentlinks.get(i);
				String changeparentvariable = StringFormatter.formatForAttribute(
						"CHANGEPARENTFOR" + linkedtoparent.getInstancename() + "OF" + dataobject.getName());
				String changeparentobject = StringFormatter.formatForJavaClass(changeparentvariable);
				String parenttype = linkedtoparent.getParentObjectForLink().getName().toLowerCase();
				String parentclass = StringFormatter.formatForJavaClass(parenttype);
				String parentname = linkedtoparent.getInstancename().toLowerCase();

				sg.wl("	// change parent " + parentname + " of type " + parenttype);
				sg.wl("	SComponentBand " + changeparentvariable
						+ " = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
				sg.wl("	SPageText titlefor" + changeparentvariable + " = new  SPageText(\"select a new "
						+ linkedtoparent.getParentObjectForLink().getLabel() + " as parent "
						+ linkedtoparent.getDisplayname() + "\",SPageText.TYPE_TITLE,this);");
				sg.wl("	" + changeparentvariable + ".addElement(titlefor" + changeparentvariable + ");");
				sg.wl("	SObjectSearcher<" + parentclass + "> projectsearchfor" + changeparentvariable + " = AtgSearch"
						+ parenttype + "Page.getsearchpanel(this, ");
				sg.wl("			\"" + changeparentvariable.toUpperCase() + "\");");
				sg.wl("	" + changeparentvariable + ".addElement(projectsearchfor" + changeparentvariable + ");");
				sg.wl("	Atg" + changeparentobject + "Action.ActionRef " + changeparentvariable + "action = Atg"
						+ changeparentobject + "Action.get().getActionRef();");
				sg.wl("	" + changeparentvariable + "action.setObjectid(objectdisplaydefinition.getAttributeInput("
						+ objectclass + ".getIdMarker()));");
				sg.wl("	" + changeparentvariable + "action.setNewparentid(projectsearchfor" + changeparentvariable
						+ ".getresultarray().getAttributeInput(" + parentclass + ".getIdMarker()));");
				sg.wl("	SActionButton " + changeparentvariable + "actionbutton = new SActionButton(\"Change\", "
						+ changeparentvariable + "action, this);");
				sg.wl("	" + changeparentvariable + ".addElement(" + changeparentvariable + "actionbutton);");
				sg.wl("	SPopupButton " + changeparentvariable + "button = new SPopupButton(this, "
						+ changeparentvariable + ",\"change parent " + linkedtoparent.getDisplayname()
						+ "\",\"allows to select a new " + linkedtoparent.getParentObjectForLink().getLabel()
						+ " as a parent for this " + dataobject.getLabel() + "\"," + changeparentvariable + "action);");
				sg.wl("	managepopup.addElement(" + changeparentvariable + "button);");

			}

			sg.wl("	AtgDelete" + objectvariable + deleteactionsuffix + "Action.ActionRef deleteaction = AtgDelete"
					+ objectvariable + deleteactionsuffix + "Action.get().getActionRef();");
			sg.wl("	deleteaction.set" + objectclass + "id(objectdisplaydefinition.getAttributeInput(" + objectclass
					+ ".getIdMarker()));");
			sg.wl("	SActionButton deletebutton= new SActionButton(\"Delete\",deleteaction,this);");
			sg.wl(" deletebutton.setConfirmationMessage(\"Are you sure you want to delete this " + dataobject.getLabel()
					+ " ?\");");
			sg.wl("	managepopup.addElement(deletebutton);");
		}

		if ((dataobject.IsIterated()) || (dataobject.isVersioned())) {

			sg.wl("	AtgShowhistoryfor" + objectvariable + "Action.ActionRef showhistoryaction = AtgShowhistoryfor"
					+ objectvariable + "Action.get().getActionRef();");
			sg.wl("	showhistoryaction.set" + objectclass + "id(objectdisplaydefinition.getAttributeInput(" + objectclass
					+ ".getIdMarker()));");
			sg.wl("	SActionButton showhistorybutton= new SActionButton(\"Show History\", showhistoryaction,this);");
			sg.wl("	managepopup.addElement(showhistorybutton);");

		}

		sg.wl("		SPopupButton managepopupbutton = new SPopupButton(this, managepopup, \"Manage\",\"Change Status of Action\",true,true,null);");
		sg.wl("		objectbuttonband.addElement(managepopupbutton);");
		sg.wl("");

		sg.wl("	AtgLaunchsearch" + objectvariable + "Action.ActionRef launchsearch" + objectvariable
				+ "action = AtgLaunchsearch" + objectvariable + "Action.get().getActionRef();");
		sg.wl("	SActionButton launchsearch" + objectvariable
				+ "button = new SActionButton(\"Search others\",launchsearch" + objectvariable + "action,this);");
		sg.wl("	objectbuttonband.addElement(launchsearch" + objectvariable + "button);	");

		if (dataobject.hasTimeslot()) {

			sg.wl("	// ---------- reschedule popup -----------------");
			sg.wl("	SComponentBand reschedulepopup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
			sg.wl("	AtgReschedule" + objectvariable + "Action.ActionRef rescheduleaction = AtgReschedule"
					+ objectvariable + "Action.get().getActionRef();");
			sg.wl("	STimeslotField reschedulefield = new STimeslotField(\"RESCHEDULE\",\"Start Time\",\"End Time\",\"Start Time\",\"End Time\", STimeslotField.DEFAULT_EMPTY,");
			sg.wl("			this.getStarttime(),this.getEndtime(), true, this);");
			sg.wl("	reschedulepopup.addElement(reschedulefield);");
			sg.wl("	rescheduleaction.setId(objectdisplaydefinition.getAttributeInput(" + objectclass
					+ ".getIdMarker()));");
			sg.wl("	rescheduleaction.setStarttime(reschedulefield.getStartDateInput());");
			sg.wl("	rescheduleaction.setEndtime(reschedulefield.getEndDateInput());");
			sg.wl("	SActionButton reschedulebutton = new SActionButton(\"OK\", rescheduleaction, true, this);");
			sg.wl("	reschedulepopup.addElement(reschedulebutton);");
			sg.wl("	SPopupButton reschedulepopupbutton = new SPopupButton(this, reschedulepopup, \"Reschedule\",\"allows to change start and end time\",false,rescheduleaction);");
			sg.wl("	objectbuttonband.addElement(reschedulepopupbutton);");
			sg.wl("	// ---------- reschedule popup end -----------------		");
			sg.wl("	");

		}

		if (dataobject.hasSchedule()) {

			sg.wl("	// ----------- insert after popup ---------------------");
			sg.wl("	SComponentBand insertafterpopup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);	");
			sg.wl("	AtgInsertafter" + objectvariable + "Action.ActionRef insertafteraction = AtgInsertafter"
					+ objectvariable + "Action.get().getActionRef();	");

			if (dataobject.hasNumbered())
				if (!dataobject.isAutoNumbered()) {
					Numbered numbered = (Numbered) dataobject.getPropertyByName("NUMBERED");
					sg.wl("	STextField insertafternumberentryfield = new STextField(\"" + numbered.getNumberLabel()
							+ "\",\"INSERTAFTERNR\",\"NUMBER\",64,\"\",false,this,false,false,false,null);");
					sg.wl("	insertafternumberentryfield.setTextBusinessData(this.getInsertafternr());");
					sg.wl("	insertafterpopup.addElement(insertafternumberentryfield);");

				}
			if (dataobject.hasNamed()) {
				Named named = (Named) dataobject.getPropertyByName("NAMED");
				sg.wl("	STextField insertafternameentryfield = new STextField(\"" + named.getNameLabel()
						+ "\",\"INSERTAFTERNAME\",\"OBJECTNAME\",64,\"\",false,this,false,false,false,null);");
				sg.wl("	insertafternameentryfield.setTextBusinessData(this.getInsertaftername());");
				sg.wl("	insertafterpopup.addElement(insertafternameentryfield);");

			}
			sg.wl("	SObjectDisplay<" + objectclass + "> " + objectvariable + "toinsert =  new SObjectDisplay<"
					+ objectclass + ">(\"" + dataobject.getName().toUpperCase()
					+ "TOINSERT\", this.getBlankforinsertafter()," + objectclass + ".getDefinition(),this,false);	");
			sg.wl("	" + objectvariable + "toinsert.setHideReadOnly();	");
			sg.wl("	insertafterpopup.addElement(" + objectvariable + "toinsert);	");
			sg.wl("	STimeslotField " + objectvariable + "toinserttimeslot = new STimeslotField(\""
					+ dataobject.getName().toUpperCase() + "TOINSERTIMESLOT\",\"Start Time\",	");
			sg.wl("			\"End Time\",\"Start Time\",\"End Time\",STimeslotField.DEFAULT_EMPTY,	");
			sg.wl("			this.getInsertafterstart(),this.getInsertafterend(), true, this);	");
			;
			sg.wl("	insertafterpopup.addElement(" + objectvariable + "toinserttimeslot);	");

			sg.wl("		insertafteraction.setOriginid(objectdisplaydefinition.getAttributeInput(" + objectclass
					+ ".getIdMarker()));");
			if (dataobject.hasNumbered())
				if (!dataobject.isAutoNumbered()) {
					sg.wl("	insertafteraction.setSuccessornr(insertafternumberentryfield.getTextInput());");

				}
			if (dataobject.hasNamed()) {
				sg.wl("	insertafteraction.setSuccessorname(insertafternameentryfield.getTextInput());");

			}
			sg.wl("	insertafteraction.setSuccessor(" + objectvariable + "toinsert.getObjectInput());");
			sg.wl("	insertafteraction.setSuccessorstartdate(" + objectvariable
					+ "toinserttimeslot.getStartDateInput());");
			sg.wl("	insertafteraction.setSuccessorenddate(" + objectvariable + "toinserttimeslot.getEndDateInput());");

			sg.wl("	SActionButton insertafterbutton = new SActionButton(\"Insert\",insertafteraction,true,this);	");
			sg.wl("	insertafterpopup.addElement(insertafterbutton);	");
			sg.wl("	SPopupButton insertafterpopupbutton = new SPopupButton(this,insertafterpopup,\"Insert After\",\"Inserts a new "
					+ objectvariable + " after the current " + objectvariable + "\",false,insertafteraction);	");
			sg.wl("	objectbuttonband.addElement(insertafterpopupbutton);		");
			sg.wl("	// ----------- insert after popup end ---------------------");

			sg.wl("	// -- addition of planning link");
			sg.wl("	");
			sg.wl("	AtgPrepareshowplanningfor" + objectvariable
					+ "Action.ActionRef showplanning = AtgPrepareshowplanningfor" + objectvariable
					+ "Action.get().getActionRef();");
			sg.wl("	showplanning.setId(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
			sg.wl("	SActionButton showplanningbutton = new SActionButton(\"Planning\",showplanning,this);");
			sg.wl("	objectbuttonband.addElement(showplanningbutton);");

		}

		// display all left links as field

		for (int i = 0; i < leftlinkedproperties.size(); i++) {
			LeftForLink<?, ?> leftlinkedproperty = leftlinkedproperties.get(i);
			DataObjectDefinition linkobject = leftlinkedproperty.getLinkObjectDefinition();
			String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getName());
			String linkobjectvariable = StringFormatter.formatForAttribute(linkobject.getName());
			String rightobjectvariable = StringFormatter
					.formatForAttribute(leftlinkedproperty.getRightObjectForLink().getName());
			String rightobjectclass = StringFormatter
					.formatForJavaClass(leftlinkedproperty.getRightObjectForLink().getName());
			@SuppressWarnings("rawtypes")
			DisplayLinkAsAttributeFromLeftObject<
					?, ?> attributeasleft = (DisplayLinkAsAttributeFromLeftObject) leftlinkedproperty
							.getLinkObjectProperty().getBusinessRuleByName("DISPLAYASATTRIBUTEFROMLEFT");

			if (attributeasleft != null) {

				sg.wl("// ---------------- Display " + linkobjectclass + " as object array field -------------------");
				sg.wl("// ----------------");
				sg.wl("	");
				sg.wl("		SObjectArrayField<" + linkobjectclass + "> left" + linkobjectvariable
						+ "s = new SObjectArrayField<" + linkobjectclass + ">(\"" + linkobjectclass.toUpperCase()
						+ "\",");
				sg.wl("				\"" + leftlinkedproperty.getLinkObjectProperty().getLabelFromLeft() + "\",\""
						+ linkobject.getLabel() + "\", this.getLeftforlinkfor" + linkobjectvariable + "(),");
				sg.wl("				" + linkobjectclass + ".getDefinition(),");
				sg.wl("				" + linkobjectclass + ".getDefinition().getLinkobjectrightnrFieldMarker(), this);");
				sg.wl("		left" + linkobjectvariable + "s.addDisplayProfile(" + linkobjectclass + "Definition.get"
						+ linkobjectclass + "Definition().getDisplayProfileHideleftobjectfields());");
				sg.wl("		" + mainobjectlocation + ".addElement(left" + linkobjectvariable + "s);");
				sg.wl("		");
				sg.wl("		AtgShow" + rightobjectvariable + "Action.ActionRef showright" + rightobjectvariable + "for"
						+ linkobjectvariable + "action = AtgShow" + rightobjectvariable
						+ "Action.get().getActionRef();");
				sg.wl("		showright" + rightobjectvariable + "for" + linkobjectvariable + "action.setId(left"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getRgidMarker()));");
				sg.wl("		left" + linkobjectvariable + "s.addDefaultAction(showright" + rightobjectvariable + "for"
						+ linkobjectvariable + "action);");
				sg.wl("		");
				sg.wl("		AtgDelete" + linkobjectvariable + "andshowleftAction.ActionRef deleteoneofleft"
						+ linkobjectvariable + " = AtgDelete" + linkobjectvariable
						+ "andshowleftAction.get().getActionRef();");
				sg.wl("		deleteoneofleft" + linkobjectvariable + ".setLeft" + objectvariable
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("		deleteoneofleft" + linkobjectvariable + ".set" + linkobjectclass + "id(left"
						+ linkobjectvariable + "s.getAttributeInput(" + linkobjectclass + ".getIdMarker()));");
				sg.wl("		left" + linkobjectvariable + "s.addDeleteAction(deleteoneofleft" + linkobjectvariable
						+ ");");

				sg.wl("				SObjectStorage<" + linkobjectclass + "> blanklinkforaddtoleft" + linkobjectvariable
						+ "s = new SObjectStorage<" + linkobjectclass + ">(\"BLANKLINKFORADDTOLEFT"
						+ linkobjectvariable.toUpperCase() + "S\",this.getLeftforlinkfor" + linkobjectvariable
						+ "blankforadd()," + linkobjectclass + ".getDefinition(),this);");
				sg.wl("				" + mainobjectlocation + ".addElement(blanklinkforaddtoleft" + linkobjectvariable
						+ "s);");
				sg.wl("				");
				sg.wl("				AtgSearchright" + rightobjectvariable + "for" + linkobjectvariable
						+ "Action.InlineActionRef addtoleft" + linkobjectvariable + "ssearchaction = AtgSearchright"
						+ rightobjectvariable + "for" + linkobjectvariable + "Action.get().getInlineActionRef();");
				sg.wl("				");

				sg.wl("				AtgCreate" + linkobjectvariable + "Action.ActionRef addtoleft" + linkobjectvariable
						+ "saction = AtgCreate" + linkobjectvariable + "Action.get().getActionRef();");
				sg.wl("				");
				sg.wl("				SFieldSearcher<" + rightobjectclass + "> addtoleft" + linkobjectvariable
						+ "ssearcher = ");
				sg.wl("						new SFieldSearcher<" + rightobjectclass + ">(\"ADDTOLEFT"
						+ linkobjectvariable.toUpperCase() + "SSEARCHER\", ");
				sg.wl("						\"add\", ");
				sg.wl("						\"close\", ");
				sg.wl("						\"enter the start of the number of the " + rightobjectvariable + "\", ");
				sg.wl("						addtoleft" + linkobjectvariable + "ssearchaction, ");
				sg.wl("						addtoleft" + linkobjectvariable + "saction, ");
				sg.wl("						" + rightobjectclass + ".getDefinition(), ");
				sg.wl("						" + rightobjectclass + ".getNrFieldMarker(), ");
				sg.wl("						this);");
				sg.wl("				");

				// ------------------------------------------------------------------------------------
				// create right object
				// ------------------------------------------------------------------------------------

				if (canCreateRightObject(leftlinkedproperty.getLinkObjectProperty())) {

					sg.wl("				AtgCreatelinkandrightobjectfor" + linkobjectvariable
							+ "Action.ActionRef createlinkandrightobjectfor" + linkobjectvariable
							+ " = AtgCreatelinkandrightobjectfor" + linkobjectvariable
							+ "Action.get().getActionRef();");
					sg.wl("				addtoleft" + linkobjectvariable
							+ "ssearcher.addBottomAction(createlinkandrightobjectfor" + linkobjectvariable
							+ ", \"Create new " + leftlinkedproperty.getRightObjectForLink().getLabel() + "\",true);");
					sg.wl("				createlinkandrightobjectfor" + linkobjectvariable + ".setObject(null);");
					// object is always numbered
					sg.wl("				createlinkandrightobjectfor" + linkobjectvariable + ".setNumber(addtoleft"
							+ linkobjectvariable + "ssearcher.getSearchTextInput());");
					// if right object named
					if (leftlinkedproperty.getRightObjectForLink().hasNamed()) {
						sg.wl("				createlinkandrightobjectfor" + linkobjectvariable
								+ ".setObjectname(addtoleft" + linkobjectvariable
								+ "ssearcher.getSearchTextInput()); ");
					}
					for (int j = 0; j < leftlinkedproperty.getRightObjectForLink().getPropertySize(); j++) {
						Property<?> thisproperty = leftlinkedproperty.getRightObjectForLink().getPropertyAt(j);
						if (thisproperty instanceof LinkedToParent) {
							LinkedToParent<?> rightobjectlinkedtoparent = (LinkedToParent<?>) thisproperty;
							LinkedToParent<?> leftobjectlinkedtoparent = this.getLeftLinkedToParent(
									leftlinkedproperty.getLinkObjectProperty(), rightobjectlinkedtoparent);
							sg.wl("				createlinkandrightobjectfor" + linkobjectvariable
									+ ".setLinkedtoparentfor"
									+ rightobjectlinkedtoparent.getInstancename().toLowerCase()
									+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass
									+ ".getDefinition().get"
									+ StringFormatter.formatForJavaClass(leftobjectlinkedtoparent.getName())
									+ "idMarker()));	");
						}
					}

					if (leftlinkedproperty.getRightObjectForLink().hasTargetDate()) {
						sg.wl("			createlinkandrightobjectfor" + linkobjectvariable + ".setTargetdate(null);");
					}

					// always present
					sg.wl("				createlinkandrightobjectfor" + linkobjectvariable
							+ ".setLeftobjectid(objectdisplaydefinition.getAttributeInput(" + objectclass
							+ ".getDefinition().getIdMarker()));");

				}

				// -------------------------------------------------------------------------------------

				sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.setLeft" + objectvariable
						+ "(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.setNr(addtoleft"
						+ linkobjectvariable + "ssearcher.getSearchTextInput());");
				for (int j = 0; j < leftlinkedproperty.getRightObjectForLink().getSearchWidgets().length; j++) {
					SearchWidgetDefinition searchwidget = leftlinkedproperty.getRightObjectForLink()
							.getSearchWidgets()[j];
					if (searchwidget.getFieldname().compareTo("NR") != 0)
						if (searchwidget.getType()!=SearchWidgetDefinition.TYPE_DATE) {
						sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
								+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "(null);");
						} else {
							sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
									+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "from(null);");
							sg.wl("				addtoleft" + linkobjectvariable + "ssearchaction.set"
									+ StringFormatter.formatForJavaClass(searchwidget.getFieldname()) + "to(null);");
						}
				}

				sg.wl("				addtoleft" + linkobjectvariable + "ssearcher.setSearchInlineOutput(AtgSearchright"
						+ rightobjectvariable + "for" + linkobjectvariable + "Action.get().getSearchresultfor"
						+ rightobjectvariable + "Ref());");
				sg.wl("				");
				sg.wl("				addtoleft" + linkobjectvariable + "saction.setLeft" + objectvariable
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("				addtoleft" + linkobjectvariable + "saction.set" + linkobjectclass
						+ "(blanklinkforaddtoleft" + linkobjectvariable + "s.getObjectInput());");
				sg.wl("				addtoleft" + linkobjectvariable + "saction.setRight" + rightobjectvariable
						+ "id(addtoleft" + linkobjectvariable + "ssearcher.getObjectIdArrayInput());");
				sg.wl("				");
				sg.wl("				left" + linkobjectvariable + "s.addNodeAtEndOfFieldData(addtoleft"
						+ linkobjectvariable + "ssearcher);");

			}

		}
		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof ImageContent) {
				ImageContent imagecontent = (ImageContent) thisproperty;
				String imagename = StringFormatter.formatForAttribute(imagecontent.getInstancename());

				sg.wl("// set image for " + imagename + "");
				sg.wl("	AtgSetimagecontentfor" + imagename + "for" + objectvariable
						+ "Action.ActionRef setimagecontentfor" + imagename + "action = AtgSetimagecontentfor"
						+ imagename + "for" + objectvariable + "Action.get().getActionRef();");
				sg.wl("	SImageChooser imagechooserfor" + imagename + " = new SImageChooser(\""
						+ imagecontent.getInstancename().toUpperCase() + "\", this,160, setimagecontentfor" + imagename
						+ "action,\"Set as " + imagename + "\");");
				sg.wl("setimagecontentfor" + imagename + "action.set" + objectclass
						+ "(objectdisplaydefinition.getAttributeInput(" + objectclass + ".getIdMarker()));");
				sg.wl("	setimagecontentfor" + imagename + "action.setFullimage(imagechooserfor" + imagename
						+ ".getFullImageDataInput());");
				sg.wl("	setimagecontentfor" + imagename + "action.setThumbnail(imagechooserfor" + imagename
						+ ".getThumbnailImageDataInput());");
				sg.wl("	SPopupButton setimagecontentfor" + imagename + "button = new SPopupButton(this, imagechooserfor"
						+ imagename + ", \"set image for " + imagename + "\",\"allows to add an image as " + imagename
						+ " from either clipboard or file\",false,setimagecontentfor" + imagename + "action);");
				sg.wl("	objectbuttonband.addElement(setimagecontentfor" + imagename + "button);");

			}
		}

		for (int i = 0; i < dataobject.propertylist.getSize(); i++) {
			Property<?> thisproperty = dataobject.propertylist.get(i);
			if (thisproperty instanceof PrintOut) {
				PrintOut printout = (PrintOut) thisproperty;
				String printoutnamevariable = StringFormatter.formatForAttribute(printout.getInstancename());

				sg.wl("	// Preview " + printoutnamevariable + "");
				sg.wl("	AtgPreviewprintoutfor" + objectvariable + "for" + printoutnamevariable
						+ "Action.InlineActionRef preview" + printoutnamevariable + " = AtgPreviewprintoutfor"
						+ objectvariable + "for" + printoutnamevariable + "Action.get().getInlineActionRef();");
				sg.wl("	preview" + printoutnamevariable + ".set" + objectclass
						+ "id(objectdisplaydefinition.getAttributeInput(" + objectclass
						+ ".getDefinition().getIdMarker()));");
				sg.wl("	SActionButton " + printoutnamevariable
						+ "preview = new SActionButton(\"Preview Printout\",preview" + printoutnamevariable
						+ ",this);");
				sg.wl("	objectbuttonband.addElement(" + printoutnamevariable + "preview);");
				sg.wl("	SFileDownloader preview" + printoutnamevariable
						+ "downloader = new SFileDownloader(\"PREVIEWDOWNLOAD\", this, preview" + printoutnamevariable
						+ ",AtgPreviewprintoutfor" + objectvariable + "for" + printoutnamevariable
						+ "Action.get().getPreviewRef());");
				sg.wl("	objectbuttonband.addElement(preview" + printoutnamevariable + "downloader);");

			}
		}

		if (objectbuttonband) {
			sg.wl("		" + mainobjectlocation + ".addElement(objectbuttonband);");
		}

		// ----------------------------------------------------------------------------------------
		// -- DISPLAY WIDGETS --
		// ----------------------------------------------------------------------------------------

		Collections.sort(this.widgets);
		for (int i = 0; i < this.widgets.size(); i++) {
			Widget thiswidget = widgets.get(i);
			String locationname = mainobjectlocation;
			if (thiswidget.getWidgetPriority() != null)
				if (thiswidget.getWidgetPriority().getTab() != null)
					locationname = thiswidget.getWidgetPriority().getTab().getWidgetName();
			thiswidget.generateWidgetCode(sg, module, locationname);
		}

		sg.wl("		return mainband;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * get the linked to parent for the left related object
	 * 
	 * @param linkobject        a link object
	 * @param rightobjectparent a linkedtoparent for the right to parent
	 * @return the linkedtoparent on left object related by a constraint on link
	 *         same parent, null if it does not exist
	 */
	public LinkedToParent<?> getLeftLinkedToParent(LinkObject<?, ?> linkobject, LinkedToParent<?> rightobjectparent) {
		for (int i = 0; i < linkobject.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<?> businessrule = linkobject.getBusinessRule(i);
			if (businessrule instanceof ConstraintOnLinkObjectSameParent) {
				ConstraintOnLinkObjectSameParent<
						?, ?> constraintsameparent = (ConstraintOnLinkObjectSameParent<?, ?>) businessrule;
				if (constraintsameparent.getRightobjectparentproperty().equals(rightobjectparent))
					return constraintsameparent.getLeftobjectparentproperty();
			}
		}
		return null;
	}

	/**
	 * This checks if it is possible to create a right object for link with only a
	 * string (number and name if required)
	 * 
	 * @param linkobjectproperty link object
	 * @return true if it is possible to create a right object for link with only a
	 *         string.
	 * 
	 */
	private boolean canCreateRightObject(LinkObject<?, ?> linkobjectproperty) {
		DataObjectDefinition rightobject = linkobjectproperty.getRightobjectforlink();
		if (!rightobject.hasNumbered())
			return false;
		if (rightobject.hasSchedule())
			return false;
		for (int i = 0; i < rightobject.getPropertySize(); i++) {
			Property<?> rightproperty = rightobject.getPropertyAt(i);
			if (rightproperty instanceof LinkedToParent) {
				LinkedToParent<?> rightlinkedtoparent = (LinkedToParent<?>) rightproperty;
				if (getLeftLinkedToParent(linkobjectproperty, rightlinkedtoparent) == null)
					return false;
			}
		}
		return true;
	}

}
