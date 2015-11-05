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

/**
 * Service exporter interface that allows auto detection of 'interfaces' of the exported service.
 * 
 * @author Costin Leau
 */
public interface InterfaceDetector {

	/**
	 * Detects the 'interfaces' of the given target, suitable for OSGi service export.
	 * 
	 * @param targetClass target class
	 * @return non-null array of detected interfaces
	 */
	Class<?>[] detect(Class<?> targetClass);
}
