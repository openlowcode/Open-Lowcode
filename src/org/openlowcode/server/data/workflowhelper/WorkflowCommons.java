/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.workflowhelper;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Email;
import org.openlowcode.module.system.data.Emailrecipient;
import org.openlowcode.module.system.data.Task;
import org.openlowcode.module.system.data.Taskuser;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.DelaytypeChoiceDefinition;
import org.openlowcode.module.system.data.choice.EmailstatusChoiceDefinition;
import org.openlowcode.module.system.data.choice.RecipienttypeChoiceDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.NamedInterface;
import org.openlowcode.server.data.properties.NumberedInterface;
import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * This class gathers procedures that are common to the different types of
 * workflow in the application.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class WorkflowCommons {
	private static final Logger logger = Logger.getLogger(WorkflowCommons.class.getName());

	/**
	 * sends an e-mail notification for a workflow
	 * 
	 * @param title            title of the notification
	 * @param bodytext         body text
	 * @param objectid         id of the object
	 * @param objectlabel      label of the object
	 * @param senderuserid     id of the user who sent the mail
	 * @param users            users to the send e-mail to
	 * @param emailtype        e-mail type
	 * @param notificationname name of notification
	 * @param modulename       name of module
	 * @param action           true if the e-mail if for action
	 */
	public static void sendNotification(String title, String bodytext, String objectid, String objectlabel,
			DataObjectId<Appuser> senderuserid, ArrayList<DataObjectId<Appuser>> users, String emailtype,
			String notificationname, String modulename, boolean action) {
		Email email = new Email();
		email.setTitle(title);
		String finalbodytext = bodytext;
		if (finalbodytext.length() > 3995)
			finalbodytext = finalbodytext.substring(0, 3995) + "...";
		email.setBodytext(finalbodytext);
		email.setObjectid(objectid);
		email.setObjectlabel(objectlabel);
		email.setModule(modulename);
		if (action) {
			email.setAction(BooleanChoiceDefinition.get().YES);
		} else {
			email.setAction(BooleanChoiceDefinition.get().NO);
		}
		String finalnotificationname = notificationname;

		if (notificationname.length() > 64)
			finalnotificationname = notificationname.substring(0, 61) + "...";

		email.setobjectname(finalnotificationname);
		boolean emailtypeprocessed = false;
		if (emailtype.equals(SimpletaskWorkflowHelper.EMAIL_DAILY)) {
			emailtypeprocessed = true;
			email.setDelaytype(DelaytypeChoiceDefinition.get().DAY);
		}
		if (emailtype.equals(SimpletaskWorkflowHelper.EMAIL_DELAY15MIN)) {
			emailtypeprocessed = true;
			email.setDelaytype(DelaytypeChoiceDefinition.get().D15M);
		}
		if (emailtype.equals(SimpletaskWorkflowHelper.EMAIL_DELAY2H)) {
			emailtypeprocessed = true;
			email.setDelaytype(DelaytypeChoiceDefinition.get().D2H);
		}
		if (emailtype.equals(SimpletaskWorkflowHelper.EMAIL_NOW)) {
			emailtypeprocessed = true;
			email.setDelaytype(DelaytypeChoiceDefinition.get().NOW);
		}
		if (emailtype.equals(SimpletaskWorkflowHelper.EMAIL_WEEKLY)) {
			emailtypeprocessed = true;
			email.setDelaytype(DelaytypeChoiceDefinition.get().WKLY);
		}
		if (!emailtypeprocessed)
			throw new RuntimeException("email delay type not valid " + emailtype);

		Appuser sender = Appuser.readone(senderuserid);
		email.setSender(sender.getEmail());
		email.insert();
		for (int i = 0; i < users.size(); i++) {
			Appuser recipient = Appuser.readone(users.get(i));
			Emailrecipient emailrecipient = new Emailrecipient();
			emailrecipient.setparentforowneremail(email.getId());
			emailrecipient.setRecipient(recipient.getEmail());
			emailrecipient.setRecipienttype(RecipienttypeChoiceDefinition.get().TO);
			emailrecipient.insert();
		}
		email.changestate(EmailstatusChoiceDefinition.getChoiceReadytosend());

	}

	/**
	 * assigns tasks to a user
	 * 
	 * @param task             task to send
	 * @param specificuser     user to send the task to
	 * @param emailtype        type of e-mail
	 * @param title            title of the e-mail
	 * @param body             body of the e-mail
	 * @param objectid         id of the object
	 * @param objectlabel      label of the object
	 * @param originator       originator of the task
	 * @param notificationname name of the notification
	 * @param object           data object subject of the workflow
	 */
	public static void assignTaskToUser(Task task, DataObjectId<Appuser> specificuser, String emailtype, String title,
			String body, String objectid, String objectlabel, DataObjectId<Appuser> originator, String notificationname,
			DataObject<?> object) {
		Taskuser taskuser = new Taskuser();
		taskuser.setleftobject(task.getId());
		taskuser.setrightobject(specificuser);
		taskuser.insert();
		ArrayList<DataObjectId<Appuser>> singleuser = new ArrayList<DataObjectId<Appuser>>();
		singleuser.add(specificuser);
		if (emailtype != null)
			sendNotification(title, body, objectid, objectlabel, originator, singleuser, emailtype, notificationname,
					object.getDefinitionFromObject().getModuleName(), true);
	}

	/**
	 * assigns a task to an authority
	 * 
	 * @param task          task
	 * @param authorityid   id of the authority
	 * @param usertoexclude user to exclude. Typically, this happens when an user
	 *                      rejects a task, it is sent back to the group, with
	 *                      exception of the user having just rejected the task
	 * @param emailtype     type of e-mail for notification
	 * @param emailname     name of the e-mail
	 * @param title         title of the e-mail
	 * @param body          body of the e-mail
	 * @param objectid      id of the object
	 * @param objectlabel   label of the object
	 * @param originator    originator for notification e-mail
	 * @param object        data object the workflow is working on
	 */
	public static void assignTaskToAuthority(Task task, DataObjectId<Authority> authorityid,
			DataObjectId<Appuser> usertoexclude, String emailtype, String emailname, String title, String body,
			String objectid, String objectlabel, DataObjectId<Appuser> originator, DataObject<?> object) {
		logger.info("start assigning task to authority, task id = " + task.getId() + " for authority " + authorityid
				+ " user to exclude = " + (usertoexclude != null ? usertoexclude.getId() : ""));
		Appuser[] authorityusers = ServerSecurityBuffer.getUniqueInstance().getUsersForAuthority(authorityid);
		boolean existingusers = false;
		ArrayList<DataObjectId<Appuser>> recipients = new ArrayList<DataObjectId<Appuser>>();
		if (authorityusers != null)
			if (authorityusers.length > 0) {

				for (int i = 0; i < authorityusers.length; i++) {
					Appuser currentuser = authorityusers[i];
					boolean process = false;
					if (usertoexclude == null)
						process = true;
					if (usertoexclude != null)
						if (!(currentuser.getId().getId().equals(usertoexclude.getId())))
							process = true;

					if (process) {
						recipients.add(currentuser.getId());
						existingusers = true;
						Taskuser taskuser = new Taskuser();
						taskuser.setleftobject(task.getId());
						taskuser.setrightobject(currentuser.getId());
						taskuser.insert();
					}

				}
			}
		if (!existingusers) {
			Appuser admin = Appuser.getobjectbynumber("admin")[0];
			recipients.add(admin.getId());
			Taskuser taskuser = new Taskuser();
			taskuser.setleftobject(task.getId());
			taskuser.setrightobject(admin.getId());
			taskuser.insert();
		}
		if (emailtype != null)
			sendNotification(title, body, objectid, objectlabel, originator, recipients, emailtype, emailname,
					object.getDefinitionFromObject().getModuleName(), true);
	}

	/**
	 * generates a string identifying the object. This is typically number + name
	 * 
	 * @param object the object to process
	 * @return the best object short string
	 */
	@SuppressWarnings("rawtypes")
	public static String generateObjectString(DataObject<?> object) {
		String objectid = object.getDefinitionFromObject().getLabel() + " ";
		if (object instanceof NumberedInterface)
			objectid += ((NumberedInterface) object).getNr() + " ";
		if (object instanceof NamedInterface)
			objectid += ((NamedInterface) object).getObjectname() + " ";
		return objectid;
	}

	/**
	 * generates a readable object id (number) if possible
	 * 
	 * @param object data object
	 * @return the best object id available
	 */
	public static String generateObjectId(DataObject<?> object) {
		String objectid = "Not Numbered";
		if (object instanceof NumberedInterface) {
			@SuppressWarnings("rawtypes")
			NumberedInterface numberobject = (NumberedInterface) object;
			objectid = numberobject.getNr();
		}
		return objectid;
	}
}
