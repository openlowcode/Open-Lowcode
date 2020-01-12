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
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.tools.trace.ExceptionLogger;

/**
 * 
 * Definition of the property of an object having an auto-link.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> object that is linked through the autolink
 * @param <F> autolink object
 */
public class HasautolinkDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & AutolinkobjectInterface<F, E>>
		extends DataObjectPropertyDefinition<E> {
	private DataObjectDefinition<F> autolinkobjectdefinition;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;
	private AutolinkobjectDefinition<F, E> autolinkobjectpropertydefinition;
	private DataObjectDefinition<E> parentobject;
	private static final Logger logger = Logger.getLogger(HasautolinkDefinition.class.getName());

	/**
	 * get autolink object definition on the related link object
	 * 
	 * @return the auto-link definition of the related link object
	 */
	protected AutolinkobjectDefinition<F, E> getAutolinkobjectPropertyDefinition() {
		return this.autolinkobjectpropertydefinition;
	}

	/**
	 * gets the parent object definition
	 * 
	 * @return the parent object definition
	 */
	protected DataObjectDefinition<E> getParent() {
		return this.parentobject;
	}

	/**
	 * creates an hasautolink definition for the object
	 * 
	 * @param parentobject             definition of the object having the auto-link
	 * @param name                     name of the link
	 * @param autolinkobjectdefinition related definition of the object holding the
	 *                                 auto-link
	 */
	public HasautolinkDefinition(DataObjectDefinition<E> parentobject, String name,
			DataObjectDefinition<F> autolinkobjectdefinition) {
		super(parentobject, name);
		this.autolinkobjectdefinition = autolinkobjectdefinition;
		this.parentobject = parentobject;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public FieldSchemaForDisplay<?>[] setFieldSchemaToDisplay() {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Hasautolink<E, F>(this, parentpayload);
	}

	/**
	 * sets the related object autolink object definition property
	 * 
	 * @param autolinkobjectpropertydefinition autolink object property definition
	 */
	public void setGenericsAutolinkobjectProperty(AutolinkobjectDefinition<F, E> autolinkobjectpropertydefinition) {
		if (autolinkobjectpropertydefinition == null)
			throw new RuntimeException("autolinkobjectpropertydefinition is null");
		this.autolinkobjectpropertydefinition = autolinkobjectpropertydefinition;
	}

	/**
	 * sets dependent property definition unique identified
	 * 
	 * @param uniqueidentifieddefinition unique identified definition of the object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
		this.uniqueidentifieddefinition = uniqueidentifieddefinition;
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
		boolean deleteothers = false;
		if (columnattributes != null)
			if (columnattributes.length > 0)
				if ("DELETEOTHERS".equals(columnattributes[0]))
					deleteothers = true;
		DataObjectPropertyDefinition objectnumbered = parentobject.getProperty("NUMBERED");
		ConstraintOnAutolinkObject<E>[] constraint = autolinkobjectpropertydefinition
				.getAllConstraintsOnAutolinkObject();
		if (objectnumbered == null)
			throw new RuntimeException("Numbered is not existing for this object " + parentobject.getName());
		if (!(objectnumbered instanceof NumberedDefinition))
			throw new RuntimeException(
					"Numbered does not have the right class for right object " + parentobject.getName());
		return new HasautolinkFlatFileLoader(objectdefinition, autolinkobjectdefinition,
				autolinkobjectpropertydefinition, (NumberedDefinition) objectnumbered, deleteothers, constraint);

	}

	@Override
	public String[] getLoaderFieldList() {
		try {
			if (parentobject.getProperty("NUMBERED") != null) {
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
		returntable[3] = "the number of the right object. It is possible to specify DELETEOTHERS, which will delete links not mentioned for the left object. It is possible to enter several values separated by '|'";
		return returntable;
	}
}
