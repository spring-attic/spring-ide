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

import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.processors.NameSpaceElementsQuickAssistProcessor;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.NamespaceElementsValidator;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class NamespaceElementsValidationTest extends AbstractBeanValidationTestCase {

	private NamespaceElementsValidator namespaceValidator;

	private boolean hasError(IDOMNode node, String attrName, boolean checkChildren) {
		if (node.getAttributes() == null) {
			return false;
		}

		AttrImpl attr = (AttrImpl) node.getAttributes().getNamedItem(attrName);
		if (attr == null && checkChildren) {
			boolean foundError = false;
			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				foundError |= hasError((IDOMNode) childNodes.item(i), attrName, checkChildren);
			}
			return foundError;
		}

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
		Set<IResourceModelElement> contextElements = getContextElements(config);
		for (IResourceModelElement contextElement : contextElements) {
			if (namespaceValidator.validateAttributeWithConfig(config, contextElement, attr, node, reporter, true,
					validator)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasError(String nodeName, int pos, String attrName, boolean checkChildren) {
		NodeList children = beansNode.getChildNodes();
		int currentPos = 0;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String currentNodeName = child.getNodeName();
			if (currentNodeName == null) {
				continue;
			}

			if (currentNodeName.equals(nodeName)) {
				if (currentPos == pos) {
					return hasError((IDOMNode) child, attrName, checkChildren);
				}

				currentPos++;
			}
		}

		return false;
	}

	private boolean hasError(String nodeName, String attrName, String attrValue, String attrToCheck) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode node = QuickfixTestUtil.getNode(nodeName, attrName, attrValue, children);

		assertNotNull(node);
		return hasError(node, attrToCheck, false);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/namespace-elements.xml");
		namespaceValidator = new NamespaceElementsValidator();
	}

	@SuppressWarnings("unchecked")
	public void testConstantWithNoClassFoundError() {
		assertTrue("Expects error", hasError("util:constant", "static-field", "com.test.Bar", "static-field"));
		List messages = reporter.getMessages();
		String expectedMessage = "Class 'com.test' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects NameSpaceElementsQuickAssistProcessor to be in reporter", getProcessor(messages,
				NameSpaceElementsQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testConstantWithNoFieldFoundError() {
		assertTrue("Expects error", hasError("util:constant", "static-field", "com.test.Foo.NO_SUCH_FIELD",
				"static-field"));
		List messages = reporter.getMessages();
		String expectedMessage = "Field 'NO_SUCH_FIELD' not found on class 'com.test.Foo'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects a NameSpaceElementsQuickAssistProcessor to be in reporter", getProcessor(messages,
				NameSpaceElementsQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testContantWithNoError() {
		assertFalse("Does not expect error", hasError("util:constant", "static-field", "com.test.Foo.ABCD",
				"static-field"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testListWithClassNotFound() {
		assertTrue("Expects error no class found error", hasError("util:list", "id", "test2", "list-class"));
		List messages = reporter.getMessages();
		String expectedMessage = "Class 'java.lang.NoSuchClass' not found";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
		assertNotNull("Expects NameSpaceElementsQuickAssistProcessor to be in reporter", getProcessor(messages,
				NameSpaceElementsQuickAssistProcessor.class));
	}

	@SuppressWarnings("unchecked")
	public void testListWithNoError() {
		assertFalse("Does not expect error", hasError("util:list", "id", "test3", "list-class"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

	@SuppressWarnings("unchecked")
	public void testListWithNonListClass() {
		assertTrue("Expects error class not list", hasError("util:list", "id", "test1", "list-class"));
		List messages = reporter.getMessages();
		String expectedMessage = "'java.lang.String' is not a sub type of 'java.util.List'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
	}

	@SuppressWarnings("unchecked")
	public void testTaskWithMethodNotFound() {
		assertTrue("Expects error no method found", hasError("task:scheduled-tasks", 1, "method", true));
		List messages = reporter.getMessages();
		String expectedMessage = "Method 'nosuchmethod' not found in class 'java.lang.String'";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
		assertNotNull("Expects an error message", getErrorMessage(messages));
	}

	@SuppressWarnings("unchecked")
	public void testTaskWithNoError() {
		assertFalse("Does not expect error", hasError("task:scheduled-tasks", 0, "method", true));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

}
