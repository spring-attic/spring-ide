/*******************************************************************************
 * Copyright (c) 2013, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.util.Util;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig.InternalScannedGenericBeanDefinition;
import org.springframework.ide.eclipse.beans.core.internal.model.process.BeansConfigPostProcessorFactory;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigEventListener;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IReloadableBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.beans.core.namespaces.IModelElementProvider;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.java.classreading.CachingJdtMetadataReaderFactory;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.java.JavaModelSourceLocation;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * This class defines a Spring beans configuration based on a Spring JavaConfig class.
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
@SuppressWarnings("restriction")
public class BeansJavaConfig extends AbstractBeansConfig implements IBeansConfig, ILazyInitializedModelElement, IReloadableBeansConfig {

	private IType configClass;
	private String configClassName;

	private BeansConfigProblemReporter problemReporter;
	private UniqueBeanNameGenerator beanNameGenerator;
	private ScannedGenericBeanDefinitionSuppressingBeanDefinitionRegistry registry;

	/** Internal cache for all children */
	private transient IModelElement[] children;
	
	public BeansJavaConfig(IBeansProject project, IType configClass, String configClassName, Type type) {
		super(project, BeansConfigFactory.JAVA_CONFIG_TYPE + configClassName, type);
		
		this.configClass = configClass;
		this.configClassName = configClassName;

		modificationTimestamp = IResource.NULL_STAMP;
		
		if (this.configClass != null) {
			IResource resource = this.configClass.getResource();
			if (resource != null && resource instanceof IFile) {
				file = (IFile) resource;
			}
			else {
				IClassFile classFile = configClass.getClassFile();
				
				PackageFragment pkg = (PackageFragment) configClass.getPackageFragment();
				IPackageFragmentRoot root = (IPackageFragmentRoot) pkg.getParent();

				if (root.isArchive()) {
					IPath zipPath = root.getPath();

					String classFileName = classFile.getElementName();
					String path = Util.concatWith(pkg.names, classFileName, '/');
					file = new ExternalFile(zipPath.toFile(), path, project.getProject());
				}
			}
		}
		
		if (file == null || !file.exists()) {
			modificationTimestamp = IResource.NULL_STAMP;
			String msg = "Beans Java config class '" + configClassName + "' not accessible";
			problems = new CopyOnWriteArraySet<ValidationProblem>();
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, msg, file, -1));
		}
		else {
			modificationTimestamp = file.getModificationStamp();
		}

	}

	public IType getConfigClass() {
		return this.configClass;
	}
	
	public String getConfigClassName() {
		return this.configClassName;
	}

	public boolean isInitialized() {
		return isModelPopulated;
	}

	@Override
	public IModelElement[] getElementChildren() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return children;
		}
		finally {
			r.unlock();
		}
	}
	
	@Override
	public Set<IBeansImport> getImports() {
		return Collections.emptySet();
	}
	
	@Override
	public int getElementStartLine() {
		return JdtUtils.getLineNumber(configClass);
	}

	@Override
	protected void readConfig() {
		if (!isModelPopulated) {

			w.lock();
			if (this.isModelPopulated) {
				w.unlock();
				return;
			}
			
			try {
				if (this.configClass == null) {
					return;
				}
				
				IBeansProject beansProject = BeansModelUtils.getParentOfClass(this, IBeansProject.class);
				if (beansProject == null) {
					return;
				}
				
				final ClassLoader cl = JdtUtils.getClassLoader(beansProject.getProject(), ApplicationContext.class.getClassLoader());
				
				if (cl.getResource(this.configClass.getFullyQualifiedName().replace('.', '/') + ".class") == null) {
					return;
				}
					
				Callable<Integer> loadBeanDefinitionOperation = new Callable<Integer>() {
					public Integer call() throws Exception {
						// Obtain thread context classloader and override with the project classloader
						ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
						Thread.currentThread().setContextClassLoader(cl);
	
						// Create special ReaderEventListener that essentially just passes through component definitions
						ReaderEventListener eventListener = new BeansConfigPostProcessorReaderEventListener();
						problemReporter = new BeansConfigProblemReporter();
						beanNameGenerator = new UniqueBeanNameGenerator(BeansJavaConfig.this);
						registry = new ScannedGenericBeanDefinitionSuppressingBeanDefinitionRegistry();
	
						try {
							registerAnnotationProcessors(eventListener);
							registerBean(eventListener, cl);
	
							IBeansConfigPostProcessor[] postProcessors = BeansConfigPostProcessorFactory.createPostProcessor(ConfigurationClassPostProcessor.class.getName());
							for (IBeansConfigPostProcessor postProcessor : postProcessors) {
								executePostProcessor(postProcessor, eventListener);
							}
						}
						finally {
							// Reset the context classloader
							Thread.currentThread().setContextClassLoader(threadClassLoader);
						}
						return 0;
					}
				};

				FutureTask<Integer> task = new FutureTask<Integer>(loadBeanDefinitionOperation);
				BeansCorePlugin.getExecutorService().submit(task);
				task.get(BeansCorePlugin.getDefault().getPreferenceStore().getInt(BeansCorePlugin.TIMEOUT_CONFIG_LOADING_PREFERENCE_ID),
						TimeUnit.SECONDS);
			}
			catch (TimeoutException e) {
				problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, "Loading of configuration '"
						+ this.configClass.getFullyQualifiedName() + "' took more than "
						+ BeansCorePlugin.getDefault().getPreferenceStore()
						.getInt(BeansCorePlugin.TIMEOUT_CONFIG_LOADING_PREFERENCE_ID) + "sec",
						file, 1));
			}
			catch (Exception e) {
				problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, String.format(
						"Error occured processing Java config '%s'. See Error Log for more details", e.getCause().getMessage()), getElementResource()));
				BeansCorePlugin.log(new Status(IStatus.INFO, BeansCorePlugin.PLUGIN_ID, String.format(
						"Error occured processing '%s'", this.configClass.getFullyQualifiedName()), e.getCause()));
			}
			finally {
				// Prepare the internal cache of all children for faster access
				List<ISourceModelElement> allChildren = new ArrayList<ISourceModelElement>(imports);
				allChildren.addAll(aliases.values());
				allChildren.addAll(components);
				allChildren.addAll(beans.values());
				Collections.sort(allChildren, new Comparator<ISourceModelElement>() {
					public int compare(ISourceModelElement element1, ISourceModelElement element2) {
						return element1.getElementStartLine() - element2.getElementStartLine();
					}
				});
				this.children = allChildren.toArray(new IModelElement[allChildren.size()]);
				
				this.isModelPopulated = true;
				w.unlock();
			}

		}
	}

	/**
	 * Sets internal list of {@link IBean}s to <code>null</code>. Any further access to the data of this instance of
	 * {@link IBeansConfig} leads to reloading of the corresponding beans config file.
	 */
	public void reload() {
		if (configClass != null) {
			try {
				w.lock();
				// System.out.println(String.format("++- resetting config '%s'", file.getFullPath().toString()));
				isModelPopulated = false;
				modificationTimestamp = IResource.NULL_STAMP;
				defaults = null;
				imports.clear();
				aliases.clear();
				beans.clear();
				components.clear();
				isBeanClassesMapPopulated = false;
				beanClassesMap.clear();
				problems.clear();
				children = null;
				//				componentDefinitions.clear();
			}
			finally {
				w.unlock();
			}

			// Reset all config sets which contain this config
			for (IBeansConfigEventListener eventListener : eventListeners) {
				eventListener.onReset(this);
			}
		}
	}

	/**
	 * Safely execute the given {@link IBeansConfigPostProcessor}.
	 */
	private void executePostProcessor(final IBeansConfigPostProcessor postProcessor,
			final ReaderEventListener eventListener) {
		SafeRunner.run(new ISafeRunnable() {

			public void handleException(Throwable exception) {
				BeansCorePlugin.log(exception);
			}

			public void run() throws Exception {
				postProcessor.postProcess(BeansConfigPostProcessorFactory.createPostProcessingContext(BeansJavaConfig.this,
						beans.values(), eventListener, problemReporter, beanNameGenerator, registry, problems));
			}
		});
	}

	/**
	 * Registers the given component definition with this {@link BeansConfig}'s beans and component storage.
	 */
	private void registerComponentDefinition(ComponentDefinition componentDefinition,
			Map<String, IModelElementProvider> elementProviders) {
		String uri = NamespaceUtils.getNameSpaceURI(componentDefinition);
		IModelElementProvider provider = elementProviders.get(uri);
		if (provider == null) {
			provider = BeansConfig.DEFAULT_ELEMENT_PROVIDER;
		}
		ISourceModelElement element = provider.getElement(BeansJavaConfig.this, componentDefinition);
		if (element instanceof IBean) {
			beans.put(element.getElementName(), (IBean) element);
		}
		else if (element instanceof IBeansComponent) {
			components.add((IBeansComponent) element);
		}
	}

	public void registerBean(ReaderEventListener eventListener, ClassLoader classloader) throws IOException {
		IJavaProject project = this.configClass.getJavaProject();
		if (project == null) {
			return;
		}

		CachingJdtMetadataReaderFactory metadataReaderFactory = new CachingJdtMetadataReaderFactory(project, classloader);
		MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(this.configClass.getFullyQualifiedName());

		AnnotatedGenericBeanDefinition abd = new AnnotatedGenericBeanDefinition(metadataReader.getAnnotationMetadata());

		//		AnnotationMetadata metadata = abd.getMetadata();
		//		if (metadata.isAnnotated(Profile.class.getName())) {
		//			AnnotationAttributes profile = MetadataUtils.attributesFor(metadata, Profile.class);
		//			if (!this.environment.acceptsProfiles(profile.getStringArray("value"))) {
		//				return;
		//			}
		//		}
		//		ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(abd);
		//		abd.setScope(scopeMetadata.getScopeName());
		abd.setScope(BeanDefinition.SCOPE_SINGLETON);

		AnnotationBeanNameGenerator nameGenerator = new AnnotationBeanNameGenerator();
		String beanName = nameGenerator.generateBeanName(abd, this.registry);
		//		AnnotationConfigUtils.processCommonDefinitionAnnotations(abd);
		//		if (qualifiers != null) {
		//			for (Class<? extends Annotation> qualifier : qualifiers) {
		//				if (Primary.class.equals(qualifier)) {
		//					abd.setPrimary(true);
		//				}
		//				else if (Lazy.class.equals(qualifier)) {
		//					abd.setLazyInit(true);
		//				}
		//				else {
		//					abd.addQualifier(new AutowireCandidateQualifier(qualifier));
		//				}
		//			}
		//		}
		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(abd, beanName);
		//		definitionHolder = AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
		eventListener.componentRegistered(new BeanComponentDefinition(abd,beanName));
	}

	/**
	 * register the default annotation processors
	 */
	protected void registerAnnotationProcessors(ReaderEventListener eventListener) {
		Set<BeanDefinitionHolder> processorDefinitions = AnnotationConfigUtils.registerAnnotationConfigProcessors(registry, null);
		
		// Nest the concrete beans in the surrounding component.
		for (BeanDefinitionHolder processorDefinition : processorDefinitions) {
			eventListener.componentRegistered(new BeanComponentDefinition(processorDefinition));
		}
	}

	class BeansConfigPostProcessorReaderEventListener extends EmptyReaderEventListener {

		// Keep the contributed model element providers
		final Map<String, IModelElementProvider> elementProviders = NamespaceUtils.getElementProviders();

		@Override
		public void componentRegistered(ComponentDefinition componentDefinition) {
			// make sure that all components that come through are safe for the model
			if (componentDefinition.getSource() == null) {
				if (componentDefinition instanceof BeanComponentDefinition) {
					try {
						((AbstractBeanDefinition) ((BeanComponentDefinition) componentDefinition).getBeanDefinition())
						.setSource(new JavaModelSourceLocation(configClass));
					} catch (JavaModelException e) {
						SpringCore.log(e);
					}
				}
			}
			registerComponentDefinition(componentDefinition, elementProviders);
		}
	}

	class BeansConfigProblemReporter implements ProblemReporter {
		public void error(Problem problem) {
		}

		public void fatal(Problem problem) {
		}

		public void warning(Problem problem) {
		}
	}

	class ScannedGenericBeanDefinitionSuppressingBeanDefinitionRegistry extends SimpleBeanDefinitionRegistry {

		@Override
		public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
				throws BeanDefinitionStoreException {
			if (beanDefinition instanceof ScannedGenericBeanDefinition) {
				super.registerBeanDefinition(beanName, new InternalScannedGenericBeanDefinition(
						(ScannedGenericBeanDefinition) beanDefinition));
			}
			else {
				super.registerBeanDefinition(beanName, beanDefinition);
			}
		}
	}

	public boolean doesAnnotationScanning() {
		return true;
	}

}
