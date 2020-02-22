/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.security;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;

/**
 * A buffer to store during a transaction the objects manipulated. This avoids
 * getting several times the objects in the persistence layer for security calculation
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SecurityBuffer {

	private HashMap<DataObjectId<?>, DataObject<?>> buffer;
	private static Logger logger = Logger.getLogger(SecurityBuffer.class.getName());
	private int queries;
	private int buffers;

	/**
	 * creates a void SecurityBuffer
	 */
	public SecurityBuffer() {
		buffer = new HashMap<DataObjectId<?>, DataObject<?>>();
		buffers = 0;
		queries = 0;
	}

	/**
	 * @param objectid an object id
	 * @return the object, taking it into the cache if possible, else in the database
	 */
	@SuppressWarnings("unchecked")
	public <E extends DataObject<E>> E getObject(DataObjectId<E> objectid) {

		DataObject<?> unparsedcachedobject = buffer.get(objectid);
		if (unparsedcachedobject != null) {
			queries++;
			logger.fine("SecurityBuffer successful cache (" + buffers + "/" + queries + ")");
			return (E) unparsedcachedobject;
		}
		E object = (E) (objectid.lookupObject());
		buffers++;
		logger.fine("SecurityBuffer read data in database (" + buffers + "/" + queries + ")");
		buffer.put((DataObjectId<?>) objectid, object);
		return object;
	}

}
