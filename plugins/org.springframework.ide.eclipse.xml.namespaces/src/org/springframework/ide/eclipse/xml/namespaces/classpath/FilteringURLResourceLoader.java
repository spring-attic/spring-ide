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
package org.springframework.ide.eclipse.xml.namespaces.classpath;

import static org.springframework.ide.eclipse.xml.namespaces.classpath.ProjectResourceLoaderCache.shouldFilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.URIUtil;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class FilteringURLResourceLoader implements ResourceLoader {

	private URL[] urls;
	private ResourceLoader parent;
	private Cache<String, Collection<String>> getResourcesCache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).build();

	public FilteringURLResourceLoader(URL[] directories, ResourceLoader parent) {
		this.urls = directories;
		this.parent = parent;
	}
	
	private static AtomicLong timeUsed = new AtomicLong();
	private static AtomicLong request = new AtomicLong();

//	private static Set<String> fetchedResources = new HashSet<>();
	
	@Override
	public URL getResource(String resourceName) {
		try {
			if (!shouldFilter(resourceName)) {
				Collection<String> resources = getResourcesCollection(resourceName);
				if (!resources.isEmpty()) {
					return new URL(resources.iterator().next());
				}
			}
		} catch (Exception e) {
			SpringXmlNamespacesPlugin.log(e);
		}
		return null;
	}

	private Collection<String> getResourcesCollection(String resourceName) {
		long start = System.currentTimeMillis();
		try {
			try {
				return getResourcesCache.get(resourceName, () -> fetchResources(resourceName));
			} catch (ExecutionException e) {
				SpringXmlNamespacesPlugin.log(e);
				return ImmutableList.of();
			}
		} finally {
			long duration = System.currentTimeMillis() - start;
			long total = timeUsed.addAndGet(duration);
			long requestCount = request.incrementAndGet();
			System.out.println("Time spent finding resources:");
			System.out.println("  requests = " + requestCount);
			System.out.println("  avg      = " + total / requestCount);
			System.out.println("  total    = " + total);
		}
	}

	private Collection<String> fetchResources(String resourceName) {
//		synchronized (fetchedResources) {
//			boolean isNew = fetchedResources.add(resourceName);
//			if (isNew) {
//				System.out.println("fecthed-resources =");
//				for (String r : fetchedResources) {
//					System.out.println(r);
//				}
//				System.out.println("--------------------------");
//			}
//		}
		ImmutableSet.Builder<String> resources = ImmutableSet.builder();
		//find in parent
		Stream<URL> parentResources = parent.getResources(resourceName);
		parentResources.forEach(resource -> 
			resources.add(resource.toString())
		);
		//find in our urls
		for (URL url : urls) {
			try {
				if (isZip(url)) {
					fetchResourceFromZip(resourceName, url, resources);
				} else {
					url.getProtocol().equals("file");
					File file = URIUtil.toFile(URIUtil.toURI(url));
					if (file.isDirectory()) {
						fetchResourceFromDirectory(resourceName, file, resources);
					} else {
						fetchResourceFromZip(resourceName, url, resources);
					}
				}
			} catch (Exception e) {
				SpringXmlNamespacesPlugin.log(e);
			}
		}
		return resources.build();
	}

	private void fetchResourceFromDirectory(String resourceName, File file, Builder<String> resources) {
		try {
			Path rootDir = file.toPath();
			FileVisitor<Path> visitor = new FileVisitor<Path>() {
				
				FileVisitResult fvr = FileVisitResult.CONTINUE;
	
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					return fvr;
				}
	
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (attrs.isRegularFile()) {
						String name = rootDir.relativize(file).toString();
						if (name.equals(resourceName)) {
							resources.add(file.toUri().toString());
							fvr = FileVisitResult.TERMINATE;
						}
					}
					return fvr;
				}
	
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return fvr;
				}
	
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					return fvr;
				}
			};
			Files.walkFileTree(rootDir, visitor);
		} catch (IOException e) {
			SpringXmlNamespacesPlugin.log(e);
		}
	}

	private void fetchResourceFromZip(String resourceName, URL url, ImmutableSet.Builder<String> requestor) {
		try {
			try (InputStream input = url.openStream()) {
				ZipInputStream zip = new ZipInputStream(input);
				ZipEntry ze = zip.getNextEntry();
				while (ze!=null) {
					if (resourceName.equals(ze.getName())) {
						//Example url: jar:file:/home/kdvolder/.m2/repository/org/springframework/boot/spring-boot/2.1.4.RELEASE/spring-boot-2.1.4.RELEASE.jar!/META-INF/spring.factories
						System.out.println("FOUND "+resourceName+" in "+url);
						requestor.add("jar:"+url+"!/"+ze);
						return;
//					} else {
//						System.out.println("mismatch: "+ze.getName());
					}
					ze = zip.getNextEntry();
				}
			}
		} catch (Exception e) {
			SpringXmlNamespacesPlugin.log(e);
		}
	}

	private boolean isZip(URL url) {
		String path = url.getPath();
		return path.endsWith(".jar") || path.endsWith(".zip");
	}

	@Override
	public Stream<URL> getResources(String resourceName) {
		if (!shouldFilter(resourceName)) {
			Collection<String> resources = getResourcesCollection(resourceName);
			return resources.stream().flatMap(s -> {
				try {
					return Stream.of(new URL(s));
				} catch (MalformedURLException e) {
					return Stream.empty();
				}
			});
		}
		return Stream.empty();
	}

	@Override
	public InputStream getResourceAsStream(String name) {
		URL url = getResource(name);
		try {
			return url != null ? url.openStream() : null;
		} catch (IOException e) {
			return null;
		}
	}

}
