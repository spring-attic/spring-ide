package org.springframework;

import java.io.Serializable;

public class SubClass extends Base implements Serializable{
	
	public SubClass(String test) {
		
	}

	public SubClass() {
		
	}
	
	private Object dao;
	
	private String bar;

	public String getRab() {
		return bar;
	}

	public void setRabsss(String bar) {
		this.bar = bar;
	}

	public Object getDao() {
		return dao;
	}

	public static void setDao(Object dao) {
	}
}
