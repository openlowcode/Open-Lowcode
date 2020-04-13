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
import java.util.logging.Logger;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.TimePeriodDataElt;
import org.openlowcode.tools.structure.TimePeriodDataEltType;

/**
 * A field allowing to edit a time-period. A time period is a time interval
 * typically used for activity and finance reporting (year, month, quarter...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class STimePeriodField extends SPageNode implements SDefaultPath {
	private static Logger logger = Logger.getLogger(STimePeriodField.class.getName());
	private String label;
	private String datafieldname;
	private String helper;
	private TimePeriod.PeriodType periodtype;
	private TimePeriodDataElt linkeddata;
	private boolean businessparameter;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private SActionRef action;
	private int prefereddisplayintable;

	/**
	 * creates a time period field
	 * 
	 * @param label             label shown to the user in the default language
	 * @param datafieldname     name of the data field (should be unique, without
	 *                          special characters and space)
	 * @param helper            a long explanation of the field that will display as
	 *                          roll-over
	 * @param periodtype        type of period (Year, quarter, month...)
	 * @param linkeddata        linked data to put when displaying the field for the
	 *                          first time
	 * @param parentpage        page the widget is created for
	 * @param businessparameter true if business parameter
	 * @param readonly          true if field is read-only
	 * @param showintitle       show the field value in object title if true
	 * @param showinbottomnotes show the field value in object bottom notes only if
	 *                          true
	 * @param action            the action to trigger at edition of the field
	 */
	public STimePeriodField(String label, String datafieldname, String helper, TimePeriod.PeriodType periodtype,
			TimePeriodDataElt linkeddata, SPage parentpage, boolean businessparameter, boolean readonly,
			boolean showintitle, boolean showinbottomnotes, SActionRef action) {
		super(parentpage);
		this.label = label;
		this.datafieldname = datafieldname;
		this.helper = helper;
		this.periodtype = periodtype;
		logger.finest("Period Type = " + this.periodtype);
		this.linkeddata = linkeddata;
		this.businessparameter = businessparameter;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.prefereddisplayintable = -1;
	}

	@Override
	public String getPathName() {
		return this.datafieldname;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));

	}

	/**
	 * sets the linked data
	 * 
	 * @param linkeddata linked data to display and potentially edit
	 */
	public void setLinkedData(TimePeriodDataElt linkeddata) {
		this.linkeddata = linkeddata;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);

		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);

		writer.addBooleanField("BSP", businessparameter);

		boolean externalreference = false;
		if (linkeddata != null)
			externalreference = true;

		writer.addBooleanField("EXR", externalreference);
		if (linkeddata != null)
			linkeddata.writeReferenceToCML(writer);

		writer.addBooleanField("ROY", readonly);
		writer.addBooleanField("ACT", (action != null));
		if (action != null)
			action.writeToCML(writer);
		writer.addBooleanField("SIT", this.showintitle);

		writer.addIntegerField("PDT", this.prefereddisplayintable);
		writer.addBooleanField("SBN", this.showinbottomnotes);
		writer.addStringField("PDT", this.periodtype != null ? this.periodtype.toString() : null);
	}

	@Override
	public String getWidgetCode() {
		return "TPF";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	/**
	 * @return the data hold in the field, to be used as input paramater for an
	 *         action
	 */
	public Function<SActionInputDataRef<TimePeriodDataEltType>, SActionDataLoc<TimePeriodDataEltType>> getTimePeriodInput() {
		return (a) -> (new SActionDataLoc<TimePeriodDataEltType>(this, a));
	}
}
