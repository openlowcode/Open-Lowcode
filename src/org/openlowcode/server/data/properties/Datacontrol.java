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

import java.util.logging.Logger;
import org.openlowcode.module.system.data.choice.ControllevelChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.constraints.DataControlHelper;
import org.openlowcode.server.data.properties.constraints.DataControlRuleFeedback;
import org.openlowcode.server.data.storage.StoredField;

/**
 * The data control property will perform controls after insert, update and set
 * state
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class Datacontrol<E extends DataObject<E> & LifecycleInterface<E, ?>> extends DataObjectProperty<E> {
	@SuppressWarnings("unused")
	private DatacontrolDefinition<E> definition;
	private StoredField<String> summary;
	@SuppressWarnings("unused")
	private Lifecycle<E, ?> lifecycle;
	private DataControlHelper<E> helper;
	private static Logger logger = Logger.getLogger(Datacontrol.class.toString());

	/**
	 * creates a data control property
	 * 
	 * @param definition    definition of the data control property
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public Datacontrol(DatacontrolDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.definition = definition;
		helper = definition.getDataControlHelper();
		summary = (StoredField<String>) this.field.lookupOnName("SUMMARY");
	}

	/**
	 * gets the summary of the controls
	 * 
	 * @return the summary
	 */
	public String getSummary() {
		return this.summary.getPayload();
	}

	/**
	 * sets the dependent property lifecycle
	 * 
	 * @param lifecycle dependent property lifecycle
	 */
	public void setDependentPropertyLifecycle(Lifecycle<E, ?> lifecycle) {
		this.lifecycle = lifecycle;
	}

	private void makeSummaryControl(E object) {
		DataControlRuleFeedback[] checkresult = helper.performcontrols(object);
		int warnings = 0;
		int errors = 0;
		if (checkresult != null)
			for (int i = 0; i < checkresult.length; i++) {
				DataControlRuleFeedback thisresult = checkresult[i];
				if (thisresult.getFeedbacktype().equals(ControllevelChoiceDefinition.get().WARNING))
					warnings++;
				if (thisresult.getFeedbacktype().equals(ControllevelChoiceDefinition.get().ERROR))
					errors++;

			}
		if ((errors == 0) && (warnings == 0))
			this.summary.setPayload("All data controls are OK");
		if ((errors == 0) && (warnings == 1))
			this.summary.setPayload("Data is valid with 1 warning");
		if ((errors == 0) && (warnings > 1))
			this.summary.setPayload("Data is valid with " + warnings + " warnings");
		if ((errors == 1))
			this.summary.setPayload("Data is invalid (1 error) !");
		if ((errors > 1))
			this.summary.setPayload("Data is invalid (" + errors + " errors) !");
	}

	/**
	 * after the object has been inserted, performs the control (note: not to lose
	 * data, invalid objects will be persisted)
	 * 
	 * @param object
	 */
	public void postprocStoredobjectInsert(E object) {

		makeSummaryControl(object);
		object.update();
	}

	/**
	 * performs thecontrols before the objects has been updated. If there is an
	 * error, the object will be persisted
	 * 
	 * @param object data object
	 */
	public void preprocUniqueidentifiedUpdate(E object) {
		makeSummaryControl(object);
	}

	/**
	 * performs a control and blocks the set state if the controls are not OK
	 * 
	 * @param object   data object
	 * @param newstate new state
	 */
	public void preprocLifecycleChangestate(E object, Object newstate) {
		DataControlRuleFeedback[] checkresult = helper.performcontrols(object);

		if (checkresult != null)
			for (int i = 0; i < checkresult.length; i++) {
				DataControlRuleFeedback thisresult = checkresult[i];

				if (thisresult.getFeedbacktype().equals(ControllevelChoiceDefinition.get().ERROR)) {
					throw new RuntimeException("Data is not valid (" + thisresult.getMessage() + ")");
				}

			}
	}

	/**
	 * massive version of the data controls before update
	 * 
	 * @param object           batch of object
	 * @param datacontrolbatch corresponding batch of data controls
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, ?>> void preprocUniqueidentifiedUpdate(E[] object,
			Datacontrol<E> datacontrolbatch[]) {
		logger.warning("This function is not optimized for batch processing");

		for (int i = 0; i < object.length; i++)
			datacontrolbatch[i].preprocUniqueidentifiedUpdate(object[i]);
	}

	/**
	 * massive version of the data controls before insert
	 * 
	 * @param object           batch of object
	 * @param datacontrolbatch corresponding batches of data controls
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, ?>> void postprocStoredobjectInsert(E[] object,
			Datacontrol<E> datacontrolbatch[]) {
		logger.warning("This function is not optimized for batch processing");
		for (int i = 0; i < object.length; i++)
			datacontrolbatch[i].postprocStoredobjectInsert(object[i]);

	}

	/**
	 * gets the validation details for the object
	 * 
	 * @param object parent data object
	 * @return readable summary of the control
	 */
	public String getvalidationdetail(E object) {
		StringBuffer totalmessages = new StringBuffer();
		DataControlRuleFeedback[] checkresult = helper.performcontrols(object);
		if (checkresult != null)
			for (int i = 0; i < checkresult.length; i++) {
				DataControlRuleFeedback thisresult = checkresult[i];
				totalmessages
						.append(thisresult.getFeedbacktype().getDisplayValue() + ": " + thisresult.getMessage() + "\n");
			}
		return totalmessages.toString();
	}

	/**
	 * validates and sends the level of errors
	 * 
	 * @obsolete
	 * @param object parent data object
	 * @return a runtime exception
	 */
	public ChoiceValue<ControllevelChoiceDefinition> validate(E object) {
		throw new RuntimeException("Not yet implemented");
	}

	/**
	 * batch version of the processing before object state. The processing will stop
	 * at the first error (this is not ideal)
	 * 
	 * @param objectbatch          batch of objects
	 * @param newstate             new states for the objects
	 * @param complexworkflowbatch corresponding complex workflow batches
	 */
	public static <E extends DataObject<E> & LifecycleInterface<E, ?>> void preprocLifecycleChangestate(E[] objectbatch,
			Object[] newstate, Datacontrol<E>[] complexworkflowbatch) {
		// ------------ object control
		// -----------------------------------------------------
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (complexworkflowbatch == null)
			throw new RuntimeException("lifecycle batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");
		if (objectbatch.length != complexworkflowbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with complexworkflow batch length " + complexworkflowbatch.length);
		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new state batch length " + newstate.length);

		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				complexworkflowbatch[i].preprocLifecycleChangestate(objectbatch[i], newstate[i]);
			}
		}
	}
}
