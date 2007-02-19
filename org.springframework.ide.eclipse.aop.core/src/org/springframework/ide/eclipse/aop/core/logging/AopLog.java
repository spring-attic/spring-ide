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
package org.springframework.ide.eclipse.aop.core.logging;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal logging - if a logger hasn't been set, dump to sdout
 */
public class AopLog {

    public static final int DEFAULT = 0;

    public static final int BUILDER = 1;

    public static final int BUILDER_CLASSPATH = 2;

    public static final int BUILDER_PROGRESS = 3;

    public static final int BUILDER_MESSAGES = 4;

    private static IAopLogger logger;

    // support for logging the start and end of activies
    private static Map<String, Long> timers = new HashMap<String, Long>();

    public static void log(String msg) {
        log(DEFAULT, msg);
    }

    public static void log(int category, String msg) {
        if (logger != null) {
            logger.log(category, msg);
        }
        else {
            System.out.println(msg);
        }
    }

    public static void logStart(String event) {
        Long now = new Long(System.currentTimeMillis());
        timers.put(event, now);
    }

    public static void logEnd(int category, String event) {
        logEnd(category, event, null);
    }

    public static void logEnd(int category, String event, String optional_msg) {
        Long then = (Long) timers.get(event);
        if (then != null) {
            long now = System.currentTimeMillis();
            long elapsed = now - then.longValue();
            if ((optional_msg != null) && (optional_msg.length() > 0)) {
                log(category,
                        "Timer event: " + elapsed + "ms: " + event + " (" + optional_msg + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
            }
            else {
                log(category, "Timer event: " + elapsed + "ms: " + event); //$NON-NLS-1$ //$NON-NLS-2$
            }
            timers.remove(event);
        }
    }

    public static void setLogger(IAopLogger l) {
        logger = l;
    }
}
