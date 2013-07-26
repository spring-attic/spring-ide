package org.test.advanced;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.test.spring.SimpleBeanClass;

@Configuration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class AdvancedConfigurationClass {
	
	@Bean
	public SimpleBeanClass simpleScannedBean() {
		return new SimpleBeanClass();
	}

}
