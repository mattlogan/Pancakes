package me.mattlogan.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import static me.mattlogan.library.Preconditions.checkNotNull;
import static me.mattlogan.library.Preconditions.checkStringNotEmpty;

/**
 * This manages a navigation stack by representing each item in the stack as a layout id, which is
 * responsible for View creation. All standard Java Stack operations are supported, with additional
 * methods for pushing and popping with animated transitions.
 */
public final class ViewStack {

    public static final int SINGLE_VIEW = -1;
    private final ParcelableIntStack stack = new ParcelableIntStack();
    private final ViewGroup container;
    private final ViewStackDelegate delegate;
    private final List<StackChangedListener> listeners = new ArrayList<>();
    private final PancakesViewInflator pancakesViewInflator;

    /**
     * Static creation method for ViewStack instances
     *
     * @param container Any {@link ViewGroup} container for navigation Views. Typically a FrameLayout
     * @param delegate  A {@link ViewStackDelegate} responsible for "finishing" the navigation stack
     * @return A new ViewStack instance
     */
    public static ViewStack create(ViewGroup container, ViewStackDelegate delegate) {
        return create(container, delegate, new PancakesViewInflator() {
            @Override
            public View inflateView(@LayoutRes int layoutResource, ViewGroup container) {
                return LayoutInflater.from(container.getContext()).inflate(layoutResource, container, false);
            }
        });
    }

    /**
     * Package-private factory method which allows us to provide a custom
     * {@link PancakesViewInflator} for testing.
     *
     * @param container Any {@link ViewGroup} container for navigation Views.  Typically a FrameLayout
     * @param delegate A {@link ViewStackDelegate} responsible for "finishing" the navigation stack
     * @param pancakesViewInflator A {@link PancakesViewInflator} which is responsible for inflating
     *                             layout resources into the container.
     *
     * @return A new ViewStack instance
     */
    static ViewStack create(ViewGroup container, ViewStackDelegate delegate, PancakesViewInflator pancakesViewInflator) {
        checkNotNull(container, "container == null");
        checkNotNull(delegate, "delegate == null");
        return new ViewStack(container, delegate, pancakesViewInflator);
    }

    private ViewStack(ViewGroup container, ViewStackDelegate delegate, PancakesViewInflator inflator) {
        this.container = container;
        this.delegate = delegate;
        this.pancakesViewInflator = inflator;
    }

