/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.internal.ui.text.correction.AssistContext;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.springframework.ide.eclipse.data.internal.validation.InvalidParameterTypeRule;

@SuppressWarnings("restriction")
public class InvalidParameterTypeMarkerResolutionGenerator implements IMarkerResolutionGenerator2 {

	public IMarkerResolution[] getResolutions(IMarker marker) {
		if (! hasResolutions(marker)) {
			return new IMarkerResolution[0];
		}
		
		IResource resource = marker.getResource();
		ICompilationUnit cu = null;
		if (resource instanceof IFile && resource.isAccessible()) {
			IJavaElement element = JavaCore.create((IFile) resource);
			if (! (element instanceof ICompilationUnit)) {
				return new IMarkerResolution[0];
			}
			cu = (ICompilationUnit) element;
		}
		
		int startPos = marker.getAttribute(IMarker.CHAR_START, 0);
		int endPos = marker.getAttribute(IMarker.CHAR_END, 0);
		AssistContext assistContext = new AssistContext(cu, startPos, endPos - startPos);
		ASTNode astNode = assistContext.getCoveringNode();
		MethodDeclaration methodDecl = getSurroundingMethod(astNode);
		if (methodDecl == null) {
			return new IMarkerResolution[0];
		}
		
		@SuppressWarnings("unchecked")
		List<SingleVariableDeclaration> parameters = methodDecl.parameters();
		if (parameters.size() != 1) {
			return new IMarkerResolution[0];
		}
		
		String propertyTypeName = marker.getAttribute(InvalidParameterTypeRule.PROPERTY_TYPE_ATTR, null);
		String propertyTypePackage = marker.getAttribute(InvalidParameterTypeRule.PROPERTY_TYPE_PACKAGE_ATTR, null);
		return new IMarkerResolution[] { new ChangeParameterTypeResolution(parameters.get(0), propertyTypeName, propertyTypePackage, cu) };
	}
	
	private MethodDeclaration getSurroundingMethod(ASTNode astNode) {
		if (astNode instanceof MethodDeclaration || astNode == null) {
			return (MethodDeclaration) astNode;
		}
		return getSurroundingMethod(astNode.getParent());
	}

	public boolean hasResolutions(IMarker marker) {
		String problemId = marker.getAttribute(IMarker.PROBLEM, null);
		String propertyTypeName = marker.getAttribute(InvalidParameterTypeRule.PROPERTY_TYPE_ATTR, null);
		String propertyTypePackage = marker.getAttribute(InvalidParameterTypeRule.PROPERTY_TYPE_PACKAGE_ATTR, null);
		return InvalidParameterTypeRule.PROBLEM_ID.equals(problemId) 
				&& propertyTypeName != null && propertyTypePackage != null;
	}

}
