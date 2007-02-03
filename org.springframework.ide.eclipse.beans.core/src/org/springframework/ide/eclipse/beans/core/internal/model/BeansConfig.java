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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.beans.factory.xml.PluggableSchemaResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.DefaultBeanDefinitionRegistry;
import org.springframework.ide.eclipse.beans.core.IBeansProjectMarker.ErrorCode;
import org.springframework.ide.eclipse.beans.core.internal.parser.BeansDtdResolver;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.FileResource;
import org.springframework.ide.eclipse.core.io.StorageResource;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.io.xml.XercesDocumentLoader;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceExtractor;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.ObjectUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class defines a Spring beans configuration.
 * 
 * @author Torsten Juergeleit
 */
public class BeansConfig extends AbstractResourceModelElement implements
		IBeansConfig {
	
	/** This bean's config file */
	private IFile file;

	/** Indicator for a beans configuration embedded in a ZIP file */
	private boolean isArchived;
	
	private Set<Problem> warnings = new LinkedHashSet<Problem>();
	
	private Set<Problem> errors = new LinkedHashSet<Problem>();

	private DocumentDefaultsDefinition defaults;

	/** List of imports (in registration order) */
	private Set<IBeansImport> imports;

	/** List of aliases (in registration order) */
	private Map<String, IBeanAlias> aliases;

	/** List of components (in registration order) */
	private Set<IBeansComponent> components;

	/** List of bean names mapped beans (in registration order) */
	private Map<String, IBean> beans;

	/** List of inner beans (in registration order) */
	private Set<IBean> innerBeans;

	/** List of bean class names mapped to list of beans implementing the
	 * corresponding class */
	private Map<String, Set<IBean>> beanClassesMap;

	public BeansConfig(IBeansProject project, String name) {
		super(project, name);
		init(name);
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONFIG_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getImports());
		children.addAll(getAliases());
		children.addAll(getComponents());
		children.addAll(getBeans());
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

			// Now ask this config's aliases
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
	 * Sets internal list of <code>IBean</code>s to <code>null</code>.
	 * Any further access to the data of this instance of
	 * <code>IBeansConfig</code> leads to reloading of this beans config file.
	 */
	public void reset() {
		warnings.clear();
		errors.clear();

		defaults = null;
		imports = null;
		aliases = null;
		beans = null;
		innerBeans = null;
		beanClassesMap = null;

		// Reset all config sets which contain this config
		for (IBeansConfigSet configSet : ((IBeansProject) getElementParent())
				.getConfigSets()) {
			if (configSet.hasConfig(getElementName())) {
				((BeansConfigSet) configSet).reset();
			}
		}
	}
	
	public void addError(Problem error) {
		errors.add(error);
	}

	public Set<Problem> getErrors() {
		return errors;
	}
	
	public void addWarning(Problem warning) {
		warnings.add(warning);
	}

	public Set<Problem> getWarnings() {
		return warnings;
	}

	public String getDefaultLazyInit() {
		return (defaults != null ? defaults.getLazyInit() : DEFAULT_LAZY_INIT);
	}

	public String getDefaultAutowire() {
		return (defaults != null ? defaults.getAutowire() : DEFAULT_AUTO_WIRE);
	}

	public String getDefaultDependencyCheck() {
		return (defaults != null ? defaults.getDependencyCheck()
				: DEFAULT_DEPENDENCY_CHECK);
	}

	public String getDefaultInitMethod() {
		return (defaults != null && defaults.getInitMethod() != null
				? defaults.getInitMethod() : DEFAULT_INIT_METHOD);
	}

	public String getDefaultDestroyMethod() {
		return (defaults != null && defaults.getDestroyMethod() != null
				? defaults.getDestroyMethod() : DEFAULT_DESTROY_METHOD);
	}

	public String getDefaultMerge() {
		// This default value was introduced with Spring 2.0 -> so we have
		// to check for an empty string here as well
		return (defaults != null && defaults.getMerge().length() > 0 ? defaults
				.getMerge() : DEFAULT_MERGE);
	}

	public Set<IBeansImport> getImports() {
		if (imports == null) {

			// Lazily initialization of import list
			readConfig();
		}
		return Collections.unmodifiableSet(imports);
	}

	public Set<IBeanAlias> getAliases() {
		if (aliases == null) {

			// Lazily initialization of alias list
			readConfig();
		}
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
		if (components == null) {

			// Lazily initialization of components list
			readConfig();
		}
		return Collections.unmodifiableSet(components);
	}

	public Set<IBean> getBeans() {
		if (beans == null) {

			// Lazily initialization of bean list
			readConfig();
		}
		return Collections.unmodifiableSet(new LinkedHashSet<IBean>(beans
				.values()));
	}

	public IBean getBean(String name) {
		if (name != null) {
			if (beans == null) {

				// Lazily initialization of bean list
				readConfig();
			}
			return beans.get(name);
		}
		return null;
	}

	public boolean hasBean(String name) {
		if (name != null) {
			if (beans == null) {

				// Lazily initialization of bean list
				readConfig();
			}
			return beans.containsKey(name);
		}
		return false;
	}

	public Set<IBean> getInnerBeans() {
		if (innerBeans == null) {

			// Lazily initialization of inner beans list
			readConfig();
		}
		return Collections.unmodifiableSet(innerBeans);
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

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansConfig)) {
			return false;
		}
		BeansConfig that = (BeansConfig) other;
		if (this.defaults != null && that.defaults != null
				&& this.defaults != that.defaults) {
			if (!ObjectUtils.nullSafeEquals(this.isArchived, that.isArchived))
				return false;
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

	public int hashCode() {
		int hashCode = 1;
		if (defaults != null) {
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(isArchived);
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getLazyInit());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getAutowire());
			hashCode = getElementType() * hashCode
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
		} else if (name.charAt(0) == '/') {
			container = ResourcesPlugin.getWorkspace().getRoot();
			fullPath = name;
		} else {
			container = (IProject) ((IResourceModelElement) getElementParent())
					.getElementResource();
			fullPath = container.getFullPath().append(name).toString();
		}
		file = (IFile) container.findMember(name);
		if (file == null || !file.isAccessible()) {
			String msg = "Beans config file '" + fullPath + "' not accessible";
			BeansModelUtils.createProblemMarker(this, msg,
					IMarker.SEVERITY_ERROR, -1, ErrorCode.PARSING_FAILED);
			Problem problem = new Problem(msg, new Location(new FileResource(
					fullPath), null));
			errors.add(problem);
		}
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config.
	 */
	private Map<String, Set<IBean>> getBeanClassesMap() {
		if (beanClassesMap == null) {
			beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
			for (IBean bean : getBeans()) {
				addBeanClassToMap(bean);
				for (IBean innerBean : bean.getInnerBeans()) {
					addBeanClassToMap(innerBean);
				}
			}
		}
		return beanClassesMap;
	}

	private void addBeanClassToMap(IBean bean) {

		// Get name of bean class - strip name of any inner class
		String className = bean.getClassName();
		if (className != null) {
			int pos = className.indexOf('$');
			if  (pos > 0) {
				className = className.substring(0, pos);
			}

			// Maintain a list of bean names within every entry in the
			// bean class map
			Set<IBean> beanClassBeans = beanClassesMap.get(className);
			if (beanClassBeans == null) {
				beanClassBeans = new LinkedHashSet<IBean>();
				beanClassesMap.put(className, beanClassBeans);
			}
			beanClassBeans.add(bean);
		}
	}

	private void readConfig() {
		imports = new LinkedHashSet<IBeansImport>();
		aliases = new LinkedHashMap<String, IBeanAlias>();
		components = new LinkedHashSet<IBeansComponent>();
		beans = new LinkedHashMap<String, IBean>();
		innerBeans = new LinkedHashSet<IBean>();
		if (file != null && file.isAccessible()) {
			Resource resource;
			if (isArchived) {
				resource = new StorageResource(new ZipEntryStorage(file
						.getProject(), getElementName()));
			} else {
				resource = new FileResource(file);
			}

			DefaultBeanDefinitionRegistry registry =
					new DefaultBeanDefinitionRegistry();
			XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(
					registry);
			reader.setDocumentLoader(new XercesDocumentLoader());
			EntityResolver resolver = new DelegatingEntityResolver(
					new BeansDtdResolver(), new PluggableSchemaResolver(
							PluggableSchemaResolver.class.getClassLoader()));
			reader.setEntityResolver(resolver);
			reader.setSourceExtractor(new XmlSourceExtractor());
			reader.setEventListener(new BeansConfigReaderEventListener(this));
			reader.setProblemReporter(new BeansConfigProblemReporter(this));
			reader.setErrorHandler(new BeansConfigErrorHandler(this, resource));
			try {
				reader.loadBeanDefinitions(resource);
			} catch (Throwable e) {	// handle ALL exceptions

				// Skip SAXParseExceptions because they're already handled by
				// the SAX ErrorHandler
				if (!(e.getCause() instanceof SAXParseException)) {
					BeansModelUtils.createProblemMarker(this, e.getMessage(),
							IMarker.SEVERITY_ERROR, -1,
							ErrorCode.PARSING_FAILED);
					Problem problem = new Problem(e.getMessage(), new Location(
							resource, null));
					errors.add(problem);
				}
			}
		}
	}

	private final class BeansConfigErrorHandler implements ErrorHandler {

		private IBeansConfig config;
		private Resource resource;

		public BeansConfigErrorHandler(IBeansConfig config,
				Resource resource) {
			this.config = config;
			this.resource = resource;
		}
		
		public void warning(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_WARNING, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
			warnings.add(createProblem(ex));
		}

		public void error(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_ERROR, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
			errors.add(createProblem(ex));
		}

		public void fatalError(SAXParseException ex) throws SAXException {
			BeansModelUtils.createProblemMarker(config, ex.getMessage(),
					IMarker.SEVERITY_ERROR, ex.getLineNumber(),
					ErrorCode.PARSING_FAILED);
			errors.add(createProblem(ex));
		}

		private Problem createProblem(SAXParseException ex) {
			XmlSourceLocation source = new XmlSourceLocation(resource, null, ex
					.getLineNumber(), ex.getLineNumber());
			return new Problem(ex.getMessage(), new Location(resource,
					source));
		}
	}

	private final class BeansConfigProblemReporter implements ProblemReporter {

		private IBeansConfig config;

		public BeansConfigProblemReporter(IBeansConfig config) {
			this.config = config;
		}

		public void fatal(Problem problem) {
			BeansModelUtils.createProblemMarker(config, problem.getMessage(),
					IMarker.SEVERITY_ERROR, problem, ErrorCode.PARSING_FAILED);
			throw new BeanDefinitionParsingException(problem);
		}

		public void error(Problem problem) {
			BeansModelUtils.createProblemMarker(config, problem.getMessage(),
					IMarker.SEVERITY_ERROR, problem, ErrorCode.PARSING_FAILED);
			errors.add(problem);
		}

		public void warning(Problem problem) {
			BeansModelUtils.createProblemMarker(config, problem.getMessage(),
					IMarker.SEVERITY_WARNING, problem,
					ErrorCode.PARSING_FAILED);
			warnings.add(problem);
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

		public BeansConfigReaderEventListener(IBeansConfig config) {
			this.config = config;
		}

		public void defaultsRegistered(DefaultsDefinition defaultsDefinition) {
			if (!isImported(defaultsDefinition)
					&& defaultsDefinition instanceof DocumentDefaultsDefinition) {
				defaults = (DocumentDefaultsDefinition) defaultsDefinition;
			}
		}

		public void importProcessed(ImportDefinition importDefinition) {
			if (!isImported(importDefinition)) {
				BeansImport imp = new BeansImport(config, importDefinition);
				imports.add(imp);
			}
		}

		public void aliasRegistered(AliasDefinition aliasDefinition) {
			if (!isImported(aliasDefinition)) {
				BeanAlias alias = new BeanAlias(config, aliasDefinition);
				aliases.put(aliasDefinition.getAlias(), alias);
			}
		}

		public void componentRegistered(
				ComponentDefinition componentDefinition) {
			if (!isImported(componentDefinition)) {
				if (componentDefinition instanceof BeanComponentDefinition) {
					if (componentDefinition.getBeanDefinitions()[0].getRole()
							!= BeanDefinition.ROLE_INFRASTRUCTURE) {
						IBean bean = new Bean(config,
								(BeanComponentDefinition) componentDefinition);
						beans.put(bean.getElementName(), bean);
						innerBeans.addAll(bean.getInnerBeans());
					}
				} else {
					IBeansComponent comp = new BeansComponent(config,
							componentDefinition);
					components.add(comp);
					innerBeans.addAll(comp.getInnerBeans());
				}
			}
		}

		private boolean isImported(BeanMetadataElement element) {
			IModelSourceLocation location = ModelUtils
					.getSourceLocation(element);
			if (location != null) {
				Resource resource = location.getResource();
				if (resource instanceof IAdaptable) {
					IFile file = (IFile) ((IAdaptable) resource)
							.getAdapter(IFile.class);
					return !config.getElementResource().equals(file);
				}
			}
			return false;
		}
	}
}
