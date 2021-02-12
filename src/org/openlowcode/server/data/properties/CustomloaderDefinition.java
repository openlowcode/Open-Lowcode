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
import java.util.function.Supplier;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * definition of the custom loader property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class CustomloaderDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>> extends DataObjectPropertyDefinition<E> {

	/**
	 * the interface that a custom loader helper should comply to
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> parent data object
	 */
	public interface CustomloaderHelper<E extends DataObject<E>> {

		/**
		 * sets the context loader. It can help a helper get an access to a dependent loader / helper
		 * 
		 * @param flatfileloader
		 */
		public void setContextLoader(FlatFileLoader<?> flatfileloader);
		
		/**
		 * gets the loader column for the corresponding column attributes
		 * 
		 * @param objectdefinition parent data object definition
		 * @param columnattributes attributes of the loader
		 * @param locale           locale
		 * @return the flat file loader column
		 */
		public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
				String[] columnattributes, ChoiceValue<ApplocaleChoiceDefinition> locale);

		/**
		 * gets the list of fields in the custom loader
		 * 
		 * @return the list of loader fields
		 */
		public String[] getLoaderFieldList();

		/**
		 * gets the same information of the field
		 * 
		 * @param name name of the field in the custom loader
		 * @return the samples for export
		 */
		public String[] getLoaderFieldSample(String name);

		/**
		 * This method will be called at the end of each line of processing, after the
		 * main operation object insertion or update.
		 * 
		 * @param objecforprocessing
		 */
		public void executeAtEndOfLine(E objecforprocessing);

	}

	private Supplier<CustomloaderHelper<E>> customloaderhelper;

	/**
	 * creates the definition of a custom loader
	 * 
	 * @param parentobject       parent data object
	 * @param name               name of the custom loader
	 * @param customloaderhelper helper defining the behaviour of the custom loader
	 */
	public CustomloaderDefinition(DataObjectDefinition<E> parentobject, String name,
			Supplier<CustomloaderHelper<E>> customloaderhelper) {
		super(parentobject, name);
		this.customloaderhelper = customloaderhelper;
	}

	@Override
	public CustomloaderHelper<E> getTransientLoaderHelper() {
		return customloaderhelper.get();
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
	public org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Access should be through the transient loader helper");

	}

	@Override
	public String[] getLoaderFieldList() {
		return this.customloaderhelper.get().getLoaderFieldList();
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return this.customloaderhelper.get().getLoaderFieldSample(name);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Customloader<E>(this, parentpayload);
	}

}
