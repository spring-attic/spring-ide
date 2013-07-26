package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExternalConfigurationClass {
	
	@Bean
	public SimpleBeanClass simpleScannedBean() {
		return new SimpleBeanClass();
	}

}
