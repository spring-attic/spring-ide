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
package org.springframework.ide.eclipse.aop.ui.xref.action;

import org.eclipse.contribution.xref.internal.ui.providers.TreeObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.springframework.ide.eclipse.aop.ui.xref.AopReferenceModelNode;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class OpenBeansAopConfigAction extends Action implements
		IViewActionDelegate {

	private AopReferenceModelNode aopNode;

	public void init(IViewPart view) {
	}

	public void selectionChanged(IAction action, ISelection selection) {
		setEnabled(false);
		Object node = ((IStructuredSelection) selection).getFirstElement();
		if (node instanceof TreeObject) {
			Object obj = ((TreeObject) node).getData();
			if (obj instanceof AopReferenceModelNode) {
				aopNode = (AopReferenceModelNode) obj;
				setEnabled(true);
			}
		}
	}

	public void run(IAction action) {
		if (isEnabled()) {
			IResource resource = aopNode.getResouce();
			if (resource instanceof IFile && resource.exists()) {
				int line = aopNode.getDefinition().getAspectLineNumber();
				SpringUIUtils.openInEditor((IFile) resource, line);
			}
		}
	}
}
