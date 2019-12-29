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

import org.openlowcode.module.system.data.AppuserDefinition;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TimestampStoredField;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class UpdatelogDefinition<E extends DataObject<E>> extends DataObjectPropertyDefinition<E> {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(CreationlogDefinition.class.getName());
	private StringStoredField updateuserid;
	private TimestampStoredField updatetime;
	private boolean fieldsintitle;
	private boolean fieldsinbottomnotes;

	public UpdatelogDefinition(DataObjectDefinition<E> parentobject) {
		super(parentobject, "UPDATELOG");

		updateuserid = new StringStoredField("UPDATEUSERID", null, 200, AdminIdDefaultValueGenerator.get());
		this.addFieldSchema(updateuserid);
		updatetime = new TimestampStoredField("UPDATETIME", null, new Date());
		this.addFieldSchema(updatetime);
		this.fieldsintitle = false;
		this.fieldsinbottomnotes = false;
		StoredTableIndex updatetimeindex = new StoredTableIndex("UPDATETIME");
		updatetimeindex.addStoredFieldSchame(updatetime);
		this.addIndex(updatetimeindex);
		StoredTableIndex updateuseridindex = new StoredTableIndex("UPDATEUSERID");
		updateuseridindex.addStoredFieldSchame(updateuserid);
		this.addIndex(updateuseridindex);
	}

	public void setFieldsInTitle() {
		this.fieldsintitle = true;
	}

	public void setFieldsInBottomNotes() {
		this.fieldsinbottomnotes = true;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();
		AppuserDefinition referenceobjectdefinition = AppuserDefinition.getAppuserDefinition();

		JoinQueryConditionDefinition<String> joincondition = AppuserDefinition.getAppuserDefinition()
				.generateJoinQueryDefinition(this.parentobject.getTableschema(), updateuserid, "UNIQUEIDENTIFIED", "ID",
						this.getName(), new QueryOperatorEqual<String>());

		ExternalFieldSchema<?> externalfield = referenceobjectdefinition.generateExternalFieldFromTitle(
				"UPDATEUSERNUMBER", "Updated By", "Indicates the user who last updated the object", joincondition, -90,
				40);
		if (this.fieldsinbottomnotes)
			externalfield.setDisplayInBottomNotes();
		if (this.fieldsintitle)
			externalfield.setDisplayInTitle();
		externalfieldlist.add(externalfield);

		return externalfieldlist;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[2];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Update User id",
				"Technical id of the user who created the object", updateuserid, false, true, -160, 20,
				this.parentobject);
		if (this.fieldsinbottomnotes)
			returnvalue[1] = new FieldSchemaForDisplay<E>("Update Time", "Time the object has been created", updatetime,
					false, true, -150, 30, this.parentobject);
		if (this.fieldsintitle)
			returnvalue[1] = new FieldSchemaForDisplay<E>("Update Time", "Time the object has been created", updatetime,
					true, false, -150, 30, this.parentobject);
		if (!((this.fieldsinbottomnotes) || (this.fieldsintitle)))
			returnvalue[1] = new FieldSchemaForDisplay<E>("Update Time", "Time the object has been created", updatetime,
					false, false, -150, 30, this.parentobject);
		return returnvalue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Updatelog(this, parentpayload);
	}

	public void setDependentDefinitionStoredobject(StoredobjectDefinition<E> storedobjectdefinition) {
		// not needed in that case
	}

	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		// not needed in that case
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		if (columnattributes == null)
			throw new RuntimeException("At least one attribute required for updatelog: DATE or USER");
		if (columnattributes != null)
			if (columnattributes.length == 0)
				throw new RuntimeException("At least one attribute required for updatelog: DATE or USER");

		if ("DATE".equals(columnattributes[0])) {

			String format = null;
			if (columnattributes.length > 1) {
				format = columnattributes[1];
			}
			return new UpdatelogDateFlatFileLoader<E>(objectdefinition, this, format, propertyextractor);

		}
		if ("USER".equals(columnattributes[0])) {

			boolean createifnotexists = false;
			if (columnattributes.length > 1) {
				for (int i = 1; i < columnattributes.length; i++) {
					if ("CREATE".equals(columnattributes[i]))
						createifnotexists = true;
				}

			}
			return new UpdatelogUserFlatFileLoader<E>(objectdefinition, this, createifnotexists, propertyextractor);
		}
		throw new RuntimeException("First attribute for update should be  DATE or USER, not " + columnattributes[0]);
	}

	@Override
	public String[] getLoaderFieldList() {
		String[] fields = new String[2];
		fields[0] = "USER";
		fields[1] = "DATE";
		return fields;
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		if (name.equals("USER")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&USER";
			returntable[1] = "OPTIONAL";
			returntable[2] = "a805391";
			returntable[3] = "the id of the user.\n It is possible to specify CREATEIFNOTEXISTS,\n then a new user is created if needed with just the id/number";
			return returntable;
		}
		if (name.equals("DATE")) {
			String[] returntable = new String[4];
			returntable[0] = this.getName() + "&DATE";
			returntable[1] = "OPTIONAL";
			returntable[2] = "2017.02.28";
			returntable[3] = "Optional parameter: specific java simpledateformat\n (e.g. \"yyyy.MM.dd G 'at' HH:mm:ss z\" for 2001.07.04 AD at 12:08:56 PDT ) ,\n definition at https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
			return returntable;
		}
		return null;
	}

}
