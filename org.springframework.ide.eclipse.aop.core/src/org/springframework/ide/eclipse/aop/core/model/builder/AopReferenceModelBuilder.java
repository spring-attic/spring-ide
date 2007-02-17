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
import org.springframework.ide.eclipse.aop.core.model.internal.AopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.AopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.internal.BeanIntroductionDefinition;
import org.springframework.ide.eclipse.aop.core.model.internal.JavaAspectDefinition;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

@SuppressWarnings("restriction")
public class AopReferenceModelBuilder {

    public static void buildAopModel(IProject project, Set<IFile> filesToBuild) {
        if (filesToBuild.size() > 0) {
            getBuildJob(project, filesToBuild).schedule();
        }
    }

    protected static void buildAopModel(IProgressMonitor monitor, Set<IFile> filesToBuild) {
        int worked = 0;
        for (IFile currentFile : filesToBuild) {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            monitor.setTaskName("Building Spring AOP reference model ["
                    + currentFile.getProjectRelativePath().toString() + "]");
            AopReferenceModelMarkerUtils.deleteProblemMarkers(currentFile);

            IAopProject aopProject = buildAopReferencesFromFile(currentFile);
            if (aopProject != null) {
                List<IAopReference> references = aopProject.getAllReferences();
                for (IAopReference reference : references) {
                    if (reference.getDefinition().getResource().equals(currentFile)
                            || reference.getResource().equals(currentFile)) {
                    	AopReferenceModelMarkerUtils.createMarker(reference, currentFile);
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
            IJavaProject javaProject = AopReferenceModelUtils.getJavaProject(config);
            if (javaProject != null) {
                aopProject = ((AopReferenceModel) Activator.getModel())
                        .getProjectWithInitialization(AopReferenceModelUtils.getJavaProject(config
                                .getElementResource().getProject()));

                aopProject.clearReferencesForResource(currentFile);

                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader weavingClassLoader = SpringCoreUtils.getClassLoader(
                		javaProject, false);
                Thread.currentThread().setContextClassLoader(weavingClassLoader);

                IStructuredModel model = null;
                List<IAspectDefinition> aspectInfos = null;
                try {
                    try {
                        model = StructuredModelManager.getModelManager().getModelForRead(
                                currentFile);
                        IDOMDocument document = ((DOMModelImpl) model).getDocument();

                        aspectInfos = AspectDefinitionBuilder.buildAspectDefinitions(document,
                                currentFile);
                    }
                    finally {
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

    private static void buildAopReferencesFromAspectDefinition(ClassLoader loader,
            IBeansConfig config, IAspectDefinition info) {

        IResource file = config.getElementResource();
        IAopProject aopProject = ((AopReferenceModel) Activator.getModel())
                .getProjectWithInitialization(AopReferenceModelUtils.getJavaProject(config
                        .getElementResource().getProject()));

        Set<IBean> beans = config.getBeans();
        for (IBean bean : beans) {
            try {
                if (info.getAspectName() != null
                        && info.getAspectName().equals(bean.getElementName())
                        && info.getResource() != null
                        && info.getResource().equals(bean.getElementResource())) {
                    // don't check advice backing bean itself
                    continue;
                }
                
                Class<?> targetClass = loader.loadClass(bean.getClassName());
                if (info instanceof BeanIntroductionDefinition) {
                    BeanIntroductionDefinition intro = (BeanIntroductionDefinition) info;
                    if (intro.getTypeMatcher().matches(targetClass)) {
                        IType jdtAspectType = BeansModelUtils.getJavaType(aopProject.getProject()
                                .getProject(), ((BeanIntroductionDefinition) info)
                                .getAspectClassName());
                        IMember jdtAspectMember = null;
                        if (intro instanceof AnnotationIntroductionDefinition) {
                            String fieldName = ((AnnotationIntroductionDefinition) intro)
                                    .getDefiningField();
                            jdtAspectMember = jdtAspectType.getField(fieldName);
                        }
                        else {
                            jdtAspectMember = jdtAspectType;
                        }

                        IType beanType = BeansModelUtils.getJavaType(aopProject.getProject()
                                .getProject(), bean.getClassName());
                        if (jdtAspectMember != null) {
                            IAopReference ref = new AopReference(info.getType(), jdtAspectMember,
                                    beanType, info, file, bean);
                            aopProject.addAopReference(ref);
                        }
                    }
                }
                else if (info instanceof JavaAspectDefinition
                        && !(info instanceof AnnotationAspectDefinition)) {
                    JavaAspectDefinition intro = (JavaAspectDefinition) info;

                    List<IMethod> matchingMethods = AopReferenceModelUtils.getMatches(targetClass, intro
                            .getAspectJPointcutExpression(), aopProject.getProject().getProject());

                    for (IMethod method : matchingMethods) {
                        IType jdtAspectType = BeansModelUtils.getJavaType(aopProject.getProject()
                                .getProject(), info.getAspectClassName());
                        IMethod jdtAspectMethod = AopReferenceModelUtils
                                .getMethod(jdtAspectType, info.getAdviceMethodName(), info
                                        .getAdviceMethodParameterTypes().length);
                        if (jdtAspectMethod != null) {
                            IAopReference ref = new AopReference(info.getType(), jdtAspectMethod,
                                    method, info, file, bean);
                            aopProject.addAopReference(ref);
                        }
                    }
                }
                else {
                    // validate the aspect definition
                    if (info.getAdviceMethod() == null) {
                        return;
                    }

                    IType jdtTargetType = BeansModelUtils.getJavaType(file.getProject(),
                            targetClass.getName());
                    IType jdtAspectType = BeansModelUtils.getJavaType(file.getProject(), info
                            .getAspectClassName());
                    Class<?> aspectJAdviceClass = AopReferenceModelBuilderUtils.getAspectJAdviceClass(info);

                    Object pc = info.getAspectJPointcutExpression();

                    AopReferenceModelBuilderUtils.createAspectJAdvice(info, aspectJAdviceClass, pc);

                    Method matchesMethod = pc.getClass().getMethod("matches", Method.class,
                            Class.class);
                    for (Method m : targetClass.getDeclaredMethods()) {
                        // Spring only allows proxying of public classes
                        if (Modifier.isPublic(m.getModifiers())) {
                            boolean matches = (Boolean) matchesMethod.invoke(pc, m, targetClass);
                            if (matches) {
                                IMethod jdtMethod = AopReferenceModelUtils.getMethod(jdtTargetType, m
                                        .getName(), m.getParameterTypes().length);
                                IMethod jdtAspectMethod = AopReferenceModelUtils.getMethod(jdtAspectType,
                                        info.getAdviceMethodName(), info.getAdviceMethod()
                                                .getParameterTypes().length);
                                if (jdtAspectMethod != null) {
                                    IAopReference ref = new AopReference(info.getType(),
                                            jdtAspectMethod, jdtMethod, info, file, bean);
                                    aopProject.addAopReference(ref);
                                }
                            }
                        }
                    }
                }
            }
            catch (Throwable t) {
                handleException(t, info, file);
            }
        }
    }

    private static void handleException(Throwable t, IAspectDefinition info, IResource file) {
        if (t instanceof NoClassDefFoundError || t instanceof ClassNotFoundException) {
        	AopReferenceModelMarkerUtils.createProblemMarker(file,
                    "Build path is incomplete. Cannot find class file for " + t.getMessage(),
                    IMarker.SEVERITY_ERROR, info.getAspectLineNumber(),
                    AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
        }
        else if (t instanceof IllegalArgumentException) {
            AopReferenceModelMarkerUtils.createProblemMarker(file, t.getMessage(), IMarker.SEVERITY_ERROR,
                    info.getAspectLineNumber(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
        }
        else if (t instanceof InvocationTargetException) {
            if (t.getCause() != null) {
                handleException(t.getCause(), info, file);
            }
            else {
                Activator.log(t);
                AopReferenceModelMarkerUtils.createProblemMarker(file, t.getMessage(),
                        IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
                        AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
            }
        }
        else {
            Activator.log(t);
            AopReferenceModelMarkerUtils.createProblemMarker(file, t.getMessage(), IMarker.SEVERITY_WARNING,
                    info.getAspectLineNumber(), AopReferenceModelMarkerUtils.AOP_PROBLEM_MARKER, file);
        }
    }

    public static Job getBuildJob(final IProject project, final Set<IFile> filesToBuild) {
        Job buildJob = new BuildJob("Building Spring AOP reference model", project, filesToBuild);
        buildJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
        buildJob.setUser(false);
        return buildJob;
    }

    private static final class BuildJob
            extends Job {

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
                Job[] buildJobs = Platform.getJobManager()
                        .find(ResourcesPlugin.FAMILY_MANUAL_BUILD);
                for (int i = 0; i < buildJobs.length; i++) {
                    Job curr = buildJobs[i];
                    if (curr != this && curr instanceof BuildJob) {
                        BuildJob job = (BuildJob) curr;
                        if (job.isCoveredBy(this)) {
                            curr.cancel(); // cancel all other build jobs of our kind
                        }
                    }
                }
            }
            try {
                monitor.beginTask("Builder Spring AOP reference model", filesToBuild.size());
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
