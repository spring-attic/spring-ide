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
package org.springframework.ide.eclipse.beans.ui.graph.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
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
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.ActionBarContributor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.properties.UndoablePropertySheetEntry;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.actions.GraphContextMenuProvider;
import org.springframework.ide.eclipse.beans.ui.graph.actions.OpenConfigFile;
import org.springframework.ide.eclipse.beans.ui.graph.actions.OpenJavaType;
import org.springframework.ide.eclipse.beans.ui.graph.model.Graph;
import org.springframework.ide.eclipse.beans.ui.graph.parts.GraphicalPartFactory;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * Beans Graph Editor
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class GraphEditor extends EditorPart implements ISelectionListener {

	public static final String EDITOR_ID = BeansGraphPlugin.PLUGIN_ID
			+ ".editor";

	public static final String CONTEXT_MENU_ID = EDITOR_ID + ".contextmenu";

	private GraphOutlinePage outlinePage;

	private DefaultEditDomain editDomain;

	private GraphicalViewer graphicalViewer;

	private ActionRegistry actionRegistry;

	private SelectionSynchronizer synchronizer;

	private List selectionActions = new ArrayList();

	private List propertyActions = new ArrayList();

	private Graph graph;

	private IModelChangeListener modelChangeListener = new GraphEditorInputModelChangeListener();

	public GraphEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}

	/**
	 * Internal {@link IModelChangeListener} that tracks changes in the graphs
	 * underlying element and context and triggers a refresh if required.
	 */
	private class GraphEditorInputModelChangeListener implements
			IModelChangeListener {

		public void elementChanged(ModelChangeEvent event) {
			final GraphEditorInput beansInput = (GraphEditorInput) getEditorInput();
			boolean refresh = false;

			IModelElement changedElement = event.getElement();
			IModelElement originalInputElement = beansInput.getElement();
			IModelElement originalContextElement = beansInput.getElement();
			
			// check if changes appeared in a spring config file
			if (changedElement instanceof IBeansConfig) {
				IResource changedResource = ((IBeansConfig) changedElement)
						.getElementResource();
				refresh = checkForRefresh(changedResource, originalInputElement)
						|| checkForRefresh(changedResource,
								originalContextElement);
			}
			// changes occured in the project configuration; added config file
			// to project or beans config set
			else if (changedElement instanceof IBeansProject) {
				IBeansProject beansProject = (IBeansProject) changedElement;
				refresh = (BeansModelUtils.getChildForElement(beansProject,
						originalInputElement) != null || BeansModelUtils
						.getChildForElement(beansProject,
								originalContextElement) != null);
			}

			if (refresh) {
				Display display = getSite().getShell().getDisplay();
				display.asyncExec(new Runnable() {
					public void run() {
						beansInput.init();
						setInput(beansInput);
						initializeGraphicalViewer();
					}
				});
			}
		}

		private boolean checkForRefresh(IResource changedResource,
				IModelElement originalInputElement) {
			boolean refresh = false;

			if (originalInputElement instanceof IBean) {
				IResource originalResource = ((IBean) originalInputElement)
						.getElementResource();
				refresh = originalResource.equals(changedResource);
			}
			else if (originalInputElement instanceof IBeansConfig) {
				IResource originalResource = ((IBeansConfig) originalInputElement)
						.getElementResource();
				refresh = originalResource.equals(changedResource);
			}
			else if (originalInputElement instanceof IBeansConfigSet) {
				refresh = ((IBeansConfigSet) originalInputElement)
						.hasConfig((IFile) changedResource);
			}
			return refresh;
		}
	}

	@Override
	public void setInput(IEditorInput input) {
		super.setInput(input);
		if (input instanceof GraphEditorInput) {
			GraphEditorInput beansInput = (GraphEditorInput) input;
			graph = new Graph(beansInput);
			setPartName(beansInput.getName());
			setContentDescription(beansInput.getToolTipText());
		}
		else {
			graph = null;
		}
	}

	/**
	 * Sets the contents of the GraphicalViewer after it has been created.
	 * @see #createGraphicalViewer(Composite)
	 */
	protected void initializeGraphicalViewer() {
		if (graph != null) {
			graph.layout(getGraphicalViewer().getControl().getFont());
			getGraphicalViewer().setContents(graph);
		}
	}

	/**
	 * Called to configure the graphical viewer before it receives its contents.
	 * This is where the root editpart should be configured.
	 */
	protected void configureGraphicalViewer() {
		ScalableRootEditPart root = new ScalableRootEditPart();

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

	@Override
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
		if (type == EditPart.class && getGraphicalViewer() != null) {
			return getGraphicalViewer().getRootEditPart();
		}
		if (type == IFigure.class && getGraphicalViewer() != null) {
			return ((GraphicalEditPart) getGraphicalViewer().getRootEditPart())
					.getFigure();
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
		page.setRootEntry(new UndoablePropertySheetEntry(getCommandStack()));
		return page;
	}

	protected ZoomManager getZoomManager() {
		return ((ScalableRootEditPart) getGraphicalViewer().getRootEditPart())
				.getZoomManager();
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
	 * <P>
	 * WARNING: This method may or may not be called by the workbench prior to
	 * {@link #dispose()}.
	 * @param parent the parent composite
	 */
	@Override
	public void createPartControl(Composite parent) {
		createGraphicalViewer(parent);
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.removeSelectionListener(this);
		getEditDomain().setActiveTool(null);
		getActionRegistry().dispose();

		// remove the change listener
		BeansCorePlugin.getModel().removeChangeListener(modelChangeListener);

		super.dispose();
	}

	/**
	 * @see org.eclipse.ui.part.WorkbenchPart#firePropertyChange(int)
	 */
	@Override
	protected void firePropertyChange(int property) {
		super.firePropertyChange(property);
		updateActions(propertyActions);
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
	 * Returns the list of {@link IAction IActions} dependant on property
	 * changes in the Editor. These actions should implement the
	 * {@link UpdateAction} interface so that they can be updated in response to
	 * property changes. An example is the "Save" action.
	 * @return the list of property-dependant actions
	 */
	protected List getPropertyActions() {
		return propertyActions;
	}

	/**
	 * Returns the list of {@link IAction IActions} dependant on changes in the
	 * workbench's {@link ISelectionService}. These actions should implement
	 * the {@link UpdateAction} interface so that they can be updated in
	 * response to selection changes. An example is the Delete action.
	 * @return the list of selection-dependant actions
	 */
	protected List getSelectionActions() {
		return selectionActions;
	}

	/**
	 * Returns the selection syncronizer object. The synchronizer can be used to
	 * sync the selection of 2 or more EditPartViewers.
	 * @return the syncrhonizer
	 */
	protected SelectionSynchronizer getSelectionSynchronizer() {
		if (synchronizer == null) {
			synchronizer = new SelectionSynchronizer();
		}
		return synchronizer;
	}

	/**
	 * Hooks the GraphicalViewer to the rest of the Editor. By default, the
	 * viewer is added to the SelectionSynchronizer, which can be used to keep 2
	 * or more EditPartViewers in sync. The viewer is also registered as the
	 * <code>ISelectionProvider</code> for the Editor's PartSite.
	 */
	protected void hookGraphicalViewer() {
		getSelectionSynchronizer().addViewer(getGraphicalViewer());
		getSite().setSelectionProvider(getGraphicalViewer());
	}

	/**
	 * Sets the site and input for this editor then creates and initializes the
	 * actions.
	 * @see org.eclipse.ui.IEditorPart#init(IEditorSite, IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		setSite(site);
		setInput(input);
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		initializeActionRegistry();

		// add the model change listener
		BeansCorePlugin.getModel().addChangeListener(modelChangeListener);
	}

	/**
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart,
	 * ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// If not the active editor, ignore selection changed.
		if (this.equals(getSite().getPage().getActiveEditor())) {
			updateActions(selectionActions);
		}
	}

	/**
	 * Initializes the ActionRegistry. This registry may be used by {@link
	 * ActionBarContributor ActionBarContributors} and/or
	 * {@link ContextMenuProvider ContextMenuProviders}.
	 * <P>
	 * This method may be called on Editor creation, or lazily the first time
	 * {@link #getActionRegistry()} is called.
	 */
	protected void initializeActionRegistry() {
		createActions();
		updateActions(propertyActions);
	}

	/**
	 * Sets the ActionRegistry for this EditorPart.
	 * @param registry the registry
	 */
	protected void setActionRegistry(ActionRegistry registry) {
		this.actionRegistry = registry;
	}

	/**
	 * Sets the EditDomain for this EditorPart.
	 * @param ed the domain
	 */
	protected void setEditDomain(DefaultEditDomain editDomain) {
		this.editDomain = editDomain;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		getGraphicalViewer().getControl().setFocus();
	}

	/**
	 * Sets the graphicalViewer for this EditorPart.
	 * @param viewer the graphical viewer
	 */
	protected void setGraphicalViewer(GraphicalViewer viewer) {
		this.graphicalViewer = viewer;
		getEditDomain().addViewer(viewer);
	}

	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding
	 * action is an {@link UpdateAction}, it will have its
	 * <code>update()</code> method called.
	 * @param actionIds the list of IDs to update
	 */
	protected void updateActions(List actionIds) {
		ActionRegistry registry = getActionRegistry();
		Iterator iter = actionIds.iterator();
		while (iter.hasNext()) {
			IAction action = registry.getAction(iter.next());
			if (action instanceof UpdateAction) {
				((UpdateAction) action).update();
			}
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getShell());
		dialog.setOriginalName("graph.jpg");
		dialog.create();
		dialog.setMessage(BeansGraphPlugin
				.getResourceString("Editor.SaveAsDialog.message"));
		dialog.setOriginalName("graph.jpg");
		dialog.open();
		IPath path = dialog.getResult();
		if (path != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile file = workspace.getRoot().getFile(path);
			String ext = file.getFileExtension();
			if (ext == null
					|| ext.length() == 0
					|| !(ext.equalsIgnoreCase("jpg") || ext
							.equalsIgnoreCase("bmp"))) {
				ErrorDialog
						.openError(
								getSite().getShell(),
								BeansGraphPlugin
										.getResourceString("Editor.SaveError.title"),
								null,
								BeansGraphPlugin
										.createErrorStatus(BeansGraphPlugin
												.getResourceString("Editor.SaveAsDialog.error")));
			}
			else {
				saveImage(file, (ext.equalsIgnoreCase("jpg") ? SWT.IMAGE_JPEG
						: SWT.IMAGE_BMP));
			}
		}
	}

	/**
	 * Saves an encoded image from this viewer.
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 * SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public void saveImage(final IFile file, final int format) {
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			public void execute(final IProgressMonitor monitor)
					throws CoreException {
				try {
					if (file.exists()) {
						file.setContents(new ByteArrayInputStream(
								createImage(format)), true, false, monitor);
					}
					else {
						file.create(new ByteArrayInputStream(
								createImage(format)), true, monitor);
					}
				}
				catch (CoreException e) {
					ErrorDialog
							.openError(
									getSite().getShell(),
									BeansGraphPlugin
											.getResourceString("Editor.SaveError.title"),
									BeansGraphPlugin
											.getResourceString("Editor.SaveError.text"),
									e.getStatus());
				}
			}
		};

		try {
			Shell shell = getSite().getWorkbenchWindow().getShell();
			new ProgressMonitorDialog(shell).run(false, true, op);
		}
		catch (InvocationTargetException e) {
			BeansGraphPlugin.log(e);
		}
		catch (InterruptedException e) {
		}
	}

	/**
	 * Returns the bytes of an encoded image from this viewer.
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 * SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public byte[] createImage(int format) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Device device = getGraphicalViewer().getControl().getDisplay();
		LayerManager lm = (LayerManager) getGraphicalViewer()
				.getEditPartRegistry().get(LayerManager.ID);
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
		}
		finally {
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

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}
}
