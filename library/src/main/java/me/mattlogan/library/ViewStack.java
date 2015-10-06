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

public final class ViewStack {

    private final Stack<ViewFactory> stack = new Stack<>();
    private final ViewGroup container;
    private final ViewStackDelegate delegate;

    public static ViewStack create(ViewGroup container, ViewStackDelegate delegate) {
        checkNotNull(container, "container == null");
        checkNotNull(delegate, "delegate == null");
        return new ViewStack(container, delegate);
    }

    private ViewStack(ViewGroup container, ViewStackDelegate delegate) {
        this.container = container;
        this.delegate = delegate;
    }

    public void saveToBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        bundle.putSerializable(tag, stack);
    }

    @SuppressWarnings("unchecked")
    public void rebuildFromBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        Stack<ViewFactory> savedStack = (Stack<ViewFactory>) bundle.getSerializable(tag);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (ViewFactory viewFactory : savedStack) {
            push(viewFactory);
        }
    }

    public ViewFactory push(ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        return pushWithAnimation(viewFactory, AnimatorFactory.NONE);
    }

    public ViewFactory pushWithAnimation(ViewFactory viewFactory, final AnimatorFactory animatorFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        checkNotNull(animatorFactory, "animatorFactory == null");
        stack.push(viewFactory);
        View view = viewFactory.createView(container.getContext(), container);
        container.addView(view);
        view.getViewTreeObserver().addOnGlobalLayoutListener(new FirstLayoutListener(view) {
            @Override
            public void onFirstLayout(View view) {
                Animator animator = animatorFactory.createAnimator(view);
                animator.addListener(pushAnimatorListener);
                animator.start();
            }
        });
        return viewFactory;
    }

    public ViewFactory pop() {
        return popWithAnimation(AnimatorFactory.NONE);
    }

    public ViewFactory popWithAnimation(AnimatorFactory animatorFactory) {
        checkNotNull(animatorFactory, "animatorFactory == null");
        if (size() == 0) {
            throw new EmptyStackException();
        }
        if (size() == 1) {
            delegate.finishStack();
            return null;
        }
        ViewFactory popped = stack.pop();
        container.getChildAt(container.getChildCount() - 2).setVisibility(View.VISIBLE);
        Animator animator = animatorFactory.createAnimator(peekView());
        animator.addListener(popAnimationListener);
        animator.start();
        return popped;
    }

    public ViewFactory peek() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return stack.peek();
    }

    public View peekView() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        return container.getChildAt(container.getChildCount() - 1);
    }

    public int size() {
        return stack.size();
    }

    public void clear() {
        stack.clear();
        container.removeAllViews();
    }

    private Animator.AnimatorListener pushAnimatorListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            if (container.getChildCount() > 1) {
                container.getChildAt(container.getChildCount() - 2).setVisibility(View.GONE);
            }
        }
    };

    private Animator.AnimatorListener popAnimationListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animator) {
            container.removeView(peekView());
        }
    };
}
