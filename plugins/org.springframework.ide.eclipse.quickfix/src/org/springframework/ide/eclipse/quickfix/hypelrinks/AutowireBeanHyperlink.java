package org.springframework.ide.eclipse.quickfix.hypelrinks;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

public class AutowireBeanHyperlink implements IHyperlink {

	private final IFile file;

	private final int line;

	private final String beanName;

	private final boolean onlyCandidate;

	public AutowireBeanHyperlink(IFile file, int line, String beanName, boolean onlyCandidate) {
		this.file = file;
		this.line = line;
		this.beanName = beanName;
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

		return "Open Autowired Bean Candidate: \"" + beanName + "\"";
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

}
