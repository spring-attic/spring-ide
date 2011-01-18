package org.springframework;

public aspect BarIntroduction {
	
	private String Foo.bar;
	
	/**
	 * test
	 */
	public void Foo.setBar(String bar) {
		this.bar = bar;
	}

	public String Foo.getBar() {
		return this.bar;
	}
	
}
