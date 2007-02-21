/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class WebflowContentAssistProcessor extends
		AbstractContentAssistProcessor {

	private void addBeanReferenceProposals(ContentAssistRequest request,
			String prefix) {
		if (prefix == null) {
			prefix = "";
		}

		IFile file = (IFile) BeansEditorUtils.getResource(request);
		BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(
				request);
		List<?> beans = BeansEditorUtils.getBeansFromConfigSets(file);
		for (int i = 0; i < beans.size(); i++) {
			IBean bean = (IBean) beans.get(i);
			requestor.acceptSearchMatch(bean, file, prefix);
		}
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request,
			String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	private void addActionMethodAttributeValueProposals(
			ContentAssistRequest request, String prefix, IType type) {
		try {
			IMethod[] methods = type.getMethods();
			if (methods != null) {
				ActionMethodSearchRequestor requestor = new ActionMethodSearchRequestor(
						request);
				for (IMethod method : methods) {
					requestor.acceptSearchMatch(method);
				}
			}
		}
		catch (JavaModelException e) {
		}
		catch (CoreException e) {
		}
	}

	private void addExceptionTypesAttributeValueProposals(
			ContentAssistRequest request, final String prefix) {
		BeansJavaCompletionUtils.addTypeHierachyAttributeValueProposals(
				request, prefix, Throwable.class.getName());
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request,
			IDOMNode node, String matchString, String attributeName) {
		String nodeName = node.getLocalName();
		if ("action".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// method
			else if ("method".equals(attributeName)
					&& BeansEditorUtils.hasAttribute(node, "bean")) {
				String className = BeansEditorUtils.getClassNameForBean(
						BeansEditorUtils.getResource(request), node
								.getOwnerDocument(), BeansEditorUtils
								.getAttribute(node, "bean"));
				IType type = BeansModelUtils.getJavaType(BeansEditorUtils
						.getResource(request).getProject(), className);
				if (type != null) {
					addActionMethodAttributeValueProposals(request,
							matchString, type);
				}
			}
		}
		else if ("bean-action".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// method
		}
		else if ("transition".equals(nodeName)) {
			// to
			if ("to".equals(attributeName)) {
				
			}
			// on-exception
			else if ("on-exception".equals(attributeName)) {
				addExceptionTypesAttributeValueProposals(request, matchString);
			}

		}
		else if ("argument".equals(nodeName)) {
			// parameter-type
			if ("parameter-type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("mapping".equals(nodeName)) {
			// to

			// from
		}
		else if ("start-state".equals(nodeName)) {
			// idref
		}
		else if ("var".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
			// class
			else if ("class".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
		else if ("if".equals(nodeName)) {
			// then

			// else
		}
		else if ("exception-handler".equals(nodeName)) {
			// bean
			if ("bean".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString);
			}
		}
		else if ("attribute".equals(nodeName)) {
			// type
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request,
			IDOMNode node) {
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request,
			String prefix, String namespace, String namespacePrefix,
			Node attributeNode) {
	}
}
