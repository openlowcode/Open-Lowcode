/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.logging.Logger;

import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.data.TimePeriod.PeriodType;
import org.openlowcode.tools.misc.StringDecoder;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.STimePeriodField;

/**
 * Definition of a time period data object field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the field is on
 */
public class TimePeriodDataObjectFieldDefinition<E extends DataObject<E>>
		extends
		DataObjectFieldDefinition<E> {
	private static Logger logger = Logger.getLogger(TimePeriodDataObjectFieldDefinition.class.getName());
	private PeriodType periodtype;
	private StringStoredField mainfield;

	/**
	 * Creates a data object field
	 * 
	 * @param name        unique name of the field (without special character,
	 *                    space...)
	 * @param displayname display name for the field in the default language
	 * @param tooltip     a long tooltip display when rolling-over
	 * @param readonly    readonly if field is not modifiable by the user
	 * @param periodtype  type of TimePeriod (Year, Quarter, Month...)
	 * @param definition  definition of the parent object
	 */
	public TimePeriodDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			PeriodType periodtype,
			DataObjectDefinition<E> definition) {
		super(name, displayname, tooltip, readonly, definition);
		this.periodtype = periodtype;
		logger.finest("Period Type in field definition = " + this.periodtype + ", name = " + this.getName());
		this.mainfield = new StringStoredField(this.getName(), null, 12);
		this.addFieldSchema(mainfield);
	}

	/**
	 * @param name        unique name of the field (without special character,
	 *                    space...)
	 * @param displayname display name for the field in the default language
	 * @param tooltip     a long tooltip display when rolling-over
	 * @param readonly    readonly if field is not modifiable by the user
	 * @param periodtype  type of TimePeriod (Year, Quarter, Month...)
	 * @param priority    priority of the field for client display between -1000
	 *                    (low) and 1000 (high)
	 * @param definition  definition of the parent object
	 */
	public TimePeriodDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			PeriodType periodtype,
			int priority,
			DataObjectDefinition<E> definition) {
		super(name, displayname, tooltip, readonly, priority, 20, definition);
		this.periodtype = periodtype;
		logger.finest("Period Type in field definition = " + this.periodtype + ", name = " + this.getName());
		this.mainfield = new StringStoredField(this.getName(), null, 12);
		this.addFieldSchema(mainfield);

	}

	/**
	 * a class allowing to decode a time period from the string stored value
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class TimePeriodFormatter
			implements
			StringDecoder {

		@Override
		public String decode(String storedvalue) {
			TimePeriod timeperiod = TimePeriod.generateFromString(storedvalue);
			if (timeperiod != null)
				return timeperiod.toString();
			return storedvalue;
		}

	}

	@Override
	public SPageNode getDataFieldDefinition() {
		STimePeriodField field = new STimePeriodField(this.getDisplayname(), this.getName(), this.getTooltip(),
				periodtype, null, null, true, this.isReadOnly(), this.isShowintitle(), this.isShowinbottonpage(), null);
		return field;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return new TimePeriodDataObjectFieldFlatFileLoader<E>(objectdefinition, columnattributes, this.getName(),
				periodtype);

	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";

		returntable[2] = "Q1-2019";

		returntable[3] = "A time Period field. This can be a quarter, a month or a year. Samples inside simple quotes: 'Y2019' 'Q1-2019', 'M07-2019' (July 2019) ";
		return returntable;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {
		return mainfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new TimePeriodDataObjectField<E>(this, parentpayload);
	}

}
