package org.openlowcode.server.data.properties;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectInterface;

/**
 * Interface that a DataObject with a Storedobject property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object this property applies to
 */
public interface StoredobjectInterface<E extends DataObject<E>> extends DataObjectInterface {
	public void insert();

	/**
	 * @return a function allowing massive insert of the type
	 */
	public MassiveInsert<E> getMassiveInsert();

	/**
	 * This function allows the insertion of a group of objects of
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
	 * @param <E> the object type
	 */
	public interface MassiveInsert<E extends DataObject<E>> {
		public void insert(E[] objectbatch);
	}
}
