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
package org.springframework.ide.eclipse.beans.ui.livegraph.views;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenBeanClassAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenContextFileAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;

/**
 * A simple view to host our graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphView extends ViewPart {

	public static final String VIEW_ID = "org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView";

	private GraphViewer viewer;

	private BaseSelectionListenerAction openBeanClassAction;

	private BaseSelectionListenerAction openContextAction;

	@Override
	public void createPartControl(Composite parent) {
		viewer = new GraphViewer(parent, SWT.NONE);
		viewer.setContentProvider(new LiveBeansGraphContentProvider());
		viewer.setLabelProvider(new LiveBeansGraphLabelProvider());
		viewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		viewer.setInput(new LiveBeansModel());
		viewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		viewer.applyLayout();
		getSite().setSelectionProvider(viewer);

		makeActions();
		hookContextMenu();

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (openContextAction != null && openContextAction.isEnabled()) {
					openContextAction.run();
				}
			}
		});
	}

	@Override
	public void dispose() {
		if (viewer != null) {
			viewer.removeSelectionChangedListener(openBeanClassAction);
			viewer.removeSelectionChangedListener(openContextAction);
		}
		super.dispose();
	}

	private void fillContextMenu(IMenuManager menuManager) {
		menuManager.add(new Separator());
		menuManager.add(openBeanClassAction);
		menuManager.add(openContextAction);
	}

	private void hookContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		fillContextMenu(menuManager);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
		});

		Menu menu = menuManager.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);

	}

	private void makeActions() {
		openBeanClassAction = new OpenBeanClassAction();
		viewer.addSelectionChangedListener(openBeanClassAction);
		openContextAction = new OpenContextFileAction();
		viewer.addSelectionChangedListener(openContextAction);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
