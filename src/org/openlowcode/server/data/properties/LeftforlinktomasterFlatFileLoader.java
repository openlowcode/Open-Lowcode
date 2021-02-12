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
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkToMaster;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * A loader allowing to create links to master to right objects using the right
 * object numbers. In the flat file, a series of numbers separated by '|' are
 * entered in the flat file, and necessary links are created. If the
 * corresponding option is set, missing links are also deleted
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current object (left for link)
 * @param <F> data object holding the link to master
 * @param <G> right data object for link to master
 */
public class LeftforlinktomasterFlatFileLoader<
		E extends DataObject<E> & NumberedInterface<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & LinkobjecttomasterInterface<F, E, G>,
		G extends DataObject<G> & NumberedInterface<G> & VersionedInterface<G>>
		extends
		FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(LeftforlinkFlatFileLoader.class.getName());
	private HashMap<String, G> rightobjectmapbynumber;
	private DataObjectDefinition<G> rightobjectdefinition;
	private DataObjectDefinition<F> linkobjectdefinition;
	private DataObjectDefinition<E> leftobjectdefinition;
	private LinkobjecttomasterDefinition<F, E, G> linktomasterpropertydef;
	private NumberedDefinition<G> rightobjectnumberproperty;
	private boolean createrightifnotexists;
	private boolean deletemissinglink;
	private ConstraintOnLinkToMaster<E, G>[] constraints;
	private String hardcodedvalue;

	/**
	 * creates the flat file loader to create links having as left object the object
	 * loaded in the left
	 * 
	 * @param leftobjectdefinition      definition of the main data object (left for
	 *                                  link to master)
	 * @param linkobjectdefinition      definition of the data object that holds the
	 *                                  link to master
	 * @param rightobjectdefinition     definition of the right data object
	 *                                  definition that holds the link to master
	 * @param linktomasterpropertydef   definition of the link to master object
	 *                                  property
	 * @param rightobjectnumberproperty the numbered property of the right object
	 *                                  (loader can be used only if the right object
	 *                                  is numbered)
	 * @param createrightifnotexists    create the right object if it does not
	 *                                  exists
	 * @param deletemissinglink         if delete missing link is set, necessary
	 *                                  links will be created so that the list of
	 *                                  links is exactly the one in the file. If
	 *                                  this option is not set, only missing links
	 *                                  are created
	 * @param hardcodedvalue            if set, if this column has any content, the
	 *                                  hard coded value is used to load links
	 * @param constraints               constraints on the link
	 */
	public LeftforlinktomasterFlatFileLoader(
			DataObjectDefinition<E> leftobjectdefinition,
			DataObjectDefinition<F> linkobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition,
			LinkobjecttomasterDefinition<F, E, G> linktomasterpropertydef,
			NumberedDefinition<G> rightobjectnumberproperty,
			boolean createrightifnotexists,
			boolean deletemissinglink,
			String hardcodedvalue,
			ConstraintOnLinkToMaster<E, G>[] constraints) {
		this.leftobjectdefinition = leftobjectdefinition;
		this.linkobjectdefinition = linkobjectdefinition;
		this.rightobjectdefinition = rightobjectdefinition;
		this.linktomasterpropertydef = linktomasterpropertydef;
		this.rightobjectnumberproperty = rightobjectnumberproperty;
		this.createrightifnotexists = createrightifnotexists;
		this.rightobjectmapbynumber = new HashMap<String, G>();
		this.constraints = constraints;
		this.deletemissinglink = deletemissinglink;
		this.hardcodedvalue = hardcodedvalue;
	}

	@Override
	public boolean isStaticPreProcessing() {
// when there are constraints on objects, there will be no attempt to create
// right objects statically at the beginning
		if (linktomasterpropertydef.getConstraintOnLinkToMasterNumber() > 0)
			return false;
		return true;
	}

	/**
	 * get dependent column for preparator
	 * 
	 * @return null value
	 */
	public String dependentColumnForPreparator() {
		return null;

	}

	@Override
	public void staticpreprocessor(String next) {
		if (constraints.length > 0)
			if (createrightifnotexists)
				throw new RuntimeException("Link " + linkobjectdefinition + " have " + constraints.length
						+ " not yet supported constraints ");
		if (next.length() > 0) {
			logger.fine("preprocessing for left for link for value = " + next);
			String[] rightobjectnumbers = null;
			if (hardcodedvalue == null) {
				rightobjectnumbers = next.split("\\|");
			} else {
				rightobjectnumbers = new String[] { hardcodedvalue };
			}

			for (int i = 0; i < rightobjectnumbers.length; i++) {
				String thisrightobjectnumber = rightobjectnumbers[i];
				if (!rightobjectmapbynumber.containsKey(thisrightobjectnumber)) {
					// new object not yet processed
					G[] thisrightobject = NumberedQueryHelper.get().getobjectbynumber(thisrightobjectnumber,
							VersionedQueryHelper.getLatestVersionQueryCondition(
									rightobjectdefinition.getAlias(NumberedQueryHelper.SINGLEOBJECT),
									rightobjectdefinition),
							rightobjectdefinition, rightobjectnumberproperty);

					if (thisrightobject.length == 1) {
						rightobjectmapbynumber.put(thisrightobjectnumber, thisrightobject[0]);
						logger.fine("Found and added rightobject = " + thisrightobjectnumber);
					}

					if (thisrightobject.length == 0) {
						if (createrightifnotexists) {
							G newrightobject = rightobjectdefinition.generateBlank();
							newrightobject.setobjectnumber(thisrightobjectnumber);
							newrightobject.insert();
							rightobjectmapbynumber.put(thisrightobjectnumber, newrightobject);
						}
					}
					if (thisrightobject.length > 1)
						throw new RuntimeException("found several right objects in latest version for " + rightobjectdefinition.getName()
								+ " with number " + thisrightobjectnumber);
				}
			}
		}

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		boolean oneupdate = false;
		String stringvalue = FlatFileLoader.parseObject(value, "Left for Link for " + linkobjectdefinition.getName());
		DataObjectId<E> leftid = object.getId();
		HashMap<DataObjectMasterId<G>, F> oldlinksfromrightid = new HashMap<DataObjectMasterId<G>, F>();
		F[] existinglinks = LinkobjecttomasterQueryHelper.get().getalllinksfromleftid(leftid, null,
				linkobjectdefinition, leftobjectdefinition, rightobjectdefinition, linktomasterpropertydef);
		for (int i = 0; i < existinglinks.length; i++)
			oldlinksfromrightid.put(existinglinks[i].getRgmsid(), existinglinks[i]);
		HashMap<DataObjectMasterId<G>, F> newlinksfromrightid = new HashMap<DataObjectMasterId<G>, F>();

		if (stringvalue.length() > 0) {
			String[] rightobjectnumbers = null;
			if (hardcodedvalue == null) {
				rightobjectnumbers = stringvalue.split("\\|");
			} else {
				rightobjectnumbers = new String[] { hardcodedvalue };
			}
			for (int i = 0; i < rightobjectnumbers.length; i++) {
				String thisrightobjectnumber = rightobjectnumbers[i];

				// check if the link exists

				if (leftid != null) {
					G rightobject = null;
					// no constraint - preload
					if (linktomasterpropertydef.getConstraintOnLinkToMasterNumber() == 0) {
						rightobject = rightobjectmapbynumber.get(thisrightobjectnumber);
					} else {
						// contraint : load object
						G[] rightobjecttable = null;
						AndQueryCondition allconstraintsquery = null;
						TableAlias maintablealias = linkobjectdefinition.getAlias(NumberedQueryHelper.SINGLEOBJECT);
						for (int k = 0; k < linktomasterpropertydef.getConstraintOnLinkToMasterNumber(); k++) {
							ConstraintOnLinkToMaster<
									E,
									G> constraintonlinkobject = linktomasterpropertydef.getConstraintOnLinkToMaster(k);
							if (constraintonlinkobject != null) {
								QueryCondition conditiononlinkconstraint = constraintonlinkobject
										.generateQueryFilter(maintablealias, leftid);
								if (allconstraintsquery == null)
									allconstraintsquery = new AndQueryCondition();
								allconstraintsquery.addCondition(conditiononlinkconstraint);
							}
						}

						if (linktomasterpropertydef.getConstraintOnLinkToMasterNumber() > 0) {
							rightobjecttable = NumberedQueryHelper.get().getobjectbynumber(thisrightobjectnumber,
									allconstraintsquery, rightobjectdefinition, rightobjectnumberproperty);

						} else {
							rightobjecttable = NumberedQueryHelper.get().getobjectbynumber(thisrightobjectnumber,
									rightobjectdefinition, rightobjectnumberproperty);

						}
						if (rightobjecttable.length > 1)
							throw new RuntimeException("There should be exactly 1 right object with number '"
									+ thisrightobjectnumber + "' respecting link constraints but there is "
									+ rightobjecttable.length + " '" + rightobjectdefinition.getName() + "' for link '"
									+ linkobjectdefinition.getName() + "' ");
						if (rightobjecttable.length == 1)
							rightobject = rightobjecttable[0];
					}
					if (rightobject == null)
						if (!this.createrightifnotexists) {
							throw new RuntimeException("The right object '" + thisrightobjectnumber
									+ "' does not exist for object '" + leftobjectdefinition.getName() +

									"' for link '" + linkobjectdefinition.getName() + "' ");
						}
					// if the object does not exist, and if there is an order to create it, it means
					// it is an object to
					// be created with constraints. It cannot be done on preprocessing as context
					// left object is needed.
					if (rightobject == null)
						if (this.createrightifnotexists) {
							G newrightobject = rightobjectdefinition.generateBlank();
							newrightobject.setobjectnumber(thisrightobjectnumber);
							for (int j = 0; j < linktomasterpropertydef.getConstraintOnLinkToMasterNumber(); j++) {
								ConstraintOnLinkToMaster<
										E, G> constraint = linktomasterpropertydef.getConstraintOnLinkToMaster(j);
								constraint.enrichRightObjectAfterCreation(newrightobject, object);
							}
							newrightobject.insert();
							rightobject = newrightobject;
						}
					DataObjectMasterId<G> rightobjectmasterid = rightobject.getMasterid();

					F existinglink = oldlinksfromrightid.get(rightobjectmasterid);
					if (existinglink != null)
						newlinksfromrightid.put(rightobjectmasterid, existinglink);

					if (existinglink == null) {
						F link = linkobjectdefinition.generateBlank();
						link.setleftobject(leftid);
						link.setrightobjectmaster(rightobjectmasterid);
						logger.fine(" -- preparing insert, index = " + i);

						link.insert();
						newlinksfromrightid.put(rightobjectmasterid, link);
						oneupdate = true;
						logger.fine(" -- insert done, index = " + i);
					}
				}

			}
		}
		if (this.deletemissinglink) {
			Iterator<F> oldlinksiterator = oldlinksfromrightid.values().iterator();
			while (oldlinksiterator.hasNext()) {
				F thisoldlink = oldlinksiterator.next();
				if (newlinksfromrightid.get(thisoldlink.getRgmsid()) == null)
					thisoldlink.delete();
			}
		}

		return oneupdate;
	}

	@Override
	public boolean processAfterLineInsertion() {
		return true;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		logger.finest("  -------------- starting object extraction for " + currentobject.getNr() + " for link "
				+ linkobjectdefinition.getLabel() + " ---------------------- ");
		TwoDataObjects<F, G>[] links = LinkobjecttomasterQueryHelper.get().getlinksandrightobject(currentobject.getId(),
				null, linkobjectdefinition, leftobjectdefinition, rightobjectdefinition, linktomasterpropertydef);
		logger.finest("    * found elements for link " + linkobjectdefinition.getLabel() + " nb=" + links.length);
		StringBuffer linksummary = new StringBuffer();
		for (int i = 0; i < links.length; i++) {
			TwoDataObjects<F, G> thislink = links[i];
			if (i > 0)
				linksummary.append('|');
			linksummary.append(thislink.getObjectTwo().getNr());
		}
		cell.setCellValue(linksummary.toString());

		logger.finest(" -----------------------------------------------------------------------------------");
		return false;
	}

}
