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
package org.springframework.ide.eclipse.config.ui.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;

/**
 * Action for adding the selected file as a beans configuration file.
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Tomasz Zarna
 * @since 2.3.2
 */
public class AddAsBeanConfigAction extends Action implements IObjectActionDelegate {

	private Set<IFile> selectedFiles;

	public void run(IAction action) {
		for (IFile selectedFile : selectedFiles) {
			IBeansProject springProject = BeansCorePlugin.getModel().getProject(selectedFile.getProject());
			((BeansProject) springProject).addConfig(selectedFile, IBeansConfig.Type.MANUAL);
			((BeansProject) springProject).saveDescription();
		}
		BeansModelLabelDecorator.update();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		selectedFiles = new HashSet<IFile>();
		if (selection instanceof StructuredSelection) {
			for (Object object : ((StructuredSelection) selection).toArray()) {
				if (object instanceof IFile) {
					IFile file = (IFile) object;
					if (file != null) {
						if (isBeansProject(file.getProject()) && isBeansConfigFileToBe(file)) {
							selectedFiles.add(file);
						}
					}
				}
			}
		}

		action.setEnabled(selectedFiles.size() > 0);
	}

	private boolean isBeansProject(IProject project) {
		return BeansCorePlugin.getModel().getProject(project) != null;
	}

	private boolean isBeansConfigFileToBe(IFile file) {
		try {
			IContentTypeManager contentTypeManager = Platform.getContentTypeManager();
			InputStream contents = file.getContents();
			try {
				IContentType contentType = contentTypeManager.findContentTypeFor(contents, file.getName());
				if (contentType != null
						&& contentType.isKindOf(contentTypeManager
								.getContentType("com.springsource.sts.config.ui.beanConfigFile")) //$NON-NLS-1$
						&& !BeansCoreUtils.isBeansConfig(file)) {
					return true;
				}
			}
			finally {
				contents.close();
			}
		}
		catch (IOException e) {
			// if something goes wrong, treats the file as non spring content
			// type
		}
		catch (CoreException e) {
			// if something goes wrong, treats the file as non spring content
			// type
		}
		return false;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

}
