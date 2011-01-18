/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.inplace;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;

/**
 * {@link Action} implementations that opens the selected element.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class OpenRevealableReferenceNodeAction extends Action {

	private TreeViewer viewer;

	public OpenRevealableReferenceNodeAction(Shell shell, TreeViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void run() {
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection) selection).getFirstElement();
			if (obj instanceof IRevealableReferenceNode) {
				((IRevealableReferenceNode) obj).openAndReveal();
			}
		}
	}

}
