package org.springframework.ide.eclipse.maven.pom;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

public class PomMergeViewerCreator implements IViewerCreator {

	public PomMergeViewerCreator() {
	}

	@Override
	public Viewer createViewer(Composite parent, CompareConfiguration config) {
		return new PomMergeViewer(parent, 0, config);
	}

}
