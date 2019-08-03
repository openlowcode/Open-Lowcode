/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

import java.util.logging.Logger;

/**
 * A OLc messages are made of a succession of MessageElements. the String
 * message sent is made of the succession of serialization of message elements.
 * 
 * @author Open Lowcode SAS
 *
 */
public abstract class MessageElement {
	private static Logger logger = Logger.getLogger(MessageElement.class.getName());

	/**
	 * 
	 * @param padding
	 * @param firstattribute indicates if this element if the first attribute inside
	 *                       the structure If not, it is expected that the
	 *                       serialization print a separator (comma)
	 * @return
	 */
	public abstract String serialize(String padding, boolean firstattribute);

	@Override
	public String toString() {
		try {
			return serialize("", false).trim().replace('\n', ' ');
		} catch (Exception e) {
			logger.warning("Error in serialization of MessageElement " + e.getClass() + " - " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning("   * " + e.getStackTrace()[i]);
			return "#ERROR";

		}
	}

}
