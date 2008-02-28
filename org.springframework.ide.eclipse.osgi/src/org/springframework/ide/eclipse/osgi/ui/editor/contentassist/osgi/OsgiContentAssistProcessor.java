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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.BeanReferenceContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.ClassContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.MethodContentAssistCalculator;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.NamespaceContentAssistProcessorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.java.FlagsMethodFilter;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link INamespaceContentAssistProcessor} implementation responsible for the
 * <code>osgi:*</code> namespace.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class OsgiContentAssistProcessor extends NamespaceContentAssistProcessorSupport {

	@Override
	public void init() {
		BeanReferenceContentAssistCalculator beanRef = new BeanReferenceContentAssistCalculator(
				true);
		registerContentAssistCalculator("service", "ref", beanRef);
		registerContentAssistCalculator("service", "depends-on", beanRef);
		registerContentAssistCalculator("bundle", "depends-on", beanRef);
		registerContentAssistCalculator("property-placeholder", "defaults-ref", beanRef);
		registerContentAssistCalculator("reference", "ref", beanRef);
		registerContentAssistCalculator("reference", "depends-on", beanRef);
		registerContentAssistCalculator("registration-listener", "ref", beanRef);

		ClassContentAssistCalculator classRef = new ClassContentAssistCalculator(true);
		registerContentAssistCalculator("service", "interface", classRef);
		registerContentAssistCalculator("reference", "interface", classRef);

		MethodContentAssistCalculator methodRef = new MethodContentAssistCalculator(
				new FlagsMethodFilter(FlagsMethodFilter.NOT_INTERFACE
						| FlagsMethodFilter.NOT_CONSTRUCTOR)) {

			@Override
			protected IType calculateType(ContentAssistRequest request, String attributeName) {
				if (request.getNode() != null
						&& "registration-listener".equals(request.getNode().getLocalName())) {
					String ref = BeansEditorUtils.getAttribute(request.getNode(),
							"ref");
					if (ref != null) {
						IFile file = BeansEditorUtils.getFile(request);
						String className = BeansEditorUtils.getClassNameForBean(file, request
								.getNode().getOwnerDocument(), ref);
						return JdtUtils.getJavaType(file.getProject(), className);
					}
				}
				return null;
			}
		};
		registerContentAssistCalculator("registration-method", methodRef);
		registerContentAssistCalculator("unregistration-method", methodRef);

	}
}
