/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.provider;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;

/**
 * {@link ASTVisitor} that captures all ASTNodes which represent resource annotations.
 * @author Jared Rodriguez
 * @since 2.0.5
 */
public class ASTAnnotationVisitor extends ASTVisitor {
	
	private ArrayList<ASTNode> annotations = new ArrayList<ASTNode>();

	private String packageName = null;

	/**
	 * Visit {@link MarkerAnnotation} to find Autowired annotations.
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("org.springframework.beans.factory.annotation.Autowired")
				|| name.equals("Autowired"))
			annotations.add(node);
		return false;
	}

	/**
	 * Visit {@link PackageDeclaration} to extract the package declaration.
	 */
	@Override
	public boolean visit(PackageDeclaration node) {
		packageName = node.getName().getFullyQualifiedName();
		return false;
	}

	/**
	 * Visit {@link NormalAnnotation} where we can find Resource annotations.
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		String name = node.getTypeName().getFullyQualifiedName();
		if (name.equals("javax.annotation.Resource") || name.equals("Resource"))
			annotations.add(node);
		return false;
	}

	/**
	 * The package name of the object being processed.
	 * @return the package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * A list of all relevant annotations visited.
	 * @return a list of annotations
	 */
	public List<ASTNode> getAnnotations() {
		return annotations;
	}

}
