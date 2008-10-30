/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.java.OrMethodFilter;
import org.w3c.dom.Node;

/**
 * {@link NodeClassMethodContentAssistCalculator} that calculates content assist proposals for the
 * <code>lookup-method</code> element.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class LookupMethodContentAssistCalculator extends NodeClassMethodContentAssistCalculator {

	private static IMethodFilter FILTER;

	static {
		OrMethodFilter filter = new OrMethodFilter();
		filter.addMethodFilter(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC
				| FlagsMethodFilter.NOT_CONSTRUCTOR | FlagsMethodFilter.NOT_INTERFACE, 0));
		filter.addMethodFilter(new FlagsMethodFilter(FlagsMethodFilter.PROTECTED
				| FlagsMethodFilter.NOT_CONSTRUCTOR | FlagsMethodFilter.NOT_INTERFACE, 0));
		FILTER = filter;
	}

	public LookupMethodContentAssistCalculator() {
		super(FILTER);
	}

	@Override
	protected Node getClassNode(IContentAssistContext context) {
		return context.getParentNode();
	}
}
