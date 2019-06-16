/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.pdf;

import java.io.IOException;

/**
 * This interface allows to specify a text to be calculated at the exact time of
 * printing. The advantage is that at this time, the complete document layout is
 * solved. So, especially, the page number of each widget is known, which allows
 * to generate layout.
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
@FunctionalInterface
public interface TextAfterLayout {

	/**
	 * @return solved text
	 * @throws IOException
	 */
	public String generateText() throws IOException;
}
