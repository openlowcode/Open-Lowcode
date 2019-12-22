package org.openlowcode.server.data.formula;

import org.openlowcode.server.data.DataObject;



/**
 * an object navigator allows to navigate from one object to another inside the formula
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the input object for the navigation
 * @param <F> the output object for the navigation
 */
@FunctionalInterface
public interface ObjectNavigator<E extends DataObject<E>,F extends DataObject<F>> {
	/**
	 * executes the navigation at the time of calculation of the formula
	 * @param object input object
	 * @return output objects of the navigation
	 */
	public F[] navigate(E object);

}
