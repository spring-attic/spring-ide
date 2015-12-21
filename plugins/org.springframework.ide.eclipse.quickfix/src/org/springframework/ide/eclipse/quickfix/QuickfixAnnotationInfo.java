/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix;

import org.eclipse.wst.sse.ui.internal.reconcile.validator.AnnotationInfo;
import org.eclipse.wst.validation.internal.provisional.core.IMessage;

/**
 * Annotation info for beans editor quick fix
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class QuickfixAnnotationInfo extends AnnotationInfo {

	public QuickfixAnnotationInfo(IMessage message) {
		super(message);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof QuickfixAnnotationInfo) {
			QuickfixAnnotationInfo info = (QuickfixAnnotationInfo) obj;
			return info.getMessage().equals(getMessage());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return getMessage().hashCode();
	}

}
