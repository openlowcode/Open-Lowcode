/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;
import java.util.function.Function;
import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DateDataEltType;

/**
 * A field storing a timeslot (start date and end date)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class STimeslotField
		extends
		SPageNode
		implements
		SDefaultPath {

	public static final int DEFAULT_EMPTY = 0;
	public static final int DEFAULT_TODAY = 1;

	private String startfieldlabel;
	private String endfieldlabel;
	private String startfieldhelper;
	private String endfieldhelper;
	private DateDataElt startdata;
	private DateDataElt enddata;

	private String widgetname;
	private int defaultvalue;
	private boolean timeedit;

	/**
	 * creates a field to store a timeslot
	 * 
	 * @param widgetname       unique name of the widget in the path
	 * @param startfieldlabel  label of the start time field
	 * @param endfieldlabel    label of the end time field
	 * @param startfieldhelper helper of the start time field
	 * @param endfieldhelper   helper of the end time field
	 * @param defaultvalue     default value to either empty or today for date
	 * @param startdata        start date data reference as input argument of the
	 *                         page
	 * @param enddata          end date data reference as input argument of the page
	 * @param timeedit         if true, time-edit is allowed, else only entry of
	 *                         date
	 * @param parentpage       parent page
	 */
	public STimeslotField(
			String widgetname,
			String startfieldlabel,
			String endfieldlabel,
			String startfieldhelper,
			String endfieldhelper,
			int defaultvalue,
			DateDataElt startdata,
			DateDataElt enddata,
			boolean timeedit,
			SPage parentpage) {

		super(parentpage);
		this.widgetname = widgetname;
		this.startfieldlabel = startfieldlabel;
		this.endfieldlabel = endfieldlabel;
		this.startfieldhelper = startfieldhelper;
		if (this.startfieldhelper == null)
			this.startfieldhelper = "";

		this.endfieldhelper = endfieldhelper;
		if (this.endfieldhelper == null)
			this.endfieldhelper = "";

		this.defaultvalue = defaultvalue;
		this.startdata = startdata;
		this.enddata = enddata;
		this.timeedit = timeedit;
	}

	@Override
	public String getPathName() {
		return this.widgetname;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("WGN", this.widgetname);
		writer.addBooleanField("TME", this.timeedit);
		writer.addStringField("STL", this.startfieldlabel);
		writer.addStringField("ENL", this.endfieldlabel);
		writer.addStringField("STH", this.startfieldhelper);
		writer.addStringField("ENH", this.endfieldhelper);
		writer.addIntegerField("DFV", defaultvalue);
		if (this.startdata != null) {
			writer.addBooleanField("HSD", true);
			startdata.writeReferenceToCML(writer);
		} else {
			writer.addBooleanField("HSD", false);
		}
		if (this.enddata != null) {
			writer.addBooleanField("HED", true);
			enddata.writeReferenceToCML(writer);
		} else {
			writer.addBooleanField("HED", false);
		}

	}

	/**
	 * get start date reference to be used as argument for an action
	 * 
	 * @return start date reference
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getStartDateInput() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a, "STARTDATE"));
	}

	/**
	 * get end date reference to be used as argument for an action
	 * 
	 * @return end date reference
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getEndDateInput() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a, "ENDDATE"));
	}

	@Override
	public String getWidgetCode() {
		return "TSF";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
