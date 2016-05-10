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

import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.ide.eclipse.editor.support.hover.HoverInfo;
import org.springframework.ide.eclipse.editor.support.util.HtmlBuffer;

public class ValueHintHoverInfo extends HoverInfo {

	private ValueHint hint;

	public ValueHintHoverInfo(ValueHint hint) {
		this.hint = hint;
	}

	@Override
	protected String renderAsHtml() {
		String description = getDescription();
		if (description!=null) {
			HtmlBuffer html = new HtmlBuffer();
			html.bold(""+hint.getValue());
			html.p(description);
			return html.toString();
		}
		return null;
	}

	private String getDescription() {
		String d = hint.getDescription();
		if (StringUtil.hasText(d)) {
			return d;
		}
		d = hint.getShortDescription();
		if (StringUtil.hasText(d)) {
			return d;
		}
		return null;
	}

}
