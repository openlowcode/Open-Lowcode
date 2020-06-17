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
import java.util.Iterator;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.CActionButton.ButtonHandler;
import org.openlowcode.client.runtime.PageActionManager;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.scene.transform.Transform;

/**
 * Creates a field searcher widget allowing to enter a string and search for objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CFieldSearcher
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String name;
	private String addlabel;
	private String closelabel;
	@SuppressWarnings("unused")
	private String rollovertip;
	private CPageInlineAction searchaction;
	private CPageAction actionaftersearch;
	private ArrayList<CBusinessField<?>> payloadlist;
	private String fieldtoshow;
	@SuppressWarnings("unused")
	private PageActionManager actionmanager;
	private BorderPane mainpane;
	private TextField searchfield;
	private Button closebutton;
	private CInlineActionDataRef inlineactiondataref;
	private Popup resultpopup;
	private ListView<String> resultlist;
	private ArrayDataElt<ObjectDataElt> data;
	private boolean hasbottomaction;
	private CPageAction bottomaction;
	private boolean showonlyifempty;
	private String bottomactionlabel;
	private VBox resultbox;
	private Button bottombutton;
	private boolean hasaction;
	private CPageInlineAction inlineenirichaction;
	private Tooltip searchtooltip;

	/**
	 * creates the CFieldSearcher from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CFieldSearcher(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAM");
		this.addlabel = reader.returnNextStringField("ADL");
		this.closelabel = reader.returnNextStringField("CSL");
		this.rollovertip = reader.returnNextStringField("RLT");
		reader.returnNextStartStructure("SRA");
		reader.returnNextStartStructure("INLINEACTION");
		this.searchaction = new CPageInlineAction(reader);
		reader.returnNextEndStructure("SRA");
		this.hasaction = reader.returnNextBooleanField("ATP");
		if (hasaction) {
			reader.returnNextStartStructure("ACT");
			reader.returnNextStartStructure("ACTION");
			this.actionaftersearch = new CPageAction(reader);

			reader.returnNextEndStructure("ACT");
		} else {
			reader.returnNextStartStructure("INLACT");
			reader.returnNextStartStructure("INLINEACTION");
			this.inlineenirichaction = new CPageInlineAction(reader);
			reader.returnNextEndStructure("INLACT");
		}
		payloadlist = new ArrayList<CBusinessField<?>>();
		reader.returnNextStartStructure("ATTRS");
		while (reader.structureArrayHasNextElement("ATTR")) {
			CBusinessField<?> thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		fieldtoshow = reader.returnNextStringField("FTS");
		this.inlineactiondataref = new CInlineActionDataRef(reader, this);
		this.hasbottomaction = reader.returnNextBooleanField("HAB");
		if (this.hasbottomaction) {
			reader.returnNextStartStructure("ACTION");
			this.bottomaction = new CPageAction(reader);
			this.bottomactionlabel = reader.returnNextStringField("BAL");
			this.showonlyifempty = reader.returnNextBooleanField("SOE");
		}
		reader.returnNextEndStructure("FLDSRC");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		inputdata.addInlineActionDataRef(this.inlineactiondataref);
		this.searchtooltip = new Tooltip("Enter a few letters or complete name\nand press Enter or Control to search.");

		this.actionmanager = actionmanager;

		this.mainpane = new BorderPane();

		closebutton = new Button(this.closelabel);
		closebutton.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		searchfield = new TextField();
		searchfield.setPromptText(this.addlabel);
		searchfield.setTooltip(searchtooltip);
		searchfield.setMaxWidth(45);
		searchfield.focusedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean oldvalue, Boolean newvalue) {
				if (newvalue.booleanValue()) {
					// get focus
					searchfield.setMaxWidth(200);

				} else {
					searchfield.setMaxWidth(45);

					searchfield.clear();
					resultpopup.hide();
					if (bottombutton != null)
						resultbox.getChildren().remove(bottombutton);
				}

			}

		});

		searchfield.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent keyevent) {
				if (keyevent.getCode() == KeyCode.CONTROL) {
					searchfield.fireEvent(new ActionEvent());
				}

			}

		});

		actionmanager.registerInlineAction(searchfield, searchaction);

		searchfield.addEventHandler(ActionEvent.ACTION, actionmanager);

		this.mainpane.setCenter(searchfield);

		resultpopup = new Popup();
		resultbox = new VBox();

		resultlist = new ListView<String>();
		resultlist.setMaxHeight(120);

		resultbox.getChildren().add(resultlist);
		resultpopup.getContent().add(resultbox);
		if (this.hasbottomaction) {
			bottombutton = new Button(this.bottomactionlabel);
			bottombutton.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
			actionmanager.registerEvent(bottombutton, this.bottomaction);
			ButtonHandler buttonhandler = new ButtonHandler(actionmanager);
			bottombutton.setOnMouseClicked(buttonhandler);
		}
		if (inlineenirichaction != null)
			actionmanager.registerInlineAction(resultlist, inlineenirichaction);
		if (actionaftersearch != null)
			actionmanager.registerEvent(resultlist, actionaftersearch);
		resultlist.addEventHandler(ActionEvent.ACTION, actionmanager);
		resultlist.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {

				resultlist.fireEvent(new ActionEvent());
				resultpopup.hide();
			}
		});

		searchfield.localToSceneTransformProperty().addListener(new ChangeListener<Transform>() {

			@Override
			public void changed(ObservableValue<? extends Transform> arg0, Transform arg1, Transform arg2) {
				if (resultpopup.isShowing()) {
					resultpopup.hide();
					resultbox.getChildren().remove(bottombutton);
				}

			}

		});

		return mainpane;

	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof TextDataEltType) {
			return new TextDataElt(eltname, searchfield.getText());
		}
		if (type instanceof ObjectIdDataEltType) {
			int index = resultlist.getSelectionModel().getSelectedIndex();
			ObjectDataElt object = data.getObjectAtIndex(index);
			searchfield.setText("");

			return new ObjectIdDataElt(eltname, object.lookupEltByName("ID").defaultTextRepresentation());

		}
		if (type instanceof ObjectDataEltType) {
			int index = resultlist.getSelectionModel().getSelectedIndex();
			ObjectDataElt object = data.getObjectAtIndex(index);
			searchfield.setText("");

			return object.deepcopy(eltname);
		}

		if (type instanceof ArrayDataEltType) {
			searchfield.setText("");
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>) type;
			if (arraytype.getPayloadType() instanceof ObjectIdDataEltType) {
				Iterator<Integer> selectedindices = resultlist.getSelectionModel().getSelectedIndices().iterator();
				ArrayDataElt<
						ObjectIdDataElt> answer = new ArrayDataElt<ObjectIdDataElt>(eltname, new ObjectIdDataEltType());
				while (selectedindices.hasNext()) {
					answer.addElement(new ObjectIdDataElt(eltname, data.getObjectAtIndex(selectedindices.next())
							.lookupEltByName("ID").defaultTextRepresentation()));
				}
				return answer;
			}
		}
		throw new RuntimeException("data request not supported, eltname = " + eltname + ", type = " + type.printType());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void forceUpdateData(DataElt dataelt) {

		if (!(dataelt instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("inline page data does not have expected %s type, actually found %s",
							dataelt.getName(), dataelt.getType()));
		data = (ArrayDataElt<ObjectDataElt>) dataelt;
		ArrayList<String> itemstoshow = new ArrayList<String>();
		for (int i = 0; i < data.getObjectNumber(); i++) {
			ObjectDataElt thisobject = data.getObjectAtIndex(i);
			SimpleDataElt fieldtoshowobject = thisobject.lookupEltByName(fieldtoshow);
			if (fieldtoshowobject == null)
				throw new RuntimeException("field not found in object " + fieldtoshow + ", all object fields = "
						+ thisobject.dropFieldNames());
			itemstoshow.add(fieldtoshowobject.defaultTextRepresentation());
		}
		ObservableList<String> itemstoshowlist = FXCollections.observableList(itemstoshow);
		resultlist.setItems(itemstoshowlist);
		double wx = searchfield.getScene().getX() + searchfield.getScene().getWindow().getX();
		double wy = searchfield.getScene().getY() + searchfield.getHeight() + searchfield.getScene().getWindow().getY();
		Point2D point = searchfield.localToScene(0, 0);
		if (this.hasbottomaction) {
			boolean showbottomaction = false;
			if (this.showonlyifempty)
				if (itemstoshow.size() == 0)
					showbottomaction = true;
			if (!this.showonlyifempty)
				showbottomaction = true;
			if (showbottomaction)
				resultbox.getChildren().add(bottombutton);
		}

		resultpopup.show(searchfield, wx + point.getX(), wy + point.getY());

	}

	@Override
	public void mothball() {

	}

}
