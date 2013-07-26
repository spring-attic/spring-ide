package org.test.profile;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.test.spring.SimpleBeanClass;

@Configuration
@Profile("testProfile")
public class ProfileConfigurationClass {
	
	@Bean
	public SimpleBeanClass simpleScannedBean() {
		return new SimpleBeanClass();
	}

}
