package org.test.spring;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;

public class SimpleBeanClassWithAttribute {
	
	@Bean(autowire=Autowire.BY_NAME)
	public Object getInstanceOfBean() {
		return null;
	}

}
