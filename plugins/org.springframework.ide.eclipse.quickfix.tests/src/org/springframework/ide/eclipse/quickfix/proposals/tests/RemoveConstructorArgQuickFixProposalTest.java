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
package org.springframework.ide.eclipse.quickfix.proposals.tests;

import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveConstructorArgQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Test case for AddConstructorArgQuickFixProposal
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class RemoveConstructorArgQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IDOMNode getBeanAndApplyProposal(String beanName, int removal, int original) {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());
		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		RemoveConstructorArgQuickFixProposal proposal = new RemoveConstructorArgQuickFixProposal(offset, length, false,
				removal, beanNode, "");
		proposal.apply(document);

		return beanNode;
	}

	private int getNumConstructorArgs(IDOMNode beanNode) {
		NodeList children = beanNode.getChildNodes();
		int count = 0;
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			String nodeName = child.getNodeName();
			if (nodeName != null && nodeName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
				count++;
			}
		}

		return count;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/remove-constructor-arg-proposal.xml");
	}

	public void testRemove1ConstructorArgFrom1() {
		IDOMNode beanNode = getBeanAndApplyProposal("removeConstructorArgTest1", 1, 1);
		assertEquals("Expects 0 <constructor-arg>", 0, getNumConstructorArgs(beanNode));
	}

	public void testRemove1ConstructorArgFrom2() {
		IDOMNode beanNode = getBeanAndApplyProposal("removeConstructorArgTest2", 1, 2);
		assertEquals("Expects 1 <constructor-arg>", 1, getNumConstructorArgs(beanNode));
	}

	public void testRemove2ConstructorArgsFrom2() {
		IDOMNode beanNode = getBeanAndApplyProposal("removeConstructorArgTest3", 2, 2);
		assertEquals("Expects 0 <constructor-arg>", 0, getNumConstructorArgs(beanNode));
	}

	public void testRemove2ConstructorArgsFrom3() {
		IDOMNode beanNode = getBeanAndApplyProposal("removeConstructorArgTest4", 2, 3);
		assertEquals("Expects 1 <constructor-arg>", 1, getNumConstructorArgs(beanNode));
	}

}
