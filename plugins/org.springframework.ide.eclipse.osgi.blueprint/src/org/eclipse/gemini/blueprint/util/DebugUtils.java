/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *   Oracle Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Utility class used for debugging exceptions in OSGi environment, such as
 * class loading errors.
 * 
 * The main entry point is
 * {@link #debugClassLoadingThrowable(Throwable, Bundle, Class[])} which will
 * try to determine the cause by trying to load the given interfaces using the
 * given bundle.
 * 
 * <p/> The debugging process can be potentially expensive.
 * 
 * @author Costin Leau
 * @author Andy Piper
 */
public abstract class DebugUtils {

	private static final String EQUALS = "=";
	private static final String DOUBLE_QUOTE = "\"";
	private static final String SEMI_COLON = ";";
	private static final String COMMA = ",";
	/** use degradable logger */
	private static final Log log = LogUtils.createLogger(DebugUtils.class);

	// currently not used but might be in the future
	private static final String PACKAGE_REGEX = "([^;,]+(?:;?\\w+:?=((\"[^\"]+\")|([^,]+)))*)+";
	private static final Pattern PACKAGE_PATTERN = Pattern.compile(PACKAGE_REGEX);


	/**
	 * Tries to debug the cause of the {@link Throwable}s that can appear when
	 * loading classes in OSGi environments (for example when creating proxies).
	 * 
	 * <p/> This method will try to determine the class that caused the problem
	 * and to search for it in the given bundle or through the classloaders of
	 * the given classes.
	 * 
	 * It will look at the classes are visible by the given bundle on debug
	 * level and do a bundle discovery process on trace level.
	 * 
	 * The method accepts also an array of classes which will be used for
	 * loading the 'problematic' class that caused the exception on debug level.
	 * 
	 * @param loadingThrowable class loading {@link Throwable} (such as
	 * {@link NoClassDefFoundError} or {@link ClassNotFoundException})
	 * @param bundle bundle used for loading the classes
	 * @param classes (optional) array of classes that will be used for loading
	 * the problematic class
	 */
	public static void debugClassLoadingThrowable(Throwable loadingThrowable, Bundle bundle, Class<?>[] classes) {

		String className = null;
		// NoClassDefFoundError
		if (loadingThrowable instanceof NoClassDefFoundError) {
			className = loadingThrowable.getMessage();
			if (className != null)
				className = className.replace('/', '.');
		}
		// ClassNotFound
		else if (loadingThrowable instanceof ClassNotFoundException) {
			className = loadingThrowable.getMessage();

			if (className != null)
				className = className.replace('/', '.');
		}

		if (className != null) {

			debugClassLoading(bundle, className, null);

			if (!ObjectUtils.isEmpty(classes) && log.isDebugEnabled()) {
				StringBuilder message = new StringBuilder();

				// Check out all the classes.
				for (int i = 0; i < classes.length; i++) {
					ClassLoader cl = classes[i].getClassLoader();
					String cansee = "cannot";
					if (ClassUtils.isPresent(className, cl))
						cansee = "can";
					message.append(classes[i] + " is loaded by " + cl + " which " + cansee + " see " + className);
				}
				log.debug(message);
			}
		}
	}

	/**
	 * Tries (through a best-guess attempt) to figure out why a given class
	 * could not be found. This method will search the given bundle and its
	 * classpath to determine the reason for which the class cannot be loaded.
	 * 
	 * <p/> This method tries to be effective especially when the dealing with
	 * {@link NoClassDefFoundError} caused by failure of loading transitive
	 * classes (such as getting a NCDFE when loading <code>foo.A</code>
	 * because <code>bar.B</code> cannot be found).
	 * 
	 * @param bundle the bundle to search for (and which should do the loading)
	 * @param className the name of the class that failed to be loaded in dot
	 * format (i.e. java.lang.Thread)
	 * @param rootClassName the name of the class that triggered the loading
	 * (i.e. java.lang.Runnable)
	 */
	public static void debugClassLoading(Bundle bundle, String className, String rootClassName) {
		boolean trace = log.isTraceEnabled();
		if (!trace)
			return;

		Dictionary dict = bundle.getHeaders();
		String bname = dict.get(Constants.BUNDLE_NAME) + "(" + dict.get(Constants.BUNDLE_SYMBOLICNAME) + ")";
		if (trace)
			log.trace("Could not find class [" + className + "] required by [" + bname + "] scanning available bundles");

		BundleContext context = OsgiBundleUtils.getBundleContext(bundle);
		int pkgIndex = className.lastIndexOf('.');
		// Reject global packages
		if (pkgIndex < 0) {
			if (trace)
				log.trace("Class is not in a package, its unlikely that this will work");
			return;
		}
		
		String packageName = className.substring(0, pkgIndex);

		Version iversion = hasImport(bundle, packageName);
		if (iversion != null && context != null) {
			if (trace)
				log.trace("Class is correctly imported as version [" + iversion + "], checking providing bundles");
			Bundle[] bundles = context.getBundles();
			for (int i = 0; i < bundles.length; i++) {
				if (bundles[i].getBundleId() != bundle.getBundleId()) {
					Version exported = checkBundleForClass(bundles[i], className, iversion);
					// Everything looks ok, but is the root bundle importing the
					// dependent class also?
					if (exported != null && exported.equals(iversion) && rootClassName != null) {
						for (int j = 0; j < bundles.length; j++) {
							Version rootexport = hasExport(bundles[j], rootClassName.substring(0,
								rootClassName.lastIndexOf('.')));
							if (rootexport != null) {
								// TODO -- this is very rough, check the bundle
								// classpath also.
								Version rootimport = hasImport(bundles[j], packageName);
								if (rootimport == null || !rootimport.equals(iversion)) {
									if (trace)
										log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundles[j])
												+ "] exports [" + rootClassName + "] as version [" + rootexport
												+ "] but does not import dependent package [" + packageName
												+ "] at version [" + iversion + "]");
								}
							}
						}
					}
				}
			}
		}
		if (hasExport(bundle, packageName) != null) {
			if (trace)
				log.trace("Class is exported, checking this bundle");
			checkBundleForClass(bundle, className, iversion);
		}
	}

	private static Version checkBundleForClass(Bundle bundle, String name, Version iversion) {
		String packageName = name.substring(0, name.lastIndexOf('.'));
		Version hasExport = hasExport(bundle, packageName);

		// log.info("Examining Bundle [" + bundle.getBundleId() + ": " + bname +
		// "]");
		// Check for version matching
		if (hasExport != null && !hasExport.equals(iversion)) {
			log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] exports [" + packageName
					+ "] as version [" + hasExport + "] but version [" + iversion + "] was required");
			return hasExport;
		}
		// Do more detailed checks
		String cname = name.substring(packageName.length() + 1) + ".class";
		Enumeration e = bundle.findEntries("/" + packageName.replace('.', '/'), cname, false);
		if (e == null) {
			if (hasExport != null) {
				URL url = checkBundleJarsForClass(bundle, name);
				if (url != null) {
					log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] contains [" + cname
							+ "] in embedded jar [" + url.toString() + "] but exports the package");
				}
				else {
					log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] does not contain ["
							+ cname + "] but exports the package");
				}
			}

			String root = "/";
			String fileName = packageName;
			if (packageName.lastIndexOf(".") >= 0) {
				root = root + packageName.substring(0, packageName.lastIndexOf(".")).replace('.', '/');
				fileName = packageName.substring(packageName.lastIndexOf(".") + 1).replace('.', '/');
			}
			Enumeration pe = bundle.findEntries(root, fileName, false);
			if (pe != null) {
				if (hasExport != null) {
					log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] contains package ["
							+ packageName + "] and exports it");
				}
				else {
					log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] contains package ["
							+ packageName + "] but does not export it");
				}

			}
		}
		// Found the resource, check that it is exported.
		else {
			if (hasExport != null) {
				log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] contains resource [" + cname
						+ "] and it is correctly exported as version [" + hasExport + "]");
				Class<?> c = null;
				try {
					c = bundle.loadClass(name);
				}
				catch (ClassNotFoundException e1) {
					// Ignored
				}
				log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] loadClass [" + cname
						+ "] returns [" + c + "]");
			}
			else {
				log.trace("Bundle [" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "] contains resource [" + cname
						+ "] but its package is not exported");
			}
		}
		return hasExport;
	}

	private static URL checkBundleJarsForClass(Bundle bundle, String name) {
		String cname = name.replace('.', '/') + ".class";
		for (Enumeration e = bundle.findEntries("/", "*.jar", true); e != null && e.hasMoreElements();) {
			URL url = (URL) e.nextElement();
			JarInputStream jin = null;
			try {
				jin = new JarInputStream(url.openStream());
				// Copy entries from the real jar to our virtual jar
				for (JarEntry ze = jin.getNextJarEntry(); ze != null; ze = jin.getNextJarEntry()) {
					if (ze.getName().equals(cname)) {
						jin.close();
						return url;
					}
				}
			}
			catch (IOException e1) {
				log.trace("Skipped " + url.toString() + ": " + e1.getMessage());
			}

			finally {
				if (jin != null) {
					try {
						jin.close();
					}
					catch (Exception ex) {
						// ignore it
					}
				}
			}

		}
		return null;
	}

	/**
	 * Get the version of a package import from a bundle.
	 * 
	 * @param bundle
	 * @param packageName
	 * @return
	 */
	private static Version hasImport(Bundle bundle, String packageName) {
		Dictionary dict = bundle.getHeaders();
		// Check imports
		String imports = (String) dict.get(Constants.IMPORT_PACKAGE);
		Version v = getVersion(imports, packageName);
		if (v != null) {
			return v;
		}
		// Check for dynamic imports
		String dynimports = (String) dict.get(Constants.DYNAMICIMPORT_PACKAGE);
		if (dynimports != null) {
			for (StringTokenizer strok = new StringTokenizer(dynimports, COMMA); strok.hasMoreTokens();) {
				StringTokenizer parts = new StringTokenizer(strok.nextToken(), SEMI_COLON);
				String pkg = parts.nextToken().trim();
				if (pkg.endsWith(".*") && packageName.startsWith(pkg.substring(0, pkg.length() - 2)) || pkg.equals("*")) {
					Version version = Version.emptyVersion;
					for (; parts.hasMoreTokens();) {
						String modifier = parts.nextToken().trim();
						if (modifier.startsWith("version")) {
							version = Version.parseVersion(modifier.substring(modifier.indexOf(EQUALS) + 1).trim());
						}
					}
					return version;
				}
			}
		}
		return null;
	}

	private static Version hasExport(Bundle bundle, String packageName) {
		Dictionary dict = bundle.getHeaders();
		return getVersion((String) dict.get(Constants.EXPORT_PACKAGE), packageName);
	}

	/**
	 * Get the version of a package name.
	 * 
	 * @param stmt
	 * @param packageName
	 * @return
	 */
	private static Version getVersion(String stmt, String packageName) {
		if (stmt != null) {
			String[] pkgs = splitIntoPackages(stmt);

			for (int packageIndex = 0; packageIndex < pkgs.length; packageIndex++) {
				String pkgToken = pkgs[packageIndex].trim();
				String pkg = null;
				Version version = null;
				int firstDirectiveIndex = pkgToken.indexOf(SEMI_COLON);
				if (firstDirectiveIndex > -1) {
					pkg = pkgToken.substring(0, firstDirectiveIndex);
				}
				else {
					pkg = pkgToken;
					version = Version.emptyVersion;
				}

				// check for version only if we have a match
				if (pkg.equals(packageName)) {
					// no version determined, find one
					if (version == null) {
						String[] directiveTokens = pkgToken.substring(firstDirectiveIndex + 1).split(SEMI_COLON);
						for (int directiveTokenIndex = 0; directiveTokenIndex < directiveTokens.length; directiveTokenIndex++) {
							String directive = directiveTokens[directiveTokenIndex].trim();
							// found it
							if (directive.startsWith(Constants.VERSION_ATTRIBUTE)) {
								String value = directive.substring(directive.indexOf(EQUALS) + 1).trim();

								boolean lowEqualTo = value.startsWith("\"[");
								boolean lowGreaterThen = value.startsWith("\"(");
								if (lowEqualTo || lowGreaterThen) {
									boolean highEqualTo = value.endsWith("]\"");
									boolean highLessThen = value.endsWith(")\"");

									// remove brackets
									value = value.substring(2, value.length() - 2);
									int commaIndex = value.indexOf(COMMA);

									// TODO: currently, only the left side is considered
									Version left = Version.parseVersion(value.substring(0, commaIndex));
									Version right = Version.parseVersion(value.substring(commaIndex + 1));

									return left;
								}

								// check quotes
								if (value.startsWith("\"")) {
									return Version.parseVersion(value.substring(1, value.length() - 1));
								}
								return Version.parseVersion(value);
							}
						}
						if (version == null) {
							version = Version.emptyVersion;
						}
					}
					return version;
				}
			}
		}
		return null;
	}

	private static String[] splitIntoPackages(String stmt) {
		// spit the statement into packages but consider "
		List pkgs = new ArrayList(2);

		StringBuilder pkg = new StringBuilder();
		boolean ignoreComma = false;
		for (int stringIndex = 0; stringIndex < stmt.length(); stringIndex++) {
			char currentChar = stmt.charAt(stringIndex);
			if (currentChar == ',') {
				if (ignoreComma) {
					pkg.append(currentChar);
				}
				else {
					pkgs.add(pkg.toString());
					pkg = new StringBuilder();
					ignoreComma = false;
				}
			}
			else {
				if (currentChar == '\"') {
					ignoreComma = !ignoreComma;
				}
				pkg.append(currentChar);
			}
		}
		pkgs.add(pkg.toString());
		return (String[]) pkgs.toArray(new String[pkgs.size()]);
	}
}
