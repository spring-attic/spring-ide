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
package org.springframework.ide.eclipse.boot.properties.editor.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.springframework.ide.eclipse.boot.properties.editor.quickfix.MetaDataManipulator;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import junit.framework.TestCase;

public class MetaDataManipulatorTest extends TestCase {

	public static class MockContent implements MetaDataManipulator.ContentStore {

		private String content;
		private String encoding = "utf8";

		public MockContent(String intialContent) {
			this.content = intialContent;
		}

		@Override
		public String toString() {
			return content;
		}

		@Override
		public InputStream getContents() throws Exception {
			return new ByteArrayInputStream(content.getBytes(encoding));
		}

		@Override
		public void setContents(InputStream inputStream) throws Exception {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			IOUtil.pipe(inputStream, bytes);
			content = bytes.toString(encoding);
		}

	}

	public void testAddOneElementFromEmpty() throws Exception {
		MockContent content = new MockContent("");
		MetaDataManipulator md = new MetaDataManipulator(content);

		md.addDefaultInfo("test.property");
		md.save();

		assertEquals(
				"{\"properties\": [{\n" +
				"  \"description\": \"A description for 'test.property'\",\n" +
				"  \"name\": \"test.property\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"}]}",
				//================
				content.toString());

		md.addDefaultInfo("another.property");
		md.save();

		assertEquals(
				"{\"properties\": [\n" +
				"  {\n" +
				"    \"description\": \"A description for 'test.property'\",\n" +
				"    \"name\": \"test.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  },\n" +
				"  {\n" +
				"    \"description\": \"A description for 'another.property'\",\n" +
				"    \"name\": \"another.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  }\n" +
				"]}",
				//================
				content.toString());

		assertTrue(md.isReliable());

	}

	public void testRawContent() throws Exception {
		MockContent content = new MockContent("garbage");
		MetaDataManipulator md = new MetaDataManipulator(content);

		md.addDefaultInfo("test.property");
		md.save();

		assertEquals(
				"garbage{\n" +
				"  \"description\": \"A description for 'test.property'\",\n" +
				"  \"name\": \"test.property\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"}\n",
				//================
				content.toString());

		md.addDefaultInfo("another.property");
		md.save();

		assertEquals(
				"garbage{\n" +
				"  \"description\": \"A description for 'test.property'\",\n" +
				"  \"name\": \"test.property\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"},\n" +
				"{\n" +
				"  \"description\": \"A description for 'another.property'\",\n" +
				"  \"name\": \"another.property\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"}\n",
				//================
				content.toString());

		assertFalse(md.isReliable());
	}

	public void testRawContent2() throws Exception {
		MockContent content = new MockContent(
				//almost correct content, its missing a comma
				"{\"properties\": [\n" +
				"  {\n" +
				"    \"description\": \"A description for 'test.property'\",\n" +
				"    \"name\": \"test.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  }\n" + //missing comma!
				"  {\n" +
				"    \"description\": \"A description for 'another.property'\",\n" +
				"    \"name\": \"another.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  }\n" +
				"]}"
		);
		MetaDataManipulator md = new MetaDataManipulator(content);

		md.addDefaultInfo("foo.bar");
		md.save();

		assertEquals(
				"{\"properties\": [\n" +
				"  {\n" +
				"    \"description\": \"A description for 'test.property'\",\n" +
				"    \"name\": \"test.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  }\n" +
				"  {\n" +
				"    \"description\": \"A description for 'another.property'\",\n" +
				"    \"name\": \"another.property\",\n" +
				"    \"type\": \"java.lang.String\"\n" +
				"  },\n" +
				//TODO: The indentation is off... maybe this could be fixed
				"{\n" +
				"  \"description\": \"A description for 'foo.bar'\",\n" +
				"  \"name\": \"foo.bar\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"}\n" +
				"]}",
				//================
				content.toString());

		assertFalse(md.isReliable());
	}

	public void testDisallowRawContent() throws Exception {
		MockContent content;
		MetaDataManipulator md;

		// empty files can be reliabley manipulated?
		content = new MockContent("");
		md = new MetaDataManipulator(content);

		md.addDefaultInfo("test.property");
		md.save();

		assertEquals(
				"{\"properties\": [{\n" +
				"  \"description\": \"A description for 'test.property'\",\n" +
				"  \"name\": \"test.property\",\n" +
				"  \"type\": \"java.lang.String\"\n" +
				"}]}",
				//================
				content.toString());

	}

}
