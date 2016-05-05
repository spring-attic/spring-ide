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
package org.springframework.ide.eclipse.boot.properties.editor.reconciling;

import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;

/**
 * Helper class to reconcile text contained in a document region as a comma-separated list.
 *
 * @author Kris De Volder
 */
public class CommaListReconciler {

	TypeUtil typeUtil;

	public CommaListReconciler(TypeUtil typeUtil) {
		this.typeUtil = typeUtil;
	}


	public void reconcile(DocumentRegion region, Type type, IProblemCollector problems) {
		//TODO: implement this
	}

}
