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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.metadata.MetadataUIImages;
import org.springframework.ide.eclipse.metadata.core.RequestMappingAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.core.RequestMappingMethodAnnotationMetadata;
import org.springframework.ide.eclipse.metadata.ui.RequestMappingMethodToClassMap;
import org.springframework.ide.eclipse.ui.SpringUIUtils;


/**
 * @author Leo Dos Santos
 */
public class OpenInJavaEditorAction extends BaseSelectionListenerAction {

	public OpenInJavaEditorAction() {
		super(Messages.OpenInJavaEditorAction_TITLE);
		setImageDescriptor(MetadataUIImages.DESC_OBJS_JAVA_FILE);
	}

	@Override
	public void run() {
		IModelSourceLocation sourceLocation = null;
		IStructuredSelection selection = getStructuredSelection();
		Object obj = selection.getFirstElement();

		if (obj instanceof RequestMappingAnnotationMetadata) {
			RequestMappingAnnotationMetadata annotation = (RequestMappingAnnotationMetadata) obj;
			sourceLocation = annotation.getElementSourceLocation();
		} else if (obj instanceof RequestMappingMethodToClassMap) {
			RequestMappingMethodAnnotationMetadata annotation = ((RequestMappingMethodToClassMap) obj)
					.getMethodMetadata();
			sourceLocation = annotation.getElementSourceLocation();
		}

		if (sourceLocation != null) {
			SpringUIUtils.openInEditor(((FileResource) sourceLocation
					.getResource()).getRawFile(),
					sourceLocation.getStartLine(), true);
		}
	}

}
