/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.batch.ui.editor.hyperlink.osgi;

import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.springframework.ide.eclipse.beans.ui.editor.hyperlink.NamespaceHyperlinkDetectorSupport;
import org.springframework.ide.eclipse.beans.ui.editor.namespaces.INamespaceHyperlinkDetector;

/**
 * {@link INamespaceHyperlinkDetector} implementation responsible for the <code>batch:*</code>
 * namespace.
 * @author Christian Dupuis
 * @since 2.2.2
 */
public class BatchHyperlinkDetector extends NamespaceHyperlinkDetectorSupport implements
		IHyperlinkDetector {
	
	// Intentionally empty for now
	
}
