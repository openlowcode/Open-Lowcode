/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionDataLoc;
import org.openlowcode.server.graphic.widget.SObjectDisplay;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;

import org.openlowcode.tools.structure.TextDataElt;

/**
 * an action received from the clients. Actions are executed, and provide as
 * result a page and businessdata to fill the page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SActionRef extends Named {
	private int inputsecurityargumentindex;
	private static Logger logger = Logger.getLogger(SActionRef.class.getName());
	private String module;

	/**
	 * @return get the module of the action reference
	 */
	public String getModule() {
		return module;
	}

	@SuppressWarnings("rawtypes")
	protected NamedList<SActionDataLoc> actiondata;

	@SuppressWarnings("rawtypes")
	protected NamedList<SNullActionInputDataRef> nullactiondata;

	/**
	 * creates an action reference
	 * 
	 * @param name               name of the action (unique for the module)
	 * @param module             name of the module (unique)
	 * @param inputargumentindex 0 or more if there exists an inputargument index,
	 *                           -1 if there is no input security argument
	 * 
	 */
	@SuppressWarnings("rawtypes")
	protected SActionRef(String name, String module, int inputargumentindex) {
		super(name);
		this.module = module;
		this.inputsecurityargumentindex = inputargumentindex;

		actiondata = new NamedList<SActionDataLoc>();
		nullactiondata = new NamedList<SNullActionInputDataRef>();
	}

	/**
	 * adds action business data
	 * 
	 * @param dataelt location of the action business data on the page
	 */
	@SuppressWarnings("rawtypes")
	protected void addActionBusinessData(SActionDataLoc dataelt) {
		actiondata.add(dataelt);
	}

	/**
	 * adds a null action business data
	 * 
	 * @param nullactioninput a null action business data
	 */
	@SuppressWarnings("rawtypes")
	protected void setNullActionBusinessData(SNullActionInputDataRef nullactioninput) {
		nullactiondata.add(nullactioninput);
	}

	/**
	 * generates the potential action data to check for security. This allows to
	 * hide buttons if the button for the action should be disabled
	 * 
	 * @param pagedata pagedata
	 * @return an action data with the security argument, when relevant, filled at
	 *         the correct sequence. Other attributes will not be filled.
	 */
	public SActionData generatePotentialActionDataForSecurity(SPageData pagedata) {
		logger.fine(" ------------ Generating Potential Action Data for " + this.getName() + " for pagedata size  = "
				+ pagedata.size());
		if (inputsecurityargumentindex == -1)
			return null;
		SActionData builtactiondata = new SActionData();
		for (int i = 0; i < actiondata.getSize(); i++) {
			if (inputsecurityargumentindex != i) {
				builtactiondata.addData(null);
				logger.fine("    * action data " + i + " name = " + actiondata.get(i).getName()
						+ " is not security relevant, hardcoded to null");
			} else {
				for (int j = 0; j < actiondata.getSize(); j++) {
					SActionDataLoc<?> thisactiondata = actiondata.get(j);
					SActionInputDataRef<?> thisactiondataref = thisactiondata.getInputActionDataRef();
					if (thisactiondataref.getOrder() == i) {
						logger.fine("    * detected relevant page data input index=" + j + " for action data index = "
								+ i + ", name = " + actiondata.get(i).getName());
						// process inputargument
						SPageNode node = thisactiondata.getOriginNode();
						boolean treated = false;

						// ----------- OBJECT DISPLAY
						if (node instanceof SObjectDisplay) {
							logger.fine("      * node is object display");

							SObjectDisplay<?> display = (SObjectDisplay<?>) node;
							TObjectDataElt<?> object = display.getInputData();
							// ------------- EXTRACTION OF OBJECTID FROM OBJECT, FIRST CASE TREATED
							if (thisactiondataref.getType() instanceof ObjectIdDataEltType) {
								logger.fine("      * action data is object id");
								String actionattributenamename = thisactiondataref.getName();
								String fieldnameinobject = thisactiondata.getObjectFieldName();
								SimpleDataElt elt = object.lookupEltByName(fieldnameinobject);
								if (elt == null)
									throw new RuntimeException(
											"Did not find element with name = " + fieldnameinobject + " in object ");
								if (!(elt instanceof TextDataElt))
									throw new RuntimeException(
											"Expecting DataObjectId as TextDataElt, got " + elt.getClass().getName()
													+ ", printout = " + elt.defaultTextRepresentation());
								TextDataElt idelt = (TextDataElt) (elt);
								ObjectIdDataElt objectidelt = new ObjectIdDataElt(actionattributenamename,
										idelt.getPayload());
								builtactiondata.addData(objectidelt);
								treated = true;
							}
						}

						if (!treated) {
							logger.fine("        - not treated");
							return null;
							// use case not treated, do not provide back anything, widget will not be hidden
						}
					}
				}

			}
		}
		return builtactiondata;

	}

	/**
	 * writes the attribute in the order specified
	 * 
	 * @param writer message writer
	 * @throws IOException if any error is encountered writing the message
	 */
	public void writeAttributesInOrder(MessageWriter writer) throws IOException {
		// algorithm to order attributes

		ArrayList<SActionDataLoc<?>> orderedactiondataloc = new ArrayList<SActionDataLoc<?>>();
		ArrayList<SNullActionInputDataRef<?>> orderednullactiondataloc = new ArrayList<SNullActionInputDataRef<?>>();

		int index = 0;
		boolean found = true;
		int security = 0;
		while ((found) && (security < 10000)) {
			found = false;

			for (int i = 0; i < actiondata.getSize(); i++) {
				SActionDataLoc<?> thisactiondata = actiondata.get(i);
				if (thisactiondata.getSequence() == index) {
					if (found)
						throw new RuntimeException(
								String.format("for the action " + this.getName() + ", the attribute number " + index
										+ " is duplicated, error found on " + thisactiondata.getName()));
					orderedactiondataloc.add(thisactiondata);
					orderednullactiondataloc.add(null);
					found = true;

				}
			}

			for (int i = 0; i < nullactiondata.getSize(); i++) {
				SNullActionInputDataRef<?> thisnullactiondata = nullactiondata.get(i);
				if (thisnullactiondata.getOrder() == index) {
					if (found)
						throw new RuntimeException(
								String.format("for the action " + this.getName() + ", the attribute number " + index
										+ " is duplicated, error found on " + thisnullactiondata.getName()));
					orderedactiondataloc.add(null);
					orderednullactiondataloc.add(thisnullactiondata);
					found = true;

				}
			}
			security++;
			index++;
		}
		if (security == 10000)
			throw new RuntimeException("security loop error");

		for (int i = 0; i < orderedactiondataloc.size(); i++) {
			if (orderedactiondataloc.get(i) != null)
				orderedactiondataloc.get(i).WriteToCDL(writer);
			if (orderednullactiondataloc.get(i) != null)
				orderednullactiondataloc.get(i).WriteToCDL(writer);
		}
	}

	/**
	 * writes a reference to an action inside the widget
	 * 
	 * @param writer message writer
	 * @throws IOException if any transmission error is encountered
	 */
	public void writeToCML(MessageWriter writer) throws IOException {

		writer.startStructure("ACTION");
		writer.addStringField("NAME", this.getName());
		writer.addStringField("MODULE", this.module);
		writer.startStructure("ACTIONDATAS");

		writeAttributesInOrder(writer);

		writer.endStructure("ACTIONDATAS");
		writer.endStructure("ACTION");
	}

	/**
	 * gets the provided action execution
	 * 
	 * @return action execution
	 */
	public ActionExecution getAction() {
		SModule thismodule = OLcServer.getServer().getModuleByName(module);
		if (thismodule == null)
			throw new RuntimeException(String.format(
					"Module unknown %s for action request %s during processing of actionref", module, this.getName()));
		ActionExecution action = thismodule.getAction(this.getName());
		if (action == null)
			throw new RuntimeException(String.format("In module %s,  action unknown %s during processing of actionref",
					module, this.getName()));
		return action;
	}

	/**
	 * gets the action data loc for the attribute with the given name
	 * 
	 * @param name name of the attribute
	 * @return the action data loc if it exists, else null
	 */
	public SActionDataLoc<?> getActionData(String name) {
		return actiondata.lookupOnName(name);
	}
}
