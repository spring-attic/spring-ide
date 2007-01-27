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

package org.springframework.ide.eclipse.aop.core.parser;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
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
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AnnotationIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.AopModel;
import org.springframework.ide.eclipse.aop.core.model.internal.AopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.BeansAopMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

@SuppressWarnings("restriction")
public class BeanAopModelBuilder {

	public static void buildAopModel(Set<IFile> filesToBuild) {
		if (filesToBuild.size() > 0) {
			getBuildJob(filesToBuild).schedule();
		}
	}

	protected static void buildAopModel(IProgressMonitor monitor, Set<IFile> filesToBuild) {
		int worked = 0;
		for (IFile currentFile : filesToBuild) {
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			monitor.setTaskName("Building Spring AOP model ["
					+ currentFile.getProjectRelativePath().toString() + "]");
			BeansAopMarkerUtils.deleteProblemMarkers(currentFile);

			IAopProject aopProject = buildAopReferencesFromFile(currentFile);
			if (aopProject != null) {
				List<IAopReference> references = aopProject.getAllReferences();
				for (IAopReference reference : references) {
					if (reference.getDefinition().getResource().equals(currentFile)
							|| reference.getResource().equals(currentFile)) {
						BeansAopMarkerUtils.createMarker(reference, currentFile);
					}
				}
			}
			worked++;
			monitor.worked(worked);

		}
		// update images and text decoractions
		Activator.getModel().fireModelChanged();
	}

	@SuppressWarnings("deprecation")
	private static IAopProject buildAopReferencesFromFile(IFile currentFile) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(currentFile.getProject());

		// change to Job.getJobManager as soon as we only use Eclipse 3.3
		IJobManager jobMan = Platform.getJobManager();
		ILock lock = jobMan.newLock();
		lock.acquire();

