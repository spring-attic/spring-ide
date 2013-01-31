/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.ZestStyles;
import org.eclipse.zest.layouts.LayoutStyles;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.ConnectToApplicationAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.FilterInnerBeansAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.LoadModelAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenBeanClassAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.OpenBeanDefinitionAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.RefreshApplicationAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.ToggleGroupByAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.actions.ToggleViewModeAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelCollection;

/**
 * A simple view to host our graph
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansGraphView extends ViewPart {

	public static final String VIEW_ID = "org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView";

	public static final String PREF_DISPLAY_MODE = LiveGraphUiPlugin.PLUGIN_ID
			+ ".prefs.displayMode.LiveBeansGraphView";

	public static final String PREF_GROUP_MODE = LiveGraphUiPlugin.PLUGIN_ID + ".prefs.groupByMode.LiveBeansGraphView";

	public static final String PREF_FILTER_INNER_BEANS = LiveGraphUiPlugin.PLUGIN_ID
			+ ".prefs.filterInnerBeans.LiveBeansGraphView";

	public static final int DISPLAY_MODE_GRAPH = 0;

	public static final int DISPLAY_MODE_TREE = 1;

	public static final int GROUP_BY_RESOURCE = 0;

	public static final int GROUP_BY_CONTEXT = 1;

	private ToggleViewModeAction[] displayModeActions;

	private ToggleGroupByAction[] groupByActions;

	private final MultiViewerSelectionProvider selectionProvider;

	private BaseSelectionListenerAction openBeanClassAction;

	private BaseSelectionListenerAction openBeanDefAction;

	private FilterInnerBeansAction filterInnerBeansAction;

	private final InnerBeansViewerFilter innerBeansFilter;

	private LiveBeansModel activeInput;

	private Action connectApplicationAction;

	private PageBook pagebook;

	private GraphViewer graphViewer;

	private TreeViewer treeViewer;

	private final IPreferenceStore prefStore;

	private int activeDisplayMode;

	private int activeGroupByMode;

	public LiveBeansGraphView() {
		super();
		prefStore = LiveGraphUiPlugin.getDefault().getPreferenceStore();
		selectionProvider = new MultiViewerSelectionProvider();
		innerBeansFilter = new InnerBeansViewerFilter();
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

		graphViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (openBeanDefAction != null && openBeanDefAction.isEnabled()) {
					openBeanDefAction.run();
				}
			}
		});
	}

	@Override
	public void createPartControl(Composite parent) {
		pagebook = new PageBook(parent, SWT.NONE);
		getViewSite().setSelectionProvider(selectionProvider);
		makeActions();
		createGraphViewer();
		createTreeViewer();
		hookToolBar();
		hookPullDownMenu();
		hookContextMenu();
		selectionProvider.addSelectionChangedListener(openBeanClassAction);
		selectionProvider.addSelectionChangedListener(openBeanDefAction);
		setDisplayMode(prefStore.getInt(PREF_DISPLAY_MODE));
		setGroupByMode(prefStore.getInt(PREF_GROUP_MODE));
		setFilterInnerBeans(prefStore.getBoolean(PREF_FILTER_INNER_BEANS));
	}

	private void createTreeViewer() {
		treeViewer = new TreeViewer(pagebook, SWT.NONE);
		treeViewer.setContentProvider(new LiveBeansTreeContentProvider(this));
		treeViewer.setLabelProvider(new LiveBeansTreeLabelProvider());
		treeViewer.setSorter(new ViewerSorter());

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (openBeanDefAction != null && openBeanDefAction.isEnabled()) {
					openBeanDefAction.run();
				}
			}
		});
	}

	@Override
	public void dispose() {
		selectionProvider.removeSelectionChangedListener(openBeanClassAction);
		selectionProvider.removeSelectionChangedListener(openBeanDefAction);
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
			menuManager.add(new LoadModelAction(this, model));
		}
		if (activeDisplayMode == DISPLAY_MODE_TREE) {
			menuManager.add(new Separator());
			for (ToggleGroupByAction action : groupByActions) {
				menuManager.add(action);
			}
		}
		// if (activeDisplayMode == DISPLAY_MODE_GRAPH) {
		menuManager.add(new Separator());
		menuManager.add(filterInnerBeansAction);
		// }
	}

	public int getGroupByMode() {
		return activeGroupByMode;
	}

	public LiveBeansModel getInput() {
		return activeInput;
	}

	private void hookContextMenu() {
		MenuManager menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		fillContextMenu(menuManager);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ISelection selection = getViewSite().getSelectionProvider().getSelection();
				if (!selection.isEmpty()) {
					fillContextMenu(manager);
				}
			}
		});

		graphViewer.getControl().setMenu(menuManager.createContextMenu(graphViewer.getControl()));
		treeViewer.getControl().setMenu(menuManager.createContextMenu(treeViewer.getControl()));
		getViewSite().registerContextMenu(menuManager, selectionProvider);
	}

	private void hookPullDownMenu() {
		IActionBars bars = getViewSite().getActionBars();
		IMenuManager menuManager = bars.getMenuManager();
		menuManager.setRemoveAllWhenShown(true);
		fillPullDownMenu(menuManager);

		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				fillPullDownMenu(manager);
			}
		});
	}

	private void hookToolBar() {
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager toolbar = bars.getToolBarManager();
		for (ToggleViewModeAction displayModeAction : displayModeActions) {
			toolbar.add(displayModeAction);
		}
		toolbar.add(new Separator());
		toolbar.add(new RefreshApplicationAction(this));
	}

	private boolean isViewerVisible(Viewer viewer) {
		return viewer != null && !viewer.getControl().isDisposed() && viewer.getControl().isVisible();
	}

	private void makeActions() {
		openBeanClassAction = new OpenBeanClassAction();
		openBeanDefAction = new OpenBeanDefinitionAction();
		connectApplicationAction = new ConnectToApplicationAction(this);
		displayModeActions = new ToggleViewModeAction[] { new ToggleViewModeAction(this, DISPLAY_MODE_GRAPH),
				new ToggleViewModeAction(this, DISPLAY_MODE_TREE) };
		groupByActions = new ToggleGroupByAction[] { new ToggleGroupByAction(this, GROUP_BY_RESOURCE),
				new ToggleGroupByAction(this, GROUP_BY_CONTEXT) };
		filterInnerBeansAction = new FilterInnerBeansAction(this);
	}

	public void setDisplayMode(int mode) {
		if (mode == DISPLAY_MODE_GRAPH) {
			pagebook.showPage(graphViewer.getControl());
		}
		else if (mode == DISPLAY_MODE_TREE) {
			pagebook.showPage(treeViewer.getControl());
		}
		for (ToggleViewModeAction action : displayModeActions) {
			action.setChecked(mode == action.getDisplayMode());
		}
		activeDisplayMode = mode;
		prefStore.setValue(PREF_DISPLAY_MODE, mode);
	}

	@Override
	public void setFocus() {
		if (isViewerVisible(graphViewer)) {
			graphViewer.getControl().setFocus();
		}
		else if (isViewerVisible(treeViewer)) {
			treeViewer.getControl().setFocus();
		}
	}

	public void setGroupByMode(int mode) {
		activeGroupByMode = mode;
		if (isViewerVisible(treeViewer)) {
			treeViewer.refresh();
		}
		for (ToggleGroupByAction action : groupByActions) {
			action.setChecked(mode == action.getGroupByMode());
		}
		prefStore.setValue(PREF_GROUP_MODE, mode);
	}

	public void setInput(LiveBeansModel model) {
		activeInput = model;
		if (graphViewer != null) {
			graphViewer.setInput(activeInput);
		}
		if (treeViewer != null) {
			treeViewer.setInput(activeInput);
		}
	}

	public void setFilterInnerBeans(boolean filtered) {
		if (graphViewer != null) {
			if (filtered) {
				graphViewer.addFilter(innerBeansFilter);
			}
			else {
				graphViewer.removeFilter(innerBeansFilter);
			}
			graphViewer.applyLayout();
		}
		if (treeViewer != null) {
			if (filtered) {
				treeViewer.addFilter(innerBeansFilter);
			}
			else {
				treeViewer.removeFilter(innerBeansFilter);
			}
		}
		filterInnerBeansAction.setChecked(filtered);
		prefStore.setValue(PREF_FILTER_INNER_BEANS, filtered);
	}

	private class MultiViewerSelectionProvider implements ISelectionProvider {

		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			if (graphViewer != null) {
				graphViewer.addSelectionChangedListener(listener);
			}
			if (treeViewer != null) {
				treeViewer.addSelectionChangedListener(listener);
			}
		}

		public ISelection getSelection() {
			if (isViewerVisible(graphViewer)) {
				return graphViewer.getSelection();
			}
			if (isViewerVisible(treeViewer)) {
				return treeViewer.getSelection();
			}
			return null;
		}

		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			if (graphViewer != null) {
				graphViewer.removeSelectionChangedListener(listener);
			}
			if (treeViewer != null) {
				treeViewer.removeSelectionChangedListener(listener);
			}
		}

		public void setSelection(ISelection selection) {
			if (isViewerVisible(graphViewer)) {
				graphViewer.setSelection(selection);
			}
			else if (isViewerVisible(treeViewer)) {
				treeViewer.setSelection(selection);
			}
		}

	}

}
