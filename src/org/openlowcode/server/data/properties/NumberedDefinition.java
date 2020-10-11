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

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.constraints.AutonumberingRule;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * a property specifying a unique business identifier (number) for this data
 * object. This can be generated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 */
public class NumberedDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		DataObjectPropertyDefinition<E> {

	private StringStoredField nr;
	private CheckExistingNumber<E> numbercheck;
	UniqueidentifiedDefinition<E> dependentdefinitionuniqueidentified;
	private AutonumberingRule<E> autonumberingrule;
	private String numberlabel = "Number";
	private int numberfieldlength;

	/**
	 * 
	 * 
	 * @param overridescheckexistingnumber
	 */
	public void overridesCheckExistingNumber(CheckExistingNumber<E> overridescheckexistingnumber) {
		this.numbercheck = overridescheckexistingnumber;
	}

	/**
	 * Creates a numbered property with default numbered length (64 characters)
	 * 
	 * @param parentobject object the property is added on
	 */
	public NumberedDefinition(DataObjectDefinition<E> parentobject) {
		this(parentobject, 64);
	}

	/**
	 * Creates a numbered property with specified numbered length
	 * 
	 * @param parentobject      object the property is added on
	 * @param numberfieldlength specified number field length
	 */
	public NumberedDefinition(DataObjectDefinition<E> parentobject, int numberfieldlength) {
		super(parentobject, "NUMBERED");
		this.numberfieldlength = numberfieldlength;
		numbercheck = new CheckExistingNumber<E>(this);
		nr = new StringStoredField("NR", null, this.numberfieldlength);
		this.addFieldSchema(nr);
		this.autonumberingrule = null;
		StoredTableIndex nrindex = new StoredTableIndex("NR");
		nrindex.addStoredFieldSchema(nr);
		this.addIndex(nrindex);
	}

	public void overridesNumberLabel(String newlabel) {
		this.numberlabel = newlabel;
	}

	public boolean DoesNumberExist(String number, E object) {
		return numbercheck.exists(number, object);
	}

	public String getNumberLabel() {
		return this.numberlabel;
	}

	public AutonumberingRule<E> getAutonumberingRule() {
		return this.autonumberingrule;
	}

	public void setDependentDefinitionUniqueidentified(
			UniqueidentifiedDefinition<E> dependentdefinitionuniqueidentified) {
		this.dependentdefinitionuniqueidentified = dependentdefinitionuniqueidentified;
	}

	public UniqueidentifiedDefinition<E> getDependentDefinitionUniqueidentified() {
		return this.dependentdefinitionuniqueidentified;
	}

	public void addAutonumberingrule(AutonumberingRule<E> autonumberingrule) {
		this.autonumberingrule = autonumberingrule;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[1];
		if (this.autonumberingrule == null)
			returnvalue[0] = new FieldSchemaForDisplay<E>(this.numberlabel,
					"the unique business identified of this object", nr, true, false, true, 900, 20, this.parentobject);
		if (this.autonumberingrule != null) {
			if (this.autonumberingrule.orderedAsNumber()) {
				returnvalue[0] = new FieldSchemaForDisplay<E>(this.numberlabel,
						"the unique business identified of this object", nr, true, false, true, 900, 20,
						this.parentobject, this.autonumberingrule.getNumberOffset());
			} else {
				returnvalue[0] = new FieldSchemaForDisplay<E>(this.numberlabel,
						"the unique business identified of this object", nr, true, false, true, 900, 20,
						this.parentobject);
			}
		}
		return returnvalue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Numbered(this, parentpayload);
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {

		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columnattributes.length == 0)
			return new NumberedFlatFileLoader(objectdefinition, this, false, propertyextractor);
		if (columnattributes.length == 1)
			if (columnattributes[0].equals("UPDATE"))
				return new NumberedFlatFileLoader(objectdefinition, this, true, propertyextractor);
		throw new RuntimeException("attribute set '" + columnattributes[0] + "' not supported for numbered for object "
				+ objectdefinition.getName());
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[] { "" };
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		if (this.getAutonumberingRule() == null) {
			returntable[1] = "MANDATORY";
			returntable[3] = "Optional parameter: UPDATE this means that if row already exists, it will be updated. Else, if row exists, an error will be thrown.";

		} else {
			returntable[1] = "OPTIONAL";
			returntable[3] = "Note: this object has auto-numbering, if you do not specify number, a sequence will be generated.\nYou should be careful not to insert a value that may collide with the sequence.\nOptional parameter: UPDATE this means that if row already exists, it will be updated. Else, if row exists, an error will be thrown.";

		}
		returntable[2] = "A230505";

		returntable[3] = "Note: this object has auto-numbering, if you do not specify number, a sequence will be generated.\nYou should be careful not to insert a value that may collide with the sequence.\nOptional parameter: UPDATE this means that if row already exists, it will be updated. Else, if row exists, an error will be thrown.";
		return returntable;
	}

}
