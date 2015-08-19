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

/**
 * Provides a means to map {@link SpringPropertyProblem} to a {@link ProblemSeverity}. Problems mapped to 'IGNORE'
 * severity will suppressed.
 *
 * @author Kris De Volder
 */
public interface SeverityProvider {

	ProblemSeverity getSeverity(SpringPropertyProblem problem);

}
