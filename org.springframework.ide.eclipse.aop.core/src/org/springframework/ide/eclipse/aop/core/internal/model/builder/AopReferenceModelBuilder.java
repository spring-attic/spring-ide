/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.progress.IProgressConstants;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.AopCoreImages;
import org.springframework.ide.eclipse.aop.core.internal.model.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReference;
import org.springframework.ide.eclipse.aop.core.internal.model.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanAspectDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.internal.model.JavaAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
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
public class AopReferenceModelBuilder extends Job {

	private static final String PROCESSING_TOOK_MSG = "Processing took";

	private Set<IResource> affectedResources;

	private Set<IResource> originalResources;

	private IProjectClassLoaderSupport classLoaderSupport;

	
	/**
	 * Constructor to create a {@link AopReferenceModelBuilder} instance.
	 * @param affectedResources the set of resources that should be processed
	 */
	public AopReferenceModelBuilder(Set<IResource> affectedResources, Set<IResource> originalResources) {
		super(Activator.getFormattedMessage("AopReferenceModelProjectBuilder.buildingAopReferenceModel"));
		this.affectedResources = affectedResources;
		this.originalResources = originalResources;
		setPriority(Job.BUILD);
		// make sure that only one Job at a time runs but without blocking the UI
		setRule(new BlockingOnSelfSchedulingRule());
		setProperty(IProgressConstants.ICON_PROPERTY, AopCoreImages.DESC_OBJS_ASPECT);
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
		
		MarkerModifyingJob markerJob = new MarkerModifyingJob();
		monitor.beginTask(Activator.getFormattedMessage(
				"AopReferenceModelBuilder.startBuildingAopReferenceModel"), affectedResources.size());
		try {
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
					
					markerJob.addResource(currentFile);
					IAopProject aopProject = buildAopReferencesForFile(currentFile, monitor);
					AopLog.log(AopLog.BUILDER_MESSAGES,	Activator
							.getFormattedMessage("AopReferenceModelBuilder.constructedAopReferenceModel"));
	
					if (aopProject != null) {
						List<IAopReference> references = aopProject.getAllReferences();
						markerJob.addAopReference(currentResource, references);
					}
	
					worked++;
					monitor.worked(worked);
					AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
							"AopReferenceModelBuilder.doneBuildingReferenceModel", currentFile
									.getFullPath().toString()));
	
				}
			}
			AopLog.logEnd(AopLog.BUILDER, PROCESSING_TOOK_MSG);
			// update images and text decorations
			Activator.getModel().fireModelChanged();
		}
		finally {
			// schedule marker update job
			markerJob.schedule();
		}
		
	}

	/**
	 * Builds AOP references for given {@link IBean} instances. Matches the
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
			final IType jdtAspectType = JdtUtils.getJavaType(aopProject.getProject().getProject(),
					info.getAspectClassName());

			// check type not found and exclude factory beans
			if (jdtTargetType == null
					|| Introspector.doesImplement(jdtTargetType, FactoryBean.class.getName())) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.skippingFactoryBeanDefinition", bean));
				return;
			}

			// do in context of active weaving class loader
			this.classLoaderSupport
					.executeCallback(new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

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
											bean, info, aopProject.getProject().getProject());
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

	private void buildAopReferencesForBeansConfig(IBeansConfig config, IAspectDefinition info,
			AspectDefinitionMatcher matcher, IProgressMonitor monitor) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopReferenceModel) Activator.getModel())
				.getProjectWithInitialization(JdtUtils.getJavaProject(info.getResource()
						.getProject()));

		// add support for imported beans configuration files by extending the
		// scope to the importing beans configuration
		if (config instanceof IImportedBeansConfig) {
			config = BeansModelUtils.getParentOfClass(config, BeansConfig.class);
		}

		Set<IBean> beans = new LinkedHashSet<IBean>();
		beans.addAll(config.getBeans());

		// add component registered beans
		for (IBeansComponent component : config.getComponents()) {
			Set<IBean> nestedBeans = component.getBeans();
			for (IBean nestedBean : nestedBeans) {
				if (!nestedBean.isInfrastructure()) {
					beans.add(nestedBean);
				}
			}
		}

		buildAopReferencesForBeans(config, info, matcher, monitor, file, aopProject, beans);
	}

	private void buildAopReferencesForBeans(IModelElement config, IAspectDefinition info,
			AspectDefinitionMatcher matcher, IProgressMonitor monitor, IResource file,
			IAopProject aopProject, Set<IBean> beans) {

		monitor.subTask(Activator
				.getFormattedMessage("AopReferenceModelBuilder.buildingAopReferences"));

		for (IBean bean : beans) {
			monitor.subTask(Activator.getFormattedMessage(
					"AopReferenceModelBuilder.buildingAopReferencesForBean", bean.getElementName(),
					bean.getElementResource().getFullPath()));
			buildAopReferencesForBean(bean, config, info, file, aopProject, matcher, monitor);

			// Make sure that inner beans are handled as well
			buildAopReferencesForBeans(config, info, matcher, monitor, file, aopProject,
					BeansModelUtils.getInnerBeans(bean));
		}
	}

	/**
	 * Iterates over the list of {@link IBeansConfigSet} to determine if the
	 * given <code>config</code> is part of a certain config set and as such
	 * the {@link IAspectDefinition} need to be matched against the beans
	 * contained in the config set.
	 */
	private void buildAopReferencesFromBeansConfigSets(IBeansProject project, IBeansConfig config,
			IAspectDefinition info, AspectDefinitionMatcher matcher, IProgressMonitor monitor) {
		if (this.originalResources.contains(config.getElementResource())) {
			for (IBeansConfigSet configSet : project.getConfigSets()) {
				if 	(configSet.getConfigs().contains(config)) {
					Set<IBeansConfig> configs = configSet.getConfigs();
					for (IBeansConfig configSetConfig : configs) {
						if (!config.equals(configSetConfig)) {
							buildAopReferencesForBeansConfig(configSetConfig, info, matcher, monitor);
						}
					}
				}
			}
		}
	}

	private IAopProject buildAopReferencesForFile(IFile currentFile, IProgressMonitor monitor) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(currentFile.getProject());

		if (project != null) {
			IBeansConfig config = project.getConfig(currentFile, true);
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			if (javaProject != null) {
				aopProject = ((AopReferenceModel) Activator.getModel())
						.getProjectWithInitialization(javaProject);

				aopProject.clearReferencesForResource(currentFile);

				// prepare class loaders
				this.classLoaderSupport = createWeavingClassLoaderSupport(project.getProject());

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

					AspectDefinitionMatcher matcher = new AspectDefinitionMatcher();
					for (IAspectDefinition info : aspectInfos) {

						// build model for config
						buildAopReferencesForBeansConfig(config, info, matcher, monitor);

						// build model for config sets
						buildAopReferencesFromBeansConfigSets(project, config, info, matcher,
								monitor);
					}
				}
			}
		}
		return aopProject;
	}

	/**
	 * Template method to create a {@link IProjectClassLoaderSupport} instance.
	 * <p>
	 * This implementation simply calls
	 * {@link JdtUtils#getProjectClassLoaderSupport(IProject)}
	 */
	protected IProjectClassLoaderSupport createWeavingClassLoaderSupport(IProject project) {
		return JdtUtils.getProjectClassLoaderSupport(project);
	}

	/**
	 * Handles any exception that might come up during parsing and matching of
	 * pointcuts.
	 */
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
			AopReferenceModelMarkerUtils.createProblemMarker(info.getResource(), Activator
					.getFormattedMessage("AopReferenceModelBuilder.pointcutIsMalformed", t
							.getMessage()), IMarker.SEVERITY_ERROR,
					info.getAspectStartLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, info.getResource());
		}
		else if (t instanceof InvocationTargetException || t instanceof RuntimeException) {
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
						.getAspectStartLineNumber(),
						AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
			}
		}
		else {
			AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.exception", t.getMessage(), info, bean));
			Activator.log(t);
			AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
					"AopReferenceModelBuilder.exception", t.getMessage()),
					IMarker.SEVERITY_WARNING, info.getAspectStartLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
	}

	/**
	 * Implementation for {@link IWorkspaceRunnable#run(IProgressMonitor)}
	 * method that triggers the building of the aop reference model.
	 */
	public IStatus run(IProgressMonitor monitor) {
		try {
			if (!monitor.isCanceled()) {
				this.buildAopModel(monitor);
			}
			else {
				return Status.CANCEL_STATUS;
			}
		}
		finally {
			affectedResources = null;
		}
		return Status.OK_STATUS;
	}
	
	
	/**
	 * {@link Job} implementation that handles deletion and creation of markers
	 * for the aop reference model.
	 * <p>
	 * This Job schedules itself with a workspace scheduling rule; meaning the
	 * workspace will be locked for modifications while this jobs runs.
	 * @since 2.0.4
	 */
	private class MarkerModifyingJob extends Job {

		private Map<IResource, List<IAopReference>> references = new HashMap<IResource, List<IAopReference>>();

		private Set<IResource> resources = new HashSet<IResource>();

		public MarkerModifyingJob() {
			super("Creating AOP reference model markers");
			setPriority(Job.INTERACTIVE);
			setProperty(IProgressConstants.ICON_PROPERTY, AopCoreImages.DESC_OBJS_ASPECT);
			setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			try {
				for (IResource currentFile : resources) {
					monitor.beginTask("Creating AOP reference model markers for file ["
							+ currentFile.getFullPath().toString() + "]", IProgressMonitor.UNKNOWN);
					AopReferenceModelMarkerUtils.deleteProblemMarkers(currentFile);
					AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.deletedProblemMarkers", currentFile.getFullPath().toString()));
					// could be that no references have been recorded as the problem during pc matching
					// occurred
					if (references.containsKey(currentFile) && references.get(currentFile) != null) {
						for (IAopReference reference : references.get(currentFile)) {
							if (reference.getDefinition().getResource().equals(currentFile)
									|| reference.getResource().equals(currentFile)) {
								AopReferenceModelMarkerUtils.createMarker(reference, currentFile);
							}
						}
						AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
							"AopReferenceModelBuilder.createdProblemMarkers", currentFile.getFullPath().toString()));
					}
				}
				monitor.done();
				return Status.OK_STATUS;
			}
			catch (Exception e) {
				Activator.log(e);
				return Status.CANCEL_STATUS;
			}
		}

		public void addResource(IResource resource) {
			this.resources.add(resource);
		}

		public void addAopReference(IResource resource, List<IAopReference> references) {
			// create new list to prevent concurrent modification problems
			this.references.put(resource, new ArrayList<IAopReference>(references));
		}
	}
	
	/**
	 * {@link ISchedulingRule} implementation that always conflicts with other
	 * {@link BlockingOnSelfSchedulingRule}s.
	 * <p>
	 * This rule prevents that at no time more than one job with this scheduling
	 * rule attached runs.
	 * @since 2.0.4
	 */
	private class BlockingOnSelfSchedulingRule implements ISchedulingRule {
		
		/**
		 * Always returns <code>false</code>.
		 */
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}
		
		/**
		 * Returns <code>true</code> if <code>rule</code> is of type
		 * {@link BlockingOnSelfSchedulingRule}.
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof BlockingOnSelfSchedulingRule;
		}
	}

}
