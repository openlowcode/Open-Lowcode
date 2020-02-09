/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * An utility class to make a region resizable. used for ObjectArray
 * 
 * * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 * SAS</a>
 *
 */
public class DragResizer {
	private static final int RESIZE_MARGIN = 5;
	private final Region region;

	private double y;

	private boolean initMinHeight;

	private boolean dragging;

	private DragResizer(Region aRegion) {
		region = aRegion;
	}

	/**
	 * make the following region resizable
	 * 
	 * @param region
	 */
	public static void makeResizable(Region region) {
		final DragResizer resizer = new DragResizer(region);

		region.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mousePressed(event);

			}
		});
		region.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseDragged(event);

			}
		});
		region.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseOver(event);

			}
		});
		region.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseReleased(event);

			}
		});
	}

	protected void mouseReleased(MouseEvent event) {
		dragging = false;
		region.setCursor(Cursor.DEFAULT);

	}

	protected void mouseOver(MouseEvent event) {
		if (isInDraggableZone(event) || dragging) {
			region.setCursor(Cursor.S_RESIZE);

		} else {
			region.setCursor(Cursor.DEFAULT);

		}
	}

	protected boolean isInDraggableZone(MouseEvent event) {

		return event.getY() > (region.getHeight() - RESIZE_MARGIN);
	}

	protected void mouseDragged(MouseEvent event) {

		if (!dragging) {

			return;
		}

		double mousey = event.getY();

		double newHeight = region.getMinHeight() + (mousey - y);

		region.setMinHeight(newHeight);

		y = mousey;
	}

	protected void mousePressed(MouseEvent event) {

		if (!isInDraggableZone(event)) {
			return;
		}
		dragging = true;
		if (!initMinHeight) {
			region.setMinHeight(region.getHeight());

			initMinHeight = true;
		}

		y = event.getY();
	}

}
