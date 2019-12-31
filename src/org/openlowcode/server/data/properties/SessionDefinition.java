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

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;

/**
 * Definition of the session property. A session is a portion of a Timeslot
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object of the session
 * @param <F> data object of the 'parent' Timeslot
 */
public class SessionDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & TimeslotInterface<F>>
		extends DataObjectPropertyDefinition<E> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SessionDefinition.class.getName());
	@SuppressWarnings("unused")
	private DataObjectDefinition<F> parenttimeslotdefinition;
	private TimestampStoredField starttime;
	private TimestampStoredField endtime;
	private IntegerStoredField sequence;
	private StringStoredField valid;
	@SuppressWarnings("unused")
	private TimeslotDefinition<F> parenttimeslotpropertydefinition;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentified;
	private LinkedtoparentDefinition<E, F> linkedtoparentdefinition;

	/**
	 * gets the dependent linked to parent definition
	 * 
	 * @return the dependent property linked to parent for the object
	 */
	public LinkedtoparentDefinition<E, F> getLinkedToParentDefinition() {
		return this.linkedtoparentdefinition;
	}

	/**
	 * creates the definition of the session property for an object
	 * 
	 * @param parentobject             definition of the parent object
	 * @param parenttimeslotdefinition definition of the parent timeslot object
	 */
	public SessionDefinition(DataObjectDefinition<E> parentobject, DataObjectDefinition<F> parenttimeslotdefinition) {
		super(parentobject, "SESSION");
		this.parenttimeslotdefinition = parenttimeslotdefinition;
		starttime = new TimestampStoredField("STARTTIME", null, new Date());
		this.addFieldSchema(starttime);
		endtime = new TimestampStoredField("ENDTIME", null, new Date());
		this.addFieldSchema(endtime);
		sequence = new IntegerStoredField("SEQUENCE", null, new Integer(1));
		this.addFieldSchema(sequence);
		valid = new StringStoredField("VALID", null, 16);
		this.addFieldSchema(valid);

	}

	/**
	 * gets the property of the related data object for timeslot
	 * 
	 * @param parenttimeslotpropertydefinition property of the related (F) parent
	 *                                         timeslot.
	 */
	public void setGenericsParenttimeslotProperty(TimeslotDefinition<F> parenttimeslotpropertydefinition) {
		this.parenttimeslotpropertydefinition = parenttimeslotpropertydefinition;
	}

	/**
	 * sets the dependent property unique-identified
	 * 
	 * @param uniqueidentified definition of the related unique identified property
	 *                         for the object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	/**
	 * sets the dependent property linked to parent (the parent timeslot)
	 * 
	 * @param linkedtoparentdefinition definition of the property linked to parent
	 */
	public void setDependentDefinitionLinkedtoparent(LinkedtoparentDefinition<E, F> linkedtoparentdefinition) {
		this.linkedtoparentdefinition = linkedtoparentdefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {

		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalues = new FieldSchemaForDisplay[4];

		returnvalues[0] = new FieldSchemaForDisplay<E>("Start Time", "Start of the session", starttime, false, false,
				605, 30, this.parentobject);
		returnvalues[1] = new FieldSchemaForDisplay<E>("End Time", "End of the session", endtime, false, false, 600, 30,
				this.parentobject);
		returnvalues[2] = new FieldSchemaForDisplay<E>("Sequence", "Session sequence", sequence, false, false, 595, 30,
				this.parentobject);
		returnvalues[3] = new FieldSchemaForDisplay<E>("Valid", "Is Session in valid timeslot", valid, false, false,
				false, BooleanChoiceDefinition.get(), 595, 30, this.parentobject);
		return returnvalues;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Session<E, F>(this, parentpayload);
	}

}
