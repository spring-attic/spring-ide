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
import org.springframework.aop.aspectj.AspectJAdviceParameterNameDiscoverer;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.internal.AopMethodElement;
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
public class BeansWeavingProjectBuilder implements IProjectBuilder {

    public void build(IFile file, IProgressMonitor monitor) {

        Set<IFile> filesToBuild = BeansWeavingUtils.getFilesToBuild(file);
        if (filesToBuild.size() == 0 && BeansCoreUtils.isBeansConfig(file)) {
            filesToBuild.add(file);
        }

        monitor.beginTask(BeansCorePlugin.getFormattedMessage(
                "BeansProjectValidator.validateFile", file.getFullPath()
                        .toString()), filesToBuild.size());
        int work = 0;
        for (IFile currentFile : filesToBuild) {

            // TODO reduce scope here
            BeansWeavingMarkerUtils.deleteProblemMarkers(currentFile.getProject());

            monitor.worked(work++);

            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            IAopProject aopProject = parseFile(currentFile);

            List<IAopReference> references = aopProject.getAllReferences();
            for (IAopReference reference : references) {
                BeansWeavingMarkerUtils.createMarker(reference);
            }
            monitor.done();
        }
    }

    private IAopProject parseFile(IFile currentFile) {
        IBeansProject project = BeansCorePlugin.getModel().getProject(
                currentFile.getProject());
        BeansConfig config = (BeansConfig) project.getConfig(currentFile);

        IAopProject aopProject = BeansWeavingPlugin.getModel().getProject(
                config.getElementResource().getProject());
        aopProject.clearReferencesForResource(currentFile);

        try {
            IDOMDocument document = ((DOMModelImpl) StructuredModelManager
                    .getModelManager().getModelForRead(currentFile)).getDocument();
            List<BeanAspectDefinition> aspectInfos = BeanAspectDefinitionParser.parse(document, currentFile);
            for (BeanAspectDefinition info : aspectInfos) {
                buildModel(config, info);
            }
        }
        catch (IOException e) {
        }
        catch (CoreException e) {
        }
        return aopProject;
    }

    private void buildModel(IBeansConfig config, BeanAspectDefinition info) {

        IResource file = config.getElementResource();
        IJavaProject project = BeansWeavingUtils.getJavaProject(config);
        IAopProject aopProject = BeansWeavingPlugin.getModel().getProject(
                config.getElementResource().getProject());

        ClassLoader weavingClassLoader = BeansWeavingUtils
                .getProjectClassLoader(project);
        ClassLoader classLoader = Thread.currentThread()
                .getContextClassLoader();
        Thread.currentThread().setContextClassLoader(weavingClassLoader);

        Set<String> beanClasses = config.getBeanClasses();
        for (String beanClass : beanClasses) {
            try {

                Class targetClass = weavingClassLoader.loadClass(beanClass);
                Class aspectClass = weavingClassLoader
                        .loadClass(info.getClassName());

                Method aspectMethod = BeanUtils.resolveSignature(
                        info.getMethod(), aspectClass);

                AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
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
                
                List<IMethod> matchingMethods = advice.getMatches(targetClass);

                for (IMethod method : matchingMethods) {
                    IType jdtAspectType = BeansModelUtils.getJavaType(
                            aopProject.getProject(), aspectClass.getName());
                    IMethod jdtAspectMethod = BeansWeavingUtils.getMethod(
                            jdtAspectType, info.getMethod(), aspectMethod
                                    .getParameterTypes().length);
                    IMethod source = new AopMethodElement(config
                            .getElementResource(), info.getNode(), info.getDocument(),
                            jdtAspectMethod);
                    IAopReference ref = new AopReference(info.getType(), source, method);
                    aopProject.addAopReference(ref);
                }
            }
            catch (Exception e) {
                SpringCore.log(e);
            }
            finally {
                Thread.currentThread().setContextClassLoader(classLoader);
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
                jdtMethod = BeansWeavingUtils.getMethod(type, methodName,
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

        private String throwing = null;
        
        private String returning = null;
        
        public InternalAspectJAdvice(IProject project,
                Method aspectJAdviceMethod,
                AspectJExpressionPointcut pointcutExpression) throws Exception {
            super(aspectJAdviceMethod, pointcutExpression, null);
            String name = aspectJAdviceMethod.getDeclaringClass().getName();
            this.type = BeansModelUtils.getJavaType(project, name);
            this.project = project;
            afterPropertiesSet();
        }

        public List<IMethod> getMatches(Class clazz) {
            IType jdtTargetClass = BeansModelUtils.getJavaType(project, clazz
                    .getName());
            Method[] methods = clazz.getDeclaredMethods();
            List<IMethod> matchingMethod = new ArrayList<IMethod>();
            for (Method method : methods) {
                if (getPointcut().matches(method, clazz)) {
                    IMethod jdtMethod = BeansWeavingUtils.getMethod(
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
            // We need to discover them, or if that fails, guess,
            // and if we can't guess with 100% accuracy, fail.
            PrioritizedParameterNameDiscoverer discoverer = new PrioritizedParameterNameDiscoverer();
            discoverer.addDiscoverer(new JdtParameterNameDiscoverer(this.type));
            AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer = 
                new AspectJAdviceParameterNameDiscoverer(getPointcut().getExpression());
            adviceParameterNameDiscoverer.setReturningName(this.returning);
            adviceParameterNameDiscoverer.setThrowingName(this.throwing);
            // last in chain, so if we're called and we fail, that's bad...
            adviceParameterNameDiscoverer.setRaiseExceptions(true);
            discoverer.addDiscoverer(adviceParameterNameDiscoverer);
            return discoverer;
        }
        
        public void setReturningName(String name) {
            setReturningNameNoCheck(name);
            this.returning = name;
        }

        public void setThrowingName(String name) {
            setThrowingNameNoCheck(name);
            this.throwing = name;
        }
    }
}
