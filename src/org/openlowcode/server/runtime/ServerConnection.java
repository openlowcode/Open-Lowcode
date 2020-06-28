/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.tools.misc.StringExtremityPrinter;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.trace.ExceptionLogger;
import org.openlowcode.OLcVersion;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Moduleusage;
import org.openlowcode.module.system.data.ModuleusageDefinition;
import org.openlowcode.module.system.page.SimpleloginPage;
import org.openlowcode.server.action.ActionExecution;
import org.openlowcode.server.action.SActionData;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LinkedtoparentQueryHelper;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.OrQueryCondition;
import org.openlowcode.server.data.storage.PersistenceGateway;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.security.ActionAuthorization;
import org.openlowcode.server.security.ActionObjectSecurityManager;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;

/**
 * The component in the sever managing connections with the clients
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ServerConnection
		extends
		Thread {
	private static Logger logger = Logger.getLogger(ServerConnection.class.getName());
	private Socket socket;
	private String ip; // ip of the computer connecting
	private OLcServer server;
	private long starttime;
	private boolean messageaudit;
	// true until there is a message received from server that Thread should stop
	private boolean alive = true;
	private int messagefound = 0;
	private Exception exceptionduringmessagestart = null;
	private static final int MESSAGESTART_WAITING = 0;
	private static final int MESSAGESTART_FOUND = 1;
	private static final int MESSAGESTART_ERROR = 2;

	/**
	 * Creates a server connection
	 * 
	 * @param socket       server socket
	 * @param server       main server class
	 * @param messageaudit true if messages should be audited in logs
	 */
	public ServerConnection(Socket socket, OLcServer server, boolean messageaudit) {
		this.socket = socket;
		this.ip = socket.getInetAddress().toString();
		this.server = server;
		this.messageaudit = messageaudit;
	}

	/**
	 * a link allowing to access directly most of actions
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	private class CLink {
		private String module;
		private String action;
		private ArrayList<String> attributenames;
		private ArrayList<String> attributevalues;
		private int index = 0;
		private boolean parsingover = false;
		private final char STRING_DELIMITER = '"';

		/**
		 * @return the module of the action to call
		 */
		public String getModule() {
			return this.module;
		}

		/**
		 * @return the name of the action
		 */
		public String getAction() {
			return this.action;
		}

		/**
		 * @return the number of a attributes of the action
		 */
		public int getAttributeNr() {
			return attributenames.size();
		}

		/**
		 * the name of attribute at index specified
		 * 
		 * @param index index between 0 (included) and getAttributeNr() (excluded)
		 * @return the name of the attribute at the specified index
		 */
		public String getAttributeName(int index) {
			return attributenames.get(index);
		}

		/**
		 * the attribute value at index specified
		 * 
		 * @param index index between 0 (included) and getAttributeNr() (excluded)
		 * @return the value of the attribute at the specified index
		 */
		public String getAttributeValue(int index) {
			return attributevalues.get(index);
		}

		/**
		 * creates a link
		 * 
		 * @param address the address of the action
		 */
		public CLink(String address) {
			logger.info("processing CLink address " + address);
			this.module = null;

			this.action = null;
			attributenames = new ArrayList<String>();
			attributevalues = new ArrayList<String>();
			if (address != null)
				if (address.length() > 0)
					parseAddress(address);
		}

		/***
		 * parses the address given and throws an exception if the address is invalid
		 * 
		 * @param address the address to parse
		 */
		public void parseAddress(String address) {
			this.module = parseToken(address).toUpperCase();
			logger.info("identified module = " + module);
			if (!parsingover) {
				if (address.charAt(index) != '.')
					throw new RuntimeException("Expecting dot '.' after module name " + module);
				index++;
				String secondtoken = parseToken(address).toUpperCase();
				if (secondtoken.compareTo("PAGE") != 0) {
					this.action = secondtoken;
					logger.info("Identified action " + this.action);
					if (!parsingover) {
						if (address.charAt(index) != ':')
							throw new RuntimeException("Expecting column ':' after module name " + module);
						index++;
						boolean first = true;
						while (!parsingover) {
							logger.info("parsing attribute, index = " + index);
							if (!first) {
								if (address.charAt(index) != ',')
									throw new RuntimeException("Expecting comma ',' after module name " + module
											+ ", currentindex = " + index);
								index++;
							} else {
								first = false;
							}
							String attributename = parseToken(address);

							if (address.charAt(index) != '=')
								throw new RuntimeException("Expecting dot '.' after module name " + module);
							index++;
							if (address.charAt(index) != STRING_DELIMITER)
								throw new RuntimeException("Expecting '\"' for attribute");
							index++;
							String attributepayload = parseStringToken(address);
							logger.info("identified  attribute " + attributename + " value : " + attributepayload);
							attributenames.add(attributename);
							attributevalues.add(attributepayload);
							if (index >= address.length())
								parsingover = true;
						}

					}

				} else {
					if (address.charAt(index) != '.')
						throw new RuntimeException("Expecting dot '.' after module name " + module);
					index++;
					@SuppressWarnings("unused")
					String thirdtoken = parseToken(address).toUpperCase();
					// right now, third token is unused
					if (!parsingover)
						throw new RuntimeException("Expected address is finished after page for address " + address);
				}

			}
		}

		/**
		 * a utility class to split a string by delimiter
		 * 
		 * @param address the string to parse
		 * @return the first token
		 */
		public String parseStringToken(String address) {
			StringBuffer content = new StringBuffer();
			boolean laststringdelimiter = false;
			int currentcharacter = address.charAt(index);
			while (currentcharacter != -1) {
				char character = (char) currentcharacter;
				// no '"' as last string -- normal process
				if (!laststringdelimiter)
					if (currentcharacter != STRING_DELIMITER)
						content.append(character);

				if (laststringdelimiter)
					if (currentcharacter != STRING_DELIMITER) {

						return content.toString();
					}
				if (currentcharacter == STRING_DELIMITER) {
					if (laststringdelimiter) {
						laststringdelimiter = false;
						content.append(character);
					} else {
						laststringdelimiter = true;
					}
				}
				index++;
				if (index >= address.length()) {
					currentcharacter = -1;
				} else {
					currentcharacter = address.charAt(index);
				}

			}

			if (!laststringdelimiter)
				throw new RuntimeException("End of File reached while parsing string content for address " + address);
			return content.toString();
		}

		/**
		 * parses a token
		 * 
		 * @param address
		 * @return the content of the next token
		 */
		public String parseToken(String address) {
			char currentchar = address.charAt(index);
			if (MessageSimpleReader.firstcharStringToken.indexOf(currentchar) == -1)
				throw new RuntimeException("Address does not start with a string token " + address);
			StringBuffer stringbuffer = new StringBuffer("");
			while (MessageSimpleReader.followingcharStringToken.indexOf(currentchar) != -1) {
				stringbuffer.append(currentchar);
				index++;
				if (index < address.length()) {
					currentchar = address.charAt(index);
				} else {
					parsingover = true;
					break;
				}
			}
			return stringbuffer.toString();
		}
	}

	/**
	 * launches the action corresponding to the CLink, or throw a RuntimeException
	 * else
	 * 
	 * @param address address to process
	 * @param writer  message writer for sending the result of the action
	 * @throws IOException if any issue is encountered sending the action to the
	 *                     client
	 */
	public void processCLink(String address, MessageBufferedWriter writer) throws IOException {
		logger.info("received CLINK request for " + address);
		CLink parsedlinkinfo = new CLink(address);
		ActionExecution action = null;
		SActionData actiondata = new SActionData();
		if (parsedlinkinfo.module == null) {
			action = OLcServer.getServer().getMainmodule().getActionForDefaultPage();
		} else {
			SModule module = OLcServer.getServer().getModuleByName(parsedlinkinfo.getModule());
			if (module == null)
				throw new RuntimeException("Module specified does not exist " + parsedlinkinfo.getModule());
			if (parsedlinkinfo.getAction() == null) {
				action = module.getActionForDefaultPage();
			} else {
				action = module.getAction(parsedlinkinfo.getAction());

				for (int i = 0; i < parsedlinkinfo.getAttributeNr(); i++) {
					actiondata.addCLinkAttribute(parsedlinkinfo.getAttributeName(i),
							parsedlinkinfo.getAttributeValue(i));
				}
			}

		}
		if (action == null)
			throw new RuntimeException("No action was found for CLink address = " + address);

		DataObjectId<Appuser> userid = server.getSecuritymanager().isValidSession(ip, server.getCidForConnection());
		if (userid == null) {
			setLoginWithContextAction(action, actiondata, writer);
		} else {
			executeAction(userid, action, actiondata, writer);
		}

	}

	@Override
	public void run() {
		try {
			server.setIpForConnection(ip);

			starttime = System.currentTimeMillis();
			InputStreamReader inputstreamreader = new InputStreamReader(socket.getInputStream(),
					Charset.forName("UTF-8"));
			MessageSimpleReader reader = new MessageSimpleReader(new BufferedReader(inputstreamreader));
			OutputStreamWriter outputstreamwriter = new OutputStreamWriter(socket.getOutputStream(),
					Charset.forName("UTF-8"));
			MessageBufferedWriter writer = new MessageBufferedWriter(new BufferedWriter(outputstreamwriter),
					messageaudit);
			logger.info("Received new connection from " + ip);
			logger.info("audit of connection encoding : inbound " + inputstreamreader.getEncoding() + ", outbound = "
					+ outputstreamwriter.getEncoding());

			labelloop: while ((socket.isConnected()) && (!socket.isClosed())) {
				// read one CML message
				try {

					// if interruption comes at this point, should stop
					messagefound = MESSAGESTART_WAITING;
					exceptionduringmessagestart = null;
					Thread checkstartmessage = new Thread() {

						@Override
						public void run() {
							try {
								reader.returnNextMessageStart();
								messagefound = MESSAGESTART_FOUND;
							} catch (OLcRemoteException | IOException e) {
								exceptionduringmessagestart = e;
								messagefound = MESSAGESTART_ERROR;
							}

						}

					};
					checkstartmessage.start();
					while ((messagefound == MESSAGESTART_WAITING) & (alive)) {
						Thread.sleep(3);
					}

					if (!alive) {
						logger.severe("Shutdown thread " + this.getId() + " as got request from server");
						break labelloop;
					}

					if ((messagefound == MESSAGESTART_ERROR) && (exceptionduringmessagestart instanceof IOException)) {
						logger.warning("Found exception " + exceptionduringmessagestart + " relaunching ");
						for (int i = 0; i < exceptionduringmessagestart.getStackTrace().length; i++)
							logger.warning("   " + exceptionduringmessagestart.getStackTrace()[i]);
						// special case detecting end of connection
						// if (exceptionduringmessagestart.getErrorCode()==1003)
						break labelloop;
						// throw exceptionduringmessagestart;
					}

					boolean majorquerytreated = false;
					String majorquery = reader.returnNextStartStructure();
					if (majorquery.compareTo("REQUEST") == 0) {
						majorquerytreated = true;
						boolean requesttreated = false;
						// reads optional string sessionid attribute
						MessageElement element = reader.getNextElement();

						if (element instanceof MessageStringField) {
							// detected session id
							MessageStringField cid = (MessageStringField) element;
							if (cid.getFieldName().compareTo("CID") != 0)
								throw new RuntimeException(
										"expected CID as first attribute of request, got " + cid.getFieldcontent());
							server.setCidForConnection(cid.getFieldcontent());
							// get next
							element = reader.getNextElement();
						} else {
							// if no session id, generate a new one
							server.setCidForConnection(server.generateCid());
						}
						if (!(element instanceof MessageStartStructure))
							throw new RuntimeException("Expecting start structure, got " + element);
						String minorquery = ((MessageStartStructure) element).getStructurename();

						if (minorquery.compareTo("CLINK") == 0) {
							requesttreated = true;
							String address = reader.returnNextStringField("VALUE");

							reader.returnNextEndStructure("CLINK");
							String clientversion = reader.returnNextStringField("CVR");
							if (OLcVersion.compareWithClientVersion(clientversion) > 0) {
								/*
								 * SPage clientversionerrorpage =
								 * ClientversionerrorAction.get().executeAndShowPage(clientversion);
								 * this.sendPage(clientversionerrorpage, writer, null);
								 */
								if (clientversion.compareTo("0.32") <= 0)
									throw new RuntimeException(
											"Automatic client upgrade not supported for version 0.32 of client and below.");
								this.sendClientVersionError(writer, clientversion);
							} else {
								processCLink(address, writer);
							}
						} // ------------------- END OF CLINK
						if (minorquery.compareTo("ACTION") == 0) {
							requesttreated = true;
							String actionname = reader.returnNextStringField("NAME");
							String modulename = reader.returnNextStringField("MODULE");
							reader.startStructureArray("PAGBUF");
							// new v0.69 -- read Buffer Spec ---
							ArrayList<PageBufferSpec> clientpagesinbuffer = new ArrayList<PageBufferSpec>();
							while (reader.structureArrayHasNextElement("PAGBUF")) {
								PageBufferSpec bufferspec = new PageBufferSpec(reader);
								clientpagesinbuffer.add(bufferspec);
							}
							// ------------ read Buffer Spec ---
							SActionData actiondata = new SActionData(reader);

							SModule module = server.getModuleByName(modulename);

							if (module == null)
								throw new RuntimeException(
										String.format("Module unknown %s for action request %s coming from ip",
												modulename, actionname, ip));
							ActionExecution action = module.getAction(actionname);
							if (action == null)
								throw new RuntimeException(String.format(
										"In module %s,  action unknown %s coming from ip", modulename, actionname, ip));

							DataObjectId<Appuser> userid = server.getSecuritymanager().isValidSession(ip,
									server.getCidForConnection());
							if (actionname.compareTo("LOGIN") == 0) {
								try {
									SPage answerpage = action.executeActionFromGUI(actiondata);
									if (answerpage != null) {
										this.sendPage(answerpage, writer, null, null);
										logger.info("sent page " + answerpage.getName() + "for action " + actionname
												+ " to to ip = " + ip + ", for unconnected session ");
									}

									if (answerpage == null) {
										// Start of logic to go to previously requested page

										DataElt contextaction = actiondata.lookupAttributeOnName("CONTEXTACTION");
										if (contextaction == null)
											throw new RuntimeException(
													"was expecting context data from login action, got nothing");
										if (!(contextaction instanceof TextDataElt))
											throw new RuntimeException(
													"was expecting context data from login action to be text, got "
															+ contextaction.getClass().getCanonicalName());
										TextDataElt contextactiontext = (TextDataElt) contextaction;
										String actionmessage = contextactiontext.getPayload();
										MessageSimpleReader contactactionreader = new MessageSimpleReader(
												new StringReader(actionmessage));
										contactactionreader.returnNextMessageStart();
										contactactionreader.returnNextStartStructure("CONTEXT");
										String embeddedactionname = contactactionreader.returnNextStringField("NAME");
										String embeddedmodulename = contactactionreader.returnNextStringField("MODULE");
										SActionData embeddedactiondata = new SActionData(contactactionreader);
										contactactionreader.returnNextEndStructure("CONTEXT");
										contactactionreader.returnNextEndMessage();
										contactactionreader.close();
										// here no need to check that the user exists. However, gets it
										DataObjectId<Appuser> embeddedactionuserid = server.getSecuritymanager()
												.isValidSession(ip, server.getCidForConnection());

										SModule embeddedmodule = server.getModuleByName(embeddedmodulename);

										if (embeddedmodule == null)
											throw new RuntimeException(String.format(
													"Module unknown %s for action request %s coming from ip embedded in login",
													embeddedmodulename, actionname, ip));
										ActionExecution embeddedaction = embeddedmodule.getAction(embeddedactionname);
										if (embeddedaction == null)
											throw new RuntimeException(String.format(
													"In module %s,  action unknown %s coming from ip embedded in login ",
													embeddedmodulename, embeddedmodulename, ip));
										executeAction(embeddedactionuserid, embeddedaction, embeddedactiondata, writer);

									}
								} catch (Throwable t) {
									treatThrowable(t, actionname, userid, writer);
								}

							} else {
								if (userid != null) {
									executeAction(userid, action, actiondata, writer, clientpagesinbuffer);

								} else {
									setLoginWithContextAction(action, actiondata, writer);

								}
							}
							reader.returnNextEndStructure("ACTION"); // close REQUEST SECOND LEVEL
						}

						if (minorquery.compareTo("INLINEACTION") == 0) {
							requesttreated = true;

							String actionname = reader.returnNextStringField("NAME");
							String modulename = reader.returnNextStringField("MODULE");
							SActionData actiondata = new SActionData(reader);
							SModule module = server.getModuleByName(modulename);
							if (module == null)
								throw new RuntimeException(
										String.format("Module unknown %s for action request %s coming from ip",
												modulename, actionname, ip));
							ActionExecution action = module.getAction(actionname);
							if (action == null)
								throw new RuntimeException(String.format(
										"In module %s,  action unknown %s coming from ip", modulename, actionname, ip));
							DataObjectId<Appuser> userid = server.getSecuritymanager().isValidSession(ip,
									server.getCidForConnection());
							if (userid != null) {
								try {
									SecurityBuffer buffer = new SecurityBuffer();
									ActionAuthorization thisactionauthorization = isAuthorized(action, actiondata,
											buffer);
									if (thisactionauthorization
											.getAuthorization() != ActionAuthorization.NOT_AUTHORIZED) {
										long requeststart = System.currentTimeMillis();
										logger.info("executing inline action " + modulename + "." + actionname);
										try {
											logAction(action);
											OLcServer.getServer().resetTriggersList(); // reset remote trigger list for
																						// thread
											SPageData inlineanswer;
											if (thisactionauthorization
													.getAuthorization() == ActionAuthorization.AUTHORIZED) {
												inlineanswer = action.executeInlineAction(actiondata);
											} else {
												// potentially authorized, action is executed with a data filter for the
												// main query
												inlineanswer = action.executeInlineAction(actiondata,
														thisactionauthorization.getAdditionalconditiongenerator());
											}
											OLcServer.getServer().executeTriggerList(); // execute remote trigger list
																						// for thread
											long requestend = System.currentTimeMillis();
											logger.info("executed inline action " + modulename + "." + actionname
													+ ", execution time = " + (requestend - requeststart) + "ms");
											reader.returnNextEndStructure("INLINEACTION");
											this.sendInlineData(inlineanswer, writer);
											logger.info("sent inlinedata from page " + action.getName() + "to to ip = "
													+ ip + ", for session of user " + userid.getId());
										} catch (Throwable t) {
											treatThrowable(t, actionname, userid, writer);
										}

									} else {
										writer.sendMessageError(9999,
												"Not Authorized for the action " + action.getName());

									}
								} catch (Exception e) {
									logger.warning("------------------ Error in inline action -------------- ");
									treatThrowable(e, actionname, userid, writer);
								}

							} else {
								SPage loginpage = new SimpleloginPage("");
								this.sendPage(loginpage, writer, null, null);
								logger.info("sent login page to to ip = " + ip + " for inlineaction " + actionname);
							}

						}

						// ----------------------------------------------------------
						// END OF INLINE ACTION
						// ----------------------------------------------------------

						if (!requesttreated)
							throw new RuntimeException(String.format("The request type is invalid :" + minorquery, ip));

						reader.returnNextEndStructure("REQUEST");
					}
					if (majorquery.equals("DOWNLOADCLIENT")) {
						majorquerytreated = true;
						reader.returnNextEndStructure("DOWNLOADCLIENT");
						reader.returnNextEndMessage();
						logger.severe("Starting treating download client request");
						writer.startNewMessage();
						File clienttodownload = new File("." + File.separator + "client" + File.separator
								+ OLcServer.getServer().getClientJar());
						if (!clienttodownload.exists()) {
							writer.sendMessageError(9999, "Client file missing on server " + clienttodownload.getAbsolutePath()
									+ ". Please contact technical support.");
							logger.severe("Download client requested download failed, file " + clienttodownload.getAbsolutePath()
									+ " does not exists");
						} else {
							byte[] filecontent = new byte[(int) clienttodownload.length()];
							FileInputStream fisfordownload = new FileInputStream(clienttodownload);
							fisfordownload.read(filecontent);
							fisfordownload.close();
							SFile filetodownload = new SFile(OLcServer.getServer().getClientJar(), filecontent);
							writer.startStructure("NEWCLIENTJAR");
							writer.addLongBinaryField("JAR", filetodownload);
							writer.endStructure("NEWCLIENTJAR");
							writer.endMessage();
							writer.flushMessage();
						}
						logger.severe("Download client requested download finished");

					}
					if (majorquery.equals("SHUTDOWN")) {
						if (socket.getInetAddress().isAnyLocalAddress()
								|| socket.getInetAddress().isLoopbackAddress()) {
							logger.severe("------------------------------------------------------------");
							logger.severe("       S H U T D O W N . R E Q U E S T . R E C E I V E D");
							logger.severe("------------------------------------------------------------");

							reader.returnNextEndStructure("SHUTDOWN");
							reader.returnNextEndMessage();
							// send message to all connections to stop current action and loop
							OLcServer.getServer().sendShutdownToAllConnections();

							// sends a message to client requesting shutdown
							try {
								writer.startNewMessage();
								writer.startStructure("SHUTDOWNOK");
								writer.endStructure("SHUTDOWNOK");
								writer.endMessage();
								writer.flushMessage();

								// wait 200ms
								Thread.sleep(200);
							} catch (Throwable t) {
								logger.severe("Exception while sending shutdownOK message to client " + t.getMessage());
								for (int i = 0; i < t.getStackTrace().length; i++) {
									logger.severe(t.getStackTrace()[i].toString());
								}
							}
							System.exit(0);
						} else {
							throw new RuntimeException(
									"Shutdown is only authorized from local, but request received from "
											+ socket.getInetAddress().toString());
						}
						majorquerytreated = true;

					}

					if (!majorquerytreated) {
						throw new RuntimeException(String.format("The majorquery type is invalid :" + majorquery, ip));
					}

					reader.returnNextEndMessage(); // CLOSE MESSAGE
				} catch (Throwable t) {
					boolean disconnectionerror = false;
					if (t instanceof IOException)
						disconnectionerror = true;

					if (disconnectionerror) {
						long endtime = System.currentTimeMillis();
						long connectiontime = (endtime - starttime) / 1000;
						logger.fine("normal client disconnection between messages, total connected time = "
								+ connectiontime + "s");
					} else {

						logger.severe("Exception " + t.getMessage());
						for (int i = 0; i < t.getStackTrace().length; i++) {
							logger.severe(t.getStackTrace()[i].toString());
						}
						treatThrowable(t, "SYSTEM", null, writer);
					}
					boolean isrecoverableerror = false;
					if (t instanceof OLcRemoteException) {
						OLcRemoteException exception = (OLcRemoteException) t;
						if (exception.getRemoteErrorCode() == 1)
							isrecoverableerror = true;
					}
					if (!isrecoverableerror)
						socket.close();
				}
			}

		} catch (Exception e) {
			logger.severe("received exception [" + e.getClass().getCanonicalName() + "] in connection listening thread "
					+ e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe(" at " + e.getStackTrace()[i].toString());

		}
		OLcServer.getServer().reportThreadFinished(this.getId());
	}

	/**
	 * performs a login, keeping the action context. This is typically used after
	 * time-out when user is already in the application
	 * 
	 * @param action     action to execute
	 * @param actiondata data to execute
	 * @param writer     writer to provide output
	 * @throws IOException if any communication issue is encountered
	 */
	public void setLoginWithContextAction(ActionExecution action, SActionData actiondata, MessageBufferedWriter writer)
			throws IOException {
		try {

			String actionname = action.getName();
			if (action.getParent() == null) {
				throw new RuntimeException("Action " + action.getName() + " does not have a parent module specified ");
			}
			String modulename = action.getParent().getName();

			logger.info("unconnected request from ip = " + ip + " for action " + actionname);
			// Note - send authentication message for CLINK
			// Note - add attribute to send the old message / query as part of the
			// loginpage. Although it is a hack, propose
			// to transport it as a text
			StringWriter stringwriter = new StringWriter();
			MessageBufferedWriter embeddedactionwriter = new MessageBufferedWriter(
					new BufferedWriter(stringwriter, 100000), false);
			embeddedactionwriter.startNewMessage();
			embeddedactionwriter.startStructure("CONTEXT");
			embeddedactionwriter.addStringField("NAME", actionname);
			embeddedactionwriter.addStringField("MODULE", modulename);
			embeddedactionwriter.startStructure("ATTRIBUTES");
			for (int i = 0; i < actiondata.size(); i++) {
				embeddedactionwriter.startStructure("ATTRIBUTE");
				actiondata.getAttribute(i).writeToMessage(embeddedactionwriter, null);
				embeddedactionwriter.endStructure("ATTRIBUTE");
			}
			embeddedactionwriter.endStructure("ATTRIBUTES");
			embeddedactionwriter.endStructure("CONTEXT");
			embeddedactionwriter.endMessage();

			SPage loginpage = new SimpleloginPage(stringwriter.toString());
			this.sendPage(loginpage, writer, null, null);
			logger.info("sent login page to to ip = " + ip + " for action " + actionname);
		} catch (Throwable t) {
			treatThrowable(t, "LOGIN", null, writer);

		}
	}

	private void treatThrowable(
			Throwable e,
			String actionname,
			DataObjectId<Appuser> userid,
			MessageBufferedWriter writer) throws IOException {
		// send error message instead of sending page. Else, it is the same
		// note: no possibility now to send error properly if exception while sending
		// page, need clean mechanism like buffer that flushes
		// original page only when sure that no error. May have significant impact on
		// performance.
		PersistenceGateway.releaseForThread();
		String usertrace = "unauthenticated";
		if (userid != null)
			usertrace = userid.getId();
		logger.severe("Exception while treating  action " + actionname + " to to ip = " + ip + ", for session of user "
				+ usertrace);
		ExceptionLogger.setInLogs(e, logger);
		writer.sendMessageError(1, e.getClass().toString() + " - " + e.getMessage());
		writer.flushMessage();
		logger.severe("Sent error message to client [CLASS:" + e.getClass().toString() + ":" + e.getMessage() + "]");

	}

	private void executeAction(
			DataObjectId<Appuser> userid,
			ActionExecution action,
			SActionData actiondata,
			MessageBufferedWriter writer) throws IOException {
		executeAction(userid, action, actiondata, writer, null);
	}

	private void executeAction(
			DataObjectId<Appuser> userid,
			ActionExecution action,
			SActionData actiondata,
			MessageBufferedWriter writer,
			ArrayList<PageBufferSpec> clientpagesinbuffer) throws IOException {
		String actionname = action.getName();
		SecurityBuffer buffer = new SecurityBuffer();
		ActionAuthorization thisactionauthorization = isAuthorized(action, actiondata, buffer);
		if (thisactionauthorization.getAuthorization() != ActionAuthorization.NOT_AUTHORIZED) {
			if (action.getParentModule().IsRestriction()) {
				writer.sendMessageError(9999, "You should connect through OTP to access this action " + action.getName());
			} else 
			try { // this is too precisely located. Should catch exception wider
				logAction(action);
				OLcServer.getServer().resetTriggersList(); // reset remote server list for thread;
				SPage answerpage;
				if (thisactionauthorization.getAuthorization() == ActionAuthorization.AUTHORIZED) {
					answerpage = action.executeActionFromGUI(actiondata);
				} else {
					answerpage = action.executeActionFromGUI(actiondata,
							thisactionauthorization.getAdditionalconditiongenerator());
				}
				if (answerpage == null)
					throw new RuntimeException("Action " + action.getName() + " / " + action.getClass().getName()
							+ " brought back a null page");
				this.sendPage(answerpage, writer, buffer, clientpagesinbuffer);

				logger.info("sent page " + answerpage.getName() + "for action " + actionname + " to to ip = " + ip
						+ ", for session of user " + userid.getId());
			} catch (Throwable e) {
				treatThrowable(e, actionname, userid, writer);

			}
		} else {
			writer.sendMessageError(9999, "Not Authorized for the action " + action.getName());

		}
	}

	private void sendInlineData(SPageData inlineanswer, MessageBufferedWriter writer) throws IOException {
		if (inlineanswer == null)
			logger.severe("page was not found");
		writer.startNewMessage();
		writer.startStructure("INLINEDATA");
		inlineanswer.writeToCML(writer);
		writer.endStructure("INLINEDATA");
		writer.endMessage();
		writer.flushMessage();

	}

	/**
	 * sends a client server version mismatch
	 * 
	 * @param writer        writer to communicate to the client
	 * @param clientversion client version
	 * @throws IOException if any transmission error is encountered
	 */
	public void sendClientVersionError(MessageWriter writer, String clientversion) throws IOException {
		writer.startNewMessage();
		writer.startStructure("CLIENTUPDATE");
		writer.startStructure("RQSATRS");
		writeRequestAttribute(writer,"CLV", clientversion);
		writeRequestAttribute(writer,"SVV", OLcVersion.referenceclientversion);		
		writer.endStructure("RQSATRS");

		writer.addDateField("SVD", OLcVersion.versiondate);
		writer.endStructure("CLIENTUPDATE");
		writer.endMessage();
		writer.flushMessage();
	}
	public void writeRequestAttribute(MessageWriter writer,String name,String attribute) throws IOException {
		writer.startStructure("RQSATR");
		writer.addStringField("NAM",name);
		writer.addStringField("VAL",attribute);
		writer.endStructure("RQSATR");
	}
	/**
	 * sends a page
	 * 
	 * @param page                page to send
	 * @param writer              writer to communicate back to the client
	 * @param buffer              security buffer
	 * @param clientpagesinbuffer client pages already on the client
	 * @throws IOException if any communication issue is encountered
	 */
	public void sendPage(
			SPage page,
			MessageWriter writer,
			SecurityBuffer buffer,
			ArrayList<PageBufferSpec> clientpagesinbuffer) throws IOException {
		if (page == null)
			logger.severe("page was not found");
		logger.info("------------------------ starting transmission of page " + page.getName() + " -------------- ");
		writer.startNewMessage();
		writer.startStructure("DISPLAYPAGE");
		String cid = server.getCidForConnection();
		writer.startStructure("RQSATRS");
		writeRequestAttribute(writer,"CID",cid);
		String locale = "";
		if (cid != null)
			if (cid.length() > 0) {
				Appuser user = server.getCurrentUser();
				if (user != null)
					if (user.getPreflang() != null)
						locale = user.getPreflang().getStorageCode();
			}
		writeRequestAttribute(writer,"LCL", locale);
		writeRequestAttribute(writer,"NAME", page.getName());
		if (page.hasAdress())
			writeRequestAttribute(writer,"ADDRESS", page.getAddress());
		writeRequestAttribute(writer,"TITLE", page.getTitle());
		boolean serverhasotp = false; 
		if (OLcServer.getServer().getOTPSecurityManager()!=null) serverhasotp=true;
		boolean userhasconfirmedotp=false;
		Boolean otpthread = OLcServer.getServer().getOTPForConnection();
		if (otpthread!=null) if (otpthread.booleanValue()) userhasconfirmedotp=true;
		String otpstatus = "NONE";
		if (serverhasotp) {
			if (userhasconfirmedotp) otpstatus = "VALID";
			if (!userhasconfirmedotp) otpstatus = "INVALID";
		}
		writeRequestAttribute(writer,"OTPSTATUS",otpstatus);
		writer.endStructure("RQSATRS");
		

		writer.startStructure("CONTENT");
		SPageData data = page.getAllFinalPageAttributes();
		String pageblank = null;
		PageBufferSpec validbufferspec = null;
		if (clientpagesinbuffer != null) {
			StringWriter pageblankstringbuffer = new StringWriter();
			MessageWriter pageblankwriter = new MessageBufferedWriter(new BufferedWriter(pageblankstringbuffer), false);
			pageblankwriter.startNewMessage();
			page.getFinalContent().WriteToCDL(pageblankwriter, data, buffer);
			page.resetPath();
			pageblankwriter.endMessage();
			pageblank = pageblankstringbuffer.toString();
			logger.fine("---- wrote page blank length=" + pageblank.length() + ", hashcode = " + pageblank.hashCode());
			logger.fine(StringExtremityPrinter.printextremity(pageblank, 15));
			logger.fine("--------------------------------------------");

			for (int i = 0; i < clientpagesinbuffer.size(); i++) {
				PageBufferSpec thispagebuffer = clientpagesinbuffer.get(i);
				if ((thispagebuffer.getContentHashcode() == pageblank.hashCode())
						&& (thispagebuffer.getSize() == pageblank.length())) {
					validbufferspec = thispagebuffer;
					logger.fine(
							"    ** found match for page hash=" + pageblank.hashCode() + ",size=" + pageblank.length());
				} else {
					logger.fine("    ** no match, size = " + thispagebuffer.getSize() + " - " + pageblank.length()
							+ ", hashcode  = " + thispagebuffer.getContentHashcode() + " - " + pageblank.hashCode());
				}
			}
		}
		writer.addBooleanField("CTB", (validbufferspec != null));
		if (validbufferspec != null) {
			logger.fine("Sends valud buffer spec hashcode = " + validbufferspec.getContentHashcode() + ", size="
					+ validbufferspec.getSize());
			writer.addIntegerField("HSH", validbufferspec.getContentHashcode());
			writer.addIntegerField("SIZ", validbufferspec.getSize());

		} else {

			page.getFinalContent().WriteToCDL(writer, data, buffer);
		}
		writer.endStructure("CONTENT");
		data.writeToCML(writer);
		writer.endStructure("DISPLAYPAGE");
		writer.endMessage();
		writer.flushMessage();

	}

	/**
	 * checks if the action is authorized, limited access to pesistence
	 * 
	 * @param action action to check
	 * @param object context object
	 * @return the authorization
	 */
	public static <E extends DataObject<E>> ActionAuthorization isAuthorized(ActionExecution action, E object) {
		ActionSecurityManager[] securitymanagers = action.getActionSecurityManager();
		if (securitymanagers == null)
			return new ActionAuthorization(ActionAuthorization.NOT_AUTHORIZED);
		// first check security managers without object data

		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			if (!thismanager.queryObjectData())
				if (!(thismanager.filterObjectData())) {
					boolean authorized = thismanager.isAuthorizedForCurrentUser("Action " + action.getName(), null,
							null);
					if (authorized)
						return new ActionAuthorization(ActionAuthorization.AUTHORIZED);
				}
		}

		// second check security managers with object data, case input argument
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];

			if (thismanager.queryObjectData()) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				ActionObjectSecurityManager<E> objectsecuritymanager = (ActionObjectSecurityManager) thismanager;
				boolean authorized = objectsecuritymanager
						.isAuthorizedForCurrentUser("ACTION FOR EXECUTION " + action.getName(), object);
				if (authorized)
					return new ActionAuthorization(ActionAuthorization.AUTHORIZED);
			}
		}
		return new ActionAuthorization(ActionAuthorization.NOT_AUTHORIZED);
	}

	/**
	 * checks if the action is authorized, getting all the action data
	 * 
	 * 
	 * @param action     action
	 * @param actiondata all action data
	 * @param buffer     security buffer
	 * @return the action authorization
	 */
	public static ActionAuthorization isAuthorized(
			ActionExecution action,
			SActionData actiondata,
			SecurityBuffer buffer) {
		ActionSecurityManager[] securitymanagers = action.getActionSecurityManager();
		if (securitymanagers == null)
			return new ActionAuthorization(ActionAuthorization.NOT_AUTHORIZED);
		// first check security managers without object data
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			if (!thismanager.queryObjectData())
				if (!(thismanager.filterObjectData())) {
					boolean authorized = thismanager.isAuthorizedForCurrentUser("Action " + action.getName(),
							actiondata, buffer);
					if (authorized)
						return new ActionAuthorization(ActionAuthorization.AUTHORIZED);
				}
		}
		// second check security managers with object data, case input argument
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			if (thismanager.queryObjectData()) {
				boolean authorized = thismanager.isAuthorizedForCurrentUser("ACTION FOR EXECUTION " + action.getName(),
						actiondata, buffer);
				if (authorized)
					return new ActionAuthorization(ActionAuthorization.AUTHORIZED);
			}
		}

		// third check security managers with object data, case output argument (need to
		// cumulate filters)
		ArrayList<
				Function<TableAlias, QueryFilter>> filterfunctions = new ArrayList<Function<TableAlias, QueryFilter>>();
		for (int i = 0; i < securitymanagers.length; i++) {
			ActionSecurityManager thismanager = securitymanagers[i];
			if (thismanager.filterObjectData()) {
				logger.severe("    Action authorization security manager with filter data exits");
				Function<TableAlias, QueryFilter> function = thismanager.getOutputFilterCondition();
				logger.severe("    function = " + function);

				if (function == null)
					return new ActionAuthorization(ActionAuthorization.AUTHORIZED);
				filterfunctions.add(function);
			}
		}
		// found filter functions
		if (filterfunctions.size() > 0) {
			Function<TableAlias, QueryFilter> cumulatedfunction = new Function<TableAlias, QueryFilter>() {

				@Override
				public QueryFilter apply(TableAlias alias) {
					if (filterfunctions.size() == 1)
						return filterfunctions.get(0).apply(alias);
					QueryFilter firstconditionset = filterfunctions.get(0).apply(alias);
					QueryCondition finalcondition = (firstconditionset != null ? firstconditionset.getCondition()
							: null);
					ArrayList<TableAlias> extraaliases = new ArrayList<TableAlias>();
					if (firstconditionset != null)
						if (firstconditionset.getAliases() != null)
							for (int i = 0; i < firstconditionset.getAliases().length; i++)
								extraaliases.add(firstconditionset.getAliases()[i]);
					for (int i = 1; i < filterfunctions.size(); i++) {
						QueryFilter thisconditionset = filterfunctions.get(i).apply(alias);

						QueryCondition thiscondition = thisconditionset.getCondition();
						if (finalcondition == null)
							finalcondition = thiscondition;
						if (finalcondition != null)
							if (thiscondition != null)
								finalcondition = new OrQueryCondition(finalcondition, thiscondition);
						if (thisconditionset.getAliases() != null)
							for (int j = 0; j < thisconditionset.getAliases().length; j++)
								extraaliases.add(firstconditionset.getAliases()[j]);
					}

					return new QueryFilter(finalcondition, extraaliases.toArray(new TableAlias[0]));
				}

			};
			return new ActionAuthorization(cumulatedfunction);
		}
		return new ActionAuthorization(ActionAuthorization.NOT_AUTHORIZED);

	}

	/**
	 * gets the default page for the module
	 * 
	 * @param module module
	 * @return default page
	 */
	public SPage getDefaultPage(SModule module) {
		if (module == null)
			throw new RuntimeException("module specified is null");
		SPage page = module.getDefaultPage();
		if (page == null) {
			ActionExecution action = module.getActionForDefaultPage();
			page = action.executeActionFromGUI(null);
		}
		return page;
	}

	/**
	 * gets the main module start page for the server
	 * 
	 * @return the main moduel start page
	 */
	public SPage getMainModuleStartPage() {
		return getDefaultPage(OLcServer.getServer().getMainmodule());
	}

	/**
	 * sets the connection to inactive. When this is set, no new query will be
	 * accepted from clients
	 */
	public void setInactive() {
		this.alive = false;
	}

	/**
	 * logging the action on the server. This helps monitoring the business activity
	 * 
	 * @param action action
	 */
	public void logAction(ActionExecution action) {
		// first generate date for today noon on server time
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 12);
		Date normalizedday = calendar.getTime();
		// get current user in an efficient way
		DataObjectId<Appuser> currentuserid = OLcServer.getServer().getCurrentUserId();
		String module = action.getParent().getLabel();
		Moduleusage[] usage = Moduleusage
				.getallchildrenforsessionuser(currentuserid,
						QueryFilter.get(new AndQueryCondition(
								new SimpleQueryCondition<Date>(
										Moduleusage.getDefinition()
												.getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),
										ModuleusageDefinition.getModuleusageDefinition().getDayFieldSchema(),
										new QueryOperatorEqual<Date>(), normalizedday),
								new SimpleQueryCondition<String>(
										Moduleusage.getDefinition()
												.getAlias(LinkedtoparentQueryHelper.CHILD_OBJECT_ALIAS),
										ModuleusageDefinition.getModuleusageDefinition().getModuleFieldSchema(),
										new QueryOperatorEqual<String>(), module))));
		if (usage != null)
			if (usage.length > 0) {
				Moduleusage currentusage = usage[0];
				currentusage.setActionnr(new Integer(currentusage.getActionnr().intValue() + 1));
				currentusage.update();
			} else {
				Moduleusage currentusage = new Moduleusage();
				currentusage.setparentwithoutupdateforsessionuser(currentuserid);
				currentusage.setDay(normalizedday);
				currentusage.setModule(module);
				currentusage.setActionnr(new Integer(1));
				currentusage.insert();
			}
	}

}
