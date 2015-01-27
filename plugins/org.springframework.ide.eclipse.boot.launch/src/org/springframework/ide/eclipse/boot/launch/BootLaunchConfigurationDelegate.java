/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

import static org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME;
import static org.springframework.ide.eclipse.boot.util.StringUtil.hasText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.launch.livebean.LiveBeanSupport;

/**
 * @author Kris De Volder
 */
public class BootLaunchConfigurationDelegate extends JavaLaunchDelegate {

	//private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	public static final String LAUNCH_CONFIG_TYPE_ID = "org.springframework.ide.eclipse.boot.launch";

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

	private static final String ENABLE_DEBUG_OUTPUT = "spring.boot.debug.enable";
	public static final boolean DEFAULT_ENABLE_DEBUG_OUTPUT = false;

	private static final String ENABLE_LIVE_BEAN_SUPPORT = "spring.boot.livebean.enable";
	public static final boolean DEFAULT_ENABLE_LIVE_BEAN_SUPPORT = true;

	private static final String JMX_PORT = "spring.boot.livebean.port";

	private static final String PROFILE = "spring.boot.profile";
	public static final String DEFAULT_PROFILE = "";


//	static void debug(ILaunchConfiguration c, String msg) {
//		if (DEBUG) {
//			System.out.println(c+"#"+ c.hashCode()+ ": "+msg);
//		}
//	}

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
			return name + "="+ value;
		}
	}

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		super.launch(configuration, mode, launch, monitor);
	}

	@Override
	public String getProgramArguments(ILaunchConfiguration conf) throws CoreException {
		List<PropVal> props = getProperties(conf);
		String profile = getProfile(conf);
		boolean debugOutput = getEnableDebugOutput(conf);
		if ((props==null || props.isEmpty()) && !debugOutput && !hasText(profile)) {
			//shortcut for case where no boot-specific customizations are specified.
			return super.getProgramArguments(conf);
		}
		ArrayList<String> args = new ArrayList<String>();
		if (debugOutput) {
			args.add("--debug");
		}
		if (hasText(profile)) {
			args.add(propertyAssignmentArgument("spring.profiles.active", profile));
		}
		addPropertiesArguments(args, props);
		args.addAll(Arrays.asList(DebugPlugin.parseArguments(super.getProgramArguments(conf))));
		return DebugPlugin.renderArguments(args.toArray(new String[args.size()]), null);
	}

	@Override
	public String getVMArguments(ILaunchConfiguration conf)
			throws CoreException {
		try {
			if (getEnableLiveBeanSupport(conf)) {
				int port = 0;
				try {
					port = Integer.parseInt(getJMXPort(conf));
				} catch (Exception e) {
					//ignore: bad data in launch config.
				}
				if (port==0) {
					port = LiveBeanSupport.randomPort();
				}
				String enableLiveBeanArgs = LiveBeanSupport.liveBeanVmArgs(getJMXPort(conf));
				return enableLiveBeanArgs + super.getVMArguments(conf);
			}
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return super.getVMArguments(conf);
	}

	private void addPropertiesArguments(ArrayList<String> args, List<PropVal> props) {
		for (PropVal p : props) {
			//spring boot doesn't like empty option keys/values so skip those.
			if (p.isChecked && !p.name.isEmpty() && !p.value.isEmpty()) {
				args.add(propertyAssignmentArgument(p.name, p.value));
			}
		}
	}

	private String propertyAssignmentArgument(String name, String value) {
		if (name.contains("=")) {
			//spring boot has no handling of escape sequences like '\='
			//so we cannot represent keys containing '='.
			throw new IllegalArgumentException("property name shouldn't contain '=':"+name);
		}
		return "--"+name + "=" +value;
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
			if (hasText(p.name)) {
				String prefixed = PROPS_PREFIX+p.name+OID_SEPERATOR+(oid++);
				String valueEnabled = (p.isChecked?'1':'0')+p.value;
				conf.setAttribute(prefixed, valueEnabled);
			}
		}
	}

	/**
	 * Get the project associated with this a luanch config. Note that this
	 * method returns an IProject reference regardless of whether or not the
	 * project exists.
	 */
	public static IProject getProject(ILaunchConfiguration conf) {
		try {
			String pname = conf.getAttribute(ATTR_PROJECT_NAME, "");
			if (hasText(pname)) {
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

	public static void setProject(ILaunchConfigurationWorkingCopy conf, IProject p) {
		//debug(conf, "setProject <= "+p);
		if (p==null) {
			conf.removeAttribute(ATTR_PROJECT_NAME);
		} else {
			conf.setAttribute(ATTR_PROJECT_NAME, p.getName());
		}
	}

	public static boolean getEnableLiveBeanSupport(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(ENABLE_LIVE_BEAN_SUPPORT, DEFAULT_ENABLE_LIVE_BEAN_SUPPORT);
		} catch (Exception e) {
			BootActivator.log(e);
		}
		return DEFAULT_ENABLE_LIVE_BEAN_SUPPORT;
	}

	public static String getJMXPort(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(JMX_PORT, (String)null);
		} catch (CoreException e) {
			BootActivator.log(e);
		}
		return null;
	}

	public static void setEnableLiveBeanSupport(ILaunchConfigurationWorkingCopy conf, boolean value) {
		conf.setAttribute(ENABLE_LIVE_BEAN_SUPPORT, value);
	}

	public static void setJMXPort(ILaunchConfigurationWorkingCopy conf, String portAsStr) {
		conf.setAttribute(JMX_PORT, portAsStr);
	}

	public static String getProfile(ILaunchConfiguration conf) {
		try {
			return conf.getAttribute(PROFILE, DEFAULT_PROFILE);
		} catch (CoreException e) {
			BootActivator.log(e);
			return DEFAULT_PROFILE;
		}
	}

	public static void setProfile(ILaunchConfigurationWorkingCopy conf, String profile) {
		conf.setAttribute(PROFILE, profile);
	}

}
