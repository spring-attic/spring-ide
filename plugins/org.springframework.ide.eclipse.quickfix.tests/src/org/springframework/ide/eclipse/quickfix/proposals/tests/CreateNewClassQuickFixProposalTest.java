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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.quickfix.proposals.CreateNewClassQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class CreateNewClassQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IJavaProject javaProject;

	private void applyProposal(String beanName, String className, int constructorArgCount, Set<String> properties)
			throws Exception {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, beanName, beansNode
				.getChildNodes());
		AttrImpl classAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_CLASS);
		ITextRegion valueRegion = classAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		CreateNewClassQuickFixProposal proposal = new CreateNewClassQuickFixProposal(offset, length, className, false,
				javaProject, properties, constructorArgCount, false);
		proposal.apply(document);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/create-class-proposal.xml");
		javaProject = JavaCore.create(project);
	}

	public void testCreatingEnclosingClass() throws Exception {
		String className = "com.test.Account$Test";
		assertNull(className + " should not exist before applying proposal", JdtUtils.getJavaType(project, className));
		applyProposal("createClassTest10", className, 0, new HashSet<String>());

		assertNotNull("Expects " + className + " to be created", JdtUtils.getJavaType(project, className));
	}

	public void testWith1Property1ConstructorArg() throws Exception {
		String className = "com.test.Account5";
		Set<String> properties = new HashSet<String>();
		properties.add("creationDate");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest5", className, 1, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
		assertTrue("Expects writable property createDate to be created", Introspector.hasWritableProperty(type,
				"creationDate"));
	}

	public void testWith1Property2ConstructorArg() throws Exception {
		String className = "com.test.Account8";
		Set<String> properties = new HashSet<String>();
		properties.add("nickName");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest8", className, 2, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
		assertTrue("Expects writable property nickName to be created", Introspector.hasWritableProperty(type,
				"nickName"));
	}

	public void testWith1PropertyNoConstructorArg() throws Exception {
		String className = "com.test.Account2";
		Set<String> properties = new HashSet<String>();
		properties.add("name");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest2", className, 0, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
		assertTrue("Expects writable property name to be created", Introspector.hasWritableProperty(type, "name"));
	}

	public void testWith2Property1ConstructorArg() throws Exception {
		String className = "com.test.Account6";
		Set<String> properties = new HashSet<String>();
		properties.add("creationDate");
		properties.add("nickName");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest6", className, 1, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
		assertTrue("Expects writable property creationDate to be created", Introspector.hasWritableProperty(type,
				"creationDate"));
		assertTrue("Expects writable property nickName to be created", Introspector.hasWritableProperty(type,
				"nickName"));
	}

	public void testWith2Property2ConstructorArg() throws Exception {
		String className = "com.test.Account9";
		Set<String> properties = new HashSet<String>();
		properties.add("nickName");
		properties.add("expiryDate");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest9", className, 2, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
		assertTrue("Expects writable property nickName to be created", Introspector.hasWritableProperty(type,
				"nickName"));
		assertTrue("Expects writable property expiryDate to be created", Introspector.hasWritableProperty(type,
				"expiryDate"));
	}

	public void testWith2PropertyNoConstructorArg() throws Exception {
		String className = "com.test.Account3";
		Set<String> properties = new HashSet<String>();
		properties.add("name");
		properties.add("creationDate");

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest3", className, 0, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
		assertTrue("Expects writable property name to be created", Introspector.hasWritableProperty(type, "name"));
		assertTrue("Expects writable property creationDate to be created", Introspector.hasWritableProperty(type,
				"creationDate"));
	}

	public void testWithNoProperty1ConstructorArg() throws Exception {
		String className = "com.test.Account4";

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest4", className, 1, new HashSet<String>());

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 1 param to be created", Introspector.hasConstructor(type, 1, false));
	}

	public void testWithNoProperty2ConstructorArg() throws Exception {
		String className = "com.test.Account7";

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest7", className, 2, new HashSet<String>());

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 2 params to be created", Introspector.hasConstructor(type, 2, false));
	}

	public void testWithNoPropertyNoConstructorArg() throws Exception {
		String className = "com.test.Account1";
		Set<String> properties = new HashSet<String>();

		assertNull(className + " should not exist before applying proposal", javaProject.findType(className));
		applyProposal("createClassTest1", className, 0, properties);

		IType type = javaProject.findType(className);

		assertNotNull("Expects " + className + " to be created", type);
		assertTrue("Expects constructor with 0 param to be created", Introspector.hasConstructor(type, 0, false));
	}
}
