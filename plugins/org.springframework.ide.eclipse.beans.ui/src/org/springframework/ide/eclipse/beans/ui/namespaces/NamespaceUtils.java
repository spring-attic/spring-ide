/*******************************************************************************
 * Copyright (c) 2006, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceDefinition.Version;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.util.StringUtils;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class NamespaceUtils {

	public static final String NAMESPACES_EXTENSION_POINT = BeansUIPlugin.PLUGIN_ID + ".namespaces";

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	public static final String TOOLS_NAMESPACE_URI = "http://www.springframework.org/schema/tool";

	public static final String P_NAMESPACE_URI = "http://www.springframework.org/schema/p";
	
	public static final String C_NAMESPACE_URI = "http://www.springframework.org/schema/c";

	private static final Object IMAGE_REGISTRY_LOCK = new Object();

	private static final INamespaceDefinition P_NAMESPACE_DEFINITION = new DefaultNamespaceDefinition("p",
			P_NAMESPACE_URI, null, new DefaultImageAccessor(BeansUIPlugin.PLUGIN_ID,
					"/icons/full/obj16/property_obj.gif"));
	
	private static final INamespaceDefinition C_NAMESPACE_DEFINITION = new DefaultNamespaceDefinition("c",
			C_NAMESPACE_URI, null, new DefaultImageAccessor(BeansUIPlugin.PLUGIN_ID,
					"/icons/full/obj16/constructor_obj.gif"));

	/**
	 * Returns the namespace URI for the given {@link ISourceModelElement} or
	 * <code>"http://www.springframework.org/schema/beans"</code> if no namespace URI found.
	 */
	public static String getNameSpaceURI(ISourceModelElement element) {
		String namespaceURI = ModelUtils.getNameSpaceURI(element);
		if (namespaceURI == null) {
			namespaceURI = DEFAULT_NAMESPACE_URI;
		}
		return namespaceURI;
	}

	/**
	 * Returns the {@link INamespaceLabelProvider} for the given {@link ISourceModelElement}'s namespace.
	 */
	public static INamespaceLabelProvider getLabelProvider(ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						if (config.getAttribute("labelProvider") != null) {
							try {
								Object provider = config.createExecutableExtension("labelProvider");
								if (provider instanceof INamespaceLabelProvider) {
									return (INamespaceLabelProvider) provider;
								}
							}
							catch (CoreException e) {
								BeansUIPlugin.log(e);
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the {@link ITreeContentProvider} for the given {@link ISourceModelElement}'s namespace.
	 */
	public static ITreeContentProvider getContentProvider(ISourceModelElement element) {
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = getNameSpaceURI(element);
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {
						if (config.getAttribute("contentProvider") != null) {
							try {
								Object provider = config.createExecutableExtension("contentProvider");
								if (provider instanceof IContentProvider) {
									return (ITreeContentProvider) provider;
								}
							}
							catch (CoreException e) {
								BeansUIPlugin.log(e);
							}
						}
						return null;
					}
				}
			}
		}
		return null;
	}

	public static List<INamespaceDefinition> getNamespaceDefinitions() {
		return getNamespaceDefinitions(null);
	}

	public static List<INamespaceDefinition> getNamespaceDefinitions(final IProject project,
			final INamespaceDefinitionTemplate definitionTemplate) {
		if (definitionTemplate != null) {

			Job namespaceDefinitionJob = new Job((project != null ? String.format(
					"Loading namespaces for project '%s'", project.getName()) : "Loading namespaces")) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					List<INamespaceDefinition> namespaceDefinitions = getNamespaceDefinitions(project);
					definitionTemplate.doWithNamespaceDefinitions((INamespaceDefinition[]) namespaceDefinitions
							.toArray(new INamespaceDefinition[namespaceDefinitions.size()]), project);
					return Status.OK_STATUS;
				}
			};
			namespaceDefinitionJob.setPriority(Job.INTERACTIVE);
			namespaceDefinitionJob.schedule();

			return Collections.emptyList();
		}
		return getNamespaceDefinitions(project);
	}

	protected static List<INamespaceDefinition> getNamespaceDefinitions(IProject project) {
		List<INamespaceDefinition> namespaceDefinitions = new ArrayList<INamespaceDefinition>();

		INamespaceDefinitionResolver definitionResolver = BeansCorePlugin.getNamespaceDefinitionResolver(project);
		Set<org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition> detectedNamespaceDefinitions = definitionResolver
				.getNamespaceDefinitions();

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null
				&& !org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils
						.useNamespacesFromClasspath(project)) {
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					String uri = config.getAttribute("uri");
					org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = definitionResolver
							.resolveNamespaceDefinition(uri);
					detectedNamespaceDefinitions.remove(namespaceDefinition);

					String prefix = config.getAttribute("prefix");
					if (!StringUtils.hasText(prefix) && namespaceDefinition != null) {
						prefix = namespaceDefinition.getPrefix();
					}
					String schemaLocation = config.getAttribute("defaultSchemaLocation");
					if (!StringUtils.hasText(schemaLocation) && namespaceDefinition != null) {
						schemaLocation = namespaceDefinition.getDefaultSchemaLocation();
					}
					IImageAccessor image = null;
					if (config.getAttribute("icon") != null) {
						String ns = config.getDeclaringExtension().getNamespaceIdentifier();
						String icon = config.getAttribute("icon");
						image = new DefaultImageAccessor(ns, icon);
					}
					else if (namespaceDefinition != null) {
						image = new NamespaceDefinitionImageAccessor(namespaceDefinition);
					}

					DefaultNamespaceDefinition def = null;
					if (namespaceDefinition != null) {
						def = new DefaultNamespaceDefinition(prefix, uri, schemaLocation,
								namespaceDefinition.getUriMapping(), image);
					}
					else {
						def = new DefaultNamespaceDefinition(prefix, uri, schemaLocation, new Properties(), image);
					}

					// get schema locations from nested child elements
					IConfigurationElement[] schemaLocationConfigElements = config.getChildren("schemaLocation");
					for (IConfigurationElement schemaLocationConfigElement : schemaLocationConfigElements) {
						def.addSchemaLocation(schemaLocationConfigElement.getAttribute("url"));
					}
					if (def.getSchemaLocations().size() == 0 && namespaceDefinition != null) {
						def.getSchemaLocations().addAll(namespaceDefinition.getSchemaLocations());
					}

					namespaceDefinitions.add(def);
				}
			}
		}

		for (org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition : detectedNamespaceDefinitions) {
			DefaultNamespaceDefinition def = new DefaultNamespaceDefinition(namespaceDefinition.getPrefix(),
					namespaceDefinition.getNamespaceUri(), namespaceDefinition.getDefaultSchemaLocation(),
					namespaceDefinition.getUriMapping(), new NamespaceDefinitionImageAccessor(namespaceDefinition));
			def.getSchemaLocations().addAll(namespaceDefinition.getSchemaLocations());
			namespaceDefinitions.add(def);
		}

		boolean foundPNamespace = false;
		boolean foundCNamespace = false;
		INamespaceDefinition defaultNamespace = null; 
		
		// Remove the tool namespace as we don't want to surface on the UI
		for (INamespaceDefinition definition : new ArrayList<INamespaceDefinition>(namespaceDefinitions)) {
			if (TOOLS_NAMESPACE_URI.equals(definition.getNamespaceURI())) {
				namespaceDefinitions.remove(definition);
			} else if (DEFAULT_NAMESPACE_URI.equals(definition.getNamespaceURI())) {
				defaultNamespace = definition;
			}
			else if (P_NAMESPACE_URI.equals(definition.getNamespaceURI())) {
				foundPNamespace = true;
			}
			else if (C_NAMESPACE_URI.equals(definition.getNamespaceURI())) {
				foundCNamespace = true;
			}
		}
		
		if (!foundPNamespace && defaultNamespace != null) {
			// Add in p-Namespace if we found the default namespace
			namespaceDefinitions.add(P_NAMESPACE_DEFINITION);
		}
		if (!foundCNamespace && defaultNamespace != null) {
			// Add in c-Namespace if we found the default namespace
			// && is Spring 3.1 or greater
			Set<String> locations = defaultNamespace.getSchemaLocations();
			for (String locationUri : locations) {
				if (isSpring31(locationUri)) {
					namespaceDefinitions.add(C_NAMESPACE_DEFINITION);
					break;
				}
			}
		}

		Collections.sort(namespaceDefinitions, new Comparator<INamespaceDefinition>() {
			public int compare(INamespaceDefinition o1, INamespaceDefinition o2) {
				if (o1 != null && o1.getDefaultNamespacePrefix() != null && o2 != null
						&& o2.getDefaultNamespacePrefix() != null) {
					return o1.getDefaultNamespacePrefix().compareTo(o2.getDefaultNamespacePrefix());
				}
				return 0;
			}
		});

		return namespaceDefinitions;
	}
	
	private static boolean isSpring31(String locationUri) {
		Version minimum = new Version("3.1");
		Version temp = Version.MINIMUM_VERSION;
		Matcher matcher = DefaultNamespaceDefinition.VERSION_PATTERN.matcher(locationUri);
		if (matcher.matches()) {
			temp = new Version(matcher.group(1));
		}
		if (temp.compareTo(minimum) >= 0) {
			return true;
		}
		return false;
	}

	public static INamespaceDefinition getDefaultNamespaceDefinition() {
		INamespaceDefinitionResolver definitionResolver = BeansCorePlugin.getNamespaceDefinitionResolver(null);
		org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = definitionResolver
				.resolveNamespaceDefinition(DEFAULT_NAMESPACE_URI);

		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(NAMESPACES_EXTENSION_POINT);
		if (point != null) {
			String namespaceURI = DEFAULT_NAMESPACE_URI;
			for (IExtension extension : point.getExtensions()) {
				for (IConfigurationElement config : extension.getConfigurationElements()) {
					if (namespaceURI.equals(config.getAttribute("uri"))) {

						String prefix = config.getAttribute("prefix");
						if (!StringUtils.hasText(prefix) && namespaceDefinition != null) {
							prefix = namespaceDefinition.getPrefix();
						}
						String schemaLocation = config.getAttribute("defaultSchemaLocation");
						if (!StringUtils.hasText(schemaLocation) && namespaceDefinition != null) {
							schemaLocation = namespaceDefinition.getDefaultSchemaLocation();
						}
						String uri = config.getAttribute("uri");
						IImageAccessor image = null;
						if (config.getAttribute("icon") != null) {
							String ns = config.getDeclaringExtension().getNamespaceIdentifier();
							String icon = config.getAttribute("icon");
							image = new DefaultImageAccessor(ns, icon);
						}
						else if (namespaceDefinition != null) {
							image = new NamespaceDefinitionImageAccessor(namespaceDefinition);
						}
						return new DefaultNamespaceDefinition(prefix, uri, schemaLocation,
								namespaceDefinition.getUriMapping(), image);
					}
				}
			}
		}

		if (namespaceDefinition != null) {
			return new DefaultNamespaceDefinition(namespaceDefinition.getPrefix(),
					namespaceDefinition.getNamespaceUri(), namespaceDefinition.getDefaultSchemaLocation(),
					namespaceDefinition.getUriMapping(), new NamespaceDefinitionImageAccessor(namespaceDefinition));
		}
		return null;
	}

	/**
	 * Returns an {@link Image} instance which is located at the indicated icon path.
	 */
	public static Image getImage(
			org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition) {
		if (StringUtils.hasText(namespaceDefinition.getIconPath())) {
			synchronized (IMAGE_REGISTRY_LOCK) {
				Image image = BeansUIPlugin.getDefault().getImageRegistry().get(namespaceDefinition.getIconPath());
				if (image == null) {
					InputStream is = namespaceDefinition.getIconStream();
					if (is != null) {
						try {
							ImageDescriptor imageDescriptor = ImageDescriptor.createFromImageData(new ImageData(is));
							BeansUIPlugin.getDefault().getImageRegistry()
									.put(namespaceDefinition.getIconPath(), imageDescriptor);
							image = BeansUIPlugin.getDefault().getImageRegistry()
									.get(namespaceDefinition.getIconPath());
						}
						catch (Exception e) {
							BeansUIPlugin.log(String.format(
									"Error creating image resource for namespace definition '%s'",
									namespaceDefinition.getNamespaceUri()), e);
							return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
						}
						finally {
							if (is != null) {
								try {
									is.close();
								}
								catch (IOException e) {
								}
							}
						}
					}
					else {
						BeansUIPlugin
								.getDefault()
								.getImageRegistry()
								.put(namespaceDefinition.getIconPath(),
										BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD));
						image = BeansUIPlugin.getDefault().getImageRegistry().get(namespaceDefinition.getIconPath());
					}
				}
				return image;
			}
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}
	}

	/**
	 * Returns an {@link Image} instance which is located at the indicated icon path.
	 */
	private static Image getImage(String ns, String icon) {
		if (StringUtils.hasText(icon)) {
			synchronized (IMAGE_REGISTRY_LOCK) {
				Image image = BeansUIPlugin.getDefault().getImageRegistry().get(icon);
				if (image == null) {
					ImageDescriptor imageDescriptor = BeansUIPlugin.imageDescriptorFromPlugin(ns, icon);
					BeansUIPlugin.getDefault().getImageRegistry().put(icon, imageDescriptor);
					image = BeansUIPlugin.getDefault().getImageRegistry().get(icon);
				}
				return image;
			}
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
		}
	}

	public interface INamespaceDefinitionTemplate {

		void doWithNamespaceDefinitions(INamespaceDefinition[] namespaceDefinitions, IProject project);

	}

	private static class DefaultImageAccessor extends DisplayThreadImageAccessor {

		private final String ns;

		private final String icon;

		public DefaultImageAccessor(String ns, String icon) {
			this.ns = ns;
			this.icon = icon;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image createImage() {
			return NamespaceUtils.getImage(ns, icon);
		}
	}

	private static class NamespaceDefinitionImageAccessor extends DisplayThreadImageAccessor {

		private final org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition definition;

		public NamespaceDefinitionImageAccessor(
				org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition definition) {
			this.definition = definition;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Image createImage() {
			return NamespaceUtils.getImage(definition);
		}
	}

}
