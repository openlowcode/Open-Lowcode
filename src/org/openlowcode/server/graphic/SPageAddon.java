/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic;


/**
 * A Page addon is used to add common content to all pages of the application.
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SPageAddon extends SPage {
	private SPage internalcontent;
	/**
	 * @return
	 */
	public SPageNode insertInternalContent() {
		return internalcontent.getContent();
	}
	/**
	 * creates a page addon
	 * @param name name of the addon
	 */
	public SPageAddon(String name) {
		super(name);

	}
	/**
	 * specifies the main payload
	 * @param internalcontent main payload
	 */
	public void setInternalContent(SPage internalcontent) {
		this.internalcontent = internalcontent;
	}
	
	/**
	 * @return the attributes of the page add-on
	 */
	public abstract SPageData getAllAddonPageAttributes();
	@Override
	public SPageData getAllPageAttributes() {
		SPageData data = this.getAllAddonPageAttributes();
		data.addPrefixToVariables("A");
		internalcontent.getAllPageAttributes().addPrefixToVariables("M");
		for (int i=0;i<internalcontent.getAllPageAttributes().size();i++) {
			data.addDataElt(internalcontent.getAllPageAttributes().getAttribute(i));
		}
		return data;
	}

	

}
