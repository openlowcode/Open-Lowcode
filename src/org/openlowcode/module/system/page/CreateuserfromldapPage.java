/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.page;

import org.openlowcode.module.system.action.CreateuserfromldapAction;
import org.openlowcode.module.system.action.QueryforuserAction;
import org.openlowcode.module.system.action.generated.AtgShowusergroupAction;
import org.openlowcode.module.system.data.Ldapuser;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;
import org.openlowcode.module.system.page.generated.AbsCreateuserfromldapPage;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SActionButton;
import org.openlowcode.server.graphic.widget.SChoiceTextField;
import org.openlowcode.server.graphic.widget.SCollapsibleBand;
import org.openlowcode.server.graphic.widget.SComponentBand;
import org.openlowcode.server.graphic.widget.SObjectArray;
import org.openlowcode.server.graphic.widget.SObjectIdStorage;
import org.openlowcode.server.graphic.widget.SPageText;
import org.openlowcode.server.graphic.widget.STextField;
import org.openlowcode.server.graphic.widget.STextStorage;

/**
 * Page allowing to create an user from an LDAP page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class CreateuserfromldapPage
		extends
		AbsCreateuserfromldapPage {

	/**
	 * displays the page allowing to create a user from LDAP
	 * 
	 * @param group       group the user is created in
	 * @param grouplabel  label of the group
	 * @param userid      LDAP account id
	 * @param userpasword LDAP account passwork
	 * @param ldapbase    base for LDAP connection
	 * @param locale      prefered locale for the user
	 * @param encoding    prefered encoding for the user
	 * @param message     message on last action
	 */
	public CreateuserfromldapPage(
			DataObjectId<Usergroup> group,
			String grouplabel,
			String userid,
			String userpasword,
			String ldapbase,
			ChoiceValue<ApplocaleChoiceDefinition> locale,
			ChoiceValue<PreferedfileencodingChoiceDefinition> encoding,
			String message) {
		super(group, grouplabel, userid, userpasword, ldapbase, locale, encoding, message);
	}

	@Override
	protected SPageNode getContent() {
		SComponentBand mainband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		mainband.addElement(new SPageText("Create Users", SPageText.TYPE_TITLE, this));
		SObjectIdStorage<Usergroup> groupidstorage = new SObjectIdStorage<Usergroup>("GROUPID", this, this.getGroup());
		mainband.addElement(groupidstorage);

		SComponentBand groupband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);
		SPageText grouplabeltext = new SPageText(this.getGrouplabel(), SPageText.TYPE_NORMAL, this);
		groupband.addElement(grouplabeltext);
		AtgShowusergroupAction.ActionRef backtousergroup = AtgShowusergroupAction.get().getActionRef();
		backtousergroup.setId(groupidstorage.getObjectIdInput());
		groupband.addElement(new SActionButton("Back to group", backtousergroup, this));
		mainband.addElement(groupband);
		SComponentBand detailsband = new SComponentBand(SComponentBand.DIRECTION_DOWN, this);
		SCollapsibleBand details = new SCollapsibleBand(this, detailsband, "Connection details", false);
		mainband.addElement(details);
		STextStorage grouplabelstorage = new STextStorage("GROUPLABEL", this, this.getGrouplabel());
		mainband.addElement(grouplabelstorage);
		STextField youruser = new STextField("LDAP account id", "YOURUSERID",
				"Fill it in case the LDAP service account of server is not setup on the server. LDAP Account fully qualified name or id of a user in Gallium",
				64, "", true, this, false, false, false, null);
		youruser.setTextBusinessData(this.getUserid());
		detailsband.addElement(youruser);
		STextField yourpassword = new STextField("LDAP account password", "YOURPASSWORD",
				"Fill it in case the LDAP service account of server is not setup on the server: password of the LDAP account",
				64, "", true, this, false, false, false, null);
		yourpassword.setTextBusinessData(this.getUserpasword());
		yourpassword.hideDisplay();
		detailsband.addElement(yourpassword);
		SChoiceTextField<ApplocaleChoiceDefinition> preferedlocale = new SChoiceTextField<ApplocaleChoiceDefinition>(
				"Locale", "LOCALE", "Locale to create new user", ApplocaleChoiceDefinition.get(), this.getLocale(),
				this, false, null);
		detailsband.addElement(preferedlocale);
		SChoiceTextField<PreferedfileencodingChoiceDefinition> preferedfileencoding = new SChoiceTextField<
				PreferedfileencodingChoiceDefinition>("File Encoding", "FILEENCODING",
						"prefered file encoding for new user", PreferedfileencodingChoiceDefinition.get(),
						this.getEncoding(), this, false, null);
		detailsband.addElement(preferedfileencoding);

		STextField ldapbase = new STextField("LDAP Base", "LDAPBASE", "Base for enterprise search", 64, "", true, this,
				false, false, false, null);
		ldapbase.setTextBusinessData(this.getLdapbase());
		detailsband.addElement(ldapbase);
		QueryforuserAction.InlineActionRef searchinldap = QueryforuserAction.get().getInlineActionRef();

		STextField searchid = new STextField("Search id", "SEARCHID", "Start of the id you are searching", 64, "", true,
				this, false, false, false, searchinldap);
		mainband.addElement(searchid);
		STextField searchlastname = new STextField("Search Last name", "SEARCHLASTNAME", "Search last name", 64, "",
				true, this, false, false, false, searchinldap);
		mainband.addElement(searchlastname);
		mainband.addElement(new SPageText(this.getMessage(), SPageText.TYPE_NORMAL, this));
		searchinldap.setUserid(youruser.getTextInput());
		searchinldap.setUserpasword(yourpassword.getTextInput());
		searchinldap.setLdapbase(ldapbase.getTextInput());
		searchinldap.setLastnamestartsby(searchlastname.getTextInput());
		searchinldap.setIdstartsby(searchid.getTextInput());

		SActionButton search = new SActionButton("Search in directory", searchinldap, this);
		mainband.addElement(search);

		SObjectArray<Ldapuser> usersearchresult = new SObjectArray<Ldapuser>("SEARCHRESULTARRAY", searchinldap,
				QueryforuserAction.get().getLdapusersRef(), Ldapuser.getDefinition(), this);
		mainband.addElement(usersearchresult);
		usersearchresult.setAllowMultiSelect();
		usersearchresult.setAllowDataClear();
		usersearchresult.setRowsToDisplay(1);
		CreateuserfromldapAction.ActionRef createuseraction = CreateuserfromldapAction.get().getActionRef();
		createuseraction.setGroup(groupidstorage.getObjectIdInput());
		createuseraction.setGrouplabel(grouplabelstorage.getTextInput());
		createuseraction.setSelectedldapuser(usersearchresult.getActiveObjectArray());
		createuseraction.setUserid(youruser.getTextInput());
		createuseraction.setUserpasword(yourpassword.getTextInput());
		createuseraction.setLdapbase(ldapbase.getTextInput());
		createuseraction.setLocale(preferedlocale.getChoiceInput());
		createuseraction.setEncoding(preferedfileencoding.getChoiceInput());
		SActionButton createusers = new SActionButton("Create Users", createuseraction, this);
		mainband.addElement(createusers);
		return mainband;
	}

	@Override
	public String generateTitle(
			DataObjectId<Usergroup> group,
			String grouplabel,
			String userid,
			String userpasword,
			String ldapbase,
			ChoiceValue<ApplocaleChoiceDefinition> locale,
			ChoiceValue<PreferedfileencodingChoiceDefinition> encoding,
			String message) {
		return "Create user from LDAP in group " + grouplabel;
	}

}
