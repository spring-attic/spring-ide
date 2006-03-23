package org.springframework.ide.eclipse.core.ui.treemodel;

import java.util.List;

/**
 * This interface is for a tree Model Item. A Tree Model has parent and childs and methods to handle them. It also has a UID.
 * 
 * @author pagregoire
 */
public interface IModelItem {

    /**
     * This method returns the parent for this model item or null if it doesn't have any parent.
     * 
     * @return another IModelItem implementation
     */
    public IModelItem getParent();

    /**
     * This method returns a list of children model items for this model item.
     * 
     * @return a list of IModelItem implementations.
     */
    public List getChildren();

    /**
     * This method returns a child model item for this model item.
     * 
     * @param UID
     *            the unique ID of the child model item to retrieve
     * @return an IModelItem implementation
     */
    public IModelItem getChild(String UID);

    /**
     * This method tests if this model item has any children model items.
     * 
     * @return <b>true</b> if it has children, <b>false </b> if it doesn't
     */
    public boolean hasChildren();

    /**
     * This method tests if this model item has any children model items.
     * 
     * @param UID
     *            the unique ID of the child model item to retrieve
     * @return <b>true</b> if it has children, <b>false </b> if it doesn't
     */
    public boolean hasChild(String UID);

    /**
     * This method returns the unique ID of this model item. Note that the unique ID generation is not handled by this interface. It is left to the implementor.
     * 
     * @return a String representing the unique ID
     */
    public String getUID();

    /**
     * This method clears all children references from this model item.
     */
    public void clearChildren();

    /**
     * This method removes a given children reference from this model item.
     * 
     * @param UID
     *            the Unique id of the reference to remove.
     */
    public void removeChild(String UID);

    /**
     * This method adds a child model item to this model item
     * 
     * @param modelItem
     *            an IModelItem implementation
     */
    public void addChild(IModelItem modelItem);

    /**
     * This method adds this child model item as a child model item to another model item. Calling this method on a root model item will throw a ModelException
     * 
     * @param modelItem
     *            an IModelItem implementation
     */
    public void setParent(IModelItem modelItem);

    public void accept(IModelItemVisitor visitor);
}
