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
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.quickfix.proposals.AddToConfigSetQuickFixProposal;
import org.springframework.ide.eclipse.quickfix.tests.QuickfixTestUtil;


/**
 * @author Terry Denney
 */
@SuppressWarnings("restriction")
public class AddToConfigSetQuickFixProposalTest extends AbstractBeanFileQuickfixTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		openBeanEditor("src/config-set-proposal-test.xml");
	}

	public void testAddToConfigSet() {
		IDOMNode beanNode = QuickfixTestUtil.getNode(BeansSchemaConstants.ELEM_BEAN, "addToConfigSetTest", beansNode
				.getChildNodes());
		AttrImpl parentAttr = (AttrImpl) beanNode.getAttributes().getNamedItem(BeansSchemaConstants.ATTR_PARENT);
		ITextRegion valueRegion = parentAttr.getValueRegion();

		int offset = getOffset(valueRegion, beanNode);
		int length = getLength(valueRegion, false);

		BeansProject beanProject = (BeansProject) BeansCorePlugin.getModel().getProject(project);
		IBeansConfigSet configSet = beanProject.getConfigSet("AddToConfigSetTest");
		IBeansConfig config = beanProject.getConfig(file);

		assertFalse("Expects config file to not be in config set", configSet.getConfigs().contains(config));

		AddToConfigSetQuickFixProposal proposal = new AddToConfigSetQuickFixProposal(offset, length, false, file,
				configSet, beanProject);
		proposal.apply(document);

		configSet = beanProject.getConfigSet("AddToConfigSetTest");
		config = beanProject.getConfig(file);
		assertTrue("Expects config file to be added into config set", configSet.getConfigs().contains(config));
	}

}
