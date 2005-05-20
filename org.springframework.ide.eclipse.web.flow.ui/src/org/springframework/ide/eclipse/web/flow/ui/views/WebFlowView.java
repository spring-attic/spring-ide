/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.web.flow.ui.WebFlowUIPlugin;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ModelLabelDecorator;
import org.springframework.ide.eclipse.web.flow.ui.model.ModelLabelProvider;
import org.springframework.ide.eclipse.web.flow.ui.model.ProjectNode;
import org.springframework.ide.eclipse.web.flow.ui.model.RootNode;
import org.springframework.ide.eclipse.web.flow.ui.views.actions.CollapseAllAction;
import org.springframework.ide.eclipse.web.flow.ui.views.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.web.flow.ui.views.actions.OpenPropertiesAction;
import org.springframework.ide.eclipse.web.flow.ui.views.actions.PropertySheetAction;

public class WebFlowView extends ViewPart implements IWebFlowView {

    public static final String CONTEXT_MENU_ID = "#WebFlowViewContext";

    public static final String VIEW_ID = WebFlowUIPlugin.PLUGIN_ID
            + ".views.beansView";

    public static final IViewPart showView() {
        try {
            return WebFlowUIPlugin.getActiveWorkbenchPage().showView(VIEW_ID);
        }
        catch (PartInitException e) {
            WebFlowUIPlugin.log(e);
        }
        return null;
    }

    private OpenPropertiesAction openPropertiesAction;

    private RootNode rootNode;

    private TreeViewer treeViewer;

    public WebFlowView() {
        this.rootNode = null;
    }

    private void createContextMenu(final Viewer viewer) {
        MenuManager menuMgr = new MenuManager();
        menuMgr.setRemoveAllWhenShown(true);
        menuMgr.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillContextMenu(mgr);
            }
        });

        Menu menu = menuMgr.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);

        // Register the context menu such that other plugins may contribute
        getSite().registerContextMenu(CONTEXT_MENU_ID, menuMgr, viewer);
    }

    public void createPartControl(Composite parent) {
        TreeViewer viewer = createViewer(parent);
        this.treeViewer = viewer;

        initializeActions();
        createContextMenu(viewer);
        fillToolBar();

        getSite().setSelectionProvider(viewer);
    }

    private TreeViewer createViewer(Composite parent) {
        TreeViewer viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL);
        viewer.setContentProvider(new WebFlowViewContentProvider(this));
        viewer.setLabelProvider(new DecoratingLabelProvider(
                new ModelLabelProvider(), new ModelLabelDecorator()));
        viewer.setInput(getRootNode());
        viewer.setSorter(new ViewerSorter() {
            public int compare(Viewer viewer, Object e1, Object e2) {
                return e1.toString().compareToIgnoreCase(e2.toString());
            }
        });

        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick(event);
            }
        });

        getViewSite().setSelectionProvider(viewer);
        return viewer;
    }

    private void fillContextMenu(IMenuManager menuMgr) {
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
        menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS
                + "-end"));
        menuMgr.add(openPropertiesAction);
    }

    private void fillToolBar() {
        IToolBarManager mgr = getViewSite().getActionBars().getToolBarManager();
        mgr.removeAll();

        mgr.add(new CollapseAllAction(treeViewer));
        mgr.add(new LexicalSortingAction(treeViewer));
        mgr.add(new PropertySheetAction(this));

        mgr.update(false);
    }

    public Object getAdapter(Class adapter) {
        return super.getAdapter(adapter);
    }

    private RootNode getRootNode() {
        if (rootNode == null) {
            rootNode = new RootNode(this);
            rootNode.reloadConfigs();
        }
        return rootNode;
    }

    private IResource getSelectedResource(ISelection selection) {
        if (selection instanceof IStructuredSelection
                && ((IStructuredSelection) selection).size() == 1) {
            Object elem = ((IStructuredSelection) selection).getFirstElement();
            if (elem instanceof ProjectNode) {
                return ((ProjectNode) elem).getProject();
            }
            else if (elem instanceof ConfigNode) {
                return ((ConfigNode) elem).getConfigFile();
            }
        }
        return null;
    }

    public ShowInContext getShowInContext() {
        IResource resource = getSelectedResource(getViewer().getSelection());
        if (resource != null && resource.exists()) {
            ISelection selection = new StructuredSelection(resource);
            return new ShowInContext(null, selection);
        }
        return null;
    }

    private int getStartLineFromSelectedNode(ISelection selection) {
        if (selection instanceof IStructuredSelection
                && ((IStructuredSelection) selection).size() == 1) {
            Object elem = ((IStructuredSelection) selection).getFirstElement();
        }
        return -1;
    }

    public Viewer getViewer() {
        return treeViewer;
    }

    private void handleDoubleClick(DoubleClickEvent event) {
        ISelection selection = event.getSelection();
        if (selection instanceof IStructuredSelection
                && ((IStructuredSelection) selection).size() == 1) {
            Object elem = ((IStructuredSelection) selection).getFirstElement();
            if (elem instanceof ProjectNode || elem instanceof ConfigSetNode) {

                // expand or collapse selected project or config set
                if (treeViewer.getExpandedState(elem)) {
                    treeViewer.collapseToLevel(elem, TreeViewer.ALL_LEVELS);
                }
                else {
                    treeViewer.expandToLevel(elem, 1);
                }
            }
            else {

                // open selected config/bean/constructor/property in editor
                IResource resource = getSelectedResource(selection);
                if (resource instanceof IFile && resource.exists()) {
                    int line = getStartLineFromSelectedNode(selection);
                    SpringUIUtils.openInEditor((IFile) resource, line);
                }
            }
        }
    }

    private void initializeActions() {
        openPropertiesAction = new OpenPropertiesAction(this);
    }

    public void refresh() {
        treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                treeViewer.refresh();
            }
        });
    }

    public void setFocus() {
        treeViewer.getControl().setFocus();
    }
}