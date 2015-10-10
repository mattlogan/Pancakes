package me.mattlogan.library;

/**
 * A listener used to know when the view stack has changed. (Push or Pop)
 * Similar to Android's FragmentManager.BackStackChangedListener.
 * Note: onViewStackChanged() gets called after animations finish.
 */
public interface ViewStackChangedListener {
    void onViewStackChanged();
}
