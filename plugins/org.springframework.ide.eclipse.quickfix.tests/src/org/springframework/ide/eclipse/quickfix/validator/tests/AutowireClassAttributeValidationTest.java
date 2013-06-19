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
public class AutowireClassAttributeValidationTest extends AbstractBeanValidationTestCase {

	private ClassAttributeValidator classAttrValidator;

	private boolean hasError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode node = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, children);

		AttrImpl classAttr = (AttrImpl) node.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);

		String className = classAttr.getNodeValue();

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId(file));
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

		createBeansEditorValidator("src/autowire.xml");
		classAttrValidator = new ClassAttributeValidator();
	}

	@SuppressWarnings("unchecked")
	public void testAutowireWithNoError() {
		assertFalse("Does not expect error", hasError("autowireTest4"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testAutowireWithTooFewConstructorArg() {
		assertFalse("Does not expect error", hasError("autowireTest3"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testNoAutowireWithNoError() {
		assertFalse("Does not expect error", hasError("autowireTest2"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testNoAutowireWithTooFewConstructorArg() {
		assertTrue("Expects error with too few constructor-arg", hasError("autowireTest1"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "No constructor with 0 arguments defined in class 'com.test.AccountContribution'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter",
				getProcessor(messages, ConstructorArgQuickAssistProcessor.class));
	}

}
