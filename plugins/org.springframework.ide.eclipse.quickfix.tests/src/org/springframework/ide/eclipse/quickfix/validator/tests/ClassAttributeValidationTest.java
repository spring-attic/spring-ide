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
import org.springframework.ide.eclipse.quickfix.processors.ClassAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.ClassDeprecatedQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.ConstructorArgQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.ClassAttributeValidator;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class ClassAttributeValidationTest extends AbstractBeanValidationTestCase {

	private ClassAttributeValidator classAttrValidator;

	private boolean hasError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode node = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);

		AttrImpl classAttr = (AttrImpl) node.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);

		String className = classAttr.getNodeValue();

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (classAttrValidator.validateAttributeWithConfig(config, contextElement, file, classAttr, node, reporter,
					true, validator, className)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/class-attribute.xml");
		classAttrValidator = new ClassAttributeValidator();
	}

	@SuppressWarnings("unchecked")
	public void testClassFound() {
		assertFalse("Does not expect error", hasError("classTest1"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testComment() {
		assertTrue("Expects error no class found error", hasError("classTest6"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'Foo' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				ClassAttributeQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testConstructorArgs() {
		assertTrue("Expects error with too many constructor-arg", hasError("classTest4"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "No constructor with 1 argument defined in class 'com.test.Account'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ConstructorArgQuickAssistProcessor to be in reporter", getProcessor(messages,
				ConstructorArgQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testDeprecatedClass() {
		assertTrue("Expects error in class attribute", hasError("deprecatedTest"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'com.test.DeprecatedAccount' is marked deprecated";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects a warning message", getWarningMessage(messages));
		assertNotNull("Expects ClassDeprecatedQuickAssistProcessor to be in reporter", getProcessor(messages,
				ClassDeprecatedQuickAssistProcessor.class));
	}

	public void testInnerClass() {
		assertFalse("Does not expect error", hasError("classTest5"));
	}

	@SuppressWarnings("unchecked")
	public void testInterface() {
		assertTrue("Expects error", hasError("interfaceTest"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'com.test.Bar' is an interface";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Exepcts a warning message", getWarningMessage(messages));
	}

	@SuppressWarnings("unchecked")
	public void testUnknownClass() {
		assertTrue("Expects error in class attribute", hasError("classTest3"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'com.test.Accoun' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				ClassAttributeQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testUnqualifiedClassName() {
		assertTrue("Expects error in class attribute", hasError("classTest2"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'Account' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				ClassAttributeQuickAssistProcessor.class));
	}

}
