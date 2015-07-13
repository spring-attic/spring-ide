/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import org.springframework.ide.eclipse.boot.dash.model.requestmappings.JLRMethodParser;

import junit.framework.TestCase;

public class JLRMethodParserTest extends TestCase {

	public void testCase1() throws Exception {
		String data = "public java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(java.security.Principal)";
		assertEquals("org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint", JLRMethodParser.getFQClassName(data));
	}

	public void testCase1b() throws Exception {
		String data = "public synchronized java.lang.Object org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint.invoke(java.security.Principal)";
		assertEquals("org.springframework.boot.actuate.endpoint.mvc.HealthMvcEndpoint", JLRMethodParser.getFQClassName(data));
	}

	public void testCase2() throws Exception {
		String data = "java.util.Collection<demo.Reservation> demo.ReservationRestController.reservations()";
		assertEquals("demo.ReservationRestController", JLRMethodParser.getFQClassName(data));
	}

	public void testCase3() throws Exception {
		String data = "public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)";
		assertEquals("org.springframework.boot.autoconfigure.web.BasicErrorController", JLRMethodParser.getFQClassName(data));
	}

	public void testCase4() throws Exception {
		String data = "public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest) throws java.lang.Exception";
		assertEquals("org.springframework.boot.autoconfigure.web.BasicErrorController", JLRMethodParser.getFQClassName(data));
	}

	public void testGarbage() throws Exception {
		assertNull(JLRMethodParser.getFQClassName(null));
		assertNull(JLRMethodParser.getFQClassName(""));
		assertNull(JLRMethodParser.getFQClassName("haha"));
		assertNull(JLRMethodParser.getFQClassName("String haha()"));
		assertNull(JLRMethodParser.getFQClassName("public synchronized String haha()"));
	}

}
