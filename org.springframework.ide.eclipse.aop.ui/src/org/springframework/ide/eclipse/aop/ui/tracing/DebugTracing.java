/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.aop.ui.tracing;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.aop.ui.Activator;

public class DebugTracing {

    /**
     * General debug trace for the plug-in enabled through the master trace switch.
     */
    public static boolean DEBUG = false;

    /**
     * Progress information for the compiler
     */
    public static boolean DEBUG_BUILDER_PROGRESS = true;

    /**
     * More detailed trace for compiler task list messages
     */
    public static boolean DEBUG_BUILDER_MESSAGES = true;

    /**
     * More detailed trace for the project builder
     */
    public static boolean DEBUG_BUILDER = true;

    /**
     * More detailed trace for project classpaths
     */
    public static boolean DEBUG_BUILDER_CLASSPATH = true;

    public static String startupInfo() {
        Bundle bundle = Activator.getDefault().getBundle();
        String version = (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
        StringBuffer eventData = new StringBuffer();
        eventData.append("Spring IDE version ");
        eventData.append(version);
        return eventData.toString();
    }
}
