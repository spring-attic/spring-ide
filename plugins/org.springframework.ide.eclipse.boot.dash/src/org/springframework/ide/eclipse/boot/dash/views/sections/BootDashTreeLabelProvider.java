/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views.sections;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.util.Stylers;
import org.springframework.ide.eclipse.boot.dash.views.BootDashCellLabelProvider;
import org.springframework.ide.eclipse.boot.dash.views.BootDashElementLabelProvider;

/**
 * @author Kris De Volder
 */
public class BootDashTreeLabelProvider extends BootDashCellLabelProvider {

	public BootDashTreeLabelProvider(Stylers stylers, ColumnViewer tv) {
		//TODO: refactor so we do not need this 'dummy' column?
		super(tv, BootDashColumn.TREE_VIEWER_MAIN/*dummy*/, stylers);
	}
}
