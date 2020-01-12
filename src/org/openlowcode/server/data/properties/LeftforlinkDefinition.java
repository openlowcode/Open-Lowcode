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
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkObject;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.tools.trace.ExceptionLogger;

/**
 * the property that is put on the left object for a link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current object (left for link)
 * @param <F> data object holding the link
 * @param <G> right data object for link
 */
public class LeftforlinkDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, E, G>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends DataObjectPropertyDefinition<E> {
	private static Logger logger = Logger.getLogger(LeftforlinkDefinition.class.getName());
	private DataObjectDefinition<F> linkobjectdefinition;
	private LinkobjectDefinition<F, E, G> linkobjectpropertydefinition;
	private DataObjectDefinition<E> parentobject;
	private DataObjectDefinition<G> rightobjectforlinkdefinition;
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;

	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<G> uniqueidentifiedforrightobject;

	@Override
	public DataObjectDefinition<E> getParentObject() {
		return this.parentobject;
	}

	/**
	 * creates the definition of the data object link
	 * 
	 * @param parentobject                 parent data object definition
	 * @param name                         name of the link
	 * @param linkobjectdefinition         definition of the data object for the
	 *                                     link
	 * @param rightobjectforlinkdefinition definition of the right data object for
	 *                                     the link
	 */
	public LeftforlinkDefinition(DataObjectDefinition<E> parentobject, String name,
			DataObjectDefinition<F> linkobjectdefinition, DataObjectDefinition<G> rightobjectforlinkdefinition) {
		super(parentobject, name);
		this.linkobjectdefinition = linkobjectdefinition;
		this.parentobject = parentobject;
		this.rightobjectforlinkdefinition = rightobjectforlinkdefinition;
	}

	/**
	 * get the link object property definition for the linked object
	 * 
	 * @return the link object property
	 */
	public LinkobjectDefinition<F, E, G> getLinkObjectPropertyDefinition() {
		return this.linkobjectpropertydefinition;
	}

	/**
	 * sets the link object property on the data object holding the link
	 * 
	 * @param linkobjectpropertydefinition
	 */
	public void setGenericsLinkobjectProperty(LinkobjectDefinition<F, E, G> linkobjectpropertydefinition) {
		this.linkobjectpropertydefinition = linkobjectpropertydefinition;

	}

	/**
	 * gets the unique identified property definition on the data object on the left
	 * for the link
	 * 
	 * @return the unique identified property
	 */
	public UniqueidentifiedDefinition<E> getUniqueIdentifiedDefinition() {
		return this.uniqueidentifieddefinition;
	}

	/**
	 * sets the dependent property UniqueIdentified
	 * 
	 * @param uniqueidentifieddefinition dependent property unique identified
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
		this.uniqueidentifieddefinition = uniqueidentifieddefinition;

	}

	/**
	 * sets the unique identified property for the link
	 * 
	 * @param uniqueidentifiedforrightobject unique identified property for the
	 *                                       right object for the link
	 */
	public void setGenericsRightobjectforlinkProperty(UniqueidentifiedDefinition<G> uniqueidentifiedforrightobject) {
		this.uniqueidentifiedforrightobject = uniqueidentifiedforrightobject;

	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Leftforlink<E, F, G>(this, parentpayload);
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		boolean createmissing = false;
		boolean deletelink = false;
		String hardcodedvalue = null;
		if (columnattributes != null)
			if (columnattributes.length > 0)
				for (int i = 0; i < columnattributes.length; i++) {
					if ("CREATE".equals(columnattributes[i]))
						createmissing = true;
					if ("DELETELINK".equals(columnattributes[i]))
						deletelink = true;
					if (columnattributes[i].startsWith("HARDCODEDVALUE=")) {
						String value = columnattributes[i].substring("HARDCODEDVALUE=".length());
						if (value.trim().length() > 0)
							hardcodedvalue = value.trim();
					}
				}

		DataObjectPropertyDefinition<G> rightobjectnumbered = rightobjectforlinkdefinition.getProperty("NUMBERED");
		ConstraintOnLinkObject<E, G>[] constraints = linkobjectpropertydefinition.getAllConstraints();
		if (rightobjectnumbered == null)
			throw new RuntimeException(
					"Numbered is not existing for right object " + rightobjectforlinkdefinition.getName());
		if (!(rightobjectnumbered instanceof NumberedDefinition))
			throw new RuntimeException("Numbered does not have the right class for right object "
					+ rightobjectforlinkdefinition.getName());
		return new LeftforlinkFlatFileLoader(objectdefinition, linkobjectdefinition, rightobjectforlinkdefinition,
				linkobjectpropertydefinition, (NumberedDefinition) rightobjectnumbered, createmissing, deletelink,
				hardcodedvalue, constraints);

	}

	@Override
	public String[] getLoaderFieldList() {
		try {

			if (rightobjectforlinkdefinition.getProperty("NUMBERED") != null) {
				return new String[] { "" };
			}
		} catch (Exception e) {
			ExceptionLogger.setInLogs(e, logger);
		}
		return new String[] {};
	}

	@Override
	public String[] getLoaderFieldSample(String name) {

		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "ID1234";
		returntable[3] = "the number of the right object. It is possible to specify CREATE, which will create the right object "
				+ rightobjectforlinkdefinition.getName()
				+ " with the given number. \"DELETELINK\" option will delete links not mentioned in this line. It is possible to enter several values separated by '|'";
		return returntable;
	}
}
