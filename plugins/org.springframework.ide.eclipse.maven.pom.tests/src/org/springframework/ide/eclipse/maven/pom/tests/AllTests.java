package org.springframework.ide.eclipse.maven.pom.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		PomStructureCreatorTest.class,
		DifferencerTest.class,
})
public class AllTests {

}
