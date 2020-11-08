/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.server.data.properties.constraints;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 *
 * A constraint on link object restrains the right objects that can be linked to
 * a left object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> the left object
 * @param <F> the right object
 */
public abstract class ConstraintOnLinkObject<E extends DataObject<E>, F extends DataObject<F>> {

	/**
	 * checks if it is authorized to set a link between two objects with their id
	 * 
	 * @param leftobject  id of the left object
	 * @param rightobject if of the right object
	 * @return true if link creation is authorized, false if link creation is not
	 *         authorized
	 */
	public abstract boolean checklinkvalid(DataObjectId<E> leftobject, DataObjectId<F> rightobject);

	/**
	 * checks if it is authorized to set a link between two objects
	 * 
	 * @param leftobject  left object
	 * @param rightobject right object
	 * @return true if link creation is authorized, false if link creation is not
	 *         authorized
	 */
	public abstract boolean checklinkvalid(E leftobject, F rightobject);

	/**
	 * gets a detailed error message if link is invalid
	 * 
	 * @param leftobject  left object
	 * @param rightobject right object
	 * @return a readable error message
	 */
	public abstract String getInvalidLinkErrorMessage(E leftobject, F rightobject);

	/**
	 * generates a filter to show only valid objects when querying for potential
	 * right objects
	 * 
	 * @param maintablealias alias to generate the condition for
	 * @param leftobjectid   id of the left object
	 * @return a query condition that will return only valid right objects for link
	 *         creation
	 */
	public abstract QueryCondition generateQueryFilter(TableAlias maintablealias, DataObjectId<E> leftobjectid);

	/**
	 * generates a filter to show only valid objects when querying for potential
	 * right objects
	 * 
	 * @param maintablealias alias to generate the condition for
	 * @param leftobject     draft left object (not yet persisted)
	 * @return a query condition that will return only valid right objects for link
	 *         creation
	 */
	public abstract QueryCondition generateQueryFilter(TableAlias maintablealias, E leftobject);

	/**
	 * generates a filter to show only valid objects when querying for potential
	 * left objects
	 * 
	 * @param maintablealias alias to generate the condition for
	 * @param rightobjectid  id of the right object
	 * @return a query condition that will return only valid left objects for link
	 *         creation
	 */
	public abstract QueryCondition generateReverseQueryFilter(TableAlias maintablealias, DataObjectId<F> rightobjectid);

	/**
	 * checks if the constraint allows to load links
	 * 
	 * @return true if it is authorized to use left for link loader with this
	 *         constraint
	 */
	public abstract boolean isLeftForLinkLoaderManaged();

	/**
	 * this method will enrich the right object after it has been created in case
	 * right objects are created when loading links
	 * 
	 * @param rightobjectbeforecreation to be implemented if
	 *                                  isLeftForLinkLoaderManaged returns true.
	 * @param leftobject                left object to enrich data
	 */
	public abstract void enrichRightObjectAfterCreation(F rightobjectbeforecreation, E leftobject);
}
