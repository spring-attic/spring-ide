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
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;

/**
 * Utility class that encapsulates the loading of a {@link IDOMDocument} from
 * the given {@link IFile}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings( { "restriction", "unused" })
public abstract class AbstractAspectDefinitionBuilder implements
		IAspectDefinitionBuilder {
	
	protected static final String ADVICE_REF_ATTRIBUTE = "advice-ref";

	protected static final String ADVISOR_ELEMENT = "advisor";

	protected static final String AFTER_ELEMENT = "after";

	protected static final String AFTER_RETURNING_ELEMENT = "after-returning";

	protected static final String AFTER_THROWING_ELEMENT = "after-throwing";

	protected static final String AOP_NAMESPACE_URI = "http://www.springframework.org/schema/aop";

	protected static final String ARG_NAMES_ATTRIBUTE = "arg-names";

	protected static final String AROUND_ELEMENT = "around";

	protected static final String ASPECT_ELEMENT = "aspect";

	protected static final String BEFORE_ELEMENT = "before";

	protected static final String CONFIG_ELEMENT = "config";

	protected static final String DECLARE_PARENTS_ELEMENT = "declare-parents";

	protected static final String DEFAULT_IMPL_ATTRIBUTE = "default-impl";

	protected static final String EXPRESSION_ATTRIBUTE = "expression";

	protected static final String ID_ATTRIBUTE = "id";

	protected static final String IMPLEMENT_INTERFACE_ATTRIBUTE = "implement-interface";

	protected static final String METHOD_ATTRIBUTE = "method";

	protected static final String POINTCUT_ELEMENT = "pointcut";

	protected static final String POINTCUT_REF_ATTRIBUTE = "pointcut-ref";

	protected static final String PROXY_TARGET_CLASS_ATTRIBUTE = "proxy-target-class";

	protected static final String RETURNING_ATTRIBUTE = "returning";

	protected static final String THROWING_ATTRIBUTE = "throwing";

	protected static final String TYPES_MATCHING_ATTRIBUTE = "types-matching";

	protected static final String ASPECTJ_AUTOPROXY_ELEMENT = "aspectj-autoproxy";

	protected static final String BEAN_ELEMENT = "bean";

	protected static final String BEANS_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	protected static final String INCLUDE_ELEMENT = "include";

	protected static final String NAME_ATTRIBUTE = "name";

	public final List<IAspectDefinition> buildAspectDefinitions(IFile file,
			IWeavingClassLoaderSupport classLoaderSupport) {
		final List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();
		
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getModelForRead(
					file);
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				doBuildAspectDefinitions(document, file, aspectInfos, classLoaderSupport);
			}
		}
		catch (IOException e) {
			Activator.log(e);
		}
		catch (CoreException e) {
			Activator.log(e);
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
		}
		return aspectInfos;
	}
	
	protected String getAttribute(Node node, String attributeName) {
		if (hasAttribute(node, attributeName)) {
			String value = node.getAttributes().getNamedItem(attributeName)
					.getNodeValue();
			value = StringUtils.replace(value, "\n", " ");
			value = StringUtils.replace(value, "\t", " ");
			return StringUtils.replace(value, "\r", " ");
		}
		return null;
	}

	protected boolean hasAttribute(Node node, String attributeName) {
		return (node != null && node.hasAttributes() && node.getAttributes()
				.getNamedItem(attributeName) != null);
	}
	
	protected abstract void doBuildAspectDefinitions(IDOMDocument document,
			IFile file, List<IAspectDefinition> aspectInfos, IWeavingClassLoaderSupport classLoaderSupport);
}
