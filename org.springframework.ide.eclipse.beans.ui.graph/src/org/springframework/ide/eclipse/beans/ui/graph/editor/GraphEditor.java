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

package org.springframework.ide.eclipse.beans.ui.graph.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.GEFPlugin;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.actions.GraphContextMenuProvider;
import org.springframework.ide.eclipse.beans.ui.graph.actions.OpenConfigFile;
import org.springframework.ide.eclipse.beans.ui.graph.actions.OpenJavaType;
import org.springframework.ide.eclipse.beans.ui.graph.actions.PrintAction;
import org.springframework.ide.eclipse.beans.ui.graph.actions.ShowInView;
import org.springframework.ide.eclipse.beans.ui.graph.model.Graph;
import org.springframework.ide.eclipse.beans.ui.graph.parts.GraphicalPartFactory;

public class GraphEditor extends EditorPart {

	public static final String EDITOR_ID = BeansGraphPlugin.PLUGIN_ID +
																	  ".editor";
	public static final String CONTEXT_MENU_ID = EDITOR_ID + ".contextmenu";

	private static final String ERROR_TITLE = "GraphEditor.error.title";
	private static final String ERROR_MSG_CYCLE = "GraphEditor.error.msg.cycle";

	private GraphOutlinePage outlinePage;
	private DefaultEditDomain editDomain;
	private GraphicalViewer graphicalViewer;
	private ActionRegistry actionRegistry;
	private Graph graph;

