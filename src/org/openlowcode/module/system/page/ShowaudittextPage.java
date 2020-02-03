/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.page.generated.AbsShowaudittextPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * a page showing the audit text (server encoding issue investigation)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowaudittextPage
		extends
		AbsShowaudittextPage {

	/**
	 * creates the show audit text page
	 * 
	 * @param storedtext   the text as stored on the database
	 * @param unstoredtext the text that just made a round-trip to the server
	 */
	public ShowaudittextPage(String storedtext, String unstoredtext) {
		super(storedtext, unstoredtext);

	}

	@Override
	public String generateTitle(String storedtext, String unstoredtext) {
		return "Audit Text Result";
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Text Audit Result", SPageText.TYPE_TITLE, this));
		mainband.addElement(
				new SPageText("Below is shown the text with roundtrip to server.", SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SPageText(this.getUnstoredtext(), SPageText.TYPE_NORMAL, this));
		STextField unstoredtextfield = new STextField("Unstored Text", "UNSTOREDTEXT", "Unstored Text", 4000, "", false,
				this, false, false, false, null);
		unstoredtextfield.setTextBusinessData(this.getUnstoredtext());
		mainband.addElement(unstoredtextfield);
		mainband.addElement(new SPageText("Below is shown the text with roundtrip to server and storage to database.",
				SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SPageText(this.getStoredtext(), SPageText.TYPE_NORMAL, this));
		STextField storedtextfield = new STextField("Stored Text", "STOREDTEXT", "Stored Text", 4000, "", false, this,
				false, false, false, null);
		storedtextfield.setTextBusinessData(this.getStoredtext());
		mainband.addElement(storedtextfield);

		return mainband;
	}

}
