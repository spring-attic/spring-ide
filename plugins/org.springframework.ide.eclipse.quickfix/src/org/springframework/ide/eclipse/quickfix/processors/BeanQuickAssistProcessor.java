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
package org.springframework.ide.eclipse.quickfix.processors;

import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.jface.text.quickassist.IQuickAssistProcessor;
import org.eclipse.jface.text.source.Annotation;

/**
 * Parent abstract quick assist processor for beans XML editor.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class BeanQuickAssistProcessor implements IQuickAssistProcessor {

	protected int offset, length;

	protected String text;

	protected boolean missingEndQuote;

	public BeanQuickAssistProcessor(int offset, int length, String text, boolean missingEndQuote) {
		this.offset = offset;
		this.length = length;
		this.text = text;
		this.missingEndQuote = missingEndQuote;
	}

	public boolean canAssist(IQuickAssistInvocationContext invocationContext) {
		return false;
	}

	public boolean canFix(Annotation annotation) {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BeanQuickAssistProcessor) {
			BeanQuickAssistProcessor processor = (BeanQuickAssistProcessor) obj;
			return processor.offset == offset && processor.text.equals(text);
		}
		return super.equals(obj);
	}

	public String getErrorMessage() {
		return null;
	}

	@Override
	public int hashCode() {
		return ("" + offset + text).hashCode();
	}

}
