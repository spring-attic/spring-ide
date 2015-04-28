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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.springframework.ide.eclipse.boot.properties.editor.reconciling.SpringPropertiesReconcileEngine.IProblemCollector;

public interface IReconcileEngine {
	public void reconcile(IDocument doc, IProblemCollector problemCollector, IProgressMonitor mon);
}
