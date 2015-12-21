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
package org.springframework.ide.eclipse.config.graph.parts;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Text;

/**
 * CellEditorLocator for Activities.
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class ActivityCellEditorLocator implements CellEditorLocator {
	private Label label;

	/**
	 * Creates a new ActivityCellEditorLocator for the given Label
	 * @param label the Label
	 */
	public ActivityCellEditorLocator(Label label) {
		setLabel(label);
	}

	/**
	 * Returns the Label figure.
	 * @return the Label
	 */
	protected Label getLabel() {
		return label;
	}

	/**
	 * @see CellEditorLocator#relocate(org.eclipse.jface.viewers.CellEditor)
	 */
	public void relocate(CellEditor celleditor) {
		Text text = (Text) celleditor.getControl();
		Point pref = text.computeSize(-1, -1);
		Rectangle rect = label.getTextBounds().getCopy();
		label.translateToAbsolute(rect);
		text.setBounds(rect.x - 1, rect.y - 1, pref.x + 1, pref.y + 1);
	}

	/**
	 * Sets the label.
	 * @param label The label to set
	 */
	protected void setLabel(Label label) {
		this.label = label;
	}

}
