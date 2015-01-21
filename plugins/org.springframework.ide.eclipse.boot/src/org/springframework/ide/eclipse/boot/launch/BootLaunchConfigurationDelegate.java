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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.springframework.ide.eclipse.boot.core.BootActivator;

/**
 * @author Kris De Volder
 */
public class BootLaunchConfigurationDelegate extends JavaLaunchDelegate {

	public static final String LAUNCH_CONFIG_TYPE_ID = "org.springframework.ide.eclipse.boot.launch";

	public static class PropVal {
		public String name;
		public String value;
		boolean isChecked;

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
		if (props==null || props.isEmpty()) {
			//shortcut for case where no boot-specific customizations are specified.
			return super.getProgramArguments(conf);
		}
		ArrayList<String> args = new ArrayList<String>();
		addPropertiesArguments(args, props);
		args.addAll(Arrays.asList(DebugPlugin.parseArguments(super.getProgramArguments(conf))));
		return DebugPlugin.renderArguments(args.toArray(new String[args.size()]), null);
	}

	private void addPropertiesArguments(ArrayList<String> args, List<PropVal> props) {
		for (PropVal p : props) {
			//spring boot doesn't like empty option keys so skip those.
			if (p.isChecked && !p.name.isEmpty() && !p.value.isEmpty()) {
				//spring boot has no handling of escape sequences like '\=' to
				//so we cannot represent keys containing '='.
				if (p.name.contains("=")) {
					throw new IllegalArgumentException("property name's shouldn't contain '=':"+p);
				}
				args.add("--"+p.name + "=" +p.value);
			}
		}
	}

	/**
	 * Spring boot properties are stored as launch confiuration properties with
	 * an extra prefix added to property name to avoid name clashes with
	 * other launch config properties.
	 */
	private static final String PROPS_PREFIX = "spring.boot.prop.";

	/**
	 * To be able to store multiple assignment to the same spring boot
	 * property name we add a 'oid' at the end of each stored
	 * property name. OID_SEPERATOR is used to separate the 'real'
	 * property name from the 'oid' string.
	 */
	private static final char OID_SEPERATOR = ':';

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
			// 'emtpy' entries user added but never filled in.
			if (hasText(p.name)) {
				String prefixed = PROPS_PREFIX+p.name+OID_SEPERATOR+(oid++);
				String valueEnabled = (p.isChecked?'1':'0')+p.value;
				conf.setAttribute(prefixed, valueEnabled);
			}
		}
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

	private static boolean hasText(String name) {
		return name!=null && !name.trim().equals("");
	}


}
