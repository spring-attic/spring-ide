package org.springframework.ide.eclipse.core.ui.treemodel;

/**
 * This class defines a Model Item Event triggered by some action against the model.
 * @author pagregoire
 */
public class ModelItemEvent {
    /**
     * the item that the event originated from.
     */
    private IModelItem sourceItem;
    /**
     * an item directly concerned by this event.
     */
    private IModelItem targetItem;
    /**
     * the eventType taken from the constants in this class.
     */
    private int eventType;
    /**
     * This kind of event is triggered when a property of a model item is changed.
     */
    public static final int ITEM_PROPERTY_CHANGED = 1 << 1;
    /**
     * This kind of event is triggered just before a child is added to this model item.
     */
    public static final int PRE_ADD_CHILD = 1 << 2;
    /**
     * This kind of event is triggered just after a child is added to this model item.
     */
    public static final int POST_ADD_CHILD = 1 << 3;
    /**
     * This kind of event is triggered just before a child is removed from this model item.
     */
    public static final int PRE_REMOVE_CHILD = 1 << 4;
    /**
     * This kind of event is triggered just after a child is removed from this model item.
     */
    public static final int POST_REMOVE_CHILD = 1 << 5;

    /**
     * This Constructor creates an event specifying its source, target and type.
     * @param sourceItem
     * @param targetItem
     * @param eventType
     */
    public ModelItemEvent(IModelItem sourceItem,IModelItem targetItem,int eventType) {
        this.sourceItem=sourceItem;
        this.targetItem=targetItem;
        this.eventType=eventType;
    }
    
    /**
     * This method returns the event type of this event.
     * @return an integer value taken from the constants of this class.
     */
    public int getEventType() {
        return eventType;
    }
    
    /** 
     * This method returns the model item that originated this event.  
     * @return
     */
    public IModelItem getSourceItem() {
        return sourceItem;
    }
    /**
     * This method another affected item.
     * @return
     */
    public IModelItem getTargetItem() {
        return targetItem;
    }
}
