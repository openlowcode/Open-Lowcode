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
import java.util.logging.Logger;

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
	protected HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasFilter<E,F>> dynamicaliaslisthelper;
	protected DataObjectDefinition<E> objectdefinition;
	private static Logger logger = Logger.getLogger(ConditionalAliasListHelper.class.getName());
	/**
	 * @param objectdefinition
	 */
	public ConditionalAliasListHelper(DataObjectDefinition<E> objectdefinition) {
		this.objectdefinition = objectdefinition;
		this.conditionalaliaslist = new  HashMap<String,ChoiceValue<F>[]>();
		this.conditionaldynamicaliaslist = new HashMap<Pair<String,String>,ChoiceValue<F>[]>();
		this.dynamicaliaslisthelper = new HashMap<Pair<String,String>,FlatFileExtractorDynamicAliasFilter<E,F>>();
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
				show =  isAliasValid(thisdynamicalias,selectedvalue,conditionaldynamicaliaslist);
				if (show) {
					FlatFileExtractorDynamicAliasFilter<E,F> helper = dynamicaliaslisthelper.get(thisdynamicalias);
					if (helper==null) throw new RuntimeException("Dynamic Alias '"+thisdynamicalias.getFirstobject()+"' - ' "+thisdynamicalias.getSecondobject()+"' does not have an helper. Please check the definition ");
					String[] relevantdynamics = helper.generateForExportWithoutContext(objectdefinition,selectedvalue);
					if (relevantdynamics!=null) for (int k=0;k<relevantdynamics.length;k++) {
						String thisdynamicpart = relevantdynamics[k];
						logger.fine("Building dynamic alias for simple specific alias list Alias = "+thisdynamicalias.getFirstobject()+"-"+thisdynamicalias.getSecondobject());
						String finalcolumn = thisdynamicalias.getFirstobject()+thisdynamicpart+thisdynamicalias.getSecondobject();
						specificaliaslist.add(finalcolumn);
					}
				}
			}
			String thisalias = objectdefinition.getAliasat(i);
			ChoiceValue<F>[] conditionalforthisalias = conditionalaliaslist.get(thisalias);
			if (conditionalforthisalias==null) {
				specificaliaslist.add(thisalias);
			} else {
				boolean isvalid = isAliasValid(thisalias,selectedvalue,conditionalaliaslist);
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
	public void addDynamicAliasHelper(String aliasbefore,String aliasafter,FlatFileExtractorDynamicAliasFilter<E,F> filter) {
		this.dynamicaliaslisthelper.put(new Pair<String,String>(aliasbefore,aliasafter), filter);
	}
	
	/**
	 * check if an alias is valid for the flat file loader
	 * 
	 * @param alias        the alias
	 * @param filter       the value of a filter
	 * @param restrictions some unauthorized values
	 * @return
	 */
	public static <Y extends Object,Z extends FieldChoiceDefinition<Z>> boolean isAliasValid(
			Y alias,
			ChoiceValue<Z> filter,
			HashMap<Y, ChoiceValue<Z>[]> restrictions) {
		if (!restrictions.containsKey(alias)) {
			logger.severe("    >>> No restriction for dynamic alias "+alias+" for selection "+filter);
			return true;
		} else {
			if (filter == null) {
				logger.severe("    >>> dynamic alias "+alias+" discarded  "+filter);
				return false;
			}
			ChoiceValue<Z>[] restrictionsforalias = restrictions.get(alias);
			for (int i = 0; i < restrictionsforalias.length; i++) {
				if (restrictionsforalias[i].getStorageCode().equals(filter.getStorageCode())) {
					logger.severe("    >>> dynamic alias "+alias+" valid for selected choice"+filter);
					return true;
				} else {
					logger.severe("     >>> dynamic alias "+alias+" selected choice "+restrictionsforalias[i]+" is not valid, user asked for "+filter);
				}
			}
			return false;
		}

	}
}
