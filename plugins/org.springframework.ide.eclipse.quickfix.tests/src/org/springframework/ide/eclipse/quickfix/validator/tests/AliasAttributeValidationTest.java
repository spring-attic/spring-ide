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
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.springframework.ide.eclipse.quickfix.validator.BeanAliasValidator;
import org.w3c.dom.NodeList;

/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class AliasAttributeValidationTest extends AbstractBeanValidationTestCase {

	private BeanAliasValidator aliasAttrValidator;

	private boolean hasError(String beanName) {
		NodeList children = beansNode.getChildNodes();
		IDOMNode node = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_ALIAS, beanName, children);

		AttrImpl aliasAttr = (AttrImpl) node.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_ALIAS);

		String alias = aliasAttr.getNodeValue();

		IBeansConfig config = BeansCorePlugin.getModel().getConfig(BeansConfigId.create(file));
		Set<IResourceModelElement> contextElements = getContextElements(config);

		for (IResourceModelElement contextElement : contextElements) {
			if (aliasAttrValidator.validateAttributeWithConfig(config, contextElement, file, aliasAttr, node, reporter,
					true, validator, alias)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		createBeansEditorValidator("src/alias-attribute.xml");
		aliasAttrValidator = new BeanAliasValidator();
	}

	@SuppressWarnings("unchecked")
	public void testHasOverrideError() {
		assertTrue("Expects error", hasError("account2"));
		List<IMessage> messages = reporter.getMessages();
		String expectedMessage = "Overrides another bean in the same config file";
		List<String> visibleMessages = getVisibleMessages(messages);
		assertEquals("Expects 1 message", 1, visibleMessages.size());
		assertEquals(expectedMessage, visibleMessages.get(0));
	}

	@SuppressWarnings("unchecked")
	public void testNoError() {
		assertFalse("Does not expect error", hasError("account3"));
		assertEquals("Expects no messages", 0, getVisibleMessages(reporter.getMessages()).size());
	}

}
