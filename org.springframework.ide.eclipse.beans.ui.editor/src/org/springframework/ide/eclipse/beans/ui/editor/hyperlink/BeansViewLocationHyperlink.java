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

package org.springframework.ide.eclipse.beans.ui.editor.hyperlink;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springframework.ide.eclipse.beans.ui.views.BeansViewLocation;

/**
 * This implementation of <code>IHyperlink</code> represents a link to a node
 * within the Spring beans view.
 * @author Torsten Juergeleit
 */
public class BeansViewLocationHyperlink implements IHyperlink {

	private final IRegion region;
	private BeansViewLocation location;

	public BeansViewLocationHyperlink(IRegion region, BeansViewLocation location) {
		this.region = region;
		this.location = location;
	}

	public IRegion getHyperlinkRegion() {
		return this.region;
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		return null;
	}

	public void open() {
		location.show();
	}
}
