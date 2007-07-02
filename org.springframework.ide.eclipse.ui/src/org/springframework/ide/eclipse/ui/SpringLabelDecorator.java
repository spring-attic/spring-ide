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
package org.springframework.ide.eclipse.ui;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * This decorator adds an overlay image to all Spring Projects.
 * @author Christian Dupuis
 * @since 2.1
 */
public class SpringLabelDecorator extends LabelProvider implements
		ILightweightLabelDecorator {

	public static final String DECORATOR_ID = SpringUIPlugin.PLUGIN_ID
			+ ".model.modelLabelDecorator";

	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof ISpringProject) {
			decorateModelElement(((ISpringProject) element), decoration);
		}
	}

	public static void update() {
		SpringUIUtils.getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbench workbench = PlatformUI.getWorkbench();
				workbench.getDecoratorManager().update(DECORATOR_ID);
			}
		});
	}

	/**
	 * Adds error and warning decorations to {@link IBeansModelElement}.
	 */
	private void decorateModelElement(IModelElement element,
			IDecoration decoration) {
		addErrorOverlay(decoration, getSeverity(element));
	}

	protected final void addErrorOverlay(IDecoration decoration, int severity) {
		if (severity == IMarker.SEVERITY_WARNING) {
			decoration.addOverlay(SpringUIImages.DESC_OVR_WARNING,
					IDecoration.BOTTOM_LEFT);
		}
		else if (severity == IMarker.SEVERITY_ERROR) {
			decoration.addOverlay(SpringUIImages.DESC_OVR_ERROR,
					IDecoration.BOTTOM_LEFT);
		}
	}

	protected int getSeverity(Object element) {
		if (element instanceof ISpringProject) {
			return MarkerUtils.getHighestSeverityFromMarkersInRange(
					((ISpringProject) element).getProject(), -1, -1);
		}
		return 0;
	}
}
