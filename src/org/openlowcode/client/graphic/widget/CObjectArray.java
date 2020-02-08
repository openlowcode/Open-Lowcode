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
import java.math.BigDecimal;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.richtext.RichText;
import org.openlowcode.client.graphic.widget.table.CObjectArrayColumnModel;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.client.runtime.PageActionModifier;
import org.openlowcode.client.runtime.UnsavedDataWarning;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.IntegerDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectDataEltType;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TabPane;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * This table allows to show a an array of objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectArray
		extends
		CPageNode {

	@SuppressWarnings("unused")
	private String externaldatareference;
	private String name;
	private boolean inline;
	private boolean inputdata;
	private boolean allowdataclear;
	private ArrayList<CBusinessField<?>> payloadlist;
	private CPageDataRef datareference;
	private TableView<ObjectTableRow> thistable;
	private CPageAction action;
	private CInlineActionDataRef inlineactiondataref;

	@SuppressWarnings("unused")
	private Window parentwindow;
	private int forcedrowheight;
	private boolean isinlineupdate;
	private boolean updatemodeactive;
	private boolean allowmultipleselect;
	// ----------------------- context menu
	private ContextMenu contextmenu;

	private MenuItem startupdate;
	private MenuItem commitupdate;
	private MenuItem copydata;
	private MenuItem copytechdata;

	private CPageInlineAction updateinlineaction;
	private CPageAction updateaction;
	private CInlineActionDataRef inlineupdateactionoutputdataref;
	private ArrayList<String> updateactionfields;

	private Background normalbackground;
	private Background editablebackground;
	private CPageInlineAction inlineaction;
	private ChangeListener<Boolean> focuslostlistener;
	private ArrayList<CMultiFieldConstraint> allobjectconstraints;
	private boolean isupdate;
	private HashMap<String, CPageDataRef> overridenlabels;

	private boolean unsavedupdatewarning;
	private String updatewarningmessage;
	private String updatewarningcontinue;
	private String updatewarningstop;
	private boolean updatenote = false;
	private int rowstodisplay;

	/**
	 * launch an update on the table after client finished entering data in thetable
	 * 
	 * @param event      action event
	 * @param mouseevent mouse event
	 */
	public void launchupdate(ActionEvent event, MouseEvent mouseevent) {
		boolean update = true;
		if (updatenote) {
			updatenotecomment = actionmanager.getClientDisplay().showModalTextEntry("Enter update note", 200);
			if (updatenotecomment == null)
				update = false;
		}
		if (update) {
			thistable.setEditable(false);
			thistable.getSelectionModel().setCellSelectionEnabled(false);
			startupdate.setDisable(false);
			commitupdate.setDisable(true);
			if (this.allowdataclear) {
				this.clearall.setDisable(false);
				this.clearselected.setDisable(false);
			}

			if (allowmultipleselect) {
				thistable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			} else {
				thistable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
			}
			thistable.setBackground(normalbackground);
			ObservableList<ObjectTableRow> tabledata = thistable.getItems();
			boolean isupdated = false;
			for (int i = 0; i < tabledata.size(); i++) {
				ObjectTableRow thisrow = tabledata.get(i);

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
						.updateStatusBar("No modification performed on table in edit mode");

			}
			updatemodeactive = false;
		}
	}

	/**
	 * creates an object array from the message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectArray(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		this.allowmultipleselect = reader.returnNextBooleanField("AMS");
		this.allowdataclear = reader.returnNextBooleanField("ADC");
		this.forcedrowheight = reader.returnNextIntegerField("RWH");
		this.rowstodisplay = reader.returnNextIntegerField("RTD");
		// --------------------------------------------------

		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;
			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				this.action = new CPageAction(reader);
				reader.startStructureArray("ATTR");
			} else {
				if (actiontag.getStructurename().compareTo("INLINEACTION") == 0) {
					this.inlineaction = new CPageInlineAction(reader);
					reader.startStructureArray("ATTR");
				} else {
					if (actiontag.getStructurename().compareTo("ATTRS") == 0) {

					} else {
						throw new RuntimeException("expected a startstructure 'ATTRS' tag, got " + element.toString()
								+ " at path " + reader.getCurrentElementPath());
					}

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

		if (inline) {
			this.inlineactiondataref = new CInlineActionDataRef(reader, this);

		}
		this.inputdata = reader.returnNextBooleanField("IND");
		if (this.inputdata) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
		}
		this.isinlineupdate = reader.returnNextBooleanField("INLUPD");

		if (this.isinlineupdate) {

			this.updateactionfields = new ArrayList<String>();

			reader.returnNextStartStructure("INLUPD");
			this.updatemodeactive = reader.returnNextBooleanField("DUM");
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
			logger.fine("			-- Exceptional debug for inline action update note " + this.updatenote);
			reader.returnNextEndStructure("INLUPD");

		}
		this.isupdate = reader.returnNextBooleanField("UPD");
		if (this.isupdate) {

			this.updateactionfields = new ArrayList<String>();
			reader.returnNextStartStructure("UPD");
			this.updatemodeactive = reader.returnNextBooleanField("DUM");
			reader.returnNextStartStructure("ACTION");
			this.updateaction = new CPageAction(reader);
			reader.startStructureArray("FIELD");
			while (reader.structureArrayHasNextElement("FIELD")) {
				String fieldforupdate = reader.returnNextStringField("NAM");
				this.updateactionfields.add(fieldforupdate);
				reader.returnNextEndStructure("FIELD");
			}
			allobjectconstraints = new ArrayList<CMultiFieldConstraint>();
			reader.startStructureArray("CTR");
			while (reader.structureArrayHasNextElement("CTR")) {
				CMultiFieldConstraint constraint = new CMultiFieldConstraint(reader, payloadlist);
				allobjectconstraints.add(constraint);
			}
			this.updatenote = reader.returnNextBooleanField("UPDNOT");
			logger.fine("			-- Exceptional debug for action update note " + this.updatenote);
			reader.returnNextEndStructure("UPD");
		}

		overridenlabels = new HashMap<String, CPageDataRef>();
		reader.startStructureArray("OVWLBL");
		while (reader.structureArrayHasNextElement("OVWLBL")) {
			String fieldtooverride = reader.returnNextStringField("FLD");
			CPageDataRef dataref = CPageDataRef.parseCPageDataRef(reader);
			overridenlabels.put(fieldtooverride, dataref);
			reader.returnNextEndStructure("OVWLBL");
		}
		this.unsavedupdatewarning = reader.returnNextBooleanField("UNSDATWAR");
		if (this.unsavedupdatewarning) {
			this.updatewarningmessage = reader.returnNextStringField("UNSWARMES");
			this.updatewarningcontinue = reader.returnNextStringField("UNSWARCON");
			this.updatewarningstop = reader.returnNextStringField("UNSWARSTP");
		}
		reader.returnNextEndStructure("OBJARR");
	}

	/**
	 * reads an array of object data elements
	 * 
	 * @param inputdata page input data
	 * @param dataref   reference of the data element to take
	 * @return the array of objects
	 */
	public ArrayDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", dataref.getName()));
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thiselementarray;
	}

	private ReadOnlyMouseHandler readonlymousehandler;
	private UpdateMouseHandler updatemousehandler;
	private PageActionManager actionmanager;
	private String updatenotecomment = null;
	private MenuItem clearall;
	private MenuItem clearselected;

	/**
	 * prints the column to the clipboard
	 * 
	 * @param actionmanager action manager
	 * @param thiscolumn    a column
	 * @param thiselement   element to print
	 * @return a string
	 */
	public static String printBusinessFieldToClipboard(
			PageActionManager actionmanager,
			CBusinessField<?> thiscolumn,
			SimpleDataElt thiselement) {
		boolean specialtreatment = false;
		if (thiscolumn instanceof CChoiceField) {
			CChoiceField thischoicefield = (CChoiceField) thiscolumn;
			CChoiceFieldValue choicevalue = thischoicefield
					.getChoiceFieldValue(thiselement.defaultTextRepresentation());
			if (choicevalue != null) {
				specialtreatment = true;
				return choicevalue.getDisplayvalue();
			}
		}
		if (thiscolumn instanceof CTextField) {
			CTextField thistextfield = (CTextField) thiscolumn;
			if (thistextfield.isRichText()) {
				RichText richtext = new RichText(thiselement.defaultTextRepresentation());
				return richtext.generateHtmlString();

			}
		}
		if (thiselement instanceof DateDataElt) {
			DateDataElt dateelt = (DateDataElt) thiselement;
			if (dateelt.getPayload() != null)
				return CDateField.dateformat.format(dateelt.getPayload());
			specialtreatment = true;

		}
		if (thiselement instanceof DecimalDataElt) {

			DecimalDataElt decimalelt = (DecimalDataElt) thiselement;
			BigDecimal payload = decimalelt.getPayload();

			Format format = NumberFormat.getInstance(Locale.US);
			String locale = actionmanager.getClientSession().getLocale();
			if (locale != null)
				if (locale.length() > 0) {
					if ("FR".equals(locale))
						format = NumberFormat.getInstance(Locale.FRANCE);
				}
			// final replace all is to suppress space as thousand separator, which is not
			// understood
			// by spreadsheets.
			if (payload != null)
				return format.format(payload).replaceAll("\\u00A0", "");

			specialtreatment = true;
		}

		if (!specialtreatment) {
			logger.fine("copy to clipboard : default print for element " + thiselement.defaultTextRepresentation()
					+ " class " + thiselement.getClass());
			String content = RichText.escapetoHTML(thiselement.defaultTextRepresentation());
			return content;
		}
		return "";
	}

	/**
	 * copy all fields to the clipboard to paste into a spreadsheet
	 * 
	 * @param showhiddenfields if true, shows the hidden fields, if false, only
	 *                         shows the visible fields
	 */
	public void copyTableToClipboard(boolean showhiddenfields) {
		try {
			ObservableList<ObjectTableRow> tabledata = thistable.getItems();
			StringBuilder clipboardstring = new StringBuilder();
			clipboardstring.append("<table cellspacing=\"0\" >");
			clipboardstring.append("<tr>");
			boolean[] showcolumns = new boolean[payloadlist.size()];
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
			for (int i = 0; i < tabledata.size(); i++) {
				line++;
				ObjectTableRow thisrow = tabledata.get(i);
				ObjectDataElt objectdataelt = thisrow.getObject();
				clipboardstring.append("<tr>");
				for (int j = 0; j < payloadlist.size(); j++) {
					CBusinessField<?> thiscolumn = payloadlist.get(j);
					if (showcolumns[j]) {
						clipboardstring.append("<td>");
						SimpleDataElt thiselement = objectdataelt.lookupEltByName(thiscolumn.getFieldname());
						clipboardstring.append(printBusinessFieldToClipboard(actionmanager, thiscolumn, thiselement));
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
			logger.warning("Exception while copying to clipboard " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning("   " + e.getStackTrace()[i]);
			actionmanager.getClientSession().getActiveClientDisplay()
					.updateStatusBar("Error while copying data :  " + e.getMessage(), true);
		}
	}

	private static class ReadOnlyMouseHandler
			implements
			EventHandler<MouseEvent> {
		private boolean isregisteredaction;
		CObjectArray thisobjectarray;
		private PageActionManager actionmanager;

		public ReadOnlyMouseHandler(
				PageActionManager actionmanager,
				CObjectArray thisobjectarray,
				boolean isregisteredaction) {
			this.isregisteredaction = isregisteredaction;
			this.thisobjectarray = thisobjectarray;
			this.actionmanager = actionmanager;
		}

		@Override
		public void handle(MouseEvent mouseevent) {
			logger.warning("  ----> Mouse Event detected inside Object array ");
			if (mouseevent.getClickCount() == 2 && mouseevent.isShiftDown()) {

				thisobjectarray.copyTableToClipboard(false);
			}
			if (!mouseevent.isShiftDown())
				if (isregisteredaction)
					if (thisobjectarray.thistable.getSelectionModel().getSelectedItem() != null) {
						logger.severe(" --> launching handling of event");
						actionmanager.getMouseHandler().handle(mouseevent);
					} else {
						logger.severe(" --> handling of event discarded as no item selected");
					}

		}

		public void mothball() {
			thisobjectarray = null;

		}

	}

	/**
	 * a mouse handler to manage update on the table
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class UpdateMouseHandler
			implements
			EventHandler<MouseEvent> {
		private CObjectArray thisobjectarray;
		private EventHandler<? super MouseEvent> mouseeventlistener;

		/**
		 * creates an update mouse handler
		 * 
		 * @param thisobjectarray    object array widget
		 * @param mouseeventlistener mouse event listener
		 */
		public UpdateMouseHandler(CObjectArray thisobjectarray, EventHandler<? super MouseEvent> mouseeventlistener) {
			this.thisobjectarray = thisobjectarray;
			this.mouseeventlistener = mouseeventlistener;
		}

		@Override
		public void handle(MouseEvent event) {
			MouseButton button = event.getButton();

			if (button == MouseButton.PRIMARY) {
				if (event.getClickCount() == 1 && (event.isShiftDown())) {

					if (!thisobjectarray.updatemodeactive) { // currently, table in read mode, move to
						logger.fine("moving tableview " + thisobjectarray.name + " to update mode");
						thisobjectarray.thistable.setEditable(true);
						thisobjectarray.thistable.getSelectionModel().setCellSelectionEnabled(true);
						thisobjectarray.thistable.setBackground(thisobjectarray.editablebackground);
						thisobjectarray.updatemodeactive = true;
						thisobjectarray.startupdate.setDisable(true);
						thisobjectarray.commitupdate.setDisable(false);

					} else { // currently, table in update mode, update all changed rows and move back to
								// read-only

						thisobjectarray.launchupdate(null, event);

					}
				}
				if (event.getClickCount() == 1 && (!event.isShiftDown())) {
					if (thisobjectarray.thistable.getEditingCell() == null) {
						@SuppressWarnings("unchecked")
						TablePosition<
								ObjectTableRow,
								?> focusedCellPosition = thisobjectarray.thistable.getFocusModel().getFocusedCell();
						thisobjectarray.thistable.edit(focusedCellPosition.getRow(),
								focusedCellPosition.getTableColumn());

					}
				}
				// checking that something is actually selecting when double-clicking.
				if ((event.getClickCount() > 1) || (event.getClickCount() == 1 && event.isControlDown()))
					if (thisobjectarray.thistable.getSelectionModel().getSelectedItem() != null) {
						// trigger the action on double click only if updatemode is not active
						if (!thisobjectarray.updatemodeactive) {
							mouseeventlistener.handle(event);
						}
					}
			} else {

			}
		}

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

	private final static String COPY_DATA_EMPTY = "Copy Data (empty)";
	private TabPane[] parenttabpanes;

	private void generatenbLinesLabel() {
		long nbelements = this.thistable.getItems().size();
		String nbelementlabel = COPY_DATA_EMPTY;
		if (nbelements == 1)
			nbelementlabel = "Copy Data (1 line)";
		if (nbelements > 1)
			nbelementlabel = "Copy Data (" + nbelements + " lines)";
		this.copydata.setText(nbelementlabel);
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		this.parenttabpanes = parenttabpanes;
		modifycolumnmodel(inputdata);
		this.actionmanager = actionmanager;
		if ((action != null) || (inlineaction != null)) {
			this.readonlymousehandler = new ReadOnlyMouseHandler(actionmanager, this, true);
		} else {
			this.readonlymousehandler = new ReadOnlyMouseHandler(actionmanager, this, false);
		}
		this.updatemousehandler = new UpdateMouseHandler(this, readonlymousehandler);
		CObjectArrayColumnModel tablemodel = new CObjectArrayColumnModel(payloadlist, this.updateactionfields,
				(this.updateinlineaction != null ? this.updateinlineaction.key()
						: (this.updateaction != null ? this.updateaction.key() : null)),
				updatemousehandler);
		// generates table with title
		logger.fine("Generate model with forced row height = " + forcedrowheight);
		this.thistable = tablemodel.generateTableViewModel(actionmanager, this.forcedrowheight);

		// -------------------------------------------
		// ---- C O N T E X T . M E N U --------------
		// -------------------------------------------

		contextmenu = new ContextMenu();

		startupdate = new MenuItem("Start Update");
		commitupdate = new MenuItem("Store Update");
		copydata = new MenuItem("Copy Data");
		copytechdata = new MenuItem("Copy Data with Details");

		if (this.allowdataclear) {
			clearall = new MenuItem("Clear all");
			clearselected = new MenuItem("Clear selected");
		}

		contextmenu.getItems().add(startupdate);
		contextmenu.getItems().add(commitupdate);
		contextmenu.getItems().add(copydata);
		contextmenu.getItems().add(copytechdata);
		if (this.allowdataclear) {
			contextmenu.getItems().add(clearall);
			contextmenu.getItems().add(clearselected);
			if (this.updatemodeactive) {
				clearall.setDisable(true);
				clearselected.setDisable(true);
			} else {
				clearall.setDisable(false);
				clearselected.setDisable(false);

			}
		}
		if ((this.isinlineupdate) || (this.isupdate)) {
			if (this.updatemodeactive) {
				startupdate.setDisable(true);
				commitupdate.setDisable(false);
				thistable.setEditable(true);
				thistable.getSelectionModel().setCellSelectionEnabled(true);
				thistable.setBackground(editablebackground);

			} else {
				startupdate.setDisable(false);
				commitupdate.setDisable(true);

			}
		} else {
			startupdate.setDisable(true);
			commitupdate.setDisable(true);
		}
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
		if (clearall != null)
			clearall.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					thistable.getItems().clear();
					generatenbLinesLabel();

				}

			});

		if (clearselected != null)
			clearselected.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					thistable.getItems().removeAll(thistable.getSelectionModel().getSelectedItems());

					generatenbLinesLabel();

				}

			});
		startupdate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				thistable.setEditable(true);
				thistable.getSelectionModel().setCellSelectionEnabled(true);
				thistable.setBackground(editablebackground);
				updatemodeactive = true;
				startupdate.setDisable(true);
				commitupdate.setDisable(false);
				if (allowdataclear) {
					clearall.setDisable(true);
					clearselected.setDisable(true);
				}
				thistable.setTooltip(null);
				actionmanager.getClientSession().getActiveClientDisplay().updateStatusBar(
						"You have started editing a table, please do not forget to save before leaving the page (richt click on another cell + store update)",
						true);
			}
		});

		if (this.updateinlineaction != null)
			actionmanager.registerInlineAction(commitupdate, updateinlineaction);
		if (this.updateaction != null)
			actionmanager.registerEvent(commitupdate, updateaction);

		commitupdate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				launchupdate(arg0, null);
			}
		});

		thistable.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {
				if (contextmenu.isShowing()) {
					contextmenu.hide();
				} else {
					contextmenu.show(thistable, event.getScreenX(), event.getScreenY());
					thistable.setTooltip(null);

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
		this.thistable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (contextmenu.isShowing())
				contextmenu.hide();
		});
		// -------------------------------------------
		// ---- E N D . C O N T E X T . M E N U ------
		// -------------------------------------------

		this.thistable.setStyle("-fx-base: #ffffff;   ");

		if (this.allowmultipleselect) {
			thistable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		} else {
			thistable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		}
		normalbackground = thistable.getBackground();
		editablebackground = new Background(
				new BackgroundFill(new Color(93 / 255, 140 / 255, 174 / 255, 0.5), null, null));

		int linenumber = 0;
		if (this.datareference != null) {
			// gets all data
			ArrayDataElt<ObjectDataElt> data = getExternalContent(inputdata, datareference);

			ObservableList<ObjectTableRow> thistabledata = FXCollections.observableArrayList();

			for (int i = 0; i < data.getObjectNumber(); i++) {
				ObjectDataElt thisline = data.getObjectAtIndex(i);
				thistabledata
						.add(new ObjectTableRow(thisline, allobjectconstraints, this.thistable, this, actionmanager));
			}

			// puts data in table
			thistable.setItems(thistabledata);
			this.generatenbLinesLabel();
			linenumber = thistabledata.size();
		}
		if (this.inlineactiondataref != null) {

			inputdata.addInlineActionDataRef(this.inlineactiondataref);
		}

		if (this.inlineupdateactionoutputdataref != null) {
			inputdata.addInlineActionDataRef(this.inlineupdateactionoutputdataref);
		}

		int maxheight = linenumber * tablemodel.getFinalRowHeightInPixel() + 30;
		if (maxheight < 42)
			maxheight = 42; // for empty table display
		int preferedheight = maxheight;
		// max prefered height is 250 by default, but can be more if specified
		int maxpreferedheight = 250;
		if (this.rowstodisplay > 0)
			maxpreferedheight = rowstodisplay * tablemodel.getFinalRowHeightInPixel() + 30;
		if (preferedheight > maxpreferedheight) {
			preferedheight = maxpreferedheight;

		} else {
			logger.fine("----> Updating row height with parameter rows = " + rowstodisplay + ", row height = "
					+ tablemodel.getFinalRowHeightInPixel() + ", result = "
					+ (rowstodisplay * tablemodel.getFinalRowHeightInPixel() + 30));
		}

		thistable.setMinHeight(preferedheight);
		thistable.setPrefHeight(preferedheight);
		thistable.setMaxHeight(maxheight);

		// add action handler for double click on row and also copy paste
		if ((action != null) || (inlineaction != null)) {
			if (action != null)
				actionmanager.registerEvent(thistable, action);
			if (inlineaction != null)
				actionmanager.registerInlineAction(thistable, inlineaction);

		}

		this.parentwindow = parentwindow;
		DragResizer.makeResizable(thistable);

		// add update action

		if (this.updateinlineaction != null)
			actionmanager.registerInlineActionWithModifier(thistable, this.updateinlineaction,
					PageActionModifier.getShiftPressed());
		if (this.updateaction != null)
			actionmanager.registerEventWithModifier(thistable, this.updateaction, PageActionModifier.getShiftPressed());

		String tooltipstring = "Press Right click for actions\n";
		if ((this.isinlineupdate) || (this.isupdate)) {
			tooltipstring += "\nPress Shift + Left click to start or finish update";
		}
		tooltipstring += "\nPress Shift + Double click to copy data";
		Tooltip tabletooltip = new Tooltip(tooltipstring);
		thistable.setTooltip(tabletooltip);
		return thistable;
	}

	public void commitOngoingEdition() {
		TablePosition<ObjectTableRow, ?> editingcell = thistable.getEditingCell();
		if (editingcell != null) {
			updatemodeactive = false;
			thistable.setEditable(false);
			thistable.getSelectionModel().setCellSelectionEnabled(false);
			startupdate.setDisable(false);
			commitupdate.setDisable(true);
		}
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {

		commitOngoingEdition();

		if (type instanceof ObjectIdDataEltType) {
			if (objectfieldname == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			ObjectDataElt object = this.thistable.getSelectionModel().getSelectedItem().getObject();
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

		if (type instanceof TextDataEltType) {
			TextDataElt updatenote = new TextDataElt(eltname, this.updatenotecomment);
			logger.finer("got update note and put it in element " + eltname);
			return updatenote;
		}

		if (type instanceof IntegerDataEltType) {
			if (objectfieldname == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			ObjectDataElt object = this.thistable.getSelectionModel().getSelectedItem().getObject();
			SimpleDataElt field = object.lookupEltByName(objectfieldname);
			if (field == null)
				throw new RuntimeException(
						"field not found " + objectfieldname + ", available fields = " + object.dropFieldNames());
			if (!(field instanceof IntegerDataElt))
				throw new RuntimeException("field for name = " + objectfieldname + " is not Integer");
			IntegerDataElt integerfield = (IntegerDataElt) field;
			return integerfield;
		}
		if (type instanceof ArrayDataEltType) {
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>) type;
			DataEltType payloadtypeinarray = arraytype.getPayloadType();
			if (payloadtypeinarray instanceof ObjectDataEltType) {
				// --------------- try to get edited data

				ObjectDataEltType objecttype = (ObjectDataEltType) payloadtypeinarray;
				ObservableList<ObjectTableRow> tabledata = thistable.getItems();
				ArrayDataElt<ObjectDataElt> output = new ArrayDataElt<ObjectDataElt>(eltname, objecttype);
				int countrowsupdated = 0;
				for (int i = 0; i < tabledata.size(); i++) {
					ObjectTableRow thisrow = tabledata.get(i);

					if (thisrow.isRowUpdate()) {
						ObjectDataElt thisobject = thisrow.getObject();
						thisobject.changeName(eltname);
						output.addElement(thisobject);
						countrowsupdated++;
					}
				}
				// sends back updated data if it exists.
				if (output.getObjectNumber() > 0) {
					logger.info("Selected rows updated and sent back, number = " + countrowsupdated
							+ " for element name = " + eltname);
					return output;
				}

				ObservableList<ObjectTableRow> selecteditems = thistable.getSelectionModel().getSelectedItems();

				ArrayDataElt<ObjectDataElt> outputselected = new ArrayDataElt<ObjectDataElt>(eltname, objecttype);

				for (int i = 0; i < selecteditems.size(); i++) {
					ObjectTableRow thisrow = selecteditems.get(i);

					ObjectDataElt thisobject = thisrow.getObject();
					thisobject.changeName(eltname);
					outputselected.addElement(thisobject);
					countrowsupdated++;
				}
				logger.info("Selected rows updated and sent back, number = " + countrowsupdated + " for element name = "
						+ eltname);
				return outputselected;
			}

			if (payloadtypeinarray instanceof ObjectIdDataEltType) {
				Iterator<ObjectTableRow> selectedobjects = this.thistable.getSelectionModel().getSelectedItems()
						.iterator();
				ArrayDataElt<ObjectIdDataElt> output = new ArrayDataElt<ObjectIdDataElt>(eltname, payloadtypeinarray);
				while (selectedobjects.hasNext()) {
					ObjectDataElt thisobject = selectedobjects.next().getObject();
					SimpleDataElt field = thisobject.lookupEltByName(objectfieldname);
					if (field == null)
						throw new RuntimeException("field not found " + objectfieldname + ", available fields = "
								+ thisobject.dropFieldNames());
					if (!(field instanceof TextDataElt))
						throw new RuntimeException("field for name = " + objectfieldname + " is not text");
					TextDataElt textfield = (TextDataElt) field;
					ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
					output.addElement(objectid);
				}
				return output;
			}

		}
		// add here treatment of array element.

		throw new RuntimeException(String.format("Unsupported extraction type %s ", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		if (!(dataelt instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("inline page data does not have expected %s type, actually found %s",
							dataelt.getName(), dataelt.getType()));

		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) dataelt;
		HashMap<String, ObjectDataElt> existingrowsbyid = new HashMap<String, ObjectDataElt>();

		ObservableList<ObjectTableRow> thistabledata = thistable.getItems();

		for (int i = 0; i < thistabledata.size(); i++) {
			ObjectDataElt element = thistabledata.get(i).getObject();
			existingrowsbyid.put(element.getUID(), element);

		}
		for (int i = 0; i < thiselementarray.getObjectNumber(); i++) {
			ObjectDataElt thiselement = thiselementarray.getObjectAtIndex(i);
			if (existingrowsbyid.get(thiselement.getUID()) == null) {
				thistabledata.add(
						new ObjectTableRow(thiselement, allobjectconstraints, this.thistable, this, actionmanager));
			}
		}

		long minheight = thistabledata.size() * 25 + 40;
		if (minheight > 500)
			minheight = 500;
		thistable.setMinHeight(minheight);

		reviewDataWarningForTable();
		this.generatenbLinesLabel();
		for (int i = 0; i < this.parenttabpanes.length; i++)
			parenttabpanes[i].requestLayout();

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	public void mothball() {

		if (thistable != null) {
			if (thistable.getFocusModel() != null)
				thistable.getFocusModel().focus(null);

			if (this.readonlymousehandler != null) {
				thistable.removeEventHandler(MouseEvent.MOUSE_CLICKED, readonlymousehandler);
				readonlymousehandler.mothball();
			}
			if (this.updatemousehandler != null) {
				thistable.removeEventHandler(MouseEvent.MOUSE_CLICKED, updatemousehandler);
			}
			thistable.setOnMouseClicked(null);
			thistable.setOnKeyPressed(null);
			thistable.setOnKeyReleased(null);
			thistable.setSelectionModel(null);
			if (focuslostlistener != null)
				thistable.focusedProperty().removeListener(focuslostlistener);
			focuslostlistener = null;
			thistable.setOnContextMenuRequested(null);
			copydata.setOnAction(null);
			startupdate.setOnAction(null);
			commitupdate.setOnAction(null);
			this.actionmanager = null;
			ObservableList<ObjectTableRow> tablerows = thistable.getItems();
			Iterator<ObjectTableRow> rowiterator = tablerows.iterator();
			while (rowiterator.hasNext()) {
				ObjectTableRow thisrow = rowiterator.next();
				thisrow.mothball();
			}

			if (thistable.getItems() != null)
				if (thistable.getItems().size() > 0) {

					if (thistable.getSortOrder() != null)
						if (thistable.getSortOrder().size() > 0)
							thistable.getSortOrder().clear();
					thistable.getItems().clear();
				}
			if (thistable.getColumns() != null)
				if (thistable.getColumns().size() > 0) {

					thistable.getColumns().clear();
				}
			thistable = null;
			logger.finer("mothball succesfully done on table");
		} else {
			logger.info("mothball called on table with null tableview " + this.toString());
		}

	}

	/**
	 * When this method is triggered, it reviews the number of rows in the table
	 * that have been edited, and according to total data table, create or remove
	 * the data update warning
	 * 
	 * @throws GalliumException
	 */
	public void reviewDataWarningForTable() {
		if (this.unsavedupdatewarning) {
			logger.info("------------------- starting evaluating unsaved data -----------------------");
			ObservableList<ObjectTableRow> objecttablerow = thistable.getItems();
			Iterator<ObjectTableRow> rowiterator = objecttablerow.iterator();
			int updatedrow = 0;
			while (rowiterator.hasNext()) {
				ObjectTableRow thisrow = rowiterator.next();
				if (thisrow.isRowUpdate())
					updatedrow++;
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
