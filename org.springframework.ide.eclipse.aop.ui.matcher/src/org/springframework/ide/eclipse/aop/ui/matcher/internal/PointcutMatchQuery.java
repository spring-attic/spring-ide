/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.ui.matcher.internal;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.text.Match;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReference;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AspectDefinitionMatcher;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.aop.ui.matcher.PointcutMatcherPlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.util.Assert;

/**
 * {@link ISearchQuery} implementation matches pointcut expression on {@link IBeansConfig}s that are contained in the
 * given scope.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PointcutMatchQuery implements ISearchQuery {

	private static class PointcutDefinition implements IAspectDefinition {

		private final boolean isProxyTargetClass;

		private final String expression;

		public PointcutDefinition(boolean isProxyTargetClass, String expression) {
			this.isProxyTargetClass = isProxyTargetClass;
			this.expression = expression;
		}

		public Method getAdviceMethod() {
			return null;
		}

		public String getAdviceMethodName() {
			return null;
		}

		public String[] getAdviceMethodParameterTypes() {
			return null;
		}

		public String[] getArgNames() {
			return null;
		}

		public String getAspectClassName() {
			return null;
		}

		public int getAspectEndLineNumber() {
			return 0;
		}

		public String getAspectName() {
			return null;
		}

		public int getAspectStartLineNumber() {
			return 0;
		}

		public String getPointcutExpression() {
			return expression;
		}

		public IResource getResource() {
			return null;
		}

		public String getReturning() {
			return null;
		}

		public String getThrowing() {
			return null;
		}

		public ADVICE_TYPE getType() {
			return null;
		}

		public boolean isProxyTargetClass() {
			return isProxyTargetClass;
		}

		public void setResource(IResource file) {

		}

	}

	private PointcutMatcherScope scope;

	private String expression;

	private ISearchResult result;

	private boolean isProxyTragetClass;

	public PointcutMatchQuery(PointcutMatcherScope scope, String pattern, boolean isProxyTragetClass) {
		Assert.notNull(scope);
		this.scope = scope;
		this.expression = pattern;
		this.isProxyTragetClass = isProxyTragetClass;
	}

	public boolean canRerun() {
		return true;
	}

	public boolean canRunInBackground() {
		return true;
	}

	private IProjectClassLoaderSupport getClassLoaderSupport(IProject project) {
		return JdtUtils.getProjectClassLoaderSupport(project, null);
	}

	public String getExpression() {
		return expression;
	}

	public String getLabel() {
		return PointcutMatcherMessages.MatcherQuery_label;
	}

	private Set<IAopReference> getMatches(Set<IBean> beans, IAspectDefinition definition,
			AspectDefinitionMatcher matcher) {
		Set<IAopReference> references = new HashSet<IAopReference>();
		for (IBean bean : beans) {
			IProject project = bean.getElementResource().getProject();
			if (!bean.isInfrastructure()) {
				String className = BeansModelUtils.getBeanClass(bean, null);
				try {
					Class<?> targetClass = ClassUtils.loadClass(className);
					Set<IMethod> matchingMethods = matcher.matches(targetClass, bean, definition, project);
					for (IMethod method : matchingMethods) {
						IAopReference ref = new AopReference(definition.getType(), null, -1, method, JdtUtils
								.getLineNumber(method), definition, bean.getElementResource(), bean);
						references.add(ref);
					}
				}
				catch (Throwable e) {
				}
			}
		}
		return references;
	}

	public PointcutMatcherScope getScope() {
		return scope;
	}

	public final ISearchResult getSearchResult() {
		if (result == null) {
			result = new PointcutMatcherResult(this);
		}
		return result;
	}

	public final IStatus run(final IProgressMonitor monitor) {
		final PointcutMatcherResult result = (PointcutMatcherResult) getSearchResult();
		result.removeAll();

		final IAspectDefinition definition = new PointcutDefinition(isProxyTragetClass, expression);

		for (final IModelElement element : scope.getModelElements()) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			final IModelElementVisitor visitor = new IModelElementVisitor() {
				public boolean visit(final IModelElement element, IProgressMonitor monitor) {

					if (element instanceof IBeansConfig) {
						final IBeansConfig config = (IBeansConfig) element;

						monitor.beginTask("Matching pointcut in file ["
								+ config.getElementResource().getProjectRelativePath().toString() + "]", 100);

						IProject project = config.getElementResource().getProject();
						try {
							// get beans before messing around with the class
							// loader
							final Set<IBean> beans = config.getBeans();

							getClassLoaderSupport(project).executeCallback(
									new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {
										public void doWithActiveProjectClassLoader() throws Throwable {
											for (IAopReference reference : getMatches(beans, definition,
													new AspectDefinitionMatcher())) {
												Match match = new Match(reference, Match.UNIT_LINE, -1, -1);
												result.addMatch(match);
											}
										}
									});
						}
						catch (Throwable e) {
						}
						return false;
					}
					return true;
				}
			};
			element.accept(visitor, monitor);
		}

		Object[] args = new Object[] { new Integer(result.getMatchCount()) };
		String message = MessageUtils.format(PointcutMatcherMessages.MatcherQuery_status, args);
		return new Status(IStatus.OK, PointcutMatcherPlugin.PLUGIN_ID, 0, message, null);
	}
}
