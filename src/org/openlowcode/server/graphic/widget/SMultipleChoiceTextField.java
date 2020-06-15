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
import java.util.HashMap;
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
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.MultipleChoiceDataEltType;

/**
 * A field allowing to show several values of a choice being selected.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of choice used in this multiple choice text field
 */
public class SMultipleChoiceTextField<E extends FieldChoiceDefinition<E>>
		extends
		SPageNode
		implements
		SDefaultPath {

	private String label;
	private String datafieldname;
	private String helper;
	private E choicedefinition;
	private SActionRef action;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private boolean businessparameter;
	@SuppressWarnings("rawtypes")
	private MultipleChoiceDataElt multiplechoicelinkeddata;
	private int prefereddisplayintable;
	private boolean compactshow;
	private boolean twolines;
	private HashMap<String, ChoiceValue<E>> preselectedvalues;

	/**
	 * sets linked data to be shown
	 * 
	 * @param multiplechoicelinkedata linked data
	 */
	@SuppressWarnings("rawtypes")
	public void setLinkedData(MultipleChoiceDataElt multiplechoicelinkedata) {
		this.multiplechoicelinkeddata = multiplechoicelinkedata;
	}

	/**
	 * creates a multiple choice text field
	 * 
	 * @param label             label of the field to show
	 * @param datafieldname     unique name in the context of the next significant
	 *                          parent
	 * @param helper            roll-over helper
	 * @param choicedefinition  definition of the choice (possible values)
	 * @param parentpage        parent page
	 * @param businessparameter true if business parameter
	 * @param readonly          true if read-only
	 * @param showintitle       true if show also in title when part of an object
	 *                          display
	 * @param showinbottomnotes true if show only in bottom notes when part of an
	 *                          object display
	 * @param action            action to trigger after edition is done (not active)
	 */
	public SMultipleChoiceTextField(
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
		this.choicedefinition = choicedefinition;

		this.businessparameter = businessparameter;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottomnotes = showinbottomnotes;
		this.action = action;
		this.preselectedvalues = new HashMap<String, ChoiceValue<E>>();
	}

	public void setPreselectedValue(ChoiceValue<E> preselectedvalue) {
		this.preselectedvalues.put(preselectedvalue.getStorageCode(), preselectedvalue);
	}

	/**
	 * field is shown in a compact way (no space between label and choice entry)
	 */
	public void setCompactShow() {
		this.compactshow = true;
		this.twolines = false;
	}

	/**
	 * @param twolines if true,field is shown in two lines (content below label)
	 */
	public void setCompactShow(boolean twolines) {
		this.compactshow = true;
		this.twolines = twolines;
	}

	/**
	 * if field is displayed in table of objects, set width of the table column in
	 * characters
	 * 
	 * @param prefereddisplayintable width of the table column in characters
	 */
	public void setPreferedDisplayInTable(int prefereddisplayintable) {
		this.prefereddisplayintable = prefereddisplayintable;
	}

	/**
	 * gets a reference to the data hold in this field
	 * 
	 * @return a reference to be used as input attribute of one of the pages actions
	 */
	public Function<
			SActionInputDataRef<MultipleChoiceDataEltType>,
			SActionDataLoc<MultipleChoiceDataEltType>> getMultipleChoiceArrayInput() {
		return (a) -> (new SActionDataLoc<MultipleChoiceDataEltType>(this, a));
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
		writer.addBooleanField("CPS", compactshow);
		writer.addBooleanField("TWL", twolines);

		writer.addStringField("DFN", datafieldname);
		writer.addStringField("HPR", helper);

		writer.addBooleanField("BSP", businessparameter);

		boolean externalreference = false;
		if (multiplechoicelinkeddata != null)
			externalreference = true;

		writer.addBooleanField("EXR", externalreference);
		if (multiplechoicelinkeddata != null)
			multiplechoicelinkeddata.writeReferenceToCML(writer);

		writer.startStructure("CCLS");
		for (int i = 0; i < this.choicedefinition.getChoiceValue().length; i++) {
			ChoiceValue<E> choicevalue = this.choicedefinition.getChoiceValue()[i];
			writer.startStructure("CCL");
			writer.addStringField("STV", choicevalue.getName());
			writer.addStringField("DSV", choicevalue.getDisplayValue());
			writer.addStringField("HLP", choicevalue.getTooltip());
			if (this.preselectedvalues.containsKey(choicevalue.getStorageCode())) {
				writer.addBooleanField("PSL",true);
			} else {
				writer.addBooleanField("PSL",false);
			}
			writer.endStructure("CCL");
		}
		writer.endStructure("CCLS");
		writer.addBooleanField("ROY", readonly);
		if (action != null) {
			writer.addBooleanField("HAC", true);
			action.writeToCML(writer);
		} else {
			writer.addBooleanField("HAC", false);
		}
		writer.addBooleanField("SIT", this.showintitle);
		writer.addIntegerField("PDT", this.prefereddisplayintable);
		writer.addBooleanField("SBN", this.showinbottomnotes);

	}

	@Override
	public String getWidgetCode() {
		return "MCF";
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
				if (thismanager.isAuthorizedForCurrentUser("SMultipleChoiceTextField " + this.label, actiondata,
						buffer))
					return false;
			}
			return true;
		}
		return false;
	}

}
