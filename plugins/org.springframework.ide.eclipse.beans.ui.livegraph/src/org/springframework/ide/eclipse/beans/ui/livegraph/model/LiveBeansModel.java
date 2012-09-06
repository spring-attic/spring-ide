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
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A mock pre-generated beans model
 * 
 * @author Leo Dos Santos
 */
public class LiveBeansModel {

	private final List<LiveBean> beans;

	public LiveBeansModel() {
		beans = new ArrayList<LiveBean>();

		// applicationContext-dataSource.xml
		LiveBean dataSource = new LiveBean("dataSource");
		LiveBean initDatabase = new LiveBean("<anonymous> DataSourceInitializer");

		// applicationContext-jdbc.xml
		LiveBean txManager = new LiveBean("transacationManager");
		LiveBean clinic = new LiveBean("clinic");
		LiveBean callMonitor = new LiveBean("callMonitor");

		// petclinic-servlet.xml
		LiveBean visits = new LiveBean("visits");
		LiveBean vets = new LiveBean("vets");
		LiveBean marshaller = new LiveBean("marshaller");
		LiveBean messageSource = new LiveBean("messageSource");
		LiveBean methodHandler = new LiveBean("<anonymous> AnnotationMethodHandlerAdapter");
		LiveBean exceptionResolver = new LiveBean("<anonymous> SimpleMappingExceptionResolver");
		LiveBean contentNegotiatingViewResolver = new LiveBean("<anonymous> ContentNegotiatingViewResolver");
		LiveBean beanNameViewResolver = new LiveBean("<anonymous> BeanNameViewResolver");
		LiveBean internalResourceViewResolver = new LiveBean("<anonymous> InternalResourceViewResolver");

		initDatabase.addChild(dataSource);
		txManager.addChild(dataSource);
		vets.addChild(marshaller);

		beans.add(dataSource);
		beans.add(initDatabase);
		beans.add(txManager);
		beans.add(clinic);
		beans.add(callMonitor);
		beans.add(visits);
		beans.add(vets);
		beans.add(marshaller);
		beans.add(messageSource);
		beans.add(methodHandler);
		beans.add(exceptionResolver);
		beans.add(contentNegotiatingViewResolver);
		beans.add(beanNameViewResolver);
		beans.add(internalResourceViewResolver);
	}

	public List<LiveBean> getBeans() {
		return beans;
	}

}
