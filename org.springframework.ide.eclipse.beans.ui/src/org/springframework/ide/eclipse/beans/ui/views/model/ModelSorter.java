/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views.model;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class ModelSorter extends ViewerSorter {

	// Categories
	public static final int PROJECT = 0;
	public static final int CONFIG_SET = 1;
	public static final int CONFIG = 2;
	public static final int BEAN = 3;
	public static final int CONSTRUCTOR = 4;
	public static final int PROPERTY = 5;

	private boolean sortBeans;

	public ModelSorter(boolean sortBeans) {
		this.sortBeans = sortBeans;
	}

	public int category(Object element) {
	    int category;
	    if (element instanceof ProjectNode) {
	        category = PROJECT;
	    } else if (element instanceof ConfigSetNode) {
	        category = CONFIG_SET;
	    } else if (element instanceof ConfigNode) {
	        category = CONFIG;
	    } else if (element instanceof BeanNode) {
	        category = BEAN;
	    } else if (element instanceof ConstructorArgumentNode) {
	        category = CONSTRUCTOR;
	    } else {
	        category = PROPERTY;
	    }
	    return category;
	}
    
 	public int compare(Viewer viewer, Object obj1, Object obj2) {
 	    int compare = 0;
 	    if (viewer instanceof ContentViewer) {
			int cat1 = category(obj1);
			int cat2 = category(obj2);
			if (cat1 == cat2) {
				ILabelProvider lprov = (ILabelProvider)
									((ContentViewer) viewer).getLabelProvider();
				if (cat1 == CONFIG) {
					String text1 = lprov.getText(obj1);
					String text2 = lprov.getText(obj2);
	
					// Configs are sorted differently - configs in
					// sub-directories first, configs in project's root last
					int pos1 = text1.indexOf('/');
					int pos2 = text2.indexOf('/');
					if (pos1 == -1 && pos2 == -1) {
						compare = collator.compare(text1, text2);
					} else {
						if (pos1 == -1) {
							compare = 1;
						} else if (pos2 == -1) {
							compare = -1;
						} else {
							compare = collator.compare(text1, text2);
						}
					}
				} else {
					if (cat1 == PROJECT || cat1 == CONFIG_SET || sortBeans) {
						compare = collator.compare(lprov.getText(obj1),
												   lprov.getText(obj2));
					}
				}
			}
 	    } else {
 	    		compare = super.compare(viewer, obj1, obj2);
 	    }
		return compare;
	}
}
