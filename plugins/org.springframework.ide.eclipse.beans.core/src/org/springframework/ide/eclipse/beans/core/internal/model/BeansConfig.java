/*******************************************************************************
 * Copyright (c) 2004, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IPersistableElement;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.EmptyReaderEventListener;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.beans.factory.xml.BeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DelegatingNamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DocumentAccessor;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.XmlCatalogDelegatingEntityResolver;
import org.springframework.ide.eclipse.beans.core.internal.model.process.BeansConfigPostProcessorFactory;
import org.springframework.ide.eclipse.beans.core.internal.parser.BeansDtdResolver;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigEventListener;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IReloadableBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.beans.core.namespaces.IModelElementProvider;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.io.EclipsePathMatchingResourcePatternResolver;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.io.StorageResource;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.core.io.xml.XercesDocumentLoader;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.DefaultModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class defines a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Dave Watkins
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class BeansConfig extends AbstractBeansConfig implements IBeansConfig, ILazyInitializedModelElement, IReloadableBeansConfig {

	private static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID + "/model/loading/debug";

	private static final boolean DEBUG = SpringCore.isDebug(DEBUG_OPTION);

	/** The default element provider used for non-namespaced elements */
	public static final IModelElementProvider DEFAULT_ELEMENT_PROVIDER = new DefaultModelElementProvider();

	/** The resource that is currently being processed or null if non is processed */
	private volatile IResource currentResource = null;

	/** The resource that is currently being processed or null if non is processed; just a different type then the above */
	private volatile EncodedResource currentEncodedResource;

	/** {@link IBeansConfigPostProcessor}s that are detected in this configs beans and component map */
	private volatile Set<IBeansConfigPostProcessor> ownPostProcessors = new HashSet<IBeansConfigPostProcessor>();

	/** {@link IBeansConfigPostProcessor}s that have been removed between the two last reads of the backing xml file */
	private volatile Set<IBeansConfigPostProcessor> removedPostProcessors = new HashSet<IBeansConfigPostProcessor>();

	/**
	 * {@link IBeansConfigPostProcessor}s that have been contributed by external {@link IBeansConfig} from other
	 * {@link IBeansConfigSet}
	 */
	private volatile Map<IBeansConfigPostProcessor, Set<IBeansConfig>> externalPostProcessors = new ConcurrentHashMap<IBeansConfigPostProcessor, Set<IBeansConfig>>();

	/** {@link Resource} implementation to be passed to Spring core for reading */
	private volatile Resource resource;

	/** {@link ProblemReporter} implementation for later use */
	private volatile ProblemReporter problemReporter;

	/** {@link BeanNameGenerator} implementation for later use */
	private volatile BeanNameGenerator beanNameGenerator;

	/** {@link BeanDefinitionRegistry} implementation for later use */
	private volatile SimpleBeanDefinitionRegistry registry;

	/** Internal cache for all children */
	private transient IModelElement[] children;
	
	private transient Stack<CompositeComponentDefinition> componentDefinitions = new Stack<CompositeComponentDefinition>();

	/**
	 * Creates a new {@link BeansConfig}.
	 */
	public BeansConfig(IBeansProject project, String name, Type type) {
		super(project, name, type);
		init(name, project);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public boolean isInitialized() {
		return isModelPopulated;
	}

	/**
	 * Sets internal list of {@link IBean}s to <code>null</code>. Any further access to the data of this instance of
	 * {@link IBeansConfig} leads to reloading of the corresponding beans config file.
	 */
	public void reload() {
		if (file != null) {
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
				
				componentDefinitions.clear();

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
	 * Checks the file for the given name. If the given name defines an external resource (leading '/' -> not part of
	 * the project this config belongs to) get the file from the workspace else from the project. If the name specifies
	 * an entry in an archive then the {@link #isArchived} flag is set. If the corresponding file is not available or
	 * accessible then an entry is added to the config's list of errors.
	 * @param project
	 */
	protected void init(String name, IBeansProject project) {
		IContainer container = null;
		String fileName = null;
		String fullPath = null;

		// At first check for a config file in a JAR
		int pos = name.indexOf(ZipEntryStorage.DELIMITER);
		if (pos != -1) {
			isArchived = true;
			fileName = name.substring(0, pos);
		}
		else {
			fileName = name;
		}

		// Now check if is an workspace external resource
		if (fileName.startsWith(EXTERNAL_FILE_NAME_PREFIX)) {

			fileName = fileName.substring(EXTERNAL_FILE_NAME_PREFIX.length());

			// Resolve eventual contained classpath variables
			IPath resolvedPath = JavaCore.getResolvedVariablePath(new Path(fileName));
			if (resolvedPath != null) {
				fileName = resolvedPath.toString();
			}

			// Create an external file instance
			file = new ExternalFile(new File(fileName), name.substring(pos + 1), project.getProject());
		}
		else {
			container = (IProject) ((IResourceModelElement) getElementParent()).getElementResource();
			fullPath = container.getFullPath().append(fileName).toString();

			// Try to find the configuration file in the workspace
			file = (IFile) container.findMember(fileName);
		}

		if (file == null || !file.exists()) {
			modificationTimestamp = IResource.NULL_STAMP;
			String msg = "Beans config file '" + fullPath + "' not accessible";
			problems = new CopyOnWriteArraySet<ValidationProblem>();
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, msg, file, -1));
		}
		else {
			modificationTimestamp = file.getModificationStamp();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readConfig() {
		if (!this.isModelPopulated) {

			long start = System.currentTimeMillis();
			long count = 0;

			// Only install Eclipse-based resource loader if enabled in project properties
			// IMPORTANT: the following block needs to stay before the w.lock()
			// as it could otherwise create a runtime deadlock
			ResourceLoader resourceLoader = null;
			if (getElementParent() instanceof IBeansProject && ((IBeansProject) getElementParent()).isImportsEnabled()) {
				resourceLoader = new EclipsePathMatchingResourcePatternResolver(file.getProject(),
						JdtUtils.getClassLoader(file.getProject(), BeansCorePlugin.getClassLoader()));
			}
			else {
				resourceLoader = new ClassResourceFilteringPatternResolver(file.getProject(), JdtUtils.getClassLoader(
						file.getProject(), BeansCorePlugin.getClassLoader()));
			}

			w.lock();
			if (this.isModelPopulated) {
				w.unlock();
				return;
			}
			try {
				// Publish start events
				for (IBeansConfigEventListener eventListener : eventListeners) {
					eventListener.onReadStart(this);
				}

				if (file != null && file.exists()) {

					modificationTimestamp = file.getModificationStamp();
					if (isArchived) {
						if (file instanceof Resource) {
							resource = (Resource) file;
						}
						else {
							resource = new StorageResource(new ZipEntryStorage(file.getProject(), getElementName()),
									file.getProject());
						}
					}
					else {
						resource = new FileResource(file);
					}

					// Set up classloader to use for NamespaceHandler and XSD loading
					final ClassLoader cl;
					if (NamespaceUtils.useNamespacesFromClasspath(file.getProject())) {
						cl = JdtUtils.getClassLoader(file.getProject(),  BeansCorePlugin.getClassLoader());
					}
					else {
						 cl = BeansCorePlugin.getClassLoader();
					}

					registry = new ScannedGenericBeanDefinitionSuppressingBeanDefinitionRegistry();
					EntityResolver resolver = new XmlCatalogDelegatingEntityResolver(new BeansDtdResolver(), new PluggableSchemaResolver(cl));
					final DocumentAccessor documentAccessor = new DocumentAccessor();
					final SourceExtractor sourceExtractor = new DelegatingSourceExtractor(file.getProject());
					final BeansConfigReaderEventListener eventListener = new BeansConfigReaderEventListener(this, resource, sourceExtractor, documentAccessor);
					final NamespaceHandlerResolver namespaceHandlerResolver = new DelegatingNamespaceHandlerResolver(cl, this,	documentAccessor);
					
					problemReporter = new BeansConfigProblemReporter();
					beanNameGenerator = new UniqueBeanNameGenerator(this);

					final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(registry) {

						@Override
						public int loadBeanDefinitions(EncodedResource encodedResource)
								throws BeanDefinitionStoreException {

							// Capture the current resource being processed to handle parsing exceptions correctly and
							// create the validation error on the correct resource
							if (encodedResource != null && encodedResource.getResource() instanceof IAdaptable) {
								currentResource = (IResource) ((IAdaptable) encodedResource.getResource()).getAdapter(IResource.class);
								currentEncodedResource = encodedResource;
							}

							try {
								// Delegate actual processing to XmlBeanDefinitionReader
								int loadedBeans = 0;
								if (encodedResource.getResource().exists()) {
									loadedBeans = super.loadBeanDefinitions(encodedResource);
								}
								return loadedBeans;
							}
							finally {
								// Reset currently processed resource before leaving
								currentResource = null;
								currentEncodedResource = null;
							}
						}

						@Override
						public int registerBeanDefinitions(Document doc, Resource resource)
								throws BeanDefinitionStoreException {
							try {
								documentAccessor.pushDocument(doc);
								return super.registerBeanDefinitions(doc, resource);
							}
							finally {
								documentAccessor.popDocument();
							}
						}
						
						@Override
						public XmlReaderContext createReaderContext(Resource resource) {
							return new ProfileAwareReaderContext(resource, problemReporter, eventListener,
									sourceExtractor, this, namespaceHandlerResolver);
						}
						
						@Override
						protected BeanDefinitionDocumentReader createBeanDefinitionDocumentReader() {
							return new ToolingFriendlyBeanDefinitionDocumentReader(BeansConfig.this);
						}
					};

					reader.setDocumentLoader(new XercesDocumentLoader());
					reader.setResourceLoader(resourceLoader);

					reader.setEntityResolver(resolver);
					reader.setSourceExtractor(sourceExtractor);
					reader.setEventListener(eventListener);
					reader.setProblemReporter(problemReporter);
					reader.setErrorHandler(new BeansConfigErrorHandler());
					reader.setNamespaceHandlerResolver(namespaceHandlerResolver);
					reader.setBeanNameGenerator(beanNameGenerator);
					reader.setEnvironment(new ToolingAwareEnvironment());
					
					final Map<Throwable, Integer> throwables = new HashMap<Throwable, Integer>();
					try {
						Callable<Integer> loadBeanDefinitionOperation = new Callable<Integer>() {

							public Integer call() {
								
								// Obtain thread context classloader and override with the project classloader
								ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
								Thread.currentThread().setContextClassLoader(cl);
					
								try {
									// Load bean definitions
									int count = reader.loadBeanDefinitions(resource);

									// Finally register post processed beans and components
									eventListener.registerComponents();

									// Post process beans config if required
									postProcess();

									return count;
								}
								catch (Exception e) {
									// Record the exception to throw it later
									throwables.put(e, LineNumberPreservingDOMParser.getStartLineNumber(documentAccessor.getLastElement()));
								}
								finally {
									// Reset the context classloader
									Thread.currentThread().setContextClassLoader(threadClassLoader);
								}
								return 0;
							}

						};

						try {
							FutureTask<Integer> task = new FutureTask<Integer>(loadBeanDefinitionOperation);
							BeansCorePlugin.getExecutorService().submit(task);
							count = task.get(BeansCorePlugin.getDefault().getPreferenceStore().getInt(BeansCorePlugin.TIMEOUT_CONFIG_LOADING_PREFERENCE_ID),
									TimeUnit.SECONDS);

							// if we recored an exception use this instead of stupid concurrent exception
							if (throwables.size() > 0) {
								throw throwables.keySet().iterator().next();
							}
						}
						catch (TimeoutException e) {
							problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, "Loading of resource '"
									+ resource.getFile().getAbsolutePath() + "' took more than "
									+ BeansCorePlugin.getDefault().getPreferenceStore()
											.getInt(BeansCorePlugin.TIMEOUT_CONFIG_LOADING_PREFERENCE_ID) + "sec",
									file, 1));
						}
					}
					catch (Throwable e) {
						int line = -1;
						if (throwables.containsKey(e)) {
							line = throwables.get(e);
						}
						// Skip SAXParseExceptions because they're already handled by the SAX ErrorHandler
						if (e instanceof BeanDefinitionStoreException) {
							if (e.getCause() != null) {
								problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, String.format(
										"Error occured processing XML '%s'. See Error Log for more details", e.getCause().getMessage()), file, line));
								BeansCorePlugin.log(new Status(IStatus.INFO, BeansCorePlugin.PLUGIN_ID, String.format(
										"Error occured processing '%s'", file.getFullPath()), e.getCause()));
							}
							else {
								problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e.getMessage(), file, line));
								BeansCorePlugin.log(new Status(IStatus.INFO, BeansCorePlugin.PLUGIN_ID, String.format(
										"Error occured processing '%s'", file.getFullPath()), e));
							}
						}
						else if (!(e.getCause() instanceof SAXParseException)
								&& !(e instanceof BeanDefinitionParsingException)) {
							problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e.getMessage(), file, line));
							BeansCorePlugin.log(new Status(IStatus.INFO, BeansCorePlugin.PLUGIN_ID, String.format(
									"Error occured processing '%s'", file.getFullPath()), e));
						}
					}
				}
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

				// Run external post processors
				postProcessExternal(externalPostProcessors.keySet());

				// Publish events for all existing post processors
				if (this.ownPostProcessors != null) {
					for (IBeansConfigPostProcessor postProcessor : ownPostProcessors) {
						for (IBeansConfigEventListener eventListener : eventListeners) {
							eventListener.onPostProcessorDetected(this, postProcessor);
						}
					}
				}
				// Publish events for all removed post processors
				if (this.removedPostProcessors != null) {
					for (IBeansConfigPostProcessor postProcessor : removedPostProcessors) {
						for (IBeansConfigEventListener eventListener : eventListeners) {
							eventListener.onPostProcessorRemoved(this, postProcessor);
						}
					}
				}

				for (IBeansConfigEventListener eventListener : eventListeners) {
					eventListener.onReadEnd(this);
				}

				if (DEBUG) {
					System.out.println(String.format("> loading of %s beans from %s took %sms", count, file
							.getFullPath().toString(), (System.currentTimeMillis() - start)));
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPersistableElement.class) {
			return new BeansModelElementToPersistableElementAdapter(this);
		}
		else if (adapter == IResource.class) {
			return getElementResource();
		}
		else if (adapter == Resource.class) {
			return resource;
		}
		return super.getAdapter(adapter);
	}

	/**
	 * Entry into processing the contributed {@link IBeansConfigPostProcessor}.
	 */
	private void postProcess() {

		Set<IBeansConfigPostProcessor> detectedPostProcessors = new LinkedHashSet<IBeansConfigPostProcessor>();
		removedPostProcessors = new LinkedHashSet<IBeansConfigPostProcessor>();

		// Create special ReaderEventListener that essentially just passes through component definitions
		ReaderEventListener eventListener = new BeansConfigPostProcessorReaderEventListener();

		// Collect the beans from this config and all imported configs
		List<IBean> beansClone = new ArrayList<IBean>();

		// Important: don't use getBeans or getComponents on this instance -> will lock
		beansClone.addAll(beans.values());
		for (IBeansComponent component : components) {
			addBeansFromCompoent(component, beansClone);
		}

		// Now collect from all imported configurations as well
		for (IBeansImport beansImport : imports) {
			for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
				beansClone.addAll(bc.getBeans());
				for (IBeansComponent component : bc.getComponents()) {
					addBeansFromCompoent(component, beansClone);
				}
			}
		}

		// Run all generally contributed post processors
		for (IBeansConfigPostProcessor postProcessor : BeansConfigPostProcessorFactory.createPostProcessor(null)) {
			executePostProcessor(postProcessor, eventListener);
		}

		// Run all post processors specific to a bean class
		for (IBean bean : beansClone) {
			String beanClassName = bean.getClassName();
			if (beanClassName != null) {
				IType type = JdtUtils.getJavaType(getElementResource().getProject(), beanClassName);
				if (type != null) {
					for (IBeansConfigPostProcessor postProcessor : BeansConfigPostProcessorFactory
							.createPostProcessor(beanClassName)) {

						executePostProcessor(postProcessor, eventListener);

						// Keep the detected post processor for later
						detectedPostProcessors.add(postProcessor);
					}
				}
			}
		}

		// Detect deleted post processors
		for (IBeansConfigPostProcessor postProcessor : ownPostProcessors) {
			if (!detectedPostProcessors.contains(postProcessor)) {
				ownPostProcessors.remove(postProcessor);
				removedPostProcessors.add(postProcessor);
			}
		}

		// Detect newly added post processors
		for (IBeansConfigPostProcessor postProcessor : detectedPostProcessors) {
			if (!ownPostProcessors.contains(postProcessor)) {
				ownPostProcessors.add(postProcessor);
			}
		}

	}

	private void addBeansFromCompoent(IBeansComponent component, List<IBean> beansClone) {
		beansClone.addAll(component.getBeans());
		
		for (IBeansComponent bc : component.getComponents()) {
			addBeansFromCompoent(bc, beansClone);
		}
	}

	/**
	 * Execute the externally added {@link IBeansConfigPostProcessor}s.
	 * <p>
	 * This will only execute the given post processors if this config is already populated.
	 */
	private void postProcessExternal(Set<IBeansConfigPostProcessor> postProcessors) {
		if (this.isModelPopulated) {
			try {
				w.lock();

				// Create special ReaderEventListener that essentially just passes through component definitions
				ReaderEventListener eventListener = new BeansConfigPostProcessorReaderEventListener();

				// Run all external found post processor instances
				for (IBeansConfigPostProcessor postProcessor : postProcessors) {
					if (!ownPostProcessors.contains(postProcessor)) {
						executePostProcessor(postProcessor, eventListener);
					}
				}
			}
			finally {
				w.unlock();
			}
		}
	}

	/**
	 * Add the given {@link IBeansConfigPostProcessor}.
	 * <p>
	 * If this config has already been populated only the given {@link IBeansConfigPostProcessor} will be executed;
	 * otherwise executing is deferred until the model gets populated
	 */
	protected void addExternalPostProcessor(IBeansConfigPostProcessor postProcessor, IBeansConfig config) {
		try {
			w.lock();
			if (externalPostProcessors.containsKey(postProcessor)) {
				externalPostProcessors.get(postProcessor).add(config);
			}
			else {
				Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
				configs.add(config);
				externalPostProcessors.put(postProcessor, configs);

				if (!ownPostProcessors.contains(postProcessor)) {
					// Run the external post processors
					Set<IBeansConfigPostProcessor> postProcessors = new HashSet<IBeansConfigPostProcessor>();
					postProcessors.add(postProcessor);
					postProcessExternal(postProcessors);
				}
			}
		}
		finally {
			w.unlock();
		}
	}

	/**
	 * Remove the given {@link IBeansConfigPostProcessor}.
	 * <p>
	 * This will trigger a reload of this config in order to remove obsolete {@link IBean}s.
	 */
	protected void removeExternalPostProcessor(IBeansConfigPostProcessor postProcessor, IBeansConfig config) {
		try {
			w.lock();
			if (externalPostProcessors.containsKey(postProcessor)
					&& externalPostProcessors.get(postProcessor).remove(config)) {
				if (externalPostProcessors.get(postProcessor).size() == 0) {
					externalPostProcessors.remove(postProcessor);
				}
			}
			reload();
		}
		finally {
			w.unlock();
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
				postProcessor.postProcess(BeansConfigPostProcessorFactory.createPostProcessingContext(BeansConfig.this,
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
			provider = DEFAULT_ELEMENT_PROVIDER;
		}
		ISourceModelElement element = provider.getElement(BeansConfig.this, componentDefinition);
		if (element instanceof IBean) {
			beans.put(element.getElementName(), (IBean) element);
		}
		else if (element instanceof IBeansComponent) {
			components.add((IBeansComponent) element);
		}
	}

	/**
	 * Returns the current {@link IResource} that is being processed.
	 */
	private IResource getCurrentResource() {
		if (currentResource != null) {
			return currentResource;
		}
		return file;
	}

	/**
	 * Implementation of {@link ReaderEventListener} which populates the current instance of {@link IBeansConfig} with
	 * data from the XML bean definition reader events.
	 */
	class BeansConfigReaderEventListener implements ReaderEventListener {

		private IBeansConfig config;

		private Resource resource;

		private Map<String, IModelElementProvider> elementProviders;

		private Map<Resource, Set<ComponentDefinition>> componentDefinitionsCache;

		private Map<Resource, Set<ImportDefinition>> importDefinitionsCache;

		private Map<Resource, Set<AliasDefinition>> aliasDefinitionsCache;

		private Map<Resource, DocumentDefaultsDefinition> defaultDefinitionsCache;

		private SourceExtractor sourceExtractor;

		private DocumentAccessor documentAccessor;

		public BeansConfigReaderEventListener(IBeansConfig config, Resource resource, SourceExtractor sourceExtractor,
				DocumentAccessor documentAccessor) {
			this.config = config;
			this.resource = resource;
			this.elementProviders = NamespaceUtils.getElementProviders();
			this.componentDefinitionsCache = new HashMap<Resource, Set<ComponentDefinition>>();
			this.importDefinitionsCache = new HashMap<Resource, Set<ImportDefinition>>();
			this.aliasDefinitionsCache = new HashMap<Resource, Set<AliasDefinition>>();
			this.defaultDefinitionsCache = new HashMap<Resource, DocumentDefaultsDefinition>();
			this.sourceExtractor = sourceExtractor;
			this.documentAccessor = documentAccessor;
		}

		/**
		 * {@inheritDoc}
		 */
		public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
			if (defaultsDefinition instanceof DocumentDefaultsDefinition) {
				Object source = defaultsDefinition.getSource();

				if (source instanceof IModelSourceLocation) {
					Resource resource = ((IModelSourceLocation) source).getResource();
					addDefaultToCache((DocumentDefaultsDefinition) defaultsDefinition, resource);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void importProcessed(ImportDefinition importDefinition) {
			Object source = importDefinition.getSource();

			if (source instanceof IModelSourceLocation) {
				Resource resource = ((IModelSourceLocation) source).getResource();
				addImportToCache(importDefinition, resource);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void aliasRegistered(AliasDefinition aliasDefinition) {
			Object source = aliasDefinition.getSource();

			if (source instanceof IModelSourceLocation) {
				Resource resource = ((IModelSourceLocation) source).getResource();
				addAliasToCache(aliasDefinition, resource);
			}
		}

		/**
		 * Converts the given {@link ComponentDefinition} into a corresponding {@link ISourceModelElement} via a
		 * namespace-specific {@link IModelElementProvider}. These providers are registered via the extension point
		 * <code>org.springframework.ide.eclipse.beans.core.namespaces</code>.
		 */
		public void componentRegistered(ComponentDefinition componentDefinition) {
			Object source = componentDefinition.getSource();

			// make sure to attach a default source location
			if (source == null && currentEncodedResource != null && currentEncodedResource.getResource() != null) {
				source = sourceExtractor.extractSource(documentAccessor.getCurrentElement(),
						currentEncodedResource.getResource());
				for (BeanDefinition beanDefinition : componentDefinition.getBeanDefinitions()) {
					if (beanDefinition.getSource() == null && beanDefinition instanceof AbstractBeanDefinition) {
						((AbstractBeanDefinition) beanDefinition).setSource(source);
						((AbstractBeanDefinition) beanDefinition).setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
					}
				}
			}

			// make sure nested BeanDefinitions have all the source extraction applied
			addSourceToNestedBeanDefinitions(componentDefinition);

			if (source instanceof IModelSourceLocation) {
				Resource resource = ((IModelSourceLocation) source).getResource();
				addComponentToCache(componentDefinition, resource);
			}
		}

		private void addAliasToCache(AliasDefinition aliasDefinition, Resource resource) {
			if (aliasDefinitionsCache.containsKey(resource)) {
				aliasDefinitionsCache.get(resource).add(aliasDefinition);
			}
			else {
				Set<AliasDefinition> aliasDefinitions = new LinkedHashSet<AliasDefinition>();
				aliasDefinitions.add(aliasDefinition);
				aliasDefinitionsCache.put(resource, aliasDefinitions);
			}
		}

		private void addSourceToNestedBeanDefinitions(ComponentDefinition componentDefinition) {
			if (componentDefinition instanceof CompositeComponentDefinition) {
				CompositeComponentDefinition compositeComponentDefinition = (CompositeComponentDefinition) componentDefinition;
				for (ComponentDefinition nestedComponentDefinition : compositeComponentDefinition.getNestedComponents()) {
					for (BeanDefinition beanDefinition : nestedComponentDefinition.getBeanDefinitions()) {
						if (!(beanDefinition.getSource() instanceof IModelSourceLocation)
								&& beanDefinition instanceof AbstractBeanDefinition) {
							((AbstractBeanDefinition) beanDefinition).setSource(sourceExtractor.extractSource(
									beanDefinition.getSource(), resource));
						}
					}
				}

			}
		}

		private void addComponentToCache(ComponentDefinition componentDefinition, Resource resource) {
			if (componentDefinitionsCache.containsKey(resource)) {
				componentDefinitionsCache.get(resource).add(componentDefinition);
			}
			else {
				Set<ComponentDefinition> componentDefinitions = new LinkedHashSet<ComponentDefinition>();
				componentDefinitions.add(componentDefinition);
				componentDefinitionsCache.put(resource, componentDefinitions);
			}
		}

		private void addDefaultToCache(DocumentDefaultsDefinition defaultsDefinition, Resource resource) {
			if (!defaultDefinitionsCache.containsKey(resource)) {
				defaultDefinitionsCache.put(resource, defaultsDefinition);
			}
		}

		private void addImportToCache(ImportDefinition importDefinition, Resource resource) {
			if (importDefinitionsCache.containsKey(resource)) {
				importDefinitionsCache.get(resource).add(importDefinition);
			}
			else {
				Set<ImportDefinition> importDefinitions = new LinkedHashSet<ImportDefinition>();
				importDefinitions.add(importDefinition);
				importDefinitionsCache.put(resource, importDefinitions);
			}
		}

		public void registerComponents() {

			// Start with the root resource
			defaults = defaultDefinitionsCache.get(resource);
			Set<ComponentDefinition> componentDefinitions = componentDefinitionsCache.get(resource);
			if (componentDefinitions != null) {
				for (ComponentDefinition componentDefinition : componentDefinitions) {
					registerComponentDefinition(componentDefinition, elementProviders);
				}
			}

			Set<AliasDefinition> aliasDefinitions = aliasDefinitionsCache.get(resource);
			if (aliasDefinitions != null) {
				for (AliasDefinition aliasDefinition : aliasDefinitions) {
					aliases.put(aliasDefinition.getAlias(), new BeanAlias(config, aliasDefinition));
				}
			}

			Set<ImportDefinition> importDefinitions = importDefinitionsCache.get(resource);
			if (importDefinitions != null) {
				for (ImportDefinition importDefinition : importDefinitions) {
					processImportDefinition(importDefinition, config);
				}
			}
		}

		private void processImportDefinition(ImportDefinition importDefinition, IBeansConfig config) {
			BeansImport beansImport = new BeansImport(config, importDefinition);

			if (config instanceof BeansConfig) {
				imports.add(beansImport);
			}
			else if (config instanceof ImportedBeansConfig) {
				((ImportedBeansConfig) config).addImport(beansImport);
			}

			if (((IBeansProject) getElementParent()).isImportsEnabled()) {
				Resource[] importedResources = importDefinition.getActualResources();

				for (Resource importedResource : importedResources) {
					ImportedBeansConfig importedBeansConfig = new ImportedBeansConfig(beansImport, importedResource,
							getType());
					importedBeansConfig.readConfig();
					beansImport.addImportedBeansConfig(importedBeansConfig);

					importedBeansConfig.setDefaults(defaultDefinitionsCache.get(importedResource));
					Set<ComponentDefinition> componentDefinitions = componentDefinitionsCache.get(importedResource);
					if (componentDefinitions != null) {
						for (ComponentDefinition componentDefinition : componentDefinitions) {
							String uri = NamespaceUtils.getNameSpaceURI(componentDefinition);
							IModelElementProvider provider = elementProviders.get(uri);
							if (provider == null) {
								provider = DEFAULT_ELEMENT_PROVIDER;
							}
							ISourceModelElement element = provider.getElement(importedBeansConfig, componentDefinition);
							if (element instanceof IBean) {
								importedBeansConfig.addBean((IBean) element);
							}
							else if (element instanceof IBeansComponent) {
								importedBeansConfig.addComponent((IBeansComponent) element);
							}
						}
					}

					Set<AliasDefinition> aliasDefinitions = aliasDefinitionsCache.get(importedResource);
					if (aliasDefinitions != null) {
						for (AliasDefinition aliasDefinition : aliasDefinitions) {
							importedBeansConfig.addAlias(new BeanAlias(importedBeansConfig, aliasDefinition));
						}
					}

					// Process nested imports
					Set<ImportDefinition> importDefinitions = importDefinitionsCache.get(importedResource);
					if (importDefinitions != null) {
						for (ImportDefinition nestedImportDefinition : importDefinitions) {
							processImportDefinition(nestedImportDefinition, importedBeansConfig);
						}
					}

					importedBeansConfig.readFinish();
				}
			}
		}
	}

	/**
	 * {@link ResourcePatternResolver} that checks if <code>.class</code> resource are being requested.
	 */
	class ClassResourceFilteringPatternResolver extends EclipsePathMatchingResourcePatternResolver implements
			ResourcePatternResolver {

		/**
		 * Creates a new {@link ClassResourceFilteringPatternResolver}
		 */
		public ClassResourceFilteringPatternResolver(IProject project, ClassLoader classLoader) {
			super(project, classLoader);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Resource getResource(String location) {
			// Pass package scanning through to the default pattern resolver. This is required for
			// component-scanning.
			if (location.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				return super.getResource(location);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Resource[] getResources(String locationPattern) throws IOException {
			// Pass package scanning through to the default pattern resolver. This is required for
			// component-scanning.
			if (locationPattern.endsWith(ClassUtils.CLASS_FILE_SUFFIX)) {
				return super.getResources(locationPattern);
			}
			return new Resource[0];
		}

	}

	/**
	 * SAX {@link ErrorHandler} implementation that creates {@link ValidationProblem}s for all reported problems
	 */
	class BeansConfigErrorHandler implements ErrorHandler {

		/**
		 * {@inheritDoc}
		 */
		public void warning(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_WARNING, e.getMessage(), getCurrentResource(), e
					.getLineNumber()));
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e.getMessage(), getCurrentResource(), e
					.getLineNumber()));
		}

		/**
		 * {@inheritDoc}
		 */
		public void fatalError(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e.getMessage(), getCurrentResource(), e
					.getLineNumber()));
		}
	}

	/**
	 * Implementation of the Spring tooling API {@link ProblemReporter} interface. This implementation creates
	 * {@link ValidationProblem}s for all reported problems.
	 */
	class BeansConfigProblemReporter implements ProblemReporter {

		/**
		 * {@inheritDoc}
		 */
		public void fatal(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, getMessage(problem), getCurrentResource(),
					getLine(problem)));
			throw new BeanDefinitionParsingException(problem);
		}

		/**
		 * {@inheritDoc}
		 */
		public void error(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, getMessage(problem), getCurrentResource(),
					getLine(problem)));
		}

		/**
		 * {@inheritDoc}
		 */
		public void warning(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_WARNING, getMessage(problem), getCurrentResource(),
					getLine(problem)));
		}

		/**
		 * Returns the concatenated message form the {@link Problem} and the {@link Problem#getRootCause()}.
		 */
		private String getMessage(Problem problem) {
			StringBuffer message = new StringBuffer(problem.getMessage());
			Throwable rootCause = problem.getRootCause();
			if (rootCause != null) {

				// Retrieve nested exception
				while (rootCause.getCause() != null) {
					rootCause = rootCause.getCause();
				}
				message.append(": ");
				message.append(rootCause.getMessage());
			}
			return message.toString();
		}

		/**
		 * Returns the line of the problem by introspecting the {@link XmlSourceLocation}.
		 */
		private int getLine(Problem problem) {
			Object source = problem.getLocation().getSource();
			if (source instanceof XmlSourceLocation) {
				return ((XmlSourceLocation) source).getStartLine();
			}
			else if (source instanceof Node) {
				return LineNumberPreservingDOMParser.getStartLineNumber((Node) source);
			}
			return -1;
		}
	}

	/**
	 * Special {@link ReaderEventListener} implementation that passes
	 * {@link ReaderEventListener#componentRegistered(ComponentDefinition)} calls to this configs
	 * {@link BeansConfig#registerComponentDefinition(ComponentDefinition, Map)}.
	 * @author Christian Dupuis
	 * @since 2.2.5
	 */
	class BeansConfigPostProcessorReaderEventListener extends EmptyReaderEventListener {

		// Keep the contributed model element providers
		final Map<String, IModelElementProvider> elementProviders = NamespaceUtils.getElementProviders();

		@Override
		public void componentRegistered(ComponentDefinition componentDefinition) {
			// make sure that all components that come through are safe for the model
			if (componentDefinition.getSource() == null) {
				if (componentDefinition instanceof BeanComponentDefinition) {
					((AbstractBeanDefinition) ((BeanComponentDefinition) componentDefinition).getBeanDefinition())
							.setSource(new DefaultModelSourceLocation(1, 1, resource));
				}
			}
			registerComponentDefinition(componentDefinition, elementProviders);
		}
	}

	/**
	 * Extension to {@link SimpleBeanDefinitionRegistry} that suppresses registrations of
	 * {@link ScannedGenericBeanDefinition} instances as those contain references to the a classloader which we want to
	 * discard.
	 * @since 2.3.1
	 */
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

	/**
	 * Alternative to {@link ScannedGenericBeanDefinition} that rejects the internal dependency to a ClassLoader hold by
	 * the {@link AnnotationMetadata}.
	 * @since 2.3.1
	 */
	protected static class InternalScannedGenericBeanDefinition extends GenericBeanDefinition implements
			AnnotatedBeanDefinition {

		private static final long serialVersionUID = 467157320316462045L;

		public InternalScannedGenericBeanDefinition(AbstractBeanDefinition beanDefinition) {
			setBeanClassName(beanDefinition.getBeanClassName());
			setSource(beanDefinition.getSource());
			setResource(beanDefinition.getResource());
		}

		public AnnotationMetadata getMetadata() {
			return null;
		}
	}

	/**
	 * Extension to the default {@link DefaultBeanDefinitionDocumentReader} to suppress import statements with
	 * placeholders in resource attributes as this is not support in the IDE.
	 * @since 2.3.1
	 */
	static class ToolingFriendlyBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader {

		private Environment environment;
		
		private BeanDefinitionParserDelegate delegate;

		private BeansConfig beansConfig;

		public ToolingFriendlyBeanDefinitionDocumentReader(BeansConfig beansConfig) {
			this.beansConfig = beansConfig;
		}

		@Override
		public void setEnvironment(Environment environment) {
			super.setEnvironment(environment);
			this.environment = environment;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void importBeanDefinitionResource(Element ele) {
			String location = ele.getAttribute(RESOURCE_ATTRIBUTE);
			if (SpringCoreUtils.hasPlaceHolder(location)) {
				getReaderContext().warning("Resource location contains placeholder", ele);
			}
			else {
				super.importBeanDefinitionResource(ele);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected BeanDefinitionParserDelegate createDelegate(XmlReaderContext readerContext, Element root, BeanDefinitionParserDelegate parentDelegate) {
			BeanDefinitionParserDelegate delegate = new ErrorSuppressingBeanDefinitionParserDelegate(readerContext, environment);
			delegate.initDefaults(root, parentDelegate);
			return delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
			BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);
			if (bdHolder != null) {
				// Obtain source before decorating; decoration might discard the original source location
				Object source = bdHolder.getSource();
				bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);

				// Set the original source in case it got discared by the decorator
				if (bdHolder.getSource() == null) {
					((AbstractBeanDefinition) bdHolder.getBeanDefinition()).setSource(source);
				}

				try {
					// Register the final decorated instance
					BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
				}
				catch (BeanDefinitionStoreException ex) {
					getReaderContext().error(
							"Failed to register bean definition with name '" + bdHolder.getBeanName() + "'", ele, ex);
				}
				// Send registration event
				getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
			}
		}
		
		@Override
		protected void doRegisterBeanDefinitions(Element root) {
			String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);
			String[] specifiedProfiles = null;
			if (StringUtils.hasText(profileSpec)) {
				specifiedProfiles = StringUtils.tokenizeToStringArray(profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
			}
			
			// Spring 3.1 profile support; register composite component definition to carry nested beans
			ProfileAwareCompositeComponentDefinition cd = new ProfileAwareCompositeComponentDefinition(root.getNodeName(), 
					getReaderContext().extractSource(root), specifiedProfiles);
			beansConfig.componentDefinitions.push(cd);
			try {
				
				// any nested <beans> elements will cause recursion in this method. In
				// order to propagate and preserve <beans> default-* attributes correctly,
				// keep track of the current (parent) delegate, which may be null. Create
				// the new (child) delegate with a reference to the parent for fallback purposes,
				// then ultimately reset this.delegate back to its original (parent) reference.
				// this behavior emulates a stack of delegates without actually necessitating one.
				BeanDefinitionParserDelegate parent = this.delegate;
				this.delegate = createDelegate(getReaderContext(), root, parent);
	
				preProcessXml(root);
				parseBeanDefinitions(root, this.delegate);
				postProcessXml(root);
	
				this.delegate = parent;
			}
			finally {
				beansConfig.componentDefinitions.pop();
				getReaderContext().fireComponentRegistered(cd);
			}
		}
	}
	
	class ProfileAwareReaderContext extends XmlReaderContext {

		private ReaderEventListener eventListener;
		
		public ProfileAwareReaderContext(Resource resource, ProblemReporter problemReporter,
				ReaderEventListener eventListener, SourceExtractor sourceExtractor, XmlBeanDefinitionReader reader,
				NamespaceHandlerResolver namespaceHandlerResolver) {
			super(resource, problemReporter, eventListener, sourceExtractor, reader, namespaceHandlerResolver);
			this.eventListener = eventListener;
		}
		
		@Override
		public void fireComponentRegistered(ComponentDefinition componentDefinition) {
			if (!componentDefinitions.empty()) {
				componentDefinitions.peek().addNestedComponent(componentDefinition);
			}
			else {
				eventListener.componentRegistered(componentDefinition);
			}
		}
		
	}

	/**
	 * Extension to {@link BeanDefinitionParserDelegate} that captures the class and linkage errors from loading
	 * {@link NamespaceHandler}s instances.
	 * @since 2.3.1
	 */
	static class ErrorSuppressingBeanDefinitionParserDelegate extends BeanDefinitionParserDelegate {

		private final XmlReaderContext readerContext;

		public ErrorSuppressingBeanDefinitionParserDelegate(XmlReaderContext readerContext, Environment environment) {
			super(readerContext, environment);
			this.readerContext = readerContext;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public BeanDefinition parseCustomElement(Element ele, BeanDefinition containingBd) {
			String namespaceUri = getNamespaceURI(ele);
			try {
				readerContext.getNamespaceHandlerResolver().resolve(namespaceUri);
			}
			catch (FatalBeanException e) {
				// Beautify the exception message bit
				String msg = e.getMessage();
				int ix = msg.indexOf(';');
				if (ix > 0) {
					msg = msg.substring(0, ix);
				}
				readerContext.warning(msg + ". Check Error Log for more details.", ele);
				BeansCorePlugin.log(new Status(IStatus.WARNING, BeansCorePlugin.PLUGIN_ID,
						"Problem loading NamespaceHandler for '" + namespaceUri + "'.", e));
				return null;
			}
			return super.parseCustomElement(ele, containingBd);
		}
	}
}	
