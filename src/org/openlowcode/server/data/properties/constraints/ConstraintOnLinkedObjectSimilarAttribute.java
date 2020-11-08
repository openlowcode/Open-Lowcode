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
import org.openlowcode.server.data.DataSetterFromObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * This constraint will allow to link two objects only if they have same value
 * on the attribute defined on each object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 *
 * @param <E> left data object for the link
 * @param <F> right data object for the link
 * @param <G> class of the payload of the attribute being compared on the two
 *        objects
 */
public class ConstraintOnLinkedObjectSimilarAttribute<E extends DataObject<E>, F extends DataObject<F>, G extends Object>
		extends ConstraintOnLinkObject<E, F> {
	private static Logger logger = Logger.getLogger(ConstraintOnLinkedObjectSimilarAttribute.class.getName());
	DataExtractor<E, G> leftobjectextractor;
	DataExtractor<F, G> rightobjectextractor;
	DataExtractorFromObject<E, G> leftobjectextractorfromobject;
	DataExtractorFromObject<F, G> rightobjectextractorfromobject;
	@SuppressWarnings("unused")
	private DataSetterFromObject<E, G> leftobjectsetter;
	private DataSetterFromObject<F, G> rightobjectsetter;
	private String errormessagestart;

	private StoredFieldSchema<G> rightattributemarker;
	private StoredFieldSchema<G> leftattributemarker;

	/**
	 * creates a constraint on link object with similar attribute
	 * 
	 * @param leftobjectextractor            extractor of the attribute from the
	 *                                       left object id
	 * @param rightobjectextractor           extractor of the attribute from the
	 *                                       right object id
	 * @param leftobjectextractorfromobject  extractor of the attribute from the
	 *                                       left object (without persistence call)
	 * @param rightobjectextractorfromobject extractor of the attribute from the
	 *                                       right object (without persistence call)
	 * @param leftobjectsetter               setter on the left object for the
	 *                                       attribute
	 * @param rightobjectsetter              setter on the right object for the
	 *                                       attribute
	 * @param leftattributemarker            left attribute marker
	 * @param rightattributemarker           right attribute marker
	 * @param errormessagestart              start of the error message for
	 *                                       traceability purposes
	 */
	public ConstraintOnLinkedObjectSimilarAttribute(DataExtractor<E, G> leftobjectextractor,
			DataExtractor<F, G> rightobjectextractor, DataExtractorFromObject<E, G> leftobjectextractorfromobject,
			DataExtractorFromObject<F, G> rightobjectextractorfromobject, DataSetterFromObject<E, G> leftobjectsetter,
			DataSetterFromObject<F, G> rightobjectsetter, StoredFieldSchema<G> leftattributemarker,
			StoredFieldSchema<G> rightattributemarker, String errormessagestart) {
		this.leftobjectextractor = leftobjectextractor;
		this.rightobjectextractor = rightobjectextractor;
		this.leftattributemarker = leftattributemarker;
		this.rightattributemarker = rightattributemarker;
		this.leftobjectextractorfromobject = leftobjectextractorfromobject;
		this.rightobjectextractorfromobject = rightobjectextractorfromobject;
		this.leftobjectsetter = leftobjectsetter;
		this.rightobjectsetter = rightobjectsetter;
		this.errormessagestart = errormessagestart;
	}

	@Override
	public boolean checklinkvalid(DataObjectId<E> leftobjectid, DataObjectId<F> rightobjectid) {
		G leftfield = leftobjectextractor.extract(leftobjectid);
		G rightfield = rightobjectextractor.extract(rightobjectid);

		if (leftfield == null) {
			if (rightfield == null)
				return true;
			if (rightfield != null)
				return false;
		}
		if (rightfield == null)
			return false;

		if (leftfield.equals(rightfield)) {
			logger.info("check on left and right object is consistent (left oid = " + leftobjectid + ", right oid = "
					+ rightobjectid);
			return true;
		}
		logger.info("check on left and right object is not consistent (left oid = " + leftobjectid + ", right oid = "
				+ rightobjectid);
		return false;
	}

	@Override
	public QueryCondition generateQueryFilter(TableAlias maintablealias, DataObjectId<E> leftobjectid)
			{

		G leftobjectfieldvalue = leftobjectextractor.extract(leftobjectid);
		SimpleQueryCondition<G> attributecorrectquerycondition = new SimpleQueryCondition<G>(maintablealias,
				rightattributemarker, new QueryOperatorEqual<G>(), leftobjectfieldvalue);
		return attributecorrectquerycondition;
	}

	@Override
	public QueryCondition generateQueryFilter(TableAlias maintablealias, E leftobject) {
		G leftobjectfieldvalue = this.leftobjectextractorfromobject.extract(leftobject);
		SimpleQueryCondition<G> attributecorrectquerycondition = new SimpleQueryCondition<G>(maintablealias,
				rightattributemarker, new QueryOperatorEqual<G>(), leftobjectfieldvalue);
		return attributecorrectquerycondition;
	}
	
	@Override
	public QueryCondition generateReverseQueryFilter(TableAlias maintablealias, DataObjectId<F> rightobjectid)
			 {
		G rightobjectfieldvalue = rightobjectextractor.extract(rightobjectid);
		SimpleQueryCondition<G> attributecorrectquerycondition = new SimpleQueryCondition<G>(maintablealias,
				leftattributemarker, new QueryOperatorEqual<G>(), rightobjectfieldvalue);
		return attributecorrectquerycondition;
	}

	@Override
	public boolean checklinkvalid(E leftobject, F rightobject)  {
		G leftvalue = leftobjectextractorfromobject.extract(leftobject);
		G rightvalue = rightobjectextractorfromobject.extract(rightobject);
		if (leftvalue == null) {
			if (rightvalue == null)
				return true;
			return false;
		}
		if (leftvalue.equals(rightvalue))
			return true;
		return false;
	}

	@Override
	public String getInvalidLinkErrorMessage(E leftobject, F rightobject)  {
		StringBuffer error = new StringBuffer();
		error.append(errormessagestart);
		error.append("\nFirst object: ");
		error.append(leftobject.dropIdToString());
		error.append(", value = ");
		error.append(leftobjectextractorfromobject.extract(leftobject));
		error.append("\nSecond object: ");
		error.append(rightobject.dropIdToString());
		error.append(", value = ");
		error.append(rightobjectextractorfromobject.extract(rightobject));

		return error.toString();

	}

	@Override
	public boolean isLeftForLinkLoaderManaged()  {
		return true;
	}

	@Override
	public void enrichRightObjectAfterCreation(F rightobjectbeforecreation, E leftobject)  {
		rightobjectsetter.setValueOnObject(rightobjectbeforecreation,
				leftobjectextractorfromobject.extract(leftobject));

	}



}
