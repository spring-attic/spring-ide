package org.test.spring;

import org.springframework.context.annotation.Bean;

public class MethodIdentificationSubtype {

	@Bean
	public Object getInstance() {
		return null;
	}
	
	@Bean
	public Object getInstance(Object arg1) {
		return null;
	}
	
	@Bean
	public Object getInstance(String arg1) {
		return null;
	}
	
	@Bean
	public Object getInstance(Object arg1, Object arg2) {
		return null;
	}
	
	@Bean
	public Object getInstance(String arg1, Object arg2) {
		return null;
	}
	
	@Bean
	public Object getInstance(Object arg1, String arg2) {
		return null;
	}
	
	@Bean
	public Object getInstance(String arg1, String arg2) {
		return null;
	}
	
	@Bean
	public Object getInstance(String arg1, String arg2, String arg3) {
		return null;
	}

}
