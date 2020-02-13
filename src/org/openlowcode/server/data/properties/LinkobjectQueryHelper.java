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

import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.QueryHelper;
import org.openlowcode.server.data.ThreeDataObjects;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkObject;
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
 * Query helper for the left object. This provides various queries using the
 * link object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LinkobjectQueryHelper {

	private static final int BATCH_QUERY_SIZE = 20;
	private static final String BLANK_ID = "NEVERLAND";
	private static LinkobjectQueryHelper singleton = new LinkobjectQueryHelper();

	public static LinkobjectQueryHelper get() {
		return singleton;
	}

	/**
	 * gets the left id field for the link
	 * 
	 * @param definition LinkObject definition
	 * @return the left id field for the link
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> StoredFieldSchema<String> getLeftIdFieldSchema(
			LinkobjectDefinition<E, F, G> definition) {
		return (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("LFID");
	}

	/**
	 * gets the right id field for the link
	 * 
	 * @param definition LinkObject definition
	 * @return the right id field for the link
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> StoredFieldSchema<String> getRightIdFieldSchema(
			LinkobjectDefinition<E, F, G> definition) {
		return (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("RGID");
	}

	/**
	 * generates a query condition on the left id of the link
	 * 
	 * @param alias                  table alias to generate the id for
	 * @param idvalue                value of the left id
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @return the query condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> QueryCondition getLeftidQueryCondition(
			TableAlias alias, DataObjectId<F> idvalue, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition) {
		LinkobjectDefinition<E, F, G> definition = new LinkobjectDefinition<E, F, G>(parentobjectdefinition,
				leftobjectdefinition, rightobjectdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> id = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("LFID");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, id, new QueryOperatorEqual<String>(),
					(idvalue == null ? BLANK_ID : idvalue.getId()));
		return new SimpleQueryCondition<String>(alias, id, new QueryOperatorEqual<String>(),
				(idvalue == null ? BLANK_ID : idvalue.getId()));
	}

	/**
	 * generates a query condition on the right id of the link
	 * 
	 * @param alias                  table alias to generate the id for
	 * @param idvalue                value of the right id
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @return the query condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> QueryCondition getRightidQueryCondition(
			TableAlias alias, DataObjectId<G> idvalue, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition) {
		LinkobjectDefinition<E, F, G> definition = new LinkobjectDefinition<E, F, G>(parentobjectdefinition,
				leftobjectdefinition, rightobjectdefinition);
		@SuppressWarnings("unchecked")
		StoredFieldSchema<String> id = (StoredFieldSchema<String>) definition.getDefinition().lookupOnName("RGID");
		if (alias == null)
			return new SimpleQueryCondition<String>(null, id, new QueryOperatorEqual<String>(),
					(idvalue == null ? BLANK_ID : idvalue.getId()));
		return new SimpleQueryCondition<String>(alias, id, new QueryOperatorEqual<String>(),
				(idvalue == null ? BLANK_ID : idvalue.getId()));
	}

	/**
	 * gets all the link objects for the provided left id
	 * 
	 * @param leftid                 id of the left object
	 * @param additionalcondition    additional filter condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return all the link objects for the provided left id and additional filter
	 *         condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromleftid(
			DataObjectId<F> leftid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<E, F, G> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, getLeftidQueryCondition(
				alias, leftid, parentobjectdefinition, leftobjectdefinition, rightobjectdefinition));

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
	 * gets all the link objects for the provided left id
	 * 
	 * @param leftid                 id of the left object
	 * @param additionalcondition    additional query condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return all the link objects for the provided left id and additional filter
	 *         condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromleftid(
			DataObjectId<F>[] leftid, QueryCondition additionalcondition,
			DataObjectDefinition<E> parentobjectdefinition, DataObjectDefinition<F> leftobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition, LinkobjectDefinition<E, F, G> propertydefinition) {

		ArrayList<E> results = new ArrayList<E>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (leftid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			QueryCondition objectuniversalcondition = parentobjectdefinition
					.getUniversalQueryCondition(propertydefinition, "SINGLEOBJECT");
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min < leftid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < leftid.length) {
					thisuniqueidcondition = getLeftidQueryCondition(alias, leftid[j], parentobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = getLeftidQueryCondition(alias, null, parentobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);

				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}

			QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			while (answer.next()) {
				E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
				// put all results in a hasmap;
				results.add(formattedanswer);
			}
			}
		}

		return results.toArray(parentobjectdefinition.generateArrayTemplate());

	}

	/**
	 * gets all links from the provided series of right object ids
	 * 
	 * @param rightid                a series of right objet ids
	 * @param additionalcondition    additional filter condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return all the link objects for the provided series of right ids and
	 *         additional filter condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromrightid(
			DataObjectId<G>[] rightid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<E, F, G> propertydefinition) {
		ArrayList<E> results = new ArrayList<E>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (rightid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
			aliaslist.add(alias);
			if (additionalcondition != null)
				if (additionalcondition.getAliases() != null)
					for (int j = 0; j < additionalcondition.getAliases().length; j++)
						aliaslist.add(additionalcondition.getAliases()[j]);
			QueryCondition objectuniversalcondition = parentobjectdefinition
					.getUniversalQueryCondition(propertydefinition, "SINGLEOBJECT");
			if (additionalcondition != null)
				if (additionalcondition.getCondition() != null)
					objectuniversalcondition = new AndQueryCondition(objectuniversalcondition,
							additionalcondition.getCondition());
			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min<rightid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < rightid.length) {
					thisuniqueidcondition = getRightidQueryCondition(alias, rightid[j], parentobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = getRightidQueryCondition(alias, null, parentobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);

				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}

			QueryCondition finalcondition = uniqueidcondition;
			if (objectuniversalcondition != null) {
				finalcondition = new AndQueryCondition(objectuniversalcondition, uniqueidcondition);
			}

			QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias, finalcondition);
			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedcondition));
			while (answer.next()) {
				E formattedanswer = parentobjectdefinition.generateFromRow(answer, alias);
				// put all results in a hasmap;
				results.add(formattedanswer);
			}
		}}

		return results.toArray(parentobjectdefinition.generateArrayTemplate());
	}

	/**
	 * gets all links from the right object id
	 * 
	 * @param rightid                a right object id
	 * @param additionalcondition    additional filter condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return all the link objects for the right object id and additional filter
	 *         condition
	 */

	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromrightid(
			DataObjectId<G> rightid, QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<E, F, G> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, alias,
				getRightidQueryCondition(alias, rightid, parentobjectdefinition, leftobjectdefinition,
						rightobjectdefinition));
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
	 * gets all links from the provided left and right object id
	 * 
	 * @param leftid                 a left object id
	 * @param rightid                a right object id
	 * @param additionalcondition    additional filter condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return all the link objects for the left object id and the right object id
	 *         and additional filter condition
	 */
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getalllinksfromleftandrightid(
			DataObjectId<F> leftid, DataObjectId<G> rightid, QueryFilter additionalcondition,
			DataObjectDefinition<E> parentobjectdefinition, DataObjectDefinition<F> leftobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition, LinkobjectDefinition<E, F, G> propertydefinition) {
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		TableAlias alias = parentobjectdefinition.getAlias("SINGLEOBJECT");
		aliaslist.add(alias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		AndQueryCondition leftandright = new AndQueryCondition();
		leftandright.addCondition(getLeftidQueryCondition(alias, leftid, parentobjectdefinition, leftobjectdefinition,
				rightobjectdefinition));
		leftandright.addCondition(getRightidQueryCondition(alias, rightid, parentobjectdefinition, leftobjectdefinition,
				rightobjectdefinition));
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

	public final static String LINKSANDBOTHOBJECTS_LINKOBJECTALIAS = "SINGLEOBJECT";
	public final static String LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS = "LEFTOBJECTALIAS";
	public final static String LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS = "RIGHTOBJECTALIAS";

	/**
	 * gets the left objects, links and right objects for the given query filter
	 * 
	 * @param additionalcondition    additional filter condition
	 * @param parentobjectdefinition definition of the parent object
	 * @param leftobjectdefinition   definition of the left object for the link
	 * @param rightobjectdefinition  definition of the right object for the link
	 * @param propertydefinition     definition of the link object property
	 * @return an array of triples of left object, link and right object
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> ThreeDataObjects<F, E, G>[] getlinksandbothobjects(
			QueryFilter additionalcondition, DataObjectDefinition<E> parentobjectdefinition,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<E, F, G> propertydefinition) {
		// --- generate three aliases

		TableAlias linkalias = parentobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		TableAlias leftobjectalias = leftobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		TableAlias rightobjectalias = rightobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(leftobjectalias);
		aliaslist.add(rightobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		// LinkobjectDefinition definition = new
		// LinkobjectDefinition(parentobjectdefinition, leftobjectdefinition,
		// rightobjectdefinition);
		AndQueryCondition twojoinscondition = new AndQueryCondition();

		twojoinscondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getLeftIdFieldSchema(propertydefinition), leftobjectalias,
				leftobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		twojoinscondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getRightIdFieldSchema(propertydefinition), rightobjectalias,
				rightobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				twojoinscondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = parentobjectdefinition.extendquery(aliaslist, linkalias, twojoinscondition);

		QueryCondition extendedconditionforleft = leftobjectdefinition.extendquery(aliaslist, leftobjectalias,
				extendedcondition);

		QueryCondition extendedconditionforleftandright = rightobjectdefinition.extendquery(aliaslist, rightobjectalias,
				extendedconditionforleft);

		QueryCondition leftuniversalcondition = leftobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		if (leftuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					leftuniversalcondition);

		QueryCondition rightuniversalcondition = rightobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
		if (rightuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					rightuniversalcondition);

		QueryCondition linkuniversalcondition = parentobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		if (linkuniversalcondition != null)
			extendedconditionforleftandright = new AndQueryCondition(extendedconditionforleftandright,
					linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforleftandright));
		ArrayList<ThreeDataObjects<F, E, G>> returnlist = new ArrayList<ThreeDataObjects<F, E, G>>();
		while (row.next()) {
			F objectone = leftobjectdefinition.generateFromRow(row, leftobjectalias);
			E objecttwo = parentobjectdefinition.generateFromRow(row, linkalias);
			G objectthree = rightobjectdefinition.generateFromRow(row, rightobjectalias);
			returnlist.add(new ThreeDataObjects<F, E, G>(objectone, objecttwo, objectthree));
		}
		return returnlist.toArray(new ThreeDataObjects[0]);
	}

	/**
	 * gets the right object alias for potential right object query
	 * 
	 * @param rightobjectdefinition right object definition
	 * @return the table alias
	 */
	public static <G extends DataObject<G>> TableAlias getRightObjectAliasForPotentialRightObject(
			DataObjectDefinition<G> rightobjectdefinition) {
		TableAlias maintablealias = rightobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);
		return maintablealias;
	}

	/**
	 * gets the left object alias for potential left object query
	 * 
	 * @param leftobjectdefinition left object definition
	 * @return the table alias
	 */
	public static <F extends DataObject<F>> TableAlias getLeftObjectAliasForPotentialLeftObject(
			DataObjectDefinition<F> leftobjectdefinition) {
		TableAlias maintablealias = leftobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);
		return maintablealias;
	}

	/**
	 * gets the link object alias in the get link and both objects
	 * 
	 * @param linkdefinition definition of the link object
	 * @return the provided alias
	 */
	public static <E extends DataObject<E>> TableAlias getLinkObjectAliasForLinkObject(
			DataObjectDefinition<E> linkdefinition) {
		TableAlias maintablealias = linkdefinition.getAlias(LinkobjectQueryHelper.LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		return maintablealias;
	}

	/**
	 * gets the potential right objects for the given left object for link creation
	 * 
	 * @param leftobjectid          left object id
	 * @param additionalcondition   additional query filter
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  definition of the left object
	 * @param rightobjectdefinition definition of the right object
	 * @param propertydefinition    link object property definition
	 * @return the potential right objects
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> G[] getpotentialrightobject(
			DataObjectId<E> leftobjectid, QueryFilter additionalcondition, DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<E> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<F, E, G> propertydefinition) {
		// makes a query on right object

		QueryCondition extendedcondition = null;
		TableAlias maintablealias = rightobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);

		AndQueryCondition allconstraintsquery = null;
		for (int i = 0; i < propertydefinition.getConstraintOnLinkObjectNumber(); i++) {
			ConstraintOnLinkObject<E, G> constraintonlinkobject = propertydefinition.getConstraintOnLinkObject(i);
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
		// TICKET T-223

		if (extendedcondition != null) {
			extendedcondition = (additionalcondition != null
					? new AndQueryCondition(extendedcondition, additionalcondition.getCondition())
					: extendedcondition);
		} else {
			extendedcondition = (additionalcondition != null ? additionalcondition.getCondition() : null);
		}
		G[] result = StoredobjectQueryHelper.get().getallactive(
				new QueryFilter(extendedcondition,
						(additionalcondition != null ? additionalcondition.getAliases() : null)),
				rightobjectdefinition,
				propertydefinition.getRightuniqueidentifieddefinition().getDependentDefinitionStoredobject());

		return result;
	}

	/**
	 * gets the potential left objects for the right object
	 * 
	 * @param rightobjectid         id of the right object
	 * @param additionalcondition   additional query filter
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  definition of the left object
	 * @param rightobjectdefinition definition of the right object
	 * @param propertydefinition    link object property
	 * @return the potential left objects for link creation
	 */
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getpotentialleftobject(
			DataObjectId<G> rightobjectid, QueryFilter additionalcondition,
			DataObjectDefinition<F> linkobjectdefinition, DataObjectDefinition<E> leftobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition, LinkobjectDefinition<F, E, G> propertydefinition) {
		// makes a query on right object

		QueryCondition extendedcondition = null;
		TableAlias maintablealias = leftobjectdefinition
				.getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive);
		AndQueryCondition allconstraintsquery = null;
		for (int i = 0; i < propertydefinition.getConstraintOnLinkObjectNumber(); i++) {
			ConstraintOnLinkObject<E, G> constraintonlinkobject = propertydefinition.getConstraintOnLinkObject(i);
			if (constraintonlinkobject != null) {
				QueryCondition conditiononlinkconstraint = constraintonlinkobject
						.generateReverseQueryFilter(maintablealias, rightobjectid);
				if (allconstraintsquery == null)
					allconstraintsquery = new AndQueryCondition();
				allconstraintsquery.addCondition(conditiononlinkconstraint);
			}
		}
		if (allconstraintsquery != null)
			extendedcondition = allconstraintsquery;
		// TICKET T-223
		if (extendedcondition != null) {
			extendedcondition = new AndQueryCondition(extendedcondition, additionalcondition.getCondition());
		} else {
			extendedcondition = additionalcondition.getCondition();
		}
		E[] result = StoredobjectQueryHelper.get().getallactive(
				new QueryFilter(extendedcondition, additionalcondition.getAliases()), leftobjectdefinition,
				propertydefinition.getLeftuniqueidentifieddefinition().getDependentDefinitionStoredobject());

		return result;
	}

	/**
	 * gets all the left objects with a link from the given right object
	 * 
	 * @param rightid               right object id
	 * @param additionalcondition   additional filter condition
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  left object definition
	 * @param rightobjectdefinition right object definition
	 * @param propertydefinition    definition of the link object property
	 * @return the list of left objects linked from the specified right object
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> E[] getleftobjectsfromright(
			DataObjectId<G> rightid, QueryFilter additionalcondition, DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<E> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<F, E, G> propertydefinition) {
		TableAlias linkalias = linkobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		TableAlias leftobjectalias = leftobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(leftobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		LinkobjectDefinition<F, E, G> definition = new LinkobjectDefinition<F, E, G>(linkobjectdefinition,
				leftobjectdefinition, rightobjectdefinition);
		QueryCondition rightidcondition = linkobjectdefinition.extendquery(aliaslist, linkalias,
				getRightidQueryCondition(linkalias, rightid, linkobjectdefinition, leftobjectdefinition,
						rightobjectdefinition));

		AndQueryCondition twojoinscondition = new AndQueryCondition();
		twojoinscondition.addCondition(rightidcondition);
		twojoinscondition.addCondition(new JoinQueryCondition<String>(linkalias, this.getLeftIdFieldSchema(definition),
				leftobjectalias, leftobjectdefinition.getTableschema().lookupFieldByName("ID"),
				new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				twojoinscondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = linkobjectdefinition.extendquery(aliaslist, linkalias, twojoinscondition);

		QueryCondition extendedconditionforleft = leftobjectdefinition.extendquery(aliaslist, leftobjectalias,
				extendedcondition);

		QueryCondition leftuniversalcondition = leftobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		if (leftuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, leftuniversalcondition);

		QueryCondition linkuniversalcondition = linkobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		if (linkuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforleft));
		ArrayList<E> returnlist = new ArrayList<E>();
		while (row.next()) {
			E objectone = leftobjectdefinition.generateFromRow(row, leftobjectalias);

			returnlist.add(objectone);
		}
		return returnlist.toArray(leftobjectdefinition.generateArrayTemplate());
	}

	/**
	 * an array of link and right objects corresponding to the specified left object
	 * id
	 * 
	 * @param leftid                if of the left object
	 * @param additionalcondition   additional filter condition
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  left object definition
	 * @param rightobjectdefinition right object definition
	 * @param propertydefinition    definition of the link object property
	 * @return a set of link and right objects having the specified id as left
	 *         object
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> TwoDataObjects<F, G>[] getlinksandrightobject(
			DataObjectId<E>[] leftid, QueryFilter additionalcondition, DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<E> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<F, E, G> propertyDefinition) {
		ArrayList<TwoDataObjects<F, G>> results = new ArrayList<TwoDataObjects<F, G>>();

		// work by batches to ensure query is not too long
		for (int i = 0; i < (leftid.length / BATCH_QUERY_SIZE) + 1; i++) {
			NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
			TableAlias linkalias = linkobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
			TableAlias rightobjectalias = rightobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
			aliaslist.add(linkalias);
			aliaslist.add(rightobjectalias);

			OrQueryCondition uniqueidcondition = new OrQueryCondition();
			int min = i * BATCH_QUERY_SIZE;
			if (min<leftid.length) {
			for (int j = min; j < min + BATCH_QUERY_SIZE; j++) {
				QueryCondition thisuniqueidcondition = null;
				if (j < leftid.length) {
					thisuniqueidcondition = getLeftidQueryCondition(linkalias, leftid[j], linkobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);
				} else {
					// all queries will have batch size conditions. If not enough id, a blank id is
					// used
					thisuniqueidcondition = getLeftidQueryCondition(linkalias, null, linkobjectdefinition,
							leftobjectdefinition, rightobjectdefinition);

				}
				uniqueidcondition.addCondition(thisuniqueidcondition);
			}

			AndQueryCondition joinquerycondition = new AndQueryCondition();
			joinquerycondition.addCondition(uniqueidcondition);
			joinquerycondition.addCondition(new JoinQueryCondition<String>(linkalias,
					this.getRightIdFieldSchema(propertyDefinition), rightobjectalias,
					rightobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
			if (additionalcondition != null)
				if (additionalcondition.getCondition() != null)
					joinquerycondition.addCondition(additionalcondition.getCondition());

			QueryCondition extendedcondition = linkobjectdefinition.extendquery(aliaslist, linkalias,
					joinquerycondition);

			QueryCondition extendedconditionforright = rightobjectdefinition.extendquery(aliaslist, rightobjectalias,
					extendedcondition);

			QueryCondition rightuniversalcondition = rightobjectdefinition.getUniversalQueryCondition(null,
					LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
			if (rightuniversalcondition != null)
				extendedconditionforright = new AndQueryCondition(extendedconditionforright, rightuniversalcondition);

			QueryCondition linkuniversalcondition = linkobjectdefinition.getUniversalQueryCondition(null,
					LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
			if (linkuniversalcondition != null)
				extendedconditionforright = new AndQueryCondition(extendedconditionforright, linkuniversalcondition);

			Row answer = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforright));
			while (answer.next()) {
				F objectone = linkobjectdefinition.generateFromRow(answer, linkalias);
				G objecttwo = rightobjectdefinition.generateFromRow(answer, rightobjectalias);
				results.add(new TwoDataObjects<F, G>(objectone, objecttwo));
			}
		}}

		return results.toArray(new TwoDataObjects[0]);

	}

	/**
	 * gets links and right object corresponding to the specified left object id
	 * 
	 * @param leftid                specified left object id
	 * @param additionalcondition   additional filter condition
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  left object definition
	 * @param rightobjectdefinition right object definition
	 * @param propertydefinition    definition of the link object property
	 * @return the list of links and right objects
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> TwoDataObjects<F, G>[] getlinksandrightobject(
			DataObjectId<E> leftid, QueryFilter additionalcondition, DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<E> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<F, E, G> propertyDefinition) {
		TableAlias linkalias = linkobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		TableAlias rightobjectalias = rightobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(rightobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);
		// LinkobjectDefinition<F,E,G> definition = new
		// LinkobjectDefinition<F,E,G>(linkobjectdefinition, leftobjectdefinition,
		// rightobjectdefinition);
		QueryCondition idcondition = linkobjectdefinition.extendquery(aliaslist, linkalias, getLeftidQueryCondition(
				linkalias, leftid, linkobjectdefinition, leftobjectdefinition, rightobjectdefinition));

		AndQueryCondition joinquerycondition = new AndQueryCondition();
		joinquerycondition.addCondition(idcondition);
		joinquerycondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getRightIdFieldSchema(propertyDefinition), rightobjectalias,
				rightobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				joinquerycondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = linkobjectdefinition.extendquery(aliaslist, linkalias, joinquerycondition);

		QueryCondition extendedconditionforright = rightobjectdefinition.extendquery(aliaslist, rightobjectalias,
				extendedcondition);

		QueryCondition rightuniversalcondition = rightobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_RIGHTOBJECTALIAS);
		if (rightuniversalcondition != null)
			extendedconditionforright = new AndQueryCondition(extendedconditionforright, rightuniversalcondition);

		QueryCondition linkuniversalcondition = linkobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		if (linkuniversalcondition != null)
			extendedconditionforright = new AndQueryCondition(extendedconditionforright, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforright));
		ArrayList<TwoDataObjects<F, G>> returnlist = new ArrayList<TwoDataObjects<F, G>>();
		while (row.next()) {

			F objectone = linkobjectdefinition.generateFromRow(row, linkalias);
			G objecttwo = rightobjectdefinition.generateFromRow(row, rightobjectalias);
			returnlist.add(new TwoDataObjects<F, G>(objectone, objecttwo));
		}
		return returnlist.toArray(new TwoDataObjects[0]);
	}

	/**
	 * get left object and links corresponding to the right object id
	 * 
	 * @param rightid               right object id
	 * @param additionalcondition   additional filter condition
	 * @param linkobjectdefinition  definition of the link object
	 * @param leftobjectdefinition  left object definition
	 * @param rightobjectdefinition right object definition
	 * @param propertydefinition    definition of the link object property
	 * @return a list of left objects and links corresponding to the right object id
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>> TwoDataObjects<E, F>[] getlinksandleftobject(
			DataObjectId<G> rightid, QueryFilter additionalcondition, DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<E> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition,
			LinkobjectDefinition<F, E, G> propertyDefinition) {
		TableAlias linkalias = linkobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		TableAlias leftobjectalias = leftobjectdefinition.getAlias(LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		NamedList<TableAlias> aliaslist = new NamedList<TableAlias>();
		aliaslist.add(linkalias);
		aliaslist.add(leftobjectalias);
		if (additionalcondition != null)
			if (additionalcondition.getAliases() != null)
				for (int i = 0; i < additionalcondition.getAliases().length; i++)
					aliaslist.add(additionalcondition.getAliases()[i]);

		// LinkobjectDefinition<F,E,G> definition = new
		// LinkobjectDefinition<F,E,G>(linkobjectdefinition, leftobjectdefinition,
		// rightobjectdefinition);
		QueryCondition idcondition = linkobjectdefinition.extendquery(aliaslist, linkalias, getRightidQueryCondition(
				linkalias, rightid, linkobjectdefinition, leftobjectdefinition, rightobjectdefinition));

		AndQueryCondition joinquerycondition = new AndQueryCondition();
		joinquerycondition.addCondition(idcondition);
		joinquerycondition.addCondition(new JoinQueryCondition<String>(linkalias,
				this.getLeftIdFieldSchema(propertyDefinition), leftobjectalias,
				leftobjectdefinition.getTableschema().lookupFieldByName("ID"), new QueryOperatorEqual<String>()));
		if (additionalcondition != null)
			if (additionalcondition.getCondition() != null)
				joinquerycondition.addCondition(additionalcondition.getCondition());

		QueryCondition extendedcondition = linkobjectdefinition.extendquery(aliaslist, linkalias, joinquerycondition);

		QueryCondition extendedconditionforleft = leftobjectdefinition.extendquery(aliaslist, leftobjectalias,
				extendedcondition);

		QueryCondition leftuniversalcondition = leftobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LEFTOBJECTALIAS);
		if (leftuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, leftuniversalcondition);

		QueryCondition linkuniversalcondition = linkobjectdefinition.getUniversalQueryCondition(null,
				LINKSANDBOTHOBJECTS_LINKOBJECTALIAS);
		if (linkuniversalcondition != null)
			extendedconditionforleft = new AndQueryCondition(extendedconditionforleft, linkuniversalcondition);

		Row row = QueryHelper.getHelper().query(new SelectQuery(aliaslist, extendedconditionforleft));
		ArrayList<TwoDataObjects<E, F>> returnlist = new ArrayList<TwoDataObjects<E, F>>();
		while (row.next()) {

			E objectone = leftobjectdefinition.generateFromRow(row, leftobjectalias);
			F objecttwo = linkobjectdefinition.generateFromRow(row, linkalias);
			returnlist.add(new TwoDataObjects<E, F>(objectone, objecttwo));
		}
		return returnlist.toArray(new TwoDataObjects[0]);
	}

}
