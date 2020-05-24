/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.table.CObjectArrayColumnModel;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.ObjectTreeDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

/**
 * This widget allows the display and edition of a list of elements in a tree
 * structure. Main features are:
 * <ul>
 * <li>load a preliminary tree with structure from the server</li>
 * <li>possibility to load elements after first display (go deeper in the
 * tree)</li>
 * <li>edition of some fields</li>
 * <li>put data in lines corresponding to several objects (e.g. display business
 * object + link to same object)</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 */
public class CObjectTreeArray
		extends
		CPageNode {
	public static final int TREE_NAVIGATION_CIRCUITBREAKER = 512;
	private static Logger logger = Logger.getLogger(CObjectTreeArray.class.getName());
	@SuppressWarnings("unused")
	private String externaldatareference;
	@SuppressWarnings("unused")
	private String name;
	private boolean inline;
	private ArrayList<CBusinessField<?>> payloadlist;
	private CPageDataRef datareference;

	private CPageAction action;
	@SuppressWarnings("unused")
	private CInlineActionDataRef inlineactiondataref;
	@SuppressWarnings("unused")
	private Window parentwindow;
	private TreeTableView<ObjectDataElt> thistreetable;

	// ----------------------- context menu
	private ContextMenu contextmenu;
	private MenuItem expandall;
	private MenuItem copydata;
	private MenuItem copyflattree;
	private MenuItem copytechdata;
	private HashMap<String, CPageDataRef> overridenlabels;
	private PageActionManager actionmanager;
	private ObjectDataElt selecteditem;
	/**
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectTreeArray(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		// --------------------------------------------------

		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;
			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				this.action = new CPageAction(reader);
				reader.startStructureArray("ATTR");
			} else {

				if (actiontag.getStructurename().compareTo("ATTRS") == 0) {

				} else {
					throw new RuntimeException("expected a startstructure 'ATTRS' tag, got " + element.toString()
							+ " at path " + reader.getCurrentElementPath());
				}

			}
		}

		// --------------------------------------
		payloadlist = new ArrayList<CBusinessField<?>>();
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		this.inline = reader.returnNextBooleanField("INL");
		if (!inline)
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
		if (inline) {
			this.inlineactiondataref = new CInlineActionDataRef(reader, this);
			logger.finer("==============================>>> Parsed Inline data ref");
		}
		overridenlabels = new HashMap<String, CPageDataRef>();
		reader.startStructureArray("OVWLBL");
		while (reader.structureArrayHasNextElement("OVWLBL")) {
			String fieldtooverride = reader.returnNextStringField("FLD");
			CPageDataRef dataref = CPageDataRef.parseCPageDataRef(reader);
			overridenlabels.put(fieldtooverride, dataref);
			reader.returnNextEndStructure("OVWLBL");
		}

		reader.returnNextEndStructure("OBJTRA");
	}

	public ObjectTreeDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = %s" + dataref.getName());
		// control not perfect
		if (!(thiselement instanceof ObjectTreeDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ObjectTreeDataElt<ObjectDataElt> thiselementtree = (ObjectTreeDataElt<ObjectDataElt>) thiselement;
		return thiselementtree;
	}

	private void generatenbLinesLabel() {
		long nbelements = this.thistreetable.getExpandedItemCount();
		String nbelementlabel = "Copy Data (empty)";
		if (nbelements == 1)
			nbelementlabel = "Copy Data (1 line)";
		if (nbelements > 1)
			nbelementlabel = "Copy Data (" + nbelements + " lines)";
		this.copydata.setText(nbelementlabel);
	}

	public TreeItem<ObjectDataElt> parseNodeTree(
			ObjectTreeDataElt<ObjectDataElt> nodetree,
			String nodeid,
			String path,
			int circuitbreaker) {
		if (circuitbreaker > 9999)
			throw new RuntimeException("circuitbreaker on nodetree for objectdataelt " + nodetree);
		boolean alreadyexists = false;
		if (nodeid == null)
			if (path.contains("null"))
				alreadyexists = true;
		if (nodeid != null)
			if (path.contains("[" + nodeid + "]"))
				alreadyexists = true;

		ObjectDataElt nodeobject = nodetree.getObject(nodeid);
		logger.fine("L" + circuitbreaker + " - " + (nodeobject != null ? nodeobject.getUID() : "NULL"));
		TreeItem<ObjectDataElt> parent = new TreeItem<ObjectDataElt>(nodetree.getObject(nodeid));
		if (!alreadyexists) {
			String newpath = path + "[" + nodeid + "]";
			ArrayList<String> childrenid = nodetree.getChildrenId(nodeid);
			if (childrenid == null)
				throw new RuntimeException(
						"null childrenid array for nodeid = " + nodeid + ", circuitbreaker = " + circuitbreaker);
			for (int i = 0; i < childrenid.size(); i++) {
				logger.fine("L" + circuitbreaker + " - add child" + childrenid.get(i) + " at path " + newpath);

				TreeItem<ObjectDataElt> childtreeitem = parseNodeTree(nodetree, childrenid.get(i), newpath,
						circuitbreaker + 1);
				parent.getChildren().add(childtreeitem);

			}
		} else {
			logger.fine("Stops recursive loop, path = " + path + " nodeid = " + nodeid);
		}
		return parent;

	}

	private void modifycolumnmodel(CPageData inputdata) {
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> thisfield = payloadlist.get(i);
			CPageDataRef overrides = overridenlabels.get(thisfield.getFieldname());
			if (overrides != null) {
				TextDataElt thiselement = (TextDataElt) inputdata.lookupDataElementByName(overrides.getName());
				if (thiselement == null)
					throw new RuntimeException("could not find a page data called " + thisfield.getFieldname());
				thisfield.overridesLabel(thiselement.getPayload());
			}

		}
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		this.actionmanager = actionmanager;

		modifycolumnmodel(inputdata);
		CObjectArrayColumnModel treemodel = new CObjectArrayColumnModel(payloadlist);
		this.thistreetable = treemodel.generateTreeTableViewModel(actionmanager);
		this.thistreetable.setStyle("-fx-base: #ffffff;   ");
		int linenumber = 0;
		if (this.datareference != null) {
			ObjectTreeDataElt<ObjectDataElt> data = getExternalContent(inputdata, datareference);
			linenumber = data.getObjectNr();
			thistreetable.setRoot(parseNodeTree(data, data.getRootId(), "", 0));
			if (data.getRootId() == null)
				thistreetable.showRootProperty().set(false);
		}

		// add action handler for double click on row
		if (action != null) {
			logger.fine("setting action for treetable " + action.getModule() + "." + action.getName() + " for widget "
					+ thistreetable);
			actionmanager.registerEvent(thistreetable, action);
			thistreetable.setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					// sometimes, click can go through while no item selected
					if (thistreetable.getSelectionModel().getSelectedItem() != null) {
						// to manage simple double click
						if (event.getClickCount() > 1)
							actionmanager.getMouseHandler().handle(event);
					}
					// to manage click with control. If no special rule, you
					// need to click 3 times
					if (event.getClickCount() == 1)
						if (event.isControlDown()) {
							actionmanager.getMouseHandler().handle(event);

						}

				}

			});
		}
		this.parentwindow = parentwindow;

		contextmenu = new ContextMenu();

		expandall = new MenuItem("Open All");
		copydata = new MenuItem("Copy Data");
		copyflattree = new MenuItem("Copy Flat Tree");
		copytechdata = new MenuItem("Copy Data with Details");

		contextmenu.getItems().add(expandall);
		contextmenu.getItems().add(copydata);
		contextmenu.getItems().add(copyflattree);
		contextmenu.getItems().add(copytechdata);

		copytechdata.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyTableToClipboard(true);
			}

		});
		copydata.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyTableToClipboard(false);
			}

		});
		copyflattree.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				copyAsFlatTreeToClipboard();
			}
		});
		expandall.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				expandall();
				generatenbLinesLabel();
			}

		});

		thistreetable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				if (contextmenu.isShowing()) {
					contextmenu.hide();
				} else {
					thistreetable.getSelectionModel().clearSelection();
					contextmenu.show(thistreetable, event.getScreenX(), event.getScreenY());

				}
			}
		});

		contextmenu.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
				if (!newvalue)
					contextmenu.hide();

			}

		});

		this.thistreetable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (contextmenu.isShowing())
				contextmenu.hide();
				if (newSelection!=null) if (newSelection.getValue()!=null) selecteditem = newSelection.getValue();
		});

		// ----------------- Set tree table size
		int normalizedlinenumber = linenumber;
		if (normalizedlinenumber > 20)
			normalizedlinenumber = 20;
		int maxheight = normalizedlinenumber * treemodel.getFinalRowHeightInPixel() + 40;
		int preferedheight = maxheight;
		thistreetable.setMinHeight(preferedheight);
		thistreetable.setPrefHeight(preferedheight);
		thistreetable.setMaxHeight(maxheight);

		generatenbLinesLabel();
		return this.thistreetable;
	}

	public void expandall() {
		TreeItem<ObjectDataElt> data = thistreetable.getRoot();

		expandall(data, 0);
	}

	private void expandall(TreeItem<ObjectDataElt> thisitem, int circuitbreaker) {
		if (circuitbreaker > TREE_NAVIGATION_CIRCUITBREAKER)
			throw new RuntimeException("Circuit Breaker on " + thisitem.getValue().getUID());
		thisitem.setExpanded(true);
		Iterator<TreeItem<ObjectDataElt>> childreniterator = thisitem.getChildren().iterator();
		while (childreniterator.hasNext()) {
			TreeItem<ObjectDataElt> childnode = childreniterator.next();
			expandall(childnode, circuitbreaker + 1);
		}
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (type instanceof ObjectIdDataEltType) {
			if (objectfieldname == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			ObjectDataElt object = selecteditem;
			SimpleDataElt field = object.lookupEltByName(objectfieldname);
			if (field == null)
				throw new RuntimeException(
						"field not found " + objectfieldname + ", available fields = " + object.dropFieldNames());
			if (!(field instanceof TextDataElt))
				throw new RuntimeException("field for name = " + objectfieldname + " is not text");
			TextDataElt textfield = (TextDataElt) field;
			ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
			return objectid;
		}
		throw new RuntimeException(String.format("Unsupported extraction type %s ", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {
		if (thistreetable != null) {
			if (thistreetable.getFocusModel() != null)
				thistreetable.getFocusModel().focus(null);

			thistreetable.setOnMouseClicked(null);
			thistreetable.setOnKeyPressed(null);
			thistreetable.setOnKeyReleased(null);
			thistreetable.setSelectionModel(null);
			thistreetable.setOnContextMenuRequested(null);
			copydata.setOnAction(null);
			this.copytechdata.setOnAction(null);
			this.expandall.setOnAction(null);

			if (thistreetable.getColumns() != null)
				if (thistreetable.getColumns().size() > 0)
					thistreetable.getColumns().clear();
			thistreetable.setRoot(null);
			thistreetable = null;
			logger.finer("mothball succesfully done on table");
		} else {
			logger.fine("mothball called on table with null tableview " + this.toString());
		}

	}

	private class ObjectInStructure {
		private ObjectDataElt payload;
		private String cascade;
		private int level;
		private boolean leaf;

		public ObjectInStructure(ObjectDataElt payload, String cascade, int level, boolean leaf) {
			super();
			this.payload = payload;
			this.cascade = cascade;
			this.level = level;
			this.leaf = leaf;
		}

	}

	private void extractDataFromTree(
			ArrayList<ObjectInStructure> treedata,
			TreeItem<ObjectDataElt> nodetoprocess,
			int leveltouse,
			String cascadetouse,
			int circuitbreaker) {
		if (circuitbreaker > CObjectTreeArray.TREE_NAVIGATION_CIRCUITBREAKER)
			throw new RuntimeException("Circuit Breaker on too deep recursive structure");
		String currentnodecascade = cascadetouse;
		int levelforchildren = leveltouse;

		if (nodetoprocess.getValue() != null) {
			currentnodecascade = (cascadetouse.length() > 0 ? cascadetouse : "1");
			boolean leaf = true;
			if (nodetoprocess.getChildren().size() > 0)
				leaf = false;
			treedata.add(new ObjectInStructure(nodetoprocess.getValue(), currentnodecascade, leveltouse, leaf));
			levelforchildren++;
		}
		if (nodetoprocess.isExpanded()) {
			Iterator<TreeItem<ObjectDataElt>> childreniterator = nodetoprocess.getChildren().iterator();
			int childindex = 0;
			while (childreniterator.hasNext()) {
				childindex++;
				TreeItem<ObjectDataElt> thischild = childreniterator.next();
				String childcascade = currentnodecascade + (currentnodecascade.length() > 0 ? "." : "") + childindex;
				extractDataFromTree(treedata, thischild, levelforchildren, childcascade, circuitbreaker + 1);
			}
		}
	}

	public void copyAsFlatTreeToClipboard() {
		try {
			TreeItem<ObjectDataElt> rootnode = thistreetable.getRoot();
			ArrayList<ObjectInStructure> treedata = new ArrayList<ObjectInStructure>();
			extractDataFromTree(treedata, rootnode, 1, "", 1);
			int maxlevel = 0;
			for (int i = 0; i < treedata.size(); i++) {
				if (treedata.get(i).level > maxlevel)
					maxlevel = treedata.get(i).level;
			}
			StringBuilder clipboardstring = new StringBuilder();
			clipboardstring.append("<table cellspacing=\"0\" >");
			clipboardstring.append("<tr>");
			int line = 0;
			for (int i = 0; i < maxlevel - 1; i++) {
				clipboardstring.append("<th>LEVEL");
				clipboardstring.append((i + 1));
				clipboardstring.append("</th>");
			}

			boolean[] showcolumns = new boolean[payloadlist.size()];

			for (int j = 0; j < payloadlist.size(); j++) {
				CBusinessField<?> thiscolumnheader = payloadlist.get(j);
				boolean show = false;
				if (!thiscolumnheader.isShowinbottomnotes())
					show = true;

				showcolumns[j] = show;
				if (show) {
					String columntitle = thiscolumnheader.getLabel();
					clipboardstring.append("<th>");
					clipboardstring.append(columntitle);
					clipboardstring.append("</th>");
				}
			}
			clipboardstring.append("</tr>");

			String[] currentlabels = new String[maxlevel - 1];
			for (int i = 0; i < currentlabels.length; i++)
				currentlabels[i] = "";

			for (int i = 0; i < treedata.size(); i++) {
				line++;
				ObjectInStructure thisobject = treedata.get(i);
				ObjectDataElt thisrowdata = thisobject.payload;
				if (thisobject.level < maxlevel) {
					CBusinessField<?> thiscolumn = payloadlist.get(0);
					SimpleDataElt thiselement = thisrowdata.lookupEltByName(thiscolumn.getFieldname());
					currentlabels[thisobject.level - 1] = CObjectArray.printBusinessFieldToClipboard(actionmanager,
							thiscolumn, thiselement);
				}

				if (thisobject.leaf) {
					clipboardstring.append("<tr>");

					for (int j = 0; j < currentlabels.length; j++) {
						clipboardstring.append("<td>");
						clipboardstring.append(currentlabels[j]);
						clipboardstring.append("</td>");
					}
					for (int j = 0; j < payloadlist.size(); j++) {
						CBusinessField<?> thiscolumn = payloadlist.get(j);
						if (showcolumns[j]) {
							clipboardstring.append("<td>");
							SimpleDataElt thiselement = thisrowdata.lookupEltByName(thiscolumn.getFieldname());
							clipboardstring.append(
									CObjectArray.printBusinessFieldToClipboard(actionmanager, thiscolumn, thiselement));
							clipboardstring.append("</td>");
						}
					}
					clipboardstring.append("</tr>\n");
				}

			}
			clipboardstring.append("</table>");
			final ClipboardContent content = new ClipboardContent();
			content.putHtml(clipboardstring.toString());

			Clipboard.getSystemClipboard().setContent(content);
			actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar("Copied table with " + line
					+ " line(s) to clipboard. You may paste it in a spreadsheet or word processor");

		} catch (Exception e) {
			logger.warning("Error while copying data to clipboard " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning("   " + e.getStackTrace()[i]);
			actionmanager.getClientSession().getActiveClientDisplay()
					.updateStatusBar("Error while copying data to clipboard " + e.getMessage(), true);
		}
	}

	public void copyTableToClipboard(boolean showhiddenfields) {
		try {
			TreeItem<ObjectDataElt> rootnode = thistreetable.getRoot();
			ArrayList<ObjectInStructure> treedata = new ArrayList<ObjectInStructure>();
			extractDataFromTree(treedata, rootnode, 1, "", 1);
			StringBuilder clipboardstring = new StringBuilder();
			clipboardstring.append("<table cellspacing=\"0\" >");
			clipboardstring.append("<tr>");
			boolean[] showcolumns = new boolean[payloadlist.size()];
			clipboardstring.append("<th>Level</th><th>Cascade</th>");
			for (int j = 0; j < payloadlist.size(); j++) {
				CBusinessField<?> thiscolumnheader = payloadlist.get(j);
				boolean show = false;
				if (!thiscolumnheader.isShowinbottomnotes())
					show = true;
				if (thiscolumnheader.isShowinbottomnotes())
					if (showhiddenfields)
						show = true;
				showcolumns[j] = show;
				if (show) {
					String columntitle = thiscolumnheader.getLabel();
					clipboardstring.append("<th>");
					clipboardstring.append(columntitle);
					clipboardstring.append("</th>");
				}
			}
			clipboardstring.append("</tr>\n");
			int line = 0;
			for (int i = 0; i < treedata.size(); i++) {
				line++;
				ObjectInStructure thisobject = treedata.get(i);
				ObjectDataElt thisrowdata = thisobject.payload;
				clipboardstring.append("<tr>");
				clipboardstring.append("<td>");
				clipboardstring.append(thisobject.level);
				clipboardstring.append("</td>");
				clipboardstring.append("<td>");
				clipboardstring.append(thisobject.cascade);
				clipboardstring.append("</td>");

				for (int j = 0; j < payloadlist.size(); j++) {
					CBusinessField<?> thiscolumn = payloadlist.get(j);
					if (showcolumns[j]) {
						clipboardstring.append("<td>");
						SimpleDataElt thiselement = thisrowdata.lookupEltByName(thiscolumn.getFieldname());
						clipboardstring.append(
								CObjectArray.printBusinessFieldToClipboard(actionmanager, thiscolumn, thiselement));
						clipboardstring.append("</td>");
					}
				}
				clipboardstring.append("</tr>\n");
			}
			clipboardstring.append("</table>");
			final ClipboardContent content = new ClipboardContent();
			content.putHtml(clipboardstring.toString());

			Clipboard.getSystemClipboard().setContent(content);
			actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar("Copied table with " + line
					+ " line(s) to clipboard. You may paste it in a spreadsheet or word processor");
		} catch (Exception e) {
			logger.warning("Error while copying data to clipboard " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning("   " + e.getStackTrace()[i]);
			actionmanager.getClientSession().getActiveClientDisplay()
					.updateStatusBar("Error while copying data to clipboard " + e.getMessage(), true);
		}
	}

}
