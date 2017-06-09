/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cloudfoundry.manifest.editor;

import static org.springframework.ide.eclipse.editor.support.yaml.reconcile.YamlSchemaProblems.problemType;

import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

/**
 * 
 */
public class ManifestYamlSchemaProblemsTypes {

	public static final ProblemType UNKNOWN_SERVICES_PROBLEM = problemType("UnknownServicesProblem", ProblemSeverity.WARNING);
	public static final ProblemType UNKNOWN_DOMAIN_PROBLEM = problemType("UnknownDomainProblem", ProblemSeverity.WARNING);
	public static final ProblemType UNKNOWN_STACK_PROBLEM = problemType("UnknownStackProblem", ProblemSeverity.WARNING);
	public static final ProblemType IGNORED_PROPERTY = problemType("IgnoredProperty", ProblemSeverity.WARNING);
	public static final ProblemType MUTUALLY_EXCLUSIVE_PROPERTY_PROBLEM = problemType("MutuallyExclusiveProperty",
			ProblemSeverity.ERROR);
	
}
