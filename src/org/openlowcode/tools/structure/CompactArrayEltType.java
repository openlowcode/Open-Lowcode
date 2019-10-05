/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;


/**
 * A way to write efficiently an array
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 * @param <E> a type of element
 */
/**
 * @author demau
 *
 * @param <E>
 */
public interface CompactArrayEltType<E extends DataElt> {
	
	
	
	
	public abstract void writeCompactArray(ArrayList<E> datatowrite, MessageWriter writer,HashMap<String,NamedInterface> hiddenfields) throws IOException;
	
	public abstract void readCompactArray(ArrayList<E> emptylisttofill,MessageReader reader) throws OLcRemoteException,IOException;
}
