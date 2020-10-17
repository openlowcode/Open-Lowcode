/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.message;

import java.io.IOException;
import java.util.HashMap;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.HasidInterface;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedInterface;
import org.openlowcode.tools.structure.ObjectDataElt;

/**
 * A data element message typed to a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class TObjectDataElt<E extends DataObject<E>> extends ObjectDataElt {

	private E object;

	/**
	 * creates a data object element. will generate a unique id if not existing
	 * 
	 * @param name   name of the object
	 * @param object object
	 */
	public TObjectDataElt(String name, E object) {
		super(name, new TObjectDataEltType<E>(object.getDefinitionFromObject()), object.getFieldList());
		this.object = object;
		if (object instanceof HasidInterface) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			HasidInterface<E> hasidobject = (HasidInterface) object;
			this.setUID(hasidobject.getId().getId());
		} else {
			if (object.getTransientid() != null) {
				this.setUID(object.getTransientid());
			} else {
				this.setUID(Thread.currentThread().getId() + "/" + OLcServer.getServer().getNextSequence());
			}
		}
	}

	/**
	 * get the object payload
	 * 
	 * @return object payload
	 */
	public E getContent() {
		return this.object;
	}

	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		object.writeObjectContent(writer, hiddenfields, this.getUID());
		writer.endStructure("DELT");

	}
}
