/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.core;

/**
 * Markers related with Spring Web Flow projects.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * 
 *  @author Christian Dupuis
 */
public interface IWebFlowProjectMarker {

    /**
     * Spring Web Flow project problem marker type (value 
     * <code>"org.springframework.ide.eclipse.web.flow.core.problemmarker"</code>).
     * This can be used to recognize those markers in the workspace that flag
     * problems related with Spring Web Flow projects.
     */
    public static final String PROBLEM_MARKER = WebFlowCorePlugin.PLUGIN_ID
            + ".problemmarker";

    /**
     * Error code marker attribute (value <code>"errorCode"</code>).
     */
    public static final String ERROR_CODE = "errorCode";

    /**
     * Error data marker attribute (value <code>"errorData"</code>).
     */
    public static final String ERROR_DATA = "errorData";

    // Codes used for attribute 'ERROR_CODE'
    public static final int ERROR_CODE_NONE = 0;

    public static final int ERROR_CODE_PARSING_FAILED = 1;
}
