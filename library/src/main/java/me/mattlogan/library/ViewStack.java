package me.mattlogan.library;

import android.os.Bundle;
import android.view.ViewGroup;

import java.util.Stack;

import static me.mattlogan.library.Preconditions.checkNotNull;

public final class ViewStack {

    private static final String STACK_TAG = "stack";

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

    public ViewFactory push(ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        stack.push(viewFactory);
        updateContainer();
        return viewFactory;
    }

    public void rebuildFromBundle(Bundle bundle) {
        @SuppressWarnings("unchecked")
        Stack<ViewFactory> savedStack = (Stack<ViewFactory>) bundle.getSerializable(STACK_TAG);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (ViewFactory viewFactory : savedStack) {
            stack.push(viewFactory);
        }
        updateContainer();
    }

    public ViewFactory pop() {
        ViewFactory next = stack.pop();
        if (size() == 0) {
            delegate.onFinishStack();
        } else {
            updateContainer();
        }
        return next;
    }

    public ViewFactory peek() {
        return stack.peek();
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
        checkNotNull(bundle, "bundle == null");
        bundle.putSerializable(STACK_TAG, stack);
    }
}
