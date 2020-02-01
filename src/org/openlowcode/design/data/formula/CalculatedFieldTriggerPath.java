/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.formula;

import java.util.ArrayList;

import org.openlowcode.design.data.properties.basic.ComputedDecimal;
import org.openlowcode.design.generation.StringFormatter;

/**
 * A trigger path for a calculated field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CalculatedFieldTriggerPath {
	private ComputedDecimal originfield;
	private ArrayList<SignificantTriggerPath> path;

	/**
	 * creates a path originating at the given origin field
	 * 
	 * @param originfield the original field of the trigger path
	 */
	public CalculatedFieldTriggerPath(ComputedDecimal originfield) {
		this.originfield = originfield;
		this.path = new ArrayList<SignificantTriggerPath>();
	}

	/**
	 * @return get the origin field of thepath
	 */
	public ComputedDecimal getOriginField() {
		return this.originfield;
	}

	/**
	 * @return generates the path
	 */
	public String generatePath() {
		String fullpath = "new LocalPath(" + StringFormatter.formatForJavaClass(originfield.getParent().getName())
				+ ".getDefinition())";
		for (int i = path.size() - 1; i >= 0; i--) {
			fullpath = path.get(i).generatePath(fullpath);
		}
		return fullpath;
	}

	/**
	 * add a path element
	 * 
	 * @param thistriggerpath path element for the trigger path
	 */
	public void addPath(SignificantTriggerPath thistriggerpath) {
		this.path.add(thistriggerpath);
	}

	@Override
	protected CalculatedFieldTriggerPath clone() {
		CalculatedFieldTriggerPath clone = new CalculatedFieldTriggerPath(originfield);
		for (int i = 0; i < path.size(); i++)
			clone.addPath(path.get(i));
		return clone;
	}

}
