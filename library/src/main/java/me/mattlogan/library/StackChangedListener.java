package me.mattlogan.library;

import android.view.View;

/**
 * Listener interface for stack-changed events
 */
public interface StackChangedListener {
    /**
     * Called when a View is added to the container
     *
     * @param view The added View
     */
    void onViewAdded(View view);

    /**
     * Called when a View is removed from the container
     */
    void onViewRemoved();
}
