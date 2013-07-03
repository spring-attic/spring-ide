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
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanReferenceRule;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanConstructorArgumentHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeanPropertyHelper;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

/**
 * Abstract class for validating bean reference attribute
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class BeanReferenceValidator extends BeanValidator {

	static class BeanReferenceValidationContextHelper extends BeansValidationContextHelper {

		private final IFile file;

		private final Document document;

		public BeanReferenceValidationContextHelper(AttrImpl attribute, IDOMNode node,
				IResourceModelElement rootElement, IProject project, IReporter reporter,
				BeansEditorValidator validator, QuickfixProcessorFactory quickfixFactory, boolean affectsWholeBean,
				boolean reportError, IBeansConfig config) {
			super(attribute, node, rootElement, project, reporter, validator, quickfixFactory, affectsWholeBean,
					reportError, config);
			this.file = (IFile) config.getElementResource();
			this.document = node.getOwnerDocument();
		}

		@Override
		public void error(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			if (!foundInXml(attributes)) {
				super.error(element, problemId, message, attributes);
			}
		}

		private boolean foundInXml(ValidationProblemAttribute... attributes) {
			if (attributes != null) {
				for (ValidationProblemAttribute attribute : attributes) {
					if ("BEAN".equals(attribute.getKey())) {
						String beanName = (String) attribute.getValue();
						return BeansEditorUtils.getFirstReferenceableNodeById(document, beanName, file) != null;
					}
				}
			}
			return false;
		}

		@Override
		public void info(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			if (!foundInXml(attributes)) {
				super.info(element, problemId, message, attributes);
			}
		}

		@Override
		public void warning(IResourceModelElement element, String problemId, String message,
				ValidationProblemAttribute... attributes) {
			if (!foundInXml(attributes)) {
				super.warning(element, problemId, message, attributes);
			}
		}
	}

	private IBeansModelElement getBeansModelElement(AttrImpl attribute, IDOMNode parent, IFile file, String text) {

		IBeansModelElement element = null;
		IDOMNode beanNode = getParentBeanNode(parent);
		if (beanNode == null) {
			return null;
		}

		BeanHelper parentBean = new BeanHelper(beanNode, file, file.getProject());

		if (attribute.getLocalName().equals(BeansSchemaConstants.ATTR_DEPENDS_ON)) {
			parentBean.getBeanDefinition().setDependsOn(
					StringUtils.tokenizeToStringArray(text,
							BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS));
			element = parentBean;
		}
		else if (attribute.getLocalName().equals(BeansSchemaConstants.ATTR_PARENT)) {
			element = parentBean;
		}
		else if (attribute.getLocalName().equals(BeansSchemaConstants.ATTR_FACTORY_BEAN)) {
			parentBean.getBeanDefinition().setFactoryBeanName(text);
			element = parentBean;
		}
		else if (parent.getLocalName().equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)
				&& attribute.getLocalName().equals(BeansSchemaConstants.ATTR_REF)) {
			BeanConstructorArgumentHelper ctorHelper = new BeanConstructorArgumentHelper(0, parent, file, parentBean);
			ctorHelper.setValue(new RuntimeBeanReference(text));
			element = ctorHelper;
		}
		else if (parent.getLocalName().equals(BeansSchemaConstants.ELEM_PROPERTY)
				&& attribute.getLocalName().equals(BeansSchemaConstants.ATTR_REF)) {
			BeanPropertyHelper propertyHelper = new BeanPropertyHelper(parent, file, parentBean);
			propertyHelper.setValue(new RuntimeBeanReference(text));
			element = propertyHelper;
		}

		return element;
	}

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contexlElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {

		if (!StringUtils.hasLength(text)) {
			return false;
		}

		IProject project = file.getProject();

		ValidationRuleDefinition ruleDefinition = getValidationRule(project, BeanReferenceRule.class);
		BeanReferenceRule refRule = (BeanReferenceRule) (ruleDefinition != null ? ruleDefinition.getRule() : null);

		if (refRule != null) {

			IBeansModelElement modelElement = getBeansModelElement(attribute, parent, file, text);
			if (modelElement == null) {
				return false;
			}

			BeanReferenceValidationContextHelper context = new BeanReferenceValidationContextHelper(attribute, parent,
					contexlElement, project, reporter, validator, QuickfixProcessorFactory.REF, false, reportError,
					config);
			context.setCurrentRuleDefinition(ruleDefinition);
			refRule.validate(modelElement, context, null);

			if (context.getErrorFound()) {
				return true;
			}
		}
		return false;
	}

}
