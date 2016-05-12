/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.completions;

import org.springframework.ide.eclipse.boot.properties.editor.metadata.StsValueHint;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;

public class ValueHintHoverInfo extends HoverInfo {

	private StsValueHint hint;

	public ValueHintHoverInfo(StsValueHint hint) {
		this.hint = hint;
	}

	@Override
	protected String renderAsHtml() {
		HtmlBuffer html = new HtmlBuffer();
		html.bold(""+hint.getValue());
		html.raw("<p>");
		hint.getDescription().render(html);
		html.raw("</p>");
		return html.toString();
	}

}
