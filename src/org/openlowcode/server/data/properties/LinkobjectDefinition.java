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
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkObject;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;

/**
 * Definition of the property making this data object a link object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> link object
 * @param <F> object at left of link
 * @param <G> object at right of link
 */
public class LinkobjectDefinition<E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends DataObjectPropertyDefinition<E> {
	private DataObjectDefinition<F> leftobjectdefinition;
	private DataObjectDefinition<G> rightobjectdefinition;
	private StringStoredField leftid;
	private StringStoredField rightid;
	private ArrayList<ConstraintOnLinkObject<F, G>> constraintsonlinkobject;
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;
	private UniqueidentifiedDefinition<F> leftuniqueidentifieddefinition;
	private UniqueidentifiedDefinition<G> rightuniqueidentifieddefinition;
	private DisplayProfile<E> displayprofilehideleftobjectfields;
	private DisplayProfile<E> displayprofilehiderightobjectfields;
	private DataObjectDefinition<E> linkobjectdefinition;
	private boolean constraintmaxonelinkfromleft;
	private boolean replaceifmorethanonefromleft;
	private boolean replaceifnotuniqueforleftandright;
	private boolean constraintuniqueforleftandright;

	private boolean showfieldsforleftobject;
	private boolean showfieldsforrightobject;
	private int minpriorityforleftobjectfields;
	private int minpriorityforrightobjectfields;
	private static Logger logger = Logger.getLogger(LinkobjectDefinition.class.getName());

	/**
	 * @return
	 */
	protected DataObjectDefinition<G> getRightObjectDefinition() {
		return this.rightobjectdefinition;
	}

	/**
	 * @return
	 */
	protected DataObjectDefinition<F> getLeftObjectDefinition() {
		return this.leftobjectdefinition;
	}

	public DataObjectDefinition<E> getLinkObjectDefinition() {
		return this.linkobjectdefinition;
	}

	/**
	 * gets the related uniqueidentified definition
	 * 
	 * @return the related uniqueidentified property definition
	 */
	public UniqueidentifiedDefinition<E> getUniqueidentifiedDefinitionForLinkObject() {
		return this.uniqueidentifieddefinition;
	}

	/**
	 * if this property is set, only one link is authorized from left object
	 * 
	 * @param replaceifmorethanone true if the link will replace if more than one
	 */
	public void setMaxOneLinkFromLeft(boolean replaceifmorethanone) {
		this.constraintmaxonelinkfromleft = true;
		this.replaceifmorethanonefromleft = replaceifmorethanone;
	}

	/**
	 * if this parameter is set, only one link is allowed between two objects
	 * 
	 * @param replaceifmorethanone true if a new link created will replace the older
	 *                             ones
	 */
	public void setUniqueForLeftAndRight(boolean replaceifmorethanone) {
		this.constraintuniqueforleftandright = true;
		this.replaceifnotuniqueforleftandright = replaceifmorethanone;
	}

	/**
	 * if the link is unique for left and right, only one link is allowed between
	 * two objects
	 * 
	 * @return true if 'UniqueForLeftAndRight' behaviour is set
	 */
	public boolean isUniqueForLeftAndRight() {
		return this.constraintuniqueforleftandright;
	}

	/**
	 * if this property is set, will replace existing link if new link is created
	 * between two objects
	 * 
	 * @return if the 'replaceIfNotUniqueForLeftAndRight' property is set
	 */
	public boolean isReplaceIfNotUniqueForLeftAndRight() {
		return this.replaceifnotuniqueforleftandright;
	}

	/**
	 * if this property is set, only one link is allowed from a given left object
	 * 
	 * @return true if only one link is allowed from left object
	 */
	public boolean isMaxOneLinkFromLeft() {
		return this.constraintmaxonelinkfromleft;
	}

	/**
	 * if this property is set, will replace existing link if a link is already
	 * existing from the left object
	 * 
	 * @return true if replacing existing link from left object
	 */
	public boolean isReplaceifmorethanonefromleft() {
		return this.replaceifmorethanonefromleft;
	}

	/**
	 * creates the definition of the link object property that turns this data
	 * object into a link between a left data object and a right data object
	 * 
	 * @param parentobject                parent object definition
	 * @param leftobjectdefinition        definition of the left object for link
	 * @param rightobjectdefinition       definition of the right object for link
	 * @param showleftfieldswithpriority  if set, left fields above the priority are
	 *                                    shown on the link
	 * @param showrightfieldswithpriority if set, right fields above the priority
	 *                                    are shown on the link
	 */
	public LinkobjectDefinition(DataObjectDefinition<E> parentobject, DataObjectDefinition<F> leftobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition, int showleftfieldswithpriority,
			int showrightfieldswithpriority) {
		this(parentobject, leftobjectdefinition, rightobjectdefinition);
		if (showleftfieldswithpriority < 1000) {
			this.showfieldsforleftobject = true;
			this.minpriorityforleftobjectfields = showleftfieldswithpriority;
			logger.fine("Show fields for left object " + this.minpriorityforleftobjectfields);
		}
		if (showrightfieldswithpriority < 1000) {
			this.showfieldsforrightobject = true;
			this.minpriorityforrightobjectfields = showrightfieldswithpriority;
			logger.fine("Show fields for right object " + this.minpriorityforrightobjectfields);
		}
	}

