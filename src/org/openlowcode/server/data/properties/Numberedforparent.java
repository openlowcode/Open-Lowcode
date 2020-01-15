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

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;

/**
 * A property that will make the number of an object being checked for unicity
 * only amongst the children of the same parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object
 * @param <F> parent data object of the current data object
 */
public class Numberedforparent<E extends DataObject<E> & UniqueidentifiedInterface<E> & NumberedInterface<E> & NumberedforparentInterface<E, F>, F extends DataObject<F> & UniqueidentifiedInterface<F>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Numberedforparent.class.getName());
	private Numbered<E> numbered;
	private Linkedtoparent<E, F> relatedlinkedtoparent;
	private NumberedforparentDefinition<E, F> parseddefinition;

	/**
	 * Creates a property numbererd for parent
	 * 
	 * @param parseddefinition definition of the property for the data object
	 * @param parentpayload    payload of the parent data object
	 */
	public Numberedforparent(NumberedforparentDefinition<E, F> parseddefinition, DataObjectPayload parentpayload) {
		super(parseddefinition, parentpayload);
		this.parseddefinition = parseddefinition;
	}

	private void checkSetParent(E object, DataObjectId<F> newparentid) {
		E[] otherobjects = NumberedforparentQueryHelper.get().getobjectbynumberforparent(object.getNr(), newparentid,
				// parent id from object
				parseddefinition.getParentObject(), parseddefinition.getParentobjectforlinkdefinition(),
				parseddefinition);
		for (int i = 0; i < otherobjects.length; i++) {
			if (!otherobjects[i].getId().equals(object.getId())) {
				logger.warning(" --> Simple number versioned object " + object.getNr() + " - id "
						+ otherobjects[i].getId() + " - parent id " + newparentid.getId());

				throw new RuntimeException("The number '" + object.getNr() + "' already exists for the parent "
						+ parseddefinition.getParentobjectforlinkdefinition().getLabel() + ".");
			}
		}

	}

	/**
	 * when setting a new parent, checks if the number is unique for the new parent.
	 * Else, throws an error
	 * 
	 * @param object      current data object
	 * @param newparentid id of the new parent
	 */
	public void preprocLinkedtoparentSetparent(E object, DataObjectId<F> newparentid) {
		checkSetParent(object, newparentid);
	}

	/**
	 * when setting a new parent (even without persistencce), checks if the number
	 * is unique for the new parent, else throws an error
	 * 
	 * @param object      current data object
	 * @param newparentid id of the new parent
	 */
	public void preprocLinkedtoparentSetparentwithoutupdate(E object, DataObjectId<F> newparentid) {
		checkSetParent(object, newparentid);
	}

	/**
	 * gets the id of the parent used for the number unicity
	 * 
	 * @param object current data object
	 * @return data object id of the parent used for number unicity
	 */
	public DataObjectId<F> getparentidfornumber(E object) {
		return relatedlinkedtoparent.getId();
	}

	/**
	 * sets the dependent property numbered on the object
	 * 
	 * @param numbered property numbered on the object
	 */
	public void setDependentPropertyNumbered(Numbered<E> numbered) {
		this.numbered = numbered;
	}

	/**
	 * sets the dependent property linked to parent
	 * 
	 * @param relatedlinkedtoparent related property linked to parent
	 */
	public void setDependentPropertyLinkedtoparent(Linkedtoparent<E, F> relatedlinkedtoparent) {
		this.relatedlinkedtoparent = relatedlinkedtoparent;
	}

	/**
	 * get the dependent property number
	 * 
	 * @return dependent property number
	 */
	public Numbered<E> getDependentPropertyNumbered() {
		return this.numbered;
	}

}
