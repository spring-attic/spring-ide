package org.springframework.ide.eclipse.core.ui.treemodel;

/**
 * This interface describes a visitor for the model.
 * 
 * @author pagregoire
 */
public interface IModelItemVisitor {
    /**
     * Visits the given item element.
     * 
     * @param element
     *            the model element to visit
     * @return <code>true</code> if the elements's members should be visited; <code>false</code> if they should be skipped
     */
    public boolean visit(IModelItem modelItem);
}