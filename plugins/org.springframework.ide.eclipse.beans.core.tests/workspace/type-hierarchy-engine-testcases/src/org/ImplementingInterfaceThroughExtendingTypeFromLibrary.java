package org;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class ImplementingInterfaceThroughExtendingTypeFromLibrary<T> extends AbstractFactoryBean<T> {

	@Override
	protected T createInstance() throws Exception {
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return null;
	}

}
