/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime.email;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.openlowcode.module.system.data.Email;
import org.openlowcode.module.system.data.Emailrecipient;
import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;
import org.openlowcode.module.system.data.choice.EmailstatusChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.properties.LifecycleQueryHelper;
import org.openlowcode.server.data.properties.StoredobjectQueryHelper;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;

import org.openlowcode.server.security.ServerSecurityBuffer;

/**
 * the mail daemon in the server in charge of sending e-mails
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class MailDaemon extends Thread {
	private SimpleDateFormat iCalendarDateFormat;

	private static Logger logger = Logger.getLogger(MailDaemon.class.getName());
	private String smtpserver;
	private int port;
	private String user = null;
	private String password;

	/**
	 * creates a mail daemon connection to the specified server with authentication
	 * 
	 * @param smtpserver URL of the smtp server
	 * @param port       active port
	 * @param user       user (service account) to connect to the smtp server
	 * @param password   password of the service account
	 */
	public MailDaemon(String smtpserver, int port, String user, String password) {
		this(smtpserver, port);
		this.user = user;
		this.password = password;

	}

	/**
	 * creates a mail daemon connecting to the specified server (and default port)
	 * without authentication
	 * 
	 * @param smtpserver URL of the smtp server
	 */
	public MailDaemon(String smtpserver) {
		this(smtpserver, -1);
	}

	/**
	 * creates a mail server connecting to the specified server and specified port
	 * 
	 * @param smtpserver URL of the smtp server
	 * @param port       SMTP Server port
	 */
	public MailDaemon(String smtpserver, int port) {
		this.smtpserver = smtpserver;
		this.port = port;
		iCalendarDateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmm'00Z'");
		iCalendarDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		logger.severe("Mail deamon created for smtpserver = " + smtpserver + ", port=" + port);
	}

	/**
	 * gets the best possible connection for the SMTP server (authenticated if
	 * possible)
	 * 
	 * @return the session
	 */
	private Session connectServer() {
		if (this.user != null)
			return connectAuthenticatedToServer();
		if (this.port > 0)
			return connectUnauthenticatedServer();
		return connectUnauthenticatedDefaultPort();
	}

	/**
	 * performs an unauthenticated default port connection to the SMTP server
	 * 
	 * @return the session
	 */
	private Session connectUnauthenticatedDefaultPort() {
		logger.warning("Building unauthenticated e-mail session to server '" + this.smtpserver + "' with default port");
		Properties props = new Properties();
		props.put("mail.smtp.host", this.smtpserver); // SMTP Host
		Session session = Session.getInstance(props, null);
		return session;
	}

	/**
	 * perfoms an unauthenticated connection to the SMTP server on the specified
	 * port
	 * 
	 * @return the session
	 */
	private Session connectUnauthenticatedServer() {
		logger.warning("Building unauthenticated e-mail session to server '" + this.smtpserver + "' with port '"
				+ this.port + "'");
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpserver); // SMTP Host
		props.put("mail.smtp.socketFactory.port", port); // SSL Port
		Session session = Session.getInstance(props, null);
		return session;
	}

	/**
	 * connects securely to the SMTP server (with authentication)
	 * 
	 * @return the SMTP session
	 */
	private Session connectAuthenticatedToServer() {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpserver); // SMTP Host
		props.put("mail.smtp.socketFactory.port", port); // SSL Port
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL Factory Class
		props.put("mail.smtp.auth", "true"); // Enabling SMTP Authentication
		props.put("mail.smtp.port", port); // SMTP Port

		Authenticator auth = new Authenticator() {
			// override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password);
			}
		};

		Session session = Session.getDefaultInstance(props, auth);
		return session;
	}

	@Override
	public void run() {

		while (true) {
			try {
				Systemattribute mailsending[] = Systemattribute.getobjectbynumber("S0.MAILSENDING");
				boolean hold = false;
				boolean discard = false;
				if (mailsending.length != 1) {
					logger.severe("System attribute 'MAILSENDING' not set");
				} else {
					Systemattribute actualmailsending = mailsending[0];
					if (actualmailsending.getValue().equals("HOLD"))
						hold = true;
					if (actualmailsending.getValue().equals("DISCARD"))
						discard = true;

				}

				Email[] openmails = Email.getallactive(

						QueryFilter.get(LifecycleQueryHelper.get().getStateSelectionQueryCondition(
								Email.getDefinition().getAlias(StoredobjectQueryHelper.maintablealiasforgetallactive),
								new ChoiceValue[] { EmailstatusChoiceDefinition.getChoiceReadytosend() },
								Email.getDefinition())));
				if (discard) {
					if (openmails != null)
						for (int i = 0; i < openmails.length; i++) {
							Email openmail = openmails[i];
							logger.info("Discarding e-mail " + openmail.getId() + " - " + openmail.getTitle());
							openmail.changestate(EmailstatusChoiceDefinition.getChoiceDiscarded());
						}
				}
				if ((!hold) && (!discard)) {
					if (openmails != null)
						if (openmails.length > 0) {
							Session session = connectServer();
							for (int i = 0; i < openmails.length; i++) {
								try {
									Email thismail = openmails[i];
									thismail.changestate(EmailstatusChoiceDefinition.getChoiceSending());
									Emailrecipient[] recipients = thismail
											.getallchildrenforowneremailforemailrecipient(null);
									String[] recipientstring = new String[recipients.length];
									for (int j = 0; j < recipients.length; j++)
										recipientstring[j] = recipients[j].getRecipient();
									String actionstring = "";

									if (thismail.getAction() != null)
										if (thismail.getAction().getStorageCode()
												.equals(BooleanChoiceDefinition.get().YES.getStorageCode()))
											actionstring = " You need to connect to the server "
													+ " to complete the action with a Gallium Client.";

									String fullbody = "";
									Systemattribute serverlabel = ServerSecurityBuffer.getUniqueInstance()
											.getSystemattribute("S0.SERVERLABEL");
									if (serverlabel != null)
										if (serverlabel.getValue() != null)
											if (serverlabel.getValue().length() > 0) {
												fullbody += "<font face=\"Arial, Helvetica, sans-serif\" size=\"2\" color=\"red\"><b>"
														+ serverlabel.getValue() + "</b></font><br/>";
											}
									boolean hasobject = false;

									if (thismail.getObjectid() != null)
										if (thismail.getObjectid().trim().length() > 0)
											hasobject = true;
									if (thismail.getObjectlabel() != null)
										if (thismail.getObjectlabel().trim().length() > 0)
											hasobject = true;
									boolean detailhtml = false;
									if (thismail.getDetailishtml() != null)
										if (thismail.getDetailishtml().getStorageCode() != null)
											if (thismail.getDetailishtml().getStorageCode()
													.equals(BooleanChoiceDefinition.get().YES.getStorageCode()))
												detailhtml = true;
									if (hasobject) {
										fullbody += "<font face=\"Arial, Helvetica, sans-serif\" size=\"2\">Dear Sir or Madam,<br><br>"
												+ thismail.getBodytext().replaceAll("<", "&lt;").replaceAll(">", "&gt;")
														.replaceAll("(\\r\\n|\\n|\\r)", "<br>")
												+ "<ul><li>" + thismail.getObjectid() + ": "
												+ thismail.getObjectlabel();
										if (thismail.getObjectdetail() != null)
											if (thismail.getObjectdetail().trim().length() > 0) {
												fullbody += "<ul><li>";
												if (detailhtml) {
													fullbody += thismail.getObjectdetail();

												} else {
													fullbody += thismail.getObjectdetail().replaceAll("<", "&lt;")
															.replaceAll(">", "&gt;")
															.replaceAll("(\\r\\n|\\n|\\r)", "<br>");

												}
												fullbody += "</ul></li>";
											}
										fullbody += "</li></ul>Yours faithfully,<br><br><i>This e-mail has been sent automatically from a <b>Gallium</b> Server."
												+ actionstring;

									} else {
										fullbody += "<font face=\"Arial, Helvetica, sans-serif\" size=\"2\">Dear Sir or Madam,<br><br>"
												+ thismail.getBodytext() + "<br>";
										fullbody += "Yours faithfully,<br><br><i>This e-mail has been sent automatically from a <b>Gallium</b> Server."
												+ actionstring;

									}
									String module = thismail.getModule();
									if (module != null)
										if (module.length() > 0) {
											SModule moduleobject = OLcServer.getServer().getModuleByName(module);
											if (moduleobject != null) {
												String modulemessage = moduleobject.getEmailMessage();
												if (modulemessage != null)
													if (modulemessage.length() > 0)
														fullbody += modulemessage;
											} else {
												logger.severe("Did not manage to find module '" + module
														+ "'while sending mail " + thismail.getId());
											}
										}
									fullbody += "</i></f>";
									// ---------------------- Decide if meeting -----------------------
									boolean meeting = false;
									if (thismail.getMeeting() != null)
										if (thismail.getMeeting().getStorageCode()
												.equals(BooleanChoiceDefinition.get().YES.getStorageCode()))
											meeting = true;

									if (meeting) {
										boolean cancel = false;
										if (thismail.getCancelation() != null)
											if (thismail.getMeeting().getStorageCode()
													.equals(BooleanChoiceDefinition.get().YES.getStorageCode()))
												cancel = true;
										// ----------------------------- Send meeting --------------------
										sendInvitation(session, recipientstring, thismail.getTitle(), fullbody,
												thismail.getSender(), thismail.getStarttime(), thismail.getEndtime(),
												thismail.getLocation(), thismail.getMeetinguid(), cancel);
										thismail.changestate(EmailstatusChoiceDefinition.getChoiceSent());

									} else {
										// ------------------------ Send simple mail -----------------------
										sendEmail(session, recipientstring, thismail.getTitle(), fullbody,
												thismail.getSender());
										thismail.changestate(EmailstatusChoiceDefinition.getChoiceSent());

									}

								} catch (Throwable t) {
									logger.severe("error in trying to send mail " + t.getMessage());
									StackTraceElement[] stacktrace = t.getStackTrace();
									for (int k = 0; k < stacktrace.length; k++) {
										logger.severe("   * " + stacktrace[k]);
									}
									try {

										openmails[i].changestate(EmailstatusChoiceDefinition.getChoiceError());

									} catch (RuntimeException e2) {
										logger.severe("Did not manage to set e-mail status in error to 'ERROR' "
												+ e2.getMessage());
									}
								}
							}
						}
				}
			} catch (Throwable t) {
				logger.severe("-- E-mail daemon: fatal error " + t.getMessage());
				StackTraceElement[] stacktrace = t.getStackTrace();
				for (int i = 0; i < stacktrace.length; i++) {
					logger.severe("   * " + stacktrace[i]);
				}
			}

			try {
				Thread.sleep(60000);
			} catch (InterruptedException e) {

			}

		}
	}

	/**
	 * sends invitation
	 * 
	 * @param session     session connection to the SMTP server
	 * @param toemails    list of recipient e-mails
	 * @param subject     invitation subject
	 * @param body        invitation start
	 * @param fromemail   user sending the invitation
	 * @param startdate   start date of the invitation
	 * @param enddate     end date of the invitation
	 * @param location    location of the invitation
	 * @param uid         unique id
	 * @param cancelation true if this is a cancelation
	 */
	private void sendInvitation(Session session, String[] toemails, String subject, String body, String fromemail,
			Date startdate, Date enddate, String location, String uid, boolean cancelation) {
		try {
			// prepare mail mime message
			MimetypesFileTypeMap mimetypes = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
			mimetypes.addMimeTypes("text/calendar ics ICS");
			// register the handling of text/calendar mime type
			MailcapCommandMap mailcap = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
			mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");

			MimeMessage msg = new MimeMessage(session);
			// set message headers

			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");
			InternetAddress fromemailaddress = new InternetAddress(fromemail);
			msg.setFrom(fromemailaddress);
			msg.setReplyTo(InternetAddress.parse(fromemail, false));
			msg.setSubject(subject, "UTF-8");
			msg.setSentDate(new Date());

			// set recipient

			InternetAddress[] recipients = new InternetAddress[toemails.length + 1];

			String attendeesinvcalendar = "";
			for (int i = 0; i < toemails.length; i++) {
				recipients[i] = new InternetAddress(toemails[i]);
				attendeesinvcalendar += "ATTENDEE;ROLE=REQ-PARTICIPANT;PARTSTAT=NEEDS-ACTION;RSVP=TRUE:MAILTO:"
						+ toemails[i] + "\n";
			}

			recipients[toemails.length] = fromemailaddress;
			msg.setRecipients(Message.RecipientType.TO, recipients);

			Multipart multipart = new MimeMultipart("alternative");
			// set body
			MimeBodyPart descriptionPart = new MimeBodyPart();
			descriptionPart.setContent(body, "text/html; charset=utf-8");
			multipart.addBodyPart(descriptionPart);

			// set invitation
			BodyPart calendarPart = new MimeBodyPart();

			String method = "METHOD:REQUEST\n";
			if (cancelation)
				method = "METHOD:CANCEL\n";

			String calendarContent = "BEGIN:VCALENDAR\n" + method + "PRODID: BCP - Meeting\n" + "VERSION:2.0\n"
					+ "BEGIN:VEVENT\n" + "DTSTAMP:" + iCalendarDateFormat.format(new Date()) + "\n" + "DTSTART:"
					+ iCalendarDateFormat.format(startdate) + "\n" + "DTEND:" + iCalendarDateFormat.format(enddate)
					+ "\n" + "SUMMARY:" + subject + "\n" + "UID:" + uid + "\n" + attendeesinvcalendar
					+ "ORGANIZER:MAILTO:" + fromemail + "\n" + "LOCATION:" + location + "\n" + "DESCRIPTION:" + subject
					+ "\n" + "SEQUENCE:0\n" + "PRIORITY:5\n" + "CLASS:PUBLIC\n" + "STATUS:CONFIRMED\n"
					+ "TRANSP:OPAQUE\n" + "BEGIN:VALARM\n" + "ACTION:DISPLAY\n" + "DESCRIPTION:REMINDER\n"
					+ "TRIGGER;RELATED=START:-PT00H15M00S\n" + "END:VALARM\n" + "END:VEVENT\n" + "END:VCALENDAR";

			calendarPart.addHeader("Content-Class", "urn:content-classes:calendarmessage");
			calendarPart.setContent(calendarContent, "text/calendar;method=CANCEL");
			multipart.addBodyPart(calendarPart);
			msg.setContent(multipart);
			logger.severe("Invitation is ready");
			Transport.send(msg);

			logger.severe("EMail Invitation Sent Successfully!! to " + attendeesinvcalendar);
		} catch (Exception e) {
			logger.severe(
					"--- Exception in sending invitation --- " + e.getClass().toString() + " - " + e.getMessage());
			if (e.getCause() != null)
				logger.severe(" cause  " + e.getCause().getClass().toString() + " - " + e.getCause().getMessage());
			throw new RuntimeException("email sending error " + e.getMessage() + " for server = server:"
					+ this.smtpserver + " - port:" + this.port + " - user:" + this.user);
		}

	}

	/**
	 * sends an e-mail
	 * 
	 * @param session   session connection to the SMTP server
	 * @param toemails  list of recipient e-mails
	 * @param subject   subject (title) of the e-mail
	 * @param body      body of the e-mail
	 * @param fromemail origin of the e-mail
	 */
	private void sendEmail(Session session, String[] toemails, String subject, String body, String fromemail) {
		try {
			MimeMessage msg = new MimeMessage(session);
			// set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress(fromemail));

			InternetAddress[] recipients = new InternetAddress[toemails.length];
			for (int i = 0; i < toemails.length; i++)
				recipients[i] = new InternetAddress(toemails[i]);

			msg.setReplyTo(InternetAddress.parse(fromemail, false));

			msg.setSubject(subject, "UTF-8");

			msg.setContent(body, "text/html; charset=utf-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, recipients);
			logger.severe("Message is ready");
			Transport.send(msg);

			logger.severe("EMail Sent Successfully!!");
		} catch (Exception e) {
			throw new RuntimeException("email sending error " + e.getMessage() + " for server = server:"
					+ this.smtpserver + " - port:" + this.port + " - user:" + this.user);
		}
	}
}
