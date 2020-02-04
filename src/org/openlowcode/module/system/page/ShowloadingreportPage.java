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

import org.openlowcode.module.system.data.Csvloadererror;
import org.openlowcode.module.system.data.CsvloadererrorDefinition;
import org.openlowcode.module.system.page.generated.AbsShowloadingreportPage;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SIntegerField;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * show the report of loading objects directly from the object search page
 * (without parent specified)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowloadingreportPage
		extends
		AbsShowloadingreportPage {

	/**
	 * creates the page to show a loading report
	 * 
	 * @param loadingcontext loading context, provides the identity of the parent
	 * @param inserted       number of objects inserted
	 * @param updated        number of objects updated
	 * @param errors         number of errors resulting in the object not being
	 *                       inserted or updated
	 * @param postprocerrors number of errors on other data (typically links from
	 *                       this object to another object, children objects...)
	 * @param loadingtime    loading time
	 * @param errordetail    list of errors
	 */
	public ShowloadingreportPage(
			String loadingcontext,
			Integer inserted,
			Integer updated,
			Integer errors,
			Integer postprocerrors,
			Integer loadingtime,
			Csvloadererror[] errordetail) {
		super(loadingcontext, inserted, updated, errors, postprocerrors, loadingtime, errordetail);

	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Loading report", SPageText.TYPE_TITLE, this));
		mainband.addElement(new SPageText(this.getLoadingcontext(), SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SIntegerField("Inserted", "INSERTED", "Number of new objects inserted",
				this.getInserted(), false, this, true, false, false, null));
		mainband.addElement(new SIntegerField("Updated", "UPDATED", "Number of objects modified", this.getUpdated(),
				false, this, true, false, false, null));

		mainband.addElement(new SPageText("Errors below result in objects not being inserted or updated.",
				SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SIntegerField("Errors", "ERRORS", "objets not processed", this.getErrors(), false, this,
				true, false, false, null));

		mainband.addElement(new SPageText(
				"Errors below did not prevent objects from being inserted or updated, but links may be missing.",
				SPageText.TYPE_NORMAL, this));
		mainband.addElement(new SIntegerField("Post-Processing Errors", "POSTPROCERRORS",
				"errors processing links and secondary objects", this.getPostprocerrors(), false, this, true, false,
				false, null));
		mainband.addElement(new SIntegerField("Loading Time (s)", "LOADINGTIME",
				"total processign for the file. This excludes network transfer time", this.getLoadingtime(), false,
				this, true, false, false, null));

		mainband.addElement(new SPageText("Error Details", SPageText.TYPE_TITLE, this));
		SObjectArray<Csvloadererror> errorarray = new SObjectArray<Csvloadererror>("ERRORDETAILS",
				this.getErrordetail(), CsvloadererrorDefinition.getCsvloadererrorDefinition(), this);
		errorarray.forceRowHeight(3);
		mainband.addElement(errorarray);
		return mainband;
	}

	@Override
	public String generateTitle(
			String loadingcontext,
			Integer inserted,
			Integer updated,
			Integer errors,
			Integer postprocerrors,
			Integer loadingtime,
			Csvloadererror[] errordetail) {
		return "Error Report for" + loadingcontext;
	}

}
