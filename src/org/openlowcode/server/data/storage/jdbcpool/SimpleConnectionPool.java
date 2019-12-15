/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.storage.jdbcpool;

import java.sql.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A simple implementation of the connection pool
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleConnectionPool implements ConnectionPool {
	private Logger logger = Logger.getLogger(SimpleConnectionPool.class.getCanonicalName());
	private static final int TIMEOUT = 50000; // 50s in ms
	private static final int RETRYFREQUENCY = 3; // 10ms

	/**
	 * the class representing the status of a connection
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	private class ConnectionStatus {
		public Connection connection;
		public Thread bookingthread;
		private boolean sick = false;

		/**
		 * @param connection
		 * @param bookingthread
		 */
		public ConnectionStatus(Connection connection, Thread bookingthread) {
			super();
			this.connection = connection;
			this.bookingthread = bookingthread;
		}

		/**
		 * @throws SQLException
		 */
		public void cure() throws SQLException {
			if (sick) {
				logger.warning("Resetting connection after SQL Exception ");
				try {

					this.connection.close();
				} catch (SQLException e) {
					logger.warning("  **** Error during closing of sick SQL Connection " + e.getSQLState() + "/"
							+ e.getErrorCode() + "/" + e.getMessage());
				}
				this.connection = createConnection(url, user, password);
				this.sick = false;
			}
		}

	}

	private int minnumber;
	private int maxnumber;
	private String url;
	private String user;
	private String password;
	private TreeSet<Booking> orderedbooking;
	private ArrayList<ConnectionStatus> connections;

	private Connection createConnection(String url, String user, String password) throws SQLException {
		if (user == null) {
			logger.info("trying to create connection without user for url = " + url);
			logger.severe("connection attempt URL = '" + url + "' ");
			Connection connection = DriverManager.getConnection(url);
			logger.severe("connection succeeded URL = '" + url + "' ");
			if (connection == null)
				throw new SQLException("Null connection for url = " + url);
			return connection;
		}
		logger.info("trying to create connection with user = " + user + " for url = " + url);
		logger.severe("connection attempt URL = '" + url + "' user='" + user + "' pwd='" + password + "'");

		Connection connection = DriverManager.getConnection(url, user, password);
		logger.severe("connection attempt succesfull URL = '" + url + "' user='" + user + "' pwd='" + password + "'");

		if (connection == null)
			throw new SQLException("Null connection with user = " + user + " for url = " + url);
		return connection;
	}

	/**
	 * Creates a simple connection pool for a JDBC database
	 * 
	 * @param url       URL of the database
	 * @param user      user to create the connection
	 * @param password  password to create the connection
	 * @param minnumber minimum number of connections in the pool
	 * @param maxnumber maximum number of connections in the pool
	 * @throws SQLException SQL Exception if any error is encountered
	 */
	public SimpleConnectionPool(String url, String user, String password, int minnumber, int maxnumber)
			throws SQLException {
		connections = new ArrayList<ConnectionStatus>();
		orderedbooking = new TreeSet<Booking>();
		this.minnumber = minnumber;
		this.maxnumber = maxnumber;
		this.url = url;
		this.user = user;
		this.password = password;
		for (int i = 0; i < this.minnumber; i++) {
			connections.add(new ConnectionStatus(createConnection(url, user, password), null));

			logger.info("initiated a connection with database url = " + url + ", index = " + i + ", minnumber = "
					+ minnumber + ", maxnumber = " + maxnumber);
		}
	}

	@Override
	public Connection getConnectionWithRetry() throws SQLException, InterruptedException {
		long starttime = System.currentTimeMillis();
		long currenttime = starttime;
		while (currenttime - starttime < TIMEOUT) {

			Connection connection = getConnection();
			if (connection != null) {
				logger.info("got connection for Thread " + Thread.currentThread().getId() + " after waiting time of "
						+ (currenttime - starttime) + "ms");
				return connection;
			}
			Thread.sleep(RETRYFREQUENCY);
			currenttime = System.currentTimeMillis();
		}
		logger.info("could not book connection for Thread " + Thread.currentThread().getId());
		return null;
	}

	private synchronized Connection getConnection() throws SQLException {
		int otherprioritythreads = 0;
		Iterator<Booking> iterator = orderedbooking.iterator();
		Booking ownthreadbooking = null;
		while (iterator.hasNext()) {
			Booking booking = iterator.next();

			if (booking.isForCurrentThread()) {
				ownthreadbooking = booking;
				break;
			}
			if (booking.isObsolete()) {
				logger.warning("Booking token has expired " + booking);

				iterator.remove();
			} else {
				otherprioritythreads++;
			}
		}

		int freeconnections = 0;
		for (int i = 0; i < connections.size(); i++) {

			if (connections.get(i).bookingthread == null) {
				freeconnections++;
				if (freeconnections > otherprioritythreads) {
					if (ownthreadbooking != null) {
						logger.info("Consuming own thread booking " + ownthreadbooking);
						iterator.remove();
						logger.info("cleaning other bookings for the thread");
						Iterator<Booking> cleaningiterator = orderedbooking.iterator();
						while (cleaningiterator.hasNext()) {
							Booking bookingtoclean = cleaningiterator.next();
							if (bookingtoclean.isForCurrentThread()) {
								cleaningiterator.remove();
								logger.info(
										"cleaning obsolete booking" + bookingtoclean + " for thread with connection");
							}

						}
					}

					logger.info("Connection " + i + " assigned to Thread " + Thread.currentThread().getId());

					connections.get(i).bookingthread = Thread.currentThread();
					connections.get(i).cure();
					return connections.get(i).connection;
				} else {
					logger.info("while a connection (number " + freeconnections
							+ ") is free, does not assign it to Thread " + Thread.currentThread().getId() + " because "
							+ otherprioritythreads + " other threads are waiting");
				}
			} else {
				logger.info("Connection (number " + freeconnections + ") is not free (booked by thread "
						+ connections.get(i).bookingthread.getId() + "), does not assign it to Thread "
						+ Thread.currentThread().getId() + ".");
			}
		}
		if (connections.size() < maxnumber) {
			int newindex = connections.size();
			connections.add(new ConnectionStatus(createConnection(url, user, password), null));
			logger.info("initiated a connection with database url = " + url + ", index = " + newindex + " for Thread "
					+ Thread.currentThread().getId());
			connections.get(newindex).bookingthread = Thread.currentThread();
			connections.get(newindex).cure();
			return connections.get(newindex).connection;
		}
		logger.info("Could not assign connection to Thread " + Thread.currentThread().getId()
				+ " created a token out of " + orderedbooking.size());
		Iterator<Booking> logiterator = orderedbooking.iterator();
		int logindex = 0;
		logger.info(" --- Bookings -------------- totalsize = " + orderedbooking.size()
				+ " showing first 5 elements (if exists)");
		while ((logindex < 5) && (logiterator.hasNext())) {
			logger.info("        - " + logiterator.next());
			logindex++;
		}
		logger.info(" --- Bookings - logs finished --------------------");
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < stacktrace.length; i++)
			logger.fine(stacktrace[i].toString());
		logger.info(" ---------------------------------------------------------------- ");
		;
		if (orderedbooking.size() > maxnumber * 30) {
			logger.warning("booking overflowing with " + orderedbooking.size() + " elements, removing duplicates");
			HashMap<String, String> alreadypresentthreads = new HashMap<String, String>();
			Iterator<Booking> bookingiterator = orderedbooking.iterator();
			int cleaned = 0;
			while (bookingiterator.hasNext()) {
				Booking thisbooking = bookingiterator.next();
				if (alreadypresentthreads.containsKey("" + thisbooking.threadid)) {
					bookingiterator.remove();
					cleaned++;
				} else {
					alreadypresentthreads.put("" + thisbooking.threadid, "" + thisbooking.threadid);
				}
			}
			logger.info("Removed " + cleaned + " duplicates, new booking list = " + orderedbooking.size());
		}
		orderedbooking.add(new Booking());

		return null;
	}

	@Override
	public void checkin(Connection connection) {

		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).connection == connection) {
				logger.info("releasing connection index = " + i + " for booking thread "
						+ connections.get(i).bookingthread.getId());
				connections.get(i).bookingthread = null;
			}
		}

	}

	@Override
	public void checkinandreset(Connection connection) {
		for (int i = 0; i < connections.size(); i++) {
			if (connections.get(i).connection == connection) {
				logger.warning("releasing connection index = " + i + " for booking thread "
						+ connections.get(i).bookingthread.getId() + " after SQL Error");
				connections.get(i).bookingthread = null;
				connections.get(i).sick = true;
			}
		}

	}

	@Override
	public void freecurrentthreadconnections() {
		Thread currentthread = Thread.currentThread();
		logger.info(" ----- Free all threads for thread id = " + Thread.currentThread() + " ---- ");
		for (int i = 0; i < connections.size(); i++) {
			ConnectionStatus thisconnection = connections.get(i);
			if (thisconnection.bookingthread == currentthread) {
				logger.info(" Free connection " + i + "for thread id = " + Thread.currentThread()
						+ " as part of exception handling ");
				thisconnection.bookingthread = null;
			}
		}

	}

	/**
	 * A class to store a booking. A booking is made for a maximum of 2 time-out
	 * cycles for a thread that could not get a connection. Every time a thread
	 * wants to book a connection, a check is made in the booking, and
	 * <ul>
	 * <li>all bookings longer than 2 cycles are deleted</li>
	 * <li>when a connection is ready, if there is no booking, the thread gets
	 * it</li>
	 * <li>when a connection is ready, if there is a valid first (most recent)
	 * booking from another thread, the current threads places a booking and waits
	 * </li>
	 * <li>when a connection is ready, if the threads holds the most recent booking,
	 * the current thread gets the connection</li>
	 * <li>when no connection is ready, the thread places a booking</li>
	 * </ul>
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	private class Booking implements Comparable<Booking> {
		private long bookingtime;
		private long threadid;

		@Override
		public int compareTo(Booking other) {
			if (bookingtime > other.bookingtime)
				return 1;
			if (bookingtime < other.bookingtime)
				return -1;
			if (threadid > other.threadid)
				return 1;
			if (threadid < other.threadid)
				return -1;
			return 0;
		}

		/**
		 * creates a new booking for the thread
		 */
		public Booking() {
			this.bookingtime = (new Date()).getTime();
			this.threadid = Thread.currentThread().getId();
		}

		/**
		 * @return true if the booking is ofr current thread, false else
		 */
		public boolean isForCurrentThread() {
			if (threadid == Thread.currentThread().getId()) {
				logger.info("Booking is for current Thread, booking thread = " + threadid + ", current thread = "
						+ Thread.currentThread().getId());
				return true;
			}
			logger.info("Booking is not for current Thread, booking thread = " + threadid + ", current thread = "
					+ Thread.currentThread().getId());

			return false;
		}

		/**
		 * @return true of the booking is obsolete
		 */
		public boolean isObsolete() {
			long current = new Date().getTime();
			if (current > bookingtime + RETRYFREQUENCY * 2)
				return true;
			return false;
		}

		@Override
		public String toString() {
			return "BOOKING:THREAD=" + threadid + ";BOOKINGTIME:" + bookingtime;
		}

	}

}
