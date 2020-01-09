/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties.constraints;

import java.util.logging.Logger;

import org.openlowcode.server.data.DataExtractor;
import org.openlowcode.server.data.DataExtractorFromObject;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * This constraint will only allow to link two objects if they have a similar
 * attribute value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> concerned data object (the data object being linked by the
 *        autolink)
 * @param <F> the class of the payload of the field
 */
public class ConstraintOnAutolinkObjectSimilarAttribute<E extends DataObject<E>, F extends Object>
		extends ConstraintOnAutolinkObject<E> {
	private static Logger logger = Logger.getLogger(ConstraintOnAutolinkObjectSimilarAttribute.class.getName());
	private DataExtractor<E, F> linkedobjectextractor;
	private DataExtractorFromObject<E, F> extractorfromobject;
	private StoredFieldSchema<F> attributemarker;
	private String errormessagestart;

	@Override
	public boolean checklinkvalid(DataObjectId<E> leftobjectid, DataObjectId<E> rightobjectid) {
		F leftfield = linkedobjectextractor.extract(leftobjectid);
		F rightfield = linkedobjectextractor.extract(rightobjectid);

		if (leftfield == null) {
			if (rightfield == null) {
				logger.info("check on left and right object is consistent both null (left oid = " + leftobjectid
						+ ", right oid = " + rightobjectid);

				return true;
			}
			if (rightfield != null) {
				logger.info("check on left and right object is not consistent left null (left oid = " + leftobjectid
						+ ", right oid = " + rightobjectid);

				return false;
			}
		}
		if (rightfield == null) {
			logger.info("check on left and right object is not consistent right null (left oid = " + leftobjectid
					+ ", right oid = " + rightobjectid);

			return false;
		}

		if (leftfield.equals(rightfield)) {
			logger.info("check on left and right object is consistent same value (left oid = " + leftobjectid
					+ ", right oid = " + rightobjectid);
			return true;
		}
		logger.info("check on left and right object is not consistent differnet values (left oid = " + leftobjectid
				+ ", right oid = " + rightobjectid);
		return false;
	}

	@Override
	public QueryCondition generateQueryFilter(TableAlias maintablealias, DataObjectId<E> leftobjectid) {
		F leftobjectfieldvalue = linkedobjectextractor.extract(leftobjectid);
		SimpleQueryCondition<F> attributecorrectquerycondition = new SimpleQueryCondition<F>(maintablealias,
				attributemarker, new QueryOperatorEqual<F>(), leftobjectfieldvalue);
		return attributecorrectquerycondition;
	}

	/**
	 * Creates a new constraint on auto-link object similar attribute
	 * 
	 * @param linkedobjectextractor extractor to get payload from the objet id
	 * @param extractorfromobject   extract to get payload from the object
	 * @param attributemarker       marker of the attribute on the object
	 * @param errormessagestart     start of error message for context
	 */
	public ConstraintOnAutolinkObjectSimilarAttribute(DataExtractor<E, F> linkedobjectextractor,
			DataExtractorFromObject<E, F> extractorfromobject, StoredFieldSchema<F> attributemarker,
			String errormessagestart) {
		this.linkedobjectextractor = linkedobjectextractor;
		this.attributemarker = attributemarker;
		this.extractorfromobject = extractorfromobject;
		this.errormessagestart = errormessagestart;
	}

	@Override
	public boolean checklinkvalid(E leftobject, E rightobject) {
		if (extractorfromobject.extract(leftobject).equals(extractorfromobject.extract(rightobject)))
			return true;
		return false;
	}

	@Override
	public String getInvalidLinkErrorMessage(E leftobject, E rightobject) {
		StringBuffer error = new StringBuffer();
		error.append(errormessagestart);
		error.append(".\nFirst object: ");
		error.append(leftobject.dropIdToString());
		error.append(", value = ");
		error.append(extractorfromobject.extract(leftobject));
		error.append(" \nSecond object: ");
		error.append(rightobject.dropIdToString());
		error.append(", value = ");
		error.append(extractorfromobject.extract(rightobject));

		return error.toString();

	}
}
