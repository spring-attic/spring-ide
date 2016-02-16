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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.processors.ClassAttributeQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.ConstructorArgQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.processors.RenamePropertyQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.BeanValidatorVisitor;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class PlaceholderTest extends AbstractBeanValidationTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/placeholder.xml");
	}

	public void testClass() {
		validate("placeholderTest1");
		assertTrue("Expects no messages", reporter.getMessages().isEmpty());
	}

	@SuppressWarnings({ "unchecked" })
	public void testNestedClass() {
		validate("placeholderTest3");
		assertNull("Expects no messages", getErrorMessage(reporter.getMessages()));

		validate("placeholderTest4");
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "No constructor with 0 arguments defined in class 'com.test.AccountContribution'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));

		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter",
				getProcessor(messages, ConstructorArgQuickAssistProcessor.class));
		// assertNotNull("Expects a RenamePropertyQuickAssistProcessor to be in reporter",
		// getProcessor(messages,
		// RenamePropertyQuickAssistProcessor.class));

	}

	@SuppressWarnings({ "unchecked" })
	public void testNoPlaceholder() {
		validate("placeholderTest5");
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Class 'batch.database.incrementer.class' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects ClassAttributeQuickAssistProcessor to be in reporter",
				getProcessor(messages, ClassAttributeQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testProperty() {
		validate("placeholderTest2");
		List<IMessage> messages = reporter.getMessages();
		assertEquals("Expects no messages", 0, getVisibleMessages(messages).size());
		assertNotNull("Expects RenamePropertyQuickAssistProcessor to be in reporter",
				getProcessor(messages, RenamePropertyQuickAssistProcessor.class));
	}

	private void validate(String beanName) {
		IDOMNode node = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode.getChildNodes());
		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			BeanValidatorVisitor visitor = new BeanValidatorVisitor(config, contextElement, reporter, validator);
			visitor.visitNode(node, true, true);
		}
	}

}
