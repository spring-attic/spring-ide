package org.springframework;

import java.io.Serializable;

public class SubClass extends Base implements Serializable{
	
	public SubClass(String test) {
		
	}

	public SubClass() {
		
	}
	
	private Object dao;
	
	private String bar;

	public String getBar() {
		return bar;
	}

	public void setBar(String bar) {
		this.bar = bar;
	}

	public Object getDao() {
		return dao;
	}

	public static void setDao(Object dao) {
	}
}
