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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine.ObjectInGrid;
import org.openlowcode.client.graphic.widget.table.CObjectGridLineColumn;
import org.openlowcode.client.graphic.widget.table.EditableTreeTable;
import org.openlowcode.client.graphic.widget.table.ObjectDataElementKeyExtractor;
import org.openlowcode.client.graphic.widget.table.ObjectDataElementValueUpdater;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.client.runtime.PageActionModifier;
import org.openlowcode.client.runtime.UnsavedDataWarning;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.richtext.RichText;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectDataEltType;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.structure.TimePeriodDataElt;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

/**
 * A grid is a way to display objects, with one object displayed per cell,
 * ordered in a specific place on the grid based on the value of specific
 * attributes. <br>
 * This assumes that there is only one object for the combination of lines and
 * columns
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CGrid
		extends
		CPageNode {
	private String linefield;
	private String columnfield;
	private int valuenr;
	private String[] valuefield;
	private String name;
	private CPageDataRef datareference;
	private ArrayList<CBusinessField<?>> payloadlist;
	private TableView<CObjectGridLine<String>> tableview;
	private NamedList<CObjectGridLine<String>> dataingrid;
	private ArrayList<ColumnAndStringIndex<String>> arraycolumns;
	private boolean isinlineupdate;
	private boolean updatemodeactive;
	private ArrayList<String> updateactionfields;
	private ArrayList<String> infoactionfields;
	private ArrayList<String> infoactionfieldsvalueexception;
	private CPageInlineAction updateinlineaction;
	private CPageAction cellaction;
	private ArrayList<CMultiFieldConstraint> allobjectconstraints;
	private CInlineActionDataRef inlineupdateactionoutputdataref;
	private ContextMenu contextmenu;
	private MenuItem startupdate;
	private MenuItem commitupdate;
	private MenuItem copydata;
	private UpdateMouseHandler updatemousehandler;
	private boolean iscellaction;
	private boolean unsavedupdatewarning;
	private boolean updatenote = false;
	private String updatewarningmessage;
	private String updatewarningcontinue;
	private String updatewarningstop;
	private String updatenotecomment = null;
	private PageActionManager actionmanager;
	private boolean hassecondarycolumn;
	private String secondarycolumnfield;
	private Tooltip tooltip;
	private boolean reversetree;
	private EditableTreeTable<ObjectDataElt> treetable;

	/**
	 * create a grid component
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CGrid(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		payloadlist = new ArrayList<CBusinessField<?>>();
		this.name = reader.returnNextStringField("NAME");
		this.linefield = reader.returnNextStringField("LNF");
		this.columnfield = reader.returnNextStringField("CLF");
		this.hassecondarycolumn = reader.returnNextBooleanField("HSC");
		if (this.hassecondarycolumn)
			this.secondarycolumnfield = reader.returnNextStringField("SCF");

		this.valuenr = reader.returnNextIntegerField("VLN");
		valuefield = new String[valuenr];
		for (int i = 0; i < valuenr; i++) {
			this.valuefield[i] = reader.returnNextStringField("VLF");
		}
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		reader.returnNextStartStructure("ATTRS");
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}

		this.isinlineupdate = reader.returnNextBooleanField("INLUPD");

		if (this.isinlineupdate) {
			this.updatemodeactive = false;
			this.updateactionfields = new ArrayList<String>();

			reader.returnNextStartStructure("INLUPD");
			reader.returnNextStartStructure("INLINEACTION");
			this.updateinlineaction = new CPageInlineAction(reader);

			reader.startStructureArray("FIELD");
			while (reader.structureArrayHasNextElement("FIELD")) {
				String fieldforupdate = reader.returnNextStringField("NAM");
				updateactionfields.add(fieldforupdate);
				reader.returnNextEndStructure("FIELD");
			}
			allobjectconstraints = new ArrayList<CMultiFieldConstraint>();
			this.inlineupdateactionoutputdataref = new CInlineActionDataRef(reader, this);

			reader.startStructureArray("CTR");
			while (reader.structureArrayHasNextElement("CTR")) {
				CMultiFieldConstraint constraint = new CMultiFieldConstraint(reader, payloadlist);
				allobjectconstraints.add(constraint);
			}
			this.updatenote = reader.returnNextBooleanField("UPDNOT");
			reader.returnNextEndStructure("INLUPD");

		}
		this.iscellaction = reader.returnNextBooleanField("ISCELLACT");
		if (this.iscellaction) {
			String actiontype = reader.returnNextStartStructure();
			if (!actiontype.equals("ACTION"))
				throw new RuntimeException("Inline Action not supported on grid for standard action");
			this.cellaction = new CPageAction(reader);
		}
		this.unsavedupdatewarning = reader.returnNextBooleanField("UNSDATWAR");
		if (this.unsavedupdatewarning) {
			this.updatewarningmessage = reader.returnNextStringField("UNSWARMES");
			this.updatewarningcontinue = reader.returnNextStringField("UNSWARCON");
			this.updatewarningstop = reader.returnNextStringField("UNSWARSTP");
		}
		this.reversetree = reader.returnNextBooleanField("RVT");
		infoactionfields = new ArrayList<String>();
		infoactionfieldsvalueexception = new ArrayList<String>();
		if (this.reversetree) {
			reader.startStructureArray("INFFLD");
			while (reader.structureArrayHasNextElement("INFFLD")) {
				String infofield = reader.returnNextStringField("NAM");
				infoactionfields.add(infofield);
				String valueexception = reader.returnNextStringField("EXC");
				infoactionfieldsvalueexception.add(valueexception);
				reader.returnNextEndStructure("INFFLD");
			}
		}
		reader.returnNextEndStructure("GRD");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("not yet implemented");
	}

	private static class UpdateMouseHandler
			implements
			EventHandler<MouseEvent> {
		private CGrid thisobjectgrid;

		@SuppressWarnings("unused")
		private EventHandler<? super MouseEvent> mouseeventlistener;
		private PageActionManager pageactionmanager;

		public UpdateMouseHandler(
				PageActionManager pageactionmanager,
				CGrid thisobjectgrid,
				EventHandler<? super MouseEvent> mouseeventlistener) {
			this.thisobjectgrid = thisobjectgrid;
			this.mouseeventlistener = mouseeventlistener;
			this.pageactionmanager = pageactionmanager;
		}

		@Override
		public void handle(MouseEvent event) {
			MouseButton button = event.getButton();

			if (button == MouseButton.PRIMARY) {
				if (event.getClickCount() == 1 && (event.isShiftDown())) {

					if (thisobjectgrid.isinlineupdate)
						if (!thisobjectgrid.updatemodeactive) { // currently, table in read mode, move to
							logger.fine("moving tableview " + thisobjectgrid.name + " to update mode");
							thisobjectgrid.tableview.setEditable(true);
							thisobjectgrid.tableview.getSelectionModel().setCellSelectionEnabled(true);
							thisobjectgrid.updatemodeactive = true;
							thisobjectgrid.startupdate.setDisable(true);
							thisobjectgrid.commitupdate.setDisable(false);
							return;
						}
				}
				if (thisobjectgrid.isinlineupdate)
					if (thisobjectgrid.updatemodeactive)
						if (event.getClickCount() == 1) {
							if (thisobjectgrid.tableview.getEditingCell() == null) {
								@SuppressWarnings("unchecked")
								TablePosition<CObjectGridLine<String>, ?> focusedCellPosition = thisobjectgrid.tableview
										.getFocusModel().getFocusedCell();
								thisobjectgrid.tableview.edit(focusedCellPosition.getRow(),
										focusedCellPosition.getTableColumn());

							}
						}
				if (thisobjectgrid.iscellaction)
					if (event.getClickCount() > 1) {
						// trigger the action on double click only if updatemode is not active
						if (!thisobjectgrid.updatemodeactive) {
							logger.info("Single action click detected");
							pageactionmanager.getMouseHandler().handle(event);
						}
					}
			}
		}

	}

	/**
	 * copy the content of the grid to the clipboard
	 */
	public void copyTableToClipboard() {
		ObservableList<CObjectGridLine<String>> griddata = tableview.getItems();
		StringBuilder clipboardstring = new StringBuilder();
		clipboardstring.append("<table cellspacing=\"0\" >");
		// write header
		clipboardstring.append("<tr>");
		for (int i = 0; i < this.arraycolumns.size(); i++) {
			TableColumn<CObjectGridLine<String>, String> thiscolumn = this.arraycolumns.get(i).column;
			String maincolumnlabel = thiscolumn.getText();
			if (thiscolumn.getColumns().size() == 0) {
				clipboardstring.append("<th>");
				clipboardstring.append(maincolumnlabel);
				clipboardstring.append("</th>");
			} else {
				for (int j = 0; j < thiscolumn.getColumns().size(); j++) {
					@SuppressWarnings("unchecked")
					TableColumn<CObjectGridLine<String>, String> subcolumn = (TableColumn<
							CObjectGridLine<String>, String>) thiscolumn.getColumns().get(j);
					String subcolumnlabel = maincolumnlabel + " " + subcolumn.getText();
					clipboardstring.append("<th>");
					clipboardstring.append(subcolumnlabel);
					clipboardstring.append("</th>");
				}
			}
		}
		clipboardstring.append("</tr>");
		for (int a = 0; a < griddata.size(); a++) {
			CObjectGridLine<String> thisgridline = griddata.get(a);

			clipboardstring.append("<tr>");
			for (int i = 0; i < this.arraycolumns.size(); i++) {
				TableColumn<CObjectGridLine<String>, String> thiscolumn = this.arraycolumns.get(i).column;

				if (thiscolumn.getColumns().size() == 0) {
					clipboardstring.append("<td>");
					clipboardstring.append(RichText.escapetoHTML(thiscolumn.getCellData(thisgridline).toString()));
					clipboardstring.append("</td>");
				} else {
					for (int j = 0; j < thiscolumn.getColumns().size(); j++) {
						TableColumn<
								CObjectGridLine<String>,
								?> subcolumn = (TableColumn<CObjectGridLine<String>, ?>) thiscolumn.getColumns().get(j);
						clipboardstring.append("<td>");
						clipboardstring.append(RichText.escapetoHTML(subcolumn.getCellData(thisgridline).toString()));

						clipboardstring.append("</td>");
					}
				}
			}
			clipboardstring.append("</tr>");

		}
		clipboardstring.append("</table>");
		final ClipboardContent content = new ClipboardContent();
		content.putHtml(clipboardstring.toString());

		Clipboard.getSystemClipboard().setContent(content);
		actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar("Copied grid with " + griddata.size()
				+ " line(s) to clipboard. You may paste it in a spreadsheet or word processor");

	}

	/**
	 * change the update state of the grid following user interaction
	 * 
	 * @param event      action event
	 * @param mouseevent mouse event
	 */
	public void launchupdate(ActionEvent event, MouseEvent mouseevent) {
		logger.fine(" --- ** --- display commit update with updatenote on cgrid is " + updatenote);
		boolean update = true;
		if (updatenote) {
			updatenotecomment = actionmanager.getClientDisplay().showModalTextEntry("Enter update note", 200);
			if (updatenotecomment == null)
				update = false;
		}
		if (update) {
			if (!this.reversetree) {
				tableview.setEditable(false);
				tableview.getSelectionModel().setCellSelectionEnabled(true);
				startupdate.setDisable(false);
				commitupdate.setDisable(true);

				tableview.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

				ObservableList<CObjectGridLine<String>> tabledata = tableview.getItems();
				boolean isupdated = false;
				for (int i = 0; i < tabledata.size(); i++) {
					CObjectGridLine<String> thisrow = tabledata.get(i);

					if (thisrow.isRowUpdate())
						isupdated = true;

				}
				if (isupdated) {
					if (event != null)
						actionmanager.handle(event);
					if (mouseevent != null)
						actionmanager.getMouseHandler().handle(mouseevent);
				} else {
					actionmanager.getClientSession().getActiveClientDisplay()
							.updateStatusBar("No modification performed on grid table in edit mode");
				}
				updatemodeactive = false;
			} else {
				// -------------------------
				treetable.setEditable(false);
				startupdate.setDisable(false);
				commitupdate.setDisable(true);

				int updatedrows = treetable.getUpdatedItems().size();
				if (updatedrows > 0) {
					if (event != null)
						actionmanager.handle(event);
					if (mouseevent != null)
						actionmanager.getMouseHandler().handle(mouseevent);
				} else {
					actionmanager.getClientSession().getActiveClientDisplay()
							.updateStatusBar("No modification performed on grid table in edit mode");
				}
			}
		}

	}

	private void resetAllUpdateFlags() {
		for (int i = 0; i < tableview.getItems().size(); i++) {
			CObjectGridLine<String> thisrow = tableview.getItems().get(i);
			thisrow.resetUpdateFlag();
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {

		this.actionmanager = actionmanager;
		ArrayDataElt<ObjectDataElt> data = getExternalContent(inputdata, datareference);
		if (!this.reversetree) {

			this.tooltip = new Tooltip("Double click on cell to see details\nRight click for update and copy");
			dataingrid = new NamedList<CObjectGridLine<String>>();
			arraycolumns = new ArrayList<ColumnAndStringIndex<String>>();
			// find the label of the line label

			String linefieldlabel = getLabelForField(this.linefield);

			// create a column for the line label
			CObjectGridLineColumn<String> linelabelcolumn = new CObjectGridLineColumn(linefieldlabel);
			linelabelcolumn.setEditable(false);
			// adding null as index string so that it appears first
			arraycolumns.add(new ColumnAndStringIndex(linelabelcolumn, null));

			HashMap<
					String,
					TableColumn<
							CObjectGridLine<String>,
							?>> datacolumnsbyname = new HashMap<String, TableColumn<CObjectGridLine<String>, ?>>();

			logger.finest("----------------- CGridLine " + data.getObjectNumber() + " lines ---------------");
			for (int i = 0; i < data.getObjectNumber(); i++) {
				ObjectDataElt thisline = data.getObjectAtIndex(i);
				SimpleDataElt faultyfieldtemp = thisline.lookupEltByName("YEARALLOCATED");
				if (faultyfieldtemp != null)
					logger.finest("index " + i + " YEARALLOCATED = " + faultyfieldtemp.toString());
				SimpleDataElt faultyfieldtempid = thisline.lookupEltByName("ID");
				if (faultyfieldtempid != null)
					logger.finest("index " + i + "ID = " + faultyfieldtempid.toString());
				String rowvalue = null;
				Comparable<?> rowordercode = null;
				String columnvalue = null;
				Comparable<?> columnordercode = null;
				String secondarycolumnvalue = null;
				String displayvalue[] = new String[valuefield.length];
				String fieldlabel[] = new String[valuefield.length];
				CBusinessField<?> displayfields[] = new CBusinessField[valuefield.length];
				for (int j = 0; j < payloadlist.size(); j++) {
					CBusinessField<?> thisfield = payloadlist.get(j);
					SimpleDataElt field = thisline.lookupEltByName(thisfield.getFieldname());
					String fieldvalue = field.defaultTextRepresentation();
					Comparable<?> candidaterowcode = null;
					logger.fine("---- analyzing field value for rank " + i + " for field " + thisfield.getFieldname());
					logger.fine(" * Default string representation is ");
					if (thisfield instanceof CChoiceField) {
						CChoiceField thischoicefield = (CChoiceField) thisfield;
						CChoiceFieldValue valueobject = thischoicefield.getChoiceFieldValue(fieldvalue);
						if (valueobject != null) {

							fieldvalue = valueobject.getDisplayvalue();
							candidaterowcode = String.format("%010d", valueobject.getSequence());
							logger.fine(" * field is choice field, fieldvalue = " + fieldvalue + ", stored value = "
									+ candidaterowcode);
						} else {
							fieldvalue = "";
						}

					}

					if (field instanceof TimePeriodDataElt) {
						candidaterowcode = ((TimePeriodDataElt) (field)).getPayload();
					}

					if (thisfield.getFieldname().equals(linefield)) {
						rowvalue = fieldvalue;
						rowordercode = candidaterowcode;

					}
					if (thisfield.getFieldname().equals(columnfield)) {
						columnvalue = fieldvalue;
						columnordercode = candidaterowcode;
					}

					if (this.hassecondarycolumn)
						if (thisfield.getFieldname().equals(this.secondarycolumnfield)) {
							secondarycolumnvalue = fieldvalue;
						}

					for (int k = 0; k < valuefield.length; k++) {
						if (thisfield.getFieldname().equals(valuefield[k])) {
							displayvalue[k] = fieldvalue;
							fieldlabel[k] = thisfield.getLabel();
							displayfields[k] = thisfield;
						}

					}

				}
				logger.fine("  ++ found data for line " + rowvalue + " for column " + columnvalue);

				if (rowvalue == null)
					throw new RuntimeException("Row value not found on object " + thisline.getUID());
				if (columnvalue == null)
					throw new RuntimeException("Column value not found on object " + thisline.getUID());
				if (this.hassecondarycolumn)
					if (secondarycolumnvalue == null)
						throw new RuntimeException("Secondary Column Value not found on object " + thisline.getUID());
				for (int k = 0; k < valuefield.length; k++) {
					if (fieldlabel[k] == null)
						throw new RuntimeException("Display value not found on object " + thisline.getUID());
					if (displayvalue[k] == null)
						throw new RuntimeException("Display value not found on object " + thisline.getUID());
				}
				CObjectGridLine<String> gridline = dataingrid.lookupOnName(Named.cleanName(rowvalue));
				if (gridline == null) {
					gridline = new CObjectGridLine(this, rowvalue, rowordercode);
					dataingrid.add(gridline);

				}
				if (this.hassecondarycolumn) {

					gridline.addObject(columnvalue, secondarycolumnvalue, displayvalue, fieldlabel, thisline);
					logger.finest("Adding to Grid Line " + gridline.getLineLabel() + " (" + gridline.hashCode()
							+ ") with secondary " + thisline.hashCode() + " - " + thisline.lookupEltByName("ID") + " - "
							+ thisline.lookupEltByName("YEARALLOCATED"));
				} else {
					gridline.addObject(columnvalue, displayvalue, fieldlabel, thisline);
					logger.finest("Adding to Grid Line " + gridline.getLineLabel() + " (" + gridline.hashCode() + ")"
							+ thisline.hashCode() + " - " + thisline.lookupEltByName("ID") + " - "
							+ thisline.lookupEltByName("YEARALLOCATED"));

				}
				if (datacolumnsbyname.get(columnvalue) == null) { // actually usefull, as done in the loop of first
																	// object
																	// only
					if (valuefield.length == 1) {

						// show one field only per column
						if (this.hassecondarycolumn) {
							// just create the missing column for over-column.
							TableColumn<
									CObjectGridLine<String>,
									String> overcolumn = new TableColumn<CObjectGridLine<String>, String>(columnvalue);
							overcolumn.setId(columnvalue);
							datacolumnsbyname.put(columnvalue, overcolumn);
							arraycolumns.add(new ColumnAndStringIndex(overcolumn,
									(columnordercode != null ? columnordercode : columnvalue)));
						} else {
							String updatekey = (this.updateinlineaction != null ? this.updateinlineaction.key() : null);
							boolean updatefield = false;
							if (this.updateactionfields != null)
								for (int z = 0; z < this.updateactionfields.size(); z++)
									if (displayfields[0].getFieldname().equals(updateactionfields.get(z)))
										updatefield = true;
							if (!updatefield)
								updatekey = null;
							TableColumn<CObjectGridLine<String>, ?> datacolumn = displayfields[0]
									.getTableColumnForGrid(actionmanager, 12, updatekey, columnvalue, null, true);

							datacolumnsbyname.put(columnvalue, datacolumn);
							arraycolumns.add(new ColumnAndStringIndex(datacolumn,
									(columnordercode != null ? columnordercode : columnvalue)));
						}
					} else {
						logger.fine(" --**-- adding column " + columnvalue);
						TableColumn<
								CObjectGridLine<String>,
								String> overcolumn = new TableColumn<CObjectGridLine<String>, String>(columnvalue);
						overcolumn.setId(columnvalue);
						for (int k = 0; k < valuefield.length; k++) {

							String updatekey = (this.updateinlineaction != null ? this.updateinlineaction.key() : null);
							boolean updatefield = false;
							if (this.updateactionfields != null)
								for (int z = 0; z < this.updateactionfields.size(); z++)
									if (displayfields[k].getFieldname().equals(updateactionfields.get(z)))
										updatefield = true;
							if (!updatefield)
								updatekey = null;
							TableColumn<CObjectGridLine<String>, ?> datacolumn = displayfields[k]
									.getTableColumnForGrid(actionmanager, 12, updatekey, columnvalue, null, false);
							overcolumn.getColumns().add(datacolumn);

							datacolumn.setId(columnvalue + "/" + fieldlabel[k]);
							logger.fine("   *-* adding subcolumn " + fieldlabel[k]);

							if (k == 0)
								datacolumnsbyname.put(columnvalue, datacolumn);
						}

						arraycolumns.add(new ColumnAndStringIndex(overcolumn,
								(columnordercode != null ? columnordercode : columnvalue)));

					}
				} // actually usefull, as done in the loop of first object only
				if (this.hassecondarycolumn) {
					// missing the secondary column
					if (datacolumnsbyname.get(
							CObjectGridLine.buildtwofieldscolumnindex(columnvalue, secondarycolumnvalue)) == null) {
						TableColumn<CObjectGridLine<String>, ?> overcolumn = datacolumnsbyname.get(columnvalue);
						String updatekey = (this.updateinlineaction != null ? this.updateinlineaction.key() : null);
						boolean updatefield = false;
						if (this.updateactionfields != null)
							for (int z = 0; z < this.updateactionfields.size(); z++)
								if (displayfields[0].getFieldname().equals(updateactionfields.get(z)))
									updatefield = true;
						if (!updatefield)
							updatekey = null;
						TableColumn<CObjectGridLine<String>, ?> datacolumn = displayfields[0].getTableColumnForGrid(
								actionmanager, 12, updatekey, columnvalue, secondarycolumnvalue, true);

						datacolumnsbyname.put(
								CObjectGridLine.buildtwofieldscolumnindex(columnvalue, secondarycolumnvalue),
								datacolumn);
						overcolumn.getColumns().add(datacolumn);
						datacolumn.setId(CObjectGridLine.buildtwofieldscolumnindex(columnvalue, secondarycolumnvalue));

					}
				}
			}
			// ------------------------------------------------------ End of determination
			// of data-model -----------------------------
			tableview = this.generateTableViewModel();
			ObservableList<CObjectGridLine<String>> thistabledata = FXCollections.observableArrayList();
			List<CObjectGridLine<String>> linestoorder = dataingrid.getFullList();
			Collections.sort(linestoorder);

			for (int i = 0; i < linestoorder.size(); i++) {
				logger.fine("Display ordered grid key = " + linestoorder.get(i).getCodeToOrder());
				CObjectGridLine<String> thisgridline = linestoorder.get(i);
				logger.finest("---------- Audit of objects in grid, line = " + i + " " + thisgridline.getLineLabel()
						+ " (" + thisgridline.hashCode() + ")---------------- ");
				for (int j = 0; j < thisgridline.getObjectinlineNumber(); j++) {
					ObjectInGrid thisobject = thisgridline.getObjectinline(j);
					logger.finest("   --> object  " + j + " - " + thisobject.getObject().hashCode() + " - "
							+ thisobject.getObject().lookupEltByName("ID") + " - "
							+ thisobject.getObject().lookupEltByName("YEARALLOCATED"));
				}
				thistabledata.add(thisgridline);
			}
			if (cellaction != null) {
				logger.fine(" **--** for grid " + tableview + " put action " + cellaction.getModule() + "."
						+ cellaction.getName());
				actionmanager.registerEvent(tableview, cellaction);

			}
			tableview.setItems(thistabledata);

			// -------------------------------------------
			// ---- C O N T E X T . M E N U --------------
			// -------------------------------------------

			contextmenu = new ContextMenu();

			startupdate = new MenuItem("Start Update");
			commitupdate = new MenuItem("Store Update");

			copydata = new MenuItem("Copy Data");
			contextmenu.getItems().add(startupdate);
			contextmenu.getItems().add(commitupdate);
			contextmenu.getItems().add(copydata);
			if ((this.isinlineupdate)) {
				startupdate.setDisable(false);
				commitupdate.setDisable(true);
			} else {
				startupdate.setDisable(true);
				commitupdate.setDisable(true);
			}

			copydata.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					copyTableToClipboard();
				}

			});
			startupdate.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					tableview.setEditable(true);
					tableview.getSelectionModel().setCellSelectionEnabled(true);

					updatemodeactive = true;
					startupdate.setDisable(true);
					commitupdate.setDisable(false);

					actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar(
							"You have started editing a grid table, please do not forget to save before leaving the page (richt click on another cell + store update)",
							true);
				}
			});

			if (this.updateinlineaction != null)
				actionmanager.registerInlineAction(commitupdate, updateinlineaction);

			if (this.inlineupdateactionoutputdataref != null) {
				inputdata.addInlineActionDataRef(this.inlineupdateactionoutputdataref);
			}

			commitupdate.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					launchupdate(event, null);

				}

			});

			tableview.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

				@Override
				public void handle(ContextMenuEvent event) {
					if (contextmenu.isShowing()) {
						contextmenu.hide();
					} else {
						tableview.getSelectionModel().clearSelection();
						contextmenu.show(tableview, event.getScreenX(), event.getScreenY());

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
			this.tableview.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
				if (contextmenu.isShowing())
					contextmenu.hide();
			});
			// -------------------------------------------
			// ---- E N D . C O N T E X T . M E N U ------
			// -------------------------------------------
			if (this.updateinlineaction != null)
				actionmanager.registerInlineActionWithModifier(tableview, this.updateinlineaction,
						PageActionModifier.getShiftPressed());

			if ((this.isinlineupdate) || (this.iscellaction)) {
				EventHandler<? super MouseEvent> mouseeventlistener = tableview.getOnMouseClicked();
				this.updatemousehandler = new UpdateMouseHandler(actionmanager, this, mouseeventlistener);
				tableview.setOnMouseClicked(updatemousehandler);
			}
			return tableview;
		} else {
			// ------------------------- reverse tree grid display

			ArrayList<ObjectDataElt> datainlist = new ArrayList<ObjectDataElt>();
			for (int i = 0; i < data.getObjectNumber(); i++)
				datainlist.add(data.getObjectAtIndex(i));
			treetable = new EditableTreeTable<ObjectDataElt>(datainlist);
			treetable.setDefaultIsReadOnly(true);
			// -- dealing with lines (that will actually be displayed as columns)

			if (this.valuefield.length != 1) {
				StringBuffer dropactionfields = new StringBuffer();
				for (int i = 0; i < this.valuefield.length; i++)
					dropactionfields.append(this.valuefield[i] + "/");
				throw new RuntimeException("Only one update field managed, currently " + this.updateactionfields.size()
						+ " fields defined " + dropactionfields);
			}
			CBusinessField<?> linefield = getFieldForFieldName(this.linefield);
			CBusinessField<?> payloadfield = getFieldForFieldName(this.valuefield[0]);

			if (!(linefield instanceof ObjectDataElementKeyExtractor))
				throw new RuntimeException(
						"Field " + this.linefield + " cannot be used as column criteria for column group");
			if (!(payloadfield instanceof ObjectDataElementValueUpdater))
				throw new RuntimeException("Field " + this.updateactionfields.get(0)
						+ " cannot be used as payload criteria for a column group");

			ObjectDataElementKeyExtractor<ObjectDataElt, ?> columnextractor = (ObjectDataElementKeyExtractor) linefield;
			ObjectDataElementValueUpdater<ObjectDataElt, ?> valueupdater = (ObjectDataElementValueUpdater) payloadfield;

			for (int i = 0; i < this.infoactionfields.size(); i++) {
				CBusinessField<?> infofield = getFieldForFieldName(this.infoactionfields.get(i));
				if (!(infofield instanceof ObjectDataElementKeyExtractor))
					throw new RuntimeException("Field" + infofield + " cannot be used as info column");
				ObjectDataElementKeyExtractor<
						ObjectDataElt, ?> infofieldextractor = (ObjectDataElementKeyExtractor) infofield;
				String exceptions = this.infoactionfieldsvalueexception.get(i);
				if (exceptions == null)
					treetable.setColumnReadOnlyField(infofield.getLabel(), infofieldextractor,
							EditableTreeTable.GROUPING_SAME);
				if (exceptions != null)
					treetable.setColumnReadOnlyField(infofield.getLabel(), infofieldextractor,
							EditableTreeTable.GROUPING_SAME, exceptions);
			}

			treetable.setColumnGrouping(columnextractor, valueupdater, "Total", EditableTreeTable.GROUPING_SUM);

			CBusinessField<?> maincolumnbusinessfield = getFieldForFieldName(this.columnfield);

			if (!(maincolumnbusinessfield instanceof ObjectDataElementKeyExtractor))
				throw new RuntimeException(
						"Field " + this.columnfield + " cannot be used as line criteria for editable table tree");
			ObjectDataElementKeyExtractor<
					ObjectDataElt, ?> mainlineextractor = (ObjectDataElementKeyExtractor) maincolumnbusinessfield;

			treetable.setLineGrouping(mainlineextractor);

			if (this.hassecondarycolumn) {

				CBusinessField<?> secondarycolumnbusinessfield = getFieldForFieldName(this.secondarycolumnfield);

				if (!(secondarycolumnbusinessfield instanceof ObjectDataElementKeyExtractor))
					throw new RuntimeException(
							"Field " + this.columnfield + " cannot be used as line criteria for editable table tree");

				ObjectDataElementKeyExtractor<
						ObjectDataElt,
						?> secondarylineextractor = (ObjectDataElementKeyExtractor) secondarycolumnbusinessfield;

				treetable.setLineGrouping(secondarylineextractor);
			}

			// -------------------------------------------
			// ---- C O N T E X T . M E N U --------------
			// -------------------------------------------

			contextmenu = new ContextMenu();

			startupdate = new MenuItem("Start Update");
			commitupdate = new MenuItem("Store Update");
			contextmenu.getItems().add(startupdate);
			contextmenu.getItems().add(commitupdate);

			if ((this.isinlineupdate)) {
				startupdate.setDisable(false);
				commitupdate.setDisable(true);
			} else {
				startupdate.setDisable(true);
				commitupdate.setDisable(true);
			}

			startupdate.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					treetable.setEditable(true);

					updatemodeactive = true;
					startupdate.setDisable(true);
					commitupdate.setDisable(false);

					actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar(
							"You have started editing a grid table, please do not forget to save before leaving the page (richt click on another cell + store update)",
							true);
				}
			});

			if (this.updateinlineaction != null)
				actionmanager.registerInlineAction(commitupdate, updateinlineaction);

			if (this.inlineupdateactionoutputdataref != null) {
				inputdata.addInlineActionDataRef(this.inlineupdateactionoutputdataref);
			}

			commitupdate.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					launchupdate(event, null);

				}

			});
			if (this.cellaction != null)
				treetable.setDoubleClickReadOnlyEventHandler(actionmanager.getMouseHandler(), true);
			Node treetablenode = treetable.getNode();
			if (this.cellaction != null)
				actionmanager.registerEvent(treetablenode, this.cellaction);

			treetablenode.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

				@Override
				public void handle(ContextMenuEvent event) {
					logger.fine("Context menu requested on editabletreetable");
					if (contextmenu.isShowing()) {
						logger.fine("After showing, hide");
						contextmenu.hide();
					} else {
						logger.fine("After hiding, show");
						contextmenu.show(treetablenode, event.getScreenX(), event.getScreenY());

					}
				}
			});

			contextmenu.focusedProperty().addListener(new ChangeListener<Boolean>() {

				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
					logger.fine(" changed value on context menu");
					if (!newvalue) {
						logger.fine(" context menu hide");
						contextmenu.hide();

					}

				}

			});

			return treetablenode;
		}
	}

	private CBusinessField<?> getFieldForFieldName(String fieldname) {
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> thisfield = payloadlist.get(i);
			if (thisfield.getFieldname().equals(fieldname))
				return thisfield;
		}
		throw new RuntimeException("line field label " + fieldname + " not found");
	}

	private String getLabelForField(String fieldname) {
		return getFieldForFieldName(fieldname).getLabel();
	}

	private TableView<CObjectGridLine<String>> generateTableViewModel() {
		TableView<CObjectGridLine<String>> returntable = new TableView<CObjectGridLine<String>>();
		Collections.sort(arraycolumns);
		for (int i = 0; i < arraycolumns.size(); i++) {
			TableColumn<CObjectGridLine<String>, String> thiscolumn = arraycolumns.get(i).column;
			logger.fine("  GTVM --- " + thiscolumn.getId());
			ObservableList<TableColumn<CObjectGridLine<String>, ?>> subcolumns = thiscolumn.getColumns();
			for (int k = 0; k < subcolumns.size(); k++)
				logger.fine("    GTVM     ++ " + subcolumns.get(k).getId());
			returntable.getColumns().add(thiscolumn);
		}
		double finalheightinpixel = 29;
		returntable.setRowFactory(tv -> new TableRow<CObjectGridLine<String>>() {

			@Override
			public void updateItem(CObjectGridLine<String> object, boolean empty) {
				super.updateItem(object, empty);
				this.setMaxHeight(finalheightinpixel);
				this.setMinHeight(finalheightinpixel);
				this.setPrefHeight(finalheightinpixel);
				this.setTextOverrun(OverrunStyle.ELLIPSIS);
				this.setEllipsisString("...");
			}
		});
		returntable.getSelectionModel().setCellSelectionEnabled(true);
		returntable.setTooltip(tooltip);
		return returntable;
	}

	/**
	 * parses the page data to get an array of object data elements
	 * 
	 * @param inputdata page input data
	 * @param dataref   reference of the data to use in the page input data
	 * @return an array of object data elements
	 */
	public ArrayDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thiselementarray;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {

		if (type instanceof ArrayDataEltType) {
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>) type;
			DataEltType payloadtypeinarray = arraytype.getPayloadType();
			if (payloadtypeinarray instanceof ObjectDataEltType) {
				ObjectDataEltType objecttype = (ObjectDataEltType) payloadtypeinarray;

				ArrayDataElt<ObjectDataElt> output = new ArrayDataElt<ObjectDataElt>(eltname, objecttype);
				if (!this.reversetree) {
					ObservableList<CObjectGridLine<String>> tabledata = tableview.getItems();
					int countrowsupdated = 0;
					for (int i = 0; i < tabledata.size(); i++) {
						CObjectGridLine<String> thisrow = tabledata.get(i);

						if (thisrow.isRowUpdate()) {
							thisrow.fillRowUpdated(output, eltname);

							countrowsupdated++;
						}
					}
					logger.fine("Selected rows updated and sent back, number = " + countrowsupdated
							+ " for element name = " + eltname);

					this.resetAllUpdateFlags();
				} else {
					List<ObjectDataElt> updateditems = this.treetable.getUpdatedItems();
					for (int i = 0; i < updateditems.size(); i++) {
						updateditems.get(i).changeName(eltname);
						output.addElement(updateditems.get(i));
					}
				}
				return output;
			}
		}

		if (type instanceof TextDataEltType) {
			TextDataElt updatenote = new TextDataElt(eltname, this.updatenotecomment);
			logger.fine("got update note and put it in element " + eltname);
			return updatenote;
		}

		if (type instanceof ObjectIdDataEltType) {
			if (objectdataloc == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			if (!this.reversetree) {
				CObjectGridLine<String> gridline = this.tableview.getSelectionModel().getSelectedItem();
				@SuppressWarnings({ "unchecked", "rawtypes" })
				ObservableList<TablePosition<?, ?>> selectedcell = (ObservableList) this.tableview.getSelectionModel()
						.getSelectedCells();
				if (selectedcell.size() != 1)
					throw new RuntimeException("Only 1 selected celle is managed");
				TablePosition<?, ?> cell = selectedcell.get(0);
				TableColumn<?, ?> selectedcolumn = cell.getTableColumn();
				TableColumnBase<?, ?> parentcolumn = selectedcolumn.getParentColumn();
				String columntitle = null;
				if (parentcolumn == null) {
					// case of only one value
					columntitle = selectedcolumn.getText();
				}

				if (parentcolumn != null) {
					// case of several values
					if (!this.hassecondarycolumn)
						columntitle = parentcolumn.getText();

				}
				ObjectDataElt object = null;
				if (!this.hassecondarycolumn)
					object = gridline.getObjectForColumn(columntitle);
				if (this.hassecondarycolumn)
					object = gridline.getObjectForColumn(parentcolumn.getText(), selectedcolumn.getText());

				SimpleDataElt field = object.lookupEltByName(objectdataloc);
				if (field == null)
					throw new RuntimeException(
							"field not found " + objectdataloc + ", available fields = " + object.dropFieldNames());
				if (!(field instanceof TextDataElt))
					throw new RuntimeException("field for name = " + objectdataloc + " is not text");
				TextDataElt textfield = (TextDataElt) field;
				ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
				return objectid;
			} else {
				// get object id of selected cell
				List<ObjectDataElt> selected = this.treetable.getSelectedElements();
				if (selected != null)
					if (selected.size() == 1) {
						ObjectDataElt selecteditem = selected.get(0);

						SimpleDataElt field = selecteditem.lookupEltByName(objectdataloc);
						if (field == null)
							throw new RuntimeException("field not found " + objectdataloc + ", available fields = "
									+ selecteditem.dropFieldNames());
						if (!(field instanceof TextDataElt))
							throw new RuntimeException("field for name = " + objectdataloc + " is not text");
						TextDataElt textfield = (TextDataElt) field;
						ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
						return objectid;

					}

			}
		}

		throw new RuntimeException(String.format("Unsupported extraction type %s ", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		reviewDataWarningForGrid();

		if (!(dataelt instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("inline page data does not have expected %s type, actually found %s",
							dataelt.getName(), dataelt.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) dataelt;
		HashMap<String, ObjectDataElt> updatedrowsbyid = new HashMap<String, ObjectDataElt>();
		for (int i = 0; i < thiselementarray.getObjectNumber(); i++) {
			updatedrowsbyid.put(thiselementarray.getObjectAtIndex(i).getUID(), thiselementarray.getObjectAtIndex(i));
		}
		int updated = 0;
		for (int i = 0; i < dataingrid.getSize(); i++) {
			CObjectGridLine<String> currentline = dataingrid.get(i);
			for (int j = 0; j < currentline.getObjectinlineNumber(); j++) {
				ObjectInGrid object = currentline.getObjectinline(j);
				String uid = object.getObject().getUID();
				ObjectDataElt relevantobject = updatedrowsbyid.get(uid);
				logger.finest("adding new object through force update data - Adding to Grid Line "
						+ currentline.getLineLabel() + " (" + currentline.hashCode() + ") " + relevantobject.hashCode()
						+ " - " + relevantobject.lookupEltByName("ID") + " - "
						+ relevantobject.lookupEltByName("YEARALLOCATED"));
				if (relevantobject != null) {
					object.forceUpdatedObject(relevantobject);
					updated++;
				}

			}
		}
		if (updated != thiselementarray.getObjectNumber())
			actionmanager.getClientSession().getActiveClientDisplay()
					.updateStatusBar("Received " + thiselementarray.getObjectNumber() + " elements, but only " + updated
							+ " could be updated on the page", true);
	}

	@Override
	public void mothball() {

		if (tableview != null) {
			if (tableview.getFocusModel() != null)
				tableview.getFocusModel().focus(null);

			if (this.updatemousehandler != null) {
				tableview.removeEventHandler(MouseEvent.MOUSE_CLICKED, updatemousehandler);
			}
			tableview.setOnMouseClicked(null);
			tableview.setOnKeyPressed(null);
			tableview.setOnKeyReleased(null);
			tableview.setSelectionModel(null);

			tableview.setOnContextMenuRequested(null);
			copydata.setOnAction(null);
			startupdate.setOnAction(null);
			commitupdate.setOnAction(null);
			this.actionmanager = null;
			ObservableList<CObjectGridLine<String>> tablerows = tableview.getItems();
			Iterator<CObjectGridLine<String>> rowiterator = tablerows.iterator();
			while (rowiterator.hasNext()) {
				CObjectGridLine<String> thisrow = rowiterator.next();
				thisrow.mothball();
			}

			tableview.getSortOrder().clear();
			tableview.getItems().clear();

			if (tableview.getColumns() != null)
				if (tableview.getColumns().size() > 0) {

					tableview.getColumns().clear();
				}
			tableview = null;
			logger.fine("mothball succesfully done on table");
		} else {
			logger.warning("mothball called on table with null tableview " + this.toString());
		}

	}

	private class ColumnAndStringIndex<E extends Comparable<E>>
			implements
			Comparable<ColumnAndStringIndex<E>> {
		private TableColumn<CObjectGridLine<String>, String> column;
		private E orderedcolumnid;

		@Override
		public int compareTo(ColumnAndStringIndex<E> o) {
			if (this.orderedcolumnid == null) {
				if (o.orderedcolumnid != null)
					return -1;
				if (o.orderedcolumnid == null)
					return 0;
			}
			if (o.orderedcolumnid == null)
				return 1;
			return this.orderedcolumnid.compareTo(o.orderedcolumnid);
		}

		public ColumnAndStringIndex(TableColumn<CObjectGridLine<String>, String> column, E orderedcolumnid) {
			super();
			this.column = column;
			this.orderedcolumnid = orderedcolumnid;
		}

		@SuppressWarnings("unused")
		public TableColumn<CObjectGridLine<String>, String> getColumn() {
			return column;
		}

		@SuppressWarnings("unused")
		public E getOrderedcolumnid() {
			return orderedcolumnid;
		}

	}

	/**
	 * When this method is triggered, it reviews the number of rows in the table
	 * that have been edited, and according to total data table, create or remove
	 * the data update warning
	 */
	public void reviewDataWarningForGrid() {
		if (this.unsavedupdatewarning) {

			logger.fine("------------------- starting evaluating unsaved data -----------------------");
			int updatedrow = 0;
			if (!this.reversetree) {
				ObservableList<CObjectGridLine<String>> objecttablerow = tableview.getItems();
				Iterator<CObjectGridLine<String>> rowiterator = objecttablerow.iterator();
				while (rowiterator.hasNext()) {
					CObjectGridLine<String> thisrow = rowiterator.next();
					if (thisrow.isRowUpdate())
						updatedrow++;
				}
			} else {
				updatedrow = this.treetable.getUpdatedItems().size();
			}
			if (updatedrow == 0) {
				// remove any present unsaved data warning for this component
				this.actionmanager.removedUnsavedDataWarningForNode(this);
				logger.fine("   * remove all updates ");
			} else {
				logger.fine("   * remove all updates ");
				this.actionmanager.addUnsavedDataWarning(new UnsavedDataWarning(this.updatewarningmessage, // message
						this.updatewarningcontinue, // continue message
						this.updatewarningstop, // stop message
						this)); // originnode

			}
		}
	}
}
