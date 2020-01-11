/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Logger;

/**
 * A listener to outside connections. It uses sockets on which the Open Lowcode
 * messaging system is used, and specific server protocol is used.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConnectionListener extends Thread {
	private int port;
	private ServerSocket serversocket;
	private OLcServer parent;
	private HashMap<String, ServerConnection> connectionrecordbythreadid;
	private boolean messageaudit;
	private String deadlockobject;
	private boolean active = true;
	Logger logger = Logger.getLogger("");

	/**
	 * Creates a connection listening to the given port
	 * 
	 * @param port         port
	 * @param parent       parent server
	 * @param messageaudit true if messages are audited
	 * @throws IOException if any problem is encountered setting up the server
	 *                     socket. Typically, this may be because a server is
	 *                     already running on the same port
	 */
	public ConnectionListener(int port, OLcServer parent, boolean messageaudit) throws IOException {
		this.deadlockobject = "DEADLOCK";
		this.port = port;
		this.messageaudit = messageaudit;
		logger.info("initiating listener to listen on port " + port);
		this.parent = parent;
		serversocket = new ServerSocket(this.port);
		connectionrecordbythreadid = new HashMap<String, ServerConnection>();
		active = true;
		this.start();
		logger.info("port " + this.port + " listening initialized. Starts listening in the background");

	}

	@Override
	public void run() {
		try {
			while (true) {

				Socket thissocket = serversocket.accept();
				synchronized (deadlockobject) {
					if (active) {
						ServerConnection connection = new ServerConnection(thissocket, parent, messageaudit);
						connection.start();
						long threadid = connection.getId();
						connectionrecordbythreadid.put("" + connection.getId(), connection);
						logger.info("starting clientconnection from address " + thissocket.getInetAddress()
								+ ", treated on thread = " + threadid);
					} else {
						thissocket.close();
						logger.info("Although received a connection, was requested from server to stop");
						break;
					}
				}
			}
		} catch (Exception e) {
			logger.severe("received exception in connection listening thread " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.severe(e.getStackTrace()[i].toString());
		}
	}

	/**
	 * sends a message to all connections not to accept further actions. Then wait
	 * up to 200 seconds for current actions to finish. After 200 seconds, close the
	 * server. THis means this is the responsibility of the server administrator not
	 * to shut-down the server if very long running actions are launched (typically
	 * a batch).<br>
	 * In current version mail daemon is NOT shutdown. This is an improvement to be
	 * performed.
	 */
	public void sendShutdownToAllConnections() {
		int threadstowaitfor = 0;
		synchronized (deadlockobject) {
			active = false;
			Iterator<Entry<String, ServerConnection>> iterator = connectionrecordbythreadid.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ServerConnection> set = iterator.next();
				ServerConnection connection = set.getValue();
				// clean old dead connections;
				if (!connection.isAlive()) {
					connectionrecordbythreadid.remove(set.getKey());

				} else {
					connection.setInactive();
					threadstowaitfor++;
				}

			}

		}
		// end of synchronized block
		long waitingtimestart = System.currentTimeMillis();
		long currenttime = System.currentTimeMillis();
		int loop = 0;
		// right now 200 seconds timer for maximum wait
		logger.severe("Notified all running threads (" + (threadstowaitfor - 1) + ") of shutdown.");
		waitingloop: while (currenttime - waitingtimestart < 1000 * 200) {
			loop++;
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				logger.severe("Got an interruption exception.");
				break waitingloop;
			}
			synchronized (deadlockobject) {
				Iterator<Entry<String, ServerConnection>> iterator = connectionrecordbythreadid.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, ServerConnection> set = iterator.next();
					ServerConnection connection = set.getValue();
					// clean old dead connections;
					if (!connection.isAlive()) {
						connectionrecordbythreadid.remove(set.getKey());
					}
				}
				if (connectionrecordbythreadid.size() == 1) {
					logger.severe("All threads shutdown, proceeding");
					break waitingloop;
				} else {
					// log every 2 seconds progress with thread stop
					if (loop % 40 == 0)
						logger.severe("- Still remaining " + (connectionrecordbythreadid.size() - 1) + " Threads");
				}

			}
			currenttime = System.currentTimeMillis();
		}
		logger.severe("--------------------------------------------------------------------------------------------");
		logger.severe("finished waiting, will shutdown,  (" + (connectionrecordbythreadid.size() - 1)
				+ ") threads still running (not normal if different from zero).");
		logger.severe("---------------------------------------------------------------------------------------------");
	}

	/**
	 * removes the thread from the catalog of threads maintained in this class after
	 * the treatment is finished
	 * 
	 * @param id id of the tream
	 */
	public void reportThreadFinished(long id) {
		logger.severe("Thread is finished " + id);
		synchronized (deadlockobject) {
			connectionrecordbythreadid.remove("" + id);
		}
	}
}
