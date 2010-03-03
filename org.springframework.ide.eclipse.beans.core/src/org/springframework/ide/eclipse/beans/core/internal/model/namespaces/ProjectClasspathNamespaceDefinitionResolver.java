/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IProject;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.ToolingAwareNamespacePlugins.NamespaceDefinition;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * {@link INamespaceDefinitionResolver} that resolves {@link INamespaceDefinition}s from the project's classpath as well.
 * <p>
 * Note: this implementation is currently not in use
 * @author Christian Dupuis
 * @since 2.3.1
 * @see BeansCorePlugin#getNamespaceDefinitionResolver(IProject)
 */
public class ProjectClasspathNamespaceDefinitionResolver implements INamespaceDefinitionResolver {

	private static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

	private static final String DEFAULT_HANDLER_MAPPINGS_LOCATION = "META-INF/spring.handlers";

	private static final String DEFAULT_TOOLING_MAPPINGS_LOCATION = "META-INF/spring.tooling";

	private final IProject project;
	
	private final INamespaceDefinitionResolver resolver;
	
	private Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();

	public ProjectClasspathNamespaceDefinitionResolver(IProject project, INamespaceDefinitionResolver resolver) {
		this.project = project;
		this.resolver = resolver;
	}

	public Set<INamespaceDefinition> getNamespaceDefinitions() {
//		long start = System.currentTimeMillis();

		// Add in namespace definitions from the classpath
		if (project != null
				&& SpringCorePreferences.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID).getBoolean(
						BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_PROPERTY, false)) {

			for (INamespaceDefinition definition : resolver.getNamespaceDefinitions()) {
				namespaceDefinitionRegistry.put(definition.getNamespaceUri(), (NamespaceDefinition) definition);
			}

			ClassLoader cls = JdtUtils.getClassLoader(project, null);

			Map<String, String> handlerMappings = new ConcurrentHashMap<String, String>();
			Map<String, String> toolingMappings = new ConcurrentHashMap<String, String>();
			Properties schemaMappings = new Properties();

			try {
				Properties mappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_HANDLER_MAPPINGS_LOCATION, cls);
				CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
				schemaMappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_SCHEMA_MAPPINGS_LOCATION, cls);
				mappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_TOOLING_MAPPINGS_LOCATION, cls);
				CollectionUtils.mergePropertiesIntoMap(mappings, toolingMappings);
			}
			catch (IOException e) {
			}

			for (Object xsd : schemaMappings.keySet()) {
				String key = xsd.toString();
				String namespaceUri = getTargetNamespace(cls.getResource(schemaMappings.getProperty(key)));
				String icon = toolingMappings.get(namespaceUri + "@icon");
				String prefix = toolingMappings.get(namespaceUri + "@prefix");
				String name = toolingMappings.get(namespaceUri + "@name");

				if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
					namespaceDefinitionRegistry.get(namespaceUri).addSchemaLocation(key);
					namespaceDefinitionRegistry.get(namespaceUri).addUri(schemaMappings.getProperty(key));
				}
				else {
					NamespaceDefinition namespaceDefinition = new NamespaceDefinition(schemaMappings);
					namespaceDefinition.setName(name);
					namespaceDefinition.setPrefix(prefix);
					namespaceDefinition.setIconPath(icon);
					namespaceDefinition.addSchemaLocation(key);
					namespaceDefinition.setNamespaceUri(namespaceUri);
					namespaceDefinition.addUri(schemaMappings.getProperty(key));
					namespaceDefinitionRegistry.put(namespaceUri, namespaceDefinition);
				}
			}
			Set<INamespaceDefinition> definitions = new HashSet<INamespaceDefinition>();
			definitions.addAll(namespaceDefinitionRegistry.values());
			
//			System.out.println(String.format("-- loading of namespace definitions took '%s'ms", (System.currentTimeMillis() - start)));
			
			return definitions;
		}
		return resolver.getNamespaceDefinitions();
	}

	private String getTargetNamespace(URL url) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(url.openStream());
			return doc.getDocumentElement().getAttribute("targetNamespace");
		}
		catch (ParserConfigurationException e) {
			BeansCorePlugin.log(e);
		}
		catch (SAXException e) {
			BeansCorePlugin.log(e);
		}
		catch (IOException e) {
			BeansCorePlugin.log(e);
		}
		return null;
	}

	public INamespaceDefinition resolveNamespaceDefinition(String namespaceUri) {
		return resolver.resolveNamespaceDefinition(namespaceUri);
	}

	public NamespaceHandler resolve(String namespaceUri) {
		return resolver.resolve(namespaceUri);
	}

}