	public GraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	public void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof GraphEditorInput) {
			GraphEditorInput beansInput = (GraphEditorInput) input;
			graph = new Graph(beansInput);
			setTitle(beansInput.getName());
		} else {
			graph = null;
		}
	}

	/** 
	 * Sets the contents of the GraphicalViewer after it has been created.
	 * @see #createGraphicalViewer(Composite)
	 */
	protected void initializeGraphicalViewer() {
		if (graph.hasCycles()) {
			MessageDialog.openError(getSite().getShell(),
						   BeansGraphPlugin.getResourceString(ERROR_TITLE),
						   BeansGraphPlugin.getResourceString(ERROR_MSG_CYCLE));
		} else {
			graph.layout(getGraphicalViewer().getControl().getFont());
			getGraphicalViewer().setContents(graph);
		}
	}

	/**
	 * Called to configure the graphical viewer before it receives its contents.
	 * This is where the root editpart should be configured.
	 */
	protected void configureGraphicalViewer() {
		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
	
		List zoomLevels = new ArrayList(3);
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
	
		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);
	
		GraphicalViewer viewer = getGraphicalViewer();
		viewer.setRootEditPart(root);
		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
		viewer.setEditPartFactory(new GraphicalPartFactory());
		viewer.getControl().setBackground(ColorConstants.listBackground);
	
		ContextMenuProvider provider = new GraphContextMenuProvider(viewer,
														   getActionRegistry());
		viewer.setContextMenu(provider);
		getSite().registerContextMenu(CONTEXT_MENU_ID, provider, viewer);
	}

	public Object getAdapter(Class type) {
		if (type == IPropertySheetPage.class) {
			return getPropertySheetPage();
		}
		if (type == GraphicalViewer.class) {
			return getGraphicalViewer();
		}
		if (type == ActionRegistry.class) {
			return getActionRegistry();
		}
		if (type == CommandStack.class) {
			return getCommandStack();
		}
		if (type == IContentOutlinePage.class) {
			return getOutlinePage();
		}
		if (type == ZoomManager.class) {
			return getZoomManager();
		}
		return super.getAdapter(type);
	}

	protected IPropertySheetPage getPropertySheetPage() {
		PropertySheetPage page = new PropertySheetPage();
		page.setRootEntry(GEFPlugin.createUndoablePropertySheetEntry(
															getCommandStack()));
		return page;		
	}

	protected ZoomManager getZoomManager() {
		return ((ScalableFreeformRootEditPart)
					   getGraphicalViewer().getRootEditPart()).getZoomManager();
	}

	/**
	 * Returns the outline view.
	 * @return the overview
	 */
	protected GraphOutlinePage getOutlinePage() {
		if (outlinePage == null && getGraphicalViewer() != null) {
			RootEditPart rootEditPart = getGraphicalViewer().getRootEditPart();
			if (rootEditPart instanceof ScalableFreeformRootEditPart) {
				outlinePage = new GraphOutlinePage(
								   (ScalableFreeformRootEditPart) rootEditPart);
			}
		}
		return outlinePage;
	}

	/**
	 * Creates actions for this editor and registers them with the
	 * {@link ActionRegistry}.
	 */
	protected void createActions() {
		ActionRegistry registry = getActionRegistry();
		IAction action;
	
		action = new OpenJavaType(this);
		registry.registerAction(action);
	
		action = new OpenConfigFile(this);
		registry.registerAction(action);
	
		action = new ShowInView(this);
		registry.registerAction(action);

		action = new PrintAction(this);
		registry.registerAction(action);
	}

	/**
	 * Creates the GraphicalViewer on the specified <code>Composite</code>.
	 * @param parent the parent composite
	 */
	protected void createGraphicalViewer(Composite parent) {
		GraphicalViewer viewer = new ScrollingGraphicalViewer();
		viewer.createControl(parent);
		getSite().setSelectionProvider(viewer);
		setGraphicalViewer(viewer);
		configureGraphicalViewer();
		initializeGraphicalViewer();
	}

	/**
	 * Realizes the Editor by creating it's Control.
	 * <P>WARNING: This method may or may not be called by the workbench prior
	 * to {@link #dispose()}.
	 * @param parent the parent composite
	 */
	public void createPartControl(Composite parent) {
		createGraphicalViewer(parent);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getEditDomain().setActiveTool(null);
		getActionRegistry().dispose();
		super.dispose();
	}

	/**
	 * Lazily creates and returns the action registry.
	 * @return the action registry
	 */
	protected ActionRegistry getActionRegistry() {
		if (actionRegistry == null) {
			actionRegistry = new ActionRegistry();
		}
		return actionRegistry;
	}

	/**
	 * Returns the command stack.
	 * @return the command stack
	 */
	protected CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}

	/**
	 * Returns the edit domain.
	 * @return the edit domain
	 */
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}

	/**
	 * Returns the graphical viewer.
	 * @return the graphical viewer
	 */
	public GraphicalViewer getGraphicalViewer() {
		return graphicalViewer;
	}

	/**
	 * Sets the site and input for this editor then creates and initializes the
	 * actions.
	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
	 */
	public void init(IEditorSite site, IEditorInput input)
		throws PartInitException {
		setSite(site);
		setInput(input);
		initializeActionRegistry();
	}

	/**
	 * Initializes the ActionRegistry.  This registry may be used by {@link
	 * ActionBarContributor ActionBarContributors} and/or
	 * {@link ContextMenuProvider ContextMenuProviders}.
	 * <P>This method may be called on Editor creation, or lazily the first
	 * time {@link #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry() {
		createActions();
	}

	/**
	 * Sets the ActionRegistry for this EditorPart.
	 * @param registry the registry
	 */
	protected void setActionRegistry(ActionRegistry registry) {
		actionRegistry = registry;
	}

	/**
	 * Sets the EditDomain for this EditorPart.
	 * @param ed the domain
	 */
	protected void setEditDomain(DefaultEditDomain ed) {
		this.editDomain = ed;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		getGraphicalViewer().getControl().setFocus();
	}

	/**
	 * Sets the graphicalViewer for this EditorPart.
	 * @param viewer the graphical viewer
	 */
	protected void setGraphicalViewer(GraphicalViewer viewer) {
		getEditDomain().addViewer(viewer);
		this.graphicalViewer = viewer;
	}

	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * @param actionIds the list of IDs to update
	 */
	protected void updateActions(List actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction((String) iter.next());
			if (action instanceof UpdateAction) {
				((UpdateAction) action).update();
			}
		}
	}

	public void doSave(IProgressMonitor monitor) {
	}

	public void doSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
		dialog.setOriginalName("graph.jpg");
		dialog.create();
		dialog.setMessage(BeansGraphPlugin.getResourceString(
												"Editor.SaveAsDialog.message"));
		dialog.setOriginalName("graph.jpg");
		dialog.open();
		IPath path = dialog.getResult();
		if (path != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile file = workspace.getRoot().getFile(path);
			String ext = file.getFileExtension();
			if (ext == null || ext.length() == 0 ||
												!(ext.equalsIgnoreCase("jpg") ||
												 ext.equalsIgnoreCase("bmp"))) {
				ErrorDialog.openError(getSite().getShell(),
									  BeansGraphPlugin.getResourceString(
													  "Editor.SaveError.title"),
									  null,
									  BeansGraphPlugin.createErrorStatus(
									  		BeansGraphPlugin.getResourceString(
												 "Editor.SaveAsDialog.error")));
			} else {
				saveImage(file, (ext.equalsIgnoreCase("jpg") ? SWT.IMAGE_JPEG :
															   SWT.IMAGE_BMP));
			}
		}
	}

	/**
	 * Saves an encoded image from this viewer.
	 *
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 *               SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public void saveImage(final IFile file, final int format) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor)
													  throws CoreException {
				try {
					if (file.exists()) {
						file.setContents(new ByteArrayInputStream(
									createImage(format)), true, false, monitor);
					} else {
						file.create(new ByteArrayInputStream(
									   createImage(format)), true, monitor);
					}
				} catch (CoreException e) {
					ErrorDialog.openError(getSite().getShell(),
								BeansGraphPlugin.getResourceString(
												  "Editor.SaveError.title"),
								BeansGraphPlugin.getResourceString(
												   "Editor.SaveError.text"),
								e.getStatus());
				}
			}
		};

		try {
			Shell shell = getSite().getWorkbenchWindow().getShell();
			new ProgressMonitorDialog(shell).run(false, true, op);
		} catch (InvocationTargetException e) {
			BeansGraphPlugin.log(e);
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Returns the bytes of an encoded image from this viewer.
	 *
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 *               SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public byte[] createImage(int format) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Device device = getGraphicalViewer().getControl().getDisplay();
		LayerManager lm = (LayerManager)
				getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID);
		IFigure figure = lm.getLayer(LayerConstants.PRINTABLE_LAYERS);
		Rectangle r = figure.getClientArea();

		Image image = null;
		GC gc = null;
		Graphics g = null;
		try {
			image = new Image(device, r.width, r.height);
			gc = new GC(image);
			g = new SWTGraphics(gc);
			g.translate(r.x * -1, r.y * -1);

			figure.paint(g);

			ImageLoader imageLoader = new ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			imageLoader.save(result, format);
		} finally {
			if (g != null) {
				g.dispose();
			}
			if (gc != null) {
				gc.dispose();
			}
			if (image != null) {
				image.dispose();
			}
		}
		return result.toByteArray();
	}

	public void gotoMarker(IMarker marker) {
	}

	public boolean isDirty() {
		return false;
	}

	public boolean isSaveAsAllowed() {
		return true;
	}
}
