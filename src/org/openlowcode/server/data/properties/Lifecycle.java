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


import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.server.runtime.OLcServer;

/**
 * A property providing a lifecycle to the object. A lifecycle determines what
 * is the current state / status of the object. State can be used as a base for
 * print-out, security access...
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object holding the lifecycle
 * @param <F> the transition choice definition
 */
public class Lifecycle<E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E,F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectProperty<E> {
	private StoredField<String> state;
	private LifecycleDefinition<E, F> lifecycledefinition;
	private Uniqueidentified<E> uniqueidentified;
	private F lifecyclehelper;
	private StoredField<Date> finalstatetime;
	private static Logger logger = Logger.getLogger(Lifecycle.class.getName());

	public Uniqueidentified<E> getUniqueidentified() {
		return uniqueidentified;
	}

	/**
	 * creates a lifecycle property for the object
	 * 
	 * @param definition      definition of the lifecycle property for the data
	 *                        object class
	 * @param parentpayload   parent data object payload
	 * @param lifecyclehelper transition choice category used for the lifecycle
	 */
	@SuppressWarnings("unchecked")
	public Lifecycle(LifecycleDefinition<E, F> definition, DataObjectPayload parentpayload, F lifecyclehelper) {
		super(definition, parentpayload);
		this.lifecycledefinition = definition;
		state = (StoredField<String>) this.field.lookupOnName("STATE");
		finalstatetime = (StoredField<Date>) this.field.lookupOnName("FINALSTATETIME");
		this.lifecyclehelper = lifecyclehelper;
		this.state.setPayload(lifecyclehelper.getDefaultChoice().getStorageCode());

	}

	/**
	 * @return the date the object reached for the last time a final state
	 */
	public Date getFinalstatetime() {
		return this.finalstatetime.getPayload();
	}

	/**
	 * @return gets the state as stored value
	 */
	public String getState() {
		return this.state.getPayload();
	}

	/**
	 * sets the state (without check) and persists the object
	 * 
	 * @param object data object
	 * @param state  new state stored value
	 */
	protected void setState(E object, String state) {
		String oldstate = this.state.getPayload();
		String niceoldstate = lifecycledefinition.getLifecycleHelper().showDisplay(oldstate);
		String nicenewstate = lifecycledefinition.getLifecycleHelper().showDisplay(state);
		this.state.setPayload(state);
		if (object.getDefinitionFromObject().hasProperty("ITERATED")) {
			@SuppressWarnings("unchecked")
			IteratedInterface<E> iterated = (IteratedInterface<E>) object;
			iterated.setupdatenote("Set State: from '" + niceoldstate + "' to '" + nicenewstate + "'");
		}
		object.update();
	}

	/**
	 * sets state without persistence to the object (without control either
	 * 
	 * @param state the state stored value
	 */
	protected void setStateWithoutPersistence(String state) {
		this.state.setPayload(state);
	}

	/**
	 * return true if the given state is final
	 * 
	 * @param newvalue state to analyze
	 * @return true if final, false if not
	 */
	public boolean isStateFinal(ChoiceValue<F> newvalue) {
		if (lifecyclehelper.isChoiceFinal(newvalue))
			return true;
		return false;
	}

	/**
	 * controls that the new state provided is valid, and performs the state change
	 * 
	 * @param object   data object
	 * @param newvalue new state
	 */
	@SuppressWarnings("unchecked")
	public void changestate(E object, ChoiceValue<F> newvalue) {
		if (this.getState().equals(newvalue.getStorageCode())) {
			String lastupdatestring = "";
			if (object instanceof UpdatelogInterface) {
				UpdatelogInterface<E> updatelog = (UpdatelogInterface<E>) object;
				Appuser lastupdater = Appuser.readone(updatelog.getUpdateuserid());
				lastupdatestring = ", last update at " + updatelog.getUpdatetime() + " by "
						+ (lastupdater != null ? lastupdater.getNr() + " - " + lastupdater.getName()
								: " Invalid user ID=" + updatelog.getId());
			}
			logger.warning("  ---> Requesting set state '" + newvalue.getStorageCode()
					+ "' although it is current state for object " + object.dropIdToString() + lastupdatestring);

			StackTraceElement[] currentstacktrace = Thread.currentThread().getStackTrace();
			for (int i = 0; i < currentstacktrace.length; i++)
				logger.warning("      + " + currentstacktrace[i]);
			logger.warning(
					"  -----------------------------------------------------------------------------------------------------------------------");
			return;
		}
		boolean valid  = false;
		if (lifecyclehelper.parseValueFromStorageCode(this.getState()).isAuthorizedTransitions(newvalue)) valid=true;
		if (!valid) if (OLcServer.getServer().isCurrentUserAdmin(object.getDefinitionFromObject().getModuleName())) {
			valid=true;
			logger.warning("Bypassing change state rule for object "+object.dropIdToString()+" old state = "+this.getState()+", new state = "+newvalue.getDisplayValue()+" as admin request");
		}
		if (valid) {

			if (lifecyclehelper.isChoiceFinal(newvalue))
				this.finalstatetime.setPayload(new Date());
			this.setState(object, newvalue.getStorageCode());
			if (lifecycledefinition.getRolluponparent() != null) {
				lifecycledefinition.getRolluponparent().rollupStateOnParent(object);
			}
		} else {
			throw new RuntimeException("Transition from state '" + this.getState() + "' to state '"
					+ newvalue.getStorageCode() + "' is not authorized for normal user");

		}
	}

