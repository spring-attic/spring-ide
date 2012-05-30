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
import org.springframework.ide.eclipse.quickfix.proposals.AddConstructorParamQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class AddConstructorParamQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IType getTypeAndApplyProposal(String beanName, int addition, int original, String className)
			throws JavaModelException {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());
		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		IJavaProject javaProject = JavaCore.create(project);

		IType type = javaProject.findType(className);

		int total = original + addition;
		assertFalse("Constructor with " + total + " params should not exist before applying proposal.", Introspector
				.hasConstructor(type, total, false));

		IMethod constructor = null;

		IMethod[] methods = type.getMethods();
		for (IMethod method : methods) {
			if (method.isConstructor() && method.getNumberOfParameters() == original) {
				constructor = method;
				break;
			}
		}
		AddConstructorParamQuickFixProposal proposal = new AddConstructorParamQuickFixProposal(offset, length, false,
				addition, constructor, "", javaProject);
		proposal.apply(document);

		return type;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/add-constructor-param-proposal.xml");
	}

	public void testAdd1ConstructorParamTo0() throws JavaModelException {
		IType type = getTypeAndApplyProposal("addConstructorParamTest1", 1, 0, "com.test.Account");
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
	}

	public void testAdd1ConstructorParamTo1() throws JavaModelException {
		IType type = getTypeAndApplyProposal("addConstructorParamTest3", 1, 1, "com.test.AccountContribution");
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
	}

	public void testAdd2ConstructorParamsTo0() throws JavaModelException {
		IType type = getTypeAndApplyProposal("addConstructorParamTest2", 2, 0, "com.test.Account");
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
	}

	public void testAdd2ConstructorParamsTo1() throws JavaModelException {
		IType type = getTypeAndApplyProposal("addConstructorParamTest4", 2, 1, "com.test.AccountContribution");
		assertTrue("Expects constructor with 3 params to be created", Introspector.hasConstructor(type, 3, false));
	}

}
