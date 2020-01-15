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

import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.ChoiceDataEltType;

/**
 * 
 * a widget displaying a list of value field stored as text. It offers the
 * following:
 * <ul>
 * <li>choice between preset values</li>
 * <li>different stored value and display value</li>
 * <li>a tooltip text that can be multiline for each value</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class SChoiceTextField<E extends FieldChoiceDefinition<E>>
		extends
		SPageNode
		implements
		SDefaultPath {

	private String label;
	private String datafieldname;
	private String helper;
	private E choicedefinition;
	private ChoiceValue<E> defaultvalue;
	private boolean businessparameter;
	private boolean compactshow;
	@SuppressWarnings("rawtypes")
	private ChoiceDataElt linkeddata;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private SActionRef action;
	private int prefereddisplayintable;

	private boolean twolines;

	/**
	 * sets the linked data to put in the choice text field
	 * 
	 * @param linkeddata linked data
	 */
	public void setLinkedData(@SuppressWarnings("rawtypes") ChoiceDataElt linkeddata) {
		this.linkeddata = linkeddata;
	}

	/**
	 * creates a choice text field
	 * 
	 * @param label            label of the field (what is shown in default language
	 * @param datafieldname    name of the data field (without spaces or special
	 *                         characters
	 * @param helper           tooltip shown in roll-over mouse
	 * @param choicedefinition definition of the choice value
	 * @param linkeddata       linked data in the action
	 * @param parentpage       parent page for the widget
	 * @param readonly         if true, field is only read-only
	 * @param action           action ot launch by default after value is chosen
	 */
	@SuppressWarnings("rawtypes")
	public SChoiceTextField(
			String label,
			String datafieldname,
			String helper,
			E choicedefinition,
			ChoiceDataElt linkeddata,
			SPage parentpage,
			boolean readonly,
			SActionRef action) {
		this(label, datafieldname, helper, choicedefinition, null, parentpage, true, readonly, false, false, action);
		this.setLinkedData(linkeddata);
	}

	/**
	 * creates a choice text field
	 * 
	 * @param label             label of the field (what is shown in default
	 *                          language
	 * @param datafieldname     name of the data field (without spaces or special
	 *                          characters
	 * @param helper            tooltip shown in roll-over mouse
	 * @param choicedefinition  definition of the choice value
	 * @param defaultvalue      value to show if not input is given
	 * @param parentpage        parent business page
	 * @param businessparameter true if business parameter
	 * @param readonly          true if read-only
	 * @param showintitle       when part of a data object, show the field in title
	 *                          (and also in normal seciton)
	 * @param showinbottomnotes when part of a data obejct, show only in the bottom
	 *                          notes
	 * @param action            default action to launch
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public SChoiceTextField(
			String label,
			String datafieldname,
			String helper,
			E choicedefinition,
			ChoiceValue defaultvalue,
			SPage parentpage,
			boolean businessparameter,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action) {
		this(label, datafieldname, helper, choicedefinition, parentpage, businessparameter, readonly, showintitle,
				showinbottomnotes, action);
		this.defaultvalue = defaultvalue;
	}

	/**
	 * creates a choice text field
	 * 
	 * @param label             label of the field (what is shown in default
	 *                          language
	 * @param datafieldname     name of the data field (without spaces or special
	 *                          characters
	 * @param helper            tooltip shown in roll-over mouse
	 * @param choicedefinition  definition of the choice value
	 * @param parentpage        parent business page
	 * @param businessparameter true if business parameter
	 * @param readonly          true if read-only
	 * @param showintitle       when part of a data object, show the field in title
	 *                          (and also in normal seciton)
	 * @param showinbottomnotes when part of a data obejct, show only in the bottom
	 *                          notes
	 * @param action            default action to launch
	 */
	public SChoiceTextField(
			String label,
			String datafieldname,
			String helper,
			E choicedefinition,
			SPage parentpage,
			boolean businessparameter,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			SActionRef action) {
		super(parentpage);
		this.label = label;
		this.datafieldname = datafieldname;
		this.helper = helper;
		if (helper == null)
			helper = "";
		this.choicedefinition = choicedefinition;

		this.businessparameter = businessparameter;
		this.linkeddata = null;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.readonly = readonly;
		this.prefereddisplayintable = -1;
		this.compactshow = false;
		this.twolines = false;

	}

	/**
	 * if true, the field will be compact, if false, the field will align on general
	 * object layout
	 */
	public void setCompactShow() {
		this.compactshow = true;
		this.twolines = false;
	}

	/**
	 * sets compact show, potentially showing the label on a top line, and the
	 * choice box on the bottom line
	 * 
	 * @param twolines true if two lines
	 */
	public void setCompactShow(boolean twolines) {
		this.compactshow = true;
		this.twolines = twolines;
	}

	/**
	 * if used in a data object, allows to set the default width of the column
	 * 
	 * @param prefereddisplayintable width of the column in characters
	 */
	public void setPreferedDisplayInTable(int prefereddisplayintable) {
		this.prefereddisplayintable = prefereddisplayintable;
	}

	@Override
	public String getPathName() {
		return this.datafieldname;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", label);
		writer.addBooleanField("CPS", compactshow);
		writer.addBooleanField("TWL", twolines);

		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);

		if (defaultvalue != null) {
			writer.addStringField("DFC", defaultvalue.getStorageCode());
		}
		writer.addBooleanField("BSP", businessparameter);

		boolean externalreference = false;
		if (linkeddata != null)
			externalreference = true;

		writer.addBooleanField("EXR", externalreference);
		if (linkeddata != null)
			linkeddata.writeReferenceToCML(writer);
		;
		writer.startStructure("CCLS");
		for (int i = 0; i < this.choicedefinition.getChoiceValue().length; i++) {
			ChoiceValue<E> choicevalue = this.choicedefinition.getChoiceValue()[i];
			writer.startStructure("CCL");
			writer.addStringField("STV", choicevalue.getName());
			writer.addStringField("DSV", choicevalue.getDisplayValue());
			writer.addStringField("HLP", choicevalue.getTooltip());
			writer.endStructure("CCL");
		}
		writer.endStructure("CCLS");
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
		return "CTF";
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
				if (thismanager.isAuthorizedForCurrentUser("SChoiceTextField " + this.label, actiondata, buffer))
					return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @return an attribute having the content of the choice field
	 */
	public Function<SActionInputDataRef<ChoiceDataEltType>, SActionDataLoc<ChoiceDataEltType>> getChoiceInput() {
		return (a) -> (new SActionDataLoc<ChoiceDataEltType>(this, a));
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
