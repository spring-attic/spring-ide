/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.core.builder;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
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
import org.springframework.ide.eclipse.aop.core.model.internal.AopReference;
import org.springframework.ide.eclipse.aop.core.parser.BeanAspectDefinitionParser;
import org.springframework.ide.eclipse.aop.core.util.BeansAopMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.BeansAopUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansAopProjectBuilder implements IProjectBuilder {

    public void build(IFile file, IProgressMonitor monitor) {

        Set<IFile> filesToBuild = BeansAopUtils.getFilesToBuild(file);

        monitor.beginTask(BeansCorePlugin.getFormattedMessage(
                "BeansProjectValidator.validateFile", file.getFullPath()
                        .toString()), filesToBuild.size());
        int work = 0;
        for (IFile currentFile : filesToBuild) {

            BeansAopMarkerUtils.deleteProblemMarkers(currentFile);

            monitor.worked(work++);

            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            IAopProject aopProject = buildAopReferencesFromFile(currentFile);
            if (aopProject != null) {
                List<IAopReference> references = aopProject.getAllReferences();
                for (IAopReference reference : references) {
                    if (reference.getResource().equals(currentFile)) {
                        BeansAopMarkerUtils
                                .createMarker(reference, currentFile);
                    }
                }
            }

            // update images and text decoractions
            Activator.getModel().fireModelChanged();

            monitor.done();
        }
    }

    private IAopProject buildAopReferencesFromFile(IFile currentFile) {
        IAopProject aopProject = null;
        IBeansProject project = BeansCorePlugin.getModel().getProject(
                currentFile.getProject());
        
        if (project != null) {
            BeansConfig config = (BeansConfig) project.getConfig(currentFile);
            IJavaProject javaProject = BeansAopUtils.getJavaProject(config);
            
            aopProject = Activator.getModel().getProject(
                    config.getElementResource().getProject());

            aopProject.clearReferencesForResource(currentFile);

            ClassLoader weavingClassLoader = SpringCoreUtils
                    .getClassLoader(javaProject);
            ClassLoader classLoader = Thread.currentThread()
                    .getContextClassLoader();
            Thread.currentThread().setContextClassLoader(weavingClassLoader);

            IStructuredModel model = null;
            try {
                model = StructuredModelManager.getModelManager()
                        .getModelForRead(currentFile);
                IDOMDocument document = ((DOMModelImpl) model).getDocument();
                List<IAspectDefinition> aspectInfos = BeanAspectDefinitionParser
                        .buildAspectDefinitions(document, currentFile);
                for (IAspectDefinition info : aspectInfos) {
                    buildAopReferencesFromAspectDefinition(weavingClassLoader, config, info);
                }
            }
            catch (IOException e) {
                BeansCorePlugin.log(e);
            }
            catch (CoreException e) {
                BeansCorePlugin.log(e);
            }
            finally {
                Thread.currentThread().setContextClassLoader(classLoader);
                if (model != null) {
                    model.releaseFromRead();
                }
            }
        }
        return aopProject;
    }

    private void buildAopReferencesFromAspectDefinition(ClassLoader loader, IBeansConfig config,
            IAspectDefinition info) {

        IResource file = config.getElementResource();
        IAopProject aopProject = Activator.getModel().getProject(
                config.getElementResource().getProject());

        Set<IBean> beans = config.getBeans();
        for (IBean bean : beans) {
            try {

                Class targetClass = loader.loadClass(bean.getClassName());
                JdtAwareAspectJAdviceMatcher advice = new JdtAwareAspectJAdviceMatcher(file
                        .getProject(), info.getAdviceMethod(), info
                        .getPointcut());
                if (info.getThrowing() != null) {
                    advice.setThrowingName(info.getThrowing());
                }
                if (info.getReturning() != null) {
                    advice.setReturningName(info.getReturning());
                }
                advice.afterPropertiesSet();

                try {
                    List<IMethod> matchingMethods = advice
                            .getMatches(targetClass);
                    for (IMethod method : matchingMethods) {
                        IType jdtAspectType = BeansModelUtils.getJavaType(
                                aopProject.getProject(), info.getClassName());
                        IMethod jdtAspectMethod = BeansAopUtils
                                .getMethod(jdtAspectType, info.getMethod(),
                                        info.getAdviceMethod()
                                                .getParameterTypes().length);
                        IAopReference ref = new AopReference(info.getType(),
                                jdtAspectMethod, method, info, file, bean);
                        aopProject.addAopReference(ref);
                    }
                }
                catch (IllegalArgumentException e) {
                    BeansAopMarkerUtils.createProblemMarker(file, e
                            .getMessage(), IMarker.SEVERITY_ERROR, info
                            .getAspectLineNumber(),
                            BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
                }
                catch (Exception e) {
                    // suppress this
                }
            }
            catch (NoClassDefFoundError e) {
                BeansAopMarkerUtils.createProblemMarker(file,
                        "Class dependency is missing: " + e.getMessage(),
                        IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
                        BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
            }
            catch (Throwable t) {
                BeansAopMarkerUtils.createProblemMarker(file, t.getMessage(),
                        IMarker.SEVERITY_WARNING, info.getAspectLineNumber(),
                        BeansAopMarkerUtils.AOP_PROBLEM_MARKER, file);
            }
        }
    }
}
