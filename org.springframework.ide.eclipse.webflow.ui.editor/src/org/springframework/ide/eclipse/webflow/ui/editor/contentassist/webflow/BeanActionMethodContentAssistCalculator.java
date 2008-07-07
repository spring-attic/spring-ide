/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.editor.contentassist.webflow;

import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;

/**
 * {@link WebflowActionMethodContentAssistCalculator} extension used to provide
 * content assist for action methods.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class BeanActionMethodContentAssistCalculator extends
		WebflowActionMethodContentAssistCalculator {

	public BeanActionMethodContentAssistCalculator() {
		super(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC
				| FlagsMethodFilter.NOT_INTERFACE
				| FlagsMethodFilter.NOT_CONSTRUCTOR));
	}
}
