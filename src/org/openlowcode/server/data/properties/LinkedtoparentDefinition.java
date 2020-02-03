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
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectFieldDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.DisplayProfile;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.constraints.LinkedToDefaultParent;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * Definition of the property to link an object to a parent. When this property
 * is set, it is compulsory to link the object, from creation to an existing
 * parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> this data object
 * @param <F> the data object of the parent for linkedToParent relationship
 */
public class LinkedtoparentDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F>>
		extends DataObjectPropertyDefinition<E> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(LinkedtoparentDefinition.class.getName());
	private DataObjectDefinition<F> referenceobjectdefinition;
	private UniqueidentifiedDefinition<F> uniqueidentifiedforparentobjectforlink;
	private UniqueidentifiedDefinition<E> uniqueidentifiedforthisobject;
	private StringStoredField id;
	private DisplayProfile<E> displayprofilehideparentfields;
	private boolean subobject;
	private boolean showlistintable;
	private LinkedToDefaultParent<E, F> linkedtodefaultparentrule;

	/**
	 * gets the definition of the parent object
	 * 
	 * @return definition of the parent object
	 */
	public DataObjectDefinition<F> getReferenceObjectDefinition() {
		return this.referenceobjectdefinition;
	}

	/**
	 * sets a linked to default parent rule
	 * 
	 * @param linkedtodefaultparentrule new lunked to default parent rule
	 */
	public void addLinkedToDefaultParentRule(LinkedToDefaultParent<E, F> linkedtodefaultparentrule) {
		this.linkedtodefaultparentrule = linkedtodefaultparentrule;
	}

	/**
	 * get linked to default parent rule if exists
	 * 
	 * @return linked to default parent rule if exists
	 */
	public LinkedToDefaultParent<E, F> getLinkedToDefaultParentRule() {
		return this.linkedtodefaultparentrule;
	}

	/**
	 * creates a linked to parent definition
	 * 
	 * @param parentobject              object definition for the current (child)
	 *                                  object
	 * @param name                      name of the linked to parent property
	 *                                  (should be unique for the object)
	 * @param referenceobjectdefinition definition of the parent object for the
	 *                                  linked to parent
	 */
	public LinkedtoparentDefinition(DataObjectDefinition<E> parentobject, String name,
			DataObjectDefinition<F> referenceobjectdefinition) {
		super(parentobject, name);
		this.referenceobjectdefinition = referenceobjectdefinition;
		id = new StringStoredField(this.getName() + "ID", null, 200);
		this.addFieldSchema(id);
		// remove prefix LINKEDTOPARENTFOR
		StoredTableIndex parentidindex = new StoredTableIndex(this.getName().substring(17) + "PRID");
		parentidindex.addStoredFieldSchame(id);
		this.addIndex(parentidindex);

		this.subobject = false;
		this.showlistintable = false;
		this.displayprofilehideparentfields = parentobject.getDisplayProfileByName("HIDE" + name);
	}

	/**
	 * adds an index with first field the parent id, and other fields as specified
	 * here.
	 * 
	 * @param name   name of the index
	 * @param fields list of fields to add to the composite index after the
	 *               parentid.
	 */
	public void setCompositeIndex(String name, DataObjectFieldDefinition<E>[] fields) {
		StoredTableIndex thisindex = new StoredTableIndex(this.getName().substring(17) + name);
		thisindex.addStoredFieldSchame(id);
		for (int i = 0; i < fields.length; i++)
			thisindex.addStoredFieldSchame(fields[i].getMainStoredField());
		this.addIndex(thisindex);
	}

	/**
	 * @return true if child object is a subobject of the parent
	 */
	public boolean isSubobject() {
		return subobject;
	}

	/**
	 * @return true if shown as list in a table
	 */
	public boolean isShowlistintable() {
		return showlistintable;
	}

	/**
	 * @param showlistintable set shown as list in a table
	 */
	public void setSubObject(boolean showlistintable) {
		this.subobject = true;
		this.showlistintable = false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Linkedtoparent<E, F>(this, parentpayload, referenceobjectdefinition);
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();
		if (!subobject) {
			if (referenceobjectdefinition.hasProperty("NAMED")) {
				// adds an external field is the target object is named

				JoinQueryConditionDefinition<String> joincondition = referenceobjectdefinition
						.generateJoinQueryDefinition(this.parentobject.getTableschema(), id, "UNIQUEIDENTIFIED", "ID",
								this.getName(), new QueryOperatorEqual<String>());
				ExternalFieldSchema<?> externalfield = referenceobjectdefinition.generateExternalField(
						this.getName() + "NAME", "Parent " + this.referenceobjectdefinition.getLabel(),
						"Indicates the field the notes has been put in", "NAMED", "OBJECTNAME", joincondition,
						this.displayprofilehideparentfields, -50, 40);
				externalfieldlist.add(externalfield);
			}
			if (referenceobjectdefinition.hasProperty("NUMBERED")) {
				// adds an external field is the target object is named

				JoinQueryConditionDefinition<String> joincondition = referenceobjectdefinition
						.generateJoinQueryDefinition(this.parentobject.getTableschema(), id, "UNIQUEIDENTIFIED", "ID",
								this.getName(), new QueryOperatorEqual<String>());
				ExternalFieldSchema<?> externalfield = referenceobjectdefinition.generateExternalField(
						this.getName() + "NR", "Parent " + this.referenceobjectdefinition.getLabel() + " Number",
						"Indicates the field the notes has been put in", "NUMBERED", "NR", joincondition,
						this.displayprofilehideparentfields, -50, 40);
				externalfieldlist.add(externalfield);
			}
			if (referenceobjectdefinition.hasProperty("LOCATED")) {
				// adds an external field is the target object is named

				JoinQueryConditionDefinition<String> joincondition = referenceobjectdefinition
						.generateJoinQueryDefinition(this.parentobject.getTableschema(), id, "UNIQUEIDENTIFIED", "ID",
								this.getName(), new QueryOperatorEqual<String>());
				ExternalFieldSchema<?> externalfield = referenceobjectdefinition.generateExternalFieldInBottomNotes(
						this.getName() + "LOCATIONDOMAINID",
						"Parent " + this.referenceobjectdefinition.getLabel() + "Location Id",
						"Indicates the field the notes has been put in", "LOCATED", "LOCATIONDOMAINID", joincondition,
						this.displayprofilehideparentfields, -900, 40);
				externalfieldlist.add(externalfield);
			}

		}

		return externalfieldlist;

	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[1];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Parent Folder Id",
				"the technical id generated by the system of the parent", id, false, true, -60, 25, this.parentobject);
		return returnvalue;
	}

	/**
	 * gets the unique identified property for the parent object
	 * 
	 * @return unique identified property for the parent object
	 */
	public UniqueidentifiedDefinition<F> getGenericsParentobjectforlinkProperty() {
		return this.uniqueidentifiedforparentobjectforlink;
	}

	/**
	 * sets generic property unique identified for the parent object
	 * 
	 * @param uniqueidentifiedforparentobjectforlink generic property unique
	 *                                               identified for the parent
	 *                                               object
	 */
	public void setGenericsParentobjectforlinkProperty(
			UniqueidentifiedDefinition<F> uniqueidentifiedforparentobjectforlink) {
		this.uniqueidentifiedforparentobjectforlink = uniqueidentifiedforparentobjectforlink;

	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		boolean createmissing = false;
		if (columnattributes != null)
			if (columnattributes.length > 0)
				if ("CREATE".equals(columnattributes[0]))
					createmissing = true;
		return new LinkedToParentFlatFileLoader(objectdefinition, this, referenceobjectdefinition, propertyextractor,
				createmissing);
	}

	@Override
	public String[] getLoaderFieldList() {
		if (referenceobjectdefinition.getProperty("NUMBERED") != null) {
			return new String[] { "" };
		}

		return new String[] {};

	}

	@Override
	public String[] getLoaderFieldSample(String name) {

		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "ID1234";
		returntable[3] = "the number of the parent object.\n It is possible to specify the option CREATE, which will create the parent\n object "
				+ referenceobjectdefinition.getName()
				+ " with the given number.\n if CREATE is not specified and an inexistant number is provided,\n then an error is thrown for the line";
		return returntable;
	}

	/**
	 * gets unique identified property definition for this data object
	 * 
	 * @return unique identified property definition for this data object
	 */
	public UniqueidentifiedDefinition<E> getUniqueidentifiedForThisObject() {
		return this.uniqueidentifiedforthisobject;
	}

	/**
	 * sets unique identified property definition for this data object
	 * 
	 * @param uniqueidentified unique identified property definition for this data
	 *                         object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentifiedforthisobject = uniqueidentified;

	}
}