    /**
     * Saves the ViewStack state (an order list of view ids) to the provided Bundle using
     * the provided tag
     *
     * @param bundle The Bundle in which to save the serialized Stack of view ids
     * @param tag    The tag, or "bundle key," for the stored data
     */
    public void saveToBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        bundle.putParcelable(tag, stack);
    }

    /**
     * Resets the navigation stack state to what it was when saveToBundle() was called.
     *
     * @param bundle A bundle containing saved ViewStack state
     * @param tag    The tag, or key, for which the ViewStack state was saved
     */
    @SuppressWarnings("unchecked")
    public void rebuildFromBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        // TODO: do the things!
        ParcelableIntStack savedStack = bundle.getParcelable(tag);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (Integer layoutResource : savedStack) {
            pushWithoutNotifyingListeners(layoutResource);
        }
        notifyListeners();
    }

    /**
     * Pushes a View id onto the navigation stack and inflates it in the container
     *
     * @param layoutId The layout id to inflate into the parent {@link ViewGroup}.
     * @return the provided layout id (to comply with the Java Stack API)
     */
    public int push(@LayoutRes int layoutId) {
        pushWithoutNotifyingListeners(layoutId);
        notifyListeners();
        return layoutId;
    }

    private void pushWithoutNotifyingListeners(@LayoutRes int layoutId) {
        stack.push(layoutId);
        View view = pancakesViewInflator.inflateView(layoutId, container);
        container.addView(view);
        setBelowViewVisibility(View.GONE);
    }

    /**
     * Pops the top View off the navigation stack
     *
     * @return The id of the layout on the top of the stack, or SINGLE_VIEW if the stack is to
     * be finished.
     */
    public int pop() {
        if (!shouldPop()) return SINGLE_VIEW;
        int layoutId = stack.pop();
        setBelowViewVisibility(View.VISIBLE);
        container.removeView(peekView());
        notifyListeners();
        return layoutId;
    }

    /**
     * Pushes a View, created with the provided layout id, onto the navigation stack and animates
     * it using the Animator created by the provided {@link AnimatorFactory}
     *
     * @param layoutId     The id of the view to be added to the top of the navigation stack
     * @param animatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto the navigation stack
     * @return the provided layout id (to comply with the Java Stack API)
     */
    public int pushWithAnimation(@LayoutRes int layoutId,
                                         final AnimatorFactory animatorFactory) {
        checkNotNull(animatorFactory, "animatorFactory == null");
        stack.push(layoutId);
        View view = pancakesViewInflator.inflateView(layoutId, container);
        container.addView(view);
        notifyListeners();
        view.getViewTreeObserver().addOnGlobalLayoutListener(new FirstLayoutListener(view) {
            @Override
            public void onFirstLayout(View view) {
                // We have to wait until the View's first layout pass to start the animation,
                // otherwise the view's width and height would be zero.
                startAnimation(animatorFactory, view, pushAnimatorListener);
            }
        });
        return layoutId;
    }

    /**
     * Pops the top View off the navigation stack and animates it using the Animator created by the
     * provided AnimatorFactory
     *
     * @param animatorFactory responsible for the creation of an Animator to animate the current
     *                        View off the navigation stack
     * @return the layout id that was used for the creation of the top View on the
     * navigation stack
     */
    public int popWithAnimation(AnimatorFactory animatorFactory) {
        checkNotNull(animatorFactory, "animatorFactory == null");
        if (!shouldPop()) return SINGLE_VIEW;
        int popped = stack.pop();
        setBelowViewVisibility(View.VISIBLE);
        startAnimation(animatorFactory, peekView(), popAnimationListener);
        return popped;
    }

    /**
     * @return the layout id for the top view on the view stack
     */
    public int peek() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return stack.peek();
    }

    /**
     * @return the View child at the top of the navigation stack
     */
    public View peekView() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return container.getChildAt(container.getChildCount() - 1);
    }

    /**
     * @return the size of the navigation stack
     */
    public int size() {
        return stack.size();
    }

    /**
     * Clears the navigation stack and removes all Views from the provided ViewGroup container
     */
    public void clear() {
        stack.clear();
        container.removeAllViews();
        notifyListeners();
    }

    /**
     * Adds a StackChangedListener for stack-changed events
     *
     * @param listener A StackChangedListener
     * @return always true
     */
    public boolean addStackChangedListener(StackChangedListener listener) {
        return listeners.add(listener);
    }

    /**
     * Removes the supplied StackChangedListener
     *
     * @param listener The StackChangedListener to remove
     * @return true if the StackChangedListener was actually removed
     */
    public boolean removeStackChangedListener(StackChangedListener listener) {
        return listeners.remove(listener);
    }

    /**
     * Removes all StackChangedListeners
     */
    public void clearStackChangedListeners() {
        listeners.clear();
    }

    private Animator.AnimatorListener pushAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            setBelowViewVisibility(View.GONE);
        }
    };

    private Animator.AnimatorListener popAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            container.removeView(peekView());
            notifyListeners();
        }
    };

    private void setBelowViewVisibility(int visibility) {
        if (container.getChildCount() > 1) {
            container.getChildAt(container.getChildCount() - 2).setVisibility(visibility);
        }
    }

    private void startAnimation(AnimatorFactory animatorFactory, View view,
                                Animator.AnimatorListener listener) {
        Animator animator = animatorFactory.createAnimator(view);
        animator.addListener(listener);
        animator.start();
    }

    private boolean shouldPop() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        if (size() == 1) {
            delegate.finishStack();
            return false;
        }
        return true;
    }

    private void notifyListeners() {
        for (StackChangedListener listener : listeners) {
            listener.onStackChanged();
        }
    }
}
