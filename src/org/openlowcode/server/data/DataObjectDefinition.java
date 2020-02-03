/********************************************************************************
* Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0 .
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package org.openlowcode.server.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.misc.OrderedList;
import org.openlowcode.tools.misc.StringDecoder;
import org.openlowcode.tools.misc.Triple;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.DataObjectPropertyDefinition.ExternalFieldByJoinQuery;
import org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.TransientPropertiesForLoader;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.FieldSchema;
import org.openlowcode.server.data.storage.JoinQueryCondition;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.PersistentStorage;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperator;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.data.storage.TableAlias.FieldSelectionAlias;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * This class defines a data object. It lists all fields and properties that are
 * included in this object. There is one class per type of object. <b>In Open
 * Lowcode, subclasses of dataobject are auto-generated. Creating manually a
 * subclass of data-object can create unforecasted issues, and is
 * discouraged</b>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class DataObjectDefinition<E extends DataObject<E>> extends Named {
	private static Logger logger = Logger.getLogger(DataObjectDefinition.class.getCanonicalName());
	private NamedList<DataObjectFieldDefinition<E>> fielddeflist;
	private NamedList<DataObjectPropertyDefinition<E>> propertydeflist;
	private NamedList<DataObjectPropertyDefinition<E>> propertydeflistincludinglegacy;
	private NamedList<DisplayProfile<E>> displayprofiles;
	private NamedList<SMultiFieldConstraint> fieldconstraints;
	private StoredTableSchema tableschema;
	private String label;
	private String modulecode;
	private HashMap<String, String> loaderalias;
	private ArrayList<String> aliasesorderedlist;
	private String preferedspreadsheettabname = null;

	/**
	 * @return the prefered table row height
	 */
	public int getPreferedTableRowHeight() {
		return 0;
	}

	/**
	 * @return the label in the prefered language of the application
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return a non null String with the full path for a column if the given alias
	 *         is defined;
	 */
	public String getLoaderAlias(String alias) {
		return loaderalias.get(alias);
	}

	/**
	 * @param alias    the alias for data loading
	 * @param fullpath the full path for data loading
	 */
	protected void setAlias(String alias, String fullpath) {
		loaderalias.put(alias, fullpath);
		aliasesorderedlist.add(alias);
	}

	/**
	 * @return the number of loader aliases declared
	 */
	public int getAliasNumber() {
		return aliasesorderedlist.size();
	}

	/**
	 * @param index
	 * @return
	 */
	public String getAliasat(int index) {
		return aliasesorderedlist.get(index);
	}

	/**
	 * @param preferedspreadsheettabname
	 */
	public void setPreferedSpreadsheetTab(String preferedspreadsheettabname) {
		this.preferedspreadsheettabname = preferedspreadsheettabname;
	}

	/**
	 * @return
	 */
	public String getPreferedSpreadsheetTab() {
		return this.preferedspreadsheettabname;
	}

	/**
	 * @return
	 */
	public int getFieldNumber() {
		return this.fielddeflist.getSize();
	}

	/**
	 * @param index
	 * @return
	 */
	public DataObjectFieldDefinition<E> getFieldAt(int index) {
		return this.fielddeflist.get(index);
	}

	/**
	 * @param transientproperties
	 * @param columndefinitionelements
	 * @param locale
	 * @return
	 */
	public CustomloaderHelper<E> getCustomLoaderHelper(TransientPropertiesForLoader<E> transientproperties,
			String[] columndefinitionelements, ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columndefinitionelements == null)
			throw new RuntimeException("Column definition element is null");
		if (columndefinitionelements.length == 0)
			throw new RuntimeException("Column definition element table has zero length");
		if (columndefinitionelements[0].trim().length() == 0)
			throw new RuntimeException("Column definition element zero in table has zero length");
		String elementname = columndefinitionelements[0].trim().toUpperCase();
		String[] columnattributes = new String[columndefinitionelements.length - 1];
		for (int i = 0; i < columndefinitionelements.length - 1; i++) {
			columnattributes[i] = columndefinitionelements[i + 1];
		}
		// First check if property is transient
		return transientproperties.getTransientColumnGenerator(elementname);
	}

	/**
	 * @param transientproperties
	 * @param columndefinitionelements
	 * @param locale
	 * @return
	 */
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(TransientPropertiesForLoader<E> transientproperties,
			String[] columndefinitionelements, ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columndefinitionelements == null)
			throw new RuntimeException("Column definition element is null");
		if (columndefinitionelements.length == 0)
			throw new RuntimeException("Column definition element table has zero length");
		if (columndefinitionelements[0].trim().length() == 0)
			throw new RuntimeException("Column definition element zero in table has zero length");
		String elementname = columndefinitionelements[0].trim().toUpperCase();
		String[] columnattributes = new String[columndefinitionelements.length - 1];
		for (int i = 0; i < columndefinitionelements.length - 1; i++) {
			columnattributes[i] = columndefinitionelements[i + 1];
		}
		// First check if property is transient
		CustomloaderHelper<E> transientcolumngenerator = transientproperties.getTransientColumnGenerator(elementname);
		if (transientcolumngenerator != null)
			return transientcolumngenerator.getFlatFileLoaderColumn(this, columnattributes, locale);

		// Else, look for normal loaders
		DataObjectPropertyDefinition<E> relevantproperty = propertydeflistincludinglegacy.lookupOnName(elementname);

		if (relevantproperty != null) {
			return relevantproperty.getFlatFileLoaderColumn(this, columnattributes, new PropertyExtractor<E>() {

				@SuppressWarnings("unchecked")
				@Override
				public DataObjectProperty<E> extract(E dataobject) {
					return dataobject.payload.lookupPropertyOnName(relevantproperty.getName());
				}

			}, locale);
		} else {
			DataObjectFieldDefinition<E> relevantfield = fielddeflist.lookupOnName(elementname);

			if (relevantfield != null)
				return relevantfield.getFlatFileLoaderColumn(this, columnattributes, locale);
		}
		return null;

	}

	/**
	 * @param excludeddefinition the property requesting the universal condition
	 * @return a non-null query condition
	 */
	public QueryCondition getUniversalQueryCondition(DataObjectPropertyDefinition<E> excludeddefinition, String alias) {
		AndQueryCondition andcondition = new AndQueryCondition();
		boolean found = false;

		for (int i = 0; i < propertydeflistincludinglegacy.getSize(); i++) {
			DataObjectPropertyDefinition<E> thispropertydef = propertydeflistincludinglegacy.get(i);
			if (thispropertydef != excludeddefinition) {
				QueryCondition thispropertycondition = thispropertydef.getUniversalQueryCondition(alias);
				if (thispropertycondition != null) {
					andcondition.addCondition(thispropertycondition);
					found = true;
				}
			}
		}
		if (found)
			return andcondition;
		return null;
	}

	/**
	 * init all the fields of this DataObjectDefinition
	 */
	public abstract void initFields();

	/**
	 * init all the properties of this DataObjectDefinition
	 */
	public abstract void initProperties();

	/**
	 * initiates the property generic links
	 */
	public abstract void initPropertyGenericLinks();

	/**
	 * @param row   a row from the database
	 * @param alias the alias of the obect in the query
	 * @return a data object
	 */
	public abstract E generateFromRow(Row row, TableAlias alias);

	/**
	 * @return an empty array of the precise data object type. This is very useful
	 *         to generate an array of objects from an array list
	 */
	public abstract E[] generateArrayTemplate();

	/**
	 * @return generates a data object id array template.
	 */
	public abstract DataObjectId<E>[] generateIdArrayTemplate();

	/**
	 * @return a blank object
	 */
	public abstract E generateBlank();

	/**
	 * @return the table schema of this object
	 */
	public StoredTableSchema getTableschema() {
		return tableschema;
	}

	/**
	 * generates a join query definition
	 * 
	 * @param maintable       the main table to join this object into
	 * @param maintablefield  the field to use on main table
	 * @param propertyname    name of the property to join in
	 * @param propertyfield   field of the property
	 * @param sidetablesuffix suffix to add to this table
	 * @param operator        typically equals for a foreign key
	 * @return a join query condition
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <Z extends Object> JoinQueryConditionDefinition<Z> generateJoinQueryDefinition(StoredTableSchema maintable,
			StoredFieldSchema<Z> maintablefield, String propertyname, String propertyfield, String sidetablesuffix,
			QueryOperator<Z> operator) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield joinquerycondition " + maintablefield.getName()
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema<?>) field;
		return new JoinQueryConditionDefinition(maintable, maintablefield, tableschema, sidetablesuffix, storedfield,
				this, operator);
	}

	/**
	 * checks if the property with the given name exists
	 * 
	 * @param propertyname the name to check
	 * @return true if the property also exists, false else
	 */
	public boolean hasProperty(String propertyname) {
		if (propertydeflist.lookupOnName(propertyname) != null)
			return true;
		return false;
	}

	/**
	 * returns the property if it exists
	 * 
	 * @param propertyname the name to check
	 * @return the property if it exists, null else
	 */
	public DataObjectPropertyDefinition<E> getProperty(String propertyname) {
		return propertydeflist.lookupOnName(propertyname);
	}

	/**
	 * generates an external field. This is typically a field from a joint data
	 * object displayed on this object
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property in the side object
	 * @param propertyfield                name of the field in the side object
	 * @param joinqueryconditiondefinition join to do between this object and the
	 *                                     other object
	 * @return
	 */
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String propertyname, String propertyfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			int priority, int displaycolumn) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<E> storedfield = (StoredFieldSchema<E>) field;
		ExternalFieldSchema<E> thisfieldschema = new ExternalFieldSchema<E>(externalfieldname, displayname, tooltip,
				tableschema, storedfield, joinqueryconditiondefinition, priority, displaycolumn);
		return thisfieldschema;
	}

	/**
	 * generates an external field. This is typically a field from a joint data
	 * object displayed on this object.
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param fieldname                    name of the field in the side object
	 * @param joinqueryconditiondefinition join query condition with the side table
	 * @param hideifprofileset             condition for showing fields
	 * @param priority                     priority of the external field
	 * @param displaycolumn                size of the column for table
	 * @return the generated external field
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String fieldname, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn) {
		DataObjectFieldDefinition<E> fielddef = this.fielddeflist.lookupOnName(fieldname);
		if (fielddef == null)
			throw new RuntimeException("did not find field with name = " + fieldname + ", available list = "
					+ fielddeflist.dropNameList());
		FieldSchema<?> field = fielddef.getFieldSchema(0); // always get first field schema
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema<?>) field;
		ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, storedfield, joinqueryconditiondefinition, hideifprofileset, priority, displaycolumn);
		return thisfieldschema;
	}

	/**
	 * this method generates an external field compiling all the fields that are
	 * shown in the title.
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param joinqueryconditiondefinition join query condition with the side table
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return the external field
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExternalFieldSchema<?> generateExternalFieldFromTitle(String externalfieldname, String displayname,
			String tooltip, JoinQueryConditionDefinition<?> joinqueryconditiondefinition, int priority,
			int displaycolumn) {
		ExternalFieldSchema<?> extfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				this.tableschema, joinqueryconditiondefinition, priority, displaycolumn);
		// deal with properties
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = this.propertydeflist.get(i);
			FieldSchemaForDisplay<E>[] fieldschemafordisplay = thisproperty.setFieldSchemaToDisplay();
			if (fieldschemafordisplay != null)
				for (int j = 0; j < fieldschemafordisplay.length; j++) {
					FieldSchemaForDisplay<E> thisfieldschemafordisplay = fieldschemafordisplay[j];
					if (thisfieldschemafordisplay.isShowintitle()) {
						// only takes first field
						if (thisfieldschemafordisplay.getDataObjectFieldDefinition().getFieldSchemaNumber() == 0)
							throw new RuntimeException(
									"no field schema number found for " + thisfieldschemafordisplay.getDisplay());
						if (thisfieldschemafordisplay.getDataObjectFieldDefinition().getFieldSchema(0) == null)
							throw new RuntimeException("field schema number 0 is empty found for "
									+ thisfieldschemafordisplay.getDataObjectFieldDefinition().getFieldSchema(0));
						extfieldschema.addStoredField(
								thisfieldschemafordisplay.getDataObjectFieldDefinition().getFieldSchema(0));

					}
				}
		}
		// deal with fields
		for (int i = 0; i < this.fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfield = this.fielddeflist.get(i);
			if (thisfield.isShowintitle()) {
				if (thisfield.getFieldSchema(0) == null)
					throw new RuntimeException("field schema 0 is null for field " + thisfield.getDisplayname());
				extfieldschema.addStoredField(thisfield.getFieldSchema(0));

			}
		}
		return extfieldschema;
	}

	/**
	 * generates an external field from a joint table for a property formatting with
	 * the choice value
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property to get the field
	 *                                     from
	 * @param propertyfield                name of the field used to get data
	 * @param fieldchoice                  the choice to use for formatting
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param hideifprofileset             profile that may hide some fields. E.g.
	 *                                     on links,when showing a table in the left
	 *                                     objects, left object fields should not be
	 *                                     shown
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return the external field
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String propertyname, String propertyfield, FieldChoiceDefinition<?> fieldchoice,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, DisplayProfile<?> hideifprofileset,
			int priority, int displaycolumn) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema) field;
		ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, storedfield, fieldchoice, joinqueryconditiondefinition, hideifprofileset, priority,
				displaycolumn);
		return thisfieldschema;
	}

	/**
	 * generates an external field for a property
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property to get the field
	 *                                     from
	 * @param propertyfield                name of the field used to get data
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param hideifprofileset             profile that may hide some fields. E.g.
	 *                                     on links,when showing a table in the left
	 *                                     objects, left object fields should not be
	 *                                     shown
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String propertyname, String propertyfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema) field;
		ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, storedfield, joinqueryconditiondefinition, hideifprofileset, priority, displaycolumn);
		return thisfieldschema;
	}

	/**
	 * general external field from another object field
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param fieldname                    name of the origin field
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param hideifprofileset             profile that may hide some fields. E.g.
	 *                                     on links,when showing a table in the left
	 *                                     objects, left object fields should not be
	 *                                     shown
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return the external field
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ExternalFieldSchema<?> generateExternalFieldFromField(String externalfieldname, String displayname,
			String tooltip, String fieldname, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn) {
		DataObjectFieldDefinition<E> fielddefinition = this.fielddeflist.lookupOnName(fieldname);
		if (fielddefinition == null)
			throw new RuntimeException("did not find field with name = '" + fieldname + "', available list = "
					+ fielddeflist.dropNameList());
		StoredFieldSchema<?> field = fielddefinition.getMainStoredField();
		if (field == null)
			throw new RuntimeException("Did not find main field for field with name = '" + fieldname + "', class  = "
					+ fielddefinition.getClass().toString());
		if (fielddefinition instanceof ChoiceDataObjectFieldDefinition) {
			ChoiceDataObjectFieldDefinition<?, E> choicefielddefinition = (ChoiceDataObjectFieldDefinition<?, E>) fielddefinition;
			ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
					tableschema, field, choicefielddefinition.getFieldchoicedefinition(), joinqueryconditiondefinition,
					hideifprofileset, priority, displaycolumn);
			return thisfieldschema;
		}
		if (fielddefinition instanceof StringDataObjectFieldDefinition) {
			StringDataObjectFieldDefinition stringfielddefinition = (StringDataObjectFieldDefinition) fielddefinition;
			boolean richtext = stringfielddefinition.isRichtextedit();
			logger.finer(" creating external field schema with richtext = " + richtext);
			ExternalFieldSchema thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
					tableschema, field, richtext, joinqueryconditiondefinition, hideifprofileset, priority,
					displaycolumn);
			return thisfieldschema;
		}
		ExternalFieldSchema thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, field, joinqueryconditiondefinition, hideifprofileset, priority, displaycolumn);
		return thisfieldschema;
	}

	/**
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property to get the field
	 *                                     from
	 * @param propertyfield                name of the field used to get data
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param hideifprofileset             profile that may hide some fields. E.g.
	 *                                     on links,when showing a table in the left
	 *                                     objects, left object fields should not be
	 *                                     shown
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return
	 */
	public ExternalFieldSchema<?> generateExternalFieldInBottomNotes(String externalfieldname, String displayname,
			String tooltip, String propertyname, String propertyfield,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, DisplayProfile<?> hideifprofileset,
			int priority, int displaycolumn) {
		ExternalFieldSchema<?> thisfieldschema = generateExternalField(externalfieldname, displayname, tooltip,
				propertyname, propertyfield, joinqueryconditiondefinition, hideifprofileset, priority, displaycolumn);
		thisfieldschema.setDisplayInBottomNotes();
		return thisfieldschema;
	}

	/**
	 * generates a field that is ordered as a number
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property to get the field
	 *                                     from
	 * @param propertyfield                name of the field used to get data
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param hideifprofileset             profile that may hide some fields. E.g.
	 *                                     on links,when showing a table in the left
	 *                                     objects, left object fields should not be
	 *                                     shown
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @param orderedasnumber              true if the field should be ordered as
	 *                                     number
	 * @param numberoffset                 generates number from ordering at the
	 *                                     given offset (e.g. 'NR-1234' with offset
	 *                                     3 will given 1234)
	 * @return the external field
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String propertyname, String propertyfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn, boolean orderedasnumber,
			int numberoffset) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema) field;
		ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, storedfield, joinqueryconditiondefinition, hideifprofileset, priority, displaycolumn,
				orderedasnumber, numberoffset);

		return thisfieldschema;
	}

	/**
	 * generates an external field from property to be formatted as a choice
	 * 
	 * @param externalfieldname            name of the field
	 * @param displayname                  display of the field in the prefered
	 *                                     language
	 * @param tooltip                      tooltip for this field displayed in
	 *                                     rollover
	 * @param propertyname                 name of the property to get the field
	 *                                     from
	 * @param propertyfield                name of the field used to get data
	 * @param fieldchoice                  the choice to use for formatting
	 * @param joinqueryconditiondefinition query condition to get data from
	 * @param priority                     priority of the external field (fields
	 *                                     are shown per priority)
	 * @param displaycolumn                size of the column display in a table
	 * @return the external field
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ExternalFieldSchema<?> generateExternalField(String externalfieldname, String displayname, String tooltip,
			String propertyname, String propertyfield, FieldChoiceDefinition<?> fieldchoice,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, int priority, int displaycolumn) {
		DataObjectPropertyDefinition<E> propertydefinition = propertydeflist.lookupOnName(propertyname);
		if (propertydefinition == null)
			throw new RuntimeException("did not find property with name = '" + propertyname + "', available list = "
					+ propertydeflist.dropNameList());
		FieldSchema<?> field = propertydefinition.getFieldSchemaByName(propertyfield);
		if (field == null)
			throw new RuntimeException("did not find field with name '" + propertyfield + ", , available list = "
					+ propertydefinition.dropfieldnamelist());
		if (!(field instanceof StoredFieldSchema))
			throw new RuntimeException("definition of externalfield " + externalfieldname
					+ " is referencing another external field : " + field);
		StoredFieldSchema<?> storedfield = (StoredFieldSchema) field;
		ExternalFieldSchema<?> thisfieldschema = new ExternalFieldSchema(externalfieldname, displayname, tooltip,
				tableschema, storedfield, fieldchoice, joinqueryconditiondefinition, priority, displaycolumn);
		return thisfieldschema;
	}

	/**
	 * gets the display profile given the name
	 * 
	 * @param name name of the display profile
	 * @return the display profile or null if it does not exist
	 */
	public DisplayProfile<E> getDisplayProfileByName(String name) {
		return displayprofiles.lookupOnName(name);
	}

	/**
	 * creates a blank data object definition and calls the methods to fill the
	 * fields and properties
	 * 
	 * @param name       name of the data object class
	 * @param modulecode code of the parent module
	 * @param label      the display label of the object in English
	 */
	public DataObjectDefinition(String name, String modulecode, String label) {
		super(name);
		this.modulecode = modulecode;
		this.label = label;
		fielddeflist = new NamedList<DataObjectFieldDefinition<E>>();
		propertydeflist = new NamedList<DataObjectPropertyDefinition<E>>();
		propertydeflistincludinglegacy = new NamedList<DataObjectPropertyDefinition<E>>();
		displayprofiles = new NamedList<DisplayProfile<E>>();
		fieldconstraints = new NamedList<SMultiFieldConstraint>();
		loaderalias = new HashMap<String, String>();
		aliasesorderedlist = new ArrayList<String>();
	}

	/**
	 * adds a multifield constraint to this object. A multifield constraint will
	 * restrain a set of fields possible values. Simple example is to have two
	 * fields 'country' and 'city', the constraint will ensure you will choose a
	 * consistent country and city.
	 * 
	 * @param constraint the constraint to add
	 */
	public void addMultiFieldConstraint(SMultiFieldConstraint constraint) {
		this.fieldconstraints.add(constraint);
	}

	/**
	 * This method checks on the server side that multi-field constraints are
	 * respected for the object. The main use-case known is for flat file loading.
	 * If invalid situation is encountered, a runtime exception is thrown
	 * 
	 */
	@SuppressWarnings("rawtypes")
	public void checkMultiFieldConstraints(E object) {
		for (int i = 0; i < this.fieldconstraints.getSize(); i++) {
			SMultiFieldConstraint thisconstraint = this.fieldconstraints.get(i);
			ArrayList<String> thisobjectvalues = new ArrayList<String>();
			ArrayList<String> constraintfieldsequence = thisconstraint.getFieldSequence();
			for (int j = 0; j < constraintfieldsequence.size(); j++) {
				SimpleDataElt thisfield = object.getFieldList().lookupOnName(constraintfieldsequence.get(j));
				String value = "";
				if (thisfield instanceof ChoiceDataElt) {
					ChoiceDataElt<?> thischoicefield = (ChoiceDataElt) thisfield;
					value = thischoicefield.getStoredValue();
				} else {
					throw new RuntimeException("Multi-Field constraint check on server only supports choice value.");
				}
				thisobjectvalues.add(value);
			}
			thisconstraint.checkCombination(thisobjectvalues);
		}
	}

	/**
	 * this is a third step in object initialization for the external field in
	 * properties and the triggers for calculated fields
	 */
	public void setExternalFields() {
		initPropertyGenericLinks();
		for (int i = 0; i < propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thispropertydef = propertydeflist.get(i);
			thispropertydef.initiateExternalFieldSchema();
		}
		// initialize triggers
		this.setCalculatedFieldTriggers();
	}

	/**
	 * initiates the calculated fields triggers
	 */
	protected abstract void setCalculatedFieldTriggers();

	/**
	 * this is separated from the constructor to avoid loops when property reference
	 * other data definitions
	 */
	public void setFieldsAndAttributes() {

		initFields();
		initProperties();

		this.tableschema = new StoredTableSchema(this.modulecode + "_" + this.getName());
		for (int i = 0; i < fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfielddef = fielddeflist.get(i);
			for (int j = 0; j < thisfielddef.getFieldSchemaNumber(); j++) {
				this.tableschema.addField(thisfielddef.getFieldSchema(j));
			}
			logger.fine("for table " + this.tableschema.getName() + " processing " + thisfielddef.getName()
					+ " indexnumber = " + thisfielddef.getIndexNumber());
			for (int j = 0; j < thisfielddef.getIndexNumber(); j++) {
				this.tableschema.addIndex(thisfielddef.getIndexAt(j));
			}
		}
		for (int i = 0; i < propertydeflistincludinglegacy.getSize(); i++) {
			DataObjectPropertyDefinition<E> thispropertydef = propertydeflistincludinglegacy.get(i);
			for (int j = 0; j < thispropertydef.getFieldSchemaNumber(); j++) {
				FieldSchema<?> thisfield = thispropertydef.getFieldSchema(j);
				if (thisfield instanceof StoredFieldSchema) {
					StoredFieldSchema<?> thistoredfield = (StoredFieldSchema<?>) thisfield;
					this.tableschema.addField(thistoredfield);
				}

			}
			logger.fine("for table " + this.tableschema.getName() + " processing " + thispropertydef.getName()
					+ " indexnumber = " + thispropertydef.getIndexNumber());
			for (int j = 0; j < thispropertydef.getIndexNumber(); j++) {

				this.tableschema.addIndex(thispropertydef.getIndexAt(j));
			}

		}
	}

	/**
	 * this method should only be called initSimpleFields
	 * 
	 * @param simplefielddef the field to add
	 */
	public void addFieldDefinition(DataObjectFieldDefinition<E> simplefielddef) {
		fielddeflist.add(simplefielddef);
	}

	/**
	 * adds a property to the object definition.
	 * 
	 * @param propertydef the property definition
	 */
	public void addPropertyDefinition(DataObjectPropertyDefinition<E> propertydef) {
		propertydeflist.add(propertydef);
		propertydeflistincludinglegacy.add(propertydef);
	}

	/**
	 * adds a property as legacy. This means the data can still be accessed, but it
	 * is not active anymore. Typically used for internal migrations
	 * 
	 * @param propertydef the definition of the property
	 */
	public void addPropertyDefinitionAsLegacy(DataObjectPropertyDefinition<E> propertydef) {

		propertydeflistincludinglegacy.add(propertydef);
	}

	/**
	 * Initiates a blank payload for the object
	 * 
	 * @return an initiated payload for the object
	 */
	@SuppressWarnings("rawtypes")
	public DataObjectPayload initiateBlankPayload() {
		DataObjectPayload thispayload = new DataObjectPayload(tableschema);
		StringBuffer previousfield = new StringBuffer();
		for (int i = 0; i < fielddeflist.getSize(); i++) {
			if (i > 0)
				previousfield.append(',');
			DataObjectFieldDefinition<E> thisfielddef = fielddeflist.get(i);
			if (thisfielddef == null)
				throw new RuntimeException("field index " + i + " is null for object " + label + ", previous field = "
						+ previousfield.toString());
			if (thisfielddef.getName() == null)
				throw new RuntimeException(
						"field index " + i + " is null of class " + thisfielddef.getClass().toString() + "  for object "
								+ label + ", previous field = " + previousfield.toString());
			if (thisfielddef.getName().equals(""))
				throw new RuntimeException(
						"field index " + i + " is empty of class " + thisfielddef.getClass().toString() + " for object "
								+ label + ", previous field = " + previousfield.toString());

			previousfield.append(thisfielddef.getName());
			DataObjectField<?, E> fieldinstance = (DataObjectField<?, E>) thisfielddef
					.initiateFieldInstance(thispayload);
			if (fieldinstance == null)
				throw new RuntimeException("could not initiate field instance for field index " + i + " of class "
						+ thisfielddef.getClass().toString() + " for object " + label + ", previous field = "
						+ previousfield.toString());
			thispayload.addField((DataObjectField<?, E>) thisfielddef.initiateFieldInstance(thispayload));

		}

		for (int i = 0; i < propertydeflistincludinglegacy.getSize(); i++) {
			DataObjectPropertyDefinition<E> thispropertydef = propertydeflistincludinglegacy.get(i);

			thispayload.addProperty((DataObjectProperty) thispropertydef.initiateFieldInstance(thispayload));

		}
		return thispayload;
	}

	/**
	 * @param alias the alias in a query
	 * @return an alias for this object to be used in a query
	 */
	public TableAlias getAlias(String alias) {
		if (alias == null)
			return null;
		return new TableAlias(tableschema, alias);
	}

	/**
	 * ensures the persistent storage is ready to store the corresponding objects.
	 * This typically means creating tables and indexes on a relational database
	 * 
	 */
	public void updatePersistenceStorage() {
		if (this.hasProperty("STOREDOBJECT")) {
			PersistentStorage storage = PersistenceGateway.getStorage();
			if (!storage.DoesObjectExist(tableschema)) {
				// object does not exist, create
				logger.warning("PERSISTENCE: adding table " + tableschema.getName() + " with "
						+ tableschema.getIndexSize() + " indexes");
				storage.createObject(tableschema);
				for (int i = 0; i < tableschema.getIndexSize(); i++) {
					StoredTableIndex thisindex = tableschema.getIndex(i);
					storage.createSearchIndex(thisindex.getFullName(), thisindex.getParent(), thisindex.getAllFields(),
							false);
				}
			} else {
				// object exists, check fields
				for (int i = 0; i < tableschema.getStoredFieldNumber(); i++) {
					if (!storage.DoesFieldExist(tableschema, i)) {
						// create missing fields
						logger.warning("PERSISTENCE: adding field " + tableschema.getStoredField(i).getName()
								+ " in table " + tableschema.getName());
						storage.createField(tableschema, i);
					}
				}
				for (int i = 0; i < tableschema.getIndexSize(); i++) {
					StoredTableIndex thisindex = tableschema.getIndex(i);
					int currentstatus = storage.DoesIndexExist(thisindex.getParent(), thisindex.getAllFields(),
							thisindex.getFullName());
					if (currentstatus != PersistentStorage.INDEX_OK) {
						if (currentstatus == PersistentStorage.INDEX_DIFFERENT) {
							logger.warning("PERSISTENCE : updating index " + thisindex.getFullName() + " in table "
									+ tableschema.getName());
							storage.dropIndex(thisindex.getFullName());
							storage.createSearchIndex(thisindex.getFullName(), tableschema, thisindex.getAllFields(),
									false);
						} else {
							logger.warning("PERSISTENCE : adding index " + thisindex.getFullName() + " in table "
									+ tableschema.getName());
							storage.createSearchIndex(thisindex.getFullName(), tableschema, thisindex.getAllFields(),
									false);
						}
					}
				}
			}

			PersistenceGateway.checkinStorage(storage);

		} else {
			logger.info("PERSISTENCE : Object " + this.getName()
					+ " is not created in database as it is not a stored object.");
		}
	}

	/**
	 * @param sampleobject a sample object
	 * @return a list of triples including
	 *         <ul>
	 *         <li>field name</li>
	 *         <li>field display name</li>
	 *         <li>a field decoder</li>
	 *         </ul>
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Triple<String, String, StringDecoder>> getorderedFieldDefinition(E sampleobject) {
		ArrayList<Triple<String, String, StringDecoder>> keyandlabel = new ArrayList<Triple<String, String, StringDecoder>>();
		OrderedList<DataObjectFieldDefinition<E>> orderedfieldlist = getOrderedFieldDefinition();
		if (sampleobject != null) {
			List<FieldSchemaForDisplay<E>> flexiblefieldsdefinition = sampleobject.getFlexibleFieldsDefinition();
			for (int i = 0; i < flexiblefieldsdefinition.size(); i++) {
				FieldSchemaForDisplay<E> fieldschema = flexiblefieldsdefinition.get(i);
				orderedfieldlist.insertWithIndex(fieldschema.getPriority(), fieldschema.getDataObjectFieldDefinition());
			}
		}
		ArrayList<DataObjectFieldDefinition<E>> orderedfieldresultbeforefilter = orderedfieldlist.getOrderedList();
		ArrayList<DataObjectFieldDefinition<E>> orderedfieldresult = new ArrayList<DataObjectFieldDefinition<E>>();
		for (int i = 0; i < orderedfieldresultbeforefilter.size(); i++) {
			if (orderedfieldresultbeforefilter.get(i).getPriority() > 0)
				orderedfieldresult.add(orderedfieldresultbeforefilter.get(i));
		}
		for (int i = 0; i < orderedfieldresult.size(); i++) {
			DataObjectFieldDefinition<E> thisfield = orderedfieldresult.get(i);
			boolean specialtreatment = false;
			if (thisfield instanceof ChoiceDataObjectFieldDefinition) {
				specialtreatment = true;
				keyandlabel
						.add(new Triple<String, String, StringDecoder>(thisfield.getName(), thisfield.getDisplayname(),
								((ChoiceDataObjectFieldDefinition<?, E>) thisfield).getFieldchoicedefinition()));
			}
			if (thisfield instanceof MultipleChoiceDataObjectFieldDefinition) {
				specialtreatment = true;
				keyandlabel.add(new Triple<String, String, StringDecoder>(thisfield.getName(),
						thisfield.getDisplayname(),
						((MultipleChoiceDataObjectFieldDefinition<?, E>) thisfield).getFieldchoicedefinition()));
			}
			if (thisfield instanceof TimePeriodDataObjectFieldDefinition) {
				specialtreatment = true;
				keyandlabel.add(new Triple<String, String, StringDecoder>(thisfield.getName(),
						thisfield.getDisplayname(), new TimePeriodDataObjectFieldDefinition.TimePeriodFormatter()));
			}

			if (!specialtreatment)
				keyandlabel.add(new Triple<String, String, StringDecoder>(thisfield.getName(),
						thisfield.getDisplayname(), null));
		}
		return keyandlabel;
	}

	/**
	 * @return the ordered list of fields by priority
	 */
	private OrderedList<DataObjectFieldDefinition<E>> getOrderedFieldDefinition() {
		OrderedList<DataObjectFieldDefinition<E>> orderedfieldlist = new OrderedList<DataObjectFieldDefinition<E>>();
		for (int i = 0; i < fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfield = fielddeflist.get(i);

			orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);

		}
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = propertydeflist.get(i);
			DataObjectFieldDefinition<E>[] displayablefieldsforproperty = thisproperty
					.getDataObjectFieldDefinition(displayprofiles);
			for (int j = 0; j < displayablefieldsforproperty.length; j++) {

				orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
						displayablefieldsforproperty[j]);

			}
		}
		return orderedfieldlist;
	}

	/**
	 * writes all fields definition
	 * 
	 * @param writer   writer for the message
	 * @param pagedata parent page data
	 * @param buffer   security buffer
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, SPageData pagedata, SecurityBuffer buffer)
			throws IOException {

		OrderedList<DataObjectFieldDefinition<E>> orderedfieldlist = getOrderedFieldDefinition();
		ArrayList<DataObjectFieldDefinition<E>> orderedfieldresult = orderedfieldlist.getOrderedList();
		if (orderedfieldresult != null)
			for (int i = 0; i < orderedfieldresult.size(); i++) {
				writer.startStructure("ATTR");
				orderedfieldresult.get(i).getDataFieldDefinition().WriteToCDL(writer, pagedata, buffer);
				writer.endStructure("ATTR");
			}
	}

	/**
	 * writes all fields definition except the fields to hide and fields authorized
	 * by display profiles
	 * 
	 * @param writer           writer for the message
	 * @param fieldstohide     a list of fields to hide
	 * @param displayprofiles  valid display profiles
	 * @param writeconstraints if true, will write the multi field constraints
	 * @param minpriority      a filter by minimum priority
	 * @param pagedata         parent page data to write the definition in
	 * @param buffer           security buffer
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, ArrayList<DataObjectFieldMarker<E>> fieldstohide,
			NamedList<DisplayProfile<E>> displayprofiles, boolean writeconstraints, int minpriority, SPageData pagedata,
			SecurityBuffer buffer) throws IOException {
		writer.startStructure("ATTRS");
		writeFieldDefinition(writer, fieldstohide, displayprofiles, minpriority, pagedata, buffer);
		writer.endStructure("ATTRS");
		if (writeconstraints)
			writeMultiFieldConstraints(writer);
	}

	/**
	 * writes all fields definition except the fields to hide and fields authorized
	 * by display profiles, with some fields classified to show after the more
	 * 
	 * @param writer           writer for the message
	 * @param fieldstohide     a list of fields to hide
	 * @param displayprofiles  valid display profiles
	 * @param writeconstraints if true, will write the multi field constraints
	 * @param minpriority      a filter by 'minimum' priority
	 * @param morepriority     fields with priority of less than 'more' but more
	 *                         than 'minpriority' will be shown in a collapsible
	 *                         section for the object
	 * @param pagedata         parent page data to write the definition in
	 * @param buffer           security buffer
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, ArrayList<DataObjectFieldMarker<E>> fieldstohide,
			NamedList<DisplayProfile<E>> displayprofiles, boolean writeconstraints, int minpriority, int morepriority,
			SPageData pagedata, SecurityBuffer buffer) throws IOException {
		writer.startStructure("ATTRS");
		writeFieldDefinition(writer, fieldstohide, displayprofiles, morepriority, pagedata, buffer);
		writer.endStructure("ATTRS");
		writer.startStructure("MOREATTRS");
		writeFieldDefinition(writer, fieldstohide, displayprofiles, minpriority, morepriority, pagedata, buffer);
		writer.endStructure("MOREATTRS");

		if (writeconstraints)
			writeMultiFieldConstraints(writer);
	}

	/**
	 * writes the MultiFieldConstraints
	 * 
	 * @param writer the message writer to writer payload into
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeMultiFieldConstraints(MessageWriter writer) throws IOException {
		writer.startStructure("CTRS");
		for (int i = 0; i < this.fieldconstraints.getSize(); i++) {
			writer.startStructure("CTR");
			SMultiFieldConstraint thisconstraint = fieldconstraints.get(i);
			thisconstraint.writeToCML(writer);
			writer.endStructure("CTR");
		}
		writer.endStructure("CTRS");

	}

	/**
	 * writes all fields definition except the fields to hide and fields authorized
	 * by display profiles, with some fields classified to show after the more
	 * 
	 * @param writer          writer for the message
	 * @param fieldstohide    a list of fields to hide
	 * @param displayprofiles valid display profiles
	 * @param minpriority     a filter by 'minimum' priority
	 * @param morepriority    fields with priority of less than 'more' but more
	 * @param pagedata        parent page data to write the definition in
	 * @param buffer          security buffer
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, ArrayList<DataObjectFieldMarker<E>> fieldstohide,
			NamedList<DisplayProfile<E>> displayprofiles, int minpriority, int morepriority, SPageData pagedata,
			SecurityBuffer buffer) throws IOException {
		HashMap<String, DataObjectFieldMarker<E>> hiddenfieldsmap = new HashMap<String, DataObjectFieldMarker<E>>();
		if (fieldstohide != null)
			for (int i = 0; i < fieldstohide.size(); i++)
				hiddenfieldsmap.put(fieldstohide.get(i).toString(), fieldstohide.get(i));
		OrderedList<DataObjectFieldDefinition<E>> orderedfieldlist = new OrderedList<DataObjectFieldDefinition<E>>();
		logger.info(
				" --------------------- Write field definition for min priority = " + minpriority + " ------------- ");

		for (int i = 0; i < fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfield = fielddeflist.get(i);
			if (hiddenfieldsmap.get(thisfield.getName()) == null) {
				if (thisfield.getPriority() >= minpriority)
					if (thisfield.getPriority() < morepriority) {
						orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
						logger.info(" * field in: '" + thisfield.getName() + "' priority " + thisfield.getPriority());
					} else {
						if (thisfield.isShowinbottonpage())
							orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
						logger.info(" * field out: '" + thisfield.getName() + "' priority " + thisfield.getPriority());
					}

				/*
				 * writer.startStructure("ATTR");
				 * thisfield.getDataFieldDefinition().WriteToCDL(writer);
				 * writer.endStructure("ATTR");
				 */
			} else {
				if (thisfield.isShowinbottonpage())
					orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
				logger.info(" * field hidden : " + thisfield.getName());
			}
		}
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = propertydeflist.get(i);
			DataObjectFieldDefinition<E>[] displayablefieldsforproperty = thisproperty
					.getDataObjectFieldDefinition(displayprofiles);
			for (int j = 0; j < displayablefieldsforproperty.length; j++) {

				if (hiddenfieldsmap.get(displayablefieldsforproperty[j].getName()) == null) {
					if (displayablefieldsforproperty[j].getPriority() >= minpriority)
						if (displayablefieldsforproperty[j].getPriority() < morepriority) {
							orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
									displayablefieldsforproperty[j]);
							logger.info(" * property field in: '" + displayablefieldsforproperty[j].getName()
									+ "' priority " + displayablefieldsforproperty[j].getPriority());

						} else {
							if (displayablefieldsforproperty[j].isShowinbottonpage())
								orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
										displayablefieldsforproperty[j]);
							logger.info(" * property field out: '" + displayablefieldsforproperty[j].getName()
									+ "' priority " + displayablefieldsforproperty[j].getPriority());

						}
					/*
					 * writer.startStructure("ATTR");
					 * displayablefieldsforproperty[j].getDataFieldDefinition().WriteToCDL(writer);
					 * writer.endStructure("ATTR");
					 */
				} else {
					if (displayablefieldsforproperty[j].isShowinbottonpage())
						orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
								displayablefieldsforproperty[j]);
					logger.info(" * property field hidden: '" + displayablefieldsforproperty[j].getName()
							+ "' priority " + displayablefieldsforproperty[j].getPriority());

				}
			}
		}
		ArrayList<DataObjectFieldDefinition<E>> orderedfieldresult = orderedfieldlist.getOrderedList();
		if (orderedfieldresult != null)
			for (int i = 0; i < orderedfieldresult.size(); i++) {
				writer.startStructure("MOREATTR");
				orderedfieldresult.get(i).getDataFieldDefinition().WriteToCDL(writer, pagedata, buffer);
				writer.endStructure("MOREATTR");
			}
	}

	/**
	 * writes all fields definition except the fields to hide and fields authorized
	 * by display profiles
	 * 
	 * @param writer          writer for the message
	 * @param fieldstohide    a list of fields to hide
	 * @param displayprofiles valid display profiles
	 * @param minpriority     a filter by 'minimum' priority
	 * @param pagedata        parent page data to write the definition in
	 * @param buffer          security buffer
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, ArrayList<DataObjectFieldMarker<E>> fieldstohide,
			NamedList<DisplayProfile<E>> displayprofiles, int minpriority, SPageData pagedata, SecurityBuffer buffer)
			throws IOException {
		writeFieldDefinition(writer, fieldstohide, displayprofiles, minpriority, pagedata, buffer, null);
	}

	/**
	 * writes all fields definition except the fields to hide and fields authorized
	 * by display profiles, with flexible fields added
	 * 
	 * @param writer                   writer for the message
	 * @param fieldstohide             a list of fields to hide
	 * @param displayprofiles          valid display profiles
	 * @param minpriority              a filter by 'minimum' priority
	 * @param pagedata                 parent page data to write the definition in
	 * @param buffer                   security buffer
	 * @param flexiblefieldsdefinition specific field definition for one instance
	 *                                 object
	 * @throws IOException if any exception happens during the transmission
	 */
	public void writeFieldDefinition(MessageWriter writer, ArrayList<DataObjectFieldMarker<E>> fieldstohide,
			NamedList<DisplayProfile<E>> displayprofiles, int minpriority, SPageData pagedata, SecurityBuffer buffer,
			List<FieldSchemaForDisplay<E>> flexiblefieldsdefinition) throws IOException {
		HashMap<String, DataObjectFieldMarker<E>> hiddenfieldsmap = new HashMap<String, DataObjectFieldMarker<E>>();
		if (fieldstohide != null)
			for (int i = 0; i < fieldstohide.size(); i++)
				hiddenfieldsmap.put(fieldstohide.get(i).toString(), fieldstohide.get(i));
		OrderedList<DataObjectFieldDefinition<E>> orderedfieldlist = new OrderedList<DataObjectFieldDefinition<E>>();
		logger.info(
				" --------------------- Write field definition for min priority = " + minpriority + " ------------- ");

		for (int i = 0; i < fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfield = fielddeflist.get(i);
			if (hiddenfieldsmap.get(thisfield.getName()) == null) {
				if (thisfield.getPriority() >= minpriority) {
					orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
					logger.info(" * field in: '" + thisfield.getName() + "' priority " + thisfield.getPriority());
				} else {
					if (thisfield.isShowinbottonpage())
						orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
					logger.info(" * field out: '" + thisfield.getName() + "' priority " + thisfield.getPriority());
				}

			} else {
				if (thisfield.isShowinbottonpage())
					orderedfieldlist.insertWithIndex(thisfield.getPriority(), thisfield);
				logger.info(" * field hidden : " + thisfield.getName());
			}
		}
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = propertydeflist.get(i);
			DataObjectFieldDefinition<E>[] displayablefieldsforproperty = thisproperty
					.getDataObjectFieldDefinition(displayprofiles);
			for (int j = 0; j < displayablefieldsforproperty.length; j++) {

				if (hiddenfieldsmap.get(displayablefieldsforproperty[j].getName()) == null) {
					if (displayablefieldsforproperty[j].getPriority() >= minpriority) {
						orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
								displayablefieldsforproperty[j]);
						logger.info(" * property field in: '" + displayablefieldsforproperty[j].getName()
								+ "' priority " + displayablefieldsforproperty[j].getPriority());

					} else {
						if (displayablefieldsforproperty[j].isShowinbottonpage())
							orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
									displayablefieldsforproperty[j]);
						logger.info(" * property field out: '" + displayablefieldsforproperty[j].getName()
								+ "' priority " + displayablefieldsforproperty[j].getPriority());

					}

				} else {
					if (displayablefieldsforproperty[j].isShowinbottonpage())
						orderedfieldlist.insertWithIndex(displayablefieldsforproperty[j].getPriority(),
								displayablefieldsforproperty[j]);
					logger.info(" * property field hidden: '" + displayablefieldsforproperty[j].getName()
							+ "' priority " + displayablefieldsforproperty[j].getPriority());

				}
			}
		}
		if (flexiblefieldsdefinition != null)
			for (int i = 0; i < flexiblefieldsdefinition.size(); i++) {
				FieldSchemaForDisplay<E> thisflexiblefield = flexiblefieldsdefinition.get(i);
				orderedfieldlist.insertWithIndex(thisflexiblefield.getPriority(),
						thisflexiblefield.getDataObjectFieldDefinition());
			}

		ArrayList<DataObjectFieldDefinition<E>> orderedfieldresult = orderedfieldlist.getOrderedList();

		if (orderedfieldresult != null)
			for (int i = 0; i < orderedfieldresult.size(); i++) {
				writer.startStructure("ATTR");
				orderedfieldresult.get(i).getDataFieldDefinition().WriteToCDL(writer, pagedata, buffer);
				writer.endStructure("ATTR");

			}

	}

	/**
	 * extends a query for this object with all default conditions
	 * 
	 * @param tablelist       the list of table alias
	 * @param mainobjectalias the alias for this table
	 * @param condition       currentcondition
	 * @return the extended query definition
	 */
	public QueryCondition extendquery(NamedList<TableAlias> tablelist, TableAlias mainobjectalias,
			QueryCondition condition) {
		logger.finest("starting extension of query for ");
		boolean isanyextension = false;
		AndQueryCondition counpoundcondition = new AndQueryCondition();
		counpoundcondition.addCondition(condition);
		for (int i = 0; i < propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = propertydeflist.get(i);
			ExternalFieldByJoinQuery[] joinqueries = thisproperty.getExternalFieldsByJoinQueries();
			logger.finest("property = " + thisproperty + ", externalfields number = " + joinqueries.length);

			for (int j = 0; j < joinqueries.length; j++) {
				ExternalFieldByJoinQuery thisjoinquery = joinqueries[j];
				@SuppressWarnings("rawtypes")
				JoinQueryCondition joincondition = thisjoinquery.getJoinQueryConditionDefinition()
						.generateJoinQueryCondition(mainobjectalias.getName());
				TableAlias sidetablealias = joincondition.getSideTableAlias();
				thisjoinquery.restrainTableAlias(sidetablealias, mainobjectalias.getName());
				if (tablelist.lookupOnName(sidetablealias.getName()) != null) {
					logger.finest("merging alias with existing " + sidetablealias);

					TableAlias similaralias = tablelist.lookupOnName(sidetablealias.getName());
					FieldSelectionAlias<?>[] selection = sidetablealias.getFieldSelection();
					if (selection != null)
						for (int k = 0; k < selection.length; k++) {
							if (selection[k].getField() == null)
								throw new RuntimeException("field is null for index " + k + " for existing table alias "
										+ sidetablealias.getName() + " table = " + sidetablealias.getTable().getName());
							similaralias.addFieldSelection(selection[k].getField(), selection[k].getAlias());
						}

				} else {
					logger.finest("adding alias with existing " + sidetablealias);
					tablelist.add(joincondition.getSideTableAlias());
					counpoundcondition.addCondition(joincondition);
					QueryCondition joinobjectuniversalcondition = thisjoinquery.getJoinQueryConditionDefinition()
							.generateSideTableUniversalQueryCondition(mainobjectalias.getName());
					counpoundcondition.addCondition(joinobjectuniversalcondition);
				}

				isanyextension = true;
			}
		}
		if (isanyextension)
			return counpoundcondition;
		return condition;
	}

	/**
	 * @param name
	 */
	protected void defineDisplayProfile(String name) {
		this.displayprofiles.add(new DisplayProfile<E>(name, this));
	}

	/**
	 * @return
	 */
	public ArrayList<DataObjectFieldMarker<E>> getAllFieldMarkersForObjectFields() {
		ArrayList<DataObjectFieldMarker<E>> fieldmarkers = new ArrayList<DataObjectFieldMarker<E>>();
		for (int i = 0; i < this.fielddeflist.getSize(); i++) {
			logger.fine("for object '" + this.getLabel() + "', adds field marker (" + i + ") for "
					+ this.fielddeflist.get(i).getName());
			fieldmarkers.add(new DataObjectFieldMarker<E>(this, fielddeflist.get(i).getName()));
		}
		return fieldmarkers;
	}

	/**
	 * @param fieldname
	 * @return
	 */
	protected DataObjectFieldMarker<E> getFieldMarker(String fieldname) {
		return new DataObjectFieldMarker<E>(this, this.fielddeflist.lookupOnName(fieldname).getName());
	}

	/**
	 * @param propertyname
	 * @param fieldname
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected DataObjectFieldMarker<E> getPropertyFieldMarker(String propertyname, String fieldname) {
		return this.propertydeflist.lookupOnName(propertyname).getFieldMarker(this, fieldname);
	}

	/**
	 * @return the unique name of the module of the data object
	 */
	public abstract String getModuleName();

	/**
	 * @return the list of all flat file loader columns
	 */
	public ArrayList<String[]> getFlatFileLoaderDescription() {
		ArrayList<String[]> columndescriptions = new ArrayList<String[]>();
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = this.propertydeflist.get(i);
			String[] columns = thisproperty.getLoaderFieldList();
			if (columns == null)
				throw new RuntimeException("list of columns is null for property " + thisproperty.getName());
			for (int j = 0; j < columns.length; j++) {
				String[] thiscolumndescription = thisproperty.getLoaderFieldSample(columns[j]);
				if (thiscolumndescription == null)
					throw new RuntimeException("Column description is null for property " + thisproperty.getName()
							+ " for field " + columns[j]);
				columndescriptions.add(thiscolumndescription);
			}
		}
		for (int i = 0; i < this.fielddeflist.getSize(); i++) {
			DataObjectFieldDefinition<E> thisfield = this.fielddeflist.get(i);
			String[] thisfielddescription = thisfield.getLoaderFieldSample();
			if (thisfielddescription == null)
				throw new RuntimeException("Column description is null for field " + thisfield.getName() + ".");

			columndescriptions.add(thisfielddescription);
		}
		return columndescriptions;
	}

	/**
	 * @return all the transient properties for loader
	 */
	public TransientPropertiesForLoader<E> getTransientPropertiesForLoader() {
		TransientPropertiesForLoader<E> transientproperties = new TransientPropertiesForLoader<E>();
		for (int i = 0; i < this.propertydeflist.getSize(); i++) {
			DataObjectPropertyDefinition<E> thisproperty = this.propertydeflist.get(i);
			CustomloaderHelper<E> thistransientloader = thisproperty.getTransientLoaderHelper();
			if (thistransientloader != null)
				transientproperties.addTransientColumnGenerator(thisproperty.getName(), thistransientloader);
		}
		return transientproperties;
	}

	/**
	 * check if an alias is valid for the flat file loader
	 * 
	 * @param alias        the alias
	 * @param filter       the value of a filter
	 * @param restrictions some unauthorized values
	 * @return
	 */
	public static <Z extends FieldChoiceDefinition<Z>> boolean isAliasValid(String alias, ChoiceValue<Z> filter,
			HashMap<String, ChoiceValue<Z>[]> restrictions) {
		if (!restrictions.containsKey(alias)) {
			return true;
		} else {
			if (filter == null)
				return false;
			ChoiceValue<Z>[] restrictionsforalias = restrictions.get(alias);
			for (int i = 0; i < restrictionsforalias.length; i++) {
				if (restrictionsforalias[i].getStorageCode().equals(filter.getStorageCode()))
					return true;
			}
			return false;
		}

	}

}
