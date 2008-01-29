/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.mylyn.ui.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.mylyn.context.ui.InterestFilter;
import org.eclipse.mylyn.internal.ide.ui.actions.FocusProjectExplorerAction;
import org.eclipse.mylyn.internal.resources.ui.FocusCommonNavigatorAction;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.springframework.ide.eclipse.aop.mylyn.ui.AopReferenceModelNavigatorInterestFilter;
import org.springframework.ide.eclipse.aop.ui.navigator.AopReferenceModelNavigator;

/**
 * Extension of {@link FocusProjectExplorerAction} class that serves as a
 * placeholder to future customizations.
 * @author Christian Dupuis
 * @since 2.0
 */
public class FocusAopReferenceModelNavigatorAction extends FocusCommonNavigatorAction {
	
	private boolean isLinkingEnabled = false;
	
	private AopReferenceModelNavigator navigator = null;
	
	public FocusAopReferenceModelNavigatorAction() {
		super(new AopReferenceModelNavigatorInterestFilter(), true, true, true);
	}

	protected FocusAopReferenceModelNavigatorAction(InterestFilter filter) {
		super(filter, true, true, true);
	}
	
	@Override
	public List<StructuredViewer> getViewers() {
		List<StructuredViewer> viewers = new ArrayList<StructuredViewer>();

		IViewPart view = super.getPartForAction();
		if (view instanceof CommonNavigator) {
			CommonNavigator navigator = (CommonNavigator) view;
			viewers.add(navigator.getCommonViewer());
			if (navigator instanceof AopReferenceModelNavigator) {
				this.navigator = (AopReferenceModelNavigator) navigator;
			}
		}
		return viewers;
	}
	
	@Override
	protected void setDefaultLinkingEnabled(boolean on) {
		// override to always stay in the same state
		super.setDefaultLinkingEnabled(isLinkingEnabled);
	}
	
	@Override
	protected boolean isDefaultLinkingEnabled() {
		isLinkingEnabled = super.isDefaultLinkingEnabled();
		return isLinkingEnabled;
	}
	
 	@Override
 	public void run(IAction action) {
 		super.run(action);
 		refreshViewer();
 	}

	private void refreshViewer() {
		if (navigator != null) {
			navigator.elementChanged(null);
		}
	}
}
