/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.reconcile;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.editor.support.yaml.ast.YamlFileAST;

/**
 * Reconciler that consists from a number of independent YAML reconcilers.
 *
 * @author Alex Boyko
 *
 */
public class CompositeYamlASTReconciler implements YamlASTReconciler {

	private YamlASTReconciler[] reconcilers;

	public CompositeYamlASTReconciler(YamlASTReconciler... reconcilers) {
		this.reconcilers = reconcilers;
	}

	@Override
	public void reconcile(YamlFileAST ast, IProgressMonitor mon) {
		Arrays.stream(reconcilers).forEach(r -> r.reconcile(ast, mon));
	}

}