	/**
	 * creates the definition of the link object property that turns this data
	 * object into a link between a left data object and a right data object
	 * 
	 * @param parentobject          parent object definition
	 * @param leftobjectdefinition  definition of the left object for link
	 * @param rightobjectdefinition definition of the right object for link
	 */
	public LinkobjectDefinition(DataObjectDefinition<E> parentobject, DataObjectDefinition<F> leftobjectdefinition,
			DataObjectDefinition<G> rightobjectdefinition) {
		super(parentobject, "LINKOBJECT");
		this.leftobjectdefinition = leftobjectdefinition;
		this.rightobjectdefinition = rightobjectdefinition;
		this.linkobjectdefinition = parentobject;

		leftid = new StringStoredField("LFID", null, 200);
		rightid = new StringStoredField("RGID", null, 200);
		this.addFieldSchema(leftid);
		this.addFieldSchema(rightid);

		StoredTableIndex lfidindex = new StoredTableIndex("LFID");
		lfidindex.addStoredFieldSchame(leftid);
		this.addIndex(lfidindex);

		StoredTableIndex rgidindex = new StoredTableIndex("RGID");
		rgidindex.addStoredFieldSchame(rightid);
		this.addIndex(rgidindex);

		this.constraintsonlinkobject = new ArrayList<ConstraintOnLinkObject<F, G>>();
		this.displayprofilehideleftobjectfields = parentobject.getDisplayProfileByName("HIDELEFTOBJECTFIELDS");
		this.displayprofilehiderightobjectfields = parentobject.getDisplayProfileByName("HIDERIGHTOBJECTFIELDS");
		this.constraintmaxonelinkfromleft = false;
		this.replaceifmorethanonefromleft = false;
		this.showfieldsforleftobject = false;
		this.showfieldsforrightobject = false;
	}

	/**
	 * sets a constraint on link object (restricts the objects that can be linked
	 * together
	 * 
	 * @param constraintonlinkobject the constraint
	 */
	public void setContraintOnLinkObject(ConstraintOnLinkObject<F, G> constraintonlinkobject) {
		this.constraintsonlinkobject.add(constraintonlinkobject);
	}

	/**
	 * gets the constraint at the given index
	 * 
	 * @param index an index between 0 (included) and
	 *              getConstraintOnLinkObjectNumber (excluded)
	 * @return the constraint at the given index
	 */
	public ConstraintOnLinkObject<F, G> getConstraintOnLinkObject(int index) {
		return this.constraintsonlinkobject.get(index);
	}

	/**
	 * gets the number of constraints for the link object
	 * 
	 * @return the number of constraints for the link object
	 */
	public int getConstraintOnLinkObjectNumber() {
		return this.constraintsonlinkobject.size();
	}

	/**
	 * sets the related unique identified property for the left object
	 * 
	 * @param leftuniqueidentifieddefinition unique identified property for the left
	 *                                       object
	 */
	public void setGenericsLeftobjectforlinkProperty(UniqueidentifiedDefinition<F> leftuniqueidentifieddefinition) {
		this.leftuniqueidentifieddefinition = leftuniqueidentifieddefinition;

	}

	/**
	 * sets the related unique identified property for the right object
	 * 
	 * @param rightuniqueidentifieddefinition unique identified property for the
	 *                                        right object
	 */
	public void setGenericsRightobjectforlinkProperty(UniqueidentifiedDefinition<G> rightuniqueidentifieddefinition) {
		this.rightuniqueidentifieddefinition = rightuniqueidentifieddefinition;

	}

	/**
	 * @return the unique identified property for the left object
	 */
	public UniqueidentifiedDefinition<F> getLeftuniqueidentifieddefinition() {
		return leftuniqueidentifieddefinition;
	}

	/**
	 * @return the unique identified property for the right object
	 */
	public UniqueidentifiedDefinition<G> getRightuniqueidentifieddefinition() {
		return rightuniqueidentifieddefinition;
	}

