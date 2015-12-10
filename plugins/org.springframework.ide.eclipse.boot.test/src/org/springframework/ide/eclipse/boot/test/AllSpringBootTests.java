package org.springframework.ide.eclipse.boot.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		//SpringBootProjectTests.class: removed for now
		//  functionality for this is now tested via EditStartersModelTest, and
		//  other tests that use the functionalities provided by ISpringBootProject
		EnableDisableBootDevtoolsTest.class,

		NewSpringBootWizardModelTest.class,
		NewSpringBootWizardTest.class,
		SpringBootValidationTest.class,
		InitializrDependencySpecTest.class,
		EditStartersModelTest.class
})
public class AllSpringBootTests {

}
