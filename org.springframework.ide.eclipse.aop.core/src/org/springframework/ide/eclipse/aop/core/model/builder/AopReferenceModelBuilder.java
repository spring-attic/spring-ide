/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReference;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AspectDefinitionBuilderFactory;
import org.springframework.ide.eclipse.aop.core.internal.model.builder.AspectDefinitionMatcher;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;

/**
 * Handles creation and modification of the {@link AopReferenceModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelBuilder implements IWorkspaceRunnable {

	private static final String PROCESSING_TOOK_MSG = "Processing took";

	private Set<IResource> affectedResources;

	private IProjectClassLoaderSupport classLoaderSupport;

	public AopReferenceModelBuilder(Set<IResource> affectedResources) {
		this.affectedResources = affectedResources;
	}

	/**
	 * Handles the creation of the AOP reference model
	 * @param monitor the progressMonitor
	 * @param filesToBuild the files to build the model from
	 */
	protected void buildAopModel(IProgressMonitor monitor) {
		AopLog.logStart(PROCESSING_TOOK_MSG);
		AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
				"AopReferenceModelBuilder.startBuildReferenceModel", affectedResources.size()));

		int worked = 0;
		for (IResource currentResource : affectedResources) {
			if (currentResource instanceof IFile) {
				IFile currentFile = (IFile) currentResource;

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}

				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.buildingAopReferenceModel", currentFile
								.getFullPath().toString()));
				monitor.subTask(Activator.getFormattedMessage(
						"AopReferenceModelBuilder.buildingAopReferenceModel", currentFile
								.getFullPath().toString()));

				AopReferenceModelMarkerUtils.deleteProblemMarkers(currentFile);
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator
						.getFormattedMessage("AopReferenceModelBuilder.deletedProblemMarkers"));

				IAopProject aopProject = buildAopReferencesFromFile(currentFile, monitor);
				AopLog.log(AopLog.BUILDER_MESSAGES,	Activator
						.getFormattedMessage("AopReferenceModelBuilder.constructedAopReferenceModel"));

				if (aopProject != null) {
					List<IAopReference> references = aopProject.getAllReferences();
					for (IAopReference reference : references) {
						if (reference.getDefinition().getResource().equals(currentFile)
								|| reference.getResource().equals(currentFile)) {
							AopReferenceModelMarkerUtils.createMarker(reference, currentFile);
						}
					}
				}
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator
						.getFormattedMessage("AopReferenceModelBuilder.createdProblemMarkers"));
				worked++;
				monitor.worked(worked);
				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.doneBuildingReferenceModel", currentFile
								.getFullPath().toString()));

			}
		}
		AopLog.logEnd(AopLog.BUILDER, PROCESSING_TOOK_MSG);
		// update images and text decoractions
		Activator.getModel().fireModelChanged();
	}

	/**
	 * Builds AOP refererences for given {@link IBean} instances. Matches the
	 * given Aspect definition against the {@link IBean}.
	 */
	private void buildAopReferencesForBean(final IBean bean, final IModelElement context,
			final IAspectDefinition info, final IResource file, final IAopProject aopProject,
			final AspectDefinitionMatcher matcher, IProgressMonitor monitor) {
		try {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.processingBeanDefinition", bean, bean
							.getElementResource().getFullPath()));

			// check if bean is abstract
			if (bean.isAbstract()) {
				return;
			}

			final String className = BeansModelUtils.getBeanClass(bean, context);
			if (className != null && info.getAspectName() != null
					&& info.getAspectName().equals(bean.getElementName())
					&& info.getResource() != null
					&& info.getResource().equals(bean.getElementResource())) {
				// don't check advice backing bean itself
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.skippingBeanDefinition", bean));
				return;
			}

			final IType jdtTargetType = JdtUtils.getJavaType(file.getProject(), className);
			final IType jdtAspectType = JdtUtils.getJavaType(aopProject.getProject()
					.getProject(), info.getAspectClassName());

			// check type not found and exclude factory beans
			if (jdtTargetType == null
					|| Introspector.doesImplement(jdtTargetType, FactoryBean.class.getName())) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.skippingFactoryBeanDefinition", bean));
				return;
			}

			// do in context of active weaving class loader
			this.classLoaderSupport
					.executeCallback(
							new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

						public void doWithActiveProjectClassLoader() throws Throwable {
							Class<?> targetClass = ClassUtils.loadClass(className);
							// handle introductions first
							if (info instanceof BeanIntroductionDefinition) {
								BeanIntroductionDefinition intro = (BeanIntroductionDefinition) info;
								if (intro.getTypeMatcher().matches(targetClass)) {
									IMember jdtAspectMember = null;
									if (intro instanceof AnnotationIntroductionDefinition) {
										String fieldName = ((AnnotationIntroductionDefinition) intro)
												.getDefiningField();
										jdtAspectMember = jdtAspectType.getField(fieldName);
									}
									else {
										jdtAspectMember = jdtAspectType;
									}

									if (jdtAspectMember != null) {
										IAopReference ref = new AopReference(info.getType(),
												jdtAspectMember, jdtTargetType, info, file, bean);
										aopProject.addAopReference(ref);
									}
								}
							}
							else if (info instanceof BeanAspectDefinition) {
								IMethod jdtAspectMethod = null;

								if (info instanceof JavaAdvisorDefinition) {
									jdtAspectMethod = JdtUtils.getMethod(jdtAspectType, info
											.getAdviceMethodName(), info
											.getAdviceMethodParameterTypes());
								}
								else {
									// validate the aspect definition
									if (info.getAdviceMethod() == null) {
										return;
									}
									jdtAspectMethod = JdtUtils.getMethod(jdtAspectType, info
											.getAdviceMethodName(), info.getAdviceMethod()
											.getParameterTypes());
								}

								if (jdtAspectMethod != null) {

									Set<IMethod> matchingMethods = matcher.matches(targetClass,
											bean.getElementName(), info, aopProject.getProject()
													.getProject());
									for (IMethod method : matchingMethods) {
										IAopReference ref = new AopReference(info.getType(),
												jdtAspectMethod, method, info, file, bean);
										aopProject.addAopReference(ref);
									}

								}
							}
						}
					});
		}
		catch (Throwable t) {
			handleException(t, info, bean, file);
		}
	}

	private void buildAopReferencesFromAspectDefinition(IBeansConfig config,
			IAspectDefinition info, AspectDefinitionMatcher builderUtils, IProgressMonitor monitor) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopReferenceModel) Activator.getModel())
				.getProjectWithInitialization(JdtUtils.getJavaProject(info.getResource()
						.getProject()));

		Set<IBean> beans = new LinkedHashSet<IBean>();
		beans.addAll(config.getBeans());
		
		// add component registered beans
		// TODO CD consider adding components as potential weaving candidates
		/*for (IBeansComponent component : config.getComponents()) {
			beans.addAll(component.getBeans());
		}*/
		
		buildAopReferencesFromAspectDefinitionForBeans(config, info, builderUtils, monitor, file,
				aopProject, beans);
	}

	private void buildAopReferencesFromAspectDefinitionForBeans(IModelElement config,
			IAspectDefinition info, AspectDefinitionMatcher matcher, IProgressMonitor monitor,
			IResource file, IAopProject aopProject, Set<IBean> beans) {
		SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor,
				IProgressMonitor.UNKNOWN);

		subProgressMonitor.beginTask(Activator
				.getFormattedMessage("AopReferenceModelBuilder.buildingAopReferences"), beans
				.size());
		int worked = 0;
		try {
			for (IBean bean : beans) {
				subProgressMonitor.subTask(Activator.getFormattedMessage(
						"AopReferenceModelBuilder.buildingAopReferencesForBean", bean
								.getElementName(), bean.getElementResource().getFullPath()));
				buildAopReferencesForBean(bean, config, info, file, aopProject, matcher,
						monitor);
				
				// Make sure that inner beans are handled as well
				buildAopReferencesFromAspectDefinitionForBeans(config, info, matcher, monitor, file,
						aopProject, BeansModelUtils.getInnerBeans(bean));
				
				subProgressMonitor.worked(worked++);
			}
		}
		finally {
			subProgressMonitor.done();
		}
	}

	private void buildAopReferencesFromBeansConfigSets(IBeansProject project, IBeansConfig config,
			IAspectDefinition info, AspectDefinitionMatcher builderUtils, IProgressMonitor monitor) {
		// check config sets as well
		Set<IBeansConfigSet> configSets = project.getConfigSets();
		for (IBeansConfigSet configSet : configSets) {
			if (configSet.getConfigs().contains(config)) {
				Set<IBeansConfig> configs = configSet.getConfigs();
				for (IBeansConfig configSetConfig : configs) {
					if (!config.equals(configSetConfig)) {
						buildAopReferencesFromAspectDefinition(configSetConfig, info, builderUtils,
								monitor);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private IAopProject buildAopReferencesFromFile(IFile currentFile, IProgressMonitor monitor) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(currentFile.getProject());

		if (project != null) {
			IBeansConfig config = project.getConfig(currentFile);
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			if (javaProject != null) {
				aopProject = ((AopReferenceModel) Activator.getModel())
						.getProjectWithInitialization(javaProject);

				aopProject.clearReferencesForResource(currentFile);

				// prepare class loaders
				this.classLoaderSupport = createWeavingClassLoaderSupport(javaProject);

				AopLog.log(AopLog.BUILDER_CLASSPATH, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.aopBuilderClassPath", StringUtils
								.arrayToDelimitedString(((URLClassLoader) classLoaderSupport
										.getProjectClassLoader()).getURLs(), ";")));

				List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();
				Set<IAspectDefinitionBuilder> builders = AspectDefinitionBuilderFactory
						.getAspectDefinitionBuilder();
				for (IAspectDefinitionBuilder builder : builders) {
					aspectInfos.addAll(builder.buildAspectDefinitions(currentFile,
							this.classLoaderSupport));
				}

				if (aspectInfos != null) {
					
					// remove references for all definitions
					for (IAspectDefinition info : aspectInfos) {
						aopProject.clearReferencesForResource(info.getResource());
					}
					
					AspectDefinitionMatcher builderUtils = new AspectDefinitionMatcher();
					for (IAspectDefinition info : aspectInfos) {

						// build model for config
						buildAopReferencesFromAspectDefinition(config, info, builderUtils, monitor);

						// build model for config sets
						buildAopReferencesFromBeansConfigSets(project, config, info, builderUtils,
								monitor);
					}
				}
			}
		}
		return aopProject;
	}

	protected IProjectClassLoaderSupport createWeavingClassLoaderSupport(IJavaProject javaProject) {
		return JdtUtils.getProjectClassLoaderSupport(javaProject);
	}

	private void handleException(Throwable t, IAspectDefinition info, IBean bean, IResource file) {
		if (t instanceof NoClassDefFoundError || t instanceof ClassNotFoundException) {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.classDependencyError", t.getMessage(), info, bean));
			AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.buildPathIncomplete", t.getMessage()),
					IMarker.SEVERITY_ERROR, bean.getElementStartLine(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
		else if (t instanceof IllegalArgumentException) {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.pointcutIsMalformedOnBean", info, bean));
			AopReferenceModelMarkerUtils.createProblemMarker(info.getResource(), Activator.getFormattedMessage(
					"AopReferenceModelBuilder.pointcutIsMalformed", t.getMessage()),
					IMarker.SEVERITY_ERROR, info.getAspectLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, info.getResource());
		}
		else if (t instanceof InvocationTargetException) {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.exceptionFromReflectionOnBean", info, bean));
			if (t.getCause() != null) {
				handleException(t.getCause(), info, bean, file);
			}
			else {
				Activator.log(t);
				AopReferenceModelMarkerUtils.createProblemMarker(file, Activator
						.getFormattedMessage("AopReferenceModelBuilder.exceptionFromReflection", t
								.getMessage()), IMarker.SEVERITY_WARNING, info
						.getAspectLineNumber(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER,
						file);
			}
		}
		else {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.exception", t.getMessage(), info, bean));
			Activator.log(t);
			AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.exception", t.getMessage()),
					IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
	}

	/**
	 * Implementation for {@link IWorkspaceRunnable#run(IProgressMonitor)}
	 * method that triggers the building of the aop reference model.
	 */
	public void run(IProgressMonitor monitor) throws CoreException {
		try {
			if (!monitor.isCanceled()) {
				this.buildAopModel(monitor);
			}
		}
		finally {
			affectedResources = null;
		}
	}
}
