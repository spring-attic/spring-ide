/*
 * Copyright 2002-2006 the original author or authors.
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
package org.springframework.ide.eclipse.aop.ui.navigator.util;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

/**
 */
public class BeansAopNavigatorSorter extends ViewerSorter {

    public int category(Object element) {
		if (element instanceof IAopReference) {
			return 1;
		}
		return 0;
    }

    public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof IAopReference
				|| e2 instanceof IAopReference) {
			IAopReference ref1 = (IAopReference) e1;
			IAopReference ref2 = (IAopReference) e2;
            if (ref1.getResource().equals(ref2.getResource())) {
                int l1 = ref1.getDefinition().getAspectLineNumber();
                int l2 = ref2.getDefinition().getAspectLineNumber();
                if (l1 < l2) {
                    return -1;
                }
                else if (l1 == l2) {
                    return 0;
                }
                else if (l1 > l2) {
                    return 1;
                }
            }
            else {
                super.compare(viewer, e1, e2);
            }
		}
		return super.compare(viewer, e1, e2);
	}
}
