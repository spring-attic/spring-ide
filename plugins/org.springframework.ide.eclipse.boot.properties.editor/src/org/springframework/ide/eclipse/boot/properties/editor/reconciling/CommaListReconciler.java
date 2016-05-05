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

import org.eclipse.jdt.internal.corext.dom.TypeBindingVisitor;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypeUtil;
import org.springframework.ide.eclipse.editor.support.reconcile.IProblemCollector;

import reactor.core.publisher.Flux;

/**
 * Helper class to reconcile text contained in a document region as a comma-separated list.
 *
 * @author Kris De Volder
 */
public class CommaListReconciler {

	interface TypeBasedReconciler {
		void reconcile(DocumentRegion region, Type expectType, IProblemCollector problems);
	}

	private final TypeBasedReconciler valueReconciler;

	public CommaListReconciler(TypeBasedReconciler valueReconciler) {
		this.valueReconciler = valueReconciler;
	}

	void reconcileValue(DocumentRegion region, Type expectType, IProblemCollector problems) {
	}

	public void reconcile(DocumentRegion region, Type listType, IProblemCollector problems) {
		Type elType = getElementType(listType);
		//Its pointless to reconcile list of we can't determine value type.
		if (elType!=null) {
			DocumentRegion[] pieces = region.split(',');
			for (int i = 0; i < pieces.length; i++) {
				valueReconciler.reconcile(pieces[i].trim(), elType, problems);
			}
		}
	}

	private Type getElementType(Type listType) {
		Type elType = TypeUtil.getDomainType(listType);
		if (elType!=null) {
			Type nestedElType = getElementType(elType);
			if (nestedElType!=null) {
				return nestedElType;
			}
			return elType;
		}
		return null;
	}

}
