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
package org.springframework.ide.eclipse.editor.support.yaml.reconcile;

import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.yaml.schema.YTypeUtil;

public class TypeBasedYamlASTReconciler {

	private final IProblemCollector problems;
	private final YTypeUtil typeUtil;

	public TypeBasedYamlASTReconciler(IProblemCollector problems, YTypeUtil typeUtil) {
		this.problems = problems;
		this.typeUtil = typeUtil;
	}

}
