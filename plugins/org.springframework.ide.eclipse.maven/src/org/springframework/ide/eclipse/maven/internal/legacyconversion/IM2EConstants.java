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
package org.springframework.ide.eclipse.maven.internal.legacyconversion;

/**
 * Useful constants for M2E conversion
 * @author Andrew Eisenberg
 * @since 2.8.0
 */
public interface IM2EConstants {

    String OLD_NATURE = "org.maven.ide.eclipse.maven2Nature";
    String NEW_NATURE = "org.eclipse.m2e.core.maven2Nature";
    
    String OLD_BUILDER = "org.maven.ide.eclipse.maven2Builder";
    String NEW_BUILDER = "org.eclipse.m2e.core.maven2Builder";
    
    String OLD_CONTAINER = "org.maven.ide.eclipse.MAVEN2_CLASSPATH_CONTAINER";
    String NEW_CONTAINER = "org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER";
    
    String DONT_AUTO_CHECK = "dont.auto.check.for.legacy";
    
}
