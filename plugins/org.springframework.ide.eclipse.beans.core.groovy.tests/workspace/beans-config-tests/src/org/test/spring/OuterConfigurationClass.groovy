package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class OuterConfigurationClass {
	
	@Configuration
	public class InnerConfigurationClass {

		@Bean
		public SimpleBeanClass simpleScannedBean() {
			return new SimpleBeanClass();
		}

	}

}
