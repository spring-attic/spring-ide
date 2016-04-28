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
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderFactory;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.editor.support.util.FuzzyMatcher;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;

/**
 * Provides the algorithm for 'logger-name' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 */
public class LoggerNameProvider implements ValueProviderStrategy {

//	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

//	private static void debug(String string) {
//		if (DEBUG) {
//			System.out.println(string);
//		}
//	}

	/**
	 * Content assist is called inside UI thread and so doing something lenghty like
	 * a JavaSearch will block the UI thread completely freezing the UI. So, we
	 * only return as many results as can be obtained within this TIMEOUT limit.
	 * <p>
	 * Note: making this public isn't a nice way to make this timeout configurable.
	 * This is just a 'quick' hack to make it possible for test code to change this.
	 */
	public static long TIMEOUT = 300;

	private static final long DEFAULT_TIMEOUT = 300;

	/**
	 * Testing code that changes the TIMEOUT should probably also
	 * call this to restore its default value.
	 */
	public static void restoreDefaults() {
		TIMEOUT = DEFAULT_TIMEOUT;
	}

	private static class ResultsCollector extends SearchRequestor {

		private String query;
		private Map<String, ValueHint> found = new HashMap<>();
		private Collection<ValueHint> foundValues = null;

		public ResultsCollector(String query) {
			this.query = query;
		}

		@Override
		public synchronized void acceptSearchMatch(SearchMatch match) throws CoreException {
			Object element = match.getElement();
			if (element instanceof IType) {
				IType type = (IType) element;
				acceptName(type.getFullyQualifiedName());
			} else if (element instanceof IPackageFragment) {
				IPackageFragment pkg = (IPackageFragment) element;
				acceptName(pkg.getElementName());
			}
		}

		private void acceptName(String fqName) {
			if (found!=null && 0!=FuzzyMatcher.matchScore(query, fqName) && !found.containsKey(fqName)) {
//				debug("found: "+fqName);
				found.put(fqName, hint(fqName));
			}
		}

		public synchronized Collection<ValueHint> getHints() {
			//IMPORTANT: Take care to avoid ConcurrentModificationException!
			//The searchJob, while it is canceled on a timeout, doesn't stop immediately
			// so it may still 'accept' results after we retrieve / return the
			// hints. We avoid problems by setting 'found' to null, the 'accept' method
			// checks for this and ignores further results.
			if (foundValues==null) {
				foundValues = found.values();
				found = null;
			}
			return foundValues;
		}
	}

	private  static final ValueProviderStrategy INSTANCE = new LoggerNameProvider();

	public static final ValueProviderFactory FACTORY = new ValueProviderFactory() {
		@Override
		public ValueProviderStrategy create(Map<String, Object> params) {
			return INSTANCE;
		}
	};


	@Override
	public Collection<ValueHint> getValues(IJavaProject javaProject, String query) {
		if (javaProject!=null) {
			return search(javaProject, query);
		}
		return hints(ImmutableSet.<String>of());
	}

	protected Collection<ValueHint> search(final IJavaProject javaProject, final String query) {
		//TODO: it feels like code in here is a bit too 'tangled':
		//  The logic that determines what to search for is tangled up with the mechanics to ensure the
		//  search returns within a reasonable time and doesn't block the UI thread for too long.
		//Other providers are probably going to want to reuse the timeout mechanics but provide their own
		// search logic. So this needs to be separated.
		final ResultsCollector resultsCollector = new ResultsCollector(query);
		final CompletableFuture<Collection<ValueHint>> future = new CompletableFuture<>();
		Job searchJob = new Job("Seacher for "+this.getClass().getSimpleName()) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
//				long start = System.currentTimeMillis();
//				debug("Starting search for '"+query+"'");
				try {
					SearchEngine search = new SearchEngine();
					IJavaSearchScope scope = searchScopeFor(javaProject);

					SearchPattern pat = toPattern(query);
					search.search(pat, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant()}, scope, resultsCollector, monitor);
//					long duration = System.currentTimeMillis() - start;
					Collection<ValueHint> results = resultsCollector.getHints();
//					debug("Finished search for '"+query+"'");
//					debug("           results:"+results.size());
//					debug("          duration: "+duration+" ms");
					future.complete(results);
					return Status.OK_STATUS;
				} catch (CoreException e) {
//					debug("Canceled search for: "+query);
//					debug("          exception: "+ExceptionUtil.getMessage(e));
					Collection<ValueHint> results = resultsCollector.getHints();
//					long duration = System.currentTimeMillis() - start;
//					debug("           results:"+results.size());
//					debug("          duration: "+duration+" ms");
					future.complete(results);
					future.complete(resultsCollector.getHints());
					return e.getStatus();
				}
			}
			@Override
			public boolean belongsTo(Object family) {
				return family == LoggerNameProvider.this;
			}
		};
		try {
			searchJob.setSystem(true);
			searchJob.setPriority(Job.INTERACTIVE);
			searchJob.schedule();
			return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
//			debug("Search for '"+query+"' TIMEOUT");
			//more or less expected so do not log
		} catch (Exception e) {
			BootActivator.log(e);
		} finally {
			//not all paths exiting the try block imply that searchJob is completed.
			// Whatever the case may be, we should not let it run on wasting CPU
			// since we aren't going to wait any longer for more results.
//			debug("Canceling Search for '"+query+"'");
			searchJob.cancel();
		}
		//'Abnormal' exit. Either the search was slow and time out, or some error caused it to fail.
		// Return whatever we already have found already (which may be nothing)
		return resultsCollector.getHints();
	}

	private String toWildCardPattern(String query) {
		StringBuilder builder = new StringBuilder("*");
		for (char c : query.toCharArray()) {
			builder.append(c);
			builder.append('*');
		}
		return builder.toString();
	}

	private IJavaSearchScope searchScopeFor(IJavaProject javaProject) throws JavaModelException {
//		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
//		List<IPackageFragmentRoot> interestingRoots = new ArrayList<>(roots.length);
//		for (IPackageFragmentRoot r : roots) {
//
//		}
		int includeMask =
				IJavaSearchScope.APPLICATION_LIBRARIES |
				IJavaSearchScope.REFERENCED_PROJECTS |
				IJavaSearchScope.SOURCES;
		return SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject}, includeMask);
	}

	protected SearchPattern toPattern(String query) {
		String wildCardedQuery = toWildCardPattern(query);
		return SearchPattern.createOrPattern(
				toTypePattern(wildCardedQuery),
				toPackagePattern(wildCardedQuery)
		);
	}

	private SearchPattern toPackagePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.PACKAGE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	private SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}


	private static Collection<ValueHint> hints(Collection<String> stringValues) {
		Builder<ValueHint> builder = ImmutableList.builder();
		for (String string : stringValues) {
			builder.add(hint(string));
		}
		return builder.build();
	}

	private static ValueHint hint(String fqName) {
		ValueHint h = new ValueHint();
		h.setValue(fqName);
		return h;
	}

}
