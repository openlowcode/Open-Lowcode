/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DateDataEltType;

/**
 * A field holding a date as payload. This date can have a time (1st January
 * 1977 06:44) or not (just 1st January 1977), in which case the date stored in
 * system is 1pm GMT
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SDateField
		extends
		SPageNode
		implements
		SDefaultPath {
	public static final int DEFAULT_EMPTY = 0;
	public static final int DEFAULT_TODAY = 1;

	private String label;
	private String datafieldname;
	private String helper;
	private int defaultvalue;
	private boolean businessparameter;
	private DateDataElt linkeddata;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private SActionRef action;
	private boolean compactshow;
	private boolean timeedit;
	private boolean twolines;

	/**
	 * creates a date field that only allows to enter a date (no time)
	 * 
	 * @param label             label in the main language of the application
	 * @param datafieldname     unique name of the widget in the scope of parent
	 *                          widget
	 * @param helper            roll-over tip. Can be long
	 * @param defaultvalue      default value to put if field is empty
	 * @param businessparameter business parameter (to analyze if still needed)
	 * @param parent            parent widget
	 * @param readonly          true if read-only
	 * @param showintitle       if true, show in title of the object and the main
	 *                          section
	 * @param showinbottomnotes if true, show only in bottom notes
	 * @param action            action to trigger when user modifies the value
	 */
	public SDateField(
			String label,
			String datafieldname,
			String helper,
			int defaultvalue,
			boolean businessparameter,
			SPage parent,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action) {
		this(label, datafieldname, helper, defaultvalue, businessparameter, false, parent, readonly, showintitle,
				showinbottomnotes, action);
	}

	/**
	 * @param label             label in the main language of the application
	 * @param datafieldname     unique name of the widget in the scope of parent
	 *                          widget
	 * @param helper            roll-over tip. Can be long
	 * @param defaultvalue      default value to put if field is empty
	 * @param businessparameter business parameter (to analyze if still needed)
	 * @param timeedit          if true, time edition, if false just date edition
	 * @param parent            parent widget
	 * @param readonly          true if read-only
	 * @param showintitle       if true, show in title of the object and the main
	 *                          section
	 * @param showinbottomnotes if true, show only in bottom notes
	 * @param action            action to trigger when user modifies the value
	 */
	public SDateField(
			String label,
			String datafieldname,
			String helper,
			int defaultvalue,
			boolean businessparameter,
			boolean timeedit,
			SPage parent,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action) {
		super(parent);
		this.label = label;
		this.datafieldname = datafieldname;
		this.helper = helper;
		if (this.helper == null)
			this.helper = "";
		this.defaultvalue = defaultvalue;
		this.businessparameter = businessparameter;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.compactshow = false;
		this.twolines = false;
		this.timeedit = timeedit;
	}

	/**
	 * provides a reference to the payload of this field to be used as an action on
	 * the page
	 * 
	 * @return a reference to the payload of this field
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getDateInput() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a));
	}

	@Override
	public String getPathName() {
		return this.datafieldname;
	}

	/**
	 * sets the page input as data for this widget
	 * 
	 * @param data page input attribute of date format
	 */
	public void setDateBusinessData(DateDataElt data) {
		this.linkeddata = data;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);
		writer.addIntegerField("DFV", defaultvalue);
		writer.addBooleanField("BSP", businessparameter);
		writer.addBooleanField("TME", timeedit);
		boolean externalreference = false;
		if (linkeddata != null)
			externalreference = true;
		writer.addBooleanField("EXR", externalreference);
		if (linkeddata != null)
			linkeddata.writeReferenceToCML(writer);
		writer.addBooleanField("ROY", readonly);
		if (action != null)
			action.writeToCML(writer);
		writer.addBooleanField("SIT", this.showintitle);
		writer.addBooleanField("SBN", this.showinbottomnotes);
		writer.addBooleanField("CPS", compactshow);
		writer.addBooleanField("TWL", this.twolines);
	}

	/**
	 * shows the widget as compact (to be used in menus, bars...)
	 */
	public void setCompactShow() {
		this.compactshow = true;
		this.twolines = false;
	}

	/**
	 * shows the widget as compact, potentially on two lines (one for label, one for
	 * date entry widget)
	 * 
	 * @param twolines if true, shows widget on two lines
	 */
	public void setCompactShow(boolean twolines) {
		this.compactshow = true;
		this.twolines = twolines;
	}

	@Override
	public String getWidgetCode() {
		return "DAT";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		if (this.action != null) {
			ActionSecurityManager[] securitymanagers = action.getAction().getActionSecurityManager();
			if (securitymanagers == null)
				return true;
			if (buffer == null)
				return false; // this happens only on login page
			for (int i = 0; i < securitymanagers.length; i++) {
				ActionSecurityManager thismanager = securitymanagers[i];
				SActionData actiondata = action.generatePotentialActionDataForSecurity(input);
				if (thismanager.isAuthorizedForCurrentUser("SDateField " + this.label, actiondata, buffer))
					return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
