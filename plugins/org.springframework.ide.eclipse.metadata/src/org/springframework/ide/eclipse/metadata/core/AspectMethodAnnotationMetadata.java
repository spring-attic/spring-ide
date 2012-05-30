/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.core;

import java.util.HashMap;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareAnnotation;
import org.aspectj.lang.annotation.DeclareError;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.ide.eclipse.beans.core.metadata.model.AbstractMethodAnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IMethodMetadata;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * {@link IMethodMetadata} for AspectJ 5 method level annotations.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class AspectMethodAnnotationMetadata extends AbstractMethodAnnotationMetadata {

	private static final long serialVersionUID = 4275348191391381652L;
	
	/** Possible types that the metadata can be. */
	public enum Type {
		BEFORE, AFTER, AROUND, DECLARE_ANNOTATION, DECLARE_WARNING, DECLARE_ERROR, POINTCUT, DECLARE_PARENTS
	}

	private static final Map<String, Type> TYPE_MAPPING;

	static {
		TYPE_MAPPING = new HashMap<String, Type>();
		TYPE_MAPPING.put(Before.class.getName(), Type.BEFORE);
		TYPE_MAPPING.put(After.class.getName(), Type.AFTER);
		TYPE_MAPPING.put(AfterReturning.class.getName(), Type.AFTER);
		TYPE_MAPPING.put(AfterThrowing.class.getName(), Type.AFTER);
		TYPE_MAPPING.put(Around.class.getName(), Type.AROUND);
		TYPE_MAPPING.put(DeclareAnnotation.class.getName(), Type.DECLARE_ANNOTATION);
		TYPE_MAPPING.put(DeclareWarning.class.getName(), Type.DECLARE_WARNING);
		TYPE_MAPPING.put(DeclareError.class.getName(), Type.DECLARE_ERROR);
		TYPE_MAPPING.put(DeclareParents.class.getName(), Type.DECLARE_PARENTS);
		TYPE_MAPPING.put(Pointcut.class.getName(), Type.POINTCUT);
	}

	private Type type = null;

	public AspectMethodAnnotationMetadata(String key, String handle, Object value,
			IModelSourceLocation location) {
		super(key, handle, value, location);
		this.type = TYPE_MAPPING.get(key);
	}

	public Type getType() {
		return type;
	}

}
