/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.util.StringUtil;

public abstract class AbstractBootLaunchConfigurationDelegate extends JavaLaunchDelegate {

	private static final String M2E_CLASSPATH_PROVIDER = "org.eclipse.m2e.launchconfig.classpathProvider";
	protected static final String M2E_SOURCEPATH_PROVIDER = "org.eclipse.m2e.launchconfig.sourcepathProvider";
	public static final String JAVA_LAUNCH_CONFIG_TYPE_ID = IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION;
	public static final String ENABLE_DEBUG_OUTPUT = "spring.boot.debug.enable";
	public static final boolean DEFAULT_ENABLE_DEBUG_OUTPUT = false;

	private static final String BOOT_MAVEN_SOURCE_PATH_PROVIDER = "org.springframework.ide.eclipse.boot.launch.BootMavenSourcePathProvider";

	/**
	 * Spring boot properties are stored as launch confiuration properties with
	 * an extra prefix added to property name to avoid name clashes with
	 * other launch config properties.
	 */
	private static final String PROPS_PREFIX = "spring.boot.prop.";

	/**
	 * To be able to store multiple assignment to the same spring boot
	 * property name we add a 'oid' at the end of each stored
	 * property name. ?_SEPERATOR is used to separate the 'real'
	 * property name from the 'oid' string.
	 */
	private static final char OID_SEPERATOR = ':';

	public static class PropVal {
		public String name;
		public String value;
		public boolean isChecked;

		public PropVal(String name, String value, boolean isChecked) {
			//Don't use null, use empty Strings
			Assert.isNotNull(name);
			Assert.isNotNull(value);
			this.name = name;
			this.value = value;
			this.isChecked = isChecked;
		}

