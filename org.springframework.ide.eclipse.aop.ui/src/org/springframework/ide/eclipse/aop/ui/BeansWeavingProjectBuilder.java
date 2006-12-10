package org.springframework.ide.eclipse.aop.ui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.ide.eclipse.aop.ui.support.AbstractAspectJAdvice;
import org.springframework.ide.eclipse.aop.ui.support.IAopProjectMarker;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.Introspector.Statics;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.editor.BeansEditorUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.util.StringUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class BeansWeavingProjectBuilder implements IProjectBuilder {

	@SuppressWarnings("restriction")
	public void build(IFile file, IProgressMonitor monitor) {

		if (BeansCoreUtils.isBeansConfig(file)) {
			monitor.beginTask(BeansCorePlugin.getFormattedMessage(
					"BeansProjectValidator.validateFile", file.getFullPath()
							.toString()), IProgressMonitor.UNKNOWN);
			monitor.worked(1);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			BeansWeavingUtils.deleteProblemMarkers(file.getProject());

			// Validate the modified config file
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					file.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);

			try {
				IDOMDocument test = ((DOMModelImpl) StructuredModelManager
						.getModelManager().getModelForRead(file)).getDocument();
				NodeList list = test.getDocumentElement()
						.getElementsByTagNameNS(
								"http://www.springframework.org/schema/aop",
								"config");

				for (int i = 0; i < list.getLength(); i++) {
					Node node = list.item(i);
					NodeList children = node.getChildNodes();
					for (int j = 0; j < children.getLength(); j++) {
						Node child = children.item(j);
						if ("aspect".equals(child.getLocalName())) {
							String beanRef = BeansEditorUtils.getAttribute(
									child, "ref");
							String className = BeansEditorUtils
									.getClassNameForBean(file, child
											.getOwnerDocument(), beanRef);
							NodeList aspectChildren = child.getChildNodes();
							for (int g = 0; g < aspectChildren.getLength(); g++) {
								Node aspectNode = aspectChildren.item(g);
								if ("before".equals(aspectNode.getLocalName())) {
									String pointcut = BeansEditorUtils
											.getAttribute(aspectNode,
													"pointcut");
									String argNames = BeansEditorUtils
											.getAttribute(aspectNode,
													"arg-names");
									String method = BeansEditorUtils
											.getAttribute(aspectNode, "method");
									String[] argNamesArray = null;
									if (argNames != null) {
										argNamesArray = StringUtils
												.commaDelimitedListToStringArray(argNames);
									}
									processFile(config, className, method,
											argNamesArray, pointcut, test,
											(IDOMNode) aspectNode);
								}
							}
						}
					}
				}

			} catch (IOException e) {
			} catch (CoreException e) {
			}

			// processFIle(file);

			monitor.done();
		}
	}

	private void processFile(IBeansConfig config,
			String aspectBackingClassName, String aspectMethodName,
			String[] parameterNames, String pointcut, IDOMDocument document,
			IDOMNode node) {

		IResource file = config.getElementResource();
		IJavaProject project = JavaCore.create(config.getElementResource()
				.getProject());

		ClassLoader loader = getProjectClassLoader(project);
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();

		Set<String> beanClasses = config.getBeanClasses();
		for (String beanClass : beanClasses) {
			try {
				Thread.currentThread().setContextClassLoader(loader);

				Class clazz = loader.loadClass(beanClass);
				Class aspect = loader.loadClass(aspectBackingClassName);

				Method aspectMethod = BeanUtils.resolveSignature(
						aspectMethodName, aspect);

				AspectJExpressionPointcut pc = new AspectJExpressionPointcut();
				pc.setExpression(pointcut);
				if (parameterNames != null) {
					pc.setParameterNames(parameterNames);
				}
				InternalAspectJAdvice advice = new InternalAspectJAdvice(file
						.getProject(), aspectMethod, pc);

				List<IMethod> matchingMethods = advice.getMatches(clazz);

				for (IMethod method : matchingMethods) {
					createTargetMarker(method, "Advised by before advice");
					createSourceMarker(config.getElementResource(), document,
							node, "Advises " + method.toString());
				}

			} catch (Exception e) {
				SpringCore.log(e);
			} finally {
				Thread.currentThread().setContextClassLoader(classLoader);
			}
		}
	}

	public URLClassLoader getProjectClassLoader(IJavaProject project) {
		List<URL> paths = getProjectClassPathURLs(project);
		URL pathUrls[] = (URL[]) paths.toArray(new URL[0]);
		return new URLClassLoader(pathUrls, Thread.currentThread()
				.getContextClassLoader());
	}

	public List<URL> getProjectClassPathURLs(IJavaProject project) {
		List<URL> paths = new ArrayList<URL>();
		try {
			// configured classpath
			IClasspathEntry classpath[] = project.getRawClasspath();
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry path = classpath[i];
				if (path.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					URL url = path.getPath().toFile().toURL();
					paths.add(url);
				}
			}
			// build output, relative to project
			IPath location = getProjectLocation(project.getProject());
			IPath outputPath = location.append(project.getOutputLocation()
					.removeFirstSegments(1));
			paths.add(outputPath.toFile().toURL());
		} catch (Exception e) {
		}
		return paths;
	}

	public IPath getProjectLocation(IProject project) {
		if (project.getRawLocation() == null) {
			return project.getLocation();
		} else {
			return project.getRawLocation();
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
				jdtMethod = getMethod(type, methodName, argCount);
				return jdtMethod.getParameterNames();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			return null;
		}

		public String[] getParameterNames(Constructor ctor) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private class InternalAspectJAdvice extends AbstractAspectJAdvice {

		private IType type;

		private IProject project;

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
					IMethod jdtMethod = getMethod(jdtTargetClass, method
							.getName(), method.getParameterTypes().length);
					if (jdtMethod != null) {
						matchingMethod.add(jdtMethod);
					}
				}
			}
			return matchingMethod;
		}

		public boolean isAfterAdvice() {
			return false;
		}

		public boolean isBeforeAdvice() {
			return false;
		}

		protected ParameterNameDiscoverer createParameterNameDiscoverer() {
			return new JdtParameterNameDiscoverer(this.type);
		}
	}

	private final IMethod getMethod(IType type, String methodName, int argCount) {
		try {
			return Introspector.findMethod(type, methodName, argCount, true,
					Statics.DONT_CARE);
		} catch (JavaModelException e) {
		}
		return null;
	}

	private void createTargetMarker(IMethod method, String message) {
		try {
			BeansWeavingUtils.createProblemMarker(method.getResource(), message, 1,
					getLineNumber(method), IAopProjectMarker.PROBLEM_MARKER);
		} catch (CoreException e) {
		}
	}

	@SuppressWarnings("restriction")
	private void createSourceMarker(IResource file, IDOMDocument document,
			IDOMNode node, String message) {
		int line = document.getStructuredDocument().getLineOfOffset(
				node.getStartOffset()) + 1;
		BeansWeavingUtils.createProblemMarker(file, message, 1, line,
				IAopProjectMarker.BEFORE_ADVICE_MARKER);
	}

	private int getLineNumber(IMethod method) throws JavaModelException {
		if (method != null) {
			int lines = 0;
			String targetsource = method.getDeclaringType()
					.getCompilationUnit().getSource();
			String sourceuptomethod = targetsource.substring(0, method
					.getNameRange().getOffset());

			char[] chars = new char[sourceuptomethod.length()];
			sourceuptomethod.getChars(0, sourceuptomethod.length(), chars, 0);
			for (int j = 0; j < chars.length; j++) {
				if (chars[j] == '\n') {
					lines++;
				}
			}
			return new Integer(lines + 1);
		}
		return new Integer(-1);
	}

}
