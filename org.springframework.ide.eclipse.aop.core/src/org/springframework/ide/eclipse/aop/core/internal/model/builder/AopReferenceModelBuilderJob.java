/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.java.ClassUtils;
import org.springframework.ide.eclipse.core.java.IProjectClassLoaderSupport;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;

/**
 * Handles creation and modification of the {@link AopReferenceModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelBuilderJob extends Job {

	public static final Object CONTENT_FAMILY = new Object();

	private static final String PROCESSING_TOOK_MSG = "Processing took";

	private Set<IResource> affectedResources;

	private Map<IFile, Set<IAspectDefinition>> aspectDefinitionCache = null;

	private IProject currentProject;

	private IProjectClassLoaderSupport classLoaderSupport;

	private MarkerModifyingJob markerJob = null;

	private AspectDefinitionMatcher matcher = null;

	/**
	 * Constructor to create a {@link AopReferenceModelBuilderJob} instance.
	 * @param affectedResources the set of resources that should be processed
	 */
	public AopReferenceModelBuilderJob(Set<IResource> affectedResources, Set<IResource> originalResources) {
		super(Activator.getFormattedMessage("AopReferenceModelProjectBuilder.buildingAopReferenceModel"));
		this.affectedResources = affectedResources;
		// this.originalResources = originalResources;
		setPriority(Job.BUILD);
		// make sure that only one Job at a time runs but without blocking the UI
		setRule(new BlockingOnSelfSchedulingRule());
		setSystem(true);
		setProperty(IProgressConstants.ICON_PROPERTY, AopCoreImages.DESC_OBJS_ASPECT);
	}

	@Override
	public boolean belongsTo(Object family) {
		return CONTENT_FAMILY == family;
	}

	public boolean isCoveredBy(AopReferenceModelBuilderJob other) {
		if (other.affectedResources != null && this.affectedResources != null) {
			for (IResource resource : affectedResources) {
				if (!other.affectedResources.contains(resource)) {
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Implementation for {@link IWorkspaceRunnable#run(IProgressMonitor)} method that triggers the building of the aop
	 * reference model.
	 */
	public IStatus run(IProgressMonitor monitor) {
		try {
			synchronized (getClass()) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager().find(CONTENT_FAMILY);
				for (int i = 0; i < buildJobs.length; i++) {
					Job curr = buildJobs[i];
					if (curr != this && curr instanceof AopReferenceModelBuilderJob) {
						AopReferenceModelBuilderJob job = (AopReferenceModelBuilderJob) curr;
						if (job.isCoveredBy(this)) {
							curr.cancel();
						}
					}
				}
			}
			if (!monitor.isCanceled()) {
				long start = System.currentTimeMillis();
				System.out.println(String.format("- building aop model for '%s' resources", affectedResources.size()));
				this.buildAopModel(monitor);
				System.out.println(String
						.format("- aop model building took '%s'", (System.currentTimeMillis() - start)));
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
	 * Builds AOP references for given {@link IBean} instances. Matches the given Aspect definition against the
	 * {@link IBean}.
	 */
	private void buildAopReferencesForBean(final IBean bean, final IModelElement context, final IAspectDefinition info,
			final IResource file, final IAopProject aopProject, IProgressMonitor monitor) {
		try {
			AopLog
					.log(AopLog.BUILDER, Activator.getFormattedMessage(
							"AopReferenceModelBuilder.processingBeanDefinition", bean, bean.getElementResource()
									.getFullPath()));

			// check if bean is abstract
			if (bean.isAbstract()) {
				return;
			}

			final String className = BeansModelUtils.getBeanClass(bean, context);
			// don't check advice backing bean itself
			if (className != null && info.getAspectName() != null && info.getAspectName().equals(bean.getElementName())
					&& info.getResource() != null && info.getResource().equals(bean.getElementResource())) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.skippingBeanDefinition", bean));
				return;
			}

			final IType jdtTargetType = JdtUtils.getJavaType(file.getProject(), className);
			final IType jdtAspectType = JdtUtils.getJavaType(aopProject.getProject().getProject(), info
					.getAspectClassName());

			// check type not found and exclude factory beans
			if (jdtTargetType == null || bean.isFactory()) {
				AopLog.log(AopLog.BUILDER_MESSAGES, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.skippingFactoryBeanDefinition", bean));
				return;
			}

			// do in context of active weaving class loader
			this.classLoaderSupport.executeCallback(new IProjectClassLoaderSupport.IProjectClassLoaderAwareCallback() {

				public void doWithActiveProjectClassLoader() throws Throwable {
					Class<?> targetClass = ClassUtils.loadClass(className);

					// handle introductions first
					if (info instanceof BeanIntroductionDefinition) {
						BeanIntroductionDefinition intro = (BeanIntroductionDefinition) info;
						if (intro.getTypeMatcher().matches(targetClass)) {
							IMember jdtAspectMember = null;
							if (intro instanceof AnnotationIntroductionDefinition) {
								String fieldName = ((AnnotationIntroductionDefinition) intro).getDefiningField();
								jdtAspectMember = jdtAspectType.getField(fieldName);
							}
							else {
								jdtAspectMember = jdtAspectType;
							}

							if (jdtAspectMember != null) {
								IAopReference ref = new AopReference(info.getType(), jdtAspectMember, jdtTargetType,
										info, file, bean);
								aopProject.addAopReference(ref);
							}
						}
					}
					else if (info instanceof BeanAspectDefinition) {
						IMethod jdtAspectMethod = null;

						if (info instanceof JavaAdvisorDefinition) {
							jdtAspectMethod = JdtUtils.getMethod(jdtAspectType, info.getAdviceMethodName(), info
									.getAdviceMethodParameterTypes());
						}
						else {
							// validate the aspect definition
							if (info.getAdviceMethod() == null) {
								return;
							}
							jdtAspectMethod = JdtUtils.getMethod(jdtAspectType, info.getAdviceMethodName(), info
									.getAdviceMethod().getParameterTypes());
						}

						if (jdtAspectMethod != null) {
							// long start = System.currentTimeMillis();
							Set<IMethod> matchingMethods = matcher.matches(targetClass, bean, info, aopProject
									.getProject().getProject());
							for (IMethod method : matchingMethods) {
								IAopReference ref = new AopReference(info.getType(), jdtAspectMethod, method, info,
										file, bean);
								aopProject.addAopReference(ref);
							}
							// System.out.println(String.format("--- matching on '%s' took '%s'", targetClass, (System
							// .currentTimeMillis() - start)));
						}
					}
				}
			});
		}
		catch (Throwable t) {
			markerJob.addThrowableHolder(new ThrowableHolder(t, file, bean, info));
		}
	}

	private void buildAopReferencesForBeans(IModelElement config, IAspectDefinition info, IProgressMonitor monitor,
			IResource file, IAopProject aopProject, Set<IBean> beans) {

		monitor.subTask(Activator.getFormattedMessage("AopReferenceModelBuilder.buildingAopReferences"));

		for (IBean bean : beans) {
			monitor.subTask(Activator.getFormattedMessage("AopReferenceModelBuilder.buildingAopReferencesForBean", bean
					.getElementName(), bean.getElementResource().getFullPath()));
			buildAopReferencesForBean(bean, config, info, file, aopProject, monitor);

			// Make sure that inner beans are handled as well
			buildAopReferencesForBeans(config, info, monitor, file, aopProject, BeansModelUtils.getInnerBeans(bean));
		}
	}

	private void buildAopReferencesForBeansConfig(IBeansConfig config, IAspectDefinition info, IProgressMonitor monitor) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopReferenceModel) Activator.getModel()).getProjectWithInitialization(JdtUtils
				.getJavaProject(file.getProject()));

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

		buildAopReferencesForBeans(config, info, monitor, file, aopProject, beans);
	}

	private IAopProject buildAopReferencesForFile(IFile currentFile, IProgressMonitor monitor) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(currentFile.getProject());

		if (project != null) {
			IBeansConfig config = project.getConfig(currentFile, true);
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			if (javaProject != null && config != null) {

				// long start = System.currentTimeMillis();

				aopProject = ((AopReferenceModel) Activator.getModel()).getProjectWithInitialization(javaProject);
				aopProject.clearReferencesForResource(currentFile);

				// Prepare class loaders
				this.classLoaderSupport = createWeavingClassLoaderSupport(project.getProject());

				AopLog.log(AopLog.BUILDER_CLASSPATH, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.aopBuilderClassPath", StringUtils.arrayToDelimitedString(
								((URLClassLoader) classLoaderSupport.getProjectClassLoader()).getURLs(), ";")));

				List<IAspectDefinition> aspectInfos = new ArrayList<IAspectDefinition>();
				Set<IAspectDefinitionBuilder> builders = AspectDefinitionBuilderUtils.getAspectDefinitionBuilder();
				aspectInfos.addAll(buildAspectDefinitions(currentFile, builders));

				for (IBeansImport beansImport : config.getImports()) {
					for (IImportedBeansConfig importedConfig : beansImport.getImportedBeansConfigs()) {
						if (importedConfig.getElementResource() instanceof IFile) {
							aspectInfos.addAll(buildAspectDefinitions((IFile) importedConfig.getElementResource(),
									builders));
						}
					}
				}

				// remove references for all definitions
				for (IAspectDefinition info : aspectInfos) {
					aopProject.clearReferencesForResource(info.getResource());
				}

				// System.out.println(String.format("-- preparing aop model for file '%s' took '%s'", currentFile,
				// (System
				// .currentTimeMillis() - start)));
				//
				// start = System.currentTimeMillis();

				for (IAspectDefinition info : aspectInfos) {
					// build model for config
					buildAopReferencesForBeansConfig(config, info, monitor);

					// build model for config sets
					buildAopReferencesFromBeansConfigSets(project, config, info, monitor);
				}

				// System.out.println(String.format("-- building aop model for file '%s' took '%s'", currentFile,
				// (System
				// .currentTimeMillis() - start)));
			}
		}
		return aopProject;
	}

	/**
	 * Iterates over the list of {@link IBeansConfigSet} to determine if the given <code>config</code> is part of a
	 * certain config set and as such the {@link IAspectDefinition} need to be matched against the beans contained in
	 * the config set.
	 */
	private void buildAopReferencesFromBeansConfigSets(IBeansProject project, IBeansConfig config,
			IAspectDefinition info, IProgressMonitor monitor) {

		Set<IBeansConfig> foundConfigs = new LinkedHashSet<IBeansConfig>();
		for (IBeansConfigSet configSet : project.getConfigSets()) {
			if (configSet.getConfigs().contains(config)) {
				Set<IBeansConfig> configs = configSet.getConfigs();
				for (IBeansConfig configSetConfig : configs) {
					if (!config.equals(configSetConfig)) {
						foundConfigs.add(configSetConfig);
					}
				}
			}
		}
		for (IBeansConfig bc : foundConfigs) {
			buildAopReferencesForBeansConfig(bc, info, monitor);
		}
	}

	private Set<IAspectDefinition> buildAspectDefinitions(IFile file, Set<IAspectDefinitionBuilder> builders) {
		if (aspectDefinitionCache.containsKey(file)) {
			return aspectDefinitionCache.get(file);
		}

		Set<IAspectDefinition> definitions = new HashSet<IAspectDefinition>();
		for (IAspectDefinitionBuilder builder : builders) {
			definitions.addAll(builder.buildAspectDefinitions(file, classLoaderSupport));
		}

		aspectDefinitionCache.put(file, definitions);

		return definitions;
	}

	/**
	 * Handles the creation of the AOP reference model
	 * @param monitor the progressMonitor
	 * @param filesToBuild the files to build the model from
	 */
	protected void buildAopModel(IProgressMonitor monitor) {
		AopLog.logStart(PROCESSING_TOOK_MSG);
		AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage("AopReferenceModelBuilder.startBuildReferenceModel",
				affectedResources.size()));

		markerJob = new MarkerModifyingJob();
		matcher = new AspectDefinitionMatcher();
		aspectDefinitionCache = new HashMap<IFile, Set<IAspectDefinition>>();

		monitor.beginTask(Activator.getFormattedMessage("AopReferenceModelBuilder.startBuildingAopReferenceModel"),
				affectedResources.size());

		Map<IResource, IAopProject> processedProjects = new HashMap<IResource, IAopProject>();
		try {
			for (IResource currentResource : affectedResources) {
				if (currentResource instanceof IFile) {
					IFile currentFile = (IFile) currentResource;

					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}

					AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
									"AopReferenceModelBuilder.buildingAopReferenceModel", currentFile.getFullPath()
											.toString()));
					monitor.subTask(Activator.getFormattedMessage("AopReferenceModelBuilder.buildingAopReferenceModel",
							currentFile.getFullPath().toString()));

					markerJob.addResource(currentFile);

					// do the actual aop matching
					IAopProject aopProject = buildAopReferencesForFile(currentFile, monitor);

					AopLog.log(AopLog.BUILDER_MESSAGES, Activator
							.getFormattedMessage("AopReferenceModelBuilder.constructedAopReferenceModel"));

					if (aopProject != null) {
						processedProjects.put(currentFile, aopProject);
					}

					monitor.worked(1);
					AopLog.log(AopLog.BUILDER, Activator
							.getFormattedMessage("AopReferenceModelBuilder.doneBuildingReferenceModel", currentFile
									.getFullPath().toString()));

				}
			}

			for (Map.Entry<IResource, IAopProject> entry : processedProjects.entrySet()) {
				Set<IAopReference> references = entry.getValue().getAllReferences();
				markerJob.addAopReference(entry.getKey(), references);
			}

			AopLog.logEnd(AopLog.BUILDER, PROCESSING_TOOK_MSG);
		}
		finally {
			matcher.close();
			// schedule marker update job
			markerJob.schedule();
		}

	}

	/**
	 * Template method to create a {@link IProjectClassLoaderSupport} instance.
	 * <p>
	 * This implementation simply calls {@link JdtUtils#getProjectClassLoaderSupport(IProject)}
	 */
	protected IProjectClassLoaderSupport createWeavingClassLoaderSupport(IProject project) {
		if (!project.equals(this.currentProject)) {
			this.currentProject = project;
			return JdtUtils.getProjectClassLoaderSupport(project);
		}
		return this.classLoaderSupport;
	}

	/**
	 * {@link ISchedulingRule} implementation that always conflicts with other {@link BlockingOnSelfSchedulingRule}s.
	 * <p>
	 * This rule prevents that at no time more than one job with this scheduling rule attached runs.
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
		 * Returns <code>true</code> if <code>rule</code> is of type {@link BlockingOnSelfSchedulingRule}.
		 */
		public boolean isConflicting(ISchedulingRule rule) {
			return rule instanceof BlockingOnSelfSchedulingRule;
		}
	}

	/**
	 * {@link Job} implementation that handles deletion and creation of markers for the aop reference model.
	 * <p>
	 * This Job schedules itself with a workspace scheduling rule; meaning the workspace will be locked for
	 * modifications while this jobs runs.
	 * @since 2.0.4
	 */
	private class MarkerModifyingJob extends Job {

		private Map<IResource, List<IAopReference>> references = new HashMap<IResource, List<IAopReference>>();

		private Set<IResource> resources = new HashSet<IResource>();

		private Set<ThrowableHolder> throwables = new HashSet<ThrowableHolder>();

		public MarkerModifyingJob() {
			super("Creating AOP reference model markers");
			setPriority(Job.INTERACTIVE);
			setSystem(true);
			setProperty(IProgressConstants.ICON_PROPERTY, AopCoreImages.DESC_OBJS_ASPECT);
			setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		}

		public void addAopReference(IResource resource, Set<IAopReference> references) {
			// create new list to prevent concurrent modification problems
			this.references.put(resource, new ArrayList<IAopReference>(references));
		}

		public void addResource(IResource resource) {
			this.resources.add(resource);
		}

		public void addThrowableHolder(ThrowableHolder throwableHolder) {
			throwables.add(throwableHolder);
		}

		/**
		 * Handles any exception that might come up during parsing and matching of pointcuts.
		 */
		private void handleException(Throwable t, IAspectDefinition info, IBean bean, IResource file) {
			if (t instanceof NoClassDefFoundError || t instanceof ClassNotFoundException) {
				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.classDependencyError", t.getMessage(), info, bean));
				AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.buildPathIncomplete", t.getMessage()), IMarker.SEVERITY_ERROR, bean
						.getElementStartLine(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
			}
			else if (t instanceof IllegalArgumentException) {
				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.pointcutIsMalformedOnBean", info, bean));
				AopReferenceModelMarkerUtils.createProblemMarker(info.getResource(), Activator.getFormattedMessage(
						"AopReferenceModelBuilder.pointcutIsMalformed", t.getMessage()), IMarker.SEVERITY_ERROR, info
						.getAspectStartLineNumber(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, info
						.getResource());
			}
			else if (t instanceof InvocationTargetException || t instanceof RuntimeException) {
				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.exceptionFromReflectionOnBean", info, bean));
				if (t.getCause() != null) {
					handleException(t.getCause(), info, bean, file);
				}
				else {
					Activator.log(t);
					AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
							"AopReferenceModelBuilder.exceptionFromReflection", t.getMessage()),
							IMarker.SEVERITY_WARNING, info.getAspectStartLineNumber(),
							AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
				}
			}
			else {
				AopLog.log(AopLog.BUILDER, Activator.getFormattedMessage("AopReferenceModelBuilder.exception", t
						.getMessage(), info, bean));
				Activator.log(t);
				AopReferenceModelMarkerUtils.createProblemMarker(file, Activator.getFormattedMessage(
						"AopReferenceModelBuilder.exception", t.getMessage()), IMarker.SEVERITY_WARNING, info
						.getAspectStartLineNumber(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
			}
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
					// could be that no references have been recorded as the problem during pc matching occurred
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
				// adding markers for exceptions that occurred during parsing
				for (ThrowableHolder holder : throwables) {
					handleException(holder.getThrowable(), holder.getAspectDefinition(), holder.getBean(), holder
							.getResource());
				}

				// update images and text decorations
				Activator.getModel().fireModelChanged();

				monitor.done();

				return Status.OK_STATUS;
			}
			catch (Exception e) {
				Activator.log(e);
				return Status.CANCEL_STATUS;
			}
		}

	}

	/**
	 * Holder to collect {@link Exception}s thrown during pointcut parsing and matching.
	 * @since 2.0.4
	 */
	private class ThrowableHolder {

		private IAspectDefinition aspectDefinition;

		private IBean bean;

		private IResource resource;

		private Throwable throwable;

		public ThrowableHolder(Throwable throwable, IResource resource, IBean bean, IAspectDefinition aspectDefinition) {
			this.throwable = throwable;
			this.resource = resource;
			this.bean = bean;
			this.aspectDefinition = aspectDefinition;
		}

		public IAspectDefinition getAspectDefinition() {
			return aspectDefinition;
		}

		public IBean getBean() {
			return bean;
		}

		public IResource getResource() {
			return resource;
		}

		public Throwable getThrowable() {
			return throwable;
		}

	}

}
