/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.yaml.reconcile;

import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblemImpl;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
import org.yaml.snakeyaml.nodes.Node;

/**
 * Methods for creating reconciler problems for Schema based reconciler implementation.
 *
 * @author Kris De Volder
 */
public class YamlSchemaProblems {

	private static final ProblemType SCHEMA_PROBLEM = problemType("YamlSchemaProblem");
	private static final ProblemType SYNTAX_PROBLEM = problemType("YamlSyntaxProblem");

	public static ProblemType problemType(final String typeName) {
		return new ProblemType() {
			@Override
			public String getId() {
				return typeName;
			}
			@Override
			public String toString() {
				return getId();
			}
			@Override
			public ProblemSeverity getDefaultSeverity() {
				return ProblemSeverity.ERROR;
			}
			@Override
			public String getLabel() {
				//TODO: if we want a prefs page that allows controlling the severities of problems we need to implement this properly
				return getId();
			}
			@Override
			public String getDescription() {
				//TODO: if we want a prefs page that allows controlling the severities of problems we need to implement this properly
				return getId();
			}
		};
	}

	public static ReconcileProblem syntaxProblem(String msg, int offset, int len) {
		return new ReconcileProblemImpl(SYNTAX_PROBLEM, msg, offset, len);
	}

	public static ReconcileProblem schemaProblem(String msg, Node node) {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return new ReconcileProblemImpl(SCHEMA_PROBLEM, msg, start, end-start);
	}
}
