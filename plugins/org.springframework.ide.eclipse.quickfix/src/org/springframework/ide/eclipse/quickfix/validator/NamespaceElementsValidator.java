/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.validator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.validation.internal.provisional.core.IReporter;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.NamespaceElementsRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.validator.helper.XmlValidationContextHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class NamespaceElementsValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		IProject project = file.getProject();

		ValidationRuleDefinition ruleDefinition = getValidationRule(project, NamespaceElementsRule.class);
		NamespaceElementsRule rule = (NamespaceElementsRule) (ruleDefinition != null ? ruleDefinition.getRule() : null);

		if (rule != null) {

			boolean errorFound = false;
			BeanReferenceXmlValidationContextHelper context = new BeanReferenceXmlValidationContextHelper(validator,
					attribute, parent, file, config, contextElement, reporter, reportError);
			String attributeName = attribute.getName();
			context.setCurrentRuleDefinition(ruleDefinition);
			rule.validate(parent, attributeName, context);

			errorFound |= context.getErrorFound();

			if (errorFound) {
				return true;
			}
		}
		return false;
	}

	static class BeanReferenceXmlValidationContextHelper extends XmlValidationContextHelper {

		private final IFile file;

		private final Document document;

		public BeanReferenceXmlValidationContextHelper(BeansEditorValidator validator, AttrImpl attribute,
				IDOMNode node, IFile file, IBeansConfig config, IResourceModelElement contextElement,
				IReporter reporter, boolean reportError) {
			super(validator, attribute, node, file, config, contextElement, reporter, reportError);
			this.file = (IFile) config.getElementResource();
			this.document = node.getOwnerDocument();
		}

		@Override
		public void error(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
			if (!foundInXml(problemId, attributes)) {
				super.error(node, problemId, message, attributes);
			}
		}

		@Override
		public void info(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
			if (!foundInXml(problemId, attributes)) {
				super.info(node, problemId, message, attributes);
			}
		}

		@Override
		public void warning(Node node, String problemId, String message, ValidationProblemAttribute... attributes) {
			if (!foundInXml(problemId, attributes)) {
				super.warning(node, problemId, message, attributes);
			}
		}

		private boolean foundInXml(String problemId, ValidationProblemAttribute... attributes) {
			if (attributes != null && "UNDEFINED_REFERENCED_BEAN".equals(problemId)) {
				for (ValidationProblemAttribute attribute : attributes) {
					if ("BEAN".equals(attribute.getKey())) {
						String beanName = (String) attribute.getValue();
						return BeansEditorUtils.getFirstReferenceableNodeById(document, beanName, file) != null;
					}
				}
			}
			return false;
		}
	}
}
