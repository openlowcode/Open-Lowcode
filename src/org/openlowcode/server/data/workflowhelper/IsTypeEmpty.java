/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

/**
 * This interface will check if a field has any meaningful payload. It is
 * typically a non-null and non-void object (for String, non-empty string, for
 * choice value, a value is selected...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <F> type of payload
 */
@FunctionalInterface
public interface IsTypeEmpty<F extends Object> {

	/**
	 * checks if the field has a meaningful payload
	 * 
	 * @param field payload
	 * @return true if meaningful content, false else
	 */
	public boolean hascontent(F field);

}
