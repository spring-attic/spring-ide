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
import org.springframework.ide.eclipse.quickfix.proposals.AddConstructorArgQuickFixProposal;
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
public class AddConstructorArgQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IDOMNode getBeanAndApplyProposal(String beanName, int addition, int original) {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());
		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		AddConstructorArgQuickFixProposal proposal = new AddConstructorArgQuickFixProposal(offset, length, false,
				addition, beanNode, "");
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

		openBeanEditor("src/add-constructor-arg-proposal.xml");
	}

	public void testAdd1ConstructorArgTo0() {
		IDOMNode beanNode = getBeanAndApplyProposal("addConstructorArgTest1", 1, 0);
		assertEquals("Expects 1 <constructor-arg", 1, getNumConstructorArgs(beanNode));
	}

	public void testAdd1ConstructorArgTo1() {
		IDOMNode beanNode = getBeanAndApplyProposal("addConstructorArgTest2", 1, 1);
		assertEquals("Expects 2 <constructor-arg", 2, getNumConstructorArgs(beanNode));
	}

	public void testAdd2ConstructorArgTo0() {
		IDOMNode beanNode = getBeanAndApplyProposal("addConstructorArgTest3", 2, 0);
		assertEquals("Expects 2 <constructor-arg", 2, getNumConstructorArgs(beanNode));
	}

	public void testAdd2ConstructorArgTo1() {
		IDOMNode beanNode = getBeanAndApplyProposal("addConstructorArgTest4", 2, 1);
		assertEquals("Expects 3 <constructor-arg", 3, getNumConstructorArgs(beanNode));
	}

}
