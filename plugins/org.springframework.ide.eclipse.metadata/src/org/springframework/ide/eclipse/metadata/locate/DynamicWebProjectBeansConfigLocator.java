/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.metadata.locate;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.ide.eclipse.beans.core.model.locate.AbstractJavaProjectPathMatchingBeansConfigLocator;
import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.metadata.MetadataPlugin;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * {@link IBeansConfigLocator} implementation that uses the information from a <code>web.xml</code> to detect Spring
 * configuration files.
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @since 1.0.0
 */
public class DynamicWebProjectBeansConfigLocator extends AbstractJavaProjectPathMatchingBeansConfigLocator {

	/** Name of the config set to be created for the auto-detected configs */
	private static final String CONFIG_SET_NAME = "web-context"; //$NON-NLS-1$

	/** Context parameter name to configure non-default config locations */
	private static final String CONTEXT_CONFIG_LOCATION_PARAM_NAME = "contextConfigLocation"; //$NON-NLS-1$

	/** The FQCN of the {@link ContextLoaderListener} */
	private static final String CONTEXT_CONTEXT_LOADER_LISTENER_SERVLET = "org.springframework.web.context.ContextLoaderListener"; //$NON-NLS-1$

	/** The FQCN of the {@link ContextLoaderServlet} */
	private static final String CONTEXT_CONTEXT_LOADER_SERVLET_CLASS = "org.springframework.web.context.ContextLoaderServlet"; //$NON-NLS-1$

	/** The FQCN of the DispatcherServlet */
	private static final String DISPATCHER_SERVLET_CLASS = "org.springframework.web.servlet.DispatcherServlet"; //$NON-NLS-1$
	
	/** The FQCN of the Spring WS MessageDispatcherServlet */
	private static final String MESSAGE_DISPATCHER_SERVLET_CLASS = "org.springframework.ws.transport.http.MessageDispatcherServlet"; //$NON-NLS-1$

	private static final String LISTENER_XPATH_EXPRESSION = "//web-app/listener";

	private static final String CONTEXT_PARAM_XPATH_EXPRESSION = "//web-app/context-param";

	private static final String SERVLET_XPATH_EXPRESSION = "//web-app/servlet";

	private static final String LISTENER_CLASS = "listener-class";

	private static final String INIT_PARAM = "init-param";

	private static final String PARAM_NAME = "param-name";

	private static final String PARAM_VALUE = "param-value";

	private static final String SERVLET_CLASS = "servlet-class";

	private static final String SERVLET_NAME = "servlet-name";

	/** Default servlet context file suffix */
	private static final String SERVLET_CONTEXT_SUFFIX = "-servlet.xml"; //$NON-NLS-1$

	/** The detected file patterns to search for */
	private Set<String> filePatterns = new ConcurrentSkipListSet<String>();

	private final XPathExpression servletExpression;

	private final XPathExpression listenerExpression;

	private final XPathExpression contextParamExpression;

