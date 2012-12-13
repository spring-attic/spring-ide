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

import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.ConnectToApplicationAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenBeanClassAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenBeanDefinitionAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.RefreshApplicationAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelCollection;

/**
 * A simple view to host our graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphView extends ViewPart {

	private class LoadModelAction extends Action {

		private final LiveBeansModel model;

		public LoadModelAction(LiveBeansModel model) {
			super(model.getApplicationName(), Action.AS_RADIO_BUTTON);
			this.model = model;
		}

		@Override
		public boolean isChecked() {
			return model.equals(graphViewer.getInput());
		}

		@Override
		public void run() {
			setInput(model);
		}

	}

	public static final String VIEW_ID = "org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView";

	private BaseSelectionListenerAction openBeanClassAction;

	private BaseSelectionListenerAction openBeanDefAction;

	private Action connectApplicationAction;

	private PageBook pagebook;

	private GraphViewer graphViewer;

	private TreeViewer treeViewer;

	@Override
	public void createPartControl(Composite parent) {
		pagebook = new PageBook(parent, SWT.NONE);

		makeActions();
		hookPullDownMenu();

		createGraphViewer();
		createTreeViewer();

		pagebook.showPage(graphViewer.getControl());
	}

	private void createTreeViewer() {
		treeViewer = new TreeViewer(pagebook, SWT.NONE);
		treeViewer.setContentProvider(new LiveBeansTreeContentProvider());
		treeViewer.setLabelProvider(new LiveBeansTreeLabelProvider());
		treeViewer.setSorter(new ViewerSorter());
		getSite().setSelectionProvider(treeViewer);
	}

	private void createGraphViewer() {
		graphViewer = new GraphViewer(pagebook, SWT.NONE);
		graphViewer.setContentProvider(new LiveBeansGraphContentProvider());
		graphViewer.setLabelProvider(new LiveBeansGraphLabelProvider());
		graphViewer.setConnectionStyle(ZestStyles.CONNECTIONS_DIRECTED);
		// viewer.setNodeStyle(ZestStyles.NODES_FISHEYE);

		ExtendedDirectedGraphLayoutAlgorithm layout = new ExtendedDirectedGraphLayoutAlgorithm(
				LayoutStyles.NO_LAYOUT_NODE_RESIZING | SWT.HORIZONTAL);

		graphViewer.setLayoutAlgorithm(layout);
		graphViewer.applyLayout();
		getSite().setSelectionProvider(graphViewer);

		graphViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (openBeanDefAction != null && openBeanDefAction.isEnabled()) {
					openBeanDefAction.run();
				}
			}
		});

		graphViewer.addSelectionChangedListener(openBeanClassAction);
		graphViewer.addSelectionChangedListener(openBeanDefAction);

		hookContextMenu();
	}

	@Override
	public void dispose() {
		if (graphViewer != null) {
			graphViewer.removeSelectionChangedListener(openBeanClassAction);
			graphViewer.removeSelectionChangedListener(openBeanDefAction);
		}
		super.dispose();
	}

	private void fillContextMenu(IMenuManager menuManager) {
		menuManager.add(new Separator());
		menuManager.add(openBeanClassAction);
		menuManager.add(openBeanDefAction);
	}

	private void fillPullDownMenu(IMenuManager menuManager) {
		menuManager.add(connectApplicationAction);
		Set<LiveBeansModel> collection = LiveBeansModelCollection.getInstance().getCollection();
		if (collection.size() > 0) {
			menuManager.add(new Separator());
		}
		for (LiveBeansModel model : collection) {
			menuManager.add(new LoadModelAction(model));
		}
	}

	public LiveBeansModel getInput() {
		if (graphViewer != null) {
			Object obj = graphViewer.getInput();
			if (obj instanceof LiveBeansModel) {
				return (LiveBeansModel) obj;
			}
		}
		return null;
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

		Menu menu = menuManager.createContextMenu(graphViewer.getControl());
		graphViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuManager, graphViewer);

	}

	private void hookPullDownMenu() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuManager = bars.getMenuManager();
		menuManager.setRemoveAllWhenShown(true);
		fillPullDownMenu(bars.getMenuManager());

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillPullDownMenu(manager);
			}
		});
	}

	private void makeActions() {
		openBeanClassAction = new OpenBeanClassAction();
		openBeanClassAction.setEnabled(false);

		openBeanDefAction = new OpenBeanDefinitionAction();
		openBeanDefAction.setEnabled(false);

		connectApplicationAction = new ConnectToApplicationAction(this);

		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolbar = bars.getToolBarManager();
		toolbar.add(new RefreshApplicationAction(this));
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public void setInput(LiveBeansModel model) {
		if (graphViewer != null) {
			graphViewer.setInput(model);
		}
		if (treeViewer != null) {
			treeViewer.setInput(model);
		}
	}

}
