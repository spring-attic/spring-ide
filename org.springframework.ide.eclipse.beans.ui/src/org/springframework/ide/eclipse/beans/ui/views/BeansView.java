/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.views;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
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
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.ConstructorArgumentNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;
import org.springframework.ide.eclipse.beans.ui.model.ModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.model.PropertyNode;
import org.springframework.ide.eclipse.beans.ui.model.RootNode;
import org.springframework.ide.eclipse.beans.ui.views.actions.CollapseAllAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.OpenBeanClassAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.OpenPropertiesAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.PropertySheetAction;

public class BeansView extends ViewPart implements IBeansView, IShowInSource,
																 IShowInTarget {
	public static final String VIEW_ID = BeansUIPlugin.PLUGIN_ID +
															 ".views.beansView";
	public static final String CONTEXT_MENU_ID = "#BeansViewContext";

	private TreeViewer treeViewer;
	private RootNode rootNode;

	private OpenBeanClassAction openBeanClassAction;
	private OpenPropertiesAction openPropertiesAction;

	public BeansView() {
		this.rootNode = null;
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IShowInTargetList.class) {
			return new IShowInTargetList() {
				public String[] getShowInTargetIds() {
					return new String[] { JavaUI.ID_PACKAGES,
										  IPageLayout.ID_RES_NAV };
				}

			};
		}
		return super.getAdapter(adapter);
	}

	public Viewer getViewer() {
		return treeViewer;
	}

	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	public void createPartControl(Composite parent) {
		TreeViewer viewer = createViewer(parent);
		this.treeViewer = viewer;

		initializeActions();
		createContextMenu(viewer);
		fillToolBar();
	
		getSite().setSelectionProvider(viewer);
	}

	private void initializeActions() {
		openBeanClassAction = new OpenBeanClassAction(this);
		openPropertiesAction = new OpenPropertiesAction(this);
	}

	private TreeViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL |
										   SWT.V_SCROLL);
		viewer.setContentProvider(new BeansViewContentProvider(this));
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

	private RootNode getRootNode() {
		if (rootNode == null) {
			rootNode = new RootNode(this);
			rootNode.reloadConfigs();
		}
		return rootNode;
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

	private void fillContextMenu(IMenuManager menuMgr) {
		if (openBeanClassAction.isEnabled()) {
			menuMgr.add(openBeanClassAction);
		}
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuMgr.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS +
								  "-end"));
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

	private void handleDoubleClick(DoubleClickEvent event) {
		ISelection selection = event.getSelection();
		if (selection instanceof IStructuredSelection &&
								((IStructuredSelection)selection).size() == 1) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof ProjectNode || elem instanceof ConfigSetNode) {

				// expand or collapse selected project or config set
				if (treeViewer.getExpandedState(elem)) {
					treeViewer.collapseToLevel(elem, TreeViewer.ALL_LEVELS);
				} else {
					treeViewer.expandToLevel(elem, 1);
				}
			} else {

				// open selected config/bean/constructor/property in editor
				IResource resource = getSelectedResource(selection);
				if (resource instanceof IFile && resource.exists()) {
					int line = getStartLineFromSelectedNode(selection);
					BeansUIUtils.openInEditor((IFile) resource, line);
				}
			}
		}
	}

	public boolean show(ShowInContext context) {

		// First check input object for an instance of BeansViewLocation 
		Object input = context.getInput();
		if (input instanceof BeansViewLocation) {
			return showLocation((BeansViewLocation) input);
		} else if (input instanceof IBeansModelElement) {
			return showLocation(BeansUIUtils.getBeansViewLocation(
												   (IBeansModelElement) input));
		} else if (input instanceof IAdaptable) {
			Object resource = ((IAdaptable) input).getAdapter(IResource.class);
			if (resource != null) {
				if (showResource((IResource) resource)) {
					return true;
				}
			}
		}

		// Finally check selection object for an instance of IResource 
		ISelection selection = context.getSelection();
		if (selection != null && selection instanceof IStructuredSelection &&
							   ((IStructuredSelection) selection).size() == 1) {
			Object element = ((IStructuredSelection)
												   selection).getFirstElement();
			if (element instanceof IBeansModelElement) {
				return showLocation(BeansUIUtils.getBeansViewLocation(
												 (IBeansModelElement) element));
			} else if (element instanceof IResource) {
				return showResource((IResource) element);
			} else if (input instanceof IAdaptable) {
				element = ((IAdaptable) input).getAdapter(IResource.class);
				if (element != null) {
					return showResource((IResource) element);
				}
			}
		}
		return false;
	}

	private boolean showLocation(BeansViewLocation location) {
		if (location.hasProjectName()) {
			ProjectNode project = getRootNode().getProject(
													 location.getProjectName());
			if (project != null) {
				INode node = project;
				if (location.hasConfigName()) {
					ConfigNode config = project.getConfig(
													  location.getConfigName());
					if (config != null) {
						node = config;
						if (location.hasBeanName()) {;
							BeanNode bean = config.getBean(
														location.getBeanName());
							if (bean != null) {
								node = bean;
								if (location.hasPropertyName()) {
									PropertyNode property = bean.getProperty(
													location.getPropertyName());
									if (property != null) {
										node = property;
									}
								}
							}
						} 
					}
				}
				treeViewer.setSelection(new StructuredSelection(node), true);
				return true;
			}
		}
		return false;
	}

	private boolean showResource(IResource resource) {
		INode node = null;
		if (resource instanceof IProject) {
			node = getRootNode().getProject(resource.getName());
		} else if (resource instanceof IJavaProject) {
			node = getRootNode().getProject(resource.getName());
		} else if (resource instanceof IFile) {
			node = getRootNode().getConfig((IFile) resource);
		}
		if (node != null) {
			treeViewer.setSelection(new StructuredSelection(node), true);
			return true;
		}
		return false;
	}

	public ShowInContext getShowInContext() {
		IResource resource = getSelectedResource(getViewer().getSelection());
		if (resource != null && resource.exists()) {
			ISelection selection = new StructuredSelection(resource);
			return new ShowInContext(null, selection);
		}
		return null;
	}

	public void refresh() {
		treeViewer.getControl().getDisplay().asyncExec(new Runnable() {		
			public void run() {
				treeViewer.refresh();
			}
		});
	}

	private IResource getSelectedResource(ISelection selection) {
		if (selection instanceof IStructuredSelection &&
								((IStructuredSelection)selection).size() == 1) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof ProjectNode) {
				return ((ProjectNode) elem).getProject();
			} else if (elem instanceof ConfigNode) {
				return ((ConfigNode) elem).getConfigFile();
			} else if (elem instanceof BeanNode) {
				return ((BeanNode) elem).getConfigNode().getConfigFile();
			} else if (elem instanceof ConstructorArgumentNode) {
				return ((ConstructorArgumentNode)
										  elem).getConfigNode().getConfigFile();
			} else if (elem instanceof PropertyNode) {
				return ((PropertyNode) elem).getConfigNode().getConfigFile();
			}
		}
		return null;
	}

	private int getStartLineFromSelectedNode(ISelection selection) {
		if (selection instanceof IStructuredSelection &&
								((IStructuredSelection)selection).size() == 1) {
			Object elem = ((IStructuredSelection)selection).getFirstElement();
			if (elem instanceof BeanNode) {
				return ((BeanNode) elem).getStartLine();
			} else if (elem instanceof ConstructorArgumentNode) {
				return ((ConstructorArgumentNode) elem).getStartLine();
			} else if (elem instanceof PropertyNode) {
				return ((PropertyNode) elem).getStartLine();
			}
		}
		return -1;
	}

	public static final IViewPart showView() {
		try {
			return BeansUIPlugin.getActiveWorkbenchPage().showView(VIEW_ID);
		} catch (PartInitException e) {
			BeansUIPlugin.log(e);
		}
		return null;
	}
}
