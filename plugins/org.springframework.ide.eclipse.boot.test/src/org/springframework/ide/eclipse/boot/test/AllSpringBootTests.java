package org.springframework.ide.eclipse.boot.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.springframework.ide.eclipse.boot.core.dialogs.EditStartersModel;

@RunWith(Suite.class)
@SuiteClasses({
		SpringBootProjectTests.class,
		DevToolsStarterTests.class,

		NewSpringBootWizardModelTest.class,
		NewSpringBootWizardTest.class,
		SpringBootValidationTest.class,
		InitializrDependencySpecTest.class,
		EditStartersModelTest.class
})
public class AllSpringBootTests {

}
