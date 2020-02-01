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
 * This business rule changes the way the update log information is shown, by
 * putting it either in the title (more visible) or in the bottom notes of the
 * object (less visible)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class UpdateLogChangeDisplay
		extends
		PropertyBusinessRule<UpdateLog> {

	/**
	 * show update log information in bottom notes
	 */
	public static final int CHANGEDISPLAY_SHOWINBOTTOMNOTES = 1;
	/**
	 * show update log information in title
	 */
	public static final int CHANGEDISPLAY_SHOWINTITLE = 2;
	private int changedisplay;

	/**
	 * creates an update log change display business rule
	 * 
	 * @param changedisplay the change display as defined in a static int in this
	 *                      class
	 */
	public UpdateLogChangeDisplay(int changedisplay) {
		super("UPDATELOGCHANGEDISPLAY", false);
		this.changedisplay = changedisplay;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		if (this.changedisplay == CHANGEDISPLAY_SHOWINTITLE)
			sg.wl("		updatelog.setFieldsInTitle();");
		if (this.changedisplay == CHANGEDISPLAY_SHOWINBOTTOMNOTES)
			sg.wl("		updatelog.setFieldsInBottomNotes();");
	}

	@Override
	public String[] getImportstatements() {

		return null;
	}

}
