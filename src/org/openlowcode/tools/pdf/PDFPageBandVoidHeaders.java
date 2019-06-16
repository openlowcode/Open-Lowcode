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
 * A void header that takes no space for a page band
 * 
 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
 *
 */
public class PDFPageBandVoidHeaders implements PDFPageBandHeaders {

	@Override
	public void printHeaders(PDFPage currentpage, float leftprintableinmm, float topprintableinmm,
			float rightprintableinmm, float bottomprintableinmm) throws IOException {

	}

	@Override
	public float getTopHeaderSpace() {

		return 0;
	}

	@Override
	public float getBottomHeaderSpace() {

		return 0;
	}

	@Override
	public float getLeftHeaderSpace() {

		return 0;
	}

	@Override
	public float getRightHeaderSpace() {

		return 0;
	}

}
