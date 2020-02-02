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
 * this business rule will show the link as an attribute from the left object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 *        )
 * @param <F> right object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 */
public class DisplayLinkAsAttributeFromLeftObject<E extends DataObjectDefinition, F extends DataObjectDefinition>
		extends
		PropertyBusinessRule<LinkObject<E, F>> {
	/**
	 * Probably not used, see Github issue #21
	 */
	private boolean searchbynumber;

	/**
	 * creates the business rule to display link as attribute from the left object
	 * 
	 * @param searchbynumber if true, search by number, if false, search by name
	 */
	public DisplayLinkAsAttributeFromLeftObject(boolean searchbynumber) {
		super("DISPLAYASATTRIBUTEFROMLEFT", false);
		this.searchbynumber = searchbynumber;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {

	}

	@Override
	public String[] getImportstatements() {
		String[] importlist = new String[1];
		importlist[0] = "import org.openlowcode.server.graphic.widget.SObjectArrayField;";
		return importlist;
	}

	/**
	 * @return true if search of right object is by number, false if search of right
	 *         number is by name
	 */
	public boolean isSearchbynumber() {
		return searchbynumber;
	}

}
