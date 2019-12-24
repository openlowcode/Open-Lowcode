/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;

import org.openlowcode.server.action.SActionOutputDataRef;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.DataEltType;

/**
 * A data location for an inline action
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data element
 */
public class SActionInlineDataLoc<E extends DataEltType> extends Named {
	private SPageNode originnode;
	private String objectfield;
	private E type;

	/**
	 * @return the origin node of the action
	 */
	public SPageNode getoriginnode() {
		return this.originnode;
	}

	/**
	 * creates an inline action data location for a field of an object
	 * 
	 * @param originnode          node of the action
	 * @param outputactiondataref the reference of the output data of the inline
	 *                            action
	 * @param objectfield         relevant field of object if needed
	 */
	protected SActionInlineDataLoc(SPageNode originnode, SActionOutputDataRef<E> outputactiondataref,
			String objectfield) {
		super(outputactiondataref.getName());

		this.originnode = originnode;
		this.type = outputactiondataref.getType();
		this.objectfield = objectfield;
	}

	/**
	 * creates an inline action data location
	 * 
	 * @param originnode          node of the action
	 * @param outputactiondataref the reference of the output data of the inline
	 *                            action
	 */
	protected SActionInlineDataLoc(SPageNode originnode, SActionOutputDataRef<E> outputactiondataref) {
		super(outputactiondataref.getName());

		this.originnode = originnode;
		this.type = outputactiondataref.getType();
		this.objectfield = null;
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void WriteToCDL(MessageWriter writer) throws IOException {
		writer.startStructure("INLINEACTIONDATA");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", type.printType());
		if (this.objectfield != null)
			writer.addStringField("OBF", objectfield);
		writer.addStringField("PTH", originnode.printPath());

		writer.endStructure("ACTIONDATA");
	}
}
