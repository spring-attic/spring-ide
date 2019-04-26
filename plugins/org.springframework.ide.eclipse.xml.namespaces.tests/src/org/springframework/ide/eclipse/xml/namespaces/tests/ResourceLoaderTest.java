/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.ide.eclipse.xml.namespaces.classpath.FilteringURLResourceLoader;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

import com.google.common.collect.ImmutableList;

public class ResourceLoaderTest {
	
	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();
	
	@Test
	public void findResourceInJar() throws Exception {
		File jar = tmp.newFile("somejar.jar");
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(jar))) {
			zip.putNextEntry(new ZipEntry("dir/resource.txt"));
			zip.write("hello".getBytes());
			zip.closeEntry();
		}
		
		FilteringURLResourceLoader loader = new FilteringURLResourceLoader(new URL[] {
				jar.toURL()
		}, null);
		
		URL url = loader.getResource("dir/resource.txt");
		assertEquals("jar:file:"+jar+"!/dir/resource.txt", url.toString());
		
		assertEquals("hello", getResource(loader, "dir/resource.txt"));
	}

	@Test
	public void findResourceInParent() throws Exception {
		File parentJar = createJar("parent.jar", "dir/resource.txt", "parent-data");
		File childJar  = createJar("child.jar", "whatever.txt", "child-data");
		
		FilteringURLResourceLoader parentLoader = new FilteringURLResourceLoader(new URL[] {
				parentJar.toURL()
		}, null);

		FilteringURLResourceLoader loader = new FilteringURLResourceLoader(new URL[] {
				childJar.toURL()
		}, parentLoader);

		
		URL url = loader.getResource("dir/resource.txt");
		assertEquals("jar:file:"+parentJar+"!/dir/resource.txt", url.toString());
		assertEquals("parent-data", getResource(loader, "dir/resource.txt"));
	}

	@Test
	public void resourceInParentTakesPriority() throws Exception {
		File parentJar = createJar("parent.jar", "dir/resource.txt", "parent-data");
		File childJar  = createJar("child.jar", "dir/resource.txt", "child-data");
		
		FilteringURLResourceLoader parentLoader = new FilteringURLResourceLoader(new URL[] {
				parentJar.toURL()
		}, null);

		FilteringURLResourceLoader loader = new FilteringURLResourceLoader(new URL[] {
				childJar.toURL()
		}, parentLoader);

		
		URL url = loader.getResource("dir/resource.txt");
		assertEquals("jar:file:"+parentJar+"!/dir/resource.txt", url.toString());
		assertEquals("parent-data", getResource(loader, "dir/resource.txt"));
		
		List<String> all = loader.getResources("dir/resource.txt").map(Object::toString).collect(Collectors.toList()).block();
		assertEquals(ImmutableList.of(
				"jar:file:"+parentJar+"!/dir/resource.txt",
				"jar:file:"+childJar+"!/dir/resource.txt"
		), all);
	}

	@Test
	public void findResourcesInMultipleJars() throws Exception {
		File jar = createJar("one.jar", "dir/resource.txt", "one-data");
		File otherJar  = createJar("two.jar", "dir/resource.txt", "two-data");
		
		FilteringURLResourceLoader loader = new FilteringURLResourceLoader(new URL[] {
				jar.toURL(), otherJar.toURL()
		}, null);
		
		List<String> all = loader.getResources("dir/resource.txt").map(Object::toString).collect(Collectors.toList()).block();
		assertEquals(ImmutableList.of(
				"jar:file:"+jar+"!/dir/resource.txt",
				"jar:file:"+otherJar+"!/dir/resource.txt"
		), all);
	}
	
	@Test 
	public void findResourceInDir() throws Exception {
		File classpath = tmp.newFolder("dir");
		File rootResource = new File(classpath, "resource.txt");
		File subResource = new File(classpath, "subdir/resource.txt");
		subResource.getParentFile().mkdirs();
		createFile(rootResource, "root-data");
		createFile(subResource, "sub-data");
		FilteringURLResourceLoader loader = new FilteringURLResourceLoader(new URL[] {classpath.toURL()}, null);
		assertEquals(rootResource.toURL().toString(), loader.getResource("resource.txt").toString());
		assertEquals(subResource.toURL().toString(), loader.getResource("subdir/resource.txt").toString());
	}

	private void createFile(File file, String content) throws Exception {
		try (FileWriter out = new FileWriter(file)) {
			out.write(content);
		}
	}

	private File createJar(String jarName, String resourceName, String resourceContent) throws Exception {
		File jar = tmp.newFile(jarName);
		try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(jar))) {
			zip.putNextEntry(new ZipEntry(resourceName));
			zip.write(resourceContent.getBytes());
			zip.closeEntry();
		}
		return jar;
	}

	private String getResource(FilteringURLResourceLoader loader, String name) throws Exception {
		return IOUtil.toString(loader.getResourceAsStream(name));
	}
	
}
