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
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of a property that will make the number of an object being checked
 * for unicity only amongst the children of the same parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object
 * @param <F> parent data object of the current data object
 */
public class NumberedforparentDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E> & NumberedforparentInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F>>
		extends DataObjectPropertyDefinition<E> {
	private static Logger logger = Logger.getLogger(CheckExistingNumber.class.getName());

	private DataObjectDefinition<F> parentobjectforlinkdefinition;
	private NumberedDefinition<E> relatednumbereddefinition;
	private LinkedtoparentDefinition<E, F> relatedlinkedtoparentdefinition;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<F> parentuniqueidentifieddefinition;
	private NumberedforparentDefinition<E, F>.CheckExistingNumberForParent checkexistingnumberforparent;

	/**
	 * creates a new definition for the property numbered for parent
	 * 
	 * @param parentobject                  data object that has the numbered for
	 *                                      parent property
	 * @param parentobjectforlinkdefinition the definition of the parent object to
	 *                                      the current object
	 */
	public NumberedforparentDefinition(DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> parentobjectforlinkdefinition) {
		super(parentobject, "NUMBEREDFORPARENT");
		this.parentobjectforlinkdefinition = parentobjectforlinkdefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return new String[0];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Numberedforparent<E, F>(this, parentpayload);
	}

	/**
	 * sets the definition of the related property numbered
	 * 
	 * @param relatednumbereddefinition definition of the related property numbered
	 */
	public void setDependentDefinitionNumbered(NumberedDefinition<E> relatednumbereddefinition) {
		this.relatednumbereddefinition = relatednumbereddefinition;
	}

	/**
	 * sets the dependent linked to parent property. this also overrides the check
	 * existing number
	 * 
	 * @param relatedlinkedtoparentdefinition dependent linked to parent property
	 */
	public void setDependentDefinitionLinkedtoparent(LinkedtoparentDefinition<E, F> relatedlinkedtoparentdefinition) {
		this.relatedlinkedtoparentdefinition = relatedlinkedtoparentdefinition;
		if (this.relatednumbereddefinition == null)
			throw new RuntimeException("related numbered not defined yet");
		this.checkexistingnumberforparent = new CheckExistingNumberForParent(relatednumbereddefinition, this);
		this.relatednumbereddefinition.overridesCheckExistingNumber(checkexistingnumberforparent);

	}

	/**
	 * gets the related property linked to parent
	 * 
	 * @return related property linked to parent
	 */
	public LinkedtoparentDefinition<E, F> getRelatedLinkedToParentDefinition() {
		return relatedlinkedtoparentdefinition;
	}

	/**
	 * The specific utility class that will change the check unicity procedure for
	 * the number of the object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> current data object
	 * @param <F> parent data object of the current data object
	 */
	private class CheckExistingNumberForParent extends CheckExistingNumber<E> {
		@SuppressWarnings("unused")
		private NumberedDefinition<E> relatednumbereddefinition;
		private NumberedforparentDefinition<E, F> numberedforparentdefinition;

		/**
		 * creates the new utility class to check number unicity
		 * 
		 * @param relatednumbereddefinition   related definition of the property
		 *                                    numbered
		 * @param numberedforparentdefinition definition of the numbered for parent
		 *                                    property
		 */
		public CheckExistingNumberForParent(NumberedDefinition<E> relatednumbereddefinition,
				NumberedforparentDefinition<E, F> numberedforparentdefinition) {
			super(relatednumbereddefinition);
			this.numberedforparentdefinition = numberedforparentdefinition;
		}

		@Override
		public boolean exists(String nr, E object) {
			E[] otherobjects = NumberedforparentQueryHelper.get().getobjectbynumberforparent(nr,
					object.getparentidfornumber(), // extract parent id from object
					numberedforparentdefinition.parentobject, numberedforparentdefinition.parentobjectforlinkdefinition,
					numberedforparentdefinition);
			for (int i = 0; i < otherobjects.length; i++) {
				E thisobject = otherobjects[i];
				if (thisobject instanceof VersionedInterface) {
					@SuppressWarnings("rawtypes")
					VersionedInterface thisobjectversioned = (VersionedInterface) thisobject;
					if (thisobjectversioned.getLastversion().equals("Y")) {
						logger.warning(" --> Simple number check: versioned object " + nr + " - id "
								+ thisobject.getId() + " - version " + thisobjectversioned.getVersion()
								+ " - parent id " + object.getparentidfornumber());
						return true;
					}
				} else {
					logger.warning(" --> Simple number check: versioned object " + nr + " - id " + thisobject.getId()
							+ " - parent id" + object.getparentidfornumber());

					return true;
				}
			}
			return false;
		}

	}

	/**
	 * sets the definition of the property unique identified for the related parent
	 * object
	 * 
	 * @param parentuniqueidentifieddefinition unique identified definition on the
	 *                                         parent object
	 */
	public void setGenericsParentobjectforlinkProperty(UniqueidentifiedDefinition<F> parentuniqueidentifieddefinition) {
		this.parentuniqueidentifieddefinition = parentuniqueidentifieddefinition;
	}

	/**
	 * get the parent object to the current object
	 * 
	 * @return the definition of the parent object
	 */
	public DataObjectDefinition<F> getParentobjectforlinkdefinition() {
		return parentobjectforlinkdefinition;
	}

	/**
	 * get the numbered property definition on this object
	 * 
	 * @return numbered property definition on this object
	 */
	public NumberedDefinition<E> getRelatednumbereddefinition() {
		return relatednumbereddefinition;
	}

}
