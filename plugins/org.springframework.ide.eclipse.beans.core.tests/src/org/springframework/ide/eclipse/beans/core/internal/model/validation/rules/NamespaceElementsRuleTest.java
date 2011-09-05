/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.tests.BeansCoreTestCase;
import org.springframework.ide.eclipse.beans.core.tests.MarkerAssertion;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;

/**
 * Test case to test the {@link NamespaceElementsRule}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public class NamespaceElementsRuleTest extends BeansCoreTestCase {

	private IResource resource;

	@Override
	protected void setUp() throws Exception {
		// Enable XSD tool annotation rule
		Set<ValidationRuleDefinition> rules = ValidationRuleDefinitionFactory
				.getRuleDefinitions("org.springframework.ide.eclipse.beans.core.beansvalidator");
		for (ValidationRuleDefinition rule : rules) {
			if ("org.springframework.ide.eclipse.beans.core.toolAnnotation-org.springframework.ide.eclipse.beans.core.beansvalidator"
					.equals(rule.getId())) {
				rule.setEnabled(true, null);
			}
		}

		resource = createPredefinedProjectAndGetResource("validation", "src/sts-385.xml");
		Thread.sleep(5000);
	}

	public void testNamespaceValidation() throws Exception {

		MarkerAssertion[] assertions = new MarkerAssertion[] {
				new MarkerAssertion("'java.lang.String' is not a sub type of 'java.util.List'", 14),
				new MarkerAssertion("Class 'java.lang.NoSuchClass' not found", 15),
				new MarkerAssertion("Method 'nosuchmethod' not found in class 'java.lang.String'", 21),
				new MarkerAssertion("Referenced bean 'target2' not found", 22, IMarker.SEVERITY_WARNING),
				new MarkerAssertion("Class 'java.lang.NoSuchClass' not found", 29),
				new MarkerAssertion("'java.util.ArrayList' specifies a class where an interface is required", 30),
				new MarkerAssertion("Field 'NO_SUCH_FIELD' not found on class 'org.springframework.core.Ordered'", 35),
				new MarkerAssertion("Class 'org.springframework.core.NoSuchClass' not found", 36) };

		IMarker[] markers = resource.findMarkers(BeansCorePlugin.PLUGIN_ID + ".problemmarker", false,
				IResource.DEPTH_ZERO);
		assertTrue(markers.length >= 8);
		MarkerAssertion.assertMarker(markers, assertions);
	}
}
