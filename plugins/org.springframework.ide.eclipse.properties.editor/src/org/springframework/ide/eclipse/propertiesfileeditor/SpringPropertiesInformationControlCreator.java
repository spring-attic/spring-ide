/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.propertiesfileeditor;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;

/**
 * IInformationControlCreator for 'hover information' associated with Spring properties. This control is
 * used in two different contexts. 
 *  
 *    - tooltip info shown when hovering over a property 
 *    - side view for content assist that proposes property completions.  
 */
@SuppressWarnings("restriction")
public class SpringPropertiesInformationControlCreator implements IInformationControlCreator {

	/**
	 * Status text shown in the bottom 'status' area of the control (but it is only shown on the 
	 * non-enriched version of the control)
	 */
	private String statusText;

	/**
	 * Whether or not a 'enriched' version of the control should be created. Information controls generally
	 * have two different forms a 'plain' non-resizable form without scrollbars and toolbar and
	 * an 'enriched' version which may have a toolbar and scrollbars and is shown when the control
	 * has focus.
	 */
	private boolean enriched;

	public SpringPropertiesInformationControlCreator(String statusText) {
		this(false, statusText);
	}

	private SpringPropertiesInformationControlCreator(boolean enriched, String statusText) {
		this.enriched = enriched;
		this.statusText = statusText;
	}

	@Override
	public IInformationControl createInformationControl(Shell parent) {
		if (BrowserInformationControl.isAvailable(parent)) {
			if (!enriched) {
				return new BrowserInformationControl(parent, PreferenceConstants.APPEARANCE_JAVADOC_FONT, statusText) {
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return new SpringPropertiesInformationControlCreator(true, null);
					}
				};
			} else {
				ToolBarManager toolbar = createToolbar();
				return new BrowserInformationControl(parent, PreferenceConstants.APPEARANCE_JAVADOC_FONT, toolbar) {
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return new SpringPropertiesInformationControlCreator(true, null);
					}
				};

			}
		}
		return new DefaultInformationControl(parent, true);
	}

	/**
	 * Creates an optional toolbar for the enriched version of the control.
	 */
	protected ToolBarManager createToolbar() {
		return null;
	}
}
