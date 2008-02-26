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
	
	protected static void test2() {
		
	}
	
	private static Base createInstance() {
		return new Base();
	}
	
	private void init() {
		
	}
	
	private void initWithParameters(String test) {
		
	}
	
}
