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
 * A constraint on an auto-link object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public abstract class ConstraintOnAutolinkObject<E extends DataObject<E>> {
	/**
	 * checks if creation of the link is possible. this may require performing a
	 * query to get both objects from their id
	 * 
	 * @param leftobject  id of the left object
	 * @param rightobject id of the right object
	 * @return true if link is valid
	 */
	public abstract boolean checklinkvalid(DataObjectId<E> leftobject, DataObjectId<E> rightobject);

	/**
	 * performs a filter on the potential right objects given the left object id
	 * 
	 * @param maintablealias alias to generate the query filter
	 * @param leftobjectid   left object id
	 * @return the query condition
	 */
	public abstract QueryCondition generateQueryFilter(TableAlias maintablealias, DataObjectId<E> leftobjectid);

	/**
	 * check if the link is valid given both objects. Are the objects are given, the
	 * link check may be done without any call on the database
	 * 
	 * @param leftobject  left object
	 * @param rightobject right object
	 * @return true if link is valid
	 */
	public abstract boolean checklinkvalid(E leftobject, E rightobject);

	/**
	 * get the invalid link error message
	 * 
	 * @param leftobject  left object
	 * @param rightobject right object
	 * @return the invalid link error message if link is invalid, or null if link is
	 *         valid
	 */
	public abstract String getInvalidLinkErrorMessage(E leftobject, E rightobject);

}
