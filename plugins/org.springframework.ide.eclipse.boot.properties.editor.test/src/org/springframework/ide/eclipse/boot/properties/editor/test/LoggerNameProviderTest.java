/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.CachingValueProvider;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.LoggerNameProvider;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableSet;

/**
 * @author Kris De Volder
 */
public class LoggerNameProviderTest {

	private static final String[] JBOSS_RESULTS = {
			"com.fasterxml.jackson.databind.jsonFormatVisitors", //1
			"org.jboss", //2
			"org.jboss.logging", //3
			"org.jboss.logging.JBossLogManagerLogger", //4
			"org.jboss.logging.JBossLogManagerProvider", //5
			"org.jboss.logging.JBossLogRecord", //6
			"org.springframework.instrument.classloading.jboss", //7
			"org.springframework.instrument.classloading.jboss.JBossClassLoaderAdapter", //8
			"org.springframework.instrument.classloading.jboss.JBossLoadTimeWeaver", //9
			"org.springframework.instrument.classloading.jboss.JBossMCAdapter", //10
			"org.springframework.instrument.classloading.jboss.JBossMCTranslatorAdapter", //11
			"org.springframework.instrument.classloading.jboss.JBossModulesAdapter" //12
	};
	BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	private IJavaProject project;

	@Before
	public void setup() throws Exception {
		StsTestUtil.deleteAllProjects();
		CachingValueProvider.TIMEOUT = Duration.ofSeconds(20);
		project = JavaCore.create(createPredefinedMavenProject("demo"));
	}

	@After
	public void teardown() throws Exception {
		CachingValueProvider.restoreDefaults();
	}

	protected IProject createPredefinedMavenProject(final String projectName) throws Exception {
		final String bundleName = getBundleName();
		return BootProjectTestHarness.createPredefinedMavenProject(projectName, bundleName);
	}

	private String getBundleName() {
		return "org.springframework.ide.eclipse.boot.properties.editor.test";
	}

	@Test
	public void directResults() throws Exception {
		LoggerNameProvider p = new LoggerNameProvider();
		String query = "jboss";
		List<String> directQueryResults = getResults(p, query);

		dumpResults("jboss - DIRECT", directQueryResults);

		assertElements(directQueryResults, JBOSS_RESULTS);
	}

	@Test
	public void cachedResults() throws Exception {
		LoggerNameProvider p = new LoggerNameProvider();
		for (int i = 0; i < 10; i++) {
			long startTime = System.currentTimeMillis();
			String query = "jboss";
			List<String> directQueryResults = getResults(p, query);

			assertElements(directQueryResults, JBOSS_RESULTS);

			long duration = System.currentTimeMillis() - startTime;
			System.out.println(i+": "+duration+" ms");
		}
	}

	@Test
	public void incrementalResults() throws Exception {
		String fullQuery = "jboss";

		CachingValueProvider p = new LoggerNameProvider();
		for (int i = 0; i <= fullQuery.length(); i++) {
			String query = fullQuery.substring(0, i);
			List<String> results = getResults(p, query);
			dumpResults(query, results);
			if (i==fullQuery.length()) {
				System.out.println("Verifying final result!");
				//Not checking for exact equals because... quircks of JDT search engine means it
				// will actually finds less results than if we derive them by filtering incrementally.
				//If all works well, we should never find fewer results than Eclipse does.
				assertElementsAtLeast(results, JBOSS_RESULTS);
			}
		}
	}

	private void assertElementsAtLeast(List<String> results, String[] expecteds) {
		Set<String> actuals = ImmutableSet.copyOf(results);
		StringBuilder missing = new StringBuilder();
		boolean hasMissing = false;
		for (String e : expecteds) {
			if (!actuals.contains(e)) {
				missing.append(e+"\n");
				hasMissing = true;
			}
		}
		assertFalse("Missing elements:\n"+missing, hasMissing);
	}

	private void assertElements(List<String> _actual, String... _expected) {
		String expected = toSortedString(Arrays.asList(_expected));
		String actual = toSortedString(_actual);
		assertEquals(expected, actual);
	}

	private String toSortedString(List<String> list) {
		ArrayList<String> sorted = new ArrayList<>(list);
		Collections.sort(sorted);
		StringBuilder buf = new StringBuilder();
		for (String string : sorted) {
			buf.append(string+"\n");
		}
		return buf.toString();
	}

	private List<String> getResults(CachingValueProvider p, String query) {
		return p.getValues(project, query).stream()
		.map((h) -> h.getValue().toString())
		.collect(Collectors.toList());
	}

	private void dumpResults(String string, Collection<String> r) {
		System.out.println(">>> "+string);
		String[] strings = r.toArray(new String[r.size()]);
		Arrays.sort(strings);
		int i = 0;
		for (String s : strings) {
			System.out.println("\""+s+"\", //"+(++i));
//			System.out.println((++i)+" : "+s);
		}
		System.out.println("<<< "+string);
	}

}
