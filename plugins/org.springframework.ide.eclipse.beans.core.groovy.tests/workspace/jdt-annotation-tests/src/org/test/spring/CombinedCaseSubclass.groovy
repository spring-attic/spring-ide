package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping({"/index3.htm", "/index4.htm", "/index5.htm"})
public class CombinedCaseSubclass extends CombinedCaseSuperclass {
	
	@Bean
	public Object getBeanInstance() {
		return null;
	}
	
	@Bean
	public Object getBeanInstance(String name) {
		return null;
	}
	
	public Object getBeanInstance(Object somethingelse) {
		return null;
	}
	
	public Object getBeanInstance(String name, Object addOn) {
		return null;
	}
	
	@Bean
	public Object getAnotherBean() {
		return null;
	}

}
