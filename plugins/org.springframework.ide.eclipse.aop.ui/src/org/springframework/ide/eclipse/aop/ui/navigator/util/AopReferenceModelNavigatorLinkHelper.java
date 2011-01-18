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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ILinkHelper;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;

/**
 * {@link ILinkHelper} implementation that links the current selected element in
 * the workbench to elements of the {@link CommonNavigator}.
 * This class is currently not used.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelNavigatorLinkHelper implements ILinkHelper {

	public void activateEditor(IWorkbenchPage page,
			IStructuredSelection selection) {
		if (selection != null && !selection.isEmpty()) {
			if (selection.getFirstElement() instanceof IRevealableReferenceNode) {
				// TODO fix selection hen & egg problem with ISelectionListener
				/*
				 * IRevealableReferenceNode method = (IRevealableReferenceNode)
				 * selection .getFirstElement(); IEditorInput input = new
				 * FileEditorInput((IFile) method.getResource()); IEditorPart
				 * editor = page.findEditor(input); if (editor != null) {
				 * method.openAndReveal(); }
				 */
			}
		}
	}

	public IStructuredSelection findSelection(IEditorInput input) {
		return StructuredSelection.EMPTY;
	}
}
