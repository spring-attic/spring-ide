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
package org.springframework.ide.eclipse.boot.validation.rules;

import org.springframework.ide.eclipse.editor.support.reconcile.ProblemSeverity;
import org.springframework.ide.eclipse.editor.support.reconcile.ProblemType;

public class BootValidationProblemType implements ProblemType {
	
	private final String id;
	private final ProblemSeverity defaultSeverity;

	public BootValidationProblemType(String id, ProblemSeverity defaultSeverity) {
		this.id = id;
		this.defaultSeverity = defaultSeverity;
	}
	
	@Override
	public ProblemSeverity getDefaultSeverity() {
		return defaultSeverity;
	}

	@Override
	public String toString() {
		return id;
	}

	public String getId() {
		return id;
	}
}
