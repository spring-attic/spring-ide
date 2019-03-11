/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import org.eclipse.jdt.internal.ui.text.CompositeReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;

@SuppressWarnings("restriction")
public class ReconcilingUtil {

	public static IReconcilingStrategy compose(IReconcilingStrategy s1, IReconcilingStrategy s2) {
		if (s1==null) {
			return s2;
		}
		if (s2==null) {
			return s1;
		}
		CompositeReconcilingStrategy composite = new CompositeReconcilingStrategy();
		composite.setReconcilingStrategies(new IReconcilingStrategy[] {s1, s2});
		return composite;
	}

}
