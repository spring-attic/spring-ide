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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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

/**
 * Handles creation and modification of the {@link AopReferenceModel}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class AopReferenceModelBuilder implements IWorkspaceRunnable {

	private Set<IFile> filesToBuild;

	private ClassLoader weavingClassLoader;

	private ClassLoader classLoader;

	public AopReferenceModelBuilder(Set<IFile> filesToBuild) {
		this.filesToBuild = filesToBuild;
	}

	/**
	 * Handles the creation of the AOP reference model
	 * @param monitor the progressMonitor
	 * @param filesToBuild the files to build the model from
	 */
	protected void buildAopModel(IProgressMonitor monitor,
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
			monitor.subTask("Building Spring AOP reference model ["
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

	/**
	 * Builds AOP refererences for given {@link IBean} instances. Matches the
	 * given Aspect definition against the {@link IBean}.
	 */
	private void buildAopReferencesForBean(IBean bean, IModelElement context,
			IAspectDefinition info, IResource file, IAopProject aopProject) {
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

			// use the weavingClassLoader to pointcut matching
			activateWeavingClassLoader();

			IType jdtTargetType = BeansModelUtils.getJavaType(
					file.getProject(), className);
			IType jdtAspectType = BeansModelUtils.getJavaType(aopProject
					.getProject().getProject(), info.getAspectClassName());

			// check type not found and exclude factory beans
			if (jdtTargetType == null
					|| Introspector.doesImplement(jdtTargetType,
							FactoryBean.class.getName())) {
				AopLog.log(AopLog.BUILDER_MESSAGES, "Skipping bean definition ["
					+ bean + "] because either it is a FactoryBean or the IType "
					+ "could not be resolved");
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
		finally {
			recoverClassLoader();
		}

		// Make sure that inner beans are handled as well
		Set<IBean> innerBeans = BeansModelUtils.getInnerBeans(bean);
		if (innerBeans != null && innerBeans.size() > 0) {
			for (IBean innerBean : innerBeans) {
				buildAopReferencesForBean(innerBean, context, info, file,
						aopProject);
			}
		}

	}

	private void buildAopReferencesFromAspectDefinition(IBeansConfig config,
			IAspectDefinition info) {

		IResource file = config.getElementResource();
		IAopProject aopProject = ((AopReferenceModel) Activator.getModel())
				.getProjectWithInitialization(AopReferenceModelUtils
						.getJavaProject(info.getResource()
								.getProject()));

		Set<IBean> beans = config.getBeans();
		for (IBean bean : beans) {
			buildAopReferencesForBean(bean, config, info, file, aopProject);
		}
	}

	private void buildAopReferencesFromBeansConfigSets(IBeansProject project,
			BeansConfig config, IAspectDefinition info) {
		// check config sets as well
		Set<IBeansConfigSet> configSets = project.getConfigSets();
		for (IBeansConfigSet configSet : configSets) {
			if (configSet.getConfigs().contains(config)) {
				Set<IBeansConfig> configs = configSet.getConfigs();
				for (IBeansConfig configSetConfig : configs) {
					if (!config.equals(configSetConfig)) {
						buildAopReferencesFromAspectDefinition(configSetConfig,
								info);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private IAopProject buildAopReferencesFromFile(IFile currentFile) {
		IAopProject aopProject = null;
		IBeansProject project = BeansCorePlugin.getModel().getProject(
				currentFile.getProject());

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

				// prepare class loaders
				setupClassLoaders(javaProject);

				AopLog.log(AopLog.BUILDER_CLASSPATH, "AOP builder classpath: "
					+ StringUtils.arrayToDelimitedString(
						((URLClassLoader) weavingClassLoader).getURLs(), ";"));

				try {
					IStructuredModel model = null;
					List<IAspectDefinition> aspectInfos = null;
					try {
						model = StructuredModelManager.getModelManager()
								.getModelForRead(currentFile);
						if (model != null) {
							IDOMDocument document = ((DOMModelImpl) model)
									.getDocument();
							try {
								// move beyond reading the structured model to
								// avoid class loading problems
								activateWeavingClassLoader();
								aspectInfos = AspectDefinitionBuilder
										.buildAspectDefinitions(document,
												currentFile);
							}
							finally {
								recoverClassLoader();
							}
						}
					}
					finally {
						if (model != null) {
							model.releaseFromRead();
						}
					}

					if (aspectInfos != null) {
						for (IAspectDefinition info : aspectInfos) {

							// build model for config
							buildAopReferencesFromAspectDefinition(config, info);

							// build model for config sets
							buildAopReferencesFromBeansConfigSets(project,
									config, info);
						}
					}
				}
				catch (IOException e) {
					Activator.log(e);
				}
				catch (CoreException e) {
					Activator.log(e);
				}
			}
		}
		return aopProject;
	}

	private void recoverClassLoader() {
		Thread.currentThread().setContextClassLoader(classLoader);
	}

	private void activateWeavingClassLoader() {
		Thread.currentThread().setContextClassLoader(weavingClassLoader);
	}

	private void setupClassLoaders(IJavaProject javaProject) {
		classLoader = Thread.currentThread().getContextClassLoader();
		weavingClassLoader = SpringCoreUtils.getClassLoader(javaProject, false);
	}

	private void handleException(Throwable t, IAspectDefinition info,
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

	public void run(IProgressMonitor monitor) throws CoreException {
		try {
			if (!monitor.isCanceled()) {
				this.buildAopModel(monitor, this.filesToBuild);
			}
		}
		finally {
			weavingClassLoader = null;
			classLoader = null;
			filesToBuild = null;
		}
	}
}
