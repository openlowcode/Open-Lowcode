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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * The interface all link objects to master comply to
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object that is used as link to master
 * @param <F> left object for the link to master
 * @param <G> right object for the link to master
 */
public interface LinkobjecttomasterInterface<E extends DataObject<E>, F extends DataObject<F>, G extends DataObject<G>>
extends UniqueidentifiedInterface<E> {

/**
* sets the left object for this link
* 
* @param leftobjectid id of the left object
*/
public void setleftobject(DataObjectId<F> leftobjectid);

/**
* sets the right object for this link
* 
* @param rightobjectmasterid
*/
public void setrightobjectmaster(DataObjectMasterId<G> rightobjectmasterid);

/**
* @return get the left object id
*/
public DataObjectId<F> getLfid();

/**
* @return get the right master object id
*/
public DataObjectMasterId<G> getRgmsid();

public interface MassiveGetlinksandrightobject<E extends DataObject<E>, F extends DataObject<F>, G extends DataObject<G>> {
/**
 * gets all links and right objects for the left object
 * 
 * @param leftid              left object id
 * @param additionalcondition additional query filter
 * @return all links and right objects
 */
public TwoDataObjects<E, G>[] getlinksandrightobject(DataObjectId<F>[] leftid, QueryFilter additionalcondition);
}

/**
* gets an helper class that can perform a massive get links and rights objects
* for the left object
* 
* @return
*/
public MassiveGetlinksandrightobject<E, F, G> getMassiveGetlinksandrightobject();

public interface MassiveGetlinksandleftobject<E extends DataObject<E>, F extends DataObject<F>, G extends DataObject<G>> {
/**
 * gets all links and left objects for the right object
 * 
 * @param rightmsid              right object id
 * @param additionalcondition additional query filter
 * @return all left and link objects
 */
public TwoDataObjects<F, E>[] getlinksandleftobject(DataObjectMasterId<G>[] rightmsid, QueryFilter additionalcondition);
}

/**
* gets an helper class that can perform a massive get links and left objects
* for the right object
* 
* @return
*/
public MassiveGetlinksandleftobject<E, F, G> getMassiveGetlinksandleftobject();
}



