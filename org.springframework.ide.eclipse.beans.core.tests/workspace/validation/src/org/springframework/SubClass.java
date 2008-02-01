package org.springframework;

public class SubClass extends Base {
	
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

	public void setDao(Object dao) {
		this.dao = dao;
	}
}