	/**
	 * sets the dependent property unique identified for the link object
	 * @param uniqueidentifieddefinition unique identified property for the left object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
		this.uniqueidentifieddefinition = uniqueidentifieddefinition;

	}

	/**
	 * gets all the constraints at once
	 * 
	 * @return all the constraints in an array
	 */
	@SuppressWarnings("unchecked")
	public ConstraintOnLinkObject<F, G>[] getAllConstraints() {
		return this.constraintsonlinkobject.toArray(new ConstraintOnLinkObject[0]);
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();

		// - Left join query condition

		// adds an external field is the target object is named
		JoinQueryConditionDefinition<String> leftjoincondition = leftobjectdefinition.generateJoinQueryDefinition(
				this.parentobject.getTableschema(), leftid, "UNIQUEIDENTIFIED", "ID", this.getName() + "_LEFT",
				new QueryOperatorEqual<String>());

		// get named property for left object
		if (leftobjectdefinition.hasProperty("NAMED")) {
			NamedDefinition<F> nameddefinition = (NamedDefinition<F>) leftobjectdefinition.getProperty("NAMED");
			ExternalFieldSchema<?> leftexternalfield = leftobjectdefinition.generateExternalField(
					this.getName() + "LEFTNAME", leftobjectdefinition.getLabel() + " " + nameddefinition.getNameLabel(),
					"", "NAMED", "OBJECTNAME", leftjoincondition, this.displayprofilehideleftobjectfields, 800, 80);
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					nameddefinition.getNameLabel());
			externalfieldlist.add(leftexternalfield);
		}
		// get named property for left object
		if (leftobjectdefinition.hasProperty("NUMBERED")) {
			NumberedDefinition<F> numbereddefinition = (NumberedDefinition<F>) leftobjectdefinition
					.getProperty("NUMBERED");
			boolean orderedasnumber = false;
			int numberoffset = 0;
			if (numbereddefinition.getAutonumberingRule() != null)
				if (numbereddefinition.getAutonumberingRule().orderedAsNumber()) {
					orderedasnumber = true;
					numberoffset = numbereddefinition.getAutonumberingRule().getNumberOffset();
				}
			ExternalFieldSchema<?> leftexternalfield;
			if (orderedasnumber) {
				leftexternalfield = leftobjectdefinition.generateExternalField(this.getName() + "LEFTNR",
						leftobjectdefinition.getLabel() + " " + numbereddefinition.getNumberLabel(),
						"this is a stupid comment", "NUMBERED", "NR", leftjoincondition,
						this.displayprofilehideleftobjectfields, 900, 25, orderedasnumber, numberoffset);
			} else {
				leftexternalfield = leftobjectdefinition.generateExternalField(this.getName() + "LEFTNR",
						leftobjectdefinition.getLabel() + " " + numbereddefinition.getNumberLabel(),
						"this is a stupid comment", "NUMBERED", "NR", leftjoincondition,
						this.displayprofilehideleftobjectfields, 900, 60);
			}
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
					numbereddefinition.getNumberLabel());
			externalfieldlist.add(leftexternalfield);
		}
		// get state property for left object

		if (leftobjectdefinition.hasProperty("LIFECYCLE")) {
			LifecycleDefinition<?, ?> lifecycle = (LifecycleDefinition<?, ?>) (leftobjectdefinition
					.getProperty("LIFECYCLE"));
			ExternalFieldSchema<?> leftexternalfield = leftobjectdefinition.generateExternalField(
					this.getName() + "LEFTSTATE", "Left state", "this is a stupid comment", "LIFECYCLE", "STATE",
					lifecycle.getLifecycleHelper(), leftjoincondition, this.displayprofilehideleftobjectfields, 500,
					35);
			leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields, "State");
			externalfieldlist.add(leftexternalfield);
		}

		if (this.showfieldsforleftobject) {
			logger.finer("--------------- Show fields for left object ---------------------- fields presents = "
					+ leftobjectdefinition.getFieldNumber() + "   ");
			for (int i = 0; i < leftobjectdefinition.getFieldNumber(); i++) {
				DataObjectFieldDefinition<F> thisfield = leftobjectdefinition.getFieldAt(i);
				if (thisfield.getPriority() > this.minpriorityforleftobjectfields) {
					logger.finer(" ++ Field in for left " + thisfield.getName());
					ExternalFieldSchema<?> leftexternalfield = leftobjectdefinition.generateExternalFieldFromField(
							thisfield.getName() + "LEFTFIELD", thisfield.getDisplayname(), thisfield.getTooltip(),
							thisfield.getName(), leftjoincondition, this.displayprofilehideleftobjectfields,
							thisfield.getPriority(), thisfield.getDefaultcolumnintable());
					leftexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehiderightobjectfields,
							thisfield.getDisplayname());
					externalfieldlist.add(leftexternalfield);
				} else {
					logger.finer(" -- Field out for left " + thisfield.getName());
				}
			}
		}

		JoinQueryConditionDefinition<String> rightjoincondition = rightobjectdefinition.generateJoinQueryDefinition(
				this.parentobject.getTableschema(), rightid, "UNIQUEIDENTIFIED", "ID", this.getName() + "_RIGHT",
				new QueryOperatorEqual<String>());

		// get named property for right object

		if (rightobjectdefinition.hasProperty("NAMED")) {
			// adds an external field is the target object is named -- currently, there is
			// an error
			NamedDefinition<G> namedproperty = (NamedDefinition<G>) rightobjectdefinition.getProperty("NAMED");
			ExternalFieldSchema<?> rightexternalfield = rightobjectdefinition.generateExternalField(
					this.getName() + "RIGHTNAME", rightobjectdefinition.getLabel() + " " + namedproperty.getNameLabel(),
					"this is a stupid comment", "NAMED", "OBJECTNAME", rightjoincondition,
					this.displayprofilehiderightobjectfields, 800, 80);
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					namedproperty.getNameLabel());
			externalfieldlist.add(rightexternalfield);
		}

		if (rightobjectdefinition.hasProperty("NUMBERED")) {
			// adds an external field is the target object is named -- currently, there is
			// an error
			@SuppressWarnings("unchecked")
			NumberedDefinition<F> numbereddefinition = (NumberedDefinition<F>) rightobjectdefinition
					.getProperty("NUMBERED");
			boolean orderedasnumber = false;
			int numberoffset = 0;
			if (numbereddefinition.getAutonumberingRule() != null)
				if (numbereddefinition.getAutonumberingRule().orderedAsNumber()) {
					orderedasnumber = true;
					numberoffset = numbereddefinition.getAutonumberingRule().getNumberOffset();
				}
			ExternalFieldSchema<?> rightexternalfield;
			if (orderedasnumber) {
				rightexternalfield = rightobjectdefinition.generateExternalField(this.getName() + "RIGHTNR",
						rightobjectdefinition.getLabel() + " " + numbereddefinition.getNumberLabel(),
						"this is a stupid comment", "NUMBERED", "NR", rightjoincondition,
						this.displayprofilehiderightobjectfields, 900, 25, orderedasnumber, numberoffset);

			} else {
				rightexternalfield = rightobjectdefinition.generateExternalField(this.getName() + "RIGHTNR",
						rightobjectdefinition.getLabel() + " " + numbereddefinition.getNumberLabel(),
						"this is a stupid comment", "NUMBERED", "NR", rightjoincondition,
						this.displayprofilehiderightobjectfields, 900, 60);

			}
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
					numbereddefinition.getNumberLabel());
			externalfieldlist.add(rightexternalfield);
		}
		if (rightobjectdefinition.hasProperty("LIFECYCLE")) {
			LifecycleDefinition<?, ?> lifecycle = (LifecycleDefinition<?, ?>) (rightobjectdefinition
					.getProperty("LIFECYCLE"));
			ExternalFieldSchema<?> rightexternalfield = rightobjectdefinition.generateExternalField(
					this.getName() + "RIGHTSTATE", "Right object state", "this is a stupid comment", "LIFECYCLE",
					"STATE", lifecycle.getLifecycleHelper(), rightjoincondition,
					this.displayprofilehiderightobjectfields, 500, 35);
			rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields, "State");
			externalfieldlist.add(rightexternalfield);
		}
		if (this.showfieldsforrightobject) {
			logger.finer("--------------- Show fields for right object ---------------------- fields presents = "
					+ leftobjectdefinition.getFieldNumber() + "  ");
			for (int i = 0; i < rightobjectdefinition.getFieldNumber(); i++) {
				DataObjectFieldDefinition<G> thisfield = rightobjectdefinition.getFieldAt(i);
				if (thisfield.getPriority() > this.minpriorityforrightobjectfields) {
					ExternalFieldSchema<?> rightexternalfield = rightobjectdefinition.generateExternalFieldFromField(
							thisfield.getName() + "RIGHTFIELD", thisfield.getDisplayname(), thisfield.getTooltip(),
							thisfield.getName(), rightjoincondition, this.displayprofilehiderightobjectfields,
							thisfield.getPriority(), thisfield.getDefaultcolumnintable());
					rightexternalfield.addSpecialdisplaynamewhenprofileactive(this.displayprofilehideleftobjectfields,
							thisfield.getDisplayname());
					externalfieldlist.add(rightexternalfield);
					logger.fine(" ++ Field in for right " + thisfield.getName());
				} else {
					logger.fine(" -- Field out for left " + thisfield.getName());
				}
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
				16, this.parentobject);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Right id",
				"the technical id generated by the system for the right object of the part", rightid, false, true, -200,
				16, this.parentobject);

		return returnvalue;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Linkobject<E, F, G>(this, parentpayload, leftobjectdefinition, rightobjectdefinition);
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

}
