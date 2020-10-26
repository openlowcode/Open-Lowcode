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

import org.openlowcode.server.data.DataExtractor;
import org.openlowcode.server.data.DataExtractorFromObject;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * This constraint will allow to link two objects only if the left object has
 * specific values for a given field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 *
 * @param <E> left data object for the link
 * @param <F> right data object for the link
 * @param <G> class of the payload of the attribute being tested on the left
 *        object
 */
public class ConstraintOnLinkedObjectLeftAttributeValue<
		E extends DataObject<E>,
		F extends DataObject<F>,
		G extends Object>
		extends
		ConstraintOnLinkObject<E, F> {

	private StoredFieldSchema<G> leftattributemarker;
	private DataExtractor<E, G> leftobjectextractor;
	private DataExtractorFromObject<E, G> leftobjectextractorfromobject;
	private String errormessagestart;
	private G[] validvalues;

	/**
	 * creates a constraint on link object with similar attribute
	 * 
	 * @param leftobjectextractor           extractor of the attribute from the left
	 *                                      object id
	 * @param leftobjectextractorfromobject extractor of the attribute from the left
	 *                                      object (without persistence call)
	 * @param leftattributemarker           left attribute marker
	 * @param validvalues                   valid values for the left attribute
	 *                                      marker
	 * @param errormessagestart             start of the error message for
	 *                                      traceability purposes
	 */
	public ConstraintOnLinkedObjectLeftAttributeValue(
			DataExtractor<E, G> leftobjectextractor,
			DataExtractorFromObject<E, G> leftobjectextractorfromobject,
			StoredFieldSchema<G> leftattributemarker,
			G[] validvalues,
			String errormessagestart) {
		this.leftobjectextractor = leftobjectextractor;
		this.leftattributemarker = leftattributemarker;
		this.leftobjectextractorfromobject = leftobjectextractorfromobject;
		this.errormessagestart = errormessagestart;
		this.validvalues = validvalues;
	}

	@Override
	public boolean checklinkvalid(DataObjectId<E> leftobject, DataObjectId<F> rightobject) {
		G leftfield = leftobjectextractor.extract(leftobject);
		return checkValid(leftfield);
	}

	private boolean checkValid(G payload) {
		for (int i = 0; i < validvalues.length; i++) {
			if (validvalues[i].equals(payload))
				return true;
		}
		return false;
	}

	@Override
	public boolean checklinkvalid(E leftobject, F rightobject) {
		G leftfield = leftobjectextractorfromobject.extract(leftobject);
		return checkValid(leftfield);
	}

	@Override
	public String getInvalidLinkErrorMessage(E leftobject, F rightobject) {
		StringBuffer error = new StringBuffer();
		error.append(errormessagestart);
		error.append("\nFirst object: ");
		error.append(leftobject.dropIdToString());
		error.append(", value = ");
		error.append(leftobjectextractorfromobject.extract(leftobject));
		error.append("\nSecond object: ");
		error.append(rightobject.dropIdToString());
		return error.toString();
	}

	@Override
	public QueryCondition generateQueryFilter(TableAlias maintablealias, DataObjectId<E> leftobjectid) {
		return null;
	}

	@Override
	public QueryCondition generateReverseQueryFilter(TableAlias maintablealias, DataObjectId<F> rightobjectid) {
		OrQueryCondition allowedvaluescondition = new OrQueryCondition();
		for (int i = 0; i < validvalues.length; i++) {
			SimpleQueryCondition<G> attributecorrectquerycondition = new SimpleQueryCondition<G>(maintablealias,
					leftattributemarker, new QueryOperatorEqual<G>(), validvalues[i]);
			allowedvaluescondition.addCondition(attributecorrectquerycondition);
		}
		return allowedvaluescondition;
	}

	@Override
	public boolean isLeftForLinkLoaderManaged() {
		return true;
	}

	@Override
	public void enrichRightObjectAfterCreation(F rightobjectbeforecreation, E leftobject) {
		// do nothing

	}
}
