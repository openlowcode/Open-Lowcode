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

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * the flat file loader allows to create auto-links provided the object is
 * numbered. It is possible to indicate a series of numbers separated by '|'
 * (pipe character)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object being linked
 * @param <F> data object of the link
 */
public class HasautolinkFlatFileLoader<E extends DataObject<E> & NumberedInterface<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & AutolinkobjectInterface<F, E>>
		extends FlatFileLoaderColumn<E> {
	private DataObjectDefinition<E> leftobjectdefinition;
	private DataObjectDefinition<F> linkobjectdefinition;
	private AutolinkobjectDefinition<F, E> autolinkobjectpropertydef;
	private NumberedDefinition<E> leftobjectnumberproperty;
	private boolean deletelinkifnotmentioned;
	@SuppressWarnings("unused")
	private ConstraintOnAutolinkObject<E>[] constraints;
	@SuppressWarnings("unused")
	private HashMap<String, E> rightobjectmapbynumber;
	private static Logger logger = Logger.getLogger(HasautolinkFlatFileLoader.class.getName());

	/**
	 * creates a flat file loader for object with has autolink
	 * 
	 * @param leftobjectdefinition      definition of the object being linked
	 * @param linkobjectdefinition      definition of the link object
	 * @param autolinkobjectpropertydef definition of the autolink property on the
	 *                                  link
	 * @param leftobjectnumberproperty  definition of the numbered property on the
	 *                                  object
	 * @param deletelinkifnotmentioned  if true, during the loading, the loader will
	 *                                  ensure the list of links is exactly the one
	 *                                  provided, i.e. it will delete links that are
	 *                                  not mentioned
	 * @param constraints               constraints for the link
	 */
	public HasautolinkFlatFileLoader(DataObjectDefinition<E> leftobjectdefinition,
			DataObjectDefinition<F> linkobjectdefinition, AutolinkobjectDefinition<F, E> autolinkobjectpropertydef,
			NumberedDefinition<E> leftobjectnumberproperty, boolean deletelinkifnotmentioned,
			ConstraintOnAutolinkObject<E>[] constraints) {
		this.leftobjectdefinition = leftobjectdefinition;
		this.linkobjectdefinition = linkobjectdefinition;
		this.autolinkobjectpropertydef = autolinkobjectpropertydef;
		this.leftobjectnumberproperty = leftobjectnumberproperty;
		this.deletelinkifnotmentioned = deletelinkifnotmentioned;
		this.constraints = constraints;
		this.rightobjectmapbynumber = new HashMap<String, E>();
	}

	@Override
	public boolean secondpass() {
		return false;
	}

	@Override
	public void postprocessLine(E object, Object value) {
		String stringvalue = FlatFileLoader.parseObject(object,
				"Hasautolink for object " + linkobjectdefinition.getName());

		DataObjectId<E> leftid = object.getId();
		F[] alllinksforleft = AutolinkobjectQueryHelper.get().getalllinksfromleftid(leftid, null, linkobjectdefinition,
				leftobjectdefinition, autolinkobjectpropertydef);
		HashMap<DataObjectId<E>, F> oldlinksbyrightid = new HashMap<DataObjectId<E>, F>();
		for (int i = 0; i < alllinksforleft.length; i++)
			oldlinksbyrightid.put(alllinksforleft[i].getRgid(), alllinksforleft[i]);
		HashMap<DataObjectId<E>, F> newlinksbyrightid = new HashMap<DataObjectId<E>, F>();

		if (stringvalue.length() > 0) {
			String[] rightobjectnumbers = stringvalue.split("\\|");
			for (int i = 0; i < rightobjectnumbers.length; i++) {
				String thisrightobjectnumber = rightobjectnumbers[i];

				// check if the link exists

				if (leftid != null) {

					AndQueryCondition allconstraintsquery = null;
					TableAlias maintablealias = linkobjectdefinition.getAlias(NumberedQueryHelper.SINGLEOBJECT);
					for (int k = 0; k < autolinkobjectpropertydef.getConstraintOnAutolinkObjectNumber(); k++) {
						ConstraintOnAutolinkObject<E> constraintonlinkobject = autolinkobjectpropertydef
								.getConstraintOnAutolinkObject(k);
						if (constraintonlinkobject != null) {
							QueryCondition conditiononlinkconstraint = constraintonlinkobject
									.generateQueryFilter(maintablealias, leftid);
							if (allconstraintsquery == null)
								allconstraintsquery = new AndQueryCondition();
							allconstraintsquery.addCondition(conditiononlinkconstraint);
						}
					}
					E[] rightobject = null;
					if (autolinkobjectpropertydef.getConstraintOnAutolinkObjectNumber() > 0) {
						rightobject = NumberedQueryHelper.get().getobjectbynumber(thisrightobjectnumber,
								allconstraintsquery, leftobjectdefinition, leftobjectnumberproperty);

					} else {
						rightobject = NumberedQueryHelper.get().getobjectbynumber(thisrightobjectnumber,
								leftobjectdefinition, leftobjectnumberproperty);

					}

					if (rightobject.length != 1)
						throw new RuntimeException("There should be exactly 1 right object with number '"
								+ thisrightobjectnumber + "' respecting link constraints but there is "
								+ rightobject.length + " '" + leftobjectdefinition.getName() + "' for link '"
								+ linkobjectdefinition.getName() + "' ");
					DataObjectId<E> rightobjectid = rightobject[0].getId();
					logger.info(" -- performing query to get all links, index = " + i);
					F oldlink = oldlinksbyrightid.get(rightobjectid);
					if (oldlink != null)
						newlinksbyrightid.put(rightobjectid, oldlink);
					logger.info(" -- query done to get all links, index = " + i);

					if (oldlink == null) {
						F link = linkobjectdefinition.generateBlank();
						link.setleftobject(leftid);
						link.setrightobject(rightobjectid);
						logger.info(" -- preparing insert, index = " + i);

						link.insert();
						newlinksbyrightid.put(rightobjectid, link);
						logger.info(" -- insert done, index = " + i);
					}
				}

			}

		}
		// delete here if option chosen.
		if (this.deletelinkifnotmentioned) {
			Iterator<F> oldlinksiterator = oldlinksbyrightid.values().iterator();
			while (oldlinksiterator.hasNext()) {
				F thisoldlink = oldlinksiterator.next();
				if (newlinksbyrightid.get(thisoldlink.getRgid()) == null)
					thisoldlink.delete();
			}
		}

	}

	@Override
	public boolean finalpostprocessing() {
		return true;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		return false;
	}

	@Override
	public boolean isStaticPreProcessing() {
		return false;
	}

	public String dependentColumnForPreparator() {
		return null;

	}

	@Override
	public void staticpreprocessor(String next) {

	}

	@Override
	public boolean processAfterLineInsertion() {
		return true;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		TwoDataObjects<F, E>[] links = AutolinkobjectQueryHelper.get().getlinksandrightobject(currentobject.getId(),
				null, linkobjectdefinition, leftobjectdefinition, autolinkobjectpropertydef);
		StringBuffer linksummary = new StringBuffer();
		for (int i = 0; i < links.length; i++) {
			TwoDataObjects<F, E> thislink = links[i];
			if (i > 0)
				linksummary.append('|');
			linksummary.append(thislink.getObjectTwo().getNr());
		}
		cell.setCellValue(linksummary.toString());
		return false;
	}
}
