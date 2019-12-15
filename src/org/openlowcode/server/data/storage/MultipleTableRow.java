/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.server.data.storage.standardjdbc.SQLQueryConditionGenerator;
import org.openlowcode.tools.misc.NamedList;



/**
 * An holder for a series of similar database actions (insert or update). This allows
 * to leverage database efficient array insert / updates
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings("rawtypes")
public class MultipleTableRow {
	private static Logger logger = Logger.getLogger(MultipleTableRow.class.getName());
	private StoredTableSchema schema;
	
	private ArrayList<NamedList<StoredField>> payloadbyNameForData;
	private ArrayList<QueryCondition>  conditionsforupdate;
	private int currentrowindex;
	private boolean isupdatequery=false;
	private String referencequerycondition;
	
	/**
	 * @param schema common schema for all rows
	 */
	public MultipleTableRow(StoredTableSchema schema) {
		this.schema = schema;
		payloadbyNameForData = new ArrayList<NamedList<StoredField>>();
		conditionsforupdate = new ArrayList<QueryCondition>();
		currentrowindex=0;
		initiateCurrentRowIndex();
	}
	private void initiateCurrentRowIndex() {
		payloadbyNameForData.add(new NamedList<StoredField>());
		conditionsforupdate.add(null);
	}
	/**
	 * move to the next row of processing
	 */
	public void setNextQuery() {
		currentrowindex++;
		payloadbyNameForData.add(new NamedList<StoredField>());
		conditionsforupdate.add(null);
	}
	/**
	 * Adds a specific field
	 * @param storedfield the stored field to add to current row
	 */
	public void addStoredFieldToCurrentRow(StoredField storedfield) {
		this.payloadbyNameForData.get(currentrowindex).add(storedfield);
	}
	
	/**
	 * Adds all the fields in the list
	 * @param allstoredfields a list of stored fields for the current row
	 */
	public void addAllStoredFieldToCurrentRow(NamedList<StoredField> allstoredfields) {
		for (int i=0;i<allstoredfields.getSize();i++) 
			this.payloadbyNameForData.get(currentrowindex).add(allstoredfields.get(i));
	}
	
	/**
	 * @param conditionforupdate the condition for update for the current row
	 */
	public void addQueryCondition(QueryCondition conditionforupdate)  {
		conditionsforupdate.set(currentrowindex,conditionforupdate);
		if (currentrowindex==0) {
			isupdatequery=true;
			StringBuffer referencequeryconditionbuffer = new StringBuffer();
			SQLQueryConditionGenerator conditiongenerator = new SQLQueryConditionGenerator(referencequeryconditionbuffer);
			conditionforupdate.accept(conditiongenerator);
			referencequerycondition = referencequeryconditionbuffer.toString();
			
		} else {
			if (!isupdatequery) throw new RuntimeException("MultipleTableRow is not an update query");
			StringBuffer condition = new StringBuffer();
			SQLQueryConditionGenerator conditiongenerator = new SQLQueryConditionGenerator(condition);
			conditionforupdate.accept(conditiongenerator);
			String conditionstring = condition.toString();
			if (!conditionstring.equals(referencequerycondition)) throw new RuntimeException("Exception: inconsistent query condition '"+
					referencequerycondition+"' != '"+conditionstring+"'");
			// note comparing that SQL query condition (WHERE XXX = ? ) is the same, but of course, data is different
		}
		conditionsforupdate.add(conditionforupdate);
	}
	/**
	 * @return the table schema of the set of rows
	 */
	public StoredTableSchema getTableSchema() {
		return this.schema;
	}
	/**
	 * @return the number of rows
	 */
	public int getPayloadSize() {
		return this.payloadbyNameForData.size();
	}
	/**
	 * gets all fields for the row
	 * @param index index of the row
	 * @return all fields for the row
	 */
	public NamedList<StoredField> getPayload(int index) {
		return this.payloadbyNameForData.get(index);
	}
	/**
	 * gets the payload for the given field schema
	 * @param index index of the row 
	 * @param field schema of the field
	 * @return payload value
	 */
	public <E> E getPayload(int index, StoredFieldSchema<E> field)  {
		StoredField thisfield = payloadbyNameForData.get(index).lookupOnName(field.getName());
		logger.finest("getting payload for field index = "+index+", field = "+field.getName()+", payload = "+thisfield.getPayload());
		return field.castToType(thisfield.getPayload());
	}
	/**
	 * @param index index of the row
	 * @return query condition for the update
	 */
	public QueryCondition getQueryCondition(int index)  {
		return conditionsforupdate.get(index);
	}
}
