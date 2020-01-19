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
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.IntegerDataEltType;

/**
 * A field storing an integer payload
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SIntegerField
		extends
		SPageNode
		implements
		SDefaultPath {
	private String label;
	private String datafieldname;
	private String helper;
	private Integer defaultvalue;
	private boolean businessparameter;
	private IntegerDataElt linkeddata;

	private int encryptionmode;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private int prefereddisplayintable;
	private SActionRef action;

	/**
	 * creates an integer field
	 * 
	 * @param label             labek of the field in default language
	 * @param datafieldname     unique name of the field in the next parent
	 *                          significant widget
	 * @param helper            roll-over tip
	 * @param linkeddata        data to show (input of the page)
	 * @param businessparameter true if business parameter
	 * @param parent            parent page
	 * @param readonly          true if read-only
	 * @param showintitle       true if shown both in title and in main section of
	 *                          the data object
	 * @param showinbottomnotes true if shown in bottom notes of the data object
	 * @param action            default action when enter is pressed inside the
	 *                          field in edition mode
	 */
	public SIntegerField(
			String label,
			String datafieldname,
			String helper,
			IntegerDataElt linkeddata,
			boolean businessparameter,
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

		this.defaultvalue = null;
		if (this.defaultvalue == null)
			this.defaultvalue = new Integer(0);
		this.businessparameter = businessparameter;
		this.linkeddata = linkeddata;

		this.encryptionmode = 0;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.prefereddisplayintable = -1;
	}

	/**
	 * creates an integer field with hardcoded default value
	 * 
	 * @param label             labek of the field in default language
	 * @param datafieldname     unique name of the field in the next parent
	 *                          significant widget
	 * @param helper            roll-over tip
	 * @param defaultvalue      an hard-coded integer value
	 * @param businessparameter true if business parameter
	 * @param parent            parent page
	 * @param readonly          true if read-only
	 * @param showintitle       true if shown both in title and in main section of
	 *                          the data object
	 * @param showinbottomnotes true if shown in bottom notes of the data object
	 * @param action            default action when enter is pressed inside the
	 *                          field in edition mode
	 */
	public SIntegerField(
			String label,
			String datafieldname,
			String helper,
			Integer defaultvalue,
			boolean businessparameter,
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
		if (this.defaultvalue == null)
			this.defaultvalue = new Integer(0);
		this.businessparameter = businessparameter;
		this.linkeddata = null;

		this.encryptionmode = 0;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.prefereddisplayintable = -1;
	}

	/**
	 * sets the width of the column when shown as an object array
	 * 
	 * @param prefereddisplayintable width of the column in characters
	 */
	public void setPreferedDisplayInTable(int prefereddisplayintable) {
		this.prefereddisplayintable = prefereddisplayintable;
	}

	/**
	 * gets a reference to the field payload to be used as input attribute for a
	 * page action
	 * 
	 * @return reference to the field payload
	 */
	public Function<SActionInputDataRef<IntegerDataEltType>, SActionDataLoc<IntegerDataEltType>> getIntegerInput() {
		return (a) -> (new SActionDataLoc<IntegerDataEltType>(this, a));
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData data, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);

		writer.addIntegerField("DFV", defaultvalue);
		writer.addBooleanField("BSP", businessparameter);

		boolean externalreference = false;
		if (linkeddata != null)
			externalreference = true;

		writer.addBooleanField("EXR", externalreference);
		if (linkeddata != null)
			linkeddata.writeReferenceToCML(writer);

		writer.addIntegerField("ECR", encryptionmode);
		writer.addBooleanField("ROY", readonly);
		if (action != null)
			action.writeToCML(writer);
		writer.addBooleanField("SIT", this.showintitle);
		if (this.prefereddisplayintable != -1)
			writer.addIntegerField("PDT", this.prefereddisplayintable);
		writer.addBooleanField("SBN", this.showinbottomnotes);

	}

	@Override
	public String getWidgetCode() {
		return "INF";
	}

	@Override
	public boolean hideComponent(SPageData pagedata, SecurityBuffer buffer) {

		return false;
	}

	@Override
	public String getPathName() {
		return this.datafieldname;
	}

}
