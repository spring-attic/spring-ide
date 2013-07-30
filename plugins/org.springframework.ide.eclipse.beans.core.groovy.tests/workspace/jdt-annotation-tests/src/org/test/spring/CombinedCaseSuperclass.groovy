package org.test.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(["/index1.htm", "/index2.htm"])
public class CombinedCaseSuperclass {

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
	
	@Bean
	public Object getBeanInstance(String name, Object addOn) {
		return null;
	}
	
	@Bean
	public Object getSuperInstance() {
		return null;
	}
	
}
