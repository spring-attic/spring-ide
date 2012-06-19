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
package org.springframework.ide.eclipse.config.graph.actions;

import java.io.ByteArrayInputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphImages;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.config.graph.AbstractConfigGraphicalEditor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;


/**
 * {@link WorkbenchPartAction} that exports the current content to an image
 * file.
 * @author Leo Dos Santos
 * @since 2.3.1
 */
public class ExportAction extends WorkbenchPartAction {

	public static final String ID = "Export_action"; //$NON-NLS-1$

	private final AbstractConfigGraphicalEditor editor;

	public ExportAction(AbstractConfigGraphicalEditor part) {
		super(part);
		this.editor = part;
		setText(Messages.ExportAction_ACTION_LABEL);
		setId(ID);
		setToolTipText(Messages.ExportAction_TOOLTIP_LABEL);
		setImageDescriptor(BeansGraphImages.DESC_OBJS_EXPORT_ENABLED);
		setDisabledImageDescriptor(BeansGraphImages.DESC_OBJS_EXPORT_DISABLED);
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public void run() {
		SaveAsDialog dialog = new SaveAsDialog(getWorkbenchPart().getSite().getShell());
		dialog.setOriginalName(Messages.ExportAction_JPEG_ORIGINAL_TITLE);
		dialog.create();
		dialog.setMessage(BeansGraphPlugin.getResourceString("Editor.SaveAsDialog.message")); //$NON-NLS-1$
		dialog.setOriginalName(Messages.ExportAction_PNG_ORIGINAL_TITLE);
		dialog.open();
		IPath path = dialog.getResult();
		if (path != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile file = workspace.getRoot().getFile(path);
			String ext = file.getFileExtension();
			if (ext == null || ext.length() == 0
					|| !(ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("png"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				ErrorDialog.openError(getWorkbenchPart().getSite().getShell(), BeansGraphPlugin
						.getResourceString("Editor.SaveError.title"), null, BeansGraphPlugin //$NON-NLS-1$
						.createErrorStatus(BeansGraphPlugin.getResourceString("Editor.SaveAsDialog.error"))); //$NON-NLS-1$
			}
			else if (ext.equalsIgnoreCase("PNG") && !SpringCoreUtils.isEclipseSameOrNewer(3, 3)) { //$NON-NLS-1$
				ErrorDialog.openError(getWorkbenchPart().getSite().getShell(), Messages.ExportAction_ERROR_TITLE,
						Messages.ExportAction_ERROR_PNG_EXPORT_33_OR_NEWER, BeansGraphPlugin
								.createErrorStatus(BeansGraphPlugin.getResourceString("Editor.SaveAsDialog.error"))); //$NON-NLS-1$
			}
			else {
				if ("PNG".equalsIgnoreCase(ext)) { //$NON-NLS-1$
					saveImage(file, SWT.IMAGE_PNG);
				}
				else if ("JPG".equalsIgnoreCase(ext) || "JPEG".equalsIgnoreCase(ext)) { //$NON-NLS-1$ //$NON-NLS-2$
					saveImage(file, SWT.IMAGE_JPEG);
				}
				else if ("BMP".equalsIgnoreCase(ext)) { //$NON-NLS-1$
					saveImage(file, SWT.IMAGE_BMP);
				}
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
			public void execute(final IProgressMonitor monitor) throws CoreException {
				try {
					if (file.exists()) {
						file.setContents(new ByteArrayInputStream(editor.createImage(format)), true, false, monitor);
					}
					else {
						file.create(new ByteArrayInputStream(editor.createImage(format)), true, monitor);
					}
				}
				catch (CoreException e) {
					ErrorDialog.openError(getWorkbenchPart().getSite().getShell(), BeansGraphPlugin
							.getResourceString("Editor.SaveError.title"), BeansGraphPlugin //$NON-NLS-1$
							.getResourceString("Editor.SaveError.text"), e.getStatus()); //$NON-NLS-1$
				}
			}
		};

		try {
			Shell shell = getWorkbenchPart().getSite().getWorkbenchWindow().getShell();
			new ProgressMonitorDialog(shell).run(false, true, op);
		}
		catch (InvocationTargetException e) {
			BeansGraphPlugin.log(e);
		}
		catch (InterruptedException e) {
		}
	}

}
