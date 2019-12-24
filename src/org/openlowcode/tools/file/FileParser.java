/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.file;

import java.io.IOException;

/**
 * A file parser is reading a file, and sending a series of rows. Each row is
 * made of objects. When there is no more row, null is returned
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface FileParser {

	/**
	 * @return an array of objects. Note: the array may have less elements than
	 *         planned if the elements on the right are empty
	 * @throws IOException
	 */

	public Object[] parseOneLine() throws IOException;

	/**
	 * Close the fileparser
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

}
