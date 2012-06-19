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
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewBeanQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class CreateNewBeanQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private void applyProposal(AttrImpl attr, IDOMNode beanNode, String beanName) {
		ITextRegion valueRegion = attr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		CreateNewBeanQuickFixProposal proposal = new CreateNewBeanQuickFixProposal(offset, length, false, beanName,
				beanNode);
		proposal.apply(document);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/create-bean-proposal.xml");
	}

	public void testCreateNewBean() {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, "createBeanTest2", beansNode
				.getChildNodes());
		AttrImpl factoryBeanAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(
				BeansSchemaConstants.ATTR_FACTORY_BEAN);

		applyProposal(factoryBeanAttr, beanNode, "account");
		assertNotNull("Expects new bean account to be created", QuickfixTestUtil.getNode(
				BeansSchemaConstants.ELEM_BEAN, "account", beansNode.getChildNodes()));
	}

	public void testCreateNewBeanInChild() {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, "createBeanTest1", beansNode
				.getChildNodes());
		IDOMNode constructorArgNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG, beanNode
				.getChildNodes());
		AttrImpl refAttr = (AttrImpl) constructorArgNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);

		applyProposal(refAttr, beanNode, "accountManager");
		assertNotNull("Expects new bean account to be created", QuickfixTestUtil.getNode(
				BeansSchemaConstants.ELEM_BEAN, "accountManager", beansNode.getChildNodes()));
	}

	public void testCreateNewBeanInNestedBean() {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, "createBeanTest3", beansNode
				.getChildNodes());
		IDOMNode constructorArgNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG, beanNode
				.getChildNodes());
		IDOMNode nestedBeanNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_BEAN, constructorArgNode
				.getChildNodes());
		IDOMNode propertyNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_PROPERTY, nestedBeanNode
				.getChildNodes());
		AttrImpl refAttr = (AttrImpl) propertyNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);

		applyProposal(refAttr, nestedBeanNode, "balance");
		assertNotNull("Expects new bean account to be created", QuickfixTestUtil.getNode(
				BeansSchemaConstants.ELEM_BEAN, "balance", beansNode.getChildNodes()));
	}

	public void testCreateNewBeanInNonBean() {
		IDOMNode node = QuickfixTestUtil.getNode("task:scheduled-tasks", 0, beansNode.getChildNodes());
		IDOMNode taskNode = QuickfixTestUtil.getFirstNode("task:scheduled", node.getChildNodes());
		AttrImpl refAttr = (AttrImpl) taskNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_REF);
		applyProposal(refAttr, node, "target2");
		assertNotNull("Expects new bean target2 to be created", QuickfixTestUtil.getNode(
				BeansSchemaConstants.ELEM_BEAN, "target2", beansNode.getChildNodes()));
	}

}
