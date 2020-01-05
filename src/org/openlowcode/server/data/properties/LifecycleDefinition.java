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

import java.util.ArrayList;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.constraints.RollupLifecycleOnParent;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;

/**
 * Definition for a property providing a lifecycle to the object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object holding the lifecycle
 * @param <F> the transition choice definition
 */
public class LifecycleDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectPropertyDefinition<E> {

	private F lifecyclehelper;
	private StringStoredField state;
	private TimestampStoredField finalstatetime;
	private RollupLifecycleOnParent<E, ?, F, ?> rolluponparent;
	private String statelabel = "State";
	private String nonreleasedlabel;

	/**
	 * gets the roll-up lifecycle on parent (if applicable)
	 * 
	 * @return the roll-up lifecycle on parent utility class
	 */
	public RollupLifecycleOnParent<E, ?, F, ?> getRolluponparent() {
		return rolluponparent;
	}

	/**
	 * gets the lifecycle choice definition
	 * 
	 * @return the lifecycle choice definition
	 */
	public F getLifecycleHelper() {
		return lifecyclehelper;
	}

	/**
	 * to specify a label different from 'state' (e.g. status) for the state
	 * 
	 * @param newlabel new label to use
	 */
	public void overridesStateLabel(String newlabel) {
		this.statelabel = newlabel;
	}

	/**
	 * the unreleased warning text that is displayed on object. It typically warns
	 * readers of limitations of objects that are still draft
	 * 
	 * @return the unreleased warning text if filled, null else
	 */
	public String getUnreleasedWarningText() {
		return this.nonreleasedlabel;
	}

	/**
	 * Creates the definition of a lifecycle property
	 * 
	 * @param parentobject     parent object definition
	 * @param lifecyclehelper  definition of the lifecycle
	 * @param nonreleasedlabel non-released warning label if required (can be null)
	 */
	public LifecycleDefinition(DataObjectDefinition<E> parentobject, F lifecyclehelper, String nonreleasedlabel) {
		super(parentobject, "LIFECYCLE");
		this.lifecyclehelper = lifecyclehelper;
		state = new StringStoredField("STATE", null, 64, new LifecycleDefaultValueGenerator(lifecyclehelper));
		this.addFieldSchema(state);
		StoredTableIndex stateindex = new StoredTableIndex("STATE");
		stateindex.addStoredFieldSchame(state);
		this.addIndex(stateindex);
		if (lifecyclehelper.IsFinalTransitionDefined()) {
			finalstatetime = new TimestampStoredField("FINALSTATETIME", null);
			this.addFieldSchema(finalstatetime);
		}
		this.nonreleasedlabel = nonreleasedlabel;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {

		FieldSchemaForDisplay<E>[] returnvalue;
		if (lifecyclehelper.IsFinalTransitionDefined()) {
			returnvalue = new FieldSchemaForDisplay[2];
			returnvalue[1] = new FieldSchemaForDisplay<E>("Final state time", "the time the final state was reached",
					finalstatetime, false, false, true, -110, 25, this.parentobject);
		} else {
			returnvalue = new FieldSchemaForDisplay[1];
		}
		returnvalue[0] = new FieldSchemaForDisplay<E>(statelabel,
				"state of object, will determine the types of operations possibles ", state, true, false, true,
				lifecyclehelper, 710, 25, this.parentobject);
		return returnvalue;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {

		return new Lifecycle<E, F>(this, parentpayload, lifecyclehelper);
	}

	/**
	 * sets the dependent definition unique identified
	 * 
	 * @param uniqueidentified dependent definition unique identified
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {

	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return new LifecycleFlatFileLoader<E, F>(objectdefinition, this, propertyextractor, lifecyclehelper);
	}

	/**
	 * sets up a helper to roll-up lifecycle on parent if required
	 * 
	 * @param rolluponparent the helper to roll-up lifecycle on parent
	 */
	public void setLifecycleRollUpOnParent(RollupLifecycleOnParent<E, ?, F, ?> rolluponparent) {
		this.rolluponparent = rolluponparent;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[] { "" };
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		ChoiceValue<F>[] values = lifecyclehelper.getChoiceValue();
		returntable[2] = values[0].getStorageCode();
		StringBuffer allvalues = new StringBuffer("\n");
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				allvalues.append(" \n");
			allvalues.append(values[i].getStorageCode());
		}

		returntable[3] = "one value amongst " + allvalues.toString();
		return returntable;
	}
}
