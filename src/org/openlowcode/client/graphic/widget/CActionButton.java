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

import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TabPane;

import java.io.IOException;
import java.util.Optional;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import javafx.scene.control.Label;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.ChoiceDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Window;
import javafx.scene.control.ButtonType;

/**
 * A button launching an action when pressed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CActionButton
		extends
		CPageNode {
	private Button button;
	private String label;
	private String tooltip;
	private CPageAction action;
	private CPageInlineAction inlineaction;
	private boolean conditionalshow;
	private boolean forcepopuphidewheninline;
	private CPageDataRef conditionalshowdatareference;
	private boolean hasconfirmationmessage;
	private String confirmationmessage;
	private String confirmationmessagecontinuelabel;
	private String confirmationmessagestoplabel;


	private CActionButton(
			CPageSignifPath parentpath,
			String significantpath,
			String label,
			String tooltip,
			CPageAction action,
			CPageInlineAction inlineaction) {
		super(parentpath, significantpath);
		this.label = label;
		this.tooltip = tooltip;
		this.action = action;
		this.inlineaction = inlineaction;
	}

	/**
	 * creates an action button from information received from the server
	 * 
	 * @param reader     message reader
	 * @param parentpath path so far in the widget tree
	 * @throws OLcRemoteException if any error is sent from the server
	 * @throws IOException        if any communication error happens while reading
	 *                            the messag
	 */
	public CActionButton(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LABEL");
		tooltip = reader.returnNextStringField("ROLLOVERTIP");
		forcepopuphidewheninline = reader.returnNextBooleanField("FORCEPOPUPCLOSE");
		String actiontype = reader.returnNextStartStructure();
		boolean treated = false;
		if (actiontype.compareTo("ACTION") == 0) {
			action = new CPageAction(reader);
			treated = true;
		}
		if (actiontype.compareTo("INLINEACTION") == 0) {
			inlineaction = new CPageInlineAction(reader);
			treated = true;
		}
		if (!treated)
			throw new RuntimeException(" was expecting either ACTION or INLINEACTION structure, got " + actiontype);
		conditionalshow = reader.returnNextBooleanField("CDS");
		if (conditionalshow) {
			this.conditionalshowdatareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.conditionalshowdatareference.getType().equals(new ChoiceDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted ChoiceDataEltType, got %s in CPage ",
						conditionalshowdatareference.getName(), conditionalshowdatareference));

		}
		this.hasconfirmationmessage = reader.returnNextBooleanField("HCF");
		if (this.hasconfirmationmessage) {
			this.confirmationmessage = reader.returnNextStringField("CFM");
			this.confirmationmessagecontinuelabel = reader.returnNextStringField("CFC");
			this.confirmationmessagestoplabel = reader.returnNextStringField("CFS");

		}
		reader.returnNextEndStructure("ACTIONBUTTON");
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {

		if (this.conditionalshow) {
			DataElt thiselement = inputdata.lookupDataElementByName(conditionalshowdatareference.getName());
			if (thiselement == null)
				throw new RuntimeException(String.format(
						"could not find any page data with name = %s" + conditionalshowdatareference.getName()));
			if (!thiselement.getType().equals(conditionalshowdatareference.getType()))
				throw new RuntimeException(
						String.format("page data with name = %s does not have expected %s type, actually found %s",
								conditionalshowdatareference.getName(), conditionalshowdatareference.getType(),
								thiselement.getType()));
			ChoiceDataElt<?> thischoiceelement = (ChoiceDataElt<?>) thiselement;
			if (thischoiceelement.getStoredValue().compareTo("YES") != 0)
				return new Label("");
		}

		button = new Button(label);
		button.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		button.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
		button.textOverrunProperty().set(OverrunStyle.CLIP);
		// button.setMinWidth((new
		// Text(this.label).getBoundsInLocal().getWidth()+20)*1.3);
		if (tooltip != null)
			button.setTooltip(new Tooltip("tooltip"));
		if (!this.hasconfirmationmessage) {
			if (action != null) {
				actionmanager.registerEvent(button, action);
				if (callback != null)
					actionmanager.registerCallback(button, callback);
				buttonhandler = new ButtonHandler(actionmanager);
				button.setOnMouseClicked(buttonhandler);
			}
			if (inlineaction != null) {
				if (nodetocollapsewhenactiontriggered != null)
					inlineaction.setNodeToCollapse(nodetocollapsewhenactiontriggered);
				if (this.forcepopuphidewheninline) {
					actionmanager.registerInlineActionwithPopupClose(button, inlineaction);
				} else {
					actionmanager.registerInlineAction(button, inlineaction);
				}
				buttonhandler = new ButtonHandler(actionmanager);
				button.setOnMouseClicked(buttonhandler);
			}
		}
		if (this.hasconfirmationmessage) {
			button.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent arg0) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("User Confirmation");
					alert.setContentText(confirmationmessage);
					ButtonType continuetype = new ButtonType(confirmationmessagecontinuelabel);
					ButtonType stoptype = new ButtonType(confirmationmessagestoplabel);
					alert.getButtonTypes().setAll(continuetype, stoptype);
					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == continuetype) {

						if (action != null) {
							if (callback != null)
								actionmanager.directfireEvent(action, callback);
							if (callback == null)
								actionmanager.directfireEvent(action);
						}
						if (inlineaction != null) {
							if (forcepopuphidewheninline)
								inlineaction.forcePopupClose();
							actionmanager.directfireInlineEvent(inlineaction);
						}
					}

				}

			});
		}

		return button;
	}

	private ButtonHandler buttonhandler;

	static class ButtonHandler
			implements
			EventHandler<MouseEvent> {
		private PageActionManager pageactionmanager;

		public ButtonHandler(PageActionManager pageactionmanager) {
			this.pageactionmanager = pageactionmanager;
		}

		@Override
		public void handle(MouseEvent event) {
			// in case of double click, causes issues
			if (event.getClickCount() == 1)
				pageactionmanager.getMouseHandler().handle(event);

		}

	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		throw new RuntimeException(
				String.format("request of action data of type %s, but CActionButton cannot provide any data", type));
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	private Callback callback = null;

	public void setCallBack(Callback callback) {
		this.callback = callback;
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		CActionButton deepcopy = new CActionButton(this.getParentpath(), this.getSignificantpath(), label, tooltip,
				action, inlineaction);
		deepcopy.setCallBack(callback);
		return deepcopy;
	}

	@Override
	public void mothball() {
		if (buttonhandler != null)
			if (button != null) {
				button.setOnMouseClicked(null);
			}

	}

}
