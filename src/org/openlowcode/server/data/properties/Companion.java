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
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a> 
 * @since 1.13
 */
public class Companion<E extends DataObject<E> & HasidInterface<E>,F extends DataObject<F> & TypedInterface<F,G>,G extends FieldChoiceDefinition<G>> extends DataObjectProperty<E> {

	private Hasid<E> hasid;

	public Companion(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		// TODO Auto-generated constructor stub
	}
	
	public void setDependentPropertyHasid(Hasid<E> hasid) {
		this.hasid=hasid;
	}
	public void createtyped(E companionobject,F mainobject) {
		throw new RuntimeException("Not yet implemented");
	}
	public void updatetyped(E companionobject,F mainobject) {
		throw new RuntimeException("Not yet implemented");
	}
	

}
