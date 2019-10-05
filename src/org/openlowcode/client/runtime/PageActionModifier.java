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


import javafx.scene.input.MouseEvent;

/**
 * A lightweight wrapper on modifiers when an action  is pressed. Today, only Control and Shift pressed 
 * are managed. Generally, control is reserved for opening an action in a new tab, and should be 
 * discouraged for other use
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class PageActionModifier {
	
	public final static int TYPE_NOTHINGPRESSED = 0;
	public final static int TYPE_CTRLPRESSED = 1;
	public final static int TYPE_SHIFTPRESSED = 2;
	
	/**
	 * @return the page action modifier for no touch pressed
	 */
	public static PageActionModifier getNothingPressed() {
		return new PageActionModifier(TYPE_NOTHINGPRESSED);
	}
	
	/**
	 * @return  the page action modifier for control touch pressed
	 */
	public static PageActionModifier getCtrlPressed() {
		return new PageActionModifier(TYPE_CTRLPRESSED);
	}
	
	/**
	 * @return  the page action modifier for shift touch pressed
	 */
	public static PageActionModifier getShiftPressed() {
		return new PageActionModifier(TYPE_SHIFTPRESSED);
	}
	
	
	private int type;
	
	/**
	 * @param type the type, define as a static number in this class
	 */
	public PageActionModifier(int type) {
		this.type = type;
	}
	
	
	/**
	 * @param event a mouse event
	 * @return true if the mouse event corresponds to the modifier
	 */
	public boolean isActionWithModifier(MouseEvent event) {
		if (event.getClickCount()==1) {
			boolean modifier = false;
			if (event.isControlDown()) {
				modifier = true;
				if (type == TYPE_CTRLPRESSED) return true;
			} 
			
			if (event.isShiftDown()) {
				modifier =true;
				if (type == TYPE_SHIFTPRESSED) return true;
			}
			
			if (!modifier) {
				if (type == TYPE_NOTHINGPRESSED) return true;
			}
			
		}
		return false;
	}


	@Override
	public String toString() {
		return "CSPClientActionModifier["+type+"]";
	}
	
}
