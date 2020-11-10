/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic;

import java.io.IOException;
import java.util.ArrayList;

import java.util.logging.Logger;

import org.openlowcode.client.action.CActionData;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.DataElt;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * A class holding the data of a page. This is one of the main design objectives
 * of Open Lowcode light client architecture: having data well separated from
 * the layout.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPageData {

	private NamedList<DataElt> dataelements;
	private ArrayList<CInlineActionDataRef> inlineactiondataref;
	private ArrayList<Stage> subscenes;
	private static Logger LOGGER = Logger.getLogger(CPageData.class.getName());
	private boolean popup;
	private String message;

	public boolean isPopup() {
		return popup;
	}

	public String getMessage() {
		return message;
	}

	private CPageData() {
		dataelements = new NamedList<DataElt>();
	}

	/**
	 * adds a data element at the end of the current data element list
	 * 
	 * @param dataelt the data element to add
	 */
	private void addDataElt(DataElt dataelt) {
		this.dataelements.add(dataelt);
	}

	/**
	 * @return the number of data elements
	 */
	public int getElementNumber() {
		return dataelements.getSize();
	}

	/**
	 * @param index index to get the data element at
	 * @return the data element at this element
	 */
	public DataElt getDataElement(int index) {
		return dataelements.get(index);
	}

	/**
	 * gets the data element with the given name
	 * 
	 * @param name name of the element to look at
	 * @return the data element if it exists, null else
	 */
	public DataElt lookupDataElementByName(String name) {
		return dataelements.lookupOnName(name);
	}

	/**
	 * updates the page data, and the page node with inline data
	 * 
	 * @param module  module of the action
	 * @param name    name of the action
	 * @param newdata data brought back
	 */
	public void processInlineAction(String module, String name, CPageData newdata) {
		LOGGER.info("Inlineactiondataref size = " + inlineactiondataref.size());
		boolean processed = false;
		for (int i = 0; i < inlineactiondataref.size(); i++) {
			CInlineActionDataRef currentref = inlineactiondataref.get(i);
			if (currentref == null)
				throw new RuntimeException("Inline Action Data Ref number " + i + " is null");
			if (currentref.getModule() == null)
				throw new RuntimeException("Inline Action Data Ref number " + i + " module is null");
			if (currentref.getName() == null)
				throw new RuntimeException("Inline Action Data Ref number " + i + " name is null");
			if (module == null)
				throw new RuntimeException("Module in trigggered inline action is null ");
			if (name == null)
				throw new RuntimeException("Name in trigggered inline action is null ");

			if ((currentref.getModule().compareTo(module) == 0) && (currentref.getName().compareTo(name) == 0)) {
				LOGGER.finer("found one match for action for module "+module+", action "+name+" index = "+i);
				DataElt dataelt = newdata.getDataElement(currentref.getFieldorder());
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						currentref.getTargetnode().forceUpdateData(dataelt);

					}

				});

				processed = true;
			}
		}
		if (!processed) {
			LOGGER.severe("-----------------------------------------------------------------");
			LOGGER.severe(" no match found for " + module + "/" + name + ", existing inline actions below");
			for (int i = 0; i < inlineactiondataref.size(); i++) {
				CInlineActionDataRef currentref = inlineactiondataref.get(i);
				LOGGER.severe("		* " + currentref.getModule() + " " + currentref.getName());
			}
			throw new RuntimeException("Inline data not mapped " + module + "/" + name);
		}
	}

	/**
	 * Creates page data with the information from the server
	 * 
	 * @param reader the message reeader
	 * @throws OLcRemoteException if anything bad happens in the server during the
	 *                            transmission
	 * @throws IOException        if any error happens in data transmission with
	 *                            server
	 */
	public CPageData(MessageReader reader) throws OLcRemoteException, IOException {
		this();
		reader.startStructureArray("PAGEDATA");
		while (reader.structureArrayHasNextElement("PAGEDATA")) {
			DataElt dataelement = DataElt.readFromCML(reader);
			this.addDataElt(dataelement);
			reader.returnNextEndStructure("PAGEDATA");
		}
		this.message = reader.returnNextStringField("USM");
		this.popup = reader.returnNextBooleanField("POP");
		inlineactiondataref = new ArrayList<CInlineActionDataRef>();
		subscenes = new ArrayList<Stage>();

	}

	/**
	 * @param inlineactiondataref the inline action reference to add
	 */
	public void addInlineActionDataRef(CInlineActionDataRef inlineactiondataref) {
		this.inlineactiondataref.add(inlineactiondataref);

	}

	/**
	 * @param dialog a dialog (typically popup) on the page
	 */
	public void addSubScene(Stage dialog) {
		subscenes.add(dialog);

	}

	/**
	 * closes all the subscenes (popups) of the page
	 */
	public void closeSubScene() {
		for (int i = 0; i < subscenes.size(); i++)
			subscenes.get(i).close();
	}

	/**
	 * builds the echo attributes from the the action. It is hardcoded to the
	 * InlineEchoAction
	 * 
	 * @param actionattributes action to send as echo
	 * @return the action
	 */
	public static CPageData echo(CActionData actionattributes) {
		CPageData returndata = new CPageData();
		for (int i = 0; i < actionattributes.getAttributesNumber(); i++) {
			DataElt element = actionattributes.getElementAt(i);
			element.changeName(element.getName().replace("IN", "OUT"));
			returndata.addDataElt(element);
		}
		return returndata;
	}
}
