package org.test.spring;


public class OverriddenMethodWithoutAnnotation extends SimpleBeanClass {
	
	public Object getInstanceOfBean() {
		return super.getInstanceOfBean();
	}

}
