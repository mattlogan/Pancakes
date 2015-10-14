package me.mattlogan.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

import static me.mattlogan.library.Preconditions.checkNotNull;
import static me.mattlogan.library.Preconditions.checkStringNotEmpty;

/**
 * This manages a navigation stack by representing each item in the stack as a ViewFactory, which is
 * responsible for View creation. All standard Java Stack operations are supported, with additional
 * methods for pushing and popping with animated transitions.
 */
public final class ViewStack {

    private final Stack<ViewFactory> stack = new Stack<>();
    private final ViewGroup container;
    private final ViewStackDelegate delegate;
    private final List<StackChangedListener> listeners = new ArrayList<>();

    /**
     * Static creation method for ViewStack instances
     *
     * @param container Any ViewGroup container for navigation Views. Typically a FrameLayout
     * @param delegate  A ViewStackDelegate responsible for "finishing" the navigation stack
     * @return A new ViewStack instance
     */
    public static ViewStack create(ViewGroup container, ViewStackDelegate delegate) {
        checkNotNull(container, "container == null");
        checkNotNull(delegate, "delegate == null");
        return new ViewStack(container, delegate);
    }

    private ViewStack(ViewGroup container, ViewStackDelegate delegate) {
        this.container = container;
        this.delegate = delegate;
    }

    /**
     * Saves the ViewStack state (an ordered stack of ViewFactories) to the provided Bundle using
     * the provided tag
     *
     * @param bundle The Bundle in which to save the serialized Stack of ViewFactories
     * @param tag    The tag, or "bundle key," for the stored data
     */
    public void saveToBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        bundle.putSerializable(tag, stack);
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
        Stack<ViewFactory> savedStack = (Stack<ViewFactory>) bundle.getSerializable(tag);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (ViewFactory viewFactory : savedStack) {
            checkNotNull(viewFactory, "viewFactory == null");
            pushWithoutNotifyingListeners(viewFactory);
        }
        notifyListeners();
    }

    /**
     * Pushes a View, created by the provided ViewFactory, onto the navigation stack
     *
     * @param viewFactory responsible for the creation of the next View in the navigation stack
     * @return the provided ViewFactory (to comply with the Java Stack API)
     */
    public ViewFactory push(ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        pushWithoutNotifyingListeners(viewFactory);
        notifyListeners();
        return viewFactory;
    }

    private void pushWithoutNotifyingListeners(ViewFactory viewFactory) {
        stack.push(viewFactory);
        View view = viewFactory.createView(container.getContext(), container);
        container.addView(view);
        setBelowViewVisibility(View.GONE);
    }

    /**
     * Pushes a View, created by the provided ViewFactory, onto the navigation stack and animates
     * it using the Animator created by the provided AnimatorFactory
     *
     * @param viewFactory     responsible for the creation of the next View in the navigation stack
     * @param animatorFactory responsible for the creation of an Animator to animate the next View
     *                        onto the navigation stack
     * @return the provided ViewFactory (to comply with the Java Stack API)
     */
    public ViewFactory pushWithAnimation(ViewFactory viewFactory,
                                         final AnimatorFactory animatorFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        checkNotNull(animatorFactory, "animatorFactory == null");
        stack.push(viewFactory);
        View view = viewFactory.createView(container.getContext(), container);
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
        return viewFactory;
    }

    /**
     * Pops the top View off the navigation stack
     *
     * @return the ViewFactory instance that was used for the creation of the top View on the
     * navigation stack
     */
    public ViewFactory pop() {
        if (!shouldPop()) return null;
        ViewFactory popped = stack.pop();
        setBelowViewVisibility(View.VISIBLE);
        container.removeView(peekView());
        notifyListeners();
        return popped;
    }

    /**
     * Pops the top View off the navigation stack and animates it using the Animator created by the
     * provided AnimatorFactory
     *
     * @param animatorFactory responsible for the creation of an Animator to animate the current
     *                        View off the navigation stack
     * @return the ViewFactory instance that was used for the creation of the top View on the
     * navigation stack
     */
    public ViewFactory popWithAnimation(AnimatorFactory animatorFactory) {
        checkNotNull(animatorFactory, "animatorFactory == null");
        if (!shouldPop()) return null;
        ViewFactory popped = stack.pop();
        setBelowViewVisibility(View.VISIBLE);
        startAnimation(animatorFactory, peekView(), popAnimationListener);
        return popped;
    }

    /**
     * @return the ViewFactory responsible for creating the top View on the navigation stack
     */
    public ViewFactory peek() {
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
