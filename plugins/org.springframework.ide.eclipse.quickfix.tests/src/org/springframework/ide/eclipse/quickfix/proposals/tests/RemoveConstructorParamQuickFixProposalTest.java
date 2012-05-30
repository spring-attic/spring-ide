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

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.quickfix.proposals.RemoveConstructorParamQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class RemoveConstructorParamQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IJavaProject javaProject;

	private void applyProposal(String beanName, int removal, int original, String className) throws JavaModelException {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());
		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		IType type = javaProject.findType(className);

		IMethod constructor = null;

		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (method.isConstructor() && method.getNumberOfParameters() == original) {
				constructor = method;
				break;
			}
		}
		RemoveConstructorParamQuickFixProposal proposal = new RemoveConstructorParamQuickFixProposal(offset, length,
				false, removal, constructor, "", javaProject);
		proposal.apply(document);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/remove-constructor-param-proposal.xml");

		javaProject = JavaCore.create(project);
	}

	public void testRemove1ConstructorParamFrom1() throws JavaModelException {
		String className = "com.test.AccountContribution";
		IType type = javaProject.findType(className);

		assertFalse("Constructor with 0 param should not exist before applying proposal.", Introspector.hasConstructor(
				type, 0, false));
		applyProposal("removeConstructorParamTest1", 1, 1, className);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
	}

	public void testRemove1ConstructorParamsFrom2() throws JavaModelException {
		String className = "com.test.Restaurant";
		IType type = javaProject.findType(className);

		assertFalse("Constructor with 1 param should not exist before applying proposal.", Introspector.hasConstructor(
				type, 1, false));
		applyProposal("removeConstructorParamTest2", 1, 2, className);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
	}

	public void testRemove2ConstructorParamFrom2() throws JavaModelException {
		String className = "com.test.Restaurant";
		IType type = javaProject.findType(className);

		assertFalse("Constructor with 0 param should not exist before applying proposal.", Introspector.hasConstructor(
				type, 0, false));
		applyProposal("removeConstructorParamTest3", 2, 2, className);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
	}

	public void testRemove2ConstructorParamsFrom2() throws JavaModelException {
		String className = "com.test.RestaurantReward";
		IType type = javaProject.findType(className);

		assertFalse("Constructor with 1 param should not exist before applying proposal.", Introspector.hasConstructor(
				type, 1, false));
		applyProposal("removeConstructorParamTest4", 2, 3, className);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
	}

}
