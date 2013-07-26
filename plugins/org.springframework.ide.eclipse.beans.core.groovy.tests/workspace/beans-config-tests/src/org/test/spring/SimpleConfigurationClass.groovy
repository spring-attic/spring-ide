package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SimpleConfigurationClass {
	
	@Bean
	public SimpleBeanClass simpleScannedBean() {
		return new SimpleBeanClass();
	}

}
