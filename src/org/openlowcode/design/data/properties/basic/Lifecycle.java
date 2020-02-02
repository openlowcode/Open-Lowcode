/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.FieldOverrideForProperty;
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.TimestampStoredElement;
import org.openlowcode.design.data.TransitionChoiceCategory;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * This property provides a lifecycle to an object. Lifecycle is defined by a
 * {@link org.openlowcode.design.data.TransitionChoiceCategory}.
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Lifecycle
		extends
		Property<Lifecycle> {
	private TransitionChoiceCategory lifecyclehelper;
	private UniqueIdentified uniqueidentified;
	private String statelabel = "State";
	private String nonreleasedlabel = null;

	/**
	 * @return the transition choice category used for the lifecycle (list of status
	 *         and transitions, also definition of default, working, and final
	 *         states)
	 */
	public TransitionChoiceCategory getLifecycleHelper() {
		return this.lifecyclehelper;
	}

	/**
	 * creates a new lifecycle property with the default label (state)
	 * 
	 * @param lifecyclehelper the definition of the lifecycle (list of status and
	 *                        transitions, also definition of default, working and
	 *                        final states)
	 */
	public Lifecycle(TransitionChoiceCategory lifecyclehelper) {
		this(lifecyclehelper, null);
	}

	/**
	 * creates a new lifecycle property with a customized label (something more
	 * specific to the context than state)
	 * 
	 * @param lifecyclehelper the definition of the lifecycle (list of status and
	 *                        transitions)
	 * @param newstatelabel   name of the state for the object (could be a synonym
	 *                        like status...)
	 */
	public Lifecycle(TransitionChoiceCategory lifecyclehelper, String newstatelabel) {

		super("LIFECYCLE");
		if (newstatelabel != null) {
			this.statelabel = newstatelabel;
			this.addFieldOverrides(new FieldOverrideForProperty("STATE", newstatelabel));
		}
		this.lifecyclehelper = lifecyclehelper;
		if (lifecyclehelper.getDefaultChoice() == null)
			throw new RuntimeException(
					"Lifecycle Helper  for object " + parent.getName() + " does not have a default value defined");
	}

	/**
	 * creates a new lifecycle property with a customized label (something more
	 * specific to the context than state), and also a non-released label showing in
	 * red
	 * 
	 * @param lifecyclehelper  the definition of the lifecycle (list of status and
	 *                         transitions)
	 * @param newstatelabel    name of the state for the object (could be a synonym
	 *                         like status...)
	 * @param nonreleasedlabel a warning for non-released data
	 */
	public Lifecycle(TransitionChoiceCategory lifecyclehelper, String newstatelabel, String nonreleasedlabel) {
		this(lifecyclehelper, newstatelabel);
		this.nonreleasedlabel = nonreleasedlabel;
	}

	@Override
	public void controlAfterParentDefinition() {

		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		DataAccessMethod changestate = new DataAccessMethod("CHANGESTATE", null, false, true);
		changestate.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		ChoiceArgument newstate = new ChoiceArgument("NEWSTATE", lifecyclehelper);

		changestate.addInputArgument(new MethodArgument("NEWSTATE", newstate));
		this.addDataAccessMethod(changestate);

		DataAccessMethod getstateforchange = new DataAccessMethod("GETSTATEFORCHANGE",
				new ChoiceArgument("POSSIBLESTATE", lifecyclehelper), false);
		getstateforchange.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getstateforchange);

		StoredElement state = new StringStoredElement("STATE", 200);
		this.addElementasSearchElement(state, statelabel, "state of the object in its lifecycle",
				Property.FIELDDIPLSAY_TITLE_MOD, 850, 40,
				new SearchWidgetDefinition(true, "STATE", statelabel, lifecyclehelper));

		if (lifecyclehelper.hasFinalChoice()) {
			StoredElement finalstatetime = new TimestampStoredElement("FINALSTATETIME");
			this.addElement(finalstatetime);
			this.addIndex(new Index("FINALSTATETIME", finalstatetime, false));
		}

		this.addIndex(new Index("STATE", state, false));

		this.addChoiceCategoryHelper("LIFECYCLEHELPER", lifecyclehelper);
		this.addStringHelper("UNRELEASEDWARNING", this.nonreleasedlabel);
	}

	/**
	 * @return get the lifecycle helper transition category
	 */
	public TransitionChoiceCategory getTransitionChoiceCategory() {
		return this.lifecyclehelper;
	}

	/**
	 * @return the dependent property unique identified
	 */
	public UniqueIdentified getDependentPropertyUniqueIdentified() {
		return this.uniqueidentified;
	}

	/**
	 * checks if the lifecycle helper has the following choice value
	 * 
	 * @param value value to check
	 * @return true if the lifecycle helper (transition choice category) has the
	 *         value
	 */
	public boolean hasChoiceValue(ChoiceValue value) {
		return this.lifecyclehelper.hasChoiceValue(value);
	}

	/**
	 * @return the unreleased warning label shown on non-frozen data (by default, it
	 *         is null, and no warning is shown)
	 */
	public String getUnreleasedWarning() {
		return this.nonreleasedlabel;
	}

	/**
	 * returns a reference to change state action to provide a privilege just for
	 * this action
	 * 
	 * @param parent parent data object
	 * @return the marker to the change state action
	 */
	public static AutomaticActionMarker getChangeStateAutomaticAction(DataObjectDefinition parent) {
		return new AutomaticActionMarker("CHANGESTATE" + parent.getName(), parent.getOwnermodule());
	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import " + lifecyclehelper.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(lifecyclehelper.getName()) + "ChoiceDefinition;");

	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[0];
		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public void setFinalSettings() {

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

}
