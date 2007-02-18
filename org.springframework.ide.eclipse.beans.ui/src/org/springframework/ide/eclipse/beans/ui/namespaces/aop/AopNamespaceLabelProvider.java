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

package org.springframework.ide.eclipse.beans.ui.namespaces.aop;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} in the namespace
 * <code>"http://www.springframework.org/schema/aop"</code>.
 * 
 * @author Torsten Juergeleit
 */
public class AopNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	protected Image getImage(ISourceModelElement element,
			IModelElement context) {
		return AopNamespaceImages.getImage(element, context);
	}
}
