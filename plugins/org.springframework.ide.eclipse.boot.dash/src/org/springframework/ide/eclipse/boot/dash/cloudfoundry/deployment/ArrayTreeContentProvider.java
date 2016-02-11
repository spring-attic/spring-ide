package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

public class ArrayTreeContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	private static ArrayTreeContentProvider instance;

	public static ArrayTreeContentProvider getInstance() {
		synchronized(ArrayContentProvider.class) {
			if (instance == null) {
				instance = new ArrayTreeContentProvider();
			}
			return instance;
		}
	}

	@Override
	public Object[] getChildren(Object o) {
		return null;
	}

	@Override
	public Object getParent(Object o) {
		return null;
	}

	@Override
	public boolean hasChildren(Object e) {
		return false;
	}

}
