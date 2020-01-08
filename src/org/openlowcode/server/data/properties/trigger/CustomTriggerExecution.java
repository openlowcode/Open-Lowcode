/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.trigger;

import java.util.Date;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.module.system.data.Triggervalue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.formula.DataUpdateTrigger;
import org.openlowcode.server.data.formula.TriggerToExecute;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.GenericlinkQueryHelper;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Utility class to execute a custom trigger. Should be implemented by actual
 * trigger classes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the trigger is on
 */
public abstract class CustomTriggerExecution<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends DataUpdateTrigger<E> {
	private final static Logger logger = Logger.getLogger(CustomTriggerExecution.class.getName());
	public static final int MAX_TRIGGER_STRING_LENGTH = 4000;

	/**
	 * creates a custom trigger execution
	 * 
	 * @param name name of the trigger
	 */
	public CustomTriggerExecution(String name) {
		super(name);

	}

	@Override
	public final boolean isLocal() {
		return false;
	}

	@Override
	public final NamedList<TriggerToExecute<E>> compute(E contextobject, boolean forcelocalupdate) {
		DataObjectId<E> objectid = contextobject.getId();
		E newobject = objectid.lookupObject();
		if (newobject != null) {
			String newtriggerstring = generateTriggerString(newobject);

			boolean unchanged = false;
			if (newtriggerstring != null) {
				logger.fine(" trigger " + this.getName() + " has a trigger string");

				if (newtriggerstring.length() >= MAX_TRIGGER_STRING_LENGTH) {
					long time = (new Date()).getTime();
					String timehexString = Long.toHexString(time);
					int hexStringlength = timehexString.length();
					newtriggerstring = newtriggerstring.substring(0, MAX_TRIGGER_STRING_LENGTH - hexStringlength - 1)
							+ timehexString;
				}

				DataObjectId<?> genericid = DataObjectId.generateDataObjectId(contextobject.getId().getId(),
						contextobject.getId().getObjectId());
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Triggervalue[] values = Triggervalue.getallforgenericidforowner(genericid,
						QueryFilter.get(new SimpleQueryCondition(
								new TableAlias(contextobject.getDefinitionFromObject().getTableschema(),
										GenericlinkQueryHelper.maintablealiasforgetallforgenericid),
								Triggervalue.getDefinition().getTriggernameFieldSchema(), new QueryOperatorEqual(),
								this.getName())));
				// no value stored, it has to be new
				if (values == null) {
					Triggervalue newvalue = new Triggervalue();
					newvalue.setObjectsummary(newtriggerstring);
					newvalue.setlinkedobjectidforowner(genericid);
					newvalue.setTriggername(this.getName());
					newvalue.insert();
				} else {
					if (values.length == 0) {
						Triggervalue newvalue = new Triggervalue();
						newvalue.setObjectsummary(newtriggerstring);
						newvalue.setlinkedobjectidforowner(genericid);
						newvalue.setTriggername(this.getName());
						newvalue.insert();
					}
					if (values.length == 1) {
						Triggervalue foundtriggervalue = values[0];
						String oldtriggerstring = foundtriggervalue.getObjectsummary();
						if (oldtriggerstring.compareTo(newtriggerstring) == 0) {
							unchanged = true;
						} else {
							foundtriggervalue.setObjectsummary(newtriggerstring);
							foundtriggervalue.update();

						}
					}
					if (values.length > 1) {
						logger.severe(" ----------- Error in processing trigger " + this.getName() + " for object "
								+ objectid.toString() + ", triggers found = " + values.length + ", should be one");
						for (int i = 0; i < values.length; i++) {
							logger.severe("    * Object drop " + i + " name = " + values[i].getTriggername()
									+ " value = " + values[i].getObjectsummary() + " update date "
									+ values[i].getUpdatetime());

						}
						logger.severe(
								"---------------starting cleaning----------------------------------------------------");
						for (int i = 0; i < values.length; i++) {
							values[i].delete();
						}
						Triggervalue newvalue = new Triggervalue();
						newvalue.setObjectsummary(newtriggerstring);
						newvalue.setlinkedobjectidforowner(genericid);
						newvalue.setTriggername(this.getName());
						newvalue.insert();
					}
				}
			}
			// ----------------------------------------------------------------------------
			// End of trigger string treatment
			// ----------------------------------------------------------------------------
			if (!unchanged) {
				this.execute(newobject);
			}
		} else {
			logger.fine("Called trigger for null object " + contextobject);
		}
		return new NamedList<TriggerToExecute<E>>();
	}

	/**
	 * As a reminder, it is recommended to limit query to data already queried in
	 * the generate query string
	 * 
	 * @param object object to process the trigger on
	 */
	public abstract void execute(E object);

	@Override
	public final boolean isCustomTrigger() {
		return true;
	}

	/**
	 * Note: it is best practice to store in memory of the trigger all data queried
	 * to generate the string, and use it again in the execution.
	 * 
	 * @param E object
	 * @return null if the trigger string feature is not used (in that case, trigger
	 *         will always be triggered), or the string else If string is the same
	 *         as the one stored, the trigger is not triggered again.
	 * @throws GalliumException
	 */
	public String generateTriggerString(E object) {
		return null;
	}
}
