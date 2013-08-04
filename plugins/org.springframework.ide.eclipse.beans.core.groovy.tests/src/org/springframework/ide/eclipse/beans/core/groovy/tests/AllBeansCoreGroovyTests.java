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
import org.springframework.ide.core.classreading.tests.JdtAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtBasedAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtClassMetadataTest;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.tests.ResourceChangeEventHandlerTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansConfigDescriptionWriterTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansConfigFactoryTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansJavaGroovyConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansProjectTest;
import org.springframework.ide.eclipse.beans.ui.refactoring.tests.BeansJavaConfigRenameTypeRefactoringParticipantTest;

/**
 * Test suite for <code>beans.core</code> plugin.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 * @since 2.0.3
 */
@RunWith(Suite.class)
@SuiteClasses({
	JdtAnnotationMetadataTest.class,
	JdtBasedAnnotationMetadataTest.class,
	JdtClassMetadataTest.class,
	BeansConfigTest.class,
	BeansJavaGroovyConfigTest.class,
	BeansProjectTest.class,
	BeansConfigFactoryTest.class,
	BeansConfigDescriptionWriterTest.class,
	BeansJavaConfigRenameTypeRefactoringParticipantTest.class,
	ResourceChangeEventHandlerTest.class,
})
public class AllBeansCoreGroovyTests {
	// goofy junit4, no class body needed
}
