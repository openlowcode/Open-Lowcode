/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

/**
 * A method additional processing is a piece of processing information that is
 * executed either before of after the execeution of a Data Access Method of
 * another property. As an example, the creation log property will fill the
 * creation user and date after data insertion
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MethodAdditionalProcessing {
	private boolean preprocessing;
	private boolean preliminarydataaccess;
	private DataAccessMethod dependentmethod;

	/**
	 * creates a new Method additional processing
	 * 
	 * @param preprocessing   true if processing is done before the related method,
	 *                        or false if done after. Typically, the main method
	 *                        will include persistence, so main criteria is if
	 *                        method is needed before persistence to either add data
	 *                        or block persistence by throwing an exception
	 * @param dependentmethod the dependent method on another property on the same
	 *                        object
	 */
	public MethodAdditionalProcessing(boolean preprocessing, DataAccessMethod dependentmethod) {
		this.preprocessing = preprocessing;
		this.preliminarydataaccess = false;
		this.dependentmethod = dependentmethod;
		if (dependentmethod == null)
			throw new RuntimeException("Setting dependent on non existent method");
	}

	/**
	 * @param preprocessing         true if processing is done before the related
	 *                              method, or false if done after. Typically, the
	 *                              main method will include persistence, so main
	 *                              criteria is if method is needed before
	 *                              persistence to either add data or block
	 *                              persistence by throwing an exception
	 * @param preliminarydataaccess if true, signals the method preprocessing needs
	 *                              data access
	 * @param dependentmethod       the dependent method on another property on the
	 *                              same object
	 */
	public MethodAdditionalProcessing(
			boolean preprocessing,
			boolean preliminarydataaccess,
			DataAccessMethod dependentmethod) {
		this.preprocessing = preprocessing;
		this.preliminarydataaccess = preliminarydataaccess;
		this.dependentmethod = dependentmethod;
		if (dependentmethod == null)
			throw new RuntimeException("Setting dependent on non existent method");
	}

	/**
	 * @return true if method if pre-processing
	 */
	public boolean isPreprocessing() {
		return preprocessing;
	}

	/**
	 * @return true if method has preliminary data access
	 */
	public boolean isPreliminaryDataAccess() {
		return this.preliminarydataaccess;
	}

	/**
	 * @return true if method has dependent method
	 */
	public DataAccessMethod getDependentmethod() {
		return dependentmethod;
	}

}
