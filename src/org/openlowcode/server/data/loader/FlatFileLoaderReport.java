/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import org.openlowcode.module.system.data.Csvloadererror;

/**
 * A class to store report for a loading session
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FlatFileLoaderReport {
	private String context;
	private int inserted;
	private int updated;
	private int error;
	private int postprocerror;
	private long loadingtimems;
	private Csvloadererror[] errordetails;
	private int untouched;

	/**
	 * gets the context of the loading
	 * 
	 * @return the context (text string)
	 */
	public String getContext() {
		return context;
	}

	/**
	 * gets the number of elements inserted
	 * 
	 * @return the number of elements inserted
	 */
	public int getInserted() {
		return inserted;
	}

	/**
	 * gets the number of elements updated
	 * 
	 * @return the number of elements updated
	 */
	public int getUpdated() {
		return updated;
	}

	/**
	 * gets the number of errors (nothing inserted / updated at all)
	 * 
	 * @return the number of errors
	 */
	public int getError() {
		return error;
	}

	/**
	 * get the number of post processing errors: main object insert / update
	 * proceeded, but some post-processing statement failed
	 * 
	 * @return the number of post processing errors
	 */
	public int getPostprocError() {
		return postprocerror;
	}

	/**
	 * return the loading time in ms
	 * 
	 * @return loading time in ms
	 */
	public long getLoadingtimems() {
		return loadingtimems;
	}

	/**
	 * gets the number of erros
	 * 
	 * @return the loader errors
	 */
	public Csvloadererror[] getErrordetails() {
		return errordetails;
	}

	/**
	 * returns the number of lines left untouched
	 * 
	 * @return the number of lines left untouched
	 */
	public int getUntouched() {
		return this.untouched;
	}

	/**
	 * creates a new report
	 * 
	 * @param context       context for loading
	 * @param inserted      number of lines inserted
	 * @param updated       number of lines updated
	 * @param untouched     number of lines untouched
	 * @param error         number of lines in error (nothing inserted)
	 * @param postprocerror number of lines with post-processing errors (main values
	 *                      inserted but further processing failed)
	 * @param loadingtimems loading time in ms
	 * @param errordetails  list of errors
	 */
	public FlatFileLoaderReport(String context, int inserted, int updated, int untouched, int error, int postprocerror,
			long loadingtimems, Csvloadererror[] errordetails) {

		this.context = context;
		this.inserted = inserted;
		this.updated = updated;
		this.untouched = untouched;
		this.error = error;
		this.postprocerror = postprocerror;
		this.loadingtimems = loadingtimems;
		this.errordetails = errordetails;
	}

}
