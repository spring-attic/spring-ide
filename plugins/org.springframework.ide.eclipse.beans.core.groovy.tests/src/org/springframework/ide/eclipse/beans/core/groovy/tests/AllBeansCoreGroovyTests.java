/*******************************************************************************
 * Copyright (c) 2005, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.groovy.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.core.classreading.tests.JdtGroovyAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtBasedGroovyAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtGroovyClassMetadataTest;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.tests.GroovyResourceChangeEventHandlerTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansGroovyConfigDescriptionWriterTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansGroovyConfigFactoryTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansGroovyConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansJavaGroovyConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansGroovyProjectTest;
import org.springframework.ide.eclipse.beans.ui.refactoring.tests.BeansJavaGroovyConfigRenameTypeRefactoringParticipantTest;

/**
 * Test suite for <code>beans.core</code> plugin.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 * @since 2.0.3
 */
@RunWith(Suite.class)
@SuiteClasses({
	JdtGroovyAnnotationMetadataTest.class,
	JdtBasedGroovyAnnotationMetadataTest.class,
	JdtGroovyClassMetadataTest.class,
	BeansGroovyConfigTest.class,
	BeansJavaGroovyConfigTest.class,
	BeansGroovyProjectTest.class,
	BeansGroovyConfigFactoryTest.class,
	BeansGroovyConfigDescriptionWriterTest.class,
	BeansJavaGroovyConfigRenameTypeRefactoringParticipantTest.class,
	GroovyResourceChangeEventHandlerTest.class,
})
public class AllBeansCoreGroovyTests {
	// goofy junit4, no class body needed
}
