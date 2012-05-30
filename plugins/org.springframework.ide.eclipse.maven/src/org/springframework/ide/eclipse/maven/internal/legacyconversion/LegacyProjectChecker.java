/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.maven.internal.legacyconversion;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;
import org.springframework.ide.eclipse.maven.MavenCorePlugin;


/**
 * Checks for legacy maven projects in the workspace at startup.
 * Must be very careful not to accidentally load the rest of STS if
 * no maven projects are found
 * 
 * @author Andrew Eisenberg
 * @since 2.8.0
 */
public class LegacyProjectChecker implements IStartup, IM2EConstants {
    // set to true during testing mode
    public static boolean NON_BLOCKING = false;

    /**
     * This entry to the checker comes at the startup of the workbench
     */
    public void earlyStartup() {
        MavenCorePlugin.getDefault().getPreferenceStore().setDefault(DONT_AUTO_CHECK, false);
        
        if (shouldPerformCheck()) {
            Job job = new LegacyProjectsJob(false, true);
            job.schedule();
            
            ResourcesPlugin.getWorkspace().addResourceChangeListener(LegacyProjectListener.LISTENER, IResourceChangeEvent.POST_CHANGE);
        }
    }

    /**
     * @return only do check if the new m2eclipse is present and the user has not explicitly disabled the check
     */
    private boolean shouldPerformCheck() {
        return MavenCorePlugin.IS_M2ECLIPSE_PRESENT && ! MavenCorePlugin.getDefault().getPreferenceStore().getBoolean(DONT_AUTO_CHECK);
    }
}
