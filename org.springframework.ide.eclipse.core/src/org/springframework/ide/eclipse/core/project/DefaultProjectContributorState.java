package org.springframework.ide.eclipse.core.project;

import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation of the {@link IProjectContributorState} interface.
 */
public class DefaultProjectContributorState implements IProjectContributorState {

	private Map<Class, Object> managedObjects = new HashMap<Class, Object>();

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		return (T) managedObjects.get(clazz);
	}

	public boolean hold(Object obj) {
		if (managedObjects.containsKey(obj.getClass())) {
			return false;
		}
		else {
			managedObjects.put(obj.getClass(), obj);
			return true;
		}
	}

}