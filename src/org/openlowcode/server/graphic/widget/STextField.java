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

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * Definition of a text business field as a standalone element. It can be used
 * in 3 modes:
 * <ul>
 * <li>pre-filled with hardcoded value (can be null of empty string)</li>
 * <li>pre-filled with value coming from a business parameter</li>
 * <li>empty</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class STextField
		extends
		SPageNode
		implements
		SDefaultPath {

	private String label;
	private String datafieldname;
	private String helper;
	private int maxlength;
	private String defaultvalue;
	private boolean businessparameter;
	private TextDataElt linkeddata;
	private ArrayDataElt<TextDataElt> suggestions;
	private boolean hidedisplay;
	private int encryptionmode;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private int prefereddisplayintable;
	private boolean richtextedit;
	private SActionRef action;
	private boolean orderasinteger;
	private int integeroffset;
	private boolean compactshow = false;
	private boolean twolines = false;
	private boolean nosmallfield = false;

	/**
	 * creates a text field
	 * 
	 * @param label             human-readable nice label
	 * @param datafieldname     field name
	 * @param helper            roll-over tip
	 * @param maxlength         maximum length of the text field
	 * @param defaultvalue      default value of the text field
	 * @param businessparameter true if field is business parameter
	 * @param parent            parent page
	 * @param readonly          true if read only
	 * @param showintitle       true if field is to be shown in title and central
	 *                          section of object display
	 * @param showinbottomnotes true if field is to beshown only in bottom notes
	 * @param action            action to launch by default if enter is typed on
	 *                          this field
	 * @param richtextedit      if true, rich text edit is used
	 * @param nosmallfield      if true, fields cannot be displayed small and will
	 *                          be large
	 */
	public STextField(
			String label,
			String datafieldname,
			String helper,
			int maxlength,
			String defaultvalue,
			boolean businessparameter,
			SPage parent,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action,
			boolean richtextedit,
			boolean nosmallfield) {
		this(label, datafieldname, helper, maxlength, defaultvalue, businessparameter, parent, readonly, showintitle,
				showinbottomnotes, action, richtextedit);
		this.nosmallfield = nosmallfield;
	}

	/**
	 * creates a text field
	 * 
	 * @param label             human-readable nice label
	 * @param datafieldname     field name
	 * @param helper            roll-over tip
	 * @param maxlength         maximum length of the text field
	 * @param defaultvalue      default value of the text field
	 * @param businessparameter true if field is business parameter
	 * @param parent            parent page
	 * @param readonly          true if read only
	 * @param showintitle       true if field is to be shown in title and central
	 *                          section of object display
	 * @param showinbottomnotes true if field is to beshown only in bottom notes
	 * @param action            action to launch by default if enter is typed on
	 *                          this field
	 * @param richtextedit      if true, rich text edit is used
	 */
	public STextField(
			String label,
			String datafieldname,
			String helper,
			int maxlength,
			String defaultvalue,
			boolean businessparameter,
			SPage parent,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action,
			boolean richtextedit) {
		this(label, datafieldname, helper, maxlength, defaultvalue, businessparameter, parent, readonly, showintitle,
				showinbottomnotes, action);
		this.richtextedit = richtextedit;
	}

	/**
	 * @param twolines
	 */
	public void setCompactShow(boolean twolines) {
		this.compactshow = true;
		this.twolines = twolines;
	}

	/**
	 * creates a text field
	 * 
	 * @param label         human-readable nice label
	 * @param datafieldname field name
	 * @param helper        roll-over tip
	 * @param maxlength     maximum length of the text field
	 * @param defaultvalue  default value of the text field
	 * @param parent        parent page
	 * @param action        action to trigger if return is pressed while editing the
	 *                      text field
	 */
	public STextField(
			String label,
			String datafieldname,
			String helper,
			int maxlength,
			String defaultvalue,
			SPage parent,
			SActionRef action) {
		this(label, datafieldname, helper, maxlength, defaultvalue, false, parent, false, false, false, action);
	}

	/**
	 * creates a text field
	 * 
	 * @param label             human-readable nice label
	 * @param datafieldname     field name
	 * @param helper            roll-over tip
	 * @param maxlength         maximum length of the text field
	 * @param defaultvalue      default value if field is empty
	 * @param businessparameter true if business parameter (probably obsolete)
	 * @param parent            parent page
	 * @param readonly          true if read only
	 * @param showintitle       true if field is to be shown in title and central
	 *                          section of object display
	 * @param showinbottomnotes true if field is to beshown only in bottom notes
	 * @param action            action to launch by default if enter is typed on
	 *                          this field
	 */
	public STextField(
			String label,
			String datafieldname,
			String helper,
			int maxlength,
			String defaultvalue,
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
		this.maxlength = maxlength;
		this.defaultvalue = defaultvalue;
		if (this.defaultvalue == null)
			this.defaultvalue = "";
		this.businessparameter = businessparameter;
		this.linkeddata = null;
		this.hidedisplay = false;
		this.encryptionmode = 0;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.prefereddisplayintable = -1;
		this.richtextedit = false;
		this.orderasinteger = false;
	}

	/**
	 * sets the number of characters of the prefix before the field is numeric
	 * 
	 * @param integeroffset offset (length of prefix)
	 */
	public void setOrderAsInteger(int integeroffset) {
		this.orderasinteger = true;
		this.integeroffset = integeroffset;
	}

	/**
	 * sets the default number of columns to be displayed for the field
	 * 
	 * @param prefereddisplayintable default number of columns to be displayed for
	 *                               the field
	 */
	public void setPreferedDisplayInTable(int prefereddisplayintable) {
		this.prefereddisplayintable = prefereddisplayintable;
	}

	/**
	 * if true, field is set to read-only
	 */
	public void setReadOnly() {
		this.readonly = true;
	}

	/**
	 * defines that the field should not show the text being entered. This is
	 * typical for passwords
	 */
	public void hideDisplay() {
		this.hidedisplay = true;

	}

	/**
	 * sets the encryption mode (obsolete not used anymore)
	 * 
	 * @param encryptionmode encryption mode (obsolete)
	 */
	public void setEncryptionMode(int encryptionmode) {
		this.encryptionmode = encryptionmode;
	}

	/**
	 * sets the parameter of the page input data to feed this field at
	 * initialization
	 * 
	 * @param data reference to a text data element
	 */
	public void setTextBusinessData(TextDataElt data) {
		this.linkeddata = data;
	}

	/**
	 * adds suggestions for the text. This only works with short text (length
	 * smaller than 100) with no Rich-Text formatting
	 * 
	 * @param suggestions a list of text suggestions
	 */
	public void setSuggestions(ArrayDataElt<TextDataElt> suggestions) {
		this.suggestions = suggestions;
	}

	/**
	 * gets the text input to be provided into an action as argument
	 * 
	 * @return reference to the text input content
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getTextInput() {
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	/**
	 * @param all if true, all suggestions are brought back, if false, selected suggestions are brought back
	 * @return reference to the array of text for suggestions
	 * @since 1.6
	 */
	public Function<SActionInputDataRef<ArrayDataEltType<TextDataEltType>>,SActionDataLoc<ArrayDataEltType<TextDataEltType>>> getSuggestions(boolean all) {
		return (a) -> (new SActionDataLoc<ArrayDataEltType<TextDataEltType>>(this,a,(all?"FULL":"SELECTED")));
	}
	
	@Override
	public String getPathName() {
		return this.datafieldname;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {

		writer.addStringField("LBL", label);
		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);
		writer.addIntegerField("MXL", maxlength);
		writer.addBooleanField("OAI", this.orderasinteger);
		if (this.orderasinteger)
			writer.addIntegerField("INO", this.integeroffset);
		writer.addStringField("DFV", defaultvalue);
		writer.addBooleanField("BSP", businessparameter);

		boolean externalreference = false;
		if (linkeddata != null)
			externalreference = true;

		writer.addBooleanField("EXR", externalreference);
		if (linkeddata != null)
			linkeddata.writeReferenceToCML(writer);
		writer.addBooleanField("HID", hidedisplay);
		writer.addIntegerField("ECR", encryptionmode);
		writer.addBooleanField("ROY", readonly);
		if (action != null)
			action.writeToCML(writer);
		writer.addBooleanField("SIT", this.showintitle);
		if (this.prefereddisplayintable != -1)
			writer.addIntegerField("PDT", this.prefereddisplayintable);
		writer.addBooleanField("SBN", this.showinbottomnotes);
		writer.addBooleanField("RCH", this.richtextedit);
		writer.addBooleanField("CPS", this.compactshow);
		writer.addBooleanField("TWL", this.twolines);
		writer.addBooleanField("NSF", this.nosmallfield);
		writer.addBooleanField("HSG",(this.suggestions!=null));
		if (this.suggestions!=null) this.suggestions.writeReferenceToCML(writer);
	}

	@Override
	public String getWidgetCode() {
		return "TXF";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
