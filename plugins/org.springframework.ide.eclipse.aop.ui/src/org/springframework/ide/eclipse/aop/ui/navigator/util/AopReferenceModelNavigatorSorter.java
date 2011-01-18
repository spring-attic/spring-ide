/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.navigator.util;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;

/**
 * {@link ViewerSorter} that sorts the tree elements depending on element's
 * resources and their line location in the resource.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 * @see IRevealableReferenceNode
 */
public class AopReferenceModelNavigatorSorter extends ViewerSorter {

	@Override
	public int category(Object element) {
		if (element instanceof IAopReference) {
			return 1;
		}
		return 0;
	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof IRevealableReferenceNode
				&& e2 instanceof IRevealableReferenceNode) {
			IRevealableReferenceNode ref1 = (IRevealableReferenceNode) e1;
			IRevealableReferenceNode ref2 = (IRevealableReferenceNode) e2;
			if (ref1.getResource() != null
					&& ref1.getResource().equals(ref2.getResource())) {
				int l1 = ref1.getLineNumber();
				int l2 = ref2.getLineNumber();
				if (l1 < l2) {
					return -1;
				}
				else if (l1 == l2) {
					return 0;
				}
				else if (l1 > l2) {
					return 1;
				}
			}
			else {
				super.compare(viewer, ref1.getResource(), ref2.getResource());
			}
		}
		return super.compare(viewer, e1, e2);
	}
}
