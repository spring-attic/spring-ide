/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.springframework.ide.eclipse.boot.properties.editor.util.HtmlUtil;

@SuppressWarnings("restriction")
public abstract class HoverInfo extends BrowserInformationControlInput {

	public HoverInfo() {
		super(null);
	}

	@Override
	public Object getInputElement() {
		return this;
	}

	@Override
	public String getInputName() {
		return "";
	}

	public abstract String getHtml();

	/**
	 * IJavaElements associated with the hover target. Used by 'open declaration'
	 * action.
	 */
	public List<IJavaElement> getJavaElements() {
		return Collections.emptyList();
	}

	public static HoverInfo withText(final String plainText) {
		return new HoverInfo() {

			@Override
			public String getHtml() {
				return HtmlUtil.text2html(plainText);
			}

		};
	}
}
