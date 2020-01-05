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

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * flat file loader to load the state of objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 * @param <F> transition choice definition used by the lifecycle
 */
public class LifecycleFlatFileLoader<E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends FlatFileLoaderColumn<E> {
	private DataObjectDefinition<E> objectdefinition;
	private LifecycleDefinition<E, F> lifecycleproperty;
	private PropertyExtractor<E> propertyextractor;
	private F lifecyclehelper;

	/**
	 * creates a flat file loader
	 * 
	 * @param objectdefinition  definition of the parent object
	 * @param lifecycleproperty defintion of the lifecycle property
	 * @param propertyextractor extractor for the lifecycle property
	 * @param lifecyclehelper   lifecycle choice definition
	 */
	public LifecycleFlatFileLoader(DataObjectDefinition<E> objectdefinition,
			LifecycleDefinition<E, F> lifecycleproperty, PropertyExtractor<E> propertyextractor, F lifecyclehelper) {
		this.objectdefinition = objectdefinition;
		this.lifecycleproperty = lifecycleproperty;
		this.propertyextractor = propertyextractor;
		this.lifecyclehelper = lifecyclehelper;
	}

	@Override
	public boolean secondpass() {
		return true;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		String stringvalue = FlatFileLoader.parseObject(value, "Lifecycle for " + objectdefinition.getName());
		if (stringvalue != null)
			if (stringvalue.length() > 0) {
				// do something only if content. Else, will let default behaviour happen
				ChoiceValue<F>[] allchoices = lifecycleproperty.getLifecycleHelper().getChoiceValue();
				ChoiceValue<F> selected = null;
				for (int i = 0; i < allchoices.length; i++) {
					ChoiceValue<F> thischoice = allchoices[i];
					if (thischoice.getStorageCode().equals(stringvalue.trim())) {
						selected = thischoice;
						break;
					}

					if (thischoice.getDisplayValue().equals(stringvalue))
						selected = thischoice;
				}
				if (selected == null)
					throw new RuntimeException(" did not find a valid value for lifecycle for object "
							+ objectdefinition.getName() + " invalid value = " + value);
				@SuppressWarnings("unchecked")
				Lifecycle<E, F> lifecycle = (Lifecycle<E, F>) propertyextractor.extract(object);
				String oldlifecyclestate = lifecycle.getState();
				if (FlatFileLoader.isTheSame(oldlifecyclestate, value)) {
					return false;
				} else {
					if (lifecyclehelper.isChoiceFinal(selected)) {
						Date date = new Date();
						if (object instanceof UpdatelogInterface) {
							@SuppressWarnings("unchecked")
							UpdatelogInterface<E> updatelog = (UpdatelogInterface<E>) object;
							if (updatelog.getUpdatetime() != null)
								date = updatelog.getUpdatetime();
						}

						lifecycle.forceFinalStateTime(date);
					}
					lifecycle.setStateWithoutPersistence(selected.getStorageCode());
					return true;
				}
			}
		return false;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		@SuppressWarnings("unchecked")
		Lifecycle<E, F> lifecycle = (Lifecycle<E, F>) propertyextractor.extract(currentobject);
		if (lifecycle.getstateforchange(currentobject) != null)
			cell.setCellValue(lifecycle.getstateforchange(currentobject).getDisplayValue());
		return false;
	}

}
