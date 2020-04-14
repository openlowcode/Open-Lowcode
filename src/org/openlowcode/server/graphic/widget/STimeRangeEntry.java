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
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.DateDataEltType;

/**
 * A field to enter a range in time, typically when searching objects created or
 * updated during this range in time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.6
 */
public class STimeRangeEntry
		extends
		SPageNode {

	private String label;
	private String datafieldname;
	private String helper;

	/**
	 * Creates a TimeRangeEntry
	 * @param parentpage parent page
	 * @param label label shown in the GUI
	 * @param datafieldname unique name of the field in the page tree
	 * @param helper helper
	 */
	public STimeRangeEntry(SPage parentpage, String label, String datafieldname, String helper) {
		super(parentpage);
		this.label = label;
		this.datafieldname = datafieldname;
		this.helper = helper;

	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(datafieldname, this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);

	}

	@Override
	public String getWidgetCode() {
		return "TRE";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	/**
	 * @return the start of the time range as entered by the user, or null if no
	 *         range is entered
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getTimeRangeStart() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a,"START"));
	}

	/**
	 * @return the end of the time range, as entered by the user, or null if either
	 *         <ul>
	 *         <li>no range is entered</li>
	 *         <li>the range entered ends now (e.g. preset selected: today, last 15
	 *         minutes...</li>
	 *         </ul>
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getTimeRangeEnd() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a,"END"));
	}
}
