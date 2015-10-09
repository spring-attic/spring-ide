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
package org.springframework.ide.eclipse.boot.dash.test.requestmappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.ide.eclipse.boot.dash.test.requestmappings.RequestMappingAsserts.assertRequestMappingWithPath;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.ActuatorClient;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.RequestMapping;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.TypeLookup;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

public class ActuatorClientTest {

	@Test public void testBasic() throws RestClientException, Exception {
		RestTemplate rest = mock(RestTemplate.class);
		TypeLookup types = mock(TypeLookup.class);
		ActuatorClient client = new ActuatorClient(new URI("http://sample"), types, rest);
		when(rest.getForObject("http://sample/mappings", String.class))
			.thenReturn(getContents("sample.json"));

		List<RequestMapping> mappings = client.getRequestMappings();

		assertRequestMappingWithPath(mappings, "/error");
		assertRequestMappingWithPath(mappings, "/**/favicon.ico");
	}

	@Test public void testException() throws RestClientException, Exception {
		RestTemplate rest = mock(RestTemplate.class);
		TypeLookup types = mock(TypeLookup.class);
		ActuatorClient client = new ActuatorClient(new URI("http://sample"), types, rest);

		when(rest.getForObject("http://sample/mappings", String.class))
			.thenThrow(new RestClientException("Something went wrong!"));

		assertNull(client.getRequestMappings());
	}

	@Test public void testUnparsableData() throws RestClientException, Exception {
		RestTemplate rest = mock(RestTemplate.class);
		TypeLookup types = mock(TypeLookup.class);
		ActuatorClient client = new ActuatorClient(new URI("http://sample"), types, rest);

		when(rest.getForObject("http://sample/mappings", String.class))
			.thenReturn("{This is not json,,,");

		assertNull(client.getRequestMappings());
	}


	@Test public void testRequestMappingInfos() throws Exception {
		RestTemplate rest = mock(RestTemplate.class);
		TypeLookup types = mock(TypeLookup.class);
		IType type = mock(IType.class);

		String json =
				"{" +
				"   \"{[/env],methods=[GET]}\":{\n" +
				"      \"bean\":\"endpointHandlerMapping\",\n" +
				"      \"method\":\"public java.lang.Object org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter.invoke()\"\n" +
				"   }\n"+
				"}";
		String fqTypeName = "org.springframework.boot.actuate.endpoint.mvc.EndpointMvcAdapter";
		String methodName = "invoke";

		when(rest.getForObject("http://sample/mappings", String.class))
			.thenReturn(json);

		ActuatorClient client = new ActuatorClient(new URI("http://sample"), types, rest);
		when(types.findType(fqTypeName)).thenReturn(type);

		RequestMapping rm = assertRequestMappingWithPath(client.getRequestMappings(), "/env");

		assertEquals(fqTypeName, rm.getFullyQualifiedClassName());
		assertEquals(methodName, rm.getMethodName());
		assertEquals(type, rm.getType());

		//Testing getMethod and isUserDefined requires mocking too much eclipse stuff to test it here.
		// These are tested in 'testRequestMappings' in BootDashModelTest
	}

	//////////////////////////////////////////////////////////////////

	private String getContents(String resourcePath) throws Exception {
		InputStream input = this.getClass().getResourceAsStream(resourcePath);
		String s = IOUtil.toString(input);
		System.out.println(s);
		return s;
	}

}
