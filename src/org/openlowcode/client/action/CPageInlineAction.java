/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.action;

import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.widget.CollapsibleNode;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ChoiceDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * An inline action takes some data to the server, where it is processed, and,
 * as a result, another data is brought back and displayed on the same page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CPageInlineAction extends Named {
	private static Logger logger = Logger.getLogger(CPageInlineAction.class.getName());
	private NamedList<CActionDataLoc> businessdataloc;
	private String module;
	private boolean local;
	private CollapsibleNode nodetocollapse;
	/**
	 * @return true if the action is local (there are some actions that are only
	 *         moving data from a widget to another without call to the server
	 */
	public boolean isLocal() {
		return this.local;
	}

	/**
	 * @return module of the inline action
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @param reader message reader
	 * @throws OLcRemoteException if anything bad happens on the server during the
	 *                            transmission
	 * @throws IOException        if any error happens during the transmission
	 */
	public CPageInlineAction(MessageReader reader) throws OLcRemoteException, IOException {
		super(reader.returnNextStringField("NAME"));
		this.module = reader.returnNextStringField("MODULE");
		this.local = reader.returnNextBooleanField("LOCAL");
		businessdataloc = new NamedList<CActionDataLoc>();
		reader.startStructureArray("ACTIONDATA");
		while (reader.structureArrayHasNextElement("ACTIONDATA")) {
			CActionDataLoc dataloc = new CActionDataLoc(reader);
			businessdataloc.add(dataloc);

			reader.returnNextEndStructure("ACTIONDATA");

		}
		reader.startStructureArray("OUTPUTDATA");
		while (reader.structureArrayHasNextElement("OUTPUTDATA")) {

		}
		reader.returnNextEndStructure("INLINEACTION");
	}

	@SuppressWarnings("rawtypes")
	public CActionData getDataContent(CPage page) {
		CActionData result = new CActionData();
		for (int i = 0; i < businessdataloc.getSize(); i++) {
			CActionDataLoc thisbusinessdataloc = businessdataloc.get(i);
			if (thisbusinessdataloc.getPath() != null) {
				CPageNode originnode = page.getNodeAtSignificantPath(thisbusinessdataloc.getPath());

				DataElt businessdataelt = originnode.getDataElt(
						DataEltType.getDataEltType(thisbusinessdataloc.getType()), thisbusinessdataloc.getName(),
						thisbusinessdataloc.getObjectField());
				if (businessdataelt == null)
					throw new RuntimeException("no business data found for [" + i + "], datalog = "
							+ thisbusinessdataloc.getName() + ", objectfield " + thisbusinessdataloc.getObjectField()
							+ ", path " + thisbusinessdataloc.getPath() + ", type " + thisbusinessdataloc.getType());
				result.addActionAttribute(businessdataelt);
			} else {
				boolean treated = false;
				if (thisbusinessdataloc.getType().compareTo("TXT") == 0) {
					treated = true;
					result.addActionAttribute(new TextDataElt(thisbusinessdataloc.getName(), ""));
				}
				if (thisbusinessdataloc.getType().compareTo("MLC")==0) {
					treated=true;
					result.addActionAttribute(new MultipleChoiceDataElt(thisbusinessdataloc.getName()));
				}
				if (thisbusinessdataloc.getType().equals("OID")) {
					treated=true;
					result.addActionAttribute(new ObjectIdDataElt(thisbusinessdataloc.getName()));
				}
				
				if (thisbusinessdataloc.getType().startsWith("ARR")) {
					String subtype = thisbusinessdataloc.getType().substring(4);
					if (subtype.compareTo(CActionDataLoc.CHOICE_TYPE) == 0) {
						result.addActionAttribute(
								new ArrayDataElt(thisbusinessdataloc.getName(), new ChoiceDataEltType()));
						treated = true;
					}
					if (subtype.compareTo(CActionDataLoc.TEXT_TYPE) == 0) {
						result.addActionAttribute(
								new ArrayDataElt(thisbusinessdataloc.getName(), new TextDataEltType()));
						treated = true;
					}
					if (subtype.compareTo(CActionDataLoc.OBJ_ID_TYPE) == 0) {
						result.addActionAttribute(
								new ArrayDataElt(thisbusinessdataloc.getName(), new ObjectIdDataEltType()));
						treated = true;
					}

				}
				if (!treated)
					throw new RuntimeException("Type not supported yet : " + thisbusinessdataloc.getType()
							+ " for path "+thisbusinessdataloc.getPath()+", attribute name = " + thisbusinessdataloc.getName());
			}
		}
		return result;
	}

	/**
	 * @return get a key in the form of MODULE.NAME
	 */
	public String key() {
		return this.module + "." + this.getName();
	}

	private boolean forcepopupclose = false;

	/**
	 * call it to close popup after action has been called
	 */
	public void forcePopupClose() {
		this.forcepopupclose = true;

	}

	/**
	 * @return true if the popups should be closed at execution of the inline action
	 */
	public boolean isForcePopupClose() {
		return this.forcepopupclose;
	}

	/**
	 * @param nodepath a node path
	 * @return true if one of the business data locations of this inline action if
	 *         at the node path
	 */
	public boolean includesnodepath(String nodepath) {
		for (int i = 0; i < businessdataloc.getSize(); i++) {
			CActionDataLoc thisbusinessdataloc = businessdataloc.get(i);
			logger.fine("            - " + thisbusinessdataloc.getPath() + " - " + nodepath);
			if (thisbusinessdataloc.getPath() != null) {
				if (nodepath.equals(thisbusinessdataloc.getPath()))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * sets the node to collapse when the inline action is triggered
	 * @param nodetocollapse node to collapse
	 * @since 1.9
	 */
	public void setNodeToCollapse(CollapsibleNode nodetocollapse) {
		this.nodetocollapse = nodetocollapse;
	}
	/**
	 * @return the node to collapse, if any is specified
	 * @since 1.9
	 */
	public CollapsibleNode getNodeToCollapse() {
		return nodetocollapse;
	}
}
