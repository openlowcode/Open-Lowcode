/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.misc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A small utility class aimed at writing data in a nice format
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class NiceFormatters {
	/**
	 * @param number a long number
	 * @return the number, expressed in thousands (K)
	 * if greater than 1000, or in millions (M) if greater
	 * than 1000000
	 */
	public static String formatNumber(long number) {
		if (number<1000) return ""+number;
		if ((number>=1000) & (number<1000000)) return ""+(number/1000)+"."+((number/100)%10)+"K";
		return ""+(number/1000000)+"."+((number/100000)%10)+"M";
	}
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static SimpleDateFormat fulldateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS z");
	/**
	 * @param date the date to format
	 * @param showdate if true, show date, if false, shows only amount
	 * of time since the date
	 * @return
	 */
	public static String formatNiceDate(Date date,boolean showdate) {
		String stringdate = "";
		if (date!=null) { 
			String openbracket ="";
			String closebracket = "";
			if (showdate) {
				stringdate+=dateformat.format(date);
				openbracket = "(";
				closebracket = ")";
			}
			long days = (new Date().getTime()-date.getTime())/(24*3600*1000);
			 if (days>1) stringdate+=" "+openbracket+""+days+" days ago"+closebracket+"";
			 if (days==1) stringdate+=" "+openbracket+"one day ago"+closebracket+"";
			if (days==0) {
				
				long minutes = (new Date().getTime()-date.getTime())/(60*1000);
				if (minutes>1) stringdate+=" "+openbracket+""+minutes+" minutes ago"+closebracket+"";
				if (minutes==1) stringdate+= ""+openbracket+"1 minute ago"+closebracket+"";
				if (minutes==0) stringdate+=" "+openbracket+"now"+closebracket+"";
				if (minutes==-1) stringdate+= ""+openbracket+"in 1 minute"+closebracket+"";
				
				if (minutes<-1) stringdate+=" "+openbracket+"in "+(-minutes)+" minutes"+closebracket+"";
				
			}
			if (days==-1) stringdate+= ""+openbracket+"in one day"+closebracket+"";
			if (days<-1) stringdate+=" "+openbracket+"in "+(-days)+" days"+closebracket+"";
		}
		return stringdate;
	
	}
	public static String formatNiceDate(Date date) {
		return formatNiceDate(date,true);
		}
}
