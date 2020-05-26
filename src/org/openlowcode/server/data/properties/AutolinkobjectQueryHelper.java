/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.ThreeDataObjects;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.JoinQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.SelectQuery;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * the query helper of the auto-link object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AutolinkobjectQueryHelper {

	private static final int BATCH_QUERY_SIZE = 20;
	private static final String BLANK_ID = "NEVERLAND";

	private static Logger logger = Logger.getLogger(AutolinkobjectQueryHelper.class.getName());
	private static AutolinkobjectQueryHelper singleton = new AutolinkobjectQueryHelper();

	/**
	 * @return the singleton of the helper (safe for multi-thread
	 */
	public static AutolinkobjectQueryHelper get() {
		return singleton;
	}

	/**
	 * gets the field schema of the left if field
	 * 
	 * @param definition definition of the auto-link property
	 * @return field schema of the left id field
	 */
	@SuppressWarnings("unchecked")
	public StoredFieldSchema<String> getLeftIdFieldSchema(AutolinkobjectDefinition<?, ?> definition) {
		return (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("LFID");
	}

	/**
	 * gets the field schema of the right if field
	 * 
	 * @param definition definition of the auto-link property
	 * @return field schema of the right id field
	 */
	@SuppressWarnings("unchecked")
	public StoredFieldSchema<String> getRightIdFieldSchema(AutolinkobjectDefinition<?, ?> definition) {
		return (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("RGID");
	}

	/**
	 * generates a query condition filtering on the left object id
	 * 
	 * @param alias                  table alias for the auto-link
	 * @param idvalue                value of the left data object id
	 * @param parentobjectdefinition definition of the data object holding the link
	 * @param linkedobjectdefinition definition of the linked data object
	 * @return the query condition performing the filter on left object id for the
	 *         link
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends DataObject<E> & AutolinkobjectInterface<E, F> & UniqueidentifiedInterface<E>, F extends DataObject<F> & HasidInterface<F>> QueryCondition getLeftidQueryCondition(
			TableAlias alias, DataObjectId<F> idvalue, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition) {
		AutolinkobjectDefinition<E, F> definition = new AutolinkobjectDefinition(parentobjectdefinition,
				parentobjectdefinition);
		StoredFieldSchema<String> id = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("LFID");
		if (alias == null)
			return new SimpleQueryCondition(null, id, new QueryOperatorEqual<String>(),
					(idvalue == null ? BLANK_ID : idvalue.getId()));
		return new SimpleQueryCondition(alias, id, new QueryOperatorEqual<String>(),
				(idvalue == null ? BLANK_ID : idvalue.getId()));
	}

	/**
	 * generates a query condition filtering on the right object id
	 * 
	 * @param alias                  table alias for the auto-link
	 * @param idvalue                value of the right data object id
	 * @param parentobjectdefinition definition of the data object holding the link
	 * @param linkedobjectdefinition definition of the linked data object
	 * @return the query condition performing the filter on right object id for the
	 *         link
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E extends DataObject<E> & AutolinkobjectInterface<E, F> & UniqueidentifiedInterface<E>, F extends DataObject<F> & HasidInterface<F>> QueryCondition getRightidQueryCondition(
			TableAlias alias, DataObjectId<F> idvalue, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition) {
		AutolinkobjectDefinition<E, F> definition = new AutolinkobjectDefinition(parentobjectdefinition,
				linkedobjectdefinition);
		StoredFieldSchema<String> id = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("RGID");
		if (alias == null)
			return new SimpleQueryCondition(null, id, new QueryOperatorEqual<String>(),
					(idvalue == null ? BLANK_ID : idvalue.getId()));
		return new SimpleQueryCondition(alias, id, new QueryOperatorEqual<String>(),
				(idvalue == null ? BLANK_ID : idvalue.getId()));
	}

	/**
	 * gets all links corresponding to a single left object
	 * 
	 * @param leftid                   left object data object id
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links corresponding to the conditions
	 */
	public <E extends DataObject<E> & AutolinkobjectInterface<E, F> & UniqueidentifiedInterface<E>, F extends DataObject<F> & HasidInterface<F>> E[] getalllinksfromleftid(
			DataObjectId<F> leftid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {

		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				getLeftidQueryCondition(alias, leftid, parentobjectdefinition, linkedobjectdefinition));
		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}

		// is autolink, giving back the left and right link in the same query
		if (autolinkobjectDefinition.isSymetricLink()) {
			NamedList<TableAlias> aliaslistforsymetricrightquery = new NamedList<TableAlias>();
			TableAlias aliasforsymetricrightquery = parentobjectdefinition.getAlias("SINGLEOBJECT");
			aliaslistforsymetricrightquery.add(aliasforsymetricrightquery);
			QueryCondition rightconditionforautolink = parentobjectdefinition
					.extendquery(aliaslistforsymetricrightquery, aliasforsymetricrightquery, getRightidQueryCondition(
							aliasforsymetricrightquery, leftid, parentobjectdefinition, linkedobjectdefinition));
			Row rowforsymetricrightquery = QueryHelper.getHelper()
					.query(new SelectQuery(aliaslistforsymetricrightquery, rightconditionforautolink));

			while (rowforsymetricrightquery.next()) {
				E linkobject = parentobjectdefinition.generateFromRow(rowforsymetricrightquery,
						aliasforsymetricrightquery);

				if (!linkobject.getLfid().equals(leftid)) {
					linkobject.exchangeleftandrightfields();
					returnlist.add(linkobject);
				}
			}
		}

		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all links corresponding to a series of left object
	 * 
	 * @param leftid                   an array of left object data object id
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links corresponding to the conditions
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> E[] getalllinksfromleftid(
			DataObjectId<F>[] leftid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {
		ArrayList<E> results = new ArrayList<E>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (leftid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			if (additionalcondition != null)
				if (additionalcondition.getAliases() != null)
					for (int k = 0; k < additionalcondition.getAliases().length; k++)
						aliaslist.add(additionalcondition.getAliases()[k]);
			QueryCondition objectuniversalcondition = parentobjectdefinition
					.getUniversalQueryCondition(autolinkobjectDefinition, "SINGLEOBJECT");
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min<leftid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < leftid.length) {
					thisuniqueidcondition = getLeftidQueryCondition(alias, leftid[j], parentobjectdefinition,
							linkedobjectdefinition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = getLeftidQueryCondition(alias, null, parentobjectdefinition,
							linkedobjectdefinition);

				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}

			QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
			if (additionalcondition != null)
				if (additionalcondition.getCondition() != null)
					extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			while (answer.next()) {
				E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
				// put all results in a hasmap;
				results.add(formattedanswer);
			}
		}}

		if (autolinkobjectDefinition.isSymetricLink()) {

			for (int i = 0; i < (leftid.length / BATCH_QUERY_SIZE) + 1; i++) {
				NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
				TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
				aliaslist.add(alias);
				if (additionalcondition != null)
					if (additionalcondition.getAliases() != null)
						for (int k = 0; k < additionalcondition.getAliases().length; k++)
							aliaslist.add(additionalcondition.getAliases()[k]);
				QueryCondition objectuniversalcondition = parentobjectdefinition
						.getUniversalQueryCondition(autolinkobjectDefinition, "SINGLEOBJECT");
				OrQueryCondition uniqueidcondition = new OrQueryCondition();
				int min = i * BATCH_QUERY_SIZE;
				if (min<leftid.length) {
				for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
					QueryCondition thisuniqueidcondition = null;
					if (j < leftid.length) {
						thisuniqueidcondition = getRightidQueryCondition(alias, leftid[j], parentobjectdefinition,
								linkedobjectdefinition);
					} else {
						// all queries will have batch size conditions. If not enough id, a blank id is
						// used
						thisuniqueidcondition = getRightidQueryCondition(alias, null, parentobjectdefinition,
								linkedobjectdefinition);

					}
					uniqueidcondition.addCondition(thisuniqueidcondition);
				}

				QueryCondition finalcondition = uniqueidcondition;
				if (objectuniversalcondition != null) {
					finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
				}

				QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
				if (additionalcondition != null)
					if (additionalcondition.getCondition() != null)
						extendedcondition = new AndQueryCondition(extendedcondition,
								additionalcondition.getCondition());

				Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
				while (answer.next()) {
					E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
					// put all results in a hasmap;
					results.add(formattedanswer);
				}
			}}
				

		}

		return results.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all links corresponding to a single right object
	 * 
	 * @param rightid                  right object data object id
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links corresponding to the conditions
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> E[] getalllinksfromrightid(
			DataObjectId<F> rightid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {
		if (autolinkobjectDefinition.isSymetricLink())
			return new ArrayList<E>().toArray(parentobjectdefinition.generateArrayTemplate());
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int k = 0; k < additionalcondition.getAliases().length; k++)
					aliaslist.add(additionalcondition.getAliases()[k]);

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				getRightidQueryCondition(alias, rightid, parentobjectdefinition, linkedobjectdefinition));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());

		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);
		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));

		// TODO solve this mess
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));

		}
		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all links corresponding to a series of right objects
	 * 
	 * @param rightid                  an array of right object data object id
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links corresponding to the conditions
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> E[] getalllinksfromrightid(
			DataObjectId<F>[] rightid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {
		logger.severe("start infernal method");
		if (autolinkobjectDefinition.isSymetricLink())
			return new ArrayList<E>().toArray(parentobjectdefinition.generateArrayTemplate());
		ArrayList<E> results = new ArrayList<E>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (rightid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			if (additionalcondition != null)
				if (additionalcondition.getAliases() != null)
					for (int k = 0; k < additionalcondition.getAliases().length; k++)
						aliaslist.add(additionalcondition.getAliases()[k]);

			QueryCondition objectuniversalcondition = parentobjectdefinition
					.getUniversalQueryCondition(autolinkobjectDefinition, "SINGLEOBJECT");
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min<rightid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < rightid.length) {
					thisuniqueidcondition = getRightidQueryCondition(alias, rightid[j], parentobjectdefinition,
							linkedobjectdefinition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = getRightidQueryCondition(alias, null, parentobjectdefinition,
							linkedobjectdefinition);

				}
				uniqueidcondition.addCondition(thisuniqueidcondition);

			}

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}
			if (additionalcondition != null)
				if (additionalcondition.getCondition() != null)
					finalcondition = new AndQueryCondition(finalcondition, additionalcondition.getCondition());

			QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			logger.severe("before infernal call");
			while (answer.next()) {
				logger.severe("in infernal loop");
				E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
				// put all results in a hasmap;
				results.add(formattedanswer);
			}
		}}

		return results.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all links corresponding to the provided left object id and right object
	 * id
	 * 
	 * @param leftid                   a left object id
	 * @param rightid                  a right object id
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links corresponding to the conditions
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> E[] getalllinksfromleftandrightid(
			DataObjectId<F> leftid, DataObjectId<F> rightid, QueryFilter additionalcondition,
			DataObjectDefinition<E> parentobjectdefinition, DataObjectDefinition<F> linkedobjectdefinition,
			AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		AndQueryCondition leftandright = new AndQueryCondition();
		leftandright
				.addCondition(getLeftidQueryCondition(alias, leftid, parentobjectdefinition, linkedobjectdefinition));
		leftandright
				.addCondition(getRightidQueryCondition(alias, rightid, parentobjectdefinition, linkedobjectdefinition));

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, leftandright);
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());

		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedcondition = new AndQueryCondition(extendedcondition, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			returnlist.add(parentobjectdefinition.generateFromRow(row, alias));
		}
		return returnlist.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets links and both objects corresponding to the provided condition
	 * 
	 * Alias used:
	 * <ul>
	 * <li>SINGLEOBJECT on the parent object definition</li>
	 * <li>LEFTOBJECTALIAS on linked object</li>
	 * <li>RIGHTOBJECTALIAS on lined object</li>
	 * </ul>
	 * 
	 * @param additionalcondition      additional condition for further filter
	 * @param parentobjectdefinition   definition of the link object
	 * @param linkedobjectdefinition   definition of the linked data object
	 * @param autolinkobjectDefinition auto link object property
	 * @return the links and both objects corresponding to the provided conditions
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> ThreeDataObjects<F, E, F>[] getlinksandbothobjects(
			QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {

		TableAlias linkalias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		TableAlias leftobjectalias = linkedobjectdefinition.getAlias("LEFTOBJECTALIAS");
		TableAlias rightobjectalias = linkedobjectdefinition.getAlias("RIGHTOBJECTALIAS");
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(leftobjectalias);
		aliaslist.add(rightobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		AndQueryCondition twojoinscondition = new AndQueryCondition();

		twojoinscondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getLeftIdFieldSchema(autolinkobjectDefinition), leftobjectalias,
				linkedobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		twojoinscondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getRightIdFieldSchema(autolinkobjectDefinition), rightobjectalias,
				linkedobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				twojoinscondition.addCondition(additionalcondition.getCondition());

		// extends query only on main object

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, linkalias, twojoinscondition);

		QueryCondition extendedconditionforleft = linkedobjectdefinition.extendquery(aliaslist, leftobjectalias,
				extendedcondition);

		QueryCondition extendedconditionforleftandright = linkedobjectdefinition.extendquery(aliaslist,
				rightobjectalias, extendedconditionforleft);

		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					linkuniversalcondition);

		QueryCondition leftuniversalcondition = linkedobjectdefinition.getUniversalQueryCondition(null,
				"LEFTOBJECTALIAS");
		if (leftuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					leftuniversalcondition);

		QueryCondition rightuniversalcondition = linkedobjectdefinition.getUniversalQueryCondition(null,
				"RIGHTOBJECTALIAS");
		if (rightuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					rightuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforleftandright));

		ArrayList<ThreeDataObjects<F, E, F>> returnlist = new ArrayList<ThreeDataObjects<F, E, F>>();
		while (row.next()) {

			F objectone = linkedobjectdefinition.generateFromRow(row, leftobjectalias);
			E objecttwo = parentobjectdefinition.generateFromRow(row, linkalias);
			F objectthree = linkedobjectdefinition.generateFromRow(row, rightobjectalias);
			returnlist.add(new ThreeDataObjects<F, E, F>(objectone, objecttwo, objectthree));
		}
		return returnlist.toArray(new ThreeDataObjects[0]);
	}

	/**
	 * gets the alias for potential right object query
	 * 
	 * @param linkedobjectdefinition definition of the linked object
	 * @return the corresponding table alias
	 */
	public static <F extends DataObject<F>> TableAlias getRightObjectAliasForPotentialRightObject(
			DataObjectDefinition<F> linkedobjectdefinition) {
		TableAlias maintablealias = linkedobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);
		return maintablealias;
	}

	/**
	 * gets the potential right objects authorized to be linked to the given left
	 * object
	 * 
	 * @param leftobjectid             left object id
	 * @param additionalcondition      additional query condition
	 * @param definition               definition of the link data object
	 * @param linkedobjectdefinition   definition of the data object being linked to
	 * @param autolinkobjectDefinition definition of the autolink property
	 * @return the potential right object
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> F[] getpotentialrightobject(
			DataObjectId<F> leftobjectid, QueryFilter additionalcondition, DataObjectDefinition<E> definition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {
		QueryCondition extendedcondition = null;
		TableAlias maintablealias = linkedobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);
		AndQueryCondition allconstraintsquery = null;
		for (int i = 0; i < autolinkobjectDefinition.getConstraintOnAutolinkObjectNumber(); i++) {
			ConstraintOnAutolinkObject<F> constraintonlinkobject = autolinkobjectDefinition
					.getConstraintOnAutolinkObject(i);
			if (constraintonlinkobject != null) {
				QueryCondition conditiononlinkconstraint = constraintonlinkobject.generateQueryFilter(maintablealias,
						leftobjectid);
				if (allconstraintsquery == null)
					allconstraintsquery = new AndQueryCondition();
				allconstraintsquery.addCondition(conditiononlinkconstraint);
			}
		}
		if (allconstraintsquery != null)
			extendedcondition = allconstraintsquery;
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null) {
				extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
			}

		F[] result = StoredobjectQueryHelper.get().getallactive(
				new QueryFilter(extendedcondition,
						(additionalcondition != null ? additionalcondition.getAliases() : null)),
				linkedobjectdefinition, autolinkobjectDefinition.getLinkedObjectUniqueIdentifiedDefinition()
						.getDependentDefinitionStoredobject());
		return result;
	}

	/**
	 * gets the links and right objects corresponding to the provided left object id
	 * 
	 * @param leftid                 id of the left object
	 * @param additionalcondition    additional filter condition
	 * @param parentobject           definition of the link data object
	 * @param linkedobjectdefinition definition of the data object being linked to
	 * @param propertydefinition     auto-link property definition
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> TwoDataObjects<E, F>[] getlinksandrightobject(
			DataObjectId<F> leftid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> propertydefinition) {
		TableAlias linkalias = parentobject.getAlias("SINGLEOBJECT");
		TableAlias rightobjectalias = linkedobjectdefinition.getAlias("RIGHTOBJECTALIAS");
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(rightobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		QueryCondition idcondition = parentobject.extendquery(aliaslist, linkalias,
				getLeftidQueryCondition(linkalias, leftid, parentobject, linkedobjectdefinition));

		AndQueryCondition joinquerycondition = new AndQueryCondition();
		joinquerycondition.addCondition(idcondition);
		joinquerycondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getRightIdFieldSchema(propertydefinition), rightobjectalias,
				linkedobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				joinquerycondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = parentobject.extendquery(aliaslist, linkalias, joinquerycondition);

		QueryCondition extendedconditionforright = linkedobjectdefinition.extendquery(aliaslist, rightobjectalias,
				extendedcondition);

		QueryCondition rightuniversalcondition = linkedobjectdefinition.getUniversalQueryCondition(null,
				"RIGHTOBJECTALIAS");
		if (rightuniversalcondition != null)
			extendedconditionforright = new AndQueryCondition(extendedconditionforright, rightuniversalcondition);

		QueryCondition linkuniversalcondition = parentobject.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedconditionforright = new AndQueryCondition(extendedconditionforright, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforright));
		ArrayList<TwoDataObjects<E, F>> returnlist = new ArrayList<TwoDataObjects<E, F>>();
		while (row.next()) {

			E objectone = parentobject.generateFromRow(row, linkalias);
			F objecttwo = linkedobjectdefinition.generateFromRow(row, rightobjectalias);
			returnlist.add(new TwoDataObjects<E, F>(objectone, objecttwo));
		}
		return returnlist.toArray(new TwoDataObjects[0]);
	}

	/**
	 * gets the links and left objects corresponding to the provided right object
	 * 
	 * @param rightid                  right object id
	 * @param additionalcondition      additional filter condition
	 * @param parentobjectdefinition   definition of the data object holding the
	 *                                 link
	 * @param linkedobjectdefinition   definition of the data object referenced by
	 *                                 the link
	 * @param autolinkobjectDefinition definition of the autolink property
	 * @return the corresponding links and left objects
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E> & AutolinkobjectInterface<E, F>, F extends DataObject<F> & HasidInterface<F>> TwoDataObjects<F, E>[] getlinksandleftobject(
			DataObjectId<F> rightid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition, AutolinkobjectDefinition<E, F> autolinkobjectDefinition) {

		TableAlias linkalias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		TableAlias leftobjectalias = linkedobjectdefinition.getAlias("LEFTOBJECTALIAS");
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(leftobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		// LinkobjectDefinition<F,E,G> definition = new
		// LinkobjectDefinition<F,E,G>(parentobjectdefinition, leftobjectdefinition,
		// rightobjectdefinition);
		QueryCondition idcondition = parentobjectdefinition.extendquery(aliaslist, linkalias,
				getRightidQueryCondition(linkalias, rightid, parentobjectdefinition, linkedobjectdefinition));

		AndQueryCondition joinquerycondition = new AndQueryCondition();
		joinquerycondition.addCondition(idcondition);
		joinquerycondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getLeftIdFieldSchema(autolinkobjectDefinition), leftobjectalias,
				linkedobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				joinquerycondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, linkalias, joinquerycondition);

		QueryCondition extendedconditionforleft = linkedobjectdefinition.extendquery(aliaslist, leftobjectalias,
				extendedcondition);

		QueryCondition leftuniversalcondition = linkedobjectdefinition.getUniversalQueryCondition(null,
				"LEFTOBJECTALIAS");
		if (leftuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, leftuniversalcondition);

		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null, "SINGLEOBJECT");
		if (linkuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforleft));
		ArrayList<TwoDataObjects<F, E>> returnlist = new ArrayList<TwoDataObjects<F, E>>();
		while (row.next()) {

			F objectone = linkedobjectdefinition.generateFromRow(row, leftobjectalias);
			E objecttwo = parentobjectdefinition.generateFromRow(row, linkalias);
			returnlist.add(new TwoDataObjects<F, E>(objectone, objecttwo));
		}
		return returnlist.toArray(new TwoDataObjects[0]);
	}

}
