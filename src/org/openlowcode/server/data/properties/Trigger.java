/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.logging.Logger;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.formula.TriggerToExecute;
import org.openlowcode.server.data.properties.trigger.CustomTriggerExecution;
import org.openlowcode.server.runtime.OLcServer;

/**
 * Property for an object to have a trigger. The trigger will be kicked at a
 * specific condition (typically insert, update, or set state)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class Trigger<E extends DataObject<E> & UniqueidentifiedInterface<E>> extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Trigger.class.getName());
	private TriggerDefinition<E/* ,F,G */> triggerdefinition;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;

	/**
	 * creates the trigger
	 * 
	 * @param triggerdefinition definition of the trigger
	 * @param parentpayload     parent payload
	 */
	public Trigger(TriggerDefinition<E> triggerdefinition, DataObjectPayload parentpayload) {
		super(triggerdefinition, parentpayload);
		this.triggerdefinition = triggerdefinition;
	}

	/**
	 * post-processing after object insert
	 * 
	 * @param object the object
	 */
	public void postprocStoredobjectInsert(E object) {
		if (triggerdefinition.getTriggerCondition().executeOnInsert()) {

			processTrigger(object);
		}
	}

	/**
	 * post-processing after object update
	 * 
	 * @param object the object
	 */
	public void postprocUniqueidentifiedUpdate(E object) {
		if (triggerdefinition.getTriggerCondition().executeOnUpdate()) {
			processTrigger(object);
		}
	}

	/**
	 * post-processing after object delete
	 * 
	 * @param object the object
	 */
	public void preprocUniqueidentifiedDelete(E object) {
		if (triggerdefinition.getTriggerCondition().executeBeforeDelete()) {
			// the trigger is processed immediately as after that, the object will be
			// deleted and not yet available.
			processTriggerImmediately(object);
		}
	}

	/**
	 * post-processing of triggers by batch
	 * 
	 * @param objectbatch  object batch
	 * @param triggerbatch batch of the trigger property
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>> void preprocUniqueidentifiedDelete(
			E[] objectbatch, Trigger<E>[] triggerbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (triggerbatch == null)
			throw new RuntimeException("trigger batch is null");
		if (objectbatch.length != triggerbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with trigger batch length " + triggerbatch.length);
		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++)
				triggerbatch[i].preprocUniqueidentifiedDelete(objectbatch[i]);
		}
	}

	/**
	 * post-processing of the change state action
	 * 
	 * @param object   the object
	 * @param newstate new state
	 */
	public void postprocLifecycleChangestate(E object, @SuppressWarnings("rawtypes") ChoiceValue newstate) {
		if (triggerdefinition.getTriggerCondition().executeOnStateChange(newstate)) {
			processTrigger(object);
		}
	}

	/**
	 * post-processing of the change state action (massive)
	 * 
	 * @param objectbatch  batch of objects
	 * @param newstate     new state
	 * @param triggerbatch batch of trigger properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>> void postprocLifecycleChangestate(
			E[] objectbatch, @SuppressWarnings("rawtypes") ChoiceValue[] newstate, Trigger<E>[] triggerbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (triggerbatch == null)
			throw new RuntimeException("trigger batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");

		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new state batch length " + newstate.length);

		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++) {
				triggerbatch[i].postprocLifecycleChangestate(objectbatch[i], newstate[i]);
			}
		}
	}

	/**
	 * massive post-processing of update
	 * 
	 * @param objectbatch  object batch
	 * @param triggerbatch trigger batch
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>> void postprocUniqueidentifiedUpdate(
			E[] objectbatch, Trigger<E>[] triggerbatch) {
		logger.warning("----------- Not optimized for massive treatment --------------------");
		for (int i = 0; i < triggerbatch.length; i++)
			triggerbatch[i].postprocUniqueidentifiedUpdate(objectbatch[i]);
	}

	/**
	 * massive post-processing of insert
	 * 
	 * @param objectbatch  batch of object
	 * @param triggerbatch batch of trigger properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>> void postprocStoredobjectInsert(
			E[] objectbatch, Trigger<E>[] triggerbatch) {
		logger.warning("----------- Not optimized for massive treatment --------------------");
		for (int i = 0; i < triggerbatch.length; i++)
			triggerbatch[i].postprocStoredobjectInsert(objectbatch[i]);

	}

	/**
	 * Immediately executes the trigger
	 * 
	 * @param object
	 * @throws GalliumException
	 */
	private void processTriggerImmediately(E object) {
		CustomTriggerExecution<E> trigger = triggerdefinition.getTriggerExecution().generate();
		trigger.compute(object);
	}

	/**
	 * Stored the trigger in the list of triggers to execute at the end of a user
	 * action
	 * 
	 * @param object the object on which to execute the trigger
	 * @throws GalliumException
	 */
	private void processTrigger(E object) {
		CustomTriggerExecution<E> trigger = triggerdefinition.getTriggerExecution().generate();
		logger.info(" -----------------------process trigger for object " + object.dropToString() + " TRIGGER "
				+ trigger.getName() + " - " + trigger.getClass().toString());

		OLcServer.getServer()
				.addTriggerToList(new TriggerToExecute<E>(triggerdefinition.getTriggerExecution().generate(), object));
	}

	/**
	 * sets dependent property unique identified
	 * 
	 * @param uniqueidentified unique identified property for the given object
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

}
