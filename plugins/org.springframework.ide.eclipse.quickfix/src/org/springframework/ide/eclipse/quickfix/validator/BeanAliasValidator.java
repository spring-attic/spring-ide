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
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.BeanAlias;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanAliasRule;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.BeansEditorValidator;
import org.springframework.ide.eclipse.quickfix.processors.QuickfixProcessorFactory;
import org.springframework.ide.eclipse.quickfix.validator.helper.BeansValidationContextHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
public class BeanAliasValidator extends BeanValidator {

	@Override
	public boolean validateAttributeWithConfig(IBeansConfig config, IResourceModelElement contextElement, IFile file,
			AttrImpl attribute, IDOMNode parent, IReporter reporter, boolean reportError,
			BeansEditorValidator validator, String text) {
		String beanName = null, alias = null;
		NamedNodeMap attributes = parent.getAttributes();

		Node nameNode = attributes.getNamedItem(BeansSchemaConstants.ATTR_NAME);
		if (nameNode != null) {
			beanName = nameNode.getNodeValue();
		}
		Node aliasNode = attributes.getNamedItem(BeansSchemaConstants.ATTR_ALIAS);
		if (aliasNode != null) {
			alias = aliasNode.getNodeValue();
		}

		AliasDefinition definition = new AliasDefinition(beanName, alias);
		IBeanAlias beanAlias = null;

		if (alias != null) {
			beanAlias = config.getAlias(alias);
		}

		if (beanAlias == null) {
			beanAlias = new BeanAlias(config, definition);
		}

		IProject project = file.getProject();
		ValidationRuleDefinition ruleDefinition = getValidationRule(project, BeanAliasRule.class);
		BeanAliasRule aliasRule = (BeanAliasRule) (ruleDefinition != null ? ruleDefinition.getRule() : null);

		BeansValidationContextHelper context = new BeansValidationContextHelper(attribute, parent, contextElement,
				project, reporter, validator, QuickfixProcessorFactory.ALIAS, false, reportError, config);

		if (aliasRule != null) {
			aliasRule.validate(beanAlias, context, null);
		}

		return context.getErrorFound();
	}
}
