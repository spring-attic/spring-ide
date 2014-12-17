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

		public SpringPropertiesInformationControlCreator() {
		}

		@Override
		public IInformationControl createInformationControl(Shell parent) {
			if (BrowserInformationControl.isAvailable(parent)) {
				return new BrowserInformationControl(parent, PreferenceConstants.APPEARANCE_JAVADOC_FONT, true) {
					@Override
					public IInformationControlCreator getInformationPresenterControlCreator() {
						return SpringPropertiesInformationControlCreator.this;
					}
				};
			}
			return new DefaultInformationControl(parent, true);
		}
	}
