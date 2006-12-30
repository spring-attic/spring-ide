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

package org.springframework.ide.eclipse.beans.ui.namespaces.tx;

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.namespaces.beans.BeansNamespaceImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class provides images for the beans core model's
 * {@link ISourceModelElement elements} in the namespace
 * <code>"http://www.springframework.org/schema/tx"</code>.
 * 
 * @author Torsten Juergeleit
 */
public final class TxNamespaceImages {

	public static Image getImage(ISourceModelElement element) {
		return getImage(element, null);
	}

	public static Image getImage(ISourceModelElement element,
			IModelElement context) {
		if (element instanceof IBean) {
//			return getImage(BeansUIImages.getImage(
//					BeansUIImages.IMG_OBJS_BEAN), element, context);
		}
		return BeansNamespaceImages.getImage(element, context);
	}
}
