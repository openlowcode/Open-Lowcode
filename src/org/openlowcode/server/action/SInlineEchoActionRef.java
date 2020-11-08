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

import java.util.function.Function;

import org.openlowcode.module.system.design.SystemModule;
import org.openlowcode.server.graphic.widget.SActionDataLoc;
import org.openlowcode.tools.structure.DataEltType;


/**
 * An inline echo action ref, when triggered, transfers the data from a widget
 * to another.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of data to transfer
 */
public class SInlineEchoActionRef<E extends DataEltType>
		extends
		SInlineActionRef {

	private E dataelementtype;

	/**
	 * create an inline echo action ref (transfering some data from a widget to
	 * another using a pseudo inline action
	 * 
	 * @param dataelementtype type of data to transfer
	 */
	public SInlineEchoActionRef(E dataelementtype) {
		super("ECHO"+Thread.currentThread().getId()+System.nanoTime(), SystemModule.getSystemModule().getName(), -1, true);
		this.dataelementtype = dataelementtype;
	}

	protected SActionInputDataRef<E> getInputActionDataRef() {
		return new SActionInputDataRef<E>("ECHOIN", dataelementtype, 0);
	}

	/**
	 * sets the input data of the echo (from the income widget)
	 * 
	 * @param input location of the data input
	 */
	public void setInputData(Function<SActionInputDataRef<E>, SActionDataLoc<E>> input) {
		if (input == null)
			this.addActionBusinessData(SActionDataLoc.ground(getInputActionDataRef()));
		this.addActionBusinessData(input.apply(getInputActionDataRef()));
	}

	/**
	 * @return the reference to the output of the echo action
	 */
	public SActionOutputDataRef<E> getOutputActionDataRef() {
		return new SActionOutputDataRef<E>("ECHOOUT", dataelementtype, 0);
	}

}
