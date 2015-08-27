package org.springframework.ide.eclipse.boot.properties.editor.ui;

import org.eclipse.core.resources.IContainer;

public interface UserInteractions {
	IContainer chooseOne(String title, String message, IContainer[] options);
}
