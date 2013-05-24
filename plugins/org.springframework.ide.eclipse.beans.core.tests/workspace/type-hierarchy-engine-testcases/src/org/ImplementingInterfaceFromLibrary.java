package org;

import org.springframework.beans.factory.FactoryBean;

public class ImplementingInterfaceFromLibrary<T> implements FactoryBean<T> {

	@Override
	public T getObject() throws Exception {
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

}
