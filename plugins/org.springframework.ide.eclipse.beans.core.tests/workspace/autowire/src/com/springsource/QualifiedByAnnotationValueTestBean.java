package com.springsource;

import org.springframework.beans.factory.annotation.Autowired;

public class QualifiedByAnnotationValueTestBean {

	@Autowired
	@SimpleValueQualifier("special")
	private Person larry;

	public Person getLarry() {
		return larry;
	}
}