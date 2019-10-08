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

/**
 * A PagedLabel is a PDFPageBandSection that can be put in a table of content.
 * It can provide its label and its page number at the time of printing.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public interface PagedLabel {
	/**
	 * @return the Label to print
	 */
	public String getLabel();

	/**
	 * @return the page number. There are restrictions on when it can be called for
	 *         good behaviour
	 */
	public int getPageNumber();

	/**
	 * @return the original offset in mm, the table of content will typically
	 *         compress it
	 */
	public float getOriginalOffset();
}
