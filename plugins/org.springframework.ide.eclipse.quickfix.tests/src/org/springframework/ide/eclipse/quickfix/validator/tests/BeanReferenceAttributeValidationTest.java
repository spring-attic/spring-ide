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
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.BeanReferenceValidator;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeanReferenceAttributeValidationTest extends AbstractBeanValidationTestCase {

	private BeanReferenceValidator beanRefAttrValidator;

	private boolean hasBeanRefError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);
		AttrImpl attr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);
		if (attr == null) {
			attr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_PARENT);
		}

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (beanRefAttrValidator.validateAttributeWithConfig(config, contextElement, attr, beanNode, reporter,
					true, validator)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasConstructorArgError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);

		NodeList beanChildren = beanNode.getChildNodes();
		IDOMNode constructorArgNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG,
				beanChildren);

		AttrImpl attr = (AttrImpl) constructorArgNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (beanRefAttrValidator.validateAttributeWithConfig(config, contextElement, attr, constructorArgNode,
					reporter, true, validator)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasPropertyError(String beanName, String propertyName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);

		NodeList beanChildren = beanNode.getChildNodes();
		IDOMNode propertyNode = QuickfixTestUtil
				.getNode(BeansSchemaConstants.ELEM_PROPERTY, propertyName, beanChildren);

		AttrImpl attr = (AttrImpl) propertyNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (beanRefAttrValidator.validateAttributeWithConfig(config, contextElement, attr, propertyNode, reporter,
					true, validator)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/bean-ref-attribute.xml");
		beanRefAttrValidator = new BeanReferenceValidator();
	}

	@SuppressWarnings("unchecked")
	public void testConstructorArgRefWithError() {
		assertTrue("Expects ref error in constructor-arg", hasConstructorArgError("constructorArgBeanRefTest2"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Referenced bean 'accoun' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects a warning message", getWarningMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				BeanReferenceQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testConstructorArgRefWithNoError() {
		assertFalse("Does not expect error", hasConstructorArgError("constructorArgBeanRefTest1"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	// factory bean needs factory method
	// @SuppressWarnings("unchecked")
	// public void testFactoryBeanRefWithError() {
	// assertTrue("Expects factory-bean error",
	// hasBeanRefError("factoryBeanRefTest2"));
	// List<IMessage> messages = reporter.getMessages();
	// assertEquals("Expects a pair of messages in reporter", 2,
	// messages.size());
	// assertNotNull("Expects a warning message", getWarningMessage(messages));
	// assertTrue("Expects ClassAttributeQuickAssistProcessor to be in reporter",
	// getProcessor(messages) instanceof BeanReferenceQuickAssistProcessor);
	// }

	// factory bean needs factory method
	// public void testFactoryBeanRefWithNoError() {
	// assertFalse("Does not expect error",
	// hasBeanRefError("factoryBeanRefTest1"));
	// assertTrue("Expects no messages",
	// checkMessageTexts(reporter.getMessages()));
	// }

	/**
	 * parent bean ref is currently not checked since IDE rules do not validate
	 * @SuppressWarnings("unchecked") public void testParentBeanRefWithError() {
	 * assertTrue("Expects error in parent",
	 * hasBeanRefError("parentBeanRefTest2")); List<IMessage> messages =
	 * reporter.getMessages();
	 * assertEquals("Expects a pair of messages in reporter", 2,
	 * messages.size()); assertNotNull("Expects a warning message",
	 * getWarningMessage(messages)); assertTrue(
	 * "Expects ClassAttributeQuickAssistProcessor to be in reporter",
	 * getProcessor(messages) instanceof BeanReferenceQuickAssistProcessor); }
	 */

	@SuppressWarnings("unchecked")
	public void testParentBeanRefWithNoError() {
		assertFalse("Does not expect error", hasBeanRefError("parentBeanRefTest1"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testPropertyRefWithError() {
		assertTrue("Expects ref error in property", hasPropertyError("propertyBeanRefTest2", "account"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Referenced bean 'accoun' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects a warning message", getWarningMessage(messages));

		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter", getProcessor(messages,
				BeanReferenceQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testPropertyRefWithNoError() {
		assertFalse("Does not expect error", hasPropertyError("propertyBeanRefTest1", "account"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

}
