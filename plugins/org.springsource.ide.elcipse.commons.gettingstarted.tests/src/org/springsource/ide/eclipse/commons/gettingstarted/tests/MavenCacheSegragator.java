/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.gettingstarted.tests;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.embedder.IMavenConfiguration;

/**
 * Utility to change the local maven repo used by m2e to something
 * temporary (actually, a location inside the plugin state location of the
 * test bundle, so this is 'temporary' assuming every test build will
 * run tests in a new Eclipse runtime workbench.
 * 
 * The purpose of this is to avoid junk already in the local .m2 directory 
 * on the build server to mess with the tests (and vice-versa, test-junk
 * to interfere with builds.
 * 
 * @author Kris De Volder
 */
public class MavenCacheSegragator {
	
	/**
	 * If this file exists in the .m2 cache folder then leave it be. Otherwise consider it
	 * invalid and delete it.
	 */
	public static final String STAMP = "STS_1.txt";
	
	private static boolean inited = false;
	
	public static void init() throws Exception {
		if (!inited) {
			File pluginStateLoc = GettingStartedTestActivator.getDefault().getStateLocation().toFile();
			File settings = new File(pluginStateLoc, "settings.xml");
			PrintWriter out = new PrintWriter(new FileWriter(settings));
			try {
				File localRepoLocation = new File(pluginStateLoc, "m2-repo");
				out.println("<settings>\n" + 
						"  <localRepository>"+ localRepoLocation +"</localRepository>\n" + 
						"</settings>"); 
				System.out.println("Telling m2e to put local maven cache at:");
				System.out.println(localRepoLocation);
			} finally {
				out.close();
			}
			
			
			IMavenConfiguration mavenConf = MavenPlugin.getMavenConfiguration();
			mavenConf.setUserSettingsFile(settings.getAbsolutePath());
			inited = true;
		}
	}

}
