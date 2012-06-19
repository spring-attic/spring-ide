/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.graph.figures;

import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.ButtonModel;
import org.eclipse.draw2d.ChangeEvent;
import org.eclipse.draw2d.ChangeListener;
import org.eclipse.draw2d.Clickable;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Triangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * @author Leo Dos Santos
 */
public class CollapsibleContainerFigure extends ParallelActivityFigure {

	private class Expander extends Clickable {

		private final Triangle triangle;

		public Expander() {
			setStyle(Clickable.STYLE_TOGGLE);
			triangle = new Triangle();
			triangle.setSize(10, 10);
			triangle.setBackgroundColor(ColorConstants.black);
			triangle.setForegroundColor(ColorConstants.black);
			triangle.setFill(true);
			triangle.setDirection(Triangle.SOUTH);
			triangle.setLocation(new Point(5, 3));
			setLayoutManager(new FreeformLayout());
			add(triangle);
			setPreferredSize(15, 15);
			addChangeListener(new ChangeListener() {
				public void handleStateChanged(ChangeEvent event) {
					if (event.getPropertyName().equals(ButtonModel.SELECTED_PROPERTY)) {
						handleExpandedStateChanged();
					}
					else if (event.getPropertyName().equals(ButtonModel.MOUSEOVER_PROPERTY)) {
						repaint();
					}
				}
			});
		}

		protected void open() {
			triangle.setDirection(Triangle.SOUTH);
		}

		protected void close() {
			triangle.setDirection(Triangle.EAST);
		}

	}

	private final Expander expander;

	private boolean isExpanded = true;

	public CollapsibleContainerFigure(int direction) {
		super(direction);
		expander = new Expander();
		add(expander);
	}

	public void addActionListener(ActionListener listener) {
		expander.addActionListener(listener);
	}

	@Override
	public Dimension getPreferredSize(int wHint, int hHint) {
		Dimension dim = new Dimension();
		dim.width = getHeader().getPreferredSize().width;
		dim.width += expander.getPreferredSize().width;
		dim.width += getInsets().getWidth();
		dim.height = 50;
		return dim;
	}

	protected void handleExpandedStateChanged() {
		if (isExpanded) {
			expander.close();
		}
		else {
			expander.open();
		}
		isExpanded = !isExpanded;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	@Override
	public void setBounds(Rectangle rect) {
		super.setBounds(rect);
		Dimension size = expander.getPreferredSize();
		expander.setLocation(rect.getLocation());
		expander.setSize(size);
		header.setLocation(rect.getLocation().translate(expander.getSize().width + getInsets().getWidth(), 0));
	}

}
