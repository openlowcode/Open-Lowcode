/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage;

import java.util.ArrayList;


/**
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OrQueryCondition extends QueryCondition {
	
		private ArrayList<QueryCondition> conditions;
		/**
		 * creates an empty OR query condition. Conditions have to be added later
		 */
		public OrQueryCondition() {
			conditions = new ArrayList<QueryCondition>();
		}
		/**
		 * Convenience method to create a query condition joining with an 'OR' the two conditions
		 * @param condition1 first condition
		 * @param condition2 second condition
		 */
		public OrQueryCondition(QueryCondition condition1,
				QueryCondition condition2) {
			this();
			this.addCondition(condition1);
			this.addCondition(condition2);
		}
		/**
		 * Convenience method to create a query condition joining with an 'OR' the three conditions
		 * @param condition1 first condition
		 * @param condition2 second condition
		 * @param condition3 third condition
		 */
		public OrQueryCondition(QueryCondition condition1,
				QueryCondition condition2,QueryCondition condition3) {
			this();
			this.addCondition(condition1);
			this.addCondition(condition2);
			this.addCondition(condition3);
		}
		/**
		 * Convenience method to create a query condition joining with an 'OR' the three conditions
		 * @param condition1 first condition
		 * @param condition2 second condition
		 * @param condition3 third condition
		 * @param condition4 fourth condition
		 */
		public OrQueryCondition(QueryCondition condition1,
				QueryCondition condition2,QueryCondition condition3,QueryCondition condition4) {
			this();
			this.addCondition(condition1);
			this.addCondition(condition2);
			this.addCondition(condition3);
			this.addCondition(condition4);
			
		}
		
		
		/** 
		 *  Adds a condition to be joined to the other query conditions by an 'OR' operation
		 * @param condition condition to join
		 */
		public void addCondition(QueryCondition condition) {
			if (condition==null) throw new RuntimeException("Query Condition is null");
			conditions.add(condition);
			
		}
		@Override
		public void accept(Visitor visitor)  {
			visitor.visit(this);

		}
		public  QueryCondition[] returnAllConditions() {
			return conditions.toArray(new QueryCondition[0]);
		}
		@Override
		public boolean isSignificant(int circuitbreaker)  {
			if (circuitbreaker>QueryCondition.MAX_CIRCUIT_BREAKER) throw new RuntimeException("Circuit Breaker");
			for (int i=0;i<conditions.size();i++) {
				if (conditions.get(i).isSignificant(circuitbreaker+1)) return true;
			}
			return false;
		}

}
