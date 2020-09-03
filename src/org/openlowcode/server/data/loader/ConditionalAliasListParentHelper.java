/********************************************************************************
* Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
*
* This program and the accompanying materials are made available under the
* terms of the Eclipse Public License 2.0 which is available at
* http://www.eclipse.org/legal/epl-2.0 .
*
* SPDX-License-Identifier: EPL-2.0
********************************************************************************/

package org.openlowcode.server.data.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.tools.misc.Pair;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 *         An helper to determine loading aliases to use according to context
 *         (context is the parent of the main object)
 *
 * @param <E> object that is loaded
 * @param <F> the type of loading shown by the user
 * @param <G> parent of the object that is loaded / exported
 * @since 1.11
 */
public class ConditionalAliasListParentHelper<
		E extends DataObject<E>,
		F extends FieldChoiceDefinition<F>,
		G extends DataObject<G>>
		extends
		ConditionalAliasListHelper<E, F> {
	private static Logger logger = Logger.getLogger(ConditionalAliasListParentHelper.class.getName());
	private HashMap<Pair<String, String>, FlatFileExtractorDynamicAliasParentFilter<E, G,F>> dynamicaliaslistparenthelper;
	@SuppressWarnings("unused")
	private DataObjectDefinition<G> parentdefinition;
	private Function<DataObjectId<G>, G> parentextractor;
	private FlatFileExtractorParentFilter<E, G> parentaliasfilter;

	/**
	 * @param objectdefinition  definition of the object having the
	 *                          HasMultiDimensionChildren
	 * @param parentdefinition  definition of the parent of the object mentioned
	 *                          above
	 * @param parentextractor   function to get the parent from the parent object id
	 * @param parentaliasfilter alias filter for simple aliases
	 */
	public ConditionalAliasListParentHelper(
			DataObjectDefinition<E> objectdefinition,
			DataObjectDefinition<G> parentdefinition,
			Function<DataObjectId<G>, G> parentextractor,
			FlatFileExtractorParentFilter<E, G> parentaliasfilter) {
		super(objectdefinition);
		this.parentdefinition = parentdefinition;
		dynamicaliaslistparenthelper = new HashMap<
				Pair<String, String>, FlatFileExtractorDynamicAliasParentFilter<E, G,F>>();
		this.parentextractor = parentextractor;
		this.parentaliasfilter = parentaliasfilter;

	}

	/**
	 * specifies the filter for the dynamic alias
	 * 
	 * @param aliasbefore       dynamic alias before
	 * @param aliasafter        dynamic alias after
	 * @param aliasparentfilter filter to use
	 */
	public void addDynamicAliasParentHelper(
			String aliasbefore,
			String aliasafter,
			FlatFileExtractorDynamicAliasParentFilter<E, G,F> aliasparentfilter) {
		this.addDynamicAliasHelper(aliasbefore, aliasafter, aliasparentfilter);
		dynamicaliaslistparenthelper.put(new Pair<String, String>(aliasbefore, aliasafter), aliasparentfilter);
		super.addDynamicAliasHelper(aliasbefore, aliasafter, aliasparentfilter);
	}

	/**
	 * get the specific alias list for the provided parent
	 * 
	 * @param selectedvalue value for export choice
	 * @param parentid      id of the parent
	 * @return list of aliases
	 */
	public String[] getSpecificAliasList(ChoiceValue<F> selectedvalue, DataObjectId<G> parentid) {

		ArrayList<String> aliasfilteredforparent = new ArrayList<String>();
		G parent = parentextractor.apply(parentid);
		for (int i=0;i<objectdefinition.getAliasNumber();i++) {
			String staticalias = objectdefinition.getAliasat(i);
			Pair<String, String>[] dynamicaliases = objectdefinition.getDynamicAliasForIndex(i);
			if (dynamicaliases != null) {
				logger.severe(" starting processing alias " + i + "   -  " + staticalias);
				for (int j = 0; j < dynamicaliases.length; j++) {
					Pair<String, String> thisdynamicalias = dynamicaliases[j];
					ChoiceValue<F>[] conditionalforthisalias = conditionaldynamicaliaslist.get(thisdynamicalias);
					boolean show = true;
					if (conditionalforthisalias != null)
						show = isAliasValid(thisdynamicalias, selectedvalue, conditionaldynamicaliaslist);
					if (show) {
						FlatFileExtractorDynamicAliasParentFilter<E,G,F> helper = this.dynamicaliaslistparenthelper.get(thisdynamicalias);
						if (helper == null)
							throw new RuntimeException("Dynamic Alias '" + thisdynamicalias.getFirstobject() + "' - ' "
									+ thisdynamicalias.getSecondobject()
									+ "' does not have an helper. Please check the definition ");
						String[] relevantdynamics = helper.generateForExport(objectdefinition,parent,selectedvalue);
						if (relevantdynamics != null)
							for (int k = 0; k < relevantdynamics.length; k++) {
								String thisdynamicpart = relevantdynamics[k];
								logger.severe("Building dynamic alias for specific alias (" + j + "/" + k
										+ ") with parent list Alias = " + thisdynamicalias.getFirstobject() + "-"
										+ thisdynamicalias.getSecondobject()+" - Dynamic = "+thisdynamicpart);
								String finalcolumn = thisdynamicalias.getFirstobject() + thisdynamicpart
										+ thisdynamicalias.getSecondobject();
								aliasfilteredforparent.add(finalcolumn);
							}
					}
				}
			}
			if (parentaliasfilter == null) {
				boolean isvalid = isAliasValid(staticalias, selectedvalue, conditionalaliaslist);
				if (isvalid) {
					logger.severe(" - add simple alias "+staticalias);
					aliasfilteredforparent.add(staticalias);
				}
					
			}
			if (parentaliasfilter != null)
				if (parentaliasfilter.isvalid(objectdefinition, staticalias, parent)) {
					aliasfilteredforparent.add(staticalias);
					logger.severe(" - add simple alias "+staticalias);
				}
					
		}
		return aliasfilteredforparent.toArray(new String[0]);
	}

}
