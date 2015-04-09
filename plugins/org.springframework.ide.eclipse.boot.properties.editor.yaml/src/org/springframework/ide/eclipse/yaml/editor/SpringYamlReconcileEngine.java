/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.yaml.editor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.IReconcileEngine;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;
import org.springframework.ide.eclipse.boot.properties.editor.util.SpringPropertyIndexProvider;
import org.springframework.ide.eclipse.yaml.editor.ast.YamlASTProvider;

public class SpringYamlReconcileEngine implements IReconcileEngine {

	public SpringYamlReconcileEngine(YamlASTProvider astProvider,
			SpringPropertyIndexProvider indexProvider) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void reconcile(IDocument doc, IProblemCollector problemCollector,
			IProgressMonitor mon) {
		// TODO Auto-generated method stub

	}

}
