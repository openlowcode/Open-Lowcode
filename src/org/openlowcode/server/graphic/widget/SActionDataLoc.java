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

import org.openlowcode.server.action.SActionInputDataRef;

import org.openlowcode.server.graphic.SPageNode;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;

import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataEltType;
import org.openlowcode.tools.structure.MultipleChoiceDataEltType;
import org.openlowcode.tools.structure.ObjectDataEltType;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * An element of ActionBusinessData is a piece of data sent back to the server
 * with an action. It originates mostly from screen element (CSPNodeElement).
 * Todo: define how to send back data that is not in a CSPNodeElement but as
 * background context
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SActionDataLoc<E extends DataEltType> extends Named {
	private SActionInputDataRef<E> inputactiondataref;

	/**
	 * this method will ground the attribute, depending on type
	 * <ul>
	 * <li>String: empty string</li>
	 * <li>Array of any type: zero element array</li>
	 * <li>other: not yet supported</li>
	 * </ul>
	 * 
	 * @param inputactiondataref
	 * @return
	 */
	public static <E extends DataEltType> SActionDataLoc<E> ground(SActionInputDataRef<E> inputactiondataref) {
		if (inputactiondataref.getType() instanceof TextDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);
		if (inputactiondataref.getType() instanceof ArrayDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);
		if (inputactiondataref.getType() instanceof ObjectIdDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);
		if (inputactiondataref.getType() instanceof ObjectDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);
		if (inputactiondataref.getType() instanceof DateDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);
		if (inputactiondataref.getType() instanceof MultipleChoiceDataEltType)
			return new SActionDataLoc<E>(null, inputactiondataref);

		throw new RuntimeException(" type not supported : " + inputactiondataref.getType().printType() + " for "
				+ inputactiondataref.getName());
	}

	private SPageNode originnode;
	private String objectfield;
	private E type;
	private int sequence;

	/**
	 * @return
	 */
	public String getObjectFieldName() {
		return this.objectfield;
	}

	/**
	 * @return
	 */
	public SActionInputDataRef<E> getInputActionDataRef() {
		return this.inputactiondataref;
	}

	public SPageNode getOriginNode() {
		return this.originnode;
	}

	/**
	 * @return
	 */
	public int getSequence() {
		return this.sequence;
	}

	/**
	 * @param origincspnode
	 * @param inputactiondataref
	 * @param objectfield
	 */
	protected SActionDataLoc(SPageNode originnode, SActionInputDataRef<E> inputactiondataref, String objectfield) {
		super(inputactiondataref.getName());
		this.inputactiondataref = inputactiondataref;
		this.originnode = originnode;
		this.type = inputactiondataref.getType();
		this.objectfield = objectfield;
		this.sequence = inputactiondataref.getOrder();
	}

	/**
	 * @param originnode
	 * @param inputactiondataref
	 */
	protected SActionDataLoc(SPageNode originnode, SActionInputDataRef<E> inputactiondataref) {
		super(inputactiondataref.getName());
		this.inputactiondataref = inputactiondataref;
		this.originnode = originnode;
		this.type = inputactiondataref.getType();
		this.objectfield = null;
		this.sequence = inputactiondataref.getOrder();
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void WriteToCDL(MessageWriter writer) throws IOException {
		writer.startStructure("ACTIONDATA");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", type.printType());
		if (this.objectfield != null)
			writer.addStringField("OBF", objectfield);

		if (originnode != null)
			writer.addStringField("PTH", originnode.printPath());
		if (originnode == null)
			writer.addStringField("PTH", null);
		writer.endStructure("ACTIONDATA");
	}
}
