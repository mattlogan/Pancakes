package me.mattlogan.library;

/**
 * Listener interface for stack-changed events
 */
public interface StackChangedListener {
    /**
     * Called when BOTH the ViewStack's size AND the top View in the ViewGroup container have
     * changed. For a push with an animation, this happens before the animation starts (right after
     * the new View is added to the container). For a pop with an animation, this happens after the
     * animation completes (right after the old view is removed from the container).
     */
    void onStackChanged();
}
