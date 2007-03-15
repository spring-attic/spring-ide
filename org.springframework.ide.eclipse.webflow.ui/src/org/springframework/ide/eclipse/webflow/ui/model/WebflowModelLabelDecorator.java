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

package org.springframework.ide.eclipse.webflow.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.ui.Activator;

/**
 * This decorator adds an overlay image to all Spring web flow files and their
 * corresponding folders. This decoration is refreshed on every modification to
 * the Spring web flow. Therefore the decorator adds a
 * {@link IWebflowModelListener change listener} to the {@link IWebflowModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelLabelDecorator extends LabelProvider implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = Activator.PLUGIN_ID
			+ ".model.webflowModelLabelDecorator";

	private IWebflowModelListener listener;

	public WebflowModelLabelDecorator() {
		listener = new IWebflowModelListener() {
			public void modelChanged(IWebflowProject project) {
				update();
			}
		};
		org.springframework.ide.eclipse.webflow.core.Activator.getModel()
				.registerModelChangeListener(listener);
	}

	public void decorate(Object element, IDecoration decoration) {

		if (element instanceof IFolder) {
			decorateFolder((IFolder) element, decoration);
		}
		else if (element instanceof IFile) {
			decorateFile((IFile) element, decoration);
		}
	}

	protected void decorateFolder(IFolder folder, IDecoration decoration) {
		IWebflowModel model = org.springframework.ide.eclipse.webflow.core.Activator
				.getModel();
		IWebflowProject project = model.getProject(folder.getProject());
		if (project != null) {
			String path = folder.getProjectRelativePath().toString() + '/';
			for (IWebflowConfig config : project.getConfigs()) {
				if (config.getResource().getProjectRelativePath().toString()
						.startsWith(path)) {
					decoration.addOverlay(WebflowUIImages.DESC_OVR_WEBFLOW);
					break;
				}
			}
		}
	}

	protected void decorateFile(IFile file, IDecoration decoration) {
		IWebflowModel model = org.springframework.ide.eclipse.webflow.core.Activator
				.getModel();
		IWebflowProject project = model.getProject(file.getProject());
		if (project != null) {
			for (IWebflowConfig config : project.getConfigs()) {
				// The following comparison works for archived config files too
				if (config.getResource().equals(file)) {
					decoration.addOverlay(WebflowUIImages.DESC_OVR_WEBFLOW);
					break;
				}
			}
		}
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void dispose() {
		org.springframework.ide.eclipse.webflow.core.Activator.getModel()
				.removeModelChangeListener(listener);
	}

	public static final void update() {
		SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.getDecoratorManager().update(DECORATOR_ID);
			}
		});
	}
}
