/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openlowcode.client.action.CActionData;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.tools.misc.Pair;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class PageActionManager implements EventHandler<ActionEvent> {

	/**
	 * for some widgets, a subcomponent will fire the event, but a parent widget has
	 * the action attached to it. This is typically the case for action on a table
	 * cell, where the action is declared on the whole table. This interface allows
	 * to transform the local widget to the parent widget that should get the action
	 * (Note: there should be a way in the parent widget to remember which lower
	 * level widget was selected
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	@FunctionalInterface
	public interface ActionSourceTransformer {

		/**
		 * @param potentialchildwidget the widget to analyze and transform
		 * @return the widget given in input if no transformation is needed, else bring
		 *         back the parent widget
		 */
		public Object getParentWidget(Object potentialchildwidget);
	}

	private static Logger logger = Logger.getLogger(PageActionManager.class.toString());

	private HashMap<String, UnsavedDataWarning> warningspernodepath;
	private HashMap<Object, CPageAction> registeredevents;
	private HashMap<Object, HashMap<PageActionModifier, Pair<CPageAction,Boolean>>> registeredeventswithmodifier;
	private HashMap<Object, HashMap<PageActionModifier, CPageInlineAction>> registeredinlineeventswithmodifier;
	private HashMap<Object, Callback> registeredcallbacks;
	private HashMap<Object, CPageInlineAction> registeredinlineactions;

	private CPage page;

	private EventHandler<MouseEvent> advancedclickmanager;

	/**
	 * @return the client display this page action manager is working on
	 */
	public ClientDisplay getClientDisplay() {
		return this.clientdisplay;
	}

	/**
	 * @return the client session to be used by this page action manager to communicate with the server
	 */
	public ClientSession getClientSession() {
		return this.serverconnection;
	}

	private ClientDisplay clientdisplay;

	private ClientSession serverconnection;

	/**
	 * @param unsaveddatawarning configures an unsaveddatawarning on a node. If an
	 * action is fired from another node, the unsaved data warning will display first
	 */
	public void addUnsavedDataWarning(UnsavedDataWarning unsaveddatawarning) {
		CPageNode originnode = unsaveddatawarning.getOriginode();
		String path = originnode.getPath().printPath();
		this.warningspernodepath.put(path, unsaveddatawarning);
	}

	/**
	 * @param node remove an unsaveddatawarning on a node.
	 */
	public void removedUnsavedDataWarningForNode(CPageNode node) {
		String path = node.getPath().printPath();
		this.warningspernodepath.remove(path);
	}

	/**
	 * removes all stored information before displaying a new page
	 */
	public void reset() {
		logger.fine("   --- *** --- Registered Events are cleaned");
		registeredevents = new HashMap<Object, CPageAction>();
		registeredeventswithmodifier = new HashMap<Object, HashMap<PageActionModifier, Pair<CPageAction,Boolean>>>();
		registeredinlineeventswithmodifier = new HashMap<Object, HashMap<PageActionModifier, CPageInlineAction>>();
		registeredinlineactions = new HashMap<Object, CPageInlineAction>();
		registeredcallbacks = new HashMap<Object, Callback>();
		resetWarnings();
	}

	/**
	 * reset all warnings for unsaved data
	 */
	public void resetWarnings() {
		warningspernodepath = new HashMap<String, UnsavedDataWarning>();
	}

	/**
	 * @param object object the callback is configured on
	 * @param callback callback object
	 */
	public void registerCallback(Object object, Callback callback) {
		registeredcallbacks.put(object, callback);
	}

	/**
	 * @param object the object (JAVAFX widget) that will fire an event
	 * @param action the action to launch in that case
	 */
	public void registerEvent(Object object, CPageAction action) {
		logger.fine("         * registering event for action " + action.getName() + " on object " + object);
		registeredevents.put(object, action);

	}

	/**
	 * Register an event with modifier for display of action result in the same tab
	 * @param object the object (JAVAFX widget) that will fire an event
	 * @param action the action to launch in that case
	 * @param modifier relevant modifier (like Control, Shift pressed)
	 */
	public void registerEventWithModifier(Object object, CPageAction action, PageActionModifier modifier) {
		registerEventWithModifier(object,action,modifier,false);
	}

	/**
	 * Register an event with modifier for display of action result in the same tab
	 * 
	 * @param object       the object (JAVAFX widget) that will fire an event
	 * @param action       the action to launch in that case
	 * @param modifier     relevant modifier (like Control, Shift pressed)
	 * @param openinnewtab if true, action result shown in new tab (keeping current
	 *                     page in current tab), if false action result shown on
	 *                     current tab (overwriting current page)
	 * @since 1.1
	 */
	public void registerEventWithModifier(
			Object object,
			CPageAction action,
			PageActionModifier modifier,
			boolean openinnewtab) {
		HashMap<PageActionModifier, Pair<CPageAction,Boolean>> actions = registeredeventswithmodifier.get(object);
		if (actions == null) {
			actions = new HashMap<PageActionModifier, Pair<CPageAction,Boolean>>();
			registeredeventswithmodifier.put(object, actions);
		}
		actions.put(modifier, new Pair<CPageAction,Boolean>(action,new Boolean(openinnewtab)));

	}
	
	/**
	 * @return the mouse handler from this page action manager
	 */
	public EventHandler<MouseEvent> getMouseHandler() {
		return this.advancedclickmanager;
	}

	/**
	 * @param currentaction the action being triggered
	 * @return relevant unsaved data warning, null else
	 */
	protected UnsavedDataWarning checkwarnings(CPageAction currentaction) {
		Iterator<String> nodeswithwarning = warningspernodepath.keySet().iterator();
		logger.fine(" -------------Chech warning when action sent "
				+ (currentaction != null ? currentaction.getName() : "NULL") + "---------------------- ");
		while (nodeswithwarning.hasNext()) {
			String nodepath = nodeswithwarning.next();

			UnsavedDataWarning warning = warningspernodepath.get(nodepath);
			logger.fine("      * " + nodepath + " warning - " + warning);
			if (currentaction == null)
				return warning;
			if (!(currentaction.includesnodepath(nodepath)))
				return warning;
		}
		return null;
	}

	/**
	 * @param currentaction the inline action behind triggered
	 * @return unsaved data warning if it exists, null else
	 */
	protected UnsavedDataWarning checkwarnings(CPageInlineAction currentaction) {
		Iterator<String> nodeswithwarning = warningspernodepath.keySet().iterator();
		logger.fine(" -------------Chech warning when inline action sent "
				+ (currentaction != null ? currentaction.getName() : "NULL") + "---------------------- ");
		while (nodeswithwarning.hasNext()) {
			String nodepath = nodeswithwarning.next();

			UnsavedDataWarning warning = warningspernodepath.get(nodepath);
			logger.fine("      * " + nodepath + " warning - " + warning);
			if (currentaction == null)
				return warning;
			if (!(currentaction.includesnodepath(nodepath)))
				return warning;
		}
		return null;
	}

	/**
	 * @return true if confirmed to continue, false if action should stop
	 */
	public boolean checkContinueWarning() {
		logger.fine("  -- calling check warning with null arguments");
		UnsavedDataWarning warning = checkwarnings((CPageAction) null);
		if (warning == null)
			return true;
		return this.clientdisplay.displayModalPopup(warning.getMessage(), warning.getContinuemessage(),
				warning.getStopmessage());
	}

	/**
	 * directly fires an event to be processed. This is required by widgets with complex logic
	 * @param action the action fired
	 */
	public void directfireEvent(CPageAction action) {
		try {
			processAction(action);
		} catch (Exception e) {
			serverconnection.getActiveClientDisplay().updateStatusBar("Error in action " + e.getMessage(), true);
			logger.severe("exception while processing event " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe(e.getStackTrace()[i].toString());
			}
		}
	}

	/**
	 * @param action the action to fire
	 * @param callback the callback
	 */
	public void directfireEvent(CPageAction action, Callback callback) {
		try {
			if (callback != null)
				callback.callback();
			processAction(action);
		} catch (Exception e) {

			serverconnection.getActiveClientDisplay().updateStatusBar("Error in action " + e.getMessage(), true);
			logger.severe("exception while processing event " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe(e.getStackTrace()[i].toString());
			}
		}
	}

	/**
	 * @param action an inline action
	 */
	public void directfireInlineEvent(CPageInlineAction action) {
		try {
			processInlineAction(action);
		} catch (Exception e) {
			serverconnection.getActiveClientDisplay().updateStatusBar("Error in action " + e.getMessage(), true);
			logger.severe("exception while processing event " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe(e.getStackTrace()[i].toString());
			}
		}
	}

	@Override
	public void handle(ActionEvent event) {
		logger.fine("Action Manager Handle Start");
		Platform.runLater(() -> {
			try {
				logger.finer("Action manager inside run mater");

				Callback potentialcallback = registeredcallbacks.get(event.getSource());
				if (potentialcallback != null)
					potentialcallback.callback();
				CPageAction currentaction = registeredevents.get(event.getSource());

				if (currentaction != null) {
					processAction(currentaction);
					return;
				}

				CPageInlineAction currentinlineaction = this.registeredinlineactions.get(event.getSource());
				if (currentinlineaction != null) {
					processInlineAction(currentinlineaction);
					return;
				}

				if (potentialcallback == null)
					throw new RuntimeException("Unknown event from page, source was not registered in callbacks "
							+ event.getSource().toString());
				throw new RuntimeException("Unknown event from page, the action registered for callback was empty "
						+ event.getSource().toString());

			} catch (Exception e) {
				serverconnection.getActiveClientDisplay().updateStatusBar("Error in action " + e.getMessage(), true);
				logger.severe("exception while processing event " + e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++) {
					logger.severe(e.getStackTrace()[i].toString());
				}
			}
		});
	}

	private void processAction(CPageAction currentaction, boolean openinnewtab) {
		String actionname = currentaction.getName();
		String modulename = currentaction.getModule();
		CActionData actionattributes = currentaction.getDataContent(page);
		UnsavedDataWarning warning = checkwarnings(currentaction);
		boolean continuewithaction = true;
		if (warning != null) {

			serverconnection.getActiveClientDisplay().updateStatusBar(
					"Action " + actionname + " will discard unsaved data. You need to confirm to continue", true);
			continuewithaction = clientdisplay.displayModalPopup(warning.getMessage(), warning.getContinuemessage(),
					warning.getStopmessage());
		}
		if (continuewithaction) {
			serverconnection.getActiveClientDisplay()
					.updateStatusBar("Following user interaction, action " + actionname + " is sent to server. ");
			page.getAllInputData().closeSubScene();
			this.serverconnection.getClientData().setBackChainBroken();
			this.serverconnection.sendaction(actionname, modulename, actionattributes, openinnewtab);
			return;
		} else {
			serverconnection.getActiveClientDisplay().updateStatusBar("Discarded action due to unsaved data");
			return;
		}
	}

	private void processAction(CPageAction currentaction) {
		processAction(currentaction, false);
	}

	private void processInlineAction(CPageInlineAction currentinlineaction) {
		String actionname = currentinlineaction.getName();
		String modulename = currentinlineaction.getModule();
		boolean local = currentinlineaction.isLocal();
		CActionData actionattributes = currentinlineaction.getDataContent(page);
		UnsavedDataWarning warning = checkwarnings(currentinlineaction);
		boolean continuewithaction = true;
		if (warning != null) {

			serverconnection.getActiveClientDisplay().updateStatusBar(
					"Action " + actionname + " will discard unsaved data. You need to confirm to continue", true);
			continuewithaction = clientdisplay.displayModalPopup(warning.getMessage(), warning.getContinuemessage(),
					warning.getStopmessage());
		}
		if (continuewithaction) {
			if (currentinlineaction.isForcePopupClose())
				page.getAllInputData().closeSubScene();
			if (!local) {
				serverconnection.sendInlineAction(actionname, modulename, actionattributes, page);
			} else {
				page.processInlineAction(modulename, actionname, CPageData.echo(actionattributes));
			}
				// this is a shortcut
			resetWarnings();
			return;
		} else {
			serverconnection.getActiveClientDisplay().updateStatusBar("Discarded action due to unsaved data");
			return;
		}
	}

	/**
	 * Creates a new action manager
	 * @param clientdisplay parent display
	 * @param actionsourcetransformer transformer that gets, when relevant, the higher level widget (e.g.
	 * tableview) from the lowest level widget (e.g. tablecell)
	 */
	public PageActionManager(ClientDisplay clientdisplay, ActionSourceTransformer actionsourcetransformer) {
		this.registeredevents = new HashMap<Object, CPageAction>();
		this.warningspernodepath = new HashMap<String, UnsavedDataWarning>();
		this.clientdisplay = clientdisplay;

		this.serverconnection = clientdisplay.getParentServerConnection();
		this.advancedclickmanager = new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					logger.warning(" --- ### --- Mouse event detected, click count=" + event.getClickCount() + " on object "
							+ event.getSource() + " Ctrlpressed = " + event.isControlDown() + " Shiftpressed "
							+ event.isShiftDown());

					Object eventsource = event.getSource();
					Object finalobject = eventsource;

					// managing multi-click or mono click with control (to open new page). It
					// typically does not have modifiers

					if ((event.getClickCount() > 1) || (event.getClickCount() == 1 && event.isControlDown())) {
						// page action transformer only works with more than 1 click. This is a way to
						// filter
						// normal action on complex widgets (select a cell on table...)
						if (actionsourcetransformer != null)
							finalobject = actionsourcetransformer.getParentWidget(eventsource);
						CPageAction currentaction = registeredevents.get(finalobject);
						if (currentaction != null) {
							if (event.isControlDown()) {
								logger.info("Firing double click with Control to open in new tab");
								processAction(currentaction, true);

							} else {
								logger.info("Firing double click without control (keep same tab)");
								processAction(currentaction);

							}
							return;
						}

						CPageInlineAction currentinlineaction = registeredinlineactions.get(finalobject);
						if (currentinlineaction != null) {

							processInlineAction(currentinlineaction);

							return;
						}

						if (event.getClickCount() > 1) throw new RuntimeException(
								"Unknown event from page "+ event.getSource().toString()+", click="+event.getClickCount()+", Ctrl ="+event.isControlDown()+", shift="+event.isShiftDown());
					}
					// managing monoclick. Typically, this may have modifiers

					HashMap<PageActionModifier, Pair<CPageAction,Boolean>> actionswithmodifier = registeredeventswithmodifier
							.get(finalobject);
					if (actionswithmodifier == null) {
						CPageAction currentaction = registeredevents.get(finalobject);
						if (currentaction != null) {
							// when action with simple click exists with no modifier, tries to detect
							// control
							if (event.isControlDown()) {
								logger.info("Firing single click with Control to open in new tab");
								processAction(currentaction, true);
							} else {
								logger.info("Firing single click without control (keep same tab)");
								processAction(currentaction);
							}
							return;
						}
					} else {
						Iterator<PageActionModifier> modifierlist = actionswithmodifier.keySet().iterator();
						while (modifierlist.hasNext()) {
							PageActionModifier modifier = modifierlist.next();
							if (modifier.isActionWithModifier(event)) {
								Pair<CPageAction,Boolean> currentaction = actionswithmodifier.get(modifier);
									if (currentaction!=null) {
									processAction(currentaction.getFirstobject(),currentaction.getSecondobject().booleanValue());
									return;
								}
							}
						}
					}
					// modifier for inline action;
					HashMap<PageActionModifier, CPageInlineAction> inlineactionswithmodifier = registeredinlineeventswithmodifier
							.get(eventsource);
					if (inlineactionswithmodifier == null) {
						logger.fine("No inline actions with modifier for object " + eventsource);
						CPageInlineAction currentaction = registeredinlineactions.get(eventsource);
						if (currentaction != null) {
							processInlineAction(currentaction);
							return;
						}
					} else {
						Iterator<PageActionModifier> modifierlist = inlineactionswithmodifier.keySet().iterator();
						while (modifierlist.hasNext()) {
							PageActionModifier modifier = modifierlist.next();
							if (modifier.isActionWithModifier(event)) {
								CPageInlineAction currentaction = inlineactionswithmodifier.get(modifier);
								if (currentaction!=null) processInlineAction(currentaction);
								return;
							}
						}
					}

				} catch (Throwable e) {
					logger.warning("exception while processing event " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						String stacktrace = e.getStackTrace()[i].toString();
						if (stacktrace.startsWith("com.sun")) break;
						logger.warning("  - " + e.getStackTrace()[i].toString());
					}
					serverconnection.getActiveClientDisplay()
							.updateStatusBar("Error while preparing action " + e.getMessage(), true);
				}
			}
		};
	}

	/**
	 * @param page sets the page for the widget (does not perform any processing)
	 */
	public void setPage(CPage page) {
		this.page = page;
	}

	/**
	 * registers an inline action
	 * @param widget widget to trigger the action
	 * @param inlineaction inline action to launch
	 */
	public void registerInlineAction(Object widget, CPageInlineAction inlineaction) {
		registeredinlineactions.put(widget, inlineaction);

	}

	/**
	 * registers an inline action that should close the attached popup
	 * @param widget widget to trigger the action
	 * @param inlineaction inline action to launch
	 */
	public void registerInlineActionwithPopupClose(Object widget, CPageInlineAction inlineaction) {
		registeredinlineactions.put(widget, inlineaction);
		inlineaction.forcePopupClose();
	}

	/**
	 * @param widget widget to trigger the action
	 * @param inlineaction forcePopupClose
	 * @param modifier a modifier (like shift pressed...)
	 */
	public void registerInlineActionWithModifier(Object widget, CPageInlineAction inlineaction,
			PageActionModifier modifier) {
		HashMap<PageActionModifier, CPageInlineAction> actions = registeredinlineeventswithmodifier.get(widget);
		if (actions == null) {
			actions = new HashMap<PageActionModifier, CPageInlineAction>();
			registeredinlineeventswithmodifier.put(widget, actions);
		}
		actions.put(modifier, inlineaction);
	}

}
