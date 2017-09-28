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
package org.springframework.ide.eclipse.boot.dash.test.actuator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBean;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansGroup;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springframework.ide.eclipse.boot.dash.model.actuator.ActuatorClient;

/**
 * Tests data obtained from the Actuator
 *
 * @author Alex Boyko
 *
 */
public class ActuatorDataTest {

	static class TestActuatorClient extends ActuatorClient {

		private String beansJson = "";
		private String requestMappingsJson = "";

		public TestActuatorClient(TypeLookup workspaceConext) {
			super(workspaceConext);
		}

		public TestActuatorClient beansJson(String json) {
			this.beansJson = json;
			return this;
		}

		public TestActuatorClient requestMappingJson(String json) {
			this.requestMappingsJson = json;
			return this;
		}

		@Override
		protected String getRequestMappingData() throws Exception {
			return requestMappingsJson;
		}

		@Override
		protected String getBeansData() throws Exception {
			return beansJson;
		}

	}

	@Test public void testModelEquality() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(ActuatorClientTest.getContents("beans-sample.json"));
		LiveBeansModel liveBeans = client.getBeans();
		assertEquals(liveBeans, client.getBeans());
	}

	@Test public void testModelIneuality_1() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(ActuatorClientTest.getContents("beans-sample.json"));
		TestActuatorClient otherClient = new TestActuatorClient(null).beansJson(ActuatorClientTest.getContents("beans-sample-diff1.json"));
		assertNotEquals(client.getBeans(), otherClient.getBeans());
	}

	@Test public void testModelContent() throws Exception {
		TestActuatorClient client = new TestActuatorClient(null).beansJson(ActuatorClientTest.getContents("beans-sample.json"));
		LiveBeansModel liveBeans = client.getBeans();
		assertEquals(1, liveBeans.getBeansByContext().size());
		LiveBeansGroup context = liveBeans.getBeansByContext().get(0);
		assertEquals(2, context.getBeans().size());
		assertEquals(2, liveBeans.getBeans().size());
		assertEquals(2, liveBeans.getBeansByResource().size());

		assertEquals(context.getBeans(), liveBeans.getBeans());

		LiveBean bean1 = liveBeans.getBeans().get(0);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration$Jackson2ObjectMapperBuilderCustomizerConfiguration$StandardJackson2ObjectMapperBuilderCustomizer",
				bean1.getBeanType());
		assertEquals("standardJacksonObjectMapperBuilderCustomizer", bean1.getId());

		LiveBean bean2 = liveBeans.getBeans().get(1);
		assertEquals(
				"org.springframework.boot.autoconfigure.jackson.JacksonProperties",
				bean2.getBeanType());
		assertEquals("spring.jackson-org.springframework.boot.autoconfigure.jackson.JacksonProperties", bean2.getId());
	}

}
