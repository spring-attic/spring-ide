package org.springframework.ide.eclipse.aop.core.model;

public interface IAdviceChangedListener {

	/**
	 * Indicate that there has been a change in the set of advised elements
	 * 
	 */
	public void adviceChanged();
}