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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.Introspector.Public;
import org.springframework.ide.eclipse.core.java.Introspector.Static;
import org.springframework.ide.eclipse.quickfix.proposals.RenamePropertyQuickfixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class RenamePropertyQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	private IJavaProject javaProject;

	private IEditorPart editor;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		editor = openBeanEditor("src/rename-property-proposal.xml");
		javaProject = JavaCore.create(project);
	}

	public void testRenameProposal() throws Exception {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, "renamePropertyTest", beansNode
				.getChildNodes());
		IDOMNode propertyNode = QuickfixTestUtil.getFirstNode(BeansSchemaConstants.ELEM_PROPERTY, beanNode
				.getChildNodes());
		AttrImpl nameAttr = (AttrImpl) propertyNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_NAME);
		ITextRegion valueRegion = nameAttr.getValueRegion();

		int offset = getOffset(valueRegion, propertyNode);
		int length = getLength(valueRegion, false);

		String className = "com.test.Restaurant";
		IType type = javaProject.findType(className);

		assertNull("Method setCuisine should not exist before proposal is applied", Introspector.findMethod(type,
				"setCuisine", 1, Public.YES, Static.NO));
		assertNotNull("Method setCuisineType should not before proposal is applied", Introspector.findMethod(type,
				"setCuisineType", 1, Public.YES, Static.NO));

		RenamePropertyQuickfixProposal proposal = new RenamePropertyQuickfixProposal(offset, length, "cuisineType",
				className, false, file, project);
		proposal.doRename("cuisine", editor.getSite().getShell());

		assertNull("Method setCuisineType should not exist after proposal is applied", Introspector.findMethod(type,
				"setCuisineType", 1, Public.YES, Static.NO));
		assertNotNull("Method setCuisine should exist after proposal is applied", Introspector.findMethod(type,
				"setCuisine", 1, Public.YES, Static.NO));

	}
}
