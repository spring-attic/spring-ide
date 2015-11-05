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

package org.eclipse.gemini.blueprint.service.exporter.support;

import org.eclipse.gemini.blueprint.util.internal.ClassUtils;

/**
 * Default implementation of {@link InterfaceDetector}.
 * 
 * @author Costin Leau
 */
public enum DefaultInterfaceDetector implements InterfaceDetector {

	/**
	 * Do not detect anything.
	 */
	DISABLED {
		private final Class<?>[] clazz = new Class[0];

		public Class<?>[] detect(Class<?> targetClass) {
			return clazz;
		}
	},

	INTERFACES {
		public Class<?>[] detect(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.INTERFACES);
		}
	},

	CLASS_HIERARCHY {
		public Class<?>[] detect(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.CLASS_HIERARCHY);
		}
	},

	ALL_CLASSES {
		public Class<?>[] detect(Class<?> targetClass) {
			return ClassUtils.getClassHierarchy(targetClass, ClassUtils.ClassSet.ALL_CLASSES);
		}
	}
}
