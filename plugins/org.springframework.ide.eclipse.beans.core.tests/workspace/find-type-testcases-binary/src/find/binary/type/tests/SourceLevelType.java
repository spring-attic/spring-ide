package find.binary.type.tests;

import java.util.Observable;
import java.util.Observer;

public class SourceLevelType {
	
	public static class SourceLevelInnerType {
	}
	
	public void foo() {
		new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				new Observer() {
					@Override
					public void update(Observable o, Object arg) {
					}
				};
			}
			
			class NamedInnerClassOfAnonymousInnerClass {
				public void bar() {
					new Observer() {
						@Override
						public void update(Observable o, Object arg) {
							// TODO Auto-generated method stub
							
						}
					};
				}
			}
		};
	}

}
