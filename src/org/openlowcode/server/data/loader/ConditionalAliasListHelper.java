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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.tools.misc.Pair;


/**
 * An helper to determine the list of aliases to consider when performing an export of 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> object that is loaded
 * @param <F> the type of loading shown by the user
 * @since 1.11
 */

public class ConditionalAliasListHelper<E extends DataObject<E>, F extends FieldChoiceDefinition<F>> {
	protected HashMap<String,ChoiceValue<F>[]> conditionalaliaslist;
	protected HashMap<Pair<String,String>,ChoiceValue<F>[]> conditionaldynamicaliaslist;
	protected HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasFilter<E>> dynamicaliaslisthelper;
	protected DataObjectDefinition<E> objectdefinition;
	
	/**
	 * @param objectdefinition
	 */
	public ConditionalAliasListHelper(DataObjectDefinition<E> objectdefinition) {
		this.objectdefinition = objectdefinition;
		this.conditionalaliaslist = new  HashMap<String,ChoiceValue<F>[]>();
		this.conditionaldynamicaliaslist = new HashMap<Pair<String,String>,ChoiceValue<F>[]>();
		this.dynamicaliaslisthelper = new HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasFilter<E>>();
	}
	
	/**
	 * @param alias
	 * @param condition
	 */
	public void addConditionalAlias(String alias,ChoiceValue<F>[] condition) {
		this.conditionalaliaslist.put(alias,condition);
	}
	
	/**
	 * @param dynamicaliasstart
	 * @param dynamicaliasend
	 * @param condition
	 */
	public void addConditionalDynamicAlias(String dynamicaliasstart,String dynamicaliasend,ChoiceValue<F>[] condition) {
		this.conditionaldynamicaliaslist.put(new Pair<String,String>(dynamicaliasstart,dynamicaliasend),condition);
	}
	
	/**
	 * @param selectedvalue
	 * @return
	 */
	public String[] getSpecificAliasList(ChoiceValue<F> selectedvalue)  {
		ArrayList<String> specificaliaslist = new ArrayList<String>();
		for (int i=0;i<objectdefinition.getAliasNumber();i++) {
			Pair<String, String>[] dynamicaliases = objectdefinition.getDynamicAliasForIndex(i);
			if (dynamicaliases!=null) for (int j=0;j<dynamicaliases.length;j++) {
				Pair<String,String> thisdynamicalias = dynamicaliases[j];
				ChoiceValue<F>[] conditionalforthisalias = conditionaldynamicaliaslist.get(thisdynamicalias);
				boolean show=true;
				if (conditionalforthisalias!=null)
				show =  DataObjectDefinition.isAliasValid(thisdynamicalias,selectedvalue,conditionaldynamicaliaslist);
				if (show) {
					String[] relevantdynamics = dynamicaliaslisthelper.get(thisdynamicalias).generateForExportWithoutContext(objectdefinition);
					if (relevantdynamics!=null) for (int k=0;j<relevantdynamics.length;k++) {
						String thisdynamicpart = relevantdynamics[k];
						Pair<String, String> thisdynamiccolumn = objectdefinition.getColumnForDynamicAlias(thisdynamicalias);
						String finalcolumn = thisdynamiccolumn.getFirstobject()+thisdynamicpart+thisdynamiccolumn.getSecondobject();
						specificaliaslist.add(finalcolumn);
					}
				}
			}
			String thisalias = objectdefinition.getAliasat(i);
			ChoiceValue<F>[] conditionalforthisalias = conditionalaliaslist.get(thisalias);
			if (conditionalforthisalias==null) {
				specificaliaslist.add(thisalias);
			} else {
				boolean isvalid = DataObjectDefinition.isAliasValid(thisalias,selectedvalue,conditionalaliaslist);
				if (isvalid) specificaliaslist.add(thisalias);
			}
		}
		return specificaliaslist.toArray(new String[0]);
	}		
	
	/**
	 * @param aliasbefore
	 * @param aliasafter
	 * @param filter
	 */
	public void addDynamicAliasHelper(String aliasbefore,String aliasafter,FlatFileExtractorDynamicAliasFilter<E> filter) {
		this.dynamicaliaslisthelper.put(new Pair<String,String>(aliasbefore,aliasafter), filter);
	}
}
