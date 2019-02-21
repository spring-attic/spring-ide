/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.eclipse.beans.ui.live.utils.SpringResource;

public class BeansResourceDefinitionParsingTests {

	@Test
	public void classPathResource() throws Exception {
		String resourceDefinition = "class path resource [org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class]";
		String expectedPath = "org/springframework/boot/actuate/autoconfigure/audit/AuditAutoConfiguration.class";
		String expectedFQType = "org.springframework.boot.actuate.autoconfigure.audit.AuditAutoConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void classPathResourceInnerClass() throws Exception {
		String resourceDefinition = "class path resource [org/springframework/boot/actuate/autoconfigure/metrics/MetricsAutoConfiguration$MeterBindersConfiguration.class]";
		String expectedPath = "org/springframework/boot/actuate/autoconfigure/metrics/MetricsAutoConfiguration$MeterBindersConfiguration.class";
		String expectedFQType = "org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration$MeterBindersConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Test
	public void beanDefinitionIn() throws Exception {
		String resourceDefinition = "BeanDefinition defined in org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration";
		String expectedPath = "org/springframework/security/oauth2/config/annotation/web/configuration/OAuth2ClientConfiguration.class";
		String expectedFQType = "org.springframework.security.oauth2.config.annotation.web.configuration.OAuth2ClientConfiguration";
		SpringResource parser = new SpringResource(resourceDefinition);
		String actualPath = parser.getResourcePath();
		String actualClassName = parser.getClassName();
		assertEquals(expectedPath, actualPath);
		assertEquals(expectedFQType, actualClassName);
	}

	@Ignore
	@Test
	public void fileDefinition() throws Exception {
		// Need to support this case when implemented in Beans property page:
		// String resourceDefinition = "file [/Users/sts4dev/rt-boot-ls/gs-rest-service-complete/target/classes/hello/GreetingController.class]";
	}
}
