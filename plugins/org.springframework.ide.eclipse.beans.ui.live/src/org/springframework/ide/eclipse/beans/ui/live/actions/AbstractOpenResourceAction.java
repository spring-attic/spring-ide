/*******************************************************************************
 * Copyright (c) 2013, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.actions;

import org.eclipse.jdt.core.IType;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 * @author Alex Boyko
 */
public abstract class AbstractOpenResourceAction extends BaseSelectionListenerAction {
	
	protected AbstractOpenResourceAction(String text) {
		super(text);
	}
	
	protected String extractClassName(String resourcePath) {
		int index = resourcePath.lastIndexOf("/WEB-INF/classes/");
		int length = "/WEB-INF/classes/".length();
		if (index >= 0) {
			resourcePath = resourcePath.substring(index + length);
		}
		resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf(".class"));
		resourcePath = resourcePath.replaceAll("\\\\|\\/", "."); //Tolerate both '/' and '\'.
		return resourcePath;
	}

	protected String extractResourcePath(String resourceStr) {
		// Extract the resource path out of the descriptive text
		int indexStart = resourceStr.indexOf("[");
		int indexEnd = resourceStr.indexOf("]");
		if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
			resourceStr = resourceStr.substring(indexStart + 1, indexEnd);
		}
		return resourceStr;
	}

	protected boolean hasTypeInProject(TypeLookup workspaceContext, String className) {
		return workspaceContext.findType(className) != null;
	}

	protected void openInEditor(TypeLookup workspaceContext, String className) {
		IType type = workspaceContext.findType(className);
		if (type != null) {
			SpringUIUtils.openInEditor(type);
		}
	}

}