		if (project != null) {
			BeansConfig config = (BeansConfig) project.getConfig(currentFile);
			IJavaProject javaProject = BeansAopUtils.getJavaProject(config);
			if (javaProject != null) {
				aopProject = ((AopModel) Activator.getModel()).getProjectWithInitialization(config
						.getElementResource().getProject());

				aopProject.clearReferencesForResource(currentFile);

				ClassLoader weavingClassLoader = SpringCoreUtils.getClassLoader(javaProject);
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(weavingClassLoader);

				IStructuredModel model = null;
				List<IAspectDefinition> aspectInfos = null;
				try {
					try {
						model = StructuredModelManager.getModelManager().getModelForRead(
								currentFile);
						IDOMDocument document = ((DOMModelImpl) model).getDocument();
						aspectInfos = BeanAspectDefinitionParser.buildAspectDefinitions(document,
								currentFile);
					} finally {
						if (model != null) {
							model.releaseFromRead();
						}
					}

					for (IAspectDefinition info : aspectInfos) {

						// build model for config
						buildAopReferencesFromAspectDefinition(weavingClassLoader, config, info);

						// check config sets as well
						Set<IBeansConfigSet> configSets = project.getConfigSets();
						for (IBeansConfigSet configSet : configSets) {
							if (configSet.getConfigs().contains(config)) {
								Set<IBeansConfig> configs = configSet.getConfigs();
								for (IBeansConfig configSetConfig : configs) {
									if (!config.equals(configSetConfig)) {
										buildAopReferencesFromAspectDefinition(weavingClassLoader,
												configSetConfig, info);
									}
								}
							}
						}
					}
				} catch (IOException e) {
					Activator.log(e);
				} catch (CoreException e) {
					Activator.log(e);
				} finally {
					Thread.currentThread().setContextClassLoader(classLoader);
					lock.release();
				}
			}
		}
		return aopProject;
	}

	private static void buildAopReferencesFromAspectDefinition(ClassLoader loader,
			IBeansConfig config, IAspectDefinition info) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopModel) Activator.getModel())
				.getProjectWithInitialization(config.getElementResource().getProject());

		Set<IBean> beans = config.getBeans();
		for (IBean bean : beans) {
			try {

				Class<?> targetClass = loader.loadClass(bean.getClassName());
				if (info instanceof BeanIntroductionDefinition) {
					BeanIntroductionDefinition intro = (BeanIntroductionDefinition) info;
					if (intro.getTypeMatcher().matches(targetClass)) {
						IType jdtAspectType = BeansModelUtils.getJavaType(aopProject.getProject(),
								((BeanIntroductionDefinition) info).getClassName());
						IMember jdtAspectMember = null;
						if (intro instanceof AnnotationIntroductionDefinition) {
							String fieldName = ((AnnotationIntroductionDefinition) intro)
									.getDefiningField().getName();
							jdtAspectMember = jdtAspectType.getField(fieldName);
						} else {
							jdtAspectMember = jdtAspectType;
						}

						IType beanType = BeansModelUtils.getJavaType(aopProject.getProject(), bean
								.getClassName());
						if (jdtAspectMember.getResource() != null
								&& jdtAspectMember.getResource().isAccessible()) {
							IAopReference ref = new AopReference(info.getType(), jdtAspectMember,
									beanType, info, file, bean);
							aopProject.addAopReference(ref);
						}
					}
				} else if (info instanceof JavaAspectDefinition
						&& !(info instanceof AnnotationAspectDefinition)) {
					JavaAspectDefinition intro = (JavaAspectDefinition) info;

					List<IMethod> matchingMethods = BeansAopUtils.getMatches(targetClass, intro
							.getPointcut().getMethodMatcher(), aopProject.getProject());

					for (IMethod method : matchingMethods) {
						IType jdtAspectType = BeansModelUtils.getJavaType(aopProject.getProject(),
								info.getClassName());
						IMethod jdtAspectMethod = BeansAopUtils.getMethod(jdtAspectType, info
								.getMethod(), info.getAdviceMethod().getParameterTypes().length);
						if (jdtAspectMethod.getResource() != null
								&& jdtAspectMethod.getResource().isAccessible()) {
							IAopReference ref = new AopReference(info.getType(), jdtAspectMethod,
									method, info, file, bean);
							aopProject.addAopReference(ref);
						}
					}
				} else {
					JdtAwareAspectJAdviceMatcher advice = new JdtAwareAspectJAdviceMatcher(file
							.getProject(), info.getAdviceMethod(), info.getPointcut());
					if (info.getThrowing() != null) {
						advice.setThrowingName(info.getThrowing());
					}
					if (info.getReturning() != null) {
						advice.setReturningName(info.getReturning());
					}
					advice.afterPropertiesSet();

					List<IMethod> matchingMethods = advice.getMatches(targetClass);
					for (IMethod method : matchingMethods) {
						IType jdtAspectType = BeansModelUtils.getJavaType(aopProject.getProject(),
								info.getClassName());
						IMethod jdtAspectMethod = BeansAopUtils.getMethod(jdtAspectType, info
								.getMethod(), info.getAdviceMethod().getParameterTypes().length);
						if (jdtAspectMethod.getResource() != null
								&& jdtAspectMethod.getResource().isAccessible()) {
							IAopReference ref = new AopReference(info.getType(), jdtAspectMethod,
									method, info, file, bean);
							aopProject.addAopReference(ref);
						}
					}
				}
			} catch (NoClassDefFoundError e) {
				BeansAopMarkerUtils.createProblemMarker(file, "Class dependency is missing: "
						+ e.getMessage(), IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
						BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
			} catch (ClassNotFoundException e) {
				BeansAopMarkerUtils.createProblemMarker(file, "Class dependency is missing: "
						+ e.getMessage(), IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
						BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
			} catch (IllegalArgumentException e) {
				BeansAopMarkerUtils.createProblemMarker(file, e.getMessage(),
						IMarker.SEVERITY_ERROR, info.getAspectLineNumber(),
						BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
			} catch (Throwable t) {
				BeansAopMarkerUtils.createProblemMarker(file, t.getMessage(),
						IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
						BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
			}
		}
	}

	public static Job getBuildJob(final Set<IFile> filesToBuild) {
		Job buildJob = new BuildJob("Building Spring AOP model", filesToBuild);
		buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		buildJob.setUser(true);
		return buildJob;
	}

	private static final class BuildJob extends Job {

		private final Set<IFile> filesToBuild;

		private BuildJob(String name, Set<IFile> filesToBuild) {
			super(name);
			this.filesToBuild = filesToBuild;
		}

		protected IStatus run(IProgressMonitor monitor) {
			try {
				monitor.beginTask("Parsing Spring AOP", filesToBuild.size());
				buildAopModel(monitor, filesToBuild);
			} catch (OperationCanceledException e) {
				return Status.CANCEL_STATUS;
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}
	}
}
