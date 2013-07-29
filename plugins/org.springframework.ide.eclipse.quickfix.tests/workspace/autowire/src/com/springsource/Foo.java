package com.springsource;

import org.springframework.beans.factory.annotation.Autowired;

public class Foo {

	private Person larry;

	@Autowired(required = false)
	public void setLarry(Person larry) {
		this.larry = larry;
	}
	
	@Autowired(required = false)
	public void init(Person rod) {
		
	}

}
