package me.mattlogan.library;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.Stack;

public final class ViewStack {

    private static final String STACK_TAG = "stack";

    private final Stack<ViewFactory> stack = new Stack<>();
    private final ViewGroup container;
    private final ViewStackDelegate delegate;

    public static ViewStack create(ViewGroup container) {
        return new ViewStack(container, ViewStackDelegate.DEFAULT);
    }

    public static ViewStack create(ViewGroup container, ViewStackDelegate delegate) {
        return new ViewStack(container, delegate);
    }

    private ViewStack(ViewGroup container, ViewStackDelegate delegate) {
        this.container = container;
        this.delegate = delegate;
    }

    public void push(ViewFactory viewFactory) {
        if (!delegate.shouldUpdateViewStack(size(), size() + 1)) return;
        stack.push(viewFactory);
        updateContainer();
        delegate.onViewStackUpdated(size());
    }

    @SuppressWarnings("all")
    public void rebuildFromBundle(Bundle bundle) {
        Stack<ViewFactory> savedStack = (Stack<ViewFactory>) bundle.getSerializable(STACK_TAG);
        for (ViewFactory viewFactory : savedStack) {
            if (delegate.shouldUpdateViewStack(size(), size() + 1)) {
                stack.push(viewFactory);
            }
        }
        updateContainer();
        delegate.onViewStackUpdated(size());
    }

    public void pop() {
        if (!delegate.shouldUpdateViewStack(size(), size() - 1)) return;
        stack.pop();
        updateContainer();
        delegate.onViewStackUpdated(size());
    }

    public int size() {
        return stack.size();
    }

    private void updateContainer() {
        container.removeAllViews();
        if (stack.size() > 0) {
            container.addView(stack.peek().createView(container.getContext()));
        }
    }

    public void saveToBundle(Bundle bundle) {
        bundle.putSerializable(STACK_TAG, stack);
    }
}
