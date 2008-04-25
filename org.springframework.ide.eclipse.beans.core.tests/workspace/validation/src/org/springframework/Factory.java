package org.springframework;

public class Factory {
	
	public Factory(boolean one, boolean two, boolean three, int four) {
		
	}
	
	public static Factory newInstance(boolean one, boolean two, boolean three, int four) {
		return new Factory(one, two, three, four);
	}
	
}
