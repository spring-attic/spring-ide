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
		dataSource.addAttribute(LiveBean.ATTR_CLASS, "org.apache.commons.dbcp.BasicDataSource");
		dataSource.addAttribute(LiveBean.ATTR_APP_CONTEXT, "applicationContext-dataSource.xml");

		LiveBean initDatabase = new LiveBean("<anonymous> DataSourceInitializer");
		initDatabase.addAttribute(LiveBean.ATTR_APP_CONTEXT, "applicationContext-dataSource.xml");

		// applicationContext-jdbc.xml
		LiveBean txManager = new LiveBean("transactionManager");
		txManager.addAttribute(LiveBean.ATTR_CLASS, "org.springframework.jdbc.datasource.DataSourceTransactionManager");
		txManager.addAttribute(LiveBean.ATTR_APP_CONTEXT, "applicationContext-jdbc.xml");

		LiveBean clinic = new LiveBean("clinic");
		clinic.addAttribute(LiveBean.ATTR_CLASS, "org.springframework.samples.petclinic.jdbc.SimpleJdbcClinic");
		clinic.addAttribute(LiveBean.ATTR_APP_CONTEXT, "applicationContext-jdbc.xml");

		LiveBean callMonitor = new LiveBean("callMonitor");
		callMonitor.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.samples.petclinic.aspects.CallMonitoringAspect");
		callMonitor.addAttribute(LiveBean.ATTR_APP_CONTEXT, "applicationContext-jdbc.xml");

		// petclinic-servlet.xml
		LiveBean visits = new LiveBean("visits");
		visits.addAttribute(LiveBean.ATTR_CLASS, "org.springframework.samples.petclinic.web.VisitsAtomView");
		visits.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean vets = new LiveBean("vets");
		vets.addAttribute(LiveBean.ATTR_CLASS, "org.springframework.web.servlet.view.xml.MarshallingView");
		vets.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean marshaller = new LiveBean("marshaller");
		marshaller.addAttribute(LiveBean.ATTR_CLASS, "org.springframework.samples.petclinic.Vets");
		marshaller.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean messageSource = new LiveBean("messageSource");
		messageSource.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.context.support.ResourceBundleMessageSource");
		messageSource.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean methodHandler = new LiveBean("<anonymous> AnnotationMethodHandlerAdapter");
		methodHandler.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter");
		methodHandler.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean exceptionResolver = new LiveBean("<anonymous> SimpleMappingExceptionResolver");
		exceptionResolver.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.web.servlet.handler.SimpleMappingExceptionResolver");
		exceptionResolver.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean contentNegotiatingViewResolver = new LiveBean("<anonymous> ContentNegotiatingViewResolver");
		contentNegotiatingViewResolver.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.web.servlet.view.ContentNegotiatingViewResolver");
		contentNegotiatingViewResolver.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean beanNameViewResolver = new LiveBean("<anonymous> BeanNameViewResolver");
		beanNameViewResolver.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.web.servlet.view.BeanNameViewResolver");
		beanNameViewResolver.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

		LiveBean internalResourceViewResolver = new LiveBean("<anonymous> InternalResourceViewResolver");
		internalResourceViewResolver.addAttribute(LiveBean.ATTR_CLASS,
				"org.springframework.web.servlet.view.InternalResourceViewResolver");
		internalResourceViewResolver.addAttribute(LiveBean.ATTR_APP_CONTEXT, "petclinic-servlet.xml");

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
