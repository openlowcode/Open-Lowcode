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

import java.util.List;

/**
 * A node or node link can generate filter elements. A filter element is a data
 * element on which a filter can be generated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface FilterItemGenerator {
	/**
	 * @return the list of filter elements
	 */
	public List<FilterElement<?>> getFilterelement();

	/**
	 * @return the list of line grouping criterias
	 */
	public List<LineGroupingCriteria> getLineGroupingCriteria();
}
