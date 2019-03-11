/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * https://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at https://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *   Oracle Inc.
 *   Spring IDE Developers 
 *****************************************************************************/

package org.springframework.ide.eclipse.osgi.blueprint.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.ide.eclipse.osgi.blueprint.internal.support.ToStringClassAdapter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * Parsing utility class.
 * 
 * @author Andy Piper
 * @author Costin Leau
 * @author Arnaud Mergey
 * 
 * @since 3.7.2
 */
public abstract class ParserUtils {

	private static final AttributeCallback STANDARD_ATTRS_CALLBACK = new StandardAttributeCallback();
	private static final AttributeCallback BLUEPRINT_ATTRS_CALLBACK = new BlueprintAttributeCallback();
	private static final AttributeCallback PROPERTY_REF_ATTRS_CALLBACK = new PropertyRefAttributeCallback();
	private static final AttributeCallback PROPERTY_CONV_ATTRS_CALLBACK = new ConventionsCallback();

	public static final String REFERENCE_LISTENER_REF_ATTR = "org.eclipse.gemini.blueprint.config.internal.reference.listener.ref.attr";

	/**
	 * Generic attribute callback. Will parse the given callback array, w/o any
	 * standard callback.
	 * 
	 * @param element
	 *            XML element
	 * @param builder
	 *            current bean definition builder
	 * @param callbacks
	 *            array of callbacks (can be null/empty)
	 */
	public static void parseAttributes(Element element, BeanDefinitionBuilder builder, AttributeCallback[] callbacks) {
		NamedNodeMap attributes = element.getAttributes();

		for (int x = 0; x < attributes.getLength(); x++) {
			Attr attr = (Attr) attributes.item(x);

			boolean shouldContinue = true;
			if (!ObjectUtils.isEmpty(callbacks))
				for (int i = 0; i < callbacks.length && shouldContinue; i++) {
					AttributeCallback callback = callbacks[i];
					shouldContinue = callback.process(element, attr, builder);
				}
		}
	}

	/**
	 * Dedicated parsing method that uses the following stack:
	 * 
	 * <ol>
	 * <li>user given {@link AttributeCallback}s</li>
	 * <li>{@link StandardAttributeCallback}</li>
	 * <li>{@link PropertyRefAttributeCallback}</li>
	 * <li>{@link ConventionCallback}</li>
	 * </ol>
	 * </pre>
	 * 
	 * @param element
	 *            XML element
	 * @param builder
	 *            current bean definition builder
	 * @param callbacks
	 *            array of callbacks (can be null/empty)
	 */
	public static void parseCustomAttributes(Element element, BeanDefinitionBuilder builder,
			AttributeCallback[] callbacks) {
		List<AttributeCallback> list = new ArrayList<>(8);

		if (!ObjectUtils.isEmpty(callbacks))
			CollectionUtils.mergeArrayIntoCollection(callbacks, list);
		// add standard callback
		list.add(STANDARD_ATTRS_CALLBACK);
		list.add(BLUEPRINT_ATTRS_CALLBACK);
		list.add(PROPERTY_REF_ATTRS_CALLBACK);
		list.add(PROPERTY_CONV_ATTRS_CALLBACK);

		AttributeCallback[] cbacks = list.toArray(new AttributeCallback[list.size()]);
		parseAttributes(element, builder, cbacks);
	}

	/**
	 * Derivative for
	 * {@link #parseCustomAttributes(Element, BeanDefinitionBuilder, org.springframework.ide.eclipse.osgi.blueprint.internal.util.internal.config.ParserUtils.AttributeCallback[])}
	 * accepting only one {@link AttributeCallback}.
	 * 
	 * @param element
	 *            XML element
	 * @param builder
	 *            current bean definition builder
	 * @param callback
	 *            attribute callback, can be null
	 */
	public static void parseCustomAttributes(Element element, BeanDefinitionBuilder builder,
			AttributeCallback callback) {
		AttributeCallback[] callbacks = (callback == null ? new AttributeCallback[0]
				: new AttributeCallback[] { callback });
		parseCustomAttributes(element, builder, callbacks);
	}

	public static AttributeCallback[] mergeCallbacks(AttributeCallback[] callbacksA, AttributeCallback[] callbacksB) {
		if (ObjectUtils.isEmpty(callbacksA))
			if (ObjectUtils.isEmpty(callbacksB))
				return new AttributeCallback[0];
			else
				return callbacksB;
		if (ObjectUtils.isEmpty(callbacksB))
			return callbacksA;

		AttributeCallback[] newCallbacks = new AttributeCallback[callbacksA.length + callbacksB.length];
		System.arraycopy(callbacksA, 0, newCallbacks, 0, callbacksA.length);
		System.arraycopy(callbacksB, 0, newCallbacks, callbacksA.length, callbacksB.length);
		return newCallbacks;
	}
}