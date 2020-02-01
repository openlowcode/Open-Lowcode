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

import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;

/**
 * A business rule allowing to change the display of the creation log either in
 * title or in bottom notes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CreationLogChangeDisplay
		extends
		PropertyBusinessRule<CreationLog> {
	/**
	 * show display in bottom notes
	 */
	public static final int CHANGEDISPLAY_SHOWINBOTTOMNOTES = 1;
	/**
	 * show display in title
	 */
	public static final int CHANGEDISPLAY_SHOWINTITLE = 2;
	private int changedisplay;

	/**
	 * Creates a business rule changing display of creation log information
	 * 
	 * @param changedisplay change display as defined in a static int
	 */
	public CreationLogChangeDisplay(int changedisplay) {
		super("CREATIONLOGCHANGEDISPLAY", false);
		this.changedisplay = changedisplay;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		if (this.changedisplay == CHANGEDISPLAY_SHOWINTITLE)
			sg.wl("		creationlog.setFieldsInTitle();");
		if (this.changedisplay == CHANGEDISPLAY_SHOWINBOTTOMNOTES)
			sg.wl("		creationlog.setFieldsInBottomNotes();");

	}

	@Override
	public String[] getImportstatements() {
		return null;
	}

}
