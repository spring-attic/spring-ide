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

package org.springframework.ide.eclipse.bestpractices.tests.ruletests;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.internal.bestpractices.springiderules.RefElementRule;

/**
 * Test case for the {@link UseBeanInheritanceRule} class.
 * @author Wesley Coelho
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 */
public class UseBeanInheritanceRuleTest extends AbstractRuleTest {

	private static final String SUBSTRING_OF_INFO_MESSAGE = "Consider using bean inheritance";

	private void checkMarkerNotCreated(String fileName) throws Exception {
		IResource resource = createPredefinedProjectAndGetResource("bestpractices", fileName);
		IMarker[] markers = resource.findMarkers(null, false, IResource.DEPTH_ZERO);
		assertNotHasMarkerWithText(markers, RefElementRule.INFO_MESSAGE);
	}

	public void testMarkerCreated() throws Exception {
		IResource resource = createPredefinedProjectAndGetResource("bestpractices", "src/bean-inheritance-positive.xml");
		IMarker[] markers = resource.findMarkers(null, false, IResource.DEPTH_ZERO);
		assertHasMarkerWithText(markers, SUBSTRING_OF_INFO_MESSAGE);
	}

	public void testMarkerNotCreated1() throws Exception {
		checkMarkerNotCreated("src/bean-inheritance-negative-1.xml");
	}

	public void testMarkerNotCreated2() throws Exception {
		checkMarkerNotCreated("src/bean-inheritance-negative-2.xml");
	}

	public void testMarkerNotCreated3() throws Exception {
		checkMarkerNotCreated("src/bean-inheritance-negative-3.xml");
	}

	public void testMarkerNotCreated4() throws Exception {
		checkMarkerNotCreated("src/bean-inheritance-negative-4.xml");
	}

	public void testMarkerNotCreated5() throws Exception {
		checkMarkerNotCreated("src/bean-inheritance-negative-5.xml");
	}

	@Override
	String getRuleId() {
		return "com.springsource.sts.bestpractices.UseBeanInheritance";
	}

}
