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

import org.openlowcode.module.system.action.DisplaygenericobjectAction;
import org.openlowcode.module.system.data.Csvloadererror;
import org.openlowcode.module.system.data.CsvloadererrorDefinition;
import org.openlowcode.module.system.page.generated.AbsShowloadingreportforchildrenPage;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SIntegerField;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SObjectIdStorage;
import org.openlowcode.server.graphic.widget.SPageText;

/**
 * This page shows the loading report when importing children from a parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowloadingreportforchildrenPage
		extends
		AbsShowloadingreportforchildrenPage {

	/**
	 * creates the page
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
	 * @param objectid       id of the parent to be able to go back to it
	 */
	public ShowloadingreportforchildrenPage(
			String loadingcontext,
			Integer inserted,
			Integer updated,
			Integer errors,
			Integer postprocerrors,
			Integer loadingtime,
			Csvloadererror[] errordetail,
			DataObjectId<?> objectid) {
		super(loadingcontext, inserted, updated, errors, postprocerrors, loadingtime, errordetail, objectid);

	}


	@Override
	public String generateTitle(
			String loadingcontext,
			Integer inserted,
			Integer updated,
			Integer errors,
			Integer postprocerrors,
			Integer loadingtime,
			Csvloadererror[] errordetail,
			@SuppressWarnings("rawtypes") DataObjectId objectid) {
		return "Error Report for" + loadingcontext;
	}

	@SuppressWarnings("unchecked")
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
		DisplaygenericobjectAction.ActionRef displayobject = DisplaygenericobjectAction.get().getActionRef();
		@SuppressWarnings("rawtypes")
		SObjectIdStorage parentidstorage = new SObjectIdStorage("PARENTIDSTORAGE", this, this.getObjectid());
		mainband.addElement(parentidstorage);
		displayobject.setGenericid(parentidstorage.getObjectIdInput());
		SActionButton displayobjectbutton = new SActionButton("Back", displayobject, this);
		mainband.addElement(displayobjectbutton);
		mainband.addElement(new SPageText("Error Details", SPageText.TYPE_TITLE, this));
		SObjectArray<Csvloadererror> errorarray = new SObjectArray<Csvloadererror>("ERRORDETAILS",
				this.getErrordetail(), CsvloadererrorDefinition.getCsvloadererrorDefinition(), this);
		errorarray.forceRowHeight(3);
		mainband.addElement(errorarray);
		return mainband;
	}

}
