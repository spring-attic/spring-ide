/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanProperty;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * Opens the Java type for currently selected {@link IBean} or
 * {@link IBeanProperty}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenJavaElementAction extends AbstractNavigatorAction {

	private Object element;

	public OpenJavaElementAction(ICommonActionExtensionSite site) {
		super(site);
		setText("Open Java &Element"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			if (sElement instanceof IBean || sElement instanceof IBeanProperty) {
				element = (ISourceModelElement) sElement;
				return true;
			}
			else if (sElement instanceof IJavaElement) {
				element = sElement;
				return true;
			}
			else if (sElement instanceof BeanMetadataNode) {
				element = ((BeanMetadataNode) sElement).getLocation();
				return true;
			}
		}
		return false;
	}

	@Override
	public final void run() {
		IJavaElement javaElement;
		if (element instanceof IBean) {
			javaElement = BeansModelUtils.getBeanType((IBean) element, null);
		}
		else if (element instanceof IBeanProperty) {
			javaElement = BeansModelUtils.getPropertyMethod((IBeanProperty) element, null);
		}
		else if (element instanceof IJavaElement) {
			javaElement = (IJavaElement) element;
		}
		else {
			javaElement = null;
		}
		if (javaElement != null) {
			SpringUIUtils.openInEditor(javaElement);
		}
		if (element instanceof IModelSourceLocation) {
			IModelSourceLocation location = (IModelSourceLocation) element;
			SpringUIUtils.openInEditor(((FileResource) ((IModelSourceLocation) element).getResource()).getRawFile(), location.getStartLine(), true);
		}
	}
}
