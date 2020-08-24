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
	
	private HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasParentFilter<E,G>> dynamicaliaslistparenthelper;
	private DataObjectDefinition<G> parentdefinition;
	private Function<DataObjectId<G>,G> parentextractor;
	private FlatFileExtractorParentFilter<E,G> parentaliasfilter;
/**
 * @param objectdefinition
 * @param parentdefinition
 * @param parentextractor
 * @param parentaliasfilter
 */
public ConditionalAliasListParentHelper(DataObjectDefinition<E> objectdefinition,DataObjectDefinition<G> parentdefinition,Function<DataObjectId<G>,G> parentextractor,FlatFileExtractorParentFilter<E,G> parentaliasfilter) {
	super(objectdefinition);
	this.parentdefinition = parentdefinition;
	dynamicaliaslistparenthelper = new HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasParentFilter<E,G>>();
	this.parentextractor = parentextractor;
	this.parentaliasfilter = parentaliasfilter;
	
}

/**
 * @param aliasbefore
 * @param aliasafter
 * @param aliasparentfilter
 */
public void addDynamicAliasParentHelper(
		String aliasbefore,
		String aliasafter,
		FlatFileExtractorDynamicAliasParentFilter<E,G> aliasparentfilter) {
	dynamicaliaslistparenthelper.put(new Pair<String,String>(aliasbefore,aliasafter),aliasparentfilter);
	super.addDynamicAliasHelper(aliasbefore, aliasafter, aliasparentfilter);
}

/**
 * @param selectedvalue
 * @param parentid
 * @return
 */
	public String[] getSpecificAliasList(ChoiceValue<F> selectedvalue,DataObjectId<G> parentid)  {
	String[] aliasforchoice = getSpecificAliasList(selectedvalue);
	ArrayList<String> aliasfilteredforparent = new ArrayList<String>();
	G parent = parentextractor.apply(parentid);
	for (int i=0;i<aliasforchoice.length;i++) {
		Pair<String, String>[] dynamicaliases = objectdefinition.getDynamicAliasForIndex(i);
		if (dynamicaliases!=null) for (int j=0;j<dynamicaliases.length;j++) {
			Pair<String,String> thisdynamicalias = dynamicaliases[j];
			ChoiceValue<F>[] conditionalforthisalias = conditionaldynamicaliaslist.get(thisdynamicalias);
			boolean show=true;
			if (conditionalforthisalias!=null)
			show =  DataObjectDefinition.isAliasValid(thisdynamicalias,selectedvalue,conditionaldynamicaliaslist);
			if (show) {
				String[] relevantdynamics = dynamicaliaslistparenthelper.get(thisdynamicalias).generateForExport(objectdefinition, parent);
				if (relevantdynamics!=null) for (int k=0;j<relevantdynamics.length;k++) {
					String thisdynamicpart = relevantdynamics[k];
					Pair<String, String> thisdynamiccolumn = objectdefinition.getColumnForDynamicAlias(thisdynamicalias);
					String finalcolumn = thisdynamiccolumn.getFirstobject()+thisdynamicpart+thisdynamiccolumn.getSecondobject();
					aliasfilteredforparent.add(finalcolumn);
				}
			}
		}
		
		if (parentaliasfilter.isvalid(objectdefinition,aliasforchoice[i],parent)) aliasfilteredforparent.add(aliasforchoice[i]);
	}
	return aliasfilteredforparent.toArray(new String[0]);
}	


}
