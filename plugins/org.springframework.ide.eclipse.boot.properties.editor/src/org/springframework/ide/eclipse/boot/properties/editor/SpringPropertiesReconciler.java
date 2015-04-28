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

import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;

/**
 * WE unforytunately must subclass this just to make it possible to call non
 * public method 'forceReconcile'.
 *
 * We need this to trigger a reconcile when live metadata has changed.
 */
public class SpringPropertiesReconciler extends MonoReconciler {

	public SpringPropertiesReconciler(IReconcilingStrategy strategy) {
		super(strategy, false);
	}

	public void forceReconcile() {
		super.forceReconciling();
	}

}