	/**
	 * massive change state method. Optimized for batch treatment
	 * 
	 * @param objectbatch             object batch
	 * @param newstate                new state
	 * @param lifecyclearrayformethod corresponding lifecycle batch
	 */
	@SuppressWarnings("unchecked")
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E,F>, F extends TransitionFieldChoiceDefinition<F>> void changestate(
			E[] objectbatch, ChoiceValue<F>[] newstate, Lifecycle<E, F>[] lifecyclearrayformethod) {
		// ------------ object control
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (lifecyclearrayformethod == null)
			throw new RuntimeException("lifecycle batch is null");
		if (newstate == null)
			throw new RuntimeException("newstate batch is null");
		if (objectbatch.length != lifecyclearrayformethod.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with lifecycle batch length " + lifecyclearrayformethod.length);
		if (objectbatch.length != newstate.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with new state batch length " + newstate.length);

		if (objectbatch.length > 0) {
			// ------------------ state valid control
			// ---------------------------------------------
			for (int i = 0; i < objectbatch.length; i++) {
				
				boolean valid  = false;
				if (lifecyclearrayformethod[i].getState().equals(newstate[i].getStorageCode())) valid=true;
				if (!valid) if (OLcServer.getServer().isCurrentUserAdmin(objectbatch[i].getDefinitionFromObject().getModuleName())) {
					valid=true;
					logger.warning("Bypassing change state rule for object "+objectbatch[i].dropIdToString()+" old state = "+lifecyclearrayformethod[i].getState()+", new state = "+newstate[i].getDisplayValue()+" as admin request");
				}
				
				if (valid) {
					String lastupdatestring = "";
					if (objectbatch[i] instanceof UpdatelogInterface) {
						UpdatelogInterface<E> updatelog = (UpdatelogInterface<E>) objectbatch[i];
						Appuser lastupdater = Appuser.readone(updatelog.getUpdateuserid());
						lastupdatestring = ", last update at " + updatelog.getUpdatetime() + " by "
								+ (lastupdater != null ? lastupdater.getNr() + " - " + lastupdater.getName()
										: " Invalid user ID=" + updatelog.getId());
					}
					logger.warning("  ---> Requesting massive set state '" + lifecyclearrayformethod[i].getState()
							+ "' although it is current state for object " + objectbatch[i].dropIdToString()
							+ lastupdatestring);
					StackTraceElement[] currentstacktrace = Thread.currentThread().getStackTrace();
					for (int j = 0; j < currentstacktrace.length; j++)
						logger.warning("      + " + currentstacktrace[j]);
					logger.warning(
							"  -----------------------------------------------------------------------------------------------------------------------");

				} else if (!(lifecyclearrayformethod[i].lifecyclehelper
						.parseValueFromStorageCode(lifecyclearrayformethod[i].getState())
						.isAuthorizedTransitions(newstate[i])))
					throw new RuntimeException("Transition from state '" + lifecyclearrayformethod[i].getState()
							+ "' to state '" + newstate[i].getStorageCode() + "' is not authorized");
			}
			for (int i = 0; i < objectbatch.length; i++) {
				if (lifecyclearrayformethod[i].lifecyclehelper.isChoiceFinal(newstate[i]))
					lifecyclearrayformethod[i].finalstatetime.setPayload(new Date());
				String oldstate = lifecyclearrayformethod[i].state.getPayload();
				String niceoldstate = lifecyclearrayformethod[i].lifecycledefinition.getLifecycleHelper()
						.showDisplay(oldstate);
				String nicenewstate = lifecyclearrayformethod[i].lifecycledefinition.getLifecycleHelper()
						.showDisplay(newstate[i].getStorageCode());
				lifecyclearrayformethod[i].state.setPayload(newstate[i].getStorageCode());
				if (objectbatch[i].getDefinitionFromObject().hasProperty("ITERATED")) {
					IteratedInterface<E> iterated = (IteratedInterface<E>) objectbatch[i];
					iterated.setupdatenote("Set State: from '" + niceoldstate + "' to '" + nicenewstate + "'");
				}

			}

			objectbatch[0].getMassiveUpdate().update(objectbatch);
			for (int i = 0; i < objectbatch.length; i++) {
				if (lifecyclearrayformethod[i].lifecycledefinition.getRolluponparent() != null) {
					lifecyclearrayformethod[i].lifecycledefinition.getRolluponparent()
							.rollupStateOnParent(objectbatch[i]);
					logger.warning(
							" *-*-* Warning: Rollup state on parent used in batch mode. This is not yet optimized");
				}
			}
		}
	}

	/**
	 * forces the final state time
	 * 
	 * @param date new date for final state time
	 */
	void forceFinalStateTime(Date date) {
		this.finalstatetime.setPayload(date);
	}

	/**
	 * gets the current state and transition
	 * 
	 * @param object object to process
	 * @return the current state and authorized transition
	 */
	public ChoiceValue<F> getstateforchange(E object) {
		return lifecyclehelper.parseValueFromStorageCode(this.getState());
	}

	/**
	 * sets the dependent property Unique Identified for the object
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * pre-processing for the insertion of an object. puts default state for the
	 * lifecycle
	 * 
	 * @param object the object to process
	 */
	public void preprocStoredobjectInsert(E object) {
		changestate(object, lifecyclehelper.getDefaultChoice());
	}

}
