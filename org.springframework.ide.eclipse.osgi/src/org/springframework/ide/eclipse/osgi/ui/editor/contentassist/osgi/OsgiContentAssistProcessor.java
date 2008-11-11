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
package org.springframework.ide.eclipse.osgi.ui.editor.contentassist.osgi;

import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;

/**
 * {@link INamespaceContentAssistProcessor} implementation responsible for the <code>osgi:*</code>
 * namespace.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);		
		registerContentAssistCalculator("depends-on", beanRef);
		registerContentAssistCalculator("ref", beanRef);
		registerContentAssistCalculator("property-placeholder", "defaults-ref", beanRef);

		ClassContentAssistCalculator classRef = new ClassContentAssistCalculator(true);
		registerContentAssistCalculator("interface", classRef);

		MethodContentAssistCalculator methodRef = new RegistrationMethodContentAssistCalculator();
		registerContentAssistCalculator("registration-method", methodRef);
		registerContentAssistCalculator("unregistration-method", methodRef);

	}
}
