/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.design.advanced;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;

/**
 * A MainReportValue is the center of the SmartReport. It is consolidated
 * through a specific algorithm, typically sum or average. For the
 * consolidation, sum or average can be consolidated.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public abstract class MainReportValue {
	@SuppressWarnings("unused")
	private ValueConsolidator valueconsolidator;
	private int totalindex = -1;
	private boolean hastotal;

	/**
	 * @return true if total should be shown
	 */
	public boolean hasTotal() {
		return hastotal;
	}

	/**
	 * creates a main value for the given consolidator
	 * 
	 * @param valueconsolidator value consolidator
	 */
	public MainReportValue(ValueConsolidator valueconsolidator) {
		this.valueconsolidator = valueconsolidator;
		this.hastotal = false;
	}

	/**
	 * creates a main value for the given consolidator
	 * 
	 * @param valueconsolidator value consolidator
	 * @param hastotal          if true, show a total column, if false, do not show
	 *                          a total column
	 */
	public MainReportValue(ValueConsolidator valueconsolidator, boolean hastotal) {
		this.valueconsolidator = valueconsolidator;
		this.hastotal = hastotal;
	}

	/**
	 * @return get the total index
	 */
	public int getTotalIndex() {
		return totalindex;
	}

	/**
	 * totalindex set the total index
	 * 
	 * @param total index
	 */
	public void setTotalIndex(int totalindex) {
		this.totalindex = totalindex;
	}

	/**
	 * @return get the parent object
	 */
	public abstract DataObjectDefinition getParentObject();

	/**
	 * generates the extractor
	 * 
	 * @param objectname name of the object
	 * @return the extractor
	 */
	protected abstract String printExtractor(String objectname);

	/**
	 * 
	 * 
	 * @param newname new name for the field
	 * @param newlabel new label for the field
	 * @return a copy of the field to be used for total
	 */
	public abstract Field copyFieldForTotal(String newname, String newlabel);
}
