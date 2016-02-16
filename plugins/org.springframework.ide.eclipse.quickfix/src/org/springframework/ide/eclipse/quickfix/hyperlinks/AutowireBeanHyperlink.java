/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.hyperlinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Terry Denney
 * @since 3.3.0
 */
public class AutowireBeanHyperlink implements IHyperlink {

	private final IFile file;

	private final int line;

	private final String beanName;

	private boolean onlyCandidate;

	private boolean showFileName;

	public AutowireBeanHyperlink(IFile file, int line, String beanName) {
		this.file = file;
		this.line = line;
		this.beanName = beanName;
		this.onlyCandidate = false;
		this.showFileName = false;
	}

	public IFile getFile() {
		return file;
	}

	public int getLine() {
		return line;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setIsOnlyCandidate(boolean onlyCandidate) {
		this.onlyCandidate = onlyCandidate;
	}

	public void open() {
		SpringUIUtils.openInEditor(file, line);
	}

	public String getTypeLabel() {
		return null;
	}

	public String getHyperlinkText() {
		if (onlyCandidate) {
			return "Open Autowired Bean";
		}

		if (!showFileName) {
			return "Open Autowired Bean Candidate: \"" + beanName + "\"";
		}

		return "Open Autowired Bean Candidate: \"" + beanName + "\"" + " in " + file.getName();
	}

	public IRegion getHyperlinkRegion() {
		return new IRegion() {

			public int getOffset() {
				// TODO Auto-generated method stub
				return 0;
			}

			public int getLength() {
				// TODO Auto-generated method stub
				return 0;
			}
		};
	}

	public void setShowFileName(boolean showFileName) {
		this.showFileName = showFileName;
	}

}
