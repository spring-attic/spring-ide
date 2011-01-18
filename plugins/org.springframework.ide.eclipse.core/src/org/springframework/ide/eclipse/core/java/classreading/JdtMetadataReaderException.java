/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.classreading;

/**
 * @author Christian Dupuis
 * @since 2.2.5
 */
public class JdtMetadataReaderException extends RuntimeException {

	private static final long serialVersionUID = 536270588069634749L;
	
	public JdtMetadataReaderException(Throwable e) {
		super(e);
	}

	public JdtMetadataReaderException(String message) {
		super(message);
	}

	public JdtMetadataReaderException(String message, Throwable e) {
		super(message, e);
	}


}
