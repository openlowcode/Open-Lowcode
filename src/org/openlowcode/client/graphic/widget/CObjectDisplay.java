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

import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Window;
import javafx.scene.control.TitledPane;

/**
 * A widget showing all the fields of a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectDisplay
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String externaldatareference;
	private String name;
	private String label;
	private boolean readonly;
	private boolean hidereadonly;
	private CPageAction action;
	private String uid;

	private ArrayList<CBusinessField<?>> payloadlist;
	private HashMap<String, CBusinessField<?>> payloadlistbyname;
	private ArrayList<CBusinessField<?>> morepayloadlist;
	private HashMap<String, CBusinessField<?>> morepayloadlistbyname;
	private CPageDataRef datareference;
	private boolean hasnoderightoftitle;
	private CPageNode nodeelementrightoftitle;
	private ArrayList<CMultiFieldConstraint> allobjectconstraints;
	private boolean showtitle;
	private boolean showcontent;
	private HashMap<String, CPageDataRef> overridenlabels;
	private HashMap<String, CPageDataRef> suggestionsforstringfield;
	private TitledPane morepane;
	private boolean hasbuttonbar;
	private CPageNode buttonbar;

	/**
	 * creates an object display from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	@SuppressWarnings("rawtypes")
	public CObjectDisplay(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		this.label = reader.returnNextStringField("LABEL");
		this.showtitle = reader.returnNextBooleanField("SOT");
		this.showcontent = reader.returnNextBooleanField("SOC");
		// ------------------------------------------------------------------

		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;
			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				this.action = new CPageAction(reader);
				this.readonly = reader.returnNextBooleanField("RO");
			} else {
				throw new RuntimeException("only 'ACTION' structure available at this point, got "
						+ actiontag.getStructurename() + " at path " + reader.getCurrentElementPath());
			}
		} else { // CASE THERE IS NO ACTION
			if (element instanceof MessageBooleanField) {
				MessageBooleanField booleantag = (MessageBooleanField) element;
				if (((MessageBooleanField) element).getFieldName().compareTo("RO") == 0) {
					this.readonly = booleantag.getFieldContent();
				} else {
					throw new RuntimeException("expected a boolean 'RO' tag, got " + element.toString() + " at path "
							+ reader.getCurrentElementPath());
				}

			} else {
				throw new RuntimeException("expected a boolean 'RO' tag, got " + element.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
		}
		// ------------------------------------------------------------------
		this.hidereadonly = reader.returnNextBooleanField("HR");
		payloadlist = new ArrayList<CBusinessField<?>>();
		payloadlistbyname = new HashMap<String, CBusinessField<?>>();
		morepayloadlist = new ArrayList<CBusinessField<?>>();
		morepayloadlistbyname = new HashMap<String, CBusinessField<?>>();
		suggestionsforstringfield = new HashMap<String, CPageDataRef>();
		reader.startStructureArray("ATTR");
		while (reader.structureArrayHasNextElement("ATTR")) {
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);
			payloadlist.add(thisfield);
			payloadlistbyname.put(thisfield.getFieldname(), thisfield);
			reader.returnNextEndStructure("ATTR");
		}

		reader.startStructureArray("MOREATTR");
		while (reader.structureArrayHasNextElement("MOREATTR")) {
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			morepayloadlist.add(thisfield);
			morepayloadlistbyname.put(thisfield.getFieldname(), thisfield);
			reader.returnNextEndStructure("MOREATTR");
		}

		allobjectconstraints = new ArrayList<CMultiFieldConstraint>();
		if (!this.readonly) {
			reader.startStructureArray("CTR");
			while (reader.structureArrayHasNextElement("CTR")) {
				CMultiFieldConstraint constraint = new CMultiFieldConstraint(reader, payloadlist);
				allobjectconstraints.add(constraint);
			}
		}
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		this.hasnoderightoftitle = reader.returnNextBooleanField("HNRT");
		if (this.hasnoderightoftitle) {
			reader.returnNextStartStructure("NRT");
			nodeelementrightoftitle = CPageNode.parseNode(reader, parentpath);
			reader.returnNextEndStructure("NRT");

		}
		this.hasbuttonbar = reader.returnNextBooleanField("HBTBAR");
		if (this.hasbuttonbar) {
			reader.returnNextStartStructure("BBB");
			this.buttonbar = CPageNode.parseNode(reader, parentpath);
			reader.returnNextEndStructure("BBB");
			
		}
		overridenlabels = new HashMap<String, CPageDataRef>();
		reader.startStructureArray("OVWLBL");
		while (reader.structureArrayHasNextElement("OVWLBL")) {
			String fieldtooverride = reader.returnNextStringField("FLD");
			CPageDataRef dataref = CPageDataRef.parseCPageDataRef(reader);
			overridenlabels.put(fieldtooverride, dataref);
			reader.returnNextEndStructure("OVWLBL");
		}
		reader.startStructureArray("TXTSUG");
		while (reader.structureArrayHasNextElement("TXTSUG")) {
			String fieldwithsuggestions = reader.returnNextStringField("FLD");
			CPageDataRef suggestionsdataref = CPageDataRef.parseCPageDataRef(reader);
			suggestionsforstringfield.put(fieldwithsuggestions, suggestionsdataref);
			reader.returnNextEndStructure("TXTSUG");
		}

		reader.returnNextEndStructure("OBJDIS");
	}

	/**
	 * extracts the object data from page data
	 * 
	 * @param inputdata all the page data
	 * @param dataref   reference to the data element holding the object data
	 * @return object data
	 */
	public ObjectDataElt getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		ObjectDataElt thisobjectelement = (ObjectDataElt) thiselement;
		return thisobjectelement;
	}

	/**
	 * generate the javafx node holding an object data element
	 * 
	 * @param objectdataelement object data
	 * @param path              path
	 * @param payloadlist       list of fields
	 * @param hidereadonly      if true, hide read-only fields
	 * @param readonly          if true, object is read-only, if false, node is
	 *                          read-write
	 * @param actionmanager     action manager
	 * @param action            action to trigger
	 * @param label             plain label
	 * @param inputdata         page data
	 * @param parentwindow      parent window
	 * @param parenttabpanes    parent panes if relevant
	 * @return the javafx node
	 */
	public static Node generateObjectDisplay(
			ObjectDataElt objectdataelement,
			CPageSignifPath path,
			ArrayList<CBusinessField<?>> payloadlist,
			boolean hidereadonly,
			boolean readonly,
			PageActionManager actionmanager,
			CPageAction action,
			String label,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		CComponentBand fieldtable = new CComponentBand(CComponentBand.DIRECTION_DOWN, path);

		// *************** put content ***************

		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> currentfield = payloadlist.get(i);
			currentfield.setContent(objectdataelement); // this is necessary as it may still be needed to pick the value
														// for an action
			if ((!currentfield.showinbottomnotes) && (!currentfield.showintitle)) {
				currentfield.setActive(!readonly);
				if (action != null) {
					// for all active fields, add an action
					currentfield.ForceAction(actionmanager, action);
				}
				boolean show = true;

				if ((!currentfield.isEditable()) && (hidereadonly))
					show = false;
				if (show)
					fieldtable.addNode(currentfield);
			}
		}

		// generates bottom text
		String bottomtext = "";
		boolean first = true;
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> currentfield = payloadlist.get(i);
			if ((currentfield.showinbottomnotes) && (!hidereadonly)) {
				if (!first)
					bottomtext = bottomtext + "\n";
				first = false;
				bottomtext = bottomtext + currentfield.getLabel() + ":";
				bottomtext = bottomtext
						+ objectdataelement.lookupEltByName(currentfield.getFieldname()).defaultTextRepresentation();
			}
		}

		HBox totalpane = new HBox(1);
		totalpane.setPadding(new Insets(1, 1, 1, 0));
		totalpane.setAlignment(Pos.TOP_LEFT);

		Pane contentpane = new VBox(8);
		contentpane.setPadding(new Insets(2, 2, 2, 0));

		first = true;
		String titletext = "";
		if (label != null) {
			titletext = label;
		}
		for (int i = 0; i < payloadlist.size(); i++) {
			@SuppressWarnings("rawtypes")
			CBusinessField currentfield = payloadlist.get(i);
			if (currentfield.showintitle) {
				if (!first)
					titletext = titletext + ",";
				if (first)
					titletext = titletext + " ";

				first = false;

				titletext = titletext + currentfield.getLabel() + ": "
						+ objectdataelement.lookupEltByName(currentfield.getFieldname()).defaultTextRepresentation();
			}

		}

		if ((titletext.length() > 0) && (!hidereadonly)) {

			Label titlelabel = new Label(titletext);
			if (bottomtext.length() > 0) {
				Tooltip bottomtexttooltip = new Tooltip(bottomtext);
				bottomtexttooltip.setFont(Font.font(bottomtexttooltip.getFont().getName(), FontPosture.ITALIC,
						bottomtexttooltip.getFont().getSize() * 0.9));
				titlelabel.setTooltip(bottomtexttooltip);

			}
			titlelabel.setFont(
					Font.font(titlelabel.getFont().getName(), FontWeight.BOLD, titlelabel.getFont().getSize() * 1.2));
			DropShadow ds = new DropShadow();
			ds.setRadius(1.);
			ds.setOffsetX(1.);
			ds.setOffsetY(1.);
			ds.setColor(Color.color(0.8, 0.8, 0.8));
			titlelabel.setEffect(ds);
			titlelabel.setTextFill(Color.web("#17184B"));
			titlelabel.setPadding(new Insets(5, 5, 10, 50));

			contentpane.getChildren().add(titlelabel);
		}

		contentpane.getChildren().add(fieldtable.getNode(actionmanager, inputdata, parentwindow, parenttabpanes));

		totalpane.getChildren().add(contentpane);
		HBox.setHgrow(contentpane, Priority.ALWAYS);
		if (bottomtext.length() > 0) {
			Label more = new Label("?");
			String bottomtextprint = bottomtext + "\n(rightclick on ? to copy)";
			Tooltip bottomtexttooltip = new Tooltip(bottomtextprint);
			bottomtexttooltip.setFont(Font.font(bottomtexttooltip.getFont().getName(), FontPosture.ITALIC,
					bottomtexttooltip.getFont().getSize() * 0.9));
			bottomtexttooltip.setWrapText(true);
			more.setTooltip(bottomtexttooltip);
			final String bottomtextfinal = bottomtext;
			more.setOnMouseClicked(new EventHandler<MouseEvent>() {
				public void handle(MouseEvent me) {

					if (me.getButton() == MouseButton.SECONDARY) {

						Clipboard clipboard = Clipboard.getSystemClipboard();
						final ClipboardContent content = new ClipboardContent();
						content.putString(bottomtextfinal);

						clipboard.setContent(content);
					}
				}
			});
			totalpane.getChildren().add(more);

		}

		return totalpane;

	}

	private void modifycolumnmodelandaddsuggestion(CPageData inputdata) {
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> thisfield = payloadlist.get(i);
			CPageDataRef overrides = overridenlabels.get(thisfield.getFieldname());
			if (overrides != null) {
				TextDataElt thiselement = (TextDataElt) inputdata.lookupDataElementByName(overrides.getName());
				if (thiselement == null)
					throw new RuntimeException("could not find a page data called " + thisfield.getFieldname());
				thisfield.overridesLabel(thiselement.getPayload());
			}
			CPageDataRef suggestiondata = this.suggestionsforstringfield.get(thisfield.getFieldname());
			if (suggestiondata != null) {
				if (thisfield instanceof CTextField) {
					CTextField thistextfield = (CTextField) thisfield;
					thistextfield.addSuggestions(suggestiondata);
				} else throw new RuntimeException("Suggestion only valid for TextField, and "+thisfield.getFieldname()+" ");
				
			}

		}
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {

		modifycolumnmodelandaddsuggestion(inputdata);
		// *************** get reference data ********

		ObjectDataElt objectdata = getExternalContent(inputdata, datareference);
		boolean hasmorefields = false;
		// *************** set-up structure **********
		this.uid = objectdata.getUID();
		CComponentBand fieldtable = new CComponentBand(CComponentBand.DIRECTION_DOWN, this.nodepath);
		fieldtable.setMinWidth(700);
		CComponentBand hiddenfieldtable = new CComponentBand(CComponentBand.DIRECTION_DOWN, this.nodepath);
		hiddenfieldtable.setMinWidth(700);

		// **************** add suggestions **********
		if (this.showcontent)
		for (int i=0;i<payloadlist.size();i++) {
			CBusinessField<?> currentfield = payloadlist.get(i);
			CPageDataRef suggestiondata = this.suggestionsforstringfield.get(currentfield.getFieldname());
			if (suggestiondata!=null) {
				if (currentfield instanceof CTextField) {
					CTextField textfield = (CTextField) currentfield;
					textfield.addSuggestions(suggestiondata);
					logger.severe("Adding suggestions for field "+currentfield.getFieldname()+" sugestiondata = "+suggestiondata);
				} else  {
					logger.severe("Received request for suggestion for field "+currentfield.getFieldname()+" although it is not CTextField but "+currentfield.getClass());
				}
			}
		}
		
		// *************** put content ***************
		if (this.showcontent)
			for (int i = 0; i < payloadlist.size(); i++) {
				CBusinessField<?> currentfield = payloadlist.get(i);
				currentfield.setContent(objectdata); // this is necessary as it may still be needed to pick the value
														// for an action
				if (!currentfield.showinbottomnotes) {
					currentfield.setActive(!this.readonly);

					if (this.action != null) {
						// for all active fields, add an action
						currentfield.ForceAction(actionmanager, action);
					}
					boolean show = true;

					if ((!currentfield.isEditable()) && (this.hidereadonly))
						show = false;
					if (show) {
						fieldtable.addNode(currentfield);
					}
				}
			}
		if (this.showcontent)
			for (int i = 0; i < morepayloadlist.size(); i++) {
				CBusinessField<?> currentfield = morepayloadlist.get(i);
				currentfield.setContent(objectdata); // this is necessary as it may still be needed to pick the value
														// for an action
				if (!currentfield.showinbottomnotes) {
					currentfield.setActive(!this.readonly);

					if (this.action != null) {
						// for all active fields, add an action
						currentfield.ForceAction(actionmanager, action);
					}
					boolean show = true;

					if ((!currentfield.isEditable()) && (this.hidereadonly))
						show = false;
					if (show) {
						hasmorefields = true;
						hiddenfieldtable.addNode(currentfield);
					}
				}
			}

		// generates bottom text
		String bottomtext = "";
		boolean first = true;
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> currentfield = payloadlist.get(i);
			if ((currentfield.showinbottomnotes) && (!this.hidereadonly)) {
				if (!first)
					bottomtext = bottomtext + "\n";
				first = false;
				bottomtext = bottomtext + currentfield.getLabel() + ":";
				bottomtext = bottomtext
						+ objectdata.lookupEltByName(currentfield.getFieldname()).defaultTextRepresentation();
			}
		}
		first = true;
		// generates title text
		String titletext = this.label;
		for (int i = 0; i < payloadlist.size(); i++) {
			CBusinessField<?> currentfield = payloadlist.get(i);
			if (currentfield.showintitle) {
				if (!first)
					titletext = titletext + ",  ";
				if (first)
					titletext = titletext + " - ";

				first = false;
				if (currentfield instanceof CChoiceField) {
					CChoiceField thischoicefield = (CChoiceField) currentfield;
					String code = objectdata.lookupEltByName(currentfield.getFieldname()).defaultTextRepresentation();
					if (code.length() > 0) {
						CChoiceFieldValue choicevalue = thischoicefield.getChoiceFieldValue(code);
						String choiceprint = "Invalid value "+code;
						if (choicevalue!=null) choiceprint = choicevalue.getDisplayvalue();
						titletext = titletext + "   " + choiceprint;
					}
				} else {
					titletext = titletext
							+ objectdata.lookupEltByName(currentfield.getFieldname()).defaultTextRepresentation();
				}
			}
		}
		HBox totalpane = new HBox(1);
		totalpane.setPadding(new Insets(1, 1, 1, 0));
		totalpane.setAlignment(Pos.TOP_LEFT);
		Pane contentpane = new VBox(8);
		contentpane.setPadding(new Insets(2, 2, 2, 0));
		if ((titletext.length() > 0) && (!this.hidereadonly))
			if (this.showtitle) {
				Label titlelabel = new Label(titletext);

				titlelabel.setFont(Font.font(titlelabel.getFont().getName(), FontWeight.BOLD,
						titlelabel.getFont().getSize() * 1.2));
				DropShadow ds = new DropShadow();
				ds.setRadius(1.);
				ds.setOffsetX(1.);
				ds.setOffsetY(1.);
				ds.setColor(Color.color(0.8, 0.8, 0.8));
				titlelabel.setEffect(ds);
				titlelabel.setTextFill(Color.web("#17184B"));
				titlelabel.setPadding(new Insets(7, 5, 7, 50));
				HBox titleextendedpane = new HBox(1);
				titleextendedpane.alignmentProperty().set(Pos.CENTER_LEFT);
				titleextendedpane.getChildren().add(titlelabel);

				if (this.hasnoderightoftitle) {
					titleextendedpane.getChildren().add(this.nodeelementrightoftitle.getNode(actionmanager, inputdata,
							parentwindow, parenttabpanes));

				}

				if (bottomtext.length() > 0)
					if (this.showtitle) {
						Label more = new Label(" ?");
						String bottomtextprint = bottomtext + "\n(rightclick on ? to copy)";
						Tooltip bottomtexttooltip = new Tooltip(bottomtextprint);
						bottomtexttooltip.setFont(Font.font(bottomtexttooltip.getFont().getName(), FontPosture.ITALIC,
								bottomtexttooltip.getFont().getSize() * 0.9));
						bottomtexttooltip.setWrapText(true);
						more.setTooltip(bottomtexttooltip);
						final String bottomtextfinal = bottomtext;
						more.setOnMouseClicked(new EventHandler<MouseEvent>() {
							public void handle(MouseEvent me) {

								if (me.getButton() == MouseButton.SECONDARY) {

									Clipboard clipboard = Clipboard.getSystemClipboard();
									final ClipboardContent content = new ClipboardContent();
									content.putString(bottomtextfinal);

									clipboard.setContent(content);
								}
							}
						});
						titleextendedpane.getChildren().add(more);

					}

				contentpane.getChildren().add(titleextendedpane);
			}
		if (this.hasbuttonbar) {
			contentpane.getChildren().add(this.buttonbar.getNode(actionmanager, inputdata,
					parentwindow, parenttabpanes));
		}
		if (this.showcontent) {
			contentpane.getChildren().add(fieldtable.getNode(actionmanager, inputdata, parentwindow, parenttabpanes));
			if (hasmorefields) {
				morepane = new TitledPane("more",
						hiddenfieldtable.getNode(actionmanager, inputdata, parentwindow, parenttabpanes));
				morepane.setExpanded(false);
				morepane.setBorder(Border.EMPTY);
				morepane.setAnimated(false);
				if (parenttabpanes.length > 0)
					morepane.setOnMouseClicked(new EventHandler<MouseEvent>() {

						@Override
						public void handle(MouseEvent event) {
							for (int i = 0; i < parenttabpanes.length; i++)
								parenttabpanes[i].requestLayout();

						}

					});
				contentpane.getChildren().add(morepane);
			}
		}

		totalpane.getChildren().add(contentpane);
		HBox.setHgrow(contentpane, Priority.ALWAYS);

		logger.fine(" ----- start review existing data for multiplefieldconstraint ----");
		// checks if constraint is respected, else delete incorrect data
		for (int i = 0; i < this.allobjectconstraints.size(); i++) {
			CMultiFieldConstraint thisconstraint = this.allobjectconstraints.get(i);
			for (int j = 0; j < thisconstraint.getConstrainedFieldSize() - 1; j++) {
				CBusinessField<?> thisfield = thisconstraint.getConstrainedField(j);
				thisfield.pingValue();
			}
		}
		logger.fine(" ----- end review existing data for multiplefieldconstraint ----");

		return totalpane;

	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (type instanceof ObjectDataEltType) {

			ObjectDataElt object = new ObjectDataElt(eltname);
			object.setUID(this.uid);
			for (int i = 0; i < payloadlist.size(); i++) {
				CBusinessField<?> field = payloadlist.get(i);
				// only fields to update are the active ones
				if (field.isEditable()) {
					SimpleDataElt thisfieldcontent = field.getFieldDataElt();
					object.addField(thisfieldcontent);
				}
			}
			return object;
		}
		if (type instanceof ObjectIdDataEltType) {
			if (objectfieldname == null)
				throw new RuntimeException(
						"You have to specify an objectfieldname for objectid for name = " + this.name);
			CBusinessField<?> fieldfromname = this.payloadlistbyname.get(objectfieldname);
			String fielddrop = "[";
			Iterator<String> keylist = payloadlistbyname.keySet().iterator();
			while (keylist.hasNext())
				fielddrop = fielddrop + "," + keylist.next();
			fielddrop = fielddrop + "]";
			if (fieldfromname == null)
				throw new RuntimeException("could not field for specified name = '" + objectfieldname
						+ "', availablefields = " + fielddrop);
			if (!(fieldfromname instanceof CTextField))
				throw new RuntimeException("object id specified should be string, object fieldname = " + objectfieldname
						+ ", actual type = " + fieldfromname.getClass().getName());
			CTextField idfield = (CTextField) fieldfromname;
			ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, idfield.getFieldDataElt().getPayload());
			return objectid;
		}
		if (type instanceof ArrayDataEltType) {
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>) type;
			if (arraytype.getPayloadType() instanceof ObjectIdDataEltType) {
				if (objectfieldname == null)
					throw new RuntimeException(
							"You have to specify an objectfieldname for objectid for name = " + this.name);
				CBusinessField<?> fieldfromname = this.payloadlistbyname.get(objectfieldname);
				String fielddrop = "[";
				Iterator<String> keylist = payloadlistbyname.keySet().iterator();
				while (keylist.hasNext())
					fielddrop = fielddrop + "," + keylist.next();
				fielddrop = fielddrop + "]";
				if (fieldfromname == null)
					throw new RuntimeException("could not field for specified name = '" + objectfieldname
							+ "', availablefields = " + fielddrop);
				if (!(fieldfromname instanceof CTextField))
					throw new RuntimeException("object id specified should be string, object fieldname = "
							+ objectfieldname + ", actual type = " + fieldfromname.getClass().getName());
				CTextField idfield = (CTextField) fieldfromname;
				ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, idfield.getFieldDataElt().getPayload());
				ArrayDataElt<
						ObjectIdDataElt> array = new ArrayDataElt<ObjectIdDataElt>(eltname, new ObjectIdDataEltType());
				array.addElement(objectid);
				return array;

			}
		}
		throw new RuntimeException("Unsupported extraction type " + type);
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not supported by the widget yet");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {
		if (morepane != null)
			morepane.setOnMouseClicked(null);

	}

}
