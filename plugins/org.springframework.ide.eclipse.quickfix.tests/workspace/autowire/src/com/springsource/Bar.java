package com.springsource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class Bar {

	@Autowired
	@Qualifier("special")
	private Foo foo;
	
}
