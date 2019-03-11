/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;

/**
 * {@link WebflowActionMethodContentAssistCalculator} extension used to propose
 * bean-action method proposals.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanMethodContentAssistCalculator extends
		WebflowActionMethodContentAssistCalculator {

	private static final String EVENT_CLASS = 
		"org.springframework.webflow.execution.Event";

	private static final String REQUEST_CONTEXT_CLASS = 
		"org.springframework.webflow.execution.RequestContext";

	public BeanMethodContentAssistCalculator() {
		super(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC
				| FlagsMethodFilter.NOT_INTERFACE
				| FlagsMethodFilter.NOT_CONSTRUCTOR, EVENT_CLASS,
				new String[] { REQUEST_CONTEXT_CLASS }));
	}
}
