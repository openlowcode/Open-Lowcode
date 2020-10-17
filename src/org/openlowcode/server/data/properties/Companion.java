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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.formula.DataUpdateTrigger;
import org.openlowcode.server.data.formula.TriggerLauncher;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.tools.misc.NamedList;


/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a> 
 * @since 1.13
 */
public class Companion<E extends DataObject<E> & HasidInterface<E>,F extends DataObject<F> & TypedInterface<F,G>,G extends FieldChoiceDefinition<G>> extends DataObjectProperty<E> {

	private Hasid<E> hasid;
	@SuppressWarnings("unused")
	private CompanionDefinition<E, F, G> companiondefinition;
	private static Logger logger = Logger.getLogger(Companion.class.getName());

	public Companion(CompanionDefinition<E,F,G> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.companiondefinition = definition;
	}
	
	public void setDependentPropertyHasid(Hasid<E> hasid) {
		this.hasid=hasid;
	}
	public void createtyped(E companionobject,F mainobject,ChoiceValue<G> type) {
		mainobject.settypebeforecreation(type);
		mainobject.insert();
		String id = mainobject.getId().getId();
		hasid.SetId(id);
		mainobject.update();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void insertcompanion(E companionobject,F mainobject) {
		if (mainobject.getId()==null) throw new RuntimeException("Main object not yet persisted");
		if (mainobject.getId().getId()==null) throw new RuntimeException("Main object not yet persisted");
		if (mainobject.getId().getId().length()==0) throw new RuntimeException("Main object not yet persisted");
		
		String id = mainobject.getId().getId();
		hasid.SetId(id);
		NamedList<DataUpdateTrigger<E>> triggers = companionobject.getDataUpdateTriggers();
		TriggerLauncher triggerlauncher = new TriggerLauncher(triggers);
		triggerlauncher.executeTriggerList(companionobject);
		this.parentpayload.insert();
	}
	
	public void updatetyped(E companionobject,F mainobject) {
		mainobject.update();
		update(companionobject);
	}
	
	private void update(E companionobject) {
		logger.severe(" ------------ Companion object log before update------------------");
		logger.severe("   "+companionobject.dropToString());
		logger.severe("------------------------------------------------------------------");
		
		QueryCondition objectuniversalcondition = definition.getParentObject().getUniversalQueryCondition(definition,
				null);
		QueryCondition uniqueidcondition = HasidQueryHelper.getIdQueryCondition(null,
				this.hasid.getId().getId(), definition.getParentObject());
		QueryCondition finalcondition = uniqueidcondition;
		if (objectuniversalcondition != null) {
			finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
		}
		NamedList<DataUpdateTrigger<E>> triggers = companionobject.getDataUpdateTriggers();
		TriggerLauncher<E> triggerlauncher = new TriggerLauncher<E>(triggers);
		triggerlauncher.executeTriggerList(companionobject);

		parentpayload.update(finalcondition);

	}
	

}
