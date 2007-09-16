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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.ui.IPersistableElement;
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
import org.springframework.beans.factory.parsing.SourceExtractor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
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
import org.springframework.ide.eclipse.core.io.StorageResource;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.io.xml.LineNumberPreservingDOMParser;
import org.springframework.ide.eclipse.core.io.xml.XercesDocumentLoader;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.java.JavaSourceExtractor;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceExtractor;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
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
		IBeansConfig, ILazyInitializedModelElement {

	public static final IModelElementProvider DEFAULT_ELEMENT_PROVIDER = new DefaultModelElementProvider();

	/** This bean's config file */
	private IFile file;

	/** This bean config file's timestamp of last modification */
	private long modificationTimestamp;

	/** Indicator for a beans configuration embedded in a ZIP file */
	private boolean isArchived;

	/** Defaults values for this beans config file */
	private volatile DocumentDefaultsDefinition defaults;

	/** List of imports (in registration order) */
	private volatile Set<IBeansImport> imports;

	/** List of aliases (in registration order) */
	private volatile Map<String, IBeanAlias> aliases;

	/** List of components (in registration order) */
	private volatile Set<IBeansComponent> components;

	/** List of bean names mapped beans (in registration order) */
	private volatile Map<String, IBean> beans;

	/**
	 * List of bean class names mapped to list of beans implementing the
	 * corresponding class
	 */
	private volatile Map<String, Set<IBean>> beanClassesMap;

	private volatile boolean isBeanClassesMapPopulated = false;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	private volatile boolean isModelPopulated = false;

	/**
	 * List of parsing errors.
	 */
	private Set<ValidationProblem> problems;

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
		// Lazily initialization of this config
		readConfig();

		IModelSourceLocation location = ModelUtils.getSourceLocation(defaults);
		return (location != null ? location.getStartLine() : -1);
	}

	public boolean isInitialized() {
		return isModelPopulated;
	}

	public Set<ValidationProblem> getProblems() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return problems;
		}
		finally {
			r.unlock();
		}
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
			try {
				w.lock();
				isModelPopulated = false;
				modificationTimestamp = IResource.NULL_STAMP;
				defaults = null;
				imports = null;
				aliases = null;
				beans = null;
				isBeanClassesMapPopulated = false;
				beanClassesMap = null;
				problems = null;

				// Reset all config sets which contain this config
				for (IBeansConfigSet configSet : ((IBeansProject) getElementParent())
						.getConfigSets()) {
					if (configSet.hasConfig(getElementName())) {
						((BeansConfigSet) configSet).reset();
					}
				}
			}
			finally {
				w.unlock();
			}
		}
	}

	public String getDefaultLazyInit() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getLazyInit()
					: DEFAULT_LAZY_INIT);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultAutowire() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getAutowire()
					: DEFAULT_AUTO_WIRE);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultDependencyCheck() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getDependencyCheck()
					: DEFAULT_DEPENDENCY_CHECK);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultInitMethod() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null && defaults.getInitMethod() != null ? defaults
					.getInitMethod()
					: DEFAULT_INIT_METHOD);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultDestroyMethod() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null && defaults.getDestroyMethod() != null ? defaults
					.getDestroyMethod()
					: DEFAULT_DESTROY_METHOD);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultMerge() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			// This default value was introduced with Spring 2.0 -> so we have
			// to check for an empty string here as well
			return (defaults != null && defaults.getMerge() != null
					&& defaults.getMerge().length() > 0 ? defaults.getMerge()
					: DEFAULT_MERGE);
		}
		finally {
			r.unlock();
		}

	}

	public Set<IBeansImport> getImports() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return Collections.unmodifiableSet(imports);
		}
		finally {
			r.unlock();
		}
	}

	public Set<IBeanAlias> getAliases() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return Collections.unmodifiableSet(new LinkedHashSet<IBeanAlias>(
					aliases.values()));
		}
		finally {
			r.unlock();
		}
	}

	public IBeanAlias getAlias(String name) {
		if (name != null) {
			try {
				r.lock();
				return aliases.get(name);
			}
			finally {
				r.unlock();
			}
		}
		return null;
	}

	public Set<IBeansComponent> getComponents() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return Collections.unmodifiableSet(components);
		}
		finally {
			r.unlock();
		}
	}

	public Set<IBean> getBeans() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			// TODO CD eventually add nestend component beans to the
			// outgoing list.
			return Collections.unmodifiableSet(new LinkedHashSet<IBean>(beans
					.values()));
		}
		finally {
			r.unlock();
		}
	}

	public IBean getBean(String name) {
		if (name != null) {
			// Lazily initialization of this config
			readConfig();

			try {
				r.lock();
				return beans.get(name);
			}
			finally {
				r.unlock();
			}
		}
		return null;
	}

	public boolean hasBean(String name) {
		if (name != null) {
			// Lazily initialization of this config
			readConfig();

			try {
				r.lock();
				return beans.containsKey(name);
			}
			finally {
				r.unlock();
			}
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
		String fileName;
		String fullPath;

		// At first check for a config file in a JAR
		int pos = name.indexOf(ZipEntryStorage.DELIMITER);
		if (pos != -1) {
			isArchived = true;
			fileName = name.substring(0, pos);
		}
		else {
			fileName = name;
		}

		// Now check for an external config file
		if (name.charAt(0) == '/') {
			container = ResourcesPlugin.getWorkspace().getRoot();
			fullPath = fileName;
		}
		else {
			container = (IProject) ((IResourceModelElement) getElementParent())
					.getElementResource();
			fullPath = container.getFullPath().append(fileName).toString();
		}

		file = (IFile) container.findMember(fileName);
		if (file == null || !file.isAccessible()) {
			modificationTimestamp = IResource.NULL_STAMP;
			String msg = "Beans config file '" + fullPath + "' not accessible";
			problems = new LinkedHashSet<ValidationProblem>();
			problems
					.add(new ValidationProblem(IMarker.SEVERITY_ERROR, msg, -1));
		}
		else {
			modificationTimestamp = file.getModificationStamp();
		}
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config.
	 */
	private Map<String, Set<IBean>> getBeanClassesMap() {
		if (!this.isBeanClassesMapPopulated) {
			try {
				w.lock();
				if (this.isBeanClassesMapPopulated) {
					return beanClassesMap;
				}
				beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
				for (IBeansComponent component : getComponents()) {
					addComponentBeanClasses(component, beanClassesMap);
				}
				for (IBean bean : getBeans()) {
					addBeanClasses(bean, beanClassesMap);
				}
			}
			finally {
				this.isBeanClassesMapPopulated = true;
				w.unlock();
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

	private void readConfig() {
		if (!this.isModelPopulated) {
			try {
				w.lock();
				if (this.isModelPopulated) {
					return;
				}
				imports = new LinkedHashSet<IBeansImport>();
				aliases = new LinkedHashMap<String, IBeanAlias>();
				components = new LinkedHashSet<IBeansComponent>();
				beans = new LinkedHashMap<String, IBean>();
				problems = new LinkedHashSet<ValidationProblem>();
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
							new BeansDtdResolver(),
							new PluggableSchemaResolver(
									PluggableSchemaResolver.class
											.getClassLoader()));
					ReaderEventListener eventListener = new BeansConfigReaderEventListener(
							this, false);
					ProblemReporter problemReporter = new BeansConfigProblemReporter();
					BeanNameGenerator beanNameGenerator = new UniqueBeanNameGenerator(
							this);
					XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(
							registry);
					reader.setDocumentLoader(new XercesDocumentLoader());

					// set the resource loader to use the customized project
					// class loader
					reader
							.setResourceLoader(new PathMatchingResourcePatternResolver(
									JdtUtils.getClassLoader(file.getProject())));

					reader.setEntityResolver(resolver);
					reader.setSourceExtractor(new CompositeSourceExtractor(file
							.getProject()));
					reader.setEventListener(eventListener);
					reader.setProblemReporter(problemReporter);
					reader.setErrorHandler(new BeansConfigErrorHandler());
					reader
							.setNamespaceHandlerResolver(new DelegatingNamespaceHandlerResolver(
									NamespaceHandlerResolver.class
											.getClassLoader()));
					reader.setBeanNameGenerator(beanNameGenerator);
					try {
						reader.loadBeanDefinitions(resource);
						// post process beans config if required
						postProcess(problemReporter, beanNameGenerator);
					}
					catch (Throwable e) { // handle ALL exceptions

						// Skip SAXParseExceptions because they're already
						// handled by the SAX ErrorHandler
						if (!(e.getCause() instanceof SAXParseException)) {
							problems
									.add(new ValidationProblem(
											IMarker.SEVERITY_ERROR, e
													.getMessage(), -1));
							BeansCorePlugin.log(e);
						}
					}
				}
			}
			finally {
				this.isModelPopulated = true;
				w.unlock();
			}
		}
	}

	private void postProcess(ProblemReporter problemReporter,
			BeanNameGenerator beanNameGenerator) {
		ReaderEventListener readerEventListener = new BeansConfigReaderEventListener(
				this, true);

		// TODO CD do we need to check the components map as well?
		List<IBean> beansClone = new ArrayList<IBean>();
		beansClone.addAll(beans.values());
		for (IBean bean : beansClone) {
			// for now only handle postprocessor that have a direct bean class
			String beanClassName = bean.getClassName();
			if (beanClassName != null) {
				IType type = JdtUtils.getJavaType(getElementResource()
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
										.createPostProcessingContext(beans
												.values(), readerEventListener,
												problemReporter,
												beanNameGenerator));
					}
				}
			}
		}
	}

	// TODO CD remove once we agree that this is not needed anymore
	/*
	 * private final class NoOpResourcePatternResolver extends
	 * FileResourceLoader implements ResourcePatternResolver {
	 * 
	 * public Resource[] getResources(String locationPattern) throws IOException { //
	 * Ignore any resource using an URI or Ant-style regular expressions if
	 * (locationPattern.indexOf(':') != -1 || locationPattern.indexOf('*') !=
	 * -1) { return new Resource[0]; } return new Resource[] {
	 * getResource(locationPattern) }; } }
	 */

	private final class BeansConfigErrorHandler implements ErrorHandler {

		public void warning(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_WARNING, e
					.getMessage(), e.getLineNumber()));
		}

		public void error(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e
					.getMessage(), e.getLineNumber()));
		}

		public void fatalError(SAXParseException e) throws SAXException {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR, e
					.getMessage(), e.getLineNumber()));
		}
	}

	private final class BeansConfigProblemReporter implements ProblemReporter {

		public void fatal(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR,
					getMessage(problem), getLine(problem)));
			throw new BeanDefinitionParsingException(problem);
		}

		public void error(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_ERROR,
					getMessage(problem), getLine(problem)));
		}

		public void warning(Problem problem) {
			problems.add(new ValidationProblem(IMarker.SEVERITY_WARNING,
					getMessage(problem), getLine(problem)));
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

		private int getLine(Problem problem) {
			Object source = problem.getLocation().getSource();
			if (source instanceof XmlSourceLocation) {
				return ((XmlSourceLocation) source).getStartLine();
			}
			else if (source instanceof Node) {
				return LineNumberPreservingDOMParser
						.getStartLineNumber((Node) source);
			}
			return -1;
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
			if (!isExternal(defaultsDefinition)
					&& defaultsDefinition instanceof DocumentDefaultsDefinition) {
				defaults = (DocumentDefaultsDefinition) defaultsDefinition;
			}
		}

		public void importProcessed(ImportDefinition importDefinition) {
			if (allowExternal || !isExternal(importDefinition)) {
				BeansImport imp = new BeansImport(config, importDefinition);
				imports.add(imp);
			}
		}

		public void aliasRegistered(AliasDefinition aliasDefinition) {
			if (allowExternal || !isExternal(aliasDefinition)) {
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
			if (allowExternal || !isExternal(componentDefinition)) {
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

		private boolean isExternal(BeanMetadataElement element) {
			IModelSourceLocation location = ModelUtils
					.getSourceLocation(element);
			if (location != null) {
				Resource resource = location.getResource();
				if (resource instanceof IAdaptable) {

					// Adapt given resource to a file and compare it with this
					// config's resource
					return !((IAdaptable) resource).getAdapter(IFile.class)
							.equals(config.getElementResource());
				}
				// TODO CD if we want to support class path imports we need to
				// revise this
				else if (resource instanceof ClassPathResource) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * This {@link NamespaceHandlerResolver} provides a {@link NamespaceHandler}
	 * for a given namespace URI. Depending on this namespace URI the returned
	 * namespace handler is one of the following (in the provided order):
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

		private final Map<String, NamespaceHandler> namespaceHandlers;

		private final Set<NamespaceHandlerResolver> namespaceHandlerResolvers;

		public DelegatingNamespaceHandlerResolver(ClassLoader classLoader) {
			super(classLoader);
			namespaceHandlers = NamespaceUtils.getNamespaceHandlers();
			namespaceHandlerResolvers = NamespaceUtils
					.getNamespaceHandlerResolvers();
		}

		@Override
		public NamespaceHandler resolve(String namespaceUri) {

			NamespaceHandler namespaceHandler = null;

			// First check for a namespace handler provided by Spring
			namespaceHandler = super.resolve(namespaceUri);

			if (namespaceHandler != null) {
				return namespaceHandler;
			}

			// Then check for a namespace handler provided by an extension
			namespaceHandler = namespaceHandlers.get(namespaceUri);
			if (namespaceHandler != null) {
				return namespaceHandler;
			}

			// Then check the contributed NamespaceHandlerResolver
			for (NamespaceHandlerResolver resolver : namespaceHandlerResolvers) {
				try {
					namespaceHandler = resolver.resolve(namespaceUri);
					if (namespaceHandler != null) {
						return namespaceHandler;
					}
				}
				catch (Exception e) {
					// Make sure a contributed NamespaceHandlerResolver can't
					// prevent parsing
					BeansCorePlugin.log(e);
				}
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
			// don't decorate bean definition holder and just return
			return definition;
		}

		public BeanDefinition parse(Element element, ParserContext parserContext) {
			// do nothing

			// emit a warning that the NamespaceHandler cannot be found
			parserContext.getReaderContext().warning(
					"Unable to locate Spring NamespaceHandler for XML schema namespace ["
							+ element.getNamespaceURI() + "]",
					parserContext.extractSource(element.getParentNode()));
			return null;
		}
	}

	private static class CompositeSourceExtractor implements SourceExtractor {

		private Set<SourceExtractor> sourceExtractors;

		public CompositeSourceExtractor(IProject project) {
			this.sourceExtractors = new HashSet<SourceExtractor>();
			this.sourceExtractors.add(new XmlSourceExtractor());
			this.sourceExtractors.add(new JavaSourceExtractor(project));
		}

		public Object extractSource(Object sourceCandidate,
				Resource definingResource) {
			if (sourceCandidate != null) {
				for (SourceExtractor sourceExtractor : sourceExtractors) {
					Object object = sourceExtractor.extractSource(
							sourceCandidate, definingResource);
					if (!sourceCandidate.equals(object)) {
						return object;
					}
				}
			}
			return sourceCandidate;
		}
	}

	/**
	 * This {@link EntityResolver}
	 */
	private static class XmlCatalogDelegatingEntityResolver extends
			DelegatingEntityResolver {

		private final Set<EntityResolver> entityResolvers;

		public XmlCatalogDelegatingEntityResolver(EntityResolver dtdResolver,
				EntityResolver schemaResolver) {
			super(dtdResolver, schemaResolver);
			this.entityResolvers = NamespaceUtils.getEntityResolvers();
		}

		@Override
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {
			InputSource inputSource = super.resolveEntity(publicId, systemId);
			if (inputSource != null) {
				return inputSource;
			}

			inputSource = resolveEntityViaXmlCatalog(publicId, systemId);
			if (inputSource != null) {
				return inputSource;
			}

			for (EntityResolver entityResolver : this.entityResolvers) {
				try {
					inputSource = entityResolver.resolveEntity(publicId,
							systemId);
					if (inputSource != null) {
						return inputSource;
					}
				}
				catch (Exception e) {
					// Make sure a contributed EntityResolver can't prevent
					// parsing
					BeansCorePlugin.log(e);
				}
			}

			return inputSource;
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

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPersistableElement.class) {
			return new BeansModelElementToPersistableElementAdapter(this);
		}
		else if (adapter == IResource.class) {
			return getElementResource();
		}
		return super.getAdapter(adapter);
	}
}
