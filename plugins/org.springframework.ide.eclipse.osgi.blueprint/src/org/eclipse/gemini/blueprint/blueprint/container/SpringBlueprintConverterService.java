/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.blueprint.container;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.gemini.blueprint.blueprint.container.support.BlueprintEditorRegistrar;
import org.eclipse.gemini.blueprint.context.support.internal.security.SecurityUtils;
import org.osgi.service.blueprint.container.Converter;
import org.osgi.service.blueprint.container.ReifiedType;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.convert.ConversionException;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

/**
 * OSGi 4.2 Blueprint converter adapter as an Spring 3.0 ConverterService.
 * 
 * @author Costin Leau
 */
public class SpringBlueprintConverterService implements ConversionService {

	private static final class BlueprintConverterException extends ConversionException {

		public BlueprintConverterException(String message, Throwable cause) {
			super(message, cause);
		}

		public BlueprintConverterException(String message) {
			super(message);
		}
	}

	/** fallback delegate */
	private final ConversionService delegate;
	private final List<Converter> converters = new ArrayList<Converter>();
	private final SimpleTypeConverter typeConverter;
	// used for grabbing the security context
	private final ConfigurableBeanFactory cbf;
	private volatile boolean converterInitialized = false;

	public SpringBlueprintConverterService(ConversionService delegate, ConfigurableBeanFactory cbf) {
		this.delegate = delegate;
		this.cbf = cbf;
		this.typeConverter = new SimpleTypeConverter();
	}

	public void add(Converter blueprintConverter) {
		synchronized (converters) {
			converters.add(blueprintConverter);
		}
	}

	public void add(Collection<Converter> blueprintConverters) {
		synchronized (converters) {
			converters.addAll(blueprintConverters);
		}
	}

	public boolean canConvert(Class<?> sourceType, Class<?> targetType) {
		return true;
	}

	public boolean canConvert(TypeDescriptor sourceType, TypeDescriptor targetType) {
		return true;
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object source, Class<T> targetType) {
		return (T) convert(source, TypeDescriptor.forObject(source), TypeDescriptor.valueOf(targetType));
	}

	public Object convert(final Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (targetType == null)
			return source;
		
		final ReifiedType type = TypeFactory.getType(targetType);
		boolean hasSecurity = (System.getSecurityManager() != null);
		AccessControlContext acc = (hasSecurity ? SecurityUtils.getAccFrom(cbf) : null);
		Object result;

		if (hasSecurity) {
			result = AccessController.doPrivileged(new PrivilegedAction<Object>() {
				public Object run() {
					return doConvert(source, type);
				}
			}, acc);
		} else {
			result = doConvert(source, type);
		}

		if (result != null) {
			return result;
		}

		if (!targetType.isCollection() && !targetType.isArray() && !targetType.isMap()) {
			if (type.size() > 0) {
				for (int i = 0; i < type.size(); i++) {
					ReifiedType arg = type.getActualTypeArgument(i);
					if (!Object.class.equals(arg.getRawClass())) {
						throw new BlueprintConverterException(
								"No conversion found for generic argument(s) for reified type " + arg.getRawClass()
										+ "source type " + sourceType + "| targetType =" + targetType.getType(), null);
					}
				}
			}
		}

		if (delegate != null) {
			delegate.convert(source, sourceType, targetType);
		}

		lazyInitConverter();
		return typeConverter.convertIfNecessary(source, targetType.getType());
	}

	private void lazyInitConverter() {
		if (!converterInitialized) {
			synchronized (typeConverter) {
				if (!converterInitialized) {
					converterInitialized = true;
					if (cbf != null) {
						cbf.copyRegisteredEditorsTo(typeConverter);
						new BlueprintEditorRegistrar().registerCustomEditors(typeConverter);
					}
				}
			}
		}
	}

	private Object doConvert(Object source, ReifiedType type) {
		synchronized (converters) {
			for (Converter converter : converters) {
				try {
					if (converter.canConvert(source, type)) {
						return converter.convert(source, type);
					}
				} catch (Exception ex) {
					throw new BlueprintConverterException("Conversion between source " + source + " and reified type "
							+ type + " failed", ex);
				}
			}
		}
		return null;
	}
}