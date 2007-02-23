/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.aop.core.model.builder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.StringUtils;

@SuppressWarnings("restriction")
public class AopReferenceModelBuilder {

	public static void buildAopModel(IProject project, Set<IFile> filesToBuild) {
		if (filesToBuild.size() > 0) {
			getBuildJob(project, filesToBuild).schedule();
		}
	}

	protected static void buildAopModel(IProgressMonitor monitor,
			Set<IFile> filesToBuild) {
		AopLog.logStart("Processing took");
		AopLog.log(AopLog.BUILDER,
				"Start building Spring AOP reference model from "
						+ filesToBuild.size() + " file(s)");

		int worked = 0;
		for (IFile currentFile : filesToBuild) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			AopLog.log(AopLog.BUILDER, "Building Spring AOP reference model ["
					+ currentFile.getFullPath().toString() + "]");
			monitor.setTaskName("Building Spring AOP reference model ["
					+ currentFile.getFullPath().toString() + "]");

			AopReferenceModelMarkerUtils.deleteProblemMarkers(currentFile);
			AopLog.log(AopLog.BUILDER_MESSAGES, "Deleted problem markers");

			IAopProject aopProject = buildAopReferencesFromFile(currentFile);
			AopLog.log(AopLog.BUILDER_MESSAGES,
					"Construted AOP reference model");

			if (aopProject != null) {
				List<IAopReference> references = aopProject.getAllReferences();
				for (IAopReference reference : references) {
					if (reference.getDefinition().getResource().equals(
							currentFile)
							|| reference.getResource().equals(currentFile)) {
						AopReferenceModelMarkerUtils.createMarker(reference,
								currentFile);
					}
				}
			}
			AopLog.log(AopLog.BUILDER_MESSAGES,
					"Created problem markers from reference model");
			worked++;
			monitor.worked(worked);
			AopLog.log(AopLog.BUILDER,
					"Done building Spring AOP reference model ["
							+ currentFile.getFullPath().toString() + "]");

		}
		AopLog.logEnd(AopLog.BUILDER, "Processing took");
		// update images and text decoractions
		Activator.getModel().fireModelChanged();
	}

	@SuppressWarnings("deprecation")
	private static IAopProject buildAopReferencesFromFile(IFile currentFile) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				currentFile.getProject());

		ILock lock = Job.getJobManager().newLock();
		lock.acquire();

		if (project != null) {
			BeansConfig config = (BeansConfig) project.getConfig(currentFile);
			IJavaProject javaProject = AopReferenceModelUtils
					.getJavaProject(config);

			if (javaProject != null) {
				aopProject = ((AopReferenceModel) Activator.getModel())
						.getProjectWithInitialization(AopReferenceModelUtils
								.getJavaProject(config.getElementResource()
										.getProject()));

				aopProject.clearReferencesForResource(currentFile);

				ClassLoader classLoader = Thread.currentThread()
						.getContextClassLoader();
				ClassLoader weavingClassLoader = SpringCoreUtils
						.getClassLoader(javaProject, false);
				Thread.currentThread()
						.setContextClassLoader(weavingClassLoader);
				AopLog.log(AopLog.BUILDER_CLASSPATH, "AOP builder classpath: "
						+ StringUtils
								.arrayToDelimitedString(
										((URLClassLoader) weavingClassLoader)
												.getURLs(), ";"));

				IStructuredModel model = null;
				List<IAspectDefinition> aspectInfos = null;
				try {
					try {
						model = StructuredModelManager.getModelManager()
								.getModelForRead(currentFile);
						IDOMDocument document = ((DOMModelImpl) model)
								.getDocument();

						aspectInfos = AspectDefinitionBuilder
								.buildAspectDefinitions(document, currentFile);
					}
					finally {
						if (model != null) {
							model.releaseFromRead();
						}
					}

					for (IAspectDefinition info : aspectInfos) {

						// build model for config
						buildAopReferencesFromAspectDefinition(config, info);

						// check config sets as well
						Set<IBeansConfigSet> configSets = project
								.getConfigSets();
						for (IBeansConfigSet configSet : configSets) {
							if (configSet.getConfigs().contains(config)) {
								Set<IBeansConfig> configs = configSet
										.getConfigs();
								for (IBeansConfig configSetConfig : configs) {
									if (!config.equals(configSetConfig)) {
										buildAopReferencesFromAspectDefinition(
												configSetConfig, info);
									}
								}
							}
						}
					}
				}
				catch (IOException e) {
					Activator.log(e);
				}
				catch (CoreException e) {
					Activator.log(e);
				}
				finally {
					Thread.currentThread().setContextClassLoader(classLoader);
					lock.release();
				}
			}
		}
		return aopProject;
	}

	private static void buildAopReferencesFromAspectDefinition(
			IBeansConfig config, IAspectDefinition info) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopReferenceModel) Activator.getModel())
				.getProjectWithInitialization(AopReferenceModelUtils
						.getJavaProject(config.getElementResource()
								.getProject()));

		Set<IBean> beans = config.getBeans();
		for (IBean bean : beans) {
			buildAopReferencesForBean(bean, config, info, file, aopProject);
		}
	}

	private static void buildAopReferencesForBean(IBean bean,
			IModelElement context, IAspectDefinition info, IResource file,
			IAopProject aopProject) {
		try {
			AopLog.log(AopLog.BUILDER, "Processing bean definition [" + bean
					+ "] from resource ["
					+ bean.getElementResource().getFullPath() + "]");

			// check if bean is abstract
			if (bean.isAbstract()) {
				return;
			}

			String className = BeansModelUtils.getBeanClass(bean, context);
			if (className != null && info.getAspectName() != null
					&& info.getAspectName().equals(bean.getElementName())
					&& info.getResource() != null
					&& info.getResource().equals(bean.getElementResource())) {
				// don't check advice backing bean itself
				AopLog.log(AopLog.BUILDER_MESSAGES,
						"Skipping bean definition [" + bean + "]");
				return;
			}

			IType jdtTargetType = BeansModelUtils.getJavaType(
					file.getProject(), className);
			IType jdtAspectType = BeansModelUtils.getJavaType(aopProject
					.getProject().getProject(), info.getAspectClassName());

			// check type not found and exclude factory beans
			if (jdtTargetType == null
					|| Introspector.doesImplement(jdtTargetType,
							FactoryBean.class.getName())) {
				AopLog
						.log(
								AopLog.BUILDER_MESSAGES,
								"Skipping bean definition ["
										+ bean
										+ "] because either its a FactoryBean or the IType could not be resolved");
				return;
			}

			Class<?> targetClass = AopReferenceModelBuilderUtils
					.loadClass(className);

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
								jdtAspectMember, jdtTargetType, info, file,
								bean);
						aopProject.addAopReference(ref);
					}
				}
			}
			else if (info instanceof JavaAspectDefinition
					&& !(info instanceof AnnotationAspectDefinition)) {
				JavaAspectDefinition intro = (JavaAspectDefinition) info;

				IMethod jdtAspectMethod = AopReferenceModelUtils.getMethod(
						jdtAspectType, info.getAdviceMethodName(), info
								.getAdviceMethodParameterTypes().length);
				if (jdtAspectMethod != null) {

					List<IMethod> matchingMethods = AopReferenceModelUtils
							.getMatches(targetClass, intro
									.getAspectJPointcutExpression(), aopProject
									.getProject().getProject());
					for (IMethod method : matchingMethods) {
						IAopReference ref = new AopReference(info.getType(),
								jdtAspectMethod, method, info, file, bean);
						aopProject.addAopReference(ref);
					}
				}
			}
			else {
				// validate the aspect definition
				if (info.getAdviceMethod() == null) {
					return;
				}

				IMethod jdtAspectMethod = AopReferenceModelUtils.getMethod(
						jdtAspectType, info.getAdviceMethodName(), info
								.getAdviceMethod().getParameterTypes().length);
				if (jdtAspectMethod != null) {

					Object pc = AopReferenceModelBuilderUtils
							.createAspectJPointcutExpression(info);

					Method matchesMethod = pc.getClass().getMethod("matches",
							Method.class, Class.class);
					for (Method m : targetClass.getDeclaredMethods()) {
						// Spring only allows proxying of public classes
						if (Modifier.isPublic(m.getModifiers())) {
							boolean matches = (Boolean) matchesMethod.invoke(
									pc, m, targetClass);
							if (matches) {
								IMethod jdtMethod = AopReferenceModelUtils
										.getMethod(jdtTargetType, m.getName(),
												m.getParameterTypes().length);
								IAopReference ref = new AopReference(info
										.getType(), jdtAspectMethod, jdtMethod,
										info, file, bean);
								aopProject.addAopReference(ref);
							}
						}
					}
				}
			}
		}
		catch (Throwable t) {
			handleException(t, info, bean, file);
		}

		// Make sure that inner beans are handled as well
		Set<IBean> innerBeans = bean.getInnerBeans();
		if (innerBeans != null && innerBeans.size() > 0) {
			for (IBean innerBean : innerBeans) {
				buildAopReferencesForBean(innerBean, context, info, file,
						aopProject);
			}
		}

	}

	private static void handleException(Throwable t, IAspectDefinition info,
			IBean bean, IResource file) {
		if (t instanceof NoClassDefFoundError
				|| t instanceof ClassNotFoundException) {
			AopLog.log(AopLog.BUILDER, "Class dependency error ["
					+ t.getMessage() + "] occured on aspect definition ["
					+ info + "] while processing bean [" + bean
					+ "]. Check if builder classpath is complete");
			AopReferenceModelMarkerUtils.createProblemMarker(file,
					"Build path is incomplete. Cannot find class file for "
							+ t.getMessage(), IMarker.SEVERITY_ERROR, info
							.getAspectLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
		else if (t instanceof IllegalArgumentException) {
			AopLog.log(AopLog.BUILDER, "Pointcut is malformed [" + info
					+ "] while processing bean [" + bean + "]");
			AopReferenceModelMarkerUtils.createProblemMarker(file, t
					.getMessage(), IMarker.SEVERITY_ERROR, info
					.getAspectLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
		else if (t instanceof InvocationTargetException) {
			AopLog.log(AopLog.BUILDER, "Exception from reflection [" + info
					+ "] while processing bean [" + bean + "]");
			if (t.getCause() != null) {
				handleException(t.getCause(), info, bean, file);
			}
			else {
				Activator.log(t);
				AopReferenceModelMarkerUtils.createProblemMarker(file, t
						.getMessage(), IMarker.SEVERITY_WARNING, info
						.getAspectLineNumber(),
						AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
			}
		}
		else {
			AopLog.log(AopLog.BUILDER, "Exception [" + t.getMessage() + "] ["
					+ info + "] while processing bean [" + bean + "]");
			Activator.log(t);
			AopReferenceModelMarkerUtils.createProblemMarker(file, t
					.getMessage(), IMarker.SEVERITY_WARNING, info
					.getAspectLineNumber(),
					AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
		}
	}

	public static Job getBuildJob(final IProject project,
			final Set<IFile> filesToBuild) {
		Job buildJob = new BuildJob("Building Spring AOP reference model",
				project, filesToBuild);
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
				.buildRule());
		buildJob.setUser(false);
		return buildJob;
	}

	private static final class BuildJob extends Job {

		private final IProject project;

		private final Set<IFile> filesToBuild;

		private BuildJob(String name, IProject project, Set<IFile> filesToBuild) {
			super(name);
			this.project = project;
			this.filesToBuild = filesToBuild;
		}

		public boolean isCoveredBy(BuildJob other) {
			if (other.project == null) {
				return true;
			}
			return project != null && project.equals(other.project);
		}

		@SuppressWarnings("deprecation")
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (getClass()) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				Job[] buildJobs = Job.getJobManager().find(
						ResourcesPlugin.FAMILY_MANUAL_BUILD);
				for (Job curr : buildJobs) {
					if (curr != this && curr instanceof BuildJob) {
						BuildJob job = (BuildJob) curr;
						if (job.isCoveredBy(this)) {
							curr.cancel(); // cancel all other build jobs of our kind
						}
					}
				}
			}
			try {
				monitor.beginTask("Building Spring AOP reference model",
						filesToBuild.size());
				buildAopModel(monitor, filesToBuild);
			}
			catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			}
			finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}

		public boolean belongsTo(Object family) {
			return ResourcesPlugin.FAMILY_MANUAL_BUILD == family;
		}
	}
}
