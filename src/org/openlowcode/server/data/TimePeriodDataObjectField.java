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
import org.openlowcode.tools.structure.TimePeriodDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

import org.openlowcode.server.data.storage.StoredField;

/**
 * A field on a data object storing a time period. A time period is a time
 * interval typically used for activity and finance reporting (year, month,
 * quarter...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <F> the data object the field is created on
 */
public class TimePeriodDataObjectField<F extends DataObject<F>>
		extends DataObjectField<TimePeriodDataObjectFieldDefinition<F>, F> {
	private static Logger logger = Logger.getLogger(TimePeriodDataObjectField.class.getName());
	private StoredField<String> storedvalue;
	private TimePeriod timeperiod;

	@SuppressWarnings("unchecked")
	public TimePeriodDataObjectField(TimePeriodDataObjectFieldDefinition<F> definition,
			DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		storedvalue = (StoredField<String>) this.field.get(0);
		if (storedvalue.getPayload() != null) {
			this.timeperiod = TimePeriod.generateFromString(storedvalue.getPayload());
			logger.finest("Generating TimePeriod from database '" + storedvalue.getPayload() + "' timeperiod = "
					+ timeperiod + " field name = " + this.storedvalue.getName());
		} else {
			logger.finest("Stored timeperiod is null in database");
		}
	}

	@Override
	public SimpleDataElt getDataElement() {
		return new TimePeriodDataElt(definition.getName(), timeperiod);
	}

	/**
	 * gets the value of the field
	 * 
	 * @return the value of the field as Time Period
	 */
	public TimePeriod getValue() {
		return timeperiod;
	}

	/**
	 * sets the value of the field
	 * 
	 * @param timeperiod the value of the field as Time Period
	 */
	public void setValue(TimePeriod timeperiod) {
		this.timeperiod = timeperiod;
		String payloadstring = (timeperiod != null ? timeperiod.encode() : "");
		this.storedvalue.setPayload(payloadstring);
		logger.finest(
				"Setting payload for timeperiod = " + payloadstring + " field name = " + this.storedvalue.getName());
	}

	@Override
	public void postTreatmentAfterInitFromDB() {

		if (storedvalue.getPayload() != null)
			if (storedvalue.getPayload().compareTo("") != 0) {
				TimePeriod feedback = TimePeriod.generateFromString(storedvalue.getPayload());
				timeperiod = feedback;
			}
	}
}
