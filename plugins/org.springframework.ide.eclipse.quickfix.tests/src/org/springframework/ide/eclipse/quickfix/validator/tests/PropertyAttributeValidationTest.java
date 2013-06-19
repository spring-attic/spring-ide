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
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.processors.MethodDeprecatedQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.PropertyAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.RenamePropertyQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.PropertyValidator;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class PropertyAttributeValidationTest extends AbstractBeanValidationTestCase {

	private PropertyValidator propertyAttrValidator;

	private boolean hasError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);
		NodeList beanChildren = beanNode.getChildNodes();
		IDOMNode propertyNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_PROPERTY, beanChildren);
		AttrImpl attr = (AttrImpl) propertyNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_NAME);

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (propertyAttrValidator.validateAttributeWithConfig(config, contextElement, attr, propertyNode, reporter,
					true, validator)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/property-attribute.xml");
		propertyAttrValidator = new PropertyValidator();
	}

	@SuppressWarnings("unchecked")
	public void testDeprecatedSetter() {
		assertTrue("Expects error deprecated setter error", hasError("deprecatedPropertyTest"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Method 'setZoo' is marked deprecated";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects a warning message", getWarningMessage(messages));
		assertNotNull("Expects MethodDeprecatedQuickAssistProcessor to be in reporter", getProcessor(messages,
				MethodDeprecatedQuickAssistProcessor.class));
		assertNotNull("Expects a RenamePropertyQuickAssistProcessor to be in reporter", getProcessor(messages,
				RenamePropertyQuickAssistProcessor.class));

	}

	@SuppressWarnings("unchecked")
	public void testNestedPropertyFound() {
		assertFalse("Does not expect error", hasError("propertyTest3"));
		List<IMessage> messages = reporter.getMessages();
		assertEquals("Expects no messages", 0, getVisibleMessages(messages).size());
		assertNotNull("Expects RenamePropertyQuickAssistProcessor to be in reporter", getProcessor(messages,
				RenamePropertyQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testNestedPropertyNotFound() {
		assertTrue("Expects error no property found error", hasError("propertyTest4"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "No getter found for nested property 'bars' in class 'com.test.Foo'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects PropertyAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				PropertyAttributeQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testPropertyFound() {
		assertFalse("Does not expect error", hasError("propertyTest1"));
		List<IMessage> messages = reporter.getMessages();
		assertEquals("Expects no messages", 0, getVisibleMessages(messages).size());
		assertNotNull("Expects RenamePropertyQuickAssistProcessor to be in reporter", getProcessor(messages,
				RenamePropertyQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testPropertyNotFound() {
		assertTrue("Expects error no property found error", hasError("propertyTest2"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "No setter found for property 'balances' in class 'com.test.Account'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects PropertyAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				PropertyAttributeQuickAssistProcessor.class));
	}

}
