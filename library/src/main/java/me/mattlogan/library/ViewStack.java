package me.mattlogan.library;

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

        View top = peekView();
        if (top != null && top instanceof StatefulView) {
            ((StatefulView) top).saveState(bundle);
        }
    }

    public void rebuildFromBundle(Bundle bundle, String tag) {
        checkNotNull(bundle, "bundle == null");
        checkStringNotEmpty(tag, "tag is empty");
        @SuppressWarnings("unchecked")
        Stack<ViewFactory> savedStack = (Stack<ViewFactory>) bundle.getSerializable(tag);
        checkNotNull(savedStack, "Bundle doesn't contain any ViewStack state.");
        for (ViewFactory viewFactory : savedStack) {
            stack.push(viewFactory);
        }
        updateContainer();

        View top = peekView();
        if (top != null && top instanceof StatefulView) {
            ((StatefulView) top).recreateState(bundle);
        }
    }

    public ViewFactory push(ViewFactory viewFactory) {
        checkNotNull(viewFactory, "viewFactory == null");
        stack.push(viewFactory);
        updateContainer();
        return viewFactory;
    }

    public ViewFactory pop() {
        if (size() == 0) {
            throw new EmptyStackException();
        }
        if (size() == 1) {
            delegate.finishStack();
            return null;
        }
        ViewFactory next = stack.pop();
        updateContainer();
        return next;
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
        return container.getChildAt(0);
    }

    public int size() {
        return stack.size();
    }

    public void clear() {
        stack.clear();
        updateContainer();
    }

    private void updateContainer() {
        container.removeAllViews();
        if (stack.size() > 0) {
            container.addView(stack.peek().createView(container.getContext(), container));
        }
    }
}
