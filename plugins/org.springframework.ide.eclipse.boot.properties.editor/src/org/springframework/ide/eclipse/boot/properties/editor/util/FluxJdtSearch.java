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
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.util.EnumSet;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.subscriber.SignalEmitter;
import reactor.core.subscriber.SignalEmitter.Emission;
import reactor.core.util.BackpressureUtils;
import reactor.core.util.PlatformDependent;

import static reactor.core.subscriber.SignalEmitter.Emission.*;

/**
 * Helper class to perform a search using Eclipse JDT search engine returning
 * the search results as a Flux.
 * <p>
 * The conversion from Eclipse callback style using {@link SearchRequestor} involves
 * a buffer that allows subscribers to attach to the Flux after the search has already
 * started without loosing results. However, if the buffer overflows then results will
 * be lost.
 * <p>
 * Clients should therfore start consuming the results as soon as possible and avoid
 * blocking the pipeline to avoid the loss of results they may care about.
 * <p>
 * Alternatively, client can specify a large enough buffer size so that the buffer can hold
 * at least as many results as the client may care to retrieve. This will allow the returned
 * Flux to be reused any number of times without timing constraints, provided that the
 * consumer never requests more than the number of buffered results.
 *
 * @author Kris De Volder
 */
public class FluxJdtSearch {

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private SearchEngine engine = new SearchEngine();
	private IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	private SearchPattern pattern = null;
	private SearchParticipant[] participants = new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
	private int bufferSize = PlatformDependent.SMALL_BUFFER_SIZE;
	private boolean useSystemJob = false;
	private int jobPriority = Job.INTERACTIVE;

	public FluxJdtSearch engine(SearchEngine engine) {
		this.engine = engine;
		return this;
	}

	public FluxJdtSearch scope(IJavaSearchScope scope) {
		this.scope = scope;
		return this;
	}

	public FluxJdtSearch scope(IJavaProject project) throws JavaModelException {
		return scope(searchScope(project));
	}

	public FluxJdtSearch bufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
		return this;
	}

	public FluxJdtSearch pattern(SearchPattern pattern) {
		this.pattern = pattern;
		return this;
	}

	/**
	 * Create a search scope that includes a given project and its dependencies.
	 */
	public static IJavaSearchScope searchScope(IJavaProject javaProject) throws JavaModelException {
		int includeMask =
				IJavaSearchScope.APPLICATION_LIBRARIES |
				IJavaSearchScope.REFERENCED_PROJECTS |
				IJavaSearchScope.SOURCES;
		return SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject}, includeMask);
	}

	/**
	 * Implementation of {@link SearchRequestor} that emits search results to an {@link EmitterProcessor}
	 * with replay capability.
	 *
	 * @author Kris De Volder
	 */
	class FluxSearchRequestor extends SearchRequestor {

		private boolean isCanceled = false;
		private EmitterProcessor<SearchMatch> emitter = EmitterProcessor.<SearchMatch>replay(bufferSize).connect();
		private Flux<SearchMatch> flux = emitter.doOnCancel(() -> isCanceled=true);

		public Flux<SearchMatch> asFlux() {
			return flux;
		}

		@Override
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			if (isCanceled) {
				debug("!!!! canceling search !!!!");
				//Stop searching
				throw new OperationCanceledException();
			}
			emitter.onNext(match);
		}

		public void cancel() {
			isCanceled = true;
		}

		public void done() {
			emitter.onComplete();
		}
	}

	protected SearchEngine searchEngine() {
		return new SearchEngine();
	}

	protected SearchParticipant[] participants() {
		return new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()};
	}

	public Flux<SearchMatch> search() {
		validate();
		FluxSearchRequestor requestor = new FluxSearchRequestor();
		Job job = new Job("Search for "+pattern) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				long start = System.currentTimeMillis();
				debug("Starting search for '"+pattern+"'");
				try {
					searchEngine().search(pattern, participants, scope, requestor, monitor);
					requestor.done();
				} catch (Exception e) {
					debug("Canceled search for: "+pattern);
					debug("          exception: "+ExceptionUtil.getMessage(e));
					long duration = System.currentTimeMillis() - start;
					debug("          duration: "+duration+" ms");
					requestor.cancel();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(useSystemJob);
		job.setPriority(jobPriority);
		job.schedule();
		return requestor.asFlux();
	}

	private void validate() {
		Assert.isNotNull(engine, "engine");
		Assert.isNotNull(scope, "scope");
		Assert.isNotNull(pattern, "pattern");
		Assert.isNotNull(participants, "pattern");
		Assert.isLegal(bufferSize > 0);
	}

}
