/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeDate;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * Data element storing a date (in the java sense, storing precise time with ms precision)
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
/**
 * @author demau
 *
 */
public class DateDataElt extends SimpleDataElt {
	private Date payload;
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public Date getPayload() {
		return this.payload;
	}
	/**
	 * @param name name of the element
	 */
	public DateDataElt(String name) {
		super(name,new DateDataEltType());
		
	}
	/**
	 * @param name name of the element
	 * @param payload date payload
	 */
	public DateDataElt(String name,Date payload) {
		super(name, new DateDataEltType());
		this.payload = payload;
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		writer.addDateField("PLD",payload);

	}
	@Override
	protected Object getMessageArrayValue() {
		return payload;
	}
	
	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = reader.returnNextDateField("PLD");
		
	}
	
	public void updatePayload(Date date) {
		this.payload = date;
	}
	
	@Override
	public String defaultTextRepresentation() {
		if (payload!=null) { 
		String stringdate=dateformat.format(payload);
		long days = (new Date().getTime()-payload.getTime())/(24*3600*1000);
		 if (days>1) stringdate+=" ("+days+" days ago)";
		 if (days==1) stringdate+=" (yesterday)";
		if (days==0) stringdate+=" (today)";
		return stringdate;
		}
		return "[Date not set]";
	}
	@Override
	public DateDataElt cloneElt()  {
		if (payload!=null) return new DateDataElt(this.getName(),new Date(this.payload.getTime()));
		return new DateDataElt(this.getName(),null);
	}
	@Override
	public void forceContent(String constraintvalue)  {
		throw new RuntimeException("not yet implemented");
		
	}
	@Override
	public boolean equals(Object other) {
		if (other==null) return false;
		if (!(other instanceof DateDataElt)) return false;
		DateDataElt parseddataelt = (DateDataElt) other;
		return (this.payload.equals(parseddataelt.payload));
	}
	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(),MessageFieldTypeDate.singleton);
	}
}
