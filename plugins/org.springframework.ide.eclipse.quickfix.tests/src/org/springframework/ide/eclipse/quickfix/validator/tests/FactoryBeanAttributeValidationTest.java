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
import org.springframework.ide.eclipse.quickfix.processors.BeanReferenceQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.MissingFactoryMethodAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.FactoryBeanValidator;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class FactoryBeanAttributeValidationTest extends AbstractBeanValidationTestCase {

	private FactoryBeanValidator factoryBeanAttrValidator;

	private boolean hasError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);
		AttrImpl attr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_FACTORY_BEAN);

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (factoryBeanAttrValidator.validateAttributeWithConfig(config, contextElement, attr, beanNode, reporter,
					true, validator)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/factory-bean-test.xml");
		factoryBeanAttrValidator = new FactoryBeanValidator();
	}

	@SuppressWarnings("unchecked")
	public void testInvalidBean() {
		assertTrue("Expects error", hasError("invalidBean"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Referenced factory bean 'foo' is invalid (abstract or no bean class)";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
	}

	@SuppressWarnings("unchecked")
	public void testNoError() {
		assertFalse("Expects no error", hasError("correctBean"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testNoFactoryMethod() {
		assertTrue("Expects error", hasError("noFactoryMethod"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "A factory bean requires a factory method";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects MissingFactoryMethodAttributeQuickAssistProcessor to be in reporter",
				getProcessor(messages, MissingFactoryMethodAttributeQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testUnknownFactoryBean() {
		assertTrue("Expects error", hasError("unknownFactoryBean"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Factory bean 'no_such_bean' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects BeanReferenceQuickAssistProcessor to be in reporter",
				getProcessor(messages, BeanReferenceQuickAssistProcessor.class));
	}

}
