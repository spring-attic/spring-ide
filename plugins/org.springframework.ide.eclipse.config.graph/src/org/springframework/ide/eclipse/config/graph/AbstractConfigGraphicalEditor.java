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
package org.springframework.ide.eclipse.config.graph;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.KeyStroke;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.MouseWheelHandler;
import org.eclipse.gef.MouseWheelZoomHandler;
import org.eclipse.gef.RootEditPart;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDragSourceListener;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DirectEditAction;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.gef.ui.actions.PrintAction;
import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.gef.ui.parts.GraphicalEditorWithPalette;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.config.core.IConfigEditor;
import org.springframework.ide.eclipse.config.core.IConfigEditorPage;
import org.springframework.ide.eclipse.config.core.contentassist.SpringConfigContentAssistProcessor;
import org.springframework.ide.eclipse.config.core.extensions.PageAdaptersExtensionPointConstants;
import org.springframework.ide.eclipse.config.graph.actions.ExportAction;
import org.springframework.ide.eclipse.config.graph.actions.ResetManualLayoutAction;
import org.springframework.ide.eclipse.config.graph.actions.ShowPropertiesAction;
import org.springframework.ide.eclipse.config.graph.actions.ShowSourceAction;
import org.springframework.ide.eclipse.config.graph.actions.SpringConfigContextMenuProvider;
import org.springframework.ide.eclipse.config.graph.model.AbstractConfigGraphDiagram;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigEditPartFactory;
import org.springframework.ide.eclipse.config.graph.parts.AbstractConfigPaletteFactory;
import org.springframework.ide.eclipse.config.graph.parts.ActivityDiagramPart;