		@Override
		public String toString() {
			return (isChecked?"[X] ":"[ ] ") +
					name + "="+ value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isChecked ? 1231 : 1237);
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PropVal other = (PropVal) obj;
			if (isChecked != other.isChecked)
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}
	}

	@Override
	public String[] getClasspath(ILaunchConfiguration conf) throws CoreException {
		try {
			//Must do exactly what a Java Launch config would do. It is not enough to simply
			// call super. Me must also pass a launch config exactly like the JDT one, including
			// its type and some 'magic' attributes added for m2e
			ILaunchConfigurationWorkingCopy wc = copyAs(conf, JAVA_LAUNCH_CONFIG_TYPE_ID);
			enableMavenClasspathProvider(wc);
			return super.getClasspath(wc);
		} catch (Exception e) {
			//In case the hacky stuff above fails, do something that mostly works, even if
			// it does gets a classpath polluted with test dependencies.
			// See https://issuetracker.springsource.com/browse/STS-4085
			BootActivator.log(e);
			return super.getClasspath(conf);
		}
	}

	public static List<ILaunchConfiguration> getLaunchConfigs(IProject p, String confTypeId) {
		try {
			ILaunchManager lm = getLaunchMan();
			ILaunchConfigurationType type = lm.getLaunchConfigurationType(confTypeId);
			if (type!=null) {
				ILaunchConfiguration[] configs = lm.getLaunchConfigurations(type);
				if (configs!=null && configs.length>0) {
					ArrayList<ILaunchConfiguration> result = new ArrayList<ILaunchConfiguration>();
					for (ILaunchConfiguration conf : configs) {
						if (p.equals(getProject(conf))) {
							result.add(conf);
						}
					}
					return result;
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return Collections.emptyList();
	}

	public static void clearProperties(ILaunchConfigurationWorkingCopy conf) {
		try {
			//note: e43 doesn't use generics for conf.getAttributes, hence the
			// funky casting below.
			for (Object _prefixedProp : conf.getAttributes().keySet()) {
				String prefixedProp = (String) _prefixedProp;
				if (prefixedProp.startsWith(PROPS_PREFIX)) {
					conf.removeAttribute(prefixedProp);
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	public static void setMainType(ILaunchConfigurationWorkingCopy config, String typeName) {
		config.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, typeName);
	}

	@SuppressWarnings("unchecked")
	public static List<PropVal> getProperties(ILaunchConfiguration conf) {
		ArrayList<PropVal> props = new ArrayList<PropVal>();
		try {
			//Note: in e43 conf.getAttributes doesn't use generics yet. So to
			//build with 4.3 we need to to some funky casting below.
			for (Object _e : conf.getAttributes().entrySet()) {
				try {
					Map.Entry<String, Object> e = (Entry<String, Object>) _e;
					String prefixed = e.getKey();
					if (prefixed.startsWith(PROPS_PREFIX)) {
						String name = prefixed.substring(PROPS_PREFIX.length());
						int dotPos = name.lastIndexOf(OID_SEPERATOR);
						if (dotPos>=0) {
							name = name.substring(0, dotPos);
						}
						String valueEnablement = (String)e.getValue();
						String value = valueEnablement.substring(1);
						boolean enabled = valueEnablement.charAt(0)=='1';
						props.add(new PropVal(name, value, enabled));
					}
				} catch (Exception ignore) {
					//silently ignore invalid property data.
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return props;
	}

	public static void setProperties(ILaunchConfigurationWorkingCopy conf, List<PropVal> props) {
		if (props==null) {
			props = Collections.emptyList();
		}
		clearProperties(conf);
		int oid = 0; //unique id appended to each stored key, otherwise we loose
		//entries with identical keys.
		for (PropVal p : props) {
			//Don't store stuff with 'empty keys'. These are likely just
			// 'empty' entries user added but never filled in.
			if (StringUtil.hasText(p.name)) {
				String prefixed = PROPS_PREFIX+p.name+OID_SEPERATOR+(oid++);
				String valueEnabled = (p.isChecked?'1':'0')+p.value;
				conf.setAttribute(prefixed, valueEnabled);
			}
		}
	}

	protected void addPropertiesArguments(ArrayList<String> args, List<PropVal> props) {
		for (PropVal p : props) {
			//spring boot doesn't like empty option keys/values so skip those.
			if (p.isChecked && !p.name.isEmpty() && !p.value.isEmpty()) {
				args.add(propertyAssignmentArgument(p.name, p.value));
			}
		}
	}

	protected String propertyAssignmentArgument(String name, String value) {
		if (name.contains("=")) {
			//spring boot has no handling of escape sequences like '\='
			//so we cannot represent keys containing '='.
			throw new IllegalArgumentException("property name shouldn't contain '=':"+name);
		}
		return "--"+name + "=" +value;
	}


	public static boolean getEnableDebugOutput(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_DEBUG_OUTPUT, DEFAULT_ENABLE_DEBUG_OUTPUT);
		} catch (Exception e) {
			BootActivator.log(e);
			return DEFAULT_ENABLE_DEBUG_OUTPUT;
		}
	}

	public static void setEnableDebugOutput(ILaunchConfigurationWorkingCopy conf, boolean enable) {
		conf.setAttribute(ENABLE_DEBUG_OUTPUT, enable);
	}

	/**
	 * Get the project associated with this a luanch config. Note that this
	 * method returns an IProject reference regardless of whether or not the
	 * project exists.
	 */
	public static IProject getProject(ILaunchConfiguration conf) {
		try {
			String pname = getProjectName(conf);
			if (StringUtil.hasText(pname)) {
				IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(pname);
				//debug(conf, "getProject => "+p);
				return p;
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		//debug(conf, "getProject => NULL");
		return null;
	}

	public static String getProjectName(ILaunchConfiguration conf)
			throws CoreException {
		return conf.getAttribute(ATTR_PROJECT_NAME, "");
	}

	public static void setProject(ILaunchConfigurationWorkingCopy conf, IProject p) {
		//debug(conf, "setProject <= "+p);
		if (p==null) {
			conf.removeAttribute(ATTR_PROJECT_NAME);
		} else {
			conf.setAttribute(ATTR_PROJECT_NAME, p.getName());
		}
	}

	/**
	 * Enable maven classpath provider if applicable to this conf.
	 * Addresses https://issuetracker.springsource.com/browse/STS-4085
	 */
	static void enableMavenClasspathProvider(ILaunchConfigurationWorkingCopy conf) {
		try {
			if (conf.getType().getIdentifier().equals(JAVA_LAUNCH_CONFIG_TYPE_ID)) {
				//Take care not to add this a 'real' Boot launch config or it will cause m2e to throw exceptions
				//These 'magic' attributes should only be added to a 'cloned' copy of our config with the right type.
				IProject p = getProject(conf);
				if (p!=null && p.hasNature(SpringBootCore.M2E_NATURE)) {
					if (!conf.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER)) {
						conf.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH_PROVIDER, M2E_CLASSPATH_PROVIDER);
					}
					if (!conf.hasAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER)) {
						conf.setAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, M2E_SOURCEPATH_PROVIDER);
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
	}

	/**
	 * Copy a given launch config into a 'clone' that has all the same attributes but
	 * a different type id.
	 * @throws CoreException
	 */
	private static ILaunchConfigurationWorkingCopy copyAs(ILaunchConfiguration conf,
			String newType) throws CoreException {
		ILaunchManager launchManager = getLaunchMan();
		ILaunchConfigurationType launchConfigurationType = launchManager
				.getLaunchConfigurationType(newType);
		ILaunchConfigurationWorkingCopy wc = launchConfigurationType.newInstance(null,
				launchManager.generateLaunchConfigurationName(conf.getName()));
		wc.setAttributes(conf.getAttributes());
		return wc;
	}

	public static ILaunchManager getLaunchMan() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

	@Override
	public void launch(ILaunchConfiguration conf, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException {
		conf = configureSourcePathProvider(conf);
		super.launch(conf, mode, launch, monitor);
	}

	protected ILaunchConfiguration configureSourcePathProvider(ILaunchConfiguration conf) throws CoreException {
		IProject project = BootLaunchConfigurationDelegate.getProject(conf);
		if (project.hasNature(SpringBootCore.M2E_NATURE)) {
			conf = setAttribute(conf, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH_PROVIDER, BOOT_MAVEN_SOURCE_PATH_PROVIDER);
		}
		return conf;
	}

	private ILaunchConfiguration setAttribute(ILaunchConfiguration conf, String a, String v) {
		try {
			if (!Objects.equals(v, conf.getAttribute(a, (String)null))) {
				ILaunchConfigurationWorkingCopy wc = conf.getWorkingCopy();
				wc.setAttribute(a, v);
				conf = wc.doSave();
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return conf;
	}


}
