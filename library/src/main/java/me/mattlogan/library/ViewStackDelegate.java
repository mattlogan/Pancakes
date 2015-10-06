package me.mattlogan.library;

/**
 * Interface for "finishing" a navigation stack. The intended implementation is calling finish()
 * in the host Activity.
 */
public interface ViewStackDelegate {
    void finishStack();
}
