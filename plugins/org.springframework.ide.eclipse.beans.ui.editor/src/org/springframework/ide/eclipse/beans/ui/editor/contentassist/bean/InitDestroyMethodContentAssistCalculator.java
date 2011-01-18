/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.editor.contentassist.bean;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.IContentAssistContext;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NodeClassMethodContentAssistCalculator;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.IMethodFilter;
import org.w3c.dom.Node;

/**
 * {@link NodeClassMethodContentAssistCalculator} extension that calculates
 * content assist proposals for <code>init-method</code> and
 * <code>destory-method</code> methods on the <code>bean</code> element.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class InitDestroyMethodContentAssistCalculator extends
		NodeClassMethodContentAssistCalculator {

	private static IMethodFilter FILTER = new FlagsMethodFilter(FlagsMethodFilter.NOT_CONSTRUCTOR
			| FlagsMethodFilter.NOT_INTERFACE, 0);

	
	public InitDestroyMethodContentAssistCalculator() {
		super(FILTER);
	}

	@Override
	protected Node getClassNode(IContentAssistContext context) {
		return context.getNode();
	}

}
