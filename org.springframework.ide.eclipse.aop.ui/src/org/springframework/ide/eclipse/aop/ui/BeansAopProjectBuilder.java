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

package org.springframework.ide.eclipse.aop.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.AopReference;
import org.springframework.ide.eclipse.aop.ui.support.AbstractAspectJAdvice;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

@SuppressWarnings("restriction")
public class BeansAopProjectBuilder implements IProjectBuilder {

    public void build(IFile file, IProgressMonitor monitor) {

        Set<IFile> filesToBuild = BeansAopUtils.getFilesToBuild(file);
        if (filesToBuild.size() == 0 && BeansCoreUtils.isBeansConfig(file)) {
            filesToBuild.add(file);
        }

        monitor.beginTask(BeansCorePlugin.getFormattedMessage(
                "BeansProjectValidator.validateFile", file.getFullPath()
                        .toString()), filesToBuild.size());
        int work = 0;
        for (IFile currentFile : filesToBuild) {

            // TODO reduce scope here
            BeansAopMarkerUtils.deleteProblemMarkers(currentFile
                    .getProject());

            monitor.worked(work++);

            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            IAopProject aopProject = parseFile(currentFile);

            List<IAopReference> references = aopProject.getAllReferences();
            for (IAopReference reference : references) {
                BeansAopMarkerUtils.createMarker(reference);
            }
            monitor.done();
        }
    }

    private IAopProject parseFile(IFile currentFile) {
        IBeansProject project = BeansCorePlugin.getModel().getProject(
                currentFile.getProject());
        BeansConfig config = (BeansConfig) project.getConfig(currentFile);
        IJavaProject javaProject = BeansAopUtils.getJavaProject(config);
        IAopProject aopProject = BeansAopPlugin.getModel().getProject(
                config.getElementResource().getProject());

        aopProject.clearReferencesForResource(currentFile);

        ClassLoader weavingClassLoader = BeansAopUtils
                .getProjectClassLoader(javaProject);
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        Thread.currentThread().setContextClassLoader(weavingClassLoader);

        try {
            IDOMDocument document = ((DOMModelImpl) StructuredModelManager
                    .getModelManager().getModelForRead(currentFile))
                    .getDocument();
            List<BeanAspectDefinition> aspectInfos = BeanAspectDefinitionParser
                    .parse(document, currentFile);
            for (BeanAspectDefinition info : aspectInfos) {
                buildModel(weavingClassLoader, config, info);
            }
        }
        catch (IOException e) {
        }
        catch (CoreException e) {
        }
        finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
        return aopProject;
    }

    private void buildModel(ClassLoader loader, IBeansConfig config,
            BeanAspectDefinition info) {

        IResource file = config.getElementResource();
        IAopProject aopProject = BeansAopPlugin.getModel().getProject(
                config.getElementResource().getProject());

        Set<String> beanClasses = config.getBeanClasses();
        for (String beanClass : beanClasses) {
            try {

                Class targetClass = loader.loadClass(beanClass);
                Class aspectClass = loader.loadClass(info.getClassName());

                Method aspectMethod = BeanUtils.resolveSignature(info
                        .getMethod(), aspectClass);

                AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
                pc.setPointcutDeclarationScope(targetClass);
                pc.setExpression(info.getPointcut());
                if (info.getArgNames() != null) {
                    pc.setParameterNames(info.getArgNames());
                }
                InternalAspectJAdvice advice = new InternalAspectJAdvice(file
                        .getProject(), aspectMethod, pc);
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
                                aopProject.getProject(), aspectClass.getName());
                        IMethod jdtAspectMethod = BeansAopUtils.getMethod(
                                jdtAspectType, info.getMethod(), aspectMethod
                                        .getParameterTypes().length);
                        IAopReference ref = new AopReference(info.getType(),
                        		jdtAspectMethod, method, info, file);
                        aopProject.addAopReference(ref);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            catch (Exception e) {
                SpringCore.log(e);
            }
        }
    }

    private class JdtParameterNameDiscoverer implements ParameterNameDiscoverer {

        private IType type;

        public JdtParameterNameDiscoverer(IType type) {
            this.type = type;
        }

        public String[] getParameterNames(Method method) {
            String methodName = method.getName();
            int argCount = method.getParameterTypes().length;
            IMethod jdtMethod;
            try {
                jdtMethod = BeansAopUtils.getMethod(type, methodName,
                        argCount);
                return jdtMethod.getParameterNames();
            }
            catch (JavaModelException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String[] getParameterNames(Constructor ctor) {
            return null;
        }
    }

    private class InternalAspectJAdvice
            extends AbstractAspectJAdvice {

        private IType type;

        private IProject project;

        public InternalAspectJAdvice(IProject project,
                Method aspectJAdviceMethod,
                AspectJExpressionPointcut pointcutExpression) throws Exception {
            super(aspectJAdviceMethod, pointcutExpression, null);
            String name = aspectJAdviceMethod.getDeclaringClass().getName();
            this.type = BeansModelUtils.getJavaType(project, name);
            this.project = project;
        }

        public List<IMethod> getMatches(Class clazz) {
            IType jdtTargetClass = BeansModelUtils.getJavaType(project, clazz
                    .getName());
            Method[] methods = clazz.getDeclaredMethods();
            List<IMethod> matchingMethod = new ArrayList<IMethod>();
            for (Method method : methods) {
                if (getPointcut().matches(method, clazz)) {
                    IMethod jdtMethod = BeansAopUtils.getMethod(
                            jdtTargetClass, method.getName(), method
                                    .getParameterTypes().length);
                    if (jdtMethod != null) {
                        matchingMethod.add(jdtMethod);
                    }
                }
            }
            return matchingMethod;
        }

        public boolean isAfterAdvice() {
            return true;
        }

        public boolean isBeforeAdvice() {
            return true;
        }

        protected ParameterNameDiscoverer createParameterNameDiscoverer() {
            return new JdtParameterNameDiscoverer(this.type);
        }

        public void setReturningName(String name) {
            setReturningNameNoCheck(name);
        }

        public void setThrowingName(String name) {
            setThrowingNameNoCheck(name);
        }
    }
}
