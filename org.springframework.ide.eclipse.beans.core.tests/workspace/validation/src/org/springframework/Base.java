package org.springframework;

public class Base implements FooInterface {
	
	public Base() {
	}
	
	public Base(String test) {
		
	}
	
	private String foo;

	public String getFoo() {
		return foo;
	}

	public void setFoo(String foo) {
		this.foo = foo;
	}
	
	protected void test1() {
		
	}
	
	protected static test2() {
		
	}
	
}
