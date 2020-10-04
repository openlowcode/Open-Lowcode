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

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectFieldDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.DisplayProfile;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * definition of a link between two objects of the same class
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object used as autolink
 * @param <F> the data object being referenced by the autolink
 */
public class AutolinkobjectDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & HasidInterface<F>>
		extends DataObjectPropertyDefinition<E> {
	private static Logger logger = Logger.getLogger(AutolinkobjectDefinition.class.getName());
	private DataObjectDefinition<F> linkedobjectdefinition;
	private StringStoredField leftid;
	private StringStoredField rightid;

	private DisplayProfile<E> displayprofilehideleftobjectfields;
	private DisplayProfile<E> displayprofilehiderightobjectfields;
	private boolean symetriclink;
	private ArrayList<ConstraintOnAutolinkObject<F>> contrainstonautolinkobject;
	private UniqueidentifiedDefinition<E> uniqueidentified;
	private UniqueidentifiedDefinition<F> linkedobjectuniqueidentifieddefinition;
	private DataObjectDefinition<E> linkobjectdefinition;

	/**
	 * @return the related unique identified definition
	 */
	public UniqueidentifiedDefinition<E> getUniqueidentifiedDefinitionForLinkObject() {
		return this.uniqueidentified;
	}

	/**
	 * @return the definition of the auto-link object
	 */
	protected DataObjectDefinition<E> getLinkObjectDefinition() {
		return this.linkobjectdefinition;
	}

	/**
	 * @return the definition of the object that is the source and target of the
	 *         auto-link
	 */
	protected DataObjectDefinition<F> getLinkedObjectDefinition() {
		return this.linkedobjectdefinition;
	}

	/**
	 * @return true if the link is symetric
	 */
	public boolean isSymetricLink() {
		return this.symetriclink;
	}

	/**
	 * sets the link as symetric. If the link is symetric, it means linking A and B
	 * does not have a direction (and is the same as linking B and A)
	 */
	public void setSymetricLink() {
		this.symetriclink = true;
	}

	/**
	 * creates the definition of an auto-link object property
	 * 
	 * @param linkobjectdefinition   the data object used as auto-link
	 * @param linkedobjectdefinition the data object referenced by the auto-link
	 */
	public AutolinkobjectDefinition(DataObjectDefinition<E> linkobjectdefinition,
			DataObjectDefinition<F> linkedobjectdefinition) {
		super(linkobjectdefinition, "AUTOLINKOBJECT");
		this.linkedobjectdefinition = linkedobjectdefinition;
		this.linkobjectdefinition = linkobjectdefinition;
		this.leftid = new StringStoredField("LFID", null, 200);
		this.rightid = new StringStoredField("RGID", null, 200);
		this.addFieldSchema(leftid);
		this.addFieldSchema(rightid);
		StoredTableIndex lfidindex = new StoredTableIndex("LFID");
		lfidindex.addStoredFieldSchame(leftid);
		this.addIndex(lfidindex);
		StoredTableIndex rgidindex = new StoredTableIndex("RGID");
		rgidindex.addStoredFieldSchame(rightid);
		this.addIndex(rgidindex);

		this.contrainstonautolinkobject = new ArrayList<ConstraintOnAutolinkObject<F>>();
		this.displayprofilehideleftobjectfields = parentobject.getDisplayProfileByName("HIDELEFTOBJECTFIELDS");
		this.displayprofilehiderightobjectfields = parentobject.getDisplayProfileByName("HIDERIGHTOBJECTFIELDS");
		this.symetriclink = false;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();
		// - Left join query condition
		logger.fine("	|*|_|*| ------------------- Starting adding properties for autolink for object "
				+ linkedobjectdefinition.getName() + " - " + linkedobjectdefinition.getFieldNumber()
				+ "-------------------");

		// adds an external field is the target object is named
		JoinQueryConditionDefinition<String> leftjoincondition = linkedobjectdefinition.generateJoinQueryDefinition(
				this.parentobject.getTableschema(), leftid, "UNIQUEIDENTIFIED", "ID", this.getName() + "_LEFT",
				new QueryOperatorEqual<String>());

		// get named property for left object
		if (linkedobjectdefinition.hasProperty("NAMED")) {
			@SuppressWarnings("unchecked")
			NamedDefinition<E> named = (NamedDefinition<E>) linkedobjectdefinition.getProperty("NAMED");

			ExternalFieldSchema<?> leftexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "LEFTNAME", "Left object name", "this is a stupid comment", "NAMED", "OBJECTNAME",
					leftjoincondition, this.displayprofilehideleftobjectfields, 800, 80);
			logger.fine(" ---+++--- setting exceptional name " + named.getNameLabel());
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					named.getNameLabel());
			externalfieldlist.add(leftexternalfield);
		}
		// get named property for left object
		if (linkedobjectdefinition.hasProperty("NUMBERED")) {
			@SuppressWarnings("unchecked")
			NumberedDefinition<E> numbered = (NumberedDefinition<E>) linkedobjectdefinition.getProperty("NUMBERED");

			ExternalFieldSchema<?> leftexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "LEFTNR", "Left object number", "this is a stupid comment", "NUMBERED", "NR",
					leftjoincondition, this.displayprofilehideleftobjectfields, 900, 40);
			logger.fine(" ---+++---setting exceptional number " + numbered.getNumberLabel());
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					numbered.getNumberLabel());
			externalfieldlist.add(leftexternalfield);
		}
		// get state property for left object

		if (linkedobjectdefinition.hasProperty("LIFECYCLE")) {
			LifecycleDefinition<?, ?> lifecycle = (LifecycleDefinition<?, ?>) (linkedobjectdefinition
					.getProperty("LIFECYCLE"));
			ExternalFieldSchema<?> leftexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "LEFTSTATE", "Left state", "this is a stupid comment", "LIFECYCLE", "STATE",
					lifecycle.getLifecycleHelper(), leftjoincondition, this.displayprofilehideleftobjectfields, 500,
					35);
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields, "State");
			externalfieldlist.add(leftexternalfield);
		}

		if (linkedobjectdefinition.hasProperty("TIMESLOT")) {
			@SuppressWarnings("unused")
			TimeslotDefinition<?> timeslotdefinition = (TimeslotDefinition<?>) (linkedobjectdefinition
					.getProperty("TIMESLOT"));
			ExternalFieldSchema<?> starttimeexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "LEFTSTARTTIME", "Left Start Time", "this is a stupid comment", "TIMESLOT",
					"STARTTIME", leftjoincondition, this.displayprofilehideleftobjectfields, 540, 35);
			starttimeexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					"Start Time");
			externalfieldlist.add(starttimeexternalfield);
			ExternalFieldSchema<?> endtimeexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "LEFTENDTIME", "Left End Time", "this is a stupid comment", "TIMESLOT", "ENDTIME",
					leftjoincondition, this.displayprofilehideleftobjectfields, 530, 35);
			endtimeexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					"End Time");
			externalfieldlist.add(endtimeexternalfield);

		}

		JoinQueryConditionDefinition<String> rightjoincondition = linkedobjectdefinition.generateJoinQueryDefinition(
				this.parentobject.getTableschema(), rightid, "UNIQUEIDENTIFIED", "ID", this.getName() + "_RIGHT",
				new QueryOperatorEqual<String>());

		// get named property for right object

		if (linkedobjectdefinition.hasProperty("NAMED")) {
			// adds an external field is the target object is named -- currently, there is
			// an error
			@SuppressWarnings("unchecked")
			NamedDefinition<E> named = (NamedDefinition<E>) linkedobjectdefinition.getProperty("NAMED");
			ExternalFieldSchema<?> rightexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "RIGHTNAME", "Right object name", "this is a stupid comment", "NAMED",
					"OBJECTNAME", rightjoincondition, this.displayprofilehiderightobjectfields, 800, 80);
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					named.getNameLabel());
			externalfieldlist.add(rightexternalfield);
		}

		if (linkedobjectdefinition.hasProperty("NUMBERED")) {
			// adds an external field is the target object is named -- currently, there is
			// an error
			@SuppressWarnings("unchecked")
			NumberedDefinition<E> numbered = (NumberedDefinition<E>) linkedobjectdefinition.getProperty("NUMBERED");
			ExternalFieldSchema<?> rightexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "RIGHTNR", "Right object number", "this is a stupid comment", "NUMBERED", "NR",
					rightjoincondition, this.displayprofilehiderightobjectfields, 900, 40);
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					numbered.getNumberLabel());
			externalfieldlist.add(rightexternalfield);
		}
		if (linkedobjectdefinition.hasProperty("LIFECYCLE")) {
			LifecycleDefinition<?, ?> lifecycle = (LifecycleDefinition<?, ?>) (linkedobjectdefinition
					.getProperty("LIFECYCLE"));
			ExternalFieldSchema<?> rightexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "RIGHTSTATE", "Right object state", "this is a stupid comment", "LIFECYCLE",
					"STATE", lifecycle.getLifecycleHelper(), rightjoincondition,
					this.displayprofilehiderightobjectfields, 500, 35);
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields, "State");
			externalfieldlist.add(rightexternalfield);
		}

		if (linkedobjectdefinition.hasProperty("TIMESLOT")) {
			@SuppressWarnings("unused")
			TimeslotDefinition<?> timeslotdefinition = (TimeslotDefinition<?>) (linkedobjectdefinition
					.getProperty("TIMESLOT"));
			ExternalFieldSchema<?> starttimeexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "RIGHTSTARTTIME", "Right Start Time", "this is a stupid comment", "TIMESLOT",
					"STARTTIME", rightjoincondition, this.displayprofilehiderightobjectfields, 540, 35);
			starttimeexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					"Start Time");
			externalfieldlist.add(starttimeexternalfield);
			ExternalFieldSchema<?> endtimeexternalfield = linkedobjectdefinition.generateExternalField(
					this.getName() + "RIGHTENDTIME", "Right End Time", "this is a stupid comment", "TIMESLOT",
					"ENDTIME", rightjoincondition, this.displayprofilehiderightobjectfields, 530, 35);
			endtimeexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					"End Time");
			externalfieldlist.add(endtimeexternalfield);

		}

		logger.fine("	|*|_|*| ------------------- Starting adding fields for autolink for object "
				+ linkedobjectdefinition.getName() + " - " + linkedobjectdefinition.getFieldNumber()
				+ "-------------------");
		for (int i = 0; i < linkedobjectdefinition.getFieldNumber(); i++) {
			DataObjectFieldDefinition<F> thisfield = linkedobjectdefinition.getFieldAt(i);
			if (thisfield.getPriority() >= 500) {
				ExternalFieldSchema<?> rightexternalfield = linkedobjectdefinition.generateExternalField(
						"RIGHTOBJECT" + thisfield.getName(), thisfield.getDisplayname(), thisfield.getTooltip(),
						thisfield.getName(), rightjoincondition, this.displayprofilehiderightobjectfields,
						thisfield.getPriority(), thisfield.getDefaultcolumnintable());
				rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
						thisfield.getDisplayname());
				logger.fine("	|*|_|*| ------------ Adding right field " + thisfield.getName() + " in autolink");
				externalfieldlist.add(rightexternalfield);
			} else {
				logger.fine("	|*|_|*| field " + thisfield.getName() + " discarded as priority not enough : "
						+ thisfield.getPriority());
			}
		}

		logger.fine("	|*|_|*| ------------------- Starting adding fields for autolink for object "
				+ linkedobjectdefinition.getName() + " - " + linkedobjectdefinition.getFieldNumber()
				+ "-------------------");
		for (int i = 0; i < linkedobjectdefinition.getFieldNumber(); i++) {
			DataObjectFieldDefinition<F> thisfield = linkedobjectdefinition.getFieldAt(i);
			if (thisfield.getPriority() >= 500) {
				ExternalFieldSchema<?> leftexternalfield = linkedobjectdefinition.generateExternalField(
						"LEFTOBJECT" + thisfield.getName(), thisfield.getDisplayname(), thisfield.getTooltip(),
						thisfield.getName(), leftjoincondition, this.displayprofilehideleftobjectfields,
						thisfield.getPriority(), thisfield.getDefaultcolumnintable());
				leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
						thisfield.getDisplayname());
				logger.fine("	|*|_|*| ------------ Adding left field " + thisfield.getName() + " in autolink");
				externalfieldlist.add(leftexternalfield);
			} else {
				logger.fine("	|*|_|*| field " + thisfield.getName() + " discarded as priority not enough : "
						+ thisfield.getPriority());
			}
		}

		return externalfieldlist;

	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[2];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Left id",
				"the technical id generated by the system for the left object of the part", leftid, false, true, -200,
				16, this.linkobjectdefinition);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Right id",
				"the technical id generated by the system for the right object of the part", rightid, false, true, -200,
				16, this.linkobjectdefinition);

		return returnvalue;
	}

	/**
	 * @return the number of constraints on auto-link object number
	 */
	public int getConstraintOnAutolinkObjectNumber() {
		return contrainstonautolinkobject.size();
	}

	/**
	 * @return all the constraints on autolink
	 */
	@SuppressWarnings("unchecked")
	public ConstraintOnAutolinkObject<F>[] getAllConstraintsOnAutolinkObject() {
		return contrainstonautolinkobject.toArray(new ConstraintOnAutolinkObject[0]);
	}

	/**
	 * @param index get constraint on auto-link object for the provided index
	 * @return the constraint at the given index
	 */
	public ConstraintOnAutolinkObject<F> getConstraintOnAutolinkObject(int index) {
		return contrainstonautolinkobject.get(index);
	}

	/**
	 * sets an additional constraint on the auto-link object
	 * 
	 * @param constraintonautolinkobject constraint on the auto-link object
	 */
	public void setContraintOnAutolinkObject(ConstraintOnAutolinkObject<F> constraintonautolinkobject) {
		this.contrainstonautolinkobject.add(constraintonautolinkobject);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Autolinkobject<E, F>(this, parentpayload, linkedobjectdefinition);
	}

	/**
	 * set dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * sets the unique identified definition of the object being referenced by the
	 * link
	 * 
	 * @param linkedobjectuniqueidentifieddefinition definition of the hasid property of the data
	 *                                               object referenced by the link
	 */
	public void setGenericsObjectforlinkProperty(UniqueidentifiedDefinition<F> linkedobjectuniqueidentifieddefinition) {
		this.linkedobjectuniqueidentifieddefinition = linkedobjectuniqueidentifieddefinition;

	}

	/**
	 * gets the unique identified property of the linked object
	 * 
	 * @return the unique identified property of the linked object
	 */
	public UniqueidentifiedDefinition<F> getLinkedObjectUniqueidentifiedDefinition() {
		return this.linkedobjectuniqueidentifieddefinition;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet implemented");
	}
}