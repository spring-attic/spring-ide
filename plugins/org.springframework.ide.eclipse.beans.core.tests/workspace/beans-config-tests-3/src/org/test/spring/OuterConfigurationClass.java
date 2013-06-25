package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class OuterConfigurationClass {
	@Configuration
	static class InnerConfigClass1 {
		@Bean
		public SimpleBeanClass simpleScannedBean() {
			return new SimpleBeanClass();
		}
	}
	
	@Configuration
	static class InnerConfigClass2 {
		@Bean
		public SimpleBeanClass2 simpleScannedBean() {
			return new SimpleBeanClass2();
		}
	}
	static class InnerConfigClass3 {
		@Configuration
		static class InnerInnerConfigClass {
			@Bean
			public SimpleBeanClass3 simpleScannedBean() {
				return new SimpleBeanClass3();
			}
		}
	}
	
	static class InnerConfigClass4 {
		@Configuration
		static class InnerInnerConfigClass {
			@Bean
			public SimpleBeanClass3 simpleScannedBean() {
				return new SimpleBeanClass3();
			}
		}
	}
	
}
