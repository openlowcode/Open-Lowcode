/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.tools.messages.SFile;

/**
 * an object with the print-out property will generate a PDF print-out when the
 * object reaches a final state of the lifecycle
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 * @param <F> transition choice definition for the lifecycle
 */
public class Printout<E extends DataObject<E> & FilecontentInterface<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Printout.class.getName());
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	private PrintoutDefinition<E, F> printoutdefinition;

	/**
	 * print out property for the object
	 * 
	 * @param definition    definition of the print-out property
	 * @param parentpayload parent data object payload
	 */
	public Printout(PrintoutDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.printoutdefinition = definition;
	}

	/**
	 * generates a preview of the print-out
	 * 
	 * @param object data object
	 * @return the binary file
	 */
	public SFile generatepreview(E object) {
		PrintOutGenerator<E> generator = printoutdefinition.getGenerator();
		return generator.generateContent(object,
				printoutdefinition.getPrintoutLabel() + "-TEMP-" + sdf.format(new Date()));
	}

	/**
	 * generates the real printout after data object reaches a final state
	 * 
	 * @param object data object
	 */
	public void generateprintout(E object) {
		PrintOutGenerator<E> generator = printoutdefinition.getGenerator();
		generator.addContentAsAttachment(object, printoutdefinition.getPrintoutLabel());
	}

	/**
	 * post-processing to the change state. generates a print-out if an appropriate
	 * lifecycle state is reached
	 * 
	 * @param object   data object
	 * @param newstate new state
	 */
	public void postprocLifecycleChangestate(E object, ChoiceValue<F> newstate) {
		ChoiceValue<F> triggerstates[] = printoutdefinition.getTriggerStates();
		boolean istrigger = false;
		for (int i = 0; i < triggerstates.length; i++) {
			ChoiceValue<F> thisvalue = triggerstates[i];
			if (thisvalue.getStorageCode().equals(newstate.getStorageCode()))
				istrigger = true;

		}
		if (istrigger)
			generateprintout(object);
	}

	/**
	 * post processing for massive set state (not yet optimized for batch
	 * processing)
	 * 
	 * @param objectbatch   batch of object
	 * @param newstate      new state
	 * @param printoutbatch corresponding batch of print-out properties for the
	 *                      objects
	 */
	public static <E extends DataObject<E> & FilecontentInterface<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>> void postprocLifecycleChangestate(
			E[] objectbatch, ChoiceValue<F>[] newstate, Printout<E, F>[] printoutbatch) {
		// ------------ object control
		// -----------------------------------------------------
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (printoutbatch == null)
			throw new RuntimeException("lifecycle batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");
		if (objectbatch.length != printoutbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with printout batch length " + printoutbatch.length);
		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new printout batch length " + newstate.length);
		logger.warning(" *-*-* This method is not yet optimized for batch processing");
		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				printoutbatch[i].postprocLifecycleChangestate(objectbatch[i], newstate[i]);
			}
		}
	}
}
