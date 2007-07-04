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
package org.springframework.ide.eclipse.webflow.ui.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringLabelDecorator;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
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
public class WebflowModelLabelDecorator extends SpringLabelDecorator implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = Activator.PLUGIN_ID
			+ ".model.webflowModelLabelDecorator";

	public static final void update() {
		SpringUIUtils.updateDecorator(SpringLabelDecorator.DECORATOR_ID);
		SpringUIUtils.updateDecorator(DECORATOR_ID);
	}

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
		else if (element instanceof IWebflowModelElement) {
			decorateWebflowModelElement(((IModelElement) element), decoration);
		}
		else if (element instanceof IWebflowProject) {
			decorateWebflowModelElement(((IModelElement) element), decoration);
		}
	}

	protected void decorateFile(IFile file, IDecoration decoration) {
		if (WebflowModelUtils.isWebflowConfig(file)) {
			addErrorOverlay(decoration, getSeverity(file));
			decoration.addOverlay(WebflowUIImages.DESC_OVR_WEBFLOW);
		}
	}

	protected void decorateFolder(IFolder folder, IDecoration decoration) {
		IWebflowModel model = org.springframework.ide.eclipse.webflow.core.Activator
				.getModel();
		if (model.hasProject(folder.getProject())) {
			IWebflowProject project = model.getProject(folder.getProject());
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

	/**
	 * Adds error and warning decorations to {@link IWebflowModelElement}.
	 * @since 2.0.1
	 */
	private void decorateWebflowModelElement(IModelElement element,
			IDecoration decoration) {
		addErrorOverlay(decoration, getSeverity(element));
	}

	public void dispose() {
		org.springframework.ide.eclipse.webflow.core.Activator.getModel()
				.removeModelChangeListener(listener);
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	protected int getSeverity(Object element) {
		if (element instanceof IWebflowProject) {
			int severity = 0;
			for (IWebflowConfig config : ((IWebflowProject) element)
					.getConfigs()) {
				severity = MarkerUtils.getHighestSeverityFromMarkersInRange(
						config.getResource(), -1, -1);
				if (severity == IMarker.SEVERITY_ERROR) {
					break;
				}
			}
			return severity;
		}
		else if (element instanceof IWebflowConfig) {
			return MarkerUtils.getHighestSeverityFromMarkersInRange(
					((IWebflowConfig) element).getResource(), -1, -1);
		}
		else if (element instanceof IResource) {
			return MarkerUtils.getHighestSeverityFromMarkersInRange(
					(IResource) element, -1, -1);
		}
		return 0;
	}
}
