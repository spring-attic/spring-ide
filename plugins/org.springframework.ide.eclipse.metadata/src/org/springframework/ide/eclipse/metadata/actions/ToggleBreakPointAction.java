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
package org.springframework.ide.eclipse.metadata.actions;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.actions.ToggleBreakpointAdapter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingMethodAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingMethodToClassMap;


/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class ToggleBreakPointAction extends BaseSelectionListenerAction {

	private IWorkbenchPart workbenchPart;

	private ToggleBreakpointAdapter breakpointAdapter;

	public ToggleBreakPointAction(IWorkbenchPart workbenchPart) {
		super(Messages.ToggleBreakPointAction_TITLE);
		setImageDescriptor(MetadataUIImages.DESC_OBJS_BREAKPOINT);
		this.workbenchPart = workbenchPart;
		breakpointAdapter = new ToggleBreakpointAdapter();
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		Object obj = selection.getFirstElement();

		if (obj instanceof RequestMappingAnnotationMetadata) {
			RequestMappingAnnotationMetadata annotation = (RequestMappingAnnotationMetadata) obj;
			IType type = (IType) JavaCore.create(annotation.getClassHandle());
			breakpointAdapter.toggleClassBreakpoints(workbenchPart,
					new StructuredSelection(type));
		} else if (obj instanceof RequestMappingMethodToClassMap) {
			RequestMappingMethodAnnotationMetadata annotation = ((RequestMappingMethodToClassMap) obj)
					.getMethodMetadata();
			IMethod method = (IMethod) JavaCore.create(annotation
					.getHandleIdentifier());
			breakpointAdapter.toggleMethodBreakpoints(workbenchPart,
					new StructuredSelection(method));
		}
	}

}
