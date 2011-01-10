/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.mylyn.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.mylyn.resources.ui.FocusCommonNavigatorAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.springframework.ide.eclipse.mylyn.ui.SpringExplorerInterestFilter;

/**
 * Extension of {@link FocusCommonNavigatorAction} class that serves as a placeholder to future
 * customizations.
 * @author Christian Dupuis
 * @since 2.0
 */
public class FocusSpringExplorerAction extends FocusCommonNavigatorAction {

	public FocusSpringExplorerAction() {
		super(new SpringExplorerInterestFilter(), true, true, true);
	}

	protected FocusSpringExplorerAction(InterestFilter filter) {
		super(filter, true, true, true);
	}

	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();

		IViewPart view = super.getPartForAction();
		if (view instanceof CommonNavigator) {
			CommonNavigator navigator = (CommonNavigator) view;
			viewers.add(navigator.getCommonViewer());
		}
		return viewers;
	}

}
