/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.batch.core.configuration.v1.xml;

import org.springframework.batch.core.configuration.xml.JobParser;
import org.springframework.batch.core.configuration.xml.JobRepositoryParser;
import org.springframework.batch.core.configuration.xml.TopLevelJobListenerParser;
import org.springframework.batch.core.configuration.xml.TopLevelStepListenerParser;
import org.springframework.batch.core.configuration.xml.TopLevelStepParser;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Christian Dupuis
 * @author Dave Syer
 */
public class CoreNamespaceHandler extends NamespaceHandlerSupport {

	/**
	 * @see NamespaceHandler#init()
	 */
	public void init() {
		this.registerBeanDefinitionParser("job", new JobParser());
		this.registerBeanDefinitionParser("step", new TopLevelStepParser());
		this.registerBeanDefinitionParser("job-repository", new JobRepositoryParser());
		this.registerBeanDefinitionParser("job-listener", new TopLevelJobListenerParser());
		this.registerBeanDefinitionParser("step-listener", new TopLevelStepListenerParser());
	}
}