	{
		try {
			XPathFactory newInstance = XPathFactory.newInstance();
			XPath xpath = newInstance.newXPath();
			this.servletExpression = xpath.compile(SERVLET_XPATH_EXPRESSION);
			this.listenerExpression = xpath.compile(LISTENER_XPATH_EXPRESSION);
			this.contextParamExpression = xpath.compile(CONTEXT_PARAM_XPATH_EXPRESSION);
		}
		catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getBeansConfigSetName(Set<IFile> files) {
		return CONFIG_SET_NAME;
	}

	/**
	 * Returns <code>true</code> if the given <code>file</code> is a <code>web.xml</code>.
	 */
	public boolean requiresRefresh(IFile file) {
		if (file.getFullPath().toString().endsWith("WEB-INF/web.xml")) {
			IFile webArtifact = SpringCoreUtils.getDeploymentDescriptor(file.getProject());
			if (webArtifact != null) {
				return file.getFullPath().equals(webArtifact.getFullPath());
			}
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the given project has the Spring IDE nature and has the <code>jst.web</code> facet.
	 */
	public boolean supports(IProject project) {
		return SpringCoreUtils.isSpringProject(project) && SpringCoreUtils.hasProjectFacet(project, "jst.web"); //$NON-NLS-1$
	}

	private String getChildValue(Element node, String nodeName) {
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (nodeName.equals(child.getNodeName())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Creates file patterns from the given {@link #CONTEXT_CONFIG_LOCATION_PARAM_NAME} context parameters.
	 */
	private boolean processContextConfigLocationParameter(NodeList initParams) {
		boolean nonDefaultLocations = false;

		for (int i = 0; i < initParams.getLength(); i++) {
			String name = null;
			String value = null;

			Node initParam = initParams.item(i);
			NodeList initParamChildren = initParam.getChildNodes();
			for (int j = 0; j < initParamChildren.getLength(); j++) {
				Node initParamChild = initParamChildren.item(j);
				if (PARAM_NAME.equals(initParamChild.getNodeName())) {
					name = initParamChild.getTextContent();
				}
				else if (PARAM_VALUE.equals(initParamChild.getNodeName())) {
					value = initParamChild.getTextContent();
				}
			}
			if (CONTEXT_CONFIG_LOCATION_PARAM_NAME.equals(name) && StringUtils.hasText(value)) {
				String[] configLocations = StringUtils.tokenizeToStringArray(value,
						ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
				nonDefaultLocations = true;
				for (String configLocation : configLocations) {

					// Remove classpath: protocal from config location
					int ix = configLocation.indexOf(':');
					if (ix > 0) {
						configLocation = configLocation.substring(ix + 1);
					}

					filePatterns.add(configLocation);
				}
			}
		}

		// By default if non specific configuration is found we need to install the default location
		if (!nonDefaultLocations) {
			filePatterns.add(XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION);
		}
		return nonDefaultLocations;
	}

	/**
	 * Reads the <code>web.xml</code> deployment descriptor and searches for {@link ContextLoaderListener},
	 * {@link ContextLoaderServlet} and DispatcherServlet configuration sections.
	 */
	@Override
	protected boolean canLocateInProject(IProject project) {
		// Make sure it is a java project
		if (!super.canLocateInProject(project)) {
			return false;
		}

		// Add the path to the WEB-INF directory as root dir
		IFile deploymentDescriptor = SpringCoreUtils.getDeploymentDescriptor(project);
		if (deploymentDescriptor != null) {
			try {
				NodeList nodes = (NodeList) servletExpression.evaluate(SpringCoreUtils
						.parseDocument(deploymentDescriptor), XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element element = (Element) nodes.item(i);
					String servletName = getChildValue(element, SERVLET_NAME);
					String servletClass = getChildValue(element, SERVLET_CLASS);
					if (StringUtils.hasText(servletName) && StringUtils.hasText(servletClass)) {

						// Figure out the DispatcherServlet names to add
						// "WEB-INF/<servlet-name>-servlet.xml" as file pattern and install default
						// pattern if required.
						if (DISPATCHER_SERVLET_CLASS.equals(servletClass)
								|| MESSAGE_DISPATCHER_SERVLET_CLASS.equals(servletClass)) {
							NodeList initParams = element.getElementsByTagName(INIT_PARAM);
							boolean nonDefaultLocations = processContextConfigLocationParameter(initParams);
							if (!nonDefaultLocations) {
								filePatterns.add(new StringBuilder(
										XmlWebApplicationContext.DEFAULT_CONFIG_LOCATION_PREFIX).append(servletName)
										.append(SERVLET_CONTEXT_SUFFIX).toString());
							}
						}

						// Add ContextLoaderServlet configuration
						else if (CONTEXT_CONTEXT_LOADER_SERVLET_CLASS.equals(servletClass)) {
							NodeList initParams = element.getElementsByTagName(INIT_PARAM);
							processContextConfigLocationParameter(initParams);
						}
					}
				}
			}
			catch (Exception e) {
				SpringCore.log(new Status(IStatus.WARNING, MetadataPlugin.PLUGIN_ID, 1, e.getMessage(), e));
			}

			// Add ContextLoaderListener configurations
			try {
				NodeList nodes = (NodeList) listenerExpression.evaluate(SpringCoreUtils
						.parseDocument(deploymentDescriptor), XPathConstants.NODESET);
				for (int i = 0; i < nodes.getLength(); i++) {
					Element element = (Element) nodes.item(i);
					String listenerClass = getChildValue(element, LISTENER_CLASS);
					if (CONTEXT_CONTEXT_LOADER_LISTENER_SERVLET.equals(listenerClass)) {

						NodeList contextParams = (NodeList) contextParamExpression.evaluate(SpringCoreUtils
								.parseDocument(deploymentDescriptor), XPathConstants.NODESET);
						processContextConfigLocationParameter(contextParams);
						break;
					}
				}
			}
			catch (Exception e) {
				SpringCore.log(new Status(IStatus.WARNING, MetadataPlugin.PLUGIN_ID, 1, e.getMessage(), e));
			}
		}
		else {
			return false;
		}

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Set<String> getAllowedFilePatterns() {
		return filePatterns;
	}

	/**
	 * Returns the root directories to search. Basically returns the java source folders and location of the
	 * <code>web.xml</code> as root directories.
	 */
	@Override
	protected Set<IPath> getRootDirectories(IProject project) {
		Set<IPath> rootDirectories = new LinkedHashSet<IPath>(super.getRootDirectories(project));

		// Add the path to the WEB-INF directory as root dir
		IFile webArtifact = SpringCoreUtils.getDeploymentDescriptor(project);
		if (webArtifact != null) {
			IPath path = webArtifact.getFullPath();
			if (path != null) {
				// Remove /WEB-INF/web.xml from path
				rootDirectories.add(path.removeLastSegments(2));
			}
		}

		return rootDirectories;
	}
}
