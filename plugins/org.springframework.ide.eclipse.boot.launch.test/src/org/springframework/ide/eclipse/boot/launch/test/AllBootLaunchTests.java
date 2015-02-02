package org.springframework.ide.eclipse.boot.launch.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	MainTypeSelectionModelTest.class,
	ProfileHistoryTest.class
})
public class AllBootLaunchTests {

}
