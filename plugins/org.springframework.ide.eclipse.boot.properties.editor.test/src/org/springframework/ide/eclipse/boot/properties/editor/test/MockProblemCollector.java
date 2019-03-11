/*******************************************************************************
 * Copyright (c) 2015,2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;
import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class MockProblemCollector implements IProblemCollector {

	private List<ReconcileProblem> problems = null;
	private final Set<ProblemType> ignoredTypes;

	public MockProblemCollector() {
		this(ImmutableSet.of());
	}

	public MockProblemCollector(Set<ProblemType> ignoredTypes) {
		this.ignoredTypes = ignoredTypes;
	}

	public List<ReconcileProblem> getAllProblems() {
		return problems;
	}

	public void beginCollecting() {
		problems = new ArrayList<>();
	}

	public void endCollecting() {
	}

	public void accept(ReconcileProblem e) {
		if (!ignoredTypes.contains(e.getType())) {
			problems.add(e);
		}
	}
}