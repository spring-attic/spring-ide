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
package org.springframework.ide.eclipse.boot.properties.editor;

import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SeverityProvider;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.ProblemSeverity;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertyProblem;

/**
 * Implementation {@link SeverityProvider} that simplies returns the hard-coded 'default severity' based
 * from the problem's ProblemType.
 *
 * @author Kris De Volder
 */
public class DefaultSeverityProvider implements SeverityProvider {

	@Override
	public ProblemSeverity getSeverity(SpringPropertyProblem problem) {
		return problem.getType().getDefaultSeverity();
	}

}
