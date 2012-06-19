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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.quickfix.proposals.CreateConstructorQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class CreateConstructorQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private void applyProposal(String beanName, int constructorParamCount, String className) throws JavaModelException {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());

		List<String> constructorParamClassNames = new ArrayList<String>();
		for (int i = 0; i < constructorParamCount; i++) {
			constructorParamClassNames.add("Object");
		}

		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		IJavaProject javaProject = JavaCore.create(project);

		CreateConstructorQuickFixProposal proposal = new CreateConstructorQuickFixProposal(offset, length, className,
				false, javaProject, constructorParamClassNames);
		proposal.apply(document);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/create-constructor-proposal.xml");
	}

	public void testCreateConstructorWith1Param() throws JavaModelException {
		String className = "com.test.Account";
		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(className);
		assertFalse("Constructor with 2 params should not exist before applying proposal", Introspector.hasConstructor(
				type, 2, false));
		applyProposal("createConstructorTest2", 2, className);
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
	}

	public void testCreateConstructorWith2Params() throws JavaModelException {
		String className = "com.test.AccountContribution";
		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(className);
		assertFalse("Constructor with 0 param should not exist before applying proposal", Introspector.hasConstructor(
				type, 0, false));
		applyProposal("createConstructorTest3", 0, className);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
	}

	public void testCreateConstructorWithNoParam() throws JavaModelException {
		String className = "com.test.Account";
		IJavaProject javaProject = JavaCore.create(project);
		IType type = javaProject.findType(className);
		assertFalse("Constructor with 1 param should not exist before applying proposal", Introspector.hasConstructor(
				type, 1, false));
		applyProposal("createConstructorTest1", 1, className);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
	}
}
