package org.springframework.ide.eclipse.beans.ui.wizards.wizards.pages;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;
import org.springframework.ide.eclipse.core.ui.treemodel.IModelItem;
import org.springframework.ide.eclipse.core.ui.treemodel.IModelItemListener;
import org.springframework.ide.eclipse.core.ui.treemodel.ModelItemEvent;
import org.springframework.ide.eclipse.core.ui.treemodel.RootModelItem;

public class PropertiesListContentProvider implements ITreeContentProvider {

	private RootModelItem invisibleRoot;

	private TreeListDialogField treeListDialogField;

	private IModelItemListener listener;
	
	private final String MODEL_ID;

	public PropertiesListContentProvider(TreeListDialogField treeListDialogField,String modelId) {
		this.treeListDialogField = treeListDialogField;
		this.MODEL_ID=modelId;
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void dispose() {
		invisibleRoot.removeListener(listener);
		invisibleRoot = null;
	}

	public Object[] getElements(Object parent) {
		// if parent is the dialog field, get the root element
		if (parent.equals(this.treeListDialogField)) {
			if (invisibleRoot == null) {
				initialize();
			}
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}

	private void initialize() {
		invisibleRoot = RootModelItem.getInstance(MODEL_ID);
		listener = new IModelItemListener() {
			public void changeOccured(ModelItemEvent modelItemEvent) {
				if (modelItemEvent.getEventType() == ModelItemEvent.POST_ADD_CHILD) {
					treeListDialogField.getTreeViewer().refresh();
					treeListDialogField.getTreeViewer().expandAll();
				}
				if (modelItemEvent.getEventType() == ModelItemEvent.POST_REMOVE_CHILD) {
					treeListDialogField.getTreeViewer().refresh();
					treeListDialogField.getTreeViewer().expandAll();
				}
			}
		};
		invisibleRoot.addListener(listener);
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof IModelItem) {
			List children = ((IModelItem) parent).getChildren();
			return children.toArray(new IModelItem[children.size()]);
		}
		return new Object[0];
	}

	public Object getParent(Object child) {
		if (child instanceof IModelItem) {
			return ((IModelItem) child).getParent();
		}
		return null;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof IModelItem)
			return ((IModelItem) parent).hasChildren();
		return false;
	}
}
