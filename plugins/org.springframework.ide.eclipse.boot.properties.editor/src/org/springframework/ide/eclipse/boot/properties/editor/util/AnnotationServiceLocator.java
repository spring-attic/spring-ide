/*******************************************************************************
 * Copyright (c) 2011 Knowledge Computing Corp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Karl M. Davis (Knowledge Computing Corp.) - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * <p>
 * Provides utility methods useful for discovering APT services.
 * </p>
 * <p>
 * Please note that most of this code was copied from the <code>org.eclipse.jdt.apt.core</code> plugin's
 * <code>org.eclipse.jdt.apt.core.internal.JarFactoryContainer</code> class.
 * </p>
 *
 * @author karldavis
 */
public class AnnotationServiceLocator {

  /**
   * The name of the {@link Class} used to load/create annotation processors in Java 5.
   */
  private static final String JAVA5_FACTORY_NAME = "com.sun.mirror.apt.AnnotationProcessorFactory"; //$NON-NLS-1$

  /**
   * The name of the {@link Class} used to load/create annotation processors in Java 6 and later.
   */
  private static final String JAVA6_FACTORY_NAME = "javax.annotation.processing.Processor"; //$NON-NLS-1$

  /**
   * List of jar file entries within <code>META-INF/services</code> that specify auto-loadable service providers.
   */
  private static final String[] APT_SERVICES = {JAVA5_FACTORY_NAME, JAVA6_FACTORY_NAME};

  /**
   * Given a JAR file, get the names of any auto-loadable Java 5-style or Java 6-style annotation processor
   * implementations provided by the JAR. The information is based on the Sun <a
   * href="https://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider"> Jar Service Provider spec</a>:
   * the jar file contains a META-INF/services directory; that directory contains text files named according to the
   * desired interfaces; and each file contains the names of the classes implementing the specified service. The files
   * may also contain whitespace (which is to be ignored). The '#' character indicates the beginning of a line comment,
   * also to be ignored. Implied but not stated in the spec is that this routine also ignores anything after the first
   * nonwhitespace token on a line.
   *
   * @param jar the <code>.jar</code> {@link File} to inspect for annotation processor services
   * @return the {@link Set} of auto-loadable Java 5-style or Java 6-style annotation processor {@link ServiceEntry}s
   *         provided by the specified JAR, or an empty {@link Set} if no such {@link ServiceEntry}s are found
   */
  public static Set<ServiceEntry> getAptServiceEntries(File jar) throws IOException {
    // Sanity checks:
    if(jar == null)
      throw new IllegalArgumentException(String.format("Null %s.", File.class));
    if(!jar.exists())
      throw new IllegalArgumentException(String.format("Specified file does not exist: %s", jar.getAbsolutePath()));
    if(!jar.canRead())
      throw new IllegalArgumentException(String.format("Specified file not readable: %s", jar.getAbsolutePath()));

    Set<ServiceEntry> serviceEntries = new HashSet<>();
    JarFile jarFile = null;

    try {
      jarFile = new JarFile(jar);

      for(String serviceName : APT_SERVICES) {
        String providerName = "META-INF/services/" + serviceName; //$NON-NLS-1$

        // Get the service provider def file out of the jar.
        JarEntry provider = jarFile.getJarEntry(providerName);
        if(provider == null) {
          continue;
        }

        // Extract classnames from the service provider def file.
        InputStream is = jarFile.getInputStream(provider);
        Set<ServiceEntry> serviceFileEntries = readServiceProvider(serviceName, is);
        serviceEntries.addAll(serviceFileEntries);
      }

      return serviceEntries;
    } finally {
      try {
        if(jarFile != null)
          jarFile.close();
      } catch(IOException ioe) {
      }
    }
  }

  /**
   * Read service classnames from a service provider definition.
   *
   * @param serviceName the name of the service that <code>servicesDeclarationFile</code> contains entries for
   * @param servicesDeclarationFile an {@link InputStream} for the <code>META-INF/services</code> file to load
   *          {@link ServiceEntry}s from
   * @return the {@link Set} of {@link ServiceEntry}s that were found in the specified <code>META-INF/services</code>
   *         file, or an empty {@link Set} if no entries were found in the file
   * @see https://download.oracle.com/javase/1.4.2/docs/guide/sound/programmer_guide/chapter13.html
   */
  private static Set<ServiceEntry> readServiceProvider(String serviceName, InputStream servicesDeclarationFile)
      throws IOException {
    Set<ServiceEntry> serviceEntries = new HashSet<>();
    BufferedReader servicesReader = null;

    try {
      servicesReader = new BufferedReader(new InputStreamReader(servicesDeclarationFile, "UTF-8")); //$NON-NLS-1$
      for(String line = servicesReader.readLine(); line != null; line = servicesReader.readLine()) {
        // hack off any comments
        int iComment = line.indexOf('#');
        if(iComment >= 0) {
          line = line.substring(0, iComment);
        }
        // add the first non-whitespace token to the list
        final String[] tokens = line.split("\\s", 2); //$NON-NLS-1$
        if(tokens[0].length() > 0) {
          ServiceEntry serviceEntry = new ServiceEntry(serviceName, tokens[0]);
          serviceEntries.add(serviceEntry);
        }
      }

      return serviceEntries;
    } finally {
      if(servicesReader != null)
        try {
          servicesReader.close();
        } catch(IOException ioe) {
        }
    }
  }

  /**
   * Represents a single SPI entry.
   */
  public static final class ServiceEntry {

    private final String serviceName;

    private final String serviceProviderClassName;

    /**
     * Constructs a new {@link ServiceEntry} instance.
     *
     * @param serviceName the name of the service that the provider implements
     * @param serviceProviderClassName the {@link Class} name of the service provider represented by this
     *          {@link ServiceEntry}
     */
    public ServiceEntry(String serviceName, String serviceProviderClassName) {
      this.serviceName = serviceName;
      this.serviceProviderClassName = serviceProviderClassName;
    }

    /**
     * @return the name of the service that the provider implements
     */
    public String getServiceName() {
      return this.serviceName;
    }

    /**
     * @return the {@link Class} name of the service provider represented by this {@link ServiceEntry}
     */
    public String getServiceProviderClassName() {
      return this.serviceProviderClassName;
    }
  }
}
