/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsProbeserverthreadsAction;
import org.openlowcode.module.system.data.Serverthread;
import org.openlowcode.module.system.page.ProbeserverthreadsPage;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * An action to probe threads on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @since 1.11
 */
public class ProbeserverthreadsAction
		extends
		AbsProbeserverthreadsAction {

	/**
	 * probe the server threads and display their CPU time
	 * 
	 * @param parent parent module
	 */
	public ProbeserverthreadsAction(SModule parent) {
		super(parent);
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		ArrayList<Serverthread> serverthreadlist = new ArrayList<Serverthread>();
		Map<Thread, StackTraceElement[]> threadlist = Thread.getAllStackTraces();
		Iterator<Entry<Thread, StackTraceElement[]>> threaditerator = threadlist.entrySet().iterator();
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		while (threaditerator.hasNext()) {
			Entry<Thread, StackTraceElement[]> threadentry = threaditerator.next();
			Thread thread = threadentry.getKey();
			StackTraceElement[] stacktrace = threadentry.getValue();
			long id = thread.getId();
			String name = thread.getName();
			StringBuffer stacktracebuffer = new StringBuffer();
			for (int i = 0; i < stacktrace.length; i++)
				stacktracebuffer.append(stacktrace[i].toString() + "\n");
			String stacktracestring = stacktracebuffer.toString();
			Serverthread serverthread = new Serverthread();
			serverthread.setStack(truncateAtLength(stacktracestring, 1024));
			serverthread.setThreadid(new BigDecimal(id));
			serverthread.setName(truncateAtLength(name, 245));
			long threadcputime = threadMXBean.getThreadCpuTime(id);
			serverthread.setServercpu(new BigDecimal(threadcputime / 1000000l));
			serverthreadlist.add(serverthread);
		}
		return new ActionOutputData(serverthreadlist.toArray(new Serverthread[0]));
	}

	private String truncateAtLength(String origin, int length) {
		if (origin.length() > length)
			return origin.substring(0, length - 3) + "...";
		return origin;
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return new ProbeserverthreadsPage(logicoutput.getThreads());
	}

}
