/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import org.openlowcode.module.system.data.choice.ControllevelChoiceDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.workflowhelper.DataObjectExtractor;
import org.openlowcode.server.data.workflowhelper.IsTypeEmpty;

/**
 * A control rule checking that the given field is filled
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 * @param <F> type of the payload of the field
 */
public class DataControlRuleFieldFilled<E extends DataObject<E>, F extends Object> extends DataControlRule<E> {
	private DataObjectExtractor<E, F> extractor;
	private IsTypeEmpty<F> typecontroller;
	private String fieldname;

	/**
	 * creates a data control rule checking that the field is filled
	 * 
	 * @param extractor      extractor of the field payload
	 * @param fieldname      field name
	 * @param typecontroller controler checking if the field is empty
	 */
	public DataControlRuleFieldFilled(DataObjectExtractor<E, F> extractor, String fieldname,
			IsTypeEmpty<F> typecontroller) {
		this.extractor = extractor;
		this.fieldname = fieldname;
		this.typecontroller = typecontroller;
	}

	@Override
	public DataControlRuleFeedback control(E object) {
		F field = extractor.extract(object);
		if (field == null)
			return new DataControlRuleFeedback("Compulsory field " + fieldname + " is null.",
					ControllevelChoiceDefinition.get().ERROR);
		if (typecontroller == null)
			return null;
		if (typecontroller.hascontent(field))
			return null;
		return new DataControlRuleFeedback("Compulsory field " + fieldname + " is empty.",
				ControllevelChoiceDefinition.get().ERROR);
	}

}
