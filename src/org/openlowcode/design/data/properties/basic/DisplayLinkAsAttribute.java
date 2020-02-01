/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * This business rule will display as an attribute widgets all the links from
 * either the left object or the right object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> left object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 *        )
 * @param <F> right object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 */
public class DisplayLinkAsAttribute<E extends DataObjectDefinition, F extends DataObjectDefinition>
		extends
		PropertyBusinessRule<LinkObject<E, F>> {
	private boolean leftdisplay;
	private boolean searchbynumber;

	/**
	 * creates the display link as attribute business rule
	 * 
	 * @param name            name of the link
	 * @param userimplemented if true, it should be implemented by the user
	 * @param leftdisplay     if true, display link as attribute from the left
	 * @param searchbynumber  if true, search by number, if false, search by name
	 */
	public DisplayLinkAsAttribute(String name, boolean userimplemented, boolean leftdisplay, boolean searchbynumber) {
		super(name, userimplemented);
		this.leftdisplay = leftdisplay;
		this.searchbynumber = searchbynumber;
	}

	/**
	 * @return true if display is added on the left object, right if display is
	 *         added on the right object
	 */
	public boolean isLeftdisplay() {
		return leftdisplay;
	}

	/**
	 * @return true to search by number, false to search by name
	 */
	public boolean isSearchbynumber() {
		return searchbynumber;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
	}

	@Override
	public String[] getImportstatements() {
		return null;
	}

}
