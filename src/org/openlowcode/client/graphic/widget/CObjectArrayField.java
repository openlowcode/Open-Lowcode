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

import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.client.runtime.PageActionModifier;
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
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Window;

/**
 * Creates a widget showing an array of objects by displaying each object as a
 * button showing one field of the object. The buttons are aligned horizontally
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectArrayField
		extends
		CPageNode {
	private static Logger logger = Logger.getLogger(CObjectArrayField.class.getCanonicalName());
	private String label;
	private String helper;
	private String name;
	private CPageAction defaultaction;
	private CPageAction deleteaction;
	private ArrayList<CBusinessField<?>> payloadlist;
	private ObjectDataElt selecteddataelt = null;
	private ArrayDataElt<ObjectDataElt> thiselementarray;
	private Pane datapane;
	private String fieldtoshow;
	private PageActionManager actionmanager;
	private CPageNode objectatendoffielddata;
	private CPageDataRef datareference;
	private Node dataatendoffielddata;
	@SuppressWarnings("unused")
	private BorderPane helperpane;
	private boolean inlinefeeding;
	@SuppressWarnings("unused")
	private CPageInlineAction feedinginlineaction;
	private CInlineActionDataRef feedinginlineactionoutputdata;
	private Tooltip tooltip;

	/**
	 * creates an object array field widget from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectArrayField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.label = reader.returnNextStringField("LBL");
		this.helper = reader.returnNextStringField("HPR");
		this.name = reader.returnNextStringField("NAM");
		String startstructure = reader.returnNextStartStructure();
		if (startstructure.compareTo("DFT") == 0) {
			reader.returnNextStartStructure("ACTION");
			this.defaultaction = new CPageAction(reader);
			reader.returnNextEndStructure("DFT");
			startstructure = reader.returnNextStartStructure();
		}
		if (startstructure.compareTo("DLT") == 0) {
			reader.returnNextStartStructure("ACTION");
			this.deleteaction = new CPageAction(reader);
			reader.returnNextEndStructure("DLT");
			startstructure = reader.returnNextStartStructure();
		}
		if (startstructure.compareTo("NAE") == 0) {
			objectatendoffielddata = CPageNode.parseNode(reader, parentpath);
			reader.returnNextEndStructure("NAE");
			startstructure = reader.returnNextStartStructure();

		}
		if (startstructure.compareTo("ATTRS") != 0)
			throw new RuntimeException("Expecting attributes list start structure, got " + startstructure);
		payloadlist = new ArrayList<CBusinessField<?>>();
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		fieldtoshow = reader.returnNextStringField("FTS");
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		this.inlinefeeding = reader.returnNextBooleanField("INF");
		if (this.inlinefeeding) {
			reader.returnNextStartStructure("INLACT");
			reader.returnNextStartStructure("INLINEACTION");
			this.feedinginlineaction = new CPageInlineAction(reader);
			this.feedinginlineactionoutputdata = new CInlineActionDataRef(reader, this);
			reader.returnNextEndStructure("INLACT");
		}
		reader.returnNextEndStructure("OBJARF");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

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

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		logger.fine("built node CObjectArrayField " + this.name);
		this.actionmanager = actionmanager;
		if (this.inlinefeeding) {
			inputdata.addInlineActionDataRef(this.feedinginlineactionoutputdata);

		}
		this.tooltip = new Tooltip("click to open object\nShift+click to unlink object.");
		HBox thispane = new HBox();

		if (label != null)
			if (label.length() > 0) {
				Label thislabel = new Label(label);
				if (helper != null)
					if (helper.length() > 0)
						thislabel.setTooltip(new Tooltip(helper));
				thislabel.setFont(
						Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
				thislabel.setMinWidth(120);
				thislabel.setWrapText(true);
				thislabel.setMaxWidth(120);
				thispane.getChildren().add(thislabel);
			}

		datapane = new FlowPane();
		boolean nolabel = true;
		if (label != null)
			if (label.length() > 0) {
				FlowPane flowdatapane = new FlowPane();
				thispane.setAlignment(Pos.TOP_LEFT);
				flowdatapane.setPrefWrapLength(400);
				// for object workflow tasks.
				flowdatapane.setVgap(4);
				flowdatapane.setHgap(8);
				this.datapane = flowdatapane;
				nolabel = false;
			}

		if (nolabel) {
			HBox boxdatapane = new HBox();
			thispane.setAlignment(Pos.CENTER_LEFT);
			boxdatapane.setAlignment(Pos.CENTER_LEFT);
			this.datapane = boxdatapane;
		}

		// this is to keep the widget tiny when used as right of title

		thiselementarray = getExternalContent(inputdata, datareference);
		if (objectatendoffielddata != null)
			dataatendoffielddata = objectatendoffielddata.getNode(actionmanager, inputdata, parentwindow,
					parenttabpanes);
		refreshDisplay();

		thispane.getChildren().add(datapane);

		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (type instanceof ObjectIdDataEltType) {
			if (objectfieldname == null)
				throw new RuntimeException("objectid field should have an objectfieldname");
			if (selecteddataelt == null)
				throw new RuntimeException(
						"no selected data element for widget " + this.name + " at path " + this.getSignificantpath());
			ObjectDataElt object = selecteddataelt;
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
		if (type instanceof ArrayDataEltType) {
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>) type;
			DataEltType payloadtypeinarray = arraytype.getPayloadType();
			if (payloadtypeinarray instanceof ObjectDataEltType) {
				ObjectDataEltType objecttype = (ObjectDataEltType) payloadtypeinarray;
				ArrayDataElt<ObjectDataElt> output = new ArrayDataElt<ObjectDataElt>(eltname, objecttype);
				for (int i = 0; i < thiselementarray.getObjectNumber(); i++) {
					// fields are not sent back
					ObjectDataElt thisobject = thiselementarray.getObjectAtIndex(i);
					ObjectDataElt object = new ObjectDataElt(eltname);
					object.setUID(thisobject.getUID());
					output.addElement(object);
				}
				return output;
			}
		}
		throw new RuntimeException(String.format("Unsupported extraction type %s ", type));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forceUpdateData(DataElt dataelt) {
		boolean treated = false;
		if (dataelt instanceof ArrayDataElt) {
			thiselementarray = (ArrayDataElt<ObjectDataElt>) dataelt;
			refreshDisplay();
			treated = true;
		}
		if (dataelt instanceof ObjectDataElt) {
			ObjectDataElt element = (ObjectDataElt) dataelt;
			thiselementarray.addElement(element.deepcopy(thiselementarray.getName()));
			refreshDisplay();
			treated = true;
		}
		if (!treated)
			throw new RuntimeException(
					String.format("inline page data %s does not have supported type, actually found %s",
							dataelt.getName(), dataelt.getType()));

	}

	private class ObjectArrayFieldMouseEvent
			implements
			EventHandler<MouseEvent> {
		private int index;

		public ObjectArrayFieldMouseEvent(int index) {
			this.index = index;
		}

		@Override
		public void handle(MouseEvent mouseevent) {
			selecteddataelt = thiselementarray.getObjectAtIndex(index);
			actionmanager.getMouseHandler().handle(mouseevent);
		}

	}

	private void refreshDisplay() {
		logger.fine("Refresh CObjectArrayField " + this.name);

		datapane.getChildren().clear();
		CBusinessField<?> selectedfield = null;
		StringBuffer allfields = new StringBuffer("[");
		for (int i = 0; i < this.payloadlist.size(); i++) {
			if (i > 0)
				allfields.append(',');
			CBusinessField<?> thisfielddefinition = this.payloadlist.get(i);
			allfields.append(thisfielddefinition.getFieldname());
			if (thisfielddefinition.getFieldname().compareTo(fieldtoshow) == 0) {
				selectedfield = thisfielddefinition;
			}
		}
		allfields.append("]");
		if (selectedfield == null)
			throw new RuntimeException("no title field on object, fieldtoshow = '" + fieldtoshow + "', list of field = "
					+ allfields.toString());
		for (int i = 0; i < thiselementarray.getObjectNumber(); i++) {

			ObjectDataElt thisobject = thiselementarray.getObjectAtIndex(i);
			String buttontitle = null;
			// analysis all fields

			for (int j = 0; j < this.payloadlist.size(); j++) {
				CBusinessField<?> thisfielddefinition = this.payloadlist.get(j);
				if (thisfielddefinition.getFieldname().compareTo(fieldtoshow) == 0) {
					SimpleDataElt fieldcontent = thisobject.lookupEltByName(thisfielddefinition.getFieldname());
					buttontitle = fieldcontent.defaultTextRepresentation();
				}
			}

			if (buttontitle == null)
				throw new RuntimeException("no button title found on object");
			Button objectbutton = new Button(buttontitle);
			objectbutton.setTooltip(this.tooltip);
			objectbutton.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");

			if (this.defaultaction != null) {
				actionmanager.registerEventWithModifier(objectbutton, defaultaction,
						PageActionModifier.getNothingPressed());
				actionmanager.registerEventWithModifier(objectbutton, defaultaction,
					PageActionModifier.getCtrlPressed());
			}
			if (this.deleteaction != null)
				actionmanager.registerEventWithModifier(objectbutton, deleteaction,
						PageActionModifier.getShiftPressed());

			objectbutton.setOnMousePressed(new ObjectArrayFieldMouseEvent(i));

			logger.fine("Add objectbutton " + buttontitle);
			datapane.getChildren().add(objectbutton);

		}
		if (dataatendoffielddata != null)
			datapane.getChildren().add(dataatendoffielddata);

	}

	@Override
	public void mothball() {

	}

}
