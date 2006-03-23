package org.springframework.ide.eclipse.core.ui.treemodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class defines the RootModelItem singleton registry.
 * 
 * @author pagregoire
 */
public class RootModelItem implements IModelItem {

    /**
     * the default UID for the RootModelItem
     */
    public static final String UID = "<root>";

    /**
     * the instances registry of RootModelItem singleton
     */
    private static Map instances = new HashMap();

    /**
     * the listeners registered in this RootModelItem
     */
    private List listeners = new ArrayList();

    /**
     * the flag toggling on and off the listeners.
     */
    private boolean listenersToggled = true;

    /**
     * the childs of this RootModelItem
     */
    private Map childs = new HashMap();

    /**
     * The default private constructor.
     */
    private RootModelItem() {
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#setParent(ec.ep.dit.isp.foundry.model.IModelItem)
     */
    public void setParent(IModelItem modelItem) {
        throw new ModelException("Impossible to set a parent to a Root Item");
    }

    /**
     * This method tests if the RootModelItem instance exists.
     * 
     * @return true if it exists, false otherwise.
     */
    public static boolean isInstanciated(String rootModelItemUID) {
        return (instances.containsKey(rootModelItemUID));
    }

    /**
     * This method returns the RootModelItem instance, creating it if it doesn't already exist.
     * 
     * @return the RootModelItem instance
     */
    public static RootModelItem getInstance(String rootModelItemUID) {
        if (!instances.containsKey(rootModelItemUID)) {
            instances.put(rootModelItemUID, new RootModelItem());
        }
        return (RootModelItem) instances.get(rootModelItemUID);
    }

    /**
     * This method registers a listener.
     * 
     * @param listener
     *            an implementation of IModelListener
     */
    public void addListener(IModelItemListener listener) {
        listeners.add(listener);
    }

    /**
     * This method de-registers a listener.
     * 
     * @param listener
     *            an implementation of IModelListener
     */
    public void removeListener(IModelItemListener listener) {
        listeners.remove(listener);
    }

    /**
     * This method toggle the listeners' triggering ON
     */
    public void toggleListenersOn() {
        listenersToggled = true;
    }

    /**
     * This method toggle the listeners' triggering OFF
     */
    public void toggleListenersOff() {
        listenersToggled = false;
    }

    /**
     * This method is used by AbstractModelItem implementors to trigger the listeners from this RootModelItem.
     * 
     * @param event
     */
    protected void triggerListeners(ModelItemEvent event) {
        if (listenersToggled) {
            IModelItemListener listener = null;
            for (Iterator it = listeners.iterator(); it.hasNext();) {
                listener = (IModelItemListener) it.next();
                listener.changeOccured(event);
            }
        }
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#addChild(ec.ep.dit.isp.foundry.model.IModelItem)
     */
    public void addChild(IModelItem childModelItem) {
        triggerListeners(new ModelItemEvent(this, childModelItem, ModelItemEvent.PRE_ADD_CHILD));
        childs.put(childModelItem.getUID(), childModelItem);
        if (childModelItem instanceof AbstractModelItem) {
            ((AbstractModelItem) childModelItem).setParent(this);
        }
        triggerListeners(new ModelItemEvent(this, childModelItem, ModelItemEvent.POST_ADD_CHILD));
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#removeChild(java.lang.String)
     */
    public void removeChild(String UID) {
        IModelItem childModelItem = (IModelItem) getChild(UID);
        triggerListeners(new ModelItemEvent(this, childModelItem, ModelItemEvent.PRE_REMOVE_CHILD));
        childs.remove(UID);
        triggerListeners(new ModelItemEvent(this, childModelItem, ModelItemEvent.POST_REMOVE_CHILD));
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#clearChildren()
     */
    public void clearChildren() {
        for (Iterator it = childs.keySet().iterator(); it.hasNext();) {
            removeChild((String) it.next());
        }
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getUID()
     */
    public String getUID() {
        return UID;
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getChildren()
     */
    public List getChildren() {
        return new ArrayList(childs.values());
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getParent()
     */
    public IModelItem getParent() {
        return null;
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#hasChildren()
     */
    public boolean hasChildren() {
        return !childs.isEmpty();
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#hasChild(java.lang.String)
     */
    public boolean hasChild(String UID) {
        return childs.containsKey(UID);
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#getChild(java.lang.String)
     */
    public IModelItem getChild(String UID) {
        return (IModelItem) childs.get(UID);
    }

    /**
     * This method returns a description of this RootModelItem instance.
     * 
     * @return a StringBuffer containing the description
     */
    public StringBuffer toStringBufferDescription() {
        return new StringBuffer(UID);
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(toStringBufferDescription());
        for (Iterator it = childs.keySet().iterator(); it.hasNext();) {
            buffer.append(it.next().toString());
        }
        return buffer.toString();
    }

    /**
     * @see ec.ep.dit.isp.foundry.model.IModelItem#accept(ec.ep.dit.isp.foundry.model.IModelItemVisitor)
     */
    public void accept(IModelItemVisitor visitor) {
        if (visitor.visit(this)) {
            for (Iterator it = getChildren().iterator(); it.hasNext();) {
                ((IModelItem) it.next()).accept(visitor);
            }
        }
    }

}
