package org.springframework.ide.eclipse.boot.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		SpringBootProjectTests.class,
		DevToolsStarterTests.class,

		NewSpringBootWizardModelTest.class,
		NewSpringBootWizardTest.class,
		SpringBootValidationTest.class
})
public class AllSpringBootTests {

}
