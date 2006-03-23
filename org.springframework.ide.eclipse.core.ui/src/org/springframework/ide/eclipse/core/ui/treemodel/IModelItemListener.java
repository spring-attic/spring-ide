package org.springframework.ide.eclipse.core.ui.treemodel;

/**
 * This interface describes what a listener for the model's events has to implement.
 * @author pagregoire
 */
public interface IModelItemListener {
    
    /**
     * This method will be called is this listener is registered to the RootModelItem, when an Event is triggered on any model item.
     * @param modelItemEvent the event that has been propagated.
     */
    public void changeOccured(ModelItemEvent modelItemEvent);
}
