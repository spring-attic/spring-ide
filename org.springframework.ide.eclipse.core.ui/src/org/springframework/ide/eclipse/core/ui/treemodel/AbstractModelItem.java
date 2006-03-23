package org.springframework.ide.eclipse.core.ui.treemodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is an abstract definition of a model item. It should be implemented.
 * 
 * @author pagregoire
 */
public abstract class AbstractModelItem implements IModelItem, Comparable {
    /**
     * The children of the model item.
     */
    protected Map children = new TreeMap();

    /**
     * The parent of the model item.
     */
    protected IModelItem parent;

    /**
     * This method propagates a given event to the parent model item or triggers the listeners if the parent element is a RootModelItem.
     * 
     * @param modelItemEvent
     *            the event to propagate
     * @see RootModelItem#triggerListeners(ModelItemEvent)
     */
    protected void propagateEvent(ModelItemEvent modelItemEvent) {
        if (this.parent != null) {
            if (this.parent instanceof AbstractModelItem) {
                ((AbstractModelItem) parent).propagateEvent(modelItemEvent);
            } else if (this.parent instanceof RootModelItem) {
                ((RootModelItem) parent).triggerListeners(modelItemEvent);
            }
        }
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#addChild(ec.ep.dit.isp.foundry.model.IModelItem)
     */
    public void addChild(IModelItem child) {
        propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.PRE_ADD_CHILD));
        children.put(child.getUID(), child);
        if (child instanceof AbstractModelItem) {
            if (child.getParent() == null) {
                ((AbstractModelItem) child).parent = this;
            }
        }
        propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.POST_ADD_CHILD));
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#setParent(ec.ep.dit.isp.foundry.model.IModelItem)
     */
    public void setParent(IModelItem parent) {
        if (parent instanceof AbstractModelItem) {
            ((AbstractModelItem) parent).addChild(this);
        }
        this.parent = parent;
        propagateEvent(new ModelItemEvent(this, null, ModelItemEvent.ITEM_PROPERTY_CHANGED));
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#clearChildren()
     */
    public void clearChildren() {
        for (Iterator it = children.keySet().iterator(); it.hasNext();) {
            removeChild((String) it.next());
        }
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#removeChild(java.lang.String)
     */
    public void removeChild(String UID) {
        IModelItem child = (IModelItem) children.get(UID);
        propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.PRE_REMOVE_CHILD));
        children.remove(UID);
        propagateEvent(new ModelItemEvent(this, child, ModelItemEvent.POST_REMOVE_CHILD));
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getChildren()
     */
    public List getChildren() {
        return new ArrayList(children.values());
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#hasChildren()
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#hasChild(java.lang.String)
     */
    public boolean hasChild(String UID) {
        return children.containsKey(UID);
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getChild(java.lang.String)
     */
    public IModelItem getChild(String UID) {
        return (IModelItem) children.get(UID);
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getParent()
     */
    public IModelItem getParent() {
        return parent;
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getUID()
     */
    public abstract String getUID();

    /**
     * @return
     */
    public abstract StringBuffer toStringBufferDescription();

    /**
     * This is an utility method that allows a simple tabbed display of the toString() representation of the objects' tree.
     * 
     * @param level
     *            an integer representing the number of tabulations to display before the current element.
     * @return a StringBuffer containing a representation of this model item and of its children.
     */
    protected StringBuffer toStringBufferChildren(int level) {
        StringBuffer result = new StringBuffer();
        result.append(toStringBufferDescription());
        for (Iterator it = children.values().iterator(); it.hasNext();) {
            result.append("\n");
            for (int i = 0; i < level; i++) {
                result.append("\t");
            }
            result.append(((AbstractModelItem) it.next()).toStringBufferChildren(level + 1));
        }
        return result;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        int result = 0;
        if (o instanceof AbstractModelItem) {
            result = getUID().compareTo(((AbstractModelItem) o).getUID());
        } else {
            result = 1;
        }
        return result;
    }

    public void accept(IModelItemVisitor visitor) {
        if (visitor.visit(this)) {
            for (Iterator it = getChildren().iterator(); it.hasNext();) {
                ((IModelItem) it.next()).accept(visitor);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append("[\n");
        result.append(toStringBufferChildren(1));
        result.append("\n]");
        return result.toString();
    }
}
