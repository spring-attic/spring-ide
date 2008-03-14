package org.springframework;

import org.springframework.beans.factory.annotation.Required;

public class Foo {
	
	private String bar;

	@Required
	public void setBar(String bar) {
		this.bar = bar;
	}
	
}
