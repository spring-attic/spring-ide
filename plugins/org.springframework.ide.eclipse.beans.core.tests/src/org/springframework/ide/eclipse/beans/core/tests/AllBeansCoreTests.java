/*******************************************************************************
 * Copyright (c) 2005, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.core.classreading.tests.JdtAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtBasedAnnotationMetadataTest;
import org.springframework.ide.core.classreading.tests.JdtClassMetadataTest;
import org.springframework.ide.eclipse.beans.core.autowire.AutowireDependencyProviderTest;
import org.springframework.ide.eclipse.beans.core.autowire.CommonAnnotationInjectionMetadataProviderTests;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.tests.ResourceChangeEventHandlerTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanClassRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanConstructorArgumentRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanConstructorArgumentRulesAutowireTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanInitDestroyMethodRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.BeanPropertyRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.NamespaceElementsRuleTest;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.rules.RequiredPropertyRuleTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansProjectAutoConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansProjectDescriptionWriterTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansConfigFactoryTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansJavaConfigTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansModelUtilsTest;
import org.springframework.ide.eclipse.beans.core.model.tests.BeansProjectTest;
import org.springframework.ide.eclipse.beans.ui.refactoring.tests.BeansJavaConfigRenameTypeRefactoringParticipantTest;
import org.springframework.ide.eclipse.core.java.IntrospectorTest;
import org.springframework.ide.eclipse.core.java.JdtUtilsTest;
import org.springframework.ide.eclipse.core.java.TypeHierarchyEngineTest;

/**
 * Test suite for <code>beans.core</code> plugin.
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 * @author Leo Dos Santos
 * @since 2.0.3
 */
@RunWith(Suite.class)
@SuiteClasses({
	BeansCoreUtilsTest.class,
	BeanClassRuleTest.class,
	BeanConstructorArgumentRuleTest.class,
	BeanConstructorArgumentRulesAutowireTest.class,
	BeanPropertyRuleTest.class,
	BeanInitDestroyMethodRuleTest.class,
	RequiredPropertyRuleTest.class,
	NamespaceElementsRuleTest.class,
	IntrospectorTest.class,
	JdtUtilsTest.class,
	AutowireDependencyProviderTest.class,
	CommonAnnotationInjectionMetadataProviderTests.class,
	JdtAnnotationMetadataTest.class,
	JdtBasedAnnotationMetadataTest.class,
	JdtClassMetadataTest.class,
	BeansConfigTest.class,
	BeansJavaConfigTest.class,
	BeansProjectTest.class,
	BeansProjectAutoConfigTest.class,
	BeansConfigFactoryTest.class,
	BeansProjectDescriptionWriterTest.class,
	BeansJavaConfigRenameTypeRefactoringParticipantTest.class,
	ResourceChangeEventHandlerTest.class,
	TypeHierarchyEngineTest.class,
	BeansModelUtilsTest.class
})
public class AllBeansCoreTests {
	// goofy junit4, no class body needed
}
