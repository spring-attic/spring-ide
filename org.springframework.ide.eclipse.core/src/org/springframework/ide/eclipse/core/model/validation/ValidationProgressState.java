/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

/**
 * Simple holder to report validation progress against. 
 * @author Christian Dupuis
 * @since 2.3.0
 * @see AbstractValidationContext
 */
class ValidationProgressState {

	private int errorCount = 0;

	private int infoCount = 0;

	private int warningCount = 0;

	public void incrementErrorCount() {
		errorCount++;
	}

	public void incrementErrorCountBy(int n) {
		errorCount += n;
	}

	public void incrementInfoCount() {
		infoCount++;
	}

	public void incrementInfoCountBy(int n) {
		infoCount += n;
	}

	public void incrementWarningCount() {
		warningCount++;
	}

	public void incrementWarningCountBy(int n) {
		warningCount += n;
	}

	protected int getErrorCount() {
		return errorCount;
	}

	protected int getInfoCount() {
		return infoCount;
	}

	protected int getWarningCount() {
		return warningCount;
	}

}
