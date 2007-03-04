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
