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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.springframework.ide.eclipse.editor.support.reconcile.ReconcileProblem;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;

/**
 * Provides a means to map {@link SpringPropertyProblem} to a {@link ProblemSeverity}. Problems mapped to 'IGNORE'
 * severity will suppressed.
 *
 * @author Kris De Volder
 */
public interface SeverityProvider {

	ProblemSeverity getSeverity(ReconcileProblem problem);

	/**
	 * Why is this here? So that SeverityProvider's that cache severities may clear the cache prior to
	 * reconciling to ensure that up-to-date severities are provided during the session.
	 */
	void startReconciling();

}
