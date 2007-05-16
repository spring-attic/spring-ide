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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker.ErrorCode;
import org.springframework.ide.eclipse.beans.core.internal.model.process.BeansConfigPostProcessorFactory;
import org.springframework.ide.eclipse.beans.core.internal.parser.BeansDtdResolver;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.beans.core.namespaces.DefaultModelElementProvider;
import org.springframework.ide.eclipse.beans.core.namespaces.IModelElementProvider;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.io.FileResourceLoader;
import org.springframework.ide.eclipse.core.io.StorageResource;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.io.xml.XercesDocumentLoader;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceExtractor;
import org.springframework.util.ObjectUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class defines a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class BeansConfig extends AbstractResourceModelElement implements
		IBeansConfig {

	public static final IModelElementProvider DEFAULT_ELEMENT_PROVIDER = new DefaultModelElementProvider();

	/** This bean's config file */
	private IFile file;

	/** This bean config file's timestamp of last modification */
	private long modificationTimestamp;

	/** Indicator for a beans configuration embedded in a ZIP file */
	private boolean isArchived;

	/** Defaults values for this beans config file */
	private DocumentDefaultsDefinition defaults;

	/** List of imports (in registration order) */
	private Set<IBeansImport> imports;

	/** List of aliases (in registration order) */
	private Map<String, IBeanAlias> aliases;

	/** List of components (in registration order) */
	private Set<IBeansComponent> components;

	/** List of bean names mapped beans (in registration order) */
	private Map<String, IBean> beans;

	/**
	 * List of bean class names mapped to list of beans implementing the
	 * corresponding class
	 */
	private Map<String, Set<IBean>> beanClassesMap;

	public BeansConfig(IBeansProject project, String name) {
		super(project, name);
		init(name);
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONFIG_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {

		// Lazily initialization of this config
		readConfig();
		List<ISourceModelElement> children = new ArrayList<ISourceModelElement>(
				getImports());
		children.addAll(getAliases());
		children.addAll(getComponents());
		children.addAll(getBeans());
		Collections.sort(children, new Comparator<ISourceModelElement>() {
			public int compare(ISourceModelElement element1,
					ISourceModelElement element2) {
				return element1.getElementStartLine()
						- element2.getElementStartLine();
			}
		});
		return children.toArray(new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return file;
	}

	public boolean isElementArchived() {
		return isArchived;
	}

	public int getElementStartLine() {
		IModelSourceLocation location = ModelUtils.getSourceLocation(defaults);
		return (location != null ? location.getStartLine() : -1);
	}

	public boolean isInitialized() {
		return beans != null;
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this config
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this config's imports
			for (IBeansImport imp : getImports()) {
				imp.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Now ask this config's aliases
			for (IBeanAlias alias : getAliases()) {
				alias.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Now ask this config's components
			for (IBeansComponent component : getComponents()) {
				component.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this configs's beans
			for (IBean bean : getBeans()) {
				bean.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	/**
	 * Sets internal list of {@link IBean}s to <code>null</code>. Any
	 * further access to the data of this instance of {@link IBeansConfig} leads
	 * to reloading of the corresponding beans config file.
	 */
	public void reload() {
		if (file != null && file.isAccessible()
				&& modificationTimestamp < file.getModificationStamp()) {
			modificationTimestamp = IResource.NULL_STAMP;
			defaults = null;
			imports = null;
			aliases = null;
			beans = null;
			beanClassesMap = null;

			// Reset all config sets which contain this config
			for (IBeansConfigSet configSet : ((IBeansProject) getElementParent())
					.getConfigSets()) {
				if (configSet.hasConfig(getElementName())) {
					((BeansConfigSet) configSet).reset();
				}
			}
		}
	}

	public String getDefaultLazyInit() {

		// Lazily initialization of this config
		readConfig();
		return (defaults != null ? defaults.getLazyInit() : DEFAULT_LAZY_INIT);
	}

	public String getDefaultAutowire() {

		// Lazily initialization of this config
		readConfig();
		return (defaults != null ? defaults.getAutowire() : DEFAULT_AUTO_WIRE);
	}

	public String getDefaultDependencyCheck() {

		// Lazily initialization of this config
		readConfig();
		return (defaults != null ? defaults.getDependencyCheck()
				: DEFAULT_DEPENDENCY_CHECK);
	}

	public String getDefaultInitMethod() {

		// Lazily initialization of this config
		readConfig();
		return (defaults != null && defaults.getInitMethod() != null ? defaults
				.getInitMethod() : DEFAULT_INIT_METHOD);
	}

	public String getDefaultDestroyMethod() {

		// Lazily initialization of this config
		readConfig();
		return (defaults != null && defaults.getDestroyMethod() != null ? defaults
				.getDestroyMethod()
				: DEFAULT_DESTROY_METHOD);
	}

	public String getDefaultMerge() {

		// Lazily initialization of this config
		readConfig();

		// This default value was introduced with Spring 2.0 -> so we have
		// to check for an empty string here as well
		return (defaults != null && defaults.getMerge() != null
				&& defaults.getMerge().length() > 0 ? defaults.getMerge()
				: DEFAULT_MERGE);
	}

	public Set<IBeansImport> getImports() {

		// Lazily initialization of this config
		readConfig();
		return Collections.unmodifiableSet(imports);
	}

	public Set<IBeanAlias> getAliases() {

		// Lazily initialization of this config
		readConfig();
		return Collections.unmodifiableSet(new LinkedHashSet<IBeanAlias>(
				aliases.values()));
	}

	public IBeanAlias getAlias(String name) {
		if (name != null) {
			return aliases.get(name);
		}
		return null;
	}

	public Set<IBeansComponent> getComponents() {

		// Lazily initialization of this config
		readConfig();
		return Collections.unmodifiableSet(components);
	}

	public Set<IBean> getBeans() {

		// Lazily initialization of this config
		readConfig();
		return Collections.unmodifiableSet(new LinkedHashSet<IBean>(beans
				.values()));
	}

	public IBean getBean(String name) {
		if (name != null) {

			// Lazily initialization of this config
			readConfig();
			return beans.get(name);
		}
		return null;
	}

	public boolean hasBean(String name) {
		if (name != null) {

			// Lazily initialization of this config
			readConfig();
			return beans.containsKey(name);
		}
		return false;
	}

	public boolean isBeanClass(String className) {
		if (className != null) {
			return getBeanClassesMap().containsKey(className);
		}
		return false;
	}

	public Set<String> getBeanClasses() {
		return Collections.unmodifiableSet(new LinkedHashSet<String>(
				getBeanClassesMap().keySet()));
	}

	public Set<IBean> getBeans(String className) {
		if (isBeanClass(className)) {
			return Collections.unmodifiableSet(getBeanClassesMap().get(
					className));
		}
		return new HashSet<IBean>();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansConfig)) {
			return false;
		}
		BeansConfig that = (BeansConfig) other;
		if (!ObjectUtils.nullSafeEquals(this.isArchived, that.isArchived))
			return false;
		if (this.defaults != null && that.defaults != null
				&& this.defaults != that.defaults) {
			if (!ObjectUtils.nullSafeEquals(this.defaults.getLazyInit(),
					that.defaults.getLazyInit()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getAutowire(),
					that.defaults.getAutowire()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getDependencyCheck(),
					that.defaults.getDependencyCheck()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getInitMethod(),
					that.defaults.getInitMethod()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getDestroyMethod(),
					that.defaults.getDestroyMethod()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getMerge(),
					that.defaults.getMerge()))
				return false;
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(isArchived);
		if (defaults != null) {
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getLazyInit());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getAutowire());
			hashCode = getElementType()
					* hashCode
					+ ObjectUtils.nullSafeHashCode(defaults
							.getDependencyCheck());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getInitMethod());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getDestroyMethod());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getMerge());
		}
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		return getElementName() + ": " + getBeans();
	}

	/**
	 * Checks the file for the given name. If the given name defines an external
	 * resource (leading '/' -> not part of the project this config belongs to)
	 * get the file from the workspace else from the project. If the name
	 * specifies an entry in an archive then the {@link #isArchived} flag is
	 * set. If the corresponding file is not available or accessible then an
	 * entry is added to the config's list of errors.
	 */
	private void init(String name) {
		IContainer container;
		String fullPath;

		// At first check for a config file in a JAR
		int pos = name.indexOf(ZipEntryStorage.DELIMITER);
		if (pos != -1) {
			isArchived = true;
			container = (IProject) ((IResourceModelElement) getElementParent())
					.getElementResource();
			name = name.substring(0, pos);
			fullPath = container.getFullPath().append(name).toString();

			// Now check for an external config file
		}
		else if (name.charAt(0) == '/') {
			container = ResourcesPlugin.getWorkspace().getRoot();
			fullPath = name;
		}
		else {
			container = (IProject) ((IResourceModelElement) getElementParent())
					.getElementResource();
			fullPath = container.getFullPath().append(name).toString();
		}
		file = (IFile) container.findMember(name);
		if (file == null || !file.isAccessible()) {
			modificationTimestamp = IResource.NULL_STAMP;
			String msg = "Beans config file '" + fullPath + "' not accessible";
			BeansModelUtils.createProblemMarker(this, msg,
					IMarker.SEVERITY_ERROR, -1, ErrorCode.PARSING_FAILED);
		}
		else {
			modificationTimestamp = file.getModificationStamp();
		}
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config.
	 */
	private Map<String, Set<IBean>> getBeanClassesMap() {
		if (beanClassesMap == null) {
			beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
			for (IBeansComponent component : getComponents()) {
				addComponentBeanClasses(component, beanClassesMap);
			}
			for (IBean bean : getBeans()) {
				addBeanClasses(bean, beanClassesMap);
			}
		}
		return beanClassesMap;
	}

	private void addComponentBeanClasses(IBeansComponent component,
			Map<String, Set<IBean>> beanClasses) {
		for (IBean bean : component.getBeans()) {
			addBeanClasses(bean, beanClasses);
		}
		for (IBeansComponent innerComponent : component.getComponents()) {
			addComponentBeanClasses(innerComponent, beanClasses);
		}
	}

	private void addBeanClasses(IBean bean, Map<String, Set<IBean>> beanClasses) {
		addBeanClass(bean, beanClasses);
		for (IBean innerBean : BeansModelUtils.getInnerBeans(bean)) {
			addBeanClass(innerBean, beanClasses);
		}
	}

	private void addBeanClass(IBean bean, Map<String, Set<IBean>> beanClasses) {

		// Get name of bean class - strip name of any inner class
		String className = bean.getClassName();
		if (className != null) {
			int pos = className.indexOf('$');
			if (pos > 0) {
				className = className.substring(0, pos);
			}

			// Maintain a list of bean names within every entry in the
			// bean class map
			Set<IBean> beanClassBeans = beanClasses.get(className);
			if (beanClassBeans == null) {
				beanClassBeans = new LinkedHashSet<IBean>();
				beanClasses.put(className, beanClassBeans);
			}
			beanClassBeans.add(bean);
		}
	}

	private synchronized void readConfig() {
		if (imports == null) {
			imports = new LinkedHashSet<IBeansImport>();
			aliases = new LinkedHashMap<String, IBeanAlias>();
			components = new LinkedHashSet<IBeansComponent>();
			beans = new LinkedHashMap<String, IBean>();
			if (file != null && file.isAccessible()) {
				modificationTimestamp = file.getModificationStamp();
				Resource resource;
				if (isArchived) {
					resource = new StorageResource(new ZipEntryStorage(file
							.getProject(), getElementName()));
				}
				else {
					resource = new FileResource(file);
				}

				DefaultBeanDefinitionRegistry registry = new DefaultBeanDefinitionRegistry();
				EntityResolver resolver = new XmlCatalogDelegatingEntityResolver(
						new BeansDtdResolver(), new PluggableSchemaResolver(
								PluggableSchemaResolver.class.getClassLoader()));

				ReaderEventListener eventListener = new BeansConfigReaderEventListener(
						this, false);
				ProblemReporter problemReporter = new BeansConfigProblemReporter(
						this);
				BeanNameGenerator beanNameGenerator = new UniqueBeanNameGenerator(
						this);

				XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(
						registry);
				reader.setDocumentLoader(new XercesDocumentLoader());
				reader.setResourceLoader(new NoOpResourcePatternResolver());
				reader.setEntityResolver(resolver);
				reader.setSourceExtractor(new XmlSourceExtractor());
				reader.setEventListener(eventListener);
				reader.setProblemReporter(problemReporter);
				reader.setErrorHandler(new BeansConfigErrorHandler(this));
				reader
						.setNamespaceHandlerResolver(new DelegatingNamespaceHandlerResolver(
								NamespaceHandlerResolver.class.getClassLoader()));
				reader.setBeanNameGenerator(beanNameGenerator);
				try {
					reader.loadBeanDefinitions(resource);
					// post process beans config if required
					postProcess(problemReporter, beanNameGenerator);
				}
				catch (Throwable e) { // handle ALL exceptions

					// Skip SAXParseExceptions because they're already handled
					// by the SAX ErrorHandler
					if (!(e.getCause() instanceof SAXParseException)) {
						BeansModelUtils.createProblemMarker(this, e
								.getMessage(), IMarker.SEVERITY_ERROR, -1,
								ErrorCode.PARSING_FAILED);
						BeansCorePlugin.log(e);
					}
				}

			}
		}
	}

	private void postProcess(ProblemReporter problemReporter,
			BeanNameGenerator beanNameGenerator) {
		// TODO do we need to check the components map as well???
		ReaderEventListener readerEventListener = new BeansConfigReaderEventListener(
				this, true);
		for (IBean bean : getBeans()) {
			// TODO for now only handle postprocessor that have a direct
			// bean class
			String beanClassName = bean.getClassName();
			if (beanClassName != null) {
				IType type = BeansModelUtils.getJavaType(getElementResource()
						.getProject(), beanClassName);
				if (type != null
						&& (Introspector.doesImplement(type,
								BeanFactoryPostProcessor.class.getName()) || Introspector
								.doesImplement(type, BeanPostProcessor.class
										.getName()))) {
					IBeansConfigPostProcessor postProcessor = BeansConfigPostProcessorFactory
							.createPostProcessor(beanClassName);
					if (postProcessor != null) {
						postProcessor
								.postProcess(BeansConfigPostProcessorFactory
										.createPostProcessingContext(this,
												readerEventListener,
												problemReporter,
												beanNameGenerator));
					}
				}
			}
		}
	}

	private final class NoOpResourcePatternResolver extends FileResourceLoader
			implements ResourcePatternResolver {

		public Resource[] getResources(String locationPattern)
				throws IOException {

			// Ignore any resource using an URI or Ant-style regular expressions
			if (locationPattern.indexOf(':') != -1
					|| locationPattern.indexOf('*') != -1) {
				return new Resource[0];
			}
			return new Resource[] { getResource(locationPattern) };
		}
	}

	private final class BeansConfigErrorHandler implements ErrorHandler {

		private IBeansConfig config;

		public BeansConfigErrorHandler(IBeansConfig config) {
			this.config = config;
		}

		public void warning(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_WARNING, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
		}

		public void error(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_ERROR, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_ERROR, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
		}
	}

	private final class BeansConfigProblemReporter implements ProblemReporter {

		private IBeansConfig config;

		public BeansConfigProblemReporter(IBeansConfig config) {
			this.config = config;
		}

		public void fatal(Problem problem) {
			BeansModelUtils.createProblemMarker(config, getMessage(problem),
					IMarker.SEVERITY_ERROR, problem, ErrorCode.PARSING_FAILED);
			throw new BeanDefinitionParsingException(problem);
		}

		public void error(Problem problem) {
			BeansModelUtils.createProblemMarker(config, getMessage(problem),
					IMarker.SEVERITY_ERROR, problem, ErrorCode.PARSING_FAILED);
		}

		public void warning(Problem problem) {
			BeansModelUtils
					.createProblemMarker(config, getMessage(problem),
							IMarker.SEVERITY_WARNING, problem,
							ErrorCode.PARSING_FAILED);
		}

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
	}

	/**
	 * Implementation of {@link ReaderEventListener} which populates the current
	 * instance of {@link IBeansConfig} with data from the XML bean definition
	 * reader events.
	 */
	private final class BeansConfigReaderEventListener implements
			ReaderEventListener {

		private IBeansConfig config;

		private Map<String, IModelElementProvider> elementProviders;

		private boolean allowExternal = false;

		public BeansConfigReaderEventListener(IBeansConfig config,
				boolean allowExternal) {
			this.config = config;
			this.allowExternal = allowExternal;
			this.elementProviders = NamespaceUtils.getElementProviders();
		}

		public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
			if (allowExternal || !isImported(defaultsDefinition)
					&& defaultsDefinition instanceof DocumentDefaultsDefinition) {
				defaults = (DocumentDefaultsDefinition) defaultsDefinition;
			}
		}

		public void importProcessed(ImportDefinition importDefinition) {
			if (allowExternal || !isImported(importDefinition)) {
				BeansImport imp = new BeansImport(config, importDefinition);
				imports.add(imp);
			}
		}

		public void aliasRegistered(AliasDefinition aliasDefinition) {
			if (allowExternal || !isImported(aliasDefinition)) {
				BeanAlias alias = new BeanAlias(config, aliasDefinition);
				aliases.put(aliasDefinition.getAlias(), alias);
			}
		}

		/**
		 * Converts the given {@link ComponentDefinition} into a corresponding
		 * {@link ISourceModelElement} via a namespace-specific
		 * {@link IModelElementProvider}. These providers are registered via
		 * the extension point
		 * <code>org.springframework.ide.eclipse.beans.core.namespaces</code>.
		 */
		public void componentRegistered(ComponentDefinition componentDefinition) {
			if (allowExternal || !isImported(componentDefinition)) {
				String uri = NamespaceUtils
						.getNameSpaceURI(componentDefinition);
				IModelElementProvider provider = elementProviders.get(uri);
				if (provider == null) {
					provider = DEFAULT_ELEMENT_PROVIDER;
				}
				ISourceModelElement element = provider.getElement(config,
						componentDefinition);
				if (element instanceof IBean) {
					beans.put(element.getElementName(), (IBean) element);
				}
				else if (element instanceof IBeansComponent) {
					components.add((IBeansComponent) element);
				}
			}
		}

		private boolean isImported(BeanMetadataElement element) {
			IModelSourceLocation location = ModelUtils
					.getSourceLocation(element);
			if (location != null) {
				Resource resource = location.getResource();
				if (resource instanceof IAdaptable) {

					// Adapt given resource to a file and compare it with this
					// config's resource
					return !config.getElementResource().equals(
							((IAdaptable) resource).getAdapter(IFile.class));
				}
			}
			return false;
		}
	}

	/**
	 * This {@link NamespaceHandlerResolver}Êprovides a
	 * {@link NamespaceHandler} for a given namespace URI. Depending on this
	 * namespace URI the returned namespace handler is one of the following (in
	 * the provided order):
	 * <ol>
	 * <li>a namespace handler provided by the Spring framework</li>
	 * <li>a namespace handler contributed via the extension point
	 * <code>org.springframework.ide.eclipse.beans.core.namespaces</code></li>
	 * <li>a no-op {@link NoOpNamespaceHandler namespace handler}</li>
	 * </ol>
	 */
	private static final class DelegatingNamespaceHandlerResolver extends
			DefaultNamespaceHandlerResolver {

		private static final NamespaceHandler NO_OP_NAMESPACE_HANDLER = new NoOpNamespaceHandler();

		private Map<String, NamespaceHandler> namespaceHandlers;

		public DelegatingNamespaceHandlerResolver(ClassLoader classLoader) {
			super(classLoader);
			namespaceHandlers = NamespaceUtils.getNamespaceHandlers();
		}

		@Override
		public NamespaceHandler resolve(String namespaceUri) {

			// First check for a namespace handler provided by Spring
			NamespaceHandler namespaceHandler = super.resolve(namespaceUri);
			if (namespaceHandler != null) {
				return namespaceHandler;
			}

			// Then check for a namespace handler provided by an extension
			namespaceHandler = namespaceHandlers.get(namespaceUri);
			if (namespaceHandler != null) {
				return namespaceHandler;
			}

			// Finally use a no-op namespace handler
			return NO_OP_NAMESPACE_HANDLER;
		}
	}

	private static final class NoOpNamespaceHandler implements NamespaceHandler {

		public void init() {
			// do nothing
		}

		public BeanDefinitionHolder decorate(Node source,
				BeanDefinitionHolder definition, ParserContext parserContext) {
			// do nothing
			return null;
		}

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			// do nothing
			return null;
		}
	}

	/**
	 * This {@link EntityResolver}
	 */
	private static class XmlCatalogDelegatingEntityResolver extends
			DelegatingEntityResolver {

		public XmlCatalogDelegatingEntityResolver(EntityResolver dtdResolver,
				EntityResolver schemaResolver) {
			super(dtdResolver, schemaResolver);
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			InputSource inputSource = super.resolveEntity(publicId, systemId);
			if (inputSource != null) {
				return inputSource;
			}

			return resolveEntityViaXmlCatalog(publicId, systemId);
		}

		public InputSource resolveEntityViaXmlCatalog(String publicId,
				String systemId) {
			ICatalog catalog = XMLCorePlugin.getDefault()
					.getDefaultXMLCatalog();
			if (systemId != null) {
				try {
					String resolvedSystemId = catalog.resolveSystem(systemId);
					if (resolvedSystemId == null) {
						resolvedSystemId = catalog.resolveURI(systemId);
					}
					if (resolvedSystemId != null) {
						return new InputSource(resolvedSystemId);
					}
				}
				catch (MalformedURLException me) {
					// ignore
				}
				catch (IOException ie) {
					// ignore
				}
			}
			if (publicId != null) {
				if (!(systemId != null && systemId.endsWith(XSD_SUFFIX))) {
					try {
						String resolvedSystemId = catalog.resolvePublic(
								publicId, systemId);
						if (resolvedSystemId == null) {
							resolvedSystemId = catalog.resolveURI(publicId);
						}
						if (resolvedSystemId != null) {
							return new InputSource(resolvedSystemId);
						}
					}
					catch (MalformedURLException me) {
						// ignore
					}
					catch (IOException ie) {
						// ignore
					}
				}
			}
			return null;
		}
	}
}
