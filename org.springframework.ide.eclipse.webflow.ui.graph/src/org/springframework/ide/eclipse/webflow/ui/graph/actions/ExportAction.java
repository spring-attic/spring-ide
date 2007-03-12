/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.springframework.ide.eclipse.webflow.ui.graph.Activator;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowImages;

/**
 * 
 */
public class ExportAction extends WorkbenchPartAction {

	/**
	 * 
	 */
	public static final String ID = "Export_action";

	/**
	 * 
	 */
	private WebflowEditor editor;

	/**
	 * 
	 * 
	 * @param part 
	 */
	public ExportAction(WebflowEditor part) {
		super(part);
		this.editor = part;
		setText("Export");
		setId(ID);
		setToolTipText("Exports the web flow to an image");
		setImageDescriptor(WebflowImages.DESC_OBJS_EXPORT_ENABLED);
		setDisabledImageDescriptor(WebflowImages.DESC_OBJS_EXPORT_DISABLED);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		SaveAsDialog dialog = new SaveAsDialog(getWorkbenchPart().getSite()
				.getShell());
		IFile editorFile = ((IFileEditorInput) this.editor.getEditorInput())
				.getFile();
		String fileName = editorFile.getName();
		fileName = fileName.substring(0, fileName.lastIndexOf("."));
		dialog.setOriginalName(fileName + ".jpg");
		dialog.create();
		dialog
				.setMessage("Specify a name and location for the image. Either enter .bmp or .jpg extension.");
		dialog.setOriginalName(fileName + ".jpg");
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
								getWorkbenchPart().getSite().getShell(),
								"Error",
								"Error during processing of export",
								Activator.createErrorStatus("error", null));
			}
			else {
				saveImage(file, (ext.equalsIgnoreCase("jpg") ? SWT.IMAGE_JPEG
						: SWT.IMAGE_BMP));
			}
		}
	}

	/**
	 * Saves an encoded image from this viewer.
	 * 
	 * @param file 
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 * SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * 
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
					}
					else {
						file.create(new ByteArrayInputStream(
								createImage(format)), true, monitor);
					}
				}
				catch (CoreException e) {
					ErrorDialog.openError(getWorkbenchPart().getSite()
							.getShell(), "Error",
							"Error during processing of export", e.getStatus());
				}
			}
		};

		try {
			Shell shell = getWorkbenchPart().getSite().getWorkbenchWindow()
					.getShell();
			new ProgressMonitorDialog(shell).run(false, true, op);
		}
		catch (InvocationTargetException e) {
			Activator.log(e);
		}
		catch (InterruptedException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	/* (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {
		return true;
	}

	/**
	 * Returns the bytes of an encoded image from this viewer.
	 * 
	 * @param format one of SWT.IMAGE_BMP, SWT.IMAGE_BMP_RLE, SWT.IMAGE_GIF
	 * SWT.IMAGE_ICO, SWT.IMAGE_JPEG or SWT.IMAGE_PNG
	 * 
	 * @return the bytes of an encoded image for the specified viewer
	 */
	public byte[] createImage(int format) {
		ByteArrayOutputStream result = new ByteArrayOutputStream();

		Device device = this.editor.getGraphViewer().getControl().getDisplay();
		LayerManager lm = (LayerManager) this.editor.getGraphViewer()
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

}