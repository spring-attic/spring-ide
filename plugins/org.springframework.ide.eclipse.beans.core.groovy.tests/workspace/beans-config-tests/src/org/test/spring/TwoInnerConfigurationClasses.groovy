package org.test.spring;

import org.springframework.context.annotation.Configuration;

public class TwoInnerConfigurationClasses {
	
	@Configuration
	public class InnerConfigClass1 {
	}

	@Configuration
	public class InnerConfigClass2 {
	}

}
