package me.mattlogan.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import java.util.EmptyStackException;
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
    private ViewStackChangedListener changedListener;

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

    public void addViewStackChangedListener(ViewStackChangedListener listener) {
        //Not checking null so the listener can be removed by setting null.
        this.changedListener = listener;
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
            push(viewFactory, true);
        }
    }

    /**
     * Pushes a View, created by the provided ViewFactory, onto the navigation stack
     *
     * @param viewFactory responsible for the creation of the next View in the navigation stac
     * @param skipChangeListener flag to skip sending any updates to the view stack change listener
     *                           when rebuilding the view stack after rotation
     * @return the provided ViewFactory (to comply with the Java Stack API)
     */
    private ViewFactory push(ViewFactory viewFactory, boolean skipChangeListener) {
        checkNotNull(viewFactory, "viewFactory == null");
        stack.push(viewFactory);
        View view = viewFactory.createView(container.getContext(), container);
        container.addView(view);
        setBelowViewVisibility(View.GONE);
        if(!skipChangeListener && changedListener != null) {
            changedListener.onViewStackChanged();
        }
        return viewFactory;
    }

    /**
     * Pushes a View, created by the provided ViewFactory, onto the navigation stack
     *
     * @param viewFactory responsible for the creation of the next View in the navigation stack
     * @return the provided ViewFactory (to comply with the Java Stack API)
     */
    public ViewFactory push(ViewFactory viewFactory) {
        return push(viewFactory, false);
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
        if(changedListener != null) {
            changedListener.onViewStackChanged();
        }
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
    }

    private Animator.AnimatorListener pushAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            setBelowViewVisibility(View.GONE);
            if(changedListener != null) {
                changedListener.onViewStackChanged();
            }
        }
    };

    private Animator.AnimatorListener popAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            container.removeView(peekView());
            if(changedListener != null) {
                changedListener.onViewStackChanged();
            }
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
}