/**
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public abstract class AbstractConfigGraphicalEditor extends GraphicalEditorWithPalette implements IConfigEditorPage {

	public static String PAGE_KIND = "graphicalEditor"; //$NON-NLS-1$

	private IConfigEditor editor;

	private IContentOutlinePage contentOutline;

	private AbstractConfigGraphDiagram diagram;

	private PaletteRoot root;

	private KeyHandler keyHandler;

	private String namespaceUri;

	private AbstractConfigPaletteFactory paletteFactory;

	private final Set<IConfigurationElement> adapterDefinitions = new HashSet<IConfigurationElement>();

	private TemplateTransferDragSourceListener transferSourceListener;

	private TemplateTransferDropTargetListener transferTargetListener;

	public AbstractConfigGraphicalEditor() {
		super();
	}

	public AbstractConfigGraphicalEditor(IConfigEditor editor, String uri) {
		this();
		initialize(editor, uri);
	}

	@Override
	public void commandStackChanged(EventObject event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
		super.commandStackChanged(event);
	}

	@Override
	protected void configureGraphicalViewer() {
		super.configureGraphicalViewer();
		ScalableFreeformRootEditPart rootEditPart = new ScalableFreeformRootEditPart();
		getGraphicalViewer().setRootEditPart(rootEditPart);

		List<String> zoomLevels = new ArrayList<String>();
		zoomLevels.add(ZoomManager.FIT_ALL);
		zoomLevels.add(ZoomManager.FIT_WIDTH);
		zoomLevels.add(ZoomManager.FIT_HEIGHT);
		rootEditPart.getZoomManager().setZoomLevelContributions(zoomLevels);

		IAction zoomIn = new ZoomInAction(rootEditPart.getZoomManager());
		IAction zoomOut = new ZoomOutAction(rootEditPart.getZoomManager());
		getActionRegistry().registerAction(zoomIn);
		getActionRegistry().registerAction(zoomOut);
		getSite().getKeyBindingService().registerAction(zoomIn);
		getSite().getKeyBindingService().registerAction(zoomOut);
		getGraphicalViewer().setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
				MouseWheelZoomHandler.SINGLETON);

		getGraphicalViewer().setEditPartFactory(createEditPartFactory());
		getGraphicalViewer().setKeyHandler(
				new GraphicalViewerKeyHandler(getGraphicalViewer()).setParent(getCommonKeyHandler()));

		ContextMenuProvider provider = createContextMenuProvider();
		getGraphicalViewer().setContextMenu(provider);
		getEditorSite().registerContextMenu(provider, getGraphicalViewer(), false);
	}

	@Override
	protected void createActions() {
		super.createActions();
		ActionRegistry registry = getActionRegistry();
		IAction action;

		action = new DirectEditAction((IWorkbenchPart) this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new PrintAction(this);
		registry.registerAction(action);

		action = new ExportAction(this);
		registry.registerAction(action);

		action = new ShowPropertiesAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ShowSourceAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());

		action = new ResetManualLayoutAction(this);
		registry.registerAction(action);
		getSelectionActions().add(action.getId());
	}

	protected ContextMenuProvider createContextMenuProvider() {
		return new SpringConfigContextMenuProvider(getGraphicalViewer(), getActionRegistry());
	}

	protected abstract AbstractConfigEditPartFactory createEditPartFactory();

	protected abstract AbstractConfigGraphDiagram createFlowDiagram();

	/**
	 * Returns the bytes of an encoded image from this viewer.
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 * SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public byte[] createImage(int format) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Device device = getGraphicalViewer().getControl().getDisplay();
		LayerManager lm = (LayerManager) getGraphicalViewer().getEditPartRegistry().get(LayerManager.ID);
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

	protected abstract AbstractConfigPaletteFactory createPaletteFactory();

	protected PaletteRoot createPaletteRoot() {
		paletteFactory = createPaletteFactory();
		return paletteFactory.getPaletteRoot();
	}

	@Override
	public void dispose() {
		if (transferSourceListener != null) {
			getPaletteViewer().removeDragSourceListener(transferSourceListener);
		}
		if (transferTargetListener != null) {
			getGraphicalViewer().removeDropTargetListener(transferTargetListener);
		}
		super.dispose();
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		getCommandStack().markSaveLocation();
		diagram.doSaveCoordinates();
	}

	@Override
	public void doSaveAs() {
		getCommandStack().markSaveLocation();
		setInput(editor.getEditorInput());
		modelUpdated();
		diagram.doSaveCoordinates();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class type) {
		if (IContentOutlinePage.class == type) {
			return getContentOutline();
		}
		if (ActionRegistry.class == type) {
			return getActionRegistry();
		}
		if (CommandStack.class == type) {
			return getEditDomain().getCommandStack();
		}
		if (ZoomManager.class == type) {
			return getGraphicalViewer().getProperty(ZoomManager.class.toString());
		}
		if (ISelectionProvider.class == type) {
			return getGraphicalViewer();
		}
		return super.getAdapter(type);
	}

	public Set<IConfigurationElement> getAdapterDefinitions() {
		return adapterDefinitions;
	}

	protected KeyHandler getCommonKeyHandler() {
		if (keyHandler == null) {
			keyHandler = new KeyHandler();
			// 127 is ASCII value for delete key
			keyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0),
					getActionRegistry().getAction(ActionFactory.DELETE.getId()));
			// 8 is ASCII value for backspace key
			keyHandler.put(KeyStroke.getReleased(SWT.BS, 8, 0),
					getActionRegistry().getAction(ActionFactory.DELETE.getId()));
			keyHandler.put(KeyStroke.getPressed(SWT.F2, 0),
					getActionRegistry().getAction(GEFActionConstants.DIRECT_EDIT));
		}
		return keyHandler;
	}

	private IContentOutlinePage getContentOutline() {
		if (contentOutline == null && getGraphicalViewer() != null) {
			RootEditPart rootEditPart = getGraphicalViewer().getRootEditPart();
			if (rootEditPart instanceof ScalableFreeformRootEditPart) {
				contentOutline = new SpringConfigGraphOutlinePage((ScalableFreeformRootEditPart) rootEditPart);
			}
		}
		return contentOutline;
	}

	public AbstractConfigGraphDiagram getDiagram() {
		return diagram;
	}

	public IDOMDocument getDomDocument() {
		return editor.getDomDocument();
	}

	public IConfigEditor getEditor() {
		return editor;
	}

	public String getNamespaceUri() {
		return namespaceUri;
	}

	public String getPageKind() {
		return PAGE_KIND;
	}

	@Override
	protected PaletteRoot getPaletteRoot() {
		if (root == null) {
			root = createPaletteRoot();
		}
		return root;
	}

	public IFile getResourceFile() {
		return editor.getResourceFile();
	}

	public SpringConfigContentAssistProcessor getXmlProcessor() {
		return getEditor().getXmlProcessor();
	}

	private void initAdapterDefinitions() {
		Set<IConfigurationElement> allAdapters = editor.getAdapterDefinitions();
		for (IConfigurationElement config : allAdapters) {
			String parent = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_PARENT_URI);
			String uri = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_NAMESPACE_URI);
			String label = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_LABEL);
			String partFactory = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_EDITPART_FACTORY);
			String modelFactory = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_MODEL_FACTORY);
			String paletteFactory = config.getAttribute(PageAdaptersExtensionPointConstants.ATTR_PALETTE_FACTORY);
			if (parent != null && parent.equals(namespaceUri) && uri != null && partFactory != null
					&& modelFactory != null && label != null && paletteFactory != null) {
				adapterDefinitions.add(config);
			}
		}
	}

	public void initialize(IConfigEditor editor, String uri) {
		this.editor = editor;
		this.namespaceUri = uri;
		initAdapterDefinitions();
		diagram = createFlowDiagram();
		DefaultEditDomain defaultEditDomain = new DefaultEditDomain(this);
		setEditDomain(defaultEditDomain);
	}

	@Override
	protected void initializeGraphicalViewer() {
		getGraphicalViewer().setContents(diagram);
		transferTargetListener = new TemplateTransferDropTargetListener(getGraphicalViewer());
		getGraphicalViewer().addDropTargetListener(transferTargetListener);
	}

	@Override
	protected void initializePaletteViewer() {
		super.initializePaletteViewer();
		transferSourceListener = new TemplateTransferDragSourceListener(getPaletteViewer());
		getPaletteViewer().addDragSourceListener(transferSourceListener);
	}

	public void modelUpdated() {
		ActivityDiagramPart part = (ActivityDiagramPart) getGraphicalViewer().getContents();
		if (part != null && part.isActive()) {
			part.refreshAll();
		}
	}

	public void namespacesUpdated() {
		if (paletteFactory != null) {
			paletteFactory.updatePalette();
		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (this.equals(editor.getActiveEditor())) {
			updateActions(getSelectionActions());
		}
	}

}
