/*
 * Copyright 2002-2006 the original author or authors.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInSource;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.IShowInTargetList;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.views.navigator.LocalSelectionTransfer;
import org.eclipse.wst.xml.ui.internal.tabletree.XMLMultiPageEditorPart;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.IPreferencesConstants;
import org.springframework.ide.eclipse.beans.ui.views.actions.CollapseAllAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.LexicalSortingAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.LinkWithEditorAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.OpenBeanClassAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.OpenConfigFileAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.OpenPropertiesAction;
import org.springframework.ide.eclipse.beans.ui.views.actions.PropertySheetAction;
import org.springframework.ide.eclipse.beans.ui.views.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConstructorArgumentNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.views.model.ModelSorter;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.views.model.PropertyNode;
import org.springframework.ide.eclipse.beans.ui.views.model.RootNode;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * This is a tree view of all Spring projects and their configs and config
 * sets within the workspace.
 * @author Torsten Juergeleit
 */
public class BeansView extends ViewPart implements IBeansView, IShowInSource,
											IShowInTarget, ISelectionListener {
	public static final String VIEW_ID = BeansUIPlugin.PLUGIN_ID +
															 ".views.beansView";
	public static final String CONTEXT_MENU_ID = "#BeansViewContext";

	private TreeViewer treeViewer;
	private RootNode rootNode;

	private OpenConfigFileAction openConfigFileAction;
	private OpenBeanClassAction openBeanClassAction;
	private OpenPropertiesAction openPropertiesAction;

	private boolean isSortingEnabled;
	private boolean isLinkingEnabled;

	public BeansView() {
		this.rootNode = null;
		IPreferenceStore store = BeansUIPlugin.getDefault().getPreferenceStore();
		setSortingEnabled(store.getBoolean(IPreferencesConstants.VIEW_SORT));
		setLinkingEnabled(store.getBoolean(IPreferencesConstants.VIEW_LINK));
	}

	public boolean isSortingEnabled() {
		return isSortingEnabled;
	}

	public void setSortingEnabled(boolean isSortingEnabled) {
		this.isSortingEnabled = isSortingEnabled;
		IPreferenceStore store = BeansUIPlugin.getDefault()
				.getPreferenceStore();
		store.setValue(IPreferencesConstants.VIEW_SORT, isSortingEnabled);
		BeansUIPlugin.getDefault().savePluginPreferences();
		if (treeViewer != null) {
			treeViewer.setSorter(isSortingEnabled ? new ModelSorter(true)
					: new ModelSorter(false));
		}
	}

	public boolean isLinkingEnabled() {
		return isLinkingEnabled;
	}

	public void setLinkingEnabled(boolean isLinkingEnabled) {
		this.isLinkingEnabled = isLinkingEnabled;
		IPreferenceStore store = BeansUIPlugin.getDefault().getPreferenceStore();
		store.setValue(IPreferencesConstants.VIEW_LINK, isLinkingEnabled);
		BeansUIPlugin.getDefault().savePluginPreferences();
	}

	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		super.dispose();
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
		treeViewer = createViewer(parent);

		initializeActions();
		createContextMenu(treeViewer);
		fillToolBar();
	
		getSite().setSelectionProvider(treeViewer);
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(this);
	}

	private void initializeActions() {
		openConfigFileAction = new OpenConfigFileAction(this);
		openBeanClassAction = new OpenBeanClassAction(this);
		openPropertiesAction = new OpenPropertiesAction(this);
	}

	private TreeViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL |
										   SWT.V_SCROLL);
		viewer.setContentProvider(new BeansViewContentProvider(this));
		viewer.setLabelProvider(new ModelLabelProvider());
		viewer.setInput(getRootNode());
		viewer.setSorter(new ModelSorter(isSortingEnabled));
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
		});
        viewer.addDropSupport(DND.DROP_MOVE | DND.DROP_COPY,
        			   new Transfer[] { LocalSelectionTransfer.getInstance() },
        			   new BeansViewDropAdapter(viewer));
        viewer.addDragSupport(DND.DROP_MOVE | DND.DROP_COPY,
 			   new Transfer[] { LocalSelectionTransfer.getInstance() },
 			   new BeansViewDragAdapter(viewer));
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
		if (openConfigFileAction.isEnabled()) {
			menuMgr.add(openConfigFileAction);
		}
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
	
		mgr.add(new LexicalSortingAction(this));
		mgr.add(new PropertySheetAction(this));
		mgr.add(new Separator());
		mgr.add(new CollapseAllAction(treeViewer));
		mgr.add(new LinkWithEditorAction(this));

		mgr.update(false);	
	}

	private void handleDoubleClick(DoubleClickEvent event) {
		INode node = getSelectedNode();
		if (node instanceof ProjectNode || node instanceof ConfigSetNode) {

			// Expand or collapse selected project or config set
			if (treeViewer.getExpandedState(node)) {
				treeViewer.collapseToLevel(node, TreeViewer.ALL_LEVELS);
			} else {
				treeViewer.expandToLevel(node, 1);
			}
		} else {

			// Open selected config/bean/constructor/property in editor
			IResource resource = getResourceFromSelectedNode();
			if (resource instanceof IFile && resource.exists()) {
				int line = getStartLineFromSelectedNode();
				SpringUIUtils.openInEditor((IFile) resource, line);
			}
		}
	}

	public boolean show(ShowInContext context) {

		// First check input object for an instance of BeansViewLocation 
		Object input = context.getInput();
		if (input instanceof BeansViewLocation) {
			return showLocation((BeansViewLocation) input);
		} else if (input instanceof IModelElement) {
			return showLocation(BeansUIUtils.getBeansViewLocation(
													   (IModelElement) input));
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
			if (element instanceof IModelElement) {
				return showLocation(BeansUIUtils.getBeansViewLocation(
													 (IModelElement) element));
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
				} else if (location.hasConfigSetName()) {
					ConfigSetNode configSet = project.getConfigSet(
												  location.getConfigSetName());
					if (configSet != null) {
						node = configSet;
						if (location.hasBeanName()) {;
							BeanNode bean = configSet.getBean(
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
		if (resource instanceof IFile) {
			node = getRootNode().getConfig((IFile) resource);
		} else if (resource instanceof IProject) {
			node = getRootNode().getProject(resource.getName());
		} else if (resource instanceof IAdaptable &&
				((IAdaptable) resource).getAdapter(IJavaProject.class) != null) {
			node = getRootNode().getProject(resource.getName());
		}
		if (node != null) {
			treeViewer.setSelection(new StructuredSelection(node), true);
			return true;
		}
		return false;
	}

	public ShowInContext getShowInContext() {
		IResource resource = getResourceFromSelectedNode();
		if (resource != null && resource.exists()) {
			ISelection selection = new StructuredSelection(resource);
			return new ShowInContext(null, selection);
		}
		return null;
	}

	public void refresh() {
		treeViewer.getControl().getDisplay().asyncExec(new Runnable() {		
			public void run() {
				BeansViewState state = getState();
				treeViewer.refresh();
				setState(state);
			}
		});
	}

	private BeansViewState getState() {
		BeansViewState state = new BeansViewState();

		// Save state of expanded elements
		Object[] expandedElements = treeViewer.getExpandedElements();
		for (int i = 0; i < expandedElements.length; i++) {
			INode node = (INode) expandedElements[i];
			if (node.getElement() != null) {
				state.expandedElements.add(node.getElement().getElementID());
			}
		}

		// Save state of selected elements
		ISelection selection = treeViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Iterator selectedElements = ((IStructuredSelection)
														 selection).iterator();
			while (selectedElements.hasNext()) {
				INode node = (INode) selectedElements.next();
				state.selectedElements.add(node.getID());
			}
		}
		return state;
	}

	private void setState(BeansViewState state) {

		// Restore state of expanded elements
		List expandedElements = new ArrayList();
		Iterator expandedElementIDs = state.expandedElements.iterator();
		while (expandedElementIDs.hasNext()) {
			String elementID = (String) expandedElementIDs.next();
			INode node = getRootNode().getNode(elementID);
			if (node != null) {
				expandedElements.add(node);
			}
		}
		if (!expandedElements.isEmpty()) {
			treeViewer.setExpandedElements(expandedElements.toArray(
										  new INode[expandedElements.size()]));
		}

		// Restore state of selected elements
		List selectedElements = new ArrayList();
		Iterator selectedElementIDs = state.selectedElements.iterator();
		while (selectedElementIDs.hasNext()) {
			String elementID = (String) selectedElementIDs.next();
			INode node = getRootNode().getNode(elementID);
			if (node != null) {
				selectedElements.add(node);
			}
		}
		if (!selectedElements.isEmpty()) {
			treeViewer.setSelection(new StructuredSelection(selectedElements));
		}
	}

	private static class BeansViewState {
		public List expandedElements = new ArrayList();
		public List selectedElements = new ArrayList();
	}

	public INode getSelectedNode() {
		ISelection selection = getViewer().getSelection();
		if (selection instanceof IStructuredSelection &&
								((IStructuredSelection)selection).size() == 1) {
			Object elem = ((IStructuredSelection) selection).getFirstElement();
			if (elem instanceof INode) {
				return (INode) elem;
			}
		}
		return null;
	}

	public IResource getResourceFromSelectedNode() {
		INode node = getSelectedNode();
		if (node instanceof ProjectNode) {
			return ((ProjectNode) node).getProject().getProject();
		} else if (node instanceof ConfigNode) {
			return ((ConfigNode) node).getConfigFile();
		} else if (node instanceof BeanNode) {
			return ((BeanNode) node).getConfigNode().getConfigFile();
		} else if (node instanceof ConstructorArgumentNode) {
			return ((ConstructorArgumentNode)
										  node).getConfigNode().getConfigFile();
		} else if (node instanceof PropertyNode) {
			return ((PropertyNode) node).getConfigNode().getConfigFile();
		}
		return null;
	}

	public int getStartLineFromSelectedNode() {
		INode node = getSelectedNode();
		if (node instanceof BeanNode) {
			return ((BeanNode) node).getStartLine();
		} else if (node instanceof ConstructorArgumentNode) {
			return ((ConstructorArgumentNode) node).getStartLine();
		} else if (node instanceof PropertyNode) {
			return ((PropertyNode) node).getStartLine();
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

	/**
	 * If linking is enabled (via toolbar action) the links to beans XML
	 * editor. 
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (isLinkingEnabled() && isPartVisible()
				&& part instanceof XMLMultiPageEditorPart
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection sselection = (IStructuredSelection) selection;
			if (sselection.size() == 1) {
				Object selectedObj = sselection.getFirstElement();
				if (selectedObj instanceof Element) {
					IFile file = getEditorFile(part);
					if (file != null
							&& SpringCoreUtils.isSpringProject(file
									.getProject())) {
						linkToBeansXmlEditor(file, (Element) selectedObj);
					}
				}
			}
		}
	}

	private boolean isPartVisible() {
		return getSite().getPage().isPartVisible(this);
	}

	private IFile getEditorFile(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart) part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				return ((IFileEditorInput) input).getFile();
			}
		}
		return null;
	}

	private void linkToBeansXmlEditor(IFile file, Element element) {
		String elementName = element.getNodeName();
		Node parentNode = element.getParentNode();
		String parentName = parentNode.getNodeName();
		if ("bean".equals(elementName) && "beans".equals(parentName)) {
			Node idAttribute = element.getAttributeNode("id");
			if (idAttribute != null && idAttribute.getNodeValue() != null) {
				BeansViewLocation location = new BeansViewLocation();
				location.setProjectName(file.getProject().getName());
				location.setConfigName(file.getProjectRelativePath()
						.toString());
				location.setBeanName(idAttribute.getNodeValue());
				showLocation(location);
			}
		} else if ("property".equals(elementName) && "bean".equals(parentName)
				&& "beans".equals(parentNode.getParentNode().getNodeName())) {
			Node idAttribute = ((Element) parentNode).getAttributeNode("id");
			Node nameAttribute = element.getAttributeNode("name");
			if (idAttribute != null && idAttribute.getNodeValue() != null
					&& nameAttribute != null
					&& nameAttribute.getNodeValue() != null) {
				BeansViewLocation location = new BeansViewLocation();
				location.setProjectName(file.getProject().getName());
				location.setConfigName(file.getProjectRelativePath()
						.toString());
				location.setBeanName(idAttribute.getNodeValue());
				location.setPropertyName(nameAttribute.getNodeValue());
				showLocation(location);
			}
		}
	}
}
