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

package org.springframework.ide.eclipse.beans.ui.model;

import org.springframework.ide.eclipse.ui.SpringUIImageFlags;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface BeansModelImageFlags extends SpringUIImageFlags {

    public static final int FLAG_EXTERNAL = 1 << 3;
    public static final int FLAG_CHILD = 1 << 4;
    public static final int FLAG_FACTORY = 1 << 5;
    public static final int FLAG_ABSTRACT = 1 << 6;
    public static final int FLAG_PROTOTYPE = 1 << 7;
    public static final int FLAG_LAZY_INIT = 1 << 8;
    public static final int FLAG_ANNOTATION = 1 << 9;
}
