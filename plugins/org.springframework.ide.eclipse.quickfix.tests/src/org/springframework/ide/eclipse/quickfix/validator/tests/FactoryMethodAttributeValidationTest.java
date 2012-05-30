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
package org.springframework.ide.eclipse.quickfix.validator.tests;

import java.util.List;
import java.util.Set;

import org.eclipse.wst.validation.internal.provisional.core.IMessage;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.processors.MethodAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.RenameMethodQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.BeanValidator;
import org.springframework.ide.eclipse.quickfix.validator.ClassAttributeValidator;
import org.springframework.ide.eclipse.quickfix.validator.FactoryMethodValidator;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class FactoryMethodAttributeValidationTest extends AbstractBeanValidationTestCase {

	private FactoryMethodValidator factoryMethodAttrValidator;

	private ClassAttributeValidator classAttrValidator;

	private boolean hasMethodError(String beanName, BeanValidator beanValidator) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);
		AttrImpl attr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_FACTORY_METHOD);

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (beanValidator.validateAttributeWithConfig(config, contextElement, attr, beanNode, reporter, true,
					validator)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/factory-method-test.xml");
		factoryMethodAttrValidator = new FactoryMethodValidator();
		classAttrValidator = new ClassAttributeValidator();
	}

	@SuppressWarnings("unchecked")
	public void testWithNoClass() {
		assertTrue("Expects error", hasMethodError("noClass", factoryMethodAttrValidator));
		List<IMessage> messages = reporter.getMessages();
		String message = "Factory method needs class from root or parent bean";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals("Expects " + message, message, visibleMessages.get(0));
		assertNotNull("Expects a warning message", getErrorMessage(messages));
	}

	@SuppressWarnings("unchecked")
	public void testWithNoError() {
		assertFalse("Does not expect error in factory-method", hasMethodError("test1", factoryMethodAttrValidator));
		assertFalse("Does not expect error in class", hasMethodError("test1", classAttrValidator));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
		assertNotNull("Expects RenameMethodQuickAssistProcessor to be in reporter", getProcessor(
				reporter.getMessages(), RenameMethodQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testWithUndefinedMethod() {
		assertTrue("Expects error in factory-method", hasMethodError("test2", factoryMethodAttrValidator));
		List<IMessage> messages = reporter.getMessages();
		String message = "Static factory method 'createFoos' with 1 arguments not found in factory bean class 'com.test.Foo'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(message, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects MethodAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				MethodAttributeQuickAssistProcessor.class));
		assertNotNull("Expects a RenameMethodQuickAssistProcessor to be in reporter", getProcessor(messages,
				RenameMethodQuickAssistProcessor.class));
	}

}
