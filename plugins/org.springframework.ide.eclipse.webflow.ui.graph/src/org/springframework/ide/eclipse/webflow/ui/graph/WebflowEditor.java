/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.parts.ScrollableThumbnail;
import org.eclipse.draw2d.parts.Thumbnail;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.tools.ConnectionCreationTool;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertySheetPageContributor;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.cleanup.CleanupProcessorXML;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowConfig;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.ExportAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.OpenBeansConfigAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.OpenBeansGraphAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.OpenConfigFileAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.SetAsStartStateAction;
import org.springframework.ide.eclipse.webflow.ui.graph.actions.WebflowContextMenuProvider;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StatePartFactory;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StateTreeEditPartFactory;
import org.w3c.dom.Node;

/**
 * {@link GraphicalEditorWithFlyoutPalette} that contributes the WebflowEditor
 * to the Eclipse workbench
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowEditor extends GraphicalEditorWithFlyoutPalette implements
		ITabbedPropertySheetPageContributor {

	private class OutlinePage extends ContentOutlinePage implements IAdaptable {

		static final int ID_OUTLINE = 0;

		static final int ID_OVERVIEW = 1;

		private DisposeListener disposeListener;

		private Control outline;

		private Canvas overview;

		private PageBook pageBook;

		private IAction showOutlineAction, showOverviewAction;

		private Thumbnail thumbnail;

		public OutlinePage(EditPartViewer viewer) {
			super(viewer);
		}

		protected void configureOutlineViewer() {
			getViewer().setEditDomain(getEditDomain());
			getViewer().setEditPartFactory(new StateTreeEditPartFactory());
			ContextMenuProvider provider = new WebflowContextMenuProvider(
					getViewer(), getActionRegistry());
			getViewer().setContextMenu(provider);
			getSite()
					.registerContextMenu(
							"org.springframework.ide.eclipse.webflow.ui.graph.contextmenu", //$NON-NLS-1$
							provider, getSite().getSelectionProvider());
			getViewer().setKeyHandler(getCommonKeyHandler());
			IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
			showOutlineAction = new Action() {

				public void run() {
					showPage(ID_OUTLINE);
				}
			};
			showOutlineAction
					.setImageDescriptor(WebflowImages.DESC_OBJS_OUTLINE); //$NON-NLS-1$
			showOutlineAction.setToolTipText("Show tree outline");
			tbm.add(showOutlineAction);
			showOverviewAction = new Action() {

				public void run() {
					showPage(ID_OVERVIEW);
				}
			};
			showOverviewAction
					.setImageDescriptor(WebflowImages.DESC_OBJS_OVERVIEW); //$NON-NLS-1$
			showOverviewAction.setToolTipText("Show graphical outline");
			tbm.add(showOverviewAction);
			showPage(ID_OUTLINE);
		}

		public void createControl(Composite parent) {
			pageBook = new PageBook(parent, SWT.NONE);
			outline = getViewer().createControl(pageBook);
			overview = new Canvas(pageBook, SWT.NONE);
			pageBook.showPage(outline);
			configureOutlineViewer();
			hookOutlineViewer();
			initializeOutlineViewer();
		}

		public void dispose() {
			unhookOutlineViewer();
			if (thumbnail != null) {
				thumbnail.deactivate();
				thumbnail = null;
			}
			super.dispose();
			// WebflowEditor.this.outlinePage = null;
		}

		public Object getAdapter(Class type) {
			if (type == ZoomManager.class)
				return getGraphicalViewer().getProperty(
						ZoomManager.class.toString());
			return null;
		}

		public Control getControl() {
			return pageBook;
		}

		protected void hookOutlineViewer() {
			getSelectionSynchronizer().addViewer(getViewer());
		}

		public void init(IPageSite pageSite) {
			super.init(pageSite);
			ActionRegistry registry = getActionRegistry();
			IActionBars bars = pageSite.getActionBars();
			String id = ActionFactory.UNDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.REDO.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = ActionFactory.DELETE.getId();
			bars.setGlobalActionHandler(id, registry.getAction(id));
			id = SetAsStartStateAction.STARTSTATE;
			bars.setGlobalActionHandler(id, registry.getAction(id));
			bars.updateActionBars();
		}

		public void initializeOutlineViewer() {
			setContents(diagram);
		}

		protected void initializeOverview() {
			LightweightSystem lws = new LightweightSystem(overview);
			RootEditPart rep = getGraphicalViewer().getRootEditPart();
			if (rep instanceof ScalableRootEditPart) {
				ScalableRootEditPart root = (ScalableRootEditPart) rep;
				thumbnail = new ScrollableThumbnail((Viewport) root.getFigure());
				thumbnail.setBorder(new MarginBorder(3));
				thumbnail.setSource(root
						.getLayer(LayerConstants.PRINTABLE_LAYERS));
				lws.setContents(thumbnail);
				disposeListener = new DisposeListener() {

					public void widgetDisposed(DisposeEvent e) {
						if (thumbnail != null) {
							thumbnail.deactivate();
							thumbnail = null;
						}
					}
				};
				getEditor().addDisposeListener(disposeListener);
			}
		}

		public void setContents(Object contents) {
			getViewer().setContents(contents);
		}

		protected void showPage(int id) {
			if (id == ID_OUTLINE) {
				showOutlineAction.setChecked(true);
				showOverviewAction.setChecked(false);
				pageBook.showPage(outline);
				if (thumbnail != null)
					thumbnail.setVisible(false);
			}
			else if (id == ID_OVERVIEW) {
				if (thumbnail == null)
					initializeOverview();
				showOutlineAction.setChecked(false);
				showOverviewAction.setChecked(true);
				pageBook.showPage(overview);
				thumbnail.setVisible(true);
			}
		}

		protected void unhookOutlineViewer() {
			getSelectionSynchronizer().removeViewer(getViewer());
			if (disposeListener != null && getEditor() != null
					&& !getEditor().isDisposed())
				getEditor().removeDisposeListener(disposeListener);
		}
	}

	private class ResourceTracker implements IResourceChangeListener,
			IResourceDeltaVisitor {

		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta delta = event.getDelta();
			try {
				if (delta != null)
					delta.accept(this);
			}
			catch (CoreException exception) {
			}
		}

		public boolean visit(IResourceDelta delta) {
			if (delta == null
					|| !delta.getResource().equals(
							((WebflowEditorInput) getEditorInput()).getFile()))
				return true;

			if (delta.getKind() == IResourceDelta.REMOVED) {
				Display display = getSite().getShell().getDisplay();
				if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { // if

					display.asyncExec(new Runnable() {

						public void run() {
							if (!isDirty())
								closeEditor(false);
						}
					});
				}
				else { // else if it was moved or renamed
					final IFile newFile = ResourcesPlugin.getWorkspace()
							.getRoot().getFile(delta.getMovedToPath());
					display.asyncExec(new Runnable() {

						public void run() {
							IWebflowConfig config = ((WebflowEditorInput) getEditorInput())
									.getConfig();
							config.setResource(newFile);
							List<IWebflowConfig> configs = config.getProject()
									.getConfigs();
							config.getProject().setConfigs(configs);
							superSetInput(new WebflowEditorInput(config));
						}
					});
				}
			}
			else if (delta.getKind() == IResourceDelta.CHANGED) {
				if (!isDirty() || isCurrentlySaving) {
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {

						public void run() {
							IWebflowConfig config = ((WebflowEditorInput) getEditorInput())
									.getConfig();
							setInput(new WebflowEditorInput(config));
							getCommandStack().flush();
							initializeGraphicalViewer();
							if (outlinePage != null) {
								outlinePage.initializeOutlineViewer();
							}
						}
					});
				}
				else if (isDirty()
						&& MessageDialog
								.openQuestion(
										Activator.getDefault().getWorkbench()
												.getActiveWorkbenchWindow()
												.getShell(),
										"File Changed",
										"The file has been changed on the file system. Do you want to load the changes?")) {
					Display display = getSite().getShell().getDisplay();
					display.asyncExec(new Runnable() {

						public void run() {
							IWebflowConfig config = ((WebflowEditorInput) getEditorInput())
									.getConfig();
							setInput(new WebflowEditorInput(config));
							getCommandStack().flush();
							initializeGraphicalViewer();
							if (outlinePage != null) {
								outlinePage.initializeOutlineViewer();
							}
						}
					});
				}
			}
			return false;
		}
	}

	private IWebflowState diagram;

	private IFile file;

	private boolean isCurrentlySaving = false;

	private IStructuredModel model;

	private OutlinePage outlinePage;

	private IPartListener partListener = new IPartListener() {

		// If an open, unsaved file was deleted, query the user to either do a
		// "Save As"
		// or close the editor.
		public void partActivated(IWorkbenchPart part) {
			if (part != WebflowEditor.this)
				return;
			if (getEditorInput() instanceof WebflowEditorInput
					&& !((WebflowEditorInput) getEditorInput()).getFile()
							.exists()) {
				Shell shell = getSite().getShell();
				String title = "res";
				String message = "erer";
				String[] buttons = { "Save", "Close" };
				MessageDialog dialog = new MessageDialog(shell, title, null,
						message, MessageDialog.QUESTION, buttons, 0);
				if (dialog.open() == 0) {
					if (!performSaveAs())
						partActivated(part);
				}
				else {
					closeEditor(false);
				}
			}
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
		}

	};

	private ResourceTracker resourceListener = new ResourceTracker();

	private PaletteRoot root;

	private boolean savePreviouslyNeeded = false;

	private KeyHandler sharedKeyHandler;

	public static final String EDITOR_ID = "org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor";

	public WebflowEditor() {
		DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
		defaultEditDomain.setActiveTool(new ConnectionCreationTool());
		setEditDomain(defaultEditDomain);
	}

	protected void closeEditor(boolean save) {
		getSite().getPage().closeEditor(this, save);
	}

	public void commandStackChanged(EventObject event) {
		if (isDirty()) {
			if (!savePreviouslyNeeded()) {
				setSavePreviouslyNeeded(true);
				firePropertyChange(IEditorPart.PROP_DIRTY);
			}
		}
		else {
			setSavePreviouslyNeeded(false);
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		super.commandStackChanged(event);
	}

	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScalableRootEditPart root = new ScalableRootEditPart();

		List<String> zoomLevels = new ArrayList<String>();
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		root.getZoomManager().setZoomLevelContributions(zoomLevels);
		root.getZoomManager().setZoomLevels(
				new double[] { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1,
						1.5, 2, 2.5, 3 });
		IAction zoomIn = new ZoomInAction(root.getZoomManager());
		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);

		getActionRegistry().registerAction(new ExportAction(this));
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);
		getGraphicalViewer().setRootEditPart(root);
		getGraphicalViewer().setEditPartFactory(new StatePartFactory());
		getGraphicalViewer().setKeyHandler(
				new GraphicalViewerKeyHandler(getGraphicalViewer())
						.setParent(getCommonKeyHandler()));

		ContextMenuProvider provider = new WebflowContextMenuProvider(
				getGraphicalViewer(), getActionRegistry());
		getGraphicalViewer().setContextMenu(provider);
		getSite().registerContextMenu(
				"org.springframework.ide.eclipse.webflow.ui.graph.contextmenu", //$NON-NLS-1$
				provider, getGraphicalViewer());

	}

	@SuppressWarnings("unchecked")
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new SetAsStartStateAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new EditPropertiesAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ExportAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new OpenConfigFileAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new OpenBeansGraphAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new OpenBeansConfigAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

	}

	public void dispose() {
		getSite().getWorkbenchWindow().getPartService().removePartListener(
				partListener);
		partListener = null;
		if (getEditorInput() instanceof WebflowEditorInput) {
			((WebflowEditorInput) getEditorInput()).getFile().getWorkspace()
					.removeResourceChangeListener(resourceListener);
		}

		if (this.diagram != null) {
			diagram = null;
		}

		if (this.model != null) {
			this.model.releaseFromEdit();
		}

		super.dispose();
	}

	public void doSave(IProgressMonitor monitor) {
		try {
			this.isCurrentlySaving = true;
			model.aboutToChangeModel();
			// reattach root node from document
			IDOMDocument document = ((DOMModelImpl) model).getDocument();
			Node root = document.getDocumentElement();
			document.replaceChild(diagram.getNode(), root);

			formatElement(monitor);
			model.changedModel();
			model.save();
			getCommandStack().markSaveLocation();
			this.isCurrentlySaving = false;
		}
		catch (Exception e) {
		}
	}

	public void doSaveAs() {
		performSaveAs();
	}

	private void formatElement(IProgressMonitor monitor) {
		FormatProcessorXML formatProcessor = new FormatProcessorXML();
		formatProcessor.setProgressMonitor(monitor);
		formatProcessor.getFormatPreferences().setClearAllBlankLines(true);
		formatProcessor.formatModel(model);

		CleanupProcessorXML bla = new CleanupProcessorXML();
		bla.getCleanupPreferences().setCompressEmptyElementTags(true);
		bla.cleanupModel(model);
	}

	public Object getAdapter(Class type) {
		if (type == IContentOutlinePage.class) {
			if (outlinePage == null) {
				outlinePage = new OutlinePage(new TreeViewer());
			}
			return outlinePage;
		}
		if (type == ZoomManager.class)
			return getGraphicalViewer().getProperty(
					ZoomManager.class.toString());
		if (type == IPropertySheetPage.class)
			return new TabbedPropertySheetPage(this);

		return super.getAdapter(type);
	}

	protected KeyHandler getCommonKeyHandler() {
		if (sharedKeyHandler == null) {
			sharedKeyHandler = new KeyHandler();
			sharedKeyHandler
					.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
							getActionRegistry().getAction(
									ActionFactory.DELETE.getId()));
			sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(
							GEFActionConstants.DIRECT_EDIT));
			// TODO CD uncomment this once it is agreed and the appropriate actions
			// are implemented
			//sharedKeyHandler.put(KeyStroke.getPressed(SWT.F3, 0),
			//		getActionRegistry().getAction(OpenConfigFileAction.OPEN_FILE));
			//sharedKeyHandler.put(KeyStroke.getPressed(SWT.F3, SWT.CTRL),
			//		getActionRegistry().getAction(OpenConfigFileAction.OPEN_FILE));
		}
		return sharedKeyHandler;
	}

	protected FigureCanvas getEditor() {
		return (FigureCanvas) getGraphicalViewer().getControl();
	}

	public GraphicalViewer getGraphViewer() {
		return getGraphicalViewer();
	}

	protected PaletteRoot getPaletteRoot() {
		if (root == null)
			root = WebflowEditorPaletteFactory.createPalette(WebflowModelXmlUtils.isVersion1Flow(diagram));
		return root;
	}

	public void gotoMarker(IMarker marker) {
	}

	protected void initializeGraphicalViewer() {
		if (diagram != null) {
			getGraphicalViewer().setContents(diagram);
		}
	}

	public boolean isDirty() {
		return isSaveOnCloseNeeded();
	}

	public boolean isSaveAsAllowed() {
		return true;
	}

	public boolean isSaveOnCloseNeeded() {
		return getCommandStack().isDirty();
	}

	public boolean performSaveAs() {
		SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
				.getShell());
		dialog.setOriginalFile(((WebflowEditorInput) getEditorInput())
				.getFile());
		dialog.open();
		IPath path = dialog.getResult();

		this.isCurrentlySaving = true;

		if (path == null)
			return false;

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IFile file = workspace.getRoot().getFile(path);

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

			@SuppressWarnings("restriction")
			public void execute(final IProgressMonitor monitor)
					throws CoreException {
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					model.aboutToChangeModel();

					// reattach root node from document
					IDOMDocument document = ((DOMModelImpl) model)
							.getDocument();
					Node root = document.getDocumentElement();
					document.replaceChild(diagram.getNode(), root);

					formatElement(monitor);
					model.changedModel();
					model.save(out);
					file.create(new ByteArrayInputStream(out.toByteArray()),
							true, monitor);
					out.close();
					getCommandStack().markSaveLocation();
				}
				catch (Exception e) {
				}
			}
		};

		IWebflowConfig config = ((WebflowEditorInput) getEditorInput())
				.getConfig();
		IWebflowConfig newConfig = org.springframework.ide.eclipse.webflow.core.Activator
				.getModel().getProject(file.getProject()).getConfig(file);

		if (newConfig == null) {
			newConfig = new WebflowConfig(config.getProject());
			newConfig.setBeansConfigs(config.getBeansConfigs());
			newConfig.setResource(file);
			List<IWebflowConfig> configs = config.getProject().getConfigs();
			configs.add(newConfig);
			config.getProject().setConfigs(configs);
		}
		else {
			newConfig.setBeansConfigs(config.getBeansConfigs());
			List<IWebflowConfig> configs = config.getProject().getConfigs();
			config.getProject().setConfigs(configs);
		}

		try {
			new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell())
					.run(false, true, op);
			setInput(new WebflowEditorInput(newConfig));
			getCommandStack().markSaveLocation();
		}
		catch (Exception e) {
		}

		try {
			superSetInput(new WebflowEditorInput(newConfig));
			getCommandStack().markSaveLocation();
		}
		catch (Exception e) {
		}
		this.isCurrentlySaving = false;
		return true;
	}

	private boolean savePreviouslyNeeded() {
		return savePreviouslyNeeded;
	}

	protected void setInput(IEditorInput input) {

		if (input instanceof FileEditorInput) {
			IFile tempFile = ((FileEditorInput) input).getFile();
			if (WebflowModelUtils.isWebflowConfig(tempFile)) {
				input = new WebflowEditorInput(WebflowModelUtils
						.getWebflowConfig(tempFile));
			}
		}

		if (input instanceof WebflowEditorInput) {

			superSetInput(input);
			WebflowEditorInput webflowEditorInput = ((WebflowEditorInput) input);
			this.file = webflowEditorInput.getFile();
			setPartName(this.file.getName());

			try {
				model = null;
				model = StructuredModelManager.getModelManager()
						.getExistingModelForEdit(this.file);
				if (model == null) {
					model = StructuredModelManager.getModelManager()
							.getModelForEdit(this.file);

				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model)
							.getDocument();
					this.diagram = new WebflowState(webflowEditorInput
							.getConfig());
					IDOMNode root = (IDOMNode) document.getDocumentElement();
					IDOMNode rootClone = (IDOMNode) root.cloneNode(true);
					webflowEditorInput.initLineNumbers(root, rootClone);
					this.diagram
							.init(rootClone, webflowEditorInput.getConfig());
				}
			}
			catch (Exception e) {
			}
		}
		else {
			super.setInput(input);
		}
	}

	private void setSavePreviouslyNeeded(boolean value) {
		savePreviouslyNeeded = value;
	}

	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
		getSite().getWorkbenchWindow().getPartService().addPartListener(
				partListener);
	}

	protected void superSetInput(IEditorInput input) {
		if (getEditorInput() != null) {
			IFile file = ((WebflowEditorInput) getEditorInput()).getFile();
			file.getWorkspace().removeResourceChangeListener(resourceListener);
		}

		super.setInput(input);

		if (getEditorInput() != null) {
			IFile file = ((WebflowEditorInput) getEditorInput()).getFile();
			file.getWorkspace().addResourceChangeListener(resourceListener);
			setPartName(file.getName());
		}
	}

	public String getContributorId() {
		return getSite().getId();
	}
}
