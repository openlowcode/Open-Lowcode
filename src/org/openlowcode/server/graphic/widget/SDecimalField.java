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
import java.math.BigDecimal;

import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.DecimalDataElt;

/**
 * A field allowing the edition of a decimal field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SDecimalField extends SPageNode implements SDefaultPath {
	private String label;
	private String datafieldname;
	private String helper;
	private int scale;
	private int precision;
	private BigDecimal defaultvalue;
	private boolean businessparameter;
	private DecimalDataElt linkeddata;
	private SDecimalFormatter decimalformatter;
	private int encryptionmode;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private int prefereddisplayintable;
	private SActionRef action;

	/**
	 * Creates a new Decimal Field
	 * 
	 * @param label             user-friendly label in the main application language
	 * @param datafieldname     unique name of the field in the object
	 * @param helper            long explanation for mouse roll-over
	 * @param precision         precision in the sense of java BigDecimal (total
	 *                          number of digits)
	 * @param scale             scale in the sense of java BigDecimal (number of
	 *                          digits right of comma)
	 * @param defaultvalue      a default value if no data is provided
	 * @param businessparameter true if the field is standalone, false if the field
	 *                          is part of an object
	 * @param parent            parent page of the component
	 * @param readonly          true if field should be read-only
	 * @param showintitle       if in an object, put the field in title
	 * @param showinbottomnotes if in an object, put the field in the bottom notes
	 * @param decimalformatter  a decimal formatter
	 * @param action            action to trigger in case the user double clicks
	 */
	public SDecimalField(String label, String datafieldname, String helper, int precision, int scale,
			BigDecimal defaultvalue, boolean businessparameter, SPage parent, boolean readonly, boolean showintitle,
			boolean showinbottomnotes, SDecimalFormatter decimalformatter, SActionRef action) {
		super(parent);
		this.label = label;
		this.datafieldname = datafieldname;
		this.helper = helper;
		if (this.helper == null)
			this.helper = "";
		this.scale = scale;
		this.precision = precision;
		this.defaultvalue = defaultvalue;
		this.businessparameter = businessparameter;
		this.linkeddata = null;
		this.decimalformatter = decimalformatter;
		this.encryptionmode = 0;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.prefereddisplayintable = -1;
	}

	/**
	 * @param prefereddisplayintable the width of the field in characters if
	 *                               displayed in a table
	 */
	public void setPreferedDisplayInTable(int prefereddisplayintable) {
		this.prefereddisplayintable = prefereddisplayintable;
	}

	/**
	 * forces the field to read-only
	 */
	public void setReadOnly() {
		this.readonly = true;
	}

	@Override
	public String getPathName() {
		return this.datafieldname;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);
		writer.addIntegerField("PRC", precision);
		writer.addIntegerField("SCL", scale);

		writer.addDecimalField("DFV", defaultvalue);
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
		writer.addBooleanField("HDF", (this.decimalformatter != null));
		if (this.decimalformatter != null) {
			writer.startStructure("DEF");
			decimalformatter.writepayload(writer);
			writer.endStructure("DEF");
		}
	}

	@Override
	public String getWidgetCode() {
		return "DCF";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
