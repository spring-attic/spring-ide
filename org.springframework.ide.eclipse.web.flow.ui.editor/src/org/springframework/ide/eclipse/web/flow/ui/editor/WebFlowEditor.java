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

package org.springframework.ide.eclipse.web.flow.ui.editor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
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
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.internal.XmlModelWriter;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowState;
import org.springframework.ide.eclipse.web.flow.core.model.IPersistableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.EditPropertiesAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.ExportAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.OpenBeansGraphAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.OpenBeansViewAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.WebFlowContextMenuProvider;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.OpenConfigFileAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.actions.SetAsStartStateAction;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.StatePartFactory;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.StateTreeEditPartFactory;

public class WebFlowEditor extends GraphicalEditorWithPalette {
    
    private boolean isCurrentlySaving = false;

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
            ContextMenuProvider provider = new WebFlowContextMenuProvider(
                    getViewer(), getActionRegistry());
            getViewer().setContextMenu(provider);
            getSite()
                    .registerContextMenu(
                            "org.springframework.ide.eclipse.web.flow.ui.editor.contextmenu", //$NON-NLS-1$
                            provider, getSite().getSelectionProvider());
            getViewer().setKeyHandler(getCommonKeyHandler());
            IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
            showOutlineAction = new Action() {

                public void run() {
                    showPage(ID_OUTLINE);
                }
            };
            showOutlineAction
                    .setImageDescriptor(WebFlowImages.DESC_OBJS_OUTLINE); //$NON-NLS-1$
            showOutlineAction.setToolTipText("Show tree outline");
            tbm.add(showOutlineAction);
            showOverviewAction = new Action() {

                public void run() {
                    showPage(ID_OVERVIEW);
                }
            };
            showOverviewAction
                    .setImageDescriptor(WebFlowImages.DESC_OBJS_OVERVIEW); //$NON-NLS-1$
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
            WebFlowEditor.this.outlinePage = null;
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
            String id = IWorkbenchActionConstants.UNDO;
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = IWorkbenchActionConstants.REDO;
            bars.setGlobalActionHandler(id, registry.getAction(id));
            id = IWorkbenchActionConstants.DELETE;
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
            } else if (id == ID_OVERVIEW) {
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
            } catch (CoreException exception) {
                // What should be done here?
            }
        }

        public boolean visit(IResourceDelta delta) {
            if (delta == null
                    || !delta.getResource().equals(
                            ((WebFlowEditorInput) getEditorInput()).getFile()))
                return true;

            if (delta.getKind() == IResourceDelta.REMOVED) {
                Display display = getSite().getShell().getDisplay();
                if ((IResourceDelta.MOVED_TO & delta.getFlags()) == 0) { // if
                    // the
                    // file
                    // was
                    // deleted
                    // NOTE: The case where an open, unsaved file is deleted is
                    // being handled by the
                    // PartListener added to the Workbench in the initialize()
                    // method.
                    display.asyncExec(new Runnable() {

                        public void run() {
                            if (!isDirty())
                                closeEditor(false);
                        }
                    });
                } else { // else if it was moved or renamed
                    final IFile newFile = ResourcesPlugin.getWorkspace()
                            .getRoot().getFile(delta.getMovedToPath());
                    display.asyncExec(new Runnable() {

                        public void run() {
                            superSetInput(new WebFlowEditorInput(newFile, ((WebFlowEditorInput) getEditorInput()).getElementId()));
                        }
                    });
                }
            } else if (delta.getKind() == IResourceDelta.CHANGED) {
                if (!isDirty() || isCurrentlySaving) {
                    final IFile newFile = ResourcesPlugin.getWorkspace()
                            .getRoot().getFile(delta.getFullPath());
                    Display display = getSite().getShell().getDisplay();
                    display.asyncExec(new Runnable() {

                        public void run() {
                            setInput(new WebFlowEditorInput(newFile,((WebFlowEditorInput) getEditorInput()).getElementId()));
                            getCommandStack().flush();
                            initializeGraphicalViewer();
                            outlinePage.initializeOutlineViewer();
                        }
                    });
                } else if (isDirty()
                        && MessageDialog
                                .openQuestion(
                                        WebFlowPlugin
                                                .getActiveWorkbenchWindow()
                                                .getShell(),
                                        "File Changed",
                                        "The file has been changed on the file system. Do you want to load the changes?")) {
                    final IFile newFile = ResourcesPlugin.getWorkspace()
                            .getRoot().getFile(delta.getFullPath());
                    Display display = getSite().getShell().getDisplay();
                    display.asyncExec(new Runnable() {

                        public void run() {
                            setInput(new WebFlowEditorInput(newFile, ((WebFlowEditorInput) getEditorInput()).getElementId()));
                            getCommandStack().flush();
                            initializeGraphicalViewer();
                            outlinePage.initializeOutlineViewer();
                        }
                    });
                }
            }
            return false;
        }
    }

    private IWebFlowState diagram;

    private IFile file;

    private OutlinePage outlinePage;

    private IPartListener partListener = new IPartListener() {

        // If an open, unsaved file was deleted, query the user to either do a
        // "Save As"
        // or close the editor.
        public void partActivated(IWorkbenchPart part) {
            if (part != WebFlowEditor.this)
                return;
            if (!((WebFlowEditorInput) getEditorInput()).getFile().exists()) {
                Shell shell = getSite().getShell();
                String title = "res";
                String message = "erer";
                String[] buttons = { "Save", "Close" };
                MessageDialog dialog = new MessageDialog(shell, title, null,
                        message, MessageDialog.QUESTION, buttons, 0);
                if (dialog.open() == 0) {
                    if (!performSaveAs())
                        partActivated(part);
                } else {
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

    public WebFlowEditor() {
        DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
        defaultEditDomain.setActiveTool(new ConnectionCreationTool());
        setEditDomain(defaultEditDomain);
    }

    protected void closeEditor(boolean save) {
        getSite().getPage().closeEditor(this, save);
    }

    /**
     * @see org.eclipse.gef.commands.CommandStackListener#commandStackChanged(java.util.EventObject)
     */
    public void commandStackChanged(EventObject event) {
        if (isDirty()) {
            if (!savePreviouslyNeeded()) {
                setSavePreviouslyNeeded(true);
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        } else {
            setSavePreviouslyNeeded(false);
            firePropertyChange(IEditorPart.PROP_DIRTY);
        }
        super.commandStackChanged(event);
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#configureGraphicalViewer()
     */
    protected void configureGraphicalViewer() {
        super.configureGraphicalViewer();
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

        getActionRegistry().registerAction(new ExportAction(this));

        getSite().getKeyBindingService().registerAction(zoomIn);
        getSite().getKeyBindingService().registerAction(zoomOut);
        getGraphicalViewer().setRootEditPart(root);
        getGraphicalViewer().setEditPartFactory(new StatePartFactory());
        getGraphicalViewer().setKeyHandler(
                new GraphicalViewerKeyHandler(getGraphicalViewer())
                        .setParent(getCommonKeyHandler()));

        ContextMenuProvider provider = new WebFlowContextMenuProvider(
                getGraphicalViewer(), getActionRegistry());
        getGraphicalViewer().setContextMenu(provider);
        getSite()
                .registerContextMenu(
                        "org.springframework.ide.eclipse.web.flow.ui.editor.contextmenu", //$NON-NLS-1$
                        provider, getGraphicalViewer());

    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#createActions()
     */
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

        action = new OpenBeansViewAction(this);
        registry.registerAction(action);
        getSelectionActions().add(action.getId());

    }

    /**
     * Creates an appropriate output stream and writes the activity diagram out
     * to this stream.
     * 
     * @param os
     *            the base output stream
     * @throws IOException
     */
    protected void createOutputStream(OutputStream os) throws Exception {
        XmlModelWriter writer = new XmlModelWriter(os);
        ((IPersistableModelElement) diagram).save(writer);
        writer.close();
    }

    public void dispose() {
        getSite().getWorkbenchWindow().getPartService().removePartListener(
                partListener);
        partListener = null;
        ((WebFlowEditorInput) getEditorInput()).getFile().getWorkspace()
                .removeResourceChangeListener(resourceListener);

        super.dispose();
    }

    /**
     * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave(IProgressMonitor monitor) {
        try {
            this.isCurrentlySaving = true;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            createOutputStream(out);
            IFile file = ((WebFlowEditorInput) getEditorInput()).getFile();
            file.setContents(new ByteArrayInputStream(out.toByteArray()), true,
                    false, monitor);
            out.close();
            getCommandStack().markSaveLocation();
            this.isCurrentlySaving = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSaveAs() {
        performSaveAs();
    }

    public Object getAdapter(Class type) {
        if (type == IContentOutlinePage.class) {
            outlinePage = new OutlinePage(new TreeViewer());
            return outlinePage;
        }
        if (type == ZoomManager.class)
            return getGraphicalViewer().getProperty(
                    ZoomManager.class.toString());

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
            //sharedKeyHandler.put(, 0), getActionRegistry()
            //        .getAction(EditPropertiesAction.EDITPROPERTIES));
        }
        return sharedKeyHandler;
    }

    protected FigureCanvas getEditor() {
        return (FigureCanvas) getGraphicalViewer().getControl();
    }

    public GraphicalViewer getGraphViewer() {
        return getGraphicalViewer();
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithPalette#getPaletteRoot()
     */
    protected PaletteRoot getPaletteRoot() {
        if (root == null)
            root = WebFlowEditorPaletteFactory.createPalette();
        return root;
    }

    public void gotoMarker(IMarker marker) {
        System.out.println("");
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditor#initializeGraphicalViewer()
     */
    protected void initializeGraphicalViewer() {
        getGraphicalViewer().setContents(diagram);
    }

    /**
     * @see org.eclipse.gef.ui.parts.GraphicalEditorWithPalette#initializePaletteViewer()
     */
    protected void initializePaletteViewer() {
        super.initializePaletteViewer();
        getPaletteViewer().addDragSourceListener(
                new TemplateTransferDragSourceListener(getPaletteViewer()));
    }

    /**
     * @see org.eclipse.ui.ISaveablePart#isDirty()
     */
    public boolean isDirty() {
        return isSaveOnCloseNeeded();
    }

    /**
     * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed() {
        return true;
    }

    /**
     * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
     */
    public boolean isSaveOnCloseNeeded() {
        return getCommandStack().isDirty();
    }

    /**
     * @see org.eclipse.ui.ISaveablePart#doSaveAs()
     */
    public boolean performSaveAs() {
        SaveAsDialog dialog = new SaveAsDialog(getSite().getWorkbenchWindow()
                .getShell());
        dialog.setOriginalFile(((WebFlowEditorInput) getEditorInput())
                .getFile());
        dialog.open();
        IPath path = dialog.getResult();
        
        this.isCurrentlySaving = true;
        
        if (path == null)
            return false;

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IFile file = workspace.getRoot().getFile(path);

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            public void execute(final IProgressMonitor monitor)
                    throws CoreException {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    createOutputStream(out);
                    file.create(new ByteArrayInputStream(out.toByteArray()),
                            true, monitor);
                    out.close();
                    isCurrentlySaving = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell())
                    .run(false, true, op);
            setInput(new WebFlowEditorInput( (IFile) file,  ((WebFlowEditorInput) this.getEditorInput()).getElementId()));
            getCommandStack().markSaveLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            superSetInput(new WebFlowEditorInput(file, ((WebFlowEditorInput) this.getEditorInput()).getElementId()));
            getCommandStack().markSaveLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    private boolean savePreviouslyNeeded() {
        return savePreviouslyNeeded;
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
     */
    protected void setInput(IEditorInput input) {
        superSetInput(input);
        this.file = ((WebFlowEditorInput) input).getFile();
        setPartName(this.file.getName());
        try {
            diagram = ((WebFlowEditorInput) input).getRootState();
        } catch (WebFlowDefinitionException e) {
            diagram = new WebFlowState(null, null);
            MessageDialog.openError(WebFlowPlugin.getActiveWorkbenchWindow()
                    .getShell(), "Error opening Spring Web Flow config file", e
                    .getMessage());
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
            IFile file = ((WebFlowEditorInput) getEditorInput()).getFile();
            file.getWorkspace().removeResourceChangeListener(resourceListener);
        }

        super.setInput(input);

        if (getEditorInput() != null) {
            IFile file = ((WebFlowEditorInput) getEditorInput()).getFile();
            file.getWorkspace().addResourceChangeListener(resourceListener);
            setPartName(file.getName());
        }
    }
}