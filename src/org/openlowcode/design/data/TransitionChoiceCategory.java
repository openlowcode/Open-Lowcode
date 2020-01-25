/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * A choice category that only allows some transitions depending on the previous
 * value. This is used as a base for
 * {@link org.openlowcode.design.data.properties.Lifecycle] property.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TransitionChoiceCategory
		extends
		ChoiceCategory {
	private NamedList<ChoiceValue> values;

	private ChoiceValue defaultchoice;
	private HashMap<ChoiceValue, ChoiceValue> finalstatesmap;
	private HashMap<ChoiceValue, NamedList<ChoiceValue>> authorizedtransitions;
	private ChoiceValue defaultworkingchoice;
	private ChoiceValue defaultfinalchoice;
	private boolean authorizealltransitions = false;

	/**
	 * returns true if the transition choice category included a choice value with a
	 * similar name (storage code)
	 * 
	 * @param externalvalue a choice value
	 * @return true if similar choice value exists
	 */
	public boolean hasChoiceValue(ChoiceValue externalvalue) {
		if (values.lookupOnName(externalvalue.getName()) != null)
			return true;
		return false;
	}

	/**
	 * @return the default working choice for the transition choice category. This
	 *         is used in simple workflows whenever someone takes the task for him
	 */
	public ChoiceValue getDefaultWorkingChoice() {
		return this.defaultworkingchoice;
	}

	/**
	 * @return the choice value defined as default choice. THis is the choice value
	 *         that will be put on objects at creation for lifecycle
	 */
	public ChoiceValue getDefaultChoice() {
		return defaultchoice;
	}

	/**
	 * @param startvalue
	 * @param endvalue
	 */
	public void DefineTransition(ChoiceValue startvalue, ChoiceValue endvalue) {
		if (values.lookupOnName(startvalue.getName()) == null)
			throw new RuntimeException(
					"you can only set as start for transition a choicevalue that is already registered");
		if (values.lookupOnName(endvalue.getName()) == null)
			throw new RuntimeException(
					"you can only set as end for transition a choicevalue that is already registered");

		NamedList<ChoiceValue> transitionsforstart = authorizedtransitions.get(startvalue);
		if (transitionsforstart == null) {
			transitionsforstart = new NamedList<ChoiceValue>();
			authorizedtransitions.put(startvalue, transitionsforstart);
		}
		transitionsforstart.add(endvalue);
	}

	/**
	 * @return true if the choice category has a final choice (used to signal in
	 *         lifecycle that the object has been processed and is now frozen)
	 */
	public boolean hasFinalChoice() {
		if (finalstatesmap.size() > 0)
			return true;
		return false;
	}

	/**
	 * the choice a new object will use for a lifecycle
	 * 
	 * @param defaultchoice choice to use as a lifecycle (needs to have been added
	 *                      before)
	 */
	public void setDefaultChoice(ChoiceValue defaultchoice) {
		if (values.lookupOnName(defaultchoice.getName()) == null)
			throw new RuntimeException("you can only set as default a choicevalue that is already registered");
		this.defaultchoice = defaultchoice;
	}

	/**
	 * checks if there is a valid transition between those two values
	 * 
	 * @param startvalue current value for the object
	 * @param endvalue   target value to change the object to
	 * @return true if the transition exists
	 */
	private boolean doesTransitionExist(ChoiceValue startvalue, ChoiceValue endvalue) {
		if (startvalue == null)
			return false;
		if (endvalue == null)
			return false;
		NamedList<ChoiceValue> transitionsforstart = authorizedtransitions.get(startvalue);
		if (transitionsforstart == null)
			return false;
		if (transitionsforstart.lookupOnName(endvalue.getName()) != null)
			return true;
		return false;
	}

	/**
	 * the choice the object lifecycle will move to when someone accepts a task to
	 * work on the object in a one step workflow.<br>
	 * Note: a default working choice should have a transition from and to the
	 * default choice.
	 * 
	 * @param defaultworkingchoice default working choice (should be already
	 *                             registered)
	 */
	public void setDefaultWorkingChoice(ChoiceValue defaultworkingchoice) {
		if (defaultchoice == null)
			throw new RuntimeException("you can only set defaultworkingchoice if a defaultchoice exists");
		if (values.lookupOnName(defaultworkingchoice.getName()) == null)
			throw new RuntimeException(
					"you can only set as defaultworkingchoice a choicevalue that is already registered");
		if (!doesTransitionExist(defaultchoice, defaultworkingchoice))
			throw new RuntimeException("no two ways transitions exist between defaultchoice '" + defaultchoice.getName()
					+ "' and defaultworkingchoice '" + defaultworkingchoice.getName() + "'");
		this.defaultworkingchoice = defaultworkingchoice;
	}

	/**
	 * declares the choice as final which means that if there is a final state date,
	 * the date will be filled when transition to this state
	 * 
	 * @param finalchoice state to be used as final choice (should be already
	 *                    registered)
	 */
	public void setChoiceAsFinal(ChoiceValue finalchoice) {
		if (values.lookupOnName(finalchoice.getName()) == null)
			throw new RuntimeException("you can only set as final a choicevalue that is already registered");
		this.finalstatesmap.put(finalchoice, finalchoice);
	}

	/**
	 * if this method is called, all transitions will be authorized. This may be
	 * useful for first mock-ups or for very simple lifecycles
	 */
	public void setAuthorizeAllTransitions() {
		this.authorizealltransitions = true;
	}

	/**
	 * creates a transition choice category with given name and storage length
	 * 
	 * @param name             a name that should be unique in the module
	 * @param keystoragelength should be higher or equal to the largest name /
	 *                         storage code of the choice values
	 */
	public TransitionChoiceCategory(String name, int keystoragelength) {
		super(name, keystoragelength);
		values = new NamedList<ChoiceValue>();
		authorizedtransitions = new HashMap<ChoiceValue, NamedList<ChoiceValue>>();
		finalstatesmap = new HashMap<ChoiceValue, ChoiceValue>();

	}

	/**
	 * adds a choice value to this choice category
	 * 
	 * @param value the value to add
	 */
	public void addValue(ChoiceValue value) {
		values.add(value);
	}

	/**
	 * @return true if the choice category has a default working choice
	 */
	public boolean hasDefaultWorkingChoice() {
		if (this.defaultworkingchoice != null)
			return true;
		return false;
	}

	/**
	 * sets a choice as default final choice. By default, objects will be put to
	 * this lifecycle when simple workflows are finished
	 * 
	 * @param defaultfinalchoice the choice (already registered) that should be the
	 *                           default final choice
	 */
	public void setDefaultFinalChoice(ChoiceValue defaultfinalchoice) {
		if (values.lookupOnName(defaultfinalchoice.getName()) == null)
			throw new RuntimeException(
					"you can only set as defaultfinalchoice a choicevalue that is already registered");
		this.defaultfinalchoice = defaultfinalchoice;
		this.setChoiceAsFinal(defaultfinalchoice);

	}

	@Override
	public String getDefinitionClass() {

		return "TransitionFieldChoiceDefinition";
	}

	@Override
	public void generatetoFile(SourceGenerator sg, Module module) throws IOException {
		sg.wl("package " + module.getPath() + ".data.choice;");
		sg.wl("");
		sg.wl("import java.util.ArrayList;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.TransitionFieldChoiceDefinition;");

		sg.wl("import org.openlowcode.tools.trace.GalliumException;");
		sg.wl("");
		String classname = StringFormatter.formatForJavaClass(this.getName()) + "ChoiceDefinition";
		sg.wl("public class " + classname + " extends " + this.getDefinitionClass() + " {");
		sg.wl("	private static " + classname + " singleton;");
		sg.wl("");
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("	public static ChoiceValue<" + classname + "> getChoice"
					+ StringFormatter.formatForJavaClass(currentvalue.getName()) + "() {");
			sg.wl("		return singleton." + currentvalue.getName().toUpperCase() + ";");
			sg.wl("	}");

			sg.wl("");
		}
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("	private ChoiceValue<" + classname + "> " + currentvalue.getName().toUpperCase() + ";");
		}
		sg.wl("");
		sg.wl("	private ArrayList<ChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
				+ "ChoiceDefinition>> allvalueslist;");

		sg.wl("");
		sg.wl("	private " + classname + "()  {");
		sg.wl("		super(" + this.getKeyStorageLength() + "," + this.authorizealltransitions + ");");
		String transitionrestrictions = "";
		if (!this.authorizealltransitions)
			transitionrestrictions = ",true";
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("		" + currentvalue.getName().toUpperCase() + " = new ChoiceValue<" + classname + ">(\""
					+ currentvalue.getName() + "\",\"" + currentvalue.getDisplayName() + "\",\""
					+ currentvalue.getTooltip() + "\",true" + transitionrestrictions + ");");

		}
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("		this.addChoiceValue(" + currentvalue.getName().toUpperCase() + ");");
		}
		Iterator<ChoiceValue> transitionstartlist = authorizedtransitions.keySet().iterator();
		while (transitionstartlist.hasNext()) {
			ChoiceValue thistransitionstart = transitionstartlist.next();
			NamedList<ChoiceValue> endlist = authorizedtransitions.get(thistransitionstart);
			for (int i = 0; i < endlist.getSize(); i++) {
				ChoiceValue endtransition = endlist.get(i);
				sg.wl("		this.DefineTransition(" + thistransitionstart.getName().toUpperCase() + ","
						+ endtransition.getName().toUpperCase() + ");");
			}
		}
		if (this.defaultchoice != null) {
			sg.wl("		this.setDefaultChoice(" + defaultchoice.getName().toUpperCase() + ");");
		}
		if (this.defaultworkingchoice != null) {
			sg.wl("		this.setDefaultWorkingChoice(" + defaultworkingchoice.getName().toUpperCase() + ");");
		}
		if (this.defaultfinalchoice != null) {
			sg.wl("		this.setDefaultFinalChoice(" + defaultfinalchoice.getName().toUpperCase() + ");");
		}
		Iterator<Entry<ChoiceValue, ChoiceValue>> entryset = this.finalstatesmap.entrySet().iterator();
		while (entryset.hasNext()) {
			ChoiceValue thisfinalchoice = entryset.next().getKey();
			sg.wl("		this.defineFinalTransitionChoice(" + thisfinalchoice.getName().toUpperCase() + ");");
		}
		sg.wl("		allvalueslist = new ArrayList<ChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
				+ "ChoiceDefinition>>();");
		for (int i = 0; i < this.values.getSize(); i++) {
			ChoiceValue currentvalue = this.values.get(i);
			sg.wl("		allvalueslist.add(" + currentvalue.getName() + ");");
		}
		sg.wl("		finalizeTransitions();");
		sg.wl("	}");
		sg.wl("");

		sg.wl("	public int getValueNumber() {");
		sg.wl("		return allvalueslist.size();");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	public ChoiceValue<" + StringFormatter.formatForJavaClass(this.getName())
				+ "ChoiceDefinition> getChoiceAtIndex(int index) {");
		sg.wl("		return allvalueslist.get(index);");
		sg.wl("	}		");
		sg.wl("");
		sg.wl("	public static " + classname + " get()  {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + classname + " temp = new " + classname + "();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("}");
		sg.close();

	}

	@Override
	public int getDisplayLabelLength(int label) {
		int maxlength = label;
		for (int i = 0; i < values.getSize(); i++) {
			if (values.get(i).getDisplayName() != null)
				if (values.get(i).getDisplayName().length() > maxlength)
					maxlength = values.get(i).getDisplayName().length();
		}
		return maxlength;
	}

	@Override
	public boolean isKeyPresent(String key) {
		ChoiceValue value = this.values.lookupOnName(key);
		if (value == null)
			return false;
		return true;
	}
}
