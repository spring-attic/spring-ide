package org.springframework.ide.eclipse.boot.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		//SpringBootProjectTests.class: removed for now
		//  functionality for this is now tested via EditStartersModelTest, and
		//  other tests that use the functionalities provided by ISpringBootProject
		EditStartersModelTest.class,
		EnableDisableBootDevtoolsTest.class,
		NewSpringBootWizardModelTest.class,
		NewSpringBootWizardTest.class,
		InitializrDependencySpecTest.class,
		SpringBootValidationTest.class
})
public class AllSpringBootTests {

}